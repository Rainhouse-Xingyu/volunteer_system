import request from '@/utils/request'

export function getViolationList(current, size, keyword) {
  return request({
    url: '/violation/list',
    method: 'get',
    params: { current, size, keyword }
  })
}

export function processViolation(data) {
  return request({
    url: '/violation/process',
    method: 'put',
    data // { violationId: 1, reason: "..." }
  })
}

export function submitViolation(data) {
  return request({
    url: '/violation/report',
    method: 'post',
    data
  })
}

// 举报相关 API
export function getReportList() {
  return request({
    url: '/comments/reports',
    method: 'get'
  })
}

export function processReport(reportId, status) {
  return request({
    url: `/comments/reports/${reportId}/process`,
    method: 'put',
    params: { status }
  })
}