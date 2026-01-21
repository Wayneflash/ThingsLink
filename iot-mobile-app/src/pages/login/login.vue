<template>
  <view class="login-page">
    <view class="settings-button" @click="goToSettings">
      <Icon name="Setting" :size="24" color="#86868b" />
    </view>
    <view class="login-content">
      <view class="logo-section">
        <view class="logo-icon">
          <Icon name="Connection" :size="56" color="#ffffff" />
        </view>
        <text class="logo-title">ThingsLink</text>
        <text class="logo-subtitle">智慧物联网平台</text>
      </view>
      
      <view class="login-form">
        <view class="input-group">
          <view class="input-icon">
            <Icon name="User" :size="20" color="#667eea" />
          </view>
          <input 
            :value="loginForm.username"
            @input="(e) => { loginForm.username = e.detail.value }"
            placeholder="请输入用户名" 
            class="form-input"
            placeholder-style="color: #86868b"
            autocomplete="off"
            :auto-focus="false"
          />
        </view>
        <view class="input-group">
          <view class="input-icon">
            <Icon name="Lock" :size="20" color="#667eea" />
          </view>
          <input 
            :value="loginForm.password"
            @input="(e) => { loginForm.password = e.detail.value }"
            type="password"
            placeholder="请输入密码" 
            class="form-input"
            placeholder-style="color: #86868b"
            autocomplete="new-password"
            :auto-focus="false"
          />
        </view>
        
        <button class="login-button" @click="handleLogin" :disabled="loading">
          <text>{{ loading ? '登录中...' : '登 录' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { login } from '@/api/auth'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    Icon
  },
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  onLoad() {
    // 立即强制清空
    this.loginForm.username = ''
    this.loginForm.password = ''
    this.$nextTick(() => {
      this.clearForm()
    })
  },
  onShow() {
    // 每次显示页面时都强制清空
    this.loginForm.username = ''
    this.loginForm.password = ''
    this.$nextTick(() => {
      this.clearForm()
    })
  },
  onReady() {
    // 页面渲染完成后多次强制清空，确保覆盖自动填充
    this.clearForm()
    setTimeout(() => {
      this.clearForm()
    }, 50)
    setTimeout(() => {
      this.clearForm()
    }, 150)
    setTimeout(() => {
      this.clearForm()
    }, 300)
    setTimeout(() => {
      this.clearForm()
    }, 500)
  },
  mounted() {
    // Vue 生命周期也清空
    this.clearForm()
  },
  methods: {
    clearForm() {
      // 强制设置为空字符串
      this.loginForm.username = ''
      this.loginForm.password = ''
      // 强制更新视图
      this.$nextTick(() => {
        this.$forceUpdate()
        // #ifdef H5
        // 直接操作 DOM 清空（H5 环境）
        try {
          const inputs = document.querySelectorAll('.form-input')
          inputs.forEach(input => {
            if (input) {
              input.value = ''
              input.setAttribute('value', '')
              // 触发 input 事件确保 Vue 响应
              const event = new Event('input', { bubbles: true })
              input.dispatchEvent(event)
            }
          })
        } catch (e) {
          console.log('DOM操作失败:', e)
        }
        // #endif
      })
    },
    goToSettings() {
      uni.navigateTo({
        url: '/pages/login/settings'
      })
    },
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        uni.showToast({
          title: '请输入用户名和密码',
          icon: 'none'
        })
        return
      }
      
      this.loading = true
      try {
        const res = await login({
          username: this.loginForm.username,
          password: this.loginForm.password
        })
        
        // 保存 token 和用户信息（同步操作，立即完成）
        uni.setStorageSync('token', res.token)
        uni.setStorageSync('userInfo', res.userInfo)
        if (res.menus) {
          uni.setStorageSync('menus', res.menus)
        }
        
        // 立即跳转，不等待任何回调（同步操作已完成后直接跳转）
        uni.reLaunch({
          url: '/pages/overview/overview'
        })
        
        // 显示成功消息（异步，不阻塞跳转）
        setTimeout(() => {
          uni.showToast({
            title: '登录成功',
            icon: 'success',
            duration: 1500
          })
        }, 100)
      } catch (error) {
        console.error('登录失败:', error)
        // 确保错误信息是字符串
        let errorMessage = '登录失败'
        if (error && typeof error === 'object') {
          errorMessage = error.message || error.errMsg || error.msg || '登录失败'
        } else if (error && typeof error === 'string') {
          errorMessage = error
        }
        uni.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  position: relative;
}

.settings-button {
  position: fixed;
  top: 48rpx;
  right: 48rpx;
  width: 80rpx;
  height: 80rpx;
  background: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  z-index: 10;
  cursor: pointer;
  transition: all 0.3s ease;
}

.settings-button:active {
  transform: scale(0.95);
  background: #f5f5f7;
}

.login-content {
  padding: 80rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  justify-content: center;
}

.logo-section {
  text-align: center;
  margin-bottom: 80rpx;
}

.logo-icon {
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.3);
}

.logo-title {
  display: block;
  font-size: 56rpx;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16rpx;
}

.logo-subtitle {
  display: block;
  font-size: 28rpx;
  color: #86868b;
}

.login-form {
  width: 100%;
  max-width: 800rpx;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.input-icon {
  color: #667eea;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 30rpx;
  color: #1d1d1f;
  background: transparent;
}

.login-button {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 24rpx;
  color: white;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  margin-bottom: 32rpx;
}

.login-button:active {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(102, 126, 234, 0.4);
}

.login-button[disabled] {
  opacity: 0.6;
}
</style>
