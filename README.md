# PriceTracker

English | [Version en Espanol](README.es.md)

PriceTracker is a backend service developed in Java 21 with Spring Boot designed for automated price tracking, historical monitoring, and evaluation of price fluctuations across e-commerce platforms (initially focused on Argentine online book retailers such as BuscaLibre and Libreria Palito).

The system enables users to register products via their web URL, automatically retrieve metadata and current prices through web scraping and embedded data parsing, track price variations over time via scheduled batch jobs, and manage users with customizable alert conditions and subscriptions.

---

## Table of Contents

- Overview
- System Architecture
- Design Patterns
- Technical and Architectural Decisions
- Domain and Data Model
- Docker Implementation
- Configuration and Environment Variables
- Installation and Launch Guide
- API Documentation and Endpoints
- Scheduled Tasks and Background Jobs
- License

---

## Overview

The primary goal of PriceTracker is to address price volatility in digital marketplaces, enabling users to automate product monitoring and trigger alerts whenever predefined conditions are met (for instance, when a price drops below a specific threshold).

### Core Features

- Automatic product metadata extraction (name, description, price, currency) directly from a supported URL.
- Decoupled, extensible multi-store scraping orchestration.
- Dynamic catalog querying using Spring Data JPA Specifications.
- Automated periodic price updates via scheduled background jobs (Spring Scheduler).
- Isolated transaction management to ensure fault tolerance in batch operations.
- Uniform RFC 7807 (Problem Details) error handling across all API responses.
- Self-contained development environment using Docker, PostgreSQL, and MailHog.

---

## System Architecture

The project follows a modular, domain-driven package structure (Package by Feature / pragmatic DDD). Each module encapsulates its respective domain entities, repositories, services, DTOs, mappers, and REST controllers.

```
com.eduardomango.pricetracker
|-- common
|   |-- architecture.scraping     # Scraping client interfaces, orchestrator
|   |   `-- adapters              # Store-specific adapters (BuscaLibre, LibreriaPalito)
|   |-- config                    # WebClient configuration and shared beans
|   |-- exceptions                # Domain exception hierarchy
|   |-- handlers                  # Global exception handler (RFC 7807)
|   `-- model                     # Value Objects (Price, Email) and base contracts (IMapper)
|-- product
|   |-- domain                    # ProductEntity, Value Objects (URL, AlertCondition), DTOs
|   |-- mapper                    # MapStruct mappers
|   |-- ProductController         # Product catalog REST endpoints
|   |-- ProductService            # Product business logic
|   |-- ProductSpecification      # Dynamic JPA query specifications
|   `-- ProductRepository         # Product data access
|-- pricehistory
|   |-- domain                    # PriceHistoryEntity for historical tracking
|   |-- PriceHistoryService       # Scheduled cron background tasks (@Scheduled)
|   |-- PriceUpdateService        # Isolated per-product transactions (REQUIRES_NEW)
|   `-- PriceHistoryRepository    # Price history data access
|-- user
|   |-- domain                    # UserEntity, DTOs, and mappers
|   |-- UserController            # User management REST endpoints
|   |-- UserService               # User business logic
|   `-- UserRepository            # User data access
|-- subscription
|   |-- domain                    # SubscriptionEntity and alert condition rules
|   `-- SubscriptionRepository    # Subscription data access
`-- notification
    `-- EmailService              # Email notification delivery infrastructure
```

### Data Flow

```
[HTTP Client / Frontend]
          |
          v
  [REST Controllers]
          |
          v
   [Service Layer]
     |          |
     |          +---> [ClientOrchestrator] ---> [ClientService / Adapters] ---> [External Websites]
     v
[JPA Repositories]
     |
     v
[PostgreSQL Database]
```

---

## Design Patterns

The codebase leverages several software engineering and object-oriented design patterns:

### 1. Strategy Pattern (Scraping Strategies)
- **Components**: `ClientService` (interface), `BuscaLibreClient`, `LibreriaPalitoClient` (concrete strategies).
- **Mechanism**: Each e-commerce store implements its own scraping strategy, CSS selectors, regex extraction, and embedded JSON parsing (`dataLayer` or product detail scripts). All clients adhere to the contract defined in `ClientService`:
  - `boolean supports(URI url)`: Determines if the adapter can process the requested URL host.
  - `ProductEntity getProduct(URI url)`: Performs comprehensive initial metadata extraction.
  - `Price getPrice(URI url)`: Performs lightweight price retrieval for recurring updates.

### 2. Orchestrator / Dispatcher Pattern
- **Component**: `ClientOrchestrator`.
- **Mechanism**: Spring automatically autowires all beans implementing `ClientService` into a `List<ClientService>`. When a URI is received, the orchestrator queries each client via `supports(url)` and delegates extraction to the appropriate adapter transparently. Adding support for new retailers requires only adding a new `ClientService` component without altering existing consumer code (Open/Closed Principle).

### 3. Specification Pattern
- **Component**: `ProductSpecification`.
- **Mechanism**: Utilizes Spring Data JPA's `PredicateSpecification` to construct composable, type-safe SQL predicates dynamically (`nameContains`, `nameEquals`, `descriptionContains`, `priceGreaterThan`, `priceLesserThan`). If a query filter parameter is null or empty, it returns `conjunction()`, avoiding redundant repository methods or fragile string concatenation.

### 4. Value Object Pattern (Domain-Driven Design)
- **Components**: `Price`, `URL`, `Email`, `AlertCondition`.
- **Mechanism**: Modeled as immutable Java `record`s marked with `@Embeddable`. Invariants are enforced inside compact constructors:
  - `Price`: Guarantees amounts are non-null and greater than zero, standardizing currency scale to 2 decimal places with `RoundingMode.HALF_UP`.
  - `URL`: Validates URI syntax and ensures protocols are restricted to `http` or `https`.
  - `Email`: Validates addresses against an RFC-compliant regular expression.
  - `AlertCondition`: Encapsulates business logic to evaluate if a target price satisfies threshold conditions via `isSatisfiedBy(Price currentPrice)`.

### 5. DTO and Mapper Pattern
- **Components**: `ProductRequest`, `ProductResponse`, `UserDTO`, `NewUserDTO`, `PriceDTO`, and MapStruct mappers (`ProductResponseMapper`, `UserMapper`, etc.).
- **Mechanism**: Strictly isolates persistent database entities from the external API contract, preventing overexposure of internal database schema and cyclic serialization issues.

---

## Technical and Architectural Decisions

### Decoupling Network I/O from Database Transactions
In `PriceHistoryService`, the scheduled scraping operation (a potentially slow or blocking external network request) is intentionally executed **outside** any database transaction:
```java
// Network I/O outside transaction to prevent holding database connections
Price newPrice = clientOrchestrator.getPrice(URI.create(product.getUrl().value()));

// Persistence executed inside an isolated transaction
priceUpdater.updateSingleProduct(product, newPrice);
```
This design avoids holding HikariCP connection pool resources during slow or failing HTTP requests, preventing database connection starvation.

### Transactional Boundary Isolation and Spring AOP Proxy Considerations
Persistence for price updates is extracted into a dedicated class (`PriceUpdateService`) annotated with:
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void updateSingleProduct(ProductEntity product, Price newPrice)
```
- **Why `REQUIRES_NEW`**: Each product update runs in its own isolated database transaction. If an update fails for a specific product, only that transaction rolls back, leaving previously updated products intact.
- **Why a separate class**: Spring manages transaction lifecycles through dynamic AOP proxies. Calling an annotated method from within the same class (self-invocation) bypasses the proxy, failing to initiate the new transaction. Delegating to a separate Spring bean ensures the proxy intercepts the call properly.

### Internal vs. External Identifiers (UUID vs. Long)
Entities feature a dual-identifier strategy:
- `id` (`Long`, Identity): Autoincrementing primary key used internally for performant indexing and foreign key relationships in PostgreSQL.
- `externalId` (`UUID`): Public unique identifier automatically populated during `@PrePersist`. REST API routes interact strictly with this UUID, preventing resource enumeration attacks and hiding database table size metrics.

### RFC 7807 Error Handling Standard
The centralized exception handler (`GlobalExceptionHandler`) utilizes Spring's `ProblemDetail` specification to deliver standardized JSON error responses containing `type`, `title`, `status`, `detail`, `instance`, and custom properties such as `timestamp`, `path`, and `method`.

---

## Domain and Data Model

The relational database schema is structured as follows:

```
+--------------------+            +------------------------+
|      products      | 1        * |     price_history      |
+--------------------+------------+------------------------+
| id (PK, Long)      |            | id (PK, Long)          |
| external_id (UUID) |            | product_id (FK)        |
| name               |            | current_price (amount) |
| url (value)        |            | currency               |
| current_price      |            | checked_at             |
| currency           |            +------------------------+
| description        |
| status             |            +------------------------+
| last_checked       | 1        * |     subscriptions      |
| created_at         |------------+------------------------+
| updated_at         |            | id (PK, Long)          |
+--------------------+            | product_id (FK)        |
                                  | user_id (FK)           |
+--------------------+            | target_price           |
|       users        | 1        * | target_currency        |
+--------------------+------------+ comparison_type        |
| id (PK, Long)      |            | active                 |
| external_id (UUID) |            +------------------------+
| email_address      |
+--------------------+
```

### Enumerations

- **ProductStatus**: `ACTIVE`, `INACTIVE`.
- **ComparisonType**: `LOWER_THAN`, `HIGHER_THAN`, `EQUALS`.

---

## Docker Implementation

PriceTracker includes containerization assets for development and deployment reproducibility.

### Multi-Stage Dockerfile

The `Dockerfile` employs a multi-stage build strategy:

```dockerfile
# Stage 1: Build and package
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Layer Caching**: `pom.xml` dependencies are fetched (`dependency:go-offline`) prior to copying source code. Docker caches the dependency layer, speeding up subsequent builds when only code changes.
- **Minimal Surface**: The production image discards the Maven installation, build tools, and source tree, packaging only the compiled JAR onto a lightweight Alpine Linux JRE base.

### Docker Compose Services

The `docker-compose.yml` orchestrates three isolated services:

1. **`db` (PostgreSQL 16)**:
   - Primary relational database.
   - Persistent volume mapping: `postgres_data:/var/lib/postgresql/data`.
   - Port forwarding: `5432:5432`.
2. **`app` (PriceTracker Application)**:
   - Built directly from the root `Dockerfile`.
   - Depends on `db`.
   - Port forwarding: `8080:8080`.
3. **`mailhog` (MailHog)**:
   - Development SMTP server with a built-in web viewer for verifying outbound email notifications.
   - Port `1025`: SMTP server.
   - Port `8025`: Web UI dashboard.

---

## Configuration and Environment Variables

Configuration is externalized in `src/main/resources/application.yaml` and can be overridden via environment variables:

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC database connection URL | `jdbc:postgresql://localhost:5432/pricetracker` |
| `SPRING_DATASOURCE_USERNAME` | Database user name | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate schema management strategy | `update` |
| `SPRING_MAIL_HOST` | Outbound mail SMTP host | `mailhog` |
| `SPRING_MAIL_PORT` | Outbound mail SMTP port | `1025` |

---

## Installation and Launch Guide

### Prerequisites

- Docker and Docker Compose installed.
- Or for local bare-metal development:
  - Java 21 JDK.
  - Maven 3.9+ (or use the included `./mvnw` wrapper).
  - PostgreSQL running locally on port 5432.

### Option A: Launch with Docker Compose (Recommended)

1. Clone the repository and navigate to the project directory:
   ```bash
   git clone <REPOSITORY_URL>
   cd PriceTracker
   ```

2. Build and start all services in detached or interactive mode:
   ```bash
   docker compose up --build
   ```

3. Access the services:
   - REST API: `http://localhost:8080`
   - Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`
   - MailHog Web Dashboard: `http://localhost:8025`

4. To stop services and preserve data:
   ```bash
   docker compose down
   ```

### Option B: Local Development Run

1. Spin up only the backing services (Database and MailHog):
   ```bash
   docker compose up -d db mailhog
   ```

2. Compile and package the application:
   ```bash
   ./mvnw clean compile
   ```

3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Run tests:
   ```bash
   ./mvnw test
   ```

---

## API Documentation and Endpoints

Interactive OpenAPI 3.0 documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON specification: `http://localhost:8080/v3/api-docs`

### 1. Products (`/products`)

#### Register a Product
Fetches metadata and initial price from the provided URL, creating a new product record:
```http
POST /products
Content-Type: application/json

{
  "name": "Sample Book Title",
  "url": "https://www.buscalibre.com.ar/sample-book/p/123456"
}
```

#### List Products with Dynamic Filters
```http
GET /products?name=quijote&minPrice=1000&maxPrice=15000
```
Available query parameters:
- `name` (string, optional): Case-insensitive partial name match.
- `nameMatch` (string, optional): Case-insensitive exact name match.
- `description` (string, optional): Case-insensitive description match.
- `minPrice` (decimal, optional): Minimum product price.
- `maxPrice` (decimal, optional): Maximum product price.

#### Get Product by External ID
```http
GET /products/{productId}
```
Where `{productId}` is the public UUID.

#### Delete Product
```http
DELETE /products/{productId}
```

---

### 2. Users (`/users`)

#### Create User
```http
POST /users
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### List All Users
```http
GET /users
```

#### Get User by External ID
```http
GET /users/{userId}
```

#### Update User Email
```http
PUT /users/{userId}
Content-Type: application/json

{
  "email": "updated_user@example.com"
}
```

#### Delete User
```http
DELETE /users/{userId}
```

---

## Scheduled Tasks and Background Jobs

Background scheduling is enabled via `@EnableScheduling` on the main application class.

### Price Update Job
Implemented in `PriceHistoryService.updatePriceHistory()`:
- **Cron Expression**: `0 0 9,17 * * *`
- **Execution Schedule**: Twice daily at 09:00 and 17:00.
- **Workflow**:
  1. Queries all registered products from `ProductRepository`.
  2. For each product, triggers the assigned scraping client to obtain the latest live price.
  3. Appends a new timestamped record to `PriceHistoryEntity`.
  4. Updates the `currentPrice` and `lastChecked` attributes on `ProductEntity`.
  5. Catches and logs individual scraping exceptions to prevent batch failures if a single external page fails or changes structure.

---

## License

This project is licensed under the terms of the MIT License. See the [LICENSE](LICENSE) file for full details.
