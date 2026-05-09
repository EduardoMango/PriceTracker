# PriceTracker Project Documentation

This document provides a comprehensive overview of the **PriceTracker** project, designed for both developers and AI agents to understand the system's architecture, scope, and implementation patterns.

## 🚀 Project Overview
**PriceTracker** is a Spring Boot application that monitors product prices from various e-commerce websites and notifies users when prices meet specific criteria (e.g., price drops below a threshold).

### Core Features
- **Product Scraping**: Real-time price extraction using JSoup.
- **Price History**: Tracking price fluctuations over time.
- **Subscriptions API**: Full CRUD system allowing users to subscribe to products with specific alert conditions (`ComparisonType`, target price).
- **Asynchronous Notifications**: Event-driven, asynchronous email alerts via Mailhog/SMTP when a price update satisfies an alert condition.
- **REST API**: Fully documented with Swagger/OpenAPI.

---

## 🛠 Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 4.0.6
- **Database**: PostgreSQL 16
- **Persistence**: Spring Data JPA / Hibernate
- **Scraping**: JSoup
- **Mapping**: MapStruct
- **Utilities**: Lombok, WebClient
- **Testing**: Mailhog (Email testing)
- **Containerization**: Docker & Docker Compose

---

## 📁 Project Structure

The project follows a feature-based package structure within `com.eduardomango.pricetracker`:

| Package | Responsibility |
| :--- | :--- |
| `product` | Product metadata, URLs, and status management. |
| `subscription` | Linking users to products with alert conditions (`AlertCondition`). |
| `user` | User management and profiles. |
| `pricehistory` | Logging price changes and the logic for scheduled updates. |
| `notification` | Email service implementation. |
| `common` | Shared models (`Price`, `Email`), exceptions, and global handlers. |
| `common.architecture.scraping` | Core scraping engine and site-specific adapters. |

---

## 🏗 Key Architectural Patterns

### 1. Strategy Pattern for Scraping
Scraping is decoupled using a strategy-like pattern:
- **`ClientService`**: Interface for site-specific scrapers.
- **`ClientOrchestrator`**: Determines which `ClientService` to use based on the product URL.
- **`adapters`**: Implementation of scrapers for specific sites (e.g., `BuscaLibreClient`).

### 2. Domain-Driven Design (Lite)
Each feature package contains a `domain` sub-package with:
- **Entities**: JPA entities (e.g., `ProductEntity`).
- **DTOs**: Data Transfer Objects for API requests/responses.
- **Mappers**: MapStruct interfaces for conversion.

### 3. Price & Currency Handling
Prices are handled as Value Objects in `common.model.Price`, encapsulating `BigDecimal` amount and `Currency` code to ensure consistency across the system.

### 4. Domain Integrity with Value Objects
The project uses records and embeddables for domain-specific types like `URL` (in `product.domain`) to enforce validation rules (e.g., protocol checks) at the type level.

### 5. Event-Driven Architecture (Notifications)
To decouple core domain logic (like price updates) from cross-cutting concerns (like sending emails), the system utilizes **Spring Events**. 
- `PriceUpdateService` publishes a `PriceUpdatedEvent`.
- `NotificationListener` processes this event **asynchronously** (`@Async`), evaluates active subscriptions, and sends emails without blocking the main database transaction.

---

## 🔄 Core Workflows

### Price Update Flow
1. `PriceHistoryService` periodically triggers price updates for all products via scheduled tasks (`@Scheduled`).
2. `PriceUpdateService` identifies products needing updates and `ClientOrchestrator` selects the correct scraper.
3. JSoup extracts the latest price.
4. A new `PriceHistoryEntity` is saved and the `ProductEntity` is updated within a new transaction (`Propagation.REQUIRES_NEW`).
5. A `PriceUpdatedEvent` is published.
6. `NotificationListener` intercepts the event asynchronously, checking active `SubscriptionEntity` records.
7. If an `AlertCondition` is met, `EmailService` sends a notification via JavaMailSender.

---

## 🤖 Guide for AI Agents

### How to add a new Scraper
1. Create a new class in `common.architecture.scraping.adapters`.
2. Implement `ClientService`.
3. Annotate with `@Component`.
4. Implement the `supports(String url)` method to match the target domain.
5. Implement `scrape(String url)` using JSoup.

### Key Entry Points
- **Main App**: `PriceTrackerApplication.java`
- **API Docs**: Accessible via `/swagger-ui.html` when running.
- **Controllers**: Located in `product`, `user`, etc.

### Database Configuration
- Environment variables are defined in `.env` (refer to `.env.placeholder`).
- Local development uses `docker-compose.yml` for PostgreSQL and Mailhog.

---

## 🚦 Getting Started

### Prerequisites
- JDK 21
- Docker & Docker Compose

### Running the Project
1. Start infrastructure: `docker-compose up -d`
2. Build and run: `./mvnw spring-boot:run`

### Testing Emails
Access the Mailhog UI at `http://localhost:8025` to view outgoing emails.
