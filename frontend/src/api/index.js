import request from '@/utils/request'

// 产品管理 API
export const productApi = {
  // 创建产品
  createProduct: (data) => request.post('/products/create', data),
  // 获取产品列表
  getProductList: () => request.post('/products/list', {}),
  // 获取产品详情
  getProductDetail: (id) => request.post('/products/detail', { id }),
  // 更新产品信息
  updateProduct: (data) => request.post('/products/update', data),
  // 删除产品
  deleteProduct: (id) => request.post('/products/delete', { id }),
  // 添加产品属性
  addProductAttribute: (data) => request.post('/products/attribute/create', data),
  // 获取产品属性列表
  getProductAttributes: (productId) => request.get(`/products/${productId}/attributes`),
  // 更新产品属性
  updateProductAttribute: (data) => request.post('/products/attribute/update', data),
  // 删除产品属性
  deleteProductAttribute: (id) => request.post('/products/attribute/delete', { id }),
  // 添加产品命令
  addProductCommand: (data) => request.post('/products/command/create', data),
  // 获取产品命令列表
  getProductCommands: (productId) => request.get(`/products/${productId}/commands`),
  // 更新产品命令
  updateProductCommand: (data) => request.post('/products/command/update', data),
  // 删除产品命令
  deleteProductCommand: (id) => request.post('/products/command/delete', { id }),
  // 兼容旧方法名
  create: (data) => request.post('/products/create', data),
  getById: (id) => request.post('/products/detail', { id }),
  update: (data) => request.post('/products/update', data),
  delete: (id) => request.post('/products/delete', { id }),
  addAttribute: (data) => request.post('/products/attribute/create', data),
  getAttributes: (productId) => request.get(`/products/${productId}/attributes`),
  deleteAttribute: (id) => request.post('/products/attribute/delete', { id }),
  addCommand: (data) => request.post('/products/command/create', data),
  getCommands: (productId) => request.get(`/products/${productId}/commands`),
  updateCommand: (data) => request.post('/products/command/update', data),
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

// 场景联动 API
export const sceneApi = {
  // 获取可选设备列表（支持模糊搜索）
  getAvailableDevices: (keyword) => request.get('/scene/devices', { params: { keyword } }),
  // 获取规则列表
  getRules: (pageNum = 1, pageSize = 10) => request.get(`/scene/rules?pageNum=${pageNum}&pageSize=${pageSize}`),
  // 获取启用的规则
  getEnabledRules: () => request.get('/scene/rules/enabled'),
  // 获取规则详情
  getRuleDetail: (id) => request.get(`/scene/rules/${id}`),
  // 创建规则
  createRule: (data) => request.post('/scene/rules', data),
  // 更新规则
  updateRule: (id, data) => request.put(`/scene/rules/${id}`, data),
  // 切换规则启用状态
  toggleRule: (id) => request.put(`/scene/rules/${id}/toggle`),
  // 删除规则
  deleteRule: (id) => request.delete(`/scene/rules/${id}`),
  // 获取规则执行日志
  getRuleLogs: (ruleId) => request.get(`/scene/rules/${ruleId}/logs`),
  // 获取最近执行日志
  getRecentLogs: (limit = 50) => request.get(`/scene/logs/recent?limit=${limit}`)
}
