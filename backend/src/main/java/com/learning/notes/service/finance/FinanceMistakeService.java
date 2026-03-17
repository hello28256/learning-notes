package com.learning.notes.service.finance;

import com.learning.notes.entity.finance.FinanceMistake;
import com.learning.notes.repository.finance.FinanceMistakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceMistakeService {

    private final FinanceMistakeRepository mistakeRepository;

    public List<FinanceMistake> getUserMistakes(Long userId) {
        return mistakeRepository.findByUserId(userId);
    }

    public List<FinanceMistake> getMistakesByCategory(Long userId, String category) {
        return mistakeRepository.findByUserIdAndMistakeCategory(userId, category);
    }

    public List<FinanceMistake> getUnreviewedMistakes(Long userId) {
        return mistakeRepository.findByUserIdAndIsReviewed(userId, false);
    }

    public FinanceMistake getMistakeById(Long id, Long userId) {
        return mistakeRepository.findById(id)
                .filter(m -> m.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("错误记录不存在"));
    }

    @Transactional
    public FinanceMistake createMistake(FinanceMistake mistake, Long userId) {
        mistake.setUserId(userId);
        mistake.setIsReviewed(false);
        return mistakeRepository.save(mistake);
    }

    @Transactional
    public FinanceMistake updateMistake(Long id, FinanceMistake mistake, Long userId) {
        FinanceMistake existing = getMistakeById(id, userId);
        existing.setMistakeDate(mistake.getMistakeDate());
        existing.setMistakeCategory(mistake.getMistakeCategory());
        existing.setSymbol(mistake.getSymbol());
        existing.setEmotionState(mistake.getEmotionState());
        existing.setLossAmount(mistake.getLossAmount());
        existing.setOpportunityCost(mistake.getOpportunityCost());
        existing.setRootCause(mistake.getRootCause());
        existing.setLessonLearned(mistake.getLessonLearned());
        existing.setNextRule(mistake.getNextRule());
        return mistakeRepository.save(existing);
    }

    @Transactional
    public FinanceMistake markAsReviewed(Long id, Long userId) {
        FinanceMistake mistake = getMistakeById(id, userId);
        mistake.setIsReviewed(true);
        return mistakeRepository.save(mistake);
    }

    @Transactional
    public void deleteMistake(Long id, Long userId) {
        FinanceMistake mistake = getMistakeById(id, userId);
        mistakeRepository.delete(mistake);
    }
}
