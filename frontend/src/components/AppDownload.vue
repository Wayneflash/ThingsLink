<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    :width="280"
    trigger="hover"
    popper-class="app-download-popover"
    :offset="8"
  >
    <template #reference>
      <el-button text class="app-download-btn">
        <svg 
          class="app-icon" 
          width="22" 
          height="22" 
          viewBox="0 0 24 24" 
          fill="none" 
          stroke="currentColor" 
          stroke-width="2" 
          stroke-linecap="round" 
          stroke-linejoin="round"
        >
          <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
          <line x1="12" y1="18" x2="12.01" y2="18"></line>
        </svg>
      </el-button>
    </template>

    <div class="app-download-panel">
      <div class="download-header">
        <h3 class="download-title">下载安卓APP</h3>
        <p class="download-desc">扫描二维码下载移动端应用</p>
      </div>
      
      <div class="qr-code-wrapper">
        <img 
          src="/logo/download.png" 
          alt="下载二维码" 
          class="qr-code-image"
          @error="handleImageError"
        />
      </div>
      
      <div class="download-footer">
        <p class="footer-tip">使用手机扫描二维码即可下载</p>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const visible = ref(false)

// 处理图片加载错误
const handleImageError = (e) => {
  console.error('二维码图片加载失败:', e)
  ElMessage.warning('二维码图片加载失败，请检查图片路径')
}
</script>

<style scoped>
.app-download-btn {
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-icon {
  color: #667eea;
  transition: all 0.3s ease;
}

.app-download-btn:hover {
  background-color: #f5f7fa;
}

.app-download-btn:hover .app-icon {
  color: #764ba2;
  transform: scale(1.1);
}

.app-download-panel {
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.download-header {
  text-align: center;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #ebeef5;
  width: 100%;
}

.download-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 6px 0;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.download-desc {
  font-size: 13px;
  color: #86868b;
  margin: 0;
  line-height: 1.4;
}

.qr-code-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  width: 100%;
  box-sizing: border-box;
}

.qr-code-image {
  width: 200px;
  height: 200px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background: #ffffff;
}

.download-footer {
  padding: 12px 16px 16px;
  border-top: 1px solid #ebeef5;
  width: 100%;
  text-align: center;
}

.footer-tip {
  font-size: 12px;
  color: #86868b;
  margin: 0;
  line-height: 1.4;
}
</style>

<style>
.app-download-popover {
  padding: 0 !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
  border-radius: 12px !important;
  overflow: hidden;
}
</style>
