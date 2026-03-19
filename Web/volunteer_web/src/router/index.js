import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/Register.vue')
  },
  {
    path: '/forget',
    name: 'ForgetPassword',
    component: () => import('@/views/forget/ForgetPassword.vue')
  },
  {
    path: '/home',
    name: 'Home',
    // 临时占位，后续创建
    component: { template: '<div>Home Page</div>' } 
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
