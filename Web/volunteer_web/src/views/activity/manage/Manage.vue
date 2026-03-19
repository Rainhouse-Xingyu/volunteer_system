<template>
  <div class="activity-manage-container">
    <div class="page-header">
        <div class="header-left">
            <el-button icon="ArrowLeft" plain @click="$router.back()">返回</el-button>
            <h2 class="page-title">人员管理</h2>
        </div>
        <div class="stats-bar">
            共收到报名申请: <strong>{{ total }}</strong> 条
        </div>
    </div>

    <div class="manage-content">
        <el-table
            v-loading="loading"
            :data="list"
            style="width: 100%"
            stripe
        >
            <el-table-column prop="volunteerName" label="申请人" width="180">
                <template #default="{ row }">
                    <div class="user-cell">
                        <el-avatar :size="32" :src="row.avatarUrl || ''">{{ (row.volunteerName || '').charAt(0) }}</el-avatar>
                        <span style="margin-left: 10px">{{ row.volunteerName }}</span>
                    </div>
                </template>
            </el-table-column>

            <el-table-column prop="activityTitle" label="申请活动" min-width="180" />

            <el-table-column label="报名时间" width="180">
                 <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                </template>
            </el-table-column>

            <el-table-column label="状态" width="120">
                <template #default="{ row }">
                    <el-tag :type="getStatusType(row.regStatus)">{{ getStatusText(row.regStatus) }}</el-tag>
                </template>
            </el-table-column>
            
            <el-table-column label="签到状态" width="120">
                <template #default="{ row }">
                    <el-tag v-if="row.checkinStatus === 1" type="success">已签到</el-tag>
                    <el-tag v-else type="info">未签到</el-tag>
                </template>
            </el-table-column>

            <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                    <div v-if="row.regStatus === 0">
                        <el-button size="small" type="success" @click="handleAudit(row, 1)">录用</el-button>
                        <el-button size="small" type="danger" @click="handleAudit(row, 2)">拒绝</el-button>
                    </div>
                    <div v-else>
                         <span class="action-text disabled">已处理</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

         <div class="pagination-container" v-if="total > 0">
            <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                @current-change="fetchData"
            />
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRegistrationList, auditRegistration } from '@/api/activity' // Assuming this is correct
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id

const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)


const formatTime = (t) => dayjs(t).format('MM-DD HH:mm')

const getStatusText = (s) => (['待审核', '已录用', '已拒绝', '已取消'][s] || '未知')
const getStatusType = (s) => (['warning', 'success', 'danger', 'info'][s] || 'info')

const fetchData = async () => {
    if(!activityId) return
    loading.value = true
    try {
        // API signature: getRegistrationList(activityId, current, size)
        const res = await getRegistrationList(activityId, currentPage.value, pageSize.value)
        if(res.code === 200) {
           list.value = res.data.records || []
           total.value = res.data.total || 0
        } else {
            ElMessage.error(res.message || '加载失败')
        }
    } catch(e) {
        console.error(e)
        ElMessage.error('网络错误')
    } finally {
        loading.value = false
    }
}

const handleAudit = (item, status) => {
    ElMessageBox.confirm(
        status === 1 ? '确认录用该志愿者?' : '确认拒绝该申请?',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: status === 1 ? 'success' : 'warning'
        }
    ).then(async () => {
        try {
            const res = await auditRegistration({
                id: item.regId, 
                status: status
            })
            if (res.code === 200) {
                ElMessage.success('操作成功')
                fetchData()
            } else {
                ElMessage.error(res.message || '操作失败')
            }
        } catch (e) {
             ElMessage.error('网络错误')
        }
    })
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.activity-manage-container {
    padding: 24px;
    background: #fff;
    min-height: calc(100vh - 120px);
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    border-bottom: 1px solid #EBEEF5;
    padding-bottom: 16px;
}
.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}
.page-title {
    margin: 0;
    font-size: 20px;
    color: #303133;
}
.stats-bar {
    font-size: 14px;
    color: #606266;
}
.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
}
.user-cell {
    display: flex;
    align-items: center;
}
.action-text.disabled {
    color: #C0C4CC;
    font-size: 12px;
}
</style>