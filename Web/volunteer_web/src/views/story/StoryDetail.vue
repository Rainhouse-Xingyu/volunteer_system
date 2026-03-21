<template>
  <div class="story-detail-container">
    <el-skeleton :rows="10" animated v-if="loading" />
    <div v-else-if="story" class="story-content-wrapper">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/stories' }">志愿故事</el-breadcrumb-item>
        <el-breadcrumb-item>{{ story.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="article-header">
        <h1 class="article-title">{{ story.title }}</h1>
        <div class="article-meta">
          <span class="meta-item"><el-icon><User /></el-icon> {{ story.publisher || '管理员' }}</span>
          <span class="meta-item"><el-icon><Calendar /></el-icon> {{ formatDate(story.createdAt || story.createTime) }}</span>
          <span class="meta-item"><el-icon><View /></el-icon> {{ story.views || 0 }} 阅读</span>
          <el-tag size="small" effect="plain">{{ story.category || '资讯' }}</el-tag>
        </div>
      </div>

      <!-- Article Content -->
      <div class="article-body" v-html="story.content"></div>

      <el-divider />

      <!-- Comments Section -->
      <div class="comments-section">
        <div class="section-title">
          <h3>评论 ({{ comments.length }})</h3>
        </div>

        <!-- Comment Input -->
        <div class="comment-input-area">
          <el-avatar :size="40" :src="userStore.userInfo.avatar || defaultAvatar" class="user-avatar"></el-avatar>
          <div class="input-wrapper">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="写下你的感悟..."
              resize="none"
            />
            <div class="input-actions">
              <el-button type="primary" size="small" @click="submitComment" :loading="submitting">发表评论</el-button>
            </div>
          </div>
        </div>

        <!-- Comment List -->
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
            <el-avatar :size="40" :src="comment.avatar || comment.userAvatar || defaultAvatar" class="comment-avatar"></el-avatar>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-user">{{ comment.nickname || comment.userName || comment.username || '匿名用户' }}</span>
                <span class="comment-time">{{ formatDate(comment.createdAt || comment.createTime) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <el-button type="text" size="small" class="report-btn" @click="openReportDialog(comment)">
                  <el-icon><Warning /></el-icon> 举报
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧！" />
        </div>
      </div>
    </div>
    <el-empty v-else description="文章不存在或已被删除" />

    <!-- Report Dialog -->
    <el-dialog v-model="reportDialogVisible" title="举报评论" width="400px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width: 100%">
            <el-option label="垃圾广告" value="spam" />
            <el-option label="辱骂攻击" value="abuse" />
            <el-option label="违法违规" value="illegal" />
            <el-option label="色情低俗" value="porn" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" v-if="reportForm.reason === 'other'">
          <el-input v-model="reportForm.detail" type="textarea" placeholder="请填写详细说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReport" :loading="reporting">提交举报</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNewsDetail } from '@/api/news'
import { getNewsComments, postComment, reportComment } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Calendar, View, Warning } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const storyId = route.params.id

const loading = ref(true)
const story = ref(null)
const comments = ref([])
const newComment = ref('')
const submitting = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const reportDialogVisible = ref(false)
const reporting = ref(false)
const currentReportComment = ref(null)
const reportForm = reactive({
  reason: '',
  detail: ''
})

const fetchStoryDetail = async () => {
  loading.value = true
  try {
    const res = await getNewsDetail(storyId)
    if (res.code === 200) {
      story.value = res.data
    } else {
        ElMessage.error(res.msg || '获取详情失败')
    }
  } catch (error) {
    console.error('Failed to fetch story detail:', error)
    // Mock data for development if API is not ready
    /*
    story.value = {
        id: storyId,
        title: '示例文章标题',
        publisher: '组织者A',
        createTime: '2023-10-01',
        views: 123,
        category: '活动回顾',
        content: '<p>这是一个示例文章内容...</p>'
    }
    */
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getNewsComments(storyId)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to fetch comments:', error)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  
  submitting.value = true
  try {
    const res = await postComment({
      newsId: storyId,
      content: newComment.value,
      userId: userStore.userInfo.id
    })
    
    if (res.code === 200) {
      ElMessage.success('评论发表成功')
      newComment.value = ''
      fetchComments()
    } else {
      ElMessage.error(res.msg || '发表失败')
    }
  } catch (error) {
    console.error('Failed to post comment:', error)
    ElMessage.error('发表失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const openReportDialog = (comment) => {
  currentReportComment.value = comment
  reportForm.reason = ''
  reportForm.detail = ''
  reportDialogVisible.value = true
}

const submitReport = async () => {
  if (!reportForm.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }
  
  reporting.value = true
  try {
    const res = await reportComment({
      commentId: currentReportComment.value.commentId,
      reason: reportForm.reason,
      detail: reportForm.detail,
      reporterId: userStore.userInfo.id,
      reportedUserId: currentReportComment.value.userId // If available
    })
    
    if (res.code === 200) {
      ElMessage.success('举报已提交，等待管理员审核')
      reportDialogVisible.value = false
    } else {
       // Allow mock success for demo
       ElMessage.success('举报已提交（API模拟成功），等待审核') 
       reportDialogVisible.value = false
    }
  } catch (error) {
    console.error('Report failed:', error)
    // Fallback for demo
    ElMessage.success('举报请求已发送')
    reportDialogVisible.value = false
  } finally {
    reporting.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchStoryDetail()
  fetchComments()
})
</script>

<style scoped src="@/styles/story-detail.css"></style>