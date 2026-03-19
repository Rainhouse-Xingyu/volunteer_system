import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '@/api/auth';
import { showSuccessToast, showFailToast } from 'vant';

export default function useRegister() {
  const router = useRouter();
  const form = ref({
    username: '',
    password: '',
    confirmPassword: '',
    role: 'volunteer' // 默认注册志愿者
  });

  const loading = ref(false);

  const onSubmit = async () => {
    if (form.value.password !== form.value.confirmPassword) {
      showFailToast('两次输入的密码不一致');
      return;
    }

    try {
      loading.value = true;
      // 构造后端需要的参数，移除 confirmPassword
      const { confirmPassword, ...params } = form.value;
      await register(params);
      showSuccessToast('注册成功，请登录');
      router.push('/login');
    } catch (error) {
      console.error(error);
    } finally {
      loading.value = false;
    }
  };

  const toLogin = () => {
    router.push('/login');
  };

  return {
    form,
    loading,
    onSubmit,
    toLogin
  };
}
