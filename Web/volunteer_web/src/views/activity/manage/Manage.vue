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

            <el-table-column label="评价结果" width="120">
                <template #default="{ row }">
                    <el-tag v-if="row.assessment" :type="row.assessment === 'Fail' ? 'danger' : 'success'">{{ row.assessment }}</el-tag>
                    <span v-else>-</span>
                </template>
            </el-table-column>

            <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                    <div v-if="row.regStatus === 0">
                        <el-button size="small" type="success" @click="handleAudit(row, 1)">录用</el-button>
                        <el-button size="small" type="danger" @click="handleAudit(row, 2)">拒绝</el-button>
                    </div>
                    <div v-else-if="row.regStatus === 1 && row.checkinStatus === 1">
                        <el-button v-if="!row.assessment" size="small" type="primary" @click="openAssessDialog(row)">评价</el-button>
                        <span v-else class="action-text disabled">已评价</span>
                    </div>
                    <div v-else>
                         <span class="action-text disabled">{{ getStatusText(row.regStatus) }}</span>
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

    <el-dialog v-model="assessDialogVisible" title="评价志愿者" width="400px">
        <el-form :model="assessForm" label-width="120px">
            <el-form-item label="评价结果">
                <el-select v-model="assessForm.assessment" placeholder="请选择">
                    <el-option label="优秀 (Excellent)" value="Excellent" />
                    <el-option label="良好 (Good)" value="Good" />
                    <el-option label="不合格 (Fail)" value="Fail" />
                </el-select>
            </el-form-item>
             <p v-if="assessForm.assessment === 'Fail'" style="color: red; margin-left: 120px;">
                注意：不合格将不发放积分。
            </p>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="assessDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitAssessment">确定</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRegistrationList, auditRegistration, assessVolunteer } from '@/api/activity' // Assuming this is correct
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
const total = ref(0) // Assuming this is where I insert

const assessDialogVisible = ref(false)
const currentAssessItem = ref({})
const assessForm = ref({
    assessment: 'Good'
})
const assessOptions = [
    { label: '优秀 (Excellent)', value: 'Excellent' },
    { label: '良好 (Good)', value: 'Good' },
    { label: '不合格 (Fail)', value: 'Fail' }
]

const openAssessDialog = (row) => {
    currentAssessItem.value = row
    assessForm.value.assessment = row.assessment || 'Good'
    assessDialogVisible.value = true
}

const submitAssessment = async () => {
    if(!currentAssessItem.value.regId) return
    try {
        const res = await assessVolunteer({
            id: currentAssessItem.value.regId,
            assessment: assessForm.value.assessment
        })
        if(res.code === 200) {
            ElMessage.success('评价成功')
            assessDialogVisible.value = false
            fetchData() // Refresh list
        } else {
            ElMessage.error(res.message || '评价失败')
        }
    } catch(e) {
        console.error(e)
        ElMessage.error('网络错误')
    }
}



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