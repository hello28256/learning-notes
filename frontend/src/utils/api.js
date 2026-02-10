import request from './request'

// 认证相关 API
export const authApi = {
  // 登录
  login: (data) => request.post('/auth/login', data),

  // 注册
  register: (data) => request.post('/auth/register', data),

  // 获取当前用户信息
  getCurrentUser: () => request.get('/auth/me')
}

// 笔记相关 API
export const noteApi = {
  // 上传文件
  upload: (formData) => request.post('/notes/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),

  // 获取所有笔记
  getAll: () => request.get('/notes'),

  // 根据 ID 获取笔记
  getById: (id) => request.get(`/notes/${id}`),

  // 搜索笔记
  search: (title) => request.get('/notes/search', { params: { title } }),

  // 根据分类获取笔记
  getByCategory: (category) => request.get(`/notes/category/${category}`),

  // 根据标签获取笔记
  getByTag: (tag) => request.get(`/notes/tag/${tag}`),

  // 获取所有分类
  getCategories: () => request.get('/notes/categories'),

  // 获取所有标签
  getTags: () => request.get('/notes/tags'),

  // 更新笔记
  update: (id, data) => request.put(`/notes/${id}`, null, { params: data }),

  // 删除笔记
  delete: (id) => request.delete(`/notes/${id}`),

  // 获取提交历史（用于热力图）
  getContributions: () => request.get('/notes/contributions')
}

// 用户相关 API
export const userApi = {
  // 更新头像
  updateAvatar: (avatar) => request.put('/user/avatar', { avatar }),

  // 更新用户名
  updateUsername: (username) => request.put('/user/username', { username }),

  // 更新密码
  updatePassword: (data) => request.put('/user/password', data)
}

// 分享相关 API
export const shareApi = {
  // 创建分享链接
  create: (noteId, data) => request.post(`/share/create/${noteId}`, null, { params: data }),

  // 根据短码获取分享内容
  getByCode: (shareCode) => request.get(`/share/s/${shareCode}`),

  // 获取笔记的分享信息
  getInfo: (noteId) => request.get(`/share/info/${noteId}`),

  // 取消分享
  cancel: (noteId) => request.post(`/share/cancel/${noteId}`)
}