
# 🔐 EPI Outbound Request Signing (Using Vault/Gov rule based pvt key signing)

Tried demonstrating and running **secure, signed outbound requests** from *Fusion** (simulated in Java) to a mock **EPI `/payments` API**. It showcases:

- **JWT-based delegation** using `act.sub` (per RFC 7523)
- **Message-level integrity** using RS256 signatures embedded in a custom header (`X-Signature`)
- **End-to-end signature validation** via a mock backend

---

## 📋 Use Case

> Demonstrate end-to-end outbound request message signing from Fusion to an EPI-like `/payments` endpoint.

- Use a private key to sign request content.
- Include the signature in a custom HTTP header.
- Validate correctness via a mock backend server.

---

## 🧭 Implementation Steps

1. **Set up Mock EPI Receiver**
   - Accept `/payments` POST request
   - Verify signature using known public key

2. **Private Key Setup**
   - Store private key in Vault or governance rule
   - Retrieve securely in Fusion's mapper

3. **Create Mapper in Fusion**
   - Canonicalize and hash the request body (SHA-256)
   - Sign using RS256 and inject into `X-Signature` header

4. **Call Functional API**
   - Include OAuth access token
   - Attach signed body and custom header
   - Log request, signature, and response

5. **Test Scenarios**
   - ✅ Valid signature -> expect 200 OK
   - ❌ Tampered body -> expect 401 Unauthorized
   - ❌ Missing signature -> expect 400 Bad Request

---

## ✅ Acceptance Criteria

| Criteria                                   | Status |
|-------------------------------------------|--------|
| Signature is correctly generated          | ✅     |
| EPI mock backend validates it successfully| ✅     |
| Tampered requests are rejected            | ✅     |
| Logs trace ID, timestamps, and results    | ✅     |

---

## 📦 Project Structure

```
fusion-epi-signing-java-poc/
├── client/
│   ├── JWTGenerator.java
│   ├── PaymentSender.java
│   ├── SignUtil.java
│   └── resources/
│       ├── config.properties
│       └── private_key.pem
├── server/
│   ├── PaymentReceiver.java
│   ├── VerifyUtil.java
│   └── resources/
│       └── public_key.pem
├── tests/
│   └── SignatureTamperTest.java
├── pom.xml
├── application.properties
└── README.md
```

---

## 🔐 JWT Claim Structure (RFC 7523)

```json
{
  "jti": "uuid",
  "iss": "wallet-id",
  "sub": "wallet-id",
  "aud": "https://epi.engineering/token",
  "act": {
    "sub": "consumer-id-or-psp-id"
  },
  "iat": 1300819380,
  "exp": 1300819420
}
```

### 🧠 Claim Definitions

| Claim     | Description                                      |
|-----------|--------------------------------------------------|
| `iss`     | Issuer – Wallet or TSP making the request        |
| `sub`     | Subject – same as `iss`                          |
| `aud`     | Audience – EPI token endpoint                    |
| `act.sub` | Entity being represented (consumer or PSP)       |
| `iat/exp` | Token validity                                   |
| `jti`     | Unique token ID (prevents replay attacks)        |

🎯 **Key takeaway:** Use only `act` with `sub`. It’s not `act` or `act.sub` separately.

---

## 🚀 How to Run

### 🔧 Prerequisites

- Java 17+
- Maven
- OpenSSL

### 🔑 Key Generation

```bash
openssl genrsa -out client/resources/private_key.pem 2048
openssl rsa -in client/resources/private_key.pem -pubout -out server/resources/public_key.pem
```

### ⚙️ Build & Run

```bash
# Compile
mvn clean install

# Run backend server
java -cp target/fusion-epi-signing-poc-1.0-SNAPSHOT.jar com.epi.server.PaymentReceiver

# Run client sender
java -cp target/fusion-epi-signing-poc-1.0-SNAPSHOT.jar com.fusion.client.PaymentSender
```

---

## 🧪 Tests

Run `SignatureTamperTest.java` to validate:
- ✅ Accepted request with valid signature
- ❌ Rejected request with tampered content
- ❌ Rejected request with missing signature

---

## 📚 References

- [RFC 7523: JWT Profile for OAuth 2.0](https://datatracker.ietf.org/doc/html/rfc7523)
- [EPI JWT Example Format](https://docs.epicompany.eu/platform/reference/authentication-with-oauth#examples-of-jwt-assertion-tokens)
- [OBIE Open Banking Security Profile](https://www.openbanking.org.uk/)

---

## 👨‍💻 Author

**Sachin Arora**
📧 sachnaror@gmail.com
🌐 [about.me/sachin-arora](https://about.me/sachin-arora)

---

