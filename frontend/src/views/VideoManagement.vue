<template>
  <div class="video-management-page">
    <h1 class="page-title">视频管理</h1>
    
    <!-- 视频设备列表 -->
    <el-card class="video-panel" shadow="hover">
      <template #header>
        <div class="panel-header">
          <h3 class="panel-title">全部视频设备</h3>
          <el-input
            v-model="searchQuery"
            placeholder="🔍 搜索视频名称或编码"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <GroupSelector
            v-model="filterGroup"
            placeholder="所属分组"
            clearable
            class="filter-select"
          />
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="refreshList">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>添加视频设备
          </el-button>
        </div>
      </template>
      
      <el-table 
        :data="videoList" 
        stripe 
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
        <el-table-column prop="name" label="视频名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="deviceId" label="视频编码" min-width="200" show-overflow-tooltip />
        <el-table-column prop="channelId" label="通道编码" min-width="200" show-overflow-tooltip />
        <el-table-column prop="groupName" label="所属分组" min-width="120" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row)">
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
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="视频名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入视频设备名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="视频编码" prop="deviceId">
          <el-input 
            v-model="form.deviceId" 
            placeholder="请输入GB28181设备编码（数字）" 
            maxlength="50"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="通道编码" prop="channelId">
          <el-input 
            v-model="form.channelId" 
            placeholder="请输入GB28181通道编码（数字）" 
            maxlength="50"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="所属分组" prop="groupId">
          <GroupSelector
            v-model="form.groupId"
            placeholder="请选择分组"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getVideoList, addVideoDevice, updateVideoDevice, deleteVideoDevice } from '../api/video'
import GroupSelector from '../components/GroupSelector.vue'

const router = useRouter()

// 列表数据
const videoList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 筛选条件
const searchQuery = ref('')
const filterGroup = ref(null)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加视频设备')
const dialogMode = ref('add') // add | edit
const saving = ref(false)
const formRef = ref(null)

// 表单数据
const form = reactive({
  id: null,
  name: '',
  deviceId: '',
  channelId: '',
  groupId: null,
  remark: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在1到50个字符', trigger: 'blur' }
  ],
  deviceId: [
    { required: true, message: '请输入设备编码', trigger: 'blur' },
    { pattern: /^\d+$/, message: '设备编码必须为数字', trigger: 'blur' }
  ],
  channelId: [
    { required: true, message: '请输入通道编码', trigger: 'blur' },
    { pattern: /^\d+$/, message: '通道编码必须为数字', trigger: 'blur' }
  ],
  groupId: [
    { required: true, message: '请选择所属分组', trigger: 'change' }
  ]
}

// 序号计算
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 查询列表
const loadVideoList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      search: searchQuery.value || undefined,
      groupId: filterGroup.value || undefined
    }
    
    const data = await getVideoList(params)
    videoList.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error('查询失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadVideoList()
}

// 刷新
const refreshList = () => {
  loadVideoList()
}

// 分页变化
const handlePageChange = () => {
  loadVideoList()
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/video/detail/${row.deviceId}/${row.channelId}`)
}

// 打开添加对话框
const openAddDialog = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '添加视频设备'
  resetForm()
  dialogVisible.value = true
}

// 编辑设备
const editDevice = (row) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑视频设备'
  form.id = row.id
  form.name = row.name
  form.deviceId = row.deviceId
  form.channelId = row.channelId
  form.groupId = row.groupId
  form.remark = row.remark || ''
  dialogVisible.value = true
}

// 保存
const handleSave = async () => {
  await formRef.value.validate()
  
  saving.value = true
  try {
    if (dialogMode.value === 'add') {
      await addVideoDevice(form)
      ElMessage.success('添加成功')
    } else {
      await updateVideoDevice(form)
      ElMessage.success('修改成功')
    }
    
    dialogVisible.value = false
    loadVideoList()
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 删除设备
const deleteDevice = async (row) => {
  await ElMessageBox.confirm(
    `确定删除视频设备"${row.name}"吗？`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  )
  
  try {
    await deleteVideoDevice(row.id)
    ElMessage.success('删除成功')
    loadVideoList()
  } catch (error) {
    ElMessage.error('删除失败: ' + error.message)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.deviceId = ''
  form.channelId = ''
  form.groupId = null
  form.remark = ''
  formRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  loadVideoList()
})
</script>

<style scoped>
.video-management-page {
  padding: 20px;
  min-height: 100vh;
  background: #f5f5f7;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #1d1d1f;
}

.video-panel {
  border-radius: 12px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 按钮组 - 舒适样式 - 复用设备管理页面样式 */
.panel-header .el-button {
  flex-shrink: 0;
  white-space: nowrap;
  padding: 6px 12px;
  font-size: 13px;
  height: 32px;
  min-width: auto;
  line-height: 1.4;
}

.panel-header .el-button .el-icon {
  font-size: 14px;
  margin-right: 4px;
  vertical-align: middle;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-right: auto;
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 160px;
}

/* 输入框样式优化 - 舒适型 - 复用设备管理页面样式 */
:deep(.panel-header .el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.2s ease;
  padding: 0 10px;
  height: 32px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.panel-header .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(0, 122, 255, 0.3) inset;
}

:deep(.panel-header .el-input__inner) {
  height: 32px;
  line-height: 32px;
  font-size: 13px;
}

/* 下拉框样式优化 - 舒适型 - 复用设备管理页面样式 */
:deep(.panel-header .el-select .el-input__wrapper) {
  transition: all 0.2s ease;
  padding: 0 10px;
  height: 32px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.panel-header .el-select:hover .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(0, 122, 255, 0.3) inset;
}

:deep(.panel-header .el-select .el-input__inner) {
  height: 32px;
  line-height: 32px;
  font-size: 13px;
}

:deep(.panel-header .el-input__suffix) {
  height: 32px;
  display: flex;
  align-items: center;
}

:deep(.panel-header .el-select__caret) {
  font-size: 13px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

/* 按钮样式优化 - 复用设备管理页面样式 */
:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.2s ease;
  font-weight: 500;
}

:deep(.el-button--primary) {
  box-shadow: 0 2px 4px rgba(0, 122, 255, 0.15);
}

:deep(.el-button--primary:hover) {
  box-shadow: 0 4px 8px rgba(0, 122, 255, 0.25);
  transform: translateY(-1px);
}

:deep(.el-button--small) {
  font-size: 12px;
  padding: 4px 8px;
}

/* 操作列按钮样式 - link类型按钮也添加阴影效果 */
:deep(.el-button--link.el-button--primary) {
  box-shadow: 0 1px 2px rgba(0, 122, 255, 0.1);
}

:deep(.el-button--link.el-button--primary:hover) {
  box-shadow: 0 2px 4px rgba(0, 122, 255, 0.2);
  transform: translateY(-1px);
}

:deep(.el-button--link.el-button--danger) {
  box-shadow: 0 1px 2px rgba(245, 108, 108, 0.1);
}

:deep(.el-button--link.el-button--danger:hover) {
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.2);
  transform: translateY(-1px);
}
</style>
