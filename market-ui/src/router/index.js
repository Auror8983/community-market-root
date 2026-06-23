import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    
    // --- 客户端 ---
    {
      path: '/client',
      component: () => import('../views/client/Layout.vue'),
      redirect: '/client/products',
      children: [
        { path: 'products', component: () => import('../views/client/ProductList.vue') },
        { path: 'cart', component: () => import('../views/client/Cart.vue') },
        { path: 'orders', component: () => import('../views/client/OrderList.vue') },
        { path: 'profile', component: () => import('../views/client/Profile.vue') }
      ]
    },

    // --- 员工/管理员 ---
    {
      path: '/employee',
      component: () => import('../views/employee/Layout.vue'),
      redirect: '/employee/notifications', // 【修改】默认跳转到消息中心
      children: [
        // 消息/通知中心
        { path: 'notifications', component: () => import('../views/employee/NotificationCenter.vue') },
        
        { path: 'warning', component: () => import('../views/employee/Warning.vue') },
        { path: 'inbound', component: () => import('../views/employee/Inbound.vue') },
        { path: 'orders', component: () => import('../views/employee/OrderManage.vue') },
        { path: 'inventory', component: () => import('../views/employee/InventoryManage.vue') },
        { path: 'profile', component: () => import('../views/client/Profile.vue') },

        // 管理员功能
        { path: 'products', component: () => import('../views/employee/ProductManage.vue') },
        { path: 'stats', component: () => import('../views/admin/SalesStats.vue') },
        { path: 'users', component: () => import('../views/admin/UserManage.vue') },
        { path: 'factories', component: () => import('../views/admin/FactoryManage.vue') }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.path !== '/login' && !store.userInfo.userId) {
    next('/login')
  } else {
    next()
  }
})

export default router