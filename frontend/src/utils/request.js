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
      // 如果是 401，跳转到登录页
      if (res.code === 401) {
        ElMessage.error(res.message || '未授权，请重新登录')
        localStorage.removeItem('token')
        router.push('/login')
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
        ElMessage.error('未授权，请重新登录')
        localStorage.removeItem('token')
        router.push('/login')
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
