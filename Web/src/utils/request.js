import axios from 'axios'
import { showToast } from 'vant'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 使用代理，指向后端
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 根据后端 Result 结构 { code, msg, data }
    if (res.code !== 200) {
      showToast(res.msg || '系统错误')
      
      // 401未登录处理
      if (res.code === 401) {
        // 重定向到登录页逻辑 (如果有路由)
        // location.href = '/login'
      }
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res
    }
  },
  error => {
    let msg = '网络错误'
    if (error.response) {
       // 处理 HTTP 状态码错误
       if (error.response.status === 401) {
           msg = '未授权，请登录'
       } else if (error.response.status === 500) {
           msg = '服务器内部错误'
       }
    }
    showToast(msg)
    return Promise.reject(error)
  }
)

export default service
