import { ref, computed } from 'vue'
import { authApi } from './api'

// 创建响应式状态
const token = ref(localStorage.getItem('token') || '')
const userInfo = ref(null)

// 计算属性
const isLoggedIn = computed(() => !!token.value)
const username = computed(() => userInfo.value?.username || '')
const userId = computed(() => userInfo.value?.userId || null)

// 设置 token
const setToken = (newToken) => {
  token.value = newToken
  localStorage.setItem('token', newToken)
}

// 清除 token
const clearToken = () => {
  token.value = ''
  userInfo.value = null
  localStorage.removeItem('token')
}

// 登录
const login = async (credentials) => {
  const res = await authApi.login(credentials)
  if (res.success) {
    setToken(res.data.token)
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      email: res.data.email,
      avatar: res.data.avatar
    }
  }
  return res
}

// 注册
const register = async (data) => {
  return await authApi.register(data)
}

// 获取用户信息
const fetchUserInfo = async () => {
  if (!token.value) return
  try {
    const res = await authApi.getCurrentUser()
    if (res.success) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    clearToken()
  }
}

// 退出登录
const logout = () => {
  clearToken()
}

// 初始化
if (token.value) {
  fetchUserInfo()
}

export {
  token,
  userInfo,
  isLoggedIn,
  username,
  userId,
  login,
  register,
  logout,
  fetchUserInfo,
  setToken,
  clearToken
}
