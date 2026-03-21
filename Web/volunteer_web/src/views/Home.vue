<template>
  <div class="opportunities-container">
    <div class="page-header">
      <h2>志愿活动</h2>
      <p class="subtitle">发现并参与符合您兴趣的志愿活动。</p>
    </div>

    <!-- Search & Filter -->
    <el-card shadow="never" class="filter-bar">
      <el-row :gutter="20" align="middle" class="filter-row">
        <el-col :xs="24" :sm="16" :md="18">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索活动..."
            prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            size="large"
            class="filter-input"
          />
        </el-col>
        <el-col :xs="24" :sm="8" :md="6" class="mt-mobile">
           <el-dropdown trigger="click" @command="handleCategory" style="width: 100%">
            <el-button size="large" style="width: 100%; justify-content: space-between;">
              {{ selectedCategoryLabel || '所有类别' }} <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="">所有类别</el-dropdown-item>
                <el-dropdown-item command="Environment">环境保护</el-dropdown-item>
                <el-dropdown-item command="Education">教育</el-dropdown-item>
                <el-dropdown-item command="Food">食品营养</el-dropdown-item>
                <el-dropdown-item command="Animal">动物福利</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
      </el-row>
    </el-card>

    <div class="results-info">显示 {{ total }} 个机会</div>

    <!-- Activity List -->
    <el-row :gutter="24" v-loading="loading">
      <el-col :xs="24" :sm="12" :md="12" :lg="12" v-for="item in activityList" :key="item.activityId" style="margin-bottom: 24px;">
        <el-card class="opportunity-card" shadow="hover" :body-style="{ padding: '24px', display: 'flex', flexDirection: 'column', height: '100%' }">
          <div class="card-content">
            <div class="card-top">
                <div class="icon-section">
                  <div class="category-icon" :class="getCategoryClass(categoryMap[item.categoryId] || 'Default')">
                    <el-icon v-if="categoryMap[item.categoryId] === 'Environment'"><Sunny /></el-icon>
                    <el-icon v-else-if="categoryMap[item.categoryId] === 'Education'"><School /></el-icon>
                    <el-icon v-else><Help /></el-icon>
                  </div>
                </div>
                <el-tag effect="plain" round>{{ categoryMap[item.categoryId] || '通用' }}</el-tag>
            </div>

            <h3 class="activity-title" @click="toDetail(item.activityId)">{{ item.title }}</h3>
            <p class="org-name">{{ item.organizerName || '校园志愿者协会' }}</p>
            
            <p class="description">{{ item.description }}</p>

            <div class="meta-info">
                <div class="meta-row"><el-icon><Calendar /></el-icon> {{ formatDate(item.startTime) }}</div>
                <div class="meta-row"><el-icon><Clock /></el-icon> {{ formatTimeRange(item.startTime, item.endTime) }}</div>
                <div class="meta-row"><el-icon><Location /></el-icon> {{ item.location }}</div>
                <div class="meta-row"><el-icon><User /></el-icon> {{ item.currentParticipants || 0 }}/{{ item.quota }} 已报名</div>
            </div>

            <el-button type="primary" class="signup-btn" @click="toDetail(item.activityId)">立即报名</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
     <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, Calendar, Clock, Location, User, 
  ArrowDown, School, Help, Sunny 
} from '@element-plus/icons-vue'
import { getActivityList } from '@/api/activity' 

const router = useRouter()
const searchKeyword = ref('')
const selectedCategory = ref('')
const activityList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)
const defaultImg = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg';

const categoryMap = {
    1: 'Environment',
    2: 'Education',
    3: 'Food',
    4: 'Animal'
}

const categoryLabels = {
    'Environment': '环境保护',
    'Education': '教育',
    'Food': '食品营养',
    'Animal': '动物福利'
}

const selectedCategoryLabel = computed(() => categoryLabels[selectedCategory.value])

const handleCategory = (cmd) => {
    selectedCategory.value = cmd;
    handleSearch();
}

const fetchActivities = async () => {
    loading.value = true;
    try {
        const res = await getActivityList(currentPage.value, pageSize.value, { 
            title: searchKeyword.value, 
            categoryId: getCategoryId(selectedCategory.value) 
        });
        if (res.code === 200) {
           activityList.value = res.data.records; 
           total.value = res.data.total;
        }
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
}

const getCategoryId = (catName) => {
    for(let [id, name] of Object.entries(categoryMap)) {
        if(name === catName) return id;
    }
    return null;
}

const handleSearch = () => {
    currentPage.value = 1;
    fetchActivities();
}

const handleCurrentChange = (val) => {
    currentPage.value = val;
    fetchActivities();
}

const toDetail = (id) => {
    router.push(`/activity/${id}`);
}

const formatDate = (dateStr) => {
    if (!dateStr) return '待定';
    return dateStr.split(' ')[0]; 
}

const formatTimeRange = (start, end) => {
    if(!start || !end) return '待定';
    try {
        const s = start.split(' ')[1].substring(0,5);
        const e = end.split(' ')[1].substring(0,5);
        return `${s} - ${e}`;
    } catch(e) {
        return '全天';
    }
}

const getCategoryClass = (cat) => {
    if (cat === 'Environment') return 'env-bg';
    if (cat === 'Education') return 'edu-bg';
    return 'default-bg';
}

onMounted(() => {
    fetchActivities();
})
</script>

<style scoped src="@/styles/home.css"></style>
