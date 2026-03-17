package com.learning.notes.entity.finance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_decisions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceDecision {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 50)
    private String symbol;
    
    @Column(name = "decision_type", nullable = false, length = 20)
    private String decisionType;
    
    @Column(name = "decision_date", nullable = false)
    private LocalDate decisionDate;
    
    @Column(columnDefinition = "TEXT")
    private String reasons;
    
    @Column(columnDefinition = "TEXT")
    private String risks;
    
    @Column(name = "expected_period", length = 20)
    private String expectedPeriod;
    
    @Column(name = "expected_return", precision = 5, scale = 2)
    private BigDecimal expectedReturn;
    
    @Column(name = "trigger_condition", length = 500)
    private String triggerCondition;
    
    @Column(name = "stop_loss_rule", length = 500)
    private String stopLossRule;
    
    @Column(name = "position_size", precision = 5, scale = 2)
    private BigDecimal positionSize;
    
    @Column(name = "review_reminder_date")
    private LocalDate reviewReminderDate;
    
    @Column(name = "review_result", length = 20)
    private String reviewResult;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "active";
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
