<template>
  <view class="profile-page">
    <scroll-view scroll-y class="content-scroll">
      <view class="profile-content">
        <!-- 用户信息卡片 -->
        <view class="profile-card">
          <view class="user-avatar">
            <Icon name="User" :size="48" color="#ffffff" />
          </view>
          <view class="user-info">
            <text class="user-name">{{ userInfo.username || userInfo.name || '用户' }}</text>
            <text class="user-role">{{ userInfo.roleName || '普通用户' }}</text>
          </view>
        </view>
        
        <!-- 菜单项 -->
        <view class="menu-section">
          <view class="menu-item" @click="navigateToProfile">
            <Icon name="User" :size="20" color="#1d1d1f" />
            <text>个人资料</text>
            <Icon name="ArrowRight" :size="16" color="#86868b" />
          </view>
          <view class="menu-item" @click="navigateToPassword">
            <Icon name="Lock" :size="20" color="#1d1d1f" />
            <text>修改密码</text>
            <Icon name="ArrowRight" :size="16" color="#86868b" />
          </view>
        </view>
        
        <!-- 退出登录 -->
        <view class="logout-section">
          <button class="logout-button" @click="handleLogout">退出登录</button>
        </view>
      </view>
    </scroll-view>
    <BottomNav />
  </view>
</template>

<script>
import BottomNav from '@/components/BottomNav.vue'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    BottomNav,
    Icon
  },
  data() {
    return {
      userInfo: {}
    }
  },
  onLoad() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo) {
        this.userInfo = userInfo
      } else {
        // 如果没有用户信息，尝试从API获取
        this.fetchUserInfo()
      }
    },
    async fetchUserInfo() {
      try {
        const { getCurrentUser } = await import('@/api/auth')
        const userInfo = await getCurrentUser()
        if (userInfo) {
          this.userInfo = userInfo
          uni.setStorageSync('userInfo', userInfo)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    navigateToProfile() {
      uni.navigateTo({
        url: '/pages/profile/edit'
      })
    },
    navigateToPassword() {
      uni.navigateTo({
        url: '/pages/profile/password'
      })
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除本地存储
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.removeStorageSync('menus')
            
            uni.showToast({
              title: '已退出登录',
              icon: 'success'
            })
            
            // 跳转到登录页
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages/login/login'
              })
            }, 1500)
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.content-scroll {
  height: calc(100vh - 100rpx);
  padding-bottom: 120rpx;
}

.profile-content {
  padding: 32rpx;
  padding-bottom: 120rpx;
}

.profile-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 32rpx;
  padding: 64rpx 32rpx;
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  gap: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  color: white;
}

.user-avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.user-name {
  font-size: 40rpx;
  font-weight: 600;
  color: white;
}

.user-role {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.menu-section {
  background: #ffffff;
  border-radius: 32rpx;
  margin-bottom: 32rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  border-bottom: 1rpx solid #f5f5f7;
  cursor: pointer;
  transition: all 0.3s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #f9f9f9;
}

.menu-item text {
  flex: 1;
  font-size: 30rpx;
  color: #1d1d1f;
}

.arrow-icon {
  flex-shrink: 0;
}

.icon-svg {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-svg svg {
  width: 100%;
  height: 100%;
}

.logout-section {
  padding: 32rpx 0;
}

.logout-button {
  width: 100%;
  height: 96rpx;
  background: #ff3b30;
  color: #ffffff;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-button:active {
  opacity: 0.8;
}
</style>
