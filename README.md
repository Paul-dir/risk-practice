# Risk Assessment Engine

A comprehensive Tax Risk Assessment Engine for Ethiopian Federal Tax Authority systems, built with Spring Boot and following clean architecture principles.

## Overview

This system calculates risk scores for taxpayers based on multiple compliance and behavioral factors:

- **Filing Compliance** - Late/missing tax returns
- **Payment History** - Late/partial payments
- **Financial Health** - Losses, revenue trends
- **Transaction Patterns** - Import/export analysis, related-party dealings
- **Behavioral Indicators** - Fraud history, business age
- **Industry Benchmarks** - Deviation from sector norms

## Architecture

The system uses a **centralized database architecture** where all modules (Tax Audit, Risk Engine) access a shared central database directly, providing:

- 🚀 **High Performance** - 10ms vs 300ms (30x faster than distributed services)
- 😊 **Simplicity** - No microservices complexity, no network overhead
- ✅ **Consistency** - ACID transactions, immediate data visibility
- 💰 **Lower Cost** - Single database, simpler infrastructure

For detailed architecture documentation, see:
- [Architecture Principles](ARCHITECTURE_PRINCIPLES.md)
- [Architecture Decision Summary](ARCHITECTURE_DECISION_SUMMARY.md)
- [Centralized vs Distributed Architecture](CENTRALIZED_VS_DISTRIBUTED_ARCHITECTURE.md)
- [Central Database Implementation](CENTRAL_DATABASE_IMPLEMENTATION.md)
- [Project Structure](PROJECT_STRUCTURE.md)

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Backend | Spring Boot | 3.2.x |
| Language | Java | 17+ |
| Database | PostgreSQL | 15 |
| Migration | Flyway | Latest |
| Cache | Redis | 7 |
| Messaging | Apache Kafka | Latest |
| Build | Maven | 3.8+ |
| Container | Docker | Latest |

## Key Features

### ✅ Risk Assessment Engine
- Multi-category risk scoring (Filing, Payment, Financial, Transaction, Behavioral, Industry)
- Configurable risk indicators with weights
- Historical risk profile tracking
- Confidence factor calculation
- Risk level classification (LOW, MEDIUM, HIGH, CRITICAL)

### ✅ Clean Architecture
- Hexagonal architecture with ports & adapters
- Domain-driven design
- Clear separation of concerns
- Event-driven architecture (Kafka integration)
- RESTful API design

### ✅ Database Schema
- 13 tables covering all aspects of tax risk assessment
- Master data tables (taxpayers, tax_returns, payments)
- Risk assessment tables (assessments, profiles, indicators)
- Audit trail and case management
- Full referential integrity with foreign keys

## API Endpoints

### Risk Assessment
```
POST /api/backoffice/risk-assessments
```
Calculates risk assessment for a taxpayer.

**Request:**
```json
{
  "taxpayerId": "uuid",
  "tin": "1234567890"
}
```

**Response:**
```json
{
  "assessmentId": "uuid",
  "taxpayerId": "uuid",
  "tin": "1234567890",
  "overallScore": 75.5,
  "riskLevel": "HIGH",
  "confidence": 0.85,
  "categoryScores": [...],
  "indicatorScores": [...]
}
```

### Risk Explanation
```
GET /api/portal/risk-explanations/{taxpayerId}
```
Provides detailed explanation of risk assessment for taxpayer transparency.

## Project Structure

```
risk-practice/
├── src/main/java/com/practice/risk/
│   ├── api/                    # REST controllers & DTOs
│   ├── application/            # Business logic & orchestration
│   ├── domain/                 # Domain models & services
│   ├── infrastructure/         # External adapters & persistence
│   └── config/                 # Spring configuration
│
├── src/main/resources/
│   ├── application.yml         # Production configuration
│   ├── application-dev.yml     # Development configuration
│   └── db/migration/           # Flyway database migrations
│
└── docker-compose.yml          # Docker services (PostgreSQL, Redis, Kafka)
```

See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for detailed structure documentation.

## Database Schema

### Master Data Tables
- `taxpayers` - Taxpayer master registry
- `tax_returns` - Tax filing history
- `payments` - Payment transaction history
- `outstanding_balances` - Unpaid tax liabilities
- `external_data` - Credit scores, sanctions, licenses
- `audit_history` - Previous audit records

### Risk Engine Tables
- `risk_assessments` - Risk calculation results
- `taxpayer_risk_profile` - Historical risk trends
- `risk_indicator_config` - Configurable risk rules
- `assessment_category_scores` - Category-level scores
- `assessment_indicator_scores` - Detailed indicator scores
- `risk_audit_logs` - Complete audit trail

### Tax Audit Module Tables
- `audit_cases` - Audit case management

See [CENTRAL_DATABASE_IMPLEMENTATION.md](CENTRAL_DATABASE_IMPLEMENTATION.md) for complete schema documentation.

## Prerequisites

- Java 17 or higher
- Docker & Docker Compose
- Maven 3.8+
- PostgreSQL 15 (via Docker)
- Redis 7 (via Docker)
- Apache Kafka (via Docker)

## Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/Paul-dir/risk-practice.git
cd risk-practice
```

### 2. Start Docker Services
```bash
docker-compose up -d
```

### 3. Build the Application
```bash
./mvnw clean package -DskipTests
```

### 4. Run the Application
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on http://localhost:8080

### 5. Verify
```bash
# Health check
curl http://localhost:8080/actuator/health
```

## Configuration

### Development Profile
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/risk_engine_db
    username: postgres
    password: postgres
  flyway:
    enabled: true
```

### Production Profile
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/risk_engine_db
    username: risk_engine
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
```

## Development

### Running Tests
```bash
./mvnw test
```

### Building for Production
```bash
./mvnw clean package
```

### Running with Production Profile
```bash
java -jar target/risk-practice-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Architecture Documentation

For comprehensive architecture documentation, please refer to:

1. **[ARCHITECTURE_PRINCIPLES.md](ARCHITECTURE_PRINCIPLES.md)** - Core design principles and patterns
2. **[ARCHITECTURE_DECISION_SUMMARY.md](ARCHITECTURE_DECISION_SUMMARY.md)** - Key architectural decisions
3. **[CENTRALIZED_VS_DISTRIBUTED_ARCHITECTURE.md](CENTRALIZED_VS_DISTRIBUTED_ARCHITECTURE.md)** - Detailed comparison
4. **[CENTRAL_DATABASE_IMPLEMENTATION.md](CENTRAL_DATABASE_IMPLEMENTATION.md)** - Complete database schema
5. **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** - Code organization and structure

## Contributing

This is a government project for the Ethiopian Federal Tax Authority. Please follow the established coding standards and architecture patterns.

## License

Internal use - Ethiopian Federal Tax Authority

## Contact

For questions or support, please contact the development team.
