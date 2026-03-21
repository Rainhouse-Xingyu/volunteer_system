<template>
  <div class="user-password-container">
    <div class="page-header">
        <h2>修改密码</h2>
        <van-button size="small" type="default" @click="router.back()">返回</van-button>
    </div>
    
    <van-form @submit="onSubmit" class="password-form">
      <div class="form-content">
        <van-field
          v-model="oldPassword"
          type="password"
          name="oldPassword"
          label="旧密码"
          placeholder="请输入旧密码"
          :rules="[{ required: true, message: '请填写旧密码' }]"
        />
        <van-field
          v-model="newPassword"
          type="password"
          name="newPassword"
          label="新密码"
          placeholder="请输入新密码"
          :rules="[{ required: true, message: '请填写新密码' }]"
        />
        <van-field
          v-model="confirmPassword"
          type="password"
          name="confirmPassword"
          label="确认新密码"
          placeholder="请再次输入新密码"
          :rules="[{ required: true, message: '请确认新密码' }, { validator: validateConfirm, message: '两次密码不一致' }]"
        />
      </div>
      
      <div class="form-actions">
        <van-button type="primary" native-type="submit" :loading="loading" class="submit-btn" size="large">
          修改密码
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { changePassword } from '@/api/user'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)

const validateConfirm = (val) => val === newPassword.value

const onSubmit = async () => {
    showConfirmDialog({
        title: '修改确认',
        message: '确定要修改密码吗？修改后需要重新登录。',
    }).then(async () => {
        loading.value = true
        try {
            const res = await changePassword({
                oldPassword: oldPassword.value,
                newPassword: newPassword.value
            })
            
            if (res.code === 200) {
                showSuccessToast('密码修改成功，请重新登录')
                // User should re-login
                userStore.logout()
                setTimeout(() => router.push('/login'), 1500)
            } else {
                showFailToast(res.message || '密码修改失败，请重试')
            }
        } catch (e) {
            showFailToast('请求异常: ' + (e.message || '未知错误'))
        } finally {
            loading.value = false
        }
    }).catch(() => {
        // Cancelled
    })
}
</script>

<style scoped src="@/styles/user-password.css"></style>
