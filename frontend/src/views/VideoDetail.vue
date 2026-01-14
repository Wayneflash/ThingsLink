<template>
  <div class="video-detail-page">
    <div class="page-header">
      <el-button @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h1 class="page-title">视频设备详情</h1>
    </div>
    
    <!-- 设备基本信息 -->
    <el-card class="info-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>基本信息</span>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="视频名称">
          {{ deviceInfo.name || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="所属分组">
          {{ deviceInfo.groupName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="视频编码">
          {{ deviceInfo.deviceId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="通道编码">
          {{ deviceInfo.channelId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatTime(deviceInfo.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatTime(deviceInfo.updateTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注说明" :span="2">
          {{ deviceInfo.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 设备状态信息 -->
    <el-card class="status-card" shadow="hover" v-loading="statusLoading">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>设备状态</span>
          <el-button 
            type="primary" 
            size="small" 
            @click="refreshStatus"
            :loading="statusLoading"
            style="margin-left: auto;"
          >
            <el-icon><Refresh /></el-icon>刷新状态
          </el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-if="deviceStatus">
        <el-descriptions-item label="在线状态">
          <el-tag :type="deviceStatus.onLine ? 'success' : 'danger'">
            {{ deviceStatus.onLine ? '在线' : '离线' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备IP">
          {{ deviceStatus.ip || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="设备端口">
          {{ deviceStatus.port || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="传输协议">
          {{ deviceStatus.transport || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatTime(deviceStatus.registerTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="心跳时间">
          {{ formatTime(deviceStatus.keepaliveTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="通道数量">
          {{ deviceStatus.channelCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="制造商">
          {{ deviceStatus.manufacturer || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="无法获取设备状态" />
    </el-card>
    
    <!-- 实时视频播放 -->
    <el-card class="video-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><VideoPlay /></el-icon>
          <span>实时视频</span>
          <el-button 
            v-if="!playing"
            type="success" 
            size="small" 
            @click="startPlay"
            :loading="playLoading"
            :disabled="!deviceStatus || !deviceStatus.onLine"
            style="margin-left: auto;"
          >
            <el-icon><VideoPlay /></el-icon>开始播放
          </el-button>
          <el-button 
            v-else
            type="danger" 
            size="small" 
            @click="stopPlay"
            style="margin-left: auto;"
          >
            <el-icon><VideoPause /></el-icon>停止播放
          </el-button>
        </div>
      </template>
      
      <VideoPlayer 
        v-if="playing && videoUrl"
        :video-url="videoUrl"
        @error="handlePlayError"
      />
      <el-empty v-else-if="!playing" description="点击【开始播放】查看实时视频" />
      <el-empty v-else description="正在加载视频..." />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, InfoFilled, Monitor, Refresh, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { getVideoDetail, playVideo } from '../api/video'
import VideoPlayer from '../components/VideoPlayer.vue'

const router = useRouter()
const route = useRoute()

// 设备ID（从路由参数获取）
const deviceId = route.params.id

// 设备信息
const deviceInfo = ref({})
const loading = ref(false)

// 设备状态
const deviceStatus = ref(null)
const statusLoading = ref(false)

// 视频播放
const playing = ref(false)
const playLoading = ref(false)
const videoUrl = ref('')

// 返回
const goBack = () => {
  router.back()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '-'
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 加载设备详情和状态
const loadDeviceDetail = async () => {
  loading.value = true
  try {
    const data = await getVideoDetail(deviceId)
    deviceInfo.value = data
    deviceStatus.value = data.status || null
  } catch (error) {
    ElMessage.error('获取设备详情失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 刷新设备状态
const refreshStatus = async () => {
  statusLoading.value = true
  try {
    const data = await getVideoDetail(deviceId)
    deviceStatus.value = data.status || null
    ElMessage.success('状态刷新成功')
  } catch (error) {
    ElMessage.error('刷新状态失败: ' + error.message)
  } finally {
    statusLoading.value = false
  }
}

// 开始播放
const startPlay = async () => {
  if (!deviceInfo.value.channelId) {
    ElMessage.error('通道编码不存在')
    return
  }
  
  playLoading.value = true
  try {
    const data = await playVideo({
      deviceId: deviceInfo.value.deviceId,
      channelId: deviceInfo.value.channelId
    })
    
    videoUrl.value = data.hlsUrl
    playing.value = true
    ElMessage.success('视频流获取成功')
  } catch (error) {
    ElMessage.error('获取视频流失败: ' + error.message)
  } finally {
    playLoading.value = false
  }
}

// 停止播放
const stopPlay = () => {
  playing.value = false
  videoUrl.value = ''
}

// 播放错误处理
const handlePlayError = (error) => {
  ElMessage.error('视频播放失败: ' + error)
  stopPlay()
}

// 初始化
onMounted(() => {
  if (!deviceId) {
    ElMessage.error('设备ID不存在')
    goBack()
    return
  }
  
  loadDeviceDetail()
})

// 清理
onUnmounted(() => {
  stopPlay()
})
</script>

<style scoped>
.video-detail-page {
  padding: 20px;
  min-height: 100vh;
  background: #f5f5f7;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn {
  border-radius: 8px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.info-card,
.status-card,
.video-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  background-color: #fafafa;
}

:deep(.el-descriptions__content) {
  color: #303133;
}
</style>
