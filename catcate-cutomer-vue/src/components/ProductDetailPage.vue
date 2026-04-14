<!-- 商品详情页面 -->
<template>
  <div class="product-detail-page">
    <!-- 页面头部：标题+面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
        <el-breadcrumb-item @click="$router.push('/ProductPage')">周边商品</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentProduct ? currentProduct.name : '商品详情' }}</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>{{ currentProduct ? currentProduct.name : '商品详情' }}</h1>
    </div>
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft" type="primary" plain>
        返回商品列表
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton animated>
        <template #template>
          <div class="detail-container">
            <div class="image-section">
              <el-skeleton-item variant="image" style="width: 100%; height: 400px" />
            </div>
            <div class="info-section">
              <el-skeleton-item variant="h1" style="width: 60%; height: 32px" />
              <div style="margin: 20px 0">
                <el-skeleton-item variant="text" style="width: 100%" />
                <el-skeleton-item variant="text" style="width: 80%" />
              </div>
              <el-skeleton-item variant="text" style="width: 40%; height: 24px" />
            </div>
          </div>
        </template>
      </el-skeleton>
    </div>

    <!-- 商品详情内容 -->
    <div class="product-detail-content" v-else-if="currentProduct">
      <div class="detail-container">
        <!-- 商品图片展示区 -->
        <div class="image-section">
          <div class="main-image">
            <img :src="currentProduct.imageUrl || defaultImage" :alt="currentProduct.name" @error="handleImageError" />
          </div>
          <!-- 库存标签 -->
          <div class="stock-tag"
            :class="{ low: currentProduct.stockQuantity <= 10, out: currentProduct.stockQuantity <= 0 }"
            v-if="currentProduct.stockQuantity <= 10">
            {{ currentProduct.stockQuantity <= 0 ? '已售罄' : '库存紧张' }} </div>
          </div>

          <!-- 商品信息区 -->
          <div class="info-section">
            <h1 class="product-title">{{ currentProduct.name }}</h1>
            <p class="product-description">{{ currentProduct.description }}</p>

            <!-- 价格信息 -->
            <div class="price-section">
              <div class="current-price">
                <span class="price-label">价格：</span>
                <span class="price">¥{{ parseFloat(currentProduct.price).toFixed(2) }}</span>
              </div>
              <div class="sales-info">
                已售 {{ currentProduct.salesCount || 0 }} 件
              </div>
            </div>

            <!-- 库存信息 -->
            <div class="stock-section">
              <span class="stock-label">库存：</span>
              <span class="stock-value" :class="{ out: currentProduct.stockQuantity <= 0 }">
                {{ currentProduct.stockQuantity > 0 ? `${currentProduct.stockQuantity}件` : '已售罄' }}
              </span>
            </div>

            <!-- 分类信息 -->
            <div class="category-section">
              <span class="category-label">分类：</span>
              <el-tag type="primary">
                {{ getCategoryName(currentProduct.category) }}
              </el-tag>
            </div>

            <!-- 购买数量选择 -->
            <div class="quantity-section" v-if="currentProduct.stockQuantity > 0">
              <span class="quantity-label">购买数量：</span>
              <el-input-number v-model="buyQuantity" :min="1" :max="currentProduct.stockQuantity" :step="1"
                size="medium" controls-position="right" @change="handleQuantityChange">
              </el-input-number>
              <span class="quantity-info">件 (库存: {{ currentProduct.stockQuantity }}件)</span>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button type="primary" size="large" class="buy-now-btn"
                :disabled="currentProduct.stockQuantity <= 0 || !isLogin || buyLoading" :loading="buyLoading"
                @click="buyNow">
                {{ buyLoading ? '处理中...' : '立即购买' }}
              </el-button>
              <el-button type="warning" size="large" class="add-cart-btn"
                :disabled="currentProduct.stockQuantity <= 0 || !isLogin" @click="addToCart">
                加入购物车
              </el-button>
            </div>
          </div>
        </div>

        <!-- 商品详情介绍 -->
        <div class="product-intro-section">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="商品详情" name="detail">
              <div class="detail-content">
                <h3>产品特色</h3>
                <ul class="feature-list">
                  <li>精选优质材料制作，品质保证</li>
                  <li>符合猫咪使用习惯，安全可靠</li>
                  <li>精美包装，适合送礼或自用</li>
                  <li>售后无忧，支持7天无理由退换</li>
                </ul>

                <h3>使用说明</h3>
                <p>请按照产品说明正确使用，如有疑问请联系客服。</p>

                <h3>注意事项</h3>
                <ul class="notice-list">
                  <li>请放置在猫咪可接触的安全位置</li>
                  <li>定期清洁保养，延长使用寿命</li>
                  <li>如发现损坏请及时停止使用</li>
                </ul>
              </div>
            </el-tab-pane>

            <el-tab-pane label="用户评价" name="review">
              <div class="review-content">
                <div v-if="productReviews.length > 0">
                  <div class="reviews-list">
                    <div v-for="review in productReviews" :key="review.id" class="review-item">
                      <div class="review-header">
                        <div class="review-user">
                          <el-avatar :size="32" :src="review.userAvatar || defaultAvatar"></el-avatar>
                          <span class="username">{{ review.isAnonymous ? '匿名用户' : review.username || '用户' +
                            review.userId
                          }}</span>
                        </div>
                        <div class="rating-display">
                          <!-- 调试信息 -->
                          <div style="font-size: 12px; color: #999; margin-bottom: 5px;">
                            原始评分: {{ review.rating }} (类型: {{ typeof review.rating }})
                          </div>
                          <el-rate :model-value="Number(review.rating)" disabled :max="5" size="small" show-score
                            text-color="#ff9900" score-template="{value}分"></el-rate>
                        </div>
                      </div>
                      <div class="review-body">
                        <p class="review-text">{{ review.content }}</p>
                        <div v-if="review.images" class="review-images">
                          <el-image v-for="(image, index) in review.images.split(',')" :key="index" :src="image"
                            :preview-src-list="review.images.split(',')" class="review-image-thumb" fit="cover" />
                        </div>
                        <div class="review-footer">
                          <span class="review-date">{{ formatDate(review.createTime) }}</span>
                          <span v-if="review.adminReply" class="admin-reply">
                            <el-tag type="success" size="small">商家回复</el-tag>
                          </span>
                        </div>
                        <div v-if="review.adminReply" class="admin-reply-content">
                          <p>{{ review.adminReply }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- 分页 -->
                  <div class="review-pagination" v-if="totalReviews > pageSize">
                    <el-pagination v-model:current-page="reviewPage" v-model:page-size="pageSize" :total="totalReviews"
                      layout="prev, pager, next" @current-change="handleReviewPageChange" />
                  </div>
                </div>
                <el-empty v-else description="暂无用户评价" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <!-- 商品不存在提示 -->
      <div class="product-not-found" v-else-if="!loading">
        <el-result icon="warning" title="商品不存在" sub-title="抱歉，您访问的商品不存在或已被下架">
          <template #extra>
            <el-button type="primary" @click="$router.push('/ProductPage')">
              返回商品列表
            </el-button>
          </template>
        </el-result>
      </div>
    </div>
    <!-- 地址输入对话框 -->
  <el-dialog v-model="showAddressDialog" title="填写收货地址" width="500px" :before-close="handleAddressClose">
    <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="100px">
      
      
      <el-form-item label="收货人" prop="receiverName">
        <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名"></el-input>
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="addressForm.phone" placeholder="请输入联系电话"></el-input>
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input 
          v-model="addressForm.address" 
          type="textarea" 
          :rows="3" 
          placeholder="请输入详细收货地址">
        </el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddress" :loading="addressLoading">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>
<!-- eslint-disable no-unused-vars -->
<!--  eslint-disable no-undef  -->
<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import { api } from '../utils/api';
import { getUserId, isLoggedIn, getToken } from '../utils/auth';

// 路由相关
const route = useRoute();
const router = useRouter();

// 状态管理
const isLogin = ref(isLoggedIn()); // 模拟登录状态
const activeTab = ref('detail');
const cartCount = ref(3);
const loading = ref(true);
const buyLoading = ref(false); // 添加购买加载状态
const buyQuantity = ref(1); // 购买数量

// 商品数据
const currentProduct = ref(null);
const defaultImage = 'https://picsum.photos/seed/default/300/300';

// 商品分类数据
const productCategories = ref([]);

// 商品评价相关
const productReviews = ref([]);
const reviewPage = ref(1);
const pageSize = ref(10);
const totalReviews = ref(0);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 地址相关状态
const showAddressDialog = ref(false)
const addressLoading = ref(false)
const addressFormRef = ref(null)
const defaultAddressInfo = ref(null) // 存储默认地址信息

// 地址表单数据
const addressForm = reactive({
  receiverName: '',
  phone: '',
  address: '',
  isDefault: false
})

// 地址表单验证规则
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, message: '地址长度至少5个字符', trigger: 'blur' }
  ]
}

// 获取分类名称
const getCategoryName = (code) => {
  const category = productCategories.value.find(cate => cate.code === code);
  return category ? category.name : '未知分类';
};

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = defaultImage;
};

// 页面滚动到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth' // 平滑滚动到顶部
  });
};

// 获取商品分类数据
const loadCategories = async () => {
  try {
    console.log('开始请求商品分类...');
    const response = await fetch('http://localhost:8083/catcatecutomer/product-categories/list');
    console.log('分类响应状态:', response.status);

    if (response.ok) {
      const result = await response.json();
      console.log('分类响应数据:', result);
      if (result.code === 200 && Array.isArray(result.data)) {
        productCategories.value = result.data.filter(cate => cate.isActive === 1);
      } else {
        productCategories.value = getDefaultCategories();
      }
    } else {
      throw new Error('HTTP Error: ' + response.status);
    }
  } catch (error) {
    console.error('获取商品分类失败:', error);
    productCategories.value = getDefaultCategories();
  }
};

// 获取默认分类数据
const getDefaultCategories = () => {
  return [
    { id: 1, name: '猫咪食品', code: 'FOOD', sort_order: 1, is_active: 1 },
    { id: 2, name: '猫咪玩具', code: 'TOY', sort_order: 2, is_active: 1 },
    { id: 3, name: '日用周边', code: 'SUPPLIES', sort_order: 3, is_active: 1 },
    { id: 4, name: '其他周边', code: 'OTHER', sort_order: 4, is_active: 1 }
  ];
};

// 获取商品评价
const loadProductReviews = async () => {
  try {
    if (!currentProduct.value || !currentProduct.value.id) {
      console.warn('商品ID不存在，无法加载评价');
      return;
    }

    const response = await api.get(`/reviews/product/${currentProduct.value.id}`, {
      params: {
        current: reviewPage.value,
        size: pageSize.value
      }
    });

    if (response && response.code === 200) {
      productReviews.value = response.data.records || [];
      totalReviews.value = response.data.total || 0;

      // 调试信息
      console.log('商品评价数据:', productReviews.value);
      productReviews.value.forEach((review, index) => {
        console.log(`评价${index + 1}:`, {
          rating: review.rating,
          type: typeof review.rating,
          content: review.content
        });
      });
    }
  } catch (error) {
    console.error('获取商品评价失败:', error);
    productReviews.value = [];
  }
};

// 处理评价分页变化
const handleReviewPageChange = (page) => {
  reviewPage.value = page;
  loadProductReviews();
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('zh-CN');
};

// 根据ID获取商品详情
const loadProductDetail = async (productId) => {
  try {
    loading.value = true;
    console.log('开始请求商品详情，ID:', productId);

    // 验证商品ID
    if (!productId || isNaN(productId)) {
      throw new Error('无效的商品ID: ' + productId);
    }

    const response = await fetch(`http://localhost:8083/catcatecutomer/products/${productId}`);

    console.log('商品详情响应状态:', response.status);

    if (response.ok) {
      const result = await response.json();
      console.log('商品详情响应数据:', result);

      if (result.code === 200 && result.data) {
        // 处理商品数据
        currentProduct.value = {
          ...result.data,
          price: parseFloat(result.data.price),
          stockQuantity: parseInt(result.data.stockQuantity) || 0,
          salesCount: parseInt(result.data.salesCount) || 0
        };
        console.log('商品详情加载成功:', currentProduct.value);
        ElMessage.success('商品详情加载成功');

        // 加载商品评价
        await loadProductReviews();
      } else {
        throw new Error(result.message || '获取商品详情失败');
      }
    } else {
      throw new Error('HTTP Error: ' + response.status);
    }
  } catch (error) {
    console.error('获取商品详情失败:', error);
    ElMessage.error('获取商品详情失败，请稍后重试: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 获取用户的默认地址
const loadDefaultAddress = async () => {
  try {
    if (!isLoggedIn()) {
      console.log('用户未登录，无法获取默认地址');
      return null;
    }
    
    const userId = getUserId();
    console.log('尝试获取用户默认地址，用户ID:', userId);
    
    const response = await api.get(`/addresses/user/${userId}/default`);
    console.log('默认地址API响应:', response);
    
    if (response && response.code === 200 && response.data) {
      const address = response.data;
      // 构造完整的地址信息
      const fullAddress = `${address.province || ''}${address.city || ''}${address.district || ''}${address.detailAddress || ''}`;
      
      defaultAddressInfo.value = {
        id: address.id,
        receiverName: address.receiverName,
        phone: address.phone,
        province: address.province,
        city: address.city,
        district: address.district,
        detailAddress: address.detailAddress,
        address: fullAddress,
        fullAddress: `${fullAddress} (收货人: ${address.receiverName}, 电话: ${address.phone})`,
        isDefault: address.isDefault
      };
      
      console.log('成功获取默认地址信息:', defaultAddressInfo.value);
      return defaultAddressInfo.value;
    } else {
      console.log('未找到默认地址');
      defaultAddressInfo.value = null;
      return null;
    }
  } catch (error) {
    console.error('获取默认地址失败:', error);
    defaultAddressInfo.value = null;
    return null;
  }
};



// 页面加载时获取数据
onMounted(async () => {
  // 立即滚动到页面顶部
  scrollToTop();

  // 首先加载分类数据
  await loadCategories();

  // 获取路由中的商品ID
  const productId = route.params.id || route.query.id;
  console.log('从路由获取的商品ID:', productId);

  if (productId) {
    // 确保ID是数字类型
    const numericId = parseInt(productId);
    if (!isNaN(numericId)) {
      await loadProductDetail(numericId);
      // 数据加载完成后再次确保滚动到顶部
      setTimeout(() => {
        scrollToTop();
      }, 100);
    } else {
      console.error('商品ID不是有效数字:', productId);
      loading.value = false;
      ElMessage.error('商品ID格式不正确');
    }
  } else {
    console.error('未提供商品ID');
    loading.value = false;
    ElMessage.error('商品ID不能为空');
  }
});

// 处理购买数量变化
const handleQuantityChange = (value) => {
  if (value > currentProduct.value.stockQuantity) {
    buyQuantity.value = currentProduct.value.stockQuantity;
    ElMessage.warning(`购买数量不能超过库存数量(${currentProduct.value.stockQuantity}件)`);
  } else if (value < 1) {
    buyQuantity.value = 1;
    ElMessage.warning('购买数量至少为1件');
  }
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 立即购买
const buyNow = async () => {
  // 登录校验
  if (!isLoggedIn()) {
    ElMessageBox.confirm(
      '购买商品需要先登录账号，是否立即前往登录？',
      '需要登录',
      {
        confirmButtonText: '登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push('/login');
    });
    return;
  }

  // 库存校验
  if (currentProduct.value.stockQuantity <= 0) {
    ElMessage.error('商品库存不足');
    return;
  }

  // 数量校验
  if (buyQuantity.value > currentProduct.value.stockQuantity) {
    ElMessage.error(`购买数量不能超过库存数量(${currentProduct.value.stockQuantity}件)`);
    return;
  }

  if (buyQuantity.value < 1) {
    ElMessage.error('购买数量至少为1件');
    return;
  }

  // 显示地址填写对话框前先获取默认地址
  buyLoading.value = true;
  try {
    await loadDefaultAddress();
    showAddressDialog.value = true;
    
    // 如果有默认地址，自动填充并给出提示
    if (defaultAddressInfo.value) {
      // 自动填充默认地址
      addressForm.receiverName = defaultAddressInfo.value.receiverName;
      addressForm.phone = defaultAddressInfo.value.phone;
      addressForm.address = defaultAddressInfo.value.address;
      addressForm.isDefault = false;
      
      // 给用户提示
      ElMessage.success('已自动填充默认地址，您可以直接确认或修改地址信息');
    } else {
      // 重置表单数据
      addressForm.receiverName = '';
      addressForm.phone = '';
      addressForm.address = '';
      addressForm.isDefault = false;
      ElMessage.info('您还没有设置默认地址，请填写收货信息');
    }
  } catch (error) {
    console.error('获取默认地址失败:', error);
    ElMessage.warning('获取默认地址失败，将手动填写地址');
    showAddressDialog.value = true;
    // 重置表单数据
    addressForm.receiverName = '';
    addressForm.phone = '';
    addressForm.address = '';
    addressForm.isDefault = false;
  } finally {
    buyLoading.value = false;
  }
};

// 处理地址对话框关闭
const handleAddressClose = (done) => {
  ElMessageBox.confirm('确定要取消填写地址吗？')
    .then(() => {
      done()
    })
    .catch(() => {
      // 用户点击了取消
    })
};

// 确认地址并执行购买
const confirmAddress = async () => {
  try {
    // 表单验证
    await addressFormRef.value.validate()
    
    addressLoading.value = true
    
    // 构造完整地址
    const fullAddress = `${addressForm.address} (收货人: ${addressForm.receiverName}, 电话: ${addressForm.phone})`
    
    // 准备订单数据
    const orderData = {
      userId: getUserId(),
      totalAmount: currentProduct.value.price * buyQuantity.value,
      shippingAddress: fullAddress,
      items: [{  
        productId: currentProduct.value.id,
        productName: currentProduct.value.name,
        productPrice: currentProduct.value.price,
        quantity: buyQuantity.value,
        subtotal: currentProduct.value.price * buyQuantity.value,
        itemType: 'PRODUCT'
      }],
      customerNotes: addressForm.isDefault ? '用户选择设为默认地址' : ''
    };

    console.log('发送购买请求:', orderData);

    // 使用统一的API工具调用后端接口创建订单
    const orderResponse = await api.post('/orders/create', orderData);

    console.log('订单响应:', orderResponse);

    // 检查响应结构是否正确
    if (orderResponse && orderResponse.code === 200 && orderResponse.data && orderResponse.data.orderNumber) {
      // 关闭地址对话框
      showAddressDialog.value = false
      
      // 如果用户选择了设为默认地址，更新默认地址
      if (addressForm.isDefault) {
        try {
          await api.post('/addresses/set-default', {
            userId: getUserId(),
            receiverName: addressForm.receiverName,
            phone: addressForm.phone,
            province: '', // 简化处理，实际应该分解地址
            city: '',
            district: '',
            detailAddress: addressForm.address,
            isDefault: 1
          });
          ElMessage.success('地址已设为默认地址');
        } catch (addrError) {
          console.error('设置默认地址失败:', addrError);
        }
      }
      
      // 创建订单成功，现在发起支付宝支付
      try {
        // 使用原生fetch处理支付请求，避免JSON解析
        const paymentResponse = await fetch('http://localhost:8083/catcatecutomer/payment/alipay/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
          },
          body: JSON.stringify({
            orderNumber: orderResponse.data.orderNumber,
            amount: orderResponse.data.totalAmount || (currentProduct.value.price * buyQuantity.value),
            subject: `购买商品：${currentProduct.value.name} x ${buyQuantity.value}`
          })
        });

        // 直接获取文本响应
        const paymentHtml = await paymentResponse.text();

        console.log('支付HTML内容长度:', paymentHtml.length);
        console.log('支付HTML预览:', paymentHtml.substring(0, 200));

        if (paymentHtml && paymentHtml.includes('<form')) {
          // 创建支付窗口
          const paymentWindow = window.open('', '_blank', 'width=800,height=600');
          paymentWindow.document.write(paymentHtml);
          paymentWindow.document.close();
          ElMessage.success('正在跳转到支付页面...');
          
          // 监听支付窗口关闭事件，跳转到支付成功页面
          const checkPaymentWindow = setInterval(() => {
            if (paymentWindow.closed) {
              clearInterval(checkPaymentWindow);
              // 跳转到支付成功页面
              router.push(`/PaymentSuccessfulPage?orderNumber=${orderResponse.data.orderNumber}`);
            }
          }, 1000);
        } else {
          throw new Error('支付响应格式不正确');
        }
      } catch (error) {
        console.error('支付请求失败:', error);
        throw new Error('发起支付失败：' + error.message);
      }
    } else {
      throw new Error(orderResponse?.message || '创建订单失败');
    }
  } catch (error) {
    if (error.field) {
      // 表单验证错误
      console.error('地址填写验证失败:', error);
    } else {
      // 其他错误
      console.error('购买失败:', error);
      ElMessage.error('购买失败：' + (error.message || '未知错误'));
    }
  } finally {
    addressLoading.value = false;
  }
};

// 加入购物车方法
const addToCart = async () => {
  // 登录校验
  if (!isLoggedIn()) {
    ElMessageBox.confirm(
      '加入购物车需要先登录账号，是否立即前往登录？',
      '需要登录',
      {
        confirmButtonText: '登录',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      router.push('/login');
    }).catch(() => {
      // 用户取消登录
    });
    return;
  }

  // 数据校验
  if (!currentProduct.value) {
    ElMessage.error('商品信息加载中，请稍后重试');
    return;
  }

  if (!buyQuantity.value || buyQuantity.value <= 0) {
    ElMessage.warning('请选择购买数量');
    return;
  }

  if (buyQuantity.value > currentProduct.value.stockQuantity) {
    ElMessage.warning('购买数量不能超过库存');
    return;
  }

  try {
    // 构造购物车数据
    const cartData = {
      userId: getUserId(),
      productId: currentProduct.value.id,
      quantity: buyQuantity.value
    };

    console.log('准备添加到购物车的数据:', cartData);

    // 调用后端API添加到购物车
    const response = await api.post('/shopping-cart/add', cartData);

    console.log('购物车添加响应:', response);

    if (response && response.code === 200) {
      ElMessage.success(`${currentProduct.value.name} 已成功加入购物车`);
      cartCount.value += buyQuantity.value;

      // 可选：跳转到购物车页面
      // ElMessageBox.confirm('商品已加入购物车，是否前往购物车查看？', '添加成功', {
      //     confirmButtonText: '去购物车',
      //     cancelButtonText: '继续购物'
      // }).then(() => {
      //     router.push('/cart');
      // });
    } else {
      throw new Error(response?.message || '加入购物车失败');
    }
  } catch (error) {
    console.error('加入购物车失败:', error);
    ElMessage.error('加入购物车失败：' + (error.message || '未知错误'));
  }
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 商品评价样式 */
.review-content {
  padding: 20px 0;
  min-height: 300px;
}

.reviews-list {
  max-height: 400px;
  overflow-y: auto;
}

.review-item {
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 500;
}

.rating-display {
  text-align: right;
}

.review-body {
  margin-left: 42px;
}

.review-text {
  margin: 10px 0;
  line-height: 1.6;
  color: #333;
}

.review-images {
  display: flex;
  gap: 10px;
  margin: 10px 0;
}

.review-image-thumb {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.admin-reply {
  margin-left: 10px;
}

.admin-reply-content {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.admin-reply-content p {
  margin: 0;
  color: #666;
  font-size: 13px;
}

.review-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 页面基础样式 */
.product-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
}

.page-header {
  margin-bottom: 20px;
}

.breadcrumb {
  margin-bottom: 10px;
}

.back-button {
  margin-bottom: 20px;
}

.loading-container {
  padding: 40px 0;
}

.detail-container {
  display: flex;
  gap: 30px;
  margin-bottom: 40px;
}

.image-section {
  flex: 1;
  position: relative;
}

.main-image {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.main-image img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  display: block;
}

.stock-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.stock-tag.low {
  background-color: #ff9800;
}

.stock-tag.out {
  background-color: #f44336;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 15px;
}

.product-description {
  color: #666;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 25px;
}

.price-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff8f0;
  border-radius: 8px;
}

.current-price .price-label {
  font-size: 16px;
  color: #666;
}

.current-price .price {
  font-size: 28px;
  font-weight: 700;
  color: #e74c3c;
}

.sales-info {
  font-size: 14px;
  color: #999;
}

.stock-section {
  margin-bottom: 20px;
  padding: 12px 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.stock-label {
  font-weight: 500;
  color: #333;
}

.stock-value {
  font-weight: 600;
  color: #28a745;
}

.stock-value.out {
  color: #dc3545;
}

.category-section {
  margin-bottom: 25px;
}

.category-label {
  margin-right: 10px;
  color: #333;
  font-weight: 500;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.quantity-label {
  font-weight: 500;
  color: #333;
}

.quantity-info {
  color: #666;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.buy-now-btn,
.add-cart-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.product-intro-section {
  border-top: 1px solid #eee;
  padding-top: 30px;
}

.detail-content h3 {
  font-size: 20px;
  color: #333;
  margin: 25px 0 15px;
}

.detail-content ul {
  padding-left: 20px;
}

.detail-content li {
  margin-bottom: 8px;
  line-height: 1.6;
  color: #555;
}

.detail-content p {
  line-height: 1.7;
  color: #555;
  margin-bottom: 20px;
}

.feature-list li {
  color: #28a745;
}

.notice-list li {
  color: #ff9800;
}

.product-not-found {
  text-align: center;
  padding: 60px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail-page {
    padding: 15px;
  }
  
  .detail-container {
    flex-direction: column;
  }
  
  .main-image img {
    height: 300px;
  }
  
  .product-title {
    font-size: 24px;
  }
  
  .current-price .price {
    font-size: 24px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .buy-now-btn,
  .add-cart-btn {
    width: 100%;
  }
}
</style>