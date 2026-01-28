<template>
  <div class="upload-page">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <h3>上传 Markdown 笔记</h3>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="upload-form"
      >
        <el-form-item label="选择文件" prop="file">
          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
            accept=".md,.txt,.markdown"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 .md, .txt, .markdown 文件，且不超过 50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="笔记标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入笔记标题"
            clearable
          />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select
            v-model="form.category"
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
            v-model="form.tags"
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

        <el-form-item>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="uploading"
            :disabled="!form.file"
          >
            {{ uploading ? '上传中...' : '开始上传' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预览区域 -->
    <el-card v-if="previewContent" class="preview-card">
      <template #header>
        <div class="card-header">
          <h3>预览</h3>
        </div>
      </template>
      <div class="preview-content" v-html="previewContent"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noteApi } from '@/utils/api'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const formRef = ref()
const uploading = ref(false)
const fileList = ref([])
const previewContent = ref('')
const categories = ref([])
const tags = ref([])
const markdown = new MarkdownIt()

const form = ref({
  file: null,
  title: '',
  category: '',
  tags: []
})

const rules = {
  file: [{ required: true, message: '请选择文件', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

// 获取分类和标签
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

// 处理文件选择
const handleFileChange = (file) => {
  if (file.raw) {
    form.value.file = file.raw
    form.value.title = file.name.replace(/\.(md|txt|markdown)$/i, '')
    fileList.value = [file]

    // 读取文件内容用于预览
    const reader = new FileReader()
    reader.onload = (e) => {
      const content = e.target.result
      previewContent.value = markdown.render(content)
    }
    reader.readAsText(file.raw)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        uploading.value = true

        const formData = new FormData()
        formData.append('file', form.value.file)
        formData.append('title', form.value.title)
        if (form.value.category) {
          formData.append('category', form.value.category)
        }
        if (form.value.tags && form.value.tags.length > 0) {
          formData.append('tags', form.value.tags.join(','))
        }

        const res = await noteApi.upload(formData)

        ElMessage.success('上传成功！')

        // 确认是否跳转到详情页
        ElMessageBox.confirm(
          '文件上传成功！是否要查看笔记详情？',
          '上传成功',
          {
            confirmButtonText: '查看详情',
            cancelButtonText: '继续上传',
            type: 'success'
          }
        ).then(() => {
          router.push(`/note/${res.data.id}`)
        }).catch(() => {
          // 继续上传，重置表单
          handleReset()
        })

      } catch (error) {
        ElMessage.error(error.message || '上传失败')
      } finally {
        uploading.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  form.value = {
    file: null,
    title: '',
    category: '',
    tags: []
  }
  fileList.value = []
  previewContent.value = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 监听文件变化，更新标题
watch(() => form.value.file, (newFile) => {
  if (newFile && !form.value.title) {
    form.value.title = newFile.name.replace(/\.(md|txt|markdown)$/i, '')
  }
})

onMounted(() => {
  fetchOptions()
})
</script>

<style lang="scss" scoped>
.upload-page {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.upload-card,
.preview-card {
  .card-header {
    h3 {
      margin: 0;
      color: #303133;
    }
  }
}

.upload-form {
  .upload-area {
    width: 100%;

    :deep(.el-upload) {
      width: 100%;
    }

    :deep(.el-upload-dragger) {
      width: 100%;
      height: 180px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border: 2px dashed #d9d9d9;
      border-radius: 6px;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        background: #f5f7ff;
      }
    }
  }

  .full-width {
    width: 100%;
  }
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;

  :deep(h1, h2, h3, h4, h5, h6) {
    margin-top: 20px;
    margin-bottom: 10px;
    color: #303133;
  }

  :deep(p) {
    margin-bottom: 15px;
    line-height: 1.6;
  }

  :deep(code) {
    background: #f4f4f5;
    padding: 2px 6px;
    border-radius: 3px;
    font-family: 'Courier New', monospace;
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
}

@media (min-width: 768px) {
  .upload-page {
    grid-template-columns: 1fr 1fr;
  }
}
</style>