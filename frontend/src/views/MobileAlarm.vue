<template>
  <div class="mobile-alarm">
    <!-- 顶部统计卡片 -->
    <div class="stats-section">
      <div class="stat-card" @click="filterByStatus(null)">
        <div class="stat-label">总报警数</div>
        <div class="stat-value">{{ statistics.total || 0 }}</div>
      </div>
      <div class="stat-card pending" @click="filterByStatus(0)">
        <div class="stat-label">未处理</div>
        <div class="stat-value">{{ statistics.unhandled || 0 }}</div>
      </div>
      <div class="stat-card critical" @click="filterByLevel('critical')">
        <div class="stat-label">严重</div>
        <div class="stat-value">{{ statistics.critical || 0 }}</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <el-select
        v-model="filters.alarmLevel"
        placeholder="报警级别"
        clearable
        class="filter-select"
        @change="loadAlarms"
      >
        <el-option label="全部" value="" />
        <el-option label="严重" value="critical" />
        <el-option label="警告" value="warning" />
        <el-option label="提示" value="info" />
      </el-select>
      <el-select
        v-model="filters.status"
        placeholder="处理状态"
        clearable
        class="filter-select"
        @change="loadAlarms"
      >
        <el-option label="全部" value="" />
        <el-option label="未处理" :value="0" />
        <el-option label="已处理" :value="1" />
      </el-select>
    </div>

    <!-- 报警列表 -->
    <div class="alarm-list-content">
      <div
        v-for="alarm in alarmList"
        :key="alarm.id"
        class="alarm-card"
        :class="`alarm-${alarm.alarmLevel}`"
        @click="viewAlarmDetail(alarm)"
      >
        <div class="alarm-icon">
          <el-icon v-if="alarm.alarmLevel === 'critical'" :size="24" color="#ff3b30">
            <WarningFilled />
          </el-icon>
          <el-icon v-else-if="alarm.alarmLevel === 'warning'" :size="24" color="#ff9500">
            <Warning />
          </el-icon>
          <el-icon v-else :size="24" color="#34c759">
            <InfoFilled />
          </el-icon>
        </div>
        <div class="alarm-content">
          <div class="alarm-header">
            <div class="alarm-device">{{ alarm.deviceName || '未知设备' }}</div>
            <el-tag v-if="alarm.status === 0" type="danger" size="small">未处理</el-tag>
            <el-tag v-else type="success" size="small">已处理</el-tag>
          </div>
          <div class="alarm-message">{{ alarm.message || alarm.content }}</div>
          <div class="alarm-time">
            <el-icon :size="12"><Clock /></el-icon>
            {{ formatTime(alarm.createTime) }}
          </div>
        </div>
      </div>

      <div v-if="alarmList.length === 0 && !loading" class="empty-state">
        <el-icon :size="64" color="#ccc"><BellFilled /></el-icon>
        <p>暂无报警信息</p>
      </div>

      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>加载中...</p>
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
import { ElIcon } from 'element-plus'
import {
  WarningFilled,
  Warning,
  InfoFilled,
  BellFilled,
  Clock,
  Loading,
  HomeFilled,
  Monitor,
  Bell,
  User
} from '@element-plus/icons-vue'
import { getAlarmLogList, getAlarmStatistics } from '@/api/alarm'

const router = useRouter()
const route = useRoute()

// 统计数据
const statistics = ref({
  total: 0,
  unhandled: 0,
  critical: 0
})

// 筛选条件
const filters = ref({
  alarmLevel: '',
  status: ''
})

// 报警列表
const alarmList = ref([])
const loading = ref(false)
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0
})

// 底部导航项
const bottomNavItems = [
  { path: '/mobile-overview', label: '概览', icon: HomeFilled },
  { path: '/mobile-device-list', label: '设备', icon: Monitor },
  { path: '/mobile-alarm', label: '报警', icon: Bell },
  { path: '/mobile-profile', label: '我的', icon: User }
]

const currentPath = computed(() => route.path)

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
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
const loadStatistics = async () => {
  try {
    const res = await getAlarmStatistics()
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载报警列表
const loadAlarms = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }

    if (filters.value.alarmLevel) {
      params.alarmLevel = filters.value.alarmLevel
    }
    if (filters.value.status !== '') {
      params.status = filters.value.status
    }

    const res = await getAlarmLogList(params)
    if (res.code === 200) {
      alarmList.value = res.data?.records || res.data?.list || []
      pagination.value.total = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载报警列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 按状态筛选
const filterByStatus = (status) => {
  filters.value.status = status
  loadAlarms()
}

// 按级别筛选
const filterByLevel = (level) => {
  filters.value.alarmLevel = level
  loadAlarms()
}

// 查看报警详情
const viewAlarmDetail = (alarm) => {
  router.push({
    path: '/alarms',
    query: { id: alarm.id }
  })
}

// 导航
const navigateTo = (path) => {
  router.push(path)
}

// 初始化
onMounted(() => {
  loadStatistics()
  loadAlarms()
})
</script>

<style scoped>
.mobile-alarm {
  min-height: 100vh;
  background: #f5f5f7;
  padding-bottom: 80px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.stats-section {
  display: flex;
  gap: 12px;
  padding: 16px;
}

.stat-card {
  flex: 1;
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.pending {
  border-left: 4px solid #ff9500;
}

.stat-card.critical {
  border-left: 4px solid #ff3b30;
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
}

.filter-section {
  padding: 0 16px 16px;
  display: flex;
  gap: 12px;

}

.filter-select {
  flex: 1;
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.alarm-list-content {
  padding: 0 16px;
}

.alarm-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-left: 4px solid transparent;

}

.alarm-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.alarm-card.alarm-critical {
  border-left-color: #ff3b30;
}

.alarm-card.alarm-warning {
  border-left-color: #ff9500;
}

.alarm-card.alarm-info {
  border-left-color: #34c759;
}

.alarm-icon {
  flex-shrink: 0;
}

.alarm-content {
  flex: 1;
  min-width: 0;
}

.alarm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.alarm-device {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
}

.alarm-message {
  font-size: 14px;
  color: #86868b;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.alarm-time {
  font-size: 12px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state,
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #86868b;

}

.empty-state p,
.loading-state p {
  margin: 16px 0 0;
  font-size: 14px;
}

.loading-state .is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 底部导航栏 */
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
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

/* 移动端响应式优化 */
@media (max-width: 375px) {
  .stats-section {
    padding: 12px;
    gap: 8px;
  }

  .stat-card {
    padding: 12px;
  }

  .stat-value {
    font-size: 20px;
  }

  .filter-section {
    padding: 0 12px 12px;
    gap: 8px;
  }

  .alarm-list-content {
    padding: 0 12px;
  }
}

@media (min-width: 376px) and (max-width: 414px) {
  .stats-section {
    gap: 10px;
  }
}
</style>