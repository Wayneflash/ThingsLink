<template>
  <view class="password-change-page">
    <view class="mobile-header-bar">
      <view class="back-icon" @click="goBack">
        <Icon name="ArrowLeft" :size="24" />
      </view>
      <text class="header-title">修改密码</text>
      <view class="header-right"></view>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="password-change-content">
        <!-- 表单卡片 -->
        <view class="form-card">
          <!-- 新密码 -->
          <view class="form-item">
            <text class="form-label">新密码</text>
            <view class="input-wrapper">
              <input 
                class="form-input" 
                v-model="form.newPassword"
                :type="showNewPassword ? 'text' : 'password'"
                placeholder="请输入新密码（至少6位）"
                maxlength="50"
              />
              <view class="password-toggle" @click="showNewPassword = !showNewPassword">
                <Icon :name="showNewPassword ? 'Eye' : 'EyeOff'" :size="20" color="#86868b" />
              </view>
            </view>
            <text class="form-tip">密码长度至少6位，建议使用字母、数字和特殊字符组合</text>
          </view>

          <!-- 确认新密码 -->
          <view class="form-item">
            <text class="form-label">确认新密码</text>
            <view class="input-wrapper">
              <input 
                class="form-input" 
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="请再次输入新密码"
                maxlength="50"
              />
              <view class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
                <Icon :name="showConfirmPassword ? 'Eye' : 'EyeOff'" :size="20" color="#86868b" />
              </view>
            </view>
            <text class="form-tip">请再次输入新密码以确认</text>
          </view>
        </view>

        <!-- 安全提示 -->
        <view class="security-tip-card">
          <view class="tip-header">
            <Icon name="WarningFilled" :size="20" color="#ff9500" />
            <text class="tip-title">安全提示</text>
          </view>
          <view class="tip-content">
            <text class="tip-item">1. 密码修改成功后，请使用新密码重新登录</text>
            <text class="tip-item">2. 建议定期更换密码，提高账户安全性</text>
            <text class="tip-item">3. 不要使用过于简单的密码（如123456、password等）</text>
            <text class="tip-item">4. 密码修改后，请妥善保管，避免泄露</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
          <button class="save-btn" @click="handleSave" :disabled="saving">
            <text>{{ saving ? '保存中...' : '保存修改' }}</text>
          </button>
          <button class="reset-btn" @click="handleReset" :disabled="saving">
            <text>重置</text>
          </button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { changePassword } from '@/api/auth'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    Icon
  },
  data() {
    return {
      form: {
        newPassword: '',
        confirmPassword: ''
      },
      showNewPassword: false,
      showConfirmPassword: false,
      saving: false
    }
  },
  methods: {
    validateForm() {
      // 验证新密码
      if (!this.form.newPassword || this.form.newPassword.trim() === '') {
        uni.showToast({
          title: '请输入新密码',
          icon: 'none'
        })
        return false
      }
      if (this.form.newPassword.length < 6) {
        uni.showToast({
          title: '密码长度至少6位',
          icon: 'none'
        })
        return false
      }

      // 验证确认密码
      if (!this.form.confirmPassword || this.form.confirmPassword.trim() === '') {
        uni.showToast({
          title: '请确认新密码',
          icon: 'none'
        })
        return false
      }
      if (this.form.confirmPassword !== this.form.newPassword) {
        uni.showToast({
          title: '两次输入的密码不一致',
          icon: 'none'
        })
        return false
      }

      return true
    },
    async handleSave() {
      if (!this.validateForm()) {
        return
      }

      this.saving = true
      try {
        await changePassword({
          newPassword: this.form.newPassword
        })
        
        uni.showModal({
          title: '提示',
          content: '密码修改成功！为了安全起见，建议您重新登录。是否立即退出登录？',
          confirmText: '退出登录',
          cancelText: '稍后退出',
          success: (res) => {
            if (res.confirm) {
              // 清除登录信息
              uni.removeStorageSync('token')
              uni.removeStorageSync('userInfo')
              uni.removeStorageSync('menus')
              
              // 跳转到登录页
              uni.reLaunch({
                url: '/pages/login/login'
              })
            } else {
              // 用户选择稍后退出，重置表单
              this.handleReset()
            }
          }
        })
      } catch (error) {
        console.error('修改密码失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '修改失败') 
          : String(error || '修改失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.saving = false
      }
    },
    handleReset() {
      this.form.newPassword = ''
      this.form.confirmPassword = ''
      this.showNewPassword = false
      this.showConfirmPassword = false
      uni.showToast({
        title: '已重置',
        icon: 'success',
        duration: 1500
      })
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.password-change-page {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.mobile-header-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.back-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #1d1d1f;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
  flex: 1;
  text-align: center;
}

.header-right {
  width: 48rpx;
}

.content-scroll {
  height: calc(100vh - 88rpx);
}

.password-change-content {
  padding: 32rpx;
}

.form-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.form-item {
  margin-bottom: 40rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #86868b;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  flex: 1;
  height: 88rpx;
  padding: 0 80rpx 0 24rpx;
  background: #f5f5f7;
  border: 1rpx solid #e5e7eb;
  border-radius: 16rpx;
  font-size: 30rpx;
  color: #1d1d1f;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.form-input:focus {
  border-color: #667eea;
  background: #ffffff;
}

.password-toggle {
  position: absolute;
  right: 24rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #86868b;
}

.form-tip {
  display: block;
  font-size: 24rpx;
  color: #86868b;
  margin-top: 12rpx;
  line-height: 1.5;
}

.security-tip-card {
  background: rgba(255, 149, 0, 0.08);
  border: 1rpx solid rgba(255, 149, 0, 0.2);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
}

.tip-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.tip-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #ff9500;
}

.tip-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.tip-item {
  font-size: 26rpx;
  color: #6e6e73;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 24rpx;
}

.save-btn {
  flex: 1;
  height: 96rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-btn:active:not([disabled]) {
  opacity: 0.9;
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.25);
}

.save-btn[disabled] {
  opacity: 0.5;
  transform: none;
}

.reset-btn {
  flex: 1;
  height: 96rpx;
  background: #ffffff;
  color: #1d1d1f;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: 1rpx solid #e5e7eb;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reset-btn:active:not([disabled]) {
  background: #f5f5f7;
  transform: translateY(2rpx);
}

.reset-btn[disabled] {
  opacity: 0.5;
  transform: none;
}
</style>
