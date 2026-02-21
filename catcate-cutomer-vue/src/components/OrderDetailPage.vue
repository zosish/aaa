<!-- 订单详情页面 -->
<template>
  <div class="order-detail-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="$router.push('/HomePage')">首页</el-breadcrumb-item>
        <el-breadcrumb-item @click="$router.push('/my-orders')">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>订单详情</h1>
    </div>

    <div class="order-container" v-if="orderInfo" v-loading="loading">
      <!-- 订单基本信息 -->
      <el-card class="order-basic-info">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
            <el-tag :type="getStatusTagType(orderInfo.orderStatus)">
              {{ getOrderStatusText(orderInfo.orderStatus) }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderInfo.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(orderInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusTagType(orderInfo.paymentStatus)">
              {{ getPaymentStatusText(orderInfo.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPaymentMethodText(orderInfo.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="订单总额">
            <span class="order-total">¥{{ orderInfo.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ orderInfo.shippingAddress }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 商品信息 -->
      <el-card class="order-items">
        <template #header>
          <div class="card-header">
            <span>商品信息</span>
            <!-- 评价按钮 -->
            <div v-if="canReview && !isReviewed">
              <el-button type="primary" @click="showReviewDialog = true">
                <el-icon>
                  <Star />
                </el-icon> 立即评价
              </el-button>
            </div>
            <div v-else-if="isReviewed">
              <el-tag type="success">已评价</el-tag>
            </div>
          </div>
        </template>

        <div class="items-list">
          <div v-for="item in orderItems" :key="item.id" class="order-item">
            <img :src="item.productImage || defaultImage" :alt="item.productName" class="item-image"
              @error="handleImageError">
            <div class="item-info">
              <h4 class="item-name">{{ item.productName }}</h4>
              <p class="item-price">¥{{ item.productPrice }} × {{ item.quantity }}</p>
              <p class="item-subtotal">小计：¥{{ item.subtotal }}</p>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 已有评价展示 -->
      <el-card class="existing-review" v-if="existingReview">
        <template #header>
          <span>我的评价</span>
        </template>
        <div class="review-display">
          <div class="review-header">
            <el-rate v-if="existingReview.rating > 0" v-model="existingReview.rating" :max="5" disabled show-text
              show-score />
            <span class="review-time">{{ formatDate(existingReview.createTime) }}</span>
          </div>
          <div class="review-content">
            <p>{{ existingReview.content }}</p>
          </div>
          <div v-if="existingReview.images" class="review-images">
            <el-image v-for="(image, index) in existingReview.images.split(',')" :key="index" :src="image"
              :preview-src-list="existingReview.images.split(',')" class="review-image" fit="cover" />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="商品评价" width="500px" :before-close="handleClose">
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" :max="5" allow-half show-text show-score />
        </el-form-item>

        <el-form-item label="评价内容" prop="content">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请分享您的使用体验..." maxlength="500"
            show-word-limit />
        </el-form-item>

        <el-form-item label="上传图片">
          <el-upload v-model:file-list="fileList" action="#" list-type="picture-card" :auto-upload="false" :limit="3"
            :on-exceed="handleExceed" :on-remove="handleRemove">
            <el-icon>
              <Plus />
            </el-icon>
          </el-upload>
          <div class="upload-tip">最多可上传3张图片</div>
        </el-form-item>

        <el-form-item label="匿名评价">
          <el-switch v-model="reviewForm.isAnonymous" active-text="匿名" inactive-text="实名" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="submitting">
            提交评价
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<!--  eslint-disable no-undef  -->
<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Star, Plus } from '@element-plus/icons-vue';
import { api } from '@/utils/api';
import { getUserId, isLoggedIn } from '@/utils/auth';

// 路由和状态
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

// 订单数据
const orderInfo = ref(null);
const orderItems = ref([]);

// 图片上传相关
const fileList = ref([]);

// 默认图片
const defaultImage = 'https://picsum.photos/seed/product/100/100';

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

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = defaultImage;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('zh-CN');
};

// 获取订单状态标签类型
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

// 获取支付状态标签类型
const getPaymentStatusTagType = (status) => {
  const statusMap = {
    'PENDING': '',
    'PAID': 'success',
    'FAILED': 'danger',
    'REFUNDED': 'warning'
  };
  return statusMap[status] || '';
};

// 获取支付状态文本
const getPaymentStatusText = (status) => {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'FAILED': '支付失败',
    'REFUNDED': '已退款'
  };
  return statusMap[status] || status;
};

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const methodMap = {
    'ALIPAY': '支付宝',
    'WECHAT': '微信支付'
  };
  return methodMap[method] || method;
};

// 检查是否可以评价
const checkCanReview = () => {
  if (!orderInfo.value) return false;
  // 只能对已完成且已支付的订单进行评价
  return orderInfo.value.orderStatus === 'COMPLETED' &&
    orderInfo.value.paymentStatus === 'PAID';
};

// 检查订单是否已评价
const checkOrderReviewed = async () => {
  try {
    const response = await api.get(`/reviews/check/${orderInfo.value.id}`);
    if (response && response.code === 200) {
      isReviewed.value = response.data.isReviewed;
    }
  } catch (error) {
    console.error('检查评价状态失败:', error);
  }
};

// 获取已有评价
const getExistingReview = async () => {
  try {
    const response = await api.get(`/reviews/order/${orderInfo.value.id}`);
    if (response && response.code === 200) {
      existingReview.value = response.data;
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

// 文件超出限制处理
const handleExceed = () => {
  ElMessage.warning('最多只能上传3张图片');
};

// 移除文件处理
const handleRemove = (file, fileList) => {
  console.log('移除文件:', file);
};

// 关闭弹窗处理
const handleClose = () => {
  showReviewDialog.value = false;
  resetForm();
};

// 重置表单
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

    // 处理图片上传
    let imageUrls = '';
    if (fileList.value.length > 0) {
      // 这里应该调用实际的图片上传接口
      // 暂时使用模拟数据
      imageUrls = fileList.value.map(file => file.url || '').join(',');
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
.order-detail-page {
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

.order-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-total {
  font-size: 18px;
  font-weight: bold;
  color: #e65100;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  display: flex;
  gap: 20px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
}

.item-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: 500;
  color: #5d4037;
  margin: 0 0 10px;
}

.item-price {
  font-size: 14px;
  color: #795548;
  margin: 0 0 5px;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #e65100;
  margin: 0;
}

.existing-review {
  margin-top: 20px;
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
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}

.upload-tip {
  font-size: 12px;
  color: #795548;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>