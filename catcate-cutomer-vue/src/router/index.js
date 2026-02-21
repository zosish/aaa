import { createRouter, createWebHistory } from 'vue-router'

// 定义路由
const routes = [
  // 首页
  {
    path: '/',
    name: 'Home',
    component: () => import('@/components/HomePage.vue')
  },
  // 登录
  {
    path: '/HomePage',
    name: 'HomePage',
    component: () => import('@/components/HomePage.vue')
  },
  // 猫咪图鉴
  {
    path: '/CatGuidePage',
    name: 'CatGuidePage',
    component: () => import('@/components/CatGuidePage.vue')
  },
  // 登录
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/components/LoginPage.vue')
  },
  // 套餐页面
  {
    path: '/ReservationActivityPage',
    name: 'ReservationActivityPage',
    component: () => import('@/components/ReservationActivityPage.vue')
  },
  // 周边商品商品页面
  {
    path: '/ProductPage',
    name: 'ProductPage',
    component: () => import('@/components/ProductPage.vue')
  },
  // 商品详情页面
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/components/ProductDetailPage.vue'),
    props: true
  },
  // 客户端活动页面
  {
    path: '/activities',
    name: 'ClientActivities',
    component: () => import('@/components/ClientActivitiesPage.vue')
  },
  // 支付成功页面
  {
    path: '/PaymentSuccessfulPage',
    name: 'PaymentSuccessfulPage',
    component: () => import('@/components/PaymentSuccessfulPage.vue'),
    props: true
  },
  // 购物车页面
  {
    path: '/cart',
    name: 'ShoppingCart',
    component: () => import('@/components/ShoppingCartPage.vue')
  },
  // 订单详情页面
  {
    path: '/OrderDetailPage',
    name: 'OrderDetailPage',
    component: () => import('@/components/OrderDetailPage.vue')
  },
  // 我的订单页面
  {
    path: '/my-orders',
    name: 'MyOrders',
    component: () => import('@/components/MyOrdersPage.vue')
  },
  // 订单详情查看页面
  {
    path: '/order-detail',
    name: 'OrderDetailView',
    component: () => import('@/components/OrderDetailView.vue')
  },
  // 个人中心页面
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('@/components/UserProfilePage.vue')
  },
  // 预约撸猫填写信息页面
  {
    path: '/reservation-cat',
    name: 'AppointmentCatsPage',
    component: () => import('../components/AppointmentCatsPage.vue'),
    props: (route) => ({
      catId: route.query.catId
    })
  },
  // 我的预约页面
  {
    path: '/my-reservations',
    name: 'MyReservations',
    component: () => import('@/components/MyReservationsPage.vue')
  },
  // 添加更多路由...
]

// 创建 router 实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  // 添加滚动行为控制
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（浏览器前进后退），则恢复到该位置
    if (savedPosition) {
      return savedPosition;
    }
    // 对于商品详情页，始终滚动到顶部
    if (to.name === 'ProductDetail') {
      return { top: 0, behavior: 'smooth' };
    }
    // 其他页面默认滚动到顶部
    return { top: 0 };
  }
})

export default router