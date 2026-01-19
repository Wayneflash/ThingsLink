// UniApp 请求封装 - 基于现有 request.js 适配

// API 基础地址 - 根据环境配置
// 开发环境使用代理路径，生产环境需要完整地址
const baseURL = process.env.NODE_ENV === 'development' 
  ? '/api'  // 开发环境使用代理（在manifest.json中配置）
  : 'https://your-api-domain.com'  // 生产环境，请替换为实际地址

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
    
    // 构建完整 URL
    const url = options.url.startsWith('http') 
      ? options.url 
      : baseURL + options.url
    
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
