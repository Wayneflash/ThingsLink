import Vue from 'vue'
import App from './App'
import './uni.promisify.adaptor'

Vue.config.productionTip = false

// 全局错误处理 - 防止显示 [object Object] 错误
Vue.config.errorHandler = (err, vm, info) => {
  console.error('Vue Error:', err, info)
  // 确保错误信息是字符串
  let errorMessage = '发生错误'
  if (err) {
    if (err instanceof Error) {
      errorMessage = err.message || String(err)
    } else if (typeof err === 'object') {
      errorMessage = err.message || err.errMsg || err.msg || JSON.stringify(err)
    } else {
      errorMessage = String(err)
    }
  }
  
  // 显示错误提示（避免重复提示）
  if (typeof uni !== 'undefined') {
    uni.showToast({
      title: errorMessage.length > 20 ? errorMessage.substring(0, 20) + '...' : errorMessage,
      icon: 'none',
      duration: 3000
    })
  }
}

// 全局未捕获的Promise拒绝处理
if (typeof window !== 'undefined') {
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled Promise Rejection:', event.reason)
    let errorMessage = '请求失败'
    if (event.reason) {
      if (event.reason instanceof Error) {
        errorMessage = event.reason.message || String(event.reason)
      } else if (typeof event.reason === 'object') {
        errorMessage = event.reason.message || event.reason.errMsg || event.reason.msg || '请求失败'
      } else {
        errorMessage = String(event.reason)
      }
    }
    
    if (typeof uni !== 'undefined') {
      uni.showToast({
        title: errorMessage.length > 20 ? errorMessage.substring(0, 20) + '...' : errorMessage,
        icon: 'none',
        duration: 3000
      })
    }
    
    // 阻止默认行为（避免控制台显示错误）
    event.preventDefault()
  })
}

App.mpType = 'app'

const app = new Vue({
  ...App
})
app.$mount()
