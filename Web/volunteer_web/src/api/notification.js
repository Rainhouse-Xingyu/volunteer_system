import request from '@/utils/request'

export function getMyNotifications(current, size) {
  return request({
    url: '/volunteer/notifications',
    method: 'get',
    params: { current, size }
  })
}

export function markAsRead(id) {
  return request({
    url: `/volunteer/notifications/${id}/read`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: `/volunteer/notifications/read-all`,
    method: 'put'
  })
}
