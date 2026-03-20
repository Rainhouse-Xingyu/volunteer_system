<template>
  <div class="organizer-workspace">
    <div class="header-section">
      <h2>组织者工作台</h2>
      <p class="subtitle">管理志愿活动，审核申请，追踪表现。</p>
    </div>

    <!-- Stats Cards -->
    <el-row :gutter="24" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon recruiting">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">招募中的活动</div>
              <div class="stat-value">{{ stats.recruiting || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待审核申请</div>
              <div class="stat-value">{{ stats.pending || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon managed">
              <el-icon><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">累计发布活动</div>
              <div class="stat-value">{{ stats.total || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tabs/Actions -->
    <div class="action-bar">
      <el-radio-group v-model="activeTab" class="custom-tabs" @change="handleTabChange">
        <el-radio-button label="my_activities">我的活动</el-radio-button>
        <el-radio-button label="approval_center">审核中心 <el-badge :value="stats.pending || 0" class="badge" v-if="stats.pending > 0"/></el-radio-button>
        <el-radio-button label="completed">已结束活动</el-radio-button>
      </el-radio-group>
      <el-button type="primary" :icon="Plus" @click="$router.push('/activity/create')">发布新活动</el-button>
    </div>

    <!-- Activity List -->
    <div v-loading="loading" class="activity-grid">
      <el-empty v-if="!loading && list.length === 0" description="暂无活动" />
      
      <el-card v-for="item in list" :key="item.activityId" shadow="hover" class="activity-manage-card">
        <div class="card-header">
          <span class="activity-title" :title="item.title">{{ item.title }}</span>
          <el-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</el-tag>
        </div>
        <div class="card-meta">
          <el-icon><Calendar /></el-icon> {{ formatDate(item.startTime) }}
        </div>
        
        <div class="progress-section">
          <div class="progress-info">
            <span>申请人数</span>
            <span>{{ item.currentParticipants || 0 }} / {{ item.quota }}</span>
          </div>
          <el-progress :percentage="calculatePercentage(item)" :show-text="false" />
        </div>

        <div class="status-counts" v-if="activeTab === 'approval_center'">
          <div class="status-item warning">
            待审核: <span>{{ item.pendingCount || 0 }}</span>
          </div>
        </div>

        <div class="card-actions">
           <el-button v-if="item.status === 1 || item.status === 2" :icon="FullScreen" plain type="success" size="small" @click="showQrCode(item)">签到码</el-button>
           <el-button :icon="View" plain size="small" @click="$router.push(`/activity/manage/${item.activityId}`)">管理</el-button>
           <el-button :icon="Edit" plain size="small" @click="$router.push(`/activity/edit/${item.activityId}`)">编辑</el-button>
           <el-button :icon="Delete" plain type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </el-card>
    </div>

    <div class="pagination-container" v-if="total > 0">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchData"
        />
    </div>

    <!-- QR Code Dialog -->
    <el-dialog
        v-model="qrDialogVisible"
        title="活动签到码"
        width="360px"
        center
        @close="handleQrClose"
    >
        <div class="qr-container" style="text-align: center; padding: 20px;">
            <div v-loading="loadingQr" style="min-height: 200px; display: flex; align-items: center; justify-content: center;">
                <img v-if="qrUrl" :src="qrUrl" alt="活动签到二维码" style="width: 200px; height: 200px;" />
            </div>
            <div style="margin-top: 20px;">
                <el-tag type="info" effect="plain" round>
                   <el-icon><Timer /></el-icon> {{ qrTimeLeft }}秒后刷新
                </el-tag>
            </div>
            <p style="margin-top: 10px; color: #909399; font-size: 13px;">请志愿者使用App或小程序扫描签到</p>
        </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyActivities, getSignToken, getOrganizerStats } from '@/api/activity'
import {
  UserFilled, Timer, SuccessFilled, Calendar, Plus, View, Edit, Delete, FullScreen
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'

const router = useRouter()
const activeTab = ref('my_activities')
const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const stats = ref({
    recruiting: 0,
    pending: 0,
    total: 0
})

const getStatusType = (status) => {
    // 0:Pending, 1:Recruiting, 2:Ongoing, 3:Finished, 4:Rejected
    if (status === 1) return 'primary'
    if (status === 2) return 'success'
    if (status === 3) return 'info'
    if (status === 0) return 'warning'
    return 'danger'
}

const getStatusText = (status) => {
    const map = {
        0: '审核中',
        1: '招募中',
        2: '进行中',
        3: '已结束',
        4: '已驳回'
    }
    return map[status] || '未知'
}

const formatDate = (date) => {
    return date ? dayjs(date).format('YYYY年M月D日') : '-'
}

const calculatePercentage = (item) => {
    if (!item.quota) return 0
    return Math.min(Math.round((item.currentParticipants || 0) / item.quota * 100), 100)
}

const fetchData = async () => {
    loading.value = true
    try {
        let params = {}
        if (activeTab.value === 'my_activities') {
            // My activities: Only show Recruiting (1) and Ongoing (2)
            params.status = '1,2'
        } else if (activeTab.value === 'approval_center') {
            // Approval center: Exclude finished activities
            // Usually only Recruiting (1) and Ongoing (2) accept/have pending volunteers
            params.status = '1,2'
        } else if (activeTab.value === 'completed') {
            // Completed: Only show Finished (3)
            params.status = '3'
        }

        const res = await getMyActivities(currentPage.value, pageSize.value, params)
        if (res.code === 200) {
            list.value = res.data.records || []
            total.value = res.data.total || 0
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('加载活动列表失败')
    } finally {
        loading.value = false
    }
}

const fetchStats = async () => {
    try {
        const res = await getOrganizerStats()
        if (res.code === 200) {
            // stats: { recruiting: number, pending: number, total: number }
            stats.value = res.data
        }
    } catch (e) {
        console.error(e)
    }
}

const handleTabChange = () => {
    currentPage.value = 1
    fetchData()
}

// QR Code Logic
const qrDialogVisible = ref(false)
const qrUrl = ref('')
const qrTimeLeft = ref(60)
const loadingQr = ref(false)
let qrTimer = null
let countdownTimer = null
const currentActivityId = ref(null)

const showQrCode = async (item) => {
    currentActivityId.value = item.activityId
    qrDialogVisible.value = true
    await refreshQr()
    startQrTimers()
}

const refreshQr = async () => {
    if (!currentActivityId.value) return
    if (!qrUrl.value) loadingQr.value = true // Only show loading spinner on first load or if empty
    
    try {
        const res = await getSignToken(currentActivityId.value)
        if (res.code === 200) {
            // Checkin content format: { activityId: 123, signToken: "uuid..." }
            const content = JSON.stringify({
                activityId: parseInt(currentActivityId.value),
                signToken: res.data
            })
            qrUrl.value = await QRCode.toDataURL(content, { margin: 1, width: 250 })
            qrTimeLeft.value = 60
        }
    } catch (e) {
        console.error(e)
        ElMessage.error('获取签到码失败')
    } finally {
        loadingQr.value = false
    }
}

const startQrTimers = () => {
    stopQrTimers()
    // Refresh every 60s
    qrTimer = setInterval(() => {
        refreshQr()
    }, 60000)
    
    // Countdown
    countdownTimer = setInterval(() => {
        if (qrTimeLeft.value > 0) {
            qrTimeLeft.value--
        }
    }, 1000)
}

const stopQrTimers = () => {
    if (qrTimer) clearInterval(qrTimer)
    if (countdownTimer) clearInterval(countdownTimer)
    qrTimer = null
    countdownTimer = null
}

const handleQrClose = () => {
    stopQrTimers()
    qrDialogVisible.value = false
    qrUrl.value = ''
    qrTimeLeft.value = 60
}

const handleDelete = (item) => {
    ElMessageBox.confirm('确认删除该活动? 此操作不可恢复', '警告', {
        type: 'warning'
    }).then(() => {
        // Call delete API
        ElMessage.warning('演示模式：删除功能暂未连接后端')
    })
}

onMounted(() => {
    fetchData()
    fetchStats()
})
</script>

<style scoped>
.organizer-workspace {
  padding: 24px;
}
.header-section {
  margin-bottom: 24px;
}
.subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.stats-cards {
  margin-bottom: 32px;
}
.stat-card {
  border-radius: 12px;
  border: none;
}
.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.recruiting { background: #e6f7ff; color: #1890ff; }
.pending { background: #fff7e6; color: #fa8c16; }
.managed { background: #f6ffed; color: #52c41a; }

.stat-label {
  font-size: 14px;
  color: #8492a6;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.activity-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 24px;
    margin-bottom: 24px;
}

.activity-manage-card {
    border-radius: 8px;
    transition: all 0.3s;
}
.activity-manage-card:hover {
    transform: translateY(-5px);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}
.activity-title {
    font-weight: bold;
    font-size: 16px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 70%;
}
.card-meta {
    color: #909399;
    font-size: 13px;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.progress-section {
    margin-bottom: 16px;
}
.progress-info {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #606266;
    margin-bottom: 4px;
}

.status-counts {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    font-size: 12px;
}
.status-item {
    background: #f4f4f5;
    padding: 4px 8px;
    border-radius: 4px;
}
.status-item.warning { color: #e6a23c; background: #fdf6ec; }
.status-item.success { color: #67c23a; background: #f0f9eb; }

.card-actions {
    display: flex;
    justify-content: space-between;
    border-top: 1px solid #EBEEF5;
    padding-top: 12px;
}
.card-actions .el-button {
    flex: 1;
}

.badge {
    margin-left: 4px;
}
.pagination-container {
    display: flex;
    justify-content: center;
}
</style>