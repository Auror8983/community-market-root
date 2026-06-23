<template>
  <div class="p-20">
    <el-card class="mb-20" shadow="never">
      <el-form :inline="true" class="search-form">
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="搜索商品" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部类型" style="width: 150px;">
            <el-option v-for="t in ['食品','饮品','日用品','调味品']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="特惠">
           <el-checkbox v-model="query.onlyDiscount" label="仅看临期/打折" border />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="fetchProducts">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="6" v-for="prod in products" :key="prod.productId" class="mb-20">
        <el-card :body-style="{ padding: '0px' }" class="product-card" shadow="hover">
          <div class="card-content">
            <div class="card-header">
              <h3 class="title" :title="prod.name">{{ prod.name }}</h3>
              <el-tag size="small">{{ prod.type }}</el-tag>
            </div>
            
            <div class="price-section">
              <span class="currency">¥</span>
              <span class="price">{{ getCurrentPrice(prod) }}</span>
              <span v-if="isCurrentDiscounted(prod)" class="original-price">¥{{ prod.price.toFixed(2) }}</span>
            </div>

            <div class="info-row">
              <span>保质期: {{ prod.shelfLifeValue }}{{ unitMap[prod.shelfLifeUnit] }}</span>
              <span>总库存: {{ prod.totalStock }}</span>
            </div>

            <div class="batch-box">
              <el-select 
                v-model="prod.selectedBatchId" 
                placeholder="默认发货 (系统自动分配)" 
                size="small" 
                clearable
                style="width: 100%">
                <el-option 
                  v-for="batch in prod.batches" 
                  :key="batch.batchId" 
                  :label="formatBatchLabel(batch)" 
                  :value="batch.batchId">
                  <div style="display: flex; justify-content: space-between; width: 100%">
                    <span>{{ batch.batchId }} (库存:{{ batch.stockQuantity }})</span>
                    <span v-if="batch.isNearExpiry" style="color: #f56c6c; font-size: 12px; margin-left: 10px;">临期8折</span>
                  </div>
                </el-option>
              </el-select>
            </div>

            <div class="tips-box">
              <div v-if="isCurrentDiscounted(prod)" class="discount-tip">
                🔥 已选临期批次，享受 8 折优惠！
              </div>
              <div class="date-tip">
                📅 生产日期: {{ getCurrentBatch(prod)?.productionDate || '多批次混合/优选' }}
              </div>
            </div>

            <div class="actions">
              <el-input-number v-model="prod.buyCount" :min="1" :max="999" size="small" style="width: 100px" />
              <el-button type="primary" size="small" icon="ShoppingCart" @click="addToCart(prod)">加入</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const query = ref({ name: '', type: '', onlyDiscount: false })
const products = ref([])
const cart = ref(JSON.parse(sessionStorage.getItem('cart') || '[]'))
const unitMap = { 'YEAR': '年', 'MONTH': '月', 'DAY': '天' }

const fetchProducts = async () => {
  const res = await request.get('/product/list', { params: { ...query.value, onlyOnShelf: true } })
  // 前端过滤
  let list = res.map(p => ({...p, selectedBatchId: null, buyCount: 1}))
  if(query.value.onlyDiscount) {
    list = list.filter(p => p.batches.some(b => b.isNearExpiry))
  }
  products.value = list
}

const getCurrentBatch = (prod) => {
  if (prod.selectedBatchId) {
    return prod.batches.find(b => b.batchId === prod.selectedBatchId)
  }
  // 如果没选，返回null，表示自动分配
  return null
}

// 格式化下拉选项文字
const formatBatchLabel = (batch) => {
  //在标签里也显示库存，方便用户查看
  let label = `${batch.batchId} [余:${batch.stockQuantity}]`
  if (batch.isNearExpiry) label += ' (临期8折)'
  return label
}

const isCurrentDiscounted = (prod) => {
  const batch = getCurrentBatch(prod)
  return batch ? batch.isNearExpiry : false
}

const getCurrentPrice = (prod) => {
  if (isCurrentDiscounted(prod)) {
    return (prod.price * 0.8).toFixed(2)
  }
  return prod.price.toFixed(2)
}

// 内部辅助函数：真正执行写入购物车
const commitToCart = (productId, batchId, quantity) => {
  const existing = cart.value.find(c => c.productId === productId && c.batchId === batchId)
  if(existing) {
    existing.quantity += quantity
  } else {
    cart.value.push({
      productId: productId,
      batchId: batchId,
      quantity: quantity
    })
  }
}

// 核心逻辑修改：加入购物车
const addToCart = (prod) => {
  const buyQty = prod.buyCount

  // --- 情况1：用户指定了批次 ---
  if (prod.selectedBatchId) {
    const batch = prod.batches.find(b => b.batchId === prod.selectedBatchId)
    if (!batch) return
    
    // 校验该批次库存
    if (buyQty > batch.stockQuantity) {
      ElMessage.error(`该批次库存不足！当前库存仅剩: ${batch.stockQuantity}`)
      return
    }
    
    // 执行添加
    commitToCart(prod.productId, batch.batchId, buyQty)
    ElMessage.success(`已添加 ${buyQty} 件到购物车`)
  } 
  
  // --- 情况2：用户未指定批次（自动分配）---
  else {
    // 1. 校验总库存
    if (buyQty > prod.totalStock) {
      ElMessage.error(`商品总库存不足！当前总库存仅剩: ${prod.totalStock}`)
      return
    }

    // 2. 自动分配算法 (优先扣减最早过期的/列表最前面的)
    let remainingNeed = buyQty
    let allocatedInfo = [] // 用于提示用户

    // 过滤出有库存的批次
    const availableBatches = prod.batches.filter(b => b.stockQuantity > 0)

    for (const batch of availableBatches) {
      if (remainingNeed <= 0) break // 分配完毕

      // 计算从当前批次拿多少：想要拿的 vs 当前有的
      const takeAmount = Math.min(remainingNeed, batch.stockQuantity)
      
      commitToCart(prod.productId, batch.batchId, takeAmount)
      
      allocatedInfo.push(`批次${batch.batchId} x ${takeAmount}`)
      remainingNeed -= takeAmount
    }

    // 保存到本地存储
    sessionStorage.setItem('cart', JSON.stringify(cart.value))
    
    ElMessage.success(`自动分配成功: ${allocatedInfo.join(', ')}`)
  }

  // 最后统一保存一次（如果是情况1，也需要保存）
  sessionStorage.setItem('cart', JSON.stringify(cart.value))
}

onMounted(fetchProducts)
</script>

<style scoped>
.p-20 { padding: 20px; }
.mb-20 { margin-bottom: 20px; }
.search-form .el-form-item { margin-bottom: 0; margin-right: 15px; }
.product-card { border-radius: 8px; transition: all 0.3s; border: 1px solid #ebeef5; }
.product-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.card-content { padding: 15px; }
.card-header { display: flex; justify-content: space-between; margin-bottom: 10px; height: 24px; }
.title { margin: 0; font-size: 16px; width: 140px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price-section { margin-bottom: 10px; }
.currency { font-size: 14px; color: #f56c6c; }
.price { font-size: 22px; font-weight: bold; color: #f56c6c; }
.original-price { font-size: 12px; color: #999; text-decoration: line-through; margin-left: 5px; }
.info-row { display: flex; justify-content: space-between; font-size: 12px; color: #666; margin-bottom: 10px; }
.batch-box { margin-bottom: 10px; }
.tips-box { min-height: 40px; margin-bottom: 10px; font-size: 12px; }
.discount-tip { color: #f56c6c; font-weight: bold; margin-bottom: 2px; }
.date-tip { color: #606266; }
.actions { display: flex; justify-content: space-between; }
</style>