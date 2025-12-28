import request from '@/utils/request'

// 产品管理 API
export const productApi = {
  // 创建产品
  create: (data) => request.post('/product/create', data),
  // 获取产品列表
  list: () => request.get('/product/list'),
  // 获取产品详情
  getById: (id) => request.get(`/product/${id}`),
  // 添加产品属性
  addAttribute: (data) => request.post('/product/attribute/create', data),
  // 获取产品属性列表
  getAttributes: (productId) => request.get(`/product/${productId}/attributes`),
  // 添加产品命令 ⭐️ 新增
  addCommand: (data) => request.post('/product/command/create', data),
  // 获取产品命令列表 ⭐️ 新增
  getCommands: (productId) => request.get(`/product/${productId}/commands`)
}

// 设备管理 API
export const deviceApi = {
  // 创建设备
  create: (data) => request.post('/device/create', data),
  // 获取设备列表
  list: () => request.get('/device/list'),
  // 获取设备详情
  getByCode: (deviceCode) => request.get(`/device/${deviceCode}`)
}

// 设备数据 API
export const dataApi = {
  // 获取最新数据
  getLatest: (deviceCode, limit = 10) => 
    request.get(`/data/latest/${deviceCode}?limit=${limit}`),
  // 查询历史数据
  getHistory: (params) => request.get(`/data/history/${params.deviceCode}`, { params })
}

// 命令下发 API
export const commandApi = {
  // 发送命令
  send: (data) => request.post('/command/send', data)
}
