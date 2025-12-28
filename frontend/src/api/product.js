import request from '@/utils/request'

/**
 * 产品管理 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 获取产品列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认20
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.category - 分类筛选
 * @returns {Promise} 返回产品列表数据
 */
export const getProductList = (params) => {
  return request({
    url: '/products/list',
    method: 'post',
    data: params
  })
}

/**
 * 创建产品
 * @param {Object} data - 产品信息
 * @param {string} data.name - 产品名称
 * @param {string} data.code - 产品型号/编码
 * @param {string} data.protocol - 协议类型 (MQTT/HTTP等）
 * @param {string} data.description - 产品描述
 * @param {number} data.status - 状态 (1:启用, 0:禁用）
 * @returns {Promise} 返回创建的产品信息
 */
export const createProduct = (data) => {
  return request({
    url: '/products/create',
    method: 'post',
    data
  })
}

/**
 * 获取产品详情
 * @param {number} id - 产品ID
 * @returns {Promise} 返回产品详细信息
 */
export const getProductDetail = (id) => {
  return request({
    url: '/products/detail',
    method: 'post',
    data: { id }
  })
}

/**
 * 更新产品信息
 * @param {Object} data - 产品信息
 * @param {number} data.id - 产品ID（必填）
 * @param {string} data.productName - 产品名称
 * @param {string} data.productModel - 产品型号
 * @param {string} data.protocol - 协议类型
 * @param {string} data.description - 产品描述
 * @param {number} data.status - 状态
 * @returns {Promise} 返回更新后的产品信息
 */
export const updateProduct = (data) => {
  return request({
    url: '/products/update',
    method: 'post',
    data
  })
}

/**
 * 删除产品
 * @param {number} id - 产品ID
 * @returns {Promise} 返回删除结果
 */
export const deleteProduct = (id) => {
  return request({
    url: '/products/delete',
    method: 'post',
    data: { id }
  })
}

/**
 * 添加产品属性
 * @param {Object} data - 属性信息
 * @param {number} data.productId - 产品ID
 * @param {string} data.attrName - 属性名称
 * @param {string} data.attrCode - 属性编码
 * @param {string} data.attrType - 属性类型
 * @param {string} data.unit - 单位
 * @param {string} data.description - 描述
 * @returns {Promise} 返回添加的属性信息
 */
export const addProductAttribute = (data) => {
  return request({
    url: '/products/attribute/create',
    method: 'post',
    data
  })
}

/**
 * 获取产品属性列表
 * @param {number} productId - 产品ID
 * @returns {Promise} 返回属性列表
 */
export const getProductAttributes = (productId) => {
  return request({
    url: `/products/${productId}/attributes`,
    method: 'get'
  })
}

/**
 * 添加产品命令
 * @param {Object} data - 命令信息
 * @param {number} data.productId - 产品ID
 * @param {string} data.commandName - 命令名称
 * @param {string} data.commandCode - 命令编码
 * @param {string} data.description - 描述
 * @returns {Promise} 返回添加的命令信息
 */
export const addProductCommand = (data) => {
  return request({
    url: '/products/command/create',
    method: 'post',
    data
  })
}

/**
 * 获取产品命令列表
 * @param {number} productId - 产品ID
 * @returns {Promise} 返回命令列表
 */
export const getProductCommands = (productId) => {
  return request({
    url: `/products/${productId}/commands`,
    method: 'get'
  })
}

export default {
  getProductList,
  createProduct,
  getProductDetail,
  updateProduct,
  deleteProduct,
  addProductAttribute,
  getProductAttributes,
  addProductCommand,
  getProductCommands
}
