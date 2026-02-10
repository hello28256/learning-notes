<template>
  <div id="app" :class="{ 'dark-mode': themeState.isDark }">
    <el-container class="layout-container">
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            <el-icon><Notebook /></el-icon>
            <span>学习笔记</span>
          </div>
          <div class="nav-menu">
            <el-menu
              :default-active="$route.path"
              mode="horizontal"
              router
              class="nav-menu-items"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/upload" v-if="isLoggedIn">上传笔记</el-menu-item>
            </el-menu>
          </div>
          <div class="user-section">
            <!-- 主题切换按钮 -->
            <el-button
              class="theme-toggle"
              circle
              @click="toggleTheme"
              :title="themeState.isDark ? '切换到白天模式' : '切换到夜间模式'"
            >
              <el-icon v-if="themeState.isDark"><Sunny /></el-icon>
              <el-icon v-else><Moon /></el-icon>
            </el-button>
            
            <template v-if="isLoggedIn">
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <img v-if="userInfo?.avatar" :src="userInfo.avatar" class="user-avatar" alt="头像" />
                  <el-icon v-else><User /></el-icon>
                  {{ username }}
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><Setting /></el-icon>个人设置
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" @click="$router.push('/login')">
                登录
              </el-button>
            </template>
          </div>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { Notebook, User, ArrowDown, Sunny, Moon, Setting, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { isLoggedIn, username, logout, userInfo } from '@/utils/auth'
import { themeState, toggleTheme, initTheme } from '@/utils/theme'
import { onMounted } from 'vue'

const router = useRouter()

onMounted(() => {
  initTheme()
})

const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style lang="scss">
// 导入主题样式
@import './styles/theme.css';

#app {
  height: 100vh;
  overflow: hidden;
}

.layout-container {
  height: 100%;
  background-color: var(--bg-secondary);
  transition: background-color 0.3s ease;
}

.header {
  background: var(--bg-header);
  color: var(--text-white);
  padding: 0;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.1);
  transition: background 0.3s ease;
  border-bottom: 1px solid var(--border-color);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.3s;
  color: var(--text-white);

  &:hover {
    opacity: 0.8;
  }
}

.nav-menu {
  height: 100%;
}

.nav-menu-items {
  background: var(--menu-bg);
  border: none;
  height: 100%;

  :deep(.el-menu-item) {
    color: var(--text-white);
    border-bottom: 2px solid transparent;

    &:hover {
      background: var(--menu-hover);
    }

    &.is-active {
      background: var(--menu-active);
      border-bottom-color: var(--text-white);
    }
  }
}

.user-section {
  display: flex;
  align-items: center;
  gap: 12px;

  .theme-toggle {
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: var(--text-white);
    font-size: 16px;
    width: 32px;
    height: 32px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.5);
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 5px;
    color: var(--text-white);
    cursor: pointer;
    padding: 6px 10px;
    border-radius: 6px;
    transition: background 0.2s;
    font-size: 14px;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .user-avatar {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid rgba(255, 255, 255, 0.3);
    }
  }
}

.main-content {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  padding: 20px;
  overflow-y: auto;
  transition: background-color 0.3s ease, color 0.3s ease;
}
</style>
