<template>
  <div class="product-detail">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/products' }">产品管理</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.productName || '产品详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">{{ product.productName }}</h1>
      <div class="page-actions">
        <el-button @click="goBack">← 返回列表</el-button>
        <el-button type="primary" @click="showEditDialog">编辑产品</el-button>
      </div>
    </div>

    <!-- 基本信息卡片 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><InfoFilled /></el-icon>
          <span>基本信息</span>
        </div>
      </template>
      <div class="info-grid">
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">产品名称</div>
            <div class="info-value">{{ product.productName || '-' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">产品型号</div>
            <div class="info-value">{{ product.productModel || '-' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">协议类型</div>
            <div class="info-value">{{ product.protocol || '-' }}</div>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">创建时间</div>
            <div class="info-value">{{ formatDateTime(product.createTime) }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">属性数量</div>
            <div class="info-value">{{ attributes.length }} 个</div>
          </div>
          <div class="info-item">
            <div class="info-label">命令数量</div>
            <div class="info-value">{{ commands.length }} 个</div>
          </div>
        </div>
        <div v-if="product.description" class="info-desc-row">
          <div class="info-label">产品描述</div>
          <div class="info-desc">{{ product.description }}</div>
        </div>
      </div>
    </el-card>

    <!-- Tab切换区域 -->
    <el-card class="tab-card">
      <el-tabs v-model="activeTab">
        <!-- 属性列表 -->
        <el-tab-pane label="属性列表" name="attributes">
          <div class="tab-header">
            <div class="tab-title">设备属性定义</div>
            <el-button type="primary" size="small" @click="showAddAttrDialog">
              <el-icon><Plus /></el-icon>添加属性
            </el-button>
          </div>
          <div v-if="attributes.length === 0" class="empty-state">
            <el-icon class="empty-icon" :size="48"><TrendCharts /></el-icon>
            <div class="empty-text">暂无属性，点击"添加属性"开始定义数据点</div>
          </div>
          <el-table v-else :data="attributes" stripe size="small" class="attr-table">
            <el-table-column prop="addr" label="标识符" width="200" />
            <el-table-column prop="attrName" label="属性名称" min-width="120" />
            <el-table-column prop="dataType" label="数据类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.dataType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="100">
              <template #default="{ row }">
                {{ row.unit || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="showEditAttrDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" link @click="deleteAttribute(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 命令列表 -->
        <el-tab-pane label="命令列表" name="commands">
          <div class="tab-header">
            <div class="tab-title">设备命令定义</div>
            <el-button type="primary" size="small" @click="showAddCmdDialog">
              <el-icon><Plus /></el-icon>添加命令
            </el-button>
          </div>
          <div v-if="commands.length === 0" class="empty-state">
            <el-icon class="empty-icon" :size="48"><Monitor /></el-icon>
            <div class="empty-text">暂无命令，点击"添加命令"开始定义控制指令</div>
          </div>
          <el-table v-else :data="commands" stripe size="small" class="cmd-table">
            <el-table-column prop="commandName" label="命令名称" min-width="150" />
            <el-table-column prop="addr" label="控制属性" width="120" />
            <el-table-column prop="commandValue" label="下发值" width="120" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="danger" link @click="deleteCommand(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑产品对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑产品" 
      width="600px"
    >
      <el-form :model="editForm" label-width="120px" :rules="editRules" ref="editFormRef">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="editForm.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品型号" prop="productModel">
          <el-input v-model="editForm.productModel" disabled />
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            产品型号创建后不可修改
          </div>
        </el-form-item>
        <el-form-item label="协议类型" prop="protocol">
          <el-select 
            v-model="editForm.protocol" 
            placeholder="请选择协议类型" 
            style="width: 100%;"
            disabled
          >
            <el-option label="MQTT" value="MQTT" />
          </el-select>
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            当前仅支持MQTT协议，其他协议类型正在开发中
          </div>
        </el-form-item>
        <el-form-item label="产品描述">
          <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入产品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateProduct">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑属性对话框 -->
    <el-dialog 
      v-model="attrDialogVisible" 
      :title="isEditAttrMode ? '编辑产品属性' : '添加产品属性'" 
      width="600px"
    >
      <el-form :model="attrForm" label-width="120px" :rules="attrRules" ref="attrFormRef">
        <el-form-item label="属性标识符" prop="addr">
          <el-input 
            v-model="attrForm.addr" 
            placeholder="如：tem、hum、battery" 
            :disabled="isEditAttrMode"
          />
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            <span v-if="isEditAttrMode">属性标识符创建后不可修改</span>
            <span v-else>英文标识符，用于数据上报的字段名，建议使用小写字母+下划线</span>
          </div>
        </el-form-item>
        <el-form-item label="属性名称" prop="attrName">
          <el-input v-model="attrForm.attrName" placeholder="如：温度、湿度、电池电量" />
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            中文显示名称，方便理解属性含义
          </div>
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="attrForm.dataType" placeholder="请选择数据类型" style="width: 100%;">
            <el-option label="整数 (int) - 如状态、计数" value="int" />
            <el-option label="浮点数 (float) - 如温度、湿度" value="float" />
            <!-- 暂时隐藏未使用的类型 -->
            <!-- <el-option label="字符串 (string) - 如文本、描述" value="string" /> -->
            <!-- <el-option label="布尔 (bool) - 如开关状态" value="bool" /> -->
          </el-select>
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            选择合适的数据类型，影响数据存储和处理方式
          </div>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="attrForm.unit" placeholder="如：℃、%、m/s（可选）" />
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            数值属性可填写单位，非数值属性可留空
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attrDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="isEditAttrMode ? updateAttribute() : addAttribute()">
          {{ isEditAttrMode ? '保存修改' : '确定添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加命令对话框 -->
    <el-dialog 
      v-model="cmdDialogVisible" 
      title="添加产品命令" 
      width="600px"
    >
      <el-form :model="cmdForm" label-width="120px" :rules="cmdRules" ref="cmdFormRef">
        <el-form-item label="命令名称" prop="commandName">
          <el-input v-model="cmdForm.commandName" placeholder="如：打开窗户、关闭灯光、设置模式" />
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            命令的显示名称，让用户知道这是什么操作
          </div>
        </el-form-item>
        <el-form-item label="控制属性" prop="addr">
          <el-select v-model="cmdForm.addr" placeholder="请选择要控制的属性" style="width: 100%;">
            <el-option
              v-for="attr in attributes"
              :key="attr.addr"
              :label="`${attr.attrName} (${attr.addr})`"
              :value="attr.addr"
            />
          </el-select>
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            选择要控制的属性，命令会修改该属性的值
          </div>
        </el-form-item>
        <el-form-item label="下发值" prop="commandValue">
          <el-input v-model="cmdForm.commandValue" placeholder="如：1、0、ON、OFF" />
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            命令执行时会将该值下发给设备，设备根据该值进行操作
          </div>
        </el-form-item>
        <el-alert 
          type="info"
          :closable="false"
        >
          <template #title>
            <div class="alert-title">
              <el-icon class="alert-icon" :size="16"><InfoFilled /></el-icon>
              示例
            </div>
          </template>
          如果有一个属性叫 "window"，可以创建两个命令：<br/>
          • 命令名：打开窗户 | 控制属性：window | 下发值：1<br/>
          • 命令名：关闭窗户 | 控制属性：window | 下发值：0
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="cmdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addCommand">确定添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { TrendCharts, Monitor, InfoFilled, Document, Plus } from '@element-plus/icons-vue'
import { productApi } from '@/api'

const route = useRoute()
const router = useRouter()

const product = ref({})
const attributes = ref([])
const commands = ref([])
const activeTab = ref('attributes')

const editDialogVisible = ref(false)
const attrDialogVisible = ref(false)
const cmdDialogVisible = ref(false)
const isEditAttrMode = ref(false) // 是否为编辑属性模式

const editFormRef = ref(null)
const attrFormRef = ref(null)
const cmdFormRef = ref(null)

// 编辑表单
const editForm = ref({
  productName: '',
  productModel: '',
  protocol: '',
  description: ''
})

// 属性表单
const attrForm = ref({
  addr: '',
  attrName: '',
  dataType: '',
  unit: ''
})

// 命令表单
const cmdForm = ref({
  commandName: '',
  addr: '',
  commandValue: ''
})

// 日期格式化
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  // 如果已经是正确格式，直接返回
  if (typeof dateTime === 'string' && dateTime.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/)) {
    return dateTime
  }
  // 处理ISO 8601格式（带T的格式）
  if (typeof dateTime === 'string' && dateTime.includes('T')) {
    return dateTime.replace('T', ' ').substring(0, 19)
  }
  // 否则进行格式化
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 表单验证规则
const editRules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  protocol: [{ required: true, message: '请选择协议类型', trigger: 'change' }]
}

const attrRules = {
  addr: [{ required: true, message: '请输入属性标识符', trigger: 'blur' }],
  attrName: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }]
}

const cmdRules = {
  commandName: [{ required: true, message: '请输入命令名称', trigger: 'blur' }],
  addr: [{ required: true, message: '请选择控制属性', trigger: 'change' }],
  commandValue: [{ required: true, message: '请输入下发值', trigger: 'blur' }]
}

// 加载产品详情
const loadProduct = async () => {
  try {
    const productId = route.params.id
    const res = await productApi.getProductDetail(productId)
    // res 是后端返回的 data 字段，包含产品信息
    product.value = res
    // 如果后端返回的数据中有attrs和commands，就不需要单独加载了
    if (res.attrs) {
      attributes.value = res.attrs
    }
    if (res.commands) {
      commands.value = res.commands
    }
  } catch (error) {
    console.error('加载产品详情失败:', error)
    ElMessage.error('加载产品详情失败')
  }
}

// 加载属性列表
const loadAttributes = async () => {
  try {
    const productId = route.params.id
    const res = await productApi.getProductAttributes(productId)
    // res 是后端返回的 data 字段，直接是数组
    attributes.value = res || []
  } catch (error) {
    console.error('加载属性列表失败:', error)
  }
}

// 加载命令列表
const loadCommands = async () => {
  try {
    const productId = route.params.id
    const res = await productApi.getProductCommands(productId)
    // res 是后端返回的 data 字段，直接是数组
    commands.value = res || []
  } catch (error) {
    console.error('加载命令列表失败:', error)
  }
}

// 显示编辑对话框
const showEditDialog = () => {
  editForm.value = {
    productName: product.value.productName,
    productModel: product.value.productModel,
    protocol: product.value.protocol,
    description: product.value.description || ''
  }
  editDialogVisible.value = true
}

// 更新产品信息
const updateProduct = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    
    await productApi.updateProduct({
      id: route.params.id,
      productName: editForm.value.productName,
      productModel: editForm.value.productModel,
      protocol: editForm.value.protocol,
      description: editForm.value.description
    })
    
    ElMessage.success('产品信息已更新')
    editDialogVisible.value = false
    loadProduct()
  } catch (error) {
    if (error !== false) {
      console.error('更新产品失败:', error)
      ElMessage.error('更新产品失败')
    }
  }
}

// 显示添加属性对话框
const showAddAttrDialog = () => {
  isEditAttrMode.value = false
  attrForm.value = {
    addr: '',
    attrName: '',
    dataType: '',
    unit: ''
  }
  attrDialogVisible.value = true
}

// 显示编辑属性对话框
const showEditAttrDialog = (attr) => {
  isEditAttrMode.value = true
  attrForm.value = {
    id: attr.id,
    addr: attr.addr,
    attrName: attr.attrName || '',
    dataType: attr.dataType || '',
    unit: attr.unit || '',
    description: attr.description || ''
  }
  attrDialogVisible.value = true
}

// 添加属性
const addAttribute = async () => {
  if (!attrFormRef.value) return
  
  try {
    await attrFormRef.value.validate()
    
    // 检查标识符是否重复
    if (attributes.value.some(a => a.addr === attrForm.value.addr)) {
      ElMessage.error('属性标识符已存在')
      return
    }
    
    await productApi.addProductAttribute({
      productId: route.params.id,
      addr: attrForm.value.addr,
      attrName: attrForm.value.attrName,
      dataType: attrForm.value.dataType,
      unit: attrForm.value.unit
    })
    
    ElMessage.success('属性添加成功')
    attrDialogVisible.value = false
    loadAttributes()
  } catch (error) {
    if (error !== false) {
      console.error('添加属性失败:', error)
      ElMessage.error('添加属性失败')
    }
  }
}

// 更新属性
const updateAttribute = async () => {
  // 编辑模式下不进行表单验证，直接提交数据
  try {
    // 确保ID存在
    if (!attrForm.value.id) {
      ElMessage.error('属性ID不能为空')
      console.error('属性ID为空，attrForm:', attrForm.value)
      return
    }
    
    // 验证名称
    if (!attrForm.value.attrName || attrForm.value.attrName.trim() === '') {
      ElMessage.error('请输入属性名称')
      return
    }
    
    const updateData = {
      id: attrForm.value.id,
      attrName: attrForm.value.attrName.trim(),
      unit: (attrForm.value.unit || '').trim(),
      description: (attrForm.value.description || '').trim()
    }
    
    console.log('发送更新属性请求，参数:', updateData)
    
    await productApi.updateProductAttribute(updateData)
    
    ElMessage.success('属性更新成功')
    attrDialogVisible.value = false
    loadAttributes()
  } catch (error) {
    console.error('更新属性失败:', error)
    const errorMsg = error?.message || error?.response?.data?.message || '更新属性失败'
    ElMessage.error(errorMsg)
  }
}

// 删除属性
const deleteAttribute = async (attr) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除属性"${attr.attrName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await productApi.deleteProductAttribute(attr.id)
    ElMessage.success('属性已删除')
    loadAttributes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除属性失败:', error)
      ElMessage.error('删除属性失败')
    }
  }
}

// 显示添加命令对话框
const showAddCmdDialog = () => {
  if (attributes.value.length === 0) {
    ElMessage.warning('请先添加属性后再创建命令')
    return
  }
  
  cmdForm.value = {
    commandName: '',
    addr: '',
    commandValue: ''
  }
  cmdDialogVisible.value = true
}

// 添加命令
const addCommand = async () => {
  if (!cmdFormRef.value) return
  
  try {
    await cmdFormRef.value.validate()
    
    await productApi.addProductCommand({
      productId: route.params.id,
      addr: cmdForm.value.addr,
      commandName: cmdForm.value.commandName,
      commandValue: cmdForm.value.commandValue,
      description: `控制属性 ${cmdForm.value.addr} 为 ${cmdForm.value.commandValue}`
    })
    
    ElMessage.success('命令添加成功')
    cmdDialogVisible.value = false
    loadCommands()
  } catch (error) {
    if (error !== false) {
      console.error('添加命令失败:', error)
      ElMessage.error('添加命令失败')
    }
  }
}

// 删除命令
const deleteCommand = async (cmd) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除命令"${cmd.commandName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await productApi.deleteProductCommand(cmd.id)
    ElMessage.success('命令已删除')
    loadCommands()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除命令失败:', error)
      ElMessage.error('删除命令失败')
    }
  }
}

// 返回列表
const goBack = () => {
  router.push('/products')
}

onMounted(() => {
  loadProduct()
  // attrs 和 commands 已经在 loadProduct 中加载了，不需要重复调用
  // loadAttributes()
  // loadCommands()
})
</script>

<style scoped>
.product-detail {
  padding: 12px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.breadcrumb {
  margin-bottom: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 8px;
}

.info-card {
  margin-bottom: 12px;
}

.card-header {
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1d1d1f;
}

.header-icon {
  color: #667eea;
  font-size: 16px;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #86868b;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1.4;
}

.info-desc-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 8px;
  border-top: 1px solid #e5e5e7;
}

.info-desc {
  color: #86868b;
  font-size: 13px;
  line-height: 1.5;
}

.tab-card {
  margin-top: 12px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tab-title {
  font-weight: 600;
  font-size: 14px;
  color: #1d1d1f;
}

.empty-state {
  text-align: center;
  padding: 32px 20px;
  color: #86868b;
}

.empty-icon {
  color: #c7c7cc;
  margin-bottom: 12px;
  opacity: 0.6;
}

.empty-text {
  font-size: 13px;
  color: #86868b;
}

.input-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 8px;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.hint-icon {
  color: #667eea;
  margin-top: 2px;
  flex-shrink: 0;
}

.alert-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.alert-icon {
  color: #409eff;
}

/* Element Plus 样式覆盖 */
:deep(.el-card__header) {
  padding: 10px 12px;
  border-bottom: 1px solid #e5e5e7;
  background: #fafafa;
}

/* 基本信息卡片移除头部底部边框 */
.info-card :deep(.el-card__header) {
  border-bottom: none;
}

:deep(.el-card__body) {
  padding: 12px;
}

:deep(.el-tabs__header) {
  margin-bottom: 12px;
}

:deep(.el-tabs__item) {
  font-size: 13px;
  padding: 8px 16px;
  height: 36px;
  line-height: 20px;
}

/* 表格紧凑样式 */
.attr-table,
.cmd-table {
  font-size: 13px;
}

:deep(.attr-table .el-table__header th),
:deep(.cmd-table .el-table__header th) {
  padding: 8px 0;
  background: #fafafa;
  font-weight: 600;
  color: #1d1d1f;
  height: 36px;
}

:deep(.attr-table .el-table__body td),
:deep(.cmd-table .el-table__body td) {
  padding: 8px 0;
  height: 38px;
}

:deep(.attr-table .el-table__row),
:deep(.cmd-table .el-table__row) {
  height: 38px;
}

:deep(.attr-table .el-table__cell),
:deep(.cmd-table .el-table__cell) {
  padding: 8px 0;
}

.input-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
