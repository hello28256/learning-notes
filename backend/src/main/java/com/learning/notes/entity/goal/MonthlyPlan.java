package com.learning.notes.entity.goal;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private Integer quarter;
    
    private Integer month;
    
    @Column(name = "plan_type", nullable = false, length = 20)
    private String planType;
    
    @Column(name = "focus_areas", columnDefinition = "TEXT")
    private String focusAreas;
    
    @Column(name = "action_items", columnDefinition = "TEXT")
    private String actionItems;
    
    @Column(name = "related_goal_ids", length = 300)
    private String relatedGoalIds;
    
    @Column(name = "review_summary", columnDefinition = "TEXT")
    private String reviewSummary;
    
    @Column(name = "is_completed")
    private Boolean isCompleted;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isCompleted == null) isCompleted = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
