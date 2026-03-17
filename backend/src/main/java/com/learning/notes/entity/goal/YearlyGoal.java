package com.learning.notes.entity.goal;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "yearly_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(name = "goal_title", nullable = false, length = 200)
    private String goalTitle;
    
    @Column(name = "goal_description", columnDefinition = "TEXT")
    private String goalDescription;
    
    @Column(length = 50)
    private String category;
    
    @Column(name = "key_results", columnDefinition = "TEXT")
    private String keyResults;
    
    @Column(name = "progress_percent")
    private Integer progressPercent;
    
    private Integer priority;
    
    @Column(length = 20)
    private String status;
    
    private LocalDate deadline;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (progressPercent == null) progressPercent = 0;
        if (priority == null) priority = 1;
        if (status == null) status = "active";
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
