<template>
  <div class="p-20">
    <el-card shadow="never">
      <template #header>
        <div class="flex-between">
          <span style="font-size: 18px; font-weight: bold;">📈 销售数据统计报表</span>
          <el-radio-group v-model="statType" @change="handleTypeChange" size="large">
            <el-radio-button label="PRODUCT">🛒 商品销量榜</el-radio-button>
            <el-radio-button label="USER">💰 顾客消费榜</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-if="statType === 'PRODUCT'">
        <el-form :inline="true" class="mb-20 filter-bar">
          <el-form-item label="商品类型">
            <el-select v-model="productQuery.type" placeholder="全部类型" clearable style="width: 150px">
               <el-option v-for="t in ['食品','饮品','日用品','调味品']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="价格区间">
            <div class="flex-center">
              <el-input-number v-model="productQuery.minPrice" :min="0" :controls="false" placeholder="最低价" style="width: 100px" />
              <span style="margin: 0 10px">-</span>
              <el-input-number v-model="productQuery.maxPrice" :min="0" :controls="false" placeholder="最高价" style="width: 100px" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="fetchProductStats">筛选</el-button>
            <el-button icon="Refresh" @click="resetProductQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table 
          :data="productData" 
          border stripe 
          style="width: 100%" 
          :default-sort="{ prop: 'totalSold', order: 'descending' }"
        >
          <el-table-column type="index" label="排名" width="80" align="center">
             <template #default="scope">
               <div class="rank-badge" :class="'rank-'+(scope.$index+1)" v-if="scope.$index < 3">{{ scope.$index + 1 }}</div>
               <span v-else>{{ scope.$index + 1 }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="type" label="类型" width="100" align="center">
            <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="120" sortable>
             <template #default="{row}">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="totalRevenue" label="总销售额" width="150" sortable>
             <template #default="{row}">¥{{ row.totalRevenue }}</template>
          </el-table-column>
          <el-table-column prop="totalSold" label="总销量" width="150" sortable fixed="right">
             <template #default="{row}">
               <span style="font-size: 16px; font-weight: bold; color: #409EFF">{{ row.totalSold }}</span>
             </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="statType === 'USER'">
        <div class="mb-20" style="color: #666; font-size: 14px;">
          <el-icon><InfoFilled /></el-icon> 统计说明：仅统计已付款及完成的订单，已取消或待付款的订单不计入。
        </div>

        <el-table 
          :data="userData" 
          border stripe 
          style="width: 100%"
          :default-sort="{ prop: 'totalSpent', order: 'descending' }"
        >
          <el-table-column type="index" label="排名" width="80" align="center">
             <template #default="scope">
               <div class="rank-badge" :class="'rank-'+(scope.$index+1)" v-if="scope.$index < 3">{{ scope.$index + 1 }}</div>
               <span v-else>{{ scope.$index + 1 }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="realName" label="客户姓名" />
          <el-table-column prop="username" label="账号" />
          <el-table-column prop="phone" label="联系电话" />
          <el-table-column prop="orderCount" label="下单次数" sortable align="center" />
          <el-table-column prop="totalSpent" label="消费总额" sortable fixed="right">
             <template #default="{row}">
               <span style="font-size: 16px; font-weight: bold; color: #f56c6c">¥{{ row.totalSpent }}</span>
             </template>
          </el-table-column>
        </el-table>
      </div>

    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { InfoFilled } from '@element-plus/icons-vue'

const statType = ref('PRODUCT') // PRODUCT | USER

const productData = ref([])
const productQuery = ref({ type: '', minPrice: undefined, maxPrice: undefined })

const userData = ref([])

// 加载商品统计
const fetchProductStats = async () => {
  const res = await request.get('/order/stats/product', { params: productQuery.value })
  productData.value = res
}

// 加载用户统计
const fetchUserStats = async () => {
  const res = await request.get('/order/stats/user')
  userData.value = res
}

const handleTypeChange = (val) => {
  if (val === 'PRODUCT') fetchProductStats()
  else fetchUserStats()
}

const resetProductQuery = () => {
  productQuery.value = { type: '', minPrice: undefined, maxPrice: undefined }
  fetchProductStats()
}

onMounted(() => {
  fetchProductStats() // 默认加载商品榜
})
</script>

<style scoped>
.p-20 { padding: 20px; }
.mb-20 { margin-bottom: 20px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { background: #f9fafe; padding: 15px; border-radius: 4px; }
.flex-center { display: flex; align-items: center; }

/* 排名徽章样式 */
.rank-badge {
  width: 24px; height: 24px; line-height: 24px; border-radius: 50%; 
  color: white; font-weight: bold; margin: 0 auto;
}
.rank-1 { background-color: #f56c6c; box-shadow: 0 2px 6px rgba(245, 108, 108, 0.4); } /* 金 */
.rank-2 { background-color: #E6A23C; } /* 银 */
.rank-3 { background-color: #409EFF; } /* 铜 */
</style>