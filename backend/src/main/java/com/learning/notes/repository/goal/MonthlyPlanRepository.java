package com.learning.notes.repository.goal;

import com.learning.notes.entity.goal.MonthlyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyPlanRepository extends JpaRepository<MonthlyPlan, Long> {
    List<MonthlyPlan> findByUserId(Long userId);
    List<MonthlyPlan> findByUserIdAndYear(Long userId, Integer year);
    List<MonthlyPlan> findByUserIdAndYearAndQuarter(Long userId, Integer year, Integer quarter);
    Optional<MonthlyPlan> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    List<MonthlyPlan> findByUserIdAndPlanType(Long userId, String planType);
}
