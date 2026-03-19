<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getUserInfo } from '@/api/auth'

const userStore = useUserStore()

onMounted(async () => {
    if (userStore.token && (!userStore.userInfo || !userStore.userInfo.username)) {
        try {
            const res = await getUserInfo()
            if (res.code === 200) {
                userStore.setUserInfo(res.data)
                userStore.setRole(res.data.role)
                userStore.setUserId(res.data.userId)
            }
        } catch (e) {
            console.error('Failed to restore session', e)
        }
    }
})
</script>

<style>
:root {
  --el-color-primary: #409eff;
}

body {
  margin: 0;
  background-color: #f0f2f5;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

#app {
  min-height: 100vh;
}
</style>
