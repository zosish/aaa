<!-- 订单管理 -->
 <template>
  <div class="order-management">
    <div class="page-header">
      <h1>订单管理</h1>
      <div class="header-actions">
        <el-button type="primary" icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.pendingCount || 0 }}</div>
            <div class="stat-label">待处理订单</div>
          </div>
          <el-icon class="stat-icon warning"><Warning /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.unpaidCount || 0 }}</div>
            <div class="stat-label">待支付订单</div>
          </div>
          <el-icon class="stat-icon danger"><Coin /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.todayCount || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
          <el-icon class="stat-icon info"><Calendar /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">¥{{ formatCurrency(statistics.monthSales || 0) }}</div>
            <div class="stat-label">本月销售额</div>
          </div>
          <el-icon class="stat-icon success"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选区 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="订单号">
              <el-input v-model="searchForm.orderNumber" placeholder="请输入订单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单状态">
              <el-select v-model="searchForm.orderStatus" placeholder="请选择订单状态" clearable>
                <el-option label="待处理" value="PENDING" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="已发货" value="SHIPPED" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="支付状态">
              <el-select v-model="searchForm.paymentStatus" placeholder="请选择支付状态" clearable>
                <el-option label="待支付" value="PENDING" />
                <el-option label="已支付" value="PAID" />
                <el-option label="支付失败" value="FAILED" />
                <el-option label="已退款" value="REFUNDED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="下单时间">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" icon="Search" @click="searchOrders">搜索</el-button>
            <el-button icon="Refresh" @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="orders-card">
      <el-table
        :data="ordersList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="orderNumber" label="订单号" min-width="150" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">
            ¥{{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="getOrderStatusType(scope.row.orderStatus)">
              {{ getOrderStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="120">
          <template #default="scope">
            <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
              {{ getPaymentStatusText(scope.row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewOrder(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.orderStatus === 'PENDING'" 
              size="small" 
              type="success" 
              @click="updateStatus(scope.row, 'PROCESSING')"
            >
              处理
            </el-button>
            <el-button 
              v-if="scope.row.orderStatus === 'PROCESSING'" 
              size="small" 
              type="warning" 
              @click="updateStatus(scope.row, 'SHIPPED')"
            >
              发货
            </el-button>
            <el-button 
              v-if="scope.row.orderStatus !== 'CANCELLED' && scope.row.orderStatus !== 'COMPLETED'" 
              size="small" 
              type="danger" 
              @click="cancelOrder(scope.row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="'订单详情 - ' + currentOrder.orderNumber"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentOrder.id" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentOrder.userId }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ formatCurrency(currentOrder.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(currentOrder.orderStatus)">
              {{ getOrderStatusText(currentOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(currentOrder.paymentStatus)">
              {{ getPaymentStatusText(currentOrder.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDate(currentOrder.paymentTime) }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.shippingAddress || '无' }}</el-descriptions-item>
          <el-descriptions-item label="顾客备注" :span="2">{{ currentOrder.customerNotes || '无' }}</el-descriptions-item>
          <el-descriptions-item label="管理员备注" :span="2">{{ currentOrder.adminNotes || '无' }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px;">订单商品</h3>
        <el-table :data="orderItems" border style="width: 100%">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="productPrice" label="单价" width="120">
            <template #default="scope">¥{{ formatCurrency(scope.row.productPrice) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="subtotal" label="小计" width="120">
            <template #default="scope">¥{{ formatCurrency(scope.row.subtotal) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Warning,
  Coin,
  Calendar,
  TrendCharts
} from '@element-plus/icons-vue'

// 数据状态
const loading = ref(false)
const ordersList = ref([])
const dialogVisible = ref(false)
const orderItems = ref([])
const dateRange = ref([])

// 统计数据
const statistics = reactive({
  pendingCount: 0,
  unpaidCount: 0,
  todayCount: 0,
  monthSales: 0
})

// 搜索表单
const searchForm = reactive({
  orderNumber: '',
  orderStatus: '',
  paymentStatus: ''
})

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 当前订单
const currentOrder = reactive({})

// 生命周期钩子
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  await Promise.all([
    fetchOrders(),
    fetchStatistics()
  ])
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const response = await fetch('http://localhost:8081/catcate/orders/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(params)
    })
    
    const result = await response.json()
    ordersList.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    const response = await fetch('http://localhost:8081/catcate/orders/statistics')
    const data = await response.json()
    Object.assign(statistics, data)
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 搜索订单
const searchOrders = () => {
  pagination.currentPage = 1
  fetchOrders()
}

// 重置搜索
const resetSearch = () => {
  searchForm.orderNumber = ''
  searchForm.orderStatus = ''
  searchForm.paymentStatus = ''
  dateRange.value = []
  pagination.currentPage = 1
  fetchOrders()
}

// 刷新数据
const refreshData = () => {
  loadData()
}

// 查看订单详情
const viewOrder = async (order) => {
  try {
    const response = await fetch(`http://localhost:8081/catcate/orders/${order.id}`)
    const data = await response.json()
    Object.assign(currentOrder, data.order)
    orderItems.value = data.items || []
    dialogVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 更新订单状态
const updateStatus = async (order, newStatus) => {
  try {
    await ElMessageBox.confirm(
      `确定要将订单状态更新为${getOrderStatusText(newStatus)}吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await fetch('http://localhost:8081/catcate/orders/updateStatus', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id: order.id,
        orderStatus: newStatus,
        adminNotes: `管理员于${new Date().toLocaleString()}更新状态`
      })
    })
    
    if (response.ok) {
      ElMessage.success('订单状态更新成功')
      loadData()
    } else {
      ElMessage.error('订单状态更新失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新订单状态失败:', error)
      ElMessage.error('更新订单状态失败')
    }
  }
}

// 取消订单
const cancelOrder = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入取消原因',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '取消原因不能为空'
      }
    )
    
    const response = await fetch('http://localhost:8081/catcate/orders/updateStatus', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id: order.id,
        orderStatus: 'CANCELLED',
        adminNotes: `取消原因: ${value}`
      })
    })
    
    if (response.ok) {
      ElMessage.success('订单取消成功')
      loadData()
    } else {
      ElMessage.error('订单取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchOrders()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchOrders()
}

// 工具函数
const formatCurrency = (amount) => {
  return parseFloat(amount || 0).toFixed(2)
}

const formatDate = (date) => {
  if (!date) return '无'
  return new Date(date).toLocaleString('zh-CN')
}

const getOrderStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'SHIPPED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || ''
}

const getOrderStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

const getPaymentStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PAID': 'success',
    'FAILED': 'danger',
    'REFUNDED': 'info'
  }
  return typeMap[status] || ''
}

const getPaymentStatusText = (status) => {
  const textMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'FAILED': '支付失败',
    'REFUNDED': '已退款'
  }
  return textMap[status] || status
}
</script>

<style scoped>
.order-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #1f2329;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #1f2329;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-icon {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 32px;
  opacity: 0.1;
}

.stat-icon.warning {
  color: #e6a23c;
}

.stat-icon.danger {
  color: #f56c6c;
}

.stat-icon.info {
  color: #409eff;
}

.stat-icon.success {
  color: #67c23a;
}

.filter-card {
  margin-bottom: 20px;
}

.orders-card {
  overflow: hidden;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.order-detail {
  max-height: 60vh;
  overflow-y: auto;
}
</style>