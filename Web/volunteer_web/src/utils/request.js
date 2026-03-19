import axios from 'axios';
import { showToast } from 'vant';

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 代理地址，需要在 vite.config.js 中配置
  timeout: 5000
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      showToast(res.msg || 'Error');
      // 可以处理 401 token 过期跳转登录
      if (res.code === 401) {
        // redirect to login
      }
      return Promise.reject(new Error(res.msg || 'Error'));
    } else {
      return res;
    }
  },
  error => {
    showToast(error.message || 'Request Error');
    return Promise.reject(error);
  }
);

export default service;
