<template>
  <div class="energy-statistics-page min-h-screen bg-gray-50">
    <!-- 页面头部 -->
    <div class="page-header bg-white shadow-sm mb-6 px-6 py-4">
      <h1 class="text-2xl font-semibold text-gray-900 mb-2">能耗统计</h1>
      <p class="text-sm text-gray-600">支持电、水、气三种能源类型的能耗统计分析</p>
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
              clearable
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

          <el-form-item label="设备">
            <el-select 
              v-model="queryForm.deviceId" 
              placeholder="全部设备" 
              filterable
              clearable
              class="filter-select"
            >
              <el-option 
                v-for="device in deviceList" 
                :key="device.id" 
                :label="device.deviceName" 
                :value="device.id" 
              />
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

      <!-- 统计数据卡片 -->
      <div v-if="statistics.totalConsumption !== undefined" class="statistics-cards mb-6">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card stat-card-total hover:shadow-lg transition-all duration-300 cursor-pointer" shadow="hover">
              <div class="stat-content">
                <div class="stat-header">
                  <div class="stat-icon-wrapper stat-icon-total">
                    <el-icon :size="24"><Lightning /></el-icon>
                  </div>
                  <div class="stat-label text-sm text-gray-600">总能耗</div>
                </div>
                <div class="stat-value text-3xl font-bold text-gray-900 mb-1">
                  {{ formatNumber(statistics.totalConsumption) }}
                </div>
                <div class="stat-unit text-sm text-gray-500">{{ statistics.unit }}</div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card stat-card-avg hover:shadow-lg transition-all duration-300 cursor-pointer" shadow="hover">
              <div class="stat-content">
                <div class="stat-header">
                  <div class="stat-icon-wrapper stat-icon-avg">
                    <el-icon :size="24"><TrendCharts /></el-icon>
                  </div>
                  <div class="stat-label text-sm text-gray-600">平均能耗</div>
                </div>
                <div class="stat-value text-3xl font-bold text-gray-900 mb-1">
                  {{ formatNumber(statistics.avgConsumption) }}
                </div>
                <div class="stat-unit text-sm text-gray-500">{{ statistics.unit }}/天</div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card stat-card-yoy hover:shadow-lg transition-all duration-300 cursor-pointer" shadow="hover">
              <div class="stat-content">
                <div class="stat-header">
                  <div class="stat-icon-wrapper stat-icon-yoy">
                    <el-icon :size="24"><DataAnalysis /></el-icon>
                  </div>
                  <div class="stat-label text-sm text-gray-600">同比增长</div>
                </div>
                <div 
                  class="stat-value text-3xl font-bold mb-1"
                  :class="statistics.yoy >= 0 ? 'text-red-600' : 'text-green-600'"
                >
                  {{ statistics.yoy >= 0 ? '+' : '' }}{{ formatNumber(statistics.yoy) }}%
                </div>
                <div class="stat-unit text-sm text-gray-500">相比去年同期</div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card stat-card-mom hover:shadow-lg transition-all duration-300 cursor-pointer" shadow="hover">
              <div class="stat-content">
                <div class="stat-header">
                  <div class="stat-icon-wrapper stat-icon-mom">
                    <el-icon :size="24"><DataAnalysis /></el-icon>
                  </div>
                  <div class="stat-label text-sm text-gray-600">环比增长</div>
                </div>
                <div 
                  class="stat-value text-3xl font-bold mb-1"
                  :class="statistics.mom >= 0 ? 'text-red-600' : 'text-green-600'"
                >
                  {{ statistics.mom >= 0 ? '+' : '' }}{{ formatNumber(statistics.mom) }}%
                </div>
                <div class="stat-unit text-sm text-gray-500">相比上期</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Top10设备列表和图表 -->
      <div v-if="statistics.top10Devices && statistics.top10Devices.length > 0" class="charts-row">
        <!-- Top10设备列表 -->
        <el-card class="top10-card shadow-sm flex-1" shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <el-icon :size="20" class="text-blue-500"><Monitor /></el-icon>
                <span class="text-lg font-semibold text-gray-900">能耗Top10设备</span>
              </div>
            </div>
          </template>

        <el-table 
          :data="statistics.top10Devices" 
          stripe
          class="top10-table"
          :default-sort="{ prop: 'totalConsumption', order: 'descending' }"
        >
          <el-table-column type="index" label="排名" width="80" align="center">
            <template #default="{ $index }">
              <el-tag 
                :type="$index < 3 ? 'danger' : 'info'"
                effect="plain"
                size="small"
              >
                {{ $index + 1 }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="deviceName" label="设备名称" min-width="180">
            <template #default="{ row }">
              <div class="flex items-center">
                <el-icon class="mr-2 text-gray-400"><Monitor /></el-icon>
                <span>{{ row.deviceName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="deviceCode" label="设备编码" min-width="150" />

          <el-table-column prop="totalConsumption" label="总能耗" width="180" sortable align="right">
            <template #default="{ row }">
              <span class="font-semibold text-gray-900">
                {{ formatNumber(row.totalConsumption) }}
              </span>
              <span class="ml-1 text-sm text-gray-500">{{ row.unit }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="recordCount" label="记录数" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info" effect="plain">
                {{ row.recordCount }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        </el-card>

        <!-- Top10设备柱状图 -->
        <el-card class="chart-card shadow-sm flex-1" shadow="hover">
          <template #header>
            <div class="flex items-center gap-2">
              <el-icon :size="20" class="text-purple-500"><DataAnalysis /></el-icon>
              <span class="text-lg font-semibold text-gray-900">设备能耗分布</span>
            </div>
          </template>
          <div ref="top10ChartRef" class="chart-container" v-loading="chartLoading"></div>
        </el-card>
      </div>

      <!-- 能耗占比图表区域 -->
      <div v-if="statistics.top10Devices && statistics.top10Devices.length > 0" class="charts-row mb-6">
        <!-- 能耗占比饼图 -->
        <el-card class="chart-card shadow-sm flex-1" shadow="hover">
          <template #header>
            <div class="flex items-center gap-2">
              <el-icon :size="20" class="text-pink-500"><TrendCharts /></el-icon>
              <span class="text-lg font-semibold text-gray-900">能耗占比分析</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container" v-loading="chartLoading"></div>
        </el-card>

        <!-- 能耗趋势折线图 -->
        <el-card class="chart-card shadow-sm flex-1" shadow="hover">
          <template #header>
            <div class="flex items-center gap-2">
              <el-icon :size="20" class="text-green-500"><TrendCharts /></el-icon>
              <span class="text-lg font-semibold text-gray-900">能耗趋势分析</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container" v-loading="chartLoading"></div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && hasQueried && statistics.totalConsumption === undefined"
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
import { Search, Refresh, Monitor, List, InfoFilled, Lightning, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getEnergyStatistics } from '@/api/energy'
import { getDeviceList } from '@/api/device'
import GroupTree from '@/components/GroupTree.vue'
import { getGroupTree } from '@/api/group'
import { flattenTree } from '@/utils/tree'

// 加载状态
const loading = ref(false)
const chartLoading = ref(false)
const hasQueried = ref(false) // 是否已查询过

// 图表相关
const top10ChartRef = ref(null)
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let top10ChartInstance = null
let pieChartInstance = null
let lineChartInstance = null

// 查询表单
const queryForm = reactive({
  energyType: 'electric', // 默认电力
  deviceId: null,
  groupId: null
})

// 日期范围
const dateRange = ref([])

// 设备列表
const deviceList = ref([])

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

// 统计数据
const statistics = ref({
  totalConsumption: undefined,
  unit: '',
  avgConsumption: undefined,
  yoy: 0,
  mom: 0,
  top10Devices: []
})

// 加载设备列表
const loadDevices = async () => {
  try {
    const res = await getDeviceList({
      page: 1,
      pageSize: 1000 // 获取所有设备
    })
    if (res && res.list) {
      deviceList.value = res.list
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

// 查询数据
const handleQuery = async () => {
  // 参数验证
  if (!queryForm.energyType) {
    ElMessage.warning('请选择能源类型')
    return
  }

  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  loading.value = true
  hasQueried.value = true
  try {
    const params = {
      energyType: queryForm.energyType,
      startTime: dateRange.value[0],
      endTime: dateRange.value[1],
      deviceId: queryForm.deviceId || null,
      groupId: queryForm.groupId || null
    }

    const res = await getEnergyStatistics(params)
    
    // 更新统计数据
    statistics.value = {
      totalConsumption: res.totalConsumption || 0,
      unit: res.unit || '',
      avgConsumption: res.avgConsumption || 0,
      yoy: res.yoy || 0,
      mom: res.mom || 0,
      top10Devices: res.top10Devices || []
    }
    
    // 渲染图表
    await nextTick()
    renderTop10Chart()
    renderPieChart()
    renderLineChart()
  } catch (error) {
    console.error('查询能耗统计失败:', error)
    ElMessage.error(error.message || '查询失败，请稍后重试')
    
    // 重置统计数据
    statistics.value = {
      totalConsumption: undefined,
      unit: '',
      avgConsumption: undefined,
      yoy: 0,
      mom: 0,
      top10Devices: []
    }
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.energyType = 'electric'
  queryForm.deviceId = null
  queryForm.groupId = null
  dateRange.value = []
  statistics.value = {
    totalConsumption: undefined,
    unit: '',
    avgConsumption: undefined,
    yoy: 0,
    mom: 0,
    top10Devices: []
  }
}

// 格式化数字（保留2位小数）
const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) {
    return '0.00'
  }
  return Number(num).toFixed(2)
}

// 渲染Top10设备柱状图
const renderTop10Chart = () => {
  if (!top10ChartRef.value || !statistics.value.top10Devices || statistics.value.top10Devices.length === 0) {
    return
  }

  // 销毁旧实例
  if (top10ChartInstance) {
    top10ChartInstance.dispose()
    top10ChartInstance = null
  }

  // 创建新实例
  top10ChartInstance = echarts.init(top10ChartRef.value)

  const deviceNames = statistics.value.top10Devices.map(item => item.deviceName).slice(0, 10)
  const consumptionData = statistics.value.top10Devices.map(item => item.totalConsumption).slice(0, 10)
  const unit = statistics.value.unit || ''

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
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
        return `${param.name}<br/>能耗: ${Number(param.value).toFixed(2)} ${unit}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: deviceNames,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: { 
        color: '#6b7280', 
        fontSize: 12,
        rotate: 45,
        interval: 0
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
      type: 'bar',
      data: consumptionData,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: {
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#764ba2' },
              { offset: 1, color: '#667eea' }
            ]
          }
        }
      }
    }]
  }

  top10ChartInstance.setOption(option, true)
}

// 渲染能耗占比饼图
const renderPieChart = () => {
  if (!pieChartRef.value || !statistics.value.top10Devices || statistics.value.top10Devices.length === 0) {
    return
  }

  if (pieChartInstance) {
    pieChartInstance.dispose()
    pieChartInstance = null
  }

  pieChartInstance = echarts.init(pieChartRef.value)

  const top5Devices = statistics.value.top10Devices.slice(0, 5)
  const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe']
  const data = top5Devices.map((item, index) => ({
    value: item.totalConsumption,
    name: item.deviceName,
    itemStyle: { 
      color: colors[index % colors.length],
      borderRadius: 8,
      borderColor: '#fff',
      borderWidth: 2
    }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: 'transparent',
      borderWidth: 0,
      padding: [12, 16],
      textStyle: { 
        color: '#fff',
        fontSize: 13
      },
      formatter: '{b}<br/>{c} {a}<br/>({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: { 
        color: '#6b7280', 
        fontSize: 12 
      },
      itemGap: 12
    },
    series: [{
      name: '能耗',
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 11,
        fontWeight: 500
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 13,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      data: data
    }]
  }

  pieChartInstance.setOption(option, true)
}

// 渲染能耗趋势折线图
const renderLineChart = () => {
  if (!lineChartRef.value || !statistics.value.top10Devices || statistics.value.top10Devices.length === 0) {
    return
  }

  if (lineChartInstance) {
    lineChartInstance.dispose()
    lineChartInstance = null
  }

  lineChartInstance = echarts.init(lineChartRef.value)

  const top5Devices = statistics.value.top10Devices.slice(0, 5)
  const deviceNames = top5Devices.map(item => item.deviceName)
  const consumptionData = top5Devices.map(item => item.totalConsumption)
  const unit = statistics.value.unit || ''

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
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#667eea'
        }
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
      data: deviceNames,
      axisLine: { 
        lineStyle: { color: '#e0e0e0' } 
      },
      axisLabel: { 
        color: '#6b7280', 
        fontSize: 12,
        rotate: 30
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
      symbolSize: 8,
      emphasis: {
        focus: 'series',
        itemStyle: {
          color: '#667eea',
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    }]
  }

  lineChartInstance.setOption(option, true)
}

// 窗口大小变化时调整图表
const handleResize = () => {
  if (top10ChartInstance) {
    top10ChartInstance.resize()
  }
  if (pieChartInstance) {
    pieChartInstance.resize()
  }
  if (lineChartInstance) {
    lineChartInstance.resize()
  }
}

// 监听统计数据变化
watch(() => statistics.value.top10Devices, () => {
  if (statistics.value.top10Devices && statistics.value.top10Devices.length > 0) {
    nextTick(() => {
      renderTop10Chart()
      renderPieChart()
      renderLineChart()
    })
  }
}, { deep: true })

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

// 初始化：设置默认时间范围为最近30天，并自动查询电表数据
onMounted(async () => {
  const endTime = new Date()
  const startTime = new Date()
  startTime.setDate(startTime.getDate() - 30)
  
  // 设置开始时间为当天的00:00:00
  startTime.setHours(0, 0, 0, 0)
  // 设置结束时间为当天的23:59:59
  endTime.setHours(23, 59, 59, 999)
  
  dateRange.value = [
    formatDateTime(startTime),
    formatDateTime(endTime)
  ]

  // 加载设备列表和分组列表
  await Promise.all([loadDevices(), loadGroups()])
  
  // 默认查询电表数据
  await handleQuery()
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (top10ChartInstance) {
    top10ChartInstance.dispose()
    top10ChartInstance = null
  }
  if (pieChartInstance) {
    pieChartInstance.dispose()
    pieChartInstance = null
  }
  if (lineChartInstance) {
    lineChartInstance.dispose()
    lineChartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.energy-statistics-page {
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
  gap: 10px;
  width: 100%;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
  flex-shrink: 0;
  white-space: nowrap;
}

.filter-form :deep(.el-input__wrapper) {
  padding: 0 8px;
  height: 32px;
}

.filter-form :deep(.el-select .el-input__wrapper) {
  padding: 0 8px;
  height: 32px;
}

.filter-form :deep(.el-date-editor) {
  height: 32px;
}

.filter-form :deep(.el-form-item__label) {
  padding-right: 6px;
  font-size: 12px;
  width: auto !important;
  line-height: 32px;
}

.filter-select {
  width: 110px;
}

.filter-datepicker {
  width: 280px;
}

.filter-buttons {
  margin-left: auto;
  flex-shrink: 0;
}

.filter-buttons :deep(.el-button) {
  padding: 7px 14px;
  font-size: 12px;
  white-space: nowrap;
  height: 32px;
}

.statistics-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
}

.stat-content {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-icon-avg {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-icon-yoy {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-icon-mom {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-unit {
  font-size: 13px;
  color: #9ca3af;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}

.top10-card {
  border-radius: 12px;
}

.chart-card {
  border-radius: 12px;
}

.chart-container {
  width: 100%;
  height: 400px;
  min-height: 400px;
}

.top10-table :deep(.el-table__header) {
  background-color: #f9fafb;
}

.top10-table :deep(.el-table__header th) {
  background-color: #f9fafb;
  color: #374151;
  font-weight: 600;
}

.top10-table :deep(.el-table__row:hover) {
  background-color: #f3f4f6;
}
</style>
