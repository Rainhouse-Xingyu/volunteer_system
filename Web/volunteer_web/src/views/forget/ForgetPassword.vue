<template>
  <div class="forget-container">
    <h2>找回密码</h2>
    <div class="form-box">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.email"
            name="邮箱"
            label="邮箱"
            placeholder="请输入注册邮箱"
            :rules="[{ required: true, message: '请填写邮箱' }]"
          >
            <template #button>
              <van-button size="small" type="primary" @click.prevent="onSendCode">发送验证码</van-button>
            </template>
          </van-field>
          <van-field
            v-model="form.code"
            name="验证码"
            label="验证码"
            placeholder="请输入验证码"
            :rules="[{ required: true, message: '请填写验证码' }]"
          />
          <van-field
            v-model="form.newPassword"
            type="password"
            name="新密码"
            label="新密码"
            placeholder="设置新密码"
            :rules="[{ required: true, message: '请填写新密码' }]"
          />
        </van-cell-group>
        <div class="submit-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            重置密码
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import useForget from './forget.js';
const { form, loading, onSendCode, onSubmit } = useForget();
</script>

<style scoped src="./forget.css"></style>
