<template>
  <div class="device-management-page">
    <div class="page-header">
      <h1 class="page-title">设备管理</h1>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加设备
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="searchQuery"
            placeholder="搜索设备名称或编码"
            clearable
            @change="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterStatus" placeholder="设备状态" clearable @change="handleSearch">
            <el-option label="全部状态" value="" />
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterGroup" placeholder="所属分组" clearable @change="handleSearch">
            <el-option label="全部分组" value="" />
            <el-option v-for="group in groupList" :key="group.id" :label="group.name" :value="group.id" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterProduct" placeholder="产品类型" clearable @change="handleSearch">
            <el-option label="全部产品" value="" />
            <el-option v-for="product in productList" :key="product.id" :label="product.name" :value="product.id" />
          </el-select>
        </el-col>
        <el-col :span="4" class="text-right">
          <el-button @click="loadDevices">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 设备列表 -->
    <el-card class="table-card" shadow="hover">
      <el-table
        :data="deviceList"
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="deviceCode" label="设备编码" min-width="150" />
        <el-table-column prop="productName" label="产品类型" min-width="120" />
        <el-table-column prop="groupName" label="所属分组" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastOnlineTime" label="最后上线" min-width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDevice(row)">
              详情
            </el-button>
            <el-button size="small" type="primary" link @click="editDevice(row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" link @click="deleteDevice(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑设备对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="deviceFormRef"
        :model="deviceForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="deviceForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input
            v-model="deviceForm.deviceCode"
            placeholder="请输入设备编码"
            :disabled="isEditMode"
          />
        </el-form-item>
        <el-form-item label="产品类型" prop="productId">
          <el-select v-model="deviceForm.productId" placeholder="请选择产品类型" style="width: 100%">
            <el-option v-for="product in productList" :key="product.id" :label="product.name" :value="product.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属分组" prop="groupId">
          <GroupSelector v-model="deviceForm.groupId" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="deviceForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDevice" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { getDeviceList, createDevice, updateDevice, deleteDevice as deleteDeviceAPI } from '@/api/device'
import { getGroupTree } from '@/api/group'
import { getProductList } from '@/api/product'
import GroupSelector from '@/components/GroupSelector.vue'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加设备')
const isEditMode = ref(false)
const deviceFormRef = ref(null)

// 搜索和筛选
const searchQuery = ref('')
const filterStatus = ref('')
const filterGroup = ref('')
const filterProduct = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 设备列表
const deviceList = ref([])

// 产品列表
const productList = ref([])

// 分组列表
const groupList = ref([])

// 设备表单
const deviceForm = reactive({
  id: null,
  deviceName: '',
  deviceCode: '',
  productId: null,
  groupId: null,
  remark: ''
})

// 表单验证规则
const rules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceCode: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择产品类型', trigger: 'change' }],
  groupId: [{ required: true, message: '请选择所属分组', trigger: 'change' }]
}

// 加载设备列表
const loadDevices = async () => {
  try {
    loading.value = true
    const result = await getDeviceList({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value,
      status: filterStatus.value,
      groupId: filterGroup.value,
      productId: filterProduct.value
    })
    if (result.code === 200 && result.data) {
      deviceList.value = result.data.list || []
      total.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

// 加载产品列表
const loadProducts = async () => {
  try {
    const result = await getProductList({ page: 1, pageSize: 1000 })
    if (result.code === 200 && result.data) {
      productList.value = result.data.list || []
    }
  } catch (error) {
    console.error('加载产品列表失败:', error)
  }
}

// 加载分组列表
const loadGroups = async () => {
  try {
    const result = await getGroupTree()
    if (result.tree) {
      groupList.value = flattenTree(result.tree)
    }
  } catch (error) {
    console.error('加载分组列表失败:', error)
  }
}

// 扁平化树形数据
const flattenTree = (tree) => {
  const result = []
  const flatten = (nodes) => {
    if (!Array.isArray(nodes)) return
    nodes.forEach(node => {
      result.push({
        id: node.id,
        name: node.name,
        parentId: node.parentId || 0
      })
      if (node.children && node.children.length > 0) {
        flatten(node.children)
      }
    })
  }
  flatten(tree)
  return result
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadDevices()
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加设备'
  isEditMode.value = false
  dialogVisible.value = true
}

// 查看设备详情
const viewDevice = (device) => {
  ElMessage.info('设备详情功能开发中...')
}

// 编辑设备
const editDevice = (device) => {
  dialogTitle.value = '编辑设备'
  isEditMode.value = true
  Object.assign(deviceForm, {
    id: device.id,
    deviceName: device.deviceName,
    deviceCode: device.deviceCode,
    productId: device.productId,
    groupId: device.groupId,
    remark: device.remark || ''
  })
  dialogVisible.value = true
}

// 删除设备
const deleteDevice = (device) => {
  ElMessageBox.confirm(
    `确定要删除设备"${device.deviceName}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const result = await deleteDeviceAPI(device.deviceCode)
      if (result.code === 200) {
        ElMessage.success('设备删除成功')
        loadDevices()
      }
    } catch (error) {
      ElMessage.error('设备删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 保存设备
const saveDevice = async () => {
  if (!deviceFormRef.value) return

  await deviceFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      saving.value = true
      let result
      if (isEditMode.value) {
        // 编辑模式：更新设备
        result = await updateDevice(deviceForm)
      } else {
        // 添加模式：新增设备
        result = await createDevice(deviceForm)
      }

      if (result.code === 200) {
        ElMessage.success(isEditMode.value ? '设备更新成功' : '设备添加成功')
        dialogVisible.value = false
        loadDevices()
      }
    } catch (error) {
      ElMessage.error(isEditMode.value ? '设备更新失败' : '设备添加失败')
    } finally {
      saving.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(deviceForm, {
    id: null,
    deviceName: '',
    deviceCode: '',
    productId: null,
    groupId: null,
    remark: ''
  })
  deviceFormRef.value?.clearValidate()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadDevices()
}

const handlePageChange = (val) => {
  currentPage.value = val
  loadDevices()
}

onMounted(() => {
  loadDevices()
  loadProducts()
  loadGroups()
})
</script>

<style scoped>
.device-management-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.text-right {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .device-management-page {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}
</style>
