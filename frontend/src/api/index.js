import request from '@/utils/request'

// 产品管理 API
export const productApi = {
  // 创建产品
  create: (data) => request.post('/products/create', data),
  // 获取产品列表
  list: () => request.post('/products/list', {}),
  // 获取产品详情
  getById: (id) => request.post('/products/detail', { id }),
  // 更新产品信息
  update: (data) => request.post('/products/update', data),
  // 删除产品
  delete: (id) => request.post('/products/delete', { id }),
  // 添加产品属性
  addAttribute: (data) => request.post('/products/attribute/create', data),
  // 获取产品属性列表
  getAttributes: (productId) => request.get(`/products/${productId}/attributes`),
  // 删除产品属性
  deleteAttribute: (id) => request.post('/products/attribute/delete', { id }),
  // 添加产品命令
  addCommand: (data) => request.post('/products/command/create', data),
  // 获取产品命令列表
  getCommands: (productId) => request.get(`/products/${productId}/commands`),
  // 删除产品命令
  deleteCommand: (id) => request.post('/products/command/delete', { id })
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
