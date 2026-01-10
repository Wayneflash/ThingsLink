import request from '@/utils/request'

/**
 * 获取系统配置
 */
export function getSystemConfig() {
  return request({
    url: '/system/config',
    method: 'get'
  })
}

/**
 * 获取设备连接MQTT配置（从application.yml读取，只读）
 */
export function getDeviceMqttConfig() {
  return request({
    url: '/system/mqtt-config',
    method: 'get'
  })
}

/**
 * 获取MQTT配置（兼容旧接口名）
 */
export function getMqttConfig() {
  return request({
    url: '/system/mqtt-config',
    method: 'get'
  })
}

/**
 * 获取平台连接MQTT配置（从数据库读取，可编辑）
 */
export function getPlatformMqttConfig() {
  return request({
    url: '/system/platform-mqtt-config',
    method: 'get'
  })
}

/**
 * 更新设备MQTT配置（仅admin可访问，保存到数据库，仅用于显示）
 */
export function updateDeviceMqttConfig(data) {
  return request({
    url: '/system/device-mqtt-config',
    method: 'post',
    data
  })
}

/**
 * 更新平台MQTT配置（仅admin可访问，保存到数据库，支持动态重新连接）
 */
export function updatePlatformMqttConfig(data) {
  return request({
    url: '/system/platform-mqtt-config',
    method: 'post',
    data
  })
}
