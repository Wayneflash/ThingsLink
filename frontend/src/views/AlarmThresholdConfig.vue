<template>
  <div class="alarm-config-page">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <h2 class="page-title">⚙️ 设备告警阈值配置</h2>
      <div class="stats-quick">
        <span class="stat-item">总设备 <strong>{{ stats.total }}</strong></span>
        <span class="stat-item active">已配置 <strong>{{ stats.configured }}</strong></span>
        <span class="stat-item inactive">未配置 <strong>{{ stats.unconfigured }}</strong></span>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索设备名称/设备编码"
        class="search-input"
        clearable
        @keyup.enter="loadDevices"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select 
        v-model="filters.productId" 
        placeholder="产品型号" 
        clearable 
        class="filter-select"
        style="width: 150px;"
      >
        <el-option v-for="product in products" :key="product.id" :label="product.productName" :value="product.id" />
      </el-select>

      <GroupSelector 
        v-model="filters.groupId" 
        placeholder="设备分组" 
        clearable 
        class="filter-select"
        style="width: 150px;"
      />

      <el-select 
        v-model="filters.configStatus" 
        placeholder="配置状态" 
        clearable 
        class="filter-select"
        style="width: 130px;"
      >
        <el-option label="已配置" value="configured" />
        <el-option label="未配置" value="unconfigured" />
      </el-select>

      <el-select 
        v-model="filters.onlineStatus" 
        placeholder="在线状态" 
        clearable 
        class="filter-select"
        style="width: 130px;"
      >
        <el-option label="在线" :value="1" />
        <el-option label="离线" :value="0" />
      </el-select>

      <el-button type="primary" @click="loadDevices" :icon="Search">查询</el-button>
      <el-button type="primary" @click="openBatchModal" :icon="Plus">批量配置</el-button>
    </div>

    <!-- 设备列表 -->
    <div class="table-container">
      <el-table 
        :data="devices" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="deviceName" label="设备名称" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="device-name-cell">
              <span :class="['status-dot', row.status === 1 ? 'online' : 'offline']"></span>
              <span class="device-name-text">{{ row.deviceName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="deviceCode" label="设备编码" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="device-code-text">{{ row.deviceCode }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="在线状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="['status-text', row.status === 1 ? 'status-online' : 'status-offline']">
              {{ row.status === 1 ? '在线' : '离线' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="productName" label="产品型号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="groupName" label="所属分组" min-width="110" show-overflow-tooltip />
        
        <el-table-column label="配置状态" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.alarmConfig" class="tag configured">✓ 已配置</span>
            <span v-else class="tag unconfigured">✗ 未配置</span>
          </template>
        </el-table-column>
        
        <el-table-column label="告警条件" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div v-if="row.alarmConfigObj && row.alarmConfigObj.conditions && row.alarmConfigObj.conditions.length > 0" class="condition-text">
              <template v-for="(condition, idx) in row.alarmConfigObj.conditions" :key="idx">
                <span v-if="idx > 0" class="condition-separator">或</span>
                <span class="metric">{{ getMetricLabel(condition.metric, row.productId) }}</span>
                <span class="operator">{{ condition.operator }}</span>
                <span class="value">{{ condition.threshold }}{{ getMetricUnit(condition.metric, row.productId) }}</span>
              </template>
            </div>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="告警级别" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.alarmConfigObj" class="level-badge">
              {{ getLevelIcon(row.alarmConfigObj.level) }}
            </span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="通知人员" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.alarmConfigObj && row.alarmConfigObj.notifyUsers && row.alarmConfigObj.notifyUsers.length > 0" class="notify-user-text">
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
        
        <el-table-column label="操作" width="90" fixed="right">
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
      width="900px"
      :close-on-click-modal="false"
      top="3vh"
      class="config-dialog"
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
            <span class="section-note">每个物模型属性只能配置一个条件</span>
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
                  @change="onMetricChange(index)"
                >
                  <el-option 
                    v-for="attr in getAvailableMetrics(index)" 
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
            :disabled="!canAddCondition"
          >
            添加监控条件
          </el-button>
          <div v-if="!canAddCondition" class="field-hint" style="margin-top: 8px;">
            所有可用的物模型属性已配置完成
          </div>
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
              v-model="configModal.form.notifyUser" 
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
      title="批量配置" 
      width="1100px"
      :close-on-click-modal="false"
      top="2vh"
      class="batch-dialog"
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
            <div class="section-header">
              <label class="field-label">
                监控条件
                <span class="required">*</span>
              </label>
              <span class="section-note">每个物模型属性只能配置一个条件</span>
            </div>
            <div class="conditions-list">
              <div v-for="(condition, index) in batchModal.form.conditions" :key="index" class="condition-card">
                <div class="condition-header">
                  <span class="condition-title">条件 {{ index + 1 }}</span>
                  <el-button 
                    v-if="batchModal.form.conditions.length > 1"
                    type="danger" 
                    link 
                    size="small" 
                    @click="removeBatchCondition(index)"
                  >
                    删除
                  </el-button>
                </div>
                <div class="condition-fields">
                  <el-select 
                    v-model="condition.metric" 
                    placeholder="选择监控指标" 
                    class="field-select"
                    @change="onBatchMetricChange(index)"
                  >
                    <el-option 
                      v-for="attr in getAvailableBatchMetrics(index)" 
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
                    {{ getMetricUnit(condition.metric, batchModal.productId) }}
                  </span>
                </div>
              </div>
            </div>
            <el-button 
              @click="addBatchCondition" 
              class="add-btn"
              :icon="Plus"
              plain
              :disabled="!canAddBatchCondition"
            >
              添加监控条件
            </el-button>
            <div v-if="!canAddBatchCondition" class="field-hint" style="margin-top: 8px;">
              所有可用的物模型属性已配置完成
            </div>
          </div>

          <div class="form-field">
            <label class="field-label">
              通知人员
              <span class="required">*</span>
            </label>
            <el-select 
              v-model="batchModal.form.notifyUser" 
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
          </div>

          <div class="form-field">
            <label class="field-label">预览</label>
            <el-input
              :model-value="getBatchPreview()"
              type="textarea"
              :rows="4"
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
import { ref, reactive, onMounted, computed } from 'vue'
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
    notifyUser: null,
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
  form: {
    level: 'warning',
    conditions: [{ metric: '', operator: '>', threshold: 0 }],
    notifyUser: null,
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
      
      // 预加载所有产品的物模型属性，确保告警条件能正确显示名称
      const productIds = [...new Set(deviceList.map(d => d.productId).filter(Boolean))]
      await Promise.all(productIds.map(pid => loadProductAttributes(pid)))
      
      updateStats()
    } else {
      devices.value = []
      pagination.total = 0
      updateStats()
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '加载设备列表失败'
    ElMessage.error(errorMsg)
    devices.value = []
    pagination.total = 0
    stats.total = 0
    stats.configured = 0
    stats.unconfigured = 0
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
      notifyUser: device.alarmConfigObj.notifyUsers && device.alarmConfigObj.notifyUsers.length > 0 
        ? device.alarmConfigObj.notifyUsers[0] 
        : null,
      stackMode: device.alarmConfigObj.stackMode !== false
    }
  } else {
    configModal.form = {
      level: 'warning',
      conditions: [{ metric: '', operator: '>', threshold: 0 }],
      notifyUser: null,
      stackMode: true
    }
  }
}

// 获取可用的物模型属性（排除已选择的）
const getAvailableMetrics = (currentIndex) => {
  const usedMetrics = configModal.form.conditions
    .map((c, idx) => idx !== currentIndex ? c.metric : null)
    .filter(Boolean)
  return deviceAttributes.value.filter(attr => !usedMetrics.includes(attr.addr))
}

// 检查是否可以添加条件
const canAddCondition = computed(() => {
  const usedMetrics = configModal.form.conditions.map(c => c.metric).filter(Boolean)
  return usedMetrics.length < deviceAttributes.value.length
})

// 物模型属性变化时的处理
const onMetricChange = (index) => {
  // 如果选择了已使用的属性，提示用户
  const currentMetric = configModal.form.conditions[index].metric
  const duplicateIndex = configModal.form.conditions.findIndex((c, idx) => 
    idx !== index && c.metric === currentMetric && currentMetric
  )
  if (duplicateIndex !== -1) {
    ElMessage.warning('该物模型属性已被其他条件使用，请选择其他属性')
    configModal.form.conditions[index].metric = ''
  }
}

// 添加监控条件
const addCondition = () => {
  if (canAddCondition.value) {
    configModal.form.conditions.push({ metric: '', operator: '>', threshold: 0 })
  } else {
    ElMessage.warning('所有可用的物模型属性已配置完成')
  }
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
  // 检查是否有重复的物模型属性
  const metrics = configModal.form.conditions.map(c => c.metric).filter(Boolean)
  const uniqueMetrics = new Set(metrics)
  if (metrics.length !== uniqueMetrics.size) {
    ElMessage.warning('每个物模型属性只能配置一个条件，请检查是否有重复')
    return
  }
  
  if (!configModal.form.notifyUser) {
    ElMessage.warning('请选择通知人员')
    return
  }
  
  try {
    await configureAlarm({
      deviceId: configModal.device.id,
      alarmConfig: {
        level: configModal.form.level,
        conditions: configModal.form.conditions,
        notifyUsers: [configModal.form.notifyUser], // 转换为数组
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
    conditions: [{ metric: '', operator: '>', threshold: 0 }],
    notifyUser: null,
    stackMode: true
  }
}

// 批量配置：获取可用的物模型属性（排除已选择的）
const getAvailableBatchMetrics = (currentIndex) => {
  const usedMetrics = batchModal.form.conditions
    .map((c, idx) => idx !== currentIndex ? c.metric : null)
    .filter(Boolean)
  return batchDeviceAttributes.value.filter(attr => !usedMetrics.includes(attr.addr))
}

// 批量配置：检查是否可以添加条件
const canAddBatchCondition = computed(() => {
  const usedMetrics = batchModal.form.conditions.map(c => c.metric).filter(Boolean)
  return usedMetrics.length < batchDeviceAttributes.value.length
})

// 批量配置：物模型属性变化时的处理
const onBatchMetricChange = (index) => {
  const currentMetric = batchModal.form.conditions[index].metric
  const duplicateIndex = batchModal.form.conditions.findIndex((c, idx) => 
    idx !== index && c.metric === currentMetric && currentMetric
  )
  if (duplicateIndex !== -1) {
    ElMessage.warning('该物模型属性已被其他条件使用，请选择其他属性')
    batchModal.form.conditions[index].metric = ''
  }
}

// 批量配置：添加监控条件
const addBatchCondition = () => {
  if (canAddBatchCondition.value) {
    batchModal.form.conditions.push({ metric: '', operator: '>', threshold: 0 })
  } else {
    ElMessage.warning('所有可用的物模型属性已配置完成')
  }
}

// 批量配置：删除监控条件
const removeBatchCondition = (index) => {
  if (batchModal.form.conditions.length > 1) {
    batchModal.form.conditions.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个监控条件')
  }
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
  
  const conditionsText = batchModal.form.conditions
    .filter(c => c.metric)
    .map(c => {
      const metricLabel = batchDeviceAttributes.value.find(a => a.addr === c.metric)?.attrName || c.metric
      const unit = getMetricUnit(c.metric, batchModal.productId)
      return `${metricLabel} ${c.operator} ${c.threshold}${unit}`
    })
    .join(' 或 ')
  
  const notifyUser = users.value.find(u => u.id === batchModal.form.notifyUser)
  const userName = notifyUser ? (notifyUser.realName || notifyUser.username) : '未选择'
  
  preview += `\n配置内容：告警级别=${getLevelLabel(batchModal.form.level)}，`
  preview += `条件=${conditionsText || '未配置'}，`
  preview += `通知人员=${userName}`
  
  return preview
}

// 保存批量配置
const saveBatchConfig = async () => {
  if (batchModal.selectedDeviceIds.length === 0) {
    ElMessage.warning('请至少选择一个设备')
    return
  }
  if (batchModal.form.conditions.length === 0 || !batchModal.form.conditions[0].metric) {
    ElMessage.warning('请配置至少一个监控条件')
    return
  }
  const hasEmptyMetric = batchModal.form.conditions.some(c => !c.metric)
  if (hasEmptyMetric) {
    ElMessage.warning('请为所有条件选择监控指标')
    return
  }
  // 检查是否有重复的物模型属性
  const metrics = batchModal.form.conditions.map(c => c.metric).filter(Boolean)
  const uniqueMetrics = new Set(metrics)
  if (metrics.length !== uniqueMetrics.size) {
    ElMessage.warning('每个物模型属性只能配置一个条件，请检查是否有重复')
    return
  }
  if (!batchModal.form.notifyUser) {
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
        conditions: batchModal.form.conditions.filter(c => c.metric), // 过滤空条件
        notifyUsers: [batchModal.form.notifyUser], // 转换为数组
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
  // 只显示第一个用户（因为现在是单选）
  const userId = userIds[0]
  const user = users.value.find(u => u.id === userId)
  return user ? (user.realName || user.username) : '-'
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

/* 页面标题栏 */
.page-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 16px;
  background: white;
  padding: 18px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  white-space: nowrap;
}

.stats-quick {
  display: flex;
  gap: 20px;
  align-items: center;
}

.stat-item {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
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


/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: nowrap;
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-bar .search-input {
  width: 220px;
  flex: 0 0 auto;
}

.search-input,
.filter-select {
  height: 36px;
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

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  width: 100%;
}

:deep(.el-table) {
  width: 100%;
}

:deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

:deep(.el-table thead) {
  background: #f8f9fa;
}

:deep(.el-table th) {
  padding: 14px 12px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  border-bottom: 2px solid #e4e7ed;
  white-space: nowrap;
}

:deep(.el-table th .cell) {
  white-space: nowrap;
  padding: 0;
}

:deep(.el-table td) {
  padding: 14px 12px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.6;
}

:deep(.el-table td .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0;
}

:deep(.el-table tr:hover) {
  background: #f5f7fa;
}

/* 设备名称单元格 */
.device-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.device-name-text {
  font-weight: 500;
  color: #1d1d1f;
}

.device-code-text {
  color: #909399;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
}

.status-text {
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
  font-weight: 500;
}

.status-online {
  color: #67c23a;
  background: #f0f9ff;
}

.status-offline {
  color: #e6a23c;
  background: #fef0f0;
}

.empty-text {
  color: #999;
  font-size: 14px;
}

.notify-user-text {
  font-size: 13px;
  color: #606266;
}

/* 状态指示器 */
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.online {
  background: #67c23a;
}

.status-dot.offline {
  background: #e6a23c;
}

/* 标签 */
.tag {
  display: inline-block;
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
}

.tag.configured {
  background: #f0f9ff;
  color: #409eff;
}

.tag.unconfigured {
  background: #fef0f0;
  color: #f56c6c;
}

/* 告警条件 */
.condition-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  white-space: nowrap;
}

.condition-separator {
  margin: 0 6px;
  color: #909399;
}

.condition-text .metric {
  font-weight: 500;
  color: #667eea;
}

.condition-text .operator {
  color: #e6a23c;
  font-weight: 600;
  margin: 0 2px;
}

.condition-text .value {
  font-weight: 500;
  color: #f56c6c;
}

.level-badge {
  font-size: 14px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 弹窗样式 */
.config-dialog :deep(.el-dialog),
.batch-dialog :deep(.el-dialog) {
  max-height: 96vh;
  display: flex;
  flex-direction: column;
}

.config-dialog :deep(.el-dialog__body),
.batch-dialog :deep(.el-dialog__body) {
  padding: 16px 20px;
  overflow-y: auto;
  flex: 1;
  max-height: calc(96vh - 80px);
}

.dialog-body {
  padding: 0;
  max-height: calc(96vh - 140px);
  overflow-y: auto;
}

.info-section {
  margin-bottom: 16px;
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
  margin-bottom: 16px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.batch-dialog .form-section {
  margin-bottom: 12px;
}

.batch-dialog .conditions-list {
  max-height: 150px;
  overflow-y: auto;
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
  gap: 12px;
  margin-bottom: 12px;
  max-height: 200px;
  overflow-y: auto;
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
  margin-bottom: 16px;
}

.form-field:last-child {
  margin-bottom: 0;
}

.batch-dialog .form-field {
  margin-bottom: 10px;
}

.batch-dialog .warning-box {
  margin-bottom: 16px;
  padding: 12px;
}

.batch-dialog .preview-area {
  margin-top: 8px;
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
  max-height: 140px;
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
