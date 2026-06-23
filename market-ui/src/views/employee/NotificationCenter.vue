<template>
  <div class="p-20">
    <div class="welcome-banner mb-20">
      <h2>👋 欢迎回来，{{ userStore.userInfo.realName }}</h2>
      <p class="subtitle">今天是 {{ currentDate }}，以下是您需要关注的待办事项：</p>
    </div>

    <el-row :gutter="20" class="mb-20">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card" @click="router.push('/employee/orders')">
          <div class="card-icon blue-bg"><el-icon><Box /></el-icon></div>
          <div class="card-info">
            <div class="label">待发货订单</div>
            <div class="value">{{ stats.toShip }}</div>
          </div>
          <div class="card-action">
            <el-tag v-if="stats.toShip > 0" type="danger" effect="dark">急需处理</el-tag>
            <el-tag v-else type="success">已清空</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card" @click="router.push('/employee/orders')">
          <div class="card-icon orange-bg"><el-icon><Van /></el-icon></div>
          <div class="card-info">
            <div class="label">配送中订单</div>
            <div class="value">{{ stats.delivering }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card" @click="router.push('/employee/warning')">
          <div class="card-icon yellow-bg"><el-icon><Timer /></el-icon></div>
          <div class="card-info">
            <div class="label">临期批次</div>
            <div class="value">{{ stats.nearExpiry }}</div>
          </div>
          <div class="card-action">
            <el-tag v-if="stats.nearExpiry > 0" type="warning">8折生效中</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card" @click="router.push('/employee/warning')">
          <div class="card-icon red-bg"><el-icon><Warning /></el-icon></div>
          <div class="card-info">
            <div class="label">过期/缺货</div>
            <div class="value">{{ stats.expired + stats.lowStock }}</div>
          </div>
          <div class="card-action">
            <el-tag v-if="stats.expired > 0" type="danger" effect="dark">含过期品</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card" header="🚀 快捷入口">
          <div class="quick-actions">
            <el-button type="primary" size="large" icon="Box" @click="router.push('/employee/inbound')">商品入库</el-button>
            <el-button type="success" size="large" icon="Tickets" @click="router.push('/employee/inventory')">库存查询</el-button>
            <el-button type="warning" size="large" icon="Bell" @click="router.push('/employee/warning')">预警处理</el-button>
            <el-button v-if="userStore.userInfo.role === 'ADMIN'" type="danger" size="large" icon="DataLine" @click="router.push('/employee/stats')">查看报表</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="box-card" header="🔔 系统通知">
          <el-timeline>
            <el-timeline-item v-if="stats.toShip > 0" type="danger" timestamp="实时">
              您有 {{ stats.toShip }} 个订单已付款，请尽快安排发货。
            </el-timeline-item>
            <el-timeline-item v-if="stats.expired > 0" type="danger" timestamp="严重">
              检测到 {{ stats.expired }} 个批次商品已过期，请立即前往预警中心清除库存！
            </el-timeline-item>
            <el-timeline-item v-if="stats.lowStock > 0" type="warning" timestamp="提醒">
              有 {{ stats.lowStock }} 种商品库存低于阈值，建议及时补货。
            </el-timeline-item>
            <el-timeline-item v-if="stats.nearExpiry > 0" type="info" timestamp="提醒">
              {{ stats.nearExpiry }} 个批次即将过期，系统已自动应用8折优惠。
            </el-timeline-item>
            
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const currentDate = new Date().toLocaleDateString()

const stats = ref({
  toShip: 0,
  delivering: 0,
  lowStock: 0,
  nearExpiry: 0,
  expired: 0
})

const initData = async () => {
  // 1. 获取订单统计
  try {
    const orders = await request.get('/order/list')
    // 统计状态
    stats.value.toShip = orders.filter(o => o.status === 'PAID').length
    stats.value.delivering = orders.filter(o => o.status === 'DELIVERING').length
  } catch (e) {
    console.error("加载订单失败", e)
  }

  // 2. 获取预警统计
  try {
    const warning = await request.get('/product/warning')
    if (warning) {
      stats.value.lowStock = warning.lowStock ? warning.lowStock.length : 0
      stats.value.nearExpiry = warning.nearExpiry ? warning.nearExpiry.length : 0
      stats.value.expired = warning.expired ? warning.expired.length : 0
    }
  } catch (e) {
    console.error("加载预警失败", e)
  }
}

onMounted(initData)
</script>

<style scoped>
.p-20 { padding: 20px; }
.mb-20 { margin-bottom: 20px; }
.welcome-banner h2 { margin: 0 0 5px 0; color: #303133; }
.subtitle { color: #909399; margin: 0; font-size: 14px; }

.data-card { cursor: pointer; transition: transform 0.2s; display: flex; align-items: center; position: relative; overflow: hidden; }
.data-card:hover { transform: translateY(-5px); }
:deep(.el-card__body) { display: flex; align-items: center; width: 100%; padding: 20px; }

.card-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 30px; color: white; margin-right: 15px; }
.blue-bg { background: linear-gradient(135deg, #409EFF, #79bbff); }
.orange-bg { background: linear-gradient(135deg, #E6A23C, #f3d19e); }
.yellow-bg { background: linear-gradient(135deg, #e6a23c, #f8e3c5); }
.red-bg { background: linear-gradient(135deg, #F56C6C, #fab6b6); }

.card-info { flex-grow: 1; }
.label { font-size: 14px; color: #909399; margin-bottom: 5px; }
.value { font-size: 28px; font-weight: bold; color: #303133; }
.card-action { position: absolute; top: 15px; right: 15px; }

.quick-actions { display: flex; gap: 15px; flex-wrap: wrap; }
.quick-actions .el-button { flex: 1; height: 80px; font-size: 16px; margin: 0; }
</style>