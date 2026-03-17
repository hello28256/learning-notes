package com.learning.notes.repository.finance;

import com.learning.notes.entity.finance.FinanceMistake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceMistakeRepository extends JpaRepository<FinanceMistake, Long> {
    List<FinanceMistake> findByUserId(Long userId);
    List<FinanceMistake> findByUserIdAndMistakeCategory(Long userId, String category);
    List<FinanceMistake> findByUserIdAndIsReviewed(Long userId, Boolean isReviewed);
}
