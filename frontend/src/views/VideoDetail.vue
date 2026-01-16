<template>
  <div class="video-detail-page">
    <div class="page-header">
      <el-button @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h1 class="page-title">视频设备详情</h1>
    </div>
    
    <div class="content-layout">
      <!-- 左侧：设备状态信息 -->
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
        
        <el-descriptions :column="1" border v-if="deviceStatus" class="status-descriptions">
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
        <el-empty v-else :description="statusError || '无法获取设备状态'" />
      </el-card>
      
      <!-- 右侧：实时视频播放 -->
      <el-card class="video-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><VideoPlay /></el-icon>
            <span>实时视频</span>
            <el-button 
              v-if="playing"
              type="danger" 
              size="small" 
              @click="stopPlay"
              style="margin-left: auto;"
            >
              <el-icon><VideoPause /></el-icon>停止播放
            </el-button>
          </div>
        </template>
        
        <div class="video-container">
          <VideoPlayer 
            v-if="playing && videoUrl"
            :video-url="videoUrl"
            @error="handlePlayError"
            class="video-player"
          />
          <div v-else-if="playLoading" class="video-placeholder loading">
            <div class="loading-spinner">
              <div class="spinner-ring"></div>
              <div class="spinner-ring"></div>
              <div class="spinner-ring"></div>
            </div>
            <div class="loading-text">正在加载视频流...</div>
          </div>
          <div v-else class="video-placeholder">
            <div class="play-button-wrapper" @click="startPlay">
              <el-icon class="play-icon"><VideoPlay /></el-icon>
            </div>
            <div class="play-hint">点击播放按钮开始播放</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Monitor, Refresh, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { getVideoDetail, playVideo } from '../api/video'
import VideoPlayer from '../components/VideoPlayer.vue'

const router = useRouter()
const route = useRoute()

// 设备编码和通道编码（从路由参数获取）
const deviceId = route.params.deviceId
const channelId = route.params.channelId

// 设备信息
const deviceInfo = ref({})
const loading = ref(false)

// 设备状态
const deviceStatus = ref(null)
const statusLoading = ref(false)
const statusError = ref('')

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
  statusError.value = ''
  try {
    const data = await getVideoDetail(deviceId, channelId)
    deviceInfo.value = data
    deviceStatus.value = data.status || null
    statusError.value = data.statusError || ''
  } catch (error) {
    ElMessage.error('获取设备详情失败: ' + error.message)
    statusError.value = '获取设备详情失败: ' + error.message
  } finally {
    loading.value = false
  }
  return Promise.resolve()
}

// 刷新设备状态
const refreshStatus = async () => {
  statusLoading.value = true
  statusError.value = ''
  try {
    const data = await getVideoDetail(deviceId, channelId)
    deviceStatus.value = data.status || null
    statusError.value = data.statusError || ''
    if (deviceStatus.value) {
      ElMessage.success('状态刷新成功')
    } else {
      ElMessage.warning(statusError.value || '无法获取设备状态')
    }
  } catch (error) {
    ElMessage.error('刷新状态失败: ' + error.message)
    statusError.value = '刷新状态失败: ' + error.message
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
  if (!deviceId || !channelId) {
    ElMessage.error('设备编码或通道编码不存在')
    goBack()
    return
  }
  
  // 先加载设备基本信息
  loadDeviceDetail().then(() => {
    // 然后自动刷新一次设备状态
    refreshStatus()
  })
})

// 清理
onUnmounted(() => {
  stopPlay()
})
</script>

<style scoped>
.video-detail-page {
  padding: 8px;
  height: calc(100vh - 40px);
  overflow: hidden;
  background: #f5f5f7;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-shrink: 0;
  height: 32px;
}

.back-btn {
  border-radius: 6px;
  padding: 8px 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.content-layout {
  display: flex;
  gap: 10px;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.status-card {
  width: 340px;
  flex-shrink: 0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-card :deep(.el-card__header) {
  padding: 10px 14px;
}

.status-card :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.status-descriptions {
  margin-top: 0;
}

.status-descriptions :deep(.el-descriptions__table) {
  font-size: 13px;
}

.status-descriptions :deep(.el-descriptions__label) {
  padding: 6px 10px;
  width: 90px;
}

.status-descriptions :deep(.el-descriptions__content) {
  padding: 6px 10px;
}

.status-descriptions :deep(.el-descriptions__table .el-descriptions__cell) {
  padding-bottom: 0;
}

.video-card {
  flex: 1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.video-card :deep(.el-card__header) {
  padding: 10px 14px;
}

.video-card :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.video-container {
  flex: 1;
  position: relative;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.play-button-wrapper {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.play-button-wrapper:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.play-icon {
  font-size: 50px;
  color: #fff;
  margin-left: 6px;
}

.play-hint {
  margin-top: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.loading {
  color: #fff;
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
  margin-bottom: 16px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top-color: #007aff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: 0s;
  opacity: 1;
}

.spinner-ring:nth-child(2) {
  animation-delay: 0.3s;
  opacity: 0.7;
  transform: scale(0.8);
}

.spinner-ring:nth-child(3) {
  animation-delay: 0.6s;
  opacity: 0.4;
  transform: scale(0.6);
}

.loading-text {
  margin-top: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  background-color: #fafafa;
  font-size: 13px;
}

:deep(.el-descriptions__content) {
  color: #303133;
  font-size: 13px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
}
</style>
