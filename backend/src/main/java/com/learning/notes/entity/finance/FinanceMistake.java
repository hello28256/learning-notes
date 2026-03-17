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
@Table(name = "finance_mistakes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceMistake {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "mistake_date", nullable = false)
    private LocalDate mistakeDate;
    
    @Column(name = "mistake_category", nullable = false, length = 50)
    private String mistakeCategory;
    
    @Column(length = 50)
    private String symbol;
    
    @Column(name = "emotion_state", length = 100)
    private String emotionState;
    
    @Column(name = "loss_amount", precision = 18, scale = 4)
    private BigDecimal lossAmount;
    
    @Column(name = "opportunity_cost", precision = 18, scale = 4)
    private BigDecimal opportunityCost;
    
    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;
    
    @Column(name = "lesson_learned", columnDefinition = "TEXT")
    private String lessonLearned;
    
    @Column(name = "next_rule", columnDefinition = "TEXT")
    private String nextRule;
    
    @Column(name = "is_reviewed")
    private Boolean isReviewed;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isReviewed == null) isReviewed = false;
        if (lossAmount == null) lossAmount = BigDecimal.ZERO;
        if (opportunityCost == null) opportunityCost = BigDecimal.ZERO;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
