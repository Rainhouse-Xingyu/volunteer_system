<template>
  <div class="home-container">
    <van-nav-bar title="最新活动" fixed placeholder>
      <template #right>
        <van-icon name="user-circle-o" size="24" @click="toProfile" />
      </template>
    </van-nav-bar>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div class="activity-list">
          <van-card
            v-for="item in list"
            :key="item.activityId"
            :title="item.title"
            :thumb="item.coverImg || 'https://fastly.jsdelivr.net/npm/@vant/assets/ipad.jpeg'"
            @click="toDetail(item.activityId)"
          >
            <template #desc>
              <div class="card-desc">
                <div class="desc-row">
                  <van-icon name="location-o" /> {{ item.location }}
                </div>
                <div class="desc-row">
                  <van-icon name="clock-o" /> {{ formatTime(item.startTime) }}
                </div>
              </div>
            </template>
            
            <template #tags>
              <div class="card-tags">
                <van-tag plain type="primary" style="margin-right: 5px;">
                  {{ getStatusText(item.status) }}
                </van-tag>
                <van-tag plain type="warning">
                  剩余: {{ (item.quota || 0) - (item.currentParticipants || 0) }}
                </van-tag>
              </div>
            </template>

            <template #footer>
              <van-button 
                size="mini" 
                type="primary" 
                @click.stop="onRegister(item.activityId)"
                v-if="item.status === 1"
              >
                立即报名
              </van-button>
              <van-button 
                size="mini" 
                disabled 
                v-else
              >
                {{ getStatusText(item.status) }}
              </van-button>
            </template>
          </van-card>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showLoadingToast, closeToast, showSuccessToast } from 'vant';
import request from '@/utils/request';

const router = useRouter();
const list = ref([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const currentPage = ref(0); 
const pageSize = 10;

// 状态字典
const statusMap = {
  0: '待审核',
  1: '招募中',
  2: '进行中',
  3: '已结束',
  4: '审核失败'
};

const getStatusText = (status) => statusMap[status] || '未知';

// 简单的时间格式化
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(0, 16);
};

// 加载数据
const onLoad = async () => {
  currentPage.value++;
  if (refreshing.value) {
    list.value = [];
    refreshing.value = false;
    currentPage.value = 1;
  }

  try {
    const res = await request.get('/activity/list', {
      params: {
        current: currentPage.value,
        size: pageSize
      }
    });
    
    const pageData = res.data || {};
    const records = pageData.records || [];
    
    if (records.length < pageSize) {
      finished.value = true;
    }
    
    if (currentPage.value === 1) {
      list.value = records;
    } else {
      list.value = [...list.value, ...records];
    }
  } catch (error) {
    finished.value = true;
  } finally {
    loading.value = false;
  }
};

// 下拉刷新
const onRefresh = () => {
  finished.value = false;
  loading.value = true;
  onLoad();
};

// 跳转详情
const toDetail = (id) => {
  router.push(`/activity/${id}`);
};

// 个人中心
const toProfile = () => {
  router.push('/profile');
};

// 报名
const onRegister = async (id) => {
  showLoadingToast({
    message: '报名中...',
    forbidClick: true,
  });
  
  try {
    await request.post(`/activity/register/${id}`);
    showSuccessToast('报名成功');
    // 刷新列表更新数据
    onRefresh();
  } catch (error) {
    // 错误已处理
  }
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 20px;
}

.activity-list {
  padding: 10px 0;
}

.van-card {
  background-color: #ffffff;
  margin: 10px 12px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05); 
}

.card-desc {
  margin-top: 5px;
  color: #666;
  font-size: 12px;
}

.desc-row {
  display: flex;
  align-items: center;
  margin-top: 4px;
}

.desc-row .van-icon {
  margin-right: 4px;
}

.card-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
}

:deep(.van-card__title) {
  font-size: 16px;
  font-weight: bold;
  line-height: 20px;
  margin-bottom: 5px;
}

:deep(.van-card__thumb) {
  border-radius: 4px;
  overflow: hidden;
}
</style>
