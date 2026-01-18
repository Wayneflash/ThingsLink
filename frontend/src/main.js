import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './styles/common.css'  // 包含 Tailwind CSS
import './styles/element-theme.css'  // 自定义主题
import App from './App.vue'
import router from './router'

// 版本检查和自动刷新逻辑（仅在生产环境生效）
const checkVersionAndRefresh = () => {
  // 只在生产环境检查版本（开发环境使用 Vite HMR，不需要）
  if (import.meta.env.DEV) {
    return
  }
  
  try {
    // 从 meta 标签获取构建时间戳
    const buildTimeMeta = document.querySelector('meta[name="build-timestamp"]')
    const buildTimestamp = buildTimeMeta ? buildTimeMeta.getAttribute('content') : null
    
    if (!buildTimestamp) {
      // 如果没有构建时间戳，跳过检查
      return
    }
    
    // 获取本地保存的版本信息
    const localBuildTimestamp = localStorage.getItem('app_build_timestamp')
    
    // 如果版本不同，说明有新版部署，自动刷新
    if (localBuildTimestamp && localBuildTimestamp !== buildTimestamp) {
      console.log('检测到新版本，正在刷新页面以获取最新代码...')
      // 保存新版本号
      localStorage.setItem('app_build_timestamp', buildTimestamp)
      // 刷新页面
      window.location.reload(true) // 强制从服务器获取
      return
    }
    
    // 首次访问或版本相同，保存当前版本号
    if (!localBuildTimestamp) {
      localStorage.setItem('app_build_timestamp', buildTimestamp)
    }
  } catch (error) {
    console.error('版本检查失败:', error)
    // 版本检查失败不影响应用正常启动
  }
}

// 在应用启动前检查版本
checkVersionAndRefresh()

const app = createApp(App)

app.use(ElementPlus, {
  locale: zhCn
})
app.use(router)

app.mount('#app')
