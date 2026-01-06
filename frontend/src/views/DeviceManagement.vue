<template>
  <div class="device-management-page">
    <h1 class="page-title">设备管理</h1>
    
    <div class="device-container">
      <!-- 左侧分组树 -->
      <el-card class="tree-panel" shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <strong style="font-size: 15px;">设备分组</strong>
          </div>
        </template>
        
        <!-- 全部设备按钮 -->
        <div 
          class="all-devices-btn"
          :class="{ active: currentGroupId === null }"
          @click="selectAllDevices"
        >
          <div class="btn-left">
            <el-icon><List /></el-icon>
            <span>全部设备</span>
          </div>
          <span class="device-count">{{ totalDeviceCount }}</span>
        </div>
        
        <!-- 分组树 -->
        <GroupTree 
          :groups="groups" 
          :current-group-id="currentGroupId"
          :show-actions="false"
          :show-count="false"
          @select="selectGroup"
        />
        
        <div class="tree-hint">
          💡 点击分组查看该分组下的设备
        </div>
      </el-card>

      <!-- 右侧设备列表 -->
      <el-card class="device-panel" shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 20px;">
            <h3 style="margin: 0; font-size: 16px;">{{ currentGroupTitle }}</h3>
            <div style="display: flex; gap: 12px;">
              <el-input
                v-model="searchQuery"
                placeholder="🔍 搜索设备名称或编码"
                clearable
                style="width: 240px;"
                @keyup.enter="handleSearch"
              />
              <el-select
                v-model="filterStatus"
                placeholder="设备状态"
                clearable
                filterable
                style="width: 120px;"
              >
                <el-option label="全部状态" value="" />
                <el-option label="在线" value="online" />
                <el-option label="离线" value="offline" />
              </el-select>
              <el-select
                v-model="filterProduct"
                placeholder="产品类型"
                clearable
                filterable
                style="width: 200px;"
                @visible-change="(visible) => { if(visible) console.log('产品下拉框展开，productList长度:', productList.length) }"
              >
                <el-option label="全部产品" value="" />
                <el-option v-for="product in productList" :key="product.id" :label="getProductLabel(product)" :value="product.id" />
              </el-select>
              <el-button type="primary" size="default" @click="handleSearch">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button size="default" @click="refreshDevices">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
              <el-button type="primary" size="default" @click="openAddDialog">
                <el-icon><Plus /></el-icon>
                添加设备
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table 
          :data="deviceList" 
          stripe 
          v-loading="loading"
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
          <el-table-column prop="deviceName" label="设备名称" min-width="150" />
          <el-table-column prop="deviceCode" label="设备编码" min-width="150" />
          <el-table-column prop="productName" label="产品类型" min-width="120" />
          <el-table-column prop="productModel" label="产品型号" min-width="120" />
          <el-table-column prop="groupName" label="所属分组" min-width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastOnlineTime" label="最后上线" min-width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.lastOnlineTime) }}
            </template>
          </el-table-column>
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
    </div>

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
          <el-select v-model="deviceForm.productId" placeholder="请选择产品类型" filterable style="width: 100%">
            <el-option v-for="product in productList" :key="product.id" :label="getProductLabel(product)" :value="product.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属分组" prop="groupId">
          <GroupSelector ref="groupSelectorRef" v-model="deviceForm.groupId" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, List, Search, Refresh } from '@element-plus/icons-vue'
import { getDeviceList, createDevice, updateDevice, deleteDevice as deleteDeviceAPI } from '@/api/device'
import { getGroupTree } from '@/api/group'
import { getProductList } from '@/api/product'
import GroupTree from '@/components/GroupTree.vue'
import GroupSelector from '@/components/GroupSelector.vue'
import { flattenTree } from '@/utils/tree'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加设备')
const isEditMode = ref(false)
const deviceFormRef = ref(null)
const groupSelectorRef = ref(null) // 分组选择器引用

// 搜索和筛选
const searchQuery = ref('')
const filterStatus = ref('')
const filterProduct = ref('')
const currentGroupId = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 设备列表
const deviceList = ref([])

// 产品列表
const productList = ref([])

// 分组列表
const groups = ref([])

// 总设备数
const totalDeviceCount = ref(0)

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

// 序号计算方法（从 1 开始）
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 日期格式化
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  // 如果已经是正确格式，直接返回
  if (typeof dateTime === 'string' && dateTime.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/)) {
    return dateTime
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

// 当前分组标题
const currentGroupTitle = computed(() => {
  if (!currentGroupId.value) {
    const count = deviceList.value.length
    return `全部设备 (${count}台)`
  }
  const group = groups.value.find(g => g.id === currentGroupId.value)
  if (group) {
    const count = deviceList.value.length
    return `${group.name} (${count}台)`
  }
  return '请选择分组'
})

// 加载设备列表
const loadDevices = async () => {
  try {
    loading.value = true
    const result = await getDeviceList({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value,
      status: filterStatus.value,
      groupId: currentGroupId.value,
      productId: filterProduct.value
    })
    console.log('设备列表API返回:', result)
    // 响应拦截器已经返回了data部分，所以result就是{total, list, ...}
    deviceList.value = result.list || []
    total.value = result.total || 0
    console.log('设备列表数据:', deviceList.value)
    
    // 更新总设备数（只在查询全部设备时更新）
    if (!currentGroupId.value) {
      totalDeviceCount.value = result.total || 0
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
    console.log('产品列表API返回:', result)
    // 响应拦截器已经返回了data部分，所以result就是{total, list, ...}
    productList.value = result.list || []
    console.log('产品列表数据:', productList.value)
  } catch (error) {
    console.error('加载产品列表失败:', error)
  }
}

// 加载分组列表
const loadGroups = async () => {
  try {
    const result = await getGroupTree()
    if (result.tree) {
      // 将树形数据转换为扁平化列表
      groups.value = flattenTree(result.tree)
      console.log('加载的分组数据（扁平化）:', groups.value)
    }
  } catch (error) {
    console.error('加载分组列表失败:', error)
  }
}


// 获取产品标签（产品名称+产品型号）
const getProductLabel = (product) => {
  console.log('getProductLabel调用，product:', product)
  if (!product) return ''
  const name = product.productName || product.name || ''
  const modelName = product.productModel || product.model || ''
  const label = modelName ? `${name} - ${modelName}` : name
  console.log('产品标签:', label)
  return label
}

// 选择全部设备
const selectAllDevices = () => {
  currentGroupId.value = null
  console.log('选中全部设备')
  currentPage.value = 1
  loadDevices()
}

// 选择分组
const selectGroup = (id) => {
  currentGroupId.value = id
  console.log('选中分组:', id)
  currentPage.value = 1
  loadDevices()
}

// 搜索处理
const handleSearch = () => {
  console.log('执行查询，搜索关键词:', searchQuery.value, '状态:', filterStatus.value, '产品:', filterProduct.value)
  currentPage.value = 1
  loadDevices()
}

// 刷新设备列表
const refreshDevices = () => {
  console.log('刷新设备列表')
  // 清空搜索条件
  searchQuery.value = ''
  filterStatus.value = ''
  filterProduct.value = ''
  currentPage.value = 1
  loadDevices()
  ElMessage.success('刷新成功')
}

// 打开添加对话框
const openAddDialog = async () => {
  dialogTitle.value = '添加设备'
  isEditMode.value = false
  dialogVisible.value = true
  // 刷新分组数据
  await new Promise(resolve => setTimeout(resolve, 100))
  if (groupSelectorRef.value) {
    await groupSelectorRef.value.loadGroups()
  }
}

// 查看设备详情
const viewDevice = (device) => {
  router.push({
    path: '/devices/detail',
    query: { deviceCode: device.deviceCode }
  })
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
      // axios拦截器已经返回了data部分，删除成功会返回删除结果
      await deleteDeviceAPI(device.deviceCode)
      ElMessage.success('设备删除成功')
      loadDevices()
    } catch (error) {
      console.error('删除设备失败:', error)
      ElMessage.error('设备删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 保存设备
const saveDevice = async () => {
  // 防止重复提交：如果正在保存，直接返回
  if (saving.value) {
    console.log('正在保存中，请勿重复点击')
    return
  }
  
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

      // 响应拦截器已经返回了data部分，所以result就是创建的设备对象
      ElMessage.success(isEditMode.value ? '设备更新成功' : '设备添加成功')
      dialogVisible.value = false
      loadDevices()
    } catch (error) {
      // 检查错误信息是否包含"设备编码已存在"
      if (error.message && error.message.includes('设备编码已存在')) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error(isEditMode.value ? '设备更新失败' : '设备添加失败')
      }
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
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding: 0 20px;
}

.device-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  padding: 0 20px;
  height: calc(100vh - 160px);
}

.tree-panel {
  background: white;
  border-radius: 12px;
  border: 0.5px solid rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.device-panel {
  background: white;
  border-radius: 12px;
  border: 0.5px solid rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

/* 全部设备按钮样式 */
.all-devices-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  margin-bottom: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #303133;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  user-select: none;
}

.all-devices-btn:hover {
  background: #e0f2fe;
  border-color: #93c5fd;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.all-devices-btn.active {
  background: linear-gradient(90deg, #3b82f6 0%, #60a5fa 100%);
  color: white;
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.all-devices-btn .btn-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.all-devices-btn .device-count {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.8);
  padding: 2px 8px;
  border-radius: 12px;
}

.all-devices-btn.active .device-count {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.2);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e5e7;
}

/* Element Plus 样式覆盖 */
:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e5e7;
  background: #fafafa;
  min-height: 64px;
  display: flex;
  align-items: center;
}

:deep(.el-card__body) {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
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

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-button--small) {
  font-size: 12px;
  padding: 4px 8px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .device-container {
    grid-template-columns: 250px 1fr;
  }
}

@media (max-width: 768px) {
  .device-container {
    grid-template-columns: 1fr;
    height: auto;
  }
  
  .tree-panel {
    height: auto;
    margin-bottom: 20px;
  }
  
  .device-panel {
    height: auto;
  }
}
</style>
