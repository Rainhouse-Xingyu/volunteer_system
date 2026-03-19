<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <i class="fas fa-hands-helping"></i>
        </div>
        <div class="sidebar-title">志愿者管理系统</div>
      </div>
      <nav class="sidebar-nav">
        <div class="nav-item" :class="{ active: route.path === '/home' }" @click="router.push('/home')">
          <i class="fas fa-home"></i> 系统首页
        </div>
        <div class="nav-item" :class="{ active: route.path === '/statistics' }" @click="router.push('/statistics')">
          <i class="fas fa-chart-bar"></i> 数据统计
        </div>
        
        <div class="nav-header">活动管理</div>
        <div class="nav-item" :class="{ active: route.path === '/activity/create' }" @click="router.push('/activity/create')">
          <i class="fas fa-plus-circle"></i> 发布活动
        </div>
        <div class="nav-item" :class="{ active: route.path === '/my-activity' }" @click="router.push('/my-activity')">
          <i class="fas fa-list-alt"></i> 我的活动
        </div>

        <div class="nav-header">个人中心</div>
        <div class="nav-item" :class="{ active: route.path === '/my' }" @click="router.push('/my')">
          <i class="fas fa-user-circle"></i> 个人资料
        </div>
        <div class="nav-item" :class="{ active: route.path === '/message' }" @click="router.push('/message')">
          <i class="fas fa-comment-dots"></i> 消息通知
        </div>
        
        <template v-if="userStore.role === 'ADMIN'">
          <div class="nav-header">管理员功能</div>
          <div class="nav-item" :class="{ active: route.path === '/admin/audit' }" @click="router.push('/admin/audit')">
            <i class="fas fa-clipboard-check"></i> 活动审核
          </div>
        </template>
      </nav>
      <div class="sidebar-footer">
        <div class="nav-item" @click="handleLogout">
          <i class="fas fa-sign-out-alt"></i> 退出登录
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <div class="page-title">
          <h1>{{ route.meta.title || '志愿者系统' }}</h1>
          <p>欢迎回来，{{ userStore.userInfo?.username || '志愿者' }}</p>
        </div>
        <div class="user-info">
          <div class="user-details">
            <div class="user-name">{{ userStore.userInfo?.username || '未登录' }}</div>
            <div class="user-role">{{ roleText }}</div>
          </div>
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="page-content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { showConfirmDialog } from 'vant';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const roleText = computed(() => {
  const role = userStore.role;
  if (role === 'ADMIN') return '管理员';
  if (role === 'ORGANIZER') return '活动组织者';
  return '志愿者';
});

const handleLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确认退出登录吗？',
  }).then(() => {
    userStore.logout();
    router.push('/login');
  }).catch(() => {});
};
</script>

<style scoped>
/* 侧边栏样式 */
.layout-container {
  display: flex;
  min-height: 100vh;
  background-color: #f8f9fc;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  position: fixed;
  height: 100vh;
  display: flex;
  flex-direction: column;
  z-index: 100;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-logo {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.nav-header {
  padding: 10px 20px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
  margin-top: 10px;
}

.nav-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.nav-item:hover, .nav-item.active {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border-left-color: #fff;
}

.nav-item i {
  width: 20px;
  text-align: center;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.top-bar {
  height: 80px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  flex-shrink: 0;
}

.page-title h1 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: #333;
}

.page-title p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-details {
  text-align: right;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.user-role {
  font-size: 12px;
  color: #999;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  font-size: 18px;
  border: 2px solid #667eea;
}

.page-content-wrapper {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background: #f8f9fc;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>