# NFe Processing Service API

![Java](https://img.shields.io/badge/java_21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Quarkus](https://img.shields.io/badge/quarkus-%234794EB.svg?style=for-the-badge&logo=quarkus&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue.svg?style=for-the-badge)

> **API REST para processamento de Notas Fiscais Eletrônicas (NFe)** com integração à biblioteca Java-NFe.
> 
> Arquitetura com separação de responsabilidades, configuração multi-ambiente, geração e validação XML contra .xsd oficial, montagem automática de DANFE com envio por e-mail e armazenamento do XML no Amazon S3.

---

## 🔗 Principais Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/nfe/create` | Emitir NFe (gera XML, valida, cria DANFE e envia email) |
| GET | `/nfe/list` | Listar todas as NFes |
| GET | `/nfe/tracking/{trackingId}` | Buscar NFe por código de rastreamento |
| GET | `/nfe/tracking/{trackingId}/xml` | Buscar XML da NFe por código de rastreamento |


> 📖 **Swagger UI:** http://localhost:8080/q/swagger-ui

---

## Fluxo da Aplicação

```
POST /nfe/create
    │
    ├─ Gera XML NFe
    ├─ Valida contra XSD (Receita Federal)
    ├─ Assina XML (mock em test / A1 em prod)
    ├─ Gera DANFE em PDF (JasperReports)
    ├─ Envia email com DANFE anexado
    ├─ Upload XML ──────────────────────► S3  (chave 44 dígitos = nome do arquivo)
    └─ Persiste Invoice + InvoiceXml ───► PostgreSQL

GET /nfe/list                  ──► PostgreSQL  → lista todas as NFes
GET /nfe/tracking/{id}         ──► PostgreSQL  → dados da NFe
GET /nfe/tracking/{id}/xml     ──► S3          → XML da NFe
```

---

##💻 Pré-requisitos

- [Java 21+](https://www.oracle.com/java/technologies/downloads/)
- Maven 3.8+ (opcional - projeto inclui Maven Wrapper)
- [Docker](https://www.docker.com/) (para PostgreSQL e LocalStack)

---

## ⚙️ Configuração Avançada

### Variáveis de Ambiente

Copie o arquivo de exemplo e ajuste os valores:

```bash
cp .env.example .env
```

> 💡 Em desenvolvimento, mantenha `AWS_ACCESS_KEY_ID=test` e `AWS_SECRET_ACCESS_KEY=test` para funcionar com o LocalStack.

### 📧 Email (Opcional)

> 💡 Mock está ativo por padrão — emails aparecem confirmados apenas no console (sem envio real). Para envio real, preencha `SMTP_USERNAME` e `SMTP_PASSWORD` no `.env` e desative mock.enbaled.

### ☁️ S3 — Armazenamento de XML

Em ambiente de desenvolvimento o LocalStack simula o S3 localmente (porta `4566`). O bucket `s3-nfe-bucket` é criado automaticamente pelo `docker compose up`. O XML de cada NFe é armazenado usando a **chave de acesso de 44 dígitos** como nome do arquivo.

Para apontar para o S3 real (produção), substitua as credenciais e remova a configuração de endpoint local.

### 🔐 Certificado Digital A1 (Produção/Homologação) (NÃO TESTADO)

Para integração com SEFAZ:

```bash
export NFE_CERT_PATH="/caminho/certificado.pfx"
export NFE_CERT_PASSWORD="senha_certificado"
```

## 🚀 Como Executar

### 1. Clone o repositório

```bash
git clone https://github.com/Ayrton-Machado/NFe-Processing-Service-API
cd NFe-Processing-Service-API
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
# Edite o .env com suas credenciais
```

### 3. Iniciar PostgreSQL e LocalStack (S3)

**Linux / macOS:**
```bash
docker compose up -d
```

> Isso sobe o PostgreSQL na porta `5432` e o LocalStack (S3) na porta `4566`, além de criar automaticamente o bucket `s3-nfe-bucket`.

### 4. Execute a aplicação

**Linux / macOS:**
```bash
chmod +x mvnw  # Dar permissão de execução (executar apenas uma vez)
./mvnw quarkus:dev
```

**Windows (CMD):**
```cmd
mvnw.cmd quarkus:dev
```

**Windows (PowerShell):**
```powershell
.\mvnw.cmd quarkus:dev
```

✅ **Aplicação disponível em:** http://localhost:8080  
📖 **Swagger UI:** http://localhost:8080/q/swagger-ui

> 💡 **Quarkus Dev Mode** inclui hot reload - suas alterações são aplicadas automaticamente!

---

## 🧪 Executar Testes

**Linux / macOS:**
```bash
chmod +x mvnw  # Dar permissão (se ainda não fez)
./mvnw test
```

**Windows:**
```cmd
mvnw.cmd test
```

---

## 📊 Progresso do Projeto

### ✅ Concluído

- [x] API REST com Quarkus (3 endpoints principais)
- [x] Configuração multi-ambiente (application.properties)
- [x] Ambiente (test) que executa o fluxo do sistema sem necessidade de certificado A1
- [x] Certificado mockado para ambiente Teste (desenvolvimento sem A1)
- [x] Geração de XML NFe com validação estrutural contra schemas XSD da Receita
- [x] Geração de DANFE em PDF (JasperReports)
- [x] Envio de email com anexo PDF (Quarkus Mailer)
- [x] Entidades JPA com Hibernate Panache (Invoice, InvoiceItem, InvoiceXml)
- [x] Migração de H2 para PostgreSQL
- [X] Banco e LocalStack (s3) rodando em Container Docker
- [x] LocalStack (S3) no Docker Compose para simulação local do S3
- [x] Upload do XML NFe para S3 (nome do arquivo = chave de acesso de 44 dígitos)
- [x] Testes unitários (13 testes passando com JUnit)
- [X] Exigir dados pelo POST para validação e inserção no XML

### 🚧 Não Testado (Requer Certificado A1 Real)

- [ ] Integração total com SEFAZ (Homologação/Produção)
- [ ] Validação de XML assinada contra schemas XSD da Receita
- [ ] Autorização de NFe com SEFAZ

### 📌 To-Do

- [ ] Atualizar Readme Sempre

- [X] Calcular Impostos Automaticamente
    - [X] Corrigir DANFE para retornar impostos da nota (atualmente todos zerados)

- [ ] Criar validação dos dados + testes de validação
- [ ] Criar/refatorar endpoints
    - [ ] Implementar busca do XML no bucket pelo endpoint "/nfe/tracking/{id}/xml"

- [ ] Implementar autenticação JWT

- [ ] Configurar GitHub Actions

- [ ] Implementar métricas e observabilidade completa

- [ ] Implementar filas com RabbitMQ ou Kafka

- [ ] Preparar para deploy em nuvem

### 🔮 Funcionalidades Futuras

- [ ] Integrar Frontend Angular para demonstrar fluxo com inputs

- [ ] Em produção: substituir persistência do DANFE em disco por pdf_byte em memória, enviando por email sem salvar localmente

---

## 📄 Licença

Este projeto está sob licença. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---
