<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <h2 class="title">{{ isLogin ? '登录' : '注册' }}</h2>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-form-item v-if="!isLogin" label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱（可选）"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="switch-mode">
        <el-link type="primary" @click="toggleMode">
          {{ isLogin ? '还没有账号？立即注册' : '已有账号？立即登录' }}
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { login, register } from '@/utils/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const isLogin = ref(true)

const form = reactive({
  username: '',
  password: '',
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为 6-20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isLogin.value) {
      const res = await login({
        username: form.username,
        password: form.password
      })
      if (res.success) {
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } else {
      const res = await register({
        username: form.username,
        password: form.password,
        email: form.email
      })
      if (res.success) {
        ElMessage.success('注册成功，请登录')
        isLogin.value = true
        form.password = ''
      } else {
        ElMessage.error(res.message || '注册失败')
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  max-width: 90%;

  .title {
    text-align: center;
    margin: 0;
    color: #303133;
  }
}

.login-form {
  margin-top: 20px;

  .submit-btn {
    width: 100%;
  }
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
}
</style>
