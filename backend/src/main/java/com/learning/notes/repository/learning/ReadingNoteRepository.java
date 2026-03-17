package com.learning.notes.repository.learning;

import com.learning.notes.entity.learning.ReadingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingNoteRepository extends JpaRepository<ReadingNote, Long> {
    List<ReadingNote> findByUserId(Long userId);
    List<ReadingNote> findByUserIdAndStatus(Long userId, String status);
    List<ReadingNote> findByUserIdOrderByFinishDateDesc(Long userId);
}
