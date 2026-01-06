<template>
  <div class="alarm-config-page">
    <!-- 页面标题和统计 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">设备告警阈值配置</h1>
        <div class="stats-bar">
          <div class="stat-box">
            <div class="stat-label">总设备</div>
            <div class="stat-number">{{ stats.total }}</div>
          </div>
          <div class="stat-box stat-success">
            <div class="stat-label">已配置</div>
            <div class="stat-number">{{ stats.configured }}</div>
          </div>
          <div class="stat-box stat-warning">
            <div class="stat-label">未配置</div>
            <div class="stat-number">{{ stats.unconfigured }}</div>
          </div>
        </div>
      </div>
      <el-button type="primary" class="batch-btn" @click="openBatchModal">
        批量配置
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索设备名称/设备编码"
        class="search-input"
        clearable
        @change="loadDevices"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select 
        v-model="filters.productId" 
        placeholder="产品型号" 
        clearable 
        @change="loadDevices" 
        class="filter-select"
      >
        <el-option v-for="product in products" :key="product.id" :label="product.productName" :value="product.id" />
      </el-select>

      <GroupSelector 
        v-model="filters.groupId" 
        placeholder="设备分组" 
        clearable 
        @change="loadDevices" 
        class="filter-select"
      />

      <el-select 
        v-model="filters.configStatus" 
        placeholder="配置状态" 
        clearable 
        @change="loadDevices" 
        class="filter-select"
      >
        <el-option label="已配置" value="configured" />
        <el-option label="未配置" value="unconfigured" />
      </el-select>

      <el-select 
        v-model="filters.onlineStatus" 
        placeholder="在线状态" 
        clearable 
        @change="loadDevices" 
        class="filter-select"
      >
        <el-option label="在线" :value="1" />
        <el-option label="离线" :value="0" />
      </el-select>
    </div>

    <!-- 设备列表表格 -->
    <div class="table-card">
      <el-table 
        :data="devices" 
        v-loading="loading"
        class="device-table"
        stripe
      >
        <el-table-column prop="deviceName" label="设备名称" width="200">
          <template #default="{ row }">
            <div class="device-name-cell">
              <span :class="['status-indicator', row.status === 1 ? 'online' : 'offline']"></span>
              <span class="device-name-text">{{ row.deviceName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="deviceCode" label="设备编码" width="160">
          <template #default="{ row }">
            <code class="device-code-text">{{ row.deviceCode }}</code>
          </template>
        </el-table-column>
        
        <el-table-column prop="productName" label="产品型号" width="140" />
        <el-table-column prop="groupName" label="所属分组" width="120" />
        
        <el-table-column label="配置状态" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.alarmConfig" class="status-text status-success">已配置</span>
            <span v-else class="status-text status-gray">未配置</span>
          </template>
        </el-table-column>
        
        <el-table-column label="告警条件" min-width="280">
          <template #default="{ row }">
            <div v-if="row.alarmConfigObj && row.alarmConfigObj.conditions && row.alarmConfigObj.conditions.length > 0" class="condition-row">
              <template v-for="(condition, idx) in row.alarmConfigObj.conditions" :key="idx">
                <span v-if="idx > 0" class="or-text">或</span>
                <span class="metric-name">{{ getMetricLabel(condition.metric, row.productId) }}</span>
                <span class="operator-symbol">{{ condition.operator }}</span>
                <span class="threshold-value">{{ condition.threshold }}{{ getMetricUnit(condition.metric, row.productId) }}</span>
              </template>
            </div>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="告警级别" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.alarmConfigObj" class="level-icon-text">
              {{ getLevelIcon(row.alarmConfigObj.level) }}
            </span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="通知人员" min-width="140">
          <template #default="{ row }">
            <span v-if="row.alarmConfigObj && row.alarmConfigObj.notifyUsers && row.alarmConfigObj.notifyUsers.length > 0" class="notify-text">
              {{ getUserNames(row.alarmConfigObj.notifyUsers) }}
            </span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="启用" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-if="row.alarmConfig"
              v-model="row.alarmEnabled"
              @change="handleToggleAlarmEnabled(row)"
            />
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openConfigModal(row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadDevices"
        @current-change="loadDevices"
      />
    </div>

    <!-- 单设备配置弹窗 -->
    <el-dialog
      v-model="configModal.visible"
      title="配置设备告警阈值"
      width="640px"
      :close-on-click-modal="false"
    >
      <div class="dialog-body">
        <!-- 设备信息 -->
        <div class="info-section">
          <h3 class="section-title">设备信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">设备名称</span>
              <span class="info-value">{{ configModal.device?.deviceName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">设备编码</span>
              <code class="info-value code">{{ configModal.device?.deviceCode }}</code>
            </div>
            <div class="info-item">
              <span class="info-label">产品型号</span>
              <span class="info-value">{{ configModal.device?.productName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">所属分组</span>
              <span class="info-value">{{ configModal.device?.groupName }}</span>
            </div>
          </div>
        </div>

        <!-- 告警级别 -->
        <div class="form-section">
          <h3 class="section-title">
            告警级别
            <span class="required">*</span>
          </h3>
          <el-radio-group v-model="configModal.form.level" class="level-group">
            <el-radio label="critical" class="level-option">
              <span class="level-emoji">🔴</span>
              <span>严重</span>
            </el-radio>
            <el-radio label="warning" class="level-option">
              <span class="level-emoji">🟡</span>
              <span>警告</span>
            </el-radio>
            <el-radio label="info" class="level-option">
              <span class="level-emoji">🔵</span>
              <span>提示</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 监控条件 -->
        <div class="form-section">
          <div class="section-header">
            <h3 class="section-title">监控条件</h3>
            <span class="section-note">多个条件为OR关系</span>
          </div>
          <div class="conditions-list">
            <div v-for="(condition, index) in configModal.form.conditions" :key="index" class="condition-card">
              <div class="condition-header">
                <span class="condition-title">条件 {{ index + 1 }}</span>
                <el-button 
                  v-if="configModal.form.conditions.length > 1"
                  type="danger" 
                  link 
                  size="small" 
                  @click="removeCondition(index)"
                >
                  删除
                </el-button>
              </div>
              <div class="condition-fields">
                <el-select 
                  v-model="condition.metric" 
                  placeholder="选择监控指标" 
                  class="field-select"
                >
                  <el-option 
                    v-for="attr in deviceAttributes" 
                    :key="attr.addr" 
                    :label="attr.attrName" 
                    :value="attr.addr" 
                  />
                </el-select>
                <el-select v-model="condition.operator" class="field-operator">
                  <el-option label=">" value=">" />
                  <el-option label="<" value="<" />
                  <el-option label="=" value="=" />
                </el-select>
                <el-input-number 
                  v-model="condition.threshold" 
                  :precision="2" 
                  :step="0.1"
                  class="field-number"
                />
                <span v-if="condition.metric" class="field-unit">
                  {{ getMetricUnit(condition.metric, configModal.device?.productId) }}
                </span>
              </div>
            </div>
          </div>
          <el-button 
            @click="addCondition" 
            class="add-btn"
            :icon="Plus"
            plain
          >
            添加监控条件
          </el-button>
        </div>

        <!-- 通知设置 -->
        <div class="form-section">
          <h3 class="section-title">
            通知设置
          </h3>
          <div class="form-field">
            <label class="field-label">
              通知人员
              <span class="required">*</span>
            </label>
            <el-select 
              v-model="configModal.form.notifyUsers" 
              multiple 
              placeholder="请选择通知人员"
              class="field-select-full"
            >
              <el-option 
                v-for="user in users" 
                :key="user.id" 
                :label="user.realName || user.username" 
                :value="user.id" 
              />
            </el-select>
            <div class="field-hint">按住Ctrl键可多选</div>
          </div>
          <div class="form-field">
            <label class="field-label">告警堆叠</label>
            <div class="switch-field">
              <el-switch v-model="configModal.form.stackMode" />
              <span class="switch-label">开启后，恢复前不会重复告警</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="configModal.visible = false">取消</el-button>
          <el-button type="primary" @click="saveConfig">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量配置弹窗 -->
    <el-dialog 
      v-model="batchModal.visible" 
      title="批量配置告警阈值" 
      width="680px"
      :close-on-click-modal="false"
    >
      <div class="dialog-body">
        <el-alert 
          type="warning" 
          :closable="false" 
          class="warning-box"
        >
          <template #title>
            <strong>重要提示</strong>
            <div class="alert-desc">
              批量配置将<strong>覆盖</strong>已选设备的原有配置，以本次设置为准。已配置设备的旧阈值将失效。
            </div>
          </template>
        </el-alert>

        <div class="form-section">
          <div class="form-field">
            <label class="field-label">
              选择产品
              <span class="required">*</span>
            </label>
            <el-select 
              v-model="batchModal.productId" 
              placeholder="请先选择产品型号" 
              @change="onProductChange"
              class="field-select-full"
            >
              <el-option v-for="product in products" :key="product.id" :label="product.productName" :value="product.id" />
            </el-select>
            <div class="field-hint">选择产品后，监控条件将根据该产品物模型动态加载</div>
          </div>

          <div class="form-field">
            <label class="field-label">设备分组</label>
            <GroupSelector 
              v-model="batchModal.groupId" 
              placeholder="全部分组" 
              clearable 
              @change="loadBatchDevices" 
              class="field-select-full"
            />
            <div class="field-hint">筛选该产品下指定分组的设备</div>
          </div>

          <div class="form-field">
            <label class="field-label">
              选择设备
              <span class="required">*</span>
            </label>
            <div class="device-select-area">
              <div class="select-header">
                <el-checkbox v-model="batchModal.selectAll" @change="toggleSelectAll">
                  全选/取消全选
                </el-checkbox>
                <span class="select-info">
                  已选择 <strong>{{ batchModal.selectedDeviceIds.length }}</strong> 台设备
                  <span v-if="batchModal.configuredCount > 0" class="warn-text">
                    （其中 {{ batchModal.configuredCount }} 台已配置）
                  </span>
                </span>
              </div>
              <div class="device-list">
                <div 
                  v-for="device in batchModal.availableDevices" 
                  :key="device.id" 
                  class="device-item"
                  :class="{ 'is-selected': device.selected }"
                >
                  <el-checkbox v-model="device.selected" @change="updateSelectedDevices" />
                  <div class="device-details">
                    <div class="device-title-row">
                      <span class="device-title-text">{{ device.deviceName }}</span>
                      <el-tag v-if="device.alarmConfig" type="success" size="small">已配置</el-tag>
                    </div>
                    <div class="device-meta-text">
                      {{ device.deviceCode }} | {{ device.groupName || '未分组' }} | 
                      <span :class="device.status === 1 ? 'text-success' : 'text-gray'">
                        {{ device.status === 1 ? '在线' : '离线' }}
                      </span>
                      <span v-if="device.alarmConfig" class="text-primary">
                        | 当前：{{ getConditionText(device) }}
                      </span>
                      <span v-else class="text-gray">| 未配置</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="divider-line"></div>

        <div class="form-section">
          <h3 class="section-title">统一配置</h3>
          
          <div class="form-field">
            <label class="field-label">
              告警级别
              <span class="required">*</span>
            </label>
            <el-radio-group v-model="batchModal.form.level" class="level-group">
              <el-radio label="critical" class="level-option">
                <span class="level-emoji">🔴</span>
                <span>严重</span>
              </el-radio>
              <el-radio label="warning" class="level-option">
                <span class="level-emoji">🟡</span>
                <span>警告</span>
              </el-radio>
              <el-radio label="info" class="level-option">
                <span class="level-emoji">🔵</span>
                <span>提示</span>
              </el-radio>
            </el-radio-group>
          </div>

          <div class="form-field">
            <label class="field-label">
              监控条件
              <span class="required">*</span>
            </label>
            <div class="condition-fields">
              <el-select 
                v-model="batchModal.condition.metric" 
                placeholder="选择监控指标" 
                class="field-select"
              >
                <el-option 
                  v-for="attr in batchDeviceAttributes" 
                  :key="attr.addr" 
                  :label="attr.attrName" 
                  :value="attr.addr" 
                />
              </el-select>
              <el-select v-model="batchModal.condition.operator" class="field-operator">
                <el-option label=">" value=">" />
                <el-option label="<" value="<" />
                <el-option label="=" value="=" />
              </el-select>
              <el-input-number 
                v-model="batchModal.condition.threshold" 
                :precision="2" 
                :step="0.1"
                class="field-number"
              />
              <span v-if="batchModal.condition.metric" class="field-unit">
                {{ getMetricUnit(batchModal.condition.metric, batchModal.productId) }}
              </span>
            </div>
          </div>

          <div class="form-field">
            <label class="field-label">
              通知人员
              <span class="required">*</span>
            </label>
            <el-select 
              v-model="batchModal.form.notifyUsers" 
              multiple 
              placeholder="请选择通知人员"
              class="field-select-full"
            >
              <el-option 
                v-for="user in users" 
                :key="user.id" 
                :label="user.realName || user.username" 
                :value="user.id" 
              />
            </el-select>
            <div class="field-hint">按住Ctrl键可多选</div>
          </div>

          <div class="form-field">
            <label class="field-label">预览</label>
            <el-input
              :model-value="getBatchPreview()"
              type="textarea"
              :rows="6"
              readonly
              class="preview-area"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchModal.visible = false">取消</el-button>
          <el-button type="primary" @click="saveBatchConfig">确认配置</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getDeviceList } from '@/api/device'
import { getProductList } from '@/api/product'
import { getProductAttributes } from '@/api/product'
import { getUserList } from '@/api/user'
import { configureAlarm, toggleAlarmEnabled } from '@/api/alarm'
import GroupSelector from '@/components/GroupSelector.vue'

// 数据状态
const loading = ref(false)
const devices = ref([])
const products = ref([])
const users = ref([])
const deviceAttributes = ref([])
const batchDeviceAttributes = ref([])
const productAttributesCache = ref({})

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
  pageSize: 20,
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
      keyword: filters.keyword || undefined,
      productId: filters.productId || undefined,
      groupId: filters.groupId || undefined,
      status: filters.onlineStatus !== null ? filters.onlineStatus : undefined
    }
    
    const res = await getDeviceList(params)
    if (res && res.list) {
      let deviceList = res.list || []
      
      deviceList = deviceList.map(device => {
        if (device.alarmConfig) {
          try {
            device.alarmConfigObj = JSON.parse(device.alarmConfig)
          } catch (e) {
            device.alarmConfigObj = null
          }
        }
        return device
      })
      
      if (filters.configStatus) {
        deviceList = deviceList.filter(device => {
          if (filters.configStatus === 'configured') return device.alarmConfig
          if (filters.configStatus === 'unconfigured') return !device.alarmConfig
          return true
        })
      }
      
      devices.value = deviceList
      pagination.total = res.total || 0
      updateStats()
    } else {
      devices.value = []
      pagination.total = 0
      updateStats()
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = async () => {
  try {
    const params = {
      page: 1,
      pageSize: 10000,
      keyword: filters.keyword || undefined,
      productId: filters.productId || undefined,
      groupId: filters.groupId || undefined,
      status: filters.onlineStatus !== null ? filters.onlineStatus : undefined
    }
    
    const res = await getDeviceList(params)
    if (res && res.list) {
      const allDevices = res.list || []
      stats.total = allDevices.length
      stats.configured = allDevices.filter(d => d.alarmConfig).length
      stats.unconfigured = stats.total - stats.configured
    } else {
      stats.total = 0
      stats.configured = 0
      stats.unconfigured = 0
    }
  } catch (error) {
    console.error('更新统计数据失败:', error)
  }
}

// 加载产品列表
const loadProducts = async () => {
  try {
    const res = await getProductList({})
    if (res) {
      products.value = res.list || (Array.isArray(res) ? res : [])
    }
  } catch (error) {
    console.error('加载产品列表失败:', error)
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    const res = await getUserList({ page: 1, pageSize: 1000 })
    if (res && res.list) {
      users.value = res.list || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 加载产品物模型属性
const loadProductAttributes = async (productId) => {
  if (!productId) return []
  if (productAttributesCache.value[productId]) {
    return productAttributesCache.value[productId]
  }
  
  try {
    const res = await getProductAttributes(productId)
    if (res) {
      const attrs = Array.isArray(res) ? res : []
      productAttributesCache.value[productId] = attrs
      return attrs
    }
    return []
  } catch (error) {
    console.error('加载物模型属性失败:', error)
    return []
  }
}

// 打开单设备配置弹窗
const openConfigModal = async (device) => {
  configModal.device = device
  configModal.visible = true
  
  const attrs = await loadProductAttributes(device.productId)
  deviceAttributes.value = attrs
  
  if (device.alarmConfigObj) {
    configModal.form = {
      level: device.alarmConfigObj.level || 'warning',
      conditions: device.alarmConfigObj.conditions && device.alarmConfigObj.conditions.length > 0
        ? [...device.alarmConfigObj.conditions]
        : [{ metric: '', operator: '>', threshold: 0 }],
      notifyUsers: device.alarmConfigObj.notifyUsers || [],
      stackMode: device.alarmConfigObj.stackMode !== false
    }
  } else {
    configModal.form = {
      level: 'warning',
      conditions: [{ metric: '', operator: '>', threshold: 0 }],
      notifyUsers: [],
      stackMode: true
    }
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
  if (!configModal.form.level) {
    ElMessage.warning('请选择告警级别')
    return
  }
  if (configModal.form.conditions.length === 0 || !configModal.form.conditions[0].metric) {
    ElMessage.warning('请配置至少一个监控条件')
    return
  }
  const hasEmptyMetric = configModal.form.conditions.some(c => !c.metric)
  if (hasEmptyMetric) {
    ElMessage.warning('请为所有条件选择监控指标')
    return
  }
  if (configModal.form.notifyUsers.length === 0) {
    ElMessage.warning('请选择通知人员')
    return
  }
  
  try {
    await configureAlarm({
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
const handleToggleAlarmEnabled = async (device) => {
  try {
    await toggleAlarmEnabled(device.id, device.alarmEnabled)
    ElMessage.success(device.alarmEnabled ? '已启用告警' : '已禁用告警')
  } catch (error) {
    console.error('切换告警状态失败:', error)
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
  batchModal.selectAll = false
  batchModal.configuredCount = 0
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
    const attrs = await loadProductAttributes(batchModal.productId)
    batchDeviceAttributes.value = attrs
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
      groupId: batchModal.groupId || undefined
    }
    
    const res = await getDeviceList(params)
    if (res && res.list) {
      batchModal.availableDevices = (res.list || []).map(device => {
        if (device.alarmConfig) {
          try {
            device.alarmConfigObj = JSON.parse(device.alarmConfig)
          } catch (e) {}
        }
        return { ...device, selected: false }
      })
      updateSelectedDevices()
    }
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
  
  batchModal.selectAll = batchModal.selectedDeviceIds.length === batchModal.availableDevices.length && batchModal.availableDevices.length > 0
}

// 获取批量配置预览
const getBatchPreview = () => {
  const selectedDevices = batchModal.availableDevices.filter(d => d.selected)
  let preview = `将为以下 ${selectedDevices.length} 台设备配置告警阈值：\n\n`
  
  selectedDevices.forEach(device => {
    const status = device.alarmConfig ? '[已配置→将被覆盖]' : '[未配置→新增]'
    preview += `• ${device.deviceName} (${device.deviceCode}) ${status}\n`
  })
  
  const metricLabel = batchDeviceAttributes.value.find(a => a.addr === batchModal.condition.metric)?.attrName || batchModal.condition.metric
  const unit = getMetricUnit(batchModal.condition.metric, batchModal.productId)
  const userNames = batchModal.form.notifyUsers
    .map(id => {
      const user = users.value.find(u => u.id === id)
      return user ? (user.realName || user.username) : null
    })
    .filter(Boolean)
    .join(', ')
  
  preview += `\n配置内容：告警级别=${getLevelLabel(batchModal.form.level)}，`
  preview += `条件=${metricLabel} ${batchModal.condition.operator} ${batchModal.condition.threshold}${unit}，`
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
    
    await configureAlarm({
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
const getMetricLabel = (identifier, productId) => {
  if (!productId) return identifier
  const attrs = productAttributesCache.value[productId] || []
  const attr = attrs.find(a => a.addr === identifier)
  return attr?.attrName || identifier
}

const getMetricUnit = (identifier, productId) => {
  if (!productId) return ''
  const attrs = productAttributesCache.value[productId] || []
  const attr = attrs.find(a => a.addr === identifier)
  return attr?.unit ? attr.unit : ''
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
    .map(id => {
      const user = users.value.find(u => u.id === id)
      return user ? (user.realName || user.username) : null
    })
    .filter(Boolean)
    .join(', ')
}

const getConditionText = (device) => {
  if (!device.alarmConfigObj || !device.alarmConfigObj.conditions || device.alarmConfigObj.conditions.length === 0) {
    return ''
  }
  const c = device.alarmConfigObj.conditions[0]
  const metricLabel = getMetricLabel(c.metric, device.productId)
  const unit = getMetricUnit(c.metric, device.productId)
  return `${metricLabel}${c.operator}${c.threshold}${unit}`
}

// 初始化
onMounted(() => {
  loadDevices()
  loadProducts()
  loadUsers()
})
</script>

<style scoped>
/* 主容器 */
.alarm-config-page {
  background: #f5f5f7;
  min-height: 100vh;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 页面标题和统计 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 28px 32px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 20px 0;
  letter-spacing: -0.03em;
}

.stats-bar {
  display: flex;
  gap: 32px;
}

.stat-box {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1.2;
}

.stat-success .stat-number {
  color: #34c759;
}

.stat-warning .stat-number {
  color: #ff9500;
}

.batch-btn {
  padding: 10px 24px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 10px;
  background: #007AFF;
  border: none;
  height: auto;
}

.batch-btn:hover {
  background: #0051D5;
}

/* 筛选栏 */
.filter-section {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input,
.filter-select {
  height: 40px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  box-shadow: none;
  padding-left: 36px;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #86868b;
}

.search-input :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  box-shadow: none;
}

.filter-select :deep(.el-input__wrapper:hover) {
  border-color: #86868b;
}

.filter-select :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

/* 表格卡片 */
.table-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
}

.device-table :deep(.el-table__header) {
  background: #fafafa;
}

.device-table :deep(.el-table__header th) {
  background: #fafafa;
  color: #1d1d1f;
  font-weight: 600;
  font-size: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #e5e5e7;
}

.device-table :deep(.el-table__body td) {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f7;
}

.device-table :deep(.el-table__row:hover) {
  background: #fafafa;
}

.device-table :deep(.el-table__row--striped) {
  background: #fafafa;
}

/* 设备名称单元格 */
.device-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator.online {
  background: #34c759;
}

.status-indicator.offline {
  background: #86868b;
}

.device-name-text {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.device-code-text {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #86868b;
  background: #f5f5f7;
  padding: 4px 8px;
  border-radius: 6px;
}

/* 状态文本 */
.status-text {
  font-size: 13px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
}

.status-success {
  color: #34c759;
  background: #f0f9f4;
}

.status-gray {
  color: #86868b;
  background: #f5f5f7;
}

/* 告警条件 */
.condition-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 14px;
}

.or-text {
  color: #86868b;
  font-size: 13px;
  margin: 0 4px;
}

.metric-name {
  font-weight: 600;
  color: #007AFF;
}

.operator-symbol {
  color: #ff9500;
  font-weight: 600;
}

.threshold-value {
  font-weight: 600;
  color: #1d1d1f;
}

.empty-text {
  color: #86868b;
  font-size: 14px;
}

/* 告警级别图标 */
.level-icon-text {
  font-size: 18px;
  line-height: 1;
}

/* 通知人员 */
.notify-text {
  font-size: 14px;
  color: #1d1d1f;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 弹窗样式 */
.dialog-body {
  padding: 0;
}

.info-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.required {
  color: #ff3b30;
  font-size: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  background: #fafafa;
  padding: 20px;
  border-radius: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.info-value.code {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 14px;
}

/* 表单区域 */
.form-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-note {
  font-size: 13px;
  color: #86868b;
}

.level-group {
  display: flex;
  gap: 12px;
}

.level-option {
  padding: 12px 20px;
  border: 1px solid #d2d2d7;
  border-radius: 10px;
  transition: all 0.2s;
}

.level-option:hover {
  border-color: #007AFF;
  background: #f0f7ff;
}

.level-option :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #007AFF;
}

.level-option :deep(.el-radio__input.is-checked) .el-radio__inner {
  border-color: #007AFF;
  background: #007AFF;
}

.level-emoji {
  font-size: 16px;
  margin-right: 6px;
}

/* 监控条件 */
.conditions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.condition-card {
  background: #fafafa;
  padding: 16px;
  border-radius: 10px;
  border-left: 3px solid #007AFF;
}

.condition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.condition-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.condition-fields {
  display: flex;
  gap: 12px;
  align-items: center;
}

.field-select {
  flex: 1;
}

.field-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
  box-shadow: none;
}

.field-operator {
  width: 80px;
}

.field-operator :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
  box-shadow: none;
  text-align: center;
}

.field-number {
  width: 120px;
}

.field-number :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
  box-shadow: none;
}

.field-unit {
  font-size: 15px;
  color: #86868b;
  font-weight: 500;
  min-width: 40px;
}

.add-btn {
  width: 100%;
  padding: 12px;
  border: 1px dashed #d2d2d7;
  border-radius: 8px;
  color: #007AFF;
  font-size: 15px;
  background: white;
}

.add-btn:hover {
  border-color: #007AFF;
  background: #f0f7ff;
}

/* 表单项 */
.form-field {
  margin-bottom: 24px;
}

.form-field:last-child {
  margin-bottom: 0;
}

.field-label {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 10px;
}

.field-select-full {
  width: 100%;
}

.field-select-full :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
  box-shadow: none;
}

.field-hint {
  font-size: 13px;
  color: #86868b;
  margin-top: 6px;
}

.switch-field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-label {
  font-size: 14px;
  color: #1d1d1f;
}

/* 警告提示 */
.warning-box {
  margin-bottom: 24px;
  border-radius: 10px;
  border-left: 3px solid #ff9500;
}

.warning-box :deep(.el-alert__title) {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.alert-desc {
  font-size: 14px;
  color: #1d1d1f;
  margin-top: 6px;
  line-height: 1.5;
}

/* 设备选择区域 */
.device-select-area {
  margin-top: 12px;
}

.select-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f0f7ff;
  border-radius: 8px;
  margin-bottom: 12px;
}

.select-header :deep(.el-checkbox__label) {
  font-size: 14px;
  font-weight: 500;
  color: #007AFF;
}

.select-info {
  font-size: 14px;
  color: #1d1d1f;
}

.select-info strong {
  font-weight: 600;
  color: #007AFF;
}

.warn-text {
  color: #ff9500;
}

.device-list {
  max-height: 320px;
  overflow-y: auto;
  border: 1px solid #e5e5e7;
  border-radius: 8px;
  padding: 8px;
  background: #fafafa;
}

.device-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  margin-bottom: 8px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e5e7;
  transition: all 0.2s;
  cursor: pointer;
}

.device-item:hover {
  border-color: #007AFF;
  background: #f0f7ff;
}

.device-item.is-selected {
  border-color: #007AFF;
  background: #f0f7ff;
}

.device-item:last-child {
  margin-bottom: 0;
}

.device-item :deep(.el-checkbox) {
  margin-right: 12px;
  margin-top: 2px;
}

.device-details {
  flex: 1;
}

.device-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.device-title-text {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
}

.device-meta-text {
  font-size: 13px;
  color: #86868b;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  line-height: 1.5;
}

.text-success {
  color: #34c759;
}

.text-gray {
  color: #86868b;
}

.text-primary {
  color: #007AFF;
}

/* 分隔线 */
.divider-line {
  height: 1px;
  background: #e5e5e7;
  margin: 32px 0;
}

/* 预览文本域 */
.preview-area :deep(.el-textarea__inner) {
  background: #fafafa;
  border: 1px solid #e5e5e7;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  color: #1d1d1f;
  line-height: 1.6;
}

/* 弹窗底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 0 0;
  border-top: 1px solid #e5e5e7;
  margin-top: 24px;
}

.dialog-footer .el-button {
  padding: 10px 20px;
  font-size: 15px;
  border-radius: 8px;
}

.dialog-footer .el-button--default {
  border: 1px solid #d2d2d7;
  color: #1d1d1f;
  background: white;
}

.dialog-footer .el-button--default:hover {
  background: #fafafa;
  border-color: #86868b;
}

.dialog-footer .el-button--primary {
  background: #007AFF;
  border-color: #007AFF;
}

.dialog-footer .el-button--primary:hover {
  background: #0051D5;
  border-color: #0051D5;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 24px 28px 20px;
  border-bottom: 1px solid #e5e5e7;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

:deep(.el-dialog__body) {
  padding: 24px 28px;
}

:deep(.el-switch__core) {
  width: 44px;
  height: 24px;
}

:deep(.el-switch__core::after) {
  width: 20px;
  height: 20px;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #007AFF;
}
</style>
