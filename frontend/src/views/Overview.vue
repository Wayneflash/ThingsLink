<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 - 6个核心指标 -->
    <div class="stats-cards">
      <!-- 设备总数 -->
      <div class="stat-card stat-card-primary" @click="goToDevices">
        <div class="stat-icon-wrapper">
          <div class="stat-icon" style="background: linear-gradient(135deg, #3B82F6 0%, #6366F1 100%);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z" fill="white"/>
            </svg>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-label">设备总数</div>
          <div class="stat-value">{{ formatNumber(stats.totalDevices) }}</div>
          <div class="stat-detail">
            <span class="online">在线 {{ stats.onlineDevices }}</span>
            <span class="offline">离线 {{ stats.offlineDevices }}</span>
          </div>
        </div>
      </div>

      <!-- 在线设备 -->
      <div class="stat-card stat-card-success" @click="goToDevices({ status: 'online' })">
        <div class="stat-icon-wrapper">
          <div class="stat-icon" style="background: linear-gradient(135deg, #10B981 0%, #34D399 100%);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="white"/>
            </svg>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-label">在线设备</div>
          <div class="stat-value">{{ formatNumber(stats.onlineDevices) }}</div>
          <div class="stat-detail">
            <span style="color: #94A3B8;">在线率 {{ stats.onlineRate || 0 }}%</span>
          </div>
        </div>
      </div>

      <!-- 产品数量 -->
      <div class="stat-card stat-card-warning" @click="goToProducts">
        <div class="stat-icon-wrapper">
          <div class="stat-icon" style="background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 6h-2.18c.11-.31.18-.65.18-1 0-1.66-1.34-3-3-3-1.05 0-1.96.54-2.5 1.35l-.5.67-.5-.68C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zM9 4c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm11 15H4v-2h16v2zm0-5H4V8h5.08L7 10.83 8.62 12 11 8.76l1-1.36 1 1.36L15.38 12 17 10.83 14.92 8H20v6z" fill="white"/>
            </svg>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-label">产品类型</div>
          <div class="stat-value">{{ formatNumber(stats.productCount || 0) }}</div>
          <div class="stat-detail">
            <span style="color: #94A3B8;">已创建产品</span>
          </div>
        </div>
      </div>

      <!-- 今日数据量 -->
      <div class="stat-card stat-card-info stat-card-no-click">
        <div class="stat-icon-wrapper">
          <div class="stat-icon" style="background: linear-gradient(135deg, #06B6D4 0%, #22D3EE 100%);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z" fill="white"/>
            </svg>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日数据量</div>
          <div class="stat-value">{{ formatNumber(stats.todayDataCount) }}</div>
          <div class="stat-detail">
            <span style="color: #94A3B8;">较昨日 {{ stats.dataTrend > 0 ? '+' : '' }}{{ formatPercent(stats.dataTrend) }}</span>
          </div>
        </div>
      </div>

      <!-- 今日报警 -->
      <div class="stat-card stat-card-danger" @click="goToAlarmsToday()">
        <div class="stat-icon-wrapper">
          <div class="stat-icon" style="background: linear-gradient(135deg, #EF4444 0%, #F87171 100%);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z" fill="white"/>
            </svg>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日报警</div>
          <div class="stat-value">{{ formatNumber(stats.todayAlarmCount || 0) }}</div>
          <div class="stat-detail">
            <span class="critical">严重 {{ stats.todayCriticalCount || 0 }}</span>
            <span class="warning">警告 {{ stats.todayWarningCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 今日未处理报警 -->
      <div class="stat-card stat-card-warning-alt" @click="goToAlarmsTodayUnhandled()">
        <div class="stat-icon-wrapper">
          <div class="stat-icon" style="background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" fill="white"/>
            </svg>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日未处理</div>
          <div class="stat-value">{{ formatNumber(stats.todayUnhandledCount || 0) }}</div>
          <div class="stat-detail">
            <span style="color: #94A3B8;">待处理报警</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 第一行：设备状态、产品分布 -->
    <div class="charts-grid charts-grid-top">
      <!-- 设备状态分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title-wrapper">
            <el-icon class="chart-icon" :size="20"><DataAnalysis /></el-icon>
            <span class="chart-title">设备状态分布</span>
          </div>
        </div>
        <div ref="statusChartRef" class="chart-container" v-loading="statusChartLoading"></div>
      </div>

      <!-- 产品分布饼图 -->
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title-wrapper">
            <el-icon class="chart-icon" :size="20"><Box /></el-icon>
            <span class="chart-title">产品分布</span>
          </div>
        </div>
        <div ref="productChartRef" class="chart-container" v-loading="productChartLoading"></div>
      </div>
    </div>

    <!-- 第二行：最近报警、最近活跃设备（单独一行，两个等宽） -->
    <div class="info-cards-row">
      <div class="info-card">
        <div class="card-header">
          <div class="card-title-wrapper">
            <el-icon class="card-icon" :size="20"><Bell /></el-icon>
            <span class="card-title">最近报警</span>
          </div>
          <el-button type="text" @click="goToAlarms">查看全部 →</el-button>
        </div>
        <div class="alarm-list">
          <div v-for="alarm in recentAlarms" :key="alarm.id" class="alarm-item" :class="`alarm-level-${alarm.alarmLevel}`" @click="goToAlarmDetail(alarm.id)">
            <div class="alarm-icon-wrapper">
              <div class="alarm-icon">
                <svg v-if="alarm.alarmLevel === 'critical'" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" fill="currentColor"/>
                </svg>
                <svg v-else-if="alarm.alarmLevel === 'warning'" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" fill="currentColor"/>
                </svg>
                <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z" fill="currentColor"/>
                </svg>
              </div>
            </div>
            <div class="alarm-content">
              <div class="alarm-title">{{ alarm.deviceName || alarm.deviceCode }}</div>
              <div class="alarm-desc">{{ alarm.alarmMessage }}</div>
              <div class="alarm-time">{{ formatTime(alarm.triggerTime) }}</div>
            </div>
            <div class="alarm-status">
              <el-tag :type="alarm.status === 0 ? 'warning' : 'success'" size="small">
                {{ alarm.status === 0 ? '未处理' : '已处理' }}
              </el-tag>
            </div>
          </div>
          <div v-if="recentAlarms.length === 0" class="empty-state">
            <el-icon class="empty-icon" :size="48"><Bell /></el-icon>
            <p>暂无报警数据</p>
          </div>
        </div>
      </div>

      <!-- 最近活跃设备 -->
      <div class="info-card">
        <div class="card-header">
          <div class="card-title-wrapper">
            <el-icon class="card-icon" :size="20"><Clock /></el-icon>
            <span class="card-title">最近活跃设备</span>
          </div>
          <el-button type="text" @click="goToDevices">查看全部 →</el-button>
        </div>
        <div class="device-list">
          <div v-for="device in stats.recentDevices" :key="device.deviceCode" class="device-item">
            <div class="device-icon-wrapper">
              <div class="device-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z" fill="currentColor"/>
                </svg>
              </div>
              <span :class="device.status === 1 ? 'status-dot online' : 'status-dot offline'"></span>
            </div>
            <div class="device-info">
              <div class="device-name">{{ device.deviceName }}</div>
              <div class="device-meta">
                <span>{{ device.productName }}</span>
                <span class="separator">·</span>
                <span>{{ formatTime(device.lastOnlineTime) }}</span>
              </div>
            </div>
            <div class="device-status">
              <el-tag :type="device.status === 1 ? 'success' : 'warning'" size="small">
                {{ device.status === 1 ? '在线' : '离线' }}
              </el-tag>
            </div>
          </div>
          <div v-if="stats.recentDevices.length === 0" class="empty-state">
            <el-icon class="empty-icon" :size="48"><Document /></el-icon>
            <p>暂无设备数据</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 下方一行：今日报警趋势、设备分组统计 -->
    <div class="charts-grid charts-grid-bottom">
      <!-- 今日报警趋势（按小时） -->
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title-wrapper">
            <el-icon class="chart-icon" :size="20"><Bell /></el-icon>
            <span class="chart-title">今日报警趋势（按小时）</span>
          </div>
          <div class="chart-actions">
            <el-button size="small" @click="loadTodayAlarmTrend()">刷新</el-button>
          </div>
        </div>
        <div class="data-trend-wrapper" v-loading="trendLoading">
          <div v-if="alarmTrendList.length > 0" class="data-trend-bars alarm-trend-bars">
            <div v-for="(item, index) in alarmTrendList" :key="index" class="trend-bar-item">
              <div class="trend-bar-bg">
                <div class="trend-bar-fill alarm-bar" :style="{ height: getAlarmBarHeight(item.count) + '%' }" :title="`${item.time} 共 ${item.count} 条（严重 ${item.critical}，警告 ${item.warning}）`">
                  <span class="trend-bar-value">{{ item.count }}</span>
                </div>
              </div>
              <div class="trend-bar-label">{{ item.time }}</div>
            </div>
          </div>
          <div v-else-if="!trendLoading" class="data-trend-empty">
            <el-icon :size="32" color="#c0c4cc"><Bell /></el-icon>
            <p>今日暂无报警</p>
          </div>
        </div>
      </div>

      <!-- 设备分组统计 -->
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title-wrapper">
            <el-icon class="chart-icon" :size="20"><FolderOpened /></el-icon>
            <span class="chart-title">设备分组统计</span>
          </div>
        </div>
        <div class="group-stats-list">
          <div v-for="group in groupStats" :key="group.groupId" class="group-stat-item">
            <div class="group-stat-header">
              <span class="group-name">{{ group.groupName || '未分组' }}</span>
              <span class="group-count">{{ group.deviceCount }} 台</span>
            </div>
            <div class="group-stat-bar">
              <div class="group-stat-bar-fill" :style="{ width: getGroupPercentage(group.deviceCount) + '%' }">
                <div class="group-stat-bar-info">
                  <span class="online-count">在线 {{ group.onlineCount }}</span>
                  <span class="offline-count">离线 {{ group.offlineCount }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="groupStats.length === 0" class="empty-state">
            <el-icon class="empty-icon" :size="48"><Document /></el-icon>
            <p>暂无分组数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { 
  TrendCharts, 
  DataAnalysis, 
  Box, 
  FolderOpened, 
  Bell, 
  Clock, 
  Document 
} from '@element-plus/icons-vue'
import axios from '@/utils/request'
import * as echarts from 'echarts'

const router = useRouter()

// 统计数据
const stats = ref({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  todayDataCount: 0,
  yesterdayDataCount: 0,
  alarmCount: 0,
  onlineRate: 0,
  deviceTrend: 0,
  dataTrend: 0,
  productDistribution: [],
  recentDevices: [],
  productCount: 0,
  todayAlarmCount: 0,
  todayCriticalCount: 0,
  todayWarningCount: 0,
  todayUnhandledCount: 0
})

// 图表相关
const productChartRef = ref(null)
const statusChartRef = ref(null)
let productChartInstance = null
let statusChartInstance = null

// 数据趋势列表（轻量柱状图，替代原 ECharts）

// 加载状态
const trendLoading = ref(false)
const productChartLoading = ref(false)
const statusChartLoading = ref(false)

// 今日报警趋势（按小时）
const alarmTrendList = ref([])

// 分组统计
const groupStats = ref([])

// 最近报警
const recentAlarms = ref([])

// 刷新定时器
let refreshTimer = null

// 计算属性
const onlineRate = computed(() => {
  if (stats.value.totalDevices === 0) return 0
  return Math.round((stats.value.onlineDevices / stats.value.totalDevices) * 100)
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 使用POST方法调用统计接口（符合API规范）
    const data = await axios.post('/devices/statistics')
    console.log('统计数据:', data)
    
    if (data) {
      const onlineRate = data.totalDevices > 0 
        ? Math.round((data.onlineDevices / data.totalDevices) * 100) 
        : 0
      
      // 补充最近设备的productName（如果后端没有返回）
      const recentDevices = (data.recentDevices || []).slice(0, 8).map(device => {
        // 如果设备没有productName，尝试从productDistribution中查找
        if (!device.productName && device.productId) {
          const product = data.productDistribution?.find(p => p.productId === device.productId)
          if (product) {
            device.productName = product.productName
          }
        }
        return device
      })
      
      // 获取报警统计数据
      const alarmStats = await loadAlarmStatistics()
      
      stats.value = {
        totalDevices: data.totalDevices || 0,
        onlineDevices: data.onlineDevices || 0,
        offlineDevices: data.offlineDevices || 0,
        todayDataCount: data.todayDataCount || 0,
        yesterdayDataCount: 0, // 后端暂无此字段，设为0
        alarmCount: alarmStats.total || 0,
        unhandledAlarmCount: alarmStats.unhandled || 0,
        criticalCount: alarmStats.critical || 0,
        warningCount: alarmStats.warning || 0,
        infoCount: alarmStats.info || 0,
        onlineRate: onlineRate,
        deviceTrend: 0, // 后端暂无此字段
        dataTrend: 0, // 需要计算昨日数据后才能得出
        productDistribution: data.productDistribution || [],
        recentDevices: recentDevices,
        productCount: data.productCount || 0,
        todayAlarmCount: alarmStats.todayAlarmCount || 0,
        todayCriticalCount: alarmStats.todayCriticalCount || 0,
        todayWarningCount: alarmStats.todayWarningCount || 0,
        todayUnhandledCount: alarmStats.todayUnhandledCount || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载今日报警趋势（按小时聚合）
const loadTodayAlarmTrend = async () => {
  trendLoading.value = true
  try {
    const now = new Date()
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0)
    const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999)
    const res = await axios.post('/alarm-log/list', {
      page: 1,
      pageSize: 500,
      startTime: formatDateTime(todayStart),
      endTime: formatDateTime(todayEnd)
    })
    const list = res?.list || []
    const hours = Array.from({ length: 24 }, (_, i) => ({
      time: String(i).padStart(2, '0') + ':00',
      count: 0,
      critical: 0,
      warning: 0
    }))
    list.forEach((a) => {
      const t = a.triggerTime || a.trigger_time
      if (!t) return
      const h = new Date(t).getHours()
      if (h >= 0 && h < 24) {
        hours[h].count++
        if (a.alarmLevel === 'critical') hours[h].critical++
        else if (a.alarmLevel === 'warning') hours[h].warning++
      }
    })
    alarmTrendList.value = hours
  } catch (error) {
    console.error('加载今日报警趋势失败:', error)
    alarmTrendList.value = []
  } finally {
    trendLoading.value = false
  }
}

// 报警柱状图高度（相对最大值）
const getAlarmBarHeight = (count) => {
  if (!alarmTrendList.value.length) return 0
  const max = Math.max(...alarmTrendList.value.map((item) => item.count), 1)
  return Math.round((count / max) * 100)
}

// 加载产品分布饼图
const loadProductChart = async () => {
  if (!productChartRef.value || stats.value.productDistribution.length === 0) return
  
  productChartLoading.value = true
  try {
    await nextTick()
    
    if (!productChartInstance) {
      productChartInstance = echarts.init(productChartRef.value)
    }
    
    const colors = ['#3B82F6', '#8B5CF6', '#EC4899', '#F97316', '#06B6D4', '#10B981', '#6366F1', '#14B8A6']
    const data = stats.value.productDistribution.map((item, index) => ({
      value: item.count,
      name: item.productName,
      itemStyle: { color: colors[index % colors.length] }
    }))
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255, 255, 255, 0.96)',
        borderColor: 'rgba(59, 130, 246, 0.15)',
        borderWidth: 1,
        textStyle: { color: '#1E293B', fontSize: 13 },
        padding: [10, 14],
        extraCssText: 'box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); border-radius: 10px;'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        textStyle: { color: '#64748B', fontSize: 12 }
      },
      series: [{
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}台 ({d}%)',
          fontSize: 12,
          color: '#475569'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 13,
            fontWeight: 'bold',
            color: '#1E293B'
          }
        },
        data: data
      }]
    }
    
    productChartInstance.setOption(option)
    
    window.addEventListener('resize', () => {
      productChartInstance?.resize()
    })
  } catch (error) {
    console.error('加载产品分布图失败:', error)
  } finally {
    productChartLoading.value = false
  }
}

// 加载设备状态分布图
const loadStatusChart = async () => {
  if (!statusChartRef.value) return
  
  statusChartLoading.value = true
  try {
    await nextTick()
    
    if (!statusChartInstance) {
      statusChartInstance = echarts.init(statusChartRef.value)
    }
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255, 255, 255, 0.96)',
        borderColor: 'rgba(59, 130, 246, 0.15)',
        borderWidth: 1,
        textStyle: { color: '#1E293B', fontSize: 13 },
        padding: [10, 14],
        extraCssText: 'box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); border-radius: 10px;'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        textStyle: { color: '#64748B', fontSize: 12 }
      },
      series: [{
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['38%', '50%'],
        data: [
          { value: stats.value.onlineDevices, name: '在线', itemStyle: { color: '#10B981' } },
          { value: stats.value.offlineDevices, name: '离线', itemStyle: { color: '#CBD5E1' } }
        ],
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 12,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.15)'
          }
        },
        label: {
          show: true,
          formatter: '{b}\n{c}台 ({d}%)',
          fontSize: 12,
          color: '#475569'
        }
      }]
    }
    
    statusChartInstance.setOption(option)
    
    window.addEventListener('resize', () => {
      statusChartInstance?.resize()
    })
  } catch (error) {
    console.error('加载状态分布图失败:', error)
  } finally {
    statusChartLoading.value = false
  }
}

// 加载分组统计
const loadGroupStats = async () => {
  try {
    const res = await axios.post('/devices/list', { page: 1, pageSize: 10000 })
    console.log('设备列表数据:', res)
    const devices = res?.list || []
    
    const groupMap = {}
    devices.forEach(device => {
      const groupId = device.groupId || 0
      const groupName = device.groupName || '未分组'
      
      if (!groupMap[groupId]) {
        groupMap[groupId] = {
          groupId,
          groupName,
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
    
    groupStats.value = Object.values(groupMap)
      .sort((a, b) => b.deviceCount - a.deviceCount)
      .slice(0, 5)
  } catch (error) {
    console.error('加载分组统计失败:', error)
    groupStats.value = []
  }
}

// 加载报警统计数据
const loadAlarmStatistics = async () => {
  try {
    const res = await axios.post('/alarm-log/statistics')
    console.log('报警统计数据:', res)
    
    // 加载今日报警数据
    const today = new Date()
    const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0)
    const todayEnd = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59, 999)
    
    const todayRes = await axios.post('/alarm-log/list', {
      page: 1,
      pageSize: 10000,
      startTime: formatDateTime(todayStart),
      endTime: formatDateTime(todayEnd)
    })
    
    const todayAlarms = todayRes?.list || []
    const todayCriticalCount = todayAlarms.filter(a => a.alarmLevel === 'critical').length
    const todayWarningCount = todayAlarms.filter(a => a.alarmLevel === 'warning').length
    const todayUnhandledCount = todayAlarms.filter(a => a.status === 0).length
    
    return {
      total: res?.total || 0,
      unhandled: res?.unhandled || 0,
      critical: res?.critical || 0,
      warning: res?.warning || 0,
      info: res?.info || 0,
      todayAlarmCount: todayAlarms.length,
      todayCriticalCount: todayCriticalCount,
      todayWarningCount: todayWarningCount,
      todayUnhandledCount: todayUnhandledCount
    }
  } catch (error) {
    console.error('加载报警统计失败:', error)
    return { total: 0, unhandled: 0, critical: 0, warning: 0, info: 0, todayAlarmCount: 0, todayCriticalCount: 0, todayWarningCount: 0, todayUnhandledCount: 0 }
  }
}


// 加载最近报警
const loadRecentAlarms = async () => {
  try {
    const res = await axios.post('/alarm-log/list', {
      page: 1,
      pageSize: 5,
      status: 0 // 只显示未处理的
    })
    console.log('最近报警数据:', res)
    recentAlarms.value = res?.list || []
  } catch (error) {
    console.error('加载最近报警失败:', error)
    recentAlarms.value = []
  }
}

// 获取分组百分比
const getGroupPercentage = (count) => {
  if (stats.value.totalDevices === 0) return 0
  return Math.round((count / stats.value.totalDevices) * 100)
}

// 获取告警级别样式
const getAlarmLevelClass = (level) => {
  return `alarm-level-${level || 'info'}`
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'W'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 格式化百分比
const formatPercent = (num) => {
  return num > 0 ? `+${num}%` : `${num}%`
}

// 格式化时间 - 统一使用 yyyy-MM-dd HH:mm:ss 格式
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '-'
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化日期时间 - 用于API请求参数
const formatDateTime = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 导航方法
const goToDevices = (params = {}) => {
  router.push({
    path: '/devices',
    query: params
  })
}

const goToProducts = () => {
  router.push('/products/list')
}

const goToDataQuery = (params = {}) => {
  router.push({
    path: '/data-query',
    query: params
  })
}

const goToAlarms = (params = {}) => {
  router.push({
    path: '/alarms',
    query: params
  })
}

// 跳转到今日报警（今天0点到24点）
const goToAlarmsToday = () => {
  const today = new Date()
  const startTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0)
  const endTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59, 999)
  
  router.push({
    path: '/alarms',
    query: {
      startTime: formatDateTime(startTime),
      endTime: formatDateTime(endTime)
    }
  })
}

// 跳转到今日未处理报警（今天0点到24点 + 未处理状态）
const goToAlarmsTodayUnhandled = () => {
  const today = new Date()
  const startTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0)
  const endTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59, 999)
  
  router.push({
    path: '/alarms',
    query: {
      startTime: formatDateTime(startTime),
      endTime: formatDateTime(endTime),
      status: 0
    }
  })
}

const goToDeviceDetail = (deviceCode) => {
  router.push(`/devices/${deviceCode}`)
}

const goToAlarmDetail = (alarmId) => {
  router.push({
    path: '/alarms',
    query: { alarmId }
  })
}

// 加载所有数据
const loadAllData = async () => {
  await loadStatistics()
  await loadGroupStats()
  await loadRecentAlarms()
  
  // 等待DOM更新后渲染图表
  await nextTick()
  setTimeout(() => {
    loadTodayAlarmTrend()
    loadProductChart()
    loadStatusChart()
  }, 300)
}

// 生命周期
onMounted(() => {
  console.log('Overview 组件已挂载，开始加载数据')
  try {
    loadAllData()
    
    // 每30秒刷新一次
    refreshTimer = setInterval(() => {
      loadStatistics()
      loadGroupStats()
      loadRecentAlarms()
    }, 30000)
  } catch (error) {
    console.error('Overview 组件初始化失败:', error)
    ElMessage.error('页面加载失败: ' + (error.message || '未知错误'))
  }
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  productChartInstance?.dispose()
  statusChartInstance?.dispose()
})
</script>

<style scoped>
/* ============================
   IOT Dashboard - Pro Max Design
   Design System: #3B82F6 primary / #F8FAFC bg / #1E293B text
   Style: Glassmorphism + Clean Professional
   ============================ */

.dashboard-container {
  padding: 24px;
  background: linear-gradient(135deg, #f0f4ff 0%, #f8fafc 40%, #faf5ff 100%);
  min-height: calc(100vh - 100px);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  line-height: 1.6;
}

/* ===== 统计卡片区域 ===== */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-card-no-click {
  cursor: default;
}

.stat-card-no-click:hover {
  transform: none !important;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04) !important;
}

.stat-card-no-click:hover::before {
  transform: scaleX(0) !important;
}

.stat-card::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 100%);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.stat-icon svg {
  width: 28px;
  height: 28px;
}

.stat-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #EF4444;
  color: white;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.4);
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #64748B;
  margin-bottom: 6px;
  font-weight: 500;
  letter-spacing: 0.01em;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1E293B;
  line-height: 1;
  margin-bottom: 6px;
  letter-spacing: -0.02em;
}

.stat-detail {
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.stat-detail .online {
  color: #10B981;
  font-weight: 600;
}

.stat-detail .offline {
  color: #94A3B8;
  font-weight: 500;
}

.stat-detail .critical {
  color: #EF4444;
  font-weight: 600;
}

.stat-detail .warning {
  color: #F59E0B;
  font-weight: 500;
}

/* ===== 图表区域 ===== */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.charts-grid-top {
  grid-template-columns: repeat(2, 1fr);
}

.charts-grid-bottom {
  grid-template-columns: repeat(2, 1fr);
}

/* ===== 信息卡片行：最近报警、最近活跃设备（单独一行，两列等宽） ===== */
.info-cards-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.info-cards-row .info-card {
  height: 360px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.info-cards-row .info-card .alarm-list,
.info-cards-row .info-card .device-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.chart-card {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  height: 360px;
  transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-card:hover {
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.1), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.chart-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-icon {
  color: #3B82F6;
  flex-shrink: 0;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
  letter-spacing: -0.01em;
}

.chart-actions .el-button {
  border-radius: 8px;
  font-size: 12px;
}

.chart-container {
  width: 100%;
  flex: 1;
  min-height: 0;
}

/* ===== 报警趋势柱状图 ===== */
.data-trend-wrapper {
  width: 100%;
  flex: 1;
  min-height: 0;
  padding: 4px 0;
}

.data-trend-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 100%;
  gap: 3px;
  padding-bottom: 4px;
}

.trend-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.trend-bar-bg {
  width: 100%;
  flex: 1;
  min-height: 80px;
  display: flex;
  align-items: flex-end;
  background: linear-gradient(180deg, transparent 0%, rgba(59, 130, 246, 0.03) 100%);
  border-radius: 4px;
}

.trend-bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 4px 4px 0 0;
  min-height: 3px;
  transition: height 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 3px;
  cursor: default;
}

.trend-bar-fill:hover {
  filter: brightness(1.1);
}

.trend-bar-value {
  font-size: 9px;
  color: white;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  line-height: 1;
}

.trend-bar-label {
  font-size: 9px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  line-height: 1;
}

.alarm-trend-bars .trend-bar-fill.alarm-bar {
  background: linear-gradient(180deg, #EF4444 0%, #F59E0B 100%);
}

.data-trend-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 100px;
  color: #CBD5E1;
}

.data-trend-empty p {
  margin-top: 10px;
  font-size: 13px;
  color: #94A3B8;
}

/* ===== 分组统计列表 ===== */
.group-stats-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.group-stats-list .empty-state {
  flex: 1;
  min-height: 100px;
}

.group-stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.group-stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-name {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.group-count {
  font-size: 13px;
  color: #3B82F6;
  font-weight: 600;
}

.group-stat-bar {
  height: 28px;
  background: #F1F5F9;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.group-stat-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 8px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 10px;
  min-width: 100px;
}

.group-stat-bar-info {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: white;
  font-weight: 500;
  white-space: nowrap;
}

/* ===== 底部区域 ===== */
.bottom-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-card {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.info-card:hover {
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.1), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon {
  color: #3B82F6;
  flex-shrink: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
  letter-spacing: -0.01em;
}

/* ===== 告警列表 ===== */
.alarm-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alarm-item {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  background: #F8FAFC;
  border-radius: 12px;
  border-left: 3px solid #F59E0B;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.alarm-item:hover {
  background: #F1F5F9;
  transform: translateX(3px);
}

.alarm-item.alarm-level-critical {
  border-left-color: #EF4444;
  background: rgba(254, 242, 242, 0.6);
}

.alarm-item.alarm-level-critical:hover {
  background: rgba(254, 226, 226, 0.6);
}

.alarm-item.alarm-level-warning {
  border-left-color: #F59E0B;
  background: rgba(255, 251, 235, 0.6);
}

.alarm-item.alarm-level-warning:hover {
  background: rgba(254, 243, 199, 0.6);
}

.alarm-item.alarm-level-info {
  border-left-color: #3B82F6;
  background: rgba(239, 246, 255, 0.6);
}

.alarm-item.alarm-level-info:hover {
  background: rgba(219, 234, 254, 0.6);
}

.alarm-icon-wrapper {
  flex-shrink: 0;
}

.alarm-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.alarm-content {
  flex: 1;
  min-width: 0;
}

.alarm-title {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alarm-desc {
  font-size: 12px;
  color: #64748B;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alarm-time {
  font-size: 11px;
  color: #94A3B8;
}

.alarm-status {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

/* ===== 设备列表 ===== */
.device-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 12px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.device-item:hover {
  background: #F8FAFC;
}

.device-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.device-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
}

.status-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.status-dot.online {
  background: #10B981;
}

.status-dot.offline {
  background: #94A3B8;
}

.device-info {
  flex: 1;
  min-width: 0;
}

.device-name {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-meta {
  font-size: 12px;
  color: #94A3B8;
  display: flex;
  align-items: center;
  gap: 6px;
}

.separator {
  color: #CBD5E1;
}

.device-status {
  flex-shrink: 0;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 48px 20px;
  color: #94A3B8;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  color: #CBD5E1;
  opacity: 0.5;
  margin-bottom: 8px;
}

.empty-state p {
  margin-top: 8px;
  font-size: 13px;
  color: #94A3B8;
}

/* ===== 响应式 ===== */
@media (max-width: 1600px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .bottom-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .charts-grid,
  .charts-grid-top,
  .charts-grid-bottom {
    grid-template-columns: 1fr;
  }

  .chart-card {
    height: 320px;
  }

  .info-cards-row {
    grid-template-columns: 1fr;
  }

  .info-cards-row .info-card {
    height: 320px;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .charts-grid,
  .charts-grid-top,
  .charts-grid-bottom {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .chart-card {
    height: 300px;
  }

  .info-cards-row {
    grid-template-columns: 1fr;
  }

  .info-cards-row .info-card {
    height: 300px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-icon svg {
    width: 24px;
    height: 24px;
  }
}

@media (max-width: 375px) {
  .dashboard-container {
    padding: 12px;
  }
}
</style>
