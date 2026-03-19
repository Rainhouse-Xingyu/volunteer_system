<template>
  <div class="home-page">
    <div class="page-header">
      <h2>志愿者服务平台</h2>
      <div class="user-welcome">欢迎您，{{ userStore.userInfo?.realName || userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
    </div>

    <div class="pc-content-container">
      <van-tabs v-model:active="activeTab" color="#1989fa" background="transparent" title-active-color="#1989fa" class="custom-tabs">
        <!-- Tab 0: Market (All Roles) -->
        <van-tab title="全部活动">
           <div class="activity-grid">
               <div 
                   v-for="item in list" 
                   :key="item.activityId" 
                   class="activity-card"
                   @click="toDetail(item)"
               >
                   <div class="card-image">
                       <van-image :src="item.coverUrl || defaultImg" fit="cover" width="100%" height="100%" />
                       <div class="card-status-tag" :class="'status-' + item.status">
                           {{ getStatusText(item.status) }}
                       </div>
                   </div>
                   <div class="card-body">
                       <h3 class="card-title">{{ item.title }}</h3>
                       <p class="card-desc">{{ item.description }}</p>
                       <div class="card-meta">
                           <span class="meta-item">
                               <van-icon name="friends-o" /> {{ item.currentParticipants }}/{{ item.quota }}
                           </span>
                           <span class="meta-item" v-if="item.rewardPoints">
                               <van-icon name="gold-coin-o" /> {{ item.rewardPoints }}积分
                           </span>
                       </div>
                       
                       <div class="card-actions">
                           <van-button size="small" type="primary" block v-if="role === 'VOLUNTEER' && item.status === 1" @click.stop="handleRegister(item)">立即报名</van-button>
                           <van-button size="small" plain block type="primary" v-else @click.stop="toDetail(item)">查看详情</van-button>
                       </div>
                   </div>
                   <div class="card-footer-info">
                        <van-tag plain type="primary">{{ item.category }}</van-tag>
                        <span class="end-time">截止: {{ formatTime(item.endTime) }}</span>
                   </div>
               </div>
           </div>
           
           <div v-if="list.length === 0 && !loading" class="empty-state">
                <van-empty description="暂无活动" />
           </div>
           
           <div class="load-more-container" v-if="!finished">
                <van-button plain type="primary" :loading="loading" @click="onLoad">加载更多</van-button>
           </div>
        </van-tab>
        
        <!-- Tab 1: Organizer - My Activities -->
        <van-tab title="我发布的" v-if="role === 'ORGANIZER'">
            <div class="activity-grid">
                <div v-for="item in myList" :key="item.activityId" class="activity-card" @click="toDetail(item)">
                    <div class="card-image">
                       <van-image :src="item.coverUrl || defaultImg" fit="cover" width="100%" height="100%" />
                       <div class="card-status-tag" :class="'status-' + item.status">
                           {{ getStatusText(item.status) }}
                       </div>
                   </div>
                   <div class="card-body">
                       <h3 class="card-title">{{ item.title }}</h3>
                       <div class="card-actions vertical">
                           <van-button size="small" type="primary" block v-if="[1,2].includes(item.status)" @click.stop="showQRCode(item)">显示签到码</van-button>
                           <van-button size="small" plain type="primary" block @click.stop="handleManage(item)">人员管理</van-button>
                       </div>
                   </div>
                </div>
            </div>
             <div class="load-more-container" v-if="!myFinished">
                <van-button plain type="primary" :loading="myLoading" @click="onLoadMy">加载更多</van-button>
           </div>
        </van-tab>
        
        <!-- Tab 1: Admin - Pending Audit -->
        <van-tab title="待审核" v-if="role === 'ADMIN'">
             <div class="activity-grid">
                <div v-for="item in myList" :key="item.activityId" class="activity-card" @click="toDetail(item)">
                    <div class="card-image">
                       <van-image :src="item.coverUrl || defaultImg" fit="cover" width="100%" height="100%" />
                       <div class="card-status-tag status-0">待审核</div>
                   </div>
                   <div class="card-body">
                       <h3 class="card-title">{{ item.title }}</h3>
                       <div class="organizer-name">发布者: {{ item.organizerName || '未知' }}</div>
                       <div class="card-actions">
                           <van-button size="small" type="primary" block @click.stop="toDetail(item)">去审核</van-button>
                       </div>
                   </div>
                </div>
            </div>
             <div class="load-more-container" v-if="!myFinished">
                <van-button plain type="primary" :loading="myLoading" @click="onLoadMy">加载更多</van-button>
           </div>
        </van-tab>
        
        <!-- Tab 1: Volunteer - My Registrations -->
         <van-tab title="我的报名" v-if="role === 'VOLUNTEER'">
            <div class="activity-grid">
                <div v-for="reg in myRegistrations" :key="reg.regId" class="activity-card" @click="router.push(`/activity/${reg.activityId}`)">
                    <div class="card-image">
                       <van-image :src="reg.coverUrl || defaultImg" fit="cover" width="100%" height="100%" />
                       <div class="card-status-tag" :class="reg.checkinStatus === 1 ? 'status-success' : 'status-0'">
                           {{ reg.checkinStatus === 1 ? '已签到' : formatStatus(reg.regStatus) }}
                       </div>
                   </div>
                   <div class="card-body">
                       <h3 class="card-title">{{ reg.title || reg.activityTitle }}</h3>
                       <div class="card-meta">
                           积分: {{ reg.rewardPoints ? reg.rewardPoints : '公益' }}
                       </div>
                       <div class="card-actions">
                           <van-button size="small" plain block type="primary" @click.stop="router.push(`/activity/${reg.activityId}`)">查看详情</van-button>
                       </div>
                   </div>
                </div>
            </div>
         </van-tab>

      </van-tabs>
    </div>

    <!-- Floating Check-in Button (Volunteer) -->
    <div v-if="role === 'VOLUNTEER'" class="fab-btn" @click="openScanner" title="扫码签到">
      <van-icon name="scan" size="24" color="white" />
    </div>

    <!-- Scanner Overlay -->
    <div v-if="showScanner" class="scanner-overlay" @click.self="closeScanner">
        <div class="scanner-container">
            <van-button class="close-scanner-btn" icon="cross" round size="small" @click="closeScanner" />
            <h3 style="margin: 0 0 10px;">扫码签到</h3>
            <div class="scan-tip">请扫描活动现场二维码进行签到</div>
            <div id="reader" class="scanner-box"></div>
        </div>
    </div>

    <!-- QR Code Dialog -->
    <van-dialog v-model:show="showQR" title="活动签到码" confirm-button-text="关闭">
        <div style="text-align: center; padding: 20px;">
            <img :src="qrCodeUrl" style="width: 200px; height: 200px;" />
            <p style="color:#666; margin-top:10px;">请展示给志愿者扫码</p>
        </div>
    </van-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getActivityList, registerActivity, checkIn, getMyRegistrations, getMyActivities } from '@/api/activity'
import { postComment } from '@/api/comment'
import { generateSignToken } from '@/hooks/useOrganizer'
import { showToast, showSuccessToast, showFailToast } from 'vant';
import { Html5Qrcode } from "html5-qrcode"
import QRCode from 'qrcode'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const role = computed(() => userStore.role || 'VOLUNTEER')
const activeTab = ref(0)
const defaultImg = 'https://fastly.jsdelivr.net/npm/@vant/assets/ipad.jpeg'

/* Tab 0: Market */
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 12 // Grid fitting

/* Tab 1: Specifics */
const myList = ref([])
const myLoading = ref(false)
const myFinished = ref(false)
const myPage = ref(1)

/* Volunteer Registrations */
const myRegistrations = ref([])

const loadData = async (isLoadMore = false) => {
    if(!isLoadMore) {
        page.value = 1
        list.value = []
        finished.value = false
    }
    loading.value = true
    try {
        const res = await getActivityList(page.value, pageSize);
        if(res.code === 200) {
            const records = res.data.records || []
            if (records.length > 0) {
                list.value.push(...records);
                page.value++;
                if(records.length < pageSize) finished.value = true
            } else {
                finished.value = true;
            }
        } else {
            finished.value = true
        }
    } catch(err) {
        finished.value = true;
    } finally {
        loading.value = false;
    }
}

const onLoad = () => {
    loadData(true)
}

const loadMyData = async (isLoadMore = false) => {
    if(role.value === 'VOLUNTEER') {
        loadMyRegistrations()
        return
    }

    if(!isLoadMore) {
        myPage.value = 1
        myList.value = []
        myFinished.value = false
    }
    myLoading.value = true
    try {
        let res = null;
        if(role.value === 'ORGANIZER') {
             res = await getMyActivities(myPage.value, pageSize);
        } else if(role.value === 'ADMIN') {
             res = await getActivityList(myPage.value, pageSize, { status: 0 });
        }

        if(res && res.code === 200) {
            const records = res.data.records || []
            if (records.length > 0) {
                myList.value.push(...records);
                myPage.value++;
                 if(records.length < pageSize) myFinished.value = true
            } else {
                myFinished.value = true;
            }
        } else {
            myFinished.value = true
        }
    } catch(err) {
        myFinished.value = true;
    } finally {
        myLoading.value = false;
    }
}

const onLoadMy = () => {
    loadMyData(true)
}

const loadMyRegistrations = async () => {
    if(role.value !== 'VOLUNTEER') return;
    try {
        const res = await getMyRegistrations(1, 50); 
        if(res.code === 200) {
            myRegistrations.value = res.data.records;
        }
    } catch(e){}
}

const handleRegister = async (item) => {
    try {
        const res = await registerActivity(item.activityId);
        if(res.code === 200) {
            showSuccessToast('报名成功');
            loadMyRegistrations();
        } else {
            showFailToast(res.message || '报名失败');
        }
    } catch(e) { console.error(e) }
}

onMounted(() => {
    loadData()
    if(role.value !== 'VOLUNTEER' || activeTab.value === 1) {
        loadMyData()
    }
})

watch(activeTab, (val) => {
    if(val === 1) {
        loadMyData()
    }
})

const toDetail = (item) => {
    router.push(`/activity/${item.activityId}`);
}

const handleManage = (item) => {
    router.push(`/activity/manage/${item.activityId}`);
}

// --- Organizer QR Logic ---
const showQR = ref(false)
const qrCodeUrl = ref('')

const showQRCode = async (item) => {
    try {
        const res = await generateSignToken(item.activityId);
        if(res.code === 200) {
             const data = JSON.stringify({
                 activityId: item.activityId,
                 signToken: res.data
             });
             qrCodeUrl.value = await QRCode.toDataURL(data);
             showQR.value = true;
        } else {
            showFailToast(res.message);
        }
    } catch(e) { showFailToast('请求失败'); }
}

// --- Volunteer Scanner Logic ---
const showScanner = ref(false)
let html5QrCode = null

const openScanner = () => {
    showScanner.value = true
    setTimeout(startScan, 500)
}

const startScan = () => {
    html5QrCode = new Html5Qrcode("reader");
    html5QrCode.start(
        { facingMode: "environment" },
        { fps: 10, qrbox: { width: 250, height: 250 } },
        onScanSuccess,
        (err) => {}
    ).catch(err => showFailToast('相机启动失败'));
}

const closeScanner = () => {
    if(html5QrCode && html5QrCode.isScanning) {
        html5QrCode.stop().then(() => html5QrCode.clear()).catch(e=>{});
    }
    showScanner.value = false
}

const onScanSuccess = (decodedText) => {
    closeScanner();
    try {
        const data = JSON.parse(decodedText); // Should match QR generation
        doCheckIn(data);
    } catch(e) {
        // Fallback for simple token
        doCheckIn({ signToken: decodedText })
    }
}

const doCheckIn = async (data) => {
    try {
        const res = await checkIn(data); // data needs activityId? 
        // Logic might need check: checkIn API usually takes {activityId, signToken}
        // If QR only has {activityId, signToken} it's fine.
        if(res.code === 200) {
            showSuccessToast('签到成功!');
            loadMyRegistrations();
        } else {
            showFailToast(res.message || '签到失败');
        }
    } catch(e) { showFailToast('网络请求失败'); }
}

onUnmounted(() => {
    if(html5QrCode && html5QrCode.isScanning) {
        html5QrCode.stop()
    }
})

// Formatting
const formatTime = (t) => dayjs(t).format('YYYY-MM-DD HH:mm')
const getStatusText = (s) => {
    const map = { 0:'待审核', 1:'报名中', 2:'进行中', 3:'已结束', 4:'审核未过' }
    return map[s] || '未知'
}
const formatStatus = (s) => {
    const map = {0:'审核中', 1:'已录用', 2:'已拒绝', 3:'已取消'};
    return map[s] || '未知';
}
</script>

<style scoped>
.home-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  background: #fff;
  height: 80px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.pc-content-container {
  max-width: 1200px; /* Wider for grid */
  margin: 0 auto;
  padding: 0 20px;
}

.activity-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    padding: 20px 0;
}

.activity-card {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    transition: transform 0.2s, box-shadow 0.2s;
    cursor: pointer;
    display: flex;
    flex-direction: column;
}

.activity-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.card-image {
    height: 160px;
    position: relative;
    background: #eee;
}

.card-status-tag {
    position: absolute;
    top: 10px;
    right: 10px;
    background: rgba(0,0,0,0.6);
    color: #fff;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
}
.card-status-tag.status-1 { background: #07c160; } /* 报名中 */
.card-status-tag.status-3 { background: #999; } /* 结束 */
.card-status-tag.status-success { background: #07c160; }

.card-body {
    padding: 15px;
    display: flex;
    flex-direction: column;
    flex: 1;
}

.card-title {
    margin: 0 0 8px;
    font-size: 16px;
    font-weight: 600;
    line-height: 1.4;
    height: 44px; /* 2 lines */
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.card-desc {
    color: #999;
    font-size: 13px;
    margin-bottom: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.card-meta {
    display: flex;
    gap: 15px;
    font-size: 13px;
    color: #666;
    margin-bottom: 15px;
}
.meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
}

.card-actions {
    margin-top: auto;
}
.card-actions.vertical {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.card-footer-info {
    padding: 10px 15px;
    background: #fcfcfc;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.end-time {
    font-size: 12px;
    color: #999;
}

.load-more-container {
    text-align: center;
    padding: 20px 0;
}

.fab-btn {
    position: fixed;
    bottom: 40px;
    right: 40px;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: #1989fa;
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 4px 15px rgba(25, 137, 250, 0.4);
    cursor: pointer;
    z-index: 100;
    transition: transform 0.2s;
}
.fab-btn:hover {
    transform: scale(1.1);
}

/* Scanner */
.scanner-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 2000;
}
.scanner-container {
    background: #fff;
    padding: 20px;
    border-radius: 12px;
    width: 400px;
    text-align: center;
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
}
.scanner-box {
    width: 300px;
    height: 300px;
    background: #000;
    margin-top: 20px;
}
.close-scanner-btn {
    position: absolute;
    top: 15px;
    right: 15px;
}
</style>