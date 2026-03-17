package com.learning.notes.repository.learning;

import com.learning.notes.entity.learning.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {
    List<ReviewRecord> findByUserId(Long userId);
    List<ReviewRecord> findByUserIdAndReviewType(Long userId, String reviewType);
    Optional<ReviewRecord> findByUserIdAndReviewPeriod(Long userId, String reviewPeriod);
}
