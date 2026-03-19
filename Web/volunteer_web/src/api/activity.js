import request from '@/utils/request'

export function getActivityList(current, size, params = {}) {
  return request({
    url: '/activity/list',
    method: 'get',
    params: { current, size, ...params }
  })
}

export function getActivityDetail(id) {
  return request({
    url: `/activity/${id}`,
    method: 'get'
  })
}

export function getMyActivities(current, size, params = {}) {
  return request({
    url: '/activity/my-list',
    method: 'get',
    params: { current, size, ...params }
  })
}

export function getMyRegistrations(current, size, params = {}) {
  return request({
    url: '/volunteer/my-registrations',
    method: 'get',
    params: { current, size, ...params }
  })
}

export function createActivity(data) {
  return request({
    url: '/activity/create',
    method: 'post',
    data
  })
}

export function registerActivity(id) {
  return request({
    url: `/activity/register/${id}`,
    method: 'post'
  })
}

export function cancelRegistration(id) {
  return request({
    url: `/activity/cancel/${id}`,
    method: 'post'
  })
}

export function getRecommendedActivity() {
  return request({
    url: '/volunteer/recommendation',
    method: 'get'
  })
}

// For volunteer check-in: Pass entire QR data JSON
export function checkIn(data) {
  return request({
    url: '/volunteer/checkin',
    method: 'post',
    data: data
  })
}

// Volunteer: Mark as complete
export function completeActivity(regId) {
    return request({
        url: `/volunteer/complete/${regId}`,
        method: 'post'
    })
}

// Organizer: Generate Check-in Code
export function getCheckInCode(activityId) {
  return request({
    url: `/registration/checkin-code/${activityId}`,
    method: 'post'
  })
}

// Organizer: Get registration list for an activity
export function getRegistrationList(activityId, current, size) {
  return request({
    url: `/registration/list/${activityId}`,
    method: 'get',
    params: { current, size }
  })
}

// Organizer: Audit registration (1=approve, 2=reject)
export function auditRegistration(data) {
  return request({
    url: '/registration/audit',
    method: 'post',
    data
  })
}

// Admin: Audit Activity
export function auditActivity(data) {
  return request({
    url: '/admin/activity/audit',
    method: 'put',
    data
  })
}
