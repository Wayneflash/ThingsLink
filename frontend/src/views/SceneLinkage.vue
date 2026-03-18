<template>
  <div class="scene-page">
    <!-- 顶部整行栏 (与面包屑标题行对齐) -->
    <div class="top-header-bar">
      <div class="left-group">
        <div class="filter-controls no-border">
          <el-input
            v-model="queryParams.ruleName"
            placeholder="搜索规则"
            clearable
            class="compact-input"
            @keyup.enter="loadRules"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          
          <el-select
            v-model="queryParams.deviceId"
            placeholder="按设备筛选"
            clearable
            filterable
            remote
            :remote-method="searchTriggerDevices"
            :loading="triggerDeviceLoading"
            class="compact-select"
            @change="loadRules"
          >
            <el-option v-for="dev in devices" :key="dev.id" :label="dev.deviceName" :value="dev.id" />
          </el-select>
          
          <el-button type="primary" @click="loadRules" class="compact-btn">查询</el-button>
          <el-button @click="resetQuery" class="compact-btn">重置</el-button>
        </div>
      </div>
      
      <div class="right-group">
        <el-button @click="handleRefreshAll" circle class="action-icon-btn" title="刷新">
          <el-icon><Refresh /></el-icon>
        </el-button>
        <el-button type="primary" @click="openCreateDialog" class="create-btn">
          <el-icon><Plus /></el-icon>
          <span>新建规则</span>
        </el-button>
      </div>
    </div>

    <!-- 规则列表 -->
    <div class="content-section">
      <el-table
        v-memo="[rules, loading, pagination.currentPage, pagination.pageSize, pagination.total]"
        :data="rules"
        style="width: 100%"
        class="custom-table"
        v-loading="loading"
      >
        <el-table-column prop="ruleName" label="规则名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="规则描述" min-width="520">
          <template #default="{ row }">
            <div class="rule-description-text compact-row">
              当 <strong>{{ row.triggerDeviceName }} ({{ row.triggerDeviceCode }})</strong> 的 
              <strong>{{ row.triggerAttrName || row.triggerAttr }}</strong> 
              <span>{{ getOperatorSymbol(row.operator) }}</span> 
              <strong>{{ row.triggerValue }}{{ row.triggerAttrUnit || '' }}</strong>
              <el-icon class="mx-2"><Right /></el-icon>
              控制 <strong>{{ row.targetDeviceName }} ({{ row.targetDeviceCode }})</strong> 
              执行 <strong>{{ row.targetAttrName || row.targetAttr }}</strong>：
              <strong>{{ row.targetAttrName || row.targetAttr }}</strong> 
              <span>=</span> 
              <strong>{{ row.targetValue }}{{ row.targetAttrUnit || '' }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              :model-value="row.enabled" 
              :active-value="1"
              :inactive-value="0"
              @change="(val) => toggleRule(row)"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column label="触发次数" width="90" align="center" prop="triggerCount" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" @click="openLogDialog(row)" class="action-link">日志</el-button>
              <el-button link type="primary" size="small" @click="openEditDialog(row)" class="action-link">编辑</el-button>
              <el-popconfirm title="确认删除？" @confirm="deleteRule(row)">
                <template #reference>
                  <el-button link type="danger" size="small" class="action-link">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-memo="[pagination.currentPage, pagination.pageSize, pagination.total]">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="custom-pagination"
        />
      </div>
    </div>

    <!-- 规则编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEditMode ? '编辑联动规则' : '创建联动规则'" 
      width="860px" 
      class="modern-dialog"
      destroy-on-close
    >
      <el-form
        v-loading="modelLoading"
        element-loading-text="正在加载物模型..."
        element-loading-background="rgba(255, 255, 255, 0.65)"
        :model="ruleForm"
        :rules="formRules"
        ref="formRef"
        class="modern-form"
        label-width="90px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input 
            v-model="ruleForm.ruleName" 
            placeholder="例如：客厅温度过高自动开启空调" 
            maxlength="50" 
            show-word-limit 
            class="form-input"
            :validate-event="false"
          />
        </el-form-item>

        <div class="form-grid">
          <!-- 触发条件配置区 -->
          <div class="form-card trigger-card">
            <div class="card-header">
              <el-icon class="card-icon"><Lightning /></el-icon>
              <span>触发条件 (IF)</span>
            </div>
            <div class="card-body">
              <el-form-item label="触发设备" prop="triggerDeviceId">
                <el-select-v2
                  v-model="ruleForm.triggerDeviceId"
                  filterable
                  remote
                  :remote-method="searchTriggerDevices"
                  :loading="triggerDeviceLoading"
                  placeholder="搜索并选择设备"
                  :options="deviceSelectOptions"
                  class="form-select full-width"
                  @change="handleTriggerDeviceChange"
                  :validate-event="false"
                  :disabled="modelLoading"
                />
              </el-form-item>
              
              <el-form-item label="属性" prop="triggerAttr">
                <el-autocomplete
                  v-model="triggerAttrInput"
                  :fetch-suggestions="querySearchTriggerAttrs"
                  placeholder="搜索并选择属性"
                  class="form-select full-width"
                  :trigger-on-focus="true"
                  :debounce="200"
                  :disabled="modelLoading || !ruleForm.triggerDeviceId"
                  :validate-event="false"
                  @select="handleTriggerAttrSelect"
                >
                  <template #default="{ item }">
                    <span>{{ item.label }}</span>
                    <span v-if="item.unit" style="margin-left: 8px; color: #8492a6; font-size: 12px">{{ item.unit }}</span>
                  </template>
                </el-autocomplete>
              </el-form-item>
              
              <el-form-item label="条件" prop="operator">
                <el-radio-group v-model="ruleForm.operator" :disabled="modelLoading">
                  <el-radio-button label="gt">></el-radio-button>
                  <el-radio-button label="lt"><</el-radio-button>
                  <el-radio-button label="eq">=</el-radio-button>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="阈值" prop="triggerValue">
                <el-input v-model="ruleForm.triggerValue" placeholder="输入数值" class="form-input" :validate-event="false" :disabled="modelLoading">
                  <template #append v-if="triggerAttrUnit">{{ triggerAttrUnit }}</template>
                </el-input>
              </el-form-item>
            </div>
          </div>

          <!-- 执行动作配置区 -->
          <div class="form-card action-card">
            <div class="card-header">
              <el-icon class="card-icon"><Setting /></el-icon>
              <span>执行动作 (THEN)</span>
            </div>
            <div class="card-body">
              <el-form-item label="目标设备" prop="targetDeviceId">
                <el-select-v2
                  v-model="ruleForm.targetDeviceId"
                  filterable
                  remote
                  :remote-method="searchTargetDevices"
                  :loading="targetDeviceLoading"
                  placeholder="搜索并选择设备"
                  :options="deviceSelectOptions"
                  class="form-select full-width"
                  @change="handleTargetDeviceChange"
                  :validate-event="false"
                  :disabled="modelLoading"
                />
              </el-form-item>
              
              <el-form-item label="执行动作" prop="targetAttr">
                <el-select-v2
                  v-model="ruleForm.targetAttr"
                  placeholder="选择指令"
                  :options="targetCommandOptions"
                  :loading="targetCommandsLoading"
                  class="form-select full-width"
                  @change="handleTargetAttrChange"
                  :validate-event="false"
                  :disabled="modelLoading || !ruleForm.targetDeviceId"
                />
              </el-form-item>
              
              <div v-if="selectedTargetCommand" class="command-info-display">
                <div class="info-item">
                  <span class="label">控制属性：</span>
                  <span class="value">{{ getAttrName(selectedTargetCommand.addr) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">指令类型：</span>
                  <el-tag size="small" :type="selectedTargetCommand.commandType === 'dynamic' ? 'warning' : 'success'">
                    {{ selectedTargetCommand.commandType === 'dynamic' ? '动态指令' : '固定指令' }}
                  </el-tag>
                </div>
                <div class="info-item" v-if="selectedTargetCommand.commandType === 'fixed'">
                  <span class="label">固定数值：</span>
                  <span class="value">{{ selectedTargetCommand.commandValue }} {{ targetAttrUnit }}</span>
                </div>
              </div>
              
              <el-form-item label="下发值" prop="targetValue" v-if="isTargetAttrDynamic">
                <el-input v-model="ruleForm.targetValue" placeholder="输入控制值" class="form-input" :validate-event="false" :disabled="modelLoading" />
              </el-form-item>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="submitRule" class="confirm-btn">{{ isEditMode ? '保存修改' : '创建规则' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 规则日志详情弹窗 -->
    <el-dialog v-model="logDialogVisible" :title="`执行历史 - ${currentRule?.ruleName}`" width="900px" class="apple-dialog">
      <el-table :data="ruleLogs" stripe style="width: 100%" class="custom-table small" v-loading="logLoading">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="触发条件" width="200">
          <template #default="{ row }">
            <div class="log-detail-item">
              <span class="label">设备:</span>
              <span class="value">{{ row.triggerDeviceName }}</span>
            </div>
            <div class="log-detail-item">
              <span class="label">属性:</span>
              <span class="value">{{ row.triggerAttr }}</span>
            </div>
            <div class="log-detail-item">
              <span class="label">数值:</span>
              <span class="value highlight">{{ row.triggerValue }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="执行动作" width="200">
          <template #default="{ row }">
            <div class="log-detail-item">
              <span class="label">设备:</span>
              <span class="value">{{ row.targetDeviceName }}</span>
            </div>
            <div class="log-detail-item">
              <span class="label">属性:</span>
              <span class="value">{{ row.targetAttr }}</span>
            </div>
            <div class="log-detail-item">
              <span class="label">下发值:</span>
              <span class="value highlight">{{ row.targetValue }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.executeResult === 'success' ? 'success' : 'danger'" size="small" effect="dark">
              {{ row.executeResult === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 日志分页 -->
      <div class="pagination-wrapper" v-if="logPagination.total > 0">
        <el-pagination
          v-model:current-page="logPagination.currentPage"
          v-model:page-size="logPagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="logPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleLogSizeChange"
          @current-change="handleLogCurrentChange"
          class="custom-pagination"
        />
      </div>
      
      <div v-if="ruleLogs.length === 0" class="empty-logs">
        <el-empty description="暂无执行记录" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Connection, Lightning, Setting, Clock, Refresh, Document, Edit, Delete, ArrowRight, Search, Right } from '@element-plus/icons-vue'
import { ref, reactive, computed, onMounted, shallowRef, markRaw } from 'vue'
import { sceneApi } from '@/api/index'
import { getProductAttributes, getProductCommands } from '@/api/product'

// 查询参数
const queryParams = reactive({
  ruleName: '',
  deviceId: null
})

// 重置查询
const resetQuery = () => {
  queryParams.ruleName = ''
  queryParams.deviceId = null
  loadRules()
}

// 刷新全部
const handleRefreshAll = () => {
  loadRules()
  loadRecentLogs()
}

// 数据
const loading = ref(false)
const currentRule = ref(null)
const logLoading = ref(false)

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 日志分页
const logPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const logDialogVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref(null)

// 物模型加载遮罩（选择设备后加载属性/指令期间显示）
const modelLoading = ref(false)

// 表单
const ruleForm = ref({
  id: null,
  ruleName: '',
  enabled: 1,
  triggerDeviceId: null,
  triggerAttr: '',
  operator: 'gt',
  triggerValue: '',
  targetDeviceId: null,
  targetAttr: '', // 回退为单选字符串
  targetValue: ''
})

const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  triggerDeviceId: [{ required: true, message: '请选择触发设备', trigger: 'change' }],
  triggerAttr: [{ required: true, message: '请选择触发属性', trigger: 'change' }],
  operator: [{ required: true, message: '请选择条件', trigger: 'change' }],
  triggerValue: [{ required: true, message: '请输入阈值', trigger: 'blur' }],
  targetDeviceId: [{ required: true, message: '请选择目标设备', trigger: 'change' }],
  targetAttr: [{ required: true, message: '请选择执行动作', trigger: 'change' }],
  targetValue: [{ required: true, message: '请输入下发值', trigger: 'blur' }]
}

// 触发设备属性（shallowRef 避免深度响应式带来的卡顿）
const triggerAttrs = shallowRef([])
const triggerAttrInput = ref('')
const triggerAttrUnit = ref('')
const triggerAttrsLoading = ref(false)

// 目标设备属性与指令（shallowRef 避免深度响应式带来的卡顿）
const targetAttrs = shallowRef([])
const targetCommands = shallowRef([])
const targetAttrUnit = ref('')
const targetCommandsLoading = ref(false)

// 模拟产品管理页面的简单结构，减少响应式开销
const devices = ref([])
const rules = ref([])
const recentLogs = ref([])
const ruleLogs = ref([])

// 产品元数据缓存 (属性和指令)：使用 Map + markRaw，避免缓存本身被深度代理导致卡顿
const productCache = {
  attrs: new Map(), // productId -> attributes
  cmds: new Map()   // productId -> commands
}

// 加载目标设备属性和指令
const loadTargetDeviceData = async (deviceId) => {
  const device = deviceOptions.value.find(d => d.id === deviceId) || devices.value.find(d => d.id === deviceId)
  if (!device || !device.productId) return
  
  const productId = device.productId
  targetCommandsLoading.value = true

  try {
    // 检查并加载属性缓存
    if (!productCache.attrs.has(productId)) {
      const attrs = await getProductAttributes(productId)
      productCache.attrs.set(productId, markRaw(attrs || []))
    }
    targetAttrs.value = productCache.attrs.get(productId) || []

    // 检查并加载指令缓存
    if (!productCache.cmds.has(productId)) {
      const cmds = await getProductCommands(productId)
      const cmdList = Array.isArray(cmds) ? cmds : (cmds?.list || [])
      productCache.cmds.set(productId, markRaw(cmdList))
    }
    targetCommands.value = productCache.cmds.get(productId) || []
    
    
  } catch (error) {
    console.error('加载目标设备数据失败:', error)
  } finally {
    targetCommandsLoading.value = false
  }
}

// 当前选中的目标指令详情
const selectedTargetCommand = computed(() => {
  const cmdId = ruleForm.value.targetAttr
  if (!cmdId) return null
  return targetCommands.value.find(c => c.id === cmdId)
})

// 目标属性是否需要输入值
const isTargetAttrDynamic = computed(() => {
  const cmd = selectedTargetCommand.value
  if (!cmd) return false
  // 增加更多判定条件的兼容
  return cmd.commandType === 'dynamic' || cmd.isDynamic === 1 || cmd.isDynamic === '1' || cmd.type === 'dynamic'
})

// 加载规则列表
const loadRules = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ruleName: queryParams.ruleName,
      deviceId: queryParams.deviceId
    }
    const res = await sceneApi.getRules(params.pageNum, params.pageSize)
    if (res) {
      let records = res.records || res || []
      let total = res.total || res.length || 0
      
      // 前端过滤（如果后端接口暂不支持这些参数）
      if (queryParams.ruleName) {
        records = records.filter(r => r.ruleName.includes(queryParams.ruleName))
      }
      if (queryParams.deviceId) {
        records = records.filter(r => r.triggerDeviceId === queryParams.deviceId || r.targetDeviceId === queryParams.deviceId)
      }
      
      rules.value = records
      pagination.total = total
    }
  } catch (error) {
    console.error('加载规则失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadRules()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadRules()
}

// 设备搜索
const triggerDeviceLoading = ref(false)
const targetDeviceLoading = ref(false)
const triggerDeviceKeyword = ref('')
const targetDeviceKeyword = ref('')

// 触发设备列表缓存，减少重复搜索带来的响应式开销
const deviceOptions = ref([])

// 防抖函数
const debounce = (fn, delay = 300) => {
  let timer = null
  return (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 搜索触发设备
const searchTriggerDevices = debounce(async (keyword) => {
  if (keyword === triggerDeviceKeyword.value && deviceOptions.value.length > 0) return
  triggerDeviceKeyword.value = keyword
  triggerDeviceLoading.value = true
  try {
    const res = await sceneApi.getAvailableDevices(keyword)
    if (res) {
      deviceOptions.value = res
    }
  } catch (error) {
    console.error('搜索设备失败:', error)
  } finally {
    triggerDeviceLoading.value = false
  }
}, 300)

// 搜索目标设备
const searchTargetDevices = debounce(async (keyword) => {
  if (keyword === targetDeviceKeyword.value && deviceOptions.value.length > 0) return
  targetDeviceKeyword.value = keyword
  targetDeviceLoading.value = true
  try {
    const res = await sceneApi.getAvailableDevices(keyword)
    if (res) {
      deviceOptions.value = res
    }
  } catch (error) {
    console.error('搜索设备失败:', error)
  } finally {
    targetDeviceLoading.value = false
  }
}, 300)

// 设备选项 (computed 避免模板中每次渲染都 map 生成新数组)
const deviceSelectOptions = computed(() => {
  return deviceOptions.value.map(d => ({
    label: `${d.deviceName} (${d.deviceCode})`,
    value: d.id
  }))
})

// 初始加载设备列表
const loadDevices = async () => {
  try {
    const res = await sceneApi.getAvailableDevices('')
    if (res) {
      devices.value = res
      deviceOptions.value = res
    }
  } catch (error) {
    console.error('加载设备失败:', error)
  }
}

// 加载最近日志
const loadRecentLogs = async () => {
  try {
    const res = await sceneApi.getRecentLogs(20)
    if (res) {
      recentLogs.value = res
    }
  } catch (error) {
    console.error('加载日志失败:', error)
  }
}

// 加载设备属性
const loadDeviceAttrs = async (deviceId, isTrigger) => {
  const device = deviceOptions.value.find(d => d.id === deviceId) || devices.value.find(d => d.id === deviceId)
  if (!device || !device.productId) return
  
  const productId = device.productId
  
  // 设置 loading 状态
  if (isTrigger) {
    triggerAttrsLoading.value = true
  } else {
    targetCommandsLoading.value = true
  }
  
  // 优先从缓存读取
  if (productCache.attrs.has(productId)) {
    const cached = productCache.attrs.get(productId) || []
    if (isTrigger) {
      triggerAttrs.value = cached
      triggerAttrsLoading.value = false
    } else {
      targetAttrs.value = cached
      targetCommandsLoading.value = false
    }
    return
  }

  try {
    const res = await getProductAttributes(productId)
    const attrs = res || []
    productCache.attrs.set(productId, markRaw(attrs))
    if (isTrigger) {
      triggerAttrs.value = attrs
    } else {
      targetAttrs.value = attrs
    }
  } catch (error) {
    console.error('加载属性失败:', error)
  } finally {
    if (isTrigger) {
      triggerAttrsLoading.value = false
    } else {
      targetCommandsLoading.value = false
    }
  }
}

// 选择触发设备后，预加载物模型
const handleTriggerDeviceChange = async (deviceId) => {
  ruleForm.value.triggerAttr = ''
  triggerAttrInput.value = ''
  triggerAttrs.value = []
  triggerAttrUnit.value = ''
  if (!deviceId) return
  modelLoading.value = true
  try {
    await loadDeviceAttrs(deviceId, true)
  } finally {
    modelLoading.value = false
  }
}

// 选择目标设备后，预加载物模型与指令
const handleTargetDeviceChange = async (deviceId) => {
  ruleForm.value.targetAttr = ''
  ruleForm.value.targetValue = ''
  targetCommands.value = []
  targetAttrs.value = []
  targetAttrUnit.value = ''
  if (!deviceId) return
  modelLoading.value = true
  try {
    await loadTargetDeviceData(deviceId)
  } finally {
    modelLoading.value = false
  }
}

// 触发属性搜索建议（极简：只做本地过滤，不使用下拉弹层组件）
const querySearchTriggerAttrs = (queryString, cb) => {
  const q = (queryString || '').trim().toLowerCase()
  const source = triggerAttrs.value || []
  const results = source
    .filter(a => {
      const name = (a.attrName || '').toLowerCase()
      const addr = (a.addr || '').toLowerCase()
      return !q || name.includes(q) || addr.includes(q)
    })
    .slice(0, 50)
    .map(a => ({
      value: `${a.attrName} (${a.addr})`,
      label: `${a.attrName} (${a.addr})`,
      addr: a.addr,
      unit: a.unit || ''
    }))

  cb(results)
}

const handleTriggerAttrSelect = (item) => {
  const addr = item?.addr
  ruleForm.value.triggerAttr = addr || ''
  const attr = triggerAttrs.value.find(a => a.addr === addr)
  triggerAttrUnit.value = attr?.unit || ''
}

// 目标设备指令选项 (computed 避免重复创建)
const targetCommandOptions = computed(() => {
  return targetCommands.value.map(c => ({ label: c.commandName, value: c.id }))
})

// 获取操作符描述
const getOperatorText = (op) => {
  const map = { gt: '大于', lt: '小于', eq: '等于', gte: '大于等于', lte: '小于等于', neq: '不等于' }
  return map[op] || op
}

// 获取操作符符号
const getOperatorSymbol = (op) => {
  const map = { gt: '>', lt: '<', gte: '≥', lte: '≤', eq: '=', neq: '≠' }
  return map[op] || op
}

// 获取属性名称
const getAttrName = (addr) => {
  const attr = targetAttrs.value.find(a => a.addr === addr)
  return attr?.attrName || addr
}

// 获取设备名称
const getDeviceName = (deviceId) => {
  const dev = devices.value.find(d => d.id === deviceId)
  return dev?.deviceName || '设备'
}

// 格式化时间（去掉 T）
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  // 如果时间字符串包含 T，替换为空格
  return dateTime.replace('T', ' ')
}

// 切换执行动作处理
const handleTargetAttrChange = (val) => {
  if (!val) {
    targetAttrUnit.value = ''
    return
  }
  const cmd = targetCommands.value.find(c => c.id === val)
  if (cmd) {
    // 自动映射：根据指令关联的属性获取单位
    const attr = targetAttrs.value.find(a => a.addr === cmd.addr)
    targetAttrUnit.value = attr?.unit || ''
    
    // 如果是固定指令，自动填入指令预设的值
    if (cmd.commandType === 'fixed' || cmd.isDynamic === 0) {
      ruleForm.value.targetValue = cmd.commandValue || ''
    } else {
      ruleForm.value.targetValue = ''
    }
  }
}

// 打开创建对话框
const openCreateDialog = () => {
  isEditMode.value = false
  ruleForm.value = {
    id: null,
    ruleName: '',
    enabled: 1,
    triggerDeviceId: null,
    triggerAttr: '',
    operator: 'gt',
    triggerValue: '',
    targetDeviceId: null,
    targetAttr: '', // 回退为单选
    targetValue: ''
  }
  triggerAttrs.value = []
  triggerAttrInput.value = ''
  targetAttrs.value = []
  targetCommands.value = []
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = async (rule) => {
  isEditMode.value = true
  ruleForm.value = { ...rule }
  
  // 加载属性
  if (rule.triggerDeviceId) {
    await loadDeviceAttrs(rule.triggerDeviceId, true)
    const attr = triggerAttrs.value.find(a => a.addr === ruleForm.value.triggerAttr)
    triggerAttrInput.value = attr ? `${attr.attrName} (${attr.addr})` : (ruleForm.value.triggerAttr || '')
  }
  if (rule.targetDeviceId) {
    await loadTargetDeviceData(rule.targetDeviceId)
    // 编辑回显时，需要根据后端返回的 addr 找到对应的指令 ID
    const cmd = targetCommands.value.find(c => c.addr === rule.targetAttr)
    if (cmd) {
      ruleForm.value.targetAttr = cmd.id
    }
  }
  
  dialogVisible.value = true
}

// 提交规则
const submitRule = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const submitData = { ...ruleForm.value }
    // 提交给后端时，需要将 targetAttr 映射回指令对应的 addr
    const cmd = targetCommands.value.find(c => c.id === ruleForm.value.targetAttr)
    if (cmd) {
      submitData.targetAttr = cmd.addr
      // 固定指令使用预设值，动态指令使用用户输入的值
      if (!isTargetAttrDynamic.value && cmd.commandValue) {
        submitData.targetValue = cmd.commandValue
      }
    }
    
    if (isEditMode.value) {
      try {
        const res = await sceneApi.updateRule(submitData.id, submitData)
        // 响应拦截器已处理 code !== 200 的情况，这里直接判断是否有数据
        const success = res && (res.id || res.ruleName)
        if (success) {
          ElMessage.success('规则已更新')
          dialogVisible.value = false
          loadRules()
        } else {
          ElMessage.error('保存失败')
        }
      } catch (err) {
        const errorMsg = err.response?.data?.message || err.message || '更新失败'
        ElMessage.error(errorMsg)
      }
    } else {
      try {
        const res = await sceneApi.createRule(submitData)
        // 响应拦截器已处理 code !== 200 的情况，这里直接判断是否有数据
        const success = res && (res.id || res.ruleName)
        if (success) {
          ElMessage.success('规则已创建')
          dialogVisible.value = false
          loadRules()
        } else {
          ElMessage.error('创建失败')
        }
      } catch (err) {
        const errorMsg = err.response?.data?.message || err.message || '创建失败'
        ElMessage.error(errorMsg)
      }
    }
  } catch (error) {
    if (error !== false) {
      console.error('保存规则失败:', error)
    }
  }
}

// 切换规则状态
const toggleRule = async (rule) => {
  const oldStatus = rule.enabled
  // 乐观更新 UI
  const newStatus = oldStatus === 1 ? 0 : 1
  rule.enabled = newStatus
  
  try {
    const res = await sceneApi.toggleRule(rule.id)
    // 后端返回的是 Result 对象，res 是 data 部分。如果 data 是 true 或 200 说明成功
    if (res === true || res === 1 || res?.code === 200) {
      ElMessage.success(newStatus === 1 ? '规则已启用' : '规则已禁用')
    } else {
      // 如果返回不是成功标识，回滚
      rule.enabled = oldStatus
      ElMessage.error('切换失败')
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    rule.enabled = oldStatus
    const msg = error?.response?.data?.message || error?.message || '切换状态失败'
    ElMessage.error(msg)
  }
}

// 删除规则
const deleteRule = async (rule) => {
  try {
    await ElMessageBox.confirm(`确定要删除规则"${rule.ruleName}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await sceneApi.deleteRule(rule.id)
    ElMessage.success('规则已删除')
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除规则失败:', error)
    }
  }
}

// 打开日志对话框
const openLogDialog = async (rule) => {
  currentRule.value = rule
  logPagination.currentPage = 1
  logPagination.pageSize = 10
  await loadRuleLogs(rule.id)
  logDialogVisible.value = true
}

// 加载规则日志（带分页）
const loadRuleLogs = async (ruleId) => {
  logLoading.value = true
  try {
    // 后端接口暂不支持分页，前端模拟分页
    const res = await sceneApi.getRuleLogs(ruleId)
    const allLogs = res || []
    logPagination.total = allLogs.length
    
    // 前端分页
    const start = (logPagination.currentPage - 1) * logPagination.pageSize
    const end = start + logPagination.pageSize
    ruleLogs.value = allLogs.slice(start, end)
  } catch (error) {
    console.error('加载日志失败:', error)
    ruleLogs.value = []
    logPagination.total = 0
  } finally {
    logLoading.value = false
  }
}

// 日志分页处理
const handleLogSizeChange = (val) => {
  logPagination.pageSize = val
  logPagination.currentPage = 1
  loadRuleLogs(currentRule.value.id)
}

const handleLogCurrentChange = (val) => {
  logPagination.currentPage = val
  loadRuleLogs(currentRule.value.id)
}

// 初始化
onMounted(() => {
  loadDevices()
  loadRules()
  loadRecentLogs()
})
</script>

<style scoped>
.scene-page {
  padding: 20px;
  background: #f5f5f7;
  min-height: calc(100vh - 64px);
}

/* 顶部整行栏 */
.top-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 12px 20px;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.left-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title-text {
  font-size: 18px;
  font-weight: 700;
  color: #1d1d1f;
}

.filter-controls.no-border {
  margin-left: 0;
  padding-left: 0;
  border-left: none;
}

.compact-input {
  width: 160px;
}

.compact-select {
  width: 160px;
}

.compact-btn {
  padding: 0 12px;
  height: 32px;
  border-radius: 6px;
}

.right-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 表格操作链接 */
.table-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-link {
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-link:hover {
  transform: translateX(2px);
}

.action-link:hover {
  opacity: 0.8;
}

/* 按钮悬停效果 */
.action-icon-btn {
  background: #fff;
  border: 1px solid #d1d1d6;
  width: 32px;
  height: 32px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.action-icon-btn:hover {
  background: #f5f5f7;
  transform: rotate(180deg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.create-btn {
  height: 34px;
  padding: 0 16px;
  border-radius: 17px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}

.compact-btn {
  padding: 0 12px;
  height: 32px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.compact-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 内容区域 */
.content-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid #f2f2f7;
}

.custom-pagination {
  display: flex;
  gap: 8px;
}

.custom-pagination :deep(.el-pagination__total) {
  color: #1d1d1f;
  font-weight: 500;
}

.custom-pagination :deep(.el-pager li) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.custom-pagination :deep(.el-pager li:hover) {
  background: #f5f5f7;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background: #007aff;
  color: #fff;
}

.count-tag {
  background: #f2f2f7;
  border: none;
  color: #86868b;
  font-weight: 500;
}

/* 规则卡片网格 */
.rule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.rule-card {
  background: #fff;
  border: 1px solid #f2f2f7;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  border-color: #007aff30;
}

.rule-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rule-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.rule-id {
  font-family: monospace;
  font-size: 11px;
  color: #c7c7cc;
  background: #f2f2f7;
  padding: 2px 6px;
  border-radius: 4px;
}

.rule-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 逻辑展示流 (紧凑版) */
.logic-flow.compact {
  background: #f9f9fb;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.logic-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  min-width: 0;
}

.logic-tag {
  font-size: 10px;
  font-weight: 800;
  padding: 1px 4px;
  border-radius: 4px;
  min-width: 32px;
  text-align: center;
}

.trigger-tag { background: #eef2ff; color: #007aff; }
.action-tag { background: #fef2f2; color: #ff3b30; }

.device-name {
  font-weight: 600;
  color: #3a3a3c;
  max-width: 100px;
}

.logic-detail {
  color: #86868b;
  flex: 1;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 卡片底部 */
.rule-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid #f2f2f7;
}

.stats {
  font-size: 12px;
  color: #c7c7cc;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.actions {
  display: flex;
  gap: 4px;
}

/* 表格定制 */
.custom-table {
  --el-table-border-color: #f2f2f7;
  --el-table-header-bg-color: #f5f5f7;
}

.custom-table :deep(.el-table__body tr:hover > td) {
  background: #f8fafc !important;
}

.custom-table :deep(.el-table__body tr) {
  transition: all 0.3s ease;
}

.custom-table :deep(.el-table__body tr:hover) {
  transform: scale(1.005);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin: 0 auto;
}

.status-dot.success { background: #34c759; box-shadow: 0 0 8px rgba(52, 199, 89, 0.4); }
.status-dot.error { background: #ff3b30; box-shadow: 0 0 8px rgba(255, 59, 48, 0.4); }

.log-detail {
  font-size: 13px;
  color: #48484a;
}

.mx-1 { margin-left: 4px; margin-right: 4px; }

/* 现代表单与弹窗 */
.modern-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #1d1d1f;
  font-size: 14px;
}

.modern-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-input,
.form-select {
  transition: all 0.3s ease;
}

.form-input:hover,
.form-select:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.form-input:focus,
.form-select:focus {
  box-shadow: 0 2px 12px rgba(0, 122, 255, 0.15);
}

/* 属性选择框增大宽度 */
.attr-select {
  min-width: 280px !important;
}

.attr-select :deep(.el-select__wrapper) {
  min-height: 36px;
}

.attr-option-label {
  float: left;
  font-weight: 500;
  color: #1d1d1f;
}

.attr-option-addr,
.cmd-option-addr {
  float: right;
  color: #86868b;
  font-size: 12px;
  font-family: monospace;
}

.cmd-option-label {
  float: left;
  font-weight: 500;
  color: #1d1d1f;
}

.modern-dialog {
  border-radius: 16px;
}

.modern-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f2f2f7;
}

.modern-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #1d1d1f;
}

.modern-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.modern-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f2f2f7;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #f5f5f7;
}

.confirm-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 12px;
}

.form-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #d1d5db;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
}

.card-icon {
  font-size: 20px;
}

.trigger-card .card-header { 
  color: #007aff;
  border-bottom-color: #007aff20;
}

.action-card .card-header { 
  color: #ff3b30;
  border-bottom-color: #ff3b3020;
}

.inline-inputs {
  display: flex;
  gap: 12px;
}

.flex-1 { flex: 1; }
.flex-2 { flex: 2; }
.full-width { width: 100%; }

.apple-dialog {
  border-radius: 20px;
}

.command-info-display {
  margin: -8px 0 16px 0;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateX(4px);
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #64748b;
  width: 70px;
  font-weight: 500;
}

.info-item .value {
  color: #1e293b;
  font-weight: 600;
}

.empty-logs {
  padding: 40px 0;
}

.log-detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
}

.log-detail-item:last-child {
  margin-bottom: 0;
}

.log-detail-item .label {
  color: #6b7280;
  font-weight: 500;
  min-width: 40px;
}

.log-detail-item .value {
  color: #1f2937;
  font-weight: 400;
}

.log-detail-item .value.highlight {
  color: #007aff;
  font-weight: 600;
  font-family: monospace;
}
</style>
