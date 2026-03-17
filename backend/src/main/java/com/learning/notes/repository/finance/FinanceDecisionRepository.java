package com.learning.notes.repository.finance;

import com.learning.notes.entity.finance.FinanceDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceDecisionRepository extends JpaRepository<FinanceDecision, Long> {
    List<FinanceDecision> findByUserId(Long userId);
    List<FinanceDecision> findByUserIdAndStatus(Long userId, String status);
    List<FinanceDecision> findByUserIdAndDecisionType(Long userId, String decisionType);
    List<FinanceDecision> findByUserIdAndReviewReminderDateLessThanEqualAndReviewResultIsNull(Long userId, LocalDate date);
}
