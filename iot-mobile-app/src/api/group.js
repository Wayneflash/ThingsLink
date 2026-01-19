import request from '@/utils/request'

/**
 * 设备分组 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 获取设备分组树形结构
 * @returns {Promise} 返回分组树数据
 */
export const getGroupTree = () => {
  return request({
    url: '/device-groups/tree',
    method: 'post'
  })
}

/**
 * 获取设备分组平铺列表
 * @returns {Promise} 返回分组列表
 */
export const getGroupList = () => {
  return request({
    url: '/device-groups/list',
    method: 'post'
  })
}

/**
 * 创建设备分组
 * @param {Object} data - 分组信息
 * @param {string} data.name - 分组名称 (或 groupName)
 * @param {number} data.parentId - 父分组ID，默认为0
 * @param {string} data.description - 描述
 * @param {number} data.sort - 排序值
 * @param {string} data.groupType - 分组类型
 * @returns {Promise} 返回创建的分组信息
 */
export const createGroup = (data) => {
  return request({
    url: '/device-groups/create',
    method: 'post',
    data
  })
}

/**
 * 更新设备分组
 * @param {Object} data - 分组信息
 * @param {number} data.id - 分组ID（必填）
 * @param {string} data.groupName - 分组名称
 * @param {number} data.parentId - 父分组ID
 * @param {string} data.description - 描述
 * @param {number} data.sort - 排序值
 * @returns {Promise} 返回更新后的分组信息
 */
export const updateGroup = (data) => {
  return request({
    url: '/device-groups/update',
    method: 'post',
    data
  })
}

/**
 * 删除设备分组
 * @param {Object} data - 包含id的对象
 * @returns {Promise} 返回删除结果
 */
export const deleteGroup = (data) => {
  return request({
    url: '/device-groups/delete',
    method: 'post',
    data
  })
}

export default {
  getGroupTree,
  getGroupList,
  createGroup,
  updateGroup,
  deleteGroup
}
