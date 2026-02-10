package com.learning.notes.repository;

import com.learning.notes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 笔记数据访问层
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * 根据标题模糊查询
     */
    List<Note> findByTitleContaining(String title);

    /**
     * 根据分类查询
     */
    List<Note> findByCategory(String category);

    /**
     * 根据标签查询（包含该标签的笔记）
     */
    @Query("SELECT n FROM Note n WHERE n.tags LIKE %:tag%")
    List<Note> findByTagContaining(@Param("tag") String tag);

    /**
     * 根据文件名查询
     */
    Optional<Note> findByFileName(String fileName);

    /**
     * 按创建时间倒序查询所有
     */
    List<Note> findAllByOrderByCreatedAtDesc();

    /**
     * 根据分类和标题模糊查询
     */
    @Query("SELECT n FROM Note n WHERE (:category IS NULL OR n.category = :category) AND (:title IS NULL OR n.title LIKE %:title%)")
    List<Note> findByCategoryAndTitle(@Param("category") String category, @Param("title") String title);

    /**
     * 获取所有分类
     */
    @Query("SELECT DISTINCT n.category FROM Note n WHERE n.category IS NOT NULL ORDER BY n.category")
    List<String> findAllCategories();

    /**
     * 获取所有标签（去重）
     */
    @Query("SELECT DISTINCT n.tags FROM Note n WHERE n.tags IS NOT NULL")
    List<String> findAllTags();

    /**
     * 查询所有公开笔记，按创建时间倒序
     */
    List<Note> findByIsPublicTrueOrderByCreatedAtDesc();

    /**
     * 根据用户ID查询笔记，按创建时间倒序
     */
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据标题模糊查询公开笔记
     */
    List<Note> findByTitleContainingAndIsPublicTrue(String title);

    /**
     * 根据分类查询公开笔记
     */
    List<Note> findByCategoryAndIsPublicTrue(String category);

    /**
     * 根据标签查询公开笔记
     */
    @Query("SELECT n FROM Note n WHERE n.tags LIKE %:tag% AND n.isPublic = true")
    List<Note> findByTagContainingAndIsPublicTrue(@Param("tag") String tag);

    /**
     * 查询所有公开笔记
     */
    List<Note> findByIsPublicTrue();

    /**
     * 根据用户ID查询笔记
     */
    List<Note> findByUserId(Long userId);
}