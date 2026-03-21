<template>
  <div class="activity-detail-container" v-loading="loading">
    <div v-if="activity.activityId" class="detail-wrapper">
      <el-card class="detail-card">
        <el-row :gutter="40">
          <el-col :md="10" :sm="24">
            <div class="image-box">
              <el-image 
                :src="activity.coverUrl || defaultImg" 
                fit="cover" 
                class="main-image"
                :preview-src-list="[activity.coverUrl || defaultImg]"
              />
              <el-tag :type="getStatusType(activity.status)" class="status-tag" effect="dark">
                {{ getStatusText(activity.status) }}
              </el-tag>
            </div>
          </el-col>
          
          <el-col :md="14" :sm="24">
            <div class="info-box">
              <h1 class="activity-title">{{ activity.title }}</h1>
              
              <div class="organizer-info">
                <el-avatar :size="32" :src="activity.organizerAvatar || defaultAvatar">User</el-avatar>
                <span class="organizer-name">发布者: {{ activity.organizerName || '未知组织' }}</span>
                <el-tag size="small" effect="plain" style="margin-left: 10px">{{ activity.category }}</el-tag>
              </div>

              <div class="meta-list">
                <div class="meta-item">
                  <el-icon><Timer /></el-icon>
                  <span class="label">截止时间:</span>
                  <span class="value">{{ formatTime(activity.endTime) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span class="label">活动地点:</span>
                  <span class="value">{{ activity.location || '线上/待定' }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Money /></el-icon>
                  <span class="label">获得积分:</span>
                  <span class="value highlight-orange">{{ activity.rewardPoints }} 分</span>
                </div>
                <div class="meta-item">
                  <el-icon><User /></el-icon>
                  <span class="label">招募人数:</span>
                  <span class="value">{{ activity.currentParticipants || 0 }} / {{ activity.quota }} 人</span>
                  <el-progress 
                    :percentage="calculateProgress(activity.currentParticipants, activity.quota)" 
                    :status="calculateProgressStatus(activity.currentParticipants, activity.quota)"
                    style="width: 150px; margin-left: 10px;"
                  />
                </div>
              </div>

              <div class="action-buttons">
                <!-- Admin Actions -->
                <template v-if="role === 'ADMIN'">
                    <el-button type="success" :icon="Check" v-if="activity.status === 0" @click="handleAudit(1)">审核通过</el-button>
                    <el-button type="danger" :icon="Close" v-if="activity.status === 0" @click="handleAudit(4)">拒绝申请</el-button>
                    <el-tag type="success" v-if="activity.status === 1">已发布</el-tag>
                    <el-tag type="danger" v-if="activity.status === 4">审核未过</el-tag>
                </template>

                <!-- Organizer Actions -->
                <template v-else-if="isOrganizer">
                    <el-button type="primary" :icon="Warning" @click="showCheckInCode">签到码</el-button>
                    <el-button @click="router.push(`/activity/manage/${activity.activityId}`)">人员管理</el-button>
                </template>

                <!-- Volunteer Actions -->
                <template v-else>
                    <el-button type="primary" size="large" class="join-btn" v-if="activity.status === 1 && !isRegistered" @click="handleRegister">立即报名</el-button>
                    <el-button type="success" size="large" disabled v-if="isRegistered">已报名</el-button>
                    <el-button disabled v-if="activity.status === 2">进行中</el-button>
                    <el-button disabled v-if="[3,4].includes(activity.status)">已结束</el-button>
                </template>
             </div>

            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :md="16" :sm="24">
          <el-card class="content-card" header="活动详情">
            <div class="rich-text" v-html="formatContent(activity.content || activity.description)"></div>
          </el-card>
        </el-col>
        
        <el-col :md="8" :sm="24">
          <el-card class="comment-card">
            <template #header>
              <div class="comment-header">
                <span>用户评价 ({{ comments.length }})</span>
                <el-button type="primary" link @click="showCommentDialog = true">写评价</el-button>
              </div>
            </template>
            
            <div v-if="comments.length > 0" class="comment-list">
               <div v-for="c in comments" :key="c.commentId" class="comment-item">
                  <div class="comment-user-row">
                    <el-avatar :size="24" :src="c.avatar || defaultAvatar" />
                    <span class="comment-username">{{ c.username || '匿名用户' }}</span>
                    <span class="comment-date">{{ formatTime(c.createdAt) }}</span>
                    <el-button type="danger" link size="small" style="margin-left: auto;" @click="handleReport(c.commentId)">举报</el-button>
                  </div>
                  <div class="comment-content">{{ c.content }}</div>
                  <el-divider style="margin: 10px 0;" />
               </div>
            </div>
            <el-empty v-else description="暂无评价" :image-size="60"></el-empty>
          </el-card>
        </el-col>
      </el-row>

    </div>

    <!-- Dialogs -->
    <el-dialog v-model="showCode" title="活动签到码" width="300px" center destroy-on-close>
        <div class="qrcode-container">
            <canvas id="qrcode-canvas" ref="qrCodeRef"></canvas>
            <p class="qr-tip">有效期60秒，过期请刷新</p>
            <el-button type="primary" link @click="handleRefreshCode">刷新签到码</el-button>
        </div>
    </el-dialog>

    <el-dialog v-model="showCommentDialog" title="发表评价" width="500px">
      <el-form>
        <el-form-item>
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="4"
            placeholder="参与活动有什么感想？分享一下吧~"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCommentDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitComment">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showReportDialog" title="举报评论" width="400px">
      <el-form :model="reportForm" label-position="top">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reason" placeholder="请选择原因" style="width: 100%">
             <el-option label="垃圾广告" value="垃圾广告"></el-option>
             <el-option label="辱骂攻击" value="辱骂攻击"></el-option>
             <el-option label="虚假信息" value="虚假信息"></el-option>
             <el-option label="其它" value="其它"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明">
             <el-input v-model="reportForm.detail" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReport">提交</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getActivityDetail, registerActivity, auditActivity } from '@/api/activity'
import { getActivityComments, postComment, reportComment } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Location, User, Money, Check, Close, Warning } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activity = ref({})
const comments = ref([])
const loading = ref(true)
const defaultImg = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const showCode = ref(false)
const showCommentDialog = ref(false)
const commentContent = ref('')
const qrCodeRef = ref(null)

const role = computed(() => userStore.role || 'VOLUNTEER')

// Mock check if registered - ideally this comes from API
const isRegistered = ref(false) 

const isOrganizer = computed(() => {
    if(!userStore.userId || !activity.value.organizerId) return false
    return String(activity.value.organizerId) === String(userStore.userId)
})

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info';
    case 1: return 'success';
    case 2: return 'warning';
    case 3: return 'danger';
    default: return '';
  }
};

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '报名中', 2: '进行中', 3: '已结束' };
  return map[status] || '未知';
};

const formatTime = (time) => {
    if(!time) return '-'
    return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const formatContent = (content) => {
    if(!content) return '暂无详情'
    return content.replace(/\n/g, '<br>')
}

const calculateProgress = (current, quota) => {
    if(!quota) return 0
    const p = (current / quota) * 100
    // 保留1位小数
    const val = p > 100 ? 100 : p
    return Math.round(val * 10) / 10
}

const calculateProgressStatus = (current, quota) => {
    if(!quota) return ''
    const p = current / quota
    if(p >= 1) return 'success'
    if(p >= 0.8) return 'warning'
    return ''
}

const fetchData = async () => {
    loading.value = true
    try {
        const id = route.params.id
        const res = await getActivityDetail(id)
        if(res.code === 200) {
            activity.value = res.data
        }
        // Fetch comments
        const commentRes = await getActivityComments(id)
        if(commentRes.code === 200) {
            comments.value = commentRes.data
        }
    } catch(e) {
        console.error(e)
        ElMessage.error('加载失败')
    } finally {
        loading.value = false
    }
}

const handleAudit = (status) => {
    ElMessageBox.confirm(
        status === 1 ? '确认通过该活动？' : '确认拒绝该活动？',
        '审核确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    ).then(async () => {
        try {
            const res = await auditActivity({
                activityId: activity.value.activityId,
                result: status // Note: Check API if it expects 'status' or 'result'
            });
            if(res.code === 200) {
                ElMessage.success('操作成功')
                fetchData() // refresh
            }
        } catch(e) { ElMessage.error('操作失败') }
    })
}

const handleRegister = async () => {
     ElMessageBox.confirm('确认报名参加该活动吗？', '提示', { type: 'info' })
     .then(async () => {
         try {
             // Mock API call
             const res = await registerActivity(activity.value.activityId)
             if(res.code === 200) {
                 ElMessage.success('报名成功')
                 isRegistered.value = true
                 fetchData()
             }
         } catch(e) { ElMessage.error(e.message || '报名失败') }
     })
}

const showCheckInCode = () => {
    showCode.value = true
    nextTick(() => {
        generateQRCode()
    })
}

const generateQRCode = async () => {
    const canvas = document.getElementById('qrcode-canvas')
    if(canvas) {
        // Mock token generation
        const token = `CHECKIN:${activity.value.activityId}:${Date.now()}`
        QRCode.toCanvas(canvas, token, { width: 200 }, (error) => {
            if (error) console.error(error)
        })
    }
}

const handleRefreshCode = () => {
    generateQRCode()
}

// Report Logic
const showReportDialog = ref(false)
const reportForm = ref({
    commentId: null,
    reason: '',
    detail: ''
})

const handleReport = (commentId) => {
    reportForm.value = { commentId, reason: '', detail: '' }
    showReportDialog.value = true
}

const submitReport = async () => {
    if(!reportForm.value.reason) {
        ElMessage.warning('请选择举报原因')
        return
    }
    try {
        const res = await reportComment(reportForm.value)
        if(res.code === 200) {
            ElMessage.success('举报已提交')
            showReportDialog.value = false
        } else {
             ElMessage.error(res.message || '举报失败')
        }
    } catch(e) {
        ElMessage.error('举报异常')
    }
}

const handleSubmitComment = async () => {
    if(!commentContent.value) {
        ElMessage.warning('请输入评价内容')
        return
    }
    try {
        const res = await postComment({
            activityId: activity.value.activityId,
            content: commentContent.value
        })
        
        if(res.code === 200) {
            ElMessage.success('评价已提交')
            showCommentDialog.value = false
            commentContent.value = ''
            // Reload comments
            const commentRes = await getActivityComments(activity.value.activityId)
            if(commentRes.code === 200) {
                comments.value = commentRes.data
            }
        } else {
            ElMessage.error(res.message || '评价失败')
        }
    } catch(e) {
        ElMessage.error(e.response?.data?.message || '评价失败，请稍后重试')
    }
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped src="@/styles/activity-detail.css"></style>
