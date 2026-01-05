<template>
  <div class="alarm-rules-container">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">⚙️ 告警阈值设置</h2>
        <div class="stats-quick">
          <span class="stat-item">总规则 <strong>{{ stats.total }}</strong></span>
          <span class="stat-item active">启用 <strong>{{ stats.active }}</strong></span>
        </div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleCreate" size="default">
          + 新建规则
        </el-button>
        <el-button @click="handleBatchCreate" size="default">
          批量设置
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input 
        v-model="filters.keyword" 
        placeholder="搜索规则名称" 
        style="width: 200px;"
        clearable
        @change="loadRules"
      />
      
      <el-select v-model="filters.level" placeholder="告警级别" style="width: 130px;" @change="loadRules" clearable>
        <el-option label="🔴 严重" value="critical"></el-option>
        <el-option label="🟡 警告" value="warning"></el-option>
        <el-option label="🔵 提示" value="info"></el-option>
      </el-select>

      <el-select v-model="filters.scope" placeholder="适用范围" style="width: 130px;" @change="loadRules" clearable>
        <el-option label="单设备" value="single"></el-option>
        <el-option label="批量设备" value="batch"></el-option>
        <el-option label="按产品" value="product"></el-option>
        <el-option label="按分组" value="group"></el-option>
      </el-select>
    </div>

    <!-- 规则列表（表格形式） -->
    <div class="rules-table">
      <el-table :data="rules" style="width: 100%" stripe>
        <el-table-column label="规则名称" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span :class="['level-badge', `level-${row.alarmLevel}`]">
                {{ getLevelIcon(row.alarmLevel) }}
              </span>
              <span style="font-weight: 600;">{{ row.ruleName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="触发条件" width="280">
          <template #default="{ row }">
            <div class="condition-text">
              <span class="metric">{{ getMetricText(row.conditionMetric) }}</span>
              <span class="operator">{{ row.conditionOperator }}</span>
              <span class="value">{{ row.conditionThreshold }}{{ getMetricUnit(row.conditionMetric) }}</span>
              <span v-if="row.conditionDuration" class="duration">持续{{ row.conditionDuration }}s</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="适用范围" width="150">
          <template #default="{ row }">
            <el-tag size="small" type="info">
              {{ getScopeText(row.deviceScope) }} ({{ row.deviceCount }}台)
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="通知设置" width="200">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; gap: 4px;">
              <span style="font-size: 13px;">👥 {{ row.notifyUsers?.length || 0 }}人</span>
              <div style="display: flex; gap: 4px;">
                <el-tag v-for="ch in row.notifyChannels" :key="ch" size="small">{{ ch }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="触发统计" width="120" align="center">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; gap: 2px;">
              <span style="font-size: 12px; color: #999;">今日</span>
              <span style="font-size: 16px; font-weight: 600; color: #667eea;">{{ row.todayCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link @click="handleCopy(row)">复制</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 原卡片列表（备用） -->
    <div class="rules-grid" style="display: none;">
      <div v-for="rule in rules" :key="rule.id" class="rule-card">
        <div class="rule-header">
          <div class="rule-title-row">
            <span :class="['rule-level', `level-${rule.alarmLevel}`]">
              {{ getLevelText(rule.alarmLevel) }}
            </span>
            <h3 class="rule-name">{{ rule.ruleName }}</h3>
            <el-switch 
              v-model="rule.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(rule)"
            />
          </div>
          <div class="rule-meta">
            <span class="meta-item">📱 {{ rule.deviceScope === 'single' ? '单设备' : rule.deviceScope === 'batch' ? '批量设备' : rule.deviceScope === 'product' ? '按产品' : '按分组' }}</span>
            <span class="meta-item">👥 通知 {{ rule.notifyUsers?.length || 0 }} 人</span>
            <span class="meta-item">⏱️ {{ formatTime(rule.createTime) }}</span>
          </div>
        </div>

        <div class="rule-body">
          <div class="condition-display">
            <span class="condition-metric">{{ getMetricText(rule.conditionMetric) }}</span>
            <span class="condition-operator">{{ rule.conditionOperator }}</span>
            <span class="condition-value">{{ rule.conditionThreshold }}{{ getMetricUnit(rule.conditionMetric) }}</span>
            <span v-if="rule.conditionDuration" class="condition-duration">
              持续 {{ rule.conditionDuration }}秒
            </span>
          </div>
          
          <div class="rule-devices">
            <span class="devices-label">适用设备：</span>
            <span class="devices-count">{{ rule.deviceCount || 0 }} 台</span>
          </div>

          <div class="rule-features">
            <el-tag v-if="rule.stackMode" size="small" type="warning">堆叠告警</el-tag>
            <el-tag size="small">{{ rule.notifyChannels?.join('、') || '系统通知' }}</el-tag>
          </div>
        </div>

        <div class="rule-footer">
          <div class="rule-stats">
            <span class="stat">今日触发 <strong>{{ rule.todayCount || 0 }}</strong></span>
            <span class="stat">累计触发 <strong>{{ rule.totalCount || 0 }}</strong></span>
          </div>
          <div class="rule-actions">
            <el-button type="text" @click="handleEdit(rule)">编辑</el-button>
            <el-button type="text" @click="handleCopy(rule)">复制</el-button>
            <el-button type="text" danger @click="handleDelete(rule)">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="rules.length === 0" class="empty-state">
        <span style="font-size: 64px; opacity: 0.3;">📭</span>
        <p>暂无告警规则</p>
        <el-button type="primary" @click="handleCreate">立即创建</el-button>
      </div>
    </div>

    <!-- 创建/编辑规则抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      size="600px"
      :before-close="handleDrawerClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="例如：温度过高告警" />
        </el-form-item>

        <el-form-item label="告警级别" prop="alarmLevel">
          <el-radio-group v-model="formData.alarmLevel">
            <el-radio-button label="critical">🔴 严重</el-radio-button>
            <el-radio-button label="warning">🟡 警告</el-radio-button>
            <el-radio-button label="info">🔵 提示</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="适用范围" prop="deviceScope">
          <el-radio-group v-model="formData.deviceScope">
            <el-radio-button label="single">单设备</el-radio-button>
            <el-radio-button label="batch">批量设备</el-radio-button>
            <el-radio-button label="product">按产品</el-radio-button>
            <el-radio-button label="group">按分组</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择设备" prop="deviceIds" v-if="formData.deviceScope === 'single' || formData.deviceScope === 'batch'">
          <el-select 
            v-model="formData.deviceIds" 
            multiple 
            placeholder="请选择设备"
            style="width: 100%;"
          >
            <el-option 
              v-for="device in deviceOptions" 
              :key="device.id" 
              :label="device.deviceName" 
              :value="device.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择产品" prop="productId" v-if="formData.deviceScope === 'product'">
          <el-select v-model="formData.productId" placeholder="请选择产品" style="width: 100%;">
            <el-option 
              v-for="product in productOptions" 
              :key="product.id" 
              :label="product.productName" 
              :value="product.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择分组" prop="groupId" v-if="formData.deviceScope === 'group'">
          <el-select v-model="formData.groupId" placeholder="请选择分组" style="width: 100%;">
            <el-option 
              v-for="group in groupOptions" 
              :key="group.id" 
              :label="group.groupName" 
              :value="group.id"
            />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">触发条件</el-divider>

        <el-form-item label="监控指标" prop="conditionMetric">
          <el-select v-model="formData.conditionMetric" placeholder="请选择监控指标" style="width: 100%;">
            <el-option label="温度" value="temperature" />
            <el-option label="湿度" value="humidity" />
            <el-option label="电量" value="battery" />
            <el-option label="信号强度" value="signal" />
            <el-option label="离线时长" value="offline_duration" />
          </el-select>
        </el-form-item>

        <el-form-item label="条件" prop="conditionOperator">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="formData.conditionOperator" style="width: 100%;">
                <el-option label="大于 >" value=">" />
                <el-option label="大于等于 ≥" value=">=" />
                <el-option label="小于 <" value="<" />
                <el-option label="小于等于 ≤" value="<=" />
                <el-option label="等于 =" value="=" />
              </el-select>
            </el-col>
            <el-col :span="16">
              <el-input-number 
                v-model="formData.conditionThreshold" 
                :controls="false"
                style="width: 100%;"
                placeholder="阈值"
              />
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="持续时长" prop="conditionDuration">
          <el-input-number 
            v-model="formData.conditionDuration" 
            :min="0"
            :step="60"
            placeholder="秒"
            style="width: 100%;"
          />
          <span style="color: #999; font-size: 12px; margin-left: 8px;">满足条件持续多久后触发（0表示立即触发）</span>
        </el-form-item>

        <el-divider content-position="left">通知设置</el-divider>

        <el-form-item label="通知人员" prop="notifyUsers">
          <el-select v-model="formData.notifyUsers" multiple placeholder="请选择通知人员" style="width: 100%;">
            <el-option 
              v-for="user in userOptions" 
              :key="user.id" 
              :label="user.username" 
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="通知方式" prop="notifyChannels">
          <el-checkbox-group v-model="formData.notifyChannels">
            <el-checkbox label="system">系统通知</el-checkbox>
            <el-checkbox label="email">邮件</el-checkbox>
            <el-checkbox label="sms">短信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="告警堆叠">
          <el-switch v-model="formData.stackMode" />
          <span style="color: #999; font-size: 12px; margin-left: 8px;">开启后，恢复前不会重复告警</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDrawerClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/request'

// 统计数据
const stats = ref({
  total: 0,
  active: 0,
  inactive: 0
})

// 筛选条件
const filters = reactive({
  keyword: '',
  level: '',
  scope: ''
})

// 规则列表
const rules = ref([])

// 抽屉
const drawerVisible = ref(false)
const drawerTitle = ref('新建告警规则')
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  ruleName: '',
  alarmLevel: 'warning',
  deviceScope: 'single',
  deviceIds: [],
  productId: null,
  groupId: null,
  conditionMetric: '',
  conditionOperator: '>',
  conditionThreshold: null,
  conditionDuration: 0,
  notifyUsers: [],
  notifyChannels: ['system'],
  stackMode: true,
  status: 1
})

// 表单验证
const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  alarmLevel: [{ required: true, message: '请选择告警级别', trigger: 'change' }],
  deviceScope: [{ required: true, message: '请选择适用范围', trigger: 'change' }],
  conditionMetric: [{ required: true, message: '请选择监控指标', trigger: 'change' }],
  conditionOperator: [{ required: true, message: '请选择条件', trigger: 'change' }],
  conditionThreshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }],
  notifyUsers: [{ required: true, message: '请选择通知人员', trigger: 'change' }]
}

// 选项数据
const deviceOptions = ref([])
const productOptions = ref([])
const groupOptions = ref([])
const userOptions = ref([])

// 加载规则列表
const loadRules = async () => {
  try {
    // 模拟数据
    rules.value = [
      {
        id: 1,
        ruleName: '温度过高告警',
        alarmLevel: 'critical',
        deviceScope: 'single',
        deviceCount: 1,
        conditionMetric: 'temperature',
        conditionOperator: '>',
        conditionThreshold: 30,
        conditionDuration: 300,
        notifyUsers: [1, 2],
        notifyChannels: ['system', 'email'],
        stackMode: true,
        status: 1,
        todayCount: 5,
        totalCount: 23,
        createTime: '2026-01-05 10:00:00'
      },
      {
        id: 2,
        ruleName: '设备离线告警',
        alarmLevel: 'warning',
        deviceScope: 'product',
        deviceCount: 10,
        conditionMetric: 'offline_duration',
        conditionOperator: '>',
        conditionThreshold: 600,
        conditionDuration: 0,
        notifyUsers: [1],
        notifyChannels: ['system'],
        stackMode: true,
        status: 1,
        todayCount: 2,
        totalCount: 8,
        createTime: '2026-01-04 15:30:00'
      }
    ]
    
    stats.value = {
      total: rules.value.length,
      active: rules.value.filter(r => r.status === 1).length,
      inactive: rules.value.filter(r => r.status === 0).length
    }
  } catch (error) {
    console.error('加载规则失败:', error)
    ElMessage.error('加载规则失败')
  }
}

// 加载选项数据
const loadOptions = async () => {
  // 模拟数据
  deviceOptions.value = [
    { id: 1, deviceName: '温湿度传感器-01' },
    { id: 2, deviceName: '温湿度传感器-02' }
  ]
  productOptions.value = [
    { id: 1, productName: '温湿度传感器' }
  ]
  groupOptions.value = [
    { id: 1, groupName: '总分组' }
  ]
  userOptions.value = [
    { id: 1, username: '管理员' },
    { id: 2, username: '张三' }
  ]
}

// 新建规则
const handleCreate = () => {
  drawerTitle.value = '新建告警规则'
  Object.assign(formData, {
    id: null,
    ruleName: '',
    alarmLevel: 'warning',
    deviceScope: 'single',
    deviceIds: [],
    productId: null,
    groupId: null,
    conditionMetric: '',
    conditionOperator: '>',
    conditionThreshold: null,
    conditionDuration: 0,
    notifyUsers: [],
    notifyChannels: ['system'],
    stackMode: true,
    status: 1
  })
  drawerVisible.value = true
}

// 批量创建
const handleBatchCreate = () => {
  ElMessage.info('批量创建功能开发中...')
}

// 编辑规则
const handleEdit = (rule) => {
  drawerTitle.value = '编辑告警规则'
  Object.assign(formData, rule)
  drawerVisible.value = true
}

// 复制规则
const handleCopy = (rule) => {
  drawerTitle.value = '复制告警规则'
  Object.assign(formData, { ...rule, id: null, ruleName: rule.ruleName + ' (副本)' })
  drawerVisible.value = true
}

// 删除规则
const handleDelete = async (rule) => {
  try {
    await ElMessageBox.confirm('确定删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
    loadRules()
  } catch {
    // 取消删除
  }
}

// 状态切换
const handleStatusChange = async (rule) => {
  try {
    ElMessage.success(rule.status === 1 ? '规则已启用' : '规则已禁用')
    loadRules()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    ElMessage.success(formData.id ? '保存成功' : '创建成功')
    drawerVisible.value = false
    loadRules()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 关闭抽屉
const handleDrawerClose = () => {
  formRef.value?.resetFields()
  drawerVisible.value = false
}

// 工具函数
const getLevelIcon = (level) => {
  const map = { critical: '🔴', warning: '🟡', info: '🔵' }
  return map[level] || '🟡'
}

const getLevelText = (level) => {
  const map = { critical: '严重', warning: '警告', info: '提示' }
  return map[level] || level
}

const getScopeText = (scope) => {
  const map = {
    single: '单设备',
    batch: '批量',
    product: '产品',
    group: '分组'
  }
  return map[scope] || scope
}

const getMetricText = (metric) => {
  const map = {
    temperature: '温度',
    humidity: '湿度',
    battery: '电量',
    signal: '信号强度',
    offline_duration: '离线时长'
  }
  return map[metric] || metric
}

const getMetricUnit = (metric) => {
  const map = {
    temperature: '℃',
    humidity: '%',
    battery: '%',
    signal: 'dBm',
    offline_duration: '秒'
  }
  return map[metric] || ''
}

const formatTime = (time) => {
  if (!time) return '-'
  const now = new Date()
  const target = new Date(time)
  const diff = Math.floor((now - target) / 1000)
  
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 604800) return `${Math.floor(diff / 86400)}天前`
  return target.toLocaleDateString()
}

onMounted(() => {
  loadRules()
  loadOptions()
})
</script>

<style scoped>
.alarm-rules-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

/* 顶部工具栏 */
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

.toolbar-right {
  display: flex;
  gap: 12px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

/* 规则表格 */
.rules-table {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.level-badge {
  font-size: 14px;
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

.condition-text .duration {
  color: #999;
  font-size: 12px;
}

/* 规则网格 */
.rules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.rule-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.rule-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.rule-header {
  margin-bottom: 16px;
}

.rule-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.rule-level {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.rule-level.level-critical {
  background: #fef0f0;
  color: #f56c6c;
}

.rule-level.level-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.rule-level.level-info {
  background: #ecf5ff;
  color: #409eff;
}

.rule-name {
  flex: 1;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.rule-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rule-body {
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.condition-display {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 14px;
}

.condition-metric {
  font-weight: 600;
  color: #667eea;
}

.condition-operator {
  color: #e6a23c;
  font-weight: 600;
}

.condition-value {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.condition-duration {
  color: #999;
  font-size: 12px;
}

.rule-devices {
  margin-bottom: 12px;
  font-size: 13px;
  color: #666;
}

.devices-label {
  color: #999;
}

.devices-count {
  font-weight: 600;
  color: #667eea;
}

.rule-features {
  display: flex;
  gap: 8px;
}

.rule-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.rule-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #666;
}

.rule-stats .stat strong {
  color: #667eea;
  font-weight: 600;
  margin-left: 4px;
}

.rule-actions {
  display: flex;
  gap: 8px;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-state p {
  margin: 16px 0 24px;
  font-size: 14px;
}
</style>
