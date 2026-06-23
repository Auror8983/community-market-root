<template>
  <el-container style="height: 100vh;">
    <el-aside width="200px" style="background-color: #304156;">
      <div class="logo">后台管理系统</div>
      <el-menu
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        :default-active="$route.path"
        style="border-right: none;"
      >
        <el-menu-item index="/employee/notifications"><el-icon><House /></el-icon> <span>消息中心</span></el-menu-item>
        <el-menu-item index="/employee/warning"><el-icon><Bell /></el-icon> <span>预警中心</span></el-menu-item>
        <el-menu-item index="/employee/inbound"><el-icon><Box /></el-icon> <span>商品入库</span></el-menu-item>
        <el-menu-item index="/employee/inventory"><el-icon><Tickets /></el-icon> <span>库存管理</span></el-menu-item>
        
        <el-menu-item index="/employee/orders"><el-icon><Van /></el-icon> <span>订单中心</span></el-menu-item>
        
        <div v-if="userStore.userInfo.role === 'ADMIN'">
          <el-menu-item-group title="管理员功能">
             <el-menu-item index="/employee/products"><el-icon><Goods /></el-icon> <span>商品管理</span></el-menu-item>
             <el-menu-item index="/employee/stats"><el-icon><DataLine /></el-icon> <span>销售统计</span></el-menu-item>
             <el-menu-item index="/employee/users"><el-icon><User /></el-icon> <span>用户管理</span></el-menu-item>
             <el-menu-item index="/employee/factories"><el-icon><OfficeBuilding /></el-icon> <span>厂家管理</span></el-menu-item>
          </el-menu-item-group>
        </div>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="backend-header">
        <div class="breadcrumb">
          当前角色: 
          <el-tag :type="userStore.userInfo.role === 'ADMIN' ? 'danger' : 'warning'">
            {{ roleMap[userStore.userInfo.role] || userStore.userInfo.role }}
          </el-tag>
        </div>
        
        <div class="user-action">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link" style="cursor: pointer; display: flex; align-items: center;">
              {{ userStore.userInfo.realName }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided style="color: red;">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
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
import { ArrowDown, House } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const roleMap = {
  'ADMIN': '管理员',
  'EMPLOYEE': '员工',
  'CLIENT': '客户'
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/employee/profile')
  }
}
</script>

<style scoped>
.logo { 
  color: white; text-align: center; height: 60px; line-height: 60px; 
  font-weight: bold; font-size: 18px; border-bottom: 1px solid #1f2d3d; 
  background-color: #2b3649;
}
.backend-header { 
  background: #fff; border-bottom: 1px solid #e6e6e6; 
  display: flex; justify-content: space-between; align-items: center; 
  padding: 0 20px; box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.user-action { font-size: 14px; color: #606266; }
</style>