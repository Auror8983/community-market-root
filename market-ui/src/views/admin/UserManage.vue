<template>
  <div class="p-20">
    <el-card shadow="never">
      <template #header>
        <div class="flex-between">
          <span class="page-title">👥 用户信息管理</span>
          <div>
            <el-radio-group v-model="roleFilter" @change="fetchUsers" size="default">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="EMPLOYEE">员工</el-radio-button>
              <el-radio-button label="CLIENT">客户</el-radio-button>
            </el-radio-group>
            <el-button type="primary" icon="Plus" style="margin-left: 15px" @click="openDialog({})">新增用户</el-button>
          </div>
        </div>
      </template>

      <el-table :data="users" stripe style="width: 100%" size="large">
        <el-table-column prop="userId" label="ID" width="100" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色">
           <template #default="{row}">
             <el-tag :type="getRoleTagType(row.role)">
               {{ roleMap[row.role] || row.role }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该用户吗?" @confirm="deleteUser(row.userId)">
              <template #reference>
                <el-button type="danger" link icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="ID" v-if="form.isEdit">
           <el-input v-model="form.userId" disabled />
        </el-form-item>
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="form.isEdit" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" placeholder="留空则不修改" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="角色" v-if="!form.isEdit">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="员工" value="EMPLOYEE" />
            <el-option label="客户" value="CLIENT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUser">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const users = ref([])
const roleFilter = ref('')
const dialogVisible = ref(false)
const form = ref({})

const roleMap = {
  'ADMIN': '管理员',
  'EMPLOYEE': '员工',
  'CLIENT': '客户'
}
const getRoleTagType = (role) => {
  if(role === 'ADMIN') return 'danger'
  if(role === 'EMPLOYEE') return 'warning'
  return 'success'
}
const fetchUsers = async () => {
  const res = await request.get('/user/list', { params: { role: roleFilter.value } })
  users.value = res
}

const openDialog = (row) => {
      if (row.userId) {
        form.value = { ...row, password: '', isEdit: true }
      } else {
        form.value = { role: 'CLIENT', isEdit: false }
      }
      dialogVisible.value = true
    }

const submitUser = async () => {
  if (form.value.isEdit) {
    if(!form.value.password) delete form.value.password
    await request.post('/user/update', form.value)
    ElMessage.success('更新成功')
  } else {
    await request.post('/user/add', form.value)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  fetchUsers()
}

const deleteUser = async (id) => {
  await request.delete(`/user/delete/${id}`)
  ElMessage.success('删除成功')
  fetchUsers()
}

onMounted(fetchUsers)
</script>

<style scoped>
.p-20 { padding: 20px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-size: 18px; font-weight: bold; border-left: 4px solid #409EFF; padding-left: 10px; }
</style>