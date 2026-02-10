<template>
  <div class="profile-page">
    <div class="profile-container">
      <h2 class="page-title">个人设置</h2>

      <!-- 头像设置 -->
      <div class="setting-section">
        <h3 class="section-title">头像设置</h3>
        <div class="avatar-section">
          <div class="current-avatar">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" />
            <el-icon v-else class="default-avatar"><User /></el-icon>
          </div>
          <div class="avatar-options">
            <p class="avatar-hint">选择系统头像</p>
            <div class="avatar-grid">
              <div
                v-for="(avatar, index) in presetAvatars"
                :key="index"
                class="avatar-item"
                :class="{ active: selectedAvatar === avatar }"
                @click="selectAvatar(avatar)"
              >
                <img :src="avatar" alt="头像选项" />
              </div>
            </div>
            <div class="avatar-actions">
              <el-button type="primary" @click="saveAvatar" :loading="savingAvatar">
                保存头像
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户名设置 -->
      <div class="setting-section">
        <h3 class="section-title">修改用户名</h3>
        <el-form :model="usernameForm" :rules="usernameRules" ref="usernameFormRef" label-width="100px">
          <el-form-item label="当前用户名">
            <span class="current-value">{{ userInfo.username }}</span>
          </el-form-item>
          <el-form-item label="新用户名" prop="newUsername">
            <el-input
              v-model="usernameForm.newUsername"
              placeholder="请输入新用户名"
              maxlength="20"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateUsername" :loading="savingUsername">
              修改用户名
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 密码设置 -->
      <div class="setting-section">
        <h3 class="section-title">修改密码</h3>
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input
              v-model="passwordForm.currentPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updatePassword" :loading="savingPassword">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { userInfo, fetchUserInfo } from '@/utils/auth'
import { userApi } from '@/utils/api'

// 预设头像列表
const presetAvatars = [
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Zack',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Bella',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Leo',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Molly',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Max',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Luna',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Charlie',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Lucy',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Daisy'
]

const selectedAvatar = ref('')
const savingAvatar = ref(false)
const savingUsername = ref(false)
const savingPassword = ref(false)

// 用户名表单
const usernameFormRef = ref(null)
const usernameForm = reactive({
  newUsername: ''
})

const usernameRules = {
  newUsername: [
    { required: true, message: '请输入新用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 选择头像
const selectAvatar = (avatar) => {
  selectedAvatar.value = avatar
}

// 保存头像
const saveAvatar = async () => {
  if (!selectedAvatar.value) {
    ElMessage.warning('请先选择一个头像')
    return
  }

  savingAvatar.value = true
  try {
    const res = await userApi.updateAvatar(selectedAvatar.value)
    if (res.success) {
      ElMessage.success('头像更新成功')
      await fetchUserInfo()
    } else {
      ElMessage.error(res.message || '头像更新失败')
    }
  } catch (error) {
    ElMessage.error('头像更新失败')
  } finally {
    savingAvatar.value = false
  }
}

// 修改用户名
const updateUsername = async () => {
  const valid = await usernameFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingUsername.value = true
  try {
    const res = await userApi.updateUsername(usernameForm.newUsername)
    if (res.success) {
      ElMessage.success('用户名修改成功')
      await fetchUserInfo()
      usernameForm.newUsername = ''
    } else {
      ElMessage.error(res.message || '用户名修改失败')
    }
  } catch (error) {
    ElMessage.error('用户名修改失败')
  } finally {
    savingUsername.value = false
  }
}

// 修改密码
const updatePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingPassword.value = true
  try {
    const res = await userApi.updatePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.success) {
      ElMessage.success('密码修改成功')
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    savingPassword.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-container {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 30px;
  box-shadow: var(--card-shadow);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid var(--border-color);
}

.setting-section {
  margin-bottom: 40px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.avatar-section {
  display: flex;
  gap: 30px;
  align-items: flex-start;

  @media (max-width: 600px) {
    flex-direction: column;
    align-items: center;
  }
}

.current-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--border-color);
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .default-avatar {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 60px;
    color: var(--text-secondary);
    background: var(--bg-secondary);
  }
}

.avatar-options {
  flex: 1;
}

.avatar-hint {
  color: var(--text-secondary);
  margin-bottom: 15px;
  font-size: 14px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;

  @media (max-width: 600px) {
    grid-template-columns: repeat(4, 1fr);
  }
}

.avatar-item {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.3s ease;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &:hover {
    transform: scale(1.1);
    border-color: var(--accent-color);
  }

  &.active {
    border-color: var(--accent-color);
    box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.2);
  }
}

.avatar-actions {
  margin-top: 15px;
}

.current-value {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 16px;
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}

:deep(.el-input__inner) {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-primary);

  &::placeholder {
    color: var(--text-secondary);
  }
}
</style>
