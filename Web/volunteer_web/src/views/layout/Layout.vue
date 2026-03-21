<template>
  <el-container class="layout-container">
    <!-- Desktop Sidebar -->
    <el-aside v-if="!isMobile" :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon :size="24" color="#fff" style="margin-right: 10px"><Monitor /></el-icon>
        <span v-if="!isCollapse">志愿者管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical"
        background-color="#fff"
        text-color="#606266"
        active-text-color="#409EFF"
        :collapse="isCollapse"
        :router="true"
        :collapse-transition="false"
      >
        <el-menu-item 
          v-for="item in currentMenu" 
          :key="item.index" 
          :index="item.index"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- Mobile Drawer -->
    <el-drawer
      v-model="showMobileMenu"
      direction="ltr"
      :with-header="false"
      size="280px"
      class="mobile-menu-drawer"
    >
      <div class="logo mobile-logo">
        <el-icon :size="24" color="#409EFF" style="margin-right: 10px"><Monitor /></el-icon>
        <span>志愿者管理系统</span>
        <el-icon class="close-drawer-btn" @click="showMobileMenu = false"><Close /></el-icon>
      </div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical-mobile"
        text-color="#606266"
        active-text-color="#409EFF"
        :router="true"
      >
        <el-menu-item 
          v-for="item in currentMenu" 
          :key="item.index" 
          :index="item.index"
          @click="showMobileMenu = false"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <el-container>
      <!-- Header -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="trigger" @click="toggleCollapse">
            <template v-if="isMobile">
                <Expand />
            </template>
            <template v-else>
                <Expand v-if="isCollapse" />
                <Fold v-else />
            </template>
          </el-icon>
          
          <div v-if="isMobile" class="mobile-header-title">
             <el-icon :size="20" color="#409EFF" style="margin-right: 8px"><Monitor /></el-icon>
             <span style="font-weight: bold; color: #303133;">志愿者管理系统</span>
          </div>

          <el-breadcrumb separator="/" v-if="!isMobile">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <div class="user-info-box">
             <el-tag v-if="roleText" type="success" size="small" style="margin-right: 10px">{{ roleText }}</el-tag>
             <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ userStore.userInfo?.username || '用户' }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile" v-if="userStore.role !== 'ADMIN'">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
             <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { ElMessageBox } from 'element-plus';
import { 
  Monitor, Odometer, Calendar, Star, Briefcase, User, Expand, Fold, ArrowDown, Close,
  Search, Bell, Setting, Stamp, UserFilled, Reading, EditPen, Warning, Trophy
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const isCollapse = ref(false);
const isMobile = ref(false);
const showMobileMenu = ref(false);

const volunteerMenu = [
  { index: '/dashboard', icon: Odometer, title: '仪表盘' },
  { index: '/home', icon: Search, title: '志愿活动' },
  { index: '/my-activity', icon: Star, title: '我的活动' },
  { index: '/points', icon: Trophy, title: '积分明细' },
  { index: '/stories', icon: Reading, title: '志愿故事' },
  { index: '/message', icon: Bell, title: '消息中心' },
  { index: '/profile', icon: User, title: '个人中心' }
]

const organizerMenu = [
  { index: '/organizer-dashboard', icon: Odometer, title: '工作台' },
  { index: '/organizer', icon: Briefcase, title: '活动管理' },
  { index: '/news/publish', icon: EditPen, title: '资讯发布' },
  { index: '/message', icon: Bell, title: '消息通知' },
  { index: '/profile', icon: User, title: '组织信息' }
]

const adminMenu = [
  { index: '/statistics', icon: Odometer, title: '数据看板' },
  { index: '/admin/user-manage', icon: UserFilled, title: '用户管理' },
  { index: '/admin/audit', icon: Stamp, title: '活动审核' },
  { index: '/admin/violation', icon: Warning, title: '违规处理' },
  { index: '/message', icon: Bell, title: '系统公告' }
]

const currentMenu = computed(() => {
  const role = userStore.role
  if (role === 'ADMIN') return adminMenu
  if (role === 'ORGANIZER') return organizerMenu
  return volunteerMenu
})

const checkMobile = () => {
    try {
        const rect = document.body.getBoundingClientRect()
        isMobile.value = rect.width - 1 < 992
        if (isMobile.value) {
            isCollapse.value = true
        } else {
            showMobileMenu.value = false
        }
    } catch (e) {
        // Fallback or ignore
    }
}

onMounted(() => {
    checkMobile()
    window.addEventListener('resize', checkMobile)
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', checkMobile)
})

const roleText = computed(() => {
  const role = userStore.role;
  if (role === 'ADMIN') return '管理员';
  if (role === 'ORGANIZER') return '活动组织者';
  if (role === 'VOLUNTEER') return '志愿者';
  return role || '用户';
});

const toggleCollapse = () => {
    if (isMobile.value) {
        showMobileMenu.value = !showMobileMenu.value
    } else {
        isCollapse.value = !isCollapse.value;
    }
};

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm(
      '确认要退出登录吗?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
      .then(() => {
        userStore.logout();
        router.push('/login');
      })
      .catch(() => {});
  } else if (command === 'profile') {
    router.push('/my');
  }
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
}

.aside {
  background-color: #304156;
  color: #fff;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3649;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  overflow: hidden;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 220px;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.trigger {
  font-size: 20px;
  margin-right: 20px;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info-box {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  width: 100%;
}

@media screen and (max-width: 768px) {
  .header {
    background-color: #fff;
    border-bottom: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
    height: 50px;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);
  }

  .trigger {
    font-size: 20px;
    margin-right: 10px;
    cursor: pointer;
  }

  .main {
    padding: 10px;
  }
  
  .el-drawer__body {
     padding: 10px !important;
  }
}

/* Transition */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.mobile-menu-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.mobile-logo {
  height: 60px;
  line-height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  justify-content: space-between;
}

.close-drawer-btn {
    cursor: pointer;
    font-size: 20px;
}

.mobile-header-title {
    display: flex;
    align-items: center;
}

.el-menu-vertical-mobile {
    border-right: none;
}
</style>