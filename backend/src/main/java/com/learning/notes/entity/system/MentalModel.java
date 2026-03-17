package com.learning.notes.entity.system;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mental_models")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentalModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;
    
    @Column(columnDefinition = "TEXT")
    private String definition;
    
    @Column(name = "application_scenarios", columnDefinition = "TEXT")
    private String applicationScenarios;
    
    @Column(name = "real_cases", columnDefinition = "TEXT")
    private String realCases;
    
    @Column(name = "related_readings", length = 500)
    private String relatedReadings;
    
    @Column(name = "usage_count")
    private Integer usageCount;
    
    @Column(name = "mastery_level")
    private Integer masteryLevel;
    
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
        if (usageCount == null) usageCount = 0;
        if (masteryLevel == null) masteryLevel = 1;
        if (isActive == null) isActive = true;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
