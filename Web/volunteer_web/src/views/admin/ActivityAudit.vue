<template>
  <div class="audit-container">
    <div class="page-header">
      <h2>活动审核</h2>
      <div class="header-actions">
           <el-input
            v-model="keyword"
            placeholder="搜索活动标题"
            :prefix-icon="Search"
            style="width: 240px; margin-right: 12px"
            @keyup.enter="handleSearch"
            clearable
            @clear="handleSearch"
          />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="audit-tabs">
      <el-tab-pane label="待审核" name="pending">
        <el-table
            v-loading="loading"
            :data="list"
            style="width: 100%"
            stripe
        >
            <el-table-column prop="title" label="活动标题" min-width="180">
                <template #default="{ row }">
                    <span class="activity-link" @click="viewDetail(row)">{{ row.title }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="organizerName" label="发起组织" width="180" />
            <el-table-column prop="category" label="类型" width="120">
                <template #default="{ row }">
                    <el-tag>{{ row.category }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="提交时间" width="180">
                <template #default="{ row }">
                    {{ formatDate(row.createdAt) }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                    <el-button size="small" type="success" @click="handleAudit(row, 1)">通过</el-button>
                    <el-button size="small" type="danger" @click="handleAudit(row, 4)">驳回</el-button>
                </template>
            </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="招募中" name="recruiting">
         <el-table
            v-loading="loading"
            :data="list"
            style="width: 100%"
            stripe
        >
            <el-table-column prop="title" label="活动标题" min-width="180" />
            <el-table-column prop="organizerName" label="发起组织" width="180" />
            <el-table-column prop="category" label="类型" width="120">
                <template #default="{ row }">
                    <el-tag type="success">{{ row.category }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="发布时间" width="180">
                <template #default="{ row }">
                    {{ formatDate(row.startTime) }}
                </template>
            </el-table-column>
             <el-table-column label="状态" width="100">
                <template #default>
                    <el-tag type="success">招募中</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
                 <template #default="{ row }">
                    <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
                    <el-button size="small" type="primary" plain @click="handleExportActivityReport(row)">导出报表</el-button>
                </template>
            </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="审核记录 (已驳回)" name="history">
         <el-table
            v-loading="loading"
            :data="list"
            style="width: 100%"
            stripe
        >
            <el-table-column prop="title" label="活动标题" min-width="180" />
            <el-table-column prop="organizerName" label="发起组织" width="180" />
            <el-table-column label="审核时间" width="180">
                <!-- Assuming backend tracks audit time or use updated_at -->
                <template #default="{ row }">
                    {{ formatDate(row.updatedAt) }}
                </template>
            </el-table-column>
             <el-table-column label="状态" width="100">
                <template #default>
                    <el-tag type="info">已驳回</el-tag>
                </template>
            </el-table-column>
             <el-table-column label="操作" width="220" fixed="right">
                 <template #default="{ row }">
                    <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
                     <el-button size="small" type="primary" plain @click="handleExportActivityReport(row)">导出报表</el-button>
                </template>
            </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- Audit Dialog -->
    <el-dialog v-model="showDetailDialog" title="活动详情" width="600px">
        <div v-if="currentItem" class="detail-content">
            <h3>{{ currentItem.title }}</h3>
            <p><strong>组织者:</strong> {{ currentItem.organizerName }}</p>
            <p><strong>活动类别:</strong> <el-tag size="small">{{ currentItem.category }}</el-tag></p>
            <p><strong>活动时间:</strong> {{ formatDate(currentItem.startTime) }} - {{ formatDate(currentItem.endTime) }}</p>
            <p><strong>活动地点:</strong> {{ currentItem.location }}</p>
            <p><strong>招募人数:</strong> {{ currentItem.maxPeople }}</p>
            <div class="desc-box">
                <h4>活动描述</h4>
                <p>{{ currentItem.description }}</p>
            </div>
        </div>
        <template #footer>
            <span class="dialog-footer" v-if="currentItem && currentItem.status === 0">
                <el-button @click="showDetailDialog = false">取消</el-button>
                <el-button type="danger" @click="handleAuditInDialog(4)">驳回</el-button>
                <el-button type="success" @click="handleAuditInDialog(1)">通过审核</el-button>
            </span>
            <span class="dialog-footer" v-else>
                 <el-button @click="showDetailDialog = false">关闭</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getActivityList, auditActivity, exportActivityReport } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()

const activeTab = ref('pending')
const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

const showDetailDialog = ref(false)
const currentItem = ref(null)

const formatDate = (date) => {
    if (!date) return '-'
    return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 0:待审, 1:招募中, 4:审核失败
const getStatusByTab = (tab) => {
    if (tab === 'pending') return 0
    if (tab === 'recruiting') return 1
    if (tab === 'history') return 4 
    return 0
}

const fetchData = async () => {
    loading.value = true
    try {
        const status = getStatusByTab(activeTab.value)
        const res = await getActivityList(currentPage.value, pageSize.value, keyword.value, status)
        if (res.code === 200) {
            list.value = res.data.records || []
            total.value = res.data.total || 0
        } else {
            ElMessage.error(res.message || '获取列表失败')
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('网络错误')
    } finally {
        loading.value = false
    }
}

const handleSearch = () => {
    currentPage.value = 1
    fetchData()
}

const handleTabChange = () => {
    currentPage.value = 1
    fetchData()
}

const handlePageChange = (val) => {
    currentPage.value = val
    fetchData()
}

const viewDetail = (row) => {
    currentItem.value = row
    showDetailDialog.value = true
}

const handleAudit = (row, status) => {
    ElMessageBox.confirm(
        status === 1 ? '确认通过该活动申请?' : '确认驳回该活动申请?',
        '审核确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: status === 1 ? 'success' : 'warning'
        }
    ).then(async () => {
        try {
            const res = await auditActivity({
                activityId: row.activityId,
                result: status,
                auditComment: status === 1 ? 'Approved' : 'Rejected' 
            })
            if (res.code === 200) {
                ElMessage.success('操作成功')
                fetchData() // Refresh list
                showDetailDialog.value = false
            } else {
                ElMessage.error(res.message || '操作失败')
            }
        } catch (error) {
             ElMessage.error('网络错误')
        }
    })
}

const handleAuditInDialog = (status) => {
    if (currentItem.value) {
        handleAudit(currentItem.value, status)
    }
}

const handleExportActivityReport = async (row) => {
    try {
        const res = await exportActivityReport(row.activityId)
         if (!res) {
             ElMessage.error('没有数据')
             return
         }
        // Assuming success returns the blob
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `活动报表_${row.title || row.activityId}.xlsx`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
    } catch (error) {
        console.error('Export error:', error)
        ElMessage.error('导出失败')
    }
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.audit-container {
    padding: 24px;
    background: #fff;
    min-height: calc(100vh - 120px);
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
.page-header h2 {
    margin: 0;
    font-size: 20px;
    color: #303133;
}
.header-actions {
    display: flex;
    align-items: center;
}
.activity-link {
    color: #409EFF;
    cursor: pointer;
    font-weight: 500;
}
.activity-link:hover {
    text-decoration: underline;
}
.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
}
.desc-box {
    margin-top: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
}
.desc-box h4 {
    margin: 0 0 8px;
    font-size: 14px;
    color: #303133;
}
.desc-box p {
    margin: 0;
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
}
</style>
