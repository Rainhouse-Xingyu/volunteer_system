<template>
  <div class="my-activities-container">
    <div class="page-header">
      <h2>我的活动</h2>
      <p class="subtitle">追踪您的志愿活动和影响力。</p>
    </div>

    <!-- Summary Cards -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :xs="24" :sm="12" :md="8" class="mb-4">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-content">
            <div class="summary-icon blue-bg">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="summary-text">
              <div class="label">即将进行</div>
              <div class="value">{{ stats.upcoming }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" class="mb-4">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-content">
            <div class="summary-icon green-bg">
              <el-icon><Check /></el-icon>
            </div>
            <div class="summary-text">
              <div class="label">已完成</div>
              <div class="value">{{ stats.completed }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" class="mb-4">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-content">
            <div class="summary-icon purple-bg">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="summary-text">
              <div class="label">总参加数量</div>
              <div class="value">{{ stats.totalCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tabs Filtering -->
    <el-tabs v-model="activeTab" class="activity-tabs">
      <el-tab-pane label="即将进行" name="upcoming"></el-tab-pane>
      <el-tab-pane label="已完成" name="completed"></el-tab-pane>
      <el-tab-pane label="待审核" name="pending"></el-tab-pane>
      <el-tab-pane label="我的报名" name="all"></el-tab-pane>
    </el-tabs>

    <!-- Activity List -->
     <div v-loading="loading" class="activity-list">
        <div v-if="activityList.length > 0">
             <el-card v-for="item in activityList" :key="item.regId || item.activityId" shadow="hover" class="activity-list-item">
                <div class="item-content">
                    <div class="item-main">
                        <div class="item-header">
                            <h3 class="item-title">{{ item.activityTitle || item.title }}</h3>
                            <el-tag :type="getStatusType(item)" effect="light" round>
                                {{ getStatusText(item) }}
                            </el-tag>
                        </div>
                        <p class="item-org">{{ item.organizerName || '组织者' }}</p>
                        
                        <div class="item-meta">
                             <div class="meta-part">
                                <el-icon><Calendar /></el-icon> {{ formatDate(item.startTime) }}
                             </div>
                             <div class="meta-part">
                                <el-icon><Clock /></el-icon> {{ formatTimeRange(item.startTime, item.endTime) }}
                             </div>
                             <div class="meta-part">
                                <el-icon><Location /></el-icon> {{ item.location || '线上/线下' }}
                             </div>
                             <div class="meta-part">
                                <el-icon><User /></el-icon> {{ item.currentParticipants || 0 }}/{{ item.quota }} 已报名
                             </div>
                        </div>

                         <!-- Detailed Status for 'My Registrations' tab -->
                        <div v-if="activeTab === 'all'" class="item-detailed-status" style="margin-top: 12px; padding-top: 12px; border-top: 1px dashed #eee;">
                             <el-steps :active="getStepActive(item)" finish-status="success" simple style="background: transparent; padding: 5px 0;">
                                <el-step title="已报名" />
                                <el-step title="已录用" />
                                <el-step title="已签到" />
                                <el-step title="已完成" />
                             </el-steps>
                        </div>
                    </div>
                   
                    <div class="item-actions">
                         <el-button plain @click="toDetail(item)">查看详情</el-button>
                         <el-button v-if="activeTab === 'upcoming' || activeTab === 'pending'" type="danger" plain @click="handleCancel(item)">取消报名</el-button>
                         <el-button v-if="canCheckIn(item)" type="primary" @click="openCheckIn(item)">签到</el-button>
                         <el-button v-if="canComplete(item)" type="success" plain @click="handleComplete(item)">确认完成</el-button>
                    </div>
                </div>
            </el-card>
        </div>
        <el-empty v-else description="暂无相关活动" />
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

    <!-- Completion Dialog -->
    <el-dialog v-model="showCompleteDialog" title="提交完结证明" width="500px">
        <el-alert title="请上传1-3张活动现场照片作为完结证明" type="info" :closable="false" show-icon style="margin-bottom: 20px;" />
        
        <el-upload
            v-model:file-list="fileList"
            action="/api/file/upload"
            list-type="picture-card"
            :limit="3"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :headers="{ Authorization: userStore.token ? 'Bearer ' + userStore.token : '' }"
            accept="image/*"
        >
            <el-icon><Plus /></el-icon>
        </el-upload>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="showCompleteDialog = false">取消</el-button>
                <el-button type="primary" @click="submitCompletion" :loading="processing">提交完成</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Check, Timer, Location, User, Clock, Star, Search, Plus } from '@element-plus/icons-vue'
import { getMyRegistrations, cancelRegistration, checkIn, completeActivity } from '@/api/activity' 
import { ElMessage, ElMessageBox } from 'element-plus'
import { Html5QrcodeScanner } from "html5-qrcode"
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('upcoming')
const activityList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const showCheckIn = ref(false)
const checkInActivityId = ref(null)
const html5QrcodeScanner = ref(null)

const openCheckIn = (item) => {
    checkInActivityId.value = item.activityId
    showCheckIn.value = true
    nextTick(() => {
        startScanner()
    })
}

const startScanner = () => {
    // Ensure element exists
    if (!document.getElementById('reader')) return;
    
    if (html5QrcodeScanner.value) {
        try {
            html5QrcodeScanner.value.clear();
        } catch(e) {}
    }
    html5QrcodeScanner.value = new Html5QrcodeScanner(
        "reader",
        { fps: 10, qrbox: { width: 250, height: 250 } },
        /* verbose= */ false
    );
    html5QrcodeScanner.value.render(onScanSuccess, (error) => {});
}

const onScanSuccess = async (decodedText, decodedResult) => {
    if (html5QrcodeScanner.value) {
        try {
             html5QrcodeScanner.value.clear();
        } catch(e) {}
    }
    showCheckIn.value = false;
    
    // Parse JSON
    try {
        let data = {};
        try {
            data = JSON.parse(decodedText);
        } catch (e) {
            // If strictly JSON token
             data = { signToken: decodedText }
        }
        
        // Add activityId if missing
        if (!data.activityId) {
             data.activityId = checkInActivityId.value
        }
        
        // If data just has token
        if (!data.signToken && data.token) {
            data.signToken = data.token
        }

        const res = await checkIn(data)
        if (res.code === 200) {
            ElMessage.success('签到成功')
            fetchActivities()
        } else {
            ElMessage.error(res.message || '签到失败')
        }
    } catch (e) {
        ElMessage.error('二维码处理错误')
    }
}

const closeCheckIn = () => {
    if (html5QrcodeScanner.value) {
        try {
            html5QrcodeScanner.value.clear();
        } catch (e) {}
    }
    showCheckIn.value = false
}

const showCompleteDialog = ref(false)
const processing = ref(false)
const fileList = ref([])
const currentItem = ref(null)

const handleComplete = (item) => {
    currentItem.value = item
    fileList.value = []
    showCompleteDialog.value = true
}

const handleExceed = (files) => {
    ElMessage.warning('最多只允许上传3张照片')
}

const beforeUpload = (file) => {
    // 限制图片大小
    if (file.size / 1024 / 1024 > 5) {
        ElMessage.error('图片大小不能超过5MB')
        return false
    }
    return true
}

const handleUploadSuccess = (response, uploadFile) => {
    if (response.code !== 200) {
        ElMessage.error(response.message || '上传失败')
        // remove failed file
        const index = fileList.value.indexOf(uploadFile)
        if (index !== -1) fileList.value.splice(index, 1)
    }
}

const submitCompletion = async () => {
    if (fileList.value.length === 0) {
        ElMessage.warning('请至少上传一张照片')
        return
    }
    
    // Extract URLs
    const photos = []
    for (const file of fileList.value) {
        if (file.response && file.response.code === 200) {
            photos.push(file.response.data)
        } else if (file.url) {
            photos.push(file.url)
        }
    }
    
    if (photos.length < 1) {
        ElMessage.warning('照片未上传成功或为空')
        return
    }
    if (photos.length > 3) {
        ElMessage.warning('照片最多上传3张')
        return
    }
    
    processing.value = true
    try {
        const res = await completeActivity(currentItem.value.regId, { photos })
        if (res.code === 200) {
            ElMessage.success('提交成功')
            showCompleteDialog.value = false
            fetchActivities()
        } else {
            ElMessage.error(res.message || '操作失败')
        }
    } catch (e) {
        console.error(e)
        ElMessage.error('网络错误')
    } finally {
        processing.value = false
    }
}

// Summary stats
const stats = ref({
    upcoming: 0,
    completed: 0,
    totalCount: 0
})

// Status mapping for API query
// 0: Pending, 1: Approved/Upcoming, 2: Completed, 3: Rejected, 4: Cancelled
const statusMap = {
    'upcoming': 1, 
    'completed': 2,
    'pending': 0,
    'all': null
}

const getStatusType = (item) => {
    const status = item.regStatus
    if (status === 1) return 'primary'
    if (status === 2) return 'success'
    if (status === 0) {
        if (item.endTime && dayjs(item.endTime).isBefore(dayjs())) return 'info'
        return 'warning'
    }
    if (status === 3 || status === 4) return 'info'
    return ''
}

const getStatusText = (item) => {
    const status = item.regStatus
    if (status === 0) {
         if (item.endTime && dayjs(item.endTime).isBefore(dayjs())) return '已结束'
        return '待审核'
    }
    const map = {
        1: '已录用',
        2: '已完成',
        3: '已驳回',
        4: '已取消'
    }
    return map[status] || '未知'
}

const getStepActive = (item) => {
    if (item.regStatus === 2) return 4
    if (item.regStatus === 1) {
        if (item.checkinStatus === 1) return 3
        return 2
    }
    if (item.regStatus === 0) return 1
    return 0
}

const canCheckIn = (item) => {
    return (activeTab.value === 'upcoming' || activeTab.value === 'all') && 
           item.regStatus === 1 && 
           isToday(item.startTime) && 
           item.checkinStatus !== 1
}

const canComplete = (item) => {
    return (activeTab.value === 'upcoming' || activeTab.value === 'all') &&
           item.regStatus === 1 &&
           item.checkinStatus === 1
}

watch(activeTab, () => {
    currentPage.value = 1;
    fetchActivities();
})

const fetchStats = async () => {
    try {
        // Query for upcoming (status 1)
        const resUpcoming = await getMyRegistrations(1, 1, { status: 1 })
        if (resUpcoming.code === 200) {
            stats.value.upcoming = resUpcoming.data.total
        }

        // Query for completed (status 2)
        const resCompleted = await getMyRegistrations(1, 1, { status: 2 })
        if (resCompleted.code === 200) {
            stats.value.completed = resCompleted.data.total
        }
        
        // Query for total count (all statuses)
        const resAll = await getMyRegistrations(1, 1, {})
        if (resAll.code === 200) {
             stats.value.totalCount = resAll.data.total
        }
    } catch (e) {
        console.error("Failed to fetch stats", e)
    }
}

const fetchActivities = async () => {
    loading.value = true;
    try {
        const status = statusMap[activeTab.value]
        const res = await getMyRegistrations(currentPage.value, pageSize.value, { 
             status: status
             // queryType might be needed if backend distinguishes 'my-activities' this way
        });
        
        if (res.code === 200) {
           let list = res.data.records || [];
           
           if (activeTab.value === 'upcoming') {
               list = list.filter(item => {
                   // Item is upcoming if endTime is in future
                   if (item.endTime) return dayjs(item.endTime).isAfter(dayjs())
                   return true
               })
           }
           
           activityList.value = list;
           
           // Update stats every time activities are fetched/updated
           fetchStats();
        }
    } catch (e) {
        console.error(e);
        ElMessage.error('加载失败')
    } finally {
        loading.value = false;
    }
}

const handleCancel = (item) => {
    ElMessageBox.confirm('确认取消报名?', '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            const res = await cancelRegistration(item.activityId || item.regId)
            if (res.code === 200) {
                ElMessage.success('取消成功')
                fetchActivities()
            } else {
                ElMessage.error(res.message || '取消失败')
            }
        } catch (e) {
            ElMessage.error('网络错误')
        }
    })
}

const toDetail = (item) => {
    router.push(`/activity/${item.activityId || item.id}`)
}

const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD') : '-'
const formatTimeRange = (start, end) => {
    if (!start || !end) return '-'
    return `${dayjs(start).format('HH:mm')} - ${dayjs(end).format('HH:mm')}`
}
const isToday = (date) => dayjs(date).isSame(dayjs(), 'day')

onMounted(() => {
    fetchActivities()
})
</script>

<style scoped src="@/styles/activity-my.css"></style>