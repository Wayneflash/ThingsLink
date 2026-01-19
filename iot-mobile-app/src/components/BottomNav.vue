<template>
  <view class="bottom-nav">
    <view 
      class="nav-item" 
      :class="{ active: currentPath === '/pages/overview/overview' }"
      @click="navigateTo('/pages/overview/overview')"
    >
      <view class="icon-svg" v-html="getHomeIcon()"></view>
      <text>概览</text>
    </view>
    <view 
      class="nav-item" 
      :class="{ active: currentPath === '/pages/device/list' }"
      @click="navigateTo('/pages/device/list')"
    >
      <view class="icon-svg" v-html="getDeviceIcon()"></view>
      <text>设备</text>
    </view>
    <view 
      class="nav-item" 
      :class="{ active: currentPath === '/pages/alarm/alarm' }"
      @click="navigateTo('/pages/alarm/alarm')"
    >
      <view class="icon-svg" v-html="getAlarmIcon()"></view>
      <text>报警</text>
    </view>
    <view 
      class="nav-item" 
      :class="{ active: currentPath === '/pages/profile/profile' }"
      @click="navigateTo('/pages/profile/profile')"
    >
      <view class="icon-svg" v-html="getProfileIcon()"></view>
      <text>我的</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'BottomNav',
  data() {
    return {
      currentPath: ''
    }
  },
  onLoad() {
    this.updateCurrentPath()
  },
  onShow() {
    this.updateCurrentPath()
  },
  watch: {
    '$route'() {
      this.updateCurrentPath()
    }
  },
  methods: {
    updateCurrentPath() {
      const pages = getCurrentPages()
      if (pages.length > 0) {
        const currentPage = pages[pages.length - 1]
        this.currentPath = '/' + currentPage.route
      }
    },
    navigateTo(path) {
      if (this.currentPath !== path) {
        uni.reLaunch({
          url: path
        })
      }
    },
    getHomeIcon() {
      const isActive = this.currentPath === '/pages/overview/overview'
      return `<svg width="24" height="24" viewBox="0 0 24 24" fill="${isActive ? '#667eea' : '#86868b'}" stroke="${isActive ? '#667eea' : '#86868b'}" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>`
    },
    getDeviceIcon() {
      const isActive = this.currentPath === '/pages/device/list'
      return `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="${isActive ? '#667eea' : '#86868b'}" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`
    },
    getAlarmIcon() {
      const isActive = this.currentPath === '/pages/alarm/alarm'
      return `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="${isActive ? '#667eea' : '#86868b'}" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>`
    },
    getProfileIcon() {
      const isActive = this.currentPath === '/pages/profile/profile'
      return `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="${isActive ? '#667eea' : '#86868b'}" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>`
    }
  }
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 8px 0;
  z-index: 999;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-item text {
  font-size: 24rpx;
  color: #86868b;
  transition: color 0.3s ease;
}

.nav-item.active text {
  color: #667eea;
  font-weight: 500;
}

.icon-svg {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg svg {
  width: 100%;
  height: 100%;
}
</style>
