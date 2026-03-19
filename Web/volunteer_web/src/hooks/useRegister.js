// src/hooks/useRegister.js
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

export function useRegister() {
  const router = useRouter()
  
  const form = ref({
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    role: 'VOLUNTEER' // default role
  })
  
  const isLoading = ref(false)

  const handleRegister = async () => {
    if (!form.value.username || !form.value.password || !form.value.nickname) {
      ElMessage.warning('请完整填写信息')
      return
    }

    if (form.value.password !== form.value.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }

    try {
      isLoading.value = true
      const { confirmPassword, ...registerData } = form.value
      // Adjust structure if backend needs specific user object
      const res = await register(registerData)
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(res.message || '注册失败')
      }
    } catch(err) {
      console.error(err)
      ElMessage.error(err.message || '注册异常')
    } finally {
      isLoading.value = false
    }
  }

  const toLogin = () => {
    router.push('/login')
  }

  return { form, isLoading, handleRegister, toLogin }
}