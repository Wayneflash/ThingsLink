<template>
  <div class="alarm-threshold-config">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">⚙️ 设备告警阈值配置</h2>
        <div class="stats-quick">
          <span class="stat-item">总设备 <strong>{{ stats.total }}</strong></span>
          <span class="stat-item active">已配置 <strong>{{ stats.configured }}</strong></span>
          <span class="stat-item inactive">未配置 <strong>{{ stats.unconfigured }}</strong></span>
        </div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="openBatchModal">批量配置</el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索设备名称/设备编码"
        style="width: 220px"
        clearable
        @change="loadDevices"
      />
      
      <el-select v-model="filters.productId" placeholder="产品型号" clearable @change="loadDevices" style="width: 150px">
        <el-option v-for="product in products" :key="product.id" :label="product.productName" :value="product.id" />
      </el-select>

      <el-select v-model="filters.groupId" placeholder="设备分组" clearable @change="loadDevices" style="width: 130px">
        <el-option v-for="group in groups" :key="group.id" :label="group.groupName" :value="group.id" />
      </el-select>

      <el-select v-model="filters.configStatus" placeholder="配置状态" clearable @change="loadDevices" style="width: 130px">
        <el-option label="已配置" value="configured" />
        <el-option label="未配置" value="unconfigured" />
      </el-select>

      <el-select v-model="filters.onlineStatus" placeholder="在线状态" clearable @change="loadDevices" style="width: 120px">
        <el-option label="在线" :value="1" />
        <el-option label="离线" :value="0" />
      </el-select>
    </div>

    <!-- 设备列表 -->
    <el-table :data="devices" style="width: 100%" v-loading="loading">
      <el-table-column prop="deviceName" label="设备名称" width="180">
        <template #default="{ row }">
          <div style="display: flex; align-items: center">
            <span :class="['status-dot', row.status === 1 ? 'online' : 'offline']"></span>
            <strong>{{ row.deviceName }}</strong>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="deviceCode" label="设备编码" width="140">
        <template #default="{ row }">
          <span style="color: #909399; font-family: monospace">{{ row.deviceCode }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="productName" label="产品型号" width="130" />
      <el-table-column prop="groupName" label="所属分组" width="100" />
      
      <el-table-column label="配置状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.alarmConfig" type="success" size="small">✓ 已配置</el-tag>
          <el-tag v-else type="danger" size="small">✗ 未配置</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="告警条件" width="250">
        <template #default="{ row }">
          <div v-if="row.alarmConfigObj && row.alarmConfigObj.conditions && row.alarmConfigObj.conditions.length > 0" class="condition-text">
            <span class="metric">{{ getMetricLabel(row.alarmConfigObj.conditions[0].metric) }}</span>
            <span class="operator">{{ row.alarmConfigObj.conditions[0].operator }}</span>
            <span class="value">{{ row.alarmConfigObj.conditions[0].threshold }}</span>
          </div>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="告警级别" width="70" align="center">
        <template #default="{ row }">
          <span v-if="row.alarmConfigObj">{{ getLevelIcon(row.alarmConfigObj.level) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="通知人员" width="120">
        <template #default="{ row }">
          <span v-if="row.alarmConfigObj && row.alarmConfigObj.notifyUsers" style="font-size: 12px; color: #606266">
            {{ getUserNames(row.alarmConfigObj.notifyUsers) }}
          </span>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="启用" width="70" align="center">
        <template #default="{ row }">
          <el-switch
            v-if="row.alarmConfig"
            v-model="row.alarmEnabled"
            @change="toggleAlarmEnabled(row)"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openConfigModal(row)">配置</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadDevices"
      @current-change="loadDevices"
      style="margin-top: 20px; justify-content: center"
    />

    <!-- 单设备配置弹窗 -->
    <el-dialog
      v-model="configModal.visible"
      :title="'配置设备告警阈值 - ' + configModal.device?.deviceName"
      width="600px"
    >
      <el-form :model="configModal.form" label-width="100px">
        <el-form-item label="设备信息">
          <div class="info-box">
            <div class="info-row">
              <span class="info-label">设备名称：</span>
              <span class="info-value">{{ configModal.device?.deviceName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">设备编码：</span>
              <span class="info-value" style="font-family: monospace">{{ configModal.device?.deviceCode }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">产品型号：</span>
              <span class="info-value">{{ configModal.device?.productName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">所属分组：</span>
              <span class="info-value">{{ configModal.device?.groupName }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="告警级别" required>
          <el-radio-group v-model="configModal.form.level">
            <el-radio label="critical">🔴 严重</el-radio>
            <el-radio label="warning">🟡 警告</el-radio>
            <el-radio label="info">🔵 提示</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider>监控条件</el-divider>

        <div v-for="(condition, index) in configModal.form.conditions" :key="index" class="condition-item">
          <div class="condition-item-header">
            <strong>条件 {{ index + 1 }}</strong>
            <el-button type="danger" link size="small" @click="removeCondition(index)">删除</el-button>
          </div>
          <div style="display: flex; gap: 12px; margin-top: 8px">
            <el-select v-model="condition.metric" placeholder="监控指标">
              <el-option v-for="attr in deviceAttributes" :key="attr.identifier" :label="attr.name" :value="attr.identifier" />
            </el-select>
            <el-select v-model="condition.operator" style="width: 80px">
              <el-option label=">" value=">" />
              <el-option label="<" value="<" />
              <el-option label="=" value="=" />
            </el-select>
            <el-input-number v-model="condition.threshold" :precision="2" style="width: 120px" />
          </div>
        </div>

        <el-button @click="addCondition" style="width: 100%; margin-top: 12px">+ 添加监控条件</el-button>

        <el-divider>通知设置</el-divider>

        <el-form-item label="通知人员" required>
          <el-select v-model="configModal.form.notifyUsers" multiple placeholder="请选择通知人员">
            <el-option v-for="user in users" :key="user.id" :label="user.username" :value="user.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="告警堆叠">
          <el-switch v-model="configModal.form.stackMode" />
          <div style="font-size: 12px; color: #999; margin-top: 4px">开启后，恢复前不会重复告警</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="configModal.visible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量配置弹窗 -->
    <el-dialog v-model="batchModal.visible" title="批量配置告警阈值" width="600px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        <template #title>
          <strong>重要提示</strong>
        </template>
        批量配置将<strong>覆盖</strong>已选设备的原有配置，以本次设置为准。已配置设备的旧阈值将失效。
      </el-alert>

      <el-form :model="batchModal.form" label-width="100px">
        <el-form-item label="选择产品" required>
          <el-select v-model="batchModal.productId" placeholder="请先选择产品型号" @change="onProductChange">
            <el-option v-for="product in products" :key="product.id" :label="product.productName" :value="product.id" />
          </el-select>
          <div style="font-size: 12px; color: #999; margin-top: 4px">选择产品后，监控条件将根据该产品物模型动态加载</div>
        </el-form-item>

        <el-form-item label="设备分组">
          <el-select v-model="batchModal.groupId" placeholder="全部分组" clearable @change="loadBatchDevices">
            <el-option v-for="group in groups" :key="group.id" :label="group.groupName" :value="group.id" />
          </el-select>
          <div style="font-size: 12px; color: #999; margin-top: 4px">筛选该产品下指定分组的设备</div>
        </el-form-item>

        <el-form-item label="选择设备" required>
          <div class="select-all-bar">
            <el-checkbox v-model="batchModal.selectAll" @change="toggleSelectAll">全选/取消全选</el-checkbox>
            <span class="select-count">
              已选择 <strong>{{ batchModal.selectedDeviceIds.length }}</strong> 台设备
              <span v-if="batchModal.configuredCount > 0" style="color: #e6a23c">
                （其中 {{ batchModal.configuredCount }} 台已配置）
              </span>
            </span>
          </div>
          <div class="device-checkbox-list">
            <label v-for="device in batchModal.availableDevices" :key="device.id" class="device-checkbox-item">
              <el-checkbox v-model="device.selected" @change="updateSelectedDevices" />
              <div class="device-checkbox-info">
                <div class="device-checkbox-name">
                  {{ device.deviceName }}
                  <el-tag v-if="device.alarmConfig" type="success" size="small" style="margin-left: 8px">已配置</el-tag>
                </div>
                <div class="device-checkbox-meta">
                  {{ device.deviceCode }} | {{ device.groupName || '未分组' }} | 
                  {{ device.status === 1 ? '在线' : '离线' }} | 
                  {{ device.alarmConfig ? '当前：' + getConditionText(device) : '未配置' }}
                </div>
              </div>
            </label>
          </div>
        </el-form-item>

        <el-divider>统一配置</el-divider>

        <el-form-item label="告警级别" required>
          <el-radio-group v-model="batchModal.form.level">
            <el-radio label="critical">🔴 严重</el-radio>
            <el-radio label="warning">🟡 警告</el-radio>
            <el-radio label="info">🔵 提示</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="监控条件" required>
          <div style="display: flex; gap: 12px">
            <el-select v-model="batchModal.condition.metric" placeholder="监控指标">
              <el-option v-for="attr in batchDeviceAttributes" :key="attr.identifier" :label="attr.name" :value="attr.identifier" />
            </el-select>
            <el-select v-model="batchModal.condition.operator" style="width: 80px">
              <el-option label=">" value=">" />
              <el-option label="<" value="<" />
              <el-option label="=" value="=" />
            </el-select>
            <el-input-number v-model="batchModal.condition.threshold" :precision="2" style="width: 120px" />
          </div>
        </el-form-item>

        <el-form-item label="通知人员" required>
          <el-select v-model="batchModal.form.notifyUsers" multiple placeholder="请选择通知人员">
            <el-option v-for="user in users" :key="user.id" :label="user.username" :value="user.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="预览">
          <el-input
            :model-value="getBatchPreview()"
            type="textarea"
            :rows="6"
            readonly
            style="resize: none; background: #f8f9fa"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchModal.visible = false">取消</el-button>
        <el-button type="primary" @click="saveBatchConfig">确认配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据状态
const loading = ref(false)
const devices = ref([])
const products = ref([])
const groups = ref([])
const users = ref([])
const deviceAttributes = ref([])
const batchDeviceAttributes = ref([])

// 统计数据
const stats = reactive({
  total: 0,
  configured: 0,
  unconfigured: 0
})

// 筛选条件
const filters = reactive({
  keyword: '',
  productId: null,
  groupId: null,
  configStatus: null,
  onlineStatus: null
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 单设备配置弹窗
const configModal = reactive({
  visible: false,
  device: null,
  form: {
    level: 'warning',
    conditions: [{ metric: '', operator: '>', threshold: 0 }],
    notifyUsers: [],
    stackMode: true
  }
})

// 批量配置弹窗
const batchModal = reactive({
  visible: false,
  productId: null,
  groupId: null,
  availableDevices: [],
  selectedDeviceIds: [],
  selectAll: false,
  configuredCount: 0,
  condition: { metric: '', operator: '>', threshold: 0 },
  form: {
    level: 'warning',
    notifyUsers: [],
    stackMode: true
  }
})

// 加载设备列表
const loadDevices = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword,
      productId: filters.productId,
      groupId: filters.groupId,
      status: filters.onlineStatus
    }
    
    const res = await axios.post('/api/devices/list', params)
    devices.value = (res.list || []).map(device => {
      // 解析告警配置JSON
      if (device.alarmConfig) {
        try {
          device.alarmConfigObj = JSON.parse(device.alarmConfig)
        } catch (e) {
          device.alarmConfigObj = null
        }
      }
      return device
    })
    
    // 应用配置状态筛选
    if (filters.configStatus) {
      devices.value = devices.value.filter(device => {
        if (filters.configStatus === 'configured') return device.alarmConfig
        if (filters.configStatus === 'unconfigured') return !device.alarmConfig
        return true
      })
    }
    
    pagination.total = res.total || 0
    
    // 更新统计
    updateStats()
  } catch (error) {
    console.error('加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = () => {
  stats.total = devices.value.length
  stats.configured = devices.value.filter(d => d.alarmConfig).length
  stats.unconfigured = stats.total - stats.configured
}

// 加载产品列表
const loadProducts = async () => {
  try {
    const res = await axios.get('/api/products/list')
    products.value = res || []
  } catch (error) {
    console.error('加载产品列表失败:', error)
  }
}

// 加载分组列表
const loadGroups = async () => {
  try {
    const res = await axios.get('/api/device-groups/list')
    groups.value = res || []
  } catch (error) {
    console.error('加载分组列表失败:', error)
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    const res = await axios.post('/api/users/list', { page: 1, pageSize: 1000 })
    users.value = res.list || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 打开单设备配置弹窗
const openConfigModal = async (device) => {
  configModal.device = device
  configModal.visible = true
  
  // 加载设备产品的物模型属性
  await loadDeviceAttributes(device.productId)
  
  // 如果已有配置，回填数据
  if (device.alarmConfigObj) {
    configModal.form = {
      level: device.alarmConfigObj.level || 'warning',
      conditions: device.alarmConfigObj.conditions || [{ metric: '', operator: '>', threshold: 0 }],
      notifyUsers: device.alarmConfigObj.notifyUsers || [],
      stackMode: device.alarmConfigObj.stackMode !== false
    }
  } else {
    // 重置为默认值
    configModal.form = {
      level: 'warning',
      conditions: [{ metric: '', operator: '>', threshold: 0 }],
      notifyUsers: [],
      stackMode: true
    }
  }
}

// 加载设备物模型属性
const loadDeviceAttributes = async (productId) => {
  try {
    const res = await axios.get(`/api/products/${productId}/attributes`)
    deviceAttributes.value = res || []
  } catch (error) {
    console.error('加载物模型属性失败:', error)
    deviceAttributes.value = []
  }
}

// 添加监控条件
const addCondition = () => {
  configModal.form.conditions.push({ metric: '', operator: '>', threshold: 0 })
}

// 删除监控条件
const removeCondition = (index) => {
  if (configModal.form.conditions.length > 1) {
    configModal.form.conditions.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个监控条件')
  }
}

// 保存单设备配置
const saveConfig = async () => {
  // 验证
  if (!configModal.form.level) {
    ElMessage.warning('请选择告警级别')
    return
  }
  if (configModal.form.conditions.length === 0 || !configModal.form.conditions[0].metric) {
    ElMessage.warning('请配置至少一个监控条件')
    return
  }
  if (configModal.form.notifyUsers.length === 0) {
    ElMessage.warning('请选择通知人员')
    return
  }
  
  try {
    await axios.post('/api/device-alarm/configure', {
      deviceId: configModal.device.id,
      alarmConfig: {
        level: configModal.form.level,
        conditions: configModal.form.conditions,
        notifyUsers: configModal.form.notifyUsers,
        stackMode: configModal.form.stackMode
      },
      enabled: true
    })
    
    ElMessage.success('配置成功')
    configModal.visible = false
    loadDevices()
  } catch (error) {
    console.error('配置失败:', error)
    ElMessage.error('配置失败：' + (error.response?.data?.message || error.message))
  }
}

// 切换告警启用状态
const toggleAlarmEnabled = async (device) => {
  try {
    await axios.post(`/api/device-alarm/toggle/${device.id}`, null, {
      params: { enabled: device.alarmEnabled }
    })
    ElMessage.success(device.alarmEnabled ? '已启用告警' : '已禁用告警')
  } catch (error) {
    console.error('切换告警状态失败:', error)
    // 恢复原状态
    device.alarmEnabled = !device.alarmEnabled
    ElMessage.error('操作失败')
  }
}

// 打开批量配置弹窗
const openBatchModal = () => {
  batchModal.visible = true
  batchModal.productId = null
  batchModal.groupId = null
  batchModal.availableDevices = []
  batchModal.selectedDeviceIds = []
  batchModal.form = {
    level: 'warning',
    notifyUsers: [],
    stackMode: true
  }
  batchModal.condition = { metric: '', operator: '>', threshold: 0 }
}

// 产品切换时加载设备
const onProductChange = async () => {
  if (batchModal.productId) {
    await loadDeviceAttributes(batchModal.productId)
    batchDeviceAttributes.value = deviceAttributes.value
    await loadBatchDevices()
  }
}

// 加载批量配置可选设备
const loadBatchDevices = async () => {
  if (!batchModal.productId) return
  
  try {
    const params = {
      page: 1,
      pageSize: 10000,
      productId: batchModal.productId,
      groupId: batchModal.groupId
    }
    
    const res = await axios.post('/api/devices/list', params)
    batchModal.availableDevices = (res.list || []).map(device => {
      if (device.alarmConfig) {
        try {
          device.alarmConfigObj = JSON.parse(device.alarmConfig)
        } catch (e) {}
      }
      return { ...device, selected: false }
    })
  } catch (error) {
    console.error('加载设备失败:', error)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  batchModal.availableDevices.forEach(device => {
    device.selected = batchModal.selectAll
  })
  updateSelectedDevices()
}

// 更新已选设备
const updateSelectedDevices = () => {
  batchModal.selectedDeviceIds = batchModal.availableDevices
    .filter(d => d.selected)
    .map(d => d.id)
  
  batchModal.configuredCount = batchModal.availableDevices
    .filter(d => d.selected && d.alarmConfig)
    .length
  
  batchModal.selectAll = batchModal.selectedDeviceIds.length === batchModal.availableDevices.length
}

// 获取批量配置预览
const getBatchPreview = () => {
  const selectedDevices = batchModal.availableDevices.filter(d => d.selected)
  let preview = `将为以下 ${selectedDevices.length} 台设备配置告警阈值：\n\n`
  
  selectedDevices.forEach(device => {
    const status = device.alarmConfig ? '[已配置→将被覆盖]' : '[未配置→新增]'
    preview += `• ${device.deviceName} (${device.deviceCode}) ${status}\n`
  })
  
  const metricLabel = batchDeviceAttributes.value.find(a => a.identifier === batchModal.condition.metric)?.name || batchModal.condition.metric
  const userNames = batchModal.form.notifyUsers.map(id => users.value.find(u => u.id === id)?.username).filter(Boolean).join(', ')
  
  preview += `\n配置内容：告警级别=${getLevelLabel(batchModal.form.level)}，`
  preview += `条件=${metricLabel} ${batchModal.condition.operator} ${batchModal.condition.threshold}，`
  preview += `通知人员=${userNames || '未选择'}`
  
  return preview
}

// 保存批量配置
const saveBatchConfig = async () => {
  if (batchModal.selectedDeviceIds.length === 0) {
    ElMessage.warning('请至少选择一个设备')
    return
  }
  if (!batchModal.condition.metric) {
    ElMessage.warning('请配置监控条件')
    return
  }
  if (batchModal.form.notifyUsers.length === 0) {
    ElMessage.warning('请选择通知人员')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认为 ${batchModal.selectedDeviceIds.length} 台设备批量配置告警阈值？\n\n注意：已配置设备的原有阈值将被覆盖！`,
      '确认操作',
      { type: 'warning' }
    )
    
    await axios.post('/api/device-alarm/configure', {
      deviceIds: batchModal.selectedDeviceIds,
      alarmConfig: {
        level: batchModal.form.level,
        conditions: [batchModal.condition],
        notifyUsers: batchModal.form.notifyUsers,
        stackMode: batchModal.form.stackMode
      },
      enabled: true
    })
    
    ElMessage.success('批量配置成功')
    batchModal.visible = false
    loadDevices()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量配置失败:', error)
      ElMessage.error('配置失败：' + (error.response?.data?.message || error.message))
    }
  }
}

// 辅助方法
const getMetricLabel = (identifier) => {
  const attr = deviceAttributes.value.find(a => a.identifier === identifier)
  return attr?.name || identifier
}

const getLevelIcon = (level) => {
  const icons = { critical: '🔴', warning: '🟡', info: '🔵' }
  return icons[level] || '-'
}

const getLevelLabel = (level) => {
  const labels = { critical: '严重', warning: '警告', info: '提示' }
  return labels[level] || level
}

const getUserNames = (userIds) => {
  if (!userIds || userIds.length === 0) return '-'
  return userIds
    .map(id => users.value.find(u => u.id === id)?.username)
    .filter(Boolean)
    .join(', ')
}

const getConditionText = (device) => {
  if (!device.alarmConfigObj || !device.alarmConfigObj.conditions || device.alarmConfigObj.conditions.length === 0) {
    return ''
  }
  const c = device.alarmConfigObj.conditions[0]
  const metricLabel = batchDeviceAttributes.value.find(a => a.identifier === c.metric)?.name || c.metric
  return `${metricLabel}${c.operator}${c.threshold}`
}

// 初始化
onMounted(() => {
  loadDevices()
  loadProducts()
  loadGroups()
  loadUsers()
})
</script>

<style scoped>
.alarm-threshold-config {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

.stats-quick {
  display: flex;
  gap: 20px;
}

.stat-item {
  font-size: 14px;
  color: #666;
}

.stat-item strong {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-left: 4px;
}

.stat-item.active strong {
  color: #67c23a;
}

.stat-item.inactive strong {
  color: #e6a23c;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}

.status-dot.online {
  background: #67c23a;
}

.status-dot.offline {
  background: #e6a23c;
}

.condition-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.condition-text .metric {
  font-weight: 600;
  color: #667eea;
}

.condition-text .operator {
  color: #e6a23c;
  font-weight: 600;
}

.condition-text .value {
  font-weight: 600;
  color: #f56c6c;
}

.info-box {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #666;
}

.info-value {
  font-weight: 600;
  color: #1d1d1f;
}

.condition-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
  margin-bottom: 12px;
}

.condition-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.select-all-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #ecf5ff;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 13px;
}

.select-count {
  color: #606266;
  font-weight: 500;
}

.device-checkbox-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 8px;
  background: #fafafa;
}

.device-checkbox-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  margin-bottom: 6px;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e4e7ed;
}

.device-checkbox-item:hover {
  background: #f5f7fa;
  border-color: #409eff;
}

.device-checkbox-item:last-child {
  margin-bottom: 0;
}

.device-checkbox-info {
  flex: 1;
  margin-left: 12px;
}

.device-checkbox-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.device-checkbox-meta {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}
</style>
