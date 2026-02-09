# NFe-Processing-Service-API

Sistema para processamento de dados para emissão de NF-e com TDD, SRP e regra de negócio aplicada

Rascunho:
✅ Criar Geração de XML
✅ Mockar SEFAZ com integracao Prod-Ready Assíncrona (email de pedido recebido)
✅ Gerar Danfe
✅ Enviar DANFE por email (Nfe Emitida)
✅ Dockeirização
✅ Migrar para PostgreSQL
✅ Testes Durante a implementação
✅ Criação de Front-end Angular com "Compra" simples que consuma a API.

Diferencial: processamento assincrono, integracao com API SEFAZ real, envio por email e desenvolvimento com testes e SRP

## SEFAZ
 Ambiente de Homologação - versão 4.00:

- Autorização:
https://homologacao.nfe.sefa.pr.gov.br/nfe/NFeAutorizacao4?wsdl

- Consulta Recibo:
https://homologacao.nfe.sefa.pr.gov.br/nfe/NFeRetAutorizacao4?wsdl

- Consulta Chave Acesso:
https://homologacao.nfe.sefa.pr.gov.br/nfe/NFeConsultaProtocolo4?wsdl

- Inutilização:
https://homologacao.nfe.sefa.pr.gov.br/nfe/NFeInutilizacao4?wsdl

- Consulta Status do Serviço:
https://homologacao.nfe.sefa.pr.gov.br/nfe/NFeStatusServico4?wsdl

- Consulta a Cadastro:
https://homologacao.nfe.sefa.pr.gov.br/nfe/CadConsultaCadastro4?wsdl

- Registro de Eventos:
https://homologacao.nfe.sefa.pr.gov.br/nfe/NFeRecepcaoEvento4?wsdl

##  Autenticação

**Autenticação Técnica (API):**
- E-commerce se autentica via API Key ou Bearer Token
- Garante que apenas sistemas autorizados solicitem emissões

**Identificação Fiscal (Payload):**
- CPF/CNPJ do destinatário: dado fiscal enviado no `POST /invoices`

**Autenticação Fiscal (SEFAZ):**
- Feita via certificado digital da empresa emitente
- SEFAZ valida CNPJ, certificado e dados da nota
- Consumidor final não se autentica no sistema

## 🏢 Arquitetura Simples Banco

**Modelo:** Cada empresa com suas invoices ligadas por fk

```
┌──────────────────────────┐
│  invoices (compartilhada)│  ← Uma tabela para todas as empresas
│  - id, supplier_id       │     Isolamento por supplier_id
│  - customer_cpf_cnpj     │
│  - total_amount, status  │
└──────────────────────────┘
           ↑
           │ (FK: invoice_id)
           │
┌──────────────────────────┐
│  invoice_items           │
│  - id, invoice_id        │
│  - description, qty      │
└──────────────────────────┘
```

**Como funciona:**
- Uma única tabela `invoices` para todas as empresas
- Isolamento feito via coluna `supplier_id` (FK para `suppliers`)

## Status da Invoice

- `RECEIVED` - Recebida
- `PROCESSING` - Em processamento
- `COMPLETED` - Concluída
- `ERROR` - Erro no processamento

## 🔄 Fluxo Simples

```
E-commerce (autenticado) → POST /invoices (supplier_id + dados venda + destinatário)
    ↓
Validações (supplier ativo, CPF/CNPJ, valores, itens, UF/CEP, duplicação...)
    ↓
Enriquecimento (gera série, número, trackingId, timestamps)
    ↓
Persistência (suppliers → invoices → invoice_items com status RECEIVED)
    ↓
Kafka Event (processamento assíncrono)
    ↓
Retorna 202 Accepted (id + trackingId)
```

## 🔄Fluxo Técnico (Camadas)

```
InvoiceResource → InvoiceRequestDTO
   ↓
InvoiceService
   ├── Valida Supplier (SupplierService)
   ├── Valida Dados (InvoiceValidator + CnpjValidator)
   ├── Enriquece (série, número, trackingId)
   ├── Persiste (InvoiceRepository) → status: RECEIVED
   └── Publica Evento (Kafka)
   ↓
InvoiceResponseDTO → HTTP 202 Accepted

[Assíncrono - Kafka Consumer]
InvoiceProcessor
   ├── Status → PROCESSING
   ├── Aplica regras de negócio
   └── Status → COMPLETED/ERROR
```

## 🔄 Fluxo Principal Completo

```
1. E-commerce autentica (API Key/Bearer Token)
    ↓
2. POST /invoices (supplier_id, customer, items, total)
    ↓
3. Validações (15 regras - ver seção Validações)
    ↓
4. Enriquecimento (série, número, trackingId)
    ↓
5. Persiste: Invoice + InvoiceItems (status: RECEIVED)
    ↓
6. Publica Kafka Event
    ↓
7. Retorna 202 Accepted (id, trackingId, status)

[PROCESSAMENTO ASSÍNCRONO]
    ↓
8. Kafka Consumer → InvoiceProcessor
    ↓
9. Status: RECEIVED → PROCESSING → COMPLETED/ERROR
    ↓
10. Notifica cliente (email com NFe)
```

**Consulta:**
- `GET /invoices/tracking/{trackingId}` → status da invoice

## 🔍 Validações Principais (v0.1)

### 📋 Validações Básicas de Dados
- [ ] CPF/CNPJ do destinatário deve ser válido (dígitos verificadores, rejeitar zerados/sequenciais)
- [ ] CNPJ do emitente deve estar cadastrado e ativo na tabela Suppliers
- [ ] Dados obrigatórios do destinatário: nome completo, endereço completo (CEP, rua, número, cidade, UF), email

### 💰 Validações de Valores
- [ ] Valores não podem ser negativos ou zero (quantidade, valor unitário, total)
- [ ] Total da nota deve bater com soma dos itens: Soma(item.qty × item.unitPrice) = invoice.totalAmount
- [ ] Rejeitar notas acima de valor máximo configurável

### 🛒 Validações de Itens
- [ ] Nota deve ter pelo menos 1 item
- [ ] Limite máximo de itens por nota (configurável)
- [ ] Descrição do produto obrigatória (mínimo 3 caracteres, máximo 120 caracteres)

### 📍 Validações Geográficas
- [ ] UF do destinatário deve ser válida (UFs brasileiras)
- [ ] CEP do destinatário deve ter formato válido (00000-000 ou 00000000)

### ⏰ Validações Temporais
- [ ] Data de emissão não pode ser futura
- [ ] Limite de tentativas de reprocessamento (máximo 3 tentativas, após isso status ERROR permanente)

### 🔒 Validações de Negócio
- [ ] Não permitir duplicação de notas (CNPJ emitente + CPF/CNPJ destinatário + valor total + data)
- [ ] Série e número da nota gerados automaticamente (série numérica, número sequencial por série)

## 📡 Endpoints REST

### Invoices
- `POST /invoices` - Criar invoice
- `GET /invoices/{id}` - Buscar por ID
- `GET /invoices/tracking/{trackingId}` - Buscar por tracking
- `GET /invoices` - Listar com filtros
- `PATCH /invoices/{id}/status` - Atualizar status
- `GET /invoices/stats` - Estatísticas

## 📁 Estrutura do Projeto

```
nfe-processing-service
└── src
    ├── main
    │   ├── java/br/com/nfe
    │   │   ├── resource/              # Endpoints da API REST
    │   │   │   ├── InvoiceResource.java
    │   │   │   └── SupplierResource.java
    │   │   │
    │   │   ├── service/               # Regras de negócio
    │   │   │   ├── InvoiceService.java
    │   │   │   ├── InvoiceProcessor.java
    │   │   │   └── SupplierService.java
    │   │   │
    │   │   ├── validator/             # Validações
    │   │   │   ├── InvoiceValidator.java
    │   │   │   └── CnpjValidator.java
    │   │   │
    │   │   ├── dto/                   # Dados de entrada/saída
    │   │   │   ├── InvoiceRequestDTO.java
    │   │   │   ├── InvoiceResponseDTO.java
    │   │   │   └── SupplierDTO.java
    │   │   │
    │   │   ├── entity/                # Tabelas do banco
    │   │   │   ├── Invoice.java
    │   │   │   ├── InvoiceItem.java
    │   │   │   └── Supplier.java
    │   │   │
    │   │   ├── repository/            # Acesso aos dados
    │   │   │   ├── InvoiceRepository.java
    │   │   │   └── SupplierRepository.java
    │   │   │
    │   │   ├── event/                 # Kafka (produtor/consumidor)
    │   │   │   ├── InvoiceEvent.java
    │   │   │   ├── InvoiceEventProducer.java
    │   │   │   └── InvoiceEventConsumer.java
    │   │   │
    │   │   └── exception/             # Tratamento de erros
    │   │       ├── InvoiceNotFoundException.java
    │   │       ├── InvalidInvoiceException.java
    │   │       └── GlobalExceptionHandler.java
    │   │
    │   └── resources
    │       ├── application.properties
    │       └── db/migration
    │           └── V1__create_tables.sql
    │
    └── test/java/br/com/nfe
        ├── service/
        │   ├── InvoiceServiceTest.java
        │   └── InvoiceProcessorTest.java
        ├── validator/
        │   └── CnpjValidatorTest.java
        └── resource/
            └── InvoiceResourceIT.java
```

### 🎯 O que cada camada faz

- **resource/** → Recebe requisições HTTP
- **service/** → Processa a lógica de negócio
- **validator/** → Valida CNPJ, valores, datas
- **dto/** → Transfere dados entre camadas
- **entity/** → Representa tabelas no banco
- **repository/** → Salva e busca dados
- **event/** → Publica/consome mensagens
- **exception/** → Trata erros de forma centralizada

```
Cliente envia JSON → Resource → Service → Validações → 
Salva no Banco → Publica no Kafka → Processa Assincronamente
```

### 📝 Convenções de nomenclatura

- DTOs terminam com `DTO` (ex: `InvoiceRequestDTO`)
- Resources terminam com `Resource` (ex: `InvoiceResource`)
- Services terminam com `Service` (ex: `InvoiceService`)
- Entities usam nome da entidade (ex: `Invoice`)
- Testes terminam com `Test` ou `IT` (ex: `InvoiceServiceTest.java`)

## 🎯 Fases de Implementação

| Fase | Descrição |
|------|-----------|
| **Fase 1** | Modelagem Banco
| **Fase 2** | Testes para cada regra
| **Fase 3** | Red Phase
| **Fase 4** | Green phase
| **Fase 5** | Refactor

## RDoc. ´How to run´ padrão (provisório)
You can run your application in dev mode that enables live coding using:

```shell script
mvn quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/NFe-Processing-Service-API-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.


## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)