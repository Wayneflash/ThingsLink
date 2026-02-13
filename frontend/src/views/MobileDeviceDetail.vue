<template>
  <div class="mobile-device-detail">
    <!-- 顶部导航栏 -->
    <div class="mobile-header">
      <el-icon class="back-icon" @click="goBack">
        <ArrowLeft />
      </el-icon>
      <h3 class="header-title">设备详情</h3>
      <div class="header-right"></div>
    </div>

    <!-- 设备基本信息 -->
    <div class="device-info-card">
      <div class="device-name-row">
        <h2 class="device-name">{{ deviceInfo.deviceName || '-' }}</h2>
        <el-tag :type="deviceInfo.status === 1 ? 'success' : 'warning'" size="large">
          {{ deviceInfo.status === 1 ? '在线' : '离线' }}
        </el-tag>
      </div>
      <div class="device-code">{{ deviceInfo.deviceCode || '-' }}</div>
      <div class="device-meta-grid">
        <div class="meta-item">
          <div class="meta-label">产品类型</div>
          <div class="meta-value">{{ deviceInfo.productName || '-' }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">所属分组</div>
          <div class="meta-value">{{ deviceInfo.groupName || '-' }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">最后上线</div>
          <div class="meta-value">{{ formatDateTime(deviceInfo.lastOnlineTime) || '-' }}</div>
        </div>
      </div>
    </div>

    <!-- Tab切换 -->
    <div class="tab-nav">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- Tab内容 -->
    <div class="tab-content">
      <!-- 实时数据 -->
      <div v-show="activeTab === 'realtime'" class="tab-panel">
        <div v-if="realtimeData.length > 0" class="data-grid">
          <div
            v-for="item in realtimeData"
            :key="item.key"
            class="data-card"
          >
            <div class="data-label">{{ item.label }}</div>
            <div class="data-value-row">
              <!-- RELAY 开关：开用对勾，关仅文字 -->
              <template v-if="isRelayProduct && isRelaySwitchAddr(item.key)">
                <el-icon 
                  v-if="formatRelaySwitchDisplay(item.value) === 'on'" 
                  class="switch-status-icon switch-on" 
                  :size="24"
                >
                  <CircleCheck />
                </el-icon>
                <span 
                  class="data-value" 
                  :class="formatRelaySwitchDisplay(item.value) === 'on' ? 'switch-on-text' : 'switch-off-text'"
                >
                  {{ formatRelaySwitchDisplay(item.value) === 'on' ? '开' : '关' }}
                </span>
              </template>
              <!-- 其他属性显示 -->
              <template v-else>
                <span class="data-value">{{ item.value }}</span>
                <span v-if="item.unit" class="data-unit">{{ item.unit }}</span>
              </template>
            </div>
            <div class="data-time">
              <el-icon :size="12"><Clock /></el-icon>
              {{ updateTime }}
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-icon :size="48" color="#ccc"><Document /></el-icon>
          <p>暂无实时数据</p>
        </div>
      </div>

      <!-- 历史数据 -->
      <div v-show="activeTab === 'history'" class="tab-panel">
        <div class="history-filters">
          <el-date-picker
            v-model="historyDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            size="small"
            style="width: 100%;"
          />
          <el-button type="primary" size="small" @click="queryHistory" style="margin-top: 8px; width: 100%;">
            查询
          </el-button>
        </div>
        <div v-if="historyData.length > 0" class="history-chart-container">
          <div ref="historyChartRef" class="history-chart"></div>
        </div>
        <div v-else class="empty-state">
          <el-icon :size="48" color="#ccc"><Document /></el-icon>
          <p>暂无历史数据</p>
        </div>
      </div>

      <!-- 命令控制 -->
      <div v-show="activeTab === 'command'" class="tab-panel">
        <!-- RELAY 远程开关控制 -->
        <template v-if="isRelayProduct">
          <div class="relay-control-header">
            <span class="relay-title">远程开关</span>
            <div class="relay-header-actions">
              <span v-if="deviceInfo.status !== 1" class="offline-chip">离线</span>
              <el-button type="primary" link :loading="relayRefreshLoading" class="relay-refresh-btn" @click="handleRelayRefresh">
                <el-icon><Refresh /></el-icon>刷新
              </el-button>
            </div>
          </div>
          <div class="relay-switch-panel">
            <div v-for="i in 4" :key="`switch${i}`" class="relay-card">
              <div class="relay-card-main">
                <span class="relay-card-label">开关 {{ i }}</span>
                <span class="status-badge" :class="formatRelaySwitchDisplay(getRelaySwitchValue(`switch${i}`)) === 'on' ? 'status-on' : 'status-off'">
                  {{ formatRelaySwitchDisplay(getRelaySwitchValue(`switch${i}`)) === 'on' ? '开' : '关' }}
                </span>
              </div>
              <el-switch
                :model-value="getRelaySwitchValue(`switch${i}`)"
                :loading="relaySwitchLoading[`switch${i}`]"
                :disabled="deviceInfo.status !== 1"
                inline-prompt
                active-text="开"
                inactive-text="关"
                active-value="1"
                inactive-value="0"
                @change="(val) => handleRelaySwitchChange(`switch${i}`, val)"
                class="relay-switch-control"
              />
            </div>
          </div>
        </template>
        <!-- 普通命令控制 -->
        <template v-else>
          <div v-if="productCommands.length > 0" class="command-list">
            <div
              v-for="cmd in productCommands"
              :key="cmd.addr"
              class="command-card"
            >
              <div class="command-info">
                <div class="command-name">{{ cmd.commandName || cmd.addr }}</div>
                <div class="command-desc" v-if="cmd.description">{{ cmd.description }}</div>
              </div>
              <el-button
                type="primary"
                :disabled="deviceInfo.status !== 1"
                @click="sendCommand(cmd)"
              >
                执行
              </el-button>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon :size="48" color="#ccc"><Tools /></el-icon>
            <p>暂无控制命令</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Clock,
  Document,
  Tools,
  CircleCheck,
  Refresh,
  Lightning
} from '@element-plus/icons-vue'
import { getDeviceDetail, getDeviceLatestData } from '@/api/device'
import { getProductAttributes, getProductCommands } from '@/api/product'
import { getHistoryData } from '@/api/data'
import { sendCommand as sendCommandAPI, refreshRelayStatus } from '@/api/command'
import * as echarts from 'echarts'

const router = useRouter()
const route = useRoute()

// 设备信息
const deviceInfo = ref({
  deviceName: '',
  deviceCode: '',
  productId: null,
  productName: '',
  protocol: 'MQTT1.0',
  groupName: '',
  status: 0,
  lastOnlineTime: ''
})

// Tab相关
const tabs = [
  { key: 'realtime', label: '实时数据' },
  { key: 'history', label: '历史数据' },
  { key: 'command', label: '命令控制' }
]
const activeTab = ref('realtime')

// 实时数据
const realtimeData = ref([])
const updateTime = ref('-')
let realtimeTimer = null

// 历史数据
const historyDateRange = ref([])
const historyData = ref([])
const historyChartRef = ref(null)
let historyChartInstance = null

// 产品命令
const productCommands = ref([])

// 产品属性
const productAttributes = ref([])

// 远程开关：开关 loading 状态
const relaySwitchLoading = ref({})
// 远程开关：刷新按钮 loading
const relayRefreshLoading = ref(false)

// 判断是否为 RELAY 产品
const isRelayProduct = computed(() => {
  return deviceInfo.value.protocol === 'RELAY'
})

// 判断是否为 RELAY 开关地址
const isRelaySwitchAddr = (addr) => {
  return /^switch[1-4]$/i.test(addr || '')
}

// 格式化 RELAY 开关显示值
const formatRelaySwitchDisplay = (value) => {
  return (value !== undefined && value !== null && value !== '' && String(value) === '1') ? 'on' : 'off'
}

// 获取 RELAY 开关当前值（用于 el-switch）
const getRelaySwitchValue = (addr) => {
  const item = realtimeData.value.find(r => r.key === addr)
  const v = item?.value
  if (v === undefined || v === null || v === '-') return '0'
  return String(v) === '1' ? '1' : '0'
}

// 格式化时间
const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 返回
const goBack = () => {
  router.back()
}

// 加载设备详情
const loadDeviceDetail = async () => {
  const deviceCode = route.query.deviceCode
  if (!deviceCode) {
    ElMessage.error('缺少设备编码参数')
    router.back()
    return
  }

  try {
    const res = await getDeviceDetail({ deviceCode })
    if (res.code === 200) {
      deviceInfo.value = res.data
      await loadProductAttributes()
      await loadRealtimeData()
      if (activeTab.value === 'command') {
        await loadProductCommands()
      }
    }
  } catch (error) {
    console.error('加载设备详情失败:', error)
    ElMessage.error('加载设备详情失败')
  }
}

// 加载产品属性
const loadProductAttributes = async () => {
  if (!deviceInfo.value.productId) return
  try {
    const res = await getProductAttributes({ productId: deviceInfo.value.productId })
    if (res.code === 200) {
      productAttributes.value = res.data || []
    }
  } catch (error) {
    console.error('加载产品属性失败:', error)
  }
}

// 加载实时数据
const loadRealtimeData = async () => {
  try {
    const data = await getDeviceLatestData({ deviceCode: deviceInfo.value.deviceCode })
    if (data) {
      const reportTime = data.reportTime
      if (reportTime) {
        updateTime.value = reportTime.includes('T') ? reportTime.replace('T', ' ') : reportTime
      } else {
        updateTime.value = new Date().toLocaleString('zh-CN').replace(/\//g, '-')
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
      realtimeData.value = productAttributes.value.map(attr => ({
        key: attr.addr,
        label: attr.attrName,
        value: '-',
        unit: attr.unit
      }))
    }
  } catch (error) {
    console.error('加载实时数据失败:', error)
  }
}

// 加载产品命令
const loadProductCommands = async () => {
  if (!deviceInfo.value.productId) return
  try {
    const res = await getProductCommands({ productId: deviceInfo.value.productId })
    if (res.code === 200) {
      productCommands.value = res.data || []
    }
  } catch (error) {
    console.error('加载产品命令失败:', error)
  }
}

// 查询历史数据（分页，图表模式单次最多 500 条）
const queryHistory = async () => {
  if (!historyDateRange.value || historyDateRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  try {
    const res = await getHistoryData({
      deviceCode: deviceInfo.value.deviceCode,
      startTime: historyDateRange.value[0],
      endTime: historyDateRange.value[1],
      pageNum: 1,
      pageSize: 500
    })

    if (res && res.list) {
      const dataMap = new Map()
      res.list.forEach(item => {
        let time = item.ctime ? (item.ctime.includes('T') ? item.ctime.replace('T', ' ') : item.ctime) : ''
        if (!dataMap.has(time)) dataMap.set(time, { reportTime: time })
        dataMap.get(time)[item.addr] = item.addrv
      })
      historyData.value = Array.from(dataMap.values()).sort((a, b) => new Date(a.reportTime) - new Date(b.reportTime))
      await nextTick()
      renderHistoryChart()
    }
  } catch (error) {
    console.error('查询历史数据失败:', error)
    ElMessage.error('查询历史数据失败')
  }
}

// 渲染历史数据图表
const renderHistoryChart = () => {
  if (!historyChartRef.value || historyData.value.length === 0) return

  if (historyChartInstance) {
    historyChartInstance.dispose()
  }

  historyChartInstance = echarts.init(historyChartRef.value)

  // 处理数据：按时间排序，提取各属性的时间序列
  const sortedData = [...historyData.value].sort((a, b) => new Date(a.reportTime) - new Date(b.reportTime))
  const times = sortedData.map(item => {
    const date = new Date(item.reportTime)
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
  })

  // 提取每个属性的数据（合并后格式为 { reportTime, [addr]: value }）
  const series = productAttributes.value.map(attr => {
    const values = sortedData.map(item => {
      const value = item[attr.addr] ?? item.data?.[attr.addr]
      return value !== undefined && value !== null ? parseFloat(value) : null
    })

    return {
      name: attr.attrName,
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 4
    }
  })

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: productAttributes.value.map(attr => attr.attrName), bottom: 0 },
    grid: { top: 20, left: 40, right: 20, bottom: 60 },
    xAxis: { type: 'category', data: times, boundaryGap: false },
    yAxis: { type: 'value' },
    series
  }

  historyChartInstance.setOption(option)
}

// 发送命令
const sendCommand = async (cmd) => {
  if (deviceInfo.value.status !== 1) {
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

    const res = await sendCommandAPI({
      deviceCode: deviceInfo.value.deviceCode,
      commands: [
        {
          addr: cmd.addr,
          addrv: cmd.commandValue
        }
      ]
    })

    if (res.code === 200) {
      ElMessage.success('命令已下发')
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

// 处理 RELAY 开关切换
const handleRelaySwitchChange = async (addr, targetValue) => {
  if (deviceInfo.value.status !== 1) {
    ElMessage.warning('设备离线，无法下发命令')
    return
  }

  relaySwitchLoading.value = { ...relaySwitchLoading.value, [addr]: true }
  try {
    await sendCommandAPI({
      deviceCode: deviceInfo.value.deviceCode,
      commands: [{ addr, addrv: targetValue }]
    })

    const start = Date.now()
    const timeout = 5000
    const pollInterval = 250
    const checkDone = () => {
      const cur = getRelaySwitchValue(addr)
      return cur === targetValue
    }

    // 立即检查一次
    await loadRealtimeData()
    if (checkDone()) {
      ElMessage.success('命令已生效')
      return
    }

    // 轮询检查
    do {
      await new Promise(r => setTimeout(r, pollInterval))
      await loadRealtimeData()
      if (checkDone()) {
        ElMessage.success('命令已生效')
        return
      }
      if (Date.now() - start >= timeout) break
    } while (Date.now() - start < timeout)

    ElMessage.warning('设备无响应')
  } catch (error) {
    console.error('命令下发失败:', error)
    ElMessage.error('命令下发失败')
  } finally {
    relaySwitchLoading.value = { ...relaySwitchLoading.value, [addr]: false }
  }
}

// 处理 RELAY 状态刷新
const handleRelayRefresh = async () => {
  relayRefreshLoading.value = true
  try {
    await refreshRelayStatus({ deviceCode: deviceInfo.value.deviceCode })
    await new Promise(r => setTimeout(r, 800))
    await loadRealtimeData()
    ElMessage.success('状态已刷新')
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    relayRefreshLoading.value = false
  }
}

// Tab切换
const switchTab = async (tabKey) => {
  activeTab.value = tabKey

  if (tabKey === 'realtime') {
    loadRealtimeData()
    // 启动定时刷新
    if (realtimeTimer) clearInterval(realtimeTimer)
    realtimeTimer = setInterval(() => {
      loadRealtimeData()
    }, 5000)
  } else {
    // 停止定时刷新
    if (realtimeTimer) {
      clearInterval(realtimeTimer)
      realtimeTimer = null
    }

    if (tabKey === 'history') {
      // 设置默认时间范围：最近24小时
      if (historyDateRange.value.length === 0) {
        const end = new Date()
        const start = new Date(end.getTime() - 24 * 60 * 60 * 1000)
        historyDateRange.value = [
          start.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replace(/\//g, '-').replace(',', '') + ':00',
          end.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replace(/\//g, '-').replace(',', '') + ':00'
        ]
      }
    } else if (tabKey === 'command') {
      await loadProductCommands()
      if (isRelayProduct.value) {
        await loadProductAttributes()
        try {
          await refreshRelayStatus({ deviceCode: deviceInfo.value.deviceCode })
        } catch (e) {
          console.warn('RELAY 状态刷新失败:', e)
        }
        await loadRealtimeData()
        setTimeout(() => loadRealtimeData(), 1500)
      }
    }
  }
}

// 初始化
onMounted(() => {
  loadDeviceDetail()
  if (activeTab.value === 'realtime') {
    realtimeTimer = setInterval(() => {
      loadRealtimeData()
    }, 5000)
  }
})

onBeforeUnmount(() => {
  if (realtimeTimer) {
    clearInterval(realtimeTimer)
  }
  if (historyChartInstance) {
    historyChartInstance.dispose()
  }
})
</script>

<style scoped>
.mobile-device-detail {
  min-height: 100vh;
  background: #f5f5f7;
  padding-bottom: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.mobile-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);

}

.back-icon {
  font-size: 24px;
  color: #1d1d1f;
  cursor: pointer;
  transition: color 0.3s ease;
}

.back-icon:hover {
  color: #667eea;
}

.header-title {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.device-info-card {
  background: #ffffff;
  margin: 16px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

}

.device-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.device-name {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.device-code {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 16px;
}

.device-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.meta-item {
}

.meta-label {
  font-size: 12px;
  color: #86868b;
  margin-bottom: 4px;
}

.meta-value {
  font-size: 14px;
  color: #1d1d1f;
  font-weight: 500;
}

.tab-nav {
  display: flex;
  background: #ffffff;
  margin: 0 16px 16px;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px;
  font-size: 14px;
  color: #86868b;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

.tab-content {
  padding: 0 16px;
}

.tab-panel {
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.data-card {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.data-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.data-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.data-value {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
}

.data-unit {
  font-size: 14px;
  color: #86868b;
}

.data-time {
  font-size: 11px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-filters {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.history-chart-container {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.history-chart {
  width: 100%;
  height: 300px;
}

.command-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.command-card {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.command-info {
  flex: 1;
}

.command-name {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.command-desc {
  font-size: 13px;
  color: #86868b;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #86868b;
}

.empty-state p {
  margin: 16px 0 0;
  font-size: 14px;
}

/* RELAY 远程开关 */
.relay-control-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.relay-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.relay-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.offline-chip {
  padding: 4px 12px;
  background: rgba(255, 149, 0, 0.15);
  border-radius: 10px;
  font-size: 12px;
  color: #ff9500;
  font-weight: 500;
}

.relay-refresh-btn {
  color: #667eea;
  font-size: 14px;
  padding: 0;
}

.relay-switch-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.relay-card {
  background: #ffffff;
  padding: 14px 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
}

.relay-card-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.relay-card-label {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.status-badge {
  font-size: 13px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 8px;
  display: inline-block;
  width: fit-content;
}

.status-on {
  background: rgba(52, 199, 89, 0.12);
  color: #34c759;
}

.status-off {
  background: rgba(134, 134, 139, 0.1);
  color: #86868b;
}

.relay-switch-control {
  flex-shrink: 0;
}

.relay-switch-control :deep(.el-switch__core) {
  width: 52px;
  height: 28px;
  border-radius: 14px;
}

.relay-switch-control :deep(.el-switch__core::after) {
  width: 24px;
  height: 24px;
}

.relay-switch-control :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #34c759;
}

/* 实时数据中的开关图标样式 */
.switch-status-icon.switch-on {
  color: #34c759;
}

.switch-status-icon.switch-off {
  color: #86868b;
}

.data-value-row .switch-on-text {
  color: #34c759;
  font-weight: 600;
}

.data-value-row .switch-off-text {
  color: #86868b;
  font-weight: 600;
}

/* 移动端响应式优化 */
@media (max-width: 375px) {
  .mobile-header {
    padding: 10px 12px;
  }

  .header-title {
    font-size: 16px;
  }

  .device-info-card {
    margin: 12px;
    padding: 16px;
  }

  .device-name {
    font-size: 18px;
  }

  .device-meta-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .tab-nav {
    margin: 0 12px 12px;
  }

  .tab-item {
    padding: 8px;
    font-size: 13px;
  }

  .tab-content {
    padding: 0 12px;
  }

  .data-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .data-card {
    padding: 12px;
  }

  .data-value {
    font-size: 20px;
  }

  .history-chart {
    height: 250px;
  }

}

@media (min-width: 376px) and (max-width: 414px) {
  .device-info-card {
    margin: 14px;
  }

  .data-grid {
    gap: 11px;
  }
}
</style>