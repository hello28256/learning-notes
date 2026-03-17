package com.learning.notes.service.learning;

import com.learning.notes.entity.learning.LearningLog;
import com.learning.notes.repository.learning.LearningLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningLogService {

    private final LearningLogRepository learningLogRepository;

    public List<LearningLog> getUserLogs(Long userId) {
        return learningLogRepository.findByUserId(userId);
    }

    public List<LearningLog> getLogsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return learningLogRepository.findByUserIdAndLogDateBetween(userId, startDate, endDate);
    }

    public List<LearningLog> getLogsBySubject(Long userId, String subject) {
        return learningLogRepository.findByUserIdAndSubject(userId, subject);
    }

    public LearningLog getLogById(Long id, Long userId) {
        return learningLogRepository.findById(id)
                .filter(log -> log.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("学习记录不存在"));
    }

    @Transactional
    public LearningLog createLog(LearningLog log, Long userId) {
        log.setUserId(userId);
        return learningLogRepository.save(log);
    }

    @Transactional
    public LearningLog updateLog(Long id, LearningLog log, Long userId) {
        LearningLog existing = getLogById(id, userId);
        existing.setLogDate(log.getLogDate());
        existing.setDurationMinutes(log.getDurationMinutes());
        existing.setSubject(log.getSubject());
        existing.setContent(log.getContent());
        existing.setOutputSummary(log.getOutputSummary());
        existing.setOutputLink(log.getOutputLink());
        existing.setTags(log.getTags());
        existing.setMoodScore(log.getMoodScore());
        existing.setEnergyLevel(log.getEnergyLevel());
        return learningLogRepository.save(existing);
    }

    @Transactional
    public void deleteLog(Long id, Long userId) {
        LearningLog log = getLogById(id, userId);
        learningLogRepository.delete(log);
    }

    public Integer getTotalLearningMinutes(Long userId, LocalDate startDate, LocalDate endDate) {
        Integer total = learningLogRepository.sumDurationByUserIdAndDateRange(userId, startDate, endDate);
        return total != null ? total : 0;
    }
}
