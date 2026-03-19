<template>
  <div class="my-reviews-container">
    <div class="page-header">
        <h2>我的评价</h2>
        <van-button size="small" type="default" @click="router.back()">返回</van-button>
    </div>
    
    <div class="reviews-content">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
          <van-empty description="暂无评价记录" v-if="finished && list.length === 0" />
  
          <div v-if="list.length > 0" class="review-list">
              <div v-for="item in list" :key="item.commentId" class="review-card">
                  <div class="review-header">
                      <span class="activity-title" @click="router.push(`/activity/${item.activityId}`)">
                          {{ item.activityTitle || '未知活动' }} <van-icon name="arrow" />
                      </span>
                      <span class="review-time">{{ formatTime(item.createdAt) }}</span>
                  </div>
                  <div class="review-body">{{ item.content }}</div>
              </div>
          </div>
      </van-list>
    </div>
  </div>
</template>
                <div class="review-content">{{ item.content }}</div>
            </div>
        </div>
    </van-list>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyComments } from '@/api/comment'
import { showToast } from 'vant'
import dayjs from 'dayjs'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const finished = ref(false)

const onLoad = async () => {
    try {
        // getMyComments currently returns full list
        const res = await getMyComments()
        if (res.code === 200) {
            list.value = res.data || []
        }
        finished.value = true
    } catch (e) {
        showToast('获取评价失败')
        finished.value = true
    } finally {
        loading.value = false
    }
}

const formatTime = (t) => dayjs(t).format('YYYY-MM-DD HH:mm')
</script>

<style scoped>
.my-reviews-container {
    padding: 20px;
    max-width: 1000px;
    margin: 0 auto;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
}
.page-header h2 {
    margin: 0;
    font-size: 24px;
    color: #333;
}
.reviews-content {
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.review-list {
    display: flex;
    flex-direction: column;
}
.review-card {
    padding: 20px;
    border-bottom: 1px solid #eee;
    transition: background 0.2s;
}
.review-card:last-child {
    border-bottom: none;
}
.review-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 15px;
    font-size: 14px;
    color: #999;
}
.activity-title {
    color: #667eea;
    cursor: pointer;
    font-weight: bold;
    display: flex;
    align-items: center;
}
.activity-title:hover {
    text-decoration: underline;
}
.review-time {
    color: #aaa;
}
.review-body {
    color: #333;
    line-height: 1.6;
    font-size: 15px;
}
</style>
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-size: 14px;
}
.activity-title {
    font-weight: bold;
    color: #333;
    display: flex;
    align-items: center;
}
.review-time {
    color: #999;
    font-size: 12px;
}
.review-content {
    font-size: 14px;
    color: #666;
    line-height: 1.5;
}
</style>
