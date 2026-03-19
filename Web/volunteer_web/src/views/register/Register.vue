<template>
  <div class="register-body">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>

    <!-- 注册容器 -->
    <div class="register-container">
      <div class="register-card">
        <!-- Logo区域 -->
        <div class="logo-section">
          <div class="logo-icon">
            <i class="fas fa-user-plus"></i>
          </div>
          <h1 class="logo-text">欢迎加入</h1>
          <p class="logo-subtitle">成为志愿者大家庭的一员</p>
        </div>

        <!-- 注册表单 -->
        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label class="form-question">用户名</label>
            <div class="input-wrapper">
              <input 
                type="text" 
                class="form-input" 
                v-model="form.username"
                placeholder="设置登录账号 (3-20位)" 
                required
              >
              <i class="fas fa-user input-icon"></i>
            </div>
          </div>

          <div class="form-group">
            <label class="form-question">昵称</label>
            <div class="input-wrapper">
              <input 
                type="text" 
                class="form-input" 
                v-model="form.nickname"
                placeholder="怎么称呼您？" 
                required
              >
              <i class="fas fa-id-card input-icon"></i>
            </div>
          </div>

          <div class="form-group">
            <label class="form-question">身份选择</label>
            <div class="role-radio-group">
               <label class="role-radio-label">
                  <input type="radio" v-model="form.role" value="VOLUNTEER"> 志愿者
               </label>
               <label class="role-radio-label">
                  <input type="radio" v-model="form.role" value="ORGANIZER"> 组织者
               </label>
            </div>
          </div>

          <div class="form-group">
            <label class="form-question">密码</label>
            <div class="input-wrapper">
              <input 
                type="password" 
                class="form-input" 
                v-model="form.password"
                placeholder="设置登录密码 (6位以上)" 
                required
              >
              <i class="fas fa-lock input-icon"></i>
            </div>
          </div>

          <div class="form-group">
            <label class="form-question">确认密码</label>
            <div class="input-wrapper">
              <input 
                type="password" 
                class="form-input" 
                v-model="form.confirmPassword"
                placeholder="再次输入密码" 
                required
              >
              <i class="fas fa-check-circle input-icon"></i>
            </div>
          </div>

          <div class="agreement">
            <input type="checkbox" id="agree" required checked>
            <label for="agree">我已阅读并同意 <a href="#">《志愿者服务协议》</a></label>
          </div>

          <button type="submit" class="register-btn" :class="{ loading: isLoading }">
            <span class="btn-text" v-if="!isLoading">立即注册</span>
            <div class="spinner" v-else></div>
          </button>
        </form>

        <!-- 返回登录 -->
        <div class="login-link">
          已有账号？<a @click.prevent="toLogin">立即登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRegister } from '@/hooks/useRegister'

const { form, isLoading, handleRegister, toLogin } = useRegister()
</script>

<style scoped src="./Register.css"></style>
