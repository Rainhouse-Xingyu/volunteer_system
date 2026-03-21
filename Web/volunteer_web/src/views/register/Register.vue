<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-content">
      <el-card class="login-card">
        <template #header>
          <div class="login-header">
            <el-icon :size="40" color="#409EFF" style="margin-bottom: 10px"><Monitor /></el-icon>
            <h2 class="title" style="margin: 0; font-size: 24px;">志愿者服务系统</h2>
            <p class="subtitle" style="margin: 5px 0 0; color: #909399; font-size: 14px;">加入我们，开启志愿之旅</p>
          </div>
        </template>
        
        <el-form :model="form" @submit.prevent="handleRegisterSubmit" size="large">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名 (3-20位)" 
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item>
            <el-input 
              v-model="form.nickname" 
              placeholder="请输入昵称" 
              :prefix-icon="Postcard"
            />
          </el-form-item>

          <el-form-item class="role-select">
            <el-radio-group v-model="form.role" style="width: 100%; display: flex; justify-content: space-between;">
              <el-radio label="VOLUNTEER" border style="margin-right: 0; width: 48%;">我是志愿者</el-radio>
              <el-radio label="ORGANIZER" border style="margin-right: 0; width: 48%;">我是组织者</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请设置密码 (6位以上)" 
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次确认密码" 
              :prefix-icon="Key"
              show-password
            />
          </el-form-item>

          <div class="form-options">
             <el-checkbox v-model="agree">
               我已阅读并同意 <el-link type="primary" :underline="false">《志愿者服务协议》</el-link>
             </el-checkbox>
          </div>

          <el-form-item>
            <el-button type="primary" :loading="isLoading" class="login-btn" @click="handleRegisterSubmit" style="width: 100%">
              立即注册
            </el-button>
          </el-form-item>
          
          <div class="register-link" style="text-align: center; margin-top: 10px;">
            <span style="color: #606266; margin-right: 5px;">已有账号？</span>
            <el-link type="primary" :underline="false" @click="toLogin">立即登录</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRegister } from '@/hooks/useRegister';
import { User, Lock, Monitor, Postcard, Key } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const { form, isLoading, handleRegister, toLogin } = useRegister();
const agree = ref(true);

const handleRegisterSubmit = () => {
  if (!agree.value) {
    ElMessage.warning('请先同意服务协议');
    return;
  }
  handleRegister();
}
</script>

<style scoped src="@/styles/register.css"></style>
