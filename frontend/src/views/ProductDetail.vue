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
          <span>基本信息</span>
        </div>
      </template>
      <el-row :gutter="24">
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">产品名称</div>
            <div class="info-value">{{ product.productName || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">产品型号</div>
            <div class="info-value">{{ product.productModel || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">协议类型</div>
            <div class="info-value">{{ product.protocol || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">创建时间</div>
            <div class="info-value">{{ product.createTime || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">属性数量</div>
            <div class="info-value">{{ attributes.length }} 个</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <div class="info-label">命令数量</div>
            <div class="info-value">{{ commands.length }} 个</div>
          </div>
        </el-col>
      </el-row>
      <el-divider />
      <div class="info-item">
        <div class="info-label">产品描述</div>
        <div class="info-desc">{{ product.description || '暂无描述' }}</div>
      </div>
    </el-card>

    <!-- Tab切换区域 -->
    <el-card class="tab-card">
      <el-tabs v-model="activeTab">
        <!-- 属性列表 -->
        <el-tab-pane label="属性列表" name="attributes">
          <div class="tab-header">
            <div class="tab-title">设备属性定义</div>
            <el-button type="primary" @click="showAddAttrDialog">+ 添加属性</el-button>
          </div>
          <div v-if="attributes.length === 0" class="empty-state">
            <el-icon class="empty-icon" :size="64"><TrendCharts /></el-icon>
            <div class="empty-text">暂无属性，点击上方"添加属性"开始定义数据点</div>
          </div>
          <div v-else class="attr-list">
            <div v-for="attr in attributes" :key="attr.id" class="list-card">
              <div class="list-card-main">
                <div class="list-card-title">{{ attr.addr }} - {{ attr.attrName }}</div>
                <div class="list-card-desc">类型：{{ attr.dataType }} | 单位：{{ attr.unit || '无' }}</div>
              </div>
              <div class="list-card-actions">
                <el-button size="small" type="danger" @click="deleteAttribute(attr)">删除</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 命令列表 -->
        <el-tab-pane label="命令列表" name="commands">
          <div class="tab-header">
            <div class="tab-title">设备命令定义</div>
            <el-button type="primary" @click="showAddCmdDialog">+ 添加命令</el-button>
          </div>
          <div v-if="commands.length === 0" class="empty-state">
            <el-icon class="empty-icon" :size="64"><Monitor /></el-icon>
            <div class="empty-text">暂无命令，点击上方"添加命令"开始定义控制指令</div>
          </div>
          <div v-else class="cmd-list">
            <div v-for="cmd in commands" :key="cmd.id" class="list-card">
              <div class="list-card-main">
                <div class="list-card-title">{{ cmd.commandName }}</div>
                <div class="list-card-desc">控制属性：{{ cmd.addr }} | 下发值：{{ cmd.commandValue }}</div>
              </div>
              <div class="list-card-actions">
                <el-button size="small" type="danger" @click="deleteCommand(cmd)">删除</el-button>
              </div>
            </div>
          </div>
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
          <el-select v-model="editForm.protocol" placeholder="请选择协议类型" style="width: 100%;">
            <el-option label="MQTT" value="MQTT" />
            <el-option label="HTTP" value="HTTP" />
            <el-option label="CoAP" value="CoAP" />
            <el-option label="Modbus" value="Modbus" />
          </el-select>
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

    <!-- 添加属性对话框 -->
    <el-dialog 
      v-model="attrDialogVisible" 
      title="添加产品属性" 
      width="600px"
    >
      <el-form :model="attrForm" label-width="120px" :rules="attrRules" ref="attrFormRef">
        <el-form-item label="属性标识符" prop="addr">
          <el-input v-model="attrForm.addr" placeholder="如：tem、hum、battery" />
          <div class="input-hint">
            <el-icon class="hint-icon" :size="14"><InfoFilled /></el-icon>
            英文标识符，用于数据上报的字段名，建议使用小写字母+下划线
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
        <el-button type="primary" @click="addAttribute">确定添加</el-button>
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
import { TrendCharts, Monitor, InfoFilled, Document } from '@element-plus/icons-vue'
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
  attrForm.value = {
    addr: '',
    attrName: '',
    dataType: '',
    unit: ''
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
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 12px;
}

.info-card {
  margin-bottom: 24px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.info-item {
  margin-bottom: 16px;
}

.info-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 6px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.info-desc {
  color: #86868b;
  font-size: 14px;
  line-height: 1.6;
}

.tab-card {
  margin-top: 24px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.tab-title {
  font-weight: 500;
  font-size: 15px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #86868b;
}

.empty-icon {
  color: #c7c7cc;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  font-size: 14px;
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

.attr-list,
.cmd-list {
  display: grid;
  gap: 12px;
}

.list-card {
  background: #f5f5f7;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-card-main {
  flex: 1;
}

.list-card-title {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 6px;
}

.list-card-desc {
  font-size: 13px;
  color: #86868b;
}

.list-card-actions {
  display: flex;
  gap: 8px;
}

.input-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
