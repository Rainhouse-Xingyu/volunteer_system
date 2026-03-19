import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/store/user'

const service = axios.create({
  baseURL: '/api', // Use relative path for proxy
  timeout: 10000 // 增加超时时间到10s
})

// Request interceptor
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error(error)
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    // Assuming backend returns code 200 for success
    if (res.code !== 200) {
      showToast(res.message || 'Error')
      
      // Handle 401 Unauthorized or Token Expired
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        location.reload()
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.error('err' + error)
    if (error.response) {
      if (error.response.status === 401) {
        showToast('登录过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        setTimeout(() => location.reload(), 1500)
        return Promise.reject(error)
      } else if (error.response.status === 404) {
        showToast('接口不存在 (404)')
        return Promise.reject(error)
      } else if (error.response.status === 500) {
        showToast('服务器内部错误 (500)')
        return Promise.reject(error)
      }
    }
    let msg = error.message || 'Error'
    if (msg === 'Network Error') {
        msg = '网络异常，请检查网络连接'
    } else if (msg.includes('timeout')) {
        msg = '请求超时，请重试'
    }
    showToast(msg)
    return Promise.reject(new Error(msg))
  }
)

export default service