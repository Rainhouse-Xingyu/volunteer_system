<template>
  <div class="statistics-container">
    <div class="page-header">
      <h2>数据看板</h2>
      <span class="subtitle">系统全站数据概览</span>
    </div>

    <!-- 总体统计卡片 -->
    <el-row :gutter="24" class="stat-row">
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
              <div class="stat-icon-wrapper icon-users">
                 <el-icon :size="32"><UserFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                <div class="stat-label">注册用户总数</div>
              </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
              <div class="stat-icon-wrapper icon-activities">
                 <el-icon :size="32"><List /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalActivities || 0 }}</div>
                <div class="stat-label">发布活动总数</div>
              </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
              <div class="stat-icon-wrapper icon-active">
                 <el-icon :size="32"><VideoPlay /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.activeActivities || 0 }}</div>
                <div class="stat-label">当前进行中活动</div>
              </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
              <div class="stat-icon-wrapper icon-hours">
                 <el-icon :size="32"><Timer /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalServiceHours || 0 }}</div>
                <div class="stat-label">累计志愿时长(小时)</div>
              </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Empty State for Charts since backend doesn't support them yet -->
    <div class="chart-section">
        <el-empty description="图表数据分析功能升级中..." :image-size="200"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/statistics'
import { UserFilled, List, VideoPlay, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const stats = ref({
    totalUsers: 0,
    totalActivities: 0,
    activeActivities: 0,
    totalServiceHours: 0
})

const loading = ref(false)

const fetchData = async () => {
    loading.value = true
    try {
        const res = await getOverview()
        if (res.code === 200) {
            stats.value = res.data
        } else {
            console.error('Fetch stats failed:', res.message)
        }
    } catch (error) {
        console.error('Fetch stats error:', error)
        // ElMessage.error('无法连接服务器')
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.statistics-container {
    padding: 24px;
    max-width: 1400px;
    margin: 0 auto;
}
.page-header {
    margin-bottom: 32px;
}
.page-header h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
}
.subtitle {
    margin-top: 8px;
    font-size: 14px;
    color: #909399;
    display: block;
}

.stat-row {
    margin-bottom: 24px;
}

.stat-card {
    border-radius: 8px;
    border: none;
    transition: all 0.3s;
    background: #fff;
    margin-bottom: 20px;
}
.stat-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0,0,0,0.08);
}

.stat-content {
    display: flex;
    align-items: center;
    padding: 12px 4px;
}

.stat-icon-wrapper {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;
    flex-shrink: 0;
}

.icon-users {
    background-color: #ecf5ff;
    color: #409EFF;
}
.icon-activities {
    background-color: #fdf6ec;
    color: #E6A23C;
}
.icon-active {
    background-color: #f0f9eb;
    color: #67C23A;
}
.icon-hours {
    background-color: #fef0f0;
    color: #F56C6C;
}

.stat-info {
    flex-grow: 1;
}
.stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
    margin-bottom: 6px;
    /* font-family: 'DIN Alternate', 'Helvetica Neue', Helvetica, sans-serif; */
}
.stat-label {
    font-size: 14px;
    color: #909399;
}

.chart-section {
    background: #fff;
    border-radius: 8px;
    padding: 40px;
    min-height: 400px;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>