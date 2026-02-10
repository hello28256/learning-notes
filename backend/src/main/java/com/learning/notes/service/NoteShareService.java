package com.learning.notes.service;

import com.learning.notes.entity.Note;
import com.learning.notes.entity.NoteShare;
import com.learning.notes.repository.NoteRepository;
import com.learning.notes.repository.NoteShareRepository;
import com.learning.notes.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 笔记分享服务类
 */
@Service
@RequiredArgsConstructor
public class NoteShareService {

    private final NoteShareRepository noteShareRepository;
    private final NoteRepository noteRepository;

    // 短码字符集（去除容易混淆的字符：0, O, 1, I, l）
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int SHORT_CODE_LENGTH = 8;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成随机短码
     */
    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成唯一的分享短码
     */
    private String generateUniqueShortCode() {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String code = generateShortCode();
            if (!noteShareRepository.existsByShareCode(code)) {
                return code;
            }
        }
        // 如果多次尝试都冲突，增加长度再试
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH + 2);
        for (int i = 0; i < SHORT_CODE_LENGTH + 2; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 创建分享链接
     */
    @Transactional
    public Map<String, Object> createShare(Long noteId, String shareTitle, Integer expireDays) {
        // 检查笔记是否存在
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (!noteOpt.isPresent()) {
            throw new IllegalArgumentException("笔记不存在");
        }

        Note note = noteOpt.get();

        // 获取当前用户ID（可能为null，表示匿名用户）
        Long currentUserId = UserContext.getCurrentUserId();

        // 检查权限：
        // 1. 如果是笔记创建人，可以分享
        // 2. 如果是匿名用户，只能分享公开笔记（isPublic=true）
        if (currentUserId != null && currentUserId.equals(note.getUserId())) {
            // 创建人可以分享，继续执行
        } else if (currentUserId == null && note.getIsPublic() != null && note.getIsPublic()) {
            // 匿名用户只能分享公开笔记，继续执行
        } else {
            throw new IllegalArgumentException("无权分享此笔记");
        }

        // 检查是否已存在有效的分享（按创建人查询）
        Long searchUserId = currentUserId != null ? currentUserId : 0L; // 匿名用户使用0作为标识
        Optional<NoteShare> existingShare = noteShareRepository.findByNoteIdAndCreatedByAndIsActiveTrue(noteId,
                searchUserId);
        if (existingShare.isPresent()) {
            NoteShare share = existingShare.get();
            if (share.isValid()) {
                // 返回已有的分享链接
                return buildShareResponse(share, note.getTitle());
            } else {
                // 使旧分享失效
                share.setIsActive(false);
                noteShareRepository.save(share);
            }
        }

        // 创建新的分享
        NoteShare share = new NoteShare();
        share.setNoteId(noteId);
        share.setShareCode(generateUniqueShortCode());
        share.setShareTitle(shareTitle != null && !shareTitle.isEmpty() ? shareTitle : note.getTitle());
        share.setCreatedBy(currentUserId); // 匿名用户为null
        share.setViewCount(0);
        share.setIsActive(true);

        // 设置过期时间
        if (expireDays != null && expireDays > 0) {
            share.setExpiresAt(LocalDateTime.now().plusDays(expireDays));
        }

        NoteShare savedShare = noteShareRepository.save(share);
        return buildShareResponse(savedShare, note.getTitle());
    }

    /**
     * 构建分享响应
     */
    private Map<String, Object> buildShareResponse(NoteShare share, String noteTitle) {
        Map<String, Object> result = new HashMap<>();
        result.put("shareCode", share.getShareCode());
        result.put("shareTitle", share.getShareTitle());
        result.put("shortUrl", "/s/" + share.getShareCode());
        result.put("fullUrl", "http://localhost/s/" + share.getShareCode());
        result.put("expiresAt", share.getExpiresAt());
        result.put("viewCount", share.getViewCount());
        result.put("noteTitle", noteTitle);
        return result;
    }

    /**
     * 根据短码获取分享信息
     */
    @Transactional
    public Map<String, Object> getShareByCode(String shareCode) {
        Optional<NoteShare> shareOpt = noteShareRepository.findByShareCode(shareCode);
        if (!shareOpt.isPresent()) {
            throw new IllegalArgumentException("分享链接不存在");
        }

        NoteShare share = shareOpt.get();
        if (!share.isValid()) {
            throw new IllegalArgumentException("分享链接已过期或已失效");
        }

        // 增加浏览次数
        share.setViewCount(share.getViewCount() + 1);
        noteShareRepository.save(share);

        // 获取笔记信息
        Optional<Note> noteOpt = noteRepository.findById(share.getNoteId());
        if (!noteOpt.isPresent()) {
            throw new IllegalArgumentException("笔记不存在");
        }

        Note note = noteOpt.get();

        // 检查笔记是否公开或分享者是否是笔记创建人
        if (!Boolean.TRUE.equals(note.getIsPublic())) {
            // 私有笔记只能通过分享查看，这里已经通过分享验证
        }

        Map<String, Object> result = new HashMap<>();
        result.put("shareCode", share.getShareCode());
        result.put("shareTitle", share.getShareTitle());
        result.put("viewCount", share.getViewCount());
        result.put("note", note);
        return result;
    }

    /**
     * 获取笔记的分享信息
     */
    public Map<String, Object> getNoteShare(Long noteId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return null;
        }

        Optional<NoteShare> shareOpt = noteShareRepository.findByNoteIdAndCreatedByAndIsActiveTrue(noteId,
                currentUserId);
        if (shareOpt.isPresent()) {
            NoteShare share = shareOpt.get();
            Optional<Note> noteOpt = noteRepository.findById(noteId);
            if (noteOpt.isPresent()) {
                return buildShareResponse(share, noteOpt.get().getTitle());
            }
        }
        return null;
    }

    /**
     * 取消分享
     */
    @Transactional
    public void cancelShare(Long noteId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("请先登录");
        }

        Optional<NoteShare> shareOpt = noteShareRepository.findByNoteIdAndCreatedByAndIsActiveTrue(noteId,
                currentUserId);
        if (shareOpt.isPresent()) {
            NoteShare share = shareOpt.get();
            share.setIsActive(false);
            noteShareRepository.save(share);
        }
    }
}
