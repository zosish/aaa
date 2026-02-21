import { createRouter, createWebHistory } from 'vue-router'
//import App from '../App.vue' // 根据实际路径调整

// 定义路由
const routes = [
  {
    path: '/',
    name: 'Login',
    component:  ()=>import('@/components/LoginPage.vue')// 根据实际情况设置组件
  },
  {
    path: '/HomePage',
    name: 'Home',
    component: () => import('@/components/HomePage.vue')
  },
  // 添加猫咪管理路由
  {
    path: '/admin/cats/list',
    name: 'Cat',
    component: () => import('@/components/catPage.vue')
  },
  // 添加预约路由
  {
    path: '/admin/appointments',
    name: 'Appointment',
    component: () => import('@/components/AppointmentPage.vue')
  },
  // 添加用户列表路由
  {
    path: '/admin/users',
    name: 'UserList',
    component: () => import('@/components/UserListPage.vue')
  },
  // 添加产品列表路由
  {
    path: '/admin/products/list',
    name: 'ProductList',
    component: () => import('@/components/ProductListPage.vue')
  },
  // 添加活动列表路由
  {
    path: '/admin/activities/list',
    name: 'ActivityList',
    component: () => import('@/components/ActiveListPage.vue')
  },
  // 添加评价列表路由
  {
    path: '/admin/reviews/list',
    name: 'EvaluationList',
    component: () => import('@/components/EvaluationListPage.vue')
  },
  // 添加角色权限路由
  {
    path: '/admin/roles/list',
    name: 'RoleList',
    component: () => import('@/components/RolePermissionsPage.vue')
  },
  // 添加健康记录路由
  {
    path: '/admin/cats/health',
    name: 'HealthRecords',
    component: () => import('@/components/HealthRecordsPage.vue')
  },
  // 添加时段设置路由
  {
    path: '/admin/reservations/slots',
    name: 'TimePeriodSettings',
    component: () => import('@/components/TimePeriodSettingsPage.vue')
  },
  // 添加分类管理路由
  {
    path: '/admin/products/categories',
    name: 'CategoryManagement',
    component: () => import('@/components/CategoryManagement.vue')
  },
  // 最近动态路由
  {
    path: '/admin/activities/recent',
    name: 'RecentActivities',
    component: () => import('@/components/RecentActivitiesPage.vue')
  },
  // 订单管理路由
  {
    path: '/admin/products/orders',
    name: 'OrderManagement',
    component: () => import('@/components/OrderManagementPage.vue')
  },

  // 添加更多路由...
]

// 创建 router 实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router