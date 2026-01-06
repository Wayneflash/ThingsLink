<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <div class="stats-grid">
      <div class="stat-card stat-card-primary">
        <div class="stat-icon">📱</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalDevices }}</div>
          <div class="stat-label">设备总数</div>
        </div>
      </div>
      
      <div class="stat-card stat-card-success">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.onlineDevices }}</div>
          <div class="stat-label">在线设备</div>
        </div>
      </div>
      
      <div class="stat-card stat-card-warning">
        <div class="stat-icon">❌</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.offlineDevices }}</div>
          <div class="stat-label">离线设备</div>
        </div>
      </div>
      
      <div class="stat-card stat-card-info">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayDataCount }}</div>
          <div class="stat-label">今日数据量</div>
        </div>
      </div>
    </div>

    <!-- 二级信息区域 -->
    <div class="info-grid">
      <!-- 产品分布 -->
      <div class="info-card">
        <div class="info-header">
          <span class="info-title">🏭 产品分布</span>
        </div>
        <div class="product-list">
          <div v-for="product in stats.productDistribution" :key="product.productName" class="product-item">
            <div class="product-name">{{ product.productName }}</div>
            <div class="product-count">{{ product.count }} 台</div>
          </div>
          <div v-if="stats.productDistribution.length === 0" class="empty-tip">暂无产品</div>
        </div>
      </div>

      <!-- 设备在线率 -->
      <div class="info-card">
        <div class="info-header">
          <span class="info-title">📊 设备在线率</span>
        </div>
        <div class="online-rate-container">
          <div class="online-rate-circle">
            <div class="online-rate-value">{{ onlineRate }}%</div>
          </div>
          <div class="online-rate-stats">
            <div class="rate-stat">
              <span class="rate-label">在线</span>
              <span class="rate-value success">{{ stats.onlineDevices }}</span>
            </div>
            <div class="rate-stat">
              <span class="rate-label">离线</span>
              <span class="rate-value warning">{{ stats.offlineDevices }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近更新 -->
      <div class="info-card">
        <div class="info-header">
          <span class="info-title">⏰ 最近更新</span>
        </div>
        <div class="recent-list">
          <div v-for="device in stats.recentDevices" :key="device.deviceCode" class="recent-item">
            <div class="recent-device">{{ device.deviceName }}</div>
            <div class="recent-time">{{ formatTime(device.lastOnlineTime) }}</div>
          </div>
          <div v-if="stats.recentDevices.length === 0" class="empty-tip">暂无数据</div>
        </div>
      </div>
    </div>

    <!-- 第三行：设备分组分布和分组状态 -->
    <div class="chart-row">
      <!-- 设备分组分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">📁 设备分组分布</span>
        </div>
        <div class="group-list">
          <div v-for="group in groupDistribution" :key="group.groupName" class="group-item">
            <div class="group-info">
              <div class="group-name">{{ group.groupName }}</div>
              <div class="group-count">{{ group.deviceCount }} 台</div>
            </div>
            <div class="group-bar">
              <div class="group-bar-fill" :style="{ width: getGroupPercentage(group.deviceCount) + '%' }"></div>
            </div>
          </div>
          <div v-if="groupDistribution.length === 0" class="empty-tip">暂无分组</div>
        </div>
      </div>

      <!-- 分组在线状态 -->
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">🔄 分组在线状态</span>
        </div>
        <div class="group-status-list">
          <div v-for="group in groupDistribution" :key="group.groupName" class="group-status-item">
            <div class="status-header">
              <span class="status-name">{{ group.groupName }}</span>
              <span class="status-rate">{{ getGroupOnlineRate(group) }}%</span>
            </div>
            <div class="status-detail">
              <div class="status-online">
                <span class="dot online"></span>
                <span>在线 {{ group.onlineCount || 0 }}</span>
              </div>
              <div class="status-offline">
                <span class="dot offline"></span>
                <span>离线 {{ group.offlineCount || 0 }}</span>
              </div>
            </div>
          </div>
          <div v-if="groupDistribution.length === 0" class="empty-tip">暂无数据</div>
        </div>
      </div>
    </div>

    <!-- 第四行：数据量统计 -->
    <div class="chart-row-full">
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">📊 数据上报统计（最近24小时）</span>
        </div>
        <div class="data-trend">
          <div v-for="(item, index) in dataTrend" :key="index" class="trend-item">
            <div class="trend-bar">
              <div class="trend-bar-fill" :style="{ height: getTrendHeight(item.count) + '%' }">
                <span class="trend-value">{{ item.count }}</span>
              </div>
            </div>
            <div class="trend-time">{{ item.time }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/utils/request'

const stats = ref({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  todayDataCount: 0,
  productDistribution: [],
  recentDevices: []
})

const groupDistribution = ref([])
const dataTrend = ref([])

const onlineRate = computed(() => {
  if (stats.value.totalDevices === 0) return 0
  return Math.round((stats.value.onlineDevices / stats.value.totalDevices) * 100)
})

// 获取分组百分比
const getGroupPercentage = (count) => {
  if (stats.value.totalDevices === 0) return 0
  return Math.round((count / stats.value.totalDevices) * 100)
}

// 获取分组在线率
const getGroupOnlineRate = (group) => {
  if (group.deviceCount === 0) return 0
  const online = group.onlineCount || 0
  return Math.round((online / group.deviceCount) * 100)
}

// 获取趋势图高度
const getTrendHeight = (count) => {
  const maxCount = Math.max(...dataTrend.value.map(item => item.count), 1)
  return Math.round((count / maxCount) * 100)
}

const loadStatistics = async () => {
  try {
    const res = await axios.get('/devices/statistics')
    if (res.data) {
      stats.value = {
        totalDevices: res.data.totalDevices || 0,
        onlineDevices: res.data.onlineDevices || 0,
        offlineDevices: res.data.offlineDevices || 0,
        todayDataCount: res.data.todayDataCount || 0,
        productDistribution: res.data.productDistribution || [],
        recentDevices: res.data.recentDevices || []
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载分组分布数据
const loadGroupDistribution = async () => {
  try {
    // 先获取设备列表（带分组信息）
    const deviceRes = await axios.post('/devices/list', { page: 1, pageSize: 10000 })
    const devices = deviceRes.data?.records || []
    
    if (devices.length === 0) {
      groupDistribution.value = []
      return
    }
    
    // 统计每个分组的设备数和在线数
    const groupMap = {}
    devices.forEach(device => {
      const groupId = device.groupId || 0
      const groupName = device.groupName || '默认分组'
      
      if (!groupMap[groupId]) {
        groupMap[groupId] = { 
          groupName: groupName,
          deviceCount: 0, 
          onlineCount: 0, 
          offlineCount: 0 
        }
      }
      groupMap[groupId].deviceCount++
      if (device.status === 1) {
        groupMap[groupId].onlineCount++
      } else {
        groupMap[groupId].offlineCount++
      }
    })
    
    // 转换为数组并排序
    groupDistribution.value = Object.values(groupMap)
      .sort((a, b) => b.deviceCount - a.deviceCount)
      .slice(0, 5) // 只显示前5个
  } catch (error) {
    console.error('加载分组分布失败:', error)
    groupDistribution.value = []
  }
}

// 加载24小时数据趋势
const loadDataTrend = async () => {
  const now = new Date()
  const hours = []
  
  // 生成24个小时的时间点
  for (let i = 23; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 60 * 60 * 1000)
    hours.push({
      time: `${String(time.getHours()).padStart(2, '0')}:00`,
      timestamp: time,
      count: 0
    })
  }
  
  try {
    // 获取设备数据（最近的）
    const dataRes = await axios.post('/device-data/list', { 
      page: 1, 
      pageSize: 1000
    })
    
    const dataList = dataRes.data?.records || []
    
    // 统计每个小时的数据量
    dataList.forEach(data => {
      const dataTime = new Date(data.receiveTime || data.ctime)
      const hourIndex = hours.findIndex(h => {
        const nextHour = new Date(h.timestamp.getTime() + 60 * 60 * 1000)
        return dataTime >= h.timestamp && dataTime < nextHour
      })
      if (hourIndex >= 0) {
        hours[hourIndex].count++
      }
    })
    
    dataTrend.value = hours
  } catch (error) {
    console.error('加载数据趋势失败:', error)
    // 如果加载失败，使用默认数据
    dataTrend.value = hours
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  const now = new Date()
  const target = new Date(time)
  const diff = Math.floor((now - target) / 1000)
  
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

const loadAllData = () => {
  loadStatistics()
  loadGroupDistribution()
  loadDataTrend()
}

onMounted(() => {
  loadAllData()
  // 每30秒刷新一次
  setInterval(loadAllData, 30000)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

/* 统计卡片区域 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 48px;
  line-height: 1;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-card-primary .stat-value { color: #409eff; }
.stat-card-success .stat-value { color: #67c23a; }
.stat-card-warning .stat-value { color: #e6a23c; }
.stat-card-info .stat-value { color: #909399; }

/* 信息卡片区域 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.info-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.info-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 产品分布 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.product-item:hover {
  background: #e9ecef;
}

.product-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.product-count {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

/* 在线率 */
.online-rate-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.online-rate-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

.online-rate-value {
  font-size: 32px;
  font-weight: 700;
  color: white;
}

.online-rate-stats {
  display: flex;
  gap: 24px;
  margin-top: 8px;
}

.rate-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.rate-label {
  font-size: 12px;
  color: #999;
}

.rate-value {
  font-size: 18px;
  font-weight: 600;
}

.rate-value.success { color: #67c23a; }
.rate-value.warning { color: #e6a23c; }

/* 最近更新 */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.recent-device {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.recent-time {
  font-size: 12px;
  color: #909399;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

/* 图表行 */
.chart-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.chart-row-full {
  margin-bottom: 20px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chart-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 分组分布 */
.group-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.group-count {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
}

.group-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.group-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 0.3s;
}

/* 分组状态 */
.group-status-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-status-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.status-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.status-rate {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
}

.status-detail {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #666;
}

.status-online,
.status-offline {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot.online {
  background: #67c23a;
}

.dot.offline {
  background: #e6a23c;
}

/* 数据趋势 */
.data-trend {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200px;
  padding: 20px 0;
  gap: 4px;
}

.trend-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.trend-bar {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: flex-end;
}

.trend-bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
  transition: height 0.3s;
  position: relative;
}

.trend-bar-fill:hover {
  opacity: 0.8;
}

.trend-value {
  font-size: 11px;
  color: white;
  font-weight: 600;
}

.trend-time {
  font-size: 11px;
  color: #999;
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }
  .data-trend {
    height: 150px;
  }
  .trend-bar {
    height: 110px;
  }
}
</style>
