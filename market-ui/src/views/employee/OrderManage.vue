<template>
  <div class="p-20">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>🚚 订单配送管理</span>
          <el-button icon="Refresh" circle @click="fetchOrders" title="刷新列表"></el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="fetchOrders" type="card">
        <el-tab-pane label="⏳ 未完成 (待配送 / 配送中)" name="UNFINISHED">
          <template #label>
            <span><el-icon><Timer /></el-icon> 未完成 ({{ orders.length }})</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="✅ 已完成" name="COMPLETED">
          <template #label>
             <span><el-icon><CircleCheck /></el-icon> 已完成</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-table :data="orders" stripe style="width: 100%" class="mt-20" :header-cell-style="{background:'#f5f7fa'}">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" sortable />
        
        <el-table-column prop="status" label="当前状态" width="120" align="center">
          <template #default="{row}">
            <el-tag v-if="row.status === 'PAID'" type="warning" effect="dark">待配送</el-tag>
            <el-tag v-if="row.status === 'DELIVERING'" type="primary" effect="dark">配送中 🚚</el-tag>
            <el-tag v-if="row.status === 'COMPLETED'" type="success" effect="plain">已完成</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="收货信息" show-overflow-tooltip>
           <template #default="{row}">
             <div><el-icon><User /></el-icon> {{ row.receiverName }} <el-divider direction="vertical" /> {{ row.receiverPhone }}</div>
             <div style="color: #666; font-size: 12px;">{{ row.receiverAddress }}</div>
           </template>
        </el-table-column>
        
        <el-table-column prop="totalAmount" label="总额" width="100">
           <template #default="{row}">
             <span style="color: #f56c6c; font-weight: bold;">¥{{ row.totalAmount }}</span>
           </template>
        </el-table-column>
        
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{row}">
            <el-button size="small" icon="View" @click="showDetail(row)">详情</el-button>
            
            <el-popconfirm v-if="row.status === 'PAID'" title="确认商品已打包并开始配送?" @confirm="updateStatus(row, 'deliver')">
              <template #reference>
                <el-button type="primary" size="small" icon="Van">开始配送</el-button>
              </template>
            </el-popconfirm>
            
            <el-popconfirm v-if="row.status === 'DELIVERING'" title="确认客户已收到商品?" @confirm="updateStatus(row, 'complete')">
              <template #reference>
                <el-button type="success" size="small" icon="CircleCheck">确认送达</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="📦 订单详细信息" width="800px">
      <div v-if="currentDetail">
        <el-descriptions border :column="2" class="mb-20">
           <el-descriptions-item label="订单号">{{ currentDetail.orderNo }}</el-descriptions-item>
           <el-descriptions-item label="状态">
             <el-tag>{{ currentDetail.status }}</el-tag>
           </el-descriptions-item>
           <el-descriptions-item label="下单时间">{{ currentDetail.createTime }}</el-descriptions-item>
           <el-descriptions-item label="发货时间" v-if="currentDetail.startDeliveryTime">{{ currentDetail.startDeliveryTime }}</el-descriptions-item>
           <el-descriptions-item label="收货人">{{ currentDetail.receiverName }}</el-descriptions-item>
           <el-descriptions-item label="电话">{{ currentDetail.receiverPhone }}</el-descriptions-item>
           <el-descriptions-item label="收货地址" :span="2">{{ currentDetail.receiverAddress }}</el-descriptions-item>
           <el-descriptions-item label="总金额" :span="2">
             <span style="color: red; font-weight: bold; font-size: 18px">¥{{ currentDetail.totalAmount }}</span>
           </el-descriptions-item>
        </el-descriptions>

        <el-table :data="currentDetail.details" border stripe>
          <el-table-column prop="productId" label="ID" width="80" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="batchId" label="批次号" width="140" />
          <el-table-column prop="unitPrice" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="subtotal" label="小计" width="100">
             <template #default="{row}"><strong>¥{{ row.subtotal }}</strong></template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { Timer, CircleCheck, User, Van, View } from '@element-plus/icons-vue'

const activeTab = ref('UNFINISHED')
const orders = ref([])
const dialogVisible = ref(false)
const currentDetail = ref(null)

const fetchOrders = async () => {
  orders.value = [] // 清空旧数据防止闪烁
  
  if (activeTab.value === 'UNFINISHED') {
    // 【核心逻辑】"未完成" = "待配送(PAID)" + "配送中(DELIVERING)"
    // 并发请求两种状态的订单，然后合并
    try {
      const [paidRes, deliveringRes] = await Promise.all([
        request.get('/order/list', { params: { status: 'PAID' } }),
        request.get('/order/list', { params: { status: 'DELIVERING' } })
      ])
      
      // 合并数组
      let list = [...(paidRes || []), ...(deliveringRes || [])]
      
      // 按时间倒序排列（最新的在最前）
      list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      
      orders.value = list
    } catch (e) {
      console.error(e)
    }
  } else {
    // "已完成"
    const res = await request.get('/order/list', { params: { status: 'COMPLETED' } })
    if(res) {
      orders.value = res
    }
  }
}

const showDetail = async (row) => {
  const res = await request.get(`/order/detail/${row.orderNo}`)
  currentDetail.value = res
  dialogVisible.value = true
}

const updateStatus = async (row, action) => {
  try {
    await request.post(`/order/${action}/${row.orderNo}`)
    ElMessage.success('操作成功')
    fetchOrders() // 刷新列表
  } catch(e) {
    // handled
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.p-20 { padding: 20px; }
.mt-20 { margin-top: 20px; }
.mb-20 { margin-bottom: 20px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
</style>