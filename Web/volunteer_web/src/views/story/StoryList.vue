<template>
  <div class="stories-container">
    <div class="page-header">
      <h2>志愿故事与资讯</h2>
      <p class="subtitle">发现身边的感动，了解最新志愿活动资讯</p>
    </div>

    <el-row :gutter="24">
      <el-col :span="16">
        <!-- Story List -->
        <div class="story-list">
          <el-skeleton :rows="5" animated v-if="loading" />
          <div v-else-if="stories.length > 0">
             <el-card v-for="story in stories" :key="story.newsId" shadow="hover" class="story-card" @click="viewStory(story.newsId)">
               <div class="story-wrapper">
                 <div class="story-content">
                    <div class="story-meta">
                      <el-tag size="small" :type="story.type === '活动回顾' ? 'success' : 'primary'">{{ story.category || '志愿资讯' }}</el-tag>
                      <span class="story-author">{{ story.publisher || '管理员' }}</span>
                      <span class="story-date">{{ formatDate(story.createdAt || story.createTime) }}</span>
                    </div>
                    <h3 class="story-title">{{ story.title }}</h3>
                    <p class="story-excerpt">{{ story.summary || (story.content ? story.content.substring(0, 100) + '...' : '') }}</p>
                    <div class="story-footer">
                        <span class="read-count"><el-icon><View /></el-icon> {{ story.views || 0 }} 阅读</span>
                        <!-- <span class="like-count"><el-icon><thumb-up /></el-icon> {{ story.likes || 0 }} 点赞</span> -->
                    </div>
                 </div>
                 <div class="story-cover" v-if="story.coverImage">
                   <img :src="story.coverImage" alt="cover" />
                 </div>
               </div>
             </el-card>
             
             <!-- Pagination -->
             <div class="pagination-container" v-if="total > 0">
                <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="total"
                  :page-size="queryParams.pageSize"
                  v-model:current-page="queryParams.pageNum"
                  @current-change="handlePageChange"
                />
             </div>
          </div>
          <el-empty v-else description="暂无志愿故事" />
        </div>
      </el-col>
      
      <el-col :span="8">
        <!-- Sidebar items -->
        <el-card shadow="never" class="sidebar-card">
          <template #header>
            <div class="card-header">
              <span>热门分类</span>
            </div>
          </template>
          <div class="tag-cloud">
             <el-tag 
                v-for="tag in categories" 
                :key="tag" 
                class="cloud-tag" 
                :effect="queryParams.category === tag ? 'dark' : 'plain'" 
                round
                @click="filterByCategory(tag)"
             >
                {{ tag }}
             </el-tag>
          </div>
        </el-card>

        <el-card shadow="never" class="sidebar-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>近期活动推荐</span>
            </div>
          </template>
           <ul class="recent-activities">
             <li v-for="item in recentActivities" :key="item.id">
               <router-link :to="`/activity/${item.id}`">{{ item.name }}</router-link>
             </li>
           </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getNewsList } from '@/api/news'
import { View } from '@element-plus/icons-vue'
import { getActivityList } from '@/api/activity' // Assuming this exists or I need to mock

const router = useRouter()
const loading = ref(false)
const stories = ref([])
const total = ref(0)
const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    category: undefined
})

const categories = ref(['全部', '活动回顾', '人物故事', '技能科普', '志愿通知', '政策解读'])
const recentActivities = ref([])

const fetchStories = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (params.category === '全部') {
        delete params.category
    }
    const res = await getNewsList(params)
    // Adjust based on actual API response structure
    if (res.code === 200) {
        // Handle if data is directly an array or object with list
        if (Array.isArray(res.data)) {
            stories.value = res.data
            total.value = res.data.length
        } else {
            stories.value = res.data.list || res.data.records || []
            total.value = res.data.total || 0
        }
    }
  } catch (error) {
    console.error('Failed to fetch stories:', error)
  } finally {
    loading.value = false
  }
}

const fetchRecentActivities = async () => {
    // Mock for now until we confirm activity API
    recentActivities.value = [
        { id: 1, name: '社区环保清理活动' },
        { id: 2, name: '敬老院慰问演出' },
        { id: 3, name: '图书馆图书整理' }
    ]
}

const viewStory = (id) => {
  router.push(`/story/${id}`)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const handlePageChange = (val) => {
    queryParams.pageNum = val
    fetchStories()
}

const filterByCategory = (tag) => {
    queryParams.category = tag
    queryParams.pageNum = 1
    fetchStories()
}

onMounted(() => {
  fetchStories()
  fetchRecentActivities()
})
</script>

<style scoped src="@/styles/story-list.css"></style>