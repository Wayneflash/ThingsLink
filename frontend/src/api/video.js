import request from '../utils/request'

/**
 * 视频管理API
 */

/**
 * 查询视频设备列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getVideoList(params) {
  return request({
    url: '/api/video/list',
    method: 'post',
    data: params
  })
}

/**
 * 查询视频设备详情
 * @param {String} deviceId GB28181设备编码
 * @param {String} channelId GB28181通道编码
 * @returns {Promise}
 */
export function getVideoDetail(deviceId, channelId) {
  return request({
    url: `/api/video/detail/${deviceId}/${channelId}`,
    method: 'get'
  })
}

/**
 * 获取视频播放地址
 * @param {Object} params 播放参数
 * @returns {Promise}
 */
export function playVideo(params) {
  return request({
    url: '/api/video/play',
    method: 'post',
    data: params
  })
}

/**
 * 添加视频设备
 * @param {Object} data 设备信息
 * @returns {Promise}
 */
export function addVideoDevice(data) {
  return request({
    url: '/api/video/add',
    method: 'post',
    data
  })
}

/**
 * 编辑视频设备
 * @param {Object} data 设备信息
 * @returns {Promise}
 */
export function updateVideoDevice(data) {
  return request({
    url: '/api/video/update',
    method: 'post',
    data
  })
}

/**
 * 删除视频设备
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function deleteVideoDevice(id) {
  return request({
    url: `/api/video/delete/${id}`,
    method: 'delete'
  })
}
