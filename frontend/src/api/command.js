import request from '@/utils/request'

/**
 * 命令控制 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 向设备下发命令（异步，不等待设备响应）
 * @param {Object} data - 命令信息
 * @param {string} data.deviceCode - 设备编码
 * @param {Array} data.commands - 命令列表
 * @returns {Promise} 返回下发结果
 */
export const sendCommand = (data) => {
  return request({
    url: '/commands/send',
    method: 'post',
    data
  })
}

/**
 * 查询产品命令列表
 * @param {Object} params - 查询参数
 * @param {number} params.productId - 产品ID
 * @returns {Promise} 返回命令列表
 */
export const getProductCommands = (params) => {
  return request({
    url: '/commands/product',
    method: 'post',
    data: params
  })
}

export default {
  sendCommand,
  getProductCommands
}
