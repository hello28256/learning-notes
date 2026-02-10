package com.learning.notes.service;

import com.learning.notes.entity.Note;
import com.learning.notes.repository.NoteRepository;
import com.learning.notes.util.UserContext;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 笔记服务类
 */
@Service
@RequiredArgsConstructor
public class NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository noteRepository;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${file.upload.allowed-extensions:.md,.txt,.markdown}")
    private String allowedExtensions;

    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;

    @PostConstruct
    public void init() {
        // 初始化 Markdown 解析器
        MutableDataSet options = new MutableDataSet();
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }

    /**
     * 上传并解析 Markdown 文件
     */
    @Transactional
    public Note uploadAndParseMarkdown(MultipartFile file, String category, String tags, Boolean isPublic)
            throws IOException {
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedFile(originalFilename)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        // 创建上传目录
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // 生成唯一文件名
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = Paths.get(uploadDir, uniqueFileName);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        // 读取文件内容
        String content = new String(Files.readAllBytes(filePath));

        // 解析 Markdown 为 HTML
        String htmlContent = parseMarkdownToHtml(content);

        // 创建笔记对象
        Note note = new Note();
        note.setTitle(originalFilename.replace(fileExtension, ""));
        note.setContent(content);
        note.setFileName(originalFilename);
        note.setFilePath(filePath.toString());
        note.setCategory(category);
        note.setTags(tags);
        note.setViewCount(0);
        note.setIsPublic(isPublic != null ? isPublic : false); // 使用传入的参数，默认私有
        note.setUserId(UserContext.getCurrentUserId()); // 设置当前用户

        // 保存到数据库
        return noteRepository.save(note);
    }

    /**
     * 解析 Markdown 为 HTML
     */
    private String parseMarkdownToHtml(String markdown) {
        try {
            Node document = markdownParser.parse(markdown);
            return htmlRenderer.render(document);
        } catch (Exception e) {
            log.error("Markdown 解析失败: {}", e.getMessage());
            // 如果解析失败，返回原始内容
            return "<pre>" + markdown + "</pre>";
        }
    }

    /**
     * 获取所有公开笔记
     */
    public List<Note> getAllPublicNotes() {
        return noteRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    /**
     * 获取当前用户的所有笔记（包括私有）
     */
    public List<Note> getCurrentUserNotes() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return getAllPublicNotes();
        }
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 根据 ID 获取笔记（带权限检查）
     */
    public Optional<Note> getNoteById(Long id) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            // 公开笔记所有人可见
            if (Boolean.TRUE.equals(note.getIsPublic())) {
                return noteOpt;
            }
            // 私有笔记只有作者可见
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null && currentUserId.equals(note.getUserId())) {
                return noteOpt;
            }
        }
        return Optional.empty();
    }

    /**
     * 根据标题搜索公开笔记
     */
    public List<Note> searchPublicNotesByTitle(String title) {
        return noteRepository.findByTitleContainingAndIsPublicTrue(title);
    }

    /**
     * 根据分类获取公开笔记
     */
    public List<Note> getPublicNotesByCategory(String category) {
        return noteRepository.findByCategoryAndIsPublicTrue(category);
    }

    /**
     * 根据标签获取公开笔记
     */
    public List<Note> getPublicNotesByTag(String tag) {
        return noteRepository.findByTagContainingAndIsPublicTrue(tag);
    }

    /**
     * 更新笔记浏览次数
     */
    @Transactional
    public void incrementViewCount(Long id) {
        noteRepository.findById(id).ifPresent(note -> {
            note.setViewCount(note.getViewCount() + 1);
            noteRepository.save(note);
        });
    }

    /**
     * 删除笔记（只有创建人可以删除）
     */
    @Transactional
    public void deleteNote(Long id) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();

            // 权限检查：只有创建人可以删除
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(note.getUserId())) {
                throw new IllegalArgumentException("无权删除此笔记，只有创建人可以删除");
            }

            // 删除文件
            try {
                if (note.getFilePath() != null) {
                    Files.deleteIfExists(Paths.get(note.getFilePath()));
                }
            } catch (IOException e) {
                log.error("删除文件失败: {}", e.getMessage());
            }
            // 删除数据库记录
            noteRepository.delete(note);
        } else {
            throw new IllegalArgumentException("笔记不存在");
        }
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return noteRepository.findAllCategories();
    }

    /**
     * 获取所有标签
     */
    public List<String> getAllTags() {
        return noteRepository.findAllTags();
    }

    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFile(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        return allowedExtensions.contains(extension);
    }

    /**
     * 更新笔记（只有创建人可以编辑）
     */
    @Transactional
    public Note updateNote(Long id, String title, String category, String tags) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();

            // 权限检查：只有创建人可以编辑
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(note.getUserId())) {
                throw new IllegalArgumentException("无权编辑此笔记，只有创建人可以编辑");
            }

            if (title != null)
                note.setTitle(title);
            if (category != null)
                note.setCategory(category);
            if (tags != null)
                note.setTags(tags);
            return noteRepository.save(note);
        }
        throw new IllegalArgumentException("笔记不存在");
    }

    /**
     * 获取笔记提交历史（用于热力图）
     */
    public List<Map<String, Object>> getContributionHistory() {
        List<Note> notes;
        if (UserContext.isAuthenticated()) {
            notes = noteRepository.findByUserId(UserContext.getCurrentUserId());
        } else {
            notes = noteRepository.findByIsPublicTrue();
        }

        // 按日期统计笔记数量
        Map<String, Long> dateCountMap = notes.stream()
                .collect(Collectors.groupingBy(
                        note -> note.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()));

        // 转换为列表
        return dateCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}