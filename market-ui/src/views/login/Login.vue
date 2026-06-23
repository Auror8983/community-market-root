<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="header-text">
          <h2>线上社区超市管理系统</h2>
          <p>请登录</p>
        </div>
      </template>
      <el-form :model="form" label-width="70px" @keyup.enter="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin / emp001 / client001" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" class="w-100" @click="handleLogin" :loading="loading">登录</el-button>
      </el-form>
      <div class="tips">
        <p>演示账号: admin / emp001 / client001</p>
        <p>密码均为: 123456</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/api/request'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const form = ref({ username: '', password: '' })
const loading = ref(false)
const router = useRouter()
const userStore = useUserStore()

const handleLogin = async () => {
  if(!form.value.username || !form.value.password) return ElMessage.warning('请输入用户名和密码')
  
  loading.value = true
  try {
    const user = await request.post('/user/login', form.value)
    userStore.setUser(user)
    ElMessage.success(`欢迎回来, ${user.realName}`)
    
    if (user.role === 'CLIENT') {
      router.push('/client/products')
    } else {
      // 员工和管理员跳转到通知中心
      router.push('/employee/notifications')
    }
  } catch (e) {
    // request.js handles error
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f0f2f5; }
.login-card { width: 400px; text-align: center; border-radius: 8px; }
.header-text h2 { margin: 0; color: #333; }
.header-text p { margin: 10px 0 0; color: #666; font-size: 14px; }
.w-100 { width: 100%; margin-top: 10px; height: 40px; font-size: 16px; }
.tips { margin-top: 20px; color: #999; font-size: 12px; text-align: left; background: #f9fafe; padding: 15px; border-radius: 4px; }
.tips p { margin: 5px 0; }
</style>