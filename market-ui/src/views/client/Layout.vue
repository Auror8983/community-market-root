<template>
  <el-container style="height: 100vh;">
    <el-aside width="220px" style="background-color: #304156;">
      <div class="logo">
        🛒 社区超市
        <div class="sub-logo">客户端</div>
      </div>
      <el-menu
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        :default-active="$route.path"
        style="border-right: none;"
      >
        <el-menu-item index="/client/products"><el-icon><Goods /></el-icon> <span>商品选购</span></el-menu-item>
        <el-menu-item index="/client/cart"><el-icon><ShoppingCart /></el-icon> <span>我的购物车</span></el-menu-item>
        <el-menu-item index="/client/orders"><el-icon><List /></el-icon> <span>我的订单</span></el-menu-item>
        <el-menu-item index="/client/profile"><el-icon><User /></el-icon> <span>个人信息修改</span></el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="app-header">
        <div class="welcome-text">
          欢迎光临，{{ userStore.userInfo.realName }} 
          <el-tag size="small" type="success" style="margin-left: 10px">
            {{ roleMap[userStore.userInfo.role] || userStore.userInfo.role }}
          </el-tag>
        </div>
        <el-button type="danger" link @click="logout">退出登录</el-button>
      </el-header>
      <el-main style="background-color: #f0f2f5;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 角色映射
const roleMap = {
  'ADMIN': '管理员',
  'EMPLOYEE': '员工',
  'CLIENT': '客户'
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo { color: white; text-align: center; padding: 20px 0; font-weight: bold; font-size: 20px; border-bottom: 1px solid #1f2d3d; background-color: #2b3649; }
.sub-logo { font-size: 12px; color: #909399; margin-top: 5px; font-weight: normal; }
.app-header { background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.welcome-text { font-size: 14px; color: #606266; }
</style>