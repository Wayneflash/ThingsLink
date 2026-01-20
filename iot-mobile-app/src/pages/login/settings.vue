<template>
  <view class="settings-page">
    <view class="mobile-header-bar">
      <view class="back-icon" @click="goBack">
        <Icon name="ArrowLeft" :size="24" />
      </view>
      <text class="header-title">系统设置</text>
      <view style="width: 48rpx;"></view>
    </view>
    
    <view class="content-scroll">
      <view class="mobile-content-area">
        <view class="settings-card">
          <view class="settings-header">
            <Icon name="Setting" :size="24" color="#667eea" />
            <text class="settings-title">服务器配置</text>
          </view>
          
          <view class="form-item-simple">
            <text class="form-label">服务器地址</text>
            <view class="input-box">
              <input 
                v-model="serverConfig.host"
                placeholder="请输入服务器地址" 
                class="input-text"
              />
            </view>
          </view>

          <view class="form-item-simple">
            <text class="form-label">端口号</text>
            <view class="input-box">
              <input 
                v-model="serverConfig.port"
                placeholder="请输入端口号" 
                type="number"
                class="input-text"
              />
            </view>
          </view>

          <view class="form-group">
            <text class="form-label">完整地址</text>
            <view class="form-preview">
              <text class="preview-text">{{ apiBaseURL }}</text>
            </view>
          </view>
        </view>

        <view class="button-group">
          <button class="action-button reset-button" @click="handleReset">
            <text>恢复默认</text>
          </button>
          <button class="action-button save-button" @click="handleSave" :disabled="saving">
            <text>{{ saving ? '保存中...' : '保存设置' }}</text>
          </button>
        </view>

        <view class="info-card">
          <view class="info-header">
            <Icon name="InfoFilled" :size="20" color="#667eea" />
            <text class="info-title">使用说明</text>
          </view>
          <view class="info-content">
            <text class="info-item">• 服务器地址：后端API服务器的IP地址或域名</text>
            <text class="info-item">• 端口号：后端API服务的端口，默认8080</text>
            <text class="info-item">• 修改配置后，请重新登录以应用新的服务器地址</text>
            <text class="info-item">• 保存设置后，所有API请求将使用新的服务器地址</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Icon from '@/components/Icon.vue'

export default {
  components: {
    Icon
  },
  data() {
    return {
      serverConfig: {
        host: '117.72.222.8',
        port: '8080'
      },
      saving: false
    }
  },
  computed: {
    apiBaseURL() {
      const host = this.serverConfig.host || '117.72.222.8'
      const port = this.serverConfig.port || '8080'
      return `http://${host}:${port}`
    }
  },
  onLoad() {
    this.loadConfig()
  },
  methods: {
    loadConfig() {
      // 从本地存储读取配置
      try {
        const savedConfig = uni.getStorageSync('api_server_config')
        if (savedConfig) {
          const config = typeof savedConfig === 'string' ? JSON.parse(savedConfig) : savedConfig
          this.serverConfig = {
            host: config.host || '117.72.222.8',
            port: config.port || '8080'
          }
        } else {
          // 没有保存的配置，使用默认值
          this.resetToDefault()
          // 如果main.js已经初始化了默认配置，再次读取
          const defaultConfig = uni.getStorageSync('api_server_config')
          if (defaultConfig) {
            const config = typeof defaultConfig === 'string' ? JSON.parse(defaultConfig) : defaultConfig
            this.serverConfig = {
              host: config.host || '117.72.222.8',
              port: config.port || '8080'
            }
          }
        }
      } catch (e) {
        console.error('读取配置失败:', e)
        // 使用默认配置
        this.resetToDefault()
      }
    },
    resetToDefault() {
      this.serverConfig = {
        host: '117.72.222.8',
        port: '8080'
      }
    },
    handleReset() {
      uni.showModal({
        title: '确认重置',
        content: '确定要恢复到默认配置吗？',
        success: (res) => {
          if (res.confirm) {
            // 保存重置后的默认配置
            try {
              const defaultConfig = {
                host: '117.72.222.8',
                port: '8080'
              }
              uni.setStorageSync('api_server_config', JSON.stringify(defaultConfig))
              // 更新页面显示的配置
              this.serverConfig = {
                host: defaultConfig.host,
                port: defaultConfig.port
              }
              uni.showToast({
                title: '已恢复默认配置',
                icon: 'success'
              })
            } catch (error) {
              console.error('保存默认配置失败:', error)
              uni.showToast({
                title: '恢复失败，请重试',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    handleSave() {
      // 验证输入
      if (!this.serverConfig.host || !this.serverConfig.host.trim()) {
        uni.showToast({
          title: '请输入服务器地址',
          icon: 'none'
        })
        return
      }

      if (!this.serverConfig.port || !this.serverConfig.port.trim()) {
        uni.showToast({
          title: '请输入端口号',
          icon: 'none'
        })
        return
      }

      // 验证端口号格式
      const port = parseInt(this.serverConfig.port)
      if (isNaN(port) || port < 1 || port > 65535) {
        uni.showToast({
          title: '端口号必须在1-65535之间',
          icon: 'none'
        })
        return
      }

      this.saving = true
      
      // 保存配置到本地存储
      const config = {
        host: this.serverConfig.host.trim(),
        port: this.serverConfig.port.trim()
      }
      
      uni.setStorageSync('api_server_config', JSON.stringify(config))
      
      this.saving = false
      this.goBack()
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.mobile-header-bar {
  position: sticky;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.back-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.3s ease;
  border-radius: 50%;
}

.back-icon:active {
  background-color: #f0f0f0;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.content-scroll {
  height: calc(100vh - 88rpx);
  padding-bottom: 24rpx;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.mobile-content-area {
  padding: 24rpx 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  max-width: 100%;
  box-sizing: border-box;
}

.settings-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.settings-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #e5e7eb;
}

.settings-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.form-item-simple {
  margin-bottom: 24rpx;
  padding: 0 8rpx;
}

.form-item-simple:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #1d1d1f;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.input-box {
  width: 100%;
  max-width: 100%;
  height: 76rpx;
  background: #ffffff;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.input-box:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3rpx rgba(102, 126, 234, 0.1);
}

.input-text {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1d1d1f;
  border: none;
  outline: none;
  background: transparent;
}

.form-preview {
  width: 100%;
  max-width: 100%;
  padding: 20rpx;
  background: #f5f5f7;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  box-sizing: border-box;
}

.preview-text {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
  word-break: break-all;
}

.button-group {
  display: flex;
  gap: 16rpx;
}

.action-button {
  flex: 1;
  height: 80rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reset-button {
  background: #e5e7eb;
  color: #1d1d1f;
}

.reset-button:active {
  background: #d1d1d6;
}

.save-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
}

.save-button:active:not([disabled]) {
  opacity: 0.9;
  transform: translateY(2rpx);
}

.save-button[disabled] {
  opacity: 0.5;
}

.info-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.info-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.info-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.info-item {
  font-size: 24rpx;
  color: #86868b;
  line-height: 1.5;
}
</style>
