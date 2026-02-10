package com.learning.notes.controller;

import com.learning.notes.entity.Note;
import com.learning.notes.service.NoteService;
import com.learning.notes.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 笔记控制器
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

    private final NoteService noteService;

    /**
     * 上传 Markdown 文件
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMarkdown(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false, defaultValue = "false") Boolean isPublic) {
        try {
            Note note = noteService.uploadAndParseMarkdown(file, category, tags, isPublic);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("data", note);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取所有笔记（公开笔记所有人可见，登录用户额外看到自己的私有笔记）
     */
    @GetMapping
    public ResponseEntity<?> getAllNotes() {
        try {
            List<Note> notes;
            if (UserContext.isAuthenticated()) {
                notes = noteService.getCurrentUserNotes();
            } else {
                notes = noteService.getAllPublicNotes();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取笔记列表失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取笔记列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据 ID 获取笔记详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoteById(@PathVariable Long id) {
        try {
            Optional<Note> noteOpt = noteService.getNoteById(id);
            if (noteOpt.isPresent()) {
                // 增加浏览次数
                noteService.incrementViewCount(id);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", noteOpt.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "笔记不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("获取笔记详情失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取笔记详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据标题搜索笔记（只搜索公开笔记）
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchNotes(@RequestParam String title) {
        try {
            List<Note> notes = noteService.searchPublicNotesByTitle(title);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索笔记失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索笔记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据分类获取笔记（只获取公开笔记）
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getNotesByCategory(@PathVariable String category) {
        try {
            List<Note> notes = noteService.getPublicNotesByCategory(category);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类笔记失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分类笔记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据标签获取笔记（只获取公开笔记）
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<?> getNotesByTag(@PathVariable String tag) {
        try {
            List<Note> notes = noteService.getPublicNotesByTag(tag);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取标签笔记失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取标签笔记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<String> categories = noteService.getAllCategories();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类列表失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分类列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public ResponseEntity<?> getAllTags() {
        try {
            List<String> tags = noteService.getAllTags();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tags);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取标签列表失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取标签列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        try {
            noteService.deleteNote(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "笔记删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除笔记失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除笔记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新笔记信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags) {
        try {
            Note note = noteService.updateNote(id, title, category, tags);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "笔记更新成功");
            response.put("data", note);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新笔记失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新笔记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取笔记提交历史（用于热力图）
     */
    @GetMapping("/contributions")
    public ResponseEntity<?> getContributions() {
        try {
            List<Map<String, Object>> contributions = noteService.getContributionHistory();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", contributions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取提交历史失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取提交历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}