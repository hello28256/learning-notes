package com.learning.notes.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 笔记分享实体类
 */
@Entity
@Table(name = "note_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "share_code", nullable = false, unique = true, length = 20)
    private String shareCode;

    @Column(name = "share_title", length = 100)
    private String shareTitle;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    private Integer viewCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", columnDefinition = "TINYINT DEFAULT 1")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
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

    /**
     * 检查分享是否过期
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 检查分享是否有效
     */
    public boolean isValid() {
        return isActive != null && isActive && !isExpired();
    }
}
