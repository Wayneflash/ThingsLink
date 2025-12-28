<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>设备选择</span>
          </template>
          <el-select v-model="selectedDevice" placeholder="选择设备" @change="loadDeviceData">
            <el-option v-for="d in devices" :key="d.deviceCode" 
                      :label="d.deviceName" :value="d.deviceCode" />
          </el-select>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>最新数据</span>
          </template>
          <el-table :data="latestData" border>
            <el-table-column prop="addr" label="属性" width="150" />
            <el-table-column prop="addrv" label="值" />
            <el-table-column prop="ctime" label="采集时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>数据趋势</span>
          </template>
          <div ref="chartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { deviceApi, dataApi } from '@/api'
import * as echarts from 'echarts'

const devices = ref([])
const selectedDevice = ref('')
const latestData = ref([])
const chartRef = ref(null)
let chartInstance = null

const loadDevices = async () => {
  try {
    const res = await deviceApi.list()
    devices.value = res.data
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  }
}

const loadDeviceData = async () => {
  if (!selectedDevice.value) return
  
  try {
    const res = await dataApi.getLatest(selectedDevice.value, 20)
    latestData.value = res.data.slice(0, 10)
    
    // 更新图表
    updateChart(res.data)
  } catch (error) {
    ElMessage.error('加载设备数据失败')
  }
}

const updateChart = (data) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  // 按属性分组
  const groupedData = {}
  data.forEach(item => {
    if (!groupedData[item.addr]) {
      groupedData[item.addr] = { times: [], values: [] }
    }
    groupedData[item.addr].times.push(item.ctime)
    groupedData[item.addr].values.push(parseFloat(item.addrv))
  })
  
  const series = Object.keys(groupedData).map(addr => ({
    name: addr,
    type: 'line',
    data: groupedData[addr].values
  }))
  
  const option = {
    title: { text: '数据趋势' },
    tooltip: { trigger: 'axis' },
    legend: { data: Object.keys(groupedData) },
    xAxis: { type: 'category', data: groupedData[Object.keys(groupedData)[0]]?.times || [] },
    yAxis: { type: 'value' },
    series: series
  }
  
  chartInstance.setOption(option)
}

onMounted(() => {
  loadDevices()
})
</script>
