<template>
  <div class="user-manage-container">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <el-tabs v-model="activeTab" class="content-tabs">
      <!-- User List Tab -->
      <el-tab-pane label="用户列表" name="users">
        <div class="filter-container">
          <el-input 
            v-model="userQuery.keyword" 
            placeholder="搜索用户名/昵称" 
            style="width: 200px;" 
            clearable 
            @clear="fetchUserList"
            @keyup.enter="fetchUserList"
          />
          <el-select v-model="userQuery.role" placeholder="角色" clearable style="margin-left: 10px; width: 120px;" @change="fetchUserList">
            <el-option label="志愿者" value="volunteer" />
            <el-option label="组织者" value="organizer" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px;" @click="fetchUserList">搜索</el-button>
        </div>

        <el-table v-loading="loadingUsers" :data="userList" border style="width: 100%">
          <el-table-column prop="userId" label="ID" width="80" align="center" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="nickname" label="昵称" width="150" />
          <el-table-column prop="role" label="角色" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.role === 'admin' ? 'danger' : (scope.row.role === 'organizer' ? 'warning' : 'success')">
                {{ scope.row.role === 'admin' ? '管理员' : (scope.row.role === 'organizer' ? '组织者' : '志愿者') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
                :disabled="scope.row.role === 'admin'"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" align="center" />
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="userQuery.current"
            v-model:page-size="userQuery.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="Number(userTotal) || 0"
            :hide-on-single-page="false"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <!-- Audit Tab -->
      <el-tab-pane label="信息审核" name="audit">
        <el-table v-loading="loadingAudit" :data="auditList" border style="width: 100%">
          <el-table-column prop="id" label="审核ID" width="80" align="center" />
          <el-table-column prop="userId" label="用户ID" width="80" align="center" />
          <el-table-column prop="type" label="类型" width="150">
             <template #default="scope">
                {{ formatType(scope.row.type) }}
             </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="180" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 0" type="info">待审核</el-tag>
              <el-tag v-else-if="scope.row.status === 1" type="success">已通过</el-tag>
              <el-tag v-else type="danger">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditReason" label="原因" />
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="scope">
              <el-button v-if="scope.row.status === 0" size="small" type="primary" @click="handleAudit(scope.row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="auditQuery.current"
            v-model:page-size="auditQuery.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="Number(auditTotal) || 0"
            :hide-on-single-page="false"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleAuditSizeChange"
            @current-change="handleAuditCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Audit Dialog -->
    <el-dialog v-model="auditDialogVisible" title="审核修改申请" width="600px">
      <div v-if="currentAudit">
        <div class="audit-info-row">
           <span class="label">申请类型:</span> {{ formatType(currentAudit.type) }}
        </div>
        <div class="audit-info-row">
           <span class="label">提交时间:</span> {{ currentAudit.createTime }}
        </div>
        
        <el-divider content-position="left">变更内容</el-divider>
        
        <el-table :data="getDiff(currentAudit)" border size="small">
            <el-table-column prop="field" label="字段" width="150" />
            <el-table-column prop="oldVal" label="原值" show-overflow-tooltip />
            <el-table-column prop="newVal" label="新值" show-overflow-tooltip>
                <template #default="scope">
                    <span style="color: #67C23A; font-weight: bold;">{{ scope.row.newVal }}</span>
                </template>
            </el-table-column>
        </el-table>

        <div class="audit-actions" style="margin-top: 20px;">
             <el-input v-model="auditReasonInput" placeholder="审核意见 (可选)" style="margin-bottom: 15px;" />
             <div style="text-align: right;">
                 <el-button @click="auditDialogVisible = false">取消</el-button>
                 <el-button type="danger" @click="submitAudit(2)">拒绝</el-button>
                 <el-button type="primary" @click="submitAudit(1)">通过</el-button>
             </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getUserList, updateUserStatus, getUserAuditList, auditUserUpdate } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('users')

// --- User List Logic ---
const loadingUsers = ref(false)
const userList = ref([])
const userTotal = ref(0)
const userQuery = reactive({
    current: 1,
    size: 10,
    keyword: '',
    role: ''
})

const handleSizeChange = (val) => {
    userQuery.size = val
    userQuery.current = 1
    fetchUserList()
}

const handleCurrentChange = (val) => {
    userQuery.current = val
    fetchUserList()
}

const fetchUserList = async () => {
    loadingUsers.value = true
    try {
        const res = await getUserList(userQuery)
        if (res.code === 200) {
            userList.value = res.data.records
            userTotal.value = res.data.total
        }
    } catch (e) {
        console.error(e)
    } finally {
        loadingUsers.value = false
    }
}

const handleStatusChange = async (row) => {
    try {
        await updateUserStatus(row.userId, row.status)
        ElMessage.success('状态已更新')
    } catch (e) {
        row.status = row.status === 1 ? 0 : 1 // revert
        ElMessage.error('更新失败')
    }
}

// --- Audit Logic ---
const loadingAudit = ref(false)
const auditList = ref([])
const auditTotal = ref(0)
const auditQuery = reactive({
    current: 1,
    size: 10,
    status: 0 // Default pending
})

const handleAuditSizeChange = (val) => {
    auditQuery.size = val
    auditQuery.current = 1
    fetchAuditList()
}

const handleAuditCurrentChange = (val) => {
    auditQuery.current = val
    fetchAuditList()
}

const auditDialogVisible = ref(false)
const currentAudit = ref(null)
const auditReasonInput = ref('')

const fetchAuditList = async () => {
    loadingAudit.value = true
    try {
        const res = await getUserAuditList(auditQuery)
        if (res.code === 200) {
            auditList.value = res.data.records
            auditTotal.value = res.data.total
        }
    } catch (e) {
        console.error(e)
    } finally {
        loadingAudit.value = false
    }
}

const formatType = (type) => {
    const map = {
        'volunteer_profile': '志愿者资料',
        'organizer_profile': '组织者资料',
        'user_info': '账号信息'
    }
    return map[type] || type
}

const handleAudit = (row) => {
    currentAudit.value = row
    auditReasonInput.value = ''
    auditDialogVisible.value = true
}

const getDiff = (audit) => {
    const original = audit.originalData || {}
    const modified = audit.modifiedData || {}
    const diffs = []
    
    // Check all keys in modified
    for (const key in modified) {
        if (modified[key] !== original[key]) {
            // Ignore trivial fields like updateTime if present
            if (['updateTime', 'createTime', 'userId'].includes(key)) continue;
            
            diffs.push({
                field: key,
                oldVal: original[key] === null || original[key] === undefined ? '(空)' : original[key],
                newVal: modified[key]
            })
        }
    }
    return diffs
}

const submitAudit = async (status) => {
    if (!currentAudit.value) return
    
    try {
        await auditUserUpdate(
            currentAudit.value.id, 
            status, 
            auditReasonInput.value
        )
        ElMessage.success(status === 1 ? '已通过' : '已拒绝')
        auditDialogVisible.value = false
        fetchAuditList() // refresh list
    } catch (e) {
        ElMessage.error('操作失败')
    }
}

onMounted(() => {
    fetchUserList()
    fetchAuditList()
})
</script>

<style scoped src="@/styles/admin-user-manage.css"></style>