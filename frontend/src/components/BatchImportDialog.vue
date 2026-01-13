<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量导入设备"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="batch-import-content">
      <!-- 步骤1: 下载模板 -->
      <div class="step-card">
        <div class="step-title">
          <el-icon><Download /></el-icon>
          <span>步骤1: 下载模板</span>
        </div>
        <el-button type="primary" @click="downloadTemplate">
          <el-icon><Download /></el-icon>
          下载Excel模板
        </el-button>
        <div class="hint-text">
          💡 请按照模板格式填写设备信息<br>
          必填字段：设备编码、设备名称、产品型号、分组名称
        </div>
      </div>

      <!-- 步骤2: 上传文件 -->
      <div class="step-card">
        <div class="step-title">
          <el-icon><Upload /></el-icon>
          <span>步骤2: 上传Excel文件</span>
        </div>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls"
          :file-list="fileList"
        >
          <template #trigger>
            <el-button type="primary">选择文件</el-button>
          </template>
        </el-upload>
        <div v-if="uploadedFile" class="file-info">
          <span>{{ uploadedFile.name }}</span>
          <span class="file-size">({{ formatFileSize(uploadedFile.size) }})</span>
        </div>
        <div class="hint-text">
          💡 只能上传 .xlsx 或 .xls 文件，且不超过 10MB
        </div>
      </div>

      <!-- 步骤3: 预览数据 -->
      <div v-if="previewData.length > 0" class="step-card">
        <div class="step-title">
          <el-icon><View /></el-icon>
          <span>步骤3: 预览数据</span>
        </div>
        <div class="statistics">
          <el-tag type="success" class="statistics-tag">
            有效: {{ statistics.valid }}条
          </el-tag>
          <el-tag type="danger" class="statistics-tag">
            错误: {{ statistics.error }}条
          </el-tag>
          <el-tag type="warning" class="statistics-tag">
            重复: {{ statistics.duplicate }}条
          </el-tag>
        </div>
        <el-table
          :data="previewData"
          stripe
          border
          max-height="400"
          style="margin-top: 16px;"
        >
          <el-table-column prop="rowNum" label="行号" width="80" />
          <el-table-column prop="deviceCode" label="设备编码" min-width="120" />
          <el-table-column prop="deviceName" label="设备名称" min-width="120" />
          <el-table-column prop="productModel" label="产品型号" min-width="120" />
          <el-table-column prop="groupName" label="分组名称" min-width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'valid'" type="success" size="small">✅</el-tag>
              <el-tag v-else-if="row.status === 'error'" type="danger" size="small">❌</el-tag>
              <el-tag v-else-if="row.status === 'duplicate'" type="warning" size="small">⚠️</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="errors" label="错误信息" min-width="200">
            <template #default="{ row }">
              <span v-if="row.errors && row.errors.length > 0" style="color: #f56c6c;">
                {{ row.errors.join('; ') }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :disabled="statistics.valid === 0 || importing"
        :loading="importing"
        @click="handleImport"
      >
        确认导入({{ statistics.valid }}条)
      </el-button>
    </template>

    <!-- 导入结果对话框 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="导入结果"
      width="600px"
      :append-to-body="true"
    >
      <div class="result-content">
        <el-alert
          :type="importResult && importResult.failCount > 0 ? 'warning' : 'success'"
          :closable="false"
          style="margin-bottom: 16px;"
        >
          <template #title>
            <strong v-if="importResult && importResult.failCount === 0">✅ 导入完成！</strong>
            <strong v-else>⚠️ 导入完成，部分数据失败</strong>
          </template>
        </el-alert>

        <div class="result-statistics">
          <div class="stat-item">
            <span class="stat-label">成功:</span>
            <span class="stat-value success">{{ importResult?.successCount || 0 }} 条</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">失败:</span>
            <span class="stat-value error">{{ importResult?.failCount || 0 }} 条</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总计:</span>
            <span class="stat-value">{{ importResult?.totalCount || 0 }} 条</span>
          </div>
        </div>

        <div v-if="importResult && importResult.errors && importResult.errors.length > 0" class="error-list">
          <div class="error-title">失败详情：</div>
          <el-table :data="importResult.errors" border stripe max-height="200">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column prop="deviceCode" label="设备编码" min-width="120" />
            <el-table-column prop="error" label="错误信息" min-width="250" />
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="handleResultConfirm">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Upload, View } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { batchImportDevices } from '@/api/device'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  productList: {
    type: Array,
    default: () => []
  },
  groupList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'import-success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const uploadRef = ref(null)
const fileList = ref([])
const uploadedFile = ref(null)
const parsedData = ref([])
const previewData = ref([])
const statistics = ref({
  valid: 0,
  error: 0,
  duplicate: 0
})
const importing = ref(false)
const resultDialogVisible = ref(false)
const importResult = ref(null)

// 下载模板
const downloadTemplate = () => {
  try {
    const wb = XLSX.utils.book_new()
    const headers = [
      '设备编码',
      '设备名称',
      '产品型号',
      '分组名称'
    ]
    const exampleData = [
      ['DEV001', '设备1', 'TH-001', '总分组'],
      ['DEV002', '设备2', 'TH-001', '总分组']
    ]
    const wsData = [headers, ...exampleData]
    const ws = XLSX.utils.aoa_to_sheet(wsData)
    
    // 设置列宽
    ws['!cols'] = [
      { wch: 15 }, // 设备编码
      { wch: 15 }, // 设备名称
      { wch: 15 }, // 产品型号
      { wch: 15 }  // 分组名称
    ]
    
    XLSX.utils.book_append_sheet(wb, ws, '设备导入模板')
    XLSX.writeFile(wb, '设备导入模板.xlsx')
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 文件选择变化
const handleFileChange = async (file) => {
  uploadedFile.value = file.raw
  
  // 文件大小检查
  if (file.raw.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }
  
  // 文件格式检查
  const fileName = file.raw.name
  const fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
  if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
    ElMessage.error('请上传 .xlsx 或 .xls 格式的文件')
    return
  }
  
  try {
    // 解析Excel文件
    const data = await parseExcelFile(file.raw)
    parsedData.value = data
    
    // 校验数据
    const { results, stats } = validateData(data)
    previewData.value = results
    statistics.value = stats
    
    if (results.length === 0) {
      ElMessage.warning('Excel文件中没有有效数据')
    }
  } catch (error) {
    console.error('解析Excel文件失败:', error)
    ElMessage.error('Excel文件格式错误，请检查文件是否损坏')
  }
}

// 解析Excel文件
const parseExcelFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
        const jsonData = XLSX.utils.sheet_to_json(firstSheet, { header: 1 })
        resolve(jsonData)
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = reject
    reader.readAsArrayBuffer(file)
  })
}

// 数据校验
const validateData = (rawData) => {
  const results = []
  const deviceCodeSet = new Set()
  const stats = {
    valid: 0,
    error: 0,
    duplicate: 0
  }
  
  // 跳过表头行（第0行），从第1行开始处理
  for (let i = 1; i < rawData.length; i++) {
    const row = rawData[i]
    
    // 跳过空行
    if (!row || row.length === 0 || row.every(cell => !cell || cell.toString().trim() === '')) {
      continue
    }
    
    const rowNum = i + 1 // Excel行号（从1开始）
    
    const result = {
      rowNum,
      deviceCode: (row[0] || '').toString().trim(),
      deviceName: (row[1] || '').toString().trim(),
      productModel: (row[2] || '').toString().trim(),
      groupName: (row[3] || '').toString().trim(),
      status: 'valid',
      errors: []
    }
    
    // 必填字段校验
    if (!result.deviceCode) {
      result.errors.push('设备编码不能为空')
      result.status = 'error'
    }
    if (!result.deviceName) {
      result.errors.push('设备名称不能为空')
      result.status = 'error'
    }
    if (!result.productModel) {
      result.errors.push('产品型号不能为空')
      result.status = 'error'
    }
    if (!result.groupName) {
      result.errors.push('分组名称不能为空')
      result.status = 'error'
    }
    
    // 格式校验
    if (result.deviceCode && result.deviceCode.length > 50) {
      result.errors.push('设备编码长度不能超过50')
      result.status = 'error'
    }
    if (result.deviceName && result.deviceName.length > 100) {
      result.errors.push('设备名称长度不能超过100')
      result.status = 'error'
    }
    if (result.productModel && result.productModel.length > 100) {
      result.errors.push('产品型号长度不能超过100')
      result.status = 'error'
    }
    
    // 业务逻辑校验：产品型号是否存在
    if (result.productModel && result.status !== 'error') {
      const productExists = props.productList.some(
        p => p.productModel === result.productModel
      )
      if (!productExists) {
        result.errors.push(`产品型号不存在: ${result.productModel}`)
        result.status = 'error'
      }
    }
    
    // 业务逻辑校验：分组名称是否存在，并转换为分组ID
    if (result.groupName && result.status !== 'error') {
      const group = props.groupList.find(
        g => g.name === result.groupName
      )
      if (!group) {
        result.errors.push(`分组名称不存在: ${result.groupName}`)
        result.status = 'error'
      } else {
        // 将分组名称转换为分组ID
        result.groupId = group.id
      }
    }
    
    // 检查Excel内重复
    if (result.deviceCode && result.status !== 'error') {
      if (deviceCodeSet.has(result.deviceCode)) {
        result.errors.push('设备编码在Excel内重复')
        result.status = 'duplicate'
      } else {
        deviceCodeSet.add(result.deviceCode)
      }
    }
    
    // 统计
    if (result.status === 'valid') {
      stats.valid++
    } else if (result.status === 'error') {
      stats.error++
    } else if (result.status === 'duplicate') {
      stats.duplicate++
    }
    
    results.push(result)
  }
  
  return { results, stats }
}

// 批量导入
const handleImport = async () => {
  if (statistics.value.valid === 0) {
    ElMessage.warning('没有可导入的有效数据')
    return
  }
  
  try {
    importing.value = true
    
    // 过滤出有效数据
    const validData = previewData.value.filter(item => item.status === 'valid')
    
    // 转换数据格式
    const devices = validData.map(item => ({
      deviceCode: item.deviceCode,
      deviceName: item.deviceName,
      productModel: item.productModel,
      groupId: item.groupId // 已经在校验时转换为ID了
    }))
    
    // 调用批量导入接口
    const result = await batchImportDevices({ devices })
    importResult.value = result
    resultDialogVisible.value = true
    
  } catch (error) {
    console.error('批量导入失败:', error)
    ElMessage.error('批量导入失败: ' + (error.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

// 确认导入结果
const handleResultConfirm = () => {
  resultDialogVisible.value = false
  emit('import-success', importResult.value)
  handleClose()
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  // 重置状态
  fileList.value = []
  uploadedFile.value = null
  parsedData.value = []
  previewData.value = []
  statistics.value = {
    valid: 0,
    error: 0,
    duplicate: 0
  }
  importResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 监听对话框关闭，重置状态
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    handleClose()
  }
})
</script>

<style scoped>
.batch-import-content {
  padding: 8px 0;
}

.step-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.step-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hint-text {
  font-size: 12px;
  color: #909399;
  margin-top: 12px;
  line-height: 1.6;
}

.file-info {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
}

.file-size {
  color: #909399;
  margin-left: 8px;
}

.statistics {
  margin-bottom: 16px;
}

.statistics-tag {
  margin-right: 8px;
  font-weight: 500;
}

.result-content {
  padding: 8px 0;
}

.result-statistics {
  margin: 16px 0;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-label {
  font-weight: 500;
  color: #606266;
  margin-right: 12px;
  min-width: 60px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.error {
  color: #f56c6c;
}

.error-list {
  margin-top: 16px;
}

.error-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
</style>
