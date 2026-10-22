# Experiment 3.2.1 — Continuous Integration Pipeline

> **CO6 – BT6** | Spring Boot · JUnit 5 · MockMvc · Pact · Testcontainers · GitHub Actions

---

## Project Structure

```
FSD2_EXP_3.2.1/
├── .github/workflows/ci.yml        ← GitHub Actions CI pipeline
├── pom.xml                         ← Parent Maven POM (multi-module)
│
├── product-service/                ← Backend REST API (Pact Provider)
│   ├── src/main/java/…
│   │   ├── model/Product.java
│   │   ├── repository/ProductRepository.java
│   │   ├── service/ProductService.java
│   │   └── controller/ProductController.java
│   └── src/test/java/…
│       ├── unit/ProductServiceTest.java           ← JUnit 5 Unit Tests
│       ├── integration/ProductControllerIT.java   ← MockMvc Integration Tests
│       ├── contract/ProductContractVerifierTest.java ← Pact Provider Verification
│       └── testcontainers/ProductRepositoryIT.java   ← Testcontainers (PostgreSQL)
│
└── order-service/                  ← Consumer microservice (Pact Consumer)
    ├── src/main/java/…
    │   ├── model/{Product,Order}.java
    │   ├── client/ProductClient.java
    │   └── service/OrderService.java
    └── src/test/java/…
        └── contract/ProductConsumerPactTest.java  ← Pact Consumer (generates pact)
```

---

## Testing Layers

### 1. Unit Tests — `ProductServiceTest.java`
- **Framework**: JUnit 5 + Mockito
- **What's tested**: `ProductService` business logic in complete isolation
- **Strategy**: Repository mocked with `@Mock`; only service code runs
- **Run**: `mvn test -pl product-service -Dtest="unit/**"`

### 2. Integration Tests — `ProductControllerIT.java`
- **Framework**: Spring Boot Test + MockMvc
- **What's tested**: Full HTTP stack — Controller → Service → Repository → H2 DB
- **Strategy**: No mocking; real Spring context, H2 in-memory database
- **Run**: `mvn test -pl product-service -Dtest="integration/**"`

### 3. Contract Tests (Pact Consumer-Driven)

#### Consumer — `ProductConsumerPactTest.java`
- **Framework**: Pact JVM (Consumer)
- **What's tested**: What the Order Service _expects_ from the Product Service API
- **Output**: `order-service/target/pacts/order-service-product-service.json`
- **Run**: `mvn test -pl order-service -Dtest="contract/**"`

#### Provider — `ProductContractVerifierTest.java`
- **Framework**: Pact JVM (Provider Verifier)
- **What's tested**: That the real Product Service satisfies every pact interaction
- **Input**: Pact JSON files from `target/pacts/`
- **Run**: `mvn test -pl product-service -Dtest="contract/**"`

### 4. Infrastructure Tests — `ProductRepositoryIT.java`
- **Framework**: JUnit 5 + Testcontainers + Spring Data JPA
- **What's tested**: Repository queries against a **real PostgreSQL 15** container
- **Strategy**: `@DynamicPropertySource` injects the container's JDBC URL at runtime
- **Requirement**: Docker Desktop must be running
- **Run**: `mvn test -pl product-service -Dtest="testcontainers/**"`

---

## Running Tests Locally

### Prerequisites
| Requirement | Version |
|---|---|
| Java (JDK) | 17+ |
| Maven | 3.8+ |
| Docker Desktop | Latest (for Testcontainers) |

### Commands

```bash
# Build project without tests
mvn install -DskipTests

# Run all tests (unit + integration + contract + testcontainers)
mvn test

# Run only unit tests (no Docker needed)
mvn test -pl product-service -Dtest="unit/**"

# Run only integration tests (no Docker needed)
mvn test -pl product-service -Dtest="integration/**"

# Run Pact consumer tests → generates pact files
mvn test -pl order-service -Dtest="contract/**"

# Copy generated pact files to product-service, then verify
copy order-service\target\pacts\* product-service\target\pacts\
mvn test -pl product-service -Dtest="contract/**"

# Run Testcontainers tests (Docker must be running)
mvn test -pl product-service -Dtest="testcontainers/**"
```

---

## CI Pipeline (GitHub Actions)

The pipeline (`.github/workflows/ci.yml`) runs on every **push** and **pull request**.

```
Push / PR
    │
    ├──► [Job 1] Unit & Integration Tests
    │        ├── JDK 17 setup + Maven cache
    │        ├── mvn test (unit tests)
    │        ├── mvn test (MockMvc integration tests)
    │        └── Upload surefire reports
    │
    ├──► [Job 2] Contract Tests (after Job 1)
    │        ├── Pact Consumer tests → generates pact JSON
    │        ├── Upload pact files as artifacts
    │        ├── Download pact files for provider
    │        ├── Pact Provider verification tests
    │        └── Upload contract test reports
    │
    ├──► [Job 3] Infrastructure Tests (after Job 1)
    │        ├── Testcontainers + PostgreSQL (Docker available by default)
    │        └── Upload testcontainers reports
    │
    └──► [Job 4] CI Summary (always runs)
             └── Prints result of all jobs
```

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/products` | List all products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `GET` | `/api/products/category/{cat}` | Filter by category |
| `GET` | `/api/products/search?q={kw}` | Search by name keyword |
| `POST` | `/api/products` | Create new product |
| `PUT` | `/api/products/{id}` | Update product |
| `DELETE` | `/api/products/{id}` | Delete product |

---

## Sample Request (Create Product)

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 999.99,
    "category": "Electronics",
    "stock": 50
  }'
```

---

## Pushing to GitHub to Trigger CI

```bash
cd "c:\Users\tanwa\OneDrive\Desktop\FSD2_EXP_3.2.1"
git init
git add .
git commit -m "feat: Experiment 3.2.1 - CI Pipeline with unit, integration, contract and testcontainers tests"
git branch -M main
git remote add origin https://github.com/<your-username>/fsd2-exp-3-2-1.git
git push -u origin main
```
