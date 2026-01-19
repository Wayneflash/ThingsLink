<template>
  <view class="device-detail-page">
    <view class="mobile-header-bar">
      <view class="back-icon" @click="goBack">
        <Icon name="ArrowLeft" :size="24" />
      </view>
      <text class="header-title">设备详情</text>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="mobile-content-area">
        <!-- 设备信息卡片 -->
        <view class="device-info-card">
          <view class="device-name-row">
            <text class="device-name">{{ deviceInfo.name || deviceInfo.deviceName || '-' }}</text>
            <view class="device-tag" :class="deviceInfo.status === 1 ? 'tag-success' : 'tag-warning'">
              <text>{{ deviceInfo.status === 1 ? '在线' : '离线' }}</text>
            </view>
          </view>
          <text class="device-code">{{ deviceInfo.code || deviceInfo.deviceCode || '-' }}</text>
          <view class="device-meta-grid">
            <view class="meta-item">
              <text class="meta-label">产品类型</text>
              <text class="meta-value">{{ deviceInfo.productName || '未知' }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">所属分组</text>
              <text class="meta-value">{{ deviceInfo.groupName || '未分组' }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">最后上线</text>
              <text class="meta-value time-value">{{ formatTime(deviceInfo.lastOnlineTime) }}</text>
            </view>
          </view>
        </view>

        <!-- Tab 导航 -->
        <view class="tab-nav">
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 'realtime' }"
            @click="switchTab('realtime')"
          >
            <text>实时数据</text>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 'history' }"
            @click="switchTab('history')"
          >
            <text>历史数据</text>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 'command' }"
            @click="switchTab('command')"
          >
            <text>命令控制</text>
          </view>
          <view 
            class="tab-item" 
            :class="{ active: activeTab === 'log' }"
            @click="switchTab('log')"
          >
            <text>设备日志</text>
          </view>
        </view>

        <!-- 实时数据卡片 -->
        <view v-if="activeTab === 'realtime'" class="data-grid">
          <view 
            class="data-card" 
            v-for="attr in deviceData" 
            :key="attr.identifier"
          >
            <text class="data-label">{{ attr.name || attr.identifier }}</text>
            <view class="data-value-row">
              <text class="data-value">{{ formatValue(attr.value) }}</text>
              <text class="data-unit" v-if="attr.unit">{{ attr.unit }}</text>
            </view>
            <view class="data-time">
              <Icon name="Clock" :size="12" color="#86868b" />
              <text>{{ formatTime(attr.updateTime) }}</text>
            </view>
          </view>
          
          <view v-if="deviceData.length === 0" class="empty-data">
            <text class="empty-text">暂无数据</text>
          </view>
        </view>

        <!-- 设备日志 -->
        <view v-if="activeTab === 'log'" class="log-content">
          <!-- 筛选区域 -->
          <view class="filter-card">
            <picker 
              mode="selector" 
              :range="logTypeOptions" 
              range-key="label" 
              @change="handleLogTypeChange"
              class="filter-picker"
            >
              <view class="filter-item">
                <Icon name="Document" :size="20" color="#667eea" />
                <text class="filter-label">{{ selectedLogType.label || '日志类型' }}</text>
                <Icon name="ArrowRight" :size="16" color="#86868b" />
              </view>
            </picker>
            
            <view class="filter-date-row">
              <picker 
                mode="date" 
                :value="logStartDate"
                @change="handleLogStartDateChange"
                class="filter-date-picker"
              >
                <view class="filter-date-item">
                  <Icon name="Clock" :size="18" color="#86868b" />
                  <text class="filter-date-text">{{ logStartDate || '开始日期' }}</text>
                </view>
              </picker>
              <text class="date-separator">至</text>
              <picker 
                mode="date" 
                :value="logEndDate"
                @change="handleLogEndDateChange"
                class="filter-date-picker"
              >
                <view class="filter-date-item">
                  <Icon name="Clock" :size="18" color="#86868b" />
                  <text class="filter-date-text">{{ logEndDate || '结束日期' }}</text>
                </view>
              </picker>
            </view>
            
            <button class="filter-query-btn" @click="loadDeviceLogs" :disabled="logLoading">
              <Icon name="Search" :size="20" color="#ffffff" />
              <text>{{ logLoading ? '查询中...' : '查询' }}</text>
            </button>
          </view>

          <!-- 日志列表 -->
          <view class="log-list-container">
            <view 
              class="log-item" 
              v-for="(log, index) in deviceLogList" 
              :key="index"
            >
              <!-- 左侧时间线 -->
              <view class="log-timeline">
                <view class="log-dot" :class="getLogTypeClass(log.logType)"></view>
                <view class="log-line" v-if="index < deviceLogList.length - 1"></view>
              </view>
              
              <!-- 右侧内容 -->
              <view class="log-content-wrapper">
                <view class="log-header">
                  <view class="log-type-badge" :class="getLogTypeClass(log.logType)">
                    <text>{{ getLogTypeText(log.logType) }}</text>
                  </view>
                  <text class="log-time">{{ formatTime(log.createTime || log.logTime) }}</text>
                </view>
                <text class="log-detail">{{ log.logDetail || log.content || log.message || '-' }}</text>
              </view>
            </view>
            
            <view v-if="deviceLogList.length === 0 && !logLoading" class="empty-state">
              <text class="empty-text">暂无日志数据</text>
            </view>
            <view v-if="logLoading" class="empty-state">
              <text class="empty-text">加载中...</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getDeviceDetail, getDeviceLatestData } from '@/api/device'
import { getProductAttributes } from '@/api/product'
import { getDeviceLogList } from '@/api/deviceLog'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    Icon
  },
  data() {
    return {
      deviceId: '',
      deviceCode: '',
      deviceInfo: {},
      productAttributes: [],
      deviceData: [],
      updateTime: '-',
      activeTab: 'realtime',
      // 设备日志相关
      deviceLogList: [],
      logLoading: false,
      selectedLogType: { label: '日志类型', value: '' },
      logTypeOptions: [
        { label: '日志类型', value: '' },
        { label: '全部', value: '' },
        { label: '设备上线', value: 'online' },
        { label: '设备离线', value: 'offline' },
        { label: '命令下发', value: 'command' }
      ],
      logStartDate: '',
      logEndDate: '',
      logPagination: {
        currentPage: 1,
        pageSize: 20,
        total: 0
      }
    }
  },
  onLoad(options) {
    if (options.id) {
      this.deviceId = options.id
    }
    if (options.code) {
      this.deviceCode = options.code
    }
    this.loadDeviceInfo()
  },
  methods: {
    async loadDeviceInfo() {
      try {
        const res = await getDeviceDetail({
          deviceCode: this.deviceCode || this.deviceInfo.deviceCode
        })
        if (res) {
          this.deviceInfo = res
          if (!this.deviceCode && res.deviceCode) {
            this.deviceCode = res.deviceCode
          }
          // 加载产品属性后再加载数据
          if (res.productId) {
            await this.loadProductAttributes()
          }
          // 加载实时数据
          await this.loadDeviceData()
        }
      } catch (error) {
        console.error('加载设备信息失败:', error)
        // 确保错误被正确处理，不会抛出未捕获的异常
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
    async loadProductAttributes() {
      if (!this.deviceInfo.productId) {
        console.warn('产品ID不存在')
        return
      }
      
      // 如果已经加载过，不重复加载
      if (this.productAttributes.length > 0) {
        return
      }
      
      try {
        const attrData = await getProductAttributes(this.deviceInfo.productId)
        if (attrData) {
          // 与web端一致：直接赋值数组
          this.productAttributes = attrData || []
          console.log('产品属性加载完成:', this.productAttributes.length, '个')
        }
      } catch (error) {
        console.error('加载产品属性失败:', error)
        // 确保错误信息是字符串
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '加载属性失败') 
          : String(error || '加载属性失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      }
    },
    async loadDeviceData() {
      try {
        // 先确保产品属性已加载（与web端一致）
        if (this.productAttributes.length === 0) {
          await this.loadProductAttributes()
        }
        
        const data = await getDeviceLatestData({
          deviceCode: this.deviceCode
        })
        
        if (data) {
          // 格式化时间，确保显示为 yyyy-MM-dd HH:mm:ss 格式（与web端一致）
          const reportTime = data.reportTime
          if (reportTime) {
            this.updateTime = reportTime.includes('T') ? reportTime.replace('T', ' ') : reportTime
          } else {
            const now = new Date()
            const year = now.getFullYear()
            const month = String(now.getMonth() + 1).padStart(2, '0')
            const day = String(now.getDate()).padStart(2, '0')
            const hours = String(now.getHours()).padStart(2, '0')
            const minutes = String(now.getMinutes()).padStart(2, '0')
            const seconds = String(now.getSeconds()).padStart(2, '0')
            this.updateTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
          }
          
          // 与web端完全一致：使用 data.data 作为 dataMap
          const dataMap = data.data || {}
          
          // 与web端完全一致：使用 productAttributes.map，通过 attr.addr 获取值
          this.deviceData = this.productAttributes.map(attr => {
            const value = dataMap[attr.addr]
            
            return {
              identifier: attr.addr,
              name: attr.attrName,
              value: value !== undefined && value !== null && value !== '' ? value : '-',
              unit: attr.unit || '',
              updateTime: this.updateTime
            }
          })
        } else {
          // 如果没有数据，显示空状态（与web端一致）
          this.deviceData = this.productAttributes.map(attr => ({
            identifier: attr.addr,
            name: attr.attrName,
            value: '-',
            unit: attr.unit || '',
            updateTime: this.updateTime || '-'
          }))
        }
      } catch (error) {
        console.error('加载实时数据失败:', error)
        // 确保错误信息是字符串
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '加载数据失败') 
          : String(error || '加载数据失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      }
    },
    switchTab(tabName) {
      this.activeTab = tabName
      
      if (tabName === 'history') {
        // 跳转到历史数据页面
        uni.navigateTo({
          url: `/pages/device/history?code=${this.deviceCode}&name=${encodeURIComponent(this.deviceInfo.name || this.deviceInfo.deviceName || '')}`
        })
      } else if (tabName === 'command') {
        // 跳转到命令控制页面
        uni.navigateTo({
          url: `/pages/device/command?code=${this.deviceCode}&name=${encodeURIComponent(this.deviceInfo.name || this.deviceInfo.deviceName || '')}`
        })
      } else if (tabName === 'log') {
        // 切换到日志tab，设置默认时间范围并加载日志
        if (this.deviceLogList.length === 0) {
          this.setDefaultLogDateRange()
          this.loadDeviceLogs()
        }
      } else if (tabName === 'realtime') {
        // 切换到实时数据，刷新数据
        this.loadDeviceData()
      }
    },
    setDefaultLogDateRange() {
      // 设置默认时间范围：今天00:00到现在
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
      
      this.logStartDate = this.formatDate(today)
      this.logEndDate = this.formatDate(now)
    },
    formatDate(date) {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    async loadDeviceLogs() {
      this.logLoading = true
      try {
        const params = {
          page: this.logPagination.currentPage,
          pageSize: this.logPagination.pageSize,
          deviceCode: this.deviceCode,
          logType: this.selectedLogType.value || ''
        }
        
        if (this.logStartDate) {
          params.startTime = `${this.logStartDate} 00:00:00`
        }
        if (this.logEndDate) {
          params.endTime = `${this.logEndDate} 23:59:59`
        }
        
        const res = await getDeviceLogList(params)
        if (res) {
          this.deviceLogList = res.list || res.records || []
          this.logPagination.total = res.total || 0
        } else {
          this.deviceLogList = []
          this.logPagination.total = 0
        }
      } catch (error) {
        console.error('加载设备日志失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '加载失败') 
          : String(error || '加载失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.logLoading = false
      }
    },
    handleLogTypeChange(e) {
      this.selectedLogType = this.logTypeOptions[e.detail.value]
      this.logPagination.currentPage = 1
      this.loadDeviceLogs()
    },
    handleLogStartDateChange(e) {
      this.logStartDate = e.detail.value
    },
    handleLogEndDateChange(e) {
      this.logEndDate = e.detail.value
    },
    getLogTypeText(logType) {
      const map = {
        'online': '设备上线',
        'offline': '设备离线',
        'command': '命令下发'
      }
      return map[logType] || '未知'
    },
    getLogTypeClass(logType) {
      const map = {
        'online': 'log-type-success',
        'offline': 'log-type-danger',
        'command': 'log-type-info'
      }
      return map[logType] || 'log-type-default'
    },
    goBack() {
      // 检查页面栈，如果有上一页则返回，否则跳转到设备列表
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.reLaunch({
          url: '/pages/device/list'
        })
      }
    },
    formatValue(value) {
      if (value === null || value === undefined) return '-'
      if (typeof value === 'number') {
        return value.toFixed(2)
      }
      return value
    },
    formatTime(time) {
      if (!time) return '未知'
      // 去掉T，格式化为 yyyy-MM-dd HH:mm:ss，确保在一行显示
      let timeStr = typeof time === 'string' ? time : String(time)
      if (timeStr.includes('T')) {
        timeStr = timeStr.replace('T', ' ')
      }
      // 只取前19个字符（yyyy-MM-dd HH:mm:ss），确保不换行
      return timeStr.substring(0, 19)
    },
  }
}
</script>

<style scoped>
.device-detail-page {
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

.device-info-card {
  background: #ffffff;
  margin-bottom: 32rpx;
  padding: 40rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.device-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.device-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.device-code {
  display: block;
  font-size: 26rpx;
  color: #86868b;
  margin-bottom: 32rpx;
}

.device-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.meta-label {
  font-size: 24rpx;
  color: #86868b;
}

.meta-value {
  font-size: 28rpx;
  color: #1d1d1f;
  font-weight: 500;
}

.time-value {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tab-nav {
  display: flex;
  background: #ffffff;
  margin-bottom: 32rpx;
  border-radius: 24rpx;
  padding: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.tab-item {
  flex: 1;
  min-width: 0;
  text-align: center;
  padding: 20rpx 16rpx;
  font-size: 26rpx;
  color: #86868b;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.data-card {
  background: #ffffff;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.data-label {
  display: block;
  font-size: 26rpx;
  color: #86868b;
  margin-bottom: 16rpx;
}

.data-value-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.data-value {
  font-size: 48rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.data-unit {
  font-size: 28rpx;
  color: #86868b;
}

.data-time {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 22rpx;
  color: #86868b;
}

.empty-data {
  grid-column: 1 / -1;
  text-align: center;
  padding: 128rpx 32rpx;
}

.empty-text {
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

.device-tag {
  padding: 8rpx 24rpx;
  border-radius: 12rpx;
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

/* 设备日志样式 */
.log-content {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.filter-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin: 0 auto;
  width: 92%;
  max-width: 92%;
  box-sizing: border-box;
}

.filter-picker {
  width: 100%;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 24rpx;
  background: #f5f5f7;
  border-radius: 16rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s ease;
}

.filter-item:active {
  background: #e5e7eb;
  border-color: #667eea;
}

.filter-label {
  flex: 1;
  font-size: 30rpx;
  color: #1d1d1f;
  font-weight: 500;
  text-align: left;
}

.filter-date-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: nowrap;
}

.filter-date-picker {
  flex: 1;
  min-width: 0;
}

.filter-date-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 16rpx;
  background: #f5f5f7;
  border-radius: 16rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s ease;
  white-space: nowrap;
  overflow: hidden;
}

.filter-date-item:active {
  background: #e5e7eb;
  border-color: #667eea;
}

.filter-date-text {
  flex: 1;
  font-size: 26rpx;
  color: #1d1d1f;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.date-separator {
  font-size: 26rpx;
  color: #86868b;
  padding: 0 4rpx;
  flex-shrink: 0;
  font-weight: 500;
  white-space: nowrap;
}

.filter-query-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.filter-query-btn text {
  color: #ffffff;
}

.filter-query-btn:active {
  opacity: 0.9;
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.25);
}

.filter-query-btn[disabled] {
  opacity: 0.5;
  transform: none;
}

.log-list-container {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  margin: 0 auto;
  width: 92%;
  max-width: 92%;
  box-sizing: border-box;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 24rpx 0;
  position: relative;
}

.log-item:not(:last-child) {
  border-bottom: 1rpx solid #f0f0f0;
}

/* 时间线样式 */
.log-timeline {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 40rpx;
  position: relative;
}

.log-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  z-index: 2;
}

.log-line {
  position: absolute;
  top: 20rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 2rpx;
  height: calc(100% + 24rpx);
  background: #e5e7eb;
  z-index: 1;
}

.log-dot.log-type-success {
  background: #34c759;
}

.log-dot.log-type-danger {
  background: #ff3b30;
}

.log-dot.log-type-info {
  background: #667eea;
}

.log-dot.log-type-default {
  background: #86868b;
}

/* 内容区域 */
.log-content-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  flex-wrap: wrap;
}

.log-type-badge {
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 600;
  white-space: nowrap;
  letter-spacing: 0.01em;
}

.log-type-success {
  background: rgba(52, 199, 89, 0.12);
  color: #34c759;
}

.log-type-danger {
  background: rgba(255, 59, 48, 0.12);
  color: #ff3b30;
}

.log-type-info {
  background: rgba(102, 126, 234, 0.12);
  color: #667eea;
}

.log-type-default {
  background: rgba(134, 134, 139, 0.12);
  color: #86868b;
}

.log-time {
  font-size: 24rpx;
  color: #86868b;
  white-space: nowrap;
  font-weight: 400;
  letter-spacing: 0.01em;
}

.log-detail {
  font-size: 28rpx;
  color: #1d1d1f;
  line-height: 1.6;
  word-break: break-word;
  font-weight: 400;
  letter-spacing: -0.01em;
}

.empty-state {
  text-align: center;
  padding: 128rpx 32rpx;
}

.empty-state .empty-text {
  font-size: 28rpx;
  color: #86868b;
}
</style>
