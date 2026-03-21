import request from '@/utils/request'

export function getOverview() {
    return request({
        url: '/admin/statistics/overview',
        method: 'get'
    })
}

export function getChartsData() {
    return request({
        url: '/admin/statistics/charts',
        method: 'get'
    })
}