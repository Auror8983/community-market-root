import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 从 sessionStorage 读取初始值
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || '{}'))
  const token = ref(sessionStorage.getItem('token') || '')

  const setUser = (user) => {
    userInfo.value = user
    // 存入 sessionStorage
    sessionStorage.setItem('userInfo', JSON.stringify(user))
  }

  const setToken = (newToken) => {
    token.value = newToken
    // 存入 sessionStorage
    sessionStorage.setItem('token', newToken)
  }

  const logout = () => {
    userInfo.value = {}
    token.value = ''
    // 清除 sessionStorage
    sessionStorage.removeItem('userInfo')
    sessionStorage.removeItem('token')
  }

  return { userInfo, token, setUser, setToken, logout }
})