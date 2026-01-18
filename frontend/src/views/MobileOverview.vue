<template>
  <div class="mobile-overview">
    <!-- 顶部标题栏 -->
    <div class="mobile-header">
      <h1 class="mobile-title">设备概览</h1>
      <div class="header-actions">
        <el-icon class="icon-btn" :size="24" @click="handleRefresh">
          <Refresh style="cursor: pointer;" />
        </el-icon>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="mobile-content">
      <!-- 数据摘要卡片区域 -->
      <div class="stats-grid">
        <!-- 设备总数 -->
        <div class="stat-card" @click="navigateTo('/devices')">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon :size="28">
              <Monitor />
            </el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-label">设备总数</div>
            <div class="stat-value">{{ formatNumber(stats.totalDevices) }}</div>
            <div class="stat-footer">
              <span class="online-dot"></span>
              <span>在线 {{ stats.onlineDevices }}</span>
            </div>
          </div>
        </div>

        <!-- 在线设备 -->
        <div class="stat-card success" @click="navigateTo('/devices', { status: 'online' })">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
            <el-icon :size="28">
              <CircleCheck />
            </el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-label">在线设备</div>
            <div class="stat-value">{{ formatNumber(stats.onlineDevices) }}</div>
            <div class="stat-footer">
              <span>在线率 {{ stats.onlineRate || 0 }}%</span>
            </div>
          </div>
        </div>

        <!-- 今日报警 -->
        <div class="stat-card danger" @click="navigateTo('/alarms')">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);">
            <el-icon :size="28">
              <Bell />
            </el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-label">今日报警</div>
            <div class="stat-value">{{ formatNumber(stats.todayAlarms) }}</div>
            <div class="stat-footer">
              <span>未处理 {{ stats.unhandledAlarms || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 今日数据量 -->
        <div class="stat-card info">
          <div class="stat-card-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon :size="28">
              <DataLine />
            </el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-label">今日数据量</div>
            <div class="stat-value">{{ formatNumber(stats.todayDataCount) }}</div>
            <div class="stat-footer">
              <span v-if="stats.dataTrend !== 0" :class="stats.dataTrend > 0 ? 'trend-up' : 'trend-down'">
                {{ stats.dataTrend > 0 ? '↑' : '↓' }} {{ Math.abs(stats.dataTrend) }}%
              </span>
              <span v-else>较昨日</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 报警摘要 -->
      <div class="alarm-section">
        <div class="section-header">
          <h3 class="section-title">最新报警</h3>
          <span class="section-more" @click="navigateTo('/alarms')">查看更多</span>
        </div>
        <div v-if="alarms.length === 0" class="empty-alarm">
          <el-icon :size="48" color="#ccc">
            <BellFilled />
          </el-icon>
          <p>暂无报警信息</p>
        </div>
        <div v-else class="alarm-list">
          <div
            v-for="alarm in alarms"
            :key="alarm.id"
            class="alarm-item"
            :class="`alarm-${alarm.level}`"
            @click="viewAlarmDetail(alarm)"
          >
            <div class="alarm-icon">
              <el-icon v-if="alarm.level === 'critical'" :size="20" color="#ff3b30">
                <WarningFilled />
              </el-icon>
              <el-icon v-else-if="alarm.level === 'warning'" :size="20" color="#ff9500">
                <Warning />
              </el-icon>
              <el-icon v-else :size="20" color="#34c759">
                <InfoFilled />
              </el-icon>
            </div>
            <div class="alarm-content">
              <div class="alarm-title">{{ alarm.deviceName || '未知设备' }}</div>
              <div class="alarm-message">{{ alarm.message || alarm.content }}</div>
              <div class="alarm-time">{{ formatTime(alarm.createTime) }}</div>
            </div>
            <div class="alarm-status">
              <el-tag v-if="alarm.status === 'pending'" type="danger" size="small">未处理</el-tag>
              <el-tag v-else type="success" size="small">已处理</el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <div class="section-header">
          <h3 class="section-title">快捷操作</h3>
        </div>
        <div class="actions-grid">
          <div class="action-item" @click="navigateTo('/devices')">
            <div class="action-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="24">
                <Monitor />
              </el-icon>
            </div>
            <div class="action-label">设备管理</div>
          </div>
          <div class="action-item" @click="navigateTo('/data-query')">
            <div class="action-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon :size="24">
                <Search />
              </el-icon>
            </div>
            <div class="action-label">数据查询</div>
          </div>
          <div class="action-item" @click="navigateTo('/alarms')">
            <div class="action-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);">
              <el-icon :size="24">
                <Bell />
              </el-icon>
            </div>
            <div class="action-label">报警日志</div>
          </div>
          <div class="action-item" @click="navigateTo('/energy/statistics')">
            <div class="action-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
              <el-icon :size="24">
                <DataLine />
              </el-icon>
            </div>
            <div class="action-label">能源统计</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="mobile-bottom-nav">
      <div
        v-for="item in bottomNavItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: currentPath === item.path }"
        @click="navigateTo(item.path)"
      >
        <el-icon :size="22">
          <component :is="item.icon" v-if="item.icon" />
        </el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElIcon } from 'element-plus'
import {
  Monitor,
  CircleCheck,
  Bell,
  BellFilled,
  DataLine,
  WarningFilled,
  Warning,
  InfoFilled,
  Search,
  Refresh,
  HomeFilled,
  User
} from '@element-plus/icons-vue'
import { getOverviewStats } from '@/api/statistics'
import { getAlarmLogList } from '@/api/alarm'

const router = useRouter()
const route = useRoute()

// 底部导航项
const bottomNavItems = [
  { path: '/mobile-overview', label: '概览', icon: HomeFilled },
  { path: '/devices', label: '设备', icon: Monitor },
  { path: '/alarms', label: '报警', icon: Bell },
  { path: '/profile', label: '我的', icon: User }
]

// 统计数据
const stats = ref({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  onlineRate: 0,
  todayAlarms: 0,
  unhandledAlarms: 0,
  todayDataCount: 0,
  dataTrend: 0
})

// 报警列表
const alarms = ref([])

// 当前路径
const currentPath = computed(() => route.path)

// 格式化数字
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getOverviewStats()
    if (res.code === 200) {
      stats.value = {
        totalDevices: res.data.totalDevices || 0,
        onlineDevices: res.data.onlineDevices || 0,
        offlineDevices: res.data.offlineDevices || 0,
        onlineRate: res.data.onlineRate || 0,
        todayAlarms: res.data.todayAlarms || 0,
        unhandledAlarms: res.data.unhandledAlarms || 0,
        todayDataCount: res.data.todayDataCount || 0,
        dataTrend: res.data.dataTrend || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载报警列表
const loadAlarms = async () => {
  try {
    const res = await getAlarmLogList({
      page: 1,
      pageSize: 3,
      status: 0 // 0-未处理
    })
    if (res.code === 200) {
      alarms.value = res.data?.records || res.data?.list || []
    }
  } catch (error) {
    console.error('加载报警列表失败:', error)
  }
}

// 刷新数据
const handleRefresh = () => {
  loadStats()
  loadAlarms()
  ElMessage.success('刷新成功')
}

// 导航
const navigateTo = (path, params = {}) => {
  if (params && Object.keys(params).length > 0) {
    router.push({ path, query: params })
  } else {
    router.push(path)
  }
}

// 查看报警详情
const viewAlarmDetail = (alarm) => {
  navigateTo('/alarms', { id: alarm.id })
}

// 初始化
onMounted(() => {
  loadStats()
  loadAlarms()
})
</script>

<style scoped>
.mobile-overview {
  min-height: 100vh;
  background: #f5f5f7;
  padding-bottom: 80px; /* 为底部导航留出空间 */
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 顶部标题栏 */
.mobile-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.mobile-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  cursor: pointer;
  color: #667eea;
  transition: all 0.3s ease;
}

.icon-btn:hover {
  color: #764ba2;
  transform: rotate(180deg);
}

/* 内容区域 */
.mobile-content {
  padding: 16px;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card:active {
  transform: translateY(0);
}

.stat-card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-card-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-footer {
  font-size: 12px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #34c759;
}

.trend-up {
  color: #34c759;
}

.trend-down {
  color: #ff3b30;
}

/* 报警摘要区域 */
.alarm-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.section-more {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  transition: color 0.3s ease;
}

.section-more:hover {
  color: #764ba2;
}

.empty-alarm {
  text-align: center;
  padding: 40px 20px;
  color: #86868b;
}

.empty-alarm p {
  margin: 12px 0 0;
  font-size: 14px;
}

.alarm-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alarm-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f9f9f9;
}

.alarm-item:hover {
  background: #f0f0f0;
}

.alarm-item.alarm-critical {
  border-left: 3px solid #ff3b30;
}

.alarm-item.alarm-warning {
  border-left: 3px solid #ff9500;
}

.alarm-item.alarm-info {
  border-left: 3px solid #34c759;
}

.alarm-icon {
  flex-shrink: 0;
}

.alarm-content {
  flex: 1;
  min-width: 0;
}

.alarm-title {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.alarm-message {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alarm-time {
  font-size: 12px;
  color: #86868b;
}

.alarm-status {
  flex-shrink: 0;
}

/* 快捷操作 */
.quick-actions {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.quick-actions .section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.action-item:active {
  transform: scale(0.95);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: transform 0.3s ease;
}

.action-label {
  font-size: 12px;
  color: #1d1d1f;
  text-align: center;
}

.action-item:hover .action-icon {
  transform: scale(1.1);
}

/* 底部导航栏 */
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-around;
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom));
  z-index: 1000;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #86868b;
  flex: 1;
}

.nav-item.active {
  color: #667eea;
}

.nav-item.active .nav-label {
  color: #667eea;
}

.nav-label {
  font-size: 11px;
  transition: color 0.3s ease;
}

.nav-item:hover {
  color: #667eea;
}

/* 响应式适配 - 移动端优化 */
@media (max-width: 375px) {
  .mobile-header {
    padding: 10px 12px;
  }

  .mobile-title {
    font-size: 16px;
  }

  .mobile-content {
    padding: 12px;
  }

  .stats-grid {
    gap: 10px;
  }

  .stat-card {
    padding: 12px;
    gap: 10px;
  }

  .stat-card-icon {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-label {
    font-size: 12px;
  }

  .actions-grid {
    gap: 12px;
  }

  .action-icon {
    width: 44px;
    height: 44px;
  }

  .action-label {
    font-size: 11px;
  }
}

@media (min-width: 376px) and (max-width: 414px) {
  .mobile-content {
    padding: 14px;
  }

  .stats-grid {
    gap: 11px;
  }
}

@media (min-width: 415px) and (max-width: 768px) {
  .mobile-content {
    padding: 16px;
    max-width: 768px;
    margin: 0 auto;
  }
}
</style>