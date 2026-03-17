package com.learning.notes.entity.learning;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "review_type", nullable = false, length = 20)
    private String reviewType;
    
    @Column(name = "review_period", nullable = false, length = 50)
    private String reviewPeriod;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "what_done", columnDefinition = "TEXT")
    private String whatDone;
    
    @Column(name = "what_right", columnDefinition = "TEXT")
    private String whatRight;
    
    @Column(name = "what_improve", columnDefinition = "TEXT")
    private String whatImprove;
    
    @Column(name = "goal_progress", columnDefinition = "TEXT")
    private String goalProgress;
    
    @Column(name = "asset_changes", columnDefinition = "TEXT")
    private String assetChanges;
    
    @Column(name = "learning_achievements", columnDefinition = "TEXT")
    private String learningAchievements;
    
    @Column(name = "emotion_state", length = 200)
    private String emotionState;
    
    @Column(name = "key_decisions", columnDefinition = "TEXT")
    private String keyDecisions;
    
    @Column(name = "key_growth", columnDefinition = "TEXT")
    private String keyGrowth;
    
    @Column(name = "next_year_direction", columnDefinition = "TEXT")
    private String nextYearDirection;
    
    @Column(name = "satisfaction_score")
    private Integer satisfactionScore;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
