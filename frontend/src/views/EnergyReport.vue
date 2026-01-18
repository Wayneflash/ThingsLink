<template>
  <div class="energy-report-page min-h-screen bg-gray-50">
    <!-- 页面头部 -->
    <div class="page-header bg-white shadow-sm mb-6 px-6 py-4">
      <h1 class="text-2xl font-semibold text-gray-900 mb-2">能耗报表</h1>
      <p class="text-sm text-gray-600">支持电、水、气三种能源类型的能耗报表导出</p>
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

          <el-form-item label="报表类型">
            <el-select 
              v-model="queryForm.reportType" 
              placeholder="类型" 
              class="filter-select-small"
              @change="handleQuery"
            >
              <el-option label="日报表" value="daily" />
              <el-option label="月报表" value="monthly" />
              <el-option label="年报表" value="yearly" />
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

      <!-- 报表数据表格 -->
      <el-card 
        v-if="reportData.length > 0 || loading"
        class="report-card shadow-sm"
        shadow="hover"
      >
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-semibold text-gray-900">
              {{ getEnergyTypeName(queryForm.energyType) }}报表
            </span>
            <div class="flex items-center gap-2">
              <el-tag size="small" type="info">{{ getUnit(queryForm.energyType) }}</el-tag>
              <el-button 
                size="small" 
                type="success" 
                :icon="Download"
                @click="handleExport"
                :loading="exportLoading"
              >
                导出
              </el-button>
            </div>
          </div>
        </template>

        <el-table 
          :data="reportData" 
          stripe
          class="report-table"
          v-loading="loading"
          :default-sort="{ prop: 'date', order: 'descending' }"
        >
          <el-table-column prop="deviceName" label="设备名称" min-width="180" fixed="left">
            <template #default="{ row }">
              <div class="flex items-center">
                <el-icon class="mr-2 text-gray-400"><Monitor /></el-icon>
                <span>{{ row.deviceName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="deviceCode" label="设备编码" min-width="150" />

          <el-table-column prop="date" label="日期" width="180" sortable>
            <template #default="{ row }">
              <span class="text-gray-900">{{ row.date }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="consumption" label="能耗" width="180" sortable align="right">
            <template #default="{ row }">
              <span class="font-semibold text-gray-900">
                {{ formatNumber(row.consumption) }}
              </span>
              <span class="ml-1 text-sm text-gray-500">{{ row.unit }}</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper mt-4">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleQuery"
            @current-change="handleQuery"
            class="pagination"
          />
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && hasQueried && reportData.length === 0"
        description="暂无数据"
        :image-size="120"
      />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Monitor, Download, List, InfoFilled } from '@element-plus/icons-vue'
import { getEnergyReport } from '@/api/energy'
import { getDeviceList } from '@/api/device'
import GroupTree from '@/components/GroupTree.vue'
import { getGroupTree } from '@/api/group'
import { flattenTree } from '@/utils/tree'

// 加载状态
const loading = ref(false)
const exportLoading = ref(false)
const hasQueried = ref(false) // 是否已查询过

// 查询表单
const queryForm = reactive({
  energyType: 'electric', // 默认电力
  deviceId: null,
  groupId: null,
  reportType: 'daily' // 默认日报表
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

// 报表数据
const reportData = ref([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

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

// 加载设备列表
const loadDevices = async () => {
  try {
    const res = await getDeviceList({
      page: 1,
      pageSize: 1000
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
      groupId: queryForm.groupId || null,
      reportType: queryForm.reportType || 'daily',
      page: pagination.page,
      pageSize: pagination.pageSize
    }

    const res = await getEnergyReport(params)
    
    reportData.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (error) {
    console.error('查询能耗报表失败:', error)
    ElMessage.error(error.message || '查询失败，请稍后重试')
    reportData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 导出报表
const handleExport = async () => {
  if (reportData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  exportLoading.value = true
  try {
    // TODO: 实现导出功能
    ElMessage.success('导出功能开发中...')
  } catch (error) {
    console.error('导出报表失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exportLoading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.energyType = 'electric'
  queryForm.deviceId = null
  queryForm.groupId = null
  queryForm.reportType = 'daily'
  dateRange.value = []
  pagination.page = 1
  pagination.pageSize = 10
  reportData.value = []
  pagination.total = 0
}

// 格式化数字（保留2位小数）
const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) {
    return '0.00'
  }
  return Number(num).toFixed(2)
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

  await Promise.all([loadDevices(), loadGroups()])
  await handleQuery()
})
</script>

<style scoped>
.energy-report-page {
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

.report-card {
  border-radius: 12px;
}

.report-table :deep(.el-table__header) {
  background-color: #f9fafb;
}

.report-table :deep(.el-table__header th) {
  background-color: #f9fafb;
  color: #374151;
  font-weight: 600;
}

.report-table :deep(.el-table__row:hover) {
  background-color: #f3f4f6;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

.pagination {
  margin-top: 16px;
}
</style>
