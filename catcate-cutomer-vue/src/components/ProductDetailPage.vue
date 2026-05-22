<template>
  <Layout>
    <!-- 页面头部：标题+面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item @click="$router.push('/home')">首页</el-breadcrumb-item>
        <el-breadcrumb-item @click="goToProductList">{{ getCategoryName(currentProduct?.category) || '商品列表' }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentProduct ? currentProduct.name : '商品详情' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft" type="primary" plain>
        返回
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-spin size="large" />
    </div>

    <!-- 商品详情内容 -->
    <div class="product-detail-content" v-else-if="currentProduct">
      <!-- 商品主体区域 -->
      <div class="detail-container">
        <!-- 商品图片展示区 -->
        <div class="image-section">
          <div class="main-image-wrapper">
            <el-image 
              :src="currentProduct.imageUrl || defaultImage" 
              :alt="currentProduct.name" 
              class="main-image"
              @error="handleImageError"
              fit="contain"
              :preview-src-list="imagePreviewList"
            />
          </div>
          <!-- 图片缩略图 -->
          <div class="thumbnails" v-if="productImages.length > 1">
            <div 
              v-for="(img, index) in productImages" 
              :key="index"
              class="thumbnail-item"
              :class="{ active: activeImageIndex === index }"
              @click="activeImageIndex = index"
            >
              <img :src="img" :alt="`图片${index + 1}`" @error="(e) => handleThumbnailError(e, index)" />
            </div>
          </div>
        </div>

        <!-- 商品信息区 -->
        <div class="info-section">
          <!-- 标签 -->
          <div class="product-tags">
            <el-tag type="danger" v-if="currentProduct.isHot">热卖</el-tag>
            <el-tag type="success" v-if="currentProduct.isNew">新品</el-tag>
            <el-tag type="warning" v-if="currentProduct.stockQuantity <= 5 && currentProduct.stockQuantity > 0">仅剩{{ currentProduct.stockQuantity }}件</el-tag>
          </div>

          <h1 class="product-title">{{ currentProduct.name }}</h1>
          
          <!-- 评分 -->
          <div class="product-rating" v-if="averageRating > 0">
            <el-rate :model-value="averageRating" disabled :max="5" show-score text-color="#ff9900" score-template="{value}分" />
            <span class="rating-count">({{ reviewCount }}条评价)</span>
          </div>

          <p class="product-description">{{ currentProduct.description || '暂无描述' }}</p>

          <!-- 价格信息 -->
          <div class="price-section">
            <div class="current-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ parseFloat(currentProduct.price).toFixed(2) }}</span>
            </div>
            <div class="sales-info">已售 {{ currentProduct.salesCount || 0 }} 件</div>
          </div>

          <!-- 库存信息 -->
          <div class="stock-section" :class="{ out: currentProduct.stockQuantity <= 0 }">
            <el-icon :icon="currentProduct.stockQuantity > 0 ? CheckCircle : AlertCircle" />
            <span>{{ currentProduct.stockQuantity > 0 ? `库存充足 (${currentProduct.stockQuantity}件)` : '已售罄' }}</span>
          </div>

          <!-- 分类信息 -->
          <div class="category-section">
            <span class="label">分类：</span>
            <el-tag type="primary">{{ getCategoryName(currentProduct.category) }}</el-tag>
          </div>

          <!-- 品牌信息 -->
          <div class="brand-section" v-if="currentProduct.brand">
            <span class="label">品牌：</span>
            <span class="value">{{ currentProduct.brand }}</span>
          </div>

          <!-- 购买数量选择 -->
          <div class="quantity-section" v-if="currentProduct.stockQuantity > 0">
            <span class="label">购买数量：</span>
            <el-input-number 
              v-model="buyQuantity" 
              :min="1" 
              :max="currentProduct.stockQuantity" 
              :step="1"
              size="medium" 
              controls-position="right"
              @change="handleQuantityChange"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="large" 
              class="buy-now-btn"
              :disabled="currentProduct.stockQuantity <= 0 || !isLogin || buyLoading" 
              :loading="buyLoading"
              icon="ShoppingCart"
              @click="buyNow"
            >
              {{ buyLoading ? '处理中...' : '立即购买' }}
            </el-button>
            <el-button 
              type="warning" 
              size="large" 
              class="add-cart-btn"
              :disabled="currentProduct.stockQuantity <= 0 || !isLogin" 
              icon="Plus"
              @click="addToCart"
            >
              加入购物车
            </el-button>
          </div>

        </div>
      </div>

      <!-- 商品详情介绍 -->
      <div class="product-intro-section">
        <el-tabs v-model="activeTab" type="card" class="custom-tabs">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content">
              <h3>产品特色</h3>
              <ul class="feature-list">
                <li><el-icon :icon="Check" />精选优质材料制作，品质保证</li>
                <li><el-icon :icon="Check" />符合猫咪使用习惯，安全可靠</li>
                <li><el-icon :icon="Check" />精美包装，适合送礼或自用</li>
                <li><el-icon :icon="Check" />售后无忧，支持7天无理由退换</li>
              </ul>

              <h3>使用说明</h3>
              <p>请按照产品说明正确使用，如有疑问请联系客服。</p>

              <h3>注意事项</h3>
              <ul class="notice-list">
                <li><el-icon :icon="AlertTriangle" />请放置在猫咪可接触的安全位置</li>
                <li><el-icon :icon="AlertTriangle" />定期清洁保养，延长使用寿命</li>
                <li><el-icon :icon="AlertTriangle" />如发现损坏请及时停止使用</li>
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
                        <el-avatar :size="40" :src="review.userAvatar || defaultAvatar"></el-avatar>
                        <div class="user-info">
                          <span class="username">{{ review.isAnonymous ? '匿名用户' : review.username || '用户' + review.userId }}</span>
                          <span class="review-date">{{ formatDate(review.createTime) }}</span>
                        </div>
                      </div>
                      <div class="rating-display">
                        <el-rate :model-value="Number(review.rating)" disabled :max="5" size="small" show-score text-color="#ff9900" score-template="{value}分"></el-rate>
                      </div>
                    </div>
                    <div class="review-body">
                      <p class="review-text">{{ review.content }}</p>
                      <div v-if="review.images && review.images.length > 0" class="review-images">
                        <el-image 
                          v-for="(image, index) in review.images" 
                          :key="index" 
                          :src="image"
                          :preview-src-list="review.images" 
                          class="review-image-thumb" 
                          fit="cover" 
                        />
                      </div>
                      <div v-if="review.adminReply" class="admin-reply-content">
                        <div class="reply-header">
                          <el-tag type="success" size="small">商家回复</el-tag>
                        </div>
                        <p>{{ review.adminReply }}</p>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 分页 -->
                <div class="review-pagination" v-if="totalReviews > pageSize">
                  <el-pagination 
                    v-model:current-page="reviewPage" 
                    v-model:page-size="pageSize" 
                    :total="totalReviews"
                    layout="prev, pager, next" 
                    @current-change="handleReviewPageChange" 
                  />
                </div>
              </div>
              <el-empty v-else description="暂无用户评价" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="商品参数" name="params">
            <div class="params-content">
              <table class="params-table">
                <tr>
                  <td class="param-label">商品编号</td>
                  <td class="param-value">{{ currentProduct.id }}</td>
                </tr>
                <tr>
                  <td class="param-label">商品名称</td>
                  <td class="param-value">{{ currentProduct.name }}</td>
                </tr>
                <tr>
                  <td class="param-label">商品分类</td>
                  <td class="param-value">{{ getCategoryName(currentProduct.category) }}</td>
                </tr>
                <tr>
                  <td class="param-label">品牌</td>
                  <td class="param-value">{{ currentProduct.brand || '无' }}</td>
                </tr>
                <tr>
                  <td class="param-label">价格</td>
                  <td class="param-value">¥{{ parseFloat(currentProduct.price).toFixed(2) }}</td>
                </tr>
                <tr>
                  <td class="param-label">库存</td>
                  <td class="param-value">{{ currentProduct.stockQuantity }}件</td>
                </tr>
                <tr>
                  <td class="param-label">销量</td>
                  <td class="param-value">{{ currentProduct.salesCount || 0 }}件</td>
                </tr>
              </table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 商品不存在提示 -->
    <div class="product-not-found" v-else-if="!loading">
      <el-result icon="warning" title="商品不存在" sub-title="抱歉，您访问的商品不存在或已被下架">
        <template #extra>
          <el-button type="primary" @click="$router.push('/product')">
            返回商品列表
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- 地址输入对话框（非店内餐饮） -->
    <el-dialog v-model="showAddressDialog" title="填写收货地址" width="500px" :before-close="handleAddressClose">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="100px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="addressForm.address" type="textarea" :rows="3" placeholder="请输入详细收货地址"></el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddressDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmAddress" :loading="addressLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 桌号选择对话框（店内餐饮） -->
    <el-dialog v-model="showTableDialog" title="选择桌号" width="400px" :before-close="handleTableClose">
      <div class="table-select-container">
        <el-form :model="tableForm" :rules="tableRules" ref="tableFormRef" label-width="80px">
          <el-form-item label="桌号" prop="tableNumber">
            <el-select v-model="tableForm.tableNumber" placeholder="请选择桌号" class="table-select">
              <el-option 
                v-for="table in availableTables" 
                :key="table.number" 
                :label="table.label" 
                :value="table.number"
                :disabled="table.occupied"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="notes">
            <el-input v-model="tableForm.notes" type="textarea" :rows="2" placeholder="如有特殊要求请备注"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTableDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmTable" :loading="tableLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus';
import { CheckCircle, AlertCircle, Check, AlertTriangle } from '@element-plus/icons-vue';
import { api } from '../utils/api';
import { getUserId, isLoggedIn, getToken } from '../utils/auth';
import Layout from './AppLayout.vue';

const route = useRoute();
const router = useRouter();

// 状态管理
const isLogin = ref(isLoggedIn());
const activeTab = ref('detail');
const loading = ref(true);
const buyLoading = ref(false);
const buyQuantity = ref(1);
const activeImageIndex = ref(0);

// 商品数据
const currentProduct = ref(null);
const defaultImage = 'https://picsum.photos/seed/default/500/500';
const productImages = ref([]);

// 商品分类数据
const productCategories = ref([]);

// 商品评价相关
const productReviews = ref([]);
const reviewPage = ref(1);
const pageSize = ref(10);
const totalReviews = ref(0);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 地址相关状态（非店内餐饮）
const showAddressDialog = ref(false);
const addressLoading = ref(false);
const addressFormRef = ref(null);
const defaultAddressInfo = ref(null);

// 地址表单数据
const addressForm = reactive({
  receiverName: '',
  phone: '',
  address: '',
  isDefault: false
});

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
};

// 桌号相关状态（店内餐饮）
const showTableDialog = ref(false);
const tableLoading = ref(false);
const tableFormRef = ref(null);

// 可用桌号列表
const availableTables = ref([]);

// 加载桌号列表
const loadTables = async () => {
  tableLoading.value = true;
  try {
    const response = await api.get('/tables/list');
    
    if (response.code === 200 && response.data && response.data.length > 0) {
      availableTables.value = response.data.map(table => ({
        number: table.tableNumber,
        label: table.tableNumber,
        occupied: false,
        capacity: table.capacity || 2,
        id: table.id
      }));
    } else {
      // 如果API返回空，使用默认数据
      availableTables.value = [
        { number: '1', label: '1号桌', occupied: false, capacity: 2, id: 1 },
        { number: '2', label: '2号桌', occupied: false, capacity: 2, id: 2 },
        { number: '3', label: '3号桌', occupied: false, capacity: 4, id: 3 },
        { number: '4', label: '4号桌', occupied: false, capacity: 4, id: 4 },
        { number: '5', label: '5号桌', occupied: false, capacity: 6, id: 5 },
        { number: '6', label: '6号桌', occupied: false, capacity: 2, id: 6 },
        { number: '7', label: '7号桌', occupied: false, capacity: 4, id: 7 },
        { number: '8', label: '8号桌', occupied: false, capacity: 4, id: 8 },
        { number: '9', label: '9号桌', occupied: false, capacity: 6, id: 9 },
        { number: '10', label: '10号桌', occupied: false, capacity: 2, id: 10 },
        { number: 'A1', label: 'A1号包厢', occupied: false, capacity: 8, id: 11 },
        { number: 'A2', label: 'A2号包厢', occupied: false, capacity: 8, id: 12 }
      ];
    }
  } catch (error) {
    console.error('加载桌号列表失败:', error);
    // API调用失败时使用默认数据
    availableTables.value = [
      { number: '1', label: '1号桌', occupied: false, capacity: 2, id: 1 },
      { number: '2', label: '2号桌', occupied: false, capacity: 2, id: 2 },
      { number: '3', label: '3号桌', occupied: false, capacity: 4, id: 3 },
      { number: '4', label: '4号桌', occupied: false, capacity: 4, id: 4 },
      { number: '5', label: '5号桌', occupied: false, capacity: 6, id: 5 },
      { number: '6', label: '6号桌', occupied: false, capacity: 2, id: 6 },
      { number: '7', label: '7号桌', occupied: false, capacity: 4, id: 7 },
      { number: '8', label: '8号桌', occupied: false, capacity: 4, id: 8 },
      { number: '9', label: '9号桌', occupied: false, capacity: 6, id: 9 },
      { number: '10', label: '10号桌', occupied: false, capacity: 2, id: 10 },
      { number: 'A1', label: 'A1号包厢', occupied: false, capacity: 8, id: 11 },
      { number: 'A2', label: 'A2号包厢', occupied: false, capacity: 8, id: 12 }
    ];
    ElMessage.warning('使用示例桌号数据，请确保后端服务已启动');
  } finally {
    tableLoading.value = false;
  }
};

// 桌号表单数据
const tableForm = reactive({
  tableNumber: '',
  notes: ''
});

// 桌号表单验证规则
const tableRules = {
  tableNumber: [
    { required: true, message: '请选择桌号', trigger: 'blur' }
  ]
};

// 判断是否为店内餐饮商品
const isDiningProduct = computed(() => {
  if (!currentProduct.value) return false;
  const diningCategories = ['YINPIN', 'XIAOCHI', 'DIANNEICANYIN'];
  return diningCategories.includes(currentProduct.value.category);
});

// 图片预览列表
const imagePreviewList = computed(() => {
  if (!currentProduct.value) return [];
  const images = [currentProduct.value.imageUrl || defaultImage];
  if (productImages.value.length > 0) {
    images.push(...productImages.value);
  }
  return images;
});

// 平均评分
const averageRating = computed(() => {
  if (!productReviews.value || productReviews.value.length === 0) return 0;
  const total = productReviews.value.reduce((sum, review) => sum + Number(review.rating), 0);
  return (total / productReviews.value.length).toFixed(1);
});

// 评价数量
const reviewCount = computed(() => totalReviews.value);

// 获取分类名称
const getCategoryName = (code) => {
  if (!code) return '未知分类';
  const category = productCategories.value.find(cate => cate.code === code);
  return category ? category.name : code;
};

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = defaultImage;
};

// 处理缩略图加载错误
const handleThumbnailError = (event) => {
  event.target.src = defaultImage;
};

// 页面滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 获取商品分类数据
const loadCategories = async () => {
  try {
    const response = await fetch('http://localhost:8083/catcatecutomer/product-categories/list');
    if (response.ok) {
      const result = await response.json();
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
    { id: 4, name: '其他周边', code: 'OTHER', sort_order: 4, is_active: 1 },
    { id: 5, name: '饮品', code: 'YINPIN', sort_order: 5, is_active: 1 },
    { id: 6, name: '小吃', code: 'XIAOCHI', sort_order: 6, is_active: 1 }
  ];
};

// 获取商品评价
const loadProductReviews = async () => {
  try {
    if (!currentProduct.value || !currentProduct.value.id) {
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

    if (!productId || isNaN(productId)) {
      throw new Error('无效的商品ID: ' + productId);
    }

    const response = await fetch(`http://localhost:8083/catcatecutomer/products/${productId}`);

    if (response.ok) {
      const result = await response.json();

      if (result.code === 200 && result.data) {
        const now = new Date();
        currentProduct.value = {
          ...result.data,
          price: parseFloat(result.data.price),
          stockQuantity: parseInt(result.data.stockQuantity) || 0,
          salesCount: parseInt(result.data.salesCount) || 0,
          isHot: (parseInt(result.data.salesCount) || 0) > 50,
          isNew: result.data.createdAt && new Date(result.data.createdAt) > new Date(now - 30 * 24 * 60 * 60 * 1000)
        };

        // 处理商品图片
        if (result.data.imageUrls) {
          productImages.value = result.data.imageUrls.split(',').filter(img => img.trim());
        }

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
      return null;
    }

    const userId = getUserId();
    const response = await api.get(`/addresses/user/${userId}/default`);

    if (response && response.code === 200 && response.data) {
      const address = response.data;
      const fullAddress = `${address.province || ''}${address.city || ''}${address.district || ''}${address.detailAddress || ''}`;

      defaultAddressInfo.value = {
        id: address.id,
        receiverName: address.receiverName,
        phone: address.phone,
        address: fullAddress,
        isDefault: address.isDefault
      };

      return defaultAddressInfo.value;
    } else {
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
  scrollToTop();
  await loadCategories();

  const productId = route.params.id || route.query.id;
  if (productId) {
    const numericId = parseInt(productId);
    if (!isNaN(numericId)) {
      await loadProductDetail(numericId);
      setTimeout(() => scrollToTop(), 100);
    } else {
      loading.value = false;
      ElMessage.error('商品ID格式不正确');
    }
  } else {
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

// 返回商品列表
const goToProductList = () => {
  if (currentProduct.value) {
    const isDining = ['YINPIN', 'XIAOCHI', 'DIANNEICANYIN'].includes(currentProduct.value.category);
    router.push(isDining ? '/catering' : '/product');
  } else {
    router.push('/product');
  }
};

// 立即购买
const buyNow = async () => {
  if (!isLoggedIn()) {
    ElMessageBox.confirm(
      '购买商品需要先登录账号，是否立即前往登录？',
      '需要登录',
      { confirmButtonText: '登录', cancelButtonText: '取消', type: 'warning' }
    ).then(() => {
      router.push('/login');
    });
    return;
  }

  if (currentProduct.value.stockQuantity <= 0) {
    ElMessage.error('商品库存不足');
    return;
  }

  if (buyQuantity.value > currentProduct.value.stockQuantity) {
    ElMessage.error(`购买数量不能超过库存数量(${currentProduct.value.stockQuantity}件)`);
    return;
  }

  if (buyQuantity.value < 1) {
    ElMessage.error('购买数量至少为1件');
    return;
  }

  // 判断是否为店内餐饮商品
  if (isDiningProduct.value) {
    // 店内餐饮：显示桌号选择
    tableForm.tableNumber = '';
    tableForm.notes = '';
    // 从数据库加载桌号列表
    await loadTables();
    showTableDialog.value = true;
  } else {
    // 其他商品：显示地址填写
    buyLoading.value = true;
    try {
      await loadDefaultAddress();

      if (defaultAddressInfo.value) {
        addressForm.receiverName = defaultAddressInfo.value.receiverName;
        addressForm.phone = defaultAddressInfo.value.phone;
        addressForm.address = defaultAddressInfo.value.address;
        addressForm.isDefault = false;
        ElMessage.success('已自动填充默认地址');
      } else {
        addressForm.receiverName = '';
        addressForm.phone = '';
        addressForm.address = '';
        addressForm.isDefault = false;
        ElMessage.info('您还没有设置默认地址，请填写收货信息');
      }
      showAddressDialog.value = true;
    } catch (error) {
      console.error('获取默认地址失败:', error);
      addressForm.receiverName = '';
      addressForm.phone = '';
      addressForm.address = '';
      addressForm.isDefault = false;
      showAddressDialog.value = true;
    } finally {
      buyLoading.value = false;
    }
  }
};

// 处理地址对话框关闭
const handleAddressClose = (done) => {
  ElMessageBox.confirm('确定要取消填写地址吗？')
    .then(() => done())
    .catch(() => {});
};

// 处理桌号对话框关闭
const handleTableClose = (done) => {
  ElMessageBox.confirm('确定要取消选择桌号吗？')
    .then(() => done())
    .catch(() => {});
};

// 确认桌号并执行购买（店内餐饮）
const confirmTable = async () => {
  try {
    await tableFormRef.value.validate();
    tableLoading.value = true;

    const orderData = {
      userId: getUserId(),
      totalAmount: currentProduct.value.price * buyQuantity.value,
      shippingAddress: `桌号: ${tableForm.tableNumber}`,
      items: [{
        productId: currentProduct.value.id,
        productName: currentProduct.value.name,
        productPrice: currentProduct.value.price,
        quantity: buyQuantity.value,
        subtotal: currentProduct.value.price * buyQuantity.value,
        itemType: 'DINING'
      }],
      customerNotes: tableForm.notes || ''
    };

    const orderResponse = await api.post('/orders/create', orderData);

    if (orderResponse && orderResponse.code === 200 && orderResponse.data && orderResponse.data.orderNumber) {
      showTableDialog.value = false;

      const paymentResponse = await fetch('http://localhost:8083/catcatecutomer/payment/alipay/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify({
          orderNumber: orderResponse.data.orderNumber,
          amount: orderResponse.data.totalAmount || (currentProduct.value.price * buyQuantity.value),
          subject: `店内消费：${currentProduct.value.name} x ${buyQuantity.value}，桌号：${tableForm.tableNumber}`
        })
      });

      const paymentHtml = await paymentResponse.text();

      if (paymentHtml && paymentHtml.includes('<form')) {
        const paymentWindow = window.open('', '_blank', 'width=800,height=600');
        paymentWindow.document.write(paymentHtml);
        paymentWindow.document.close();
        ElMessage.success('正在跳转到支付页面...');

        const checkPaymentWindow = setInterval(() => {
          if (paymentWindow.closed) {
            clearInterval(checkPaymentWindow);
            const paymentAmount = currentProduct.value.price * buyQuantity.value;
            router.push(`/PaymentSuccessfulPage?orderNumber=${orderResponse.data.orderNumber}&total_amount=${paymentAmount}`);
          }
        }, 1000);
      } else {
        throw new Error('支付响应格式不正确');
      }
    } else {
      throw new Error(orderResponse?.message || '创建订单失败');
    }
  } catch (error) {
    console.error('购买失败:', error);
    ElMessage.error('购买失败：' + (error.message || '未知错误'));
  } finally {
    tableLoading.value = false;
  }
};

// 确认地址并执行购买
const confirmAddress = async () => {
  try {
    await addressFormRef.value.validate();
    addressLoading.value = true;

    const fullAddress = `${addressForm.address} (收货人: ${addressForm.receiverName}, 电话: ${addressForm.phone})`;

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

    const orderResponse = await api.post('/orders/create', orderData);

    if (orderResponse && orderResponse.code === 200 && orderResponse.data && orderResponse.data.orderNumber) {
      showAddressDialog.value = false;

      if (addressForm.isDefault) {
        try {
          await api.post('/addresses/set-default', {
            userId: getUserId(),
            receiverName: addressForm.receiverName,
            phone: addressForm.phone,
            province: '',
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

      const paymentHtml = await paymentResponse.text();

      if (paymentHtml && paymentHtml.includes('<form')) {
        const paymentWindow = window.open('', '_blank', 'width=800,height=600');
        paymentWindow.document.write(paymentHtml);
        paymentWindow.document.close();
        ElMessage.success('正在跳转到支付页面...');

        const checkPaymentWindow = setInterval(() => {
          if (paymentWindow.closed) {
            clearInterval(checkPaymentWindow);
            const paymentAmount = currentProduct.value.price * buyQuantity.value;
            router.push(`/PaymentSuccessfulPage?orderNumber=${orderResponse.data.orderNumber}&total_amount=${paymentAmount}`);
          }
        }, 1000);
      } else {
        throw new Error('支付响应格式不正确');
      }
    } else {
      throw new Error(orderResponse?.message || '创建订单失败');
    }
  } catch (error) {
    console.error('购买失败:', error);
    ElMessage.error('购买失败：' + (error.message || '未知错误'));
  } finally {
    addressLoading.value = false;
  }
};

// 加入购物车方法
const addToCart = async () => {
  if (!isLoggedIn()) {
    ElMessageBox.confirm(
      '加入购物车需要先登录账号，是否立即前往登录？',
      '需要登录',
      { confirmButtonText: '登录', cancelButtonText: '取消', type: 'info' }
    ).then(() => {
      router.push('/login');
    }).catch(() => {});
    return;
  }

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
    const cartData = {
      userId: getUserId(),
      productId: currentProduct.value.id,
      quantity: buyQuantity.value
    };

    const response = await api.post('/shopping-cart/add', cartData);

    if (response && response.code === 200) {
      ElMessage.success(`${currentProduct.value.name} 已成功加入购物车`);
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
.page-header {
  margin-bottom: 20px;
}

.breadcrumb {
  --el-breadcrumb-item-color: #795548;
  --el-breadcrumb-separator-color: #795548;
}

.back-button {
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.product-detail-content {
  background-color: #fff;
  border-radius: 15px;
  overflow: hidden;
}

.detail-container {
  display: flex;
  gap: 40px;
  padding: 30px;
}

/* 图片区域 */
.image-section {
  flex: 1;
}

.main-image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 15px;
}

.main-image {
  width: 100%;
  height: 450px;
  background-color: #f8f9fa;
}

.thumbnails {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item:hover {
  border-color: #e65100;
}

.thumbnail-item.active {
  border-color: #e65100;
  box-shadow: 0 0 0 2px rgba(230, 81, 0, 0.2);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 信息区域 */
.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.product-tags :deep(.el-tag) {
  font-size: 12px;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 15px;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.rating-count {
  font-size: 14px;
  color: #999;
}

.product-description {
  color: #666;
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 25px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.price-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #fff8f0 0%, #fff0e6 100%);
  border-radius: 12px;
}

.current-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24px;
  font-weight: 600;
  color: #e74c3c;
}

.price-value {
  font-size: 36px;
  font-weight: 700;
  color: #e74c3c;
}

.sales-info {
  font-size: 14px;
  color: #999;
}

.stock-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 15px;
  margin-bottom: 15px;
  border-radius: 8px;
  font-size: 14px;
}

.stock-section :deep(.el-icon) {
  font-size: 18px;
}

.stock-section.out {
  background-color: #ffebee;
  color: #e53935;
}

.stock-section:not(.out) {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.category-section,
.brand-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
}

.category-section .label,
.brand-section .label {
  color: #666;
  font-weight: 500;
}

.category-section :deep(.el-tag) {
  font-size: 12px;
}

.brand-section .value {
  color: #333;
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

.quantity-section .label {
  font-weight: 500;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.buy-now-btn,
.add-cart-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
}

.buy-now-btn {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  border: none;
}

.add-cart-btn {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
  border: none;
}

.service-section {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.service-item :deep(.el-icon) {
  color: #e65100;
  font-size: 16px;
}

/* 商品介绍标签页 */
.product-intro-section {
  padding: 30px;
  border-top: 1px solid #eee;
}

.custom-tabs {
  --el-tabs-header-text-color: #666;
  --el-tabs-active-text-color: #e65100;
  --el-tabs-active-border-color: #e65100;
}

.detail-content h3 {
  font-size: 20px;
  color: #333;
  margin: 25px 0 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.detail-content ul {
  padding-left: 0;
  list-style: none;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  line-height: 1.6;
  color: #333;
}

.feature-list :deep(.el-icon) {
  color: #28a745;
}

.notice-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  line-height: 1.6;
  color: #e65100;
}

.notice-list :deep(.el-icon) {
  color: #e65100;
}

.detail-content p {
  line-height: 1.7;
  color: #555;
  margin-bottom: 20px;
}

/* 评价内容 */
.review-content {
  padding: 20px 0;
  min-height: 300px;
}

.reviews-list {
  max-height: 500px;
  overflow-y: auto;
}

.review-item {
  border-bottom: 1px solid #eee;
  padding: 20px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.review-user {
  display: flex;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 500;
  color: #333;
}

.review-date {
  font-size: 12px;
  color: #999;
}

.rating-display {
  text-align: right;
}

.review-body {
  margin-left: 52px;
}

.review-text {
  margin: 0 0 15px;
  line-height: 1.6;
  color: #333;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.review-image-thumb {
  width: 80px;
  height: 80px;
  border-radius: 6px;
}

.admin-reply-content {
  margin-top: 15px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.reply-header {
  margin-bottom: 8px;
}

.admin-reply-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.review-pagination {
  display: flex;
  justify-content: center;
  margin-top: 25px;
}

/* 参数内容 */
.params-content {
  padding: 20px;
}

.params-table {
  width: 100%;
  border-collapse: collapse;
}

.params-table tr {
  border-bottom: 1px solid #f0f0f0;
}

.params-table tr:last-child {
  border-bottom: none;
}

.param-label {
  width: 30%;
  padding: 15px 0;
  text-align: right;
  font-weight: 500;
  color: #666;
  padding-right: 20px;
}

.param-value {
  padding: 15px 0;
  color: #333;
}

.product-not-found {
  text-align: center;
  padding: 60px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 桌号选择对话框样式 */
.table-select-container {
  padding: 10px;
}

.table-select {
  width: 100%;
}

.table-select :deep(.el-select__tags) {
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .detail-container {
    flex-direction: column;
    padding: 20px;
  }

  .main-image {
    height: 350px;
  }

  .product-title {
    font-size: 24px;
  }

  .price-value {
    font-size: 32px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .service-section {
    flex-wrap: wrap;
    gap: 15px;
  }
}

@media (max-width: 768px) {
  .main-image {
    height: 280px;
  }

  .product-title {
    font-size: 22px;
  }

  .price-value {
    font-size: 28px;
  }

  .price-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .thumbnails {
    justify-content: flex-start;
  }
}
</style>