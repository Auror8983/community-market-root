<template>
  <div class="p-20">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>⚠️ 预警与处理中心</span>
          <el-button type="primary" icon="Refresh" circle @click="fetchData" />
        </div>
      </template>
    
      <el-tabs type="border-card">
        <el-tab-pane label="临期预警">
          <el-table :data="warningData.nearExpiry" style="width: 100%" empty-text="暂无临期商品">
            <el-table-column prop="productId" label="商品ID" width="100" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="batchId" label="批次号" width="150" />
            <el-table-column prop="stockQuantity" label="库存" width="100" />
            <el-table-column prop="expirationDate" label="过期日期" sortable>
              <template #default="scope">
                <span style="color: orange; font-weight: bold;">{{ scope.row.expirationDate }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default>
                <el-tag type="warning">自动8折生效中</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="库存不足">
          <el-table :data="warningData.lowStock" style="width: 100%" empty-text="库存充足">
            <el-table-column prop="productId" label="ID" width="80" />
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="totalStock" label="当前库存">
               <template #default="{row}">
                 <span style="color: red; font-weight: bold">{{ row.totalStock }}</span>
               </template>
            </el-table-column>
            <el-table-column prop="stockAlertThreshold" label="报警阈值" width="100" />
            <el-table-column label="操作">
              <template #default="{row}">
                <el-button type="primary" size="small" @click="goToInbound(row.name)">去补货</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="❌ 已过期商品 (待处理)">
          <div class="mb-20">
            <el-alert title="以下商品已超过保质期且仍有库存，请尽快销毁或下架！" type="error" show-icon :closable="false" />
          </div>
          <el-table :data="warningData.expired" style="width: 100%" empty-text="暂无过期商品">
            <el-table-column prop="productId" label="商品ID" width="100" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="batchId" label="批次号" />
            <el-table-column prop="expirationDate" label="过期日期" sortable>
               <template #default="{row}">
                 <span style="color: red; font-weight: 900; text-decoration: underline">{{ row.expirationDate }}</span>
               </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="滞留库存" />
          </el-table>
          
          <div class="mt-20" style="text-align: right">
            <el-button type="danger" size="large" @click="goToInventory" :disabled="!warningData.expired || warningData.expired.length === 0">
              去清除过期商品
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useRouter } from 'vue-router'

const warningData = ref({ 
  lowStock: [], 
  nearExpiry: [], 
  expired: [] 
})

const router = useRouter()

const fetchData = async () => {
  try {
    const res = await request.get('/product/warning')
    if (res) {
      warningData.value = res
    }
  } catch (e) {
    console.error("获取预警数据失败", e)
  }
}

const goToInventory = () => {
  router.push('/employee/inventory')
}

const goToInbound = (name) => {
  router.push({ path: '/employee/inbound', query: { productName: name } })
}

onMounted(fetchData)
</script>

<style scoped>
.p-20 { padding: 20px; }
.mt-20 { margin-top: 20px; }
.mb-20 { margin-bottom: 20px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
</style>