<template>
  <div class="stories-container">
    <div class="page-header">
      <h2>志愿故事</h2>
      <p class="subtitle">发现身边的感动，分享志愿者的精彩瞬间</p>
    </div>

    <el-row :gutter="24">
      <el-col :span="16">
        <!-- Story List -->
        <div class="story-list">
          <el-skeleton :rows="5" animated v-if="loading" />
          <div v-else>
             <el-card v-for="story in stories" :key="story.id" shadow="hover" class="story-card" @click="viewStory(story)">
               <div class="story-wrapper">
                 <div class="story-content">
                    <div class="story-meta">
                      <el-tag size="small" :type="story.type === '活动回顾' ? 'success' : 'primary'">{{ story.type }}</el-tag>
                      <span class="story-author">{{ story.author }}</span>
                      <span class="story-date">{{ story.publishDate }}</span>
                    </div>
                    <h3 class="story-title">{{ story.title }}</h3>
                    <p class="story-excerpt">{{ story.excerpt }}</p>
                    <div class="story-footer">
                        <span class="read-count"><el-icon><View /></el-icon> {{ story.views }} 阅读</span>
                        <span class="like-count"><el-icon><thumb-up /></el-icon> {{ story.likes }} 点赞</span>
                    </div>
                 </div>
                 <div class="story-cover" v-if="story.cover">
                   <img :src="story.cover" alt="cover" />
                 </div>
               </div>
             </el-card>
          </div>
        </div>
      </el-col>
      
      <el-col :span="8">
        <!-- Sidebar items -->
        <el-card shadow="never" class="sidebar-card">
          <template #header>
            <div class="card-header">
              <span>热门标签</span>
            </div>
          </template>
          <div class="tag-cloud">
             <el-tag v-for="tag in tags" :key="tag" class="cloud-tag" effect="plain" round>{{ tag }}</el-tag>
          </div>
        </el-card>

         <el-card shadow="never" class="sidebar-card">
          <template #header>
            <div class="card-header">
              <span>推荐组织者</span>
            </div>
          </template>
          <div class="organizer-list">
             <div v-for="org in organizers" :key="org.id" class="org-item">
                <el-avatar :size="32" :src="org.avatar">{{ org.name.charAt(0) }}</el-avatar>
                <div class="org-info">
                   <div class="org-name">{{ org.name }}</div>
                   <div class="org-desc">{{ org.desc }}</div>
                </div>
             </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Story Detail Dialog -->
    <el-dialog v-model="showDetail" :title="currentStory?.title" width="60%" center>
        <div class="story-detail-content" v-if="currentStory">
            <div class="detail-meta">
                 <span>发布于 {{ currentStory.publishDate }}</span>
                 <span>作者: {{ currentStory.author }}</span>
            </div>
             <el-divider />
            <div class="detail-body">
                <img v-if="currentStory.cover" :src="currentStory.cover" class="detail-cover" />
                <p v-html="currentStory.content"></p>
            </div>
        </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { View, ThumbUp } from '@element-plus/icons-vue'

const loading = ref(true)
const stories = ref([])
const tags = ['环境保护', '支教', '社区服务', '动物救助', '老人关怀', '疫情防控', '国际志愿者']
const organizers = ref([
    { id: 1, name: '爱心社', desc: '致力于社区公益服务', avatar: '' },
    { id: 2, name: '绿色地球', desc: '关注环境保护与可持续发展', avatar: '' },
    { id: 3, name: '阳光助学', desc: '资助贫困山区儿童教育', avatar: '' }
])

const showDetail = ref(false)
const currentStory = ref(null)

const mockStories = [
    {
        id: 1,
        title: '【回顾】2025春季社区清洁日活动圆满结束',
        author: '爱心社',
        publishDate: '2025-03-15',
        type: '活动回顾',
        excerpt: '上周末，我们在阳光社区举办了春季清洁日活动，超过50名志愿者参与其中，清理垃圾超过200公斤。感谢每一位志愿者的辛勤付出！',
        content: `
            <p>上周末，我们在阳光社区举办了春季清洁日活动，超过50名志愿者参与其中，清理垃圾超过200公斤。</p>
            <p>活动从早上9点开始，大家分组行动，有的负责清扫街道，有的负责清理花坛中的杂物。尽管天气炎热，但大家的热情丝毫不减。</p>
            <p>居民们也纷纷加入，为志愿者送来矿泉水和水果。这次活动不仅改善了社区环境，更拉近了邻里之间的距离。</p>
            <p>感谢每一位志愿者的辛勤付出！期待下一次相聚！</p>
        `,
        views: 1205,
        likes: 342,
        cover: 'https://images.unsplash.com/photo-1559027615-cd4628902d4a?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60'
    },
    {
        id: 2,
        title: '志愿者心声：我在山区支教的那一个月',
        author: '阳光助学',
        publishDate: '2025-03-10',
        type: '人物故事',
        excerpt: '刚到大山深处时，我也曾感到迷茫和不适。但当看到孩子们清澈的眼神渴求知识的样子，一切困难都变得不再重要。',
        content: `
            <p>刚到大山深处时，我也曾感到迷茫和不适。这里没有便利店，没有外卖，甚至网络信号都时断时续。</p>
            <p>但当看到孩子们清澈的眼神渴求知识的样子，一切困难都变得不再重要。我们一起晨读，一起做游戏，一起看大山外面的世界。</p>
            <p>这一个月，我教会了他们英语单词，他们教会了我什么是纯粹的快乐和坚韧。</p>
        `,
        views: 890,
        likes: 512,
        cover: 'https://images.unsplash.com/photo-1529390079861-591de354faf5?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60'
    },
    {
        id: 3,
        title: '如何成为一名优秀的急救志愿者？',
        author: '红十字急救队',
        publishDate: '2025-03-05',
        type: '技能科普',
        excerpt: '在紧急情况下，黄金四分钟往往决定了生死。掌握基础的急救知识，不仅能自救，更能救人。本文将分享几个关键的急救技巧。',
        content: `
             <p>在紧急情况下，黄金四分钟往往决定了生死。掌握基础的急救知识，不仅能自救，更能救人。</p>
             <h4>1. 保持冷静</h4>
             <p>遇到突发状况，首先要深呼吸，确认环境安全。</p>
             <h4>2. 判断意识</h4>
             <p>轻拍伤者双肩，大声呼唤，观察有无反应。</p>
             <h4>3. 心肺复苏(CPR)</h4>
             <p>如果伤者无呼吸无意识，应立即进行CPR。</p>
        `,
        views: 2300,
        likes: 880,
        cover: null
    }
]

const viewStory = (item) => {
    currentStory.value = item
    showDetail.value = true
}

onMounted(() => {
    // Simulate API delay
    setTimeout(() => {
        stories.value = mockStories
        loading.value = false
    }, 800)
})
</script>

<style scoped>
.stories-container {
    padding: 24px;
}
.page-header {
    margin-bottom: 32px;
}
.page-header h2 {
    font-size: 24px;
    margin-bottom: 8px;
    font-weight: 600;
}
.subtitle {
  color: #909399;
  font-size: 14px;
}

.story-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
}
.story-card:hover {
    transform: translateY(-2px);
}

.story-wrapper {
    display: flex;
    justify-content: space-between;
}
.story-content {
    flex: 1;
    padding-right: 20px;
}
.story-cover {
    width: 200px;
    height: 140px;
    border-radius: 6px;
    overflow: hidden;
    flex-shrink: 0;
}
.story-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.story-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    font-size: 12px;
    color: #909399;
}
.story-author {
    color: #606266;
    font-weight: 500;
}

.story-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 10px;
    color: #303133;
    line-height: 1.4;
}
.story-excerpt {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 16px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.story-footer {
    display: flex;
    gap: 20px;
    color: #909399;
    font-size: 13px;
}
.story-footer span {
    display: flex;
    align-items: center;
    gap: 4px;
}

/* Sidebar */
.sidebar-card {
    margin-bottom: 24px;
    border-radius: 8px;
}
.card-header {
    font-weight: 600;
}
.cloud-tag {
    margin: 4px;
    cursor: pointer;
}
.cloud-tag:hover {
    background-color: #ecf5ff;
    color: #409eff;
}

.org-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
}
.org-item:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
}
.org-name {
    font-weight: 500;
    font-size: 14px;
}
.org-desc {
    color: #909399;
    font-size: 12px;
    margin-top: 2px;
}

/* Detail */
.detail-meta {
    color: #909399;
    font-size: 13px;
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
}
.detail-cover {
    width: 100%;
    max-height: 400px;
    object-fit: cover;
    border-radius: 8px;
    margin-bottom: 24px;
}
.detail-body {
    line-height: 1.8;
    color: #303133;
    font-size: 16px;
}
.detail-body :deep(p) {
    margin-bottom: 16px;
}
</style>