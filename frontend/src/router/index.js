import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/Upload.vue'),
    meta: { requiresAuth: true }
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
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/s/:shareCode',
    name: 'ShareView',
    component: () => import('@/views/ShareView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const loggedIn = isLoggedIn.value

  // 需要登录的页面
  if (to.meta.requiresAuth && !loggedIn) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // 仅游客可访问的页面（如登录页）
  if (to.meta.guestOnly && loggedIn) {
    next('/')
    return
  }

  next()
})

export default router