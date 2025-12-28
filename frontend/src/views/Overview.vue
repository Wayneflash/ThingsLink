<template>
  <div class="overview-page">
    <div class="page-header">
      <h1 class="page-title">设备概览</h1>
      <el-button type="primary" @click="refreshData" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon total">
            <el-icon :size="28"><Monitor /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalDevices || 0 }}</div>
            <div class="stat-label">设备总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon online">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.onlineDevices || 0 }}</div>
            <div class="stat-label">在线设备</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon offline">
            <el-icon :size="28"><CircleClose /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ (stats.totalDevices || 0) - (stats.onlineDevices || 0) }}</div>
            <div class="stat-label">离线设备</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon data">
            <el-icon :size="28"><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayDataPoints || 0 }}</div>
            <div class="stat-label">今日数据量</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">数据趋势（最近24小时）</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container" v-loading="chartLoading"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">设备分组统计</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Refresh, Monitor, CircleCheck, CircleClose, DataLine } from '@element-plus/icons-vue'
import { getOverviewStats, getDeviceStats, getDataTrend } from '@/api/statistics'

const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let pieChart = null

// 加载状态
const loading = ref(false)
const chartLoading = ref(false)

// 统计数据
const stats = ref({
  totalDevices: 0,
  onlineDevices: 0,
  todayDataPoints: 0
})

// 初始化折线图
const initLineChart = () => {
  if (!lineChartRef.value) return

  lineChart = echarts.init(lineChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e5e7',
      borderWidth: 1,
      textStyle: { color: '#1d1d1f' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: '#e5e5e7' } },
      axisLabel: { color: '#86868b' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#e5e5e7' } },
      axisLabel: { color: '#86868b' },
      splitLine: { lineStyle: { color: '#f5f5f7', type: 'dashed' } }
    },
    series: [{
      name: '数据量',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: [],
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
        ])
      },
      lineStyle: { color: '#667eea', width: 2 },
      itemStyle: { color: '#667eea' }
    }]
  }

  lineChart.setOption(option)
}

// 更新折线图数据
const updateLineChart = (trendData) => {
  if (!lineChart) return

  const timestamps = trendData.map(item => {
    const date = new Date(item.timestamp)
    return `${date.getHours()}:00`
  })
  const values = trendData.map(item => item.value)

  lineChart.setOption({
    xAxis: { data: timestamps },
    series: [{ data: values }]
  })
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e5e7',
      borderWidth: 1,
      textStyle: { color: '#1d1d1f' },
      formatter: '{b}: {c}台 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center',
      textStyle: { color: '#1d1d1f' }
    },
    series: [{
      name: '设备分组',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      center: ['35%', '50%'],
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 18,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }]
  }

  pieChart.setOption(option)
}

// 更新饼图数据
const updatePieChart = (deviceStats) => {
  if (!pieChart) return

  // 颜色配置
  const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe']

  const data = deviceStats.map((item, index) => ({
    value: item.total,
    name: item.groupName || `分组${index + 1}`,
    itemStyle: { color: colors[index % colors.length] }
  }))

  pieChart.setOption({
    series: [{ data }]
  })
}

// 加载统计数据
const loadStats = async () => {
  try {
    const result = await getOverviewStats()
    if (result.code === 200 && result.data) {
      stats.value = {
        totalDevices: result.data.totalDevices || 0,
        onlineDevices: result.data.onlineDevices || 0,
        todayDataPoints: result.data.todayDataPoints || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载设备统计（饼图）
const loadDeviceStats = async () => {
  try {
    const result = await getDeviceStats()
    if (result.code === 200 && result.data && result.data.byGroup) {
      updatePieChart(result.data.byGroup)
    }
  } catch (error) {
    console.error('加载设备统计失败:', error)
  }
}

// 加载数据趋势（折线图）
const loadDataTrend = async () => {
  try {
    // 默认查询最近24小时的趋势
    const now = new Date()
    const startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000)
    const endTime = now

    const result = await getDataTrend({
      startTime: formatTime(startTime),
      endTime: formatTime(endTime),
      interval: 'hour'
    })

    if (result.code === 200 && result.data && result.data.trend) {
      updateLineChart(result.data.trend)
    }
  } catch (error) {
    console.error('加载数据趋势失败:', error)
  }
}

// 格式化时间
const formatTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 刷新所有数据
const refreshData = async () => {
  loading.value = true
  chartLoading.value = true
  try {
    await Promise.all([
      loadStats(),
      loadDeviceStats(),
      loadDataTrend()
    ])
    ElMessage.success('数据已刷新')
  } catch (error) {
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    loading.value = false
    chartLoading.value = false
  }
}

onMounted(async () => {
  // 初始化图表
  setTimeout(() => {
    initLineChart()
    initPieChart()
  }, 100)

  // 加载数据
  await refreshData()

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    lineChart?.resize()
    pieChart?.resize()
  })
})

onUnmounted(() => {
  lineChart?.dispose()
  pieChart?.dispose()
  window.removeEventListener('resize', () => {})
})
</script>

<style scoped>
.overview-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  transition: all 0.3s;
  border-radius: 12px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.online {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.offline {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
}

.stat-icon.data {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #86868b;
  font-weight: 500;
}

.chart-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.chart-container {
  width: 100%;
  height: 320px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .overview-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-value {
    font-size: 24px;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-card__body) {
  padding: 24px;
}
</style>
