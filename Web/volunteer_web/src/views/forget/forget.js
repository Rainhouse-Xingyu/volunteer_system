import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';

export default function useForget() {
  const router = useRouter();
  const form = ref({
    email: '',
    code: '',
    newPassword: ''
  });

  const loading = ref(false);

  const onSendCode = () => {
    if (!form.value.email) {
      showToast('请输入邮箱');
      return;
    }
    showToast('验证码已发送(模拟)');
  };

  const onSubmit = () => {
    if (!form.value.code || !form.value.newPassword) {
      showToast('请填写完整信息');
      return;
    }
    loading.value = true;
    setTimeout(() => {
      loading.value = false;
      showToast('密码重置成功(模拟)');
      router.push('/login');
    }, 1000);
  };

  return {
    form,
    loading,
    onSendCode,
    onSubmit
  };
}
