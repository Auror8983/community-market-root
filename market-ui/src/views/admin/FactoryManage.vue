<template>
  <div class="p-20">
    <el-card shadow="never">
      <template #header>
        <div class="flex-between">
          <span class="page-title">🏭 厂家/供应商管理</span>
          <el-button type="primary" icon="Plus" @click="openDialog({})">新增厂家</el-button>
        </div>
      </template>
      <el-table :data="factories" style="width: 100%" border stripe>
        <el-table-column prop="factoryId" label="ID" width="100" />
        <el-table-column prop="factoryName" label="厂家名称" />
        <el-table-column prop="contactPerson" label="联系人" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="address" label="厂家地址" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button type="primary" link icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除此厂家?" @confirm="deleteFactory(row.factoryId)">
              <template #reference>
                <el-button type="danger" link icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.factoryId ? '编辑厂家' : '新增厂家'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="ID" v-if="form.factoryId">
           <el-input v-model="form.factoryId" disabled />
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.factoryName" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFactory">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const factories = ref([])
const dialogVisible = ref(false)
const form = ref({})

const fetchList = async () => {
  const res = await request.get('/user/factory/list')
  factories.value = res
}

const openDialog = (row) => {
      // 这里的逻辑关键：如果是新增，form 里不要带 factoryId，或者置空
      if (row.factoryId) {
         form.value = { ...row }
      } else {
         form.value = {}
      }
      dialogVisible.value = true
    }

const submitFactory = async () => {
  // 复用 add 接口，假设后端做了 saveOrUpdate 处理，或者单独加一个 update 接口
  await request.post('/user/factory/add', form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

const deleteFactory = async (id) => {
  await request.delete(`/user/factory/delete/${id}`)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>
<style scoped> .p-20 { padding: 20px; } .flex-between { display: flex; justify-content: space-between; align-items: center; } .page-title { font-size: 18px; font-weight: bold; border-left: 4px solid #409EFF; padding-left: 10px; } </style>