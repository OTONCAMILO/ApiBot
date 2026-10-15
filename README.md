# ApiBotMessage

Integração robusta, assíncrona e tipada com a **Meta WhatsApp Cloud API** (Graph API v21.0).
Arquitetura limpa, Spring Boot 3.3, Java 21, zero credenciais no código.

---

## 🚀 Stack

| Camada | Tecnologia |
|--------|------------|
| Runtime | Java 21, Spring Boot 3.3.5 |
| HTTP Client | `RestClient` (Spring 6 nativo) |
| JSON | Jackson + `@JsonIgnoreProperties(ignoreUnknown=true)` |
| Assincronismo | `@Async` + pool dedicado `webhookTaskExecutor` |
| Validação | Bean Validation (JSR-380) |
| Testes | JUnit 5, MockMvc, **WireMock** (Graph API mock) |
| Build | Maven 3.9+ |

---

## 📁 Estrutura de Pacotes

```
com.apibotmessage
├── config/                 # AsyncConfig, RestClientConfig
├── controller/             # WebhookController (GET/POST /webhook)
├── client/                 # WhatsAppCloudClient (Graph API)
│   └── dto/                # MediaUploadResponse
├── service/                # MessageProcessor, MessageService
│   └── impl/               # Implementações
├── domain/
│   ├── dto/
│   │   ├── webhook/        # 18 DTOs Meta (com ignoreUnknown)
│   │   └── outbound/       # SendMessageRequest/Response
│   └── enums/              # MessageType
├── util/                   # BrazilianPhoneFormatter (E.164 + nono dígito)
```

---

## ⚙️ Configuração (`.env` ou variáveis de ambiente)

```bash
# Obrigatórios
WHATSAPP_VERIFY_TOKEN=seu_verify_token_aqui
WHATSAPP_PHONE_NUMBER_ID=123456789012345
WHATSAPP_ACCESS_TOKEN=EAAxxxxxxxxxxxxxxxxxxxx

# Opcionais
SERVER_PORT=8080
WHATSAPP_API_VERSION=v21.0
WHATSAPP_GRAPH_API_BASE_URL=https://graph.facebook.com
WHATSAPP_HTTP_CONNECT_TIMEOUT=5000
WHATSAPP_HTTP_READ_TIMEOUT=10000
```

> **NUNCA** commite tokens reais. Use secrets do CI/CD, Docker secrets, AWS Parameter Store, Vault, etc.

---

## ▶️ Como Rodar

```bash
# Compila
mvn -q compile

# Testes (WireMock sobe na porta 9999)
mvn test

# Rodar local
mvn spring-boot:run
# ou
java -jar target/ApiBotMessage-0.0.1-SNAPSHOT.jar
```

---

## 🔗 Endpoints

| Método | Path | Descrição |
|--------|------|-----------|
| `GET`  | `/webhook` | Validação Meta (`hub.challenge`) |
| `POST` | `/webhook` | Recebe mensagens/status (retorna 200 imediato) |

### Validação do Webhook (Meta Developer Console)

1. **Callback URL**: `https://seu-dominio.com/webhook`
2. **Verify Token**: valor de `WHATSAPP_VERIFY_TOKEN`
3. **Campos assinados**: `messages`, `message_deliveries`, `message_reads`, `message_echoes`

### Exemplo `curl` validação manual

```bash
curl -G "http://localhost:8080/webhook" \
  -d "hub.mode=subscribe" \
  -d "hub.verify_token=test_verify_token_123" \
  -d "hub.challenge=desafio_abc_123"
# Resposta esperada: desafio_abc_123 (string pura, sem aspas)
```

### Exemplo `curl` simulando mensagem entrante

```bash
curl -X POST "http://localhost:8080/webhook" \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "id": "123",
      "changes": [{
        "field": "messages",
        "value": {
          "messaging_product": "whatsapp",
          "metadata": {
            "display_phone_number": "15551234567",
            "phone_number_id": "123456789"
          },
          "messages": [{
            "from": "5511999999999",
            "id": "wamid.test123",
            "timestamp": "1234567890",
            "type": "text",
            "text": {"body": "Olá, tudo bem?"}
          }]
        }
      }]
    }]
  }'
# Resposta: 200 OK (vazio, imediato)
```

---

## 📤 Enviando Mensagens (programaticamente)

```java
@Autowired MessageService messageService;

// Texto simples
messageService.sendText("558293367183", "Olá! 👋");

// Template (HSM)
messageService.sendTemplate("558293367183", "hello_world", "pt_BR", List.of());

// Mídia por link
messageService.sendMedia("558293367183", "image",
    "https://exemplo.com/imagem.jpg", "Legenda", "foto.jpg");

// Upload mídia para reuso (retorna media_id)
String mediaId = messageService.uploadMedia(bytes, "image/jpeg", "foto.jpg").orElseThrow();
messageService.sendMedia("558293367183", "image", mediaId, null, null);
```

---

## 🇧🇷 Formatação de Telefone Brasileiro

`BrazilianPhoneFormatter.toE164()` normaliza **qualquer entrada** para E.164:

| Entrada | Saída E.164 |
|---------|-------------|
| `8293367183` | `558293367183` |
| `+55 82 93367-183` | `558293367183` |
| `(82) 9336-7183` | `558293367183` (celular) |
| `558293367183` | `558293367183` **(insere nono dígito automaticamente)** |

> Célulares brasileiros **sempre** saem com 13 dígitos (`55` + `DDD` + `9` + `8 dígitos`).

---

## ⚡ Assincronismo & Performance

- `WebhookController.POST` retorna `200 OK` em **< 1 ms**
- Processamento real roda em pool dedicado: `webhookTaskExecutor` (core=4, max=16, queue=100)
- `CallerRunsPolicy` evita rejeição sob carga: thread chamadora executa se fila cheia
- Timeouts HTTP: connect 5s, read 10s (configuráveis)

---

## 🧪 Testes

```bash
# Roda todos (unit + integração com WireMock)
mvn test

# Apenas unitários
mvn test -Dtest=*Test -DfailIfNoTests=false

# Coverage (JaCoCo)
mvn verify jacoco:report
# Abre target/site/jacoco/index.html
```

### Cenários cobertos

| Classe | Cenários |
|--------|----------|
| `BrazilianPhoneFormatterTest` | 12+ casos: formatos variados, nono dígito, validação, display |
| `WhatsAppCloudClientTest` | 200 OK, 401, 429, 500, upload mídia, arquivo vazio |
| `WebhookControllerTest` | verify token ok/inválido, mode inválido, POST 200 imediato |

---

## 📋 Check-list de Produção

### Segurança
- [ ] `WHATSAPP_VERIFY_TOKEN` forte (32+ chars aleatórios)
- [ ] `WHATSAPP_ACCESS_TOKEN` rotacionado periodicamente (Meta expira em ~60 dias se não for permanente)
- [ ] HTTPS obrigatório (Meta **não** aceita HTTP em produção)
- [ ] Rate limiting no reverse proxy (nginx/Cloudflare) antes do `/webhook`
- [ ] Logs sem PII: `maskPhone()` já implementado
- [ ] `spring.jackson.deserialization.fail-on-unknown-properties=false` (já no `application.properties`)

### Observabilidade
- [ ] Métricas: latência `POST /webhook`, taxa de erro 4xx/5xx Graph API, fila `webhookTaskExecutor`
- [ ] Alertas: fila > 80%, latência P99 > 500ms, erros 401/403 (token expirado)
- [ ] Tracing: `X-B3-TraceId` propagado para Graph API (adicionar `RestClient` interceptor)
- [ ] Logs estruturados (JSON) para Loki/ELK/Datadog

### Confiabilidade
- [ ] Dead Letter Queue para mensagens que falharem no processamento assíncrono
- [ ] Idempotência: deduplicar por `message.id` (Meta pode reenviar)
- [ ] Retry com backoff exponencial no `WhatsAppCloudClient` para 429/5xx (implementar `RetryTemplate` ou Resilience4j)
- [ ] Health check: `/actuator/health` + verificação de conectividade Graph API

### Escalabilidade
- [ ] Múltiplas instâncias: webhook stateless ✅ (processamento assíncrono isolado)
- [ ] Banco de dados: adicionar `MessageLogRepository` para auditoria
- [ ] Multi-tenant: `phoneNumberId` + `accessToken` por cliente (já suportado no client)

### LGPD / Compliance
- [ ] Retenção de logs: definir TTL (ex: 90 dias)
- [ ] Direito ao esquecimento: endpoint para anonimizar `from`/`to`
- [ ] Consentimento: armazenar opt-in antes de disparar templates (HSM)

---

## 🔧 Próximos Passos (Roadmap)

1. **Retry automático** com Resilience4j (429, 5xx, timeout)
2. **Persistência** (PostgreSQL + JPA) para `MessageLog`, `Conversation`, `Contact`
3. **Webhook interno** (callback para seu frontend quando mensagem chega/status muda)
4. **Gerenciamento de Templates** (CRUD + sincronização com Meta)
5. **Dashboard** (Spring Boot Admin + Grafana)
6. **Multi-tenant real** (tabela `tenant` com `phone_number_id`, `access_token_encrypted`)

---

## 📄 Licença
MIT — use, estude, modifique, distribua.

Todo esse projeto foi feito com a Ajuda da IA, inicialmente para estudo academico proprio 
para entender como funciona uma API e integrar com a API do WhatsApp Cloud.