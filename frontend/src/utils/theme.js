import { reactive, watch } from 'vue'

// 主题状态
export const themeState = reactive({
  isDark: localStorage.getItem('theme') === 'dark' || false
})

// 切换主题
export function toggleTheme() {
  themeState.isDark = !themeState.isDark
  localStorage.setItem('theme', themeState.isDark ? 'dark' : 'light')
  applyTheme()
}

// 设置主题
export function setTheme(isDark) {
  themeState.isDark = isDark
  localStorage.setItem('theme', isDark ? 'dark' : 'light')
  applyTheme()
}

// 应用主题到 DOM
export function applyTheme() {
  const html = document.documentElement
  if (themeState.isDark) {
    html.classList.add('dark-theme')
    html.classList.remove('light-theme')
  } else {
    html.classList.add('light-theme')
    html.classList.remove('dark-theme')
  }
}

// 初始化主题
export function initTheme() {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    themeState.isDark = savedTheme === 'dark'
  } else {
    // 检测系统偏好
    themeState.isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  applyTheme()
}
