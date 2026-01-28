<template>
  <div class="note-detail-page">
    <el-button @click="$router.back()" :icon="ArrowLeft" class="back-btn">
      返回
    </el-button>

    <el-card v-if="note" class="note-card">
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit, Delete, Calendar, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noteApi } from '@/utils/api'
import MarkdownIt from 'markdown-it'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const note = ref(null)
const markdown = new MarkdownIt()

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

const editRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

// 计算属性：渲染 Markdown 内容
const noteContent = computed(() => {
  if (!note.value || !note.value.content) return ''
  try {
    return markdown.render(note.value.content)
  } catch (error) {
    console.error('Markdown 渲染失败:', error)
    return `<pre>${note.value.content}</pre>`
  }
})

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

onMounted(() => {
  fetchNote()
  fetchOptions()
})
</script>

<style lang="scss" scoped>
.note-detail-page {
  max-width: 1000px;
  margin: 0 auto;
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
        color: #303133;
        font-size: 24px;
        line-height: 1.3;
      }

      .note-meta {
        display: flex;
        gap: 20px;
        font-size: 13px;
        color: #909399;

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
    background: #f8f9fa;
    padding: 15px;
    border-radius: 6px;
    margin-bottom: 20px;

    .info-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 10px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-weight: 500;
        color: #606266;
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
    color: #303133;

    :deep(h1, h2, h3, h4, h5, h6) {
      margin-top: 25px;
      margin-bottom: 15px;
      color: #303133;
      font-weight: 600;
    }

    :deep(h1) {
      font-size: 28px;
      border-bottom: 2px solid #e4e7ed;
      padding-bottom: 10px;
    }

    :deep(h2) {
      font-size: 24px;
      border-bottom: 1px solid #e4e7ed;
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

    :deep(pre) {
      background: #282c34;
      color: #abb2bf;
      padding: 15px;
      border-radius: 6px;
      overflow-x: auto;
      margin-bottom: 15px;

      code {
        background: none;
        padding: 0;
        color: inherit;
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

.full-width {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>