<template>
  <view class="overview-page">
    <!-- 顶部导航栏 -->
    <view class="mobile-header-bar">
      <text class="header-title">设备概览</text>
      <view class="header-icon refresh-btn" @click="handleRefresh">
        <view class="icon-svg" v-html="getRefreshIcon()"></view>
      </view>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="mobile-content-area">
        <!-- 核心指标卡片 - 2x2布局 -->
        <view class="stats-grid-v2">
          <!-- 设备总数 -->
          <view class="stat-card-v2 stat-primary" @click="navigateToDevices">
            <view class="stat-icon-wrapper-v2">
              <view class="stat-icon-v2" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <view class="icon-svg" v-html="getMonitorIcon()"></view>
              </view>
              <view class="stat-trend-badge" v-if="stats.totalDevices > 0">
                <text class="trend-up">↑ 5</text>
              </view>
            </view>
            <view class="stat-content-v2">
              <text class="stat-label-v2">设备总数</text>
              <text class="stat-value-v2">{{ stats.totalDevices || 0 }}</text>
              <view class="stat-detail-v2">
                <text class="detail-item">
                  <text class="status-dot online"></text>
                  在线 {{ stats.onlineDevices || 0 }}
                </text>
                <text class="detail-item">
                  <text class="status-dot offline"></text>
                  离线 {{ stats.offlineDevices || 0 }}
                </text>
              </view>
            </view>
          </view>

          <!-- 在线设备 -->
          <view class="stat-card-v2 stat-success">
            <view class="stat-icon-wrapper-v2">
              <view class="stat-icon-v2" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
                <view class="icon-svg" v-html="getCheckIcon()"></view>
              </view>
              <view class="stat-trend-badge" v-if="onlineRate > 0">
                <text class="trend-up">{{ onlineRate }}%</text>
              </view>
            </view>
            <view class="stat-content-v2">
              <text class="stat-label-v2">在线设备</text>
              <text class="stat-value-v2">{{ stats.onlineDevices || 0 }}</text>
              <view class="stat-detail-v2">
                <text class="detail-text">在线率 {{ onlineRate }}%</text>
              </view>
            </view>
          </view>

          <!-- 今日报警 -->
          <view class="stat-card-v2 stat-danger" @click="navigateToAlarm">
            <view class="stat-icon-wrapper-v2">
              <view class="stat-icon-v2" style="background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);">
                <view class="icon-svg" v-html="getWarningIcon()"></view>
              </view>
            </view>
            <view class="stat-content-v2">
              <text class="stat-label-v2">今日报警</text>
              <text class="stat-value-v2">{{ stats.todayAlarmCount || 0 }}</text>
              <view class="stat-detail-v2">
                <text class="detail-item critical">严重 {{ alarmStats.critical || 0 }}</text>
                <text class="detail-item warning">警告 {{ alarmStats.warning || 0 }}</text>
              </view>
            </view>
          </view>

          <!-- 今日数据 -->
          <view class="stat-card-v2 stat-info">
            <view class="stat-icon-wrapper-v2">
              <view class="stat-icon-v2" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <view class="icon-svg" v-html="getDataIcon()"></view>
              </view>
              <view class="stat-trend-badge" v-if="dataTrend > 0">
                <text class="trend-up">↑ {{ dataTrend }}%</text>
              </view>
            </view>
            <view class="stat-content-v2">
              <text class="stat-label-v2">今日数据量</text>
              <text class="stat-value-v2">{{ formatDataCount(stats.todayDataCount || 0) }}</text>
              <view class="stat-detail-v2">
                <text class="detail-text" v-if="dataTrend > 0">较昨日 +{{ dataTrend }}%</text>
                <text class="detail-text" v-else>条记录</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 设备状态分布图表卡片 -->
        <view class="chart-card-mobile">
          <view class="chart-header-mobile">
            <view class="chart-title-mobile">
              <view class="icon-svg" v-html="getChartIcon()" style="width: 18px; height: 18px; color: #667eea;"></view>
              <text>设备状态分布</text>
            </view>
          </view>
          <view class="chart-content-mobile">
            <view class="status-chart-mini">
              <view class="status-item">
                <view class="status-bar">
                  <view class="status-fill online" :style="{ width: onlinePercent + '%' }"></view>
                </view>
                <view class="status-info">
                  <text class="status-label">在线</text>
                  <text class="status-value">{{ stats.onlineDevices || 0 }} 台 ({{ onlineRate }}%)</text>
                </view>
              </view>
              <view class="status-item">
                <view class="status-bar">
                  <view class="status-fill offline" :style="{ width: offlinePercent + '%' }"></view>
                </view>
                <view class="status-info">
                  <text class="status-label">离线</text>
                  <text class="status-value">{{ stats.offlineDevices || 0 }} 台 ({{ offlineRate }}%)</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 最近活跃设备列表 -->
        <view class="section-card-v2">
          <view class="section-header-v2">
            <view class="section-title-v2">
              <view class="icon-svg" v-html="getClockIcon()" style="width: 18px; height: 18px; color: #667eea;"></view>
              <text>最近活跃设备</text>
            </view>
            <text class="section-more-v2" @click="navigateToDevices">查看全部 →</text>
          </view>
          <view class="device-list-v2" v-if="recentDevices.length > 0">
            <view 
              class="device-item-v2" 
              v-for="device in recentDevices" 
              :key="device.deviceCode || device.id"
              @click="navigateToDeviceDetail(device.deviceCode || device.id)"
            >
              <view class="device-icon-wrapper-v2">
                <view class="device-icon-bg-v2">
                  <view class="icon-svg" v-html="getMonitorIcon()" style="width: 20px; height: 20px;"></view>
                </view>
              </view>
              <view class="device-content-v2">
                <view class="device-header-v2">
                  <text class="device-name-v2">{{ device.deviceCode || '-' }}</text>
                  <view class="device-tag" :class="device.status === 1 || device.status === 'online' ? 'tag-success' : 'tag-warning'">
                    <text>{{ device.status === 1 || device.status === 'online' ? '在线' : '离线' }}</text>
                  </view>
                </view>
                <text class="device-meta-v2">{{ device.deviceName || '-' }} · 最后上线：<text class="time-text">{{ formatLastOnlineTime(device.lastOnlineTime) }}</text></text>
              </view>
            </view>
          </view>
          <view class="empty-state" v-else>
            <text class="empty-text">暂无活跃设备</text>
          </view>
        </view>
      </view>
    </scroll-view>
    <BottomNav />
  </view>
</template>

<script>
import { getOverviewStats, getAlarmStats } from '@/api/statistics'
import BottomNav from '@/components/BottomNav.vue'

export default {
  components: {
    BottomNav
  },
  data() {
    return {
      stats: {
        totalDevices: 0,
        onlineDevices: 0,
        offlineDevices: 0,
        todayAlarmCount: 0,
        unhandledCount: 0,
        todayDataCount: 0
      },
      recentDevices: [],
      alarmStats: {
        critical: 0,
        warning: 0
      },
      dataTrend: 0
    }
  },
  computed: {
    onlineRate() {
      if (this.stats.totalDevices === 0) return 0
      return Math.round((this.stats.onlineDevices / this.stats.totalDevices) * 100)
    },
    offlineRate() {
      if (this.stats.totalDevices === 0) return 0
      return Math.round((this.stats.offlineDevices / this.stats.totalDevices) * 100)
    },
    onlinePercent() {
      return this.onlineRate
    },
    offlinePercent() {
      return this.offlineRate
    }
  },
  onLoad() {
    this.loadStatistics()
    this.loadRecentDevices()
    this.loadAlarmStats()
  },
  onShow() {
    // 每次显示页面时刷新数据
    this.loadStatistics()
    this.loadRecentDevices()
    this.loadAlarmStats()
  },
  methods: {
    getRefreshIcon() {
      return '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"></path><path d="M21 3v5h-5"></path><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"></path><path d="M3 21v-5h5"></path></svg>'
    },
    getMonitorIcon() {
      return '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>'
    },
    getCheckIcon() {
      return '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="9 12 11 14 15 10"></polyline></svg>'
    },
    getWarningIcon() {
      return '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>'
    },
    getDataIcon() {
      return '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>'
    },
    getClockIcon() {
      return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>'
    },
    getChartIcon() {
      return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"></path><path d="M7 12l4-4 4 4 6-6"></path></svg>'
    },
    async loadStatistics() {
      try {
        const res = await getOverviewStats()
        if (res) {
          // 后端返回的字段名：totalDevices, onlineDevices, offlineDevices, todayDataCount, recentDevices
          this.stats = {
            totalDevices: res.totalDevices || 0,
            onlineDevices: res.onlineDevices || 0,
            offlineDevices: res.offlineDevices || 0,
            todayAlarmCount: res.todayAlarmCount || 0,
            unhandledCount: res.unhandledCount || 0,
            todayDataCount: res.todayDataCount || 0
          }
          // 从统计数据中获取最近活跃设备
          if (res.recentDevices && res.recentDevices.length > 0) {
            this.recentDevices = res.recentDevices.slice(0, 5).map(item => ({
              id: item.id,
              deviceCode: item.deviceCode || item.code,
              deviceName: item.deviceName || item.name,
              status: item.status, // 保持原始状态值（1=在线，0=离线）
              lastOnlineTime: item.lastOnlineTime
            }))
          }
          // 计算数据趋势（假设有昨日数据）
          if (res.yesterdayDataCount) {
            const trend = res.todayDataCount && res.yesterdayDataCount 
              ? Math.round(((res.todayDataCount - res.yesterdayDataCount) / res.yesterdayDataCount) * 100)
              : 0
            this.dataTrend = trend
          }
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        // 确保错误信息是字符串
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '获取数据失败') 
          : String(error || '获取数据失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      }
    },
    async loadAlarmStats() {
      try {
        const res = await getAlarmStats({})
        if (res) {
          // 后端返回：critical, warning, info, total, unhandled
          this.alarmStats = {
            critical: res.critical || res.criticalCount || 0,
            warning: res.warning || res.warningCount || 0
          }
        }
      } catch (error) {
        console.error('加载报警统计失败:', error)
        // 静默失败，不影响主流程
      }
    },
    async loadRecentDevices() {
      // 最近活跃设备已从loadStatistics中获取，这里不再需要单独请求
      // 如果需要刷新，直接调用loadStatistics即可
    },
    handleRefresh() {
      this.loadStatistics()
      this.loadRecentDevices()
      uni.showToast({
        title: '刷新成功',
        icon: 'success',
        duration: 1500
      })
    },
    navigateToDevices() {
      uni.switchTab({
        url: '/pages/device/list'
      })
    },
    navigateToAlarm() {
      uni.switchTab({
        url: '/pages/alarm/alarm'
      })
    },
    navigateToDeviceDetail(deviceCode) {
      uni.navigateTo({
        url: `/pages/device/detail?code=${deviceCode}`
      })
    },
    formatDataCount(count) {
      if (count >= 10000) {
        return (count / 10000).toFixed(1) + '万'
      } else if (count >= 1000) {
        return (count / 1000).toFixed(1) + 'K'
      }
      return count.toString()
    },
    formatLastOnlineTime(time) {
      if (!time) return '未知'
      try {
        const date = new Date(time)
        if (isNaN(date.getTime())) return '未知'
        
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      } catch (error) {
        console.error('时间格式化失败:', error)
        return '未知'
      }
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
    }
  }
}
</script>

<style scoped>
.overview-page {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 顶部导航栏 */
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

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.header-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
  cursor: pointer;
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

.refresh-btn:active {
  opacity: 0.6;
}

.content-scroll {
  height: calc(100vh - 88rpx);
}

.mobile-content-area {
  padding: 32rpx;
  padding-bottom: 120rpx;
}

/* 核心指标卡片 - 2x2布局 */
.stats-grid-v2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.stat-card-v2 {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.stat-card-v2:active {
  transform: translateY(-4rpx);
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.12);
}

.stat-icon-wrapper-v2 {
  position: relative;
  margin-bottom: 24rpx;
}

.stat-icon-v2 {
  width: 112rpx;
  height: 112rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.stat-trend-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 8rpx 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  font-size: 22rpx;
  font-weight: 600;
}

.trend-up {
  color: #34c759;
}

.stat-content-v2 {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.stat-label-v2 {
  font-size: 26rpx;
  color: #86868b;
  font-weight: 500;
}

.stat-value-v2 {
  font-size: 56rpx;
  font-weight: 700;
  color: #1d1d1f;
  line-height: 1.2;
}

.stat-detail-v2 {
  display: flex;
  gap: 24rpx;
  flex-wrap: wrap;
  margin-top: 8rpx;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #86868b;
}

.detail-text {
  font-size: 24rpx;
  color: #86868b;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.online {
  background: #34c759;
}

.status-dot.offline {
  background: #ff3b30;
}

/* 设备状态分布图表卡片 */
.chart-card-mobile {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.chart-header-mobile {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32rpx;
}

.chart-title-mobile {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.chart-content-mobile {
  padding: 16rpx 0;
}

.status-chart-mini {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.status-bar {
  height: 16rpx;
  background: #f5f5f7;
  border-radius: 8rpx;
  overflow: hidden;
  position: relative;
}

.status-fill {
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.6s ease;
}

.status-fill.online {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
}

.status-fill.offline {
  background: linear-gradient(90deg, #f56c6c 0%, #ff8a80 100%);
}

.status-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 26rpx;
  color: #86868b;
  font-weight: 500;
}

.status-value {
  font-size: 26rpx;
  color: #1d1d1f;
  font-weight: 600;
}

/* 最近活跃设备列表 */
.section-card-v2 {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.section-header-v2 {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32rpx;
}

.section-title-v2 {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.section-more-v2 {
  font-size: 26rpx;
  color: #667eea;
  cursor: pointer;
  font-weight: 500;
}

.device-list-v2 {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.device-item-v2 {
  display: flex;
  gap: 24rpx;
  padding: 28rpx;
  background: #f9f9f9;
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.3s ease;
}

.device-item-v2:active {
  background: #f0f0f0;
  transform: translateX(4rpx);
}

.device-icon-wrapper-v2 {
  position: relative;
  flex-shrink: 0;
}

.device-icon-bg-v2 {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.device-content-v2 {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.device-header-v2 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.device-name-v2 {
  font-size: 30rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.device-tag {
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
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

.device-meta-v2 {
  font-size: 24rpx;
  color: #86868b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-text {
  white-space: nowrap;
  display: inline;
}

.empty-state {
  text-align: center;
  padding: 64rpx 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #86868b;
}
</style>
