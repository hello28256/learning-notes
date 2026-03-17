package com.learning.notes.service.finance;

import com.learning.notes.entity.finance.FinanceDecision;
import com.learning.notes.repository.finance.FinanceDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceDecisionService {

    private final FinanceDecisionRepository decisionRepository;

    public List<FinanceDecision> getUserDecisions(Long userId) {
        return decisionRepository.findByUserId(userId);
    }

    public List<FinanceDecision> getDecisionsByType(Long userId, String type) {
        return decisionRepository.findByUserIdAndDecisionType(userId, type);
    }

    public List<FinanceDecision> getPendingReviewDecisions(Long userId) {
        return decisionRepository.findByUserIdAndReviewReminderDateLessThanEqualAndReviewResultIsNull(
                userId, LocalDate.now());
    }

    public FinanceDecision getDecisionById(Long id, Long userId) {
        return decisionRepository.findById(id)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("决策记录不存在"));
    }

    @Transactional
    public FinanceDecision createDecision(FinanceDecision decision, Long userId) {
        decision.setUserId(userId);
        return decisionRepository.save(decision);
    }

    @Transactional
    public FinanceDecision updateDecision(Long id, FinanceDecision decision, Long userId) {
        FinanceDecision existing = getDecisionById(id, userId);
        existing.setSymbol(decision.getSymbol());
        existing.setDecisionType(decision.getDecisionType());
        existing.setDecisionDate(decision.getDecisionDate());
        existing.setReasons(decision.getReasons());
        existing.setRisks(decision.getRisks());
        existing.setExpectedPeriod(decision.getExpectedPeriod());
        existing.setExpectedReturn(decision.getExpectedReturn());
        existing.setTriggerCondition(decision.getTriggerCondition());
        existing.setStopLossRule(decision.getStopLossRule());
        existing.setPositionSize(decision.getPositionSize());
        existing.setReviewReminderDate(decision.getReviewReminderDate());
        return decisionRepository.save(existing);
    }

    @Transactional
    public FinanceDecision reviewDecision(Long id, String result, String notes, Long userId) {
        FinanceDecision decision = getDecisionById(id, userId);
        decision.setReviewResult(result);
        decision.setReviewNotes(notes);
        decision.setReviewedAt(java.time.LocalDateTime.now());
        return decisionRepository.save(decision);
    }

    @Transactional
    public void deleteDecision(Long id, Long userId) {
        FinanceDecision decision = getDecisionById(id, userId);
        decisionRepository.delete(decision);
    }
}
