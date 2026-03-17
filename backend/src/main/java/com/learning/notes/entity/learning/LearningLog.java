package com.learning.notes.entity.learning;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(length = 100)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "output_summary", length = 500)
    private String outputSummary;
    
    @Column(name = "output_link", length = 500)
    private String outputLink;
    
    @Column(length = 300)
    private String tags;
    
    @Column(name = "mood_score")
    private Integer moodScore;
    
    @Column(name = "energy_level")
    private Integer energyLevel;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (durationMinutes == null) durationMinutes = 0;
        if (moodScore == null) moodScore = 3;
        if (energyLevel == null) energyLevel = 3;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
