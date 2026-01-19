<template>
  <div class="energy-trend-page min-h-screen bg-gray-50">
    <!-- 页面头部 -->
    <div class="page-header bg-white shadow-sm mb-6 px-6 py-4">
      <h1 class="text-2xl font-semibold text-gray-900 mb-2">能耗趋势</h1>
      <p class="text-sm text-gray-600">支持电、水、气三种能源类型的能耗趋势分析</p>
    </div>

    <div class="energy-container">
      <!-- 左侧分组树 -->
      <el-card class="tree-panel" shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <strong style="font-size: 15px;">设备分组</strong>
          </div>
        </template>
        
        <!-- 全部分组按钮 -->
        <div 
          class="all-groups-btn"
          :class="{ active: queryForm.groupId === null }"
          @click="selectAllGroups"
        >
          <div class="btn-left">
            <el-icon><List /></el-icon>
            <span>全部分组</span>
          </div>
        </div>
        
        <!-- 分组树 -->
        <GroupTree 
          :groups="groups" 
          :current-group-id="queryForm.groupId"
          :show-actions="false"
          :show-count="false"
          @select="selectGroup"
        />
        
        <div class="tree-hint">
          <el-icon :size="14" class="hint-icon"><InfoFilled /></el-icon>
          <span>点击分组查看该分组下的能耗数据</span>
        </div>
      </el-card>

      <!-- 右侧内容区域 -->
      <div class="content-panel">
      <!-- 查询条件卡片 -->
      <el-card class="filter-card mb-6 shadow-sm" shadow="hover">
        <el-form :model="queryForm" inline class="filter-form">
          <el-form-item label="能源类型">
            <el-select 
              v-model="queryForm.energyType" 
              placeholder="能源类型" 
              class="filter-select"
              @change="handleQuery"
            >
              <el-option label="电力" value="electric" />
              <el-option label="水耗" value="water" />
              <el-option label="气耗" value="gas" />
            </el-select>
          </el-form-item>

          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="filter-datepicker"
            />
          </el-form-item>

          <el-form-item label="时间粒度">
            <el-select 
              v-model="queryForm.dateFormat" 
              placeholder="粒度" 
              class="filter-select-small"
              @change="handleQuery"
            >
              <el-option label="按天" value="day" />
              <el-option label="按小时" value="hour" />
            </el-select>
          </el-form-item>

          <el-form-item class="filter-buttons">
            <el-button 
              type="primary" 
              @click="handleQuery"
              :icon="Search"
              :loading="loading"
              size="default"
            >
              查询
            </el-button>
            <el-button @click="resetQuery" :icon="Refresh" size="default">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 趋势图表卡片 -->
      <el-card 
        v-if="trendData.length > 0"
        class="chart-card shadow-sm mb-6"
        shadow="hover"
      >
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-semibold text-gray-900">
              {{ getEnergyTypeName(queryForm.energyType) }}趋势分析
            </span>
            <div class="flex items-center gap-2">
              <el-tag size="small" type="info">{{ getUnit(queryForm.energyType) }}</el-tag>
            </div>
          </div>
        </template>

        <div ref="trendChartRef" class="chart-container" v-loading="chartLoading"></div>
      </el-card>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && hasQueried && trendData.length === 0"
        description="暂无数据"
        :image-size="120"
      />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, List, InfoFilled } from '@element-plus/icons-vue'
import { getEnergyTrend } from '@/api/energy'
import GroupTree from '@/components/GroupTree.vue'
import { getGroupTree } from '@/api/group'
import { flattenTree } from '@/utils/tree'
import * as echarts from 'echarts'

// 加载状态
const loading = ref(false)
const chartLoading = ref(false)
const hasQueried = ref(false) // 是否已查询过

// 查询表单
const queryForm = reactive({
  energyType: 'electric', // 默认电力
  deviceId: null,
  groupId: null,
  dateFormat: 'day' // 默认按天
})

// 分组列表
const groups = ref([])

// 选择分组
const selectGroup = (groupId) => {
  queryForm.groupId = groupId
  handleQuery()
}

// 选择全部分组
const selectAllGroups = () => {
  queryForm.groupId = null
  handleQuery()
}

// 加载分组列表
const loadGroups = async () => {
  try {
    const result = await getGroupTree()
    if (result.tree) {
      groups.value = flattenTree(result.tree)
    }
  } catch (error) {
    console.error('加载分组列表失败:', error)
  }
}

// 日期范围
const dateRange = ref([])

// 趋势数据
const trendData = ref([])

// 图表相关
const trendChartRef = ref(null)
let trendChartInstance = null

// 获取能源类型名称
const getEnergyTypeName = (type) => {
  const map = {
    electric: '电力',
    water: '水耗',
    gas: '气耗'
  }
  return map[type] || '能源'
}

// 获取单位
const getUnit = (type) => {
  const map = {
    electric: 'kWh',
    water: 'm³',
    gas: 'm³'
  }
  return map[type] || ''
}

// 查询数据
const handleQuery = async () => {
  if (!queryForm.energyType) {
    ElMessage.warning('请选择能源类型')
    return
  }

  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  loading.value = true
  chartLoading.value = true
  hasQueried.value = true
  try {
    const params = {
      energyType: queryForm.energyType,
      startTime: dateRange.value[0],
      endTime: dateRange.value[1],
      groupId: queryForm.groupId || null,
      dateFormat: queryForm.dateFormat || 'day'
    }

    const res = await getEnergyTrend(params)
    trendData.value = res || []

    // 渲染图表
    await nextTick()
    renderChart()
  } catch (error) {
    console.error('查询能耗趋势失败:', error)
    ElMessage.error(error.message || '查询失败，请稍后重试')
    trendData.value = []
  } finally {
    loading.value = false
    chartLoading.value = false
  }
}

// 渲染图表
const renderChart = () => {
  if (!trendChartRef.value || trendData.value.length === 0) {
    return
  }

  // 销毁旧实例
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }

  // 创建新实例
  trendChartInstance = echarts.init(trendChartRef.value)

  const timeLabels = trendData.value.map(item => item.timeLabel)
  const consumptionData = trendData.value.map(item => item.consumption)
  const unit = trendData.value[0]?.unit || getUnit(queryForm.energyType)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: 'transparent',
      borderWidth: 0,
      padding: [12, 16],
      textStyle: { 
        color: '#fff',
        fontSize: 13
      },
      formatter: (params) => {
        const param = params[0]
        return `${param.axisValue}<br/>${param.seriesName}: ${Number(param.value).toFixed(2)} ${unit}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timeLabels,
      axisLine: { 
        lineStyle: { color: '#e0e0e0' } 
      },
      axisLabel: { 
        color: '#6b7280', 
        fontSize: 12,
        rotate: queryForm.dateFormat === 'day' ? 0 : 45
      },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      name: `能耗 (${unit})`,
      nameTextStyle: {
        color: '#6b7280',
        fontSize: 12
      },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { 
        color: '#6b7280', 
        fontSize: 12 
      },
      splitLine: { 
        lineStyle: { 
          color: '#f0f0f0', 
          type: 'dashed' 
        } 
      }
    },
    series: [{
      name: '能耗',
      type: 'line',
      smooth: true,
      data: consumptionData,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ]
        }
      },
      lineStyle: { 
        color: '#667eea', 
        width: 3 
      },
      itemStyle: { 
        color: '#667eea' 
      },
      symbol: 'circle',
      symbolSize: 6,
      emphasis: {
        focus: 'series',
        itemStyle: {
          color: '#667eea',
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    }],
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        type: 'slider',
        start: 0,
        end: 100,
        height: 24,
        bottom: 8,
        borderColor: '#e0e0e0',
        fillerColor: 'rgba(102, 126, 234, 0.2)',
        handleStyle: {
          color: '#667eea'
        }
      }
    ]
  }

  trendChartInstance.setOption(option, true)
}

// 重置查询
const resetQuery = () => {
  queryForm.energyType = 'electric'
  queryForm.groupId = null
  queryForm.dateFormat = 'day'
  dateRange.value = []
  trendData.value = []
  
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }
}

// 格式化日期为 yyyy-MM-dd HH:mm:ss
const formatDateTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 窗口大小变化时调整图表
const handleResize = () => {
  if (trendChartInstance) {
    trendChartInstance.resize()
  }
}

// 监听数据变化
watch(() => trendData.value, () => {
  if (trendData.value.length > 0) {
    nextTick(() => {
      renderChart()
    })
  }
}, { deep: true })

// 初始化：设置默认时间范围为最近30天，并自动查询电表数据
onMounted(async () => {
  const endTime = new Date()
  const startTime = new Date()
  startTime.setDate(startTime.getDate() - 30)
  
  startTime.setHours(0, 0, 0, 0)
  endTime.setHours(23, 59, 59, 999)
  
  dateRange.value = [
    formatDateTime(startTime),
    formatDateTime(endTime)
  ]

  await loadGroups()
  await handleQuery()
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.energy-trend-page {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.page-header {
  border-bottom: 1px solid #e5e7eb;
}

.energy-container {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 12px;
  padding: 0 12px;
  height: calc(100vh - 110px);
}

.tree-panel {
  background: white;
  border-radius: 12px;
  border: 0.5px solid rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}

.all-groups-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
  color: #303133;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  user-select: none;
}

.all-groups-btn:hover {
  background: #e0f2fe;
  border-color: #93c5fd;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.all-groups-btn.active {
  background: linear-gradient(90deg, #3b82f6 0%, #60a5fa 100%);
  color: white;
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.all-groups-btn .btn-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.tree-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 8px;
  padding: 6px 8px;
  background: #f5f7fa;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1.4;
}

.hint-icon {
  color: #667eea;
  flex-shrink: 0;
}

.filter-card {
  border-radius: 12px;
}

.filter-form {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
  flex-shrink: 0;
}

.filter-form :deep(.el-form-item__label) {
  padding-right: 6px;
  font-size: 12px;
  width: auto !important;
}

.filter-select {
  width: 110px;
}

.filter-select-small {
  width: 90px;
}

.filter-datepicker {
  width: 280px;
}

.filter-buttons {
  margin-left: auto;
  flex-shrink: 0;
}

.filter-buttons :deep(.el-button) {
  padding: 8px 16px;
  font-size: 13px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.chart-card {
  border-radius: 12px;
}

.chart-container {
  width: 100%;
  height: 500px;
  min-height: 500px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 400px;
    min-height: 400px;
  }
}
</style>
