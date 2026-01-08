import request from '@/utils/request'

/**
 * 消息通知 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 分页查询通知列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 当前页码
 * @param {number} params.pageSize - 每页大小
 * @param {number} params.isRead - 是否已读：0-未读，1-已读，null-全部
 * @returns {Promise} 返回通知列表
 */
export const getNotificationList = (params) => {
  return request({
    url: '/notification/list',
    method: 'post',
    data: params
  })
}

/**
 * 获取未读通知数量
 * @returns {Promise} 返回未读通知数量
 */
export const getUnreadCount = () => {
  return request({
    url: '/notification/unread-count',
    method: 'post'
  })
}

/**
 * 标记通知为已读
 * @param {Object} params - 参数
 * @param {number} params.notificationId - 通知ID（单个）
 * @param {Array<number>} params.notificationIds - 通知ID列表（批量）
 * @returns {Promise} 返回标记结果
 */
export const markAsRead = (params) => {
  return request({
    url: '/notification/mark-read',
    method: 'post',
    data: params
  })
}

/**
 * 标记所有通知为已读
 * @returns {Promise} 返回标记结果
 */
export const markAllAsRead = () => {
  return request({
    url: '/notification/mark-all-read',
    method: 'post'
  })
}

export default {
  getNotificationList,
  getUnreadCount,
  markAsRead,
  markAllAsRead
}
