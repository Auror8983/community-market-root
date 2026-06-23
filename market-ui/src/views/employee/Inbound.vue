<template>
  <div class="p-20">
    <el-card header="📦 商品入库管理">
      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="选择商品">
          <el-select 
            v-model="form.productName" 
            placeholder="请选择库中已有的商品" 
            filterable 
            style="width: 100%"
            @change="handleProductSelect"> 
            <el-option 
              v-for="item in productOptions" 
              :key="item.productId" 
              :label="item.name + ' (' + item.type + ')'" 
              :value="item.name" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="selectedProductInfo" label="商品信息">
          <el-tag type="info">类型: {{ selectedProductInfo.type }}</el-tag>
          <el-tag type="warning" style="margin-left: 10px">保质期: {{ selectedProductInfo.shelfLifeValue }}{{ unitMap[selectedProductInfo.shelfLifeUnit] }}</el-tag>
        </el-form-item>
        
        <el-form-item label="生产日期">
          <el-date-picker 
            v-model="form.productionDate" 
            type="date" 
            placeholder="选择生产日期" 
            style="width: 100%" 
            value-format="YYYY-MM-DD" />
        </el-form-item>
        
        <el-form-item label="入库数量">
          <el-input-number v-model="form.quantity" :min="1" :step="10" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" @click="submitInbound">确认入库并生成批次</el-button>
        </el-form-item>
      </el-form>
      
      <el-descriptions title="操作提示" :column="1" border class="mt-20">
        <el-descriptions-item label="自动计算">系统将根据所选商品的基础保质期，自动计算过期时间。</el-descriptions-item>
        <el-descriptions-item label="批次生成">系统将查询当前最大批次号，自动加1生成新批次ID。</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const route = useRoute()
// 默认数量改为 20
const form = ref({ productName: '', productionDate: '', quantity: 20 })
const productOptions = ref([])
const selectedProductInfo = ref(null)
const unitMap = { 'YEAR': '年', 'MONTH': '月', 'DAY': '天' }

// 加载已有商品供选择
const loadProducts = async () => {
  const res = await request.get('/product/list', { params: { onlyOnShelf: false } })
  productOptions.value = res
  
  // 核心逻辑：检查路由参数是否有 productName，如果有，自动选中
  if (route.query.productName) {
    const targetName = route.query.productName
    // 检查该商品是否存在于列表中
    const exists = res.find(p => p.name === targetName)
    if (exists) {
      form.value.productName = targetName
      handleProductSelect(targetName) // 触发联动，显示保质期信息
    }
  }
}

const handleProductSelect = (name) => {
  const prod = productOptions.value.find(p => p.name === name)
  selectedProductInfo.value = prod
}

const submitInbound = async () => {
  if(!form.value.productName || !form.value.productionDate) return ElMessage.warning('请填写完整信息')
  await request.post('/product/inbound', form.value)
  ElMessage.success('入库成功！')
  // 重置表单，数量保持20
  form.value = { productName: '', productionDate: '', quantity: 20 }
  selectedProductInfo.value = null
}

onMounted(loadProducts)
</script>
<style scoped> .p-20 { padding: 20px; } .mt-20 { margin-top: 20px; } </style>