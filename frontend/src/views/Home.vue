<template>
  <div class="home-page">
    <!-- 提交历史热力图 -->
    <ContributionGraph :contributions="contributions" />

    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">📚 我的学习笔记</h1>
        <p class="hero-subtitle">记录学习，分享知识，持续成长</p>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-number">{{ notes.length }}</span>
            <span class="stat-label">笔记</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ categories.length }}</span>
            <span class="stat-label">分类</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ tags.length }}</span>
            <span class="stat-label">标签</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索笔记标题、内容..."
          size="large"
          @keyup.enter="handleSearch"
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch" type="primary">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 分类和标签 -->
    <div class="filter-section">
      <div class="filter-group">
        <div class="filter-title">
          <el-icon><Folder /></el-icon>
          分类
        </div>
        <div class="filter-tags">
          <el-tag
            v-for="category in categories"
            :key="category"
            @click="goToCategory(category)"
            class="filter-tag category-tag"
            effect="light"
          >
            {{ category }}
          </el-tag>
        </div>
      </div>
      <div class="filter-group">
        <div class="filter-title">
          <el-icon><CollectionTag /></el-icon>
          标签
        </div>
        <div class="filter-tags">
          <el-tag
            v-for="tag in tags"
            :key="tag"
            @click="goToTag(tag)"
            class="filter-tag"
            type="info"
            effect="light"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 笔记列表 -->
    <div class="notes-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>最新笔记</span>
        </div>
        <el-button type="primary" @click="$router.push('/upload')" :icon="Plus" round>
          新建笔记
        </el-button>
      </div>

      <el-empty v-if="notes.length === 0" description="暂无笔记，快来创建第一条笔记吧~">
        <el-button type="primary" @click="$router.push('/upload')">去上传</el-button>
      </el-empty>

      <div v-else class="notes-grid">
        <div
          v-for="note in notes"
          :key="note.id"
          class="note-card"
          @click="goToNote(note.id)"
        >
          <div class="note-card-header">
            <div class="note-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="note-category" v-if="note.category">
              {{ note.category }}
            </div>
          </div>
          <h4 class="note-title">{{ note.title }}</h4>
          <div class="note-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(note.createdAt) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ note.viewCount }}
            </span>
          </div>
          <div class="note-tags" v-if="note.tags">
            <span
              v-for="tag in note.tags.split(',').slice(0, 3)"
              :key="tag"
              class="note-tag"
              @click.stop="goToTag(tag)"
            >
              #{{ tag.trim() }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, View, Calendar, Document, Folder, CollectionTag } from '@element-plus/icons-vue'
import { noteApi } from '@/utils/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import ContributionGraph from '@/components/ContributionGraph.vue'

const router = useRouter()

const notes = ref([])
const categories = ref([])
const tags = ref([])
const searchKeyword = ref('')
const contributions = ref([])

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
  return dayjs(date).format('MM-DD')
}

// 获取提交历史
const fetchContributions = async () => {
  try {
    const res = await noteApi.getContributions()
    if (res.success) {
      contributions.value = res.data || []
    }
  } catch (error) {
    console.error('获取提交历史失败:', error)
  }
}

onMounted(() => {
  fetchNotes()
  fetchCategories()
  fetchTags()
  fetchContributions()
})
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

// Hero 区域
.hero-section {
  text-align: center;
  padding: 40px 0;
  margin-bottom: 20px;

  .hero-content {
    .hero-title {
      font-size: 36px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0 0 12px 0;
      letter-spacing: -0.5px;
    }

    .hero-subtitle {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0 0 30px 0;
    }

    .hero-stats {
      display: flex;
      justify-content: center;
      gap: 40px;

      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;

        .stat-number {
          font-size: 32px;
          font-weight: 700;
          color: var(--accent-color);
          line-height: 1;
        }

        .stat-label {
          font-size: 14px;
          color: var(--text-secondary);
          margin-top: 4px;
        }
      }
    }
  }
}

// 搜索区域
.search-section {
  margin-bottom: 30px;

  .search-bar {
    max-width: 600px;
    margin: 0 auto;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 25px 0 0 25px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        padding-left: 15px;
      }

      :deep(.el-input-group__append) {
        border-radius: 0 25px 25px 0;
        overflow: hidden;

        .el-button {
          border-radius: 0;
          padding: 0 25px;
        }
      }
    }
  }
}

// 筛选区域
.filter-section {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: var(--card-shadow);

  .filter-group {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .filter-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 12px;

      .el-icon {
        color: var(--accent-color);
      }
    }

    .filter-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .filter-tag {
        cursor: pointer;
        transition: all 0.3s;
        border-radius: 16px;
        padding: 6px 14px;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        &.category-tag {
          background: var(--accent-color);
          color: white;
          border-color: var(--accent-color);
        }
      }
    }
  }
}

// 笔记区域
.notes-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);

      .el-icon {
        color: var(--accent-color);
      }
    }
  }

  .notes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;

    .note-card {
      background: var(--card-bg);
      border-radius: 12px;
      padding: 20px;
      box-shadow: var(--card-shadow);
      cursor: pointer;
      transition: all 0.3s ease;
      border: 1px solid var(--border-color);

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
        border-color: var(--accent-color);
      }

      .note-card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .note-icon {
          width: 40px;
          height: 40px;
          background: linear-gradient(135deg, var(--accent-color), var(--link-hover));
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 20px;
        }

        .note-category {
          font-size: 12px;
          color: var(--accent-color);
          background: rgba(9, 105, 218, 0.1);
          padding: 4px 10px;
          border-radius: 12px;
          font-weight: 500;
        }
      }

      .note-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 12px 0;
        line-height: 1.4;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .note-meta {
        display: flex;
        gap: 16px;
        margin-bottom: 12px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: var(--text-tertiary);

          .el-icon {
            font-size: 14px;
          }
        }
      }

      .note-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .note-tag {
          font-size: 12px;
          color: var(--text-secondary);
          background: var(--bg-secondary);
          padding: 3px 8px;
          border-radius: 4px;
          transition: all 0.2s;

          &:hover {
            color: var(--accent-color);
            background: rgba(9, 105, 218, 0.1);
          }
        }
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .hero-section {
    padding: 30px 0;

    .hero-content {
      .hero-title {
        font-size: 28px;
      }

      .hero-stats {
        gap: 24px;

        .stat-item {
          .stat-number {
            font-size: 24px;
          }
        }
      }
    }
  }

  .notes-grid {
    grid-template-columns: 1fr !important;
  }
}
</style>
