import request from '@/utils/request'

/**
 * 告警配置 API
 * 所有接口使用 POST 方法（除部分 GET 接口外），响应结构统一为 {code, data, message}
 */

/**
 * 配置设备告警阈值（单个或批量）
 * @param {Object} data - 告警配置数据
 * @param {number} data.deviceId - 设备ID（单个配置时使用）
 * @param {Array<number>} data.deviceIds - 设备ID列表（批量配置时使用）
 * @param {Object} data.alarmConfig - 告警配置对象
 * @param {string} data.alarmConfig.level - 告警级别：critical(严重), warning(警告), info(提示)
 * @param {Array<Object>} data.alarmConfig.conditions - 监控条件列表
 * @param {string} data.alarmConfig.conditions[].metric - 监控指标（物模型属性标识符）
 * @param {string} data.alarmConfig.conditions[].operator - 运算符：> < =
 * @param {number} data.alarmConfig.conditions[].threshold - 阈值（支持小数）
 * @param {Array<number>} data.alarmConfig.notifyUsers - 通知人员ID列表
 * @param {boolean} data.alarmConfig.stackMode - 告警堆叠：true=开启，false=关闭
 * @param {boolean} data.enabled - 是否启用告警
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
 * 获取设备告警配置
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 返回告警配置
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

export default {
  configureAlarm,
  getAlarmConfig,
  toggleAlarmEnabled
}

