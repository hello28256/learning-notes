package com.learning.notes.config;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Markdown 配置类
 */
@Configuration
public class MarkdownConfig {

    @Bean
    public Parser markdownParser() {
        MutableDataSet options = new MutableDataSet();
        // 配置解析选项
        return Parser.builder(options).build();
    }

    @Bean
    public HtmlRenderer htmlRenderer() {
        MutableDataSet options = new MutableDataSet();
        // 配置渲染选项
        return HtmlRenderer.builder(options).build();
    }
}