<template>
  <div class="points-container">
    <el-card class="box-card mb-4" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>积分统计</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="stat-item">
            <div class="stat-label">当前积分</div>
            <div class="stat-value text-primary">{{ myPoints }}</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-item">
            <div class="stat-label">我的排名</div>
             <div class="stat-value text-warning">第 {{ myRank }} 名</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

     <el-row :gutter="20">
        <el-col :span="16">
            <el-card class="box-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <span>积分获取明细</span>
                    </div>
                </template>
                <el-table :data="historyList" v-loading="loading" stripe style="width: 100%">
                    <el-table-column prop="activityTitle" label="活动名称" min-width="180" />
                    <el-table-column prop="rewardPoints" label="获得积分" width="100">
                        <template #default="{ row }">
                            <span class="text-success">+{{ row.rewardPoints }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="获得时间" width="180">
                         <template #default="{ row }">
                            {{ formatDate(row.checkInTime) }}
                        </template>
                    </el-table-column>
                </el-table>
                 <div class="pagination-container">
                    <el-pagination
                        v-model:current-page="currentPage"
                        v-model:page-size="pageSize"
                        :total="total"
                        layout="total, prev, pager, next"
                        @current-change="handlePageChange"
                    />
                </div>
            </el-card>
        </el-col>
        <el-col :span="8">
             <el-card class="box-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <span>积分排行榜 (Top 10)</span>
                    </div>
                </template>
                <div class="rank-list">
                    <div v-for="(user, index) in topList" :key="user.userId" class="rank-item">
                        <div class="rank-index">
                            <el-icon v-if="index === 0" color="#FFD700" :size="24"><Trophy /></el-icon>
                            <el-icon v-else-if="index === 1" color="#C0C0C0" :size="24"><Trophy /></el-icon>
                            <el-icon v-else-if="index === 2" color="#CD7F32" :size="24"><Trophy /></el-icon>
                            <span v-else class="rank-number">{{ index + 1 }}</span>
                        </div>
                        <div class="rank-avatar">
                             <el-avatar :size="36" :src="user.avatarUrl">{{ user.nickname?.charAt(0) || user.username.charAt(0) }}</el-avatar>
                        </div>
                        <div class="rank-info">
                            <div class="rank-name">{{ user.nickname || user.username }}</div>
                            <div class="rank-level">{{ user.level || '志愿者' }}</div>
                        </div>
                        <div class="rank-score text-primary">{{ user.points }} 分</div>
                    </div>
                </div>
            </el-card>
        </el-col>
     </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPointsHistory, getPointsRank } from '@/api/user'
import { Trophy } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const myPoints = ref(0)
const myRank = ref(0)
const topList = ref([])

const historyList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDate = (date) => {
    return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const fetchData = async () => {
    loading.value = true
    try {
        const res = await getPointsHistory(currentPage.value, pageSize.value)
        if (res.code === 200) {
            historyList.value = res.data.records || []
            total.value = res.data.total
        }
        
        const rankRes = await getPointsRank()
        if (rankRes.code === 200) {
            myPoints.value = rankRes.data.myPoints
            myRank.value = rankRes.data.myRank
            topList.value = rankRes.data.topList || []
        }
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const handlePageChange = (page) => {
    currentPage.value = page
    fetchData()
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.points-container {
    padding: 20px;
}
.stat-item {
    text-align: center;
    padding: 20px;
}
.stat-label {
    font-size: 16px;
    color: #909399;
    margin-bottom: 10px;
}
.stat-value {
    font-size: 32px;
    font-weight: bold;
}
.text-primary { color: #409EFF; }
.text-warning { color: #E6A23C; }
.text-success { color: #67C23A; }
.mb-4 { margin-bottom: 20px; }

.rank-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}
.rank-item {
    display: flex;
    align-items: center;
    padding: 10px;
    background: #f8f9fa;
    border-radius: 8px;
}
.rank-index {
    width: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: 10px;
}
.rank-icon {
    width: 30px;
    height: 30px;
}
.rank-number {
    font-size: 18px;
    font-weight: bold;
    color: #606266;
}
.rank-avatar {
    margin-right: 15px;
}
.rank-info {
    flex: 1;
}
.rank-name {
    font-weight: 500;
}
.rank-level {
    font-size: 12px;
    color: #909399;
}
.rank-score {
    font-weight: bold;
    font-size: 16px;
}
</style>
