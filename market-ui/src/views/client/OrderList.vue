<template>
  <div class="p-20">
    <el-card shadow="never">
      <template #header>我的订单</template>
      
      <el-table :data="orders" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="totalAmount" label="总金额">
           <template #default="{row}">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{row}">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            
            <span v-if="row.status === 'CREATED'">
              <el-button type="primary" size="small" @click="handlePay(row)">去付款</el-button>
              <el-button type="warning" size="small" @click="openEditAddress(row)">修改地址</el-button>
              <el-button type="danger" size="small" @click="handleCancel(row)">取消订单</el-button>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="📜 订单详情" width="800px">
      <div v-if="currentOrder">
        <el-descriptions border :column="2" class="mb-20">
           <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
           <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
           <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
           <el-descriptions-item label="电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
           <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
           <el-descriptions-item label="订单总价" :span="2">
             <span style="font-size: 18px; color: red; font-weight: bold;">¥{{ currentOrder.totalAmount }}</span>
           </el-descriptions-item>
        </el-descriptions>

        <el-table :data="currentOrder.details" border stripe>
          <el-table-column prop="productId" label="商品ID" width="100" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="batchId" label="批次ID" width="150" />
          <el-table-column prop="unitPrice" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="subtotal" label="小计" width="100">
             <template #default="{row}"><span style="font-weight: bold;">¥{{ row.subtotal }}</span></template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="editAddressVisible" title="✏️ 修改收货信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="editForm.address" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editAddressVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddressUpdate">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const orders = ref([])
const detailVisible = ref(false)
const editAddressVisible = ref(false)
const currentOrder = ref(null)
const editForm = ref({})

const fetchOrders = async () => {
  const res = await request.get('/order/list', { params: { userId: userStore.userInfo.userId } })
  orders.value = res
}

const getStatusText = (status) => {
  const map = { 'CREATED': '待付款', 'PAID': '待配送', 'DELIVERING': '配送中', 'COMPLETED': '已完成' }
  return map[status] || status
}
const getStatusTag = (status) => {
  const map = { 'CREATED': 'danger', 'PAID': 'warning', 'DELIVERING': 'primary', 'COMPLETED': 'success' }
  return map[status]
}

const viewDetail = async (row) => {
  const res = await request.get(`/order/detail/${row.orderNo}`)
  currentOrder.value = res
  detailVisible.value = true
}

// 1. 取消订单（逻辑：详情 -> 写入本地购物车 -> 调用取消API）
const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消该订单吗？取消后商品将放回购物车。', '提示', { type: 'warning' })
    .then(async () => {
      // 先获取详情以拿到商品列表
      const detail = await request.get(`/order/detail/${row.orderNo}`)
      
      // 恢复到本地购物车
      const cart = JSON.parse(sessionStorage.getItem('cart') || '[]')
      detail.details.forEach(item => {
        // 查找是否已存在相同商品和批次
        const exist = cart.find(c => c.productId === item.productId && c.batchId === item.batchId)
        if (exist) {
          exist.quantity += item.quantity
        } else {
          cart.push({ productId: item.productId, batchId: item.batchId, quantity: item.quantity })
        }
      })
      sessionStorage.setItem('cart', JSON.stringify(cart))

      // 调用后端取消接口
      await request.post(`/order/cancel/${row.orderNo}`)
      ElMessage.success('订单已取消，商品已回退至购物车')
      fetchOrders()
    })
}

// 2. 修改地址
const openEditAddress = (row) => {
  editForm.value = {
    orderNo: row.orderNo,
    name: row.receiverName,
    phone: row.receiverPhone,
    address: row.receiverAddress
  }
  editAddressVisible.value = true
}

const submitAddressUpdate = async () => {
  await request.post('/order/update-address', editForm.value)
  ElMessage.success('收货信息已更新')
  editAddressVisible.value = false
  fetchOrders()
}

// 3. 去付款
const handlePay = (row) => {
  ElMessageBox.confirm(`确认支付 ¥${row.totalAmount} 吗？`, '支付确认', { confirmButtonText: '确认支付' })
    .then(async () => {
      await request.post(`/order/pay/${row.orderNo}`)
      ElMessage.success('支付成功，等待商家配送')
      fetchOrders()
    })
}

onMounted(fetchOrders)
</script>
<style scoped> .p-20 { padding: 20px; } .mb-20 { margin-bottom: 20px; } </style>