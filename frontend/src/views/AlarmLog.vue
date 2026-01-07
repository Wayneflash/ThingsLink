<template>
  <div class="alarm-log-page">
    <!-- 顶部统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-label">总报警数</div>
        <div class="stat-value">{{ statistics.total || 0 }}</div>
      </div>
      <div class="stat-card pending">
        <div class="stat-label">未处理</div>
        <div class="stat-value">{{ statistics.unhandled || 0 }}</div>
      </div>
      <div class="stat-card critical">
        <div class="stat-label">严重报警</div>
        <div class="stat-value">{{ statistics.critical || 0 }}</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-label">警告</div>
        <div class="stat-value">{{ statistics.warning || 0 }}</div>
      </div>
      <div class="stat-card info">
        <div class="stat-label">提示</div>
        <div class="stat-value">{{ statistics.info || 0 }}</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <div class="filter-bar">
        <el-input
          v-model="filters.deviceCode"
          placeholder="搜索设备编码/设备名称"
          class="search-input"
          clearable
          style="width: 250px;"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select 
          v-model="filters.alarmLevel" 
          placeholder="报警级别" 
          clearable 
          style="width: 150px;"
        >
          <el-option label="严重" value="critical" />
          <el-option label="警告" value="warning" />
          <el-option label="提示" value="info" />
        </el-select>

        <el-select 
          v-model="filters.status" 
          placeholder="处理状态" 
          clearable 
          style="width: 150px;"
        >
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>

        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 380px;"
        />

        <el-button type="primary" @click="loadAlarmLogs">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="resetFilters">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>

    <!-- 报警列表 -->
    <el-card class="table-card">
      <el-table 
        :data="alarmList" 
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="deviceCode" label="设备编码" width="140" />
        <el-table-column label="报警级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.alarmLevel)" effect="dark">
              {{ getLevelText(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmMessage" label="报警内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="triggerTime" label="触发时间" width="170" />
        <el-table-column label="处理状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理人" width="110">
          <template #default="{ row }">
            <span v-if="row.status === 1 && row.handler">{{ row.handler }}</span>
            <span v-else-if="row.status === 0 && getHandlerNames(row)" class="handler-names">
              {{ getHandlerNames(row) }}
            </span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column label="处理详情" width="90" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 1 && (row.handleDescription || row.handleImages)"
              size="small" 
              type="info"
              link
              @click="viewHandleDetail(row)"
            >
              查看
            </el-button>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="handleTime" label="处理时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.status === 0 && !isHandler(row)"
              content="只有配置的处理人才能操作"
              placement="top"
            >
              <span>
                <el-button 
                  size="small" 
                  type="primary"
                  disabled
                >
                  处理
                </el-button>
              </span>
            </el-tooltip>
            <el-button 
              v-else-if="row.status === 0"
              size="small" 
              type="primary"
              @click="handleAlarmClick(row)"
            >
              处理
            </el-button>
            <span v-else style="color: #909399;">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAlarmLogs"
          @current-change="loadAlarmLogs"
        />
      </div>
    </el-card>

    <!-- 处理报警对话框 -->
    <el-dialog
      v-model="handleDialogVisible"
      title="处理报警"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="处理描述">
          <el-input
            v-model="handleForm.description"
            type="textarea"
            :rows="4"
            placeholder="请描述处理方式和结果"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            v-model:file-list="handleForm.images"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png格式，最多5张</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handleLoading">确认处理</el-button>
      </template>
    </el-dialog>

    <!-- 查看处理详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="处理详情"
      width="600px"
    >
      <div class="detail-content">
        <div class="detail-item">
          <div class="detail-label">处理人：</div>
          <div class="detail-value">{{ detailData.handler || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">处理时间：</div>
          <div class="detail-value">{{ detailData.handleTime || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">处理描述：</div>
          <div class="detail-value">{{ detailData.handleDescription || '未填写' }}</div>
        </div>
        <div v-if="detailData.handleImages && detailData.handleImages.length > 0" class="detail-item">
          <div class="detail-label">处理图片：</div>
          <div class="detail-images">
            <el-image
              v-for="(img, index) in detailData.handleImages"
              :key="index"
              :src="img"
              :preview-src-list="detailData.handleImages"
              :initial-index="index"
              fit="cover"
              class="detail-image"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getAlarmLogList, handleAlarm, getAlarmStatistics } from '@/api/alarm'
import { getUserList } from '@/api/user'

// 统计数据
const statistics = ref({
  total: 0,
  unhandled: 0,
  critical: 0,
  warning: 0,
  info: 0
})

// 处理对话框
const handleDialogVisible = ref(false)
const handleLoading = ref(false)
const currentAlarmId = ref(null)
const handleForm = ref({
  description: '',
  images: []
})

// 查看详情对话框
const detailDialogVisible = ref(false)
const detailData = ref({
  handler: '',
  handleTime: '',
  handleDescription: '',
  handleImages: []
})

// 筛选条件
const filters = ref({
  deviceCode: '',
  alarmLevel: '',
  status: null
})

// 日期范围
const dateRange = ref([])

// 分页参数
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0
})

// 告警列表
const alarmList = ref([])
const loading = ref(false)

// 用户列表（用于显示处理人名字）
const users = ref([])

// 加载用户列表
const loadUsers = async () => {
  try {
    const res = await getUserList()
    if (res && res.list) {
      users.value = res.list
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getAlarmStatistics()
    if (res) {
      statistics.value = res
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 统计数据加载失败不影响页面展示
  }
}

// 加载报警日志
const loadAlarmLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      deviceCode: filters.value.deviceCode || undefined,
      alarmLevel: filters.value.alarmLevel || undefined,
      status: filters.value.status !== null ? filters.value.status : undefined,
      startTime: dateRange.value && dateRange.value[0] ? dateRange.value[0] : undefined,
      endTime: dateRange.value && dateRange.value[1] ? dateRange.value[1] : undefined
    }
    
    const res = await getAlarmLogList(params)
    if (res) {
      alarmList.value = res.list || []
      pagination.value.total = res.total || 0
    } else {
      alarmList.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('加载报警日志失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '加载报警日志失败'
    ElMessage.error(errorMsg)
    alarmList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 处理报警
const handleAlarmClick = async (row) => {
  currentAlarmId.value = row.id
  handleForm.value = {
    description: '',
    images: []
  }
  handleDialogVisible.value = true
}

// 提交处理
const submitHandle = async () => {
  try {
    handleLoading.value = true
    
    // 准备图片数据：转换为Base64或URL列表
    const imageUrls = []
    for (const file of handleForm.value.images) {
      if (file.raw) {
        // 将图片转为Base64
        const base64 = await fileToBase64(file.raw)
        imageUrls.push(base64)
      } else if (file.url) {
        imageUrls.push(file.url)
      }
    }
    
    const res = await handleAlarm({
      alarmId: currentAlarmId.value,
      handleDescription: handleForm.value.description,
      handleImages: imageUrls
    })
    
    if (res !== undefined) {
      ElMessage.success('处理成功')
      handleDialogVisible.value = false
      await loadAlarmLogs()
      await loadStatistics()
    }
  } catch (error) {
    console.error('处理报警失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '处理失败'
    ElMessage.error(errorMsg)
  } finally {
    handleLoading.value = false
  }
}

// 文件转 Base64
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

// 获取处理人名字（未处理报警显示配置的处理人）
const getHandlerNames = (row) => {
  if (row.status !== 0 || !row.notifyUsers) return ''
  
  try {
    const notifyUserIds = JSON.parse(row.notifyUsers)
    const names = notifyUserIds.map(id => {
      const user = users.value.find(u => u.id === id)
      return user ? (user.realName || user.username) : ''
    }).filter(name => name)
    
    return names.join('、') || '-'
  } catch (error) {
    console.error('解析处理人失败:', error)
    return '-'
  }
}

// 判断当前用户是否为处理人
const isHandler = (row) => {
  if (row.status !== 0 || !row.notifyUsers) return false
  
  try {
    const currentUserId = JSON.parse(localStorage.getItem('userInfo'))?.id
    if (!currentUserId) return false
    
    const notifyUserIds = JSON.parse(row.notifyUsers)
    return notifyUserIds.includes(currentUserId)
  } catch (error) {
    console.error('判断处理人失败:', error)
    return false
  }
}

// 查看处理详情
const viewHandleDetail = (row) => {
  detailData.value = {
    handler: row.handler || '-',
    handleTime: row.handleTime || '-',
    handleDescription: row.handleDescription || '',
    handleImages: []
  }
  
  // 解析图片数据
  if (row.handleImages) {
    try {
      const images = JSON.parse(row.handleImages)
      detailData.value.handleImages = Array.isArray(images) ? images : []
    } catch (error) {
      console.error('解析处理图片失败:', error)
    }
  }
  
  detailDialogVisible.value = true
}

// 重置筛选条件
const resetFilters = () => {
  filters.value = {
    deviceCode: '',
    alarmLevel: '',
    status: null
  }
  dateRange.value = []
  pagination.value.page = 1
  loadAlarmLogs()
}

// 获取报警级别对应的Tag类型
const getLevelType = (level) => {
  const types = {
    critical: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return types[level] || 'info'
}

// 获取报警级别文本
const getLevelText = (level) => {
  const texts = {
    critical: '严重',
    warning: '警告',
    info: '提示'
  }
  return texts[level] || '未知'
}

// 页面加载时
onMounted(() => {
  loadUsers()
  loadStatistics()
  loadAlarmLogs()
})
</script>

<style scoped>
.alarm-log-page {
  padding: 24px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.pending {
  border-left: 4px solid #ff9500;
}

.stat-card.critical {
  border-left: 4px solid #ff3b30;
}

.stat-card.warning {
  border-left: 4px solid #ff9500;
}

.stat-card.info {
  border-left: 4px solid #007aff;
}

.stat-label {
  font-size: 14px;
  color: #6e6e73;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
}

/* 筛选栏 */
.filter-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 表格卡片 */
.table-card {
  overflow: visible;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 处理人名字 */
.handler-names {
  color: #409eff;
  font-size: 13px;
}

.empty-text {
  color: #909399;
  font-size: 13px;
}

/* 处理详情对话框 */
.detail-content {
  padding: 10px 0;
}

.detail-item {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-weight: 600;
  color: #303133;
  min-width: 90px;
  flex-shrink: 0;
}

.detail-value {
  color: #606266;
  flex: 1;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  flex: 1;
}

.detail-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
}

.detail-image:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}
</style>
