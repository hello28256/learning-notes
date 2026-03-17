package com.learning.notes.controller.goal;

import com.learning.notes.entity.goal.MonthlyPlan;
import com.learning.notes.entity.goal.YearlyGoal;
import com.learning.notes.service.goal.MonthlyPlanService;
import com.learning.notes.service.goal.YearlyGoalService;
import com.learning.notes.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final YearlyGoalService yearlyGoalService;
    private final MonthlyPlanService monthlyPlanService;

    // ==================== 查询接口（公开访问）====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getGoalsDashboard() {
        Long userId = UserContext.getCurrentUserId();
        int currentYear = LocalDate.now().getYear();

        Map<String, Object> dashboard = new HashMap<>();
        List<YearlyGoal> yearlyGoals = yearlyGoalService.getGoalsByYear(userId, currentYear);
        List<MonthlyPlan> monthlyPlans = monthlyPlanService.getPlansByYear(userId, currentYear);

        double avgProgress = yearlyGoals.isEmpty() ? 0
                : yearlyGoals.stream().mapToInt(YearlyGoal::getProgressPercent).average().orElse(0);

        dashboard.put("yearlyGoals", yearlyGoals);
        dashboard.put("monthlyPlans", monthlyPlans);
        dashboard.put("averageProgress", Math.round(avgProgress));
        dashboard.put("currentYear", currentYear);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/yearly")
    public ResponseEntity<List<YearlyGoal>> getYearlyGoals(@RequestParam(required = false) Integer year) {
        Long userId = UserContext.getCurrentUserId();
        if (year == null)
            year = LocalDate.now().getYear();
        return ResponseEntity.ok(yearlyGoalService.getGoalsByYear(userId, year));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyPlan>> getMonthlyPlans(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter) {
        Long userId = UserContext.getCurrentUserId();
        if (year == null)
            year = LocalDate.now().getYear();

        if (quarter != null) {
            return ResponseEntity.ok(monthlyPlanService.getPlansByQuarter(userId, year, quarter));
        }
        return ResponseEntity.ok(monthlyPlanService.getPlansByYear(userId, year));
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyPlan> getMonthlyPlan(@PathVariable Integer year, @PathVariable Integer month) {
        return ResponseEntity.ok(monthlyPlanService.getMonthlyPlan(UserContext.getCurrentUserId(), year, month)
                .orElse(null));
    }

    // ==================== 修改接口（需要登录）====================

    @PostMapping("/yearly")
    public ResponseEntity<YearlyGoal> createYearlyGoal(@RequestBody YearlyGoal goal) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(yearlyGoalService.createGoal(goal, UserContext.getCurrentUserId()));
    }

    @PutMapping("/yearly/{id}")
    public ResponseEntity<YearlyGoal> updateYearlyGoal(@PathVariable Long id, @RequestBody YearlyGoal goal) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(yearlyGoalService.updateGoal(id, goal, UserContext.getCurrentUserId()));
    }

    @PostMapping("/yearly/{id}/progress")
    public ResponseEntity<YearlyGoal> updateGoalProgress(@PathVariable Long id, @RequestParam Integer progress) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(yearlyGoalService.updateProgress(id, progress, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/yearly/{id}")
    public ResponseEntity<Void> deleteYearlyGoal(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        yearlyGoalService.deleteGoal(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/monthly")
    public ResponseEntity<MonthlyPlan> createMonthlyPlan(@RequestBody MonthlyPlan plan) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(monthlyPlanService.createPlan(plan, UserContext.getCurrentUserId()));
    }

    @PutMapping("/monthly/{id}")
    public ResponseEntity<MonthlyPlan> updateMonthlyPlan(@PathVariable Long id, @RequestBody MonthlyPlan plan) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(monthlyPlanService.updatePlan(id, plan, UserContext.getCurrentUserId()));
    }

    @PostMapping("/monthly/{id}/complete")
    public ResponseEntity<MonthlyPlan> completeMonthlyPlan(@PathVariable Long id, @RequestParam String summary) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(monthlyPlanService.completeReview(id, summary, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/monthly/{id}")
    public ResponseEntity<Void> deleteMonthlyPlan(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        monthlyPlanService.deletePlan(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }
}
