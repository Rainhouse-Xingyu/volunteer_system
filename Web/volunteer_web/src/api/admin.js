import request from '@/utils/request'

// 获取所有活动列表 (包括待审核)
// 临时使用公共搜索接口，传入 status
export function getActivityList(current, size, keyword, status) {
  return request({
    url: '/activity/list',
    method: 'get',
    params: { current, size, keyword, status }
  })
}

// 建议后端 AdminController 增加专属接口，为了鉴权
// export function getAdminActivityList(current, size, status) {
//   return request({
//     url: '/admin/activities',
//     method: 'get',
//     params: { current, size, status }
//   })
// }

// 审核活动
// result: 1=通过, 4=驳回
export function auditActivity(data) {
  return request({
    url: '/admin/activity/audit',
    method: 'put',
    data
  })
}

// 获取所有用户
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 更新用户状态
export function updateUserStatus(userId, status) {
  return request({
    url: '/admin/user/status',
    method: 'put',
    data: { userId, status }
  })
}

// 获取用户信息修改审核列表
export function getUserAuditList(params) {
  return request({
    url: '/admin/audit/list',
    method: 'get',
    params
  })
}

// 审核用户信息修改
export function auditUserUpdate(id, status, reason) {
  return request({
    url: `/admin/audit/${id}`,
    method: 'post',
    data: { status, reason }
  })
}

// 导出活动报表
export function exportActivityReport(activityId) {
  return request({
    url: `/admin/export/${activityId}`,
    method: 'get',
    responseType: 'blob'
  })
}
