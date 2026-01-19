import request from '@/utils/request'

/**
 * 获取当前用户个人资料
 */
export function getProfile() {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

/**
 * 更新当前用户个人资料
 */
export function updateProfile(data) {
  return request({
    url: '/users/profile',
    method: 'post',
    data
  })
}
