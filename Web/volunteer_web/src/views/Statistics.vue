<template>
  <div class="statistics-page">
    <!-- 时间筛选 -->
    <div class="time-filter">
      <button class="time-btn" :class="{ active: period === 'week' }" @click="selectTime('week')">本周</button>
      <button class="time-btn" :class="{ active: period === 'month' }" @click="selectTime('month')">本月</button>
      <button class="time-btn" :class="{ active: period === 'quarter' }" @click="selectTime('quarter')">本季度</button>
      <button class="time-btn" :class="{ active: period === 'year' }" @click="selectTime('year')">本年度</button>
      <button class="time-btn" :class="{ active: period === 'custom' }" @click="selectTime('custom')">自定义</button>
    </div>

    <!-- 核心统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="stat-card-header">
          <div class="stat-icon primary"><i class="fas fa-calendar-check"></i></div>
          <div class="stat-trend up"><i class="fas fa-arrow-up"></i> 12.5%</div>
        </div>
        <div class="stat-number">156</div>
        <div class="stat-label">活动总数</div>
      </div>

      <div class="stat-card success">
        <div class="stat-card-header">
          <div class="stat-icon success"><i class="fas fa-play-circle"></i></div>
          <div class="stat-trend up"><i class="fas fa-arrow-up"></i> 8.3%</div>
        </div>
        <div class="stat-number">42</div>
        <div class="stat-label">进行中活动</div>
      </div>

      <div class="stat-card warning">
        <div class="stat-card-header">
          <div class="stat-icon warning"><i class="fas fa-users"></i></div>
          <div class="stat-trend up"><i class="fas fa-arrow-up"></i> 23.1%</div>
        </div>
        <div class="stat-number">1,258</div>
        <div class="stat-label">参与志愿者</div>
      </div>

      <div class="stat-card info">
        <div class="stat-card-header">
          <div class="stat-icon info"><i class="fas fa-clock"></i></div>
          <div class="stat-trend up"><i class="fas fa-arrow-up"></i> 15.7%</div>
        </div>
        <div class="stat-number">8,492</div>
        <div class="stat-label">累计服务时长(小时)</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 活动趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">活动发布趋势</h3>
          <select style="padding: 5px 10px; border: 1px solid #ddd; border-radius: 5px;">
            <option>近7天</option>
            <option selected>近30天</option>
            <option>近90天</option>
          </select>
        </div>
        <div class="chart-container">
          <canvas id="trendChart"></canvas>
        </div>
      </div>

      <!-- 活动类型分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">活动类型分布</h3>
        </div>
        <div class="chart-container">
          <canvas id="typeChart"></canvas>
        </div>
      </div>

      <!-- 活动状态统计 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">活动状态统计</h3>
        </div>
        <div class="chart-container">
          <canvas id="statusChart"></canvas>
        </div>
      </div>

      <!-- 地区分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">活动地区分布</h3>
        </div>
        <div class="chart-container">
          <canvas id="regionChart"></canvas>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// Assuming Chart.js is available globally via CDN as per index.html modification
// Or import Chart from 'chart.js/auto'; if installed via npm, but user didn't have it.

const period = ref('month');

const selectTime = (p) => {
  period.value = p;
  console.log('Select period:', p);
};

onMounted(() => {
  if (typeof Chart === 'undefined') {
    console.error('Chart.js not loaded');
    return;
  }
  
  // 活动趋势图（折线图）
  const trendCtx = document.getElementById('trendChart').getContext('2d');
  new Chart(trendCtx, {
      type: 'line',
      data: {
          labels: ['11-01', '11-05', '11-10', '11-15', '11-20', '11-25', '11-30'],
          datasets: [{
              label: '新增活动',
              data: [12, 19, 15, 25, 22, 30, 28],
              borderColor: '#667eea',
              backgroundColor: 'rgba(102, 126, 234, 0.1)',
              tension: 0.4,
              fill: true,
              pointBackgroundColor: '#667eea',
              pointBorderColor: '#fff',
              pointBorderWidth: 2,
              pointRadius: 5
          }, {
              label: '完成活动',
              data: [8, 15, 12, 20, 18, 25, 22],
              borderColor: '#28a745',
              backgroundColor: 'rgba(40, 167, 69, 0.1)',
              tension: 0.4,
              fill: true,
              pointBackgroundColor: '#28a745',
              pointBorderColor: '#fff',
              pointBorderWidth: 2,
              pointRadius: 5
          }]
      },
      options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
              legend: {
                  position: 'top',
              }
          },
          scales: {
              y: {
                  beginAtZero: true,
                  grid: {
                      color: 'rgba(0,0,0,0.05)'
                  }
              },
              x: {
                  grid: {
                      display: false
                  }
              }
          }
      }
  });

  // 活动类型分布（饼图）
  const typeCtx = document.getElementById('typeChart').getContext('2d');
  new Chart(typeCtx, {
      type: 'doughnut',
      data: {
          labels: ['环保公益', '敬老助残', '社区服务', '教育辅导', '医疗健康', '文化体育'],
          datasets: [{
              data: [35, 25, 20, 12, 8, 10],
              backgroundColor: [
                  '#667eea',
                  '#28a745',
                  '#ffc107',
                  '#17a2b8',
                  '#dc3545',
                  '#6c757d'
              ],
              borderWidth: 0
          }]
      },
      options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
              legend: {
                  position: 'right',
                  labels: {
                      padding: 15,
                      usePointStyle: true
                  }
              }
          }
      }
  });

  // 活动状态统计（柱状图）
  const statusCtx = document.getElementById('statusChart').getContext('2d');
  new Chart(statusCtx, {
      type: 'bar',
      data: {
          labels: ['待审核', '审核通过', '进行中', '已完成', '已驳回'],
          datasets: [{
              label: '活动数量',
              data: [8, 12, 42, 88, 6],
              backgroundColor: [
                  'rgba(255, 193, 7, 0.8)',
                  'rgba(102, 126, 234, 0.8)',
                  'rgba(40, 167, 69, 0.8)',
                  'rgba(108, 117, 125, 0.8)',
                  'rgba(220, 53, 69, 0.8)'
              ],
              borderRadius: 8,
              borderSkipped: false,
          }]
      },
      options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
              legend: {
                  display: false
              }
          },
          scales: {
              y: {
                  beginAtZero: true,
                  grid: {
                      color: 'rgba(0,0,0,0.05)'
                  }
              },
              x: {
                  grid: {
                      display: false
                  }
              }
          }
      }
  });

  // 地区分布（水平柱状图）
  const regionCtx = document.getElementById('regionChart').getContext('2d');
  new Chart(regionCtx, {
      type: 'bar',
      data: {
          labels: ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区'],
          datasets: [{
              label: '活动数量',
              data: [38, 32, 28, 25, 20, 13],
              backgroundColor: 'rgba(102, 126, 234, 0.8)',
              borderRadius: 8,
              borderSkipped: false,
          }]
      },
      options: {
          indexAxis: 'y',
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
              legend: {
                  display: false
              }
          },
          scales: {
              x: {
                  beginAtZero: true,
                  grid: {
                      color: 'rgba(0,0,0,0.05)'
                  }
              },
              y: {
                  grid: {
                      display: false
                  }
              }
          }
      }
  });
});
</script>

<style scoped>
:root {
  --primary-color: #667eea;
  --primary-dark: #764ba2;
  --success-color: #28a745;
  --danger-color: #dc3545;
  --warning-color: #ffc107;
  --info-color: #17a2b8;
  --bg-light: #f8f9fc;
  --text-color: #333;
  --text-muted: #6c757d;
  --border-color: #e0e0e0;
  --white: #ffffff;
}

/* 时间筛选 */
.time-filter {
  background: #fff;
  padding: 15px 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  display: flex;
  gap: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.time-btn {
  padding: 8px 20px;
  border: 1px solid #e0e0e0;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.time-btn:hover, .time-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: transparent;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: #fff;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
}

.stat-card.primary::before { background: linear-gradient(90deg, #667eea, #764ba2); }
.stat-card.success::before { background: linear-gradient(90deg, #84fab0, #8fd3f4); }
.stat-card.warning::before { background: linear-gradient(90deg, #f6d365, #fda085); }
.stat-card.info::before { background: linear-gradient(90deg, #a8edea, #fed6e3); }

.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
}

.stat-icon.primary { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.success { background: linear-gradient(135deg, #84fab0, #8fd3f4); }
.stat-icon.warning { background: linear-gradient(135deg, #f6d365, #fda085); }
.stat-icon.info { background: linear-gradient(135deg, #a8edea, #fed6e3); }

.stat-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 20px;
}

.stat-trend.up {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.stat-trend.down {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 5px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  color: #6c757d;
  font-size: 14px;
}

/* 图表网格 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  position: relative;
  height: 300px;
}

@media (max-width: 1200px) {
    .charts-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .stats-grid {
        grid-template-columns: 1fr;
    }
}
</style>