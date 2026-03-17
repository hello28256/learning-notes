package com.learning.notes.service.goal;

import com.learning.notes.entity.goal.YearlyGoal;
import com.learning.notes.repository.goal.YearlyGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YearlyGoalService {

    private final YearlyGoalRepository yearlyGoalRepository;

    public List<YearlyGoal> getUserGoals(Long userId) {
        return yearlyGoalRepository.findByUserId(userId);
    }

    public List<YearlyGoal> getGoalsByYear(Long userId, Integer year) {
        return yearlyGoalRepository.findByUserIdAndYear(userId, year);
    }

    public List<YearlyGoal> getGoalsByCategory(Long userId, String category) {
        return yearlyGoalRepository.findByUserIdAndCategory(userId, category);
    }

    public YearlyGoal getGoalById(Long id, Long userId) {
        return yearlyGoalRepository.findById(id)
                .filter(g -> g.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("目标不存在"));
    }

    @Transactional
    public YearlyGoal createGoal(YearlyGoal goal, Long userId) {
        goal.setUserId(userId);
        goal.setProgressPercent(0);
        goal.setStatus("active");
        return yearlyGoalRepository.save(goal);
    }

    @Transactional
    public YearlyGoal updateGoal(Long id, YearlyGoal goal, Long userId) {
        YearlyGoal existing = getGoalById(id, userId);
        existing.setYear(goal.getYear());
        existing.setGoalTitle(goal.getGoalTitle());
        existing.setGoalDescription(goal.getGoalDescription());
        existing.setCategory(goal.getCategory());
        existing.setKeyResults(goal.getKeyResults());
        existing.setPriority(goal.getPriority());
        existing.setDeadline(goal.getDeadline());
        return yearlyGoalRepository.save(existing);
    }

    @Transactional
    public YearlyGoal updateProgress(Long id, Integer progress, Long userId) {
        YearlyGoal goal = getGoalById(id, userId);
        goal.setProgressPercent(progress);
        if (progress >= 100) {
            goal.setStatus("completed");
            goal.setCompletedAt(LocalDateTime.now());
        }
        return yearlyGoalRepository.save(goal);
    }

    @Transactional
    public void deleteGoal(Long id, Long userId) {
        YearlyGoal goal = getGoalById(id, userId);
        yearlyGoalRepository.delete(goal);
    }
}
