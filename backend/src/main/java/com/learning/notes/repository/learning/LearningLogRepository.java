package com.learning.notes.repository.learning;

import com.learning.notes.entity.learning.LearningLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LearningLogRepository extends JpaRepository<LearningLog, Long> {
    List<LearningLog> findByUserId(Long userId);
    List<LearningLog> findByUserIdAndLogDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<LearningLog> findByUserIdAndSubject(Long userId, String subject);
    
    @Query("SELECT SUM(l.durationMinutes) FROM LearningLog l WHERE l.userId = :userId AND l.logDate BETWEEN :startDate AND :endDate")
    Integer sumDurationByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
