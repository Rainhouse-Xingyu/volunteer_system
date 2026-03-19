import request from '@/utils/request'

export function publishNews(data) {
  return request({
    url: '/news/publish',
    method: 'post',
    data
  })
}

export function getNewsList(params) {
    return request({
        url: '/news/list',
        method: 'get',
        params
    })
}

export function getMyNewsList(params) {
    return request({
        url: '/news/my-list',
        method: 'get',
        params
    })
}

export function deleteNews(id) {
    return request({
        url: `/news/delete/${id}`,
        method: 'post'
    })
}
