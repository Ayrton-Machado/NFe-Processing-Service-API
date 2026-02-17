# NFe Processing Service API

![Java](https://img.shields.io/badge/java_21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Quarkus](https://img.shields.io/badge/quarkus-%234794EB.svg?style=for-the-badge&logo=quarkus&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue.svg?style=for-the-badge)

> **API REST para processamento de Notas Fiscais Eletrônicas (NFe)** com integração à biblioteca Java-NFe.
> 
> Arquitetura em camadas com separação de responsabilidades, configuração multi-ambiente e geração automática de DANFE com envio por e-mail.

---

## 🔗 Principais Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/nfe/create` | Emitir NFe (gera XML, valida, cria DANFE e envia email) |
| GET | `/nfe/list` | Listar todas as NFes |
| GET | `/nfe/tracking/{trackingId}` | Buscar NFe por código de rastreamento |

> 📖 **Swagger UI:** http://localhost:8080/q/swagger-ui

---

## 💻 Pré-requisitos

- [Java 21+](https://www.oracle.com/java/technologies/downloads/)
- Maven 3.8+ (opcional - projeto inclui Maven Wrapper)

---

## ⚙️ Configuração Avançada

### 📧 Email (Opcional)

Para enviar emails reais, configure:

```bash
export SMTP_USERNAME="seu-email@gmail.com"
export SMTP_PASSWORD="sua-senha-de-app"
```

> 💡 Mock está ativo, emails aparecem confirmados apenas no console (sem envio real).

### 🔐 Certificado Digital A1 (Produção/Homologação) (NÃO TESTADO)

Para integração com SEFAZ:

```bash
export NFE_CERT_PATH="/caminho/certificado.pfx"
export NFE_CERT_PASSWORD="senha_certificado"
```

## 🚀 Como Executar

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/NFe-Processing-Service-API
cd NFe-Processing-Service-API
```

### 2. Execute a aplicação

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
- [x] Entidades JPA com Hibernate Panache (Invoice, InvoiceItem)
- [x] Testes unitários (13 testes passando com JUnit)

### 🚧 Não Testado (Requer Certificado A1 Real)

- [ ] Integração total com SEFAZ (Homologação/Produção)
- [ ] Validação oficial contra schemas XSD da Receita
- [ ] Autorização de NFe com SEFAZ
- [ ] Consulta de status/recibo

### 🔮 Funcionalidades Futuras

- [ ] PostgreSQL (substituir H2 in-memory)
- [ ] Frontend Angular com fluxo de compra
    - [ ] Capturar Dados reais a partir do Input 
- [ ] Dockerização completa (Docker Compose)
- [ ] CI/CD com GitHub Actions
- [ ] Processamento assíncrono com Kafka

---

## 📄 Licença

Este projeto está sob licença. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---