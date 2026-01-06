import request from '@/utils/request'

/**
 * 认证授权 API
 * 所有接口使用 POST 方法，响应结构统一为 {code, data, message}
 */

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 返回token、用户信息和菜单权限
 */
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise} 返回登出结果
 */
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 刷新Token
 * @param {string} refreshToken - 刷新Token
 * @returns {Promise} 返回新的token
 */
export const refreshToken = (refreshToken) => {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 返回用户详细信息
 */
export const getCurrentUser = () => {
  return request({
    url: '/auth/current',
    method: 'post'
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise} 返回修改结果
 */
export const changePassword = (data) => {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

export default {
  login,
  logout,
  refreshToken,
  getCurrentUser,
  changePassword
}
