package com.learning.notes.repository.learning;

import com.learning.notes.entity.learning.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    List<KnowledgeBase> findByUserId(Long userId);
    List<KnowledgeBase> findByUserIdAndCategory(Long userId, String category);
    List<KnowledgeBase> findByUserIdAndIsActiveTrue(Long userId);
    List<KnowledgeBase> findByUserIdAndNextReviewAtLessThanEqual(Long userId, LocalDate date);
}
