<template>
  <view class="profile-edit-page">
    <view class="mobile-header-bar">
      <view class="back-icon" @click="goBack">
        <Icon name="ArrowLeft" :size="24" />
      </view>
      <text class="header-title">个人资料</text>
      <view class="header-right"></view>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="profile-edit-content">
        <!-- 表单卡片 -->
        <view class="form-card">
          <!-- 用户名（只读） -->
          <view class="form-item">
            <text class="form-label">用户名</text>
            <view class="form-value readonly">
              <text>{{ form.username || '-' }}</text>
            </view>
            <text class="form-tip">用户名不可修改</text>
          </view>

          <!-- 姓名 -->
          <view class="form-item">
            <text class="form-label">姓名</text>
            <input 
              class="form-input" 
              v-model="form.realName"
              placeholder="请输入姓名"
              maxlength="50"
            />
          </view>

          <!-- 手机号 -->
          <view class="form-item">
            <text class="form-label">手机号</text>
            <input 
              class="form-input" 
              v-model="form.phone"
              type="number"
              placeholder="请输入手机号"
              maxlength="11"
            />
            <text class="form-tip">用于接收短信通知</text>
          </view>

          <!-- 邮箱 -->
          <view class="form-item">
            <text class="form-label">邮箱</text>
            <input 
              class="form-input" 
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱地址"
              maxlength="100"
            />
            <text class="form-tip">用于接收邮件通知</text>
          </view>

          <!-- 所属分组（只读） -->
          <view class="form-item">
            <text class="form-label">所属分组</text>
            <view class="form-value readonly">
              <text>{{ form.groupName || '-' }}</text>
            </view>
          </view>

          <!-- 角色（只读） -->
          <view class="form-item">
            <text class="form-label">角色</text>
            <view class="form-value readonly">
              <text>{{ form.roleName || '-' }}</text>
            </view>
          </view>

          <!-- 状态（只读） -->
          <view class="form-item">
            <text class="form-label">状态</text>
            <view class="form-value readonly">
              <view class="status-badge" :class="form.status === 1 ? 'status-active' : 'status-inactive'">
                <text>{{ form.status === 1 ? '启用' : '禁用' }}</text>
              </view>
            </view>
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
import { getProfile, updateProfile } from '@/api/profile'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    Icon
  },
  data() {
    return {
      form: {
        id: null,
        username: '',
        realName: '',
        phone: '',
        email: '',
        groupName: '',
        roleName: '',
        status: 1
      },
      originalForm: {},
      saving: false
    }
  },
  onLoad() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const res = await getProfile()
        if (res) {
          Object.assign(this.form, res)
          // 备份原始数据
          this.originalForm = JSON.parse(JSON.stringify(res))
        }
      } catch (error) {
        console.error('加载个人资料失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '加载失败') 
          : String(error || '加载失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      }
    },
    validateForm() {
      // 验证姓名
      if (!this.form.realName || this.form.realName.trim() === '') {
        uni.showToast({
          title: '请输入姓名',
          icon: 'none'
        })
        return false
      }
      if (this.form.realName.length < 2 || this.form.realName.length > 50) {
        uni.showToast({
          title: '姓名长度在 2 到 50 个字符',
          icon: 'none'
        })
        return false
      }

      // 验证手机号（如果填写）
      if (this.form.phone && this.form.phone.trim()) {
        const phoneReg = /^1[3-9]\d{9}$/
        if (!phoneReg.test(this.form.phone)) {
          uni.showToast({
            title: '请输入正确的手机号码',
            icon: 'none'
          })
          return false
        }
      }

      // 验证邮箱（如果填写）
      if (this.form.email && this.form.email.trim()) {
        const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
        if (!emailReg.test(this.form.email)) {
          uni.showToast({
            title: '请输入正确的邮箱地址',
            icon: 'none'
          })
          return false
        }
      }

      return true
    },
    async handleSave() {
      if (!this.validateForm()) {
        return
      }

      this.saving = true
      try {
        const data = {
          realName: this.form.realName,
          phone: this.form.phone || '',
          email: this.form.email || ''
        }
        
        await updateProfile(data)
        
        uni.showToast({
          title: '保存成功',
          icon: 'success',
          duration: 2000
        })

        // 更新本地存储的用户信息
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          Object.assign(userInfo, data)
          uni.setStorageSync('userInfo', userInfo)
        }

        // 重新加载数据
        await this.loadProfile()
      } catch (error) {
        console.error('保存失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '保存失败') 
          : String(error || '保存失败')
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
      Object.assign(this.form, this.originalForm)
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
.profile-edit-page {
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

.profile-edit-content {
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

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
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

.form-value {
  min-height: 88rpx;
  padding: 0 24rpx;
  background: #f5f5f7;
  border: 1rpx solid #e5e7eb;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  font-size: 30rpx;
  color: #1d1d1f;
}

.form-value.readonly {
  background: #f9f9f9;
  color: #86868b;
}

.form-tip {
  display: block;
  font-size: 24rpx;
  color: #86868b;
  margin-top: 12rpx;
  line-height: 1.5;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.status-badge.status-active {
  background: rgba(52, 199, 89, 0.12);
  color: #34c759;
}

.status-badge.status-inactive {
  background: rgba(142, 142, 147, 0.12);
  color: #8e8e93;
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
