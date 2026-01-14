<template>
  <div class="video-player-container">
    <video 
      ref="videoRef"
      class="video-element"
      controls
      autoplay
      muted
    >
      您的浏览器不支持视频播放
    </video>
    <div v-if="error" class="error-message">
      <el-icon><WarningFilled /></el-icon>
      <span>{{ error }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import Hls from 'hls.js'
import { WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  videoUrl: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['error'])

const videoRef = ref(null)
const hls = ref(null)
const error = ref('')

// 初始化HLS播放器
const initPlayer = () => {
  if (!props.videoUrl) {
    error.value = '视频地址为空'
    emit('error', error.value)
    return
  }
  
  const video = videoRef.value
  if (!video) {
    error.value = '视频元素未初始化'
    emit('error', error.value)
    return
  }
  
  // 检查浏览器是否原生支持HLS
  if (video.canPlayType('application/vnd.apple.mpegurl')) {
    // Safari等浏览器原生支持HLS
    video.src = props.videoUrl
    video.addEventListener('error', handleVideoError)
  } else if (Hls.isSupported()) {
    // 使用hls.js
    hls.value = new Hls({
      enableWorker: true,
      lowLatencyMode: true,
      backBufferLength: 90
    })
    
    hls.value.loadSource(props.videoUrl)
    hls.value.attachMedia(video)
    
    hls.value.on(Hls.Events.MANIFEST_PARSED, () => {
      video.play().catch(err => {
        console.warn('自动播放失败:', err)
      })
    })
    
    hls.value.on(Hls.Events.ERROR, (event, data) => {
      console.error('HLS错误:', data)
      if (data.fatal) {
        switch (data.type) {
          case Hls.ErrorTypes.NETWORK_ERROR:
            error.value = '网络错误，尝试恢复...'
            hls.value.startLoad()
            break
          case Hls.ErrorTypes.MEDIA_ERROR:
            error.value = '媒体错误，尝试恢复...'
            hls.value.recoverMediaError()
            break
          default:
            error.value = '无法恢复的播放错误'
            emit('error', error.value)
            destroyPlayer()
            break
        }
      }
    })
  } else {
    error.value = '当前浏览器不支持HLS视频播放'
    emit('error', error.value)
  }
}

// 销毁播放器
const destroyPlayer = () => {
  if (hls.value) {
    hls.value.destroy()
    hls.value = null
  }
  
  const video = videoRef.value
  if (video) {
    video.removeEventListener('error', handleVideoError)
    video.pause()
    video.src = ''
  }
  
  error.value = ''
}

// 视频错误处理
const handleVideoError = (e) => {
  console.error('视频播放错误:', e)
  error.value = '视频播放失败'
  emit('error', error.value)
}

// 监听videoUrl变化
watch(() => props.videoUrl, (newUrl, oldUrl) => {
  if (newUrl !== oldUrl) {
    destroyPlayer()
    if (newUrl) {
      initPlayer()
    }
  }
})

// 初始化
onMounted(() => {
  initPlayer()
})

// 清理
onUnmounted(() => {
  destroyPlayer()
})
</script>

<style scoped>
.video-player-container {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-element {
  width: 100%;
  height: auto;
  min-height: 400px;
  display: block;
}

.error-message {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ff3b30;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.8);
  padding: 12px 24px;
  border-radius: 8px;
}
</style>
