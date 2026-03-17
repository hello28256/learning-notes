package com.learning.notes.entity.learning;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_base")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String concept;
    
    @Column(name = "key_examples", columnDefinition = "TEXT")
    private String keyExamples;
    
    @Column(name = "common_mistakes", columnDefinition = "TEXT")
    private String commonMistakes;
    
    @Column(name = "related_links", columnDefinition = "TEXT")
    private String relatedLinks;
    
    @Column(name = "related_notes", length = 300)
    private String relatedNotes;
    
    @Column(name = "mastery_level")
    private Integer masteryLevel;
    
    @Column(name = "review_count")
    private Integer reviewCount;
    
    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;
    
    @Column(name = "next_review_at")
    private LocalDate nextReviewAt;
    
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
        if (masteryLevel == null) masteryLevel = 1;
        if (reviewCount == null) reviewCount = 0;
        if (isActive == null) isActive = true;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
