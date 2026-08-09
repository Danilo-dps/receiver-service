<div style="text-align: center;">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white&style=for-the-badge" alt="Spring Boot 4.1.0"/>
  <img src="https://img.shields.io/badge/Java%2025-ED8B00?logo=openjdk&logoColor=white&style=for-the-badge" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white&style=for-the-badge" alt="Maven"/>
  <img src="https://img.shields.io/badge/HMAC--SHA256-000000?logo=letsencrypt&logoColor=white&style=for-the-badge" alt="HMAC-SHA256"/>
  <img src="https://img.shields.io/badge/RestClient-6DB33F?logo=spring&logoColor=white&style=for-the-badge" alt="RestClient"/>
</div>
<h1 style="text-align: center;">📥 Receiver Service</h1>
<p style="text-align: center;"><i>Receptor de webhooks com validação HMAC, cache dual e idempotência</i></p>

---

## 📋 Sobre

O **Receiver Service** é responsável por:

- Receber webhooks assinados do Sender
- Validar a **assinatura HMAC-SHA256** com cache dual (`current` + `previous`)
- Rejeitar requisições com timestamp expirado (mitiga replay attacks)
- Garantir **idempotência** (não processa o mesmo evento 2x)
- Sincronizar segredos automaticamente com o Coordinator

---

## 🏗️ Arquitetura

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Sender     │────────►│  Receiver    │◄────────│ Coordinator  │
│  (Porta 8080)│         │  (Porta 8081)│         │  (Porta 8082)│
└──────────────┘         └──────────────┘         └──────────────┘
                              │
                              ▼
                        Cache Dual:
                        • current  (v2)
                        • previous (v1)
```

### Padrões aplicados

| Padrão | Implementação | Propósito |
|---|---|---|
| **Dual Cache** | `AtomicReference` x2 (`current` + `previous`) | Janela de transição na rotação de segredos |
| **Retry** | Spring Retry + backoff exponencial | Resiliência no bootstrap contra Coordinator fora |
| **Scheduler** | `@Scheduled` a cada 2 min | Detecta novas versões e promove `current → previous` |
| **Idempotência** | `ConcurrentHashMap.newKeySet()` | Evita processar o mesmo `eventId` duas vezes |
| **Timing-safe** | `MessageDigest.isEqual()` | Protege contra timing attacks na validação |

---

## 🚀 Tecnologias

| Tecnologia | Versão | Propósito |
|---|---|---|
| Spring Boot | 4.1.0 | Framework principal |
| Java | 25 | Linguagem |
| Maven | 3.9+ | Build |
| Spring Retry | Nativo | Resiliência de chamadas |
| RestClient | Nativo (Spring 6.1+) | Cliente HTTP moderno |
| HMAC-SHA256 | JCA | Validação de assinaturas |

---

## ⚙️ Como executar

### Pré-requisitos
- Java 25
- Maven 3.9+
- Coordinator Service rodando na porta 8082

### Comandos
```bash
# Compilar
./mvnw clean package

# Executar
./mvnw spring-boot:run
# ou
java -jar target/receiver-service-1.0.0.jar
```

O serviço sobe na porta **8081**.

---

## 🔌 Endpoints

### `POST /webhooks/order-event`
Recebe e valida um webhook assinado.

**Headers obrigatórios:**
- `X-Webhook-Signature`: `v1=base64(hmac)`
- `X-Webhook-Timestamp`: epoch em segundos
- `X-Webhook-Id`: UUID único do evento

**Body:**
```json
{
  "eventId": "evt_abc123",
  "orderId": "12345",
  "status": "PAGO",
  "price": 299.90,
  "timestamp": "2026-08-09T14:30:00Z"
}
```

**Respostas:**
- `200 OK`: Webhook aceito e processado
- `401 Unauthorized`: Assinatura inválida, timestamp fora da janela ou versão desconhecida

---

## 🔒 Segurança

- Validação de **timestamp** (tolerância de 5 minutos contra replay attacks)
- Validação de **versão** (apenas `current` e `previous` são aceitas)
- Validação de **assinatura HMAC** sobre o payload completo
- **Comparação timing-safe** para evitar vazamento de informação
- **Idempotência** por `eventId` (em memória; em produção, use Redis/BD)

---

## 📁 Estrutura

```
receiver-service/
├── src/main/java/br/com/danilodps/receiver/
│   ├── ReceiverApplication.java
│   ├── controller/
│   │   └── WebhookController.java
│   ├── application/
│   │   └── WebhookProcessorService.java
│   ├── domain/
│   │   └── EventRequest.java
│   │
│   └── infrastructure/
│       ├── cache/
│       │   └── DualSecretCache.java
│       ├── coordinator/
│       │   └── CoordinatorClient.java
│       ├── bootstrap/
│       │   └── SecretBootstrap.java
│       ├── scheduler/
│       │   └── SecretRefreshScheduler.java
│       └── security/
│           └── WebhookSignatureVerifier.java
├── pom.xml
└── README.md
```

---

<p style="text-align: center;">Desenvolvido com ☕ e 🛡️</p>