import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/Upload.vue')
  },
  {
    path: '/note/:id',
    name: 'NoteDetail',
    component: () => import('@/views/NoteDetail.vue')
  },
  {
    path: '/category/:category',
    name: 'Category',
    component: () => import('@/views/Category.vue')
  },
  {
    path: '/tag/:tag',
    name: 'Tag',
    component: () => import('@/views/Tag.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router