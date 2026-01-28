<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索笔记标题..."
        size="large"
        @keyup.enter="handleSearch"
        clearable
      >
        <template #append>
          <el-button @click="handleSearch" :icon="Search">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 分类和标签 -->
    <div class="filter-section">
      <div class="categories">
        <span class="label">分类：</span>
        <el-tag
          v-for="category in categories"
          :key="category"
          @click="goToCategory(category)"
          class="filter-tag"
          effect="plain"
        >
          {{ category }}
        </el-tag>
      </div>
      <div class="tags">
        <span class="label">标签：</span>
        <el-tag
          v-for="tag in tags"
          :key="tag"
          @click="goToTag(tag)"
          class="filter-tag"
          type="info"
          effect="plain"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <!-- 笔记列表 -->
    <div class="notes-section">
      <div class="section-header">
        <h3>笔记列表</h3>
        <el-button type="primary" @click="$router.push('/upload')" :icon="Upload">
          上传笔记
        </el-button>
      </div>

      <el-empty v-if="notes.length === 0" description="暂无笔记，请上传" />

      <el-row :gutter="20" v-else>
        <el-col
          v-for="note in notes"
          :key="note.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="note-card" @click="goToNote(note.id)" shadow="hover">
            <div class="note-header">
              <h4 class="note-title">{{ note.title }}</h4>
              <el-tag size="small" v-if="note.category" @click.stop="goToCategory(note.category)">
                {{ note.category }}
              </el-tag>
            </div>
            <div class="note-meta">
              <span class="date">{{ formatDate(note.createdAt) }}</span>
              <span class="views">
                <el-icon><View /></el-icon>
                {{ note.viewCount }}
              </span>
            </div>
            <div class="note-tags" v-if="note.tags">
              <el-tag
                v-for="tag in note.tags.split(',')"
                :key="tag"
                size="small"
                type="info"
                @click.stop="goToTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Upload, View } from '@element-plus/icons-vue'
import { noteApi } from '@/utils/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

const notes = ref([])
const categories = ref([])
const tags = ref([])
const searchKeyword = ref('')

// 获取所有笔记
const fetchNotes = async () => {
  try {
    const res = await noteApi.getAll()
    notes.value = res.data || []
  } catch (error) {
    ElMessage.error('获取笔记列表失败')
  }
}

// 获取分类
const fetchCategories = async () => {
  try {
    const res = await noteApi.getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取标签
const fetchTags = async () => {
  try {
    const res = await noteApi.getTags()
    tags.value = res.data || []
  } catch (error) {
    console.error('获取标签失败:', error)
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    fetchNotes()
    return
  }
  try {
    const res = await noteApi.search(searchKeyword.value.trim())
    notes.value = res.data || []
    ElMessage.success(`找到 ${notes.value.length} 条结果`)
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

// 跳转到笔记详情
const goToNote = (id) => {
  router.push(`/note/${id}`)
}

// 跳转到分类
const goToCategory = (category) => {
  router.push(`/category/${encodeURIComponent(category)}`)
}

// 跳转到标签
const goToTag = (tag) => {
  router.push(`/tag/${encodeURIComponent(tag)}`)
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchNotes()
  fetchCategories()
  fetchTags()
})
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 1400px;
  margin: 0 auto;
}

.search-bar {
  margin-bottom: 20px;
  max-width: 600px;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .categories,
  .tags {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    flex-wrap: wrap;

    &:last-child {
      margin-bottom: 0;
    }

    .label {
      font-weight: 500;
      color: #606266;
      margin-right: 10px;
      white-space: nowrap;
    }

    .filter-tag {
      margin-right: 8px;
      margin-bottom: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      }
    }
  }
}

.notes-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      color: #303133;
    }
  }

  .note-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }

    .note-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .note-title {
        margin: 0;
        font-size: 16px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
      }

      .el-tag {
        cursor: pointer;
        transition: opacity 0.3s;

        &:hover {
          opacity: 0.8;
        }
      }
    }

    .note-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      font-size: 12px;
      color: #909399;

      .views {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .note-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;

      .el-tag {
        cursor: pointer;
        transition: opacity 0.3s;

        &:hover {
          opacity: 0.8;
        }
      }
    }
  }
}
</style>