<template>
  <div class="message-page">
    <div class="page-header">
      <h2>消息通知</h2>
      <el-button type="primary" plain size="small" @click="handleMarkAllRead">全部已读</el-button>
    </div>

    <!-- Message List -->
    <div v-loading="loading" class="message-content">
      <div v-if="list.length > 0" class="message-list">
        <el-card 
          v-for="msg in list" 
          :key="msg.noticeId" 
          class="message-card"
          :class="{ 'unread-card': msg.isRead === 0 }"
          shadow="hover"
          @click="openDetail(msg)"
        >
          <div class="card-content">
             <div class="card-header">
                <div class="title-section">
                   <div v-if="msg.isRead === 0" class="status-dot"></div>
                   <h3 class="msg-title">{{ msg.title }}</h3>
                </div>
                <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
             </div>
             <p class="msg-preview">{{ msg.content }}</p>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无新消息" />

      <div class="pagination-footer" v-if="total > 0">
         <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
         />
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="showDetail"
      :title="currentMsg.title"
      width="500px"
      align-center
      destroy-on-close
      :show-close="false"
      class="message-detail-dialog"
    >
      <div class="detail-container">
         <div class="detail-meta">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTime(currentMsg.createdAt) }}</span>
            <el-tag size="small" v-if="currentMsg.type" style="margin-left: auto">{{ formatType(currentMsg.type) }}</el-tag>
         </div>
         <div class="detail-body">
            {{ currentMsg.content }}
         </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showDetail = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyNotifications, markAsRead, markAllAsRead } from '@/api/notification'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0) // Need backend support for total, getMyNotifications returns Page object so it should have total

const showDetail = ref(false)
const currentMsg = ref({})

const fetchMessages = async () => {
    loading.value = true
    try {
        const res = await getMyNotifications(currentPage.value, pageSize)
        if (res.code === 200) {
            list.value = res.data.records || []
            total.value = res.data.total || 0
        }
    } catch (e) {
        ElMessage.error('获取消息失败')
    } finally {
        loading.value = false
    }
}

const handlePageChange = (val) => {
    currentPage.value = val
    fetchMessages()
}

const openDetail = async (msg) => {
    currentMsg.value = msg
    showDetail.value = true
    
    if (msg.isRead === 0) {
        try {
            await markAsRead(msg.noticeId)
            msg.isRead = 1 
            // Update unread count in store if needed
        } catch (e) {}
    }
}

const handleMarkAllRead = async () => {
    try {
        const res = await markAllAsRead()
        if (res.code === 200) {
            ElMessage.success('全部已读')
            list.value.forEach(item => item.isRead = 1)
        }
    } catch (e) {
        ElMessage.error('操作失败')
    }
}

const formatType = (type) => {
    const map = {
        'system_msg': '系统通知',
        'audit_result': '审核结果',
        'activity_update': '活动更新'
    }
    return map[type] || '通知'
}

const formatTime = (t) => dayjs(t).format('YYYY-MM-DD HH:mm')

onMounted(() => {
    fetchMessages()
})
</script>

<style scoped>
.message-page {
    padding: 24px;
    max-width: 1000px;
    margin: 0 auto;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
.page-header h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
    font-weight: 600;
}

.message-content {
    min-height: 400px;
}

.message-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.message-card {
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
    border: 1px solid #ebeef5;
}
.message-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.unread-card {
    border-left: 4px solid #409EFF;
    background-color: #fcfcfc;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.title-section {
    display: flex;
    align-items: center;
    gap: 8px;
}

.status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #f56c6c;
}

.msg-title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.msg-time {
    color: #909399;
    font-size: 13px;
    flex-shrink: 0;
}

.msg-preview {
    margin: 0;
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.pagination-footer {
    margin-top: 24px;
    display: flex;
    justify-content: center;
}

/* Detail Dialog Styling */
.detail-meta {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #909399;
    font-size: 13px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;
}
.detail-body {
    line-height: 1.8;
    color: #303133;
    font-size: 15px;
    min-height: 80px;
    white-space: pre-wrap; /* Preserve formatting */
}
</style>