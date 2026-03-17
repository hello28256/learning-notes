package com.learning.notes.controller.learning;

import com.learning.notes.entity.learning.*;
import com.learning.notes.service.learning.*;
import com.learning.notes.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningLogService learningLogService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ReadingNoteService readingNoteService;
    private final ReviewRecordService reviewRecordService;

    // ==================== 查询接口（公开访问）====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getLearningDashboard() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Object> dashboard = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        Integer weekMinutes = learningLogService.getTotalLearningMinutes(userId, weekStart, weekEnd);
        List<LearningLog> recentLogs = learningLogService.getUserLogs(userId).stream()
                .sorted((a, b) -> b.getLogDate().compareTo(a.getLogDate()))
                .limit(5)
                .toList();
        List<KnowledgeBase> dueForReview = knowledgeBaseService.getDueForReview(userId);
        List<ReadingNote> readingList = readingNoteService.getReadingNotesByStatus(userId, "reading");

        dashboard.put("weekLearningMinutes", weekMinutes);
        dashboard.put("recentLogs", recentLogs);
        dashboard.put("dueForReview", dueForReview);
        dashboard.put("readingList", readingList);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<LearningLog>> getLearningLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String subject) {
        Long userId = UserContext.getCurrentUserId();

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(learningLogService.getLogsByDateRange(
                    userId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
        }
        if (subject != null) {
            return ResponseEntity.ok(learningLogService.getLogsBySubject(userId, subject));
        }
        return ResponseEntity.ok(learningLogService.getUserLogs(userId));
    }

    @GetMapping("/knowledge")
    public ResponseEntity<List<KnowledgeBase>> getKnowledgeBase(@RequestParam(required = false) String category) {
        Long userId = UserContext.getCurrentUserId();
        if (category != null) {
            return ResponseEntity.ok(knowledgeBaseService.getKnowledgeByCategory(userId, category));
        }
        return ResponseEntity.ok(knowledgeBaseService.getUserKnowledge(userId));
    }

    @GetMapping("/knowledge/due-review")
    public ResponseEntity<List<KnowledgeBase>> getKnowledgeDueForReview() {
        return ResponseEntity.ok(knowledgeBaseService.getDueForReview(UserContext.getCurrentUserId()));
    }

    @GetMapping("/reading")
    public ResponseEntity<List<ReadingNote>> getReadingNotes(@RequestParam(required = false) String status) {
        Long userId = UserContext.getCurrentUserId();
        if (status != null) {
            return ResponseEntity.ok(readingNoteService.getReadingNotesByStatus(userId, status));
        }
        return ResponseEntity.ok(readingNoteService.getUserReadingNotes(userId));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewRecord>> getReviewRecords(@RequestParam(required = false) String type) {
        Long userId = UserContext.getCurrentUserId();
        if (type != null) {
            return ResponseEntity.ok(reviewRecordService.getReviewsByType(userId, type));
        }
        return ResponseEntity.ok(reviewRecordService.getUserReviews(userId));
    }

    @GetMapping("/reviews/{period}")
    public ResponseEntity<ReviewRecord> getReviewByPeriod(@PathVariable String period) {
        return ResponseEntity.ok(reviewRecordService.getReviewByPeriod(UserContext.getCurrentUserId(), period));
    }

    // ==================== 修改接口（需要登录）====================

    @PostMapping("/logs")
    public ResponseEntity<LearningLog> createLearningLog(@RequestBody LearningLog log) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(learningLogService.createLog(log, UserContext.getCurrentUserId()));
    }

    @PutMapping("/logs/{id}")
    public ResponseEntity<LearningLog> updateLearningLog(@PathVariable Long id, @RequestBody LearningLog log) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(learningLogService.updateLog(id, log, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<Void> deleteLearningLog(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        learningLogService.deleteLog(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/knowledge")
    public ResponseEntity<KnowledgeBase> createKnowledge(@RequestBody KnowledgeBase knowledge) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(knowledgeBaseService.createKnowledge(knowledge, UserContext.getCurrentUserId()));
    }

    @PutMapping("/knowledge/{id}")
    public ResponseEntity<KnowledgeBase> updateKnowledge(@PathVariable Long id, @RequestBody KnowledgeBase knowledge) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(knowledgeBaseService.updateKnowledge(id, knowledge, UserContext.getCurrentUserId()));
    }

    @PostMapping("/knowledge/{id}/review")
    public ResponseEntity<KnowledgeBase> reviewKnowledge(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(knowledgeBaseService.reviewKnowledge(id, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/knowledge/{id}")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        knowledgeBaseService.deleteKnowledge(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reading")
    public ResponseEntity<ReadingNote> createReadingNote(@RequestBody ReadingNote note) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(readingNoteService.createReadingNote(note, UserContext.getCurrentUserId()));
    }

    @PutMapping("/reading/{id}")
    public ResponseEntity<ReadingNote> updateReadingNote(@PathVariable Long id, @RequestBody ReadingNote note) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(readingNoteService.updateReadingNote(id, note, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/reading/{id}")
    public ResponseEntity<Void> deleteReadingNote(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        readingNoteService.deleteReadingNote(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewRecord> createReviewRecord(@RequestBody ReviewRecord review) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reviewRecordService.createReview(review, UserContext.getCurrentUserId()));
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewRecord> updateReviewRecord(@PathVariable Long id, @RequestBody ReviewRecord review) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reviewRecordService.updateReview(id, review, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReviewRecord(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        reviewRecordService.deleteReview(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }
}
