package com.learning.notes.repository.goal;

import com.learning.notes.entity.goal.YearlyGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YearlyGoalRepository extends JpaRepository<YearlyGoal, Long> {
    List<YearlyGoal> findByUserId(Long userId);
    List<YearlyGoal> findByUserIdAndYear(Long userId, Integer year);
    List<YearlyGoal> findByUserIdAndYearAndStatus(Long userId, Integer year, String status);
    List<YearlyGoal> findByUserIdAndCategory(Long userId, String category);
}
