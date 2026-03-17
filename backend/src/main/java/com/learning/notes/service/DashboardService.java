package com.learning.notes.service;

import com.learning.notes.entity.finance.*;
import com.learning.notes.entity.learning.LearningLog;
import com.learning.notes.entity.goal.YearlyGoal;
import com.learning.notes.repository.finance.*;
import com.learning.notes.repository.learning.LearningLogRepository;
import com.learning.notes.repository.goal.YearlyGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinanceAccountRepository accountRepository;
    private final FinanceTransactionRepository transactionRepository;
    private final FinancePositionRepository positionRepository;
    private final FinanceDecisionRepository decisionRepository;
    private final LearningLogRepository learningLogRepository;
    private final YearlyGoalRepository yearlyGoalRepository;

    public Map<String, Object> getDashboardData(Long userId) {
        Map<String, Object> dashboard = new HashMap<>();
        
        dashboard.put("summaryCards", getSummaryCards(userId));
        dashboard.put("assetTrend", getAssetTrend(userId));
        dashboard.put("learningTrend", getLearningTrend(userId));
        dashboard.put("recentActivities", getRecentActivities(userId));
        dashboard.put("reminders", getReminders(userId));
        
        return dashboard;
    }

    private Map<String, Object> getSummaryCards(Long userId) {
        Map<String, Object> cards = new HashMap<>();
        
        List<FinanceAccount> accounts = accountRepository.findByUserIdAndIsActiveTrue(userId);
        BigDecimal totalAssets = accounts.stream()
                .map(FinanceAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.with(TemporalAdjusters.lastDayOfMonth());
        
        BigDecimal monthIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, "deposit", monthStart, monthEnd);
        if (monthIncome == null) monthIncome = BigDecimal.ZERO;
        
        BigDecimal monthExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, "withdraw", monthStart, monthEnd);
        if (monthExpense == null) monthExpense = BigDecimal.ZERO;
        
        BigDecimal savingsRate = monthIncome.compareTo(BigDecimal.ZERO) > 0
                ? monthIncome.subtract(monthExpense).multiply(new BigDecimal("100")).divide(monthIncome, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        Integer weekLearningMinutes = learningLogRepository.sumDurationByUserIdAndDateRange(userId, weekStart, weekEnd);
        if (weekLearningMinutes == null) weekLearningMinutes = 0;
        
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
        Integer lastWeekLearningMinutes = learningLogRepository.sumDurationByUserIdAndDateRange(userId, lastWeekStart, lastWeekEnd);
        if (lastWeekLearningMinutes == null) lastWeekLearningMinutes = 0;
        
        List<YearlyGoal> goals = yearlyGoalRepository.findByUserIdAndYear(userId, now.getYear());
        double avgProgress = goals.isEmpty() ? 0 : 
                goals.stream().mapToInt(YearlyGoal::getProgressPercent).average().orElse(0);
        
        cards.put("totalAssets", totalAssets);
        cards.put("monthlyChange", monthIncome.subtract(monthExpense));
        cards.put("savingsRate", savingsRate);
        cards.put("weekLearningMinutes", weekLearningMinutes);
        cards.put("lastWeekLearningMinutes", lastWeekLearningMinutes);
        cards.put("yearGoalProgress", Math.round(avgProgress));
        
        return cards;
    }

    private List<Map<String, Object>> getAssetTrend(Long userId) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            LocalDate monthStart = month.withDayOfMonth(1);
            LocalDate monthEnd = month.with(TemporalAdjusters.lastDayOfMonth());
            
            Map<String, Object> point = new HashMap<>();
            point.put("month", month.getMonthValue() + "月");
            point.put("value", 0);
            trend.add(point);
        }
        
        return trend;
    }

    private List<Map<String, Object>> getLearningTrend(Long userId) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 3; i >= 0; i--) {
            LocalDate weekStart = now.minusWeeks(i).with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            Integer minutes = learningLogRepository.sumDurationByUserIdAndDateRange(userId, weekStart, weekEnd);
            if (minutes == null) minutes = 0;
            
            Map<String, Object> point = new HashMap<>();
            point.put("week", "W" + (now.getDayOfYear() / 7 - i));
            point.put("minutes", minutes);
            trend.add(point);
        }
        
        return trend;
    }

    private Map<String, Object> getRecentActivities(Long userId) {
        Map<String, Object> activities = new HashMap<>();
        
        List<FinanceDecision> recentDecisions = decisionRepository.findByUserId(userId)
                .stream()
                .sorted((a, b) -> b.getDecisionDate().compareTo(a.getDecisionDate()))
                .limit(3)
                .collect(Collectors.toList());
        
        List<LearningLog> recentLearning = learningLogRepository.findByUserId(userId)
                .stream()
                .sorted((a, b) -> b.getLogDate().compareTo(a.getLogDate()))
                .limit(3)
                .collect(Collectors.toList());
        
        activities.put("decisions", recentDecisions);
        activities.put("learningLogs", recentLearning);
        
        return activities;
    }

    private List<Map<String, Object>> getReminders(Long userId) {
        List<Map<String, Object>> reminders = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        List<FinanceDecision> pendingReviews = decisionRepository
                .findByUserIdAndReviewReminderDateLessThanEqualAndReviewResultIsNull(userId, now);
        
        for (FinanceDecision decision : pendingReviews) {
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("type", "decision_review");
            reminder.put("title", "决策复盘提醒: " + decision.getSymbol());
            reminder.put("date", decision.getReviewReminderDate());
            reminders.add(reminder);
        }
        
        return reminders;
    }
}
