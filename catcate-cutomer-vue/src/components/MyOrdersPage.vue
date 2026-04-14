<!-- 我的订单 -->
<template>
  <div class="my-orders-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="$router.push('/HomePage')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我的订单</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>我的订单</h1>
    </div>

    <!-- 订单列表 -->
    <section class="orders-section">
      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNumber" label="订单号" width="180" class-name="order-number-column"></el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" class-name="order-time-column">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="120" class-name="order-amount-column">
          <template #default="scope">
            ¥{{ scope.row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="120" class-name="order-status-column">
          <template #default="scope">
            <el-tag :type="getOrderStatusTagType(scope.row.orderStatus)">
              {{ getOrderStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" class-name="order-action-column">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewOrderDetail(scope.row)">查看详情</el-button>
            <el-button v-if="canCancel(scope.row)" type="danger" size="small" @click="cancelOrder(scope.row)">取消订单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          @current-change="handlePageChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="total">
        </el-pagination>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && orders.length === 0" class="empty-orders">
        <el-empty description="暂无订单数据">
          <el-button type="primary" @click="$router.push('/ProductPage')">去逛逛</el-button>
        </el-empty>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '../utils/api';
import { getUserId } from '../utils/auth';

const router = useRouter();
const orders = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 获取订单列表
const loadOrders = async () => {
  loading.value = true;
  try {
    console.log('开始请求订单数据，用户ID:', getUserId());
    const response = await api.get(`/orders/user/${getUserId()}`, {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    });
    
    console.log('订单接口原始响应:', response);
    
    // 检查响应格式 - 支持两种格式
    let dataToProcess;
    if (response && response.code === 200 && response.data) {
      // 标准格式 {code: 200, data: {...}, message: "..."}
      dataToProcess = response.data;
    } else if (response && typeof response === 'object' && response.records) {
      // 直接返回数据格式 {total: 0, current: 1, size: 10, records: [...]}
      dataToProcess = response;
    } else {
      throw new Error('响应格式不正确');
    }
    
    if (dataToProcess) {
      // 确保数据格式正确
      orders.value = Array.isArray(dataToProcess.records) ? dataToProcess.records : [];
      total.value = typeof dataToProcess.total === 'number' ? dataToProcess.total : orders.value.length;
      
      console.log('数据处理完成:');
      console.log('订单数量:', orders.value.length);
      console.log('总数量:', total.value);
      console.log('当前页:', dataToProcess.current);
      console.log('每页大小:', dataToProcess.size);
    } else {
      throw new Error('数据处理失败');
    }
  } catch (error) {
    console.error('获取订单列表失败:', error);
    ElMessage.error('获取订单列表失败，请稍后重试: ' + error.message);
    orders.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 查看订单详情
const viewOrderDetail = (order) => {
  router.push(`/OrderDetailPage?orderNumber=${order.orderNumber}`);
};

// 取消订单
const cancelOrder = (order) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await api.post(`/orders/${order.orderNumber}/cancel`);
      if (response && response.code === 200) {
        ElMessage.success('订单已取消');
        await loadOrders(); // 重新加载订单列表
      } else {
        throw new Error(response?.message || '取消订单失败');
      }
    } catch (error) {
      console.error('取消订单失败:', error);
      ElMessage.error('取消订单失败，请稍后重试');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 判断是否可以取消订单
const canCancel = (order) => {
  return order.orderStatus === 'PENDING';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('zh-CN');
};

// 获取订单状态标签类型
const getOrderStatusTagType = (status) => {
  const statusMap = {
    'PENDING': '',
    'PROCESSING': 'primary',
    'SHIPPED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  };
  return statusMap[status] || '';
};

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  };
  return statusMap[status] || status;
};

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  loadOrders();
};

onMounted(() => {
  const userId = getUserId();
  console.log('=== 我的订单页面初始化 ===');
  console.log('用户ID:', userId);
  console.log('当前时间:', new Date().toISOString());
  
  if (userId) {
    loadOrders();
  } else {
    ElMessage.warning('请先登录');
    router.push('/login');
  }
});
</script>

<style scoped>
.my-orders-page {
  min-height: calc(100vh - 70px);
  background-color: #fff9f5;
  padding: 30px 40px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #5d4037;
  margin: 0 0 8px;
}

.orders-section {
  background-color: #fff;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

/* 修复表格样式 */
.el-table th {
  text-align: left !important; /* 确保表头左对齐 */
  padding: 12px 0 !important; /* 统一padding */
}

.el-table td {
  text-align: left !important; /* 确保内容左对齐 */
  padding: 12px 0 !important; /* 统一padding */
}

/* 修复特定列的样式 */
.el-table .order-number-column,
.el-table .order-time-column,
.el-table .order-amount-column,
.el-table .order-status-column,
.el-table .order-action-column {
  text-align: left !important;
}

/* 修复按钮样式 */
.el-button {
  margin-right: 8px;
  margin-bottom: 8px;
}

/* 修复分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.empty-orders {
  padding: 40px 0;
  text-align: center;
}
</style>