<template>
  <div>
    <!-- 页面标题 -->
    <h1 class="page-title">产品管理</h1>

    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="🔍 搜索产品名称或型号..."
          clearable
          style="width: 300px;"
          @input="handleSearch"
        />
        <el-button type="primary" @click="showCreateDialog">+ 创建产品</el-button>
      </div>
    </el-card>

    <!-- 产品列表表格 -->
    <el-card>
      <el-table :data="products" border stripe>
        <el-table-column prop="id" label="产品ID" width="80" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="productModel" label="产品型号" />
        <el-table-column prop="protocol" label="协议类型" />
        <el-table-column label="属性数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.attrCount || 0 }} 个
          </template>
        </el-table-column>
        <el-table-column label="命令数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.cmdCount || 0 }} 个
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑产品对话框 -->
    <el-dialog 
      v-model="productDialogVisible" 
      :title="isEditMode ? '编辑产品' : '创建产品'" 
      width="600px"
    >
      <el-form :model="productForm" label-width="120px" :rules="formRules" ref="productFormRef">
        <el-form-item label="产品名称" prop="productName">
          <el-input 
            v-model="productForm.productName" 
            placeholder="如：智能水表、空气监测仪、温湿度传感器"
          />
          <div class="input-hint">💡 给产品起一个清晰易懂的名称，方便后续管理</div>
        </el-form-item>
        <el-form-item label="产品型号" prop="productModel">
          <el-input 
            v-model="productForm.productModel" 
            placeholder="如：WATER-PRO、AIR-01、TEM-SENSOR"
            :disabled="isEditMode"
          />
          <div class="input-hint">
            <template v-if="!isEditMode">💡 产品型号是唯一标识，建议使用英文+数字组合，创建后不可修改</template>
            <template v-else>💡 产品型号创建后不可修改</template>
          </div>
        </el-form-item>
        <el-form-item label="协议类型" prop="protocol">
          <el-select v-model="productForm.protocol" placeholder="请选择协议类型" style="width: 100%;">
            <el-option label="MQTT（推荐，轻量级物联网协议）" value="MQTT" />
            <el-option label="HTTP（适合请求-响应场景）" value="HTTP" />
            <el-option label="CoAP（适合资源受限设备）" value="CoAP" />
            <el-option label="Modbus（工业设备常用）" value="Modbus" />
          </el-select>
          <div class="input-hint">💡 选择设备使用的通信协议，影响设备接入方式</div>
        </el-form-item>
        <el-form-item label="产品描述" prop="description">
          <el-input 
            v-model="productForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="描述产品的功能、用途、适用场景等..."
          />
          <div class="input-hint">💡 详细描述有助于团队成员理解产品定位</div>
        </el-form-item>
      </el-form>

      <!-- 底部提示 -->
      <el-alert 
        v-if="!isEditMode"
        title="📋 下一步操作"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        创建产品后，点击"查看详情"跳转到详情页，添加"属性"和"命令"来完整定义设备的物模型：<br/>
        • <strong>属性</strong>：设备上报的数据字段（如温度、湿度、电量）<br/>
        • <strong>命令</strong>：平台下发给设备的控制指令（如开关、设置参数）
      </el-alert>

      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProduct">{{ isEditMode ? '保存修改' : '确定创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { productApi } from '@/api'

const router = useRouter()
const products = ref([])
const searchKeyword = ref('')
const productDialogVisible = ref(false)
const isEditMode = ref(false)
const productFormRef = ref(null)

// 表单数据
const productForm = ref({
  id: null,
  productName: '',
  productModel: '',
  protocol: 'MQTT',
  description: ''
})

// 表单验证规则
const formRules = {
  productName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  productModel: [
    { required: true, message: '请输入产品型号', trigger: 'blur' }
  ],
  protocol: [
    { required: true, message: '请选择协议类型', trigger: 'change' }
  ]
}

// 加载产品列表
const loadProducts = async () => {
  try {
    console.log('开始加载产品列表...')
    const res = await productApi.getProductList()
    console.log('产品列表响应:', JSON.stringify(res, null, 2))
    
    if (!res) {
      console.error('响应为空')
      ElMessage.error('服务器无响应')
      return
    }
    
    // res 是后端返回的 data 字段，包含 {list, total, page, pageSize, totalPages}
    if (res.list && Array.isArray(res.list)) {
      console.log('使用 res.list，长度:', res.list.length)
      products.value = res.list
    } else if (Array.isArray(res)) {
      console.log('使用 res，长度:', res.length)
      products.value = res
    } else {
      console.warn('数据格式不正确:', typeof res)
      products.value = []
    }
    
    console.log('最终 products.value:', products.value)
    console.log('产品数量:', products.value.length)
  } catch (error) {
    console.error('加载产品列表失败:', error)
    console.error('错误堆栈:', error.stack)
    ElMessage.error('加载产品列表失败: ' + error.message)
  }
}

// 搜索产品
const handleSearch = () => {
  const keyword = searchKeyword.value.toLowerCase().trim()
  if (!keyword) {
    loadProducts()
    return
  }
  
  products.value = products.value.filter(p => 
    (p.productName && p.productName.toLowerCase().includes(keyword)) ||
    (p.productModel && p.productModel.toLowerCase().includes(keyword))
  )
}

// 显示创建对话框
const showCreateDialog = () => {
  isEditMode.value = false
  productForm.value = {
    id: null,
    productName: '',
    productModel: '',
    protocol: 'MQTT',
    description: ''
  }
  productDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEditMode.value = true
  productForm.value = {
    id: row.id,
    productName: row.productName,
    productModel: row.productModel,
    protocol: row.protocol,
    description: row.description || ''
  }
  productDialogVisible.value = true
}

// 提交产品（创建或编辑）
const submitProduct = async () => {
  if (!productFormRef.value) return
  
  try {
    await productFormRef.value.validate()
    
    if (isEditMode.value) {
      // 编辑模式
      const res = await productApi.updateProduct({
        id: productForm.value.id,
        productName: productForm.value.productName,
        productModel: productForm.value.productModel,
        protocol: productForm.value.protocol,
        description: productForm.value.description
      })
      
      ElMessage.success('产品信息已更新')
      productDialogVisible.value = false
      loadProducts()
    } else {
      // 创建模式
      const res = await productApi.createProduct({
        name: productForm.value.productName,
        code: productForm.value.productModel,
        protocol: productForm.value.protocol,
        description: productForm.value.description
      })
      
      ElMessage.success('✅ 产品创建成功！')
      productDialogVisible.value = false
      // 创建成功后跳转到详情页
      if (res && res.id) {
        router.push(`/products/${res.id}`)
      } else {
        loadProducts()
      }
    }
  } catch (error) {
    if (error !== false) { // 排除表单验证失败
      console.error('提交失败:', error)
      ElMessage.error(isEditMode.value ? '更新产品失败' : '创建产品失败')
    }
  }
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/products/${row.id}`)
}

// 删除产品
const deleteProduct = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品"${row.productName}"吗？\n\n删除后该产品的所有属性和命令也将被删除，此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await productApi.deleteProduct(row.id)
    ElMessage.success('产品已删除')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除产品失败:', error)
      ElMessage.error('删除产品失败')
    }
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 24px;
  letter-spacing: -0.02em;
}

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
  line-height: 1.4;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}
</style>
