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

        <!-- RELAY 远程开关控制 -->
        <template v-if="isRelayProduct">
          <view class="relay-control-header">
            <text class="relay-title">远程开关</text>
            <view class="relay-header-actions">
              <view v-if="deviceStatus !== 1" class="offline-chip">离线</view>
              <view 
                class="refresh-btn" 
                :class="{ loading: relayRefreshLoading }"
                @click="handleRelayRefresh"
              >
                <Icon name="Refresh" :size="28" :color="relayRefreshLoading ? '#86868b' : '#667eea'" />
                <text>刷新</text>
              </view>
            </view>
          </view>

          <view class="relay-switch-panel">
            <view 
              class="relay-card"
              v-for="i in 4" 
              :key="'switch' + i"
            >
              <view class="relay-card-main">
                <text class="relay-card-label">开关 {{ i }}</text>
                <view class="relay-card-status">
                  <text 
                    class="status-badge" 
                    :class="getRelaySwitchValue('switch' + i) === '1' ? 'status-on' : 'status-off'"
                  >
                    {{ getRelaySwitchValue('switch' + i) === '1' ? '开' : '关' }}
                  </text>
                </view>
              </view>
              <view class="relay-card-switch">
                <view 
                  class="custom-switch"
                  :class="{ 
                    'custom-switch-on': getRelaySwitchValue('switch' + i) === '1',
                    'custom-switch-disabled': relaySwitchLoading['switch' + i] || deviceStatus !== 1
                  }"
                  @tap="toggleSwitch(i)"
                >
                  <view class="custom-switch-thumb"></view>
                </view>
                <text v-if="relaySwitchLoading['switch' + i]" class="loading-hint">执行中</text>
              </view>
            </view>
          </view>
        </template>

        <!-- 普通命令列表 -->
        <template v-else>
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
        </template>

        <view class="command-tip">
          <Icon name="InfoFilled" :size="16" color="#86868b" />
          <text>设备在线时才能执行控制命令</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getProductCommands, sendCommand, refreshRelayStatus } from '@/api/command'
import { getDeviceDetail, getDeviceLatestData } from '@/api/device'
import { getProductAttributes } from '@/api/product'
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
      protocol: '',
      commandList: [],
      // RELAY 远程开关
      productAttributes: [],
      relayDeviceData: {},
      relaySwitchLoading: {},
      relayRefreshLoading: false
    }
  },
  computed: {
    isRelayProduct() {
      return (this.protocol || '').toUpperCase() === 'RELAY'
    }
  },
  onLoad(options) {
    if (options.code) {
      this.deviceCode = options.code
    }
    if (options.name) {
      this.deviceName = decodeURIComponent(options.name || '')
    }
    if (options.protocol) {
      this.protocol = decodeURIComponent(options.protocol || '')
    }
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
          if (!this.protocol && res.protocol) {
            this.protocol = res.protocol
          }
          if (this.isRelayProduct) {
            await this.loadRelayData()
          } else {
            await this.loadCommands()
          }
        }
      } catch (error) {
        console.error('加载设备信息失败:', error)
        this._showError(error, '加载失败')
      }
    },
    async loadRelayData() {
      try {
        if (!this.productId) return
        const attrs = await getProductAttributes(this.productId)
        this.productAttributes = attrs || []
        try {
          await refreshRelayStatus({ deviceCode: this.deviceCode })
        } catch (e) {
          console.warn('RELAY 状态刷新失败:', e)
        }
        await new Promise(r => setTimeout(r, 800))
        await this.loadRelayLatestData()
      } catch (error) {
        console.error('加载 RELAY 数据失败:', error)
        this._showError(error, '加载失败')
      }
    },
    async loadRelayLatestData() {
      try {
        const data = await getDeviceLatestData({ deviceCode: this.deviceCode })
        if (data && data.data) {
          this.relayDeviceData = data.data
        }
      } catch (error) {
        console.error('加载实时数据失败:', error)
      }
    },
    getRelaySwitchValue(addr) {
      const v = this.relayDeviceData[addr]
      if (v === undefined || v === null || v === '-') return '0'
      return String(v) === '1' ? '1' : '0'
    },
    toggleSwitch(index) {
      if (this.relaySwitchLoading['switch' + index]) return
      if (this.deviceStatus !== 1) {
        uni.showToast({ title: '设备离线，无法下发命令', icon: 'none' })
        return
      }
      const addr = 'switch' + index
      const current = this.getRelaySwitchValue(addr)
      const newVal = current === '1' ? false : true
      this.handleRelaySwitchChange(addr, newVal)
    },
    async handleRelaySwitchChange(addr, checked) {
      if (this.relaySwitchLoading[addr]) return
      const targetValue = checked ? '1' : '0'
      if (this.deviceStatus !== 1) {
        uni.showToast({ title: '设备离线，无法下发命令', icon: 'none' })
        return
      }
      this.$set(this.relaySwitchLoading, addr, true)
      try {
        await sendCommand({
          deviceCode: this.deviceCode,
          commands: [{ addr, addrv: targetValue }]
        })
        const start = Date.now()
        const timeout = 5000
        const pollInterval = 250
        const checkDone = () => this.getRelaySwitchValue(addr) === targetValue
        await this.loadRelayLatestData()
        if (checkDone()) {
          uni.showToast({ title: '命令已生效', icon: 'success' })
          return
        }
        do {
          await new Promise(r => setTimeout(r, pollInterval))
          await this.loadRelayLatestData()
          if (checkDone()) {
            uni.showToast({ title: '命令已生效', icon: 'success' })
            return
          }
        } while (Date.now() - start < timeout)
        uni.showToast({ title: '设备无响应', icon: 'none' })
      } catch (error) {
        console.error('命令下发失败:', error)
        this._showError(error, '命令下发失败')
      } finally {
        this.$set(this.relaySwitchLoading, addr, false)
      }
    },
    async handleRelayRefresh() {
      this.relayRefreshLoading = true
      try {
        await refreshRelayStatus({ deviceCode: this.deviceCode })
        await new Promise(r => setTimeout(r, 800))
        await this.loadRelayLatestData()
        uni.showToast({ title: '状态已刷新', icon: 'success' })
      } catch (error) {
        console.error('刷新失败:', error)
        this._showError(error, '刷新失败')
      } finally {
        this.relayRefreshLoading = false
      }
    },
    async loadCommands() {
      try {
        if (!this.productId) return
        const cmdData = await getProductCommands(this.productId)
        let commandArray = []
        if (Array.isArray(cmdData)) {
          commandArray = cmdData
        } else if (cmdData && cmdData.list) {
          commandArray = cmdData.list
        }
        this.commandList = commandArray.map(cmd => ({
          id: cmd.id,
          name: cmd.commandName || cmd.name,
          description: cmd.description || cmd.desc || '',
          addr: cmd.addr || cmd.identifier || cmd.commandIdentifier,
          commandValue: cmd.commandValue || cmd.defaultValue || '',
          iconColor: this.getCommandIconColor(cmd.commandName || cmd.name)
        }))
      } catch (error) {
        console.error('加载产品命令失败:', error)
        this._showError(error, '加载失败')
        this.commandList = []
      }
    },
    async executeCommand(command) {
      if (this.deviceStatus !== 1) {
        uni.showToast({ title: '设备离线，无法执行命令', icon: 'none' })
        return
      }
      uni.showModal({
        title: '确认执行',
        content: `是否确认执行 ${command.name} 指令？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await sendCommand({
                deviceCode: this.deviceCode,
                commands: [{
                  addr: command.addr || command.identifier || command.name,
                  addrv: command.commandValue || command.value || ''
                }]
              })
              uni.showToast({ title: '命令已下发', icon: 'success' })
            } catch (error) {
              this._showError(error, '发送命令失败')
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
    _showError(error, defaultMsg) {
      const msg = error && typeof error === 'object' ? (error.message || error.errMsg || defaultMsg) : String(error || defaultMsg)
      uni.showToast({ title: msg, icon: 'none', duration: 2000 })
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

/* RELAY 远程开关 */
.relay-control-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding: 0 4rpx;
}

.relay-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.relay-header-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.offline-chip {
  padding: 6rpx 16rpx;
  background: rgba(255, 149, 0, 0.15);
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #ff9500;
  font-weight: 500;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  font-size: 26rpx;
  color: #667eea;
  font-weight: 500;
}

.refresh-btn.loading {
  opacity: 0.6;
}

.relay-switch-panel {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.relay-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
}

.relay-card:active {
  background: #fafafa;
}

.relay-card-main {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  flex: 1;
  min-height: 88rpx;
  justify-content: center;
}

.relay-card-label {
  font-size: 30rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.relay-card-status {
  display: flex;
  align-items: center;
}

.status-badge {
  font-size: 26rpx;
  font-weight: 500;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

.status-on {
  background: rgba(52, 199, 89, 0.12);
  color: #34c759;
}

.status-off {
  background: rgba(134, 134, 139, 0.1);
  color: #86868b;
}

.relay-card-switch {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

/* 自定义开关：纯 view 实现，真机点击可靠 */
.custom-switch {
  width: 100rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: #e5e7eb;
  position: relative;
  transition: background 0.25s ease;
}

.custom-switch-thumb {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  transition: transform 0.25s ease;
}

.custom-switch-on {
  background: #34c759;
}

.custom-switch-on .custom-switch-thumb {
  transform: translateX(44rpx);
}

.custom-switch-disabled {
  opacity: 0.5;
}

.loading-hint {
  font-size: 22rpx;
  color: #86868b;
}

/* 普通命令列表 */
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
}

.command-action-text {
  color: #ffffff;
}

.command-card-v2[data-disabled="true"] .command-action {
  opacity: 0.5;
}

.empty-commands {
  text-align: center;
  padding: 128rpx 32rpx;
}

.empty-commands .empty-text {
  font-size: 28rpx;
  color: #86868b;
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
