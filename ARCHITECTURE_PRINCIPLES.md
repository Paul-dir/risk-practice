# Risk Engine Architecture Principles
## For Government Tax Audit System

### Core Responsibility
The Risk Engine is **ONLY** responsible for:
1. **Assessing risk** - Calculate risk scores
2. **Explaining risk** - Generate transparent justifications  
3. **Profiling risk** - Maintain historical risk data
4. **Benchmarking** - Compare against industry standards

### What Risk Engine Does NOT Do

#### ❌ NO Workflow Decisions
```java
// WRONG - This is Workflow Engine responsibility
if (riskScore > 80) {
    workflowEngine.escalate(case);  // ❌ NO!
}

// RIGHT - Risk Engine only provides the score
return RiskAssessment.builder()
    .overallScore(riskScore)
    .build();  // ✅ YES - let consumers decide what to do
```

#### ❌ NO Case Assignment
```java
// WRONG - This is Audit Module responsibility
assignCaseToAuditor(taxpayerId, auditorId);  // ❌ NO!

// RIGHT - Risk Engine only suggests
return RiskAssessment.builder()
    .priorityRank(calculatePriority())  // ✅ Suggestion only
    .build();
```

#### ❌ NO Audit Type Selection
```java
// WRONG - This is too prescriptive
recommendedAuditType(AuditType.COMPREHENSIVE);  // ❌ Risky

// RIGHT - Provide risk profile, let Audit Module decide
return RiskAssessment.builder()
    .riskLevel(RiskLevel.HIGH)
    .riskCategories(categories)  // ✅ Provide data
    .indicators(indicators)      // ✅ Let consumer interpret
    .build();
```

### Boundary Definition

```
┌─────────────────────────────────────────────────────────────┐
│                      RISK ENGINE                            │
│                                                             │
│  INPUT:                                                     │
│   • Taxpayer ID                                            │
│   • Historical data (via ports)                            │
│   • Configuration (indicators, weights, thresholds)         │
│                                                             │
│  PROCESSING:                                               │
│   • Data collection                                        │
│   • Indicator evaluation                                   │
│   • Score calculation                                      │
│   • Confidence computation                                 │
│   • Benchmark comparison                                   │
│   • Explanation generation                                 │
│                                                             │
│  OUTPUT:                                                    │
│   • Risk Score (0-100)                                     │
│   • Risk Level (LOW/MEDIUM/HIGH/CRITICAL)                  │
│   • Confidence Factor (0.0-1.0)                            │
│   • Category Scores (Filing, Payment, Financial, etc.)     │
│   • Indicator Scores (Late Filing, Non-Paying, etc.)       │
│   • Risk Explanation (why this score?)                     │
│   • Historical Trend (IMPROVING/STABLE/DETERIORATING)      │
│                                                             │
│  EVENTS PUBLISHED:                                         │
│   • RiskAssessmentCompletedEvent                           │
│   • RiskProfileUpdatedEvent                                │
│   • CriticalRiskAlertEvent (when score > threshold)        │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   EXTERNAL CONSUMERS                         │
│                                                             │
│  Audit Module:                                             │
│   • Receives RiskAssessmentCompletedEvent                  │
│   • Decides: Comprehensive vs Desk vs Issue audit          │
│   • Creates audit case                                     │
│   • Assigns to auditor                                     │
│                                                             │
│  Workflow Engine:                                          │
│   • Receives CriticalRiskAlertEvent                        │
│   • Routes for escalation                                  │
│   • Manages approval flows                                 │
│                                                             │
│  Rule Engine:                                              │
│   • Applies business rules                                 │
│   • Determines CAAT eligibility                            │
│   • Validates audit thresholds                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Integration Pattern

#### Event-Driven Communication (Recommended)
```java
// Risk Engine publishes
@Service
public class RiskAssessmentOrchestrator {
    
    public RiskAssessment assessTaxpayer(UUID taxpayerId) {
        // 1. Collect data
        // 2. Calculate risk
        // 3. Store assessment
        // 4. Publish event - THAT'S IT!
        
        eventProducer.publish(RiskAssessmentCompletedEvent.builder()
            .assessmentId(assessment.getId())
            .taxpayerId(taxpayerId)
            .overallScore(assessment.getOverallScore())
            .riskLevel(assessment.getRiskLevel())
            .categoryScores(assessment.getCategoryScores())  // Rich data
            .indicators(assessment.getIndicatorScores())     // Rich data
            .build());
            
        return assessment;
    }
}

// Audit Module listens and decides
@Component
public class AuditCaseCreationListener {
    
    @EventListener
    public void onRiskAssessment(RiskAssessmentCompletedEvent event) {
        // AUDIT MODULE makes the decision
        if (shouldCreateAuditCase(event)) {
            AuditType type = determineAuditType(event);  // ✅ Consumer decides
            createAuditCase(event.getTaxpayerId(), type);
        }
    }
    
    private boolean shouldCreateAuditCase(RiskAssessmentCompletedEvent event) {
        // Business logic here (maybe call Rule Engine)
        return event.getRiskLevel() == RiskLevel.HIGH || 
               event.getRiskLevel() == RiskLevel.CRITICAL;
    }
    
    private AuditType determineAuditType(RiskAssessmentCompletedEvent event) {
        // Check specific indicators
        boolean hasTransferPricingRisk = event.getIndicators().stream()
            .anyMatch(i -> "RELATED_PARTY_TRANSACTIONS".equals(i.getIndicatorCode()) 
                        && i.getScore() > 50);
                        
        if (hasTransferPricingRisk) return AuditType.TRANSFER_PRICING;
        if (event.getOverallScore() > 70) return AuditType.COMPREHENSIVE;
        return AuditType.DESK;
    }
}
```

### API Design

#### ✅ Good Risk Engine API
```java
@RestController
@RequestMapping("/api/v1/risk")
public class RiskAssessmentController {
    
    // Core assessment
    @PostMapping("/assess/{taxpayerId}")
    public RiskAssessmentResponse assess(@PathVariable UUID taxpayerId);
    
    // Batch processing
    @PostMapping("/assess/batch")
    public List<RiskAssessmentResponse> assessBatch(@RequestBody List<UUID> taxpayerIds);
    
    // Get latest assessment
    @GetMapping("/assess/{taxpayerId}/latest")
    public RiskAssessmentResponse getLatest(@PathVariable UUID taxpayerId);
    
    // Get risk profile (historical)
    @GetMapping("/profile/{taxpayerId}")
    public RiskProfileResponse getProfile(@PathVariable UUID taxpayerId);
    
    // Get risk trend
    @GetMapping("/profile/{taxpayerId}/trend")
    public RiskTrendResponse getTrend(@PathVariable UUID taxpayerId);
    
    // Get risk explanation
    @GetMapping("/explain/{assessmentId}")
    public RiskExplanationResponse explain(@PathVariable UUID assessmentId);
}
```

#### ❌ Bad Risk Engine API (Too Many Responsibilities)
```java
// WRONG - These belong to other modules
@PostMapping("/assign-to-auditor")  // ❌ Audit Module
@PostMapping("/create-audit-case")  // ❌ Audit Module
@PostMapping("/escalate-case")      // ❌ Workflow Engine
@PostMapping("/approve-selection")  // ❌ Workflow Engine
```

### Configuration Separation

#### Risk Engine Configuration (OK)
```yaml
risk-engine:
  indicators:
    late-filing:
      weight: 0.25
      thresholds:
        low: 30
        medium: 90
        high: 180
  categories:
    filing-compliance:
      weight: 0.25
    payment-compliance:
      weight: 0.25
```

#### Audit Module Configuration (Separate)
```yaml
audit-module:
  case-selection:
    min-risk-score: 65.0        # ✅ Audit Module decides threshold
    auto-assign: true           # ✅ Audit Module decides workflow
    caat-eligibility: 
      enabled: true             # ✅ Rule Engine decides this
```

### Data Ownership

| Data | Owner | Why |
|------|-------|-----|
| Risk Score | Risk Engine | Core calculation |
| Risk Level | Risk Engine | Risk classification |
| Risk Indicators | Risk Engine | Score components |
| Risk Profile | Risk Engine | Historical tracking |
| Audit Case | Audit Module | Audit management |
| Case Assignment | Audit Module | Resource allocation |
| Audit Type | Audit Module | Audit planning |
| Workflow State | Workflow Engine | Process management |
| Business Rules | Rule Engine | Rule execution |

### Testing Strategy

#### Unit Tests (Risk Engine Only)
```java
@Test
public void shouldCalculateRiskScore() {
    // Arrange
    TaxpayerData data = TaxpayerData.builder()
        .lateFilingDays(95)
        .numberOfAmendments(4)
        .build();
    
    // Act
    RiskAssessment assessment = scoringService.assess(taxpayerId, "TIN123", data, configService);
    
    // Assert
    assertThat(assessment.getOverallScore()).isGreaterThan(50);
    assertThat(assessment.getRiskLevel()).isIn(RiskLevel.MEDIUM, RiskLevel.HIGH);
}
```

#### Integration Tests (Boundary Verification)
```java
@Test
public void shouldPublishEventButNotAssignCase() {
    // Arrange
    UUID taxpayerId = UUID.randomUUID();
    
    // Act
    orchestrator.assessTaxpayer(taxpayerId, "TIN123");
    
    // Assert - Only verify Risk Engine responsibilities
    verify(eventProducer).publishAssessmentCompleted(any());
    verify(assessmentRepository).save(any());
    verify(profileRepository).save(any());
    
    // ❌ Should NOT verify these (different module)
    verify(auditCaseRepository, never()).save(any());
    verify(auditorAssignmentService, never()).assign(any());
}
```

### Summary

**Focus on what the Risk Engine MUST do exceptionally well:**

1. ✅ Accurate risk scoring
2. ✅ Transparent explanations
3. ✅ Configurable indicators
4. ✅ Performance at scale (batch processing)
5. ✅ Historical tracking
6. ✅ Audit logging

**Avoid feature creep:**

1. ❌ Don't build workflow logic
2. ❌ Don't assign cases to auditors
3. ❌ Don't create audit cases
4. ❌ Don't execute business rules
5. ❌ Don't make routing decisions

**Remember:** A focused, well-designed Risk Engine that does ONE thing exceptionally well is far more valuable than a bloated system that tries to do everything.
