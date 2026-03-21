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
            <el-button v-if="userStore.role !== 'ADMIN'" icon="Edit" plain @click="toEdit">编辑</el-button>
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

        <!-- Credit Score Card -->
        <el-card shadow="hover" class="credit-card" v-if="userStore.role === 'VOLUNTEER'">
           <div class="credit-header">
              <h3>信誉评级</h3>
              <el-tag :type="creditRating.type" effect="dark">{{ creditRating.text }}</el-tag>
           </div>
           <div class="credit-body">
              <el-progress 
                  type="circle" 
                  :percentage="stats.creditScore || 0" 
                  :color="creditRating.color"
                  :width="120"
                  :stroke-width="12"
              >
                  <template #default="{ percentage }">
                      <div class="score-value">{{ percentage }}</div>
                      <div class="score-label">信誉分</div>
                  </template>
              </el-progress>
              
              <div class="credit-legend">
                  <div class="legend-item">
                      <span class="dot" style="background: #67C23A;"></span>
                      <span>90+ 优秀</span>
                  </div>
                  <div class="legend-item">
                      <span class="dot" style="background: #409EFF;"></span>
                      <span>80+ 良好</span>
                  </div>
                   <div class="legend-item">
                      <span class="dot" style="background: #E6A23C;"></span>
                      <span>60+ 一般</span>
                  </div>
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

const creditRating = computed(() => {
    const score = stats.value.creditScore
    if (score >= 90) return { text: '优秀', type: 'success', color: '#67C23A' }
    if (score >= 80) return { text: '良好', type: 'primary', color: '#409EFF' }
    if (score >= 60) return { text: '一般', type: 'warning', color: '#E6A23C' }
    return { text: '需努力', type: 'danger', color: '#F56C6C' }
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

<style scoped src="@/styles/user-me.css"></style>