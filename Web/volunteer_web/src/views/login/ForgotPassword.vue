<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-content">
      <el-card class="login-card">
        <template #header>
          <div class="login-header">
            <h2 class="title" style="margin: 0; font-size: 24px;">重置密码</h2>
            <p class="subtitle" style="margin: 5px 0 0; color: #909399; font-size: 14px;">请输入相关信息进行验证</p>
          </div>
        </template>
        
        <el-form 
          ref="resetFormRef"
          :model="resetForm" 
          :rules="rules"
          size="large"
          @submit.prevent="handleReset"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="resetForm.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="securityAnswer">
            <el-input 
              v-model="resetForm.securityAnswer" 
              placeholder="请输入验证信息(手机号或组织名称)" 
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item prop="newPassword">
            <el-input 
              v-model="resetForm.newPassword" 
              type="password" 
              placeholder="请输入新密码" 
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="resetForm.confirmPassword" 
              type="password" 
              placeholder="请确认新密码" 
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="isLoading" class="login-btn" @click="handleReset" style="width: 100%">
              重置密码
            </el-button>
          </el-form-item>
          
          <div class="register-link" style="text-align: center; margin-top: 10px;">
            <el-link type="primary" :underline="false" @click="toLogin">返回登录</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { resetPassword } from '@/api/auth'

const router = useRouter()
const resetFormRef = ref(null)
const isLoading = ref(false)

const resetForm = reactive({
  username: '',
  securityAnswer: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  securityAnswer: [
    { required: true, message: '请输入验证信息', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度 in 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const handleReset = async () => {
  if (!resetFormRef.value) return
  
  await resetFormRef.value.validate(async (valid) => {
    if (valid) {
      isLoading.value = true
      try {
        const { confirmPassword, ...data } = resetForm
        await resetPassword(data)
        ElMessage.success('密码重置成功，请重新登录')
        router.push('/login')
      } catch (error) {
        console.error(error)
      } finally {
        isLoading.value = false
      }
    }
  })
}

const toLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('@/assets/login-bg.jpg'); /* Ensure this assumes similar structure to Login.vue */
  background-size: cover;
  background-position: center;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1;
}

.login-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-card {
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
}

.title {
  color: #303133;
}
</style>