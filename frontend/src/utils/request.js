import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Token失效处理函数（统一处理）
const handleTokenExpired = (message) => {
  // 清理所有用户相关数据
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('menus')
  
  // 显示友好的错误消息
  const errorMessage = message || '登录已失效，请重新登录'
  ElMessage.error(errorMessage)
  
  // 如果当前不在登录页，则跳转到登录页
  if (router.currentRoute.value.path !== '/login') {
    router.replace({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath }
    }).catch(err => {
      // 如果路由跳转失败，使用window.location作为降级方案
      console.error('路由跳转失败:', err)
      window.location.href = '/login'
    })
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 LocalStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      // 将 token 放入 Header 的 Authorization 中
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 统一处理响应结构 {code, data, message}
    if (res.code !== 200) {
      // 如果是 401，处理token失效
      if (res.code === 401) {
        handleTokenExpired(res.message || '未授权，请重新登录')
        // 返回Promise.reject，阻止后续处理
        return Promise.reject(new Error(res.message || '登录已过期'))
      }
      
      // 返回Promise.reject，让调用方处理错误提示
      return Promise.reject(new Error(res.message || 'Error'))
    }
    
    // 返回 data 部分
    return res.data
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理HTTP状态码错误
    if (error.response) {
      const status = error.response.status
      
      if (status === 401) {
        // Token失效，统一处理
        handleTokenExpired('未授权，请重新登录')
        // 返回Promise.reject，阻止后续处理
        return Promise.reject(new Error('登录已过期'))
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
