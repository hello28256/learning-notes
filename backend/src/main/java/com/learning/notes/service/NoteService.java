package com.learning.notes.service;

import com.learning.notes.entity.Note;
import com.learning.notes.repository.NoteRepository;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 笔记服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

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
    public Note uploadAndParseMarkdown(MultipartFile file, String category, String tags) throws IOException {
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
     * 获取所有笔记
     */
    public List<Note> getAllNotes() {
        return noteRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 根据 ID 获取笔记
     */
    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    /**
     * 根据标题搜索
     */
    public List<Note> searchNotesByTitle(String title) {
        return noteRepository.findByTitleContaining(title);
    }

    /**
     * 根据分类获取笔记
     */
    public List<Note> getNotesByCategory(String category) {
        return noteRepository.findByCategory(category);
    }

    /**
     * 根据标签获取笔记
     */
    public List<Note> getNotesByTag(String tag) {
        return noteRepository.findByTagContaining(tag);
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
     * 删除笔记
     */
    @Transactional
    public void deleteNote(Long id) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
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
     * 更新笔记
     */
    @Transactional
    public Note updateNote(Long id, String title, String category, String tags) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            if (title != null) note.setTitle(title);
            if (category != null) note.setCategory(category);
            if (tags != null) note.setTags(tags);
            return noteRepository.save(note);
        }
        throw new IllegalArgumentException("笔记不存在");
    }
}