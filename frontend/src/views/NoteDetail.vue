<template>
  <div class="note-detail-page">
    <el-button @click="$router.back()" :icon="ArrowLeft" class="back-btn">
      返回
    </el-button>

    <div class="content-wrapper">
      <!-- 左侧目录 -->
      <div v-if="toc.length > 0" class="toc-sidebar" :class="{ 'toc-collapsed': tocCollapsed }">
        <div class="toc-header">
          <div class="toc-title">目录</div>
          <div class="toc-actions">
            <el-button
              type="primary"
              link
              size="small"
              @click="toggleToc"
              :title="tocCollapsed ? '展开目录' : '收起目录'"
            >
              <el-icon><ArrowLeft v-if="!tocCollapsed" /><ArrowRight v-if="tocCollapsed" /></el-icon>
            </el-button>
          </div>
        </div>
        <div v-if="!tocCollapsed" class="toc-list">
          <div
            v-for="(item, index) in toc"
            :key="item.id"
            v-show="shouldShowItem(item, index)"
            class="toc-item"
            :class="{ 
              'toc-item-active': activeTocId === item.id, 
              [`toc-level-${item.level}`]: true
            }"
          >
            <div class="toc-item-content" @click="scrollToHeading(item.id)">
              <el-icon 
                v-if="hasChildren(item)" 
                class="toc-toggle-icon"
                :class="{ 'is-collapsed': collapsedItems.has(item.id) }"
                @click.stop="toggleItem(item.id)"
              >
                <ArrowDown />
              </el-icon>
              <span v-else class="toc-toggle-placeholder"></span>
              <span class="toc-item-text">{{ item.text }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-card v-if="note" class="note-card" :class="{ 'with-toc': toc.length > 0 && !tocCollapsed, 'with-toc-collapsed': toc.length > 0 && tocCollapsed }">
      <template #header>
        <div class="note-header">
          <div class="title-section">
            <h2 class="note-title">{{ note.title }}</h2>
            <div class="note-meta">
              <span class="date">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(note.createdAt) }}
              </span>
              <span class="views">
                <el-icon><View /></el-icon>
                {{ note.viewCount }} 次浏览
              </span>
            </div>
          </div>
          <div class="note-actions">
            <el-button
              type="success"
              @click="handleShare"
              :icon="Share"
              size="small"
            >
              分享
            </el-button>
            <template v-if="isOwner">
              <el-button
                type="primary"
                @click="handleEdit"
                :icon="Edit"
                size="small"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                @click="handleDelete"
                :icon="Delete"
                size="small"
              >
                删除
              </el-button>
            </template>
          </div>
        </div>
      </template>

      <div class="note-info">
        <div class="info-item" v-if="note.category">
          <span class="label">分类：</span>
          <el-tag @click="goToCategory(note.category)" class="info-tag">
            {{ note.category }}
          </el-tag>
        </div>
        <div class="info-item" v-if="note.tags">
          <span class="label">标签：</span>
          <div class="tags-container">
            <el-tag
              v-for="tag in note.tags.split(',')"
              :key="tag"
              type="info"
              @click="goToTag(tag)"
              class="info-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="note-content" v-html="noteContent"></div>
    </el-card>

    <el-empty v-else description="加载中..." />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑笔记信息"
      width="500px"
      @close="resetEditForm"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select
            v-model="editForm.category"
            placeholder="请选择分类"
            filterable
            allow-create
            clearable
            class="full-width"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="editForm.tags"
            placeholder="请选择标签（可多选）"
            multiple
            filterable
            allow-create
            clearable
            class="full-width"
          >
            <el-option
              v-for="tag in tags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdate" :loading="updating">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分享对话框 -->
    <el-dialog
      v-model="shareDialogVisible"
      title="分享笔记"
      width="500px"
    >
      <div v-if="shareInfo" class="share-info">
        <div class="share-preview-box">
          <div class="share-preview-title">分享预览</div>
          <div class="share-preview-content">
            【{{ shareInfo.shareTitle }}】 {{ shareInfo.fullUrl }}
          </div>
          <div class="share-preview-actions">
            <el-button type="primary" @click="copyShareWithTitle" size="default">
              <el-icon><DocumentCopy /></el-icon>
              复制分享文本
            </el-button>
          </div>
        </div>
        <div class="share-meta-info">
          <el-tag v-if="shareInfo.expiresAt" size="small" type="info">
            {{ formatDate(shareInfo.expiresAt) }} 过期
          </el-tag>
          <el-tag v-else size="small" type="success">永久有效</el-tag>
          <span class="view-count">
            <el-icon><View /></el-icon>
            {{ shareInfo.viewCount || 0 }} 次浏览
          </span>
        </div>
      </div>
      <div v-else class="share-creating">
        <el-alert
          v-if="!isLoggedIn && !note?.isPublic"
          title="未登录用户只能分享公开笔记"
          type="warning"
          :closable="false"
        />
        <div v-else class="creating-tip">
          <el-icon class="loading-icon"><Loading /></el-icon>
          正在创建分享链接...
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shareDialogVisible = false">关闭</el-button>
          <el-button
            v-if="shareInfo"
            type="danger"
            @click="handleCancelShare"
            :loading="cancelingShare"
          >
            取消分享
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, ArrowDown, Edit, Delete, Calendar, View, Share, DocumentCopy, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noteApi, shareApi } from '@/utils/api'
import { userId } from '@/utils/auth'
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
import dayjs from 'dayjs'

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
const note = ref(null)

// 判断当前用户是否是笔记创建人
const isOwner = computed(() => {
  if (!note.value || !userId.value) return false
  return note.value.userId === userId.value
})

// 判断是否已登录
const isLoggedIn = computed(() => {
  return !!userId.value
})

// 目录相关
const toc = ref([])
const activeTocId = ref('')

// 编辑相关
const editDialogVisible = ref(false)
const updating = ref(false)
const editFormRef = ref()
const categories = ref([])
const tags = ref([])

const editForm = ref({
  title: '',
  category: '',
  tags: []
})

// 分享相关
const shareDialogVisible = ref(false)
const creatingShare = ref(false)
const cancelingShare = ref(false)
const shareInfo = ref(null)
const shareFormRef = ref()

const shareForm = ref({
  shareTitle: '',
  expireDays: 7
})

// 目录折叠状态
const tocCollapsed = ref(false)
const collapsedItems = ref(new Set())

// 目录折叠功能
const toggleToc = () => {
  tocCollapsed.value = !tocCollapsed.value
}

// 切换目录项的展开/折叠
const toggleItem = (itemId) => {
  if (collapsedItems.value.has(itemId)) {
    collapsedItems.value.delete(itemId)
  } else {
    collapsedItems.value.add(itemId)
  }
}

// 检查是否有子目录
const hasChildren = (item) => {
  const itemIndex = toc.value.findIndex(t => t.id === item.id)
  if (itemIndex === -1 || itemIndex === toc.value.length - 1) return false
  const nextItem = toc.value[itemIndex + 1]
  return nextItem && nextItem.level > item.level
}

// 检查目录项是否应该显示（父级没有折叠）
const shouldShowItem = (item, index) => {
  // 找到这个项目的父级
  for (let i = index - 1; i >= 0; i--) {
    if (toc.value[i].level < item.level) {
      // 如果父级被折叠，则不显示
      if (collapsedItems.value.has(toc.value[i].id)) {
        return false
      }
      // 继续检查更上层的父级
    }
  }
  return true
}

const editRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

// 解析 Markdown 生成目录
const parseToc = (content) => {
  if (!content) return []
  
  const headings = []
  const lines = content.split('\n')
  
  lines.forEach((line) => {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const id = `heading-${headings.length}`
      headings.push({ level, text, id })
    }
  })
  
  return headings
}

// 计算属性：渲染 Markdown 内容
const noteContent = computed(() => {
  if (!note.value || !note.value.content) return ''

  // 如果是 txt 文件，使用文本预览样式
  const fileName = note.value.fileName || ''
  if (fileName.endsWith('.txt')) {
    return `<pre class="text-preview">${escapeHtml(note.value.content)}</pre>`
  }

  try {
    // 解析目录
    toc.value = parseToc(note.value.content)

    // 自定义渲染，为标题添加 id
    const md = new MarkdownIt({
      highlight: function (str, lang) {
        if (lang && hljs.getLanguage(lang)) {
          try {
            return hljs.highlight(str, { language: lang }).value
          } catch (__) {}
        }
        return hljs.highlightAuto(str).value
      }
    })
    let headingIndex = 0

    md.renderer.rules.heading_open = (tokens, idx, options, env, self) => {
      const token = tokens[idx]
      if (token.tag.match(/^h[1-6]$/)) {
        token.attrSet('id', `heading-${headingIndex++}`)
      }
      return self.renderToken(tokens, idx, options)
    }

    // 自定义代码块渲染，添加语言标签和行号
    md.renderer.rules.fence = (tokens, idx, options, env, self) => {
      const token = tokens[idx]
      const lang = token.info.trim()
      const content = token.content
      
      // 生成行号（使用 <br> 换行）
      const lines = content.split('\n')
      const lineCount = lines.length > 0 && lines[lines.length - 1] === '' ? lines.length - 1 : lines.length
      const lineNumbers = Array.from({ length: lineCount }, (_, i) => i + 1).join('<br>')
      
      // 使用 highlight.js 进行代码高亮
      let highlightedCode
      if (lang && hljs.getLanguage(lang)) {
        try {
          highlightedCode = hljs.highlight(content, { language: lang }).value
        } catch (__) {
          highlightedCode = hljs.highlightAuto(content).value
        }
      } else {
        highlightedCode = hljs.highlightAuto(content).value
      }
      
      return `<div class="code-block-wrapper">
        <div class="code-block-header">
          ${lang ? `<span class="code-lang">${lang}</span>` : ''}
        </div>
        <div class="code-block-content">
          <div class="line-numbers">${lineNumbers}</div>
          <div class="code-content"><pre><code class="hljs">${highlightedCode}</code></pre></div>
        </div>
      </div>`
    }

    return md.render(note.value.content)
  } catch (error) {
    console.error('Markdown 渲染失败:', error)
    return `<pre class="text-preview">${escapeHtml(note.value.content)}</pre>`
  }
})

// HTML 转义函数
const escapeHtml = (text) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 获取笔记详情
const fetchNote = async () => {
  const id = route.params.id
  if (!id) return

  try {
    const res = await noteApi.getById(id)
    note.value = res.data
  } catch (error) {
    ElMessage.error('获取笔记详情失败')
    router.back()
  }
}

// 获取分类和标签选项
const fetchOptions = async () => {
  try {
    const [catRes, tagRes] = await Promise.all([
      noteApi.getCategories(),
      noteApi.getTags()
    ])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
  } catch (error) {
    console.error('获取选项失败:', error)
  }
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 跳转到分类
const goToCategory = (category) => {
  router.push(`/category/${encodeURIComponent(category)}`)
}

// 跳转到标签
const goToTag = (tag) => {
  router.push(`/tag/${encodeURIComponent(tag)}`)
}

// 处理编辑
const handleEdit = () => {
  if (!note.value) return

  editForm.value = {
    title: note.value.title,
    category: note.value.category || '',
    tags: note.value.tags ? note.value.tags.split(',') : []
  }

  editDialogVisible.value = true
}

// 重置编辑表单
const resetEditForm = () => {
  editForm.value = {
    title: '',
    category: '',
    tags: []
  }
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// 处理更新
const handleUpdate = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        updating.value = true

        const params = {
          title: editForm.value.title,
          category: editForm.value.category || null,
          tags: editForm.value.tags.length > 0 ? editForm.value.tags.join(',') : null
        }

        await noteApi.update(note.value.id, params)

        ElMessage.success('更新成功')
        editDialogVisible.value = false

        // 重新获取数据
        await fetchNote()
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        updating.value = false
      }
    }
  })
}

// 处理删除
const handleDelete = () => {
  ElMessageBox.confirm(
    '确定要删除这条笔记吗？此操作不可恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await noteApi.delete(note.value.id)
      ElMessage.success('删除成功')
      router.back()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 处理分享
const handleShare = async () => {
  shareInfo.value = null
  shareDialogVisible.value = true

  // 检查权限
  if (!isLoggedIn.value && !note.value?.isPublic) {
    // 未登录且非公开笔记，显示警告
    return
  }

  // 先尝试获取已有的分享信息
  try {
    const res = await shareApi.getInfo(note.value.id)
    if (res.success && res.data) {
      shareInfo.value = res.data
      return
    }
  } catch (error) {
    // 没有分享信息，继续创建
  }

  // 自动创建分享
  await autoCreateShare()
}

// 自动创建分享
const autoCreateShare = async () => {
  try {
    creatingShare.value = true
    const params = {
      shareTitle: note.value.title,
      expireDays: 7
    }
    const res = await shareApi.create(note.value.id, params)
    if (res.success) {
      shareInfo.value = res.data
      ElMessage.success('分享链接创建成功')
    } else {
      ElMessage.error(res.message || '创建分享失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '创建分享失败')
  } finally {
    creatingShare.value = false
  }
}

// 取消分享
const handleCancelShare = async () => {
  try {
    cancelingShare.value = true
    const res = await shareApi.cancel(note.value.id)
    if (res.success) {
      shareInfo.value = null
      ElMessage.success('分享已取消')
    } else {
      ElMessage.error(res.message || '取消分享失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '取消分享失败')
  } finally {
    cancelingShare.value = false
  }
}

// 复制分享链接
const copyShareUrl = () => {
  if (!shareInfo.value?.fullUrl) return

  navigator.clipboard.writeText(shareInfo.value.fullUrl).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    // 降级方案
    const input = document.createElement('input')
    input.value = shareInfo.value.fullUrl
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('链接已复制到剪贴板')
  })
}

// 复制带标题的分享文本（B站格式）
const copyShareWithTitle = () => {
  if (!shareInfo.value) return

  const shareText = `【${shareInfo.value.shareTitle}】 ${shareInfo.value.fullUrl}`

  navigator.clipboard.writeText(shareText).then(() => {
    ElMessage.success('分享文本已复制到剪贴板')
  }).catch(() => {
    // 降级方案
    const input = document.createElement('input')
    input.value = shareText
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('分享文本已复制到剪贴板')
  })
}

// 点击目录跳转
const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeTocId.value = id
  }
}

// 监听滚动更新当前激活的目录
const handleScroll = () => {
  if (toc.value.length === 0) return
  
  const headings = toc.value.map(item => document.getElementById(item.id))
  const scrollTop = window.scrollY + 100
  
  for (let i = headings.length - 1; i >= 0; i--) {
    const heading = headings[i]
    if (heading && heading.offsetTop <= scrollTop) {
      activeTocId.value = toc.value[i].id
      break
    }
  }
}

onMounted(() => {
  fetchNote()
  fetchOptions()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.note-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.content-wrapper {
  display: flex;
  gap: 20px;
  position: relative;
}

// 目录侧边栏样式
.toc-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
  height: fit-content;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  background: var(--card-bg);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--card-shadow);
  transition: all 0.3s ease;

  &.toc-collapsed {
    width: 50px;
    padding: 12px 8px;

    .toc-header {
      justify-content: center;

      .toc-title {
        display: none;
      }
    }
  }

  .toc-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--border-color);

    .toc-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }

    .toc-actions {
      display: flex;
      gap: 8px;

      .el-button {
        padding: 6px;
        height: 32px;
        width: 32px;
        font-size: 16px;
        
        .el-icon {
          font-size: 18px;
          font-weight: bold;
        }
        
        &:hover {
          background: var(--bg-secondary);
          border-radius: 6px;
        }
      }
    }
  }

  .toc-list {
    .toc-item {
      font-size: 14px;
      color: var(--text-secondary);
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background: var(--bg-secondary);
        color: var(--link-color);
      }

      &.toc-item-active {
        background: var(--bg-tertiary);
        color: var(--link-color);
        font-weight: 500;
      }

      .toc-item-content {
        display: flex;
        align-items: center;
        padding: 6px 8px;
        white-space: nowrap;
        overflow: hidden;

        .toc-toggle-icon {
          margin-right: 6px;
          font-size: 12px;
          color: var(--text-tertiary);
          transition: transform 0.2s;
          flex-shrink: 0;

          &:hover {
            color: var(--link-color);
          }

          &.is-collapsed {
            transform: rotate(-90deg);
          }
        }

        .toc-toggle-placeholder {
          width: 18px;
          flex-shrink: 0;
        }

        .toc-item-text {
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }

      // 不同层级的缩进
      &.toc-level-1 {
        .toc-item-content {
          font-weight: 600;
        }
      }

      &.toc-level-2 {
        .toc-item-content {
          padding-left: 24px;
        }
      }

      &.toc-level-3 {
        .toc-item-content {
          padding-left: 40px;
          font-size: 13px;
        }
      }

      &.toc-level-4,
      &.toc-level-5,
      &.toc-level-6 {
        .toc-item-content {
          padding-left: 56px;
          font-size: 13px;
          color: var(--text-tertiary);
        }
      }
    }
  }
}

.note-card {
  flex: 1;
  transition: max-width 0.3s ease;

  &.with-toc {
    max-width: calc(100% - 300px);
  }

  &.with-toc-collapsed {
    max-width: calc(100% - 70px);
  }
}

.back-btn {
  margin-bottom: 20px;
}

.note-card {
  :deep(.el-card__header) {
    padding: 20px;
  }

  .note-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;

    .title-section {
      flex: 1;

      .note-title {
        margin: 0 0 10px 0;
        color: var(--text-primary);
        font-size: 24px;
        line-height: 1.3;
      }

      .note-meta {
        display: flex;
        gap: 20px;
        font-size: 13px;
        color: var(--text-tertiary);

        .date,
        .views {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .note-actions {
      display: flex;
      gap: 10px;
    }
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

      .info-tag {
        cursor: pointer;
        transition: opacity 0.3s;

        &:hover {
          opacity: 0.8;
        }
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
      border-bottom: 1px solid var(--border-color);
      padding-bottom: 8px;
    }

    :deep(h3) {
      font-size: 20px;
    }

    :deep(p) {
      margin-bottom: 15px;
    }

    :deep(code) {
      background: #f4f4f5;
      padding: 2px 6px;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
    }

    // 代码块包装器样式 - 适配白天/夜晚模式
    :deep(.code-block-wrapper) {
      background: var(--code-bg);
      border-radius: 8px;
      overflow: hidden;
      margin-bottom: 15px;
      border: 1px solid var(--code-border);
      transition: background-color 0.3s ease, border-color 0.3s ease;

      .code-block-header {
        background: var(--code-header-bg);
        padding: 8px 16px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1px solid var(--code-border);
        min-height: 36px;
        transition: background-color 0.3s ease, border-color 0.3s ease;

        .code-lang {
          font-size: 12px;
          color: var(--text-secondary);
          text-transform: uppercase;
          font-weight: 600;
          letter-spacing: 0.5px;
        }
      }

      .code-block-content {
        display: flex;
        overflow-x: auto;
        background: var(--code-bg);
        transition: background-color 0.3s ease;

        .line-numbers {
          background: var(--code-bg);
          color: var(--code-line-number);
          padding: 16px 12px 16px 16px;
          text-align: right;
          font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
          font-size: 14px;
          font-weight: 600;
          line-height: 1.6;
          user-select: none;
          border-right: 1px solid var(--code-line-border);
          min-width: 44px;
          transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
        }

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

    /* 文本文件预览样式 */
    :deep(.text-preview) {
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
      line-height: 1.8;
      background: #ffffff;
      color: #1a1a1a;
      padding: 20px;
      border-radius: 8px;
      border: 1px solid var(--code-border);
      font-size: 15px;
      font-weight: 400;
      letter-spacing: 0.3px;
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

.full-width {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

// 分享对话框样式
.share-info {
  .share-preview-box {
    background: var(--bg-secondary);
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;

    .share-preview-title {
      font-size: 12px;
      color: var(--text-secondary);
      margin-bottom: 8px;
      text-align: center;
    }

    .share-preview-content {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: 6px;
      padding: 12px;
      font-size: 14px;
      line-height: 1.6;
      color: var(--text-primary);
      word-break: break-all;
      margin-bottom: 12px;
    }

    .share-preview-actions {
      display: flex;
      justify-content: center;

      .el-button {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .share-meta-info {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    padding: 8px 0;

    .view-count {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--text-secondary);
      font-size: 13px;

      .el-icon {
        font-size: 14px;
      }
    }
  }
}

.share-form {
  padding: 10px 0;
}

.share-creating {
  padding: 30px 0;
  text-align: center;

  .creating-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: var(--text-secondary);
    font-size: 14px;

    .loading-icon {
      animation: rotating 2s linear infinite;
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>