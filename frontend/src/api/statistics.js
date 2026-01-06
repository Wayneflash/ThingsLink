import request from '@/utils/request'

/**
 * 获取系统概览统计
 */
export function getOverviewStats() {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

/**
 * 获取设备统计（用于饼图）
 */
export function getDeviceStats(params) {
  return request({
    url: '/statistics/devices',
    method: 'get',
    params
  })
}

/**
 * 获取数据趋势（用于折线图）
 */
export function getDataTrend(params) {
  return request({
    url: '/statistics/data-trend',
    method: 'get',
    params
  })
}

/**
 * 获取设备告警统计
 */
export function getAlarmStats(params) {
  return request({
    url: '/statistics/device-alarms',
    method: 'get',
    params
  })
}
