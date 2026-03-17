package com.learning.notes.entity.learning;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reading_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "book_title", nullable = false, length = 200)
    private String bookTitle;
    
    @Column(length = 100)
    private String author;
    
    @Column(name = "cover_image", length = 500)
    private String coverImage;
    
    private Integer rating;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "finish_date")
    private LocalDate finishDate;
    
    @Column(name = "key_points", columnDefinition = "TEXT")
    private String keyPoints;
    
    @Column(name = "actionable_item", length = 500)
    private String actionableItem;
    
    @Column(columnDefinition = "TEXT")
    private String quotes;
    
    @Column(name = "personal_thoughts", columnDefinition = "TEXT")
    private String personalThoughts;
    
    @Column(name = "related_mental_models", length = 300)
    private String relatedMentalModels;
    
    @Column(name = "related_rules", length = 300)
    private String relatedRules;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "reading";
        if (rating == null) rating = 3;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
