<template>
  <view class="device-command-page">
    <view class="mobile-header-bar">
      <view class="back-icon" @click="goBack">
        <Icon name="ArrowLeft" :size="24" />
      </view>
      <text class="header-title">命令控制</text>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="mobile-content-area">
        <view class="device-info-bar">
          <text class="device-name-bar">{{ deviceName }}</text>
          <view class="device-tag" :class="deviceStatus === 1 ? 'tag-success' : 'tag-warning'">
            <text>{{ deviceStatus === 1 ? '在线' : '离线' }}</text>
          </view>
        </view>

        <!-- 命令列表 -->
        <view class="command-list-v2">
          <view 
            class="command-card-v2" 
            v-for="command in commandList" 
            :key="command.id"
            :data-disabled="deviceStatus !== 1"
            @click="executeCommand(command)"
          >
            <view class="command-icon-v2">
              <Icon name="CircleCheck" :size="32" color="#667eea" />
            </view>
            <view class="command-content-v2">
              <text class="command-name-v2">{{ command.name }}</text>
              <text class="command-desc-v2" v-if="command.description">{{ command.description }}</text>
            </view>
            <view class="command-action">
              <text class="command-action-text">执行</text>
            </view>
          </view>
          
          <view v-if="commandList.length === 0" class="empty-commands">
            <text class="empty-text">暂无可用命令</text>
          </view>
        </view>

        <view class="command-tip">
          <Icon name="InfoFilled" :size="16" color="#86868b" />
          <text>设备在线时才能执行控制命令</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getProductCommands, sendCommand } from '@/api/command'
import { getDeviceDetail } from '@/api/device'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    Icon
  },
  data() {
    return {
      deviceCode: '',
      deviceName: '',
      deviceStatus: 0,
      productId: null,
      commandList: []
    }
  },
  onLoad(options) {
    if (options.code) {
      this.deviceCode = options.code
    }
    if (options.name) {
      this.deviceName = decodeURIComponent(options.name)
    }
    // 先加载设备信息，然后再加载命令（设备信息中包含productId）
    this.loadDeviceInfo()
  },
  methods: {
    async loadDeviceInfo() {
      try {
        const res = await getDeviceDetail({
          deviceCode: this.deviceCode
        })
        if (res) {
          this.deviceStatus = res.status
          this.productId = res.productId
          if (!this.deviceName && res.name) {
            this.deviceName = res.name || res.deviceName
          }
          // 设备信息加载完成后，再加载命令
          await this.loadCommands()
        }
      } catch (error) {
        console.error('加载设备信息失败:', error)
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
    async loadCommands() {
      try {
        // 先确保设备信息已加载
        if (!this.productId) {
          await this.loadDeviceInfo()
        }
        
        if (!this.productId) {
          console.warn('产品ID不存在，无法加载命令')
          this.commandList = []
          return
        }
        
        const cmdData = await getProductCommands(this.productId)
        if (cmdData) {
          // 检查返回的数据结构，兼容直接返回数组或包含list的对象（与web端一致）
          let commandArray = []
          if (Array.isArray(cmdData)) {
            commandArray = cmdData
          } else if (cmdData.list && Array.isArray(cmdData.list)) {
            commandArray = cmdData.list
          }
          
          if (commandArray.length > 0) {
            this.commandList = commandArray.map(cmd => ({
              id: cmd.id,
              name: cmd.commandName || cmd.name,
              description: cmd.description || cmd.desc || '',
              addr: cmd.addr || cmd.identifier || cmd.commandIdentifier,
              commandValue: cmd.commandValue || cmd.defaultValue || '',
              iconColor: this.getCommandIconColor(cmd.commandName || cmd.name)
            }))
          } else {
            // 如果后端没有返回命令，显示空列表
            this.commandList = []
          }
        } else {
          this.commandList = []
        }
      } catch (error) {
        console.error('加载产品命令失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '加载失败') 
          : String(error || '加载失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
        this.commandList = []
      }
    },
    async executeCommand(command) {
      if (this.deviceStatus !== 1) {
        uni.showToast({
          title: '设备离线，无法执行命令',
          icon: 'none',
          duration: 2000
        })
        return
      }
      
      uni.showModal({
        title: '确认执行',
        content: `是否确认执行 ${command.name} 指令？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              // 与web端一致：使用 addr 和 addrv（commandValue）
              const data = await sendCommand({
                deviceCode: this.deviceCode,
                commands: [{
                  addr: command.addr || command.identifier || command.name,
                  addrv: command.commandValue || command.value || ''
                }]
              })
              
              if (data) {
                uni.showToast({
                  title: '命令已下发',
                  icon: 'success',
                  duration: 2000
                })
              }
            } catch (error) {
              console.error('发送命令失败:', error)
              const errorMsg = error && typeof error === 'object' 
                ? (error.message || error.errMsg || '发送命令失败') 
                : String(error || '发送命令失败')
              uni.showToast({
                title: errorMsg,
                icon: 'none',
                duration: 2000
              })
            }
          }
        }
      })
    },
    getCommandIconColor(commandName) {
      if (!commandName) return '#667eea'
      if (commandName.includes('开启')) return '#667eea'
      if (commandName.includes('关闭')) return '#11998e'
      if (commandName.includes('重启')) return '#f56c6c'
      return '#ff9500'
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.device-command-page {
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
  gap: 24rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.back-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
  cursor: pointer;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
  flex: 1;
}

.content-scroll {
  height: calc(100vh - 88rpx);
}

.mobile-content-area {
  padding: 32rpx;
}

.device-info-bar {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.device-name-bar {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.device-tag {
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.tag-success {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.tag-warning {
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.command-list-v2 {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.command-card-v2 {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.command-card-v2:active {
  transform: translateY(-4rpx);
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.12);
}

.command-icon-v2 {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  background: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.command-content-v2 {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.command-name-v2 {
  font-size: 32rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.command-desc-v2 {
  font-size: 26rpx;
  color: #86868b;
}

.command-action {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
  transition: opacity 0.3s ease;
}

.command-card-v2:active .command-action {
  opacity: 0.8;
}

.command-card-v2[data-disabled="true"] .command-action {
  opacity: 0.5;
}

.command-action-text {
  color: #ffffff;
}

.empty-commands {
  text-align: center;
  padding: 128rpx 32rpx;
}

.empty-commands .empty-text {
  font-size: 28rpx;
  color: #86868b;
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

.command-tip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx;
  background: #f5f5f7;
  border-radius: 24rpx;
  font-size: 24rpx;
  color: #86868b;
  text-align: center;
  justify-content: center;
}
</style>
