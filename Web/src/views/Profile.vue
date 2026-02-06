<template>
  <div class="profile-container">
    <!-- 1. 顶部用户信息面板 -->
    <div class="user-header">
      <div class="user-info">
        <van-image
          round
          width="64px"
          height="64px"
          :src="userInfo.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
          class="avatar"
        />
        <div class="info-text">
          <div class="name-row">
            <span class="nickname">{{ userInfo.realName || userInfo.username || '志愿者' }}</span>
            <van-tag round type="warning" size="medium" class="level-tag">
              Lv.{{ getUserLevel(userInfo.points) }}
            </van-tag>
          </div>
          <div class="id-row">
            学号: {{ userInfo.studentId || '未填写' }}
          </div>
        </div>
      </div>
      
      <!-- 积分信用展示 -->
      <div class="assets-row">
        <div class="asset-item">
          <span class="num">{{ userInfo.points || 0 }}</span>
          <span class="label">当前积分</span>
        </div>
        <div class="asset-item">
          <span class="num">{{ userInfo.creditScore || 100 }}</span>
          <span class="label">信用分</span>
        </div>
        <div class="asset-item">
          <span class="num">--</span>
          <span class="label">荣誉时长</span>
        </div>
      </div>
    </div>

    <!-- 2. 数据概览 Grid -->
    <van-grid :column-num="3" class="data-grid" clickable>
      <van-grid-item text="累计参加" @click="toHistory">
        <template #icon>
          <span class="grid-num">{{ statistics.completedCount || 0 }}</span>
        </template>
      </van-grid-item>
      <van-grid-item text="报名审核中" @click="toHistory"> 
        <template #icon>
          <span class="grid-num">{{ statistics.pendingCount || 0 }}</span>
        </template>
      </van-grid-item>
      <van-grid-item text="未读通知" @click="toNotifications">
        <template #icon>
          <van-badge :content="statistics.unreadNotice > 0 ? statistics.unreadNotice : null">
             <span class="grid-num">{{ statistics.unreadNotice || 0 }}</span>
          </van-badge>
        </template>
      </van-grid-item>
    </van-grid>

    <!-- 3. 功能列表 -->
    <div class="menu-group">
      <van-cell-group inset>
        <van-cell title="我的报名记录" is-link to="/my-registrations">
          <template #icon>
            <van-icon name="records" class="menu-icon" color="#1989fa" />
          </template>
        </van-cell>
        
        <van-cell title="我的通知" is-link to="/notifications">
          <template #icon>
            <van-icon name="volume-o" class="menu-icon" color="#ee0a24" />
          </template>
          <template #value>
             <van-badge :content="statistics.unreadNotice" v-if="statistics.unreadNotice > 0" />
          </template>
        </van-cell>

        <van-cell title="积分排行榜" is-link to="/leaderboard">
          <template #icon>
            <van-icon name="chart-trending-o" class="menu-icon" color="#ff976a" />
          </template>
        </van-cell>

        <van-cell title="评价历史" is-link to="/evaluations">
          <template #icon>
            <van-icon name="star-o" class="menu-icon" color="#ffc107" />
          </template>
        </van-cell>

        <van-cell title="系统设置" is-link>
          <template #icon>
            <van-icon name="setting-o" class="menu-icon" color="#323233" />
          </template>
        </van-cell>
      </van-cell-group>

      <div class="logout-btn">
        <van-button block round type="default" @click="handleLogout">
          退出登录
        </van-button>
      </div>
    </div>

    <!-- 底部导航占位 (如果 App 有 Tabbar 的话) -->
    <div style="height: 50px;"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const userInfo = ref({});
const statistics = ref({
  completedCount: 0,
  pendingCount: 0,
  unreadNotice: 0
});

// 计算用户等级 (简单示例: 每100分一级)
const getUserLevel = (points) => {
  if (!points) return 1;
  return Math.floor(points / 100) + 1;
};

// 获取个人资料
const fetchProfile = async () => {
  try {
    // 1. 获取基本信息 /api/volunteer/me (返回 entity.VolunteerProfile)
    // 注意：VolunteerProfile 里通常包含 realName, phone, studentId
    // 如果后端做了 LoginInterceptor 自动注入 currentUser，这里获取的是 Profile 表信息
    const res = await request.get('/volunteer/me');
    if (res.data) {
      userInfo.value = res.data;
    }
  } catch (error) {
    console.error('获取个人资料失败', error);
  }
};

// 获取统计数据 (模拟接口，或者需要后端提供 dashboard 接口)
// 目前后端暂无聚合接口，这里暂时留空或 mock，待后续完善 "fetchStatistics"
const fetchStatistics = async () => {
    // 模拟数据展示 UI 效果
    // 实际应调用 /api/volunteer/statistics
    statistics.value = {
        completedCount: 5,
        pendingCount: 1,
        unreadNotice: 2
    };
};

const handleLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出登录吗？',
  })
    .then(() => {
      localStorage.removeItem('token');
      router.replace('/login');
    })
    .catch(() => {
      // cancel
    });
};

const toHistory = () => router.push('/my-registrations');
const toNotifications = () => router.push('/notifications');

onMounted(() => {
  fetchProfile();
  fetchStatistics();
});
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: #f7f8fa;
}

/* 顶部蓝色背景区 */
.user-header {
  background: linear-gradient(to right, #4facfe, #00f2fe); 
  /* 或者 Vant 默认蓝 #1989fa */
  background-color: #1989fa;
  padding: 30px 20px;
  color: #fff;
  border-bottom-left-radius: 20px;
  border-bottom-right-radius: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
}

.avatar {
  border: 2px solid rgba(255, 255, 255, 0.5);
  margin-right: 15px;
}

.info-text {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.nickname {
  font-size: 18px;
  font-weight: bold;
  margin-right: 8px;
}

.level-tag {
  font-style: italic;
}

.id-row {
  font-size: 12px;
  opacity: 0.8;
}

.assets-row {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.asset-item {
  display: flex;
  flex-direction: column;
}

.asset-item .num {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.asset-item .label {
  font-size: 12px;
  opacity: 0.8;
}

/* Grid 调整 */
.data-grid {
  margin-top: -20px; /* 向上重叠一点 */
  padding: 0 12px;
  background: transparent;
}

:deep(.van-grid-item__content) {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.grid-num {
  font-size: 20px;
  font-weight: bold;
  color: #323233;
}

/* 菜单列表 */
.menu-group {
  margin-top: 15px;
}

.menu-icon {
  font-size: 18px;
  margin-right: 8px;
}

.logout-btn {
  margin: 30px 16px;
}
</style>
