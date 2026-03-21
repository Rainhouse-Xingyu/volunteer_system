<template>
  <div class="violation-container">
    <div class="page-header">
      <h2>违规处理</h2>
      <div class="header-actions">
        <!-- 搜索功能暂时未在后端实现，仅预留位置 -->
        <!-- <el-input ... /> -->
      </div>
    </div>

    <el-tabs v-model="activeTab" class="audit-tabs">
      <el-tab-pane label="违规审核" name="audit">
         <el-table :data="reportList" v-loading="loading" stripe style="width: 100%">
            <el-table-column prop="reportId" label="ID" width="80" />
            <el-table-column prop="volunteer_name" label="志愿者姓名" width="150">
               <template #default="{ row }">
                   <el-tag>{{ row.volunteer_name || '未知用户' }}</el-tag>
               </template>
            </el-table-column>
            <el-table-column prop="comment_content" label="违规内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="reason" label="举报原因" width="150">
                <template #default="{ row }">
                   {{ formatReason(row.reason) }}
                </template>
            </el-table-column>
            <el-table-column prop="detail" label="详细描述" width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
               <template #default="{ row }">
                   <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
                   <el-tag v-else-if="row.status === 1" type="danger">已确认</el-tag>
                   <el-tag v-else type="info">已驳回</el-tag>
               </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                    <div v-if="row.status === 0">
                        <el-button size="small" type="danger" @click="handleAudit(row, 1)">确认违规</el-button>
                        <el-button size="small" @click="handleAudit(row, 2)">驳回</el-button>
                    </div>
                    <span v-else class="text-disabled">已结束</span>
                </template>
            </el-table-column>
         </el-table>
      </el-tab-pane>

      <el-tab-pane label="违规记录" name="all">
        <el-table
            v-loading="loading"
            :data="list"
            style="width: 100%"
            stripe
        >
            <el-table-column prop="violationId" label="ID" width="80" />
            <el-table-column prop="targetType" label="类型" width="100">
                <template #default="{ row }">
                   <el-tag :type="row.targetType === 'user' ? 'info' : 'warning'">
                     {{ row.targetType === 'user' ? '用户' : (row.targetType === 'activity' ? '活动' : row.targetType) }}
                   </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="targetId" label="关联ID" width="100" />
            <el-table-column prop="reason" label="违规原因" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">
                   {{ formatReason(row.reason) }}
                </template>
            </el-table-column>
            
            <el-table-column label="处理状态" width="120">
                <template #default="{ row }">
                    <el-tag v-if="row.processedAt" type="success">已处理</el-tag>
                    <el-tag v-else type="danger">待处理</el-tag>
                </template>
            </el-table-column>

            <el-table-column label="处理时间" width="180">
                <template #default="{ row }">
                    {{ row.processedAt ? formatDate(row.processedAt) : '-' }}
                </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                    <el-button 
                      v-if="!row.processedAt"
                      size="small" 
                      type="primary" 
                      @click="openProcessDialog(row)"
                    >处理</el-button>
                    <span v-else class="text-disabled">已完成</span>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination-container">
            <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 处理弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="处理违规"
      width="500px"
    >
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="处理结果">
           <el-input 
             v-model="processForm.reason" 
             type="textarea" 
             placeholder="请输入处理结果或备注（例如：已封禁用户、已下架活动、误报已忽略）"
             rows="4"
           />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleProcess" :loading="processing">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getViolationList, processViolation, getReportList, processReport } from '@/api/violation'
import { ElMessage, ElMessageBox } from 'element-plus'

const formatReason = (reason) => {
    const map = {
        'SPAM': '垃圾广告',
        'ABUSE': '攻击谩骂',
        'POLITICAL': '涉政言论',
        'PORN': '色情低俗',
        'ILLEGAL': '违法违规',
        'OTHER': '其他原因',
        'spam': '垃圾广告',
        'abuse': '攻击谩骂',
        'political': '涉政言论',
        'porn': '色情低俗',
        'illegal': '违法违规',
        'other': '其他原因'
    }
    return map[reason] || reason
}

const loading = ref(false)
const list = ref([])
const reportList = ref([]) // 违规审核列表
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const activeTab = ref('audit') // 默认显示审核页

const dialogVisible = ref(false)
const processing = ref(false)
const currentViolation = ref(null)
const processForm = ref({
  reason: ''
})

// 监听Tab切换
watch(activeTab, (val) => {
    if (val === 'all') {
        fetchList()
    } else if (val === 'audit') {
        fetchReportList()
    }
})

onMounted(() => {
    fetchReportList()
})

const fetchReportList = async () => {
    loading.value = true
    try {
        const res = await getReportList()
        // 假设后端返回 { code: 200, data: [...] }
        if (res.code === 200) {
            reportList.value = res.data || []
        }
    } catch (err) {
        console.error(err)
    } finally {
        loading.value = false
    }
}

const handleAudit = (row, status) => {
    // status: 1-确认违规, 2-驳回(忽略)
    const actionText = status === 1 ? '确认违规并处理' : '驳回举报'
    ElMessageBox.confirm(`确定要${actionText}吗？`, '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            const res = await processReport(row.reportId, status)
            if (res.code === 200) {
                ElMessage.success('操作成功')
                fetchReportList()
            }
        } catch(e) {
            console.error(e)
        }
    })
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getViolationList(currentPage.value, pageSize.value)
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openProcessDialog = (row) => {
  currentViolation.value = row
  processForm.value.reason = ''
  dialogVisible.value = true
}

const handleProcess = async () => {
  if (!processForm.value.reason.trim()) {
      ElMessage.warning('请输入处理结果')
      return
  }
  
  processing.value = true
  try {
    const res = await processViolation({
        violationId: currentViolation.value.violationId,
        reason: processForm.value.reason
    })
    
    if (res.code === 200) {
        ElMessage.success('处理成功')
        dialogVisible.value = false
        fetchList()
    } else {
        ElMessage.error(res.msg || '处理失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    processing.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchList()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.violation-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}
.text-disabled {
    color: #909399;
    font-size: 12px;
}
.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>