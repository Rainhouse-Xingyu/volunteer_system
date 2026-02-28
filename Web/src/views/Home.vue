<template>
  <div class="home-container">
    <van-nav-bar title="志愿者活动" fixed placeholder />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div class="activity-grid">
          <div 
            v-for="item in list" 
            :key="item.activityId" 
            class="activity-col"
            @click="toDetail(item.activityId)"
          >
            <div 
              class="activity-card" 
              :class="{ 'is-full': isFull(item) }"
            >
              <div class="card-image-wrapper">
                <img 
                  :src="item.coverImg || defaultImg" 
                  class="card-image" 
                  alt="活动封面"
                />
                <div v-if="isFull(item)" class="full-mask">
                  <span>已满员</span>
                </div>
              </div>
              
              <div class="card-content">
                <div class="card-title van-multi-ellipsis--l2">
                  {{ item.title }}
                </div>
                
                <div class="card-info">
                  <div class="location-row">
                    <van-icon name="location-o" class="location-icon" />
                    <span class="location-text van-ellipsis">{{ item.location }}</span>
                  </div>
                  
                  <div class="tags-row">
                    <van-tag 
                      plain 
                      :type="isFull(item) ? 'default' : 'primary'"
                    >
                      剩余: {{ getRemaining(item) }}
                    </van-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';

const router = useRouter();
const defaultImg = 'https://fastly.jsdelivr.net/npm/@vant/assets/ipad.jpeg';

const list = ref([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const currentPage = ref(1);
const pageSize = 10;

const isFull = (item) => {
  return (item.currentParticipants || 0) >= (item.quota || 0);
};

const getRemaining = (item) => {
  const remain = (item.quota || 0) - (item.currentParticipants || 0);
  return remain > 0 ? remain : 0;
};

const onLoad = async () => {
  if (refreshing.value) {
    list.value = [];
    refreshing.value = false;
  }

  try {
    const res = await request.get('/activity/list', {
      params: {
        current: currentPage.value,
        size: pageSize,
      }
    });

    const data = res.data?.records || [];
    
    // 只有 status === 1 的活动才显示
    // 如果后端不支持 status 参数过滤，我们在前端进行过滤
    const validData = data.filter(item => item.status === 1);

    if (currentPage.value === 1) {
      list.value = validData;
    } else {
      list.value = [...list.value, ...validData];
    }

    loading.value = false;
    
    if (data.length < pageSize) {
      finished.value = true;
    } else {
      currentPage.value++;
    }
  } catch (error) {
    loading.value = false;
    finished.value = true;
    console.error('Failed to load activities:', error);
  }
};

const onRefresh = () => {
  finished.value = false;
  loading.value = true;
  currentPage.value = 1;
  onLoad();
};

const toDetail = (id) => {
  router.push(`/activity/${id}`);
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 20px;
}

/* 瀑布流/双列布局 */
.activity-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
  justify-content: space-between;
}

.activity-col {
  width: 48%;
  margin-bottom: 12px;
}

.activity-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.activity-card:active {
  transform: scale(0.95);
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 60%; /* Aspect Ratio control */
  overflow: hidden;
  background-color: #f0f0f0;
}

.card-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Full state styling */
.activity-card.is-full .card-image {
  filter: grayscale(100%);
}

.full-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  font-size: 16px;
  letter-spacing: 2px;
  z-index: 1;
}

.card-content {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #323233;
  line-height: 20px;
  margin-bottom: 8px;
  height: 40px; /* Force 2 lines height approx */
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.location-row {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #969799;
}

.location-icon {
  margin-right: 4px;
  color: #c8c9cc;
}

.tags-row {
  display: flex;
  align-items: center;
}
</style>
