import axios from 'axios'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.success !== undefined && !res.success) {
      console.error('响应错误:', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    // 处理后端返回的错误响应
    if (error.response && error.response.data) {
      const errorData = error.response.data
      const errorMessage = errorData.message || '请求失败'
      return Promise.reject(new Error(errorMessage))
    }
    return Promise.reject(error)
  }
)

export default service