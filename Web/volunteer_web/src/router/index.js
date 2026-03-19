// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
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
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', requiresAuth: true }
      },
      {
        path: 'home',
        name: 'Opportunities',
        component: () => import('@/views/Home.vue'),
        meta: { title: '志愿活动', requiresAuth: true }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { requiresAuth: true, title: '数据统计' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/message/Message.vue'),
        meta: { title: '消息通知', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Me.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'organizer',
        name: 'OrganizerWorkspace',
        component: () => import('@/views/activity/manage/OrganizerWorkspace.vue'),
        meta: { requiresAuth: true, title: '组织者工作台' }
      },
      {
        path: 'activity/create',
        name: 'ActivityCreate',
        component: () => import('@/views/activity/Create.vue'),
        meta: { requiresAuth: true, title: '发布活动' }
      },
      {
        path: 'activity/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/activity/Detail.vue'),
        meta: { requiresAuth: true, title: '活动详情' }
      },
      {
        path: 'activity/manage/:id',
        name: 'ActivityManage',
        component: () => import('@/views/activity/manage/Manage.vue'),
        meta: { requiresAuth: true, title: '活动管理' }
      },
      {
        path: 'user/edit',
        name: 'UserEdit',
        component: () => import('@/views/user/Edit.vue'),
        meta: { requiresAuth: true, title: '编辑资料' }
      },
      {
        path: 'user/password',
        name: 'UserPassword',
        component: () => import('@/views/user/Password.vue'),
        meta: { requiresAuth: true, title: '修改密码' }
      },
      {
        path: 'user/reviews',
        name: 'UserReviews',
        component: () => import('@/views/user/MyReviews.vue'),
        meta: { requiresAuth: true, title: '我的评价' }
      },
      {
        path: 'my-activity',
        name: 'MyActivity',
        component: () => import('@/views/activity/MyActivity.vue'),
        meta: { requiresAuth: true, title: '我的活动' }
      },
      {
        path: 'stories',
        name: 'Stories',
        component: () => import('@/views/story/StoryList.vue'),
        meta: { requiresAuth: true, title: '志愿故事' }
      },
      {
        path: 'news/publish',
        name: 'NewsPublish',
        component: () => import('@/views/news/Publish.vue'),
        meta: { requiresAuth: true, title: '发布资讯', role: 'ORGANIZER' }
      },
      {
        path: 'admin/audit',
        name: 'AdminActivityAudit',
        component: () => import('@/views/admin/ActivityAudit.vue'),
        meta: { requiresAuth: true, title: '活动审核', role: 'ADMIN' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    return '/login'
  }
})

export default router