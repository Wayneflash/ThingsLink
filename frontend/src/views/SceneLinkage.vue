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
      <el-table :data="rules" style="width: 100%" class="custom-table" v-loading="loading">
        <el-table-column prop="ruleName" label="规则名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="规则描述" min-width="520">
          <template #default="{ row }">
            <div class="rule-description-text compact-row">
              当 <strong>{{ row.triggerDeviceName }}</strong> 的 
              <strong>{{ row.triggerAttrName || row.triggerAttr }}</strong> 
              <span>{{ getOperatorSymbol(row.operator) }}</span> 
              <strong>{{ row.triggerValue }}{{ row.triggerAttrUnit || '' }}</strong>
              <el-icon class="mx-2"><Right /></el-icon>
              控制 <strong>{{ row.targetDeviceName }}</strong> 
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
              <el-button link type="primary" size="small" @click="openLogDialog(row)">日志</el-button>
              <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除？" @confirm="deleteRule(row)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 规则编辑对话框 (Apple Style) -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEditMode ? '编辑联动规则' : '创建联动规则'" 
      width="640px"
      class="apple-dialog"
      destroy-on-close
    >
      <el-form :model="ruleForm" label-position="top" :rules="formRules" ref="formRef" class="modern-form">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="例如：客厅温度过高自动开启空调" maxlength="50" show-word-limit />
        </el-form-item>

        <div class="form-grid">
          <!-- 触发条件配置区 -->
          <div class="form-card trigger-card">
            <div class="card-header">
              <el-icon><Lightning /></el-icon>
              <span>触发条件 (IF)</span>
            </div>
            <div class="card-body">
              <el-form-item label="触发设备" prop="triggerDeviceId">
                <el-select 
                  v-model="ruleForm.triggerDeviceId" 
                  filterable remote 
                  :remote-method="searchTriggerDevices"
                  :loading="triggerDeviceLoading"
                  placeholder="搜索并选择设备"
                  class="full-width">
                  <el-option v-for="dev in devices" :key="dev.id" :label="dev.deviceName" :value="dev.id">
                    <span style="float: left">{{ dev.deviceName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 12px">{{ dev.deviceCode }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              
              <div class="inline-inputs">
                <el-form-item label="属性" prop="triggerAttr" class="flex-2">
                  <el-select v-model="ruleForm.triggerAttr" placeholder="选择属性" class="full-width">
                    <el-option v-for="attr in triggerAttrs" :key="attr.addr" :label="attr.attrName" :value="attr.addr" />
                  </el-select>
                </el-form-item>
                <el-form-item label="条件" prop="operator" class="flex-1">
                  <el-select v-model="ruleForm.operator" class="full-width">
                    <el-option label=">" value="gt" />
                    <el-option label="<" value="lt" />
                    <el-option label="=" value="eq" />
                  </el-select>
                </el-form-item>
              </div>
              
              <el-form-item label="阈值" prop="triggerValue">
                <el-input v-model="ruleForm.triggerValue" placeholder="输入数值">
                  <template #append v-if="triggerAttrUnit">{{ triggerAttrUnit }}</template>
                </el-input>
              </el-form-item>
            </div>
          </div>

          <!-- 执行动作配置区 -->
          <div class="form-card action-card">
            <div class="card-header">
              <el-icon><Setting /></el-icon>
              <span>执行动作 (THEN)</span>
            </div>
            <div class="card-body">
              <el-form-item label="目标设备" prop="targetDeviceId">
                <el-select 
                  v-model="ruleForm.targetDeviceId" 
                  filterable remote 
                  :remote-method="searchTargetDevices"
                  :loading="targetDeviceLoading"
                  placeholder="搜索并选择设备"
                  class="full-width">
                  <el-option v-for="dev in devices" :key="dev.id" :label="dev.deviceName" :value="dev.id">
                    <span style="float: left">{{ dev.deviceName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 12px">{{ dev.deviceCode }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="执行动作" prop="targetAttr">
                <el-select 
                  v-model="ruleForm.targetAttr" 
                  placeholder="选择指令" 
                  class="full-width"
                  @change="handleTargetAttrChange"
                >
                  <el-option 
                    v-for="cmd in targetCommands" 
                    :key="cmd.id" 
                    :label="cmd.commandName" 
                    :value="cmd.id" 
                  />
                </el-select>
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
                <el-input v-model="ruleForm.targetValue" placeholder="输入控制值">
                  <template #append v-if="targetAttrUnit">{{ targetAttrUnit }}</template>
                </el-input>
              </el-form-item>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" round>取消</el-button>
          <el-button type="primary" @click="submitRule" round class="confirm-btn">{{ isEditMode ? '保存修改' : '创建规则' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 规则日志详情弹窗 -->
    <el-dialog v-model="logDialogVisible" :title="`执行历史 - ${currentRule?.ruleName}`" width="720px" class="apple-dialog">
      <el-table :data="ruleLogs" stripe style="width: 100%" class="custom-table small">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column prop="triggerValue" label="触发时数值" width="120" />
        <el-table-column label="动作详情">
          <template #default="{ row }">
            {{ row.targetAttr }} = {{ row.targetValue }}
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
      <div v-if="ruleLogs.length === 0" class="empty-logs">
        <el-empty description="暂无执行记录" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Connection, Lightning, Setting, Clock, Refresh, Document, Edit, Delete, ArrowRight, Search, Right } from '@element-plus/icons-vue'
import { ref, reactive, computed, onMounted, watch } from 'vue'
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
const rules = ref([])
const devices = ref([])
const recentLogs = ref([])
const ruleLogs = ref([])
const currentRule = ref(null)

// 对话框
const dialogVisible = ref(false)
const logDialogVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref(null)

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

// 触发设备属性
const triggerAttrs = ref([])
const triggerAttrUnit = ref('')

// 目标设备属性与指令
const targetAttrs = ref([])
const targetCommands = ref([])
const targetAttrUnit = ref('')

// 加载目标设备属性和指令
const loadTargetDeviceData = async (deviceId) => {
  const device = devices.value.find(d => d.id === deviceId)
  if (!device || !device.productId) return
  
  try {
    const [attrs, cmds] = await Promise.all([
      getProductAttributes(device.productId),
      getProductCommands(device.productId)
    ])
    targetAttrs.value = attrs || []
    const cmdList = Array.isArray(cmds) ? cmds : (cmds?.list || [])
    // 确保指令列表中 commandValue 和 addr 是正确的
    targetCommands.value = cmdList
    console.log('Loaded target commands:', targetCommands.value)
  } catch (error) {
    console.error('加载目标设备数据失败:', error)
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
      pageNum: 1,
      pageSize: 100,
      ruleName: queryParams.ruleName,
      deviceId: queryParams.deviceId
    }
    const res = await sceneApi.getRules(params.pageNum, params.pageSize)
    if (res) {
      let records = res.records || res || []
      
      // 前端过滤（如果后端接口暂不支持这些参数）
      if (queryParams.ruleName) {
        records = records.filter(r => r.ruleName.includes(queryParams.ruleName))
      }
      if (queryParams.deviceId) {
        records = records.filter(r => r.triggerDeviceId === queryParams.deviceId || r.targetDeviceId === queryParams.deviceId)
      }
      
      rules.value = records
    }
  } catch (error) {
    console.error('加载规则失败:', error)
  } finally {
    loading.value = false
  }
}

// 设备搜索
const triggerDeviceLoading = ref(false)
const targetDeviceLoading = ref(false)
const triggerDeviceKeyword = ref('')
const targetDeviceKeyword = ref('')

// 远程搜索设备
const searchTriggerDevices = async (keyword) => {
  triggerDeviceKeyword.value = keyword
  triggerDeviceLoading.value = true
  try {
    const res = await sceneApi.getAvailableDevices(keyword)
    if (res) {
      devices.value = res
    }
  } catch (error) {
    console.error('搜索设备失败:', error)
  } finally {
    triggerDeviceLoading.value = false
  }
}

const searchTargetDevices = async (keyword) => {
  targetDeviceKeyword.value = keyword
  targetDeviceLoading.value = true
  try {
    const res = await sceneApi.getAvailableDevices(keyword)
    if (res) {
      devices.value = res
    }
  } catch (error) {
    console.error('搜索设备失败:', error)
  } finally {
    targetDeviceLoading.value = false
  }
}

// 初始加载设备列表
const loadDevices = async () => {
  try {
    const res = await sceneApi.getAvailableDevices('')
    if (res) {
      devices.value = res
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
  const device = devices.value.find(d => d.id === deviceId)
  if (!device || !device.productId) return
  
  try {
    const res = await getProductAttributes(device.productId)
    if (res) {
      if (isTrigger) {
        triggerAttrs.value = res
      } else {
        targetAttrs.value = res
      }
    }
  } catch (error) {
    console.error('加载属性失败:', error)
  }
}

// 监听触发设备变化
watch(() => ruleForm.value.triggerDeviceId, (newVal) => {
  if (newVal) {
    loadDeviceAttrs(newVal, true)
    ruleForm.value.triggerAttr = ''
  }
})

// 监听目标设备变化
watch(() => ruleForm.value.targetDeviceId, (newVal) => {
  if (newVal) {
    loadTargetDeviceData(newVal)
    ruleForm.value.targetAttr = ''
  }
})

// 监听触发属性变化，获取单位
watch(() => ruleForm.value.triggerAttr, (newVal) => {
  const attr = triggerAttrs.value.find(a => a.addr === newVal)
  triggerAttrUnit.value = attr?.unit || ''
})

// 监听目标指令变化，处理单位、默认值及属性映射
watch(() => ruleForm.value.targetAttr, (newVal) => {
  console.log('Selected Target Action changed:', newVal)
  if (!newVal) {
    targetAttrUnit.value = ''
    return
  }
  const cmd = targetCommands.value.find(c => c.addr === newVal)
  if (cmd) {
    // 自动映射：根据指令关联的属性获取单位
    const attr = targetAttrs.value.find(a => a.addr === cmd.addr)
    targetAttrUnit.value = attr?.unit || ''
    
    // 如果是固定指令，自动填入指令预设的值
    if (cmd.commandType === 'fixed' || cmd.isDynamic === 0) {
      ruleForm.value.targetValue = cmd.commandValue || ''
    }
  }
}, { deep: true })

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

// 切换执行动作处理
const handleTargetAttrChange = (val) => {
  console.log('handleTargetAttrChange:', val)
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
    }

    if (!isTargetAttrDynamic.value) {
      submitData.targetValue = ''
    }
    
    if (isEditMode.value) {
      try {
        const res = await sceneApi.updateRule(submitData.id, submitData)
        // 兼容不同的后端响应结构
        const success = res && (res.code === 200 || res === true || res?.id || res?.ruleName)
        if (success) {
          ElMessage.success('规则已更新')
          dialogVisible.value = false
          loadRules()
        } else {
          ElMessage.error(res?.message || '保存失败')
        }
      } catch (err) {
        // Axios 拦截器可能会抛出错误，这里捕获并显示
        const errorMsg = err.response?.data?.message || err.message || '更新失败'
        ElMessage.error(errorMsg)
      }
    } else {
      try {
        const res = await sceneApi.createRule(submitData)
        const success = res && (res.code === 200 || res === true || res?.id || res?.ruleName)
        if (success) {
          ElMessage.success('规则已创建')
          dialogVisible.value = false
          loadRules()
        } else {
          ElMessage.error(res?.message || '创建失败')
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
  try {
    const res = await sceneApi.getRuleLogs(rule.id)
    ruleLogs.value = res || []
  } catch (error) {
    console.error('加载日志失败:', error)
    ruleLogs.value = []
  }
  logDialogVisible.value = true
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

.action-icon-btn {
  background: #fff;
  border: 1px solid #d1d1d6;
  width: 32px;
  height: 32px;
}

.create-btn {
  height: 34px;
  padding: 0 16px;
  border-radius: 17px;
  font-weight: 600;
}

/* 内容区域 */
.content-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
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
.modern-form .el-form-item__label {
  font-weight: 600;
  padding-bottom: 4px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 12px;
}

.form-card {
  border: 1px solid #f2f2f7;
  border-radius: 12px;
  padding: 16px;
  background: #f9f9fb;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 14px;
  margin-bottom: 16px;
}

.trigger-card .card-header { color: #007aff; }
.action-card .card-header { color: #ff3b30; }

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
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #edf2f7;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  margin-bottom: 4px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #64748b;
  width: 70px;
}

.info-item .value {
  color: #1e293b;
  font-weight: 500;
}

.empty-logs {
  padding: 40px 0;
}
</style>
