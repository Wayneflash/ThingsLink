import request from '@/utils/request'

/**
 * 角色管理 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 获取角色列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认20
 * @returns {Promise} 返回角色列表数据
 */
export const getRoleList = (params) => {
  return request({
    url: '/roles/list',
    method: 'post',
    data: params
  })
}

/**
 * 获取角色详情
 * @param {number} id - 角色ID
 * @returns {Promise} 返回角色详细信息
 */
export const getRoleDetail = (id) => {
  return request({
    url: '/roles/detail',
    method: 'post',
    data: { id }
  })
}

/**
 * 创建角色
 * @param {Object} data - 角色信息
 * @param {string} data.name - 角色名称
 * @param {string} data.code - 角色编码
 * @param {string} data.description - 描述
 * @param {Array} data.permissions - 权限列表
 * @returns {Promise} 返回创建的角色信息
 */
export const createRole = (data) => {
  return request({
    url: '/roles/create',
    method: 'post',
    data
  })
}

/**
 * 更新角色信息
 * @param {Object} data - 角色信息
 * @param {number} data.id - 角色ID（必填）
 * @param {string} data.name - 角色名称
 * @param {string} data.description - 描述
 * @param {Array} data.permissions - 权限列表
 * @returns {Promise} 返回更新结果
 */
export const updateRole = (data) => {
  return request({
    url: '/roles/update',
    method: 'post',
    data
  })
}

/**
 * 删除角色
 * @param {number} id - 角色ID
 * @returns {Promise} 返回删除结果
 */
export const deleteRole = (id) => {
  return request({
    url: '/roles/delete',
    method: 'post',
    data: { id }
  })
}

export default {
  getRoleList,
  getRoleDetail,
  createRole,
  updateRole,
  deleteRole
}
