<template>
  <div class="login-container">
    <div class="logo-box">
      <!-- 这里的 logo 图片路径请根据实际情况替换 -->
      <!-- <img src="@/assets/logo.png" alt="logo" /> -->
      <h2>志愿服务系统</h2>
    </div>

    <div class="form-box">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="用户名"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请填写用户名' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="密码"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
          />
        </van-cell-group>
        <div class="submit-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>
    </div>

    <div class="footer-actions">
      <span class="link" @click="toRegister">注册账号</span>
      <span class="link" @click="toForget">忘记密码?</span>
    </div>
  </div>
</template>

<script setup>
import useLogin from './login.js';
// Vant 组件需要确保已全局引入或按需引入，这里假设 main.js 已配置或使用自动引入插件
const { form, loading, onSubmit, toRegister, toForget } = useLogin();
</script>

<style scoped src="./login.css"></style>
