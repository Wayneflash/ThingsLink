<template>
  <div class="login-container">
    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="login-left-content">
        <div class="logo-section">
          <div class="logo-icon">
            <el-icon :size="48"><Connection /></el-icon>
          </div>
          <h1 class="logo-title">ThingsLink</h1>
          <p class="logo-subtitle">物联网平台</p>
        </div>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon :size="20"><Monitor /></el-icon>
            <span>设备实时监控</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><DataAnalysis /></el-icon>
            <span>数据智能分析</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Bell /></el-icon>
            <span>告警及时通知</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号和密码</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>


      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Connection, Monitor, DataAnalysis, Bell, User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      loading.value = true

      // 调用登录API
      const result = await login({
        username: loginForm.username,
        password: loginForm.password
      })

      // result 已经是 data 部分
      if (result) {
        const { token, user, menus } = result

        // 保存 token
        localStorage.setItem('token', token)

        // 保存用户信息
        localStorage.setItem('userInfo', JSON.stringify(user))

        // 保存菜单数据
        localStorage.setItem('menus', JSON.stringify(menus || []))

        ElMessage.success('登录成功')

        // 跳转到目标页面或首页
        const redirect = route.query.redirect || '/overview'
        router.push(redirect)
      } else {
        ElMessage.error(result.message || '登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 左侧装饰区域 */
.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.login-left-content {
  max-width: 500px;
  color: white;
}

.logo-section {
  margin-bottom: 60px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: white;
}

.logo-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.logo-subtitle {
  font-size: 18px;
  opacity: 0.9;
  font-weight: 300;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(5px);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateX(10px);
}

/* 右侧登录表单 */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  padding: 60px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 420px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 16px;
  color: #86868b;
}

.login-form {
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-tips {
  margin-top: 24px;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}

.tips-content strong {
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }

  .login-right {
    flex: 1;
  }
}

@media (max-width: 640px) {
  .login-right {
    padding: 40px 20px;
  }

  .form-header h2 {
    font-size: 24px;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-input__wrapper) {
  padding: 8px 15px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e5e5e7 inset;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #667eea inset;
}

:deep(.el-form-item__error) {
  font-size: 12px;
}

:deep(.el-alert) {
  border-radius: 10px;
  border: none;
  background: #f5f7fa;
}

:deep(.el-alert__icon) {
  color: #667eea;
}
</style>
