import request from '@/utils/request'

/**
 * 设备管理 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 获取设备列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.keyword - 搜索关键词（可选）
 * @param {number} params.productId - 产品ID筛选（可选）
 * @param {number} params.groupId - 分组ID筛选（可选）
 * @param {string} params.status - 状态筛选 online/offline（可选）
 * @returns {Promise} 返回设备列表数据
 */
export const getDeviceList = (params) => {
  return request({
    url: '/devices/list',
    method: 'post',
    data: params
  })
}

/**
 * 创建设备
 * @param {Object} data - 设备信息
 * @param {string} data.name - 设备名称（或 deviceName）
 * @param {string} data.code - 设备唯一编码（或 deviceCode）
 * @param {number} data.productId - 所属产品ID
 * @param {number} data.groupId - 所属分组ID
 * @param {string} data.remark - 备注信息（可选）
 * @returns {Promise} 返回创建的设备信息
 */
export const createDevice = (data) => {
  return request({
    url: '/devices',
    method: 'post',
    data
  })
}

/**
 * 获取设备详情
 * @param {Object} params - 参数对象
 * @param {string} params.deviceCode - 设备编码
 * @returns {Promise} 返回设备详细信息
 */
export const getDeviceDetail = (params) => {
  return request({
    url: '/devices/detail',
    method: 'post',
    data: params
  })
}

/**
 * 更新设备信息
 * @param {Object} data - 设备信息
 * @param {number} data.id - 设备ID（必填）
 * @param {string} data.deviceName - 设备名称（可选）
 * @param {string} data.deviceCode - 设备编码（可选）
 * @param {number} data.productId - 产品ID（可选）
 * @param {number} data.groupId - 所属分组ID（可选）
 * @param {string} data.location - 位置/备注信息（可选）
 * @returns {Promise} 返回更新后的设备信息
 */
export const updateDevice = (data) => {
  return request({
    url: '/devices/update',
    method: 'post',
    data
  })
}

/**
 * 删除设备
 * @param {string} deviceCode - 设备编码
 * @returns {Promise} 返回删除结果
 */
export const deleteDevice = (deviceCode) => {
  return request({
    url: '/devices/delete',
    method: 'post',
    data: { deviceCode }
  })
}

/**
 * 更新设备在线状态
 * @param {string} deviceCode - 设备编码
 * @param {boolean} online - 在线状态 true/false
 * @returns {Promise} 返回更新结果
 */
export const updateDeviceStatus = (deviceCode, online) => {
  return request({
    url: `/devices/${deviceCode}/status`,
    method: 'post',
    params: { online }
  })
}

/**
 * 获取设备最新数据
 * @param {Object} params - 参数对象
 * @param {string} params.deviceCode - 设备编码
 * @returns {Promise} 返回设备最新上报的数据
 */
export const getDeviceLatestData = (params) => {
  return request({
    url: '/devices/latest-data',
    method: 'post',
    data: params
  })
}

/**
 * 批量导入设备
 * @param {Object} data - 导入数据
 * @param {Array} data.devices - 设备列表
 * @returns {Promise} 返回导入结果
 */
export const batchImportDevices = (data) => {
  return request({
    url: '/devices/batch-import',
    method: 'post',
    data
  })
}

/**
 * 检查设备编码是否存在
 * @param {Object} data - 检查数据
 * @param {Array} data.deviceCodes - 设备编码列表
 * @returns {Promise} 返回已存在的设备编码列表
 */
export const checkDeviceExists = (data) => {
  return request({
    url: '/devices/check-exists',
    method: 'post',
    data
  })
}

export default {
  getDeviceList,
  createDevice,
  getDeviceDetail,
  updateDevice,
  deleteDevice,
  updateDeviceStatus,
  getDeviceLatestData,
  batchImportDevices,
  checkDeviceExists
}
