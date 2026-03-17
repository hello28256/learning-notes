package com.learning.notes.service.learning;

import com.learning.notes.entity.learning.ReadingNote;
import com.learning.notes.repository.learning.ReadingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingNoteService {

    private final ReadingNoteRepository readingNoteRepository;

    public List<ReadingNote> getUserReadingNotes(Long userId) {
        return readingNoteRepository.findByUserId(userId);
    }

    public List<ReadingNote> getReadingNotesByStatus(Long userId, String status) {
        return readingNoteRepository.findByUserIdAndStatus(userId, status);
    }

    public List<ReadingNote> getRecentCompletedBooks(Long userId) {
        return readingNoteRepository.findByUserIdOrderByFinishDateDesc(userId);
    }

    public ReadingNote getReadingNoteById(Long id, Long userId) {
        return readingNoteRepository.findById(id)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("读书笔记不存在"));
    }

    @Transactional
    public ReadingNote createReadingNote(ReadingNote note, Long userId) {
        note.setUserId(userId);
        return readingNoteRepository.save(note);
    }

    @Transactional
    public ReadingNote updateReadingNote(Long id, ReadingNote note, Long userId) {
        ReadingNote existing = getReadingNoteById(id, userId);
        existing.setBookTitle(note.getBookTitle());
        existing.setAuthor(note.getAuthor());
        existing.setCoverImage(note.getCoverImage());
        existing.setRating(note.getRating());
        existing.setStatus(note.getStatus());
        existing.setStartDate(note.getStartDate());
        existing.setFinishDate(note.getFinishDate());
        existing.setKeyPoints(note.getKeyPoints());
        existing.setActionableItem(note.getActionableItem());
        existing.setQuotes(note.getQuotes());
        existing.setPersonalThoughts(note.getPersonalThoughts());
        existing.setRelatedMentalModels(note.getRelatedMentalModels());
        existing.setRelatedRules(note.getRelatedRules());
        return readingNoteRepository.save(existing);
    }

    @Transactional
    public void deleteReadingNote(Long id, Long userId) {
        ReadingNote note = getReadingNoteById(id, userId);
        readingNoteRepository.delete(note);
    }
}
