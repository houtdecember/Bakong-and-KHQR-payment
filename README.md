# Spring Boot + Bakong KHQR Payment Integration

A learning project demonstrating how to integrate **Bakong KHQR payments** (Cambodia's national QR payment standard, built by the National Bank of Cambodia) into a **Spring Boot** application — from KHQR generation to real payment verification.

> 🇰🇭 Built as a standalone learning project to understand Bakong/KHQR integration patterns before applying them to a real-world application. Every part of this flow — QR generation, image rendering, and payment verification — has been tested with **real Bakong payments**, not just mocked responses.

---

## ✨ Features

- 🛍️ Simple Product module (id, name, price, description) to simulate an e-commerce checkout
- 💳 Payment creation with **backend-determined pricing** — the client can never set or fake a payment amount
- 🔲 Real KHQR string generation using the **official NBC `sdk-java` SDK**
- 🖼️ Scannable PNG QR code image generation via **ZXing**
- ✅ Real-time payment verification against the **live Bakong Open API**
- 🔁 Idempotent verification — safe to call `/verify` repeatedly without side effects
- ⏳ Payment expiration handling (KHQR-level and application-level)
- 🚫 Centralized error handling with proper HTTP status codes (400 / 404 / 503)
- 🏗️ Clean layered architecture: Controller → DTO → Mapper → Service → Repository

---

## 🧠 What This Project Is *Not*

This is intentionally scoped as a **payment learning sandbox** — it does **not** include booking, rental, vehicle, or reservation logic. The goal was to isolate and deeply understand the Bakong/KHQR payment flow before integrating it into a larger application.

---

## 🏗️ Architecture

```
Controller
    ↓
DTO
    ↓
Mapper
    ↓
Service
    ↓
Repository / BakongService
```

```
Product
   ↓
Create Payment (amount determined server-side)
   ↓
Generate KHQR (official NBC SDK)
   ↓
Render QR Image (ZXing)
   ↓
Customer scans & pays (Bakong-connected banking app)
   ↓
Backend verifies with live Bakong API
   ↓
Payment: PENDING → PAID
```

---

## 🔑 Core Security Principles

This project deliberately enforces a few non-negotiable rules, learned and applied throughout development:

- **The frontend is never trusted** for payment amount or payment status.
- **Payment amount is always derived from the Product's stored price**, never accepted from the client.
- **`paymentStatus` can only become `PAID`** through server-side verification against the real Bakong API — never by direct client request.
- **MD5 is used only as a transaction lookup key**, not as a security or password mechanism.

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot |
| Language | Java 21 |
| Database | MySQL (via Spring Data JPA / Hibernate) |
| KHQR Generation | [`kh.gov.nbc.bakong_khqr:sdk-java`](https://central.sonatype.com/artifact/kh.gov.nbc.bakong_khqr/sdk-java) (official NBC SDK) |
| QR Image Rendering | [ZXing](https://github.com/zxing/zxing) |
| Transaction Verification | [Bakong Open API](https://api-bakong.nbc.gov.kh/document) |
| Boilerplate Reduction | Lombok |

---

## 📋 Prerequisites

1. **Java 21+** and **Maven**
2. **MySQL** running locally (or update the datasource config for your setup)
3. A **verified individual or merchant Bakong account**
4. A **Bakong Open API Token** — register at [api-bakong.nbc.gov.kh](https://api-bakong.nbc.gov.kh/)

---

## ⚙️ Configuration

Add the following to `application.properties` (or supply via environment variables — **do not commit real credentials**):

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/your_db_name
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Bakong
bakong.account-id=${BAKONG_ACCOUNT_ID}
bakong.merchant-name=${BAKONG_MERCHANT_NAME}
bakong.merchant-city=${BAKONG_MERCHANT_CITY}
bakong.api-token=${BAKONG_API_TOKEN}
bakong.expiration-minutes=7
```

> ⚠️ **Never commit real credentials.** Use environment variables, an `.env` file excluded via `.gitignore`, or your IDE's run configuration to supply real values locally.

---

## 📦 Key Dependency

```xml
<dependency>
    <groupId>kh.gov.nbc.bakong_khqr</groupId>
    <artifactId>sdk-java</artifactId>
    <version>1.0.0.17</version>
</dependency>

<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
```

---

## 🚀 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/products` | Create a test product |
| `GET` | `/api/products` | List all products |
| `POST` | `/api/payments` | Create a payment (generates real KHQR) |
| `GET` | `/api/payments` | List all payments |
| `GET` | `/api/payments/{id}` | Get a single payment |
| `GET` | `/api/payments/{id}/qr` | Get the payment's QR code as a PNG image |
| `GET` | `/api/payments/{id}/verify` | Check payment status against live Bakong API |

---

## 🧪 Testing Flow

1. **Create a product**
   ```http
   POST /api/products
   { "name": "Coffee", "price": 5.00, "description": "Iced coffee" }
   ```

2. **Create a payment**
   ```http
   POST /api/payments
   { "productId": 1, "currency": "USD", "paymentMethod": "KHQR" }
   ```
   → Returns `qrString` and `md5Hash`, status `PENDING`

3. **Get the QR image**
   ```http
   GET /api/payments/{id}/qr
   ```
   → Returns a scannable PNG

4. **Scan and pay** using a Bakong-connected banking app

5. **Verify the payment**
   ```http
   GET /api/payments/{id}/verify
   ```
   → Status transitions to `PAID`, with `transactionId` populated

---

## 📝 Notes & Known Constraints

- The **`check_transaction_by_md5`** endpoint is restricted by NBC to be called only from servers physically located in Cambodia in production. Local development is unaffected.
- The Bakong Open API Token used here is applied directly (no auto-renewal). Tokens should be renewed periodically per NBC's policy — token auto-renewal via registered email is a natural next improvement.
- KHQR expiration (embedded in the QR itself) and the application's own `PENDING → EXPIRED` tracking are kept in sync via a single `bakong.expiration-minutes` config value.

---

## 📚 References

- [Bakong Open API Documentation (PDF)](https://bakong.nbc.gov.kh/download/KHQR/integration/Bakong%20Open%20API%20Document.pdf)
- [Official NBC `sdk-java` on Maven Central](https://central.sonatype.com/artifact/kh.gov.nbc.bakong_khqr/sdk-java)
- [KHQR Card Guideline (branding)](https://bakong.nbc.gov.kh/en/download/KHQR/guideline/KHQR%20Card%20Guideline.pdf)

---

## 📄 License

This project is for educational and integration reference purposes.
