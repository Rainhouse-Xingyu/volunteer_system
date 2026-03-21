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

    <!-- Charts Section -->
    <el-row :gutter="24" class="chart-row">
      <el-col :span="12" :xs="24">
         <el-card shadow="hover" class="chart-card">
            <template #header>
                <div class="card-header">
                    <span>活动参与度 (Top 10)</span>
                </div>
            </template>
            <div ref="activityChartRef" style="height: 300px; width: 100%;"></div>
         </el-card>
      </el-col>
      <el-col :span="12" :xs="24">
         <el-card shadow="hover" class="chart-card">
            <template #header>
                <div class="card-header">
                    <span>志愿者信誉积分分布</span>
                </div>
            </template>
            <div ref="pointsChartRef" style="height: 300px; width: 100%;"></div>
         </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="chart-row" style="margin-top: 20px;">
       <el-col :span="24">
          <el-card shadow="hover" class="chart-card">
             <template #header>
                <div class="card-header">
                    <span>近6个月志愿服务时长趋势</span>
                </div>
             </template>
             <div ref="hoursChartRef" style="height: 350px; width: 100%;"></div>
          </el-card>
       </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { getOverview, getChartsData } from '@/api/statistics'
import { UserFilled, List, VideoPlay, Timer } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref({
    totalUsers: 0,
    totalActivities: 0,
    activeActivities: 0,
    totalServiceHours: 0
})

const loading = ref(false)

// Chart Refs
const activityChartRef = ref(null)
const pointsChartRef = ref(null)
const hoursChartRef = ref(null)

let activityChart = null
let pointsChart = null
let hoursChart = null

const fetchData = async () => {
    loading.value = true
    try {
        const [overviewRes, chartsRes] = await Promise.all([
            getOverview(),
            getChartsData()
        ])
        
        if (overviewRes.code === 200) {
            stats.value = overviewRes.data
        }
        
        if (chartsRes.code === 200) {
            initCharts(chartsRes.data)
        }
    } catch (error) {
        console.error('Fetch stats error:', error)
    } finally {
        loading.value = false
    }
}

const initCharts = (data) => {
    nextTick(() => {
        // 1. Activity Chart
        if (activityChartRef.value) {
            if (activityChart) activityChart.dispose()
            activityChart = echarts.init(activityChartRef.value)
            const actData = data.activityParticipation || []
            activityChart.setOption({
                tooltip: { trigger: 'axis' },
                grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
                xAxis: { 
                    type: 'category', 
                    data: actData.map(i => i.name),
                    axisLabel: { interval: 0, rotate: 30, width: 100, overflow: 'truncate' }
                },
                yAxis: { type: 'value' },
                series: [{
                    name: '参与人数',
                    type: 'bar',
                    data: actData.map(i => i.value),
                    itemStyle: { color: '#409EFF' },
                    barMaxWidth: 50
                }]
            })
        }

        // 2. Points Chart
        if (pointsChartRef.value) {
            if (pointsChart) pointsChart.dispose()
            pointsChart = echarts.init(pointsChartRef.value)
            const pointsData = data.pointsDistribution || []
            pointsChart.setOption({
                tooltip: { trigger: 'item' },
                legend: { orient: 'vertical', left: 'left' },
                series: [{
                    name: '积分分布',
                    type: 'pie',
                    radius: ['40%', '70%'],
                    avoidLabelOverlap: false,
                    itemStyle: {
                        borderRadius: 10,
                        borderColor: '#fff',
                        borderWidth: 2
                    },
                    label: { show: false, position: 'center' },
                    emphasis: {
                        label: { show: true, fontSize: 20, fontWeight: 'bold' }
                    },
                    data: pointsData
                }]
            })
        }

        // 3. Hours Chart
        if (hoursChartRef.value) {
            if (hoursChart) hoursChart.dispose()
            hoursChart = echarts.init(hoursChartRef.value)
            const hoursData = data.serviceHours || []
            hoursChart.setOption({
                tooltip: { trigger: 'axis' },
                grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
                xAxis: { type: 'category', data: hoursData.map(i => i.name) },
                yAxis: { type: 'value', name: '小时' },
                series: [{
                    name: '服务时长',
                    data: hoursData.map(i => i.value),
                    type: 'line',
                    smooth: true,
                    areaStyle: {
                         color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                          { offset: 0, color: 'rgba(64,158,255,0.5)' },
                          { offset: 1, color: 'rgba(64,158,255,0.1)' }
                        ])
                    },
                    itemStyle: { color: '#409EFF' }
                }]
            })
        }
    })
}

// Window resize handler
const handleResize = () => {
    activityChart && activityChart.resize()
    pointsChart && pointsChart.resize()
    hoursChart && hoursChart.resize()
}

onMounted(() => {
    fetchData()
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    activityChart && activityChart.dispose()
    pointsChart && pointsChart.dispose()
    hoursChart && hoursChart.dispose()
})



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