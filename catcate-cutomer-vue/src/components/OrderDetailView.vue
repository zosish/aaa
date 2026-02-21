<!-- 订单详情查看页面 -->
<template>
  <div class="order-detail-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="$router.push('/HomePage')">首页</el-breadcrumb-item>
        <el-breadcrumb-item @click="$router.push('/my-orders')">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>订单详情</h1>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div class="detail-skeleton">
              <el-skeleton-item variant="h3" style="width: 60%" />
              <div style="margin: 20px 0">
                <el-skeleton-item variant="text" style="width: 100%" />
                <el-skeleton-item variant="text" style="width: 80%" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- 订单详情内容 -->
      <div v-else>
        <template v-if="orderInfo">
          <!-- 订单基本信息 -->
          <el-card class="order-basic-info" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">订单信息</span>
                <el-tag :type="getStatusTagType(orderInfo.orderStatus)" size="large">
                  {{ getOrderStatusText(orderInfo.orderStatus) }}
                </el-tag>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">订单号：</span>
                  <span class="value">{{ orderInfo.orderNumber }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">下单时间：</span>
                  <span class="value">{{ formatDate(orderInfo.createTime) }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">支付状态：</span>
                  <el-tag :type="getPaymentStatusTagType(orderInfo.paymentStatus)">
                    {{ getPaymentStatusText(orderInfo.paymentStatus) }}
                  </el-tag>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">支付方式：</span>
                  <span class="value">{{ getPaymentMethodText(orderInfo.paymentMethod) }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">订单总额：</span>
                  <span class="value price">¥{{ orderInfo.totalAmount }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">收货地址：</span>
                  <span class="value">{{ orderInfo.shippingAddress }}</span>
                </div>
              </el-col>
            </el-row>
          </el-card>

          <!-- 商品信息 -->
          <el-card class="order-items" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">商品信息</span>
                <!-- 评价区域 -->
                <div class="review-section">
                  <div v-if="canReview && !isReviewed" class="review-available">
                    <el-button type="primary" @click="showReviewDialog = true" size="small">
                      <el-icon><Star /></el-icon> 立即评价
                    </el-button>
                    <div class="review-hint">订单已支付，您可以对商品进行评价</div>
                  </div>
                  <div v-else-if="isReviewed" class="review-completed">
                    <el-tag type="success" size="small">已评价</el-tag>
                    <div class="review-hint">感谢您的评价！</div>
                  </div>
                  <div v-else class="review-unavailable">
                    <el-tag type="info" size="small">暂不可评价</el-tag>
                    <div class="review-hint">订单未支付或状态不符合评价条件</div>
                  </div>
                </div>
              </div>
            </template>
            
            <div class="items-list">
              <div v-for="item in orderItems" :key="item.id" class="order-item">
                <el-image 
                  :src="item.productImage || defaultImage" 
                  :alt="item.productName"
                  class="item-image"
                  @error="handleImageError"
                  fit="cover"
                />
                <div class="item-info">
                  <h4 class="item-name">{{ item.productName }}</h4>
                  <p class="item-spec">规格：{{ item.specifications || '标准版' }}</p>
                  <div class="item-price-info">
                    <span class="unit-price">¥{{ item.productPrice }}</span>
                    <span class="quantity">× {{ item.quantity }}</span>
                    <span class="subtotal">小计：¥{{ item.subtotal }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 已有评价展示 -->
          <el-card v-if="existingReview" class="existing-review" shadow="never">
            <template #header>
              <span class="card-title">我的评价</span>
            </template>
            <div class="review-display">
              <div class="review-header">
                <el-rate
                  v-model="existingReview.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
                <span class="review-time">{{ formatDate(existingReview.createTime) }}</span>
              </div>
              <div class="review-content">
                <p>{{ existingReview.content }}</p>
              </div>
              <div v-if="existingReview.images" class="review-images">
                <el-image
                  v-for="(image, index) in existingReview.images.split(',')"
                  :key="index"
                  :src="image"
                  :preview-src-list="existingReview.images.split(',')"
                  class="review-image"
                  fit="cover"
                  lazy
                />
              </div>
            </div>
          </el-card>

          <!-- 操作按钮区域 -->
          <div class="action-buttons">
            <el-button @click="$router.push('/my-orders')">返回订单列表</el-button>
            <el-button type="primary" @click="printOrder">打印订单</el-button>
            <el-button v-if="canCancelOrder" type="danger" @click="cancelOrder">取消订单</el-button>
          </div>
        </template>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-empty description="订单信息加载失败">
            <el-button type="primary" @click="loadOrderDetail">重新加载</el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 评价弹窗 -->
    <el-dialog
      v-model="showReviewDialog"
      title="商品评价"
      width="600px"
      :before-close="handleClose"
      class="review-dialog"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="80px"
      >
        <el-form-item label="商品评分" prop="rating">
          <el-rate
            v-model="reviewForm.rating"
            :max="5"
            allow-half
            show-text
            show-score
            score-template="{value}星"
          />
          <div class="rate-hint">请点击星星为商品打分</div>
        </el-form-item>
        
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="6"
            placeholder="请分享您的使用体验，帮助其他买家做出更好的选择..."
            maxlength="500"
            show-word-limit
          />
          <div class="content-hint">请至少输入10个字符</div>
        </el-form-item>
        
        <el-form-item label="上传图片">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="3"
            :on-exceed="handleExceed"
            :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多可上传3张图片，支持JPG、PNG格式</div>
        </el-form-item>
        
        <el-form-item label="匿名评价">
          <el-switch
            v-model="reviewForm.isAnonymous"
            active-text="匿名"
            inactive-text="实名"
          />
          <div class="anonymous-hint">选择匿名后，您的用户名将不会显示在评价中</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="submitting">
            {{ submitting ? '提交中...' : '提交评价' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Star, Plus } from '@element-plus/icons-vue';
import { api } from '@/utils/api';
import { getUserId, isLoggedIn } from '@/utils/auth';

// 路由和状态管理
const route = useRoute();
const router = useRouter();
const loading = ref(true);
const submitting = ref(false);
const showReviewDialog = ref(false);
const isReviewed = ref(false);
const existingReview = ref(null);
const canReview = ref(false);

// 表单引用
const reviewFormRef = ref(null);

// 数据存储
const orderInfo = ref(null);
const orderItems = ref([]);
const fileList = ref([]);

// 默认配置
const defaultImage = 'https://picsum.photos/seed/product/300/300';

// 评价表单数据
const reviewForm = reactive({
  orderId: null,
  userId: getUserId(),
  targetType: 'PRODUCT',
  targetId: null,
  rating: 5,
  content: '',
  isAnonymous: false,
  images: ''
});

// 表单验证规则
const reviewRules = {
  rating: [
    { required: true, message: '请给商品评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容至少10个字', trigger: 'blur' }
  ]
};

// 图片处理函数
const handleImageError = (event) => {
  event.target.src = defaultImage;
};

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('zh-CN');
};

// 订单状态相关函数
const getStatusTagType = (status) => {
  const statusMap = {
    'PENDING': '',
    'PROCESSING': 'primary',
    'SHIPPED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  };
  return statusMap[status] || '';
};

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

const getPaymentStatusTagType = (status) => {
  const statusMap = {
    'PENDING': '',
    'PAID': 'success',
    'FAILED': 'danger',
    'REFUNDED': 'warning'
  };
  return statusMap[status] || '';
};

const getPaymentStatusText = (status) => {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'FAILED': '支付失败',
    'REFUNDED': '已退款'
  };
  return statusMap[status] || status;
};

const getPaymentMethodText = (method) => {
  const methodMap = {
    'ALIPAY': '支付宝',
    'WECHAT': '微信支付'
  };
  return methodMap[method] || method;
};

// 评价权限检查
const checkCanReview = () => {
  if (!orderInfo.value) return false;
  // 只要订单已支付就可以评价
  return orderInfo.value.paymentStatus === 'PAID';
};

// 判断是否可以取消订单
const canCancelOrder = computed(() => {
  if (!orderInfo.value) return false;
  return ['PENDING', 'PROCESSING'].includes(orderInfo.value.orderStatus);
});

// 检查订单是否已评价
const checkOrderReviewed = async () => {
  try {
    const response = await api.get(`/reviews/check/${getUserId()}/${orderInfo.value.id}`);
    if (response && response.code === 200) {
      isReviewed.value = response.data;
    }
  } catch (error) {
    console.error('检查评价状态失败:', error);
  }
};

// 获取已有评价
const getExistingReview = async () => {
  try {
    const response = await api.get(`/reviews/order/${orderInfo.value.id}`);
    if (response && response.code === 200 && response.data.length > 0) {
      existingReview.value = response.data[0]; // 取第一条评价
    }
  } catch (error) {
    console.error('获取评价失败:', error);
  }
};

// 加载订单详情
const loadOrderDetail = async () => {
  loading.value = true;
  try {
    const orderNumber = route.query.orderNumber;
    if (!orderNumber) {
      throw new Error('订单号不能为空');
    }

    // 获取订单基本信息
    const orderResponse = await api.get(`/orders/${orderNumber}`);
    if (orderResponse && orderResponse.code === 200) {
      orderInfo.value = orderResponse.data;
      
      // 获取订单商品详情
      const itemsResponse = await api.get(`/orders/${orderNumber}/items`);
      if (itemsResponse && itemsResponse.code === 200) {
        orderItems.value = itemsResponse.data;
        
        // 设置评价的目标ID为第一个商品ID
        if (orderItems.value.length > 0) {
          reviewForm.targetId = orderItems.value[0].productId;
        }
      }
      
      // 检查评价权限
      canReview.value = checkCanReview();
      
      // 检查是否已评价
      if (canReview.value) {
        await checkOrderReviewed();
        if (isReviewed.value) {
          await getExistingReview();
        }
      }
      
      reviewForm.orderId = orderInfo.value.id;
    } else {
      throw new Error(orderResponse?.message || '获取订单详情失败');
    }
  } catch (error) {
    console.error('加载订单详情失败:', error);
    ElMessage.error('加载订单详情失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 文件上传相关处理
const handleExceed = () => {
  ElMessage.warning('最多只能上传3张图片');
};

const handleRemove = (file, fileList) => {
  console.log('移除文件:', file);
};

// 弹窗关闭处理
const handleClose = () => {
  showReviewDialog.value = false;
  resetForm();
};

// 表单重置
const resetForm = () => {
  if (reviewFormRef.value) {
    reviewFormRef.value.resetFields();
  }
  fileList.value = [];
  reviewForm.rating = 5;
  reviewForm.content = '';
  reviewForm.isAnonymous = false;
  reviewForm.images = '';
};

// 提交评价
const submitReview = async () => {
  if (!reviewFormRef.value) return;
  
  try {
    await reviewFormRef.value.validate();
    
    submitting.value = true;
    
    // 处理图片上传（这里简化处理，实际项目中需要调用图片上传接口）
    let imageUrls = '';
    if (fileList.value.length > 0) {
      imageUrls = fileList.value.map(file => file.url || 'https://picsum.photos/300/300').join(',');
    }
    
    const reviewData = {
      ...reviewForm,
      images: imageUrls,
      isAnonymous: reviewForm.isAnonymous ? 1 : 0
    };
    
    const response = await api.post('/reviews/add', reviewData);
    
    if (response && response.code === 200) {
      ElMessage.success('评价提交成功！');
      showReviewDialog.value = false;
      isReviewed.value = true;
      await loadOrderDetail(); // 重新加载订单详情
    } else {
      ElMessage.error(response?.message || '评价提交失败');
    }
  } catch (error) {
    console.error('提交评价失败:', error);
    ElMessage.error('评价提交失败：' + error.message);
  } finally {
    submitting.value = false;
  }
};

// 其他操作函数
const printOrder = () => {
  window.print();
};

const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await api.post(`/orders/cancel/${orderInfo.value.orderNumber}`);
    if (response && response.code === 200) {
      ElMessage.success('订单取消成功');
      await loadOrderDetail(); // 重新加载订单信息
    } else {
      ElMessage.error(response?.message || '取消订单失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error);
      ElMessage.error('取消订单失败：' + error.message);
    }
  }
};

// 页面初始化
onMounted(() => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loadOrderDetail();
});
</script>

<style scoped>
.order-detail-view {
  min-height: calc(100vh - 70px);
  background-color: #f5f5f5;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin: 10px 0 0;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
}

.detail-skeleton {
  background: white;
  padding: 30px;
  border-radius: 8px;
}

.order-basic-info,
.order-items,
.existing-review {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.label {
  font-weight: 500;
  color: #666;
  width: 80px;
  flex-shrink: 0;
}

.value {
  color: #333;
}

.value.price {
  color: #e74c3c;
  font-size: 18px;
  font-weight: 600;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #eee;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px;
}

.item-spec {
  font-size: 13px;
  color: #666;
  margin: 0 0 10px;
}

.item-price-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.unit-price {
  color: #e74c3c;
  font-weight: 500;
}

.quantity {
  color: #666;
}

.subtotal {
  color: #e74c3c;
  font-weight: 600;
  margin-left: auto;
}

.review-section {
  text-align: right;
}

.review-hint {
  font-size: 12px;
  color: #795548;
  margin-top: 5px;
}

.review-display {
  padding: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.review-time {
  font-size: 12px;
  color: #795548;
}

.review-content p {
  font-size: 14px;
  color: #5d4037;
  line-height: 1.6;
  margin: 0;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin: 30px 0;
}

.empty-state {
  text-align: center;
  padding: 50px 0;
}

/* 评价弹窗样式 */
.review-dialog :deep(.el-dialog__body) {
  padding: 20px 30px;
}

.rate-hint,
.content-hint,
.upload-tip,
.anonymous-hint {
  font-size: 12px;
  color: #795548;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-detail-view {
    padding: 10px;
  }
  
  .order-item {
    flex-direction: column;
  }
  
  .item-image {
    width: 100%;
    height: 200px;
  }
  
  .item-price-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>