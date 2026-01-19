import request from '@/utils/request'

/**
 * 获取系统概览统计（与web端一致，使用/devices/statistics）
 */
export function getOverviewStats() {
  return request({
    url: '/devices/statistics',
    method: 'post',
    data: {}
  })
}

/**
 * 获取设备统计（用于饼图）
 */
export function getDeviceStats(params) {
  return request({
    url: '/statistics/devices',
    method: 'post',
    data: params || {}
  })
}

/**
 * 获取数据趋势（用于折线图）
 */
export function getDataTrend(params) {
  return request({
    url: '/statistics/data-trend',
    method: 'post',
    data: params || {}
  })
}

/**
 * 获取设备告警统计（与web端一致，使用/alarm-log/statistics）
 */
export function getAlarmStats(params) {
  return request({
    url: '/alarm-log/statistics',
    method: 'post',
    data: params || {}
  })
}
