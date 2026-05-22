<template>
  <Layout>
    <div class="my-orders-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="$router.push('/home')">首页</el-breadcrumb-item>
          <el-breadcrumb-item>我的订单</el-breadcrumb-item>
        </el-breadcrumb>
        <h1>我的订单</h1>
        <p>查看和管理您的订单记录</p>
      </div>

      <!-- 状态统计卡片 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card" :class="{ active: statusFilter === '' }" @click="filterOrders('')">
              <div class="stat-icon"><ShoppingCart /></div>
              <div class="stat-info">
                <div class="stat-value">{{ totalCount }}</div>
                <div class="stat-label">全部订单</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card pending" :class="{ active: statusFilter === 'PENDING' }" @click="filterOrders('PENDING')">
              <div class="stat-icon"><Clock /></div>
              <div class="stat-info">
                <div class="stat-value">{{ pendingCount }}</div>
                <div class="stat-label">待支付</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card processing" :class="{ active: statusFilter === 'PROCESSING' }" @click="filterOrders('PROCESSING')">
              <div class="stat-icon"><Loader /></div>
              <div class="stat-info">
                <div class="stat-value">{{ processingCount }}</div>
                <div class="stat-label">处理中</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card completed" :class="{ active: statusFilter === 'COMPLETED' }" @click="filterOrders('COMPLETED')">
              <div class="stat-icon"><CheckCircle /></div>
              <div class="stat-info">
                <div class="stat-value">{{ completedCount }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 订单列表 -->
      <div class="orders-container">
        <div v-if="!loading && orders.length === 0" class="empty-state">
          <el-empty description="暂无订单记录">
            <template #image>
              <div class="empty-icon">
                <ShoppingCart />
              </div>
            </template>
            <template #bottom>
              <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
            </template>
          </el-empty>
        </div>

        <div v-else>
          <div v-for="order in paginatedOrders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-info">
                <span class="order-number">订单编号：{{ order.orderNumber }}</span>
                <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
              </div>
              <el-tag :type="getOrderStatusTagType(order.orderStatus)" size="small">
                {{ getOrderStatusText(order.orderStatus) }}
              </el-tag>
            </div>

            <div class="order-items">
              <div v-for="item in order.items" :key="item.id" class="order-item">
                <img :src="item.productImage || defaultProductImage" class="product-image" :alt="item.productName" />
                <div class="product-info">
                  <div class="product-name">{{ item.productName }}</div>
                  <div class="product-price">¥{{ formatPrice(item.productPrice) }}</div>
                </div>
                <div class="product-quantity">x{{ item.quantity }}</div>
              </div>
              <div v-if="order.items && order.items.length > 3" class="more-items">
                还有 {{ order.items.length - 3 }} 件商品
              </div>
            </div>

            <div class="order-footer">
              <div class="order-total">
                共 {{ getTotalQuantity(order.items) }} 件商品，合计：
                <span class="total-amount">¥{{ formatPrice(order.totalAmount) }}</span>
              </div>
              <div class="order-actions">
                <el-button v-if="order.orderStatus === 'PENDING'" size="small" type="danger" @click="cancelOrder(order)">
                  取消订单
                </el-button>
                <el-button v-if="order.orderStatus === 'PENDING'" size="small" type="primary" @click="payOrder(order)">
                  去支付
                </el-button>
                <el-button v-if="order.orderStatus === 'COMPLETED'" size="small" @click="viewOrderDetail(order)">
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="totalOrders > 0">
        <el-pagination 
          v-model:current-page="currentPage" 
          v-model:page-size="pageSize" 
          :page-sizes="[10, 20, 50]"
          :total="totalOrders" 
          layout="total, sizes, prev, pager, next, jumper" 
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" 
        />
      </div>

      <!-- 订单详情对话框 -->
      <el-dialog v-model="showDetailDialog" title="订单详情" width="700px" :before-close="handleDetailClose">
        <div v-if="selectedOrder" class="order-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">
              {{ selectedOrder.orderNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getOrderStatusTagType(selectedOrder.orderStatus)">
                {{ getOrderStatusText(selectedOrder.orderStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="getPaymentStatusTagType(selectedOrder.paymentStatus)">
                {{ getPaymentStatusText(selectedOrder.paymentStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付方式">
              {{ getPaymentMethodText(selectedOrder.paymentMethod) }}
            </el-descriptions-item>
            <el-descriptions-item label="总金额">
              ¥{{ formatPrice(selectedOrder.totalAmount) }}
            </el-descriptions-item>
            <el-descriptions-item label="收货地址">
              {{ selectedOrder.shippingAddress || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(selectedOrder.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="顾客备注" :span="2">
              {{ selectedOrder.customerNotes || '无备注' }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="items-section">
            <h4>商品清单</h4>
            <el-table :data="selectedOrder.items" border style="width: 100%">
              <el-table-column label="商品图片" width="100">
                <template #default="scope">
                  <img :src="scope.row.productImage || defaultProductImage" class="table-product-image" :alt="scope.row.productName" />
                </template>
              </el-table-column>
              <el-table-column prop="productName" label="商品名称" />
              <el-table-column prop="productPrice" label="单价" width="100">
                <template #default="scope">¥{{ formatPrice(scope.row.productPrice) }}</template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column label="小计" width="100">
                <template #default="scope">¥{{ formatPrice(scope.row.productPrice * scope.row.quantity) }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div class="detail-actions" v-if="selectedOrder.orderStatus === 'PENDING'">
            <el-button type="danger" @click="cancelOrder(selectedOrder)">
              取消订单
            </el-button>
            <el-button type="primary" @click="payOrder(selectedOrder)">
              去支付
            </el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Clock, Loader, CheckCircle } from '@element-plus/icons-vue'
import { api } from '@/utils/api'
import { getUserId, isLoginValid } from '@/utils/auth'
import Layout from './AppLayout.vue'

const router = useRouter()

const loading = ref(false)
const showDetailDialog = ref(false)
const selectedOrder = ref(null)

const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const defaultProductImage = 'https://picsum.photos/seed/product/100/100'

const orders = ref([])

const filteredOrders = computed(() => {
  let result = [...orders.value]

  if (statusFilter.value) {
    result = result.filter(order => order.orderStatus === statusFilter.value)
  }

  result.sort((a, b) => {
    return new Date(b.createTime) - new Date(a.createTime)
  })

  return result
})

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

const totalOrders = computed(() => filteredOrders.value.length)

const totalCount = computed(() => orders.value.length)

const pendingCount = computed(() => orders.value.filter(o => o.orderStatus === 'PENDING').length)

const processingCount = computed(() => orders.value.filter(o => o.orderStatus === 'PROCESSING').length)

const completedCount = computed(() => orders.value.filter(o => o.orderStatus === 'COMPLETED').length)

const filterOrders = (status) => {
  statusFilter.value = status
  currentPage.value = 1
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const viewOrderDetail = (order) => {
  selectedOrder.value = order
  showDetailDialog.value = true
}

const handleDetailClose = () => {
  showDetailDialog.value = false
  selectedOrder.value = null
}

const cancelOrder = (order) => {
  ElMessageBox.confirm(
    `确定要取消订单[${order.orderNumber}]吗？`,
    '取消订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      loading.value = true
      const response = await api.post(`/orders/${order.orderNumber}/cancel`)

      if (response.code === 200) {
        order.orderStatus = 'CANCELLED'
        ElMessage.success('订单已取消')
        showDetailDialog.value = false
      } else {
        throw new Error(response.message || '取消订单失败')
      }
    } catch (error) {
      ElMessage.error('取消订单失败：' + error.message)
    } finally {
      loading.value = false
    }
  })
}

const payOrder = () => {
  ElMessage.info('跳转到支付页面...')
}

const getTotalQuantity = (items) => {
  if (!items || !Array.isArray(items)) return 0
  return items.reduce((sum, item) => sum + (item.quantity || 0), 0)
}

const getOrderStatusText = (status) => {
  const statusMap = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

const getOrderStatusTagType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    SHIPPED: 'info',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || 'info'
}

const getPaymentStatusText = (status) => {
  const statusMap = {
    PENDING: '待支付',
    PAID: '已支付',
    FAILED: '支付失败',
    REFUNDED: '已退款'
  }
  return statusMap[status] || status || '待支付'
}

const getPaymentStatusTagType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    PAID: 'success',
    FAILED: 'danger',
    REFUNDED: 'info'
  }
  return typeMap[status] || 'warning'
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    ALIPAY: '支付宝',
    WECHAT: '微信支付'
  }
  return methodMap[method] || method || '未选择'
}

const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return ''
  const date = new Date(dateTimeString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatPrice = (price) => {
  if (!price) return '0.00'
  if (typeof price === 'string') {
    return parseFloat(price).toFixed(2)
  }
  if (typeof price === 'number') {
    return price.toFixed(2)
  }
  return price.toString() || '0.00'
}

const loadOrders = async () => {
  try {
    loading.value = true

    if (!isLoginValid()) {
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return
    }

    const userId = getUserId()
    if (!userId) {
      ElMessage.error('获取用户信息失败，请重新登录')
      router.push('/login')
      return
    }

    console.log('加载订单数据，用户ID:', userId)

    const response = await api.get(`/orders/user/${userId}`, {
      params: {
        page: 1,
        size: 100
      }
    })

    console.log('API响应:', response)

    if (response.code === 200) {
      const data = response.data || {}
      const records = data.records || []

      orders.value = records.map(item => ({
        id: item.id,
        orderNumber: item.orderNumber,
        userId: item.userId,
        totalAmount: item.totalAmount,
        paymentStatus: item.paymentStatus,
        paymentMethod: item.paymentMethod,
        paymentTime: item.paymentTime,
        transactionId: item.transactionId,
        shippingAddress: item.shippingAddress,
        orderStatus: item.orderStatus,
        customerNotes: item.customerNotes,
        adminNotes: item.adminNotes,
        createTime: item.createTime,
        updateTime: item.updateTime,
        items: []
      }))

      for (let order of orders.value) {
        const itemsResponse = await api.get(`/orders/${order.orderNumber}/items`)
        if (itemsResponse.code === 200) {
          order.items = itemsResponse.data.map(item => ({
            id: item.id,
            productId: item.productId,
            productName: item.productName,
            productPrice: item.productPrice,
            quantity: item.quantity,
            subtotal: item.subtotal,
            productImage: `https://picsum.photos/seed/${item.productId || Math.random()}/100/100`
          }))
        }
      }

      console.log('加载订单数据成功，共', orders.value.length, '条')
    } else {
      throw new Error(response.message || '获取订单数据失败')
    }
  } catch (error) {
    console.error('获取订单数据失败:', error)
    const errorMsg = error.message || '获取订单数据失败'
    
    if (errorMsg.includes('用户ID不能为空')) {
      ElMessage.error('用户信息无效，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error('获取订单数据失败：' + errorMsg)
    }
    orders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
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
  margin: 15px 0 8px;
}

.page-header p {
  font-size: 16px;
  color: #795548;
  margin: 0;
}

.stats-section {
  margin-bottom: 30px;
}

.stat-card {
  background: #fff;
  border-radius: 15px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border: 2px solid #ff9800;
}

.stat-card.pending.active {
  border-color: #ff9800;
}

.stat-card.processing.active {
  border-color: #1989fa;
}

.stat-card.completed.active {
  border-color: #67c23a;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #ff9800;
}

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #ff9800;
}

.stat-card.processing .stat-icon {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1989fa;
}

.stat-card.completed .stat-icon {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #67c23a;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.orders-container {
  margin-bottom: 30px;
}

.order-card {
  background: #fff;
  border-radius: 15px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 15px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-number {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.order-items {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: calc(33.33% - 10px);
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 12px;
  color: #ff9800;
  font-weight: 500;
}

.product-quantity {
  font-size: 12px;
  color: #999;
}

.more-items {
  display: flex;
  align-items: center;
  justify-content: center;
  width: calc(33.33% - 10px);
  height: 60px;
  border: 1px dashed #e0e0e0;
  border-radius: 8px;
  font-size: 12px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  margin-top: 15px;
}

.order-total {
  font-size: 14px;
  color: #333;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #ff5722;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.empty-icon {
  font-size: 60px;
  color: #ccc;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.order-detail {
  padding: 20px 0;
}

.items-section {
  margin-top: 20px;
}

.items-section h4 {
  margin-bottom: 15px;
  color: #333;
}

.table-product-image {
  width: 50px;
  height: 50px;
  border-radius: 5px;
  object-fit: cover;
}

.detail-actions {
  margin-top: 20px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .my-orders-page {
    padding: 20px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .stat-card {
    padding: 15px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .stat-value {
    font-size: 20px;
  }

  .order-item {
    width: calc(50% - 7px);
  }

  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .order-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .order-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>