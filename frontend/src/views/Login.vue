<template>
  <div class="login-container">
    <!-- 物联网动态背景装饰 -->
    <div class="bg-decoration">
      <div class="grid-pattern"></div>
      <div class="data-flow"></div>
      
      <!-- 连接节点网络 -->
      <div class="connection-nodes">
        <div v-for="i in 15" :key="i" class="node" :style="getNodeStyle(i)">
          <div class="node-core"></div>
          <div class="node-ring"></div>
        </div>
      </div>
      
      <!-- 数据包流动动画 -->
      <div class="data-packages">
        <div v-for="i in 6" :key="i" class="data-package" :style="getPackageStyle(i)">
          <div class="package-content"></div>
        </div>
      </div>
      
      <!-- 连接线动画 -->
      <svg class="connection-lines" viewBox="0 0 1920 1080" preserveAspectRatio="none">
        <defs>
          <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:rgba(102, 126, 234, 0.3);stop-opacity:0" />
            <stop offset="50%" style="stop-color:rgba(102, 126, 234, 0.6);stop-opacity:1" />
            <stop offset="100%" style="stop-color:rgba(118, 75, 162, 0.3);stop-opacity:0" />
          </linearGradient>
        </defs>
        <path 
          v-for="(line, index) in connectionLines" 
          :key="index"
          :d="line.path" 
          class="connection-line"
          :style="{ animationDelay: `${index * 0.5}s` }"
          stroke="url(#lineGradient)"
          fill="none"
          stroke-width="2"
        />
      </svg>
    </div>

    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="login-left-content">
        <div class="logo-section">
          <div class="logo-icon-wrapper">
            <div class="logo-icon-glow"></div>
            <div class="logo-icon-pulse"></div>
            <div class="logo-icon">
              <el-icon :size="52"><Connection /></el-icon>
            </div>
          </div>
          <h1 class="logo-title">ThingsLink</h1>
          <p class="logo-subtitle">智慧物联网平台</p>
          <div class="logo-tagline">连接万物，智能未来</div>
        </div>
        
        <div class="feature-list">
          <div 
            v-for="(feature, index) in features" 
            :key="index"
            class="feature-item"
            :style="{ animationDelay: `${index * 0.15}s` }"
          >
            <div class="feature-icon-wrapper">
              <div class="feature-icon-bg"></div>
              <el-icon :size="24" class="feature-icon"><component :is="feature.icon" /></el-icon>
            </div>
            <div class="feature-content">
              <div class="feature-title">{{ feature.title }}</div>
              <div class="feature-desc">{{ feature.desc }}</div>
            </div>
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
              autocomplete="off"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
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
              autocomplete="new-password"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
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
import { 
  Connection, 
  Monitor, 
  DataAnalysis, 
  Bell, 
  User, 
  Lock 
} from '@element-plus/icons-vue'
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

// 功能特性列表
const features = [
  {
    icon: Monitor,
    title: '设备实时监控',
    desc: '全方位设备状态监测与数据采集'
  },
  {
    icon: DataAnalysis,
    title: '数据智能分析',
    desc: 'AI驱动数据洞察与预测分析'
  },
  {
    icon: Bell,
    title: '告警及时通知',
    desc: '智能预警系统保障设备安全'
  }
]

// 连接线路径数据
const connectionLines = [
  { path: 'M200,150 Q400,200 600,180 T1000,200' },
  { path: 'M300,400 Q500,300 800,350 T1200,400' },
  { path: 'M500,600 Q700,500 900,550 T1300,600' },
  { path: 'M150,500 Q350,450 600,500 T1050,550' },
  { path: 'M700,250 Q900,300 1100,280 T1500,300' }
]

// 生成节点位置样式
const getNodeStyle = (index) => {
  const positions = [
    { top: '8%', left: '12%' },
    { top: '22%', left: '78%' },
    { top: '38%', left: '18%' },
    { top: '52%', left: '72%' },
    { top: '68%', left: '28%' },
    { top: '12%', left: '48%' },
    { top: '28%', left: '58%' },
    { top: '45%', left: '38%' },
    { top: '62%', left: '68%' },
    { top: '18%', left: '32%' },
    { top: '35%', left: '82%' },
    { top: '55%', left: '52%' },
    { top: '75%', left: '62%' },
    { top: '25%', left: '25%' },
    { top: '42%', left: '88%' }
  ]
  const pos = positions[(index - 1) % positions.length]
  const delay = (index - 1) * 0.25
  return {
    top: pos.top,
    left: pos.left,
    animationDelay: `${delay}s`
  }
}

// 生成数据包位置样式
const getPackageStyle = (index) => {
  const paths = [
    { startX: 200, startY: 150, endX: 800, endY: 350, duration: 4 },
    { startX: 400, startY: 400, endX: 1200, endY: 500, duration: 5 },
    { startX: 600, startY: 250, endX: 1000, endY: 550, duration: 6 },
    { startX: 300, startY: 500, endX: 1100, endY: 300, duration: 5.5 },
    { startX: 500, startY: 180, endX: 900, endY: 450, duration: 4.5 },
    { startX: 700, startY: 350, endX: 1300, endY: 600, duration: 6.5 }
  ]
  const path = paths[(index - 1) % paths.length]
  const delay = (index - 1) * 0.8
  return {
    '--start-x': `${path.startX}px`,
    '--start-y': `${path.startY}px`,
    '--end-x': `${path.endX}px`,
    '--end-y': `${path.endY}px`,
    animationDuration: `${path.duration}s`,
    animationDelay: `${delay}s`
  }
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 先验证表单
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true

    // 调用登录API
    const result = await login({
      username: loginForm.username,
      password: loginForm.password
    })

    // result 已经是 data 部分
    if (result) {
      const { token, user, menus } = result

      // 保存 token（同步操作，立即完成）
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(user))
      localStorage.setItem('menus', JSON.stringify(menus || []))
      
      // 跳转到目标页面或首页（使用 replace 避免在历史记录中留下登录页）
      const redirect = route.query.redirect || '/overview'
      
      // 立即跳转，不等待任何延迟（同步操作已完成后直接跳转）
      try {
        // 使用 replace 跳转，立即执行，不等待Promise
        router.replace(redirect)
        
        // 显示成功消息（异步，不阻塞跳转）
        setTimeout(() => {
          ElMessage.success('登录成功')
        }, 100)
      } catch (error) {
        console.error('路由跳转失败:', error)
        // 如果路由跳转失败，使用 window.location 作为降级方案
        window.location.href = redirect
      }
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    // 如果是表单验证错误，不显示错误消息
    if (error !== false && !error.message?.includes('validate')) {
      ElMessage.error(error.message || '登录失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="css">
.login-container {
  position: relative;
  display: flex;
  min-height: 100vh;
  max-height: 100vh;
  height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 
               'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 物联网动态背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(102, 126, 234, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(102, 126, 234, 0.08) 1px, transparent 1px);
  background-size: 64px 64px;
  opacity: 0.4;
  animation: gridMove 25s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(64px, 64px); }
}

.data-flow {
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background: radial-gradient(circle at center, 
    rgba(102, 126, 234, 0.1) 0%, 
    transparent 70%);
  animation: rotateFlow 40s linear infinite;
}

@keyframes rotateFlow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 连接节点网络 */
.connection-nodes {
  position: absolute;
  inset: 0;
}

.node {
  position: absolute;
  width: 12px;
  height: 12px;
  transform: translate(-50%, -50%);
  animation: nodePulse 3s ease-in-out infinite;
}

.node-core {
  position: absolute;
  inset: 0;
  background: rgba(102, 126, 234, 0.8);
  border-radius: 50%;
  box-shadow: 0 0 16px rgba(102, 126, 234, 1);
}

.node-ring {
  position: absolute;
  inset: -8px;
  border: 2px solid rgba(102, 126, 234, 0.4);
  border-radius: 50%;
  animation: ringExpand 3s ease-out infinite;
}

@keyframes nodePulse {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.8;
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.3);
    opacity: 1;
  }
}

@keyframes ringExpand {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}

/* 数据包流动动画 */
.data-packages {
  position: absolute;
  inset: 0;
}

.data-package {
  position: absolute;
  width: 16px;
  height: 16px;
  left: var(--start-x);
  top: var(--start-y);
  animation: packageMove var(--animation-duration, 5s) linear infinite;
  animation-delay: var(--animation-delay, 0s);
}

.package-content {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.9), rgba(118, 75, 162, 0.9));
  border-radius: 4px;
  box-shadow: 0 0 12px rgba(102, 126, 234, 0.8);
  transform: rotate(45deg);
}

@keyframes packageMove {
  0% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translate(
      calc(var(--end-x) - var(--start-x)), 
      calc(var(--end-y) - var(--start-y))
    ) rotate(360deg);
    opacity: 0;
  }
}

/* 连接线动画 */
.connection-lines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0.3;
}

.connection-line {
  stroke-dasharray: 10 5;
  animation: lineFlow 3s linear infinite;
  opacity: 0.6;
}

@keyframes lineFlow {
  0% {
    stroke-dashoffset: 0;
    opacity: 0.3;
  }
  50% {
    opacity: 0.8;
  }
  100% {
    stroke-dashoffset: -30;
    opacity: 0.3;
  }
}

/* 左侧装饰区域 - 优化间距适配1080p */
.login-left {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 64px;
  min-height: 0;
  z-index: 1;
}

.login-left-content {
  max-width: 560px;
  width: 100%;
  color: white;
  animation: fadeInLeft 1s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.logo-section {
  margin-bottom: 48px;
  text-align: center;
}

.logo-icon-wrapper {
  position: relative;
  width: 96px;
  height: 96px;
  margin: 0 auto 24px;
}

.logo-icon-glow {
  position: absolute;
  inset: -12px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.6), transparent 70%);
  border-radius: 50%;
  filter: blur(24px);
  animation: glowPulse 4s ease-in-out infinite;
}

.logo-icon-pulse {
  position: absolute;
  inset: -16px;
  border: 2px solid rgba(102, 126, 234, 0.4);
  border-radius: 50%;
  animation: iconPulse 3s ease-out infinite;
}

@keyframes glowPulse {
  0%, 100% { 
    opacity: 0.5; 
    transform: scale(1); 
  }
  50% { 
    opacity: 1; 
    transform: scale(1.1); 
  }
}

@keyframes iconPulse {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.logo-icon {
  position: relative;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(102, 126, 234, 0.4);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(12px);
  color: #667eea;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.logo-icon:hover {
  transform: translateY(-6px) scale(1.05);
  box-shadow: 0 16px 40px rgba(102, 126, 234, 0.5);
  border-color: rgba(102, 126, 234, 0.6);
}

.logo-title {
  font-size: 44px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -1px;
  color: #ffffff;
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 18px;
  opacity: 0.9;
  font-weight: 400;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.logo-tagline {
  font-size: 13px;
  opacity: 0.6;
  font-weight: 300;
  letter-spacing: 1.5px;
  margin-top: 4px;
}

/* 特性列表 - 优化间距适配1080p */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px 24px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(12px);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  animation: fadeInUp 0.8s ease-out both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(102, 126, 234, 0.5);
  transform: translateX(12px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.25);
}

.feature-icon-wrapper {
  position: relative;
  width: 52px;
  height: 52px;
  flex-shrink: 0;
}

.feature-icon-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2), rgba(118, 75, 162, 0.2));
  border-radius: 16px;
  transition: all 0.3s ease;
}

.feature-item:hover .feature-icon-bg {
  transform: scale(1.1);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
}

.feature-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #667eea;
  z-index: 1;
}

.feature-content {
  flex: 1;
  min-width: 0;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #ffffff;
  letter-spacing: 0.2px;
}

.feature-desc {
  font-size: 13px;
  opacity: 0.7;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.4;
}

/* 右侧登录表单 - 优化间距适配1080p */
.login-right {
  position: relative;
  flex: 0 0 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 48px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(24px);
  min-height: 0;
  z-index: 1;
}

.login-form-wrapper {
  width: 100%;
  max-width: 440px;
  animation: fadeInRight 1s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.form-header {
  margin-bottom: 32px;
  text-align: center;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.form-header p {
  font-size: 15px;
  color: #86868b;
  font-weight: 400;
}

.login-form {
  margin-bottom: 0;
}

/* 表单项间距 */
.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.input-icon {
  color: #86868b;
  font-size: 18px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  letter-spacing: 0.5px;
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.45);
}

.login-button:active {
  transform: translateY(-1px);
}

/* Element Plus 样式覆盖 */
:deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06) inset;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  background: #ffffff;
  min-height: 48px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.3) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) inset, 
              0 0 0 4px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__inner) {
  font-size: 15px;
  line-height: 1.5;
}

:deep(.el-input__inner::placeholder) {
  color: #a1a1a6;
}

:deep(.el-form-item__error) {
  font-size: 12px;
  margin-top: 8px;
  padding-left: 4px;
}

:deep(.el-button.is-loading) {
  pointer-events: none;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }

  .login-right {
    flex: 1;
    max-width: 100%;
    padding: 60px 32px;
  }
}

@media (max-width: 640px) {
  .login-right {
    padding: 48px 24px;
  }

  .form-header h2 {
    font-size: 32px;
  }

  .login-form-wrapper {
    max-width: 100%;
  }

  .login-form :deep(.el-form-item) {
    margin-bottom: 24px;
  }
}

/* 减少动画（可访问性） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
