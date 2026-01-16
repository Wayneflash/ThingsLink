<template>
  <div class="video-player-container">
    <video 
      ref="videoRef"
      class="video-element"
      controls
      autoplay
      :muted="isMuted"
      @loadedmetadata="handleLoadedMetadata"
      @play="handleVideoPlay"
      @volumechange="handleVolumeChange"
    >
      您的浏览器不支持视频播放
    </video>
    
    <!-- 自定义静音/取消静音按钮 -->
    <div class="mute-control" @click="toggleMute">
      <!-- 静音图标 -->
      <svg v-if="isMuted" class="mute-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M16.5 12C16.5 10.23 15.48 8.71 14 7.97V10.18L16.45 12.63C16.48 12.43 16.5 12.22 16.5 12ZM19 12C19 12.94 18.8 13.82 18.46 14.64L19.97 16.15C20.62 14.91 21 13.5 21 12C21 7.72 18.01 4.14 14 3.23V5.29C16.89 6.15 19 8.83 19 12ZM4.27 3L3 4.27L7.73 9H3V15H7L12 20V13.27L16.25 17.53C15.58 18.04 14.83 18.46 14 18.7V20.77C15.38 20.45 16.63 19.82 17.68 18.96L19.73 21L21 19.73L12 10.73L4.27 3ZM12 4L9.91 6.09L12 8.18V4Z" fill="currentColor"/>
      </svg>
      <!-- 音量图标 -->
      <svg v-else class="mute-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M3 9V15H7L12 20V4L7 9H3ZM16.5 12C16.5 10.23 15.48 8.71 14 7.97V16.02C15.48 15.29 16.5 13.77 16.5 12ZM14 3.23V5.29C16.89 6.15 19 8.83 19 12C19 15.17 16.89 17.85 14 18.71V20.77C18.01 19.86 21 16.28 21 12C21 7.72 18.01 4.14 14 3.23Z" fill="currentColor"/>
      </svg>
      <span class="mute-text">{{ isMuted ? '打开声音' : '关闭声音' }}</span>
    </div>
    
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
const isMuted = ref(true) // 初始静音，保证自动播放

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
    // 尝试自动播放（如果浏览器允许）
    video.play().catch(err => {
      console.warn('自动播放失败（可能需要用户交互）:', err)
    })
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
      // 尝试自动播放（如果浏览器允许）
      video.play().catch(err => {
        console.warn('自动播放失败（可能需要用户交互）:', err)
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

// 视频元数据加载完成
const handleLoadedMetadata = () => {
  const video = videoRef.value
  if (video) {
    // 视频加载完成，设置初始音量
    video.volume = 0.5 // 设置音量为50%
    // 同步muted状态
    isMuted.value = video.muted
  }
}

// 切换静音/取消静音
const toggleMute = () => {
  const video = videoRef.value
  if (video) {
    isMuted.value = !isMuted.value
    video.muted = isMuted.value
    // 如果取消静音，确保音量不为0
    if (!isMuted.value && video.volume === 0) {
      video.volume = 0.5
    }
  }
}

// 视频开始播放
const handleVideoPlay = () => {
  // 视频开始播放
}

// 音量变化事件
const handleVolumeChange = () => {
  const video = videoRef.value
  if (video) {
    // 同步muted状态
    isMuted.value = video.muted
  }
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
  height: 100%;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.mute-control {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.7);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;
  color: #fff;
  font-size: 14px;
}

.mute-control:hover {
  background: rgba(0, 0, 0, 0.9);
}

.mute-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.mute-text {
  font-size: 14px;
  user-select: none;
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
