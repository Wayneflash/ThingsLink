<template>
  <div class="alarm-analysis-container">
    <!-- 时间范围筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="timeRange" size="default" @change="handleTimeRangeChange">
        <el-radio-button label="today">今天</el-radio-button>
        <el-radio-button label="yesterday">昨天</el-radio-button>
        <el-radio-button label="7days">最近7天</el-radio-button>
        <el-radio-button label="30days">最近30天</el-radio-button>
        <el-radio-button label="custom">自定义</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-if="timeRange === 'custom'"
        v-model="customDateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        format="YYYY-MM-DD HH:mm:ss"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width: 380px; margin-left: 12px;"
        @change="handleCustomDateChange"
      />
      <el-button type="primary" @click="loadAnalysisData" :loading="loading">查询</el-button>
    </div>

    <!-- 效率卡片 -->
    <div class="efficiency-cards">
      <div class="efficiency-card">
        <div class="card-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);">
          🚨
        </div>
        <div class="card-content">
          <div class="card-label">总报警数</div>
          <div class="card-value">{{ efficiency.totalCount || 0 }}</div>
        </div>
      </div>
      <div class="efficiency-card">
        <div class="card-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);">
          ✅
        </div>
        <div class="card-content">
          <div class="card-label">处理率</div>
          <div class="card-value">{{ efficiency.handlingRate || 0 }}%</div>
        </div>
      </div>
      <div class="efficiency-card">
        <div class="card-icon" style="background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);">
          ⏱️
        </div>
        <div class="card-content">
          <div class="card-label">平均处理时间</div>
          <div class="card-value">{{ efficiency.avgHandleTime || 0 }} <span class="unit">分钟</span></div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 报警趋势图 -->
      <div class="chart-card trend-chart">
        <div class="chart-header">
          <span class="chart-title">📈 报警趋势</span>
        </div>
        <div ref="trendChartRef" class="chart-content"></div>
      </div>
      
      <!-- 级别分布图 -->
      <div class="chart-card level-chart">
        <div class="chart-header">
          <span class="chart-title">📊 级别分布</span>
        </div>
        <div ref="levelChartRef" class="chart-content"></div>
      </div>
    </div>

    <!-- 报警统计表格 -->
    <div class="table-section">
      <div class="table-header">
        <span class="table-title">报警统计</span>
        <div class="table-filter">
          <el-select v-model="tableFilter.alarmLevel" placeholder="报警级别" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="严重" value="critical" />
            <el-option label="警告" value="warning" />
            <el-option label="提示" value="info" />
          </el-select>
          <el-select v-model="tableFilter.status" placeholder="处理状态" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
          <el-button type="primary" @click="loadAlarmLogList">查询</el-button>
        </div>
      </div>
      <el-table :data="alarmLogList" stripe v-loading="tableLoading" style="width: 100%" :height="tableHeight">
        <el-table-column prop="triggerTime" label="触发时间" width="180" />
        <el-table-column prop="alarmLevel" label="级别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.alarmLevel === 'critical'" type="danger">严重</el-tag>
            <el-tag v-else-if="row.alarmLevel === 'warning'" type="warning">警告</el-tag>
            <el-tag v-else type="info">提示</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="metric" label="监控指标" width="150" />
        <el-table-column prop="alarmMessage" label="报警消息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">已处理</el-tag>
            <el-tag v-else type="danger">未处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 0" 
              type="primary" 
              size="small" 
              @click="handleAlarm(row)"
            >
              处理
            </el-button>
            <el-button 
              v-else 
              type="info" 
              size="small" 
              @click="viewAlarmDetail(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="alarmLogList.length > 0"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="table-pagination"
        @size-change="loadAlarmLogList"
        @current-change="loadAlarmLogList"
      />
    </div>

    <!-- 处理报警对话框 -->
    <el-dialog
      v-model="handleDialogVisible"
      title="处理报警"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="报警信息">
          <div class="alarm-info">
            <div><strong>触发时间:</strong> {{ currentAlarm?.triggerTime }}</div>
            <div><strong>报警级别:</strong>
              <el-tag v-if="currentAlarm?.alarmLevel === 'critical'" type="danger" size="small">严重</el-tag>
              <el-tag v-else-if="currentAlarm?.alarmLevel === 'warning'" type="warning" size="small">警告</el-tag>
              <el-tag v-else type="info" size="small">提示</el-tag>
            </div>
            <div><strong>监控指标:</strong> {{ currentAlarm?.metric }}</div>
            <div><strong>报警消息:</strong> {{ currentAlarm?.alarmMessage }}</div>
          </div>
        </el-form-item>
        <el-form-item label="处理描述">
          <el-input
            v-model="handleForm.handleDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入处理描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handleLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看报警详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="报警详情"
      width="600px"
    >
      <div v-if="currentAlarm" class="alarm-detail">
        <div class="detail-row">
          <span class="detail-label">触发时间:</span>
          <span class="detail-value">{{ currentAlarm.triggerTime }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报警级别:</span>
          <span class="detail-value">
            <el-tag v-if="currentAlarm.alarmLevel === 'critical'" type="danger">严重</el-tag>
            <el-tag v-else-if="currentAlarm.alarmLevel === 'warning'" type="warning">警告</el-tag>
            <el-tag v-else type="info">提示</el-tag>
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">监控指标:</span>
          <span class="detail-value">{{ currentAlarm.metric }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报警消息:</span>
          <span class="detail-value">{{ currentAlarm.alarmMessage }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">处理状态:</span>
          <span class="detail-value">
            <el-tag v-if="currentAlarm.status === 1" type="success">已处理</el-tag>
            <el-tag v-else type="danger">未处理</el-tag>
          </span>
        </div>
        <div v-if="currentAlarm.status === 1" class="detail-row">
          <span class="detail-label">处理人:</span>
          <span class="detail-value">{{ currentAlarm.handler }}</span>
        </div>
        <div v-if="currentAlarm.status === 1" class="detail-row">
          <span class="detail-label">处理时间:</span>
          <span class="detail-value">{{ currentAlarm.handleTime }}</span>
        </div>
        <div v-if="currentAlarm.status === 1 && currentAlarm.handleDescription" class="detail-row">
          <span class="detail-label">处理描述:</span>
          <span class="detail-value">{{ currentAlarm.handleDescription }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlarmAnalysis, getAlarmLogList, handleAlarm as handleAlarmAPI } from '@/api/alarm'
import * as echarts from 'echarts'

// Props
const props = defineProps({
  deviceCode: {
    type: String,
    required: true
  }
})

// 时间范围
const timeRange = ref('7days')
const customDateRange = ref([])

// 加载状态
const loading = ref(false)
const tableLoading = ref(false)
const handleLoading = ref(false)

// 效率数据
const efficiency = reactive({
  totalCount: 0,
  handledCount: 0,
  handlingRate: 0,
  avgHandleTime: 0
})

// 图表实例
const trendChartRef = ref(null)
const levelChartRef = ref(null)
let trendChartInstance = null
let levelChartInstance = null

// 报警日志列表
const alarmLogList = ref([])
const tableFilter = reactive({
  alarmLevel: '',
  status: ''
})
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 表格高度
const tableHeight = computed(() => {
  return 'calc(100vh - 580px)'
})

// 处理报警对话框
const handleDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentAlarm = ref(null)
const handleForm = reactive({
  handleDescription: ''
})

// 加载报警分析数据
const loadAnalysisData = async () => {
  loading.value = true
  try {
    const params = {
      deviceCodes: [props.deviceCode],
      timeRange: timeRange.value
    }
    
    if (timeRange.value === 'custom' && customDateRange.value?.length === 2) {
      params.startTime = customDateRange.value[0]
      params.endTime = customDateRange.value[1]
    }
    
    const data = await getAlarmAnalysis(params)
    if (data) {
      // 更新效率数据
      Object.assign(efficiency, data.efficiency || {})
      
      // 渲染图表
      nextTick(() => {
        renderTrendChart(data.trend || {})
        renderLevelChart(data.levelDistribution || {})
      })
    }
  } catch (error) {
    console.error('加载报警分析数据失败:', error)
    ElMessage.error('加载报警分析数据失败')
  } finally {
    loading.value = false
  }
}

// 渲染报警趋势图
const renderTrendChart = (trendData) => {
  if (!trendChartRef.value) return
  
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
  
  trendChartInstance = echarts.init(trendChartRef.value)
  
  const dates = trendData.dates || []
  const counts = trendData.counts || []
  const levels = trendData.levels || { critical: [], warning: [], info: [] }
  
  const option = {
    title: {
      text: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ddd',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#333',
        fontSize: 12
      }
    },
    legend: {
      data: ['总数', '严重', '警告', '提示'],
      top: 10,
      textStyle: {
        fontSize: 11
      }
    },
    grid: {
      left: 50,
      right: 30,
      bottom: 40,
      top: 40,
      containLabel: false
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666',
        fontSize: 11,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: true,
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: '#e5e5e5',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '总数',
        type: 'line',
        data: counts,
        smooth: true,
        itemStyle: { color: '#409eff' },
        lineStyle: { width: 2 }
      },
      {
        name: '严重',
        type: 'line',
        data: levels.critical || [],
        smooth: true,
        itemStyle: { color: '#f56c6c' },
        lineStyle: { width: 2 }
      },
      {
        name: '警告',
        type: 'line',
        data: levels.warning || [],
        smooth: true,
        itemStyle: { color: '#e6a23c' },
        lineStyle: { width: 2 }
      },
      {
        name: '提示',
        type: 'line',
        data: levels.info || [],
        smooth: true,
        itemStyle: { color: '#909399' },
        lineStyle: { width: 2 }
      }
    ]
  }
  
  trendChartInstance.setOption(option)
}

// 渲染级别分布图
const renderLevelChart = (levelData) => {
  if (!levelChartRef.value) return
  
  if (levelChartInstance) {
    levelChartInstance.dispose()
  }
  
  levelChartInstance = echarts.init(levelChartRef.value)
  
  const data = [
    { value: levelData.critical || 0, name: '严重', itemStyle: { color: '#f56c6c' } },
    { value: levelData.warning || 0, name: '警告', itemStyle: { color: '#e6a23c' } },
    { value: levelData.info || 0, name: '提示', itemStyle: { color: '#909399' } }
  ]
  
  const option = {
    title: {
      text: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ddd',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#333',
        fontSize: 12
      },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        fontSize: 11
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  levelChartInstance.setOption(option)
}

// 加载报警日志列表
const loadAlarmLogList = async () => {
  tableLoading.value = true
  try {
    const res = await getAlarmLogList({
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      deviceCode: props.deviceCode,
      alarmLevel: tableFilter.alarmLevel,
      status: tableFilter.status
    })
    alarmLogList.value = res?.list || []
    pagination.total = res?.total || 0
  } catch (error) {
    console.error('加载报警日志失败:', error)
    ElMessage.error('加载报警日志失败')
  } finally {
    tableLoading.value = false
  }
}

// 处理报警
const handleAlarm = (row) => {
  currentAlarm.value = row
  handleForm.handleDescription = ''
  handleDialogVisible.value = true
}

// 提交处理
const submitHandle = async () => {
  if (!handleForm.handleDescription.trim()) {
    ElMessage.warning('请输入处理描述')
    return
  }
  
  handleLoading.value = true
  try {
    await handleAlarmAPI({
      alarmId: currentAlarm.value.id,
      handleDescription: handleForm.handleDescription
    })
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
    loadAlarmLogList()
    loadAnalysisData()
  } catch (error) {
    console.error('处理报警失败:', error)
    ElMessage.error('处理报警失败')
  } finally {
    handleLoading.value = false
  }
}

// 查看报警详情
const viewAlarmDetail = (row) => {
  currentAlarm.value = row
  detailDialogVisible.value = true
}

// 时间范围变化
const handleTimeRangeChange = () => {
  if (timeRange.value !== 'custom') {
    customDateRange.value = []
    loadAnalysisData()
  }
}

// 自定义日期变化
const handleCustomDateChange = () => {
  if (customDateRange.value?.length === 2) {
    loadAnalysisData()
  }
}

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', () => {
    if (trendChartInstance) {
      trendChartInstance.resize()
    }
    if (levelChartInstance) {
      levelChartInstance.resize()
    }
  })
  
  // 初始加载数据
  loadAnalysisData()
  loadAlarmLogList()
})
</script>

<style scoped>
.alarm-analysis-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 2px 0 0;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

/* 筛选栏 */
.filter-bar {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

/* 效率卡片 */
.efficiency-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  flex-shrink: 0;
}

.efficiency-card {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

.efficiency-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-content {
  flex: 1;
}

.card-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 6px;
  font-weight: 500;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.02em;
}

.unit {
  font-size: 14px;
  color: #86868b;
  font-weight: 500;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  flex-shrink: 0;
}

.chart-card {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
}

.chart-header {
  margin-bottom: 12px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.chart-content {
  flex: 1;
  min-height: 280px;
}

/* 表格区域 */
.table-section {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.table-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.table-filter {
  display: flex;
  gap: 8px;
}

.table-pagination {
  padding: 8px 0;
  flex-shrink: 0;
}

/* 报警信息 */
.alarm-info {
  background: #f5f5f7;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
}

.alarm-info div {
  margin-bottom: 6px;
}

.alarm-info div:last-child {
  margin-bottom: 0;
}

/* 报警详情 */
.alarm-detail {
  padding: 8px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-label {
  width: 100px;
  font-weight: 600;
  color: #86868b;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #1d1d1f;
}
</style>
