import request from '@/utils/request'

/**
 * 用户管理 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认20
 * @param {string} params.keyword - 搜索关键词（用户名/姓名）
 * @param {number} params.groupId - 分组ID筛选
 * @param {number} params.status - 状态筛选 (1:启用, 0:禁用）
 * @returns {Promise} 返回用户列表数据
 */
export const getUserList = (params) => {
  return request({
    url: '/users/list',
    method: 'post',
    data: params
  })
}

/**
 * 创建新用户
 * @param {Object} data - 用户信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.realname - 姓名
 * @param {number} data.groupId - 分组ID
 * @param {number} data.roleId - 角色ID
 * @param {string} data.phone - 手机号
 * @param {string} data.email - 邮箱
 * @param {number} data.status - 状态 (1:启用, 0:禁用）
 * @returns {Promise} 返回创建的用户信息
 */
export const createUser = (data) => {
  return request({
    url: '/users/create',
    method: 'post',
    data
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @param {number} data.id - 用户ID（必填）
 * @param {string} data.realname - 姓名
 * @param {string} data.phone - 手机号
 * @param {string} data.email - 邮箱
 * @param {number} data.groupId - 分组ID
 * @param {number} data.roleId - 角色ID
 * @param {number} data.status - 状态 (1:启用, 0:禁用）
 * @returns {Promise} 返回更新后的用户信息
 */
export const updateUser = (data) => {
  return request({
    url: '/users/update',
    method: 'post',
    data
  })
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise} 返回删除结果
 */
export const deleteUser = (id) => {
  return request({
    url: '/users/delete',
    method: 'post',
    data: { id }
  })
}

/**
 * 启用/禁用用户
 * @param {Object} data - 状态信息
 * @param {number} data.id - 用户ID
 * @param {number} data.status - 状态 (1:启用, 0:禁用）
 * @returns {Promise} 返回更新结果
 */
export const updateUserStatus = (data) => {
  return request({
    url: '/users/status',
    method: 'post',
    data
  })
}

export default {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  updateUserStatus
}
