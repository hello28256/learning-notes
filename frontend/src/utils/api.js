import request from './request'

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
  delete: (id) => request.delete(`/notes/${id}`)
}