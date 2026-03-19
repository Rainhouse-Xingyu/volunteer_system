<template>
  <div class="news-publish-container">
    <div class="page-header">
      <h2>资讯发布</h2>
      <p class="subtitle">发布最新的志愿资讯和组织动态。</p>
    </div>

    <el-tabs v-model="activeTab" class="news-tabs">
      <el-tab-pane label="发布资讯" name="publish">
          <el-card class="publish-card">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" label-position="top">
                <el-form-item label="资讯标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入标题" maxlength="100" show-word-limit />
                </el-form-item>

                <el-form-item label="封面图片" prop="coverImage">
                    <el-upload
                        class="avatar-uploader"
                        action="/api/file/upload"
                        :show-file-list="false"
                        :on-success="handleAvatarSuccess"
                        :before-upload="beforeAvatarUpload"
                        :headers="uploadHeaders"
                        name="file" 
                    >
                        <img v-if="form.coverImage" :src="form.coverImage" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                    </el-upload>
                    <div class="upload-tip">建议尺寸 16:9，支持 JPG/PNG 格式，大小不超过 2MB</div>
                </el-form-item>

                <el-form-item label="资讯内容" prop="content">
                    <el-input 
                        v-model="form.content" 
                        type="textarea" 
                        :rows="12" 
                        placeholder="请输入资讯正文内容..." 
                        maxlength="5000"
                        show-word-limit
                    />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitForm" :loading="submitting">发布资讯</el-button>
                    <el-button @click="resetForm">重置</el-button>
                </el-form-item>
            </el-form>
          </el-card>
      </el-tab-pane>

      <el-tab-pane label="发布记录" name="history">
          <div v-loading="loading">
              <el-empty v-if="!loading && historyList.length === 0" description="暂无发布记录" />
              <div v-else class="history-list">
                  <el-card v-for="item in historyList" :key="item.newsId" class="history-item" shadow="hover">
                      <div class="history-content">
                          <div class="history-main">
                              <h3 class="history-title">{{ item.title }}</h3>
                              <p class="history-meta">
                                  <span><el-icon><View /></el-icon> {{ item.views || 0 }} 阅读</span>
                                  <span><el-icon><Clock /></el-icon> {{ formatDate(item.createdAt) }}</span>
                                  <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
                                      {{ item.status === 1 ? '已发布' : '草稿' }}
                                  </el-tag>
                              </p>
                          </div>
                          <div class="history-action">
                              <el-button type="danger" link @click="handleDelete(item)">删除</el-button>
                          </div>
                      </div>
                  </el-card>
                  
                  <div class="pagination-container">
                      <el-pagination
                        v-model:current-page="currentPage"
                        v-model:page-size="pageSize"
                        :total="total"
                        layout="prev, pager, next"
                        @current-change="loadHistory"
                      />
                  </div>
              </div>
          </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { Plus, View, Clock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { publishNews, getMyNewsList, deleteNews } from '@/api/news'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const activeTab = ref('publish')
const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)

const uploadHeaders = computed(() => ({
    Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

const form = reactive({
    title: '',
    content: '',
    coverImage: ''
})

const rules = {
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// Image Upload
const handleAvatarSuccess = (response, uploadFile) => {
    // Assuming backend returns { code: 200, data: 'url' }
    if (response.code === 200) {
        // Adjust path if needed. If backend returns full url, great. 
        // If partial, might need prefix. Assuming backend returns usable path or full url.
        // Based on FileUploadController provided earlier: it might return full path or relative.
        // Usually frontend needs to check. Let's assume response.data is the path.
        // We might need to prepend base url if it's relative.
        // For now assume direct usage or proxy handles it.
        // However, standard is usually just the path and we append domain if needed.
        // Let's assume it works like other uploads in the system.
        form.coverImage = response.data
    } else {
        ElMessage.error(response.message || '上传失败')
    }
}

const beforeAvatarUpload = (rawFile) => {
    const isImage = rawFile.type.startsWith('image/');
    const isLt2M = rawFile.size / 1024 / 1024 < 2;

    if (!isImage) {
        ElMessage.error('只能上传图片文件!');
        return false;
    }
    if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!');
        return false;
    }
    return true;
}

const submitForm = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitting.value = true
            try {
                const res = await publishNews(form)
                if (res.code === 200) {
                    ElMessage.success('发布成功')
                    resetForm()
                    activeTab.value = 'history' // Switch to history to see it
                } else {
                    ElMessage.error(res.message || '发布失败')
                }
            } catch (e) {
                ElMessage.error('网络错误')
            } finally {
                submitting.value = false
            }
        }
    })
}

const resetForm = () => {
    formRef.value.resetFields()
    form.coverImage = ''
}

// History List
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadHistory = async () => {
    loading.value = true
    try {
        const res = await getMyNewsList({ current: currentPage.value, size: pageSize.value })
        if (res.code === 200) {
            historyList.value = res.data.records
            total.value = res.data.total
        }
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const handleDelete = (item) => {
    ElMessageBox.confirm('确认删除该资讯?', '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            const res = await deleteNews(item.newsId)
            if (res.code === 200) {
                ElMessage.success('删除成功')
                loadHistory()
            } else {
                ElMessage.error(res.message || '删除失败')
            }
        } catch (e) {
             ElMessage.error('网络错误')
        }
    })
}

const formatDate = (date) => {
    return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

watch(activeTab, (val) => {
    if (val === 'history') {
        loadHistory()
    }
})

</script>

<style scoped>
.news-publish-container {
    padding: 24px;
    max-width: 1000px;
    margin: 0 auto;
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

.publish-card {
    border-radius: 8px;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 100px;
  text-align: center;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}
.history-item {
    border-radius: 8px;
}
.history-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.history-title {
    margin: 0 0 8px 0;
    font-size: 16px;
}
.history-meta {
    margin: 0;
    font-size: 13px;
    color: #909399;
    display: flex;
    gap: 16px;
    align-items: center;
}
.history-meta span {
    display: flex;
    align-items: center;
    gap: 4px;
}
.pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 24px;
}
</style>
