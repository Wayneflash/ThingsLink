import request from '@/utils/request'

/**
 * 能源统计 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 获取能耗统计
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间（格式：yyyy-MM-dd HH:mm:ss）
 * @param {string} params.endTime - 结束时间（格式：yyyy-MM-dd HH:mm:ss）
 * @param {number} params.deviceId - 设备ID（可选）
 * @param {number} params.groupId - 分组ID（可选）
 * @param {string} params.energyType - 能源类型（electric/water/gas）
 * @returns {Promise} 返回能耗统计数据
 */
export const getEnergyStatistics = (params) => {
  return request({
    url: '/energy/statistics',
    method: 'post',
    data: params
  })
}

/**
 * 获取能耗趋势
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间（格式：yyyy-MM-dd HH:mm:ss）
 * @param {string} params.endTime - 结束时间（格式：yyyy-MM-dd HH:mm:ss）
 * @param {number} params.deviceId - 设备ID（可选）
 * @param {string} params.energyType - 能源类型（electric/water/gas）
 * @param {string} params.dateFormat - 时间格式（day/hour，可选）
 * @returns {Promise} 返回能耗趋势数据
 */
export const getEnergyTrend = (params) => {
  return request({
    url: '/energy/trend',
    method: 'post',
    data: params
  })
}

/**
 * 获取能耗报表
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间（格式：yyyy-MM-dd HH:mm:ss）
 * @param {string} params.endTime - 结束时间（格式：yyyy-MM-dd HH:mm:ss）
 * @param {number} params.deviceId - 设备ID（可选）
 * @param {number} params.groupId - 分组ID（可选）
 * @param {string} params.energyType - 能源类型（electric/water/gas）
 * @param {string} params.reportType - 报表类型（daily/monthly/yearly，可选）
 * @param {number} params.page - 页码（可选，默认1）
 * @param {number} params.pageSize - 每页数量（可选，默认10）
 * @returns {Promise} 返回能耗报表数据
 */
export const getEnergyReport = (params) => {
  return request({
    url: '/energy/report',
    method: 'post',
    data: params
  })
}

/**
 * 获取实时能耗（Dashboard用）
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围（today/week/month，可选）
 * @returns {Promise} 返回实时能耗数据
 */
export const getRealtimeEnergy = (params) => {
  return request({
    url: '/energy/realtime',
    method: 'post',
    data: params
  })
}
