<template>
  <div class="share-view-page">
    <div class="share-header">
      <div class="share-title-section">
        <h1 class="share-title">{{ shareInfo?.shareTitle || '笔记分享' }}</h1>
        <div class="share-meta" v-if="shareInfo">
          <span class="view-count">
            <el-icon><View /></el-icon>
            {{ shareInfo.viewCount || 0 }} 次浏览
          </span>
        </div>
      </div>
      <div class="share-actions">
        <el-button @click="goHome" type="primary" plain>
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
      </div>
    </div>

    <el-card v-if="note" class="note-card">
      <div class="note-info">
        <div class="info-item" v-if="note.category">
          <span class="label">分类：</span>
          <el-tag>{{ note.category }}</el-tag>
        </div>
        <div class="info-item" v-if="note.tags">
          <span class="label">标签：</span>
          <div class="tags-container">
            <el-tag
              v-for="tag in note.tags.split(',')"
              :key="tag"
              type="info"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="note-content" v-html="noteContent"></div>
    </el-card>

    <el-empty v-else-if="!loading" description="分享链接无效或已过期" />

    <el-skeleton v-if="loading" :rows="10" animated />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { View, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { shareApi } from '@/utils/api'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import shell from 'highlight.js/lib/languages/shell'
import bash from 'highlight.js/lib/languages/bash'
import javascript from 'highlight.js/lib/languages/javascript'
import java from 'highlight.js/lib/languages/java'
import python from 'highlight.js/lib/languages/python'
import json from 'highlight.js/lib/languages/json'
import yaml from 'highlight.js/lib/languages/yaml'
import xml from 'highlight.js/lib/languages/xml'
import sql from 'highlight.js/lib/languages/sql'
import 'highlight.js/styles/github.css'

// 注册语言
hljs.registerLanguage('shell', shell)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('java', java)
hljs.registerLanguage('python', python)
hljs.registerLanguage('json', json)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('sql', sql)

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const shareInfo = ref(null)
const note = ref(null)

// Markdown 渲染
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        const highlighted = hljs.highlight(str, { language: lang }).value
        return `<pre class="code-block-wrapper"><div class="code-block-header"><span class="code-lang">${lang}</span></div><div class="code-block-content"><div class="code-content"><code class="hljs">${highlighted}</code></div></div></pre>`
      } catch (__) {}
    }
    return `<pre class="code-block-wrapper"><div class="code-block-header"><span class="code-lang">text</span></div><div class="code-block-content"><div class="code-content"><code class="hljs">${md.utils.escapeHtml(str)}</code></div></div></pre>`
  }
})

// 渲染后的内容
const noteContent = computed(() => {
  if (!note.value?.content) return ''
  return md.render(note.value.content)
})

// 获取分享内容
const fetchShare = async () => {
  const shareCode = route.params.shareCode
  if (!shareCode) {
    ElMessage.error('分享链接无效')
    loading.value = false
    return
  }

  try {
    loading.value = true
    const res = await shareApi.getByCode(shareCode)
    if (res.success) {
      shareInfo.value = res.data
      note.value = res.data.note
    } else {
      ElMessage.error(res.message || '分享链接无效或已过期')
    }
  } catch (error) {
    ElMessage.error(error.message || '获取分享内容失败')
  } finally {
    loading.value = false
  }
}

// 返回首页
const goHome = () => {
  router.push('/')
}

onMounted(() => {
  fetchShare()
})
</script>

<style lang="scss" scoped>
.share-view-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 20px;

  .share-title-section {
    flex: 1;

    .share-title {
      margin: 0 0 10px 0;
      color: var(--text-primary);
      font-size: 24px;
      line-height: 1.3;
    }

    .share-meta {
      display: flex;
      gap: 20px;
      font-size: 13px;
      color: var(--text-tertiary);

      .view-count {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .share-actions {
    display: flex;
    gap: 10px;
  }
}

.note-card {
  :deep(.el-card__body) {
    padding: 20px;
  }

  .note-info {
    background: var(--bg-secondary);
    padding: 15px;
    border-radius: 6px;
    margin-bottom: 20px;
    transition: background-color 0.3s ease;

    .info-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 10px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-weight: 500;
        color: var(--text-secondary);
        margin-right: 10px;
        white-space: nowrap;
        min-width: 50px;
      }

      .tags-container {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
      }
    }
  }

  .note-content {
    line-height: 1.8;
    color: var(--text-primary);

    :deep(h1, h2, h3, h4, h5, h6) {
      margin-top: 25px;
      margin-bottom: 15px;
      color: var(--text-primary);
      font-weight: 600;
    }

    :deep(h1) {
      font-size: 28px;
      border-bottom: 2px solid var(--border-color);
      padding-bottom: 10px;
    }

    :deep(h2) {
      font-size: 24px;
    }

    :deep(h3) {
      font-size: 20px;
    }

    :deep(p) {
      margin-bottom: 15px;
    }

    :deep(pre.code-block-wrapper) {
      background: var(--code-bg);
      border-radius: 8px;
      margin-bottom: 15px;
      overflow: hidden;
      border: 1px solid var(--code-border);
      transition: background-color 0.3s ease, border-color 0.3s ease;

      .code-block-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 16px;
        background: var(--code-header-bg);
        border-bottom: 1px solid var(--code-border);
        transition: background-color 0.3s ease, border-color 0.3s ease;

        .code-lang {
          font-size: 12px;
          color: var(--text-secondary);
          text-transform: uppercase;
          font-weight: 500;
        }
      }

      .code-block-content {
        display: flex;
        overflow-x: auto;
        background: var(--code-bg);
        transition: background-color 0.3s ease;

        .code-content {
          flex: 1;
          overflow-x: auto;
          background: var(--code-bg);
          transition: background-color 0.3s ease;

          pre {
            background: transparent;
            margin: 0;
            padding: 16px;
            overflow: visible;

            code.hljs {
              background: none;
              padding: 0;
              font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
              font-size: 14px;
              font-weight: 500;
              line-height: 1.6;
              color: var(--code-text);
              transition: color 0.3s ease;
              -webkit-font-smoothing: antialiased;
              -moz-osx-font-smoothing: grayscale;
            }
          }
        }
      }
    }

    :deep(pre:not(.code-block-wrapper pre)) {
      background: var(--code-bg);
      color: var(--code-text);
      padding: 16px;
      border-radius: 8px;
      overflow-x: auto;
      margin-bottom: 15px;
      white-space: pre-wrap;
      word-wrap: break-word;
      border: 1px solid var(--code-border);
      transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;

      code {
        background: none;
        padding: 0;
        color: inherit;
        font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
        font-size: 14px;
        font-weight: 600;
        line-height: 1.6;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
      }
    }

    :deep(blockquote) {
      border-left: 4px solid #409eff;
      padding-left: 15px;
      margin: 15px 0;
      color: #606266;
      background: #f0f9ff;
      padding: 10px 15px;
      border-radius: 0 4px 4px 0;
    }

    :deep(ul, ol) {
      margin-bottom: 15px;
      padding-left: 25px;

      li {
        margin-bottom: 8px;
        line-height: 1.6;
      }
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 15px;

      th, td {
        border: 1px solid #dcdfe6;
        padding: 10px;
        text-align: left;
      }

      th {
        background: #f5f7fa;
        font-weight: 600;
      }
    }

    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: 6px;
      margin: 10px 0;
    }

    :deep(a) {
      color: #409eff;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }

    :deep(hr) {
      border: none;
      border-top: 1px solid #e4e7ed;
      margin: 20px 0;
    }
  }
}
</style>
