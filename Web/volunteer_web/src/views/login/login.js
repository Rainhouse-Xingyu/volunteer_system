import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api/auth';
import { showSuccessToast, showFailToast } from 'vant';

export default function useLogin() {
  const router = useRouter();
  const form = ref({
    username: '',
    password: ''
  });

  const loading = ref(false);

  const onSubmit = async () => {
    if (!form.value.username || !form.value.password) {
      showFailToast('请输入用户名和密码');
      return;
    }

    try {
      loading.value = true;
      const res = await login(form.value);
      showSuccessToast('登录成功');
      localStorage.setItem('token', res.data); // 假设后端返回 data 为 token 字符串
      
      // 这里的跳转逻辑后续可以根据角色判断，暂时跳首页
      router.push('/home');
    } catch (error) {
      // 错误已在 request.js 处理
      console.error(error);
    } finally {
      loading.value = false;
    }
  };

  const toRegister = () => {
    router.push('/register');
  };

  const toForget = () => {
    router.push('/forget');
  };

  return {
    form,
    loading,
    onSubmit,
    toRegister,
    toForget
  };
}
