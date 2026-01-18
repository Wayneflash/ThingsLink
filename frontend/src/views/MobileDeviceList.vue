<template>
  <div class="mobile-device-list">
    <!-- 顶部搜索栏 -->
    <div class="mobile-header">
      <div class="search-wrapper">
        <el-input
          v-model="searchQuery"
          placeholder="搜索设备名称或编码"
          clearable
          class="search-input"
          @keyup.enter="loadDevices"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="filterStatus"
          placeholder="状态"
          clearable
          class="status-filter"
          @change="loadDevices"
        >
          <el-option label="全部" value="" />
          <el-option label="在线" value="online" />
          <el-option label="离线" value="offline" />
        </el-select>
      </div>
    </div>

    <!-- 设备列表 -->
    <div class="device-list-content">
      <div
        v-for="device in deviceList"
        :key="device.deviceCode"
        class="device-card"
        @click="goToDetail(device.deviceCode)"
      >
        <div class="device-status-indicator" :class="device.status === 1 ? 'online' : 'offline'"></div>
        
        <div class="device-info">
          <div class="device-name">{{ device.deviceName }}</div>
          <div class="device-meta">
            <span class="device-code">{{ device.deviceCode }}</span>
            <span class="separator">·</span>
            <span class="device-product">{{ device.productName }}</span>
          </div>
          <div class="device-time">
            <el-icon :size="12"><Clock /></el-icon>
            最后上线：{{ formatTime(device.lastOnlineTime) }}
          </div>
        </div>

        <div class="device-status-badge">
          <el-tag :type="device.status === 1 ? 'success' : 'warning'" size="small">
            {{ device.status === 1 ? '在线' : '离线' }}
          </el-tag>
        </div>
      </div>

      <div v-if="deviceList.length === 0 && !loading" class="empty-state">
        <el-icon :size="64" color="#ccc"><Box /></el-icon>
        <p>暂无设备数据</p>
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
  Search,
  Clock,
  Box,
  Loading,
  HomeFilled,
  Monitor,
  Bell,
  User
} from '@element-plus/icons-vue'
import { getDeviceList } from '@/api/device'

const router = useRouter()
const route = useRoute()

// 搜索和筛选
const searchQuery = ref('')
const filterStatus = ref('')
const loading = ref(false)

// 设备列表
const deviceList = ref([])
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

// 加载设备列表
const loadDevices = async () => {
  try {
    loading.value = true
    const res = await getDeviceList({
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      keyword: searchQuery.value || undefined,
      status: filterStatus.value || undefined
    })

    if (res.code === 200) {
      deviceList.value = res.data?.records || res.data?.list || []
      pagination.value.total = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到设备详情
const goToDetail = (deviceCode) => {
  router.push({
    path: '/mobile-device-detail',
    query: { deviceCode }
  })
}

// 导航
const navigateTo = (path) => {
  router.push(path)
}

// 初始化
onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
.mobile-device-list {
  min-height: 100vh;
  background: #f5f5f7;
  padding-bottom: 80px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.mobile-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.search-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.status-filter {
  width: 90px;
}

.status-filter :deep(.el-input__wrapper) {
  border-radius: 12px;
}

.device-list-content {
  padding: 16px;
}

.device-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
}

.device-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.device-card:active {
  transform: translateY(0);
}

.device-status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.device-status-indicator.online {
  background: #34c759;
  box-shadow: 0 0 8px rgba(52, 199, 89, 0.4);
}

.device-status-indicator.offline {
  background: #ff3b30;
}

.device-info {
  flex: 1;
  min-width: 0;
}

.device-name {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-meta {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.separator {
  color: #d1d1d6;
}

.device-time {
  font-size: 12px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.device-status-badge {
  flex-shrink: 0;
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
</style>