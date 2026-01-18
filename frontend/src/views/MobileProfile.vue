<template>
  <div class="mobile-profile">
    <!-- 用户信息卡片 -->
    <div class="profile-card">
      <div class="user-avatar">
        <el-icon :size="48"><User /></el-icon>
      </div>
      <div class="user-info">
        <div class="user-name">{{ userInfo.realName || userInfo.username || '用户' }}</div>
        <div class="user-role">{{ userInfo.roleName || '普通用户' }}</div>
      </div>
    </div>

    <!-- 功能列表 -->
    <div class="menu-section">
      <div class="menu-item" @click="navigateTo('/profile')">
        <el-icon :size="20"><User /></el-icon>
        <span>个人资料</span>
        <el-icon :size="16" class="arrow-icon"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="navigateTo('/system-config/change-password')">
        <el-icon :size="20"><Lock /></el-icon>
        <span>修改密码</span>
        <el-icon :size="16" class="arrow-icon"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="navigateTo('/mobile-overview')">
        <el-icon :size="20"><Setting /></el-icon>
        <span>返回桌面版</span>
        <el-icon :size="16" class="arrow-icon"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <el-button type="danger" size="large" class="logout-button" @click="handleLogout">
        退出登录
      </el-button>
    </div>

    <!-- 底部导航栏 -->
    <div class="mobile-bottom-nav">
      <div
        v-for="item in bottomNavItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: currentPath === item.path }"
        @click="navigateTo(item.path)"
      >
        <el-icon :size="22">
          <component :is="item.icon" v-if="item.icon" />
        </el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Lock,
  Setting,
  ArrowRight,
  HomeFilled,
  Monitor,
  Bell
} from '@element-plus/icons-vue'
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()

// 用户信息
const userInfo = ref({})

// 底部导航项
const bottomNavItems = [
  { path: '/mobile-overview', label: '概览', icon: HomeFilled },
  { path: '/mobile-device-list', label: '设备', icon: Monitor },
  { path: '/mobile-alarm', label: '报警', icon: Bell },
  { path: '/mobile-profile', label: '我的', icon: User }
]

const currentPath = computed(() => route.path)

// 加载用户信息
const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      userInfo.value = JSON.parse(userInfoStr)
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }
}

// 导航
const navigateTo = (path) => {
  router.push(path)
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    try {
      await logout()
    } catch (error) {
      console.error('退出登录API调用失败:', error)
    }

    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    ElMessage.success('已退出登录')
    router.push('/mobile-login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

// 初始化
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.mobile-profile {
  min-height: 100vh;
  background: #f5f5f7;
  padding-bottom: 80px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.profile-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 30px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;

}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-role {
  font-size: 14px;
  opacity: 0.8;
}

.menu-section {
  margin: 16px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #f9f9f9;
}

.menu-item:active {
  background: #f0f0f0;
}

.arrow-icon {
  margin-left: auto;
  color: #86868b;
}

.menu-item span {
  flex: 1;
  font-size: 15px;
  color: #1d1d1f;
}

.menu-item :deep(.el-icon) {
  color: #667eea;
}

.logout-section {
  padding: 0 16px;
  margin-top: 24px;

}

.logout-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
}

/* 底部导航栏 */
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-around;
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom));
  z-index: 1000;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #86868b;
  flex: 1;
}

.nav-item.active {
  color: #667eea;
}

.nav-item.active .nav-label {
  color: #667eea;
}

.nav-label {
  font-size: 11px;
  transition: color 0.3s ease;
}

.nav-item:hover {
  color: #667eea;
}

/* 移动端响应式优化 */
@media (max-width: 375px) {
  .profile-card {
    padding: 32px 16px 24px;
  }

  .user-avatar {
    width: 56px;
    height: 56px;
  }

  .user-name {
    font-size: 18px;
  }

  .menu-section {
    margin: 12px;
  }

  .menu-item {
    padding: 14px 16px;
  }
}
</style>