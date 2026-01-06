import request from '@/utils/request'

/**
 * 系统配置 API
 */

/**
 * 获取系统配置
 * @returns {Promise} 返回系统配置信息
 */
export const getSystemConfig = () => {
  return request({
    url: '/system/config',
    method: 'get'
  })
}

/**
 * 获取MQTT配置
 * @returns {Promise} 返回MQTT配置信息
 */
export const getMqttConfig = () => {
  return request({
    url: '/system/mqtt-config',
    method: 'get'
  })
}

export default {
  getSystemConfig,
  getMqttConfig
}