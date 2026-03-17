package com.learning.notes.service.learning;

import com.learning.notes.entity.learning.ReviewRecord;
import com.learning.notes.repository.learning.ReviewRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewRecordService {

    private final ReviewRecordRepository reviewRecordRepository;

    public List<ReviewRecord> getUserReviews(Long userId) {
        return reviewRecordRepository.findByUserId(userId);
    }

    public List<ReviewRecord> getReviewsByType(Long userId, String type) {
        return reviewRecordRepository.findByUserIdAndReviewType(userId, type);
    }

    public ReviewRecord getReviewByPeriod(Long userId, String period) {
        return reviewRecordRepository.findByUserIdAndReviewPeriod(userId, period)
                .orElse(null);
    }

    public ReviewRecord getReviewById(Long id, Long userId) {
        return reviewRecordRepository.findById(id)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("复盘记录不存在"));
    }

    @Transactional
    public ReviewRecord createReview(ReviewRecord review, Long userId) {
        review.setUserId(userId);
        return reviewRecordRepository.save(review);
    }

    @Transactional
    public ReviewRecord updateReview(Long id, ReviewRecord review, Long userId) {
        ReviewRecord existing = getReviewById(id, userId);
        existing.setReviewType(review.getReviewType());
        existing.setReviewPeriod(review.getReviewPeriod());
        existing.setStartDate(review.getStartDate());
        existing.setEndDate(review.getEndDate());
        existing.setWhatDone(review.getWhatDone());
        existing.setWhatRight(review.getWhatRight());
        existing.setWhatImprove(review.getWhatImprove());
        existing.setGoalProgress(review.getGoalProgress());
        existing.setAssetChanges(review.getAssetChanges());
        existing.setLearningAchievements(review.getLearningAchievements());
        existing.setEmotionState(review.getEmotionState());
        existing.setKeyDecisions(review.getKeyDecisions());
        existing.setKeyGrowth(review.getKeyGrowth());
        existing.setNextYearDirection(review.getNextYearDirection());
        existing.setSatisfactionScore(review.getSatisfactionScore());
        return reviewRecordRepository.save(existing);
    }

    @Transactional
    public void deleteReview(Long id, Long userId) {
        ReviewRecord review = getReviewById(id, userId);
        reviewRecordRepository.delete(review);
    }
}
