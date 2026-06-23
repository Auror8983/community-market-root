<template>
  <div class="app-container">
    <el-card class="box-card profile-card">
      <template #header>
        <div class="card-header">
          <span>👤 个人中心</span>
          <el-tag :type="roleTypeTag" effect="dark">
            {{ roleMap[userStore.userInfo.role] || userStore.userInfo.role }}
          </el-tag>
        </div>
      </template>
      
      <div class="profile-content">
        <div class="avatar-section">
          <el-avatar :size="80" style="background-color: #409EFF; font-size: 30px;">
             {{ userStore.userInfo.realName ? userStore.userInfo.realName.charAt(0) : 'User' }}
          </el-avatar>
          <h3 style="margin-top: 10px;">{{ userStore.userInfo.realName }}</h3>
        </div>

        <el-descriptions :column="1" border size="large" class="info-table">
          <el-descriptions-item label="用户ID">{{ userStore.userInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="登录账号">{{ userStore.userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ userStore.userInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货/联系地址">{{ userStore.userInfo.address }}</el-descriptions-item>
        </el-descriptions>

        <div class="action-btn" style="text-align: center; margin-top: 20px;">
          <el-button type="primary" icon="Edit" @click="openEdit" size="large" round>修改个人信息</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="✏️ 修改个人信息" width="500px" center>
      <el-form :model="form" label-width="80px" class="edit-form">
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" type="textarea" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.password" placeholder="不修改请留空" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveInfo">保存更改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const dialogVisible = ref(false)
const form = ref({})

// 汉化映射
const roleMap = {
  'ADMIN': '管理员',
  'EMPLOYEE': '员工',
  'CLIENT': '客户'
}

const roleTypeTag = computed(() => {
  const role = userStore.userInfo.role
  if(role === 'ADMIN') return 'danger'
  if(role === 'EMPLOYEE') return 'warning'
  return 'success'
})

const openEdit = () => {
  form.value = { ...userStore.userInfo, password: '' }
  dialogVisible.value = true
}

const saveInfo = async () => {
  if(!form.value.password) delete form.value.password
  await request.post('/user/update', form.value)
  const updatedUser = { ...userStore.userInfo, ...form.value }
  userStore.setUser(updatedUser)
  ElMessage.success('修改成功')
  dialogVisible.value = false
}
</script>

<style scoped>
.app-container { padding: 40px; display: flex; justify-content: center; }
.profile-card { width: 600px; border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 18px; font-weight: bold; }
.info-table { margin-top: 20px; }
</style>