<template>
  <div id="app">
    <el-container class="layout-container">
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            <el-icon><Notebook /></el-icon>
            <span>学习笔记管理系统</span>
          </div>
          <div class="nav-menu">
            <el-menu
              :default-active="$route.path"
              mode="horizontal"
              router
              class="nav-menu-items"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/upload">上传笔记</el-menu-item>
            </el-menu>
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
import { Notebook } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
#app {
  height: 100vh;
  overflow: hidden;
}

.layout-container {
  height: 100%;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
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

  &:hover {
    opacity: 0.8;
  }
}

.nav-menu {
  height: 100%;
}

.nav-menu-items {
  background: transparent;
  border: none;
  height: 100%;

  :deep(.el-menu-item) {
    color: white;
    border-bottom: 2px solid transparent;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    &.is-active {
      background: rgba(255, 255, 255, 0.15);
      border-bottom-color: white;
    }
  }
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>