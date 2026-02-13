import request from '@/utils/request'

/**
 * 数据管理 API
 */

/**
 * 获取设备最新数据
 * @param {string} deviceCode - 设备编码
 * @param {number} limit - 返回条数，默认10
 * @returns {Promise} 返回最新数据
 */
export const getLatestData = (deviceCode, limit = 10) => {
  return request({
    url: `/device-data/latest/${deviceCode}`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 分页查询设备历史数据
 * @param {Object} params - 查询参数
 * @param {string} params.deviceCode - 设备编码
 * @param {string} params.startTime - 开始时间 (yyyy-MM-dd HH:mm:ss)
 * @param {string} params.endTime - 结束时间 (yyyy-MM-dd HH:mm:ss)
 * @param {string} params.attrs - 属性标识符（多个用逗号分隔，可选）
 * @param {number} params.pageNum - 页码，默认 1
 * @param {number} params.pageSize - 每页条数，默认 100，最大 500
 * @returns {Promise<{list: Array, total: number}>}
 */
export const getHistoryData = (params) => {
  return request({
    url: '/device-data/list',
    method: 'post',
    data: params
  })
}

export default {
  getLatestData,
  getHistoryData
}
