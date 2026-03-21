<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人中心</h2>
      <p class="subtitle">管理您的个人信息和偏好设置。</p>
    </div>

    <div class="profile-layout">
      <!-- Left Column: Info -->
      <div class="left-column">
        <el-card shadow="hover" class="info-card">
          <div class="card-header-row">
            <h3>个人信息</h3>
            <el-button icon="Edit" plain @click="toEdit">编辑</el-button>
          </div>
          
          <el-form label-position="top" class="info-form">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item :label="userStore.role === 'ORGANIZER' ? '组织名称' : '姓名'">
                  <el-input v-model="userInfo.realName" disabled placeholder="未填写" prefix-icon="User" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="电子邮箱/账号">
              <el-input v-model="userInfo.email" disabled prefix-icon="Message" placeholder="未绑定" />
            </el-form-item>

            <el-form-item label="手机号码">
              <el-input v-model="userInfo.phone" disabled prefix-icon="Phone" placeholder="未绑定" />
            </el-form-item>

            <el-form-item label="学号" v-if="userStore.role === 'VOLUNTEER'">
              <el-input v-model="userInfo.studentId" disabled prefix-icon="House" placeholder="未验证" />
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input type="textarea" v-model="userInfo.bio" disabled :rows="3" placeholder="写一句座右铭..." />
            </el-form-item>
          </el-form>
        </el-card>

      </div>

      <!-- Right Column: Avatar & Stats -->
      <div class="right-column">
        <!-- Profile Card -->
        <el-card shadow="hover" class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
            <h2 class="username">{{ userInfo.username }}</h2>
            <p class="role-text">{{ roleLabel }}</p>
            <el-tag type="primary" effect="dark" round>活跃成员</el-tag>
          </div>
          
          <div class="stats-row">
            <div class="stat-item">
              <div class="stat-val">{{ stats.volunteerHours || 0 }}</div>
              <div class="stat-lbl">小时</div>
            </div>
            <div class="stat-item">
              <div class="stat-val">{{ stats.activityCount || 0 }}</div>
              <div class="stat-lbl">活动</div>
            </div>
            <div class="stat-item">
              <div class="stat-val">{{ stats.points || 0 }}</div>
              <div class="stat-lbl">积分</div>
            </div>
          </div>
        </el-card>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getVolunteerStats, getVolunteerProfile, getOrganizerProfile } from '@/api/user'
import { Message, Phone, Location, Timer, Edit, User, House } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const stats = ref({
    activityCount: 0,
    points: 0,
    creditScore: 100,
    volunteerHours: 0
})

const userInfo = ref({
    username: '',
    avatar: '',
    email: '',
    phone: '',
    location: '', // mapped to studentId or empty
    bio: '',
    realName: '',
    studentId: ''
})

const roleLabel = computed(() => {
    const role = userStore.role;
    if (role === 'ORGANIZER') return '活动组织者';
    if (role === 'ADMIN') return '管理员';
    return '学生志愿者';
})

onMounted(async () => {
    // Initialize base info from store first to avoid empty flash
    if (userStore.userInfo) {
       userInfo.value.username = userStore.userInfo.username || ''
       userInfo.value.avatar = userStore.userInfo.avatarUrl || userStore.userInfo.avatar || ''
    }

    try {
        const statsRes = await getVolunteerStats()
        if (statsRes.code === 200) {
            stats.value = statsRes.data
        }
    } catch (error) {
        console.error('Failed to fetch stats:', error)
    }

    try {
        let profileRes
        if (userStore.role === 'VOLUNTEER') {
            profileRes = await getVolunteerProfile()
        } else if (userStore.role === 'ORGANIZER') {
            profileRes = await getOrganizerProfile()
        }

        if (profileRes && profileRes.code === 200) {
            const data = profileRes.data
            userInfo.value = {
                ...userInfo.value,
                realName: data.realName || data.orgName || '',
                studentId: data.studentId || '',
                phone: data.phone || '',
                bio: data.bio || data.description || '',
                avatar: data.avatarUrl || userInfo.value.avatar,
                // If email exists in data, use it, else fallback to username if it looks like email
                email: data.email || (userInfo.value.username.includes('@') ? userInfo.value.username : userInfo.value.username)
            }
        }
    } catch (error) {
        console.error('Failed to fetch profile:', error)
    }
})

const toEdit = () => {
    router.push('/user/edit');
}
</script>

<style scoped>
.profile-page {
  padding: 24px;
}
.page-header h2 {
    font-size: 24px;
    margin-bottom: 8px;
    font-weight: 600;
}
.subtitle {
  color: #606266;
  font-size: 14px;
  margin-top: 0;
  margin-bottom: 24px;
}

.profile-layout {
  display: flex;
  gap: 24px;
}

.left-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.right-column {
  width: 360px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card, .interests-card, .profile-card, .ranking-card {
  border-radius: 12px;
  border: none;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.card-header-row h3 {
  margin: 0;
  font-size: 18px;
}

.info-form :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none;
}
.info-form :deep(.el-textarea__inner) {
  background-color: #f5f7fa;
  box-shadow: none;
}

.interest-tag {
  margin-right: 12px;
  margin-bottom: 12px;
  border-radius: 16px;
  padding: 6px 16px;
  font-size: 13px;
  color: #606266;
  background-color: #f4f4f5;
  border-color: #e9e9eb;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  border-bottom: 1px solid #f0f2f5;
}
.username {
  font-size: 20px;
  margin: 16px 0 4px;
}
.role-text {
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  padding-top: 24px;
}
.stat-item {
  text-align: center;
}
.stat-val {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-lbl {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.ranking-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 24px 0;
}
.rank-icon-wrapper {
  background: #fffbe6;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.rank-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}
.rank-desc {
  color: #909399;
  font-size: 14px;
}

.next-goal {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}
.next-goal p {
  margin: 0 0 8px;
  font-size: 13px;
  color: #606266;
}
</style>