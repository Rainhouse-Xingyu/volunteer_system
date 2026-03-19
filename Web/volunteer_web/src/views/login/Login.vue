<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-content">
      <el-card class="login-card">
        <template #header>
          <div class="login-header">
            <el-icon :size="40" color="#409EFF" style="margin-bottom: 10px"><Monitor /></el-icon>
            <h2 class="title" style="margin: 0; font-size: 24px;">志愿者服务系统</h2>
            <p class="subtitle" style="margin: 5px 0 0; color: #909399; font-size: 14px;">用爱心传递温暖，用行动改变世界</p>
          </div>
        </template>
        
        <el-form :model="form" v-on:submit.prevent="handleLogin" size="large">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-form-item>
            <el-button type="primary" :loading="isLoading" class="login-btn" @click="handleLogin" style="width: 100%">
              立即登录
            </el-button>
          </el-form-item>
          
          <div class="register-link" style="text-align: center; margin-top: 10px;">
            <span style="color: #606266; margin-right: 5px;">还没有账号？</span>
            <el-link type="primary" :underline="false" @click="toRegister">立即注册</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useLogin } from '@/hooks/useLogin';
import { User, Lock, Monitor } from '@element-plus/icons-vue';

const { form, isLoading, handleLogin, toRegister } = useLogin();
const rememberMe = ref(false);
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background-color: #f0f2f5;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  z-index: 0;
}

.login-content {
  z-index: 1;
  position: relative;
}

.login-card {
  width: 420px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  padding: 10px 0;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
