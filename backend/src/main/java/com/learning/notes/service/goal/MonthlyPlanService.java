package com.learning.notes.service.goal;

import com.learning.notes.entity.goal.MonthlyPlan;
import com.learning.notes.repository.goal.MonthlyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonthlyPlanService {

    private final MonthlyPlanRepository monthlyPlanRepository;

    public List<MonthlyPlan> getUserPlans(Long userId) {
        return monthlyPlanRepository.findByUserId(userId);
    }

    public List<MonthlyPlan> getPlansByYear(Long userId, Integer year) {
        return monthlyPlanRepository.findByUserIdAndYear(userId, year);
    }

    public List<MonthlyPlan> getPlansByQuarter(Long userId, Integer year, Integer quarter) {
        return monthlyPlanRepository.findByUserIdAndYearAndQuarter(userId, year, quarter);
    }

    public Optional<MonthlyPlan> getMonthlyPlan(Long userId, Integer year, Integer month) {
        return monthlyPlanRepository.findByUserIdAndYearAndMonth(userId, year, month);
    }

    public MonthlyPlan getPlanById(Long id, Long userId) {
        return monthlyPlanRepository.findById(id)
                .filter(p -> p.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("计划不存在"));
    }

    @Transactional
    public MonthlyPlan createPlan(MonthlyPlan plan, Long userId) {
        plan.setUserId(userId);
        plan.setIsCompleted(false);
        return monthlyPlanRepository.save(plan);
    }

    @Transactional
    public MonthlyPlan updatePlan(Long id, MonthlyPlan plan, Long userId) {
        MonthlyPlan existing = getPlanById(id, userId);
        existing.setYear(plan.getYear());
        existing.setQuarter(plan.getQuarter());
        existing.setMonth(plan.getMonth());
        existing.setPlanType(plan.getPlanType());
        existing.setFocusAreas(plan.getFocusAreas());
        existing.setActionItems(plan.getActionItems());
        existing.setRelatedGoalIds(plan.getRelatedGoalIds());
        return monthlyPlanRepository.save(existing);
    }

    @Transactional
    public MonthlyPlan completeReview(Long id, String summary, Long userId) {
        MonthlyPlan plan = getPlanById(id, userId);
        plan.setIsCompleted(true);
        plan.setReviewSummary(summary);
        return monthlyPlanRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long id, Long userId) {
        MonthlyPlan plan = getPlanById(id, userId);
        monthlyPlanRepository.delete(plan);
    }
}
