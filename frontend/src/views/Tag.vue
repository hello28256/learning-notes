<template>
  <div class="tag-page">
    <el-button @click="$router.back()" :icon="ArrowLeft" class="back-btn">
      返回
    </el-button>

    <el-card class="tag-card">
      <template #header>
        <div class="card-header">
          <h3>标签：{{ tag }}</h3>
          <span class="count">共 {{ notes.length }} 条笔记</span>
        </div>
      </template>

      <el-empty v-if="notes.length === 0" description="该标签下暂无笔记" />

      <el-row :gutter="20" v-else>
        <el-col
          v-for="note in notes"
          :key="note.id"
          :xs="24"
          :sm="12"
          :md="8"
        >
          <el-card class="note-card" @click="goToNote(note.id)" shadow="hover">
            <div class="note-header">
              <h4 class="note-title">{{ note.title }}</h4>
              <el-tag
                v-if="note.category"
                size="small"
                @click.stop="goToCategory(note.category)"
              >
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
                :effect="tag === route.params.tag ? 'dark' : 'plain'"
                @click.stop="goToTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { noteApi } from '@/utils/api'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const tag = computed(() => route.params.tag)
const notes = ref([])

// 获取标签下的笔记
const fetchNotesByTag = async () => {
  try {
    const res = await noteApi.getByTag(tag.value)
    notes.value = res.data || []
  } catch (error) {
    ElMessage.error('获取笔记失败')
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
  fetchNotesByTag()
})
</script>

<style lang="scss" scoped>
.tag-page {
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.tag-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      color: #303133;
    }

    .count {
      color: #909399;
      font-size: 14px;
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