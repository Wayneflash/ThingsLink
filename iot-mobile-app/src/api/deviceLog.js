import request from '@/utils/request'

/**
 * 设备日志 API
 */

/**
 * 分页查询设备日志
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.deviceCode - 设备编码（可选）
 * @param {string} params.logType - 日志类型 online/offline/command（可选）
 * @param {string} params.startTime - 开始时间 yyyy-MM-dd HH:mm:ss（可选）
 * @param {string} params.endTime - 结束时间 yyyy-MM-dd HH:mm:ss（可选）
 * @returns {Promise} 返回设备日志列表
 */
export const getDeviceLogList = (params) => {
  return request({
    url: '/device-logs/list',
    method: 'post',
    data: params
  })
}

export default {
  getDeviceLogList
}
