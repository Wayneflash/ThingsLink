<template>
  <div class="device-detail-page">
    <!-- 顶部导航 -->
    <div class="topbar">
      <div class="breadcrumb">
        <span @click="goBack" class="breadcrumb-link">设备管理</span>
        <span class="breadcrumb-sep">/</span>
        <span class="breadcrumb-current">设备详情</span>
      </div>
      <el-button type="primary" @click="goBack" size="default" class="back-btn">
        <el-icon style="margin-right: 6px;"><ArrowLeft /></el-icon>
        返回设备列表
      </el-button>
    </div>

    <!-- 设备信息区域：双列布局 -->
    <div v-if="activeTab !== 'history'" class="header-section">
      <!-- 左侧：基本信息 -->
      <div class="header-left">
        <div class="device-title-bar-compact">
          <div :class="['device-icon-large', deviceInfo.status === 1 ? 'icon-online' : 'icon-offline']">📱</div>
          <div class="device-title-text">
            <div class="title-row">
              <div class="device-name-wrapper">
                <h1 class="device-name-large">{{ deviceInfo.deviceName || '-' }}</h1>
                <div class="device-code-badge">{{ deviceInfo.deviceCode || '-' }}</div>
              </div>
              <div :class="['status-badge', deviceInfo.status === 1 ? 'status-online' : 'status-offline']">
                <span class="status-dot"></span>
                <span>{{ deviceInfo.status === 1 ? '在线' : '离线' }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="info-grid-compact">
          <div class="info-card-compact">
            <div class="info-label">产品类型</div>
            <div class="info-value">{{ deviceInfo.productName || '-' }}</div>
          </div>
          <div class="info-card-compact">
            <div class="info-label">所属分组</div>
            <div class="info-value">{{ deviceInfo.groupName || '-' }}</div>
          </div>
          <div class="info-card-compact">
            <div class="info-label">最后上线</div>
            <div class="info-value">{{ formatDateTime(deviceInfo.lastOnlineTime) || '-' }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧：MQTT连接信息 -->
      <div class="header-right">
        <div class="info-section-title">
          <span class="section-icon">📡</span>
          MQTT连接信息
        </div>
        <div class="mqtt-card-compact">
          <div class="mqtt-line">
            <span class="mqtt-label">地址:</span>
            <span class="mqtt-value">{{ mqttConfig.broker || '-' }}</span>
            <span class="mqtt-sep">|</span>
            <span class="mqtt-label">端口:</span>
            <span class="mqtt-value">{{ mqttConfig.port || '-' }}</span>
          </div>
          <div class="mqtt-line">
            <span class="mqtt-label">Client ID:</span>
            <code class="mqtt-code-inline">{{ deviceInfo.deviceCode || '-' }}</code>
            <el-button text class="copy-btn" @click="copyToClipboard(deviceInfo.deviceCode, 'Client ID')" size="small">
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
          </div>
          <div class="mqtt-line">
            <span class="mqtt-label">发布主题:</span>
            <code class="mqtt-code-inline">ssc/{{ deviceInfo.deviceCode || '-' }}/report</code>
            <el-button text class="copy-btn" @click="copyToClipboard(`ssc/${deviceInfo.deviceCode}/report`, '发布主题')" size="small">
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
          </div>
          <div class="mqtt-line">
            <span class="mqtt-label">订阅主题:</span>
            <code class="mqtt-code-inline">ssc/{{ deviceInfo.deviceCode || '-' }}/command</code>
            <el-button text class="copy-btn" @click="copyToClipboard(`ssc/${deviceInfo.deviceCode}/command`, '订阅主题')" size="small">
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab标签页 -->
    <div class="tabs">
      <button 
        class="tab" 
        :class="{ active: activeTab === 'realtime' }"
        @click="switchTab('realtime')"
      >
        实时数据
      </button>
      <button 
        class="tab" 
        :class="{ active: activeTab === 'history' }"
        @click="switchTab('history')"
      >
        历史数据
      </button>
      <button 
        class="tab" 
        :class="{ active: activeTab === 'command' }"
        @click="switchTab('command')"
      >
        命令控制
      </button>
    </div>

    <!-- 实时数据 Tab -->
    <div class="tab-content" :class="{ active: activeTab === 'realtime' }">
      <div v-if="realtimeData.length > 0" class="data-grid-wrapper">
        <div class="data-grid">
          <div 
            v-for="item in realtimeData" 
            :key="item.key" 
            class="data-card"
          >
            <div class="data-label">{{ item.label }}</div>
            <div>
              <span class="data-value">{{ item.value }}</span>
              <span v-if="item.unit" class="data-unit">{{ item.unit }}</span>
            </div>
            <div class="data-time">📅 {{ updateTime }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-data">
        <div class="empty-icon">📭</div>
        <div>暂无实时数据</div>
      </div>
    </div>

    <!-- 历史数据 Tab -->
    <div class="tab-content" :class="{ active: activeTab === 'history' }">
      <div class="history-toolbar">
        <el-radio-group v-model="historyViewMode" size="small">
          <el-radio-button label="chart">📈 趋势图</el-radio-button>
          <el-radio-button label="table">📊 表格</el-radio-button>
        </el-radio-group>
        <div style="flex: 1; display: flex; gap: 12px; justify-content: flex-end;">
          <el-date-picker
            v-model="historyDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 400px;"
          />
          <el-button type="primary" @click="queryHistory">查询</el-button>
        </div>
      </div>
          
      <!-- 图表视图 -->
      <div v-show="historyViewMode === 'chart'" class="history-chart-container">
        <div ref="historyChartRef" class="history-chart"></div>
        <div v-if="historyData.length === 0" class="empty-data">
          <div class="empty-icon">📊</div>
          <div>暂无历史数据</div>
        </div>
      </div>
      
      <!-- 表格视图 -->
      <div v-show="historyViewMode === 'table'" class="history-table-wrapper">
        <div class="history-table-container">
          <el-table :data="paginatedHistoryData" stripe style="width: 100%" :height="tableHeight">
            <el-table-column prop="reportTime" label="上报时间" width="220" fixed />
            <el-table-column 
              v-for="attr in productAttributes" 
              :key="attr.addr" 
              :label="attr.attrName"
              min-width="100"
            >
              <template #default="{ row }">
                <span v-if="row[attr.addr] !== undefined && row[attr.addr] !== null">
                  {{ row[attr.addr] }}
                  <span v-if="attr.unit" class="unit-text">{{ attr.unit }}</span>
                </span>
                <span v-else style="color: #ccc;">-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="historyData.length > 0"
            v-model:current-page="historyPagination.currentPage"
            v-model:page-size="historyPagination.pageSize"
            :page-sizes="[20, 50, 100, 200]"
            :total="historyPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            class="history-pagination"
          />
          <div v-if="historyData.length === 0" class="empty-data">
            <div class="empty-icon">📭</div>
            <div>暂无历史数据</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 命令控制 Tab -->
    <div class="tab-content" :class="{ active: activeTab === 'command' }">
        <div class="command-grid">
          <button
            v-for="cmd in productCommands"
            :key="cmd.id"
            class="command-btn"
            @click="sendCommand(cmd)"
          >
            <div class="command-icon">⚡</div>
            <div class="command-name">{{ cmd.commandName }}</div>
          </button>
          <div v-if="productCommands.length === 0" class="empty-data">
            <div class="empty-icon">🎮</div>
            <div>暂无可用命令</div>
          </div>
        </div>
      </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, DocumentCopy } from '@element-plus/icons-vue'
import { getDeviceDetail, getDeviceLatestData } from '@/api/device'
import { getProductAttributes, getProductCommands } from '@/api/product'
import { getHistoryData } from '@/api/data'
import { sendCommand as sendCommandAPI } from '@/api/command'
import { getMqttConfig } from '@/api/system'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

// 设备信息
const deviceInfo = reactive({
  id: null,
  deviceName: '',
  deviceCode: '',
  productId: null,
  productName: '',
  groupId: null,
  groupName: '',
  groupPath: '',
  status: 0,
  lastOnlineTime: '',
  createTime: ''
})

// 产品属性和命令
const productAttributes = ref([])
const productCommands = ref([])

// 实时数据
const realtimeData = ref([])
const updateTime = ref('-')

// 历史数据
const historyViewMode = ref('chart') // 默认显示趋势图
const historyDateRange = ref([])
const historyData = ref([])
const historyChartRef = ref(null)
let historyChartInstance = null
const historyPagination = reactive({
  currentPage: 1,
  pageSize: 50,
  total: 0
})

// 当前激活的Tab
const activeTab = ref('realtime')

// MQTT配置信息
const mqttConfig = reactive({
  broker: '',
  port: '',
  username: '',
  password: ''
})
const showPassword = ref(false)

// 加载MQTT配置
const loadMqttConfig = async () => {
  try {
    const config = await getMqttConfig()
    if (config) {
      Object.assign(mqttConfig, config)
    }
  } catch (error) {
    console.error('加载MQTT配置失败:', error)
    // 设置默认值
    mqttConfig.broker = 'localhost'
    mqttConfig.port = '1883'
    mqttConfig.username = 'admin'
    mqttConfig.password = 'admin123.'
  }
}

// 加载设备详情
const loadDeviceDetail = async () => {
  try {
    const deviceCode = route.query.deviceCode
    if (!deviceCode) {
      ElMessage.error('设备编码不存在')
      goBack()
      return
    }

    // 加载MQTT配置
    await loadMqttConfig()
    
    // 只加载设备基本信息
    const data = await getDeviceDetail({ deviceCode })
    if (data) {
      Object.assign(deviceInfo, data)
      // 加载实时数据
      loadRealtimeData()
    } else {
      ElMessage.error('获取设备详情失败')
      goBack()
    }
  } catch (error) {
    console.error('加载设备详情失败:', error)
    ElMessage.error('加载设备详情失败')
    goBack()
  }
}

// 加载产品属性（仅在需要时加载）
const loadProductAttributes = async () => {
  if (!deviceInfo.productId) {
    console.warn('产品ID不存在')
    return
  }
  
  // 如果已经加载过，不重复加载
  if (productAttributes.value.length > 0) {
    return
  }
  
  try {
    const attrData = await getProductAttributes(deviceInfo.productId)
    if (attrData) {
      productAttributes.value = attrData || []
      console.log('产品属性加载完成:', productAttributes.value.length, '个')
    }
  } catch (error) {
    console.error('加载产品属性失败:', error)
    ElMessage.error('加载产品属性失败')
  }
}

// 加载产品命令（仅在需要时加载）
const loadProductCommands = async () => {
  if (!deviceInfo.productId) {
    console.warn('产品ID不存在')
    return
  }
  
  // 如果已经加载过，不重复加载
  if (productCommands.value.length > 0) {
    return
  }
  
  try {
    const cmdData = await getProductCommands(deviceInfo.productId)
    if (cmdData) {
      // 检查返回的数据结构，兼容直接返回数组或包含list的对象
      if (Array.isArray(cmdData)) {
        productCommands.value = cmdData
      } else if (cmdData.list && Array.isArray(cmdData.list)) {
        productCommands.value = cmdData.list
      } else {
        productCommands.value = []
      }
      console.log('产品命令加载完成:', productCommands.value.length, '个')
    }
  } catch (error) {
    console.error('加载产品命令失败:', error)
    ElMessage.error('加载产品命令失败')
  }
}

// 加载实时数据
const loadRealtimeData = async () => {
  try {
    // 先确保产品属性已加载
    if (productAttributes.value.length === 0) {
      await loadProductAttributes()
    }
    
    const data = await getDeviceLatestData({ deviceCode: deviceInfo.deviceCode })
    if (data) {
      // 格式化时间，确保显示为 yyyy-MM-dd HH:mm:ss 格式
      const reportTime = data.reportTime;
      if (reportTime) {
        updateTime.value = reportTime.includes('T') ? reportTime.replace('T', ' ') : reportTime;
      } else {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        updateTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      }
      
      const dataMap = data.data || {}
      realtimeData.value = productAttributes.value.map(attr => {
        const value = dataMap[attr.addr]
        
        return {
          key: attr.addr,
          label: attr.attrName,
          value: value !== undefined && value !== null && value !== '' ? value : '-',
          unit: attr.unit
        }
      })
    } else {
      // 如果没有数据，显示空状态
      realtimeData.value = productAttributes.value.map(attr => ({
        key: attr.addr,
        label: attr.attrName,
        value: '-',
        unit: attr.unit
      }))
    }
  } catch (error) {
    console.error('加载实时数据失败:', error)
    ElMessage.error('加载实时数据失败')
  }
}

// 分页后的历史数据
const paginatedHistoryData = computed(() => {
  const start = (historyPagination.currentPage - 1) * historyPagination.pageSize
  const end = start + historyPagination.pageSize
  return historyData.value.slice(start, end)
})

// 表格高度计算（1920x1080下优化）
const tableHeight = computed(() => {
  // 顶部导航栏: ~60px
  // 面包屑: ~60px
  // Tab标签: ~50px
  // 工具栏: ~60px
  // 分页器: ~50px
  // 容器padding和margin: ~40px
  // 剩余空间给表格
  return 'calc(100vh - 320px)'
})

// 查询历史数据
const queryHistory = async () => {
  try {
    if (!historyDateRange.value || historyDateRange.value.length !== 2) {
      ElMessage.warning('请选择查询时间范围')
      return
    }

    const data = await getHistoryData({
      deviceCode: deviceInfo.deviceCode,
      startTime: historyDateRange.value[0],
      endTime: historyDateRange.value[1]
    })

    if (data) {
      // 将后端返回的DeviceData数组转换为表格需要的格式
      // 后端返回的是 [{addr: 'tem', addrv: '22.5', ctime: '2025-12-25 22:30:00'}, ...]
      // 需要按时间分组，将同一时间的多个属性合并到一行
      const dataMap = new Map()
      
      data.forEach(item => {
        // 确保时间格式为 yyyy-MM-dd HH:mm:ss
        let time = '';
        if (item.ctime) {
          time = item.ctime.includes('T') ? item.ctime.replace('T', ' ') : item.ctime;
        }
        if (!dataMap.has(time)) {
          dataMap.set(time, { reportTime: time })
        }
        dataMap.get(time)[item.addr] = item.addrv
      })
      
      historyData.value = Array.from(dataMap.values()).sort((a, b) => {
        return new Date(b.reportTime) - new Date(a.reportTime)
      })
      
      historyPagination.total = historyData.value.length
      historyPagination.currentPage = 1
      
      ElMessage.success(`查询成功，共 ${historyData.value.length} 条记录`)
      
      // 如果当前是图表视图，渲染图表
      if (historyViewMode.value === 'chart') {
        nextTick(() => {
          renderHistoryChart()
        })
      }
    }
  } catch (error) {
    console.error('查询历史数据失败:', error)
    ElMessage.error('查询历史数据失败')
  }
}

// 渲染历史数据图表
const renderHistoryChart = () => {
  console.log('=== 开始渲染图表 ===')
  console.log('historyChartRef.value:', historyChartRef.value)
  console.log('historyData.value.length:', historyData.value.length)
  console.log('productAttributes.value.length:', productAttributes.value.length)
  
  if (!historyChartRef.value) {
    console.error('图表容器未找到')
    return
  }
  
  // 检查容器尺寸
  const rect = historyChartRef.value.getBoundingClientRect()
  console.log('容器尺寸:', rect.width, 'x', rect.height)
  
  if (rect.width === 0 || rect.height === 0) {
    console.error('容器尺寸为0，可能未显示')
    // 延迟重试
    setTimeout(() => {
      if (historyChartRef.value) {
        const retryRect = historyChartRef.value.getBoundingClientRect()
        if (retryRect.width > 0 && retryRect.height > 0) {
          renderHistoryChart()
        }
      }
    }, 200)
    return
  }
  
  if (historyData.value.length === 0) {
    console.warn('无历史数据')
    return
  }
  
  if (productAttributes.value.length === 0) {
    console.warn('产品属性未加载')
    return
  }
  
  // 销毁旧实例
  if (historyChartInstance) {
    historyChartInstance.dispose()
    historyChartInstance = null
  }
  
  // 创建新实例
  historyChartInstance = echarts.init(historyChartRef.value)
  console.log('图表实例已创建')
  
  // 准备数据：按时间升序排列
  const sortedData = [...historyData.value].sort((a, b) => {
    return new Date(a.reportTime.replace('T', ' ')) - new Date(b.reportTime.replace('T', ' '))
  })
  
  console.log('排序后数据:', sortedData.length, '条')
  
  // 提取时间轴数据
  const timeData = sortedData.map(item => {
    const time = item.reportTime.replace('T', ' ')
    // 只显示时:分:秒
    return time.split(' ')[1] || time
  })
  
  console.log('时间轴数据:', timeData.slice(0, 5))
  
  // 为每个数值属性创建一条曲线
  const numericAttrs = productAttributes.value.filter(attr => {
    const dataType = attr.dataType
    return dataType === 'int' || dataType === 'float' || dataType === 'double'
  })
  
  console.log('数值属性:', numericAttrs.map(a => `${a.attrName}(${a.addr})`))
  
  if (numericAttrs.length === 0) {
    console.warn('没有数值类型的属性')
    ElMessage.warning('当前产品没有可绘制的数值属性')
    return
  }
  
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  const series = numericAttrs.map((attr, index) => {
    const seriesData = sortedData.map(item => {
      const value = item[attr.addr]
      const numValue = value !== undefined && value !== null && value !== '' ? Number(value) : null
      return numValue
    })
    
    const validCount = seriesData.filter(v => v !== null).length
    console.log(`${attr.attrName}: ${validCount} 个有效数据点`)
    
    return {
      name: attr.attrName,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: seriesData,
      itemStyle: {
        color: colors[index % colors.length]
      },
      lineStyle: {
        width: 2
      },
      connectNulls: false
    }
  })
  
  const option = {
    title: {
      text: '历史数据趋势图',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 16,
        fontWeight: 600,
        color: '#333'
      }
    },
    backgroundColor: '#fff',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ddd',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#333',
        fontSize: 12
      },
      formatter: function(params) {
        let result = `<div style="font-weight: bold; margin-bottom: 8px; font-size: 13px;">${params[0].axisValue}</div>`
        params.forEach(item => {
          if (item.value !== null) {
            const attr = numericAttrs.find(a => a.attrName === item.seriesName)
            const unit = attr?.unit || ''
            result += `<div style="margin: 4px 0;">${item.marker} ${item.seriesName}: <span style="font-weight: bold;">${item.value}</span> ${unit}</div>`
          }
        })
        return result
      }
    },
    legend: {
      data: series.map(s => s.name),
      top: 30,
      type: 'scroll',
      textStyle: {
        fontSize: 11
      }
    },
    grid: {
      left: 55,
      right: 35,
      bottom: 50,
      top: 65,
      containLabel: false
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timeData,
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666',
        fontSize: 11,
        rotate: 45,
        interval: Math.floor(timeData.length / 15) || 0
      },
      splitLine: {
        show: false
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
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100,
        zoomOnMouseWheel: true,
        moveOnMouseMove: true
      },
      {
        type: 'slider',
        start: 0,
        end: 100,
        height: 20,
        bottom: 8,
        borderColor: '#ccc',
        fillerColor: 'rgba(64, 158, 255, 0.2)',
        handleStyle: {
          color: '#409eff'
        }
      }
    ],
    series: series
  }
  
  console.log('设置图表配置')
  historyChartInstance.setOption(option, true)
  console.log('图表渲染完成')
  console.log('=== 渲染图表结束 ===')
}

// 监听视图模式切换
watch(historyViewMode, async (newMode) => {
  if (newMode === 'chart') {
    // 切换到图表视图
    if (historyData.value.length === 0) {
      // 如果没有数据，先查询
      console.log('图表视图无数据，开始查询...')
      await queryHistory()
    } else {
      // 有数据，直接渲染图表
      console.log('图表视图有数据，渲染图表...')
      // 等待 DOM 更新并稍微延迟，确保容器显示
      setTimeout(() => {
        nextTick(() => {
          renderHistoryChart()
        })
      }, 100)
    }
  }
})

// 监听窗口大小变化和Tab切换
watch([() => activeTab.value, () => historyViewMode.value], () => {
  if (activeTab.value === 'history' && historyViewMode.value === 'chart' && historyChartInstance) {
    setTimeout(() => {
      if (historyChartInstance) {
        historyChartInstance.resize()
      }
    }, 100)
  }
})

onMounted(() => {
  window.addEventListener('resize', () => {
    if (historyChartInstance) {
      historyChartInstance.resize()
    }
  })
})

/// 发送命令
const sendCommand = async (cmd) => {
  console.log('点击命令按钮:', cmd)
  console.log('设备状态:', deviceInfo.status)
  
  if (deviceInfo.status !== 1) {
    ElMessage.warning('设备离线，无法下发命令')
    return
  }

  try {
    await ElMessageBox.confirm(
      `是否确认执行 ${cmd.commandName || cmd.addr} 指令？`,
      '命令确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const data = await sendCommandAPI({
      deviceCode: deviceInfo.deviceCode,
      commands: [
        {
          addr: cmd.addr,
          addrv: cmd.commandValue
        }
      ]
    })

    if (data) {
      ElMessage.success(`命令已下发`)
      // 刷新实时数据
      setTimeout(() => {
        loadRealtimeData()
      }, 1000)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发送命令失败:', error)
      ElMessage.error('发送命令失败')
    }
  }
}

// Tab切换处理
const switchTab = async (tabName) => {
  activeTab.value = tabName
  
  if (tabName === 'realtime') {
    // 实时数据：刷新
    loadRealtimeData()
  } else if (tabName === 'history') {
    // 历史数据：先加载产品属性，再设置默认时间范围并自动查询
    await loadProductAttributes()
    if (historyDateRange.value.length === 0) {
      setDefaultHistoryRange()
    }
    // 如果还没有数据，自动查询一次
    if (historyData.value.length === 0) {
      await queryHistory()
    }
  } else if (tabName === 'command') {
    // 命令控制：加载产品命令
    await loadProductCommands()
  }
}

// 设置默认历史数据查询时间范围（今天00:00到现在）
const setDefaultHistoryRange = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
  historyDateRange.value = [
    formatDateTime(today),
    formatDateTime(now)
  ]
}

// 格式化日期时间
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

// 返回列表
const goBack = () => {
  router.push('/devices')
}

// 复制到剪贴板
const copyToClipboard = async (text, label) => {
  try {
    // 方法1：优先使用 Clipboard API（HTTPS 或 localhost）
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text)
      ElMessage({
        message: `${label}已复制到剪贴板`,
        type: 'success',
        duration: 2000
      })
      return
    }
    
    // 方法2：降级方案 - 使用传统的 execCommand（兼容 HTTP 环境）
    const textArea = document.createElement('textarea')
    textArea.value = text
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.select()
    
    try {
      const successful = document.execCommand('copy')
      document.body.removeChild(textArea)
      
      if (successful) {
        ElMessage({
          message: `${label}已复制到剪贴板`,
          type: 'success',
          duration: 2000
        })
      } else {
        throw new Error('execCommand failed')
      }
    } catch (err) {
      document.body.removeChild(textArea)
      throw err
    }
  } catch (err) {
    console.error('复制失败:', err)
    ElMessage.error('复制失败，请手动选择复制')
  }
}

onMounted(() => {
  loadDeviceDetail()
  // 设置默认历史数据时间范围
  setDefaultHistoryRange()
})
</script>

<style scoped>
/* 主容器 */
.device-detail-page {
  background: #f5f5f7;
  height: calc(100vh - 60px);
  padding: 4px 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 顶部导航与面包屑 */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  flex-shrink: 0;
  background: white;
  padding: 8px 16px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.breadcrumb-link {
  color: #007AFF;
  cursor: pointer;
  transition: all 0.2s;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.breadcrumb-link:hover {
  background: #f0f7ff;
  color: #0051d5;
}

.breadcrumb-sep {
  color: #d2d2d7;
  margin: 0 4px;
}

.breadcrumb-current {
  color: #1d1d1f;
  font-weight: 600;
}

/* 返回按钮 */
.back-btn {
  font-weight: 500 !important;
  padding: 6px 14px !important;
  font-size: 13px !important;
}

/* 顶部双列布局 */
.header-section {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.header-left {
  flex: 1.6;
  background: white;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-right {
  flex: 1;
  background: white;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
}

/* 左侧标题区域 */
.device-title-bar-compact {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 24px;
}

.device-icon-large {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  flex-shrink: 0;
  transition: all 0.3s;
}

.device-icon-large.icon-online {
  background: linear-gradient(135deg, #34C759 0%, #30D158 100%);
  box-shadow: 0 4px 16px rgba(52, 199, 89, 0.3);
}

.device-icon-large.icon-offline {
  background: linear-gradient(135deg, #86868b 0%, #a1a1a6 100%);
  box-shadow: 0 4px 16px rgba(134, 134, 139, 0.2);
}

.device-title-text {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.device-name-wrapper {
  flex: 1;
  min-width: 0;
}

.device-name-large {
  font-size: 36px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.device-code-badge {
  display: inline-flex;
  align-items: center;
  font-size: 13px;
  color: #86868b;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  background: #f5f5f7;
  padding: 4px 12px;
  border-radius: 6px;
  font-weight: 500;
}

/* 状态徽章 */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.status-online {
  background: #e8f5e9;
  color: #34C759;
}

.status-badge.status-offline {
  background: #f5f5f7;
  color: #86868b;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

/* 左侧信息网格 */
.info-grid-compact {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.info-card-compact {
  background: #f5f5f7;
  padding: 14px 18px;
  border-radius: 10px;
}

.info-label {
  font-size: 12px;
  color: #86868b;
  margin-bottom: 6px;
  font-weight: 500;
  text-transform: uppercase;
}

.info-value {
  font-size: 17px;
  color: #1d1d1f;
  font-weight: 600;
}

/* 右侧MQTT卡片 */
.mqtt-card-compact {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
}

.mqtt-line {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
}

.mqtt-code-inline {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  color: #007AFF;
  background: #f5f5f7;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 14px;
  word-break: break-all;
  flex: 1;
  font-weight: 500;
}

.mqtt-label {
  color: #86868b;
  font-weight: 500;
  white-space: nowrap;
  font-size: 15px;
}

.mqtt-value {
  color: #1d1d1f;
  font-weight: 600;
  font-size: 15px;
}

.mqtt-sep {
  color: #d2d2d7;
  margin: 0 6px;
  font-size: 15px;
}

/* 复制按钮 */
.copy-btn {
  padding: 4px !important;
  margin-left: 8px;
  color: #86868b !important;
  transition: all 0.2s;
}

.copy-btn:hover {
  color: #007AFF !important;
  background: #f0f7ff !important;
}

/* 实时数据部分 */
.data-grid-wrapper {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding: 4px 2px 0 0;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  padding: 0;
}

.data-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid transparent;
  transition: all 0.2s;
}

.data-value {
  font-size: 32px;
  font-weight: 600;
  color: #007AFF;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.data-unit {
  font-size: 20px;
  color: #86868b;
  margin-left: 6px;
  font-weight: 500;
}

.info-section-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1d1d1f;
}

.section-icon {
  font-size: 20px;
}

.device-info-section {
  flex: 1;
  min-width: 0;
}

.device-title-compact {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.device-icon-compact {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.device-title-text {
  flex: 1;
  min-width: 0;
}

.device-name-compact {
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 6px 0;
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.device-code-compact {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  background: var(--el-fill-color-light);
  padding: 3px 8px;
  border-radius: 4px;
  display: inline-block;
}

.badge-compact {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.4;
  flex-shrink: 0;
}

.device-meta-compact {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.meta-item-compact {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-label-compact {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  font-weight: 500;
}

.meta-value-compact {
  font-size: 13px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

/* MQTT信息区域 - 右侧 */
.mqtt-info-section {
  width: 440px;
  flex-shrink: 0;
  border-left: 1px solid var(--el-border-color-lighter);
  padding-left: 24px;
}

.mqtt-info-title-compact {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.mqtt-icon-compact {
  font-size: 18px;
}

.mqtt-info-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mqtt-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.mqtt-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.mqtt-label-compact {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.mqtt-value-compact {
  font-size: 13px;
  color: var(--el-text-color-primary);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.password-mask-compact {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  letter-spacing: 1.5px;
}

.password-toggle {
  padding: 0 4px !important;
  font-size: 11px !important;
  height: auto !important;
  min-height: auto !important;
  color: var(--el-color-primary) !important;
}

.mqtt-code-row {
  margin-top: 2px;
}

.mqtt-code-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.mqtt-code-label {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.mqtt-code-value {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  background: var(--el-fill-color-light);
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
  color: var(--el-color-primary);
  font-weight: 500;
  word-break: break-all;
  line-height: 1.5;
}

/* 设备头部 - 旧样式保留（兼容） */
.device-header {
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
}

.device-header-top {
  margin-bottom: 20px;
}

/* MQTT连接信息卡片 */
.mqtt-info-card {
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
}

.mqtt-info-header {
  margin-bottom: 16px;
}

.mqtt-info-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.mqtt-icon {
  font-size: 20px;
}

.mqtt-info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.mqtt-info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mqtt-info-item-full {
  grid-column: span 4;
}

.mqtt-info-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.mqtt-info-value {
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
  word-break: break-all;
  display: flex;
  align-items: center;
}

.code-text {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  background: var(--el-fill-color-light);
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
  color: var(--el-color-primary);
  font-weight: 600;
}

.password-mask {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  letter-spacing: 2px;
}

.device-header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.device-title {
  display: flex;
  align-items: center;
  gap: 18px;
}

.device-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.25);
}

.device-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin: 0 0 6px 0;
  letter-spacing: -0.02em;
}

.device-code {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  font-family: 'Consolas', 'Monaco', monospace;
  background: var(--el-fill-color-light);
  padding: 4px 10px;
  border-radius: 6px;
  display: inline-block;
}

.badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
}

.badge-online {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.badge-offline {
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
}

.device-meta {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.meta-value {
  font-size: 16px;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

/* 标签页 - Apple风格 */
.tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #d2d2d7;
  margin-bottom: 0;
  background: white;
  padding: 0;
  border-radius: 8px 8px 0 0;
  flex-shrink: 0;
  overflow: hidden;
}

.tab {
  padding: 6px 18px;
  background: transparent;
  border: none;
  color: #86868b;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}

.tab:hover {
  color: #1d1d1f;
  background: #f5f5f7;
}

.tab.active {
  color: #007AFF;
  border-bottom-color: #007AFF;
}

.tab-content {
  display: none;
  opacity: 0;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding-top: 0;
}

.tab-content.active {
  display: flex;
  flex-direction: column;
  animation: fadeInUp 0.3s ease forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}


/* 命令控制 */
.command-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.command-btn {
  padding: 28px;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.command-btn:hover {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
}

.command-btn:hover .command-icon {
  transform: scale(1.2);
}

.command-btn:hover .command-name {
  color: #fff;
}

.command-icon {
  font-size: 40px;
  margin-bottom: 12px;
  transition: transform 0.3s;
}

.command-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  transition: color 0.3s;
}

/* 历史数据 */
.history-toolbar {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 8px;
  padding: 6px 14px;
  margin-bottom: 4px;
  display: flex;
  gap: 12px;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.history-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.history-table-container {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.history-pagination {
  padding: 8px 16px;
  flex-shrink: 0;
  border-top: 1px solid #e5e5e7;
}

.history-chart-container {
  background: white;
  border: 1px solid #e5e5e7;
  border-radius: 8px;
  padding: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  max-height: 520px;
  overflow: hidden;
}

.history-chart {
  width: 100%;
  flex: 1;
  min-height: 0;
  max-height: 504px;
}

.unit-text {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-left: 4px;
}

.empty-data {
  padding: 40px;
  text-align: center;
  color: var(--el-text-color-placeholder);
}

.empty-icon {
  font-size: 72px;
  margin-bottom: 20px;
  opacity: 0.5;
}
</style>
