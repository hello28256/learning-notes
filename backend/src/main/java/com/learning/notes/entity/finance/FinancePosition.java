package com.learning.notes.entity.finance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancePosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(nullable = false, length = 50)
    private String symbol;
    
    @Column(name = "position_type", nullable = false, length = 20)
    private String positionType;
    
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "avg_cost", nullable = false, precision = 18, scale = 4)
    private BigDecimal avgCost;
    
    @Column(name = "current_price", precision = 18, scale = 4)
    private BigDecimal currentPrice;
    
    @Column(name = "market_value", precision = 18, scale = 4)
    private BigDecimal marketValue;
    
    @Column(name = "unrealized_pnl", precision = 18, scale = 4)
    private BigDecimal unrealizedPnl;
    
    @Column(name = "realized_pnl", precision = 18, scale = 4)
    private BigDecimal realizedPnl;
    
    @Column(name = "holding_logic", columnDefinition = "TEXT")
    private String holdingLogic;
    
    @Column(name = "target_price", precision = 18, scale = 4)
    private BigDecimal targetPrice;
    
    @Column(name = "stop_loss_price", precision = 18, scale = 4)
    private BigDecimal stopLossPrice;
    
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
        if (isActive == null) isActive = true;
        if (realizedPnl == null) realizedPnl = BigDecimal.ZERO;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
