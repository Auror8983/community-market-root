<template>
  <div class="p-20">
    <el-card shadow="never">
      <template #header>
        <div class="flex-between">
          <span style="font-size: 18px; font-weight: bold;">🛒 我的购物车</span>
          <el-button type="danger" link @click="clearCart" icon="Delete">清空购物车</el-button>
        </div>
      </template>

      <el-table :data="previewData.items" stripe style="width: 100%" :header-cell-style="{background:'#f5f7fa'}">
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="batchId" label="批次" width="180">
          <template #default="scope">
            {{ scope.row.batchId }}
            <el-tag v-if="scope.row.isDiscounted" type="danger" size="small" effect="dark" class="ml-5">临期8折</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
           <template #default="scope">¥{{ scope.row.originalPrice }}</template>
        </el-table-column>
        <el-table-column prop="finalPrice" label="成交价" width="100">
           <template #default="scope">
             <span :class="{ 'price-danger': scope.row.isDiscounted }">¥{{ scope.row.finalPrice }}</span>
           </template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" size="small" @change="updateQuantity(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120">
           <template #default="scope"><span style="font-weight: bold">¥{{ scope.row.subtotal }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="scope">
            <el-button type="danger" icon="Delete" circle size="small" @click="removeItem(scope.row)" />
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">🚚 收货信息 (不填默认使用个人中心信息)</el-divider>
      <el-form :inline="true" :model="addressForm" class="address-form">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" placeholder="默认姓名" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="addressForm.receiverPhone" placeholder="默认电话" /></el-form-item>
        <el-form-item label="地址" style="width: 400px"><el-input v-model="addressForm.receiverAddress" placeholder="默认地址" style="width: 350px" /></el-form-item>
      </el-form>

      <div class="footer mt-20">
        <div class="total-bar">
          <div class="total-text">总计: <span class="big-price">¥{{ previewData.totalAmount }}</span></div>
          <el-button type="primary" size="large" @click="createOrder">提交订单</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const cartItems = ref(JSON.parse(sessionStorage.getItem('cart') || '[]'))
const previewData = ref({ items: [], totalAmount: 0 })
const addressForm = ref({ receiverName: '', receiverPhone: '', receiverAddress: '' })
const userStore = useUserStore()
const router = useRouter()

const loadPreview = async () => {
  if(cartItems.value.length === 0) return
  const res = await request.post('/order/cart/preview', cartItems.value)
  previewData.value = res
}

const updateQuantity = (item) => {
  const target = cartItems.value.find(c => c.productId === item.productId && c.batchId === item.batchId)
  if(target) target.quantity = item.quantity
  sessionStorage.setItem('cart', JSON.stringify(cartItems.value))
  loadPreview()
}

const removeItem = (item) => {
  cartItems.value = cartItems.value.filter(c => !(c.productId === item.productId && c.batchId === item.batchId))
  sessionStorage.setItem('cart', JSON.stringify(cartItems.value))
  loadPreview()
}

const clearCart = () => {
  cartItems.value = []
  sessionStorage.removeItem('cart')
  previewData.value = { items: [], totalAmount: 0 }
}

const createOrder = async () => {
  if (cartItems.value.length === 0) return ElMessage.warning('购物车是空的')
  
  // 提交订单，带上地址
  const orderDTO = {
    userId: userStore.userInfo.userId,
    items: cartItems.value,
    ...addressForm.value
  }
  
  try {
    await request.post('/order/create', orderDTO)
    ElMessage.success('订单提交成功，请前往订单中心付款')
    clearCart()
    router.push('/client/orders')
  } catch (e) {
    // handled by request.js
  }
}

// 初始化时尝试填充默认信息
onMounted(() => {
  loadPreview()
  // 预填个人信息方便用户看
  if(userStore.userInfo) {
    addressForm.value.receiverName = userStore.userInfo.realName
    addressForm.value.receiverPhone = userStore.userInfo.phone
    addressForm.value.receiverAddress = userStore.userInfo.address
  }
})
</script>

<style scoped>
.p-20 { padding: 20px; }
.ml-5 { margin-left: 5px; }
.price-danger { color: #f56c6c; font-weight: bold; }
.footer { border-top: 1px solid #ebeef5; padding-top: 20px; text-align: right; }
.total-bar { display: flex; justify-content: flex-end; align-items: center; gap: 20px; }
.big-price { font-size: 28px; color: #f56c6c; font-weight: bold; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.address-form { margin-top: 20px; }
</style>