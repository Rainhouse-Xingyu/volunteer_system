import request from '@/utils/request'

// 获取活动的所有评论
export function getActivityComments(activityId) {
  return request({
    url: `/comments/activity/${activityId}`,
    method: 'get'
  })
}

// 志愿者发表评论
export function postComment(data) {
  return request({
    url: '/comments/add',
    method: 'post',
    data
  })
}

// 志愿者查看自己的评价历史
export function getMyComments() {
  return request({
    url: '/comments/volunteer/my-comments',
    method: 'get'
  })
}
