<!-- 设备管理示例组件 -->
<template>
  <div class="p-6">
    <el-card class="mb-4">
      <template #header>
        <div class="flex justify-between items-center">
          <span class="text-lg font-semibold">设备列表</span>
          <el-button type="primary" @click="handleCreate">
            新增设备
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="mb-4 flex gap-4">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索设备名称或编码"
          clearable
          class="w-64"
          @keyup.enter="loadDevices"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="设备状态"
          clearable
          class="w-40"
        >
          <el-option label="在线" value="online" />
          <el-option label="离线" value="offline" />
        </el-select>
        <el-button type="primary" @click="loadDevices">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <!-- 设备表格 -->
      <el-table :data="devices" v-loading="loading" border>
        <el-table-column prop="deviceCode" label="设备编码" width="150" />
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="productName" label="所属产品" width="150" />
        <el-table-column prop="groupPath" label="设备分组" width="200" />
        <el-table-column label="在线状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastOnlineTime" label="最后在线时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadDevices"
          @size-change="loadDevices"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getDeviceList, 
  createDevice, 
  deleteDevice, 
  getDeviceDetail 
} from '@/api/device'

// 设备列表数据
const devices = ref([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  productId: null,
  groupId: null
})

// 分页信息
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 加载设备列表
const loadDevices = async () => {
  loading.value = true
  try {
    const data = await getDeviceList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    
    // 注意：响应拦截器已配置为直接返回 data 部分
    devices.value = data.list
    pagination.total = data.total
    
  } catch (error) {
    // 错误已被拦截器统一处理，显示了错误消息
    console.error('加载设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.productId = null
  searchForm.groupId = null
  pagination.page = 1
  loadDevices()
}

// 新增设备
const handleCreate = () => {
  // TODO: 打开新增对话框
  ElMessage.info('打开新增设备对话框')
}

// 查看详情
const handleDetail = async (row) => {
  try {
    const data = await getDeviceDetail(row.deviceCode)
    console.log('设备详情:', data)
    // TODO: 打开详情对话框或跳转详情页
    ElMessage.success('获取设备详情成功')
  } catch (error) {
    console.error('获取设备详情失败:', error)
  }
}

// 编辑设备
const handleEdit = (row) => {
  // TODO: 打开编辑对话框
  ElMessage.info(`编辑设备: ${row.deviceName}`)
}

// 删除设备
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除设备"${row.deviceName}"吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteDevice(row.deviceCode)
      ElMessage.success('删除成功')
      // 重新加载列表
      loadDevices()
    } catch (error) {
      // 错误已被拦截器处理
      console.error('删除设备失败:', error)
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
/* 使用 Tailwind CSS 的原子类，无需额外样式 */
</style>
