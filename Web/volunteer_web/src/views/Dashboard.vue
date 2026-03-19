<template>
  <div class="dashboard-container">
    <!-- Top Stats Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <el-avatar :size="50" :src="userStore.userInfo?.avatarUrl" class="user-avatar" :icon="User" />
            <div class="stats-info">
              <div class="stats-value">你好</div>
              <div class="stats-label">{{ userStore.userInfo?.realName || userStore.userInfo?.nickname || userStore.userInfo?.username || '志愿者' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="icon-wrapper time-icon">
              <el-icon :size="24"><Timer /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ userStats.activityCount }}</div>
              <div class="stats-label">参加活动数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="icon-wrapper star-icon">
              <el-icon :size="24"><Star /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ userStats.points }}</div>
              <div class="stats-label">志愿积分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="icon-wrapper rank-icon">
              <el-icon :size="24"><Trophy /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ userStats.creditScore }}</div>
              <div class="stats-label">信誉分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="main-layout">
      <div class="left-panel">
        <!-- Search & Filter Section -->
        <el-card shadow="never" class="filter-card">
          <el-input
            v-model="searchQuery"
            placeholder="搜索志愿活动..."
            prefix-icon="Search"
            class="search-bar"
          />
          <div class="filter-tags">
            <div class="filter-group">
              <span class="filter-label">活动类型:</span>
              <el-tag
                v-for="type in activityTypes"
                :key="type"
                :effect="activeType === type ? 'dark' : 'plain'"
                class="filter-tag"
                @click="activeType = type"
              >
                {{ type }}
              </el-tag>
            </div>
            <div class="filter-group">
              <span class="filter-label">时间:</span>
               <el-tag
                v-for="date in dateFilters"
                :key="date"
                :effect="activeDate === date ? 'dark' : 'plain'"
                class="filter-tag"
                @click="activeDate = date"
              >
                {{ date }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <!-- Featured Activity -->
        <el-card shadow="hover" class="featured-card">
          <div v-if="recommendedActivity" class="activity-content-wrapper">
            <div class="activity-header">
              <div class="icon-box">
                <el-icon :size="32" color="#67C23A"><Sunny /></el-icon>
              </div>
              <div class="activity-title-section">
                <h3>{{ recommendedActivity.title }}</h3>
                <span class="org-name">{{ recommendedActivity.category || '推荐活动' }}</span>
              </div>
              <el-tag type="info" effect="plain">{{ formatDuration(recommendedActivity.startTime, recommendedActivity.endTime) }}</el-tag>
            </div>
            <p class="activity-desc">{{ recommendedActivity.description }}</p>
            <div class="activity-meta">
              <div class="meta-item"><el-icon><Calendar /></el-icon> {{ formatDate(recommendedActivity.startTime) }}</div>
              <div class="meta-item"><el-icon><Clock /></el-icon> {{ formatTimeRange(recommendedActivity.startTime, recommendedActivity.endTime) }}</div>
              <div class="meta-item"><el-icon><Location /></el-icon> {{ recommendedActivity.location }}</div>
              <div class="meta-item"><el-icon><User /></el-icon> {{ (recommendedActivity.currentParticipants || 0) }}/{{ recommendedActivity.maxParticipants }} 已报名</div>
            </div>
            <el-button type="primary" class="signup-btn" @click="goToDetail(recommendedActivity.activityId)">立即报名</el-button>
          </div>
          <el-empty v-else description="暂无适合您的推荐活动，请稍后再试" />
        </el-card>
      </div>

      <div class="right-panel">
        <!-- My Journey -->
        <el-card shadow="hover" class="journey-card">
          <template #header>
            <div class="card-header">
              <span>我的行程</span>
            </div>
          </template>
          <div class="journey-list">
             <el-empty v-if="journeyList.length === 0" description="暂无行程" />

             <div v-for="item in journeyList" :key="item.activityId" class="journey-item">
              <div class="j-header">
                <span class="j-title" :title="item.activityTitle">{{ item.activityTitle }}</span>
                <el-tag v-if="item.displayStatus === 'upcoming'" size="small">即将开始</el-tag>
                <el-tag v-if="item.displayStatus === 'ongoing'" type="success" size="small">进行中</el-tag>
                <el-tag v-if="item.displayStatus === 'ended'" type="info" size="small">已结束</el-tag>
              </div>
              <div class="j-time">{{ formatDateTime(item.startTime) }}</div>
              
              <el-button 
                v-if="item.displayStatus === 'ongoing'" 
                plain 
                type="primary" 
                size="small" 
                style="width: 100%; margin-top: 10px" 
                @click="openCheckIn(item.activityId)">
                <el-icon><FullScreen /></el-icon> 签到
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- Notifications -->
         <el-card shadow="hover" class="notification-card">
           <template #header>
            <div class="card-header">
              <span>通知消息</span>
              <el-icon><Bell /></el-icon>
            </div>
          </template>
          <div v-if="latestNotification" class="notif-item">
            <el-icon color="#409EFF"><InfoFilled /></el-icon>
            <div class="notif-content">
              <div class="notif-title">{{ latestNotification.title }}</div>
              <div class="notif-text">{{ latestNotification.content }}</div>
              <div class="notif-time">{{ formatTimeAgo(latestNotification.createTime) }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无新通知" :image-size="60" />
         </el-card>
      </div>
    </div>

    <!-- Check-in Modal -->
    <el-dialog
        v-model="showCheckIn"
        title="活动签到"
        width="400px"
        center
        class="check-in-dialog"
        @close="closeCheckIn"
    >
        <div class="check-in-content">
            <p class="activity-subtitle">请扫描活动现场二维码进行签到</p>
            
            <div id="reader" style="width: 300px; height: 300px; margin: 0 auto; background: #f0f0f0;"></div>

            <p class="scan-hint" style="margin-top: 20px;">请将摄像头对准二维码</p>
            
            <el-button plain class="cancel-btn" @click="closeCheckIn">取消</el-button>
        </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { checkIn, getRecommendedActivity, getMyRegistrations } from '@/api/activity'
import { getVolunteerStats } from '@/api/user'
import { getMyNotifications } from '@/api/notification'
import {
  User, Timer, Star, Trophy, Search, Calendar, Clock, Location,
  Sunny, FullScreen, Bell, InfoFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { Html5QrcodeScanner } from "html5-qrcode"
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()
const searchQuery = ref('')
const activeType = ref('全部')
const activeDate = ref('全部日期')
const showCheckIn = ref(false)
const checkInActivityId = ref(null)
const recommendedActivity = ref(null)
const journeyList = ref([])
const latestNotification = ref(null)

const activityTypes = ['全部', '环境保护', '教育', '食品营养', '动物福利', '老年关怀']
const dateFilters = ['全部日期', '今天', '本周', '本月']

// User Stats
const userStats = ref({
    activityCount: 0,
    points: 0,
    creditScore: 0
})

const fetchStats = async () => {
    try {
        const res = await getVolunteerStats()
        if (res.code === 200) {
            userStats.value = res.data
        }
    } catch (error) {
        console.error("Failed to fetch user stats", error)
    }
}

const fetchRecommendation = async () => {
    try {
        const res = await getRecommendedActivity()
        if (res.code === 200 && res.data) {
            recommendedActivity.value = res.data
        }
    } catch (error) {
        console.error("Failed to fetch recommendation", error)
    }
}

const fetchJourney = async () => {
    try {
        const res = await getMyRegistrations(1, 100)
        if (res.code === 200) {
            const records = res.data.records || []
            const now = new Date()
            
            journeyList.value = records.map(item => {
                const start = new Date(item.startTime)
                const end = new Date(item.endTime)
                let status = 'none'

                // 3 days before start -> Upcoming
                const threeDaysBefore = new Date(start)
                threeDaysBefore.setDate(start.getDate() - 3)
                
                // 12 hours after end -> Ended
                const twelveHoursAfter = new Date(end)
                twelveHoursAfter.setHours(end.getHours() + 12)

                if (now < start && item.regStatus === 1) {
                     // Check if within 3 days
                     if (now >= threeDaysBefore) {
                         status = 'upcoming'
                     }
                } else if (now >= start && now <= end && item.regStatus === 1) {
                    status = 'ongoing'
                } else if (now > end && now <= twelveHoursAfter && item.regStatus === 1) {
                    status = 'ended'
                }

                return { ...item, displayStatus: status }
            }).filter(item => item.displayStatus !== 'none')
        }
    } catch (error) {
       console.error("Failed to fetch journey", error) 
    }
}

const fetchLatestNotification = async () => {
    try {
        const res = await getMyNotifications(1, 1) // Get only 1
        if (res.code === 200 && res.data && res.data.records && res.data.records.length > 0) {
            latestNotification.value = res.data.records[0]
        }
    } catch (error) {
        console.error("Failed to fetch notification", error)
    }
}

const goToDetail = (id) => {
    router.push(`/activity/detail/${id}`)
}

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

const formatDateTime = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const y = date.getFullYear()
    const m = date.getMonth() + 1
    const d = date.getDate()
    const h = date.getHours().toString().padStart(2, '0')
    const min = date.getMinutes().toString().padStart(2, '0')
    return `${y}年${m}月${d}日 • ${h}:${min}`
}

const formatTimeAgo = (dateStr) => {
    if (!dateStr) return ''
    return dayjs(dateStr).fromNow()
}

const formatTimeRange = (start, end) => {
    if (!start || !end) return ''
    const s = new Date(start)
    const e = new Date(end)
    const format = (d) => `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
    return `${format(s)} - ${format(e)}`
}

const formatDuration = (start, end) => {
    if (!start || !end) return ''
    const s = new Date(start)
    const e = new Date(end)
    const diffMs = e - s
    const diffHrs = Math.floor(diffMs / 3600000)
    return `${diffHrs}h`
}

onMounted(() => {
    fetchStats()
    fetchRecommendation()
    fetchJourney()
    fetchLatestNotification()
})

let html5QrcodeScanner = null

const startScanner = () => {
    // Wait for DOM to update
    setTimeout(() => {
        html5QrcodeScanner = new Html5QrcodeScanner(
            "reader",
            { fps: 10, qrbox: { width: 250, height: 250 } },
            /* verbose= */ false);
        html5QrcodeScanner.render(onScanSuccess, onScanFailure);
    }, 100)
}

const onScanSuccess = async (decodedText, decodedResult) => {
    // Handle the scanned code as you like, for example:
    console.log(`Code matched = ${decodedText}`, decodedResult);
    
    // Stop scanning
    if (html5QrcodeScanner) {
        await html5QrcodeScanner.clear()
        showCheckIn.value = false
    }

    // Call API
    try {
        const res = await checkIn({ 
            activityId: checkInActivityId.value,
            signToken: decodedText 
        })
        if (res.code === 200) {
            ElMessage.success('签到成功！')
            // Refresh logic if needed
        } else {
            ElMessage.error(res.message || '签到失败')
        }
    } catch (error) {
        ElMessage.error('签到请求失败')
    }
}

const onScanFailure = (error) => {
    // console.warn(`Code scan error = ${error}`);
}

const openCheckIn = (activityId) => {
    checkInActivityId.value = activityId
    showCheckIn.value = true
    // Start scanner when dialog opens
    startScanner()
}

const closeCheckIn = () => {
    showCheckIn.value = false
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().catch(error => {
            console.error("Failed to clear html5QrcodeScanner. ", error);
        });
    }
}


</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
.stats-row {
  margin-bottom: 24px;
}
.stats-card {
  border-radius: 12px;
  border: none;
}
.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-icon { background: #e8f3ff; color: #1890ff; }
.time-icon { background: #e6f7ff; color: #096dd9; }
.star-icon { background: #fffbe6; color: #faad14; }
.rank-icon { background: #f6ffed; color: #52c41a; }

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #1f2d3d;
}
.stats-label {
  font-size: 13px;
  color: #8492a6;
  margin-top: 4px;
}

.main-layout {
  display: flex;
  gap: 24px;
}
.left-panel {
  flex: 1;
}
.right-panel {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.filter-card {
  border-radius: 12px;
  margin-bottom: 24px;
}
.search-bar {
  margin-bottom: 16px;
}
.search-bar :deep(.el-input__wrapper) {
  border-radius: 8px;
  background-color: #f5f7fa;
  box-shadow: none !important;
}
.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.filter-label {
  font-size: 13px;
  font-weight: bold;
  color: #606266;
  min-width: 60px;
}
.filter-tag {
  cursor: pointer;
  border-radius: 16px;
  padding: 0 16px;
}

.featured-card {
  border-radius: 12px;
}
.activity-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}
.icon-box {
  width: 56px;
  height: 56px;
  background: #f0f9eb;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.activity-title-section h3 {
  margin: 0;
  font-size: 18px;
}
.org-name {
  font-size: 13px;
  color: #909399;
}
.activity-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin: 16px 0;
  color: #606266;
  font-size: 13px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.signup-btn {
  width: 100%;
  border-radius: 8px;
  padding: 20px 0;
  font-weight: bold;
}

.journey-card, .notification-card {
  border-radius: 12px;
}
.card-header {
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.journey-list {
  display: flex;
  flex-direction: column;
}
.journey-item {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}
.j-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.j-title {
  font-weight: 500;
  font-size: 14px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
}
.j-time {
  font-size: 12px;
  color: #909399;
}

.notif-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #ecf5ff;
  border-radius: 8px;
}
.notif-title {
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 4px;
}
.notif-text {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
  margin-bottom: 4px;
}
.notif-time {
  font-size: 11px;
  color: #909399;
}

/* Modal Styles */
.check-in-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px 0;
}
.activity-subtitle {
    color: #606266;
    margin-bottom: 24px;
}
.qr-code-box {
    width: 200px;
    height: 200px;
    border: 1px dashed #dcdfe6;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
    background: #f5f7fa;
}
.scan-hint {
    color: #606266;
    font-size: 14px;
    margin-bottom: 8px;
}
.scan-hint-sub {
    color: #909399;
    font-size: 12px;
    margin-bottom: 24px;
}
.code-input-section {
    width: 100%;
    margin-bottom: 24px;
}
.code-input-section span {
    font-weight: bold;
    display: block;
    margin-bottom: 8px;
    font-size: 14px;
}
.check-in-btn, .cancel-btn {
    width: 100%;
    margin-bottom: 12px;
    margin-left: 0;
}
</style>