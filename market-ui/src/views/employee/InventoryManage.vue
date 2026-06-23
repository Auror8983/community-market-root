<template>
  <div class="p-20">
    <el-card shadow="never">
      <template #header>
        <div class="flex-between">
          <div class="header-left">
            <span style="font-size: 18px; font-weight: bold; margin-right: 20px">📊 商品库存与状态监控</span>
            <el-input v-model="query.name" placeholder="商品名称" style="width: 200px" clearable @clear="fetchData" />
            <el-select v-model="query.type" placeholder="类型" style="width: 150px; margin-left: 10px" clearable>
               <el-option v-for="t in ['食品','饮品','日用品','调味品']" :key="t" :label="t" :value="t" />
            </el-select>
            <el-button type="primary" icon="Search" style="margin-left: 10px" @click="fetchData">查询</el-button>
          </div>
          <el-button icon="Refresh" circle @click="fetchData" />
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" stripe row-key="productId" :header-cell-style="{background:'#f5f7fa'}">
        <el-table-column type="expand">
          <template #default="{row}">
            <div class="batch-container">
              <h4 style="margin: 0 0 10px 0; color: #606266;">📦 [{{ row.name }}] 批次管理</h4>
              <el-table :data="row.batches" border size="small">
                
                <el-table-column prop="batchId" label="批次ID" width="140">
                   <template #default="scope">
                     <span :style="getBatchTextStyle(scope.row)">{{ scope.row.batchId }}</span>
                   </template>
                </el-table-column>

                <el-table-column prop="productionDate" label="生产日期" width="120" />
                
                <el-table-column prop="expirationDate" label="过期日期" width="120">
                   <template #default="scope">
                     <span :style="getBatchTextStyle(scope.row)">{{ scope.row.expirationDate }}</span>
                   </template>
                </el-table-column>

                <el-table-column prop="stockQuantity" label="库存" width="100" />
                <el-table-column label="是否临期" width="100" align="center">
                  <template #default="scope">
                    <el-tag v-if="scope.row.isExpired" type="danger">已过期</el-tag>
                    <el-tag v-else-if="scope.row.isNearExpiry" type="warning">是</el-tag>
                    <el-tag v-else type="info">否</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="批次状态" width="100" align="center">
                   <template #default="scope">
                      <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" size="small" @change="updateBatchStatus(scope.row)" />
                   </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="scope">
                    <el-button size="small" link type="primary" icon="Edit" @click="editBatchStock(scope.row)">修改</el-button>
                    <el-popconfirm title="确认删除该批次?" @confirm="deleteBatch(scope.row.batchId)">
                      <template #reference><el-button size="small" link type="danger" icon="Delete">删除</el-button></template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="productId" label="ID" width="80" align="center" />
        
        <el-table-column label="商品名称" min-width="150" show-overflow-tooltip>
          <template #default="{row}">
            <span :style="getProductNameStyle(row.batches)">{{ row.name }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="stockAlertThreshold" label="库存阈值" width="100" align="center">
          <template #default="{row}">
             <el-tag type="info" effect="plain">{{ row.stockAlertThreshold }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="nearExpiryDays" label="临期界限" width="100" align="center">
          <template #default="{row}">
             <el-tag type="warning" effect="plain">{{ row.nearExpiryDays || 30 }}天</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
        </el-table-column>
        
        <el-table-column prop="totalStock" label="总库存" width="120" sortable align="center">
           <template #default="{row}">
             <span :style="row.totalStock < row.stockAlertThreshold ? 'color:red;font-weight:bold' : ''">
               {{ row.totalStock }}
             </span>
           </template>
        </el-table-column>

        <el-table-column label="商品总开关" width="120" align="center">
          <template #default="{row}">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="updateProductStatus(row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="stockDialogVisible" title="✏️ 修改批次库存" width="400px">
      <el-form :model="currentBatch">
         <el-form-item label="当前批次">{{ currentBatch.batchId }}</el-form-item>
         <el-form-item label="库存数量">
           <el-input-number v-model="currentBatch.stockQuantity" :min="0" />
         </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockUpdate">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const query = ref({ name: '', type: '' })
const tableData = ref([])
const stockDialogVisible = ref(false)
const currentBatch = ref({})

const fetchData = async () => {
  const res = await request.get('/product/list', { 
    params: { 
      name: query.value.name,
      type: query.value.type,
      onlyOnShelf: false 
    } 
  })
  tableData.value = res
}

const getBatchTextStyle = (batch) => {
  if (batch.isExpired) return 'color: red; font-weight: bold; text-decoration: underline;'
  if (batch.isNearExpiry) return 'color: #f56c6c; font-weight: bold;'
  return ''
}
const getProductNameStyle = (batches) => {
  if (!batches || batches.length === 0) return ''
  const hasExpired = batches.some(b => b.isExpired && b.stockQuantity > 0)
  if (hasExpired) return 'color: red; font-weight: bold; text-decoration: underline;'
  const hasNearExpiry = batches.some(b => b.isNearExpiry && b.stockQuantity > 0)
  if (hasNearExpiry) return 'color: #f56c6c; font-weight: bold;'
  return ''
}

const updateProductStatus = async (row) => {
  try {
    await request.post('/product/status', {
      productId: row.productId,
      status: row.status
    })
    ElMessage.success('商品状态已更新')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1 
  }
}

const updateBatchStatus = async (batch) => {
  try {
    await request.post('/product/batch/status', {
      batchId: batch.batchId,
      status: batch.status
    })
    ElMessage.success('批次状态已更新')
  } catch (e) {
    batch.status = batch.status === 1 ? 0 : 1 
  }
}

const deleteBatch = async (batchId) => {
  try {
    await request.delete(`/product/batch/${batchId}`)
    ElMessage.success('批次已删除')
    fetchData()
  } catch (e) {
  }
}

const editBatchStock = (batch) => {
  currentBatch.value = { ...batch }
  stockDialogVisible.value = true
}
const submitStockUpdate = async () => {

  await request.post(`/product/batch/stock/${currentBatch.value.batchId}/${currentBatch.value.stockQuantity}`)
  ElMessage.success('库存修改成功')
  stockDialogVisible.value = false
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.p-20 { padding: 20px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; }
.batch-container { padding: 10px 20px; background-color: #fdfdfd; border-radius: 4px; }
</style>