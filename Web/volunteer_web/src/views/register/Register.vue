<template>
  <div class="register-container">
    <div class="header">
      <h2>注册账号</h2>
      <p>欢迎加入志愿服务大家庭</p>
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
            placeholder="设置密码"
            :rules="[{ required: true, message: '请填写密码' }]"
          />
          <van-field
            v-model="form.confirmPassword"
            type="password"
            name="确认密码"
            label="确认密码"
            placeholder="再次输入密码"
            :rules="[{ required: true, message: '请再次输入密码' }]"
          />
          <van-field name="radio" label="角色">
            <template #input>
              <van-radio-group v-model="form.role" direction="horizontal">
                <van-radio name="volunteer">志愿者</van-radio>
                <van-radio name="organizer">组织者</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
        
        <div class="submit-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            立即注册
          </van-button>
        </div>
      </van-form>
    </div>

    <div class="footer">
      已有账号? <span class="link" @click="toLogin">去登录</span>
    </div>
  </div>
</template>

<script setup>
import useRegister from './register.js';
const { form, loading, onSubmit, toLogin } = useRegister();
</script>

<style scoped src="./register.css"></style>
