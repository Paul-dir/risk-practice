# Risk Engine - Project Structure (Aligned with Tax Audit Service)

Base package: `com.practice.risk`

Following the clean architecture pattern from the Tax Audit Service.

## Directory Tree

```
risk-engine/
├── pom.xml
├── README.md
├── ARCHITECTURE_PRINCIPLES.md
├── IMPLEMENTATION_ROADMAP.md
├── NEXT_STEPS.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── practice
│   │   │           └── risk
│   │   │               │
│   │   │               ├── api
│   │   │               │   ├── advice
│   │   │               │   │   └── GlobalExceptionHandler.java
│   │   │               │   │
│   │   │               │   ├── controller
│   │   │               │   │   ├── backoffice                    ← admin/analyst
│   │   │               │   │   │   ├── RiskAssessmentController.java
│   │   │               │   │   │   ├── RiskConfigurationController.java
│   │   │               │   │   │   ├── RiskIndicatorController.java
│   │   │               │   │   │   └── RiskBatchController.java
│   │   │               │   │   │
│   │   │               │   │   ├── portal                        ← taxpayer/public
│   │   │               │   │   │   └── RiskExplanationController.java
│   │   │               │   │   │
│   │   │               │   │   ├── webhook                       ← system-to-system
│   │   │               │   │   │   ├── TaxReturnWebhookController.java
│   │   │               │   │   │   ├── PaymentWebhookController.java
│   │   │               │   │   │   └── RegistrationWebhookController.java
│   │   │               │   │   │
│   │   │               │   │   └── internal                      ← service-to-service
│   │   │               │   │       └── InternalRiskController.java
│   │   │               │   │
│   │   │               │   └── dto
│   │   │               │       ├── request
│   │   │               │       │   ├── RiskAssessmentRequest.java
│   │   │               │       │   ├── BatchAssessmentRequest.java
│   │   │               │       │   ├── UpdateIndicatorConfigRequest.java
│   │   │               │       │   └── OverrideRiskScoreRequest.java
│   │   │               │       │
│   │   │               │       └── response
│   │   │               │           ├── RiskAssessmentResponse.java
│   │   │               │           ├── RiskExplanationResponse.java
│   │   │               │           ├── RiskProfileResponse.java
│   │   │               │           ├── RiskTrendResponse.java
│   │   │               │           └── RiskStatisticsResponse.java
│   │   │               │
│   │   │               ├── application
│   │   │               │   │
│   │   │               │   ├── event                             ← inbound event handlers
│   │   │               │   │   ├── TaxReturnFiledHandler.java
│   │   │               │   │   ├── PaymentReceivedHandler.java
│   │   │               │   │   ├── TaxpayerRegisteredHandler.java
│   │   │               │   │   └── AuditCompletedHandler.java
│   │   │               │   │
│   │   │               │   ├── port                              ← hexagonal ports
│   │   │               │   │   │                                 ── Outbound Repository Ports ──
│   │   │               │   │   ├── outbound
│   │   │               │   │   │   ├── RiskAssessmentRepositoryPort.java
│   │   │               │   │   │   ├── TaxpayerRiskProfileRepositoryPort.java
│   │   │               │   │   │   ├── RiskIndicatorConfigRepositoryPort.java
│   │   │               │   │   │   │                             ── External Data Ports ──
│   │   │               │   │   │   ├── RegistrationPort.java
│   │   │               │   │   │   ├── TaxReturnPort.java
│   │   │               │   │   │   ├── PaymentPort.java
│   │   │               │   │   │   ├── IntegrationPort.java
│   │   │               │   │   │   ├── IndustryBenchmarkPort.java
│   │   │               │   │   │   │                             ── Infrastructure Ports ──
│   │   │               │   │   │   ├── RiskEventPublisherPort.java
│   │   │               │   │   │   ├── RiskCachePort.java
│   │   │               │   │   │   └── AuditLogPort.java
│   │   │               │   │
│   │   │               │   ├── scheduler
│   │   │               │   │   └── BatchScoringScheduler.java
│   │   │               │   │
│   │   │               │   └── service
│   │   │               │       ├── RiskAssessmentOrchestrator.java
│   │   │               │       └── RiskConfigurationService.java
│   │   │               │
│   │   │               ├── config
│   │   │               │   ├── CacheConfiguration.java
│   │   │               │   ├── KafkaConfiguration.java
│   │   │               │   └── SchedulingConfiguration.java
│   │   │               │
│   │   │               ├── domain
│   │   │               │   │
│   │   │               │   ├── event                             ← domain events
│   │   │               │   │   ├── RiskAssessmentCompletedEvent.java
│   │   │               │   │   ├── RiskProfileUpdatedEvent.java
│   │   │               │   │   └── CriticalRiskAlertEvent.java
│   │   │               │   │
│   │   │               │   ├── model                             ← aggregates
│   │   │               │   │   ├── RiskAssessment.java
│   │   │               │   │   ├── TaxpayerRiskProfile.java
│   │   │               │   │   ├── CategoryScore.java
│   │   │               │   │   ├── IndicatorScore.java
│   │   │               │   │   ├── ConfidenceFactor.java
│   │   │               │   │   ├── HistoricalScore.java
│   │   │               │   │   └── RiskExplanation.java
│   │   │               │   │
│   │   │               │   ├── service                           ← domain services
│   │   │               │   │   ├── RiskScoringService.java
│   │   │               │   │   ├── RiskExplanationService.java
│   │   │               │   │   ├── RiskPrioritizationService.java
│   │   │               │   │   ├── ConfidenceCalculationService.java
│   │   │               │   │   └── TaxpayerData.java
│   │   │               │   │
│   │   │               │   └── valueobject                       ← value objects & enums
│   │   │               │       ├── RiskLevel.java
│   │   │               │       ├── RiskTrend.java
│   │   │               │       ├── AssessmentStatus.java
│   │   │               │       ├── IndicatorCategory.java
│   │   │               │       └── AuditType.java
│   │   │               │
│   │   │               ├── infrastructure
│   │   │               │   │
│   │   │               │   ├── adapter                           ← external adapters
│   │   │               │   │   ├── registration
│   │   │               │   │   │   └── MockRegistrationAdapter.java
│   │   │               │   │   ├── taxreturn
│   │   │               │   │   │   └── MockTaxReturnAdapter.java
│   │   │               │   │   ├── payment
│   │   │               │   │   │   └── MockPaymentAdapter.java
│   │   │               │   │   ├── integration
│   │   │               │   │   │   └── MockIntegrationAdapter.java
│   │   │               │   │   └── benchmark
│   │   │               │   │       └── MockIndustryBenchmarkAdapter.java
│   │   │               │   │
│   │   │               │   ├── cache
│   │   │               │   │   └── RiskCacheService.java
│   │   │               │   │
│   │   │               │   ├── config
│   │   │               │   │   └── IndicatorConfigurationService.java
│   │   │               │   │
│   │   │               │   ├── messaging
│   │   │               │   │   └── RiskEventProducer.java
│   │   │               │   │
│   │   │               │   ├── metrics
│   │   │               │   │   └── RiskEngineMetrics.java
│   │   │               │   │
│   │   │               │   └── service
│   │   │               │       └── AuditService.java
│   │   │               │
│   │   │               ├── persistence
│   │   │               │   │
│   │   │               │   ├── adapter                           ← persistence adapters
│   │   │               │   │   ├── RiskAssessmentPersistenceAdapter.java
│   │   │               │   │   ├── TaxpayerRiskProfilePersistenceAdapter.java
│   │   │               │   │   └── RiskIndicatorConfigPersistenceAdapter.java
│   │   │               │   │
│   │   │               │   └── jpa
│   │   │               │       ├── entity
│   │   │               │       │   ├── RiskAssessmentEntity.java
│   │   │               │       │   ├── TaxpayerRiskProfileEntity.java
│   │   │               │       │   ├── RiskIndicatorConfigEntity.java
│   │   │               │       │   ├── AssessmentCategoryScoreEntity.java
│   │   │               │       │   ├── AssessmentIndicatorScoreEntity.java
│   │   │               │       │   └── AuditLogEntity.java
│   │   │               │       │
│   │   │               │       └── repository
│   │   │               │           ├── RiskAssessmentJpaRepository.java
│   │   │               │           ├── TaxpayerRiskProfileJpaRepository.java
│   │   │               │           └── RiskIndicatorConfigJpaRepository.java
│   │   │               │
│   │   │               └── RiskPracticeApplication.java
│   │   │
│   │   └── resources
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db
│   │           └── migration
│   │               ├── V1__initial_schema.sql
│   │               └── V2__add_performance_indexes.sql
│   │
│   └── test
│       ├── java
│       │   └── com
│       │       └── practice
│       │           └── risk
│       │               ├── api
│       │               │   └── controller
│       │               │       ├── RiskAssessmentControllerTest.java
│       │               │       └── RiskExplanationControllerTest.java
│       │               │
│       │               ├── integration
│       │               │   └── RiskAssessmentFlowIT.java
│       │               │
│       │               └── unit
│       │                   ├── domain
│       │                   │   ├── RiskScoringServiceTest.java
│       │                   │   └── RiskExplanationServiceTest.java
│       │                   │
│       │                   └── application
│       │                       └── RiskAssessmentOrchestratorTest.java
│       │
│       └── resources
│           └── application-test.yml
```

## Layer Responsibilities

### API Layer (`api/`)
- **controller/backoffice**: Admin and analyst endpoints
- **controller/portal**: Public-facing endpoints (explanations)
- **controller/webhook**: Inbound webhooks from other services
- **controller/internal**: Service-to-service APIs
- **dto**: Request/Response DTOs (no business logic)

### Application Layer (`application/`)
- **event**: Inbound domain event handlers
- **port**: Hexagonal architecture ports (interfaces)
- **scheduler**: Scheduled jobs (batch processing)
- **service**: Application services (orchestration)

### Domain Layer (`domain/`)
- **event**: Domain events (published after state changes)
- **model**: Aggregates and entities (business logic)
- **service**: Domain services (complex business logic)
- **valueobject**: Value objects and enums

### Infrastructure Layer (`infrastructure/`)
- **adapter**: External system adapters (implementations of outbound ports)
- **cache**: Caching implementation
- **config**: Configuration services
- **messaging**: Event publishing
- **metrics**: Monitoring and metrics
- **service**: Infrastructure services

### Persistence Layer (`persistence/`)
- **adapter**: Persistence adapters (implement repository ports)
- **jpa/entity**: JPA entities
- **jpa/repository**: Spring Data repositories

## Key Differences from Current Structure

### ✅ ALIGNED (Keep as-is)
- Package structure: `com.practice.risk`
- Domain-driven design approach
- Hexagonal architecture with ports
- Event-driven architecture
- Separation of concerns

### ❌ NEEDS RESTRUCTURING (Fix)

1. **Controller Organization**
   - Current: Single `api/controller/` folder
   - Should be: Organized into `backoffice/`, `portal/`, `webhook/`, `internal/`

2. **Event Location**
   - Current: `domain/event/` doesn't exist yet - events created in root
   - Should be: All domain events in `domain/event/` package

3. **Adapter Organization**
   - Current: `infrastructure/adapter/` flat structure
   - Should be: Organized by external system (`registration/`, `taxreturn/`, etc.)

4. **Persistence Structure**
   - Current: `infrastructure/persistence/`
   - Should be: Top-level `persistence/` package with `adapter/` and `jpa/` subpackages

5. **Value Objects**
   - Current: Mixed in `domain/model/`
   - Should be: Separate `domain/valueobject/` package

## Migration Plan

### Phase 1: Create New Structure (No Code Changes)
1. Create new package structure
2. Keep old files in place

### Phase 2: Move Domain Layer
1. Move events to `domain/event/`
2. Move value objects to `domain/valueobject/`
3. Update imports

### Phase 3: Reorganize Controllers
1. Split controllers into backoffice/portal/webhook/internal
2. Update routing paths if needed

### Phase 4: Reorganize Infrastructure
1. Move adapters to proper subpackages
2. Separate persistence layer

### Phase 5: Update Tests
1. Reorganize test structure to match main structure
2. Update imports

### Phase 6: Cleanup
1. Remove old empty packages
2. Update documentation
3. Verify compilation

## Benefits of This Structure

1. **Consistency**: Matches Tax Audit Service structure
2. **Scalability**: Easy to add new controllers/adapters
3. **Clarity**: Clear separation of concerns
4. **Maintainability**: Easy to navigate and understand
5. **Team Collaboration**: Developers can work in parallel on different layers
6. **Testing**: Clear boundaries for unit vs integration tests

## Current Status

- ✅ Domain models properly separated
- ✅ Events properly defined
- ✅ Ports and adapters pattern implemented
- ❌ Controller organization needs restructuring
- ❌ Package structure needs refinement
- ❌ Test structure needs alignment

## Next Action

Run the restructuring script to move files to the correct locations while preserving all functionality.
