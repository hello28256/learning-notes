package com.learning.notes.entity.system;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rule_library")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleLibrary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;
    
    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;
    
    @Column(name = "rule_content", nullable = false, columnDefinition = "TEXT")
    private String ruleContent;
    
    @Column(name = "rule_reason", columnDefinition = "TEXT")
    private String ruleReason;
    
    @Column(name = "violation_cases", columnDefinition = "TEXT")
    private String violationCases;
    
    @Column(name = "application_count")
    private Integer applicationCount;
    
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    private Integer priority;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (applicationCount == null) applicationCount = 0;
        if (isActive == null) isActive = true;
        if (priority == null) priority = 1;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
