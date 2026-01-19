<template>
  <view class="device-log-page">
    <view class="mobile-header-bar">
      <view class="back-icon" @click="goBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
      </view>
      <text class="header-title">设备日志</text>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="mobile-content-area">
        <!-- 筛选区域 -->
        <view class="filter-section-log">
          <picker 
            mode="selector" 
            :range="logTypeOptions" 
            range-key="label" 
            @change="handleLogTypeChange"
            class="filter-select-log"
          >
            <view class="picker-text">{{ selectedLogType.label || '日志类型' }}</view>
          </picker>
          <picker 
            mode="date" 
            :value="startDate"
            @change="handleStartDateChange"
            class="date-picker"
          >
            <view class="picker-text">{{ startDate || '开始日期' }}</view>
          </picker>
          <picker 
            mode="date" 
            :value="endDate"
            @change="handleEndDateChange"
            class="date-picker"
          >
            <view class="picker-text">{{ endDate || '结束日期' }}</view>
          </picker>
          <button class="query-btn-log" @click="handleQuery">查询</button>
        </view>

        <!-- 日志列表 -->
        <view class="log-list-card">
          <view 
            class="log-item" 
            v-for="log in logList" 
            :key="log.id"
          >
            <view class="log-icon-wrapper">
              <view class="log-icon-bg" :class="getLogIconClass(log.logType)">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                  <path v-if="log.logType === 'online'" d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline v-if="log.logType === 'online'" points="22 4 12 14.01 9 11.01"></polyline>
                  <path v-else-if="log.logType === 'command'" d="M12 1v6m0 6v6M5.64 5.64l4.24 4.24m4.24 4.24l4.24 4.24M1 12h6m6 0h6M5.64 18.36l4.24-4.24m4.24-4.24l4.24-4.24"></path>
                  <path v-else d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                  <line v-if="log.logType === 'offline'" x1="12" y1="9" x2="12" y2="13"></line>
                  <line v-if="log.logType === 'offline'" x1="12" y1="17" x2="12.01" y2="17"></line>
                </svg>
              </view>
            </view>
            <view class="log-content">
              <view class="log-header">
                <view class="log-type" :class="log.logType">
                  <text>{{ getLogTypeText(log.logType) }}</text>
                </view>
                <text class="log-time">{{ formatTime(log.createTime || log.logTime) }}</text>
              </view>
              <text class="log-detail">{{ log.content || log.message || '-' }}</text>
            </view>
          </view>
          
          <view v-if="logList.length === 0 && !loading" class="empty-state">
            <text class="empty-text">暂无日志记录</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getDeviceLogList } from '@/api/deviceLog'

export default {
  data() {
    return {
      deviceCode: '',
      selectedLogType: { label: '日志类型', value: '' },
      logTypeOptions: [
        { label: '日志类型', value: '' },
        { label: '全部', value: '' },
        { label: '设备上线', value: 'online' },
        { label: '设备离线', value: 'offline' },
        { label: '命令下发', value: 'command' }
      ],
      startDate: '',
      endDate: '',
      logList: [],
      loading: false
    }
  },
  onLoad(options) {
    if (options.code) {
      this.deviceCode = options.code
    }
    // 设置默认日期范围（今天）
    const today = new Date()
    this.endDate = this.formatDate(today)
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    this.startDate = this.formatDate(yesterday)
    this.loadLogList()
  },
  methods: {
    async loadLogList() {
      this.loading = true
      try {
        const params = {
          page: 1,
          pageSize: 100,
          deviceCode: this.deviceCode,
          startTime: this.startDate ? `${this.startDate} 00:00:00` : '',
          endTime: this.endDate ? `${this.endDate} 23:59:59` : ''
        }
        if (this.selectedLogType.value) {
          params.logType = this.selectedLogType.value
        }
        
        const res = await getDeviceLogList(params)
        if (res) {
          // 后端返回的数据结构可能是 list 或 records
          this.logList = res.list || res.records || []
        }
      } catch (error) {
        console.error('加载日志列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    handleLogTypeChange(e) {
      this.selectedLogType = this.logTypeOptions[e.detail.value]
      this.loadLogList()
    },
    handleStartDateChange(e) {
      this.startDate = e.detail.value
    },
    handleEndDateChange(e) {
      this.endDate = e.detail.value
    },
    handleQuery() {
      this.loadLogList()
    },
    goBack() {
      uni.navigateBack()
    },
    getLogIconClass(type) {
      if (type === 'online') return 'success'
      if (type === 'offline') return 'danger'
      return 'command'
    },
    getLogTypeText(type) {
      const map = {
        online: '设备上线',
        offline: '设备离线',
        command: '命令下发'
      }
      return map[type] || '未知'
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatTime(time) {
      if (!time) return '-'
      // 去掉T，格式化为 yyyy-MM-dd HH:mm:ss，确保在一行显示
      let timeStr = typeof time === 'string' ? time : String(time)
      if (timeStr.includes('T')) {
        timeStr = timeStr.replace('T', ' ')
      }
      // 只取前19个字符（yyyy-MM-dd HH:mm:ss），确保不换行
      return timeStr.substring(0, 19)
    }
  }
}
</script>

<style scoped>
.device-log-page {
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

.filter-section-log {
  display: flex;
  gap: 16rpx;
  margin-bottom: 32rpx;
  align-items: center;
  flex-wrap: wrap;
}

.filter-select-log {
  flex: 1;
  min-width: 200rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #1d1d1f;
  border: 1rpx solid #e5e7eb;
}

.date-picker {
  flex: 1;
  min-width: 200rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #1d1d1f;
  border: 1rpx solid #e5e7eb;
}

.picker-text {
  color: #1d1d1f;
}

.query-btn-log {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx 48rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  flex-shrink: 0;
}

.log-list-card {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.log-item {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  display: flex;
  gap: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  border-left: 8rpx solid transparent;
  transition: all 0.3s ease;
}

.log-item:active {
  transform: translateY(-4rpx);
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.12);
}

.log-item .log-icon-wrapper {
  flex-shrink: 0;
}

.log-icon-bg {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.log-icon-bg.success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.log-icon-bg.danger {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);
}

.log-icon-bg.command {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.log-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16rpx;
}

.log-type {
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: white;
}

.log-type.success {
  background: #34c759;
}

.log-type.danger {
  background: #ff3b30;
}

.log-type.command {
  background: #667eea;
}

.log-time {
  font-size: 24rpx;
  color: #86868b;
}

.log-detail {
  font-size: 28rpx;
  color: #6e6e73;
  line-height: 1.5;
}

.empty-state {
  text-align: center;
  padding: 128rpx 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #86868b;
}
</style>
