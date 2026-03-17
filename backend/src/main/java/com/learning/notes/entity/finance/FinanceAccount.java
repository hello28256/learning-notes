package com.learning.notes.entity.finance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;
    
    @Column(precision = 18, scale = 4)
    private BigDecimal balance;
    
    @Column(length = 10)
    private String currency;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "risk_level")
    private Integer riskLevel;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balance == null) balance = BigDecimal.ZERO;
        if (currency == null) currency = "CNY";
        if (isActive == null) isActive = true;
        if (riskLevel == null) riskLevel = 1;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
