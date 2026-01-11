<template>
  <div class="alarm-config-page">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <h2 class="page-title">⚙️ 设备报警阈值配置</h2>
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
        style="width: 180px;"
      >
        <el-option v-for="product in products" :key="product.id" :label="product.productName" :value="product.id" />
      </el-select>

      <GroupSelector 
        v-model="filters.groupId" 
        placeholder="设备分组" 
        clearable 
        class="filter-select"
        style="width: 180px;"
      />

      <el-select 
        v-model="filters.configStatus" 
        placeholder="配置状态" 
        clearable 
        class="filter-select"
        style="width: 140px;"
      >
        <el-option label="已配置" value="configured" />
        <el-option label="未配置" value="unconfigured" />
      </el-select>

      <el-select 
        v-model="filters.onlineStatus" 
        placeholder="在线状态" 
        clearable 
        class="filter-select"
        style="width: 140px;"
      >
        <el-option label="在线" :value="1" />
        <el-option label="离线" :value="0" />
      </el-select>

      <el-button type="primary" @click="loadDevices" :icon="Search" style="margin-left: 8px;">查询</el-button>
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
        
        <el-table-column prop="productName" label="产品名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="groupName" label="所属分组" min-width="110" show-overflow-tooltip />
        
        <el-table-column label="配置状态" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.alarmConfig" class="tag configured">✓ 已配置</span>
            <span v-else class="tag unconfigured">✗ 未配置</span>
          </template>
        </el-table-column>
        
        <el-table-column label="处理人" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.alarmConfigObj && row.alarmConfigObj.notifyUser" class="handler-user-text">
              {{ getUserName(row.alarmConfigObj.notifyUser) }}
            </span>
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
      title="阈值配置"
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

        <!-- 全局配置 -->
        <div class="form-section">
          <h3 class="section-title">全局配置</h3>
          <div class="form-field">
            <label class="field-label">
              处理人
              <span class="required">*</span>
            </label>
            <el-select
              v-model="configModal.form.notifyUser"
              placeholder="请选择处理人"
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
            <label class="field-label">报警堆叠</label>
            <div class="switch-field">
              <el-switch v-model="configModal.form.stackMode" />
              <span class="switch-label">开启后，恢复前不会重复报警</span>
            </div>
          </div>
          <div class="form-field">
            <label class="field-label">邮件通知</label>
            <div class="switch-field">
              <el-switch v-model="configModal.form.mailEnabled" />
              <span class="switch-label">开启后，报警时将发送邮件通知处理人</span>
            </div>
          </div>
          <div class="form-field">
            <label class="field-label">短信通知</label>
            <div class="switch-field">
              <el-switch v-model="configModal.form.smsEnabled" />
              <span class="switch-label">开启后，报警时将发送短信通知处理人（暂未实现）</span>
            </div>
          </div>
        </div>

        <!-- 物模型监控配置 -->
        <div class="form-section">
          <h3 class="section-title">物模型监控配置</h3>
          <el-table
            :data="deviceAttributes"
            border
            style="width: 100%"
            max-height="300px"
          >
            <el-table-column label="属性名称" width="120">
              <template #default="{ row, $index }">
                <span v-if="$index === 0" style="font-weight: 600; color: #1d1d1f;">离线报警</span>
                <span v-else>{{ row.attrName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="70" align="center">
              <template #default="{ row, $index }">
                <el-switch
                  v-if="$index === 0"
                  v-model="configModal.form.offlineAlarm.enabled"
                  size="small"
                />
                <el-switch
                  v-else
                  v-model="configModal.form.metrics[row.addr].enabled"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column label="运算符" width="100">
              <template #default="{ row, $index }">
                <el-select
                  v-if="$index === 0"
                  v-model="configModal.form.offlineAlarm.operator"
                  :disabled="!configModal.form.offlineAlarm.enabled"
                  size="small"
                  style="width: 100%"
                >
                  <el-option label=">" value=">" />
                </el-select>
                <el-select
                  v-else
                  v-model="configModal.form.metrics[row.addr].operator"
                  :disabled="!configModal.form.metrics[row.addr].enabled"
                  size="small"
                  style="width: 100%"
                >
                  <el-option label=">" value=">" />
                  <el-option label="<" value="<" />
                  <el-option label="=" value="=" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="阈值" width="140">
              <template #default="{ row, $index }">
                <el-input-number
                  v-if="$index === 0"
                  v-model="configModal.form.offlineAlarm.threshold"
                  :disabled="!configModal.form.offlineAlarm.enabled"
                  :min="1"
                  :precision="0"
                  :step="1"
                  size="small"
                  style="width: 100%"
                />
                <el-input-number
                  v-else
                  v-model="configModal.form.metrics[row.addr].threshold"
                  :disabled="!configModal.form.metrics[row.addr].enabled"
                  :precision="2"
                  :step="0.1"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80" align="center">
              <template #default="{ row, $index }">
                <span v-if="$index === 0" class="field-unit">min</span>
                <span v-else class="field-unit">{{ row.unit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="报警级别" width="140">
              <template #default="{ row, $index }">
                <el-select
                  v-if="$index === 0"
                  v-model="configModal.form.offlineAlarm.level"
                  :disabled="!configModal.form.offlineAlarm.enabled"
                  size="small"
                  style="width: 100%"
                >
                  <el-option label="🔴 严重" value="critical" />
                  <el-option label="🟡 警告" value="warning" />
                  <el-option label="🔵 提示" value="info" />
                </el-select>
                <el-select
                  v-else
                  v-model="configModal.form.metrics[row.addr].level"
                  :disabled="!configModal.form.metrics[row.addr].enabled"
                  size="small"
                  style="width: 100%"
                >
                  <el-option label="🔴 严重" value="critical" />
                  <el-option label="🟡 警告" value="warning" />
                  <el-option label="🔵 提示" value="info" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
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
            <div class="field-hint">选择产品后，监控配置将根据该产品物模型动态加载</div>
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
          
          <!-- 全局配置 -->
          <div class="form-field">
            <label class="field-label">
              处理人
              <span class="required">*</span>
            </label>
            <el-select
              v-model="batchModal.form.notifyUser"
              placeholder="请选择处理人"
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
            <label class="field-label">报警堆叠</label>
            <div class="switch-field">
              <el-switch v-model="batchModal.form.stackMode" />
              <span class="switch-label">开启后，恢复前不会重复报警</span>
            </div>
          </div>
          <div class="form-field">
            <label class="field-label">邮件通知</label>
            <div class="switch-field">
              <el-switch v-model="batchModal.form.mailEnabled" />
              <span class="switch-label">开启后，报警时将发送邮件通知处理人</span>
            </div>
          </div>
          <div class="form-field">
            <label class="field-label">短信通知</label>
            <div class="switch-field">
              <el-switch v-model="batchModal.form.smsEnabled" />
              <span class="switch-label">开启后，报警时将发送短信通知处理人（暂未实现）</span>
            </div>
          </div>

          <!-- 物模型监控配置 -->
          <div class="form-field">
            <label class="field-label">
              物模型监控配置
              <span class="required">*</span>
            </label>
            <el-table
              :data="batchDeviceAttributes"
              border
              style="width: 100%"
              max-height="300px"
            >
              <el-table-column label="属性名称" width="120">
                <template #default="{ row, $index }">
                  <span v-if="$index === 0" style="font-weight: 600; color: #1d1d1f;">离线报警</span>
                  <span v-else>{{ row.attrName }}</span>
                </template>
              </el-table-column>
              <el-table-column label="启用" width="70" align="center">
                <template #default="{ row, $index }">
                  <el-switch
                    v-if="$index === 0"
                    v-model="batchModal.form.offlineAlarm.enabled"
                    size="small"
                  />
                  <el-switch
                    v-else
                    v-model="batchModal.form.metrics[row.addr].enabled"
                    size="small"
                  />
                </template>
              </el-table-column>
              <el-table-column label="运算符" width="100">
                <template #default="{ row, $index }">
                  <el-select
                    v-if="$index === 0"
                    v-model="batchModal.form.offlineAlarm.operator"
                    :disabled="!batchModal.form.offlineAlarm.enabled"
                    size="small"
                    style="width: 100%"
                  >
                    <el-option label=">" value=">" />
                  </el-select>
                  <el-select
                    v-else
                    v-model="batchModal.form.metrics[row.addr].operator"
                    :disabled="!batchModal.form.metrics[row.addr].enabled"
                    size="small"
                    style="width: 100%"
                  >
                    <el-option label=">" value=">" />
                    <el-option label="<" value="<" />
                    <el-option label="=" value="=" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="阈值" width="140">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-if="$index === 0"
                    v-model="batchModal.form.offlineAlarm.threshold"
                    :disabled="!batchModal.form.offlineAlarm.enabled"
                    :min="1"
                    :precision="0"
                    :step="1"
                    size="small"
                    style="width: 100%"
                  />
                  <el-input-number
                    v-else
                    v-model="batchModal.form.metrics[row.addr].threshold"
                    :disabled="!batchModal.form.metrics[row.addr].enabled"
                    :precision="2"
                    :step="0.1"
                    size="small"
                    style="width: 100%"
                  />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="80" align="center">
                <template #default="{ row, $index }">
                  <span v-if="$index === 0" class="field-unit">min</span>
                  <span v-else class="field-unit">{{ row.unit || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="报警级别" width="140">
                <template #default="{ row, $index }">
                  <el-select
                    v-if="$index === 0"
                    v-model="batchModal.form.offlineAlarm.level"
                    :disabled="!batchModal.form.offlineAlarm.enabled"
                    size="small"
                    style="width: 100%"
                  >
                    <el-option label="🔴 严重" value="critical" />
                    <el-option label="🟡 警告" value="warning" />
                    <el-option label="🔵 提示" value="info" />
                  </el-select>
                  <el-select
                    v-else
                    v-model="batchModal.form.metrics[row.addr].level"
                    :disabled="!batchModal.form.metrics[row.addr].enabled"
                    size="small"
                    style="width: 100%"
                  >
                    <el-option label="🔴 严重" value="critical" />
                    <el-option label="🟡 警告" value="warning" />
                    <el-option label="🔵 提示" value="info" />
                  </el-select>
                </template>
              </el-table-column>
            </el-table>
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
    stackMode: true,
    mailEnabled: false,
    smsEnabled: false,
    offlineAlarm: {
      enabled: false,
      operator: '>',
      threshold: 5,
      unit: 'min',
      level: 'warning'
    }
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
    stackMode: true,
    mailEnabled: false,
    smsEnabled: false,
    offlineAlarm: {
      enabled: false,
      operator: '>',
      threshold: 5,
      unit: 'min',
      level: 'warning'
    }
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
      
      // 预加载所有产品的物模型属性，确保报警条件能正确显示名称
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
  
  // 加载产品的物模型属性
  const attrs = await loadProductAttributes(device.productId)
  deviceAttributes.value = attrs
  
  // 初始化 metrics Map，为每个属性创建配置对象
  const metricsMap = {}
  attrs.forEach(attr => {
    metricsMap[attr.addr] = {
      enabled: false,
      operator: '>',
      threshold: 0,
      level: 'warning'
    }
  })
  
  // 如果设备已有配置，加载现有配置
  if (device.alarmConfigObj && device.alarmConfigObj.metrics) {
    // 新版Map结构
    Object.keys(device.alarmConfigObj.metrics).forEach(metric => {
      if (metricsMap[metric]) {
        metricsMap[metric] = { ...device.alarmConfigObj.metrics[metric] }
      }
    })
    
    configModal.form = {
      notifyUser: device.alarmConfigObj.notifyUser || null,
      stackMode: device.alarmConfigObj.stackMode !== false,
      mailEnabled: device.alarmConfigObj.mailEnabled || false,
      smsEnabled: device.alarmConfigObj.smsEnabled || false,
      metrics: metricsMap,
      offlineAlarm: device.alarmConfigObj.offlineAlarm || {
        enabled: false,
        operator: '>',
        threshold: 5,
        unit: 'min',
        level: 'warning'
      }
    }
  } else if (device.alarmConfigObj && device.alarmConfigObj.conditions) {
    // 兼容旧版条件数组结构
    device.alarmConfigObj.conditions.forEach(condition => {
      if (metricsMap[condition.metric]) {
        metricsMap[condition.metric] = {
          enabled: true,
          operator: condition.operator,
          threshold: condition.threshold,
          level: device.alarmConfigObj.level || 'warning'
        }
      }
    })
    
    configModal.form = {
      notifyUser: device.alarmConfigObj.notifyUsers && device.alarmConfigObj.notifyUsers.length > 0
        ? device.alarmConfigObj.notifyUsers[0]
        : null,
      stackMode: device.alarmConfigObj.stackMode !== false,
      mailEnabled: device.alarmConfigObj.mailEnabled || false,
      smsEnabled: device.alarmConfigObj.smsEnabled || false,
      metrics: metricsMap
    }
  } else {
    // 未配置
    configModal.form = {
      notifyUser: null,
      stackMode: true,
      mailEnabled: false,
      smsEnabled: false,
      metrics: metricsMap,
      offlineAlarm: {
        enabled: false,
        operator: '>',
        threshold: 5,
        unit: 'min',
        level: 'warning'
      }
    }
  }
}



// 保存单设备配置
const saveConfig = async () => {
  // 校验处理人（只有启用监控时才需要）
  const enabledMetrics = Object.entries(configModal.form.metrics).filter(
    ([_, config]) => config.enabled
  )
  
  // 如果没有启用任何监控属性，表示要撤销该设备的所有报警配置
  if (enabledMetrics.length === 0) {
    try {
      await configureAlarm({
        deviceId: configModal.device.id,
        alarmConfig: {
          notifyUser: configModal.form.notifyUser || null,
          stackMode: configModal.form.stackMode,
          metrics: configModal.form.metrics
        },
        enabled: false  // 全部关闭时设置为false
      })
      
      ElMessage.success('已撤销该设备的所有报警配置')
      configModal.visible = false
      loadDevices()
      return
    } catch (error) {
      console.error('配置失败:', error)
      ElMessage.error('配置失败：' + (error.response?.data?.message || error.message))
      return
    }
  }
  
  // 有启用的监控属性时，需要选择处理人
  if (!configModal.form.notifyUser) {
    ElMessage.warning('启用监控时必须选择处理人')
    return
  }
  
  // 校验启用的属性配置是否完整
  for (const [metric, config] of enabledMetrics) {
    if (!config.operator || config.threshold === null || config.threshold === undefined) {
      ElMessage.warning(`监控属性 "${getMetricName(metric)}" 的配置不完整，请检查运算符和阈值`)
      return
    }
    if (!config.level) {
      ElMessage.warning(`监控属性 "${getMetricName(metric)}" 未设置报警级别`)
      return
    }
  }
  
  try {
    await configureAlarm({
      deviceId: configModal.device.id,
      alarmConfig: {
        notifyUser: configModal.form.notifyUser,
        stackMode: configModal.form.stackMode,
        mailEnabled: configModal.form.mailEnabled,
        smsEnabled: configModal.form.smsEnabled,
        metrics: configModal.form.metrics,
        offlineAlarm: configModal.form.offlineAlarm
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

// 获取属性名称
const getMetricName = (addr) => {
  const attr = deviceAttributes.value.find(a => a.addr === addr)
  return attr ? attr.attrName : addr
}

// 切换报警启用状态（已移除此功能）

// 打开批量配置弹窗
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
    notifyUser: null,
    stackMode: true,
    mailEnabled: false,
    smsEnabled: false,
    metrics: {},
    offlineAlarm: {
      enabled: false,
      operator: '>',
      threshold: 5,
      unit: 'min',
      level: 'warning'
    }
  }
}



// 产品切换时加载设备
const onProductChange = async () => {
  if (batchModal.productId) {
    const attrs = await loadProductAttributes(batchModal.productId)
    batchDeviceAttributes.value = attrs
    
    // 初始化 metrics Map
    const metricsMap = {}
    attrs.forEach(attr => {
      metricsMap[attr.addr] = {
        enabled: false,
        operator: '>',
        threshold: 0,
        level: 'warning'
      }
    })
    batchModal.form.metrics = metricsMap
    
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



// 保存批量配置
const saveBatchConfig = async () => {
  // 校验设备选择
  if (batchModal.selectedDeviceIds.length === 0) {
    ElMessage.warning('请至少选择一个设备')
    return
  }
  
  // 校验是否有启用的监控属性
  const enabledMetrics = Object.entries(batchModal.form.metrics).filter(
    ([_, config]) => config.enabled
  )
  
  // 如果没有启用任何监控属性，表示要撤销这些设备的所有报警配置
  if (enabledMetrics.length === 0) {
    try {
      await ElMessageBox.confirm(
        `确认撤销 ${batchModal.selectedDeviceIds.length} 台设备的所有报警配置？`,
        '确认操作',
        { type: 'warning' }
      )
      
      await configureAlarm({
        deviceIds: batchModal.selectedDeviceIds,
        alarmConfig: {
          notifyUser: batchModal.form.notifyUser || null,
          stackMode: batchModal.form.stackMode,
          metrics: batchModal.form.metrics
        },
        enabled: false  // 全部关闭时设置为false
      })
      
      ElMessage.success(`已撤销 ${batchModal.selectedDeviceIds.length} 台设备的报警配置`)
      batchModal.visible = false
      loadDevices()
      return
    } catch (error) {
      if (error !== 'cancel') {
        console.error('配置失败:', error)
        ElMessage.error('配置失败：' + (error.response?.data?.message || error.message))
      }
      return
    }
  }
  
  // 有启用的监控属性时，需要选择处理人
  if (!batchModal.form.notifyUser) {
    ElMessage.warning('启用监控时必须选择处理人')
    return
  }
  
  // 校验启用的属性配置是否完整
  for (const [metric, config] of enabledMetrics) {
    if (!config.operator || config.threshold === null || config.threshold === undefined) {
      const attrName = batchDeviceAttributes.value.find(a => a.addr === metric)?.attrName || metric
      ElMessage.warning(`监控属性 "${attrName}" 的配置不完整，请检查运算符和阈值`)
      return
    }
    if (!config.level) {
      const attrName = batchDeviceAttributes.value.find(a => a.addr === metric)?.attrName || metric
      ElMessage.warning(`监控属性 "${attrName}" 未设置报警级别`)
      return
    }
  }
  
  try {
    await ElMessageBox.confirm(
      `确认为 ${batchModal.selectedDeviceIds.length} 台设备批量配置报警阈值？\n\n注意：已配置设备的原有阈值将被覆盖！`,
      '确认操作',
      { type: 'warning' }
    )
    
    await configureAlarm({
      deviceIds: batchModal.selectedDeviceIds,
      alarmConfig: {
        notifyUser: batchModal.form.notifyUser,
        stackMode: batchModal.form.stackMode,
        mailEnabled: batchModal.form.mailEnabled,
        smsEnabled: batchModal.form.smsEnabled,
        metrics: batchModal.form.metrics,
        offlineAlarm: batchModal.form.offlineAlarm
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

// 获取用户名称（单个用户ID）
const getUserName = (userId) => {
  const user = users.value.find(u => u.id === userId)
  return user ? (user.realName || user.username) : '-'
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
  gap: 16px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: nowrap;
  background: white;
  padding: 18px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-bar .search-input {
  width: 240px;
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

.handler-user-text {
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

/* 表单项内联布局 */
.form-field-inline {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 16px;
}

.form-field-inline-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-field-inline-item .field-label {
  font-size: 15px;
  color: #1d1d1f;
  font-weight: 500;
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
