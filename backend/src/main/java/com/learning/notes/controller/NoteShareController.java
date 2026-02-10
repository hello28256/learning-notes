package com.learning.notes.controller;

import com.learning.notes.service.NoteShareService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 笔记分享控制器
 */
@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NoteShareController {

    private static final Logger log = LoggerFactory.getLogger(NoteShareController.class);

    private final NoteShareService noteShareService;

    /**
     * 创建分享链接
     */
    @PostMapping("/create/{noteId}")
    public ResponseEntity<?> createShare(
            @PathVariable Long noteId,
            @RequestParam(required = false) String shareTitle,
            @RequestParam(required = false, defaultValue = "7") Integer expireDays) {
        try {
            Map<String, Object> shareInfo = noteShareService.createShare(noteId, shareTitle, expireDays);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "分享链接创建成功");
            response.put("data", shareInfo);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("创建分享失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("创建分享失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建分享失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据短码获取分享内容
     */
    @GetMapping("/s/{shareCode}")
    public ResponseEntity<?> getShareByCode(@PathVariable String shareCode) {
        try {
            Map<String, Object> shareInfo = noteShareService.getShareByCode(shareCode);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", shareInfo);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("获取分享失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("获取分享失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分享失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取笔记的分享信息
     */
    @GetMapping("/info/{noteId}")
    public ResponseEntity<?> getNoteShare(@PathVariable Long noteId) {
        try {
            Map<String, Object> shareInfo = noteShareService.getNoteShare(noteId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", shareInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分享信息失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分享信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 取消分享
     */
    @PostMapping("/cancel/{noteId}")
    public ResponseEntity<?> cancelShare(@PathVariable Long noteId) {
        try {
            noteShareService.cancelShare(noteId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "分享已取消");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("取消分享失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("取消分享失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消分享失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
