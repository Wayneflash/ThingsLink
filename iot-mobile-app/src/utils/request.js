// UniApp 请求封装 - 基于现有 request.js 适配

// 获取API基础地址
function getBaseURL() {
  // 开发环境优先使用本地代理（方便本地开发调试）
  if (process.env.NODE_ENV === 'development') {
    // 开发环境优先使用代理路径（代理到 localhost:8080）
    return '/api'
  }
  
  // 生产环境：优先从本地存储读取服务器配置
  try {
    const savedConfig = uni.getStorageSync('api_server_config')
    if (savedConfig) {
      const config = typeof savedConfig === 'string' ? JSON.parse(savedConfig) : savedConfig
      const host = config.host || '117.72.222.8'
      const port = config.port || '8080'
      return `http://${host}:${port}`
    }
  } catch (e) {
    console.error('读取服务器配置失败:', e)
  }
  
  // 默认地址（生产环境）
  return 'http://117.72.222.8:8080'
}

// API 基础地址
const baseURL = getBaseURL()

// Token失效处理函数
const handleTokenExpired = (message) => {
  // 清理所有用户相关数据
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  uni.removeStorageSync('menus')
  
  // 显示错误消息
  uni.showToast({
    title: message || '登录已过期，请重新登录',
    icon: 'none',
    duration: 2000
  })
  
  // 跳转到登录页
  setTimeout(() => {
    uni.reLaunch({
      url: '/pages/login/login'
    })
  }, 1500)
}

// 请求封装
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = uni.getStorageSync('token')
    
    // 动态获取 baseURL（每次请求都重新读取，支持运行时修改）
    const currentBaseURL = getBaseURL()
    
    // 构建完整 URL
    const url = options.url.startsWith('http') 
      ? options.url 
      : currentBaseURL + options.url
    
    // 统一处理 data/params（GET 请求用 params，POST 请求用 data）
    const method = (options.method || 'POST').toUpperCase()
    const requestData = method === 'GET' 
      ? (options.params || options.data || {})
      : (options.data || {})
    
    uni.request({
      url: url,
      method: method,
      data: requestData,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      timeout: options.timeout || 10000,
      success: (res) => {
        const data = res.data
        
        // 统一处理响应结构 {code, data, message}
        if (data.code !== 200) {
          // 如果是 401，处理token失效
          if (data.code === 401) {
            handleTokenExpired(data.message || '未授权，请重新登录')
            return reject(new Error(data.message || '登录已过期'))
          }
          
          // 显示错误提示
          const errorMsg = data.message || '请求失败'
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 2000
          })
          return reject(new Error(String(errorMsg)))
        }
        
        // 返回 data 部分
        resolve(data.data)
      },
      fail: (err) => {
        console.error('请求失败:', err)
        
        // 处理网络错误
        let errorMessage = '网络错误，请检查网络连接'
        
        if (err.errMsg) {
          if (err.errMsg.includes('timeout')) {
            errorMessage = '请求超时，请稍后重试'
          } else if (err.errMsg.includes('fail')) {
            errorMessage = '网络连接失败'
          }
        }
        
        uni.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 2000
        })
        
        // 确保reject的是Error对象，而不是普通对象
        if (err instanceof Error) {
          reject(err)
        } else {
          reject(new Error(err.errMsg || err.message || String(err)))
        }
      }
    })
  })
}

// 为 request 添加便捷方法（支持 request.post() 和 request.get()）
request.get = (url, data) => {
  return request({ url, method: 'GET', data })
}

request.post = (url, data) => {
  return request({ url, method: 'POST', data })
}

// 导出便捷方法（也支持独立导入）
export const get = request.get
export const post = request.post

export default request
