package com.learning.notes.service.learning;

import com.learning.notes.entity.learning.KnowledgeBase;
import com.learning.notes.repository.learning.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public List<KnowledgeBase> getUserKnowledge(Long userId) {
        return knowledgeBaseRepository.findByUserId(userId);
    }

    public List<KnowledgeBase> getKnowledgeByCategory(Long userId, String category) {
        return knowledgeBaseRepository.findByUserIdAndCategory(userId, category);
    }

    public List<KnowledgeBase> getDueForReview(Long userId) {
        return knowledgeBaseRepository.findByUserIdAndNextReviewAtLessThanEqual(userId, LocalDate.now());
    }

    public KnowledgeBase getKnowledgeById(Long id, Long userId) {
        return knowledgeBaseRepository.findById(id)
                .filter(k -> k.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("知识卡片不存在"));
    }

    @Transactional
    public KnowledgeBase createKnowledge(KnowledgeBase knowledge, Long userId) {
        knowledge.setUserId(userId);
        knowledge.setReviewCount(0);
        return knowledgeBaseRepository.save(knowledge);
    }

    @Transactional
    public KnowledgeBase updateKnowledge(Long id, KnowledgeBase knowledge, Long userId) {
        KnowledgeBase existing = getKnowledgeById(id, userId);
        existing.setCategory(knowledge.getCategory());
        existing.setTitle(knowledge.getTitle());
        existing.setConcept(knowledge.getConcept());
        existing.setKeyExamples(knowledge.getKeyExamples());
        existing.setCommonMistakes(knowledge.getCommonMistakes());
        existing.setRelatedLinks(knowledge.getRelatedLinks());
        existing.setRelatedNotes(knowledge.getRelatedNotes());
        existing.setMasteryLevel(knowledge.getMasteryLevel());
        return knowledgeBaseRepository.save(existing);
    }

    @Transactional
    public KnowledgeBase reviewKnowledge(Long id, Long userId) {
        KnowledgeBase knowledge = getKnowledgeById(id, userId);
        knowledge.setReviewCount(knowledge.getReviewCount() + 1);
        knowledge.setLastReviewedAt(LocalDateTime.now());
        
        int daysUntilNext = calculateNextReviewInterval(knowledge.getReviewCount());
        knowledge.setNextReviewAt(LocalDate.now().plusDays(daysUntilNext));
        
        return knowledgeBaseRepository.save(knowledge);
    }

    private int calculateNextReviewInterval(int reviewCount) {
        switch (reviewCount) {
            case 1: return 1;
            case 2: return 3;
            case 3: return 7;
            case 4: return 14;
            case 5: return 30;
            default: return 60;
        }
    }

    @Transactional
    public void deleteKnowledge(Long id, Long userId) {
        KnowledgeBase knowledge = getKnowledgeById(id, userId);
        knowledgeBaseRepository.delete(knowledge);
    }
}
