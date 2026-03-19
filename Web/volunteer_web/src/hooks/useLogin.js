import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { login, getUserInfo } from '@/api/auth'

export function useLogin() {
  const router = useRouter()
  const userStore = useUserStore()
  
  const form = ref({
    username: '',
    password: ''
  })
  
  const isLoading = ref(false)

  const toRegister = () => {
    router.push('/register')
  }

  const handleLogin = async () => {
    if (!form.value.username || !form.value.password) {
      ElMessage.warning('请输入用户名和密码')
      return;
    }

    try {
      isLoading.value = true
      const res = await login(form.value)
      if (res.code === 200) {
        ElMessage.success('登录成功')
        userStore.setToken(res.data) 
        
        try {
            const userRes = await getUserInfo();
            if(userRes.code === 200) {
                userStore.setRole(userRes.data.role); 
                userStore.setUserId(userRes.data.userId);
                userStore.setUserInfo(userRes.data); 
            }
        } catch(e) { console.error('Failed to get user info', e) }

        router.push('/home')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch(err) {
      console.error(err)
      ElMessage.error(err.message || '网络请求错误') 
    } finally {
      isLoading.value = false
    }
  }

  return { form, isLoading, handleLogin, toRegister }
}