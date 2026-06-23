<template>
  <div class="p-20">
    <el-card shadow="never">
      <template #header>
        <div class="flex-between">
          <span style="font-size: 18px; font-weight: bold;">📦 商品基础信息管理</span>
          <el-button type="primary" icon="Plus" @click="openDialog({})">新增商品</el-button>
        </div>
      </template>

      <el-table :data="products" style="width: 100%" stripe :header-cell-style="{background:'#f5f7fa'}">
        <el-table-column prop="productId" label="ID" width="100" />
        <el-table-column prop="name" label="名称" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
           <template #default="{row}">¥{{ row.price }}</template>
        </el-table-column>
        
        <el-table-column prop="stockAlertThreshold" label="库存阈值" width="100" align="center" />
        <el-table-column prop="nearExpiryDays" label="临期界限" width="100" align="center">
           <template #default="{row}">
             <el-tag type="warning" effect="plain">{{ row.nearExpiryDays || 30 }}天</el-tag>
           </template>
        </el-table-column>

        <el-table-column prop="type" label="类型" width="100" align="center">
           <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
        </el-table-column>
        
        <el-table-column label="厂家信息" show-overflow-tooltip>
           <template #default="{row}">
             <span>{{ row.factoryName }}</span>
             <span style="color:#999; font-size:12px; margin-left:5px">({{ row.factoryId }})</span>
           </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{row}">
            <el-button size="small" type="primary" link icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" type="danger" link icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.productId ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="商品ID" v-if="form.productId">
           <el-input v-model="form.productId" disabled />
        </el-form-item>
        
        <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" :step="0.1" style="width: 100%" /></el-form-item>
        <el-form-item label="类型">
           <el-select v-model="form.type" style="width: 100%">
             <el-option v-for="t in ['食品','饮品','日用品','调味品']" :key="t" :label="t" :value="t" />
           </el-select>
        </el-form-item>
        <el-form-item label="保质期">
           <el-input-number v-model="form.shelfLifeValue" style="width: 120px" />
           <el-select v-model="form.shelfLifeUnit" style="width: 100px; margin-left: 10px">
             <el-option label="年" value="YEAR" />
             <el-option label="月" value="MONTH" />
             <el-option label="日" value="DAY" />
           </el-select>
        </el-form-item>
        
        <el-form-item label="库存预警阈值"><el-input-number v-model="form.stockAlertThreshold" /></el-form-item>

        <el-form-item label="临期界限(天)">
          <el-input-number v-model="form.nearExpiryDays" :min="1" />
          <div style="font-size: 12px; color: #999; line-height: 1.2; margin-top: 5px;">
            当剩余保质期小于该天数时，自动打8折。
          </div>
        </el-form-item>
        
        <el-form-item label="选择厂家">
           <el-select v-model="form.factoryId" placeholder="请选择" filterable style="width: 100%">
             <el-option 
               v-for="fac in factoryOptions" 
               :key="fac.factoryId" 
               :label="fac.factoryName" 
               :value="fac.factoryId">
               <span style="float: left">{{ fac.factoryName }}</span>
               <span style="float: right; color: #8492a6; font-size: 13px">{{ fac.factoryId }}</span>
             </el-option>
           </el-select>
        </el-form-item>

        <el-form-item label="状态">
           <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const products = ref([])
const factoryOptions = ref([])
const dialogVisible = ref(false)
const form = ref({})

const fetchList = async () => {
  const res = await request.get('/product/list', { params: { onlyOnShelf: false } })
  products.value = res
}

const fetchFactories = async () => {
  const res = await request.get('/user/factory/list')
  factoryOptions.value = res
}

const openDialog = (row) => {
  if (row.productId) {
    form.value = { ...row }
  } else {
    // 默认临期天数设为 30
    form.value = { status: 1, shelfLifeUnit: 'MONTH', nearExpiryDays: 30 }
  }
  dialogVisible.value = true
}

const saveProduct = async () => {
  await request.post('/product/save', form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

const handleDelete = async (row) => {
  ElMessage.info('请联系管理员删除') 
}

onMounted(() => {
  fetchList()
  fetchFactories()
})
</script>
<style scoped> .p-20 { padding: 20px; } .flex-between { display: flex; justify-content: space-between; } </style>