package com.learning.notes.repository;

import com.learning.notes.entity.NoteShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 笔记分享数据访问层
 */
@Repository
public interface NoteShareRepository extends JpaRepository<NoteShare, Long> {

    /**
     * 根据分享短码查找
     */
    Optional<NoteShare> findByShareCode(String shareCode);

    /**
     * 检查分享短码是否已存在
     */
    boolean existsByShareCode(String shareCode);

    /**
     * 根据笔记ID和创建者查找有效的分享
     */
    Optional<NoteShare> findByNoteIdAndCreatedByAndIsActiveTrue(Long noteId, Long createdBy);
}
