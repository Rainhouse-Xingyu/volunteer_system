<template>
  <div class="login-container">
    <div class="header">
      <van-icon name="friends" class="logo-icon" />
      <h2 class="title">校园志愿者系统</h2>
    </div>

    <div class="form-card">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="username"
            name="username"
            label="账号"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请填写账号' }]"
          >
            <template #left-icon>
                <van-icon name="user-o" />
            </template>
          </van-field>
          <van-field
            v-model="password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
          >
            <template #left-icon>
                <van-icon name="closed-eye" />
            </template>
          </van-field>
        </van-cell-group>
        <div style="margin: 24px 16px;">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>
      
      <div class="footer-links">
        <span class="link">注册账号</span>
        <span class="divider">|</span>
        <span class="link">忘记密码</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import request from '@/utils/request'; 

// State
const username = ref('');
const password = ref('');
const loading = ref(false);
const router = useRouter();

// Submit Handler
const onSubmit = async (values) => {
  loading.value = true;
  try {
    // 调用 /auth/login 接口 (baseURL 已配置为 /api，vite proxy 会自动转发)
    // 假设后端 LoginDTO 接收 { username, password }
    const res = await request.post('/auth/login', {
      username: values.username,
      password: values.password
    });

    // 假设后端返回结构 { code: 200, data: "token_string", msg: "..." }
    // request.js 拦截器会处理非200并返回 res.data
    // 如果 loginService.login 返回的是 token 字符串，则 res.data 就是 token
    
    // 这里需要根据实际后端返回调整。Result.success("登录成功", token) -> res.data = token
    const token = res.data; 
    
    // 存 Token
    localStorage.setItem('token', token);
    
    showSuccessToast('登录成功');
    
    // 跳转
    router.replace('/home');
    
  } catch (error) {
    // 错误在 request.js 已统一下发 Toast，这里主要处理按钮状态恢复
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 16px; 
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 48px;
  color: #1989fa;
  margin-bottom: 10px;
}

.title {
  font-size: 24px;
  color: #323233;
  font-weight: 500;
  margin: 0;
}

.form-card {
  width: 100%;
  max-width: 375px; /* 适配移动端宽度，但在PC端也不会过宽 */
}

/* 覆盖 Vant cell-group inset 的一些默认边距，使其更贴合设计 */
.van-cell-group--inset {
  margin: 0;
}

.footer-links {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #969799;
}

.link {
  padding: 0 8px;
  color: #1989fa;
}

.divider {
  color: #ebedf0;
}
</style>
