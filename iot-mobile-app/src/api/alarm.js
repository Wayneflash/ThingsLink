import request from '@/utils/request'

/**
 * 抧警配置 API
 * 所有接口使用 POST 方法（除部分 GET 接口外），响应结构统一为 {code, data, message}
 */

/**
 * 配置设备抩警阈值（单个或批量）
 * @param {Object} data - 抩警配置数据
 * @param {number} data.deviceId - 设备ID（单个配置时使用）
 * @param {Array<number>} data.deviceIds - 设备ID列表（批量配置时使用）
 * @param {Object} data.alarmConfig - 抩警配置对象
 * @param {number} data.alarmConfig.notifyUser - 处理人ID（全局配置）
 * @param {boolean} data.alarmConfig.stackMode - 抩警堆叠：true=开启，false=关闭（全局配置）
 * @param {Object} data.alarmConfig.metrics - 物模型属性监控配置（Map结构）
 * @param {boolean} data.alarmConfig.metrics[metric].enabled - 是否启用监控
 * @param {string} data.alarmConfig.metrics[metric].operator - 运算符：> < =
 * @param {number} data.alarmConfig.metrics[metric].threshold - 阈值（支持小数）
 * @param {string} data.alarmConfig.metrics[metric].level - 抩警级别：critical(严重), warning(警告), info(提示)
 * @param {boolean} data.enabled - 是否启用抩警
 * @returns {Promise} 返回配置结果
 */
export const configureAlarm = (data) => {
  return request({
    url: '/device-alarm/configure',
    method: 'post',
    data
  })
}

/**
 * 获取设备抩警配置
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 返回抩警配置
 */
export const getAlarmConfig = (deviceId) => {
  return request({
    url: `/device-alarm/config/${deviceId}`,
    method: 'get'
  })
}

/**
 * 切换设备告警启用状态
 * @param {number} deviceId - 设备ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise} 返回切换结果
 */
export const toggleAlarmEnabled = (deviceId, enabled) => {
  return request({
    url: `/device-alarm/toggle/${deviceId}`,
    method: 'post',
    params: { enabled }
  })
}

/**
 * 分页查询告警日志
 * @param {Object} params - 查询参数
 * @param {number} params.page - 当前页码
 * @param {number} params.pageSize - 每页大小
 * @param {string} params.deviceCode - 设备编码（可选）
 * @param {string} params.alarmLevel - 告警级别（可选）
 * @param {number} params.status - 处理状态：0-未处理，1-已处理（可选）
 * @param {string} params.startTime - 开始时间（可选）
 * @param {string} params.endTime - 结束时间（可选）
 * @returns {Promise} 返回告警日志列表
 */
export const getAlarmLogList = (params) => {
  return request({
    url: '/alarm-log/list',
    method: 'post',
    data: params
  })
}

/**
 * 处理报警
 * @param {Object} params - 处理参数
 * @param {number} params.alarmId - 报警ID
 * @param {string} params.handleDescription - 处理描述
 * @param {Array} params.handleImages - 处理图片列表
 * @returns {Promise} 返回处理结果
 */
export const handleAlarm = (params) => {
  return request({
    url: '/alarm-log/handle',
    method: 'post',
    data: params
  })
}

/**
 * 获取未处理告警数量
 * @returns {Promise} 返回未处理告警数量
 */
export const getUnhandledCount = () => {
  return request({
    url: '/alarm-log/unhandled-count',
    method: 'post'
  })
}

/**
 * 获取告警统计信息
 * @returns {Promise} 返回告警统计数据
 */
export const getAlarmStatistics = () => {
  return request({
    url: '/alarm-log/statistics',
    method: 'post'
  })
}

/**
 * 获取报警分析数据
 * @param {Object} params - 查询参数
 * @param {Array<string>} params.deviceCodes - 设备编码列表
 * @param {string} params.timeRange - 时间范围：today/yesterday/7days/30days/custom
 * @param {string} params.startTime - 自定义开始时间（yyyy-MM-dd HH:mm:ss）
 * @param {string} params.endTime - 自定义结束时间（yyyy-MM-dd HH:mm:ss）
 * @returns {Promise} 返回报警分析数据
 */
export const getAlarmAnalysis = (params) => {
  return request({
    url: '/alarm-log/analysis',
    method: 'post',
    data: params
  })
}

export default {
  configureAlarm,
  getAlarmConfig,
  toggleAlarmEnabled,
  getAlarmLogList,
  handleAlarm,
  getUnhandledCount,
  getAlarmStatistics,
  getAlarmAnalysis
}

