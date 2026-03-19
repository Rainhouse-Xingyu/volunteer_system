import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  // Normalize role to uppercase to match template checks (VOLUNTEER/ORGANIZER)
  const storedRole = localStorage.getItem('role') || ''
  const role = ref(storedRole ? storedRole.toUpperCase() : '')
  const userId = ref(localStorage.getItem('userId') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info || {}
    localStorage.setItem('userInfo', JSON.stringify(info || {}))
  }

  const setRole = (newRole) => {
    const upperRole = newRole ? newRole.toUpperCase() : ''
    role.value = upperRole
    localStorage.setItem('role', upperRole)
  }

  const setUserId = (id) => {
      userId.value = id
      localStorage.setItem('userId', id)
  }
  
  const logout = () => {
    token.value = ''
    role.value = ''
    userId.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
    localStorage.removeItem('userInfo')
  }

  return { token, role, userId, userInfo, setToken, setUserInfo, setRole, setUserId, logout }
})