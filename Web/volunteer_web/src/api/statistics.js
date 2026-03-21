import request from '@/utils/request'

export function getOverview() {
    return request({
        url: '/admin/statistics/overview',
        method: 'get'
    })
}