<template>
  <div class="admin-dashboard">
    <!-- 顶部导航 -->
    <el-header class="main-header">
      <div class="logo">
        <el-icon :size="28">
          <div>🐱</div>
        </el-icon>
        <span class="logo-text">喵时光猫咖管理系统</span>
      </div>
      <div class="header-actions">
        <el-dropdown>
          <el-button type="text" class="user-info">
            <el-avatar :size="32" :src="currentUser.avatar || defaultAvatar"></el-avatar>
            <span>{{ currentUser.nickname || currentUser.username }}</span>
            <el-icon :size="16" class="el-icon--right"></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleSettings">系统设置</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 侧边栏导航 -->
      <el-aside width="220px" class="sidebar">
        <el-menu :default-openeds="['dashboard']" :default-active="activeMenu" class="sidebar-menu"
          @select="handleMenuSelect">
          <el-menu-item index="dashboard">
            <el-icon><Layout /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-sub-menu index="users">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="users">用户列表</el-menu-item>
            <el-menu-item index="roles/list">角色权限</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="cats">
            <template #title>
              <el-icon><Cat /></el-icon>
              <span>猫咪管理</span>
            </template>
            <el-menu-item index="cats/list">猫咪列表</el-menu-item>
            <el-menu-item index="cats/health">健康记录</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="reservations">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>预约管理</span>
            </template>
            <el-menu-item index="appointments">预约列表</el-menu-item>
            <el-menu-item index="reservations/slots">时段设置</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="products">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="products/list">商品列表</el-menu-item>
            <el-menu-item index="products/categories">分类管理</el-menu-item>
            <el-menu-item index="products/orders">订单管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="activities">
            <template #title>
              <el-icon><Ticket /></el-icon>
              <span>活动管理</span>
            </template>
            <el-menu-item index="activities/list">活动列表</el-menu-item>
            <el-menu-item index="activities/create">创建活动</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="reviews">
            <template #title>
              <el-icon><Comment /></el-icon>
              <span>评价管理</span>
            </template>
            <el-menu-item index="reviews/list">评价列表</el-menu-item>
            <el-menu-item index="reviews/settings">审核设置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="content-area">
        <div class="page-title">数据概览</div>

        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card" hoverable>
              <div class="stat-content">
                <div class="stat-info">
                  <div class="stat-label">总用户数</div>
                  <div class="stat-value">{{ stats.totalUsers }}</div>
                </div>
                <div class="stat-icon icon-users">
                  <el-icon :size="28"><User /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card" hoverable>
              <div class="stat-content">
                <div class="stat-info">
                  <div class="stat-label">今日预约</div>
                  <div class="stat-value">{{ stats.todayReservations }}</div>
                </div>
                <div class="stat-icon icon-reservations">
                  <el-icon :size="28"><Calendar /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card" hoverable>
              <div class="stat-content">
                <div class="stat-info">
                  <div class="stat-label">本月销售额</div>
                  <div class="stat-value">¥{{ stats.monthlySales.toLocaleString() }}</div>
                </div>
                <div class="stat-icon icon-sales">
                  <el-icon :size="28"><Money /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card" hoverable>
              <div class="stat-content">
                <div class="stat-info">
                  <div class="stat-label">待处理事项</div>
                  <div class="stat-value">{{ stats.pendingTasks }}</div>
                </div>
                <div class="stat-icon icon-tasks">
                  <el-icon :size="28"><TodoList /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 图表区域 -->
        <el-row :gutter="20" class="charts-row">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>销售额趋势分析</span>
                  <el-select v-model="chartTimeRange" size="small" @change="handleTimeRangeChange">
                    <el-option label="本周" value="week"></el-option>
                    <el-option label="本月" value="month"></el-option>
                    <el-option label="本季度" value="quarter"></el-option>
                    <el-option label="本年" value="year"></el-option>
                  </el-select>
                </div>
                <div class="chart-container">
                  <canvas id="salesTrendChart"></canvas>
                </div>
              </template>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>商品销售额占比</span>
                </div>
                <div class="chart-container">
                  <canvas id="salesChart"></canvas>
                </div>
              </template>
            </el-card>
          </el-col>
        </el-row>

        <!-- 最近动态 -->
        <el-row class="recent-activities-row">
          <el-col :span="24">
            <el-card class="activities-card">
              <template #header>
                <div class="card-header">
                  <span>最近动态</span>
                  <el-button size="small" type="text" @click="viewAllActivities">查看全部</el-button>
                </div>
                <el-timeline>
                  <el-timeline-item v-for="(activity, index) in recentActivities" :key="index"
                    :timestamp="formatTime(activity.time)" :icon="activity.icon" :type="activity.type">
                    <el-card>
                      <h4>{{ activity.title }}</h4>
                      <p class="activity-content">{{ activity.content }}</p>
                      <!-- <div v-if="activity.operations" class="activity-ops">
                        <el-button v-for="op in activity.operations" :key="op.id" size="mini" :type="op.type"
                          @click="handleActivityOp(op.action, activity.data)">
                          {{ op.text }}
                        </el-button>
                      </div> -->
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </template>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<!--  eslint-disable no-undef  -->
<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Layout, User, Cat, Calendar, ShoppingCart,
  Ticket, Comment, ArrowUp, ArrowDown,
  Money, TodoList
} from '@element-plus/icons-vue';
import {
  Chart as ChartJS,
  LinearScale,
  CategoryScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  LineController,
  DoughnutController
} from 'chart.js';

// 注册所需组件
ChartJS.register(
  LinearScale,
  CategoryScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  LineController,
  DoughnutController
);

// 路由
const router = useRouter();

// 状态管理
const activeMenu = ref('dashboard');
const defaultAvatar = 'https://picsum.photos/seed/admin/100/100';
const chartTimeRange = ref('month');
const recentActivities = ref([]);
let salesTrendChart = null;

// 当前用户信息
const currentUser = reactive({
  id: 1,
  username: 'admin',
  nickname: '管理员',
  avatar: '',
  role: 'ADMIN'
});

// 统计数据
const stats = reactive({
  totalUsers: 1256,
  todayReservations: 28,
  monthlySales: 35890,
  pendingTasks: 12
});

// 获取最近动态数据
const fetchRecentActivities = async () => {
  try {
    console.log('首页开始获取最近动态数据...');
    
    // 首页只需要显示最近的几条动态，所以使用小的pageSize
    const response = await fetch('http://localhost:8081/catcate/users/recentActivities?pageNum=1&pageSize=5');
    
    console.log('首页API响应状态:', response.status);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    console.log('首页接收到的数据:', result);
    
    // 首页只需要list部分的数据
    const activities = result.list || [];
    
    // 格式化时间显示
    const formattedActivities = activities.map(activity => ({
      ...activity,
      time: formatDateTime(activity.time)
    }));
    
    recentActivities.value = formattedActivities;
    console.log('首页设置的动态数据:', formattedActivities);
    
  } catch (error) {
    console.error('获取最近动态失败:', error);
    // 保留默认数据作为后备
    recentActivities.value = []; // 清空数据而不是保留默认值
  }
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 初始化图表
onMounted(async () => {
  // 获取统计数据
  await fetchStats();
  // 获取最近动态数据
  await fetchRecentActivities();
  // 初始化图表
  await initSalesTrendChart();
  // 初始化销售额图表
  await initSalesChart();
});

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await fetch('http://localhost:8081/catcate/users/stats');
    const data = await response.json();
    
    // 假设后端返回的是一个包含四个数值的数组
    Object.assign(stats, {
      totalUsers: data[0] || 0,
      todayReservations: data[1] || 0,
      monthlySales: data[2] || 0,
      pendingTasks: data[3] || 0
    });
  } catch (error) {
    console.error('获取统计数据失败:', error);
  }
};

// 初始化销售额趋势图表
const initSalesTrendChart = async () => {
  // 先获取默认数据（本月数据）
  await loadSalesTrendData('month');
};

// 加载销售趋势数据
const loadSalesTrendData = async (timeRange) => {
  try {
    let startDate, endDate;
    const today = new Date();
    
    // 使用大括号包裹每个case块，避免变量声明问题
    switch(timeRange) {
      case 'week': {
        startDate = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
        endDate = today;
        break;
      }
      case 'month': {
        startDate = new Date(today.getFullYear(), today.getMonth(), 1);
        endDate = today;
        break;
      }
      case 'quarter': {
        const quarterStartMonth = Math.floor(today.getMonth() / 3) * 3;
        startDate = new Date(today.getFullYear(), quarterStartMonth, 1);
        endDate = today;
        break;
      }
      case 'year': {
        startDate = new Date(today.getFullYear(), 0, 1);
        endDate = today;
        break;
      }
      default: {
        startDate = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
        endDate = today;
      }
    }

    const response = await fetch(
      `http://localhost:8081/catcate/orders/sales/trend?startDate=${startDate.toISOString().split('T')[0]}&endDate=${endDate.toISOString().split('T')[0]}`
    );
    
    const result = await response.json();
    const trendData = result.trendData || [];
    
    // 处理数据格式
    const labels = trendData.map(item => item.date);
    const salesData = trendData.map(item => parseFloat(item.sales_amount) || 0);
    const orderCounts = trendData.map(item => parseInt(item.order_count) || 0);
    
    // 如果图表已存在，更新数据；否则创建新图表
    if (salesTrendChart) {
      salesTrendChart.data.labels = labels;
      salesTrendChart.data.datasets[0].data = salesData;
      salesTrendChart.data.datasets[1].data = orderCounts;
      salesTrendChart.update();
    } else {
      const ctx = document.getElementById('salesTrendChart').getContext('2d');
      salesTrendChart = new ChartJS(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [
            {
              label: '销售额(元)',
              data: salesData,
              borderColor: '#409eff',
              backgroundColor: 'rgba(64, 158, 255, 0.1)',
              tension: 0.3,
              fill: true,
              yAxisID: 'y'
            },
            {
              label: '订单数量',
              data: orderCounts,
              borderColor: '#67c23a',
              backgroundColor: 'rgba(103, 194, 58, 0.1)',
              tension: 0.3,
              fill: true,
              yAxisID: 'y1'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'top'
            }
          },
          scales: {
            y: {
              type: 'linear',
              beginAtZero: true,
              position: 'left',
              title: {
                display: true,
                text: '销售额(元)'
              },
              grid: {
                color: 'rgba(0, 0, 0, 0.05)'
              }
            },
            y1: {
              type: 'linear',
              beginAtZero: true,
              position: 'right',
              title: {
                display: true,
                text: '订单数量'
              },
              grid: {
                drawOnChartArea: false
              }
            },
            x: {
              type: 'category',
              grid: {
                display: false
              }
            }
          }
        }
      });
    }
  } catch (error) {
    console.error('加载销售趋势数据失败:', error);
    ElMessage.error('加载销售趋势数据失败');
  }
};

// 获取后端销售数据并转换格式
const fetchSalesData = async () => {
  try {
    const response = await fetch('http://localhost:8081/catcate/orderItems/sales');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    return convertSalesData(data);
  } catch (error) {
    console.error('获取销售数据失败:', error);
    ElMessage.error('获取销售数据失败，使用默认数据');
    return {
      labels: ['食品', '玩具', '用品', '周边'],
      data: [35, 25, 20, 20]
    };
  }
};

// 转换销售数据格式
const convertSalesData = (rawData) => {
  const categoryMap = {
    'FOOD': '食品',
    'TOY': '玩具',
    'SUPPLIES': '用品',
    'OTHER': '周边'
  };
  
  const categoryStats = {
    '食品': 0,
    '玩具': 0,
    '用品': 0,
    '周边': 0
  };
  
  rawData.forEach(item => {
    const categoryName = categoryMap[item.category] || '周边';
    categoryStats[categoryName] += item.subtotal || 0;
  });
  
  const labels = Object.keys(categoryStats);
  const data = Object.values(categoryStats);
  
  return { labels, data };
};

// 初始化销售占比图表
const initSalesChart = async () => {
  const ctx = document.getElementById('salesChart').getContext('2d');
  const salesData = await fetchSalesData();
  
  new ChartJS(ctx, {
    type: 'doughnut',
    data: {
      labels: salesData.labels || ['食品', '玩具', '用品', '周边'],
      datasets: [{
        data: salesData.data || [35, 25, 20, 20],
        backgroundColor: [
          '#409eff',
          '#67c23a',
          '#e6a23c',
          '#f56c6c'
        ],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right'
        }
      },
      cutout: '70%'
    }
  });
};

// 方法
const handleMenuSelect = (key) => {
  activeMenu.value = key;
  if (key === 'dashboard') {
    router.push('/admin/dashboard');
  } else {
    router.push(`/admin/${key}`);
  }
};

const handleTimeRangeChange = async () => {
  ElMessage.info(`已切换到${chartTimeRange.value}数据`);
  await loadSalesTrendData(chartTimeRange.value);
};

const handleProfile = () => {
  router.push('/admin/profile');
};

const handleSettings = () => {
  router.push('/admin/settings');
};

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '确认退出',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    router.push('/');
    ElMessage.success('已退出登录');
  });
};



const viewAllActivities = () => {
  router.push('/admin/activities/recent');
};

// const handleActivityOp = (action, data) => {
//   switch (action) {
//     case 'confirm':
//       ElMessage.success('已确认预约');
//       break;
//     case 'reject':
//       ElMessage.success('已拒绝预约');
//       break;
//     case 'restock':
//       router.push('/admin/products/edit/' + data.productId);
//       break;
//   }
// };

const formatTime = (timeStr) => {
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.main-header {
  height: 60px;
  background-color: #fff7ee;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}

.user-info:hover {
  color: #333 !important;
  border: none !important;
  background-color: transparent !important;
  outline: none !important;
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  background-color: #fff7ee;
  color: #333;
  box-shadow: 1px 0 2px rgba(0, 0, 0, 0.1);
}

.sidebar-menu {
  width: 100%;
  height: 100%;
  background-color: #fff7ee;
  border-right: none;
}

.sidebar-menu .el-sub-menu__title,
.sidebar-menu .el-menu-item {
  color: #666;
  height: 50px;
  line-height: 50px;
}

.sidebar-menu .el-sub-menu__title:hover,
.sidebar-menu .el-menu-item:hover {
  background-color: #fff7ee;
  color: #333;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #fff7ee;
  color: #333;
}

.content-area {
  padding: 20px;
  overflow-y: auto;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 15px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  margin-top: 5px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 15px;
}

.icon-users {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.icon-reservations {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.icon-sales {
  background-color: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.icon-tasks {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-container {
  height: 320px;
  position: relative;
}

.recent-activities-row {
  margin-bottom: 20px;
}

.activities-card {
  height: 400px;
}

.activity-content {
  color: #606266;
  margin: 8px 0;
  line-height: 1.5;
}

.activity-ops {
  margin-top: 10px;
}

.activity-ops .el-button {
  margin-right: 10px;
}
</style>