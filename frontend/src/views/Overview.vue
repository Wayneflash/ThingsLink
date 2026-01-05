<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <span style="font-size: 32px;">📱</span>
        </div>
        <div class="stat-info">
          <div class="stat-label">设备总数</div>
          <div class="stat-value">{{ stats.totalDevices }}</div>
          <div class="stat-detail">
            <span class="online">在线 {{ stats.onlineDevices }}</span>
            <span class="offline">离线 {{ stats.offlineDevices }}</span>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <span style="font-size: 32px;">📊</span>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日数据量</div>
          <div class="stat-value">{{ stats.todayDataCount }}</div>
          <div class="stat-detail">
            <span style="color: #999;">设备上报的数据条数</span>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
          <span style="font-size: 32px;">🏭</span>
        </div>
        <div class="stat-info">
          <div class="stat-label">产品类型</div>
          <div class="stat-value">{{ productCount }}</div>
          <div class="stat-detail">
            <span v-if="stats.productDistribution.length > 0" style="color: #999;">
              {{ stats.productDistribution.map(p => p.productName).join('、') }}
            </span>
            <span v-else style="color: #999;">暂无产品</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近更新设备列表 -->
    <div class="recent-devices">
      <div class="section-title">
        <span>⏰ 最近活跃设备</span>
      </div>
      <div class="device-list">
        <div v-for="device in stats.recentDevices" :key="device.deviceCode" class="device-item">
          <div class="device-icon">
            <span :class="device.status === 1 ? 'status-dot online' : 'status-dot offline'"></span>
            📱
          </div>
          <div class="device-info">
            <div class="device-name">{{ device.deviceName }}</div>
            <div class="device-meta">{{ device.productName }} · {{ formatTime(device.lastOnlineTime) }}</div>
          </div>
          <div class="device-status">
            <span :class="device.status === 1 ? 'badge online' : 'badge offline'">
              {{ device.status === 1 ? '在线' : '离线' }}
            </span>
          </div>
        </div>
        <div v-if="stats.recentDevices.length === 0" class="empty-state">
          <span style="font-size: 48px; opacity: 0.3;">📭</span>
          <p>暂无设备数据</p>
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
  recentDevices: [],
  productCount: 0 // 添加产品总数
})

const productCount = computed(() => stats.value.productCount)

const loadStatistics = async () => {
  try {
    // axios 拦截器已经解包了 response.data.data，直接使用即可
    const data = await axios.get('/devices/statistics')
    console.log('Dashboard 统计数据:', data)
    
    if (data) {
      stats.value = {
        totalDevices: data.totalDevices || 0,
        onlineDevices: data.onlineDevices || 0,
        offlineDevices: data.offlineDevices || 0,
        todayDataCount: data.todayDataCount || 0,
        productDistribution: data.productDistribution || [],
        recentDevices: (data.recentDevices || []).slice(0, 5),
        productCount: data.productCount || 0 // 从接口获取产品总数
      }
      console.log('设置后的 stats:', stats.value)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
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

onMounted(() => {
  loadStatistics()
  // 每30秒刷新一次
  setInterval(loadStatistics, 30000)
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  display: flex;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #1d1d1f;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-detail {
  display: flex;
  gap: 16px;
  font-size: 13px;
}

.stat-detail .online {
  color: #67c23a;
  font-weight: 500;
}

.stat-detail .offline {
  color: #e6a23c;
  font-weight: 500;
}

/* 最近设备列表 */
.recent-devices {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.2s;
}

.device-item:hover {
  background: #e9ecef;
  transform: translateX(4px);
}

.device-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  position: relative;
  flex-shrink: 0;
}

.status-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-dot.online {
  background: #67c23a;
}

.status-dot.offline {
  background: #e6a23c;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.device-meta {
  font-size: 13px;
  color: #999;
}

.device-status {
  flex-shrink: 0;
}

.badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.badge.online {
  background: #f0f9ff;
  color: #67c23a;
}

.badge.offline {
  background: #fef0f0;
  color: #e6a23c;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  margin-top: 12px;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .stat-value {
    font-size: 28px;
  }
}
</style>
