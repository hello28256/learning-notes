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
@Table(name = "finance_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(length = 50)
    private String symbol;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;
    
    @Column(precision = 18, scale = 6)
    private BigDecimal quantity;
    
    @Column(precision = 18, scale = 4)
    private BigDecimal price;
    
    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;
    
    @Column(precision = 18, scale = 4)
    private BigDecimal fee;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fee == null) fee = BigDecimal.ZERO;
    }
}
