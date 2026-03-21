import request from '@/utils/request'

// 修改密码
export function changePassword(data) {
  return request({
    url: '/auth/password',
    method: 'post',
    data
  })
}

// 获取志愿者资料
export function getVolunteerProfile() {
  return request({
    url: '/volunteer/me',
    method: 'get'
  })
}

// 更新志愿者资料
export function updateVolunteerProfile(data) {
  return request({
    url: '/volunteer/update',
    method: 'post',
    data
  })
}

// 获取组织者资料
export function getOrganizerProfile() {
  return request({
    url: '/organizer/me',
    method: 'get'
  })
}

// 更新组织者资料
export function updateOrganizerProfile(data) {
  return request({
    url: '/organizer/update',
    method: 'post',
    data
  })
}

// 获取志愿者统计数据
export function getVolunteerStats() {
    return request({
        url: '/volunteer/stats',
        method: 'get'
    })
}

// 获取积分明细
export function getPointsHistory(current, size) {
    return request({
        url: '/volunteer/points/history',
        method: 'get',
        params: { current, size }
    })
}

// 获取积分排名
export function getPointsRank() {
    return request({
        url: '/volunteer/points/rank',
        method: 'get'
    })
}
