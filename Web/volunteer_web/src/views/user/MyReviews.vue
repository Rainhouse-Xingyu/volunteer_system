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

<style scoped src="@/styles/user-my-reviews.css"></style>
