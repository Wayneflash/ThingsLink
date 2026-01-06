<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between;">
          <span>设备管理</span>
          <el-button type="primary" @click="showCreateDialog">创建设备</el-button>
        </div>
      </template>
      
      <el-table :data="devices" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceCode" label="设备编码" />
        <el-table-column prop="deviceName" label="设备名称" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="lastOnlineTime" label="最后在线时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="viewData(row)">查看数据</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建设备对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建设备" width="500px">
      <el-form :model="deviceForm" label-width="100px">
        <el-form-item label="设备编码">
          <el-input v-model="deviceForm.deviceCode" placeholder="如: TEM1111" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="deviceForm.deviceName" />
        </el-form-item>
        <el-form-item label="产品">
          <el-select v-model="deviceForm.productId">
            <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="deviceForm.location" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createDevice">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { deviceApi, productApi } from '@/api'

const devices = ref([])
const products = ref([])
const createDialogVisible = ref(false)
const deviceForm = ref({
  deviceCode: '',
  deviceName: '',
  productId: null,
  location: ''
})

const loadDevices = async () => {
  try {
    const res = await deviceApi.list()
    devices.value = res.data
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  }
}

const loadProducts = async () => {
  try {
    const res = await productApi.list()
    products.value = res.data
  } catch (error) {
    ElMessage.error('加载产品列表失败')
  }
}

const showCreateDialog = () => {
  deviceForm.value = { deviceCode: '', deviceName: '', productId: null, location: '' }
  createDialogVisible.value = true
}

const createDevice = async () => {
  try {
    await deviceApi.create(deviceForm.value)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadDevices()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const viewData = (device) => {
  ElMessage.info(`查看设备 ${device.deviceName} 的数据`)
}

onMounted(() => {
  loadDevices()
  loadProducts()
})
</script>
