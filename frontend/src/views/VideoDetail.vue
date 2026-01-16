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
      
      <!-- 右侧：视频播放区域（标签页） -->
      <el-card class="video-card" shadow="hover">
        <el-tabs v-model="activeTab" class="video-tabs" @tab-change="handleTabChange">
          <!-- 实时视频标签页 -->
          <el-tab-pane label="实时视频" name="realtime">
            <template #label>
              <span class="tab-label">
                <el-icon><VideoPlay /></el-icon>
                <span>实时视频</span>
              </span>
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
            <div class="video-controls-bar">
              <el-button 
                v-if="playing"
                type="danger" 
                size="small" 
                @click="stopPlay"
              >
                <el-icon><VideoPause /></el-icon>停止播放
              </el-button>
            </div>
          </el-tab-pane>
          
          <!-- 历史录像标签页 -->
          <el-tab-pane label="历史录像" name="record">
            <template #label>
              <span class="tab-label">
                <el-icon><VideoCamera /></el-icon>
                <span>历史录像</span>
              </span>
            </template>
            <div class="record-layout">
              <!-- 左侧：录像片段列表 -->
              <div class="record-list-panel">
                <div class="record-list-header">
                  <el-date-picker
                    v-model="selectedDate"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    @change="onDateChange"
                    style="width: 100%; margin-bottom: 12px;"
                  />
                  <div class="record-list-title">录像片段列表</div>
                </div>
                <div class="record-list-body" v-loading="recordLoading">
                  <div v-if="recordList.length === 0 && !recordLoading" class="empty-record">
                    暂无录像数据
                  </div>
                  <div
                    v-for="(record, index) in recordList"
                    :key="index"
                    class="record-item"
                    :class="{ active: selectedRecordIndex === index }"
                    @click="selectRecord(index)"
                  >
                    <div>
                      <div class="record-time">{{ record.startTime }} - {{ record.endTime }}</div>
                      <div class="record-duration">时长: {{ record.duration }}</div>
                    </div>
                    <span class="record-action" @click.stop="playRecord(record)">
                      ▶
                    </span>
                  </div>
                </div>
              </div>
              
              <!-- 右侧：视频播放区域 -->
              <div class="record-player-panel">
                <div class="video-container">
                  <VideoPlayer 
                    v-if="recordPlaying && recordVideoUrl"
                    :video-url="recordVideoUrl"
                    :is-playback="true"
                    @error="handleRecordPlayError"
                    class="video-player"
                  />
                  <div v-else-if="recordPlayLoading" class="video-placeholder loading">
                    <div class="loading-spinner"></div>
                    <div class="loading-text">正在加载视频...</div>
                  </div>
                  <div v-else class="video-placeholder">
                    <el-icon class="video-placeholder-icon"><VideoCamera /></el-icon>
                    <div>请选择录像片段播放</div>
                  </div>
                </div>
                
                <!-- 播放控制栏 -->
                <div class="player-controls">
                  <!-- 时间轴 -->
                  <div class="timeline-container">
                    <div class="timeline-label">
                      <span>00:00</span>
                      <span>24:00</span>
                    </div>
                    <div class="timeline-bar" @click="onTimelineClick" @mousemove="onTimelineMouseMove" @mouseleave="onTimelineMouseLeave" ref="timelineRef">
                      <!-- 时间刻度标记 -->
                      <div
                        v-for="hour in 25"
                        :key="hour"
                        class="timeline-hour-mark"
                        :style="{ left: ((hour - 1) * 4.1667) + '%' }"
                        :title="String(hour - 1).padStart(2, '0') + ':00'"
                      >
                        <div class="hour-mark-dot"></div>
                        <div class="hour-mark-label">{{ String(hour - 1).padStart(2, '0') }}</div>
                      </div>
                      <!-- 录像片段 -->
                      <div
                        v-for="(segment, index) in timelineSegments"
                        :key="index"
                        class="timeline-segment"
                        :style="{
                          left: segment.leftPercent + '%',
                          width: segment.widthPercent + '%'
                        }"
                        @click.stop="selectTimelineSegment(segment)"
                      ></div>
                      <!-- 鼠标悬停时间提示 -->
                      <div
                        v-if="hoverTime"
                        class="timeline-tooltip"
                        :style="{ left: hoverTimePercent + '%' }"
                      >
                        {{ hoverTime }}
                      </div>
                    </div>
                  </div>
                  
                  <!-- 时间控制 -->
                  <div class="time-controls">
                    <div class="time-input-group">
                      <span class="time-label">开始时间</span>
                      <el-input
                        v-model="recordStartTime"
                        placeholder="00:00:00"
                        size="small"
                        style="width: 100px;"
                      />
                    </div>
                    <span class="time-separator">至</span>
                    <div class="time-input-group">
                      <span class="time-label">结束时间</span>
                      <el-input
                        v-model="recordEndTime"
                        placeholder="23:59:59"
                        size="small"
                        style="width: 100px;"
                      />
                    </div>
                    <el-button type="primary" size="small" @click="playByTimeRange">播放</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Monitor, Refresh, VideoPlay, VideoPause, VideoCamera } from '@element-plus/icons-vue'
import { getVideoDetail, playVideo, queryRecord, playbackRecord } from '../api/video'
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

// 标签页
const activeTab = ref('realtime')

// 视频播放
const playing = ref(false)
const playLoading = ref(false)
const videoUrl = ref('')

// 历史录像相关
const selectedDate = ref(new Date().toISOString().split('T')[0])
const recordList = ref([])
const recordLoading = ref(false)
const selectedRecordIndex = ref(-1)
const recordPlaying = ref(false)
const recordPlayLoading = ref(false)
const recordVideoUrl = ref('')
const recordStartTime = ref('00:00:00')
const recordEndTime = ref('23:59:59')
const timelineRef = ref(null)
const timelineSegments = ref([])
const currentLoadingDate = ref(null) // 当前正在加载的日期，用于判断请求是否有效
const hoverTime = ref('')
const hoverTimePercent = ref(0)

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

// 日期变化
const onDateChange = () => {
  // 重置状态
  selectedRecordIndex.value = -1
  recordPlaying.value = false
  recordVideoUrl.value = ''
  recordStartTime.value = '00:00:00'
  recordEndTime.value = '23:59:59'
  recordList.value = []
  timelineSegments.value = []
  
  // 加载新日期的录像列表
  loadRecordList()
}

// 加载录像列表
const loadRecordList = async () => {
  if (!selectedDate.value) {
    recordList.value = []
    timelineSegments.value = []
    recordLoading.value = false
    currentLoadingDate.value = null
    return
  }
  
  // 记录当前加载的日期
  const loadingDate = selectedDate.value
  currentLoadingDate.value = loadingDate
  
  recordLoading.value = true
  recordList.value = []
  timelineSegments.value = []
  
  // 设置3秒超时
  const timeoutPromise = new Promise((_, reject) => {
    setTimeout(() => {
      reject(new Error('请求超时'))
    }, 3000)
  })
  
  try {
    const startTime = `${selectedDate.value} 00:00:00`
    const endTime = `${selectedDate.value} 23:59:59`
    
    // 使用Promise.race实现3秒超时
    let data
    try {
      data = await Promise.race([
        queryRecord({
          deviceId,
          channelId,
          startTime,
          endTime
        }),
        timeoutPromise
      ])
    } catch (raceError) {
      // 如果是超时错误，直接返回空数据
      if (raceError.message?.includes('超时') || raceError.message?.includes('timeout')) {
        recordList.value = []
        timelineSegments.value = []
        return
      }
      throw raceError
    }
    
    // 检查日期是否已经变化（用户可能在请求期间切换了日期）
    if (currentLoadingDate.value !== loadingDate) {
      // 日期已经变化，忽略这次响应
      return
    }
    
    // 处理返回的数据
    if (data && data.recordList && Array.isArray(data.recordList)) {
      recordList.value = data.recordList.map(record => {
        const start = record.startTime ? (record.startTime.split(' ')[1] || record.startTime) : ''
        const end = record.endTime ? (record.endTime.split(' ')[1] || record.endTime) : ''
        const duration = calculateDuration(record.startTime, record.endTime)
        
        return {
          ...record,
          startTime: start,
          endTime: end,
          duration,
          startTimeFull: record.startTime,
          endTimeFull: record.endTime
        }
      })
      
      // 生成时间轴数据
      generateTimelineSegments()
    } else {
      // 没有录像数据
      recordList.value = []
      timelineSegments.value = []
    }
  } catch (error) {
    // 检查日期是否已经变化
    if (currentLoadingDate.value !== loadingDate) {
      // 日期已经变化，忽略错误
      return
    }
    
    // 超时或错误时，静默处理，直接显示"暂无录像数据"（不显示错误提示）
    console.error('加载录像列表失败:', error)
    recordList.value = []
    timelineSegments.value = []
  } finally {
    // 只有在当前日期没有变化时才清除loading状态
    if (currentLoadingDate.value === loadingDate) {
      recordLoading.value = false
      currentLoadingDate.value = null
    }
  }
}

// 计算时长
const calculateDuration = (startTime, endTime) => {
  try {
    const start = new Date(startTime.replace(/-/g, '/'))
    const end = new Date(endTime.replace(/-/g, '/'))
    const diff = Math.floor((end - start) / 1000)
    
    if (diff < 60) {
      return `${diff}秒`
    } else if (diff < 3600) {
      const minutes = Math.floor(diff / 60)
      const seconds = diff % 60
      return seconds > 0 ? `${minutes}分${seconds}秒` : `${minutes}分钟`
    } else {
      const hours = Math.floor(diff / 3600)
      const minutes = Math.floor((diff % 3600) / 60)
      return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
    }
  } catch (e) {
    return '-'
  }
}

// 生成时间轴数据
const generateTimelineSegments = () => {
  const segments = []
  recordList.value.forEach(record => {
    const start = timeToMinutes(record.startTime)
    const end = timeToMinutes(record.endTime)
    segments.push({
      start,
      end,
      leftPercent: (start / 1440) * 100,
      widthPercent: ((end - start) / 1440) * 100,
      record
    })
  })
  timelineSegments.value = segments
}

// 时间转分钟数（0-1440）
const timeToMinutes = (timeStr) => {
  const [h, m, s] = timeStr.split(':').map(Number)
  return h * 60 + m + s / 60
}

// 选择录像片段
const selectRecord = (index) => {
  selectedRecordIndex.value = index
  const record = recordList.value[index]
  recordStartTime.value = record.startTime
  recordEndTime.value = record.endTime
}

// 播放录像
const playRecord = async (record) => {
  selectedRecordIndex.value = recordList.value.findIndex(r => r === record)
  recordStartTime.value = record.startTime
  recordEndTime.value = record.endTime
  
  await playByTimeRange()
}

// 按时间范围播放
const playByTimeRange = async () => {
  if (!selectedDate.value) {
    ElMessage.warning('请先选择日期')
    return
  }
  
  const startTime = `${selectedDate.value} ${recordStartTime.value}`
  const endTime = `${selectedDate.value} ${recordEndTime.value}`
  
  recordPlayLoading.value = true
  try {
    const data = await playbackRecord({
      deviceId,
      channelId,
      startTime,
      endTime
    })
    
    recordVideoUrl.value = data.hlsUrl
    recordPlaying.value = true
    ElMessage.success('回放流获取成功')
  } catch (error) {
    ElMessage.error('获取回放流失败: ' + error.message)
    recordPlaying.value = false
    recordVideoUrl.value = ''
  } finally {
    recordPlayLoading.value = false
  }
}

// 时间轴点击
const onTimelineClick = (event) => {
  if (!timelineRef.value) return
  const rect = timelineRef.value.getBoundingClientRect()
  const clickX = event.clientX - rect.left
  const percent = (clickX / rect.width) * 100
  const minutes = (percent / 100) * 1440
  const timeStr = minutesToTime(minutes)
  recordStartTime.value = timeStr
}

// 时间轴鼠标移动事件
const onTimelineMouseMove = (event) => {
  if (!timelineRef.value) return
  const rect = timelineRef.value.getBoundingClientRect()
  const mouseX = event.clientX - rect.left
  const percent = Math.max(0, Math.min(100, (mouseX / rect.width) * 100))
  const minutes = (percent / 100) * 1440
  hoverTime.value = minutesToTime(minutes)
  hoverTimePercent.value = percent
}

// 时间轴鼠标离开事件
const onTimelineMouseLeave = () => {
  hoverTime.value = ''
  hoverTimePercent.value = 0
}

// 分钟数转时间字符串
const minutesToTime = (minutes) => {
  const h = Math.floor(minutes / 60)
  const m = Math.floor(minutes % 60)
  const s = Math.floor((minutes % 1) * 60)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 选择时间轴片段
const selectTimelineSegment = (segment) => {
  const record = segment.record
  const index = recordList.value.findIndex(r => r.startTimeFull === record.startTimeFull)
  if (index >= 0) {
    selectRecord(index)
  }
}

// 录像播放错误处理
const handleRecordPlayError = (error) => {
  ElMessage.error('录像播放失败: ' + error)
  recordPlaying.value = false
  recordVideoUrl.value = ''
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'record' && recordList.value.length === 0) {
    loadRecordList()
  }
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
  padding: 6px;
  height: calc(100vh - 60px);
  max-height: calc(100vh - 60px);
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
  margin-bottom: 6px;
  flex-shrink: 0;
  height: 30px;
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

.video-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.video-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 12px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.video-tabs :deep(.el-tabs__nav-wrap) {
  height: 36px;
}

.video-tabs :deep(.el-tabs__item) {
  height: 36px;
  line-height: 36px;
  padding: 0 12px;
  font-size: 13px;
}

.video-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.video-tabs :deep(.el-tab-pane) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.video-controls-bar {
  padding: 10px 14px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

/* 历史录像样式 */
.record-layout {
  display: flex;
  gap: 10px;
  height: 100%;
  padding: 10px;
  overflow: hidden;
}

.record-list-panel {
  width: 320px;
  flex-shrink: 0;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e5e5e7;
}

.record-list-header {
  padding: 10px;
  border-bottom: 1px solid #e5e5e7;
  flex-shrink: 0;
}

.record-list-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.record-list-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.record-item {
  padding: 10px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid transparent;
}

.record-item:hover {
  background: #f5f5f7;
  border-color: #e5e5e7;
}

.record-item.active {
  background: #e8f4ff;
  border-color: #007aff;
}

.record-time {
  font-size: 13px;
  color: #1d1d1f;
  font-weight: 500;
}

.record-duration {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
}

.record-action {
  color: #007aff;
  cursor: pointer;
  padding: 4px;
  font-size: 14px;
}

.record-action:hover {
  color: #0051d5;
}

.empty-record {
  text-align: center;
  padding: 40px 20px;
  color: #86868b;
  font-size: 14px;
}

.record-player-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.player-controls {
  padding: 10px;
  background: #fafafa;
  border-top: 1px solid #e5e5e7;
  flex-shrink: 0;
}

.timeline-container {
  margin-bottom: 10px;
  position: relative;
  padding-top: 24px;
  overflow: visible;
  min-height: 70px;
}

.timeline-label {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #86868b;
  margin-bottom: 2px;
  padding: 0 2px;
  position: absolute;
  top: -18px;
  left: 0;
  right: 0;
  z-index: 1;
}

.timeline-bar {
  height: 40px;
  background: #e5e5e7;
  border-radius: 6px;
  position: relative;
  overflow: visible;
  cursor: pointer;
  margin-top: 0;
}

.timeline-segment {
  position: absolute;
  top: 0;
  height: 100%;
  background: #007aff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 1;
}

.timeline-segment:hover {
  background: #0051d5;
  box-shadow: 0 0 8px rgba(0, 122, 255, 0.4);
}

/* 时间刻度标记 */
.timeline-hour-mark {
  position: absolute;
  top: -20px;
  transform: translateX(-50%);
  z-index: 2;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hour-mark-dot {
  width: 6px;
  height: 6px;
  background: #ffffff;
  border-radius: 50%;
  margin-bottom: 2px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.hour-mark-label {
  font-size: 10px;
  color: #86868b;
  text-align: center;
  white-space: nowrap;
}

/* 鼠标悬停时间提示 */
.timeline-tooltip {
  position: absolute;
  top: -28px;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 10;
}

.timeline-tooltip::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid rgba(0, 0, 0, 0.8);
}

.time-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.time-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-label {
  font-size: 13px;
  color: #1d1d1f;
  white-space: nowrap;
}

.time-separator {
  color: #86868b;
  font-size: 13px;
}

.video-placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.4;
  color: rgba(255, 255, 255, 0.6);
}
</style>
