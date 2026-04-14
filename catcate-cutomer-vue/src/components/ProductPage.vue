<!-- 商品页面 -->
<template>
  <div class="products-page">
    <!-- 页面头部：标题+面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>周边商品</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>周边商品</h1>
      <p>精选猫咪主题周边，给你和猫咪更好的体验</p>
    </div>

    <!-- 筛选区域：分类+排序 -->
    <section class="filter-section">
      <div class="filter-content">
        <!-- 商品分类筛选 -->
        <div class="filter-item">
          <label class="filter-label">商品分类</label>
          <el-select v-model="selectedCate" placeholder="全部分类" class="filter-select" @change="onFilterChange">
            <el-option label="全部分类" value="all" />
            <el-option v-for="cate in productCategories" :key="cate.id" :label="cate.name" :value="cate.code" />
          </el-select>
        </div>

        <!-- 价格排序 -->
        <div class="filter-item">
          <label class="filter-label">价格排序</label>
          <el-select v-model="sortType" placeholder="默认排序" class="filter-select" @change="onFilterChange">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
          </el-select>
        </div>

        <!-- 搜索框 -->
        <div class="filter-item">
          <label class="filter-label">商品搜索</label>
          <el-input v-model="searchKeyword" placeholder="请输入商品名称" class="search-input" clearable @clear="onSearchClear"
            @keyup.enter="onSearch">
            <!-- <template #append>
              <el-button icon="Search" @click="onSearch" />
            </template> -->
          </el-input>
        </div>
      </div>
    </section>

    <!-- 商品展示区域 -->
    <section class="section products-section">
      <!-- 筛选结果提示 -->
      <div class="result-tip">
        共找到 <span class="tip-count">{{ totalProducts }}</span> 件商品
        <span v-if="selectedCate !== 'all'">（分类：{{ getCurrentCategoryName() }}）</span>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div class="products-grid">
              <div v-for="i in 8" :key="i" class="skeleton-card">
                <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
                <div style="padding: 14px">
                  <el-skeleton-item variant="h3" style="width: 80%" />
                  <div style="margin-top: 10px">
                    <el-skeleton-item variant="text" style="width: 60%" />
                  </div>
                  <div style="margin-top: 10px">
                    <el-skeleton-item variant="text" style="width: 40%" />
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- 商品空状态 -->
      <el-empty v-else-if="paginatedProducts.length === 0" description="暂无相关商品，换个分类试试吧~" class="products-empty">
        <el-button type="primary" @click="resetFilter">重置筛选</el-button>
      </el-empty>

      <!-- 商品网格 -->
      <div v-else class="products-grid">
        <el-card v-for="product in paginatedProducts" :key="product.id" class="product-card" shadow="hover"
          @click="goToProductDetail(product)">
          <!-- 商品图片容器 -->
          <div class="product-img-container">
            <img :src="product.imageUrl || defaultImage" :alt="product.name" class="product-img"
              @error="handleImageError" />
            <!-- 库存标签 -->
            <div class="stock-tag" :class="{ low: product.stockQuantity <= 10, out: product.stockQuantity <= 0 }"
              v-if="product.stockQuantity <= 10">
              {{ product.stockQuantity <= 0 ? '已售罄' : '库存紧张' }} </div>
                <!-- 加入购物车按钮 -->
                <el-button icon="ShoppingCart" circle size="small" class="add-to-cart" @click.stop="addToCart(product)"
                  :disabled="product.stockQuantity <= 0 || !isLogin" />
            </div>

            <!-- 商品信息 -->
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-meta">
                <span class="product-category">{{ getCategoryName(product.category) }}</span>
                <span class="product-sales">销量: {{ product.salesCount || 0 }}</span>
              </div>
              <div class="product-price-area">
                <span class="product-price">¥{{ formatPrice(product.price) }}</span>
                <span class="product-stock" v-if="product.stockQuantity > 0">
                  库存：{{ product.stockQuantity }}件
                </span>
                <span class="product-stock out" v-else> 已售罄 </span>
              </div>
            </div>
        </el-card>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-container" v-if="totalProducts > 0">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[8, 16, 24, 32]"
          :total="totalProducts" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
          @current-change="handleCurrentChange" background class="custom-pagination" />
      </div>
    </section>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<!--  eslint-disable no-undef  -->
<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ShoppingCart, Search } from '@element-plus/icons-vue';
import { isLoggedIn } from '@/utils/auth';

// 路由实例
const router = useRouter();

// 状态管理
const isLogin = ref(isLoggedIn());
const loading = ref(false);
const cartCount = ref(3);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(16); // 每页显示16个商品

// 筛选条件
const selectedCate = ref('all');
const sortType = ref('default');
const searchKeyword = ref('');

// 数据存储
const productCategories = ref([]);
const products = ref([]); // 存储所有商品数据
const defaultImage = 'https://picsum.photos/seed/default/300/300';

// 计算属性：总商品数量
const totalProducts = computed(() => {
  return filteredProducts.value.length;
});

// 计算属性：当前页的商品（分页后的商品）
const paginatedProducts = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return filteredProducts.value.slice(startIndex, endIndex);
});

// 跳转到商品详情页
const goToProductDetail = (product) => {
  console.log('跳转到商品详情，传递数据:', product);

  router.push({
    name: 'ProductDetail',
    params: { id: product.id },
    state: {
      productData: {
        ...product,
        price: parseFloat(product.price),
        stockQuantity: parseInt(product.stockQuantity) || 0,
        salesCount: parseInt(product.salesCount) || 0
      }
    }
  });
};

// 获取当前分类名称
const getCurrentCategoryName = () => {
  if (selectedCate.value === 'all') return '全部';
  const category = productCategories.value.find(cate => cate.code === selectedCate.value);
  return category ? category.name : '';
};

// 根据分类代码获取分类名称
const getCategoryName = (categoryCode) => {
  const category = productCategories.value.find(cate => cate.code === categoryCode);
  return category ? category.name : categoryCode;
};

// 计算属性：格式化价格
const formatPrice = computed(() => {
  return (price) => {
    if (typeof price === 'string') {
      return parseFloat(price).toFixed(2);
    }
    return price?.toFixed(2) || '0.00';
  };
});

// 计算属性：筛选并排序后的商品（所有商品，不分页）
const filteredProducts = computed(() => {
  let result = [...products.value];

  // 分类筛选
  if (selectedCate.value !== 'all') {
    result = result.filter(product => product.category === selectedCate.value);
  }

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim();
    result = result.filter(product =>
      (product.name && product.name.toLowerCase().includes(keyword)) ||
      (product.description && product.description.toLowerCase().includes(keyword)) ||
      (product.brand && product.brand.toLowerCase().includes(keyword))
    );
  }

  // 价格排序
  if (sortType.value === 'price_asc') {
    result.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
  } else if (sortType.value === 'price_desc') {
    result.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
  }

  return result;
});

// 筛选条件变化处理
const onFilterChange = () => {
  currentPage.value = 1; // 重置到第一页
};

const onSearch = () => {
  currentPage.value = 1; // 重置到第一页
};

const onSearchClear = () => {
  searchKeyword.value = '';
  currentPage.value = 1; // 重置到第一页
};

// 重置筛选条件
const resetFilter = () => {
  selectedCate.value = 'all';
  sortType.value = 'default';
  searchKeyword.value = '';
  currentPage.value = 1;
  loadProducts();
  ElMessage.success('筛选条件已重置');
};

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1; // 重置到第一页
  console.log(`每页显示数量改为: ${val}`);
};

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
  console.log(`当前页码: ${val}`);
  // 滚动到页面顶部
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 加入购物车
const addToCart = (product) => {
  if (!isLogin.value) {
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
      // 取消登录
    });
    return;
  }

  if (product.stockQuantity <= 0) {
    ElMessage.warning('该商品已售罄，无法加入购物车');
    return;
  }

  cartCount.value++;
  ElMessage.success(`${product.name} 已成功加入购物车`);
};

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.src = defaultImage;
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
    { id: 1, name: '猫咪食品', code: 'FOOD', sortOrder: 1, isActive: 1 },
    { id: 2, name: '猫咪玩具', code: 'TOY', sortOrder: 2, isActive: 1 },
    { id: 3, name: '日用周边', code: 'SUPPLIES', sortOrder: 3, isActive: 1 },
    { id: 4, name: '其他周边', code: 'OTHER', sortOrder: 4, isActive: 1 }
  ];
};

// 获取商品数据
const loadProducts = async () => {
  try {
    loading.value = true;
    console.log('开始加载商品数据...');

    const params = {
      pageNum: 1, // 获取所有数据，不在后端分页
      pageSize: 9999, // 获取足够多的数据
      category: null,
      keyword: null,
      sortBy: null
    };

    const response = await fetch('http://localhost:8083/catcatecutomer/products/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(params)
    });

    if (response.ok) {
      const result = await response.json();
      console.log('商品数据响应:', result);

      if (result.code === 200 && result.data) {
        const data = result.data;
        if (data.records) {
          products.value = data.records.map(product => ({
            ...product,
            price: parseFloat(product.price),
            stockQuantity: parseInt(product.stockQuantity) || 0,
            salesCount: parseInt(product.salesCount) || 0
          }));
          console.log(`成功加载 ${products.value.length} 个商品`);
        } else {
          products.value = [];
        }
      } else {
        throw new Error(result.message || '获取商品数据失败');
      }
    } else {
      throw new Error('HTTP Error: ' + response.status);
    }
  } catch (error) {
    console.error('获取商品列表失败:', error);
    ElMessage.error('获取商品数据失败，请稍后重试');
    products.value = getMockProducts();
  } finally {
    loading.value = false;
  }
};

// 获取模拟商品数据（备用）
const getMockProducts = () => {
  return [
    {
      id: 1,
      name: '猫咪主题马克杯',
      category: 'SUPPLIES',
      description: '可爱猫咪图案，陶瓷材质，容量350ml，耐高温易清洗',
      price: 39.9,
      stockQuantity: 25,
      imageUrl: 'https://picsum.photos/seed/product1/300/300',
      brand: '喵趣生活',
      isAvailable: 1,
      salesCount: 128,
      createTime: '2024-01-15T10:30:00'
    },
    {
      id: 2,
      name: '进口无谷猫粮试用装',
      category: 'FOOD',
      description: '天然无谷配方，含鸡肉三文鱼，适合全年龄段猫咪，500g装',
      price: 29.9,
      stockQuantity: 8,
      imageUrl: 'https://picsum.photos/seed/product2/300/300',
      brand: '皇家宠物',
      isAvailable: 1,
      salesCount: 205,
      createTime: '2024-01-10T14:20:00'
    }
    // 可以添加更多模拟数据...
  ];
};

// 统一数据获取函数
const loadData = async () => {
  await Promise.all([
    loadCategories(),
    loadProducts()
  ]);
};

// 页面加载时获取数据
onMounted(() => {
  loadData();
});

// 监听筛选条件变化
watch([selectedCate, sortType, searchKeyword], () => {
  currentPage.value = 1; // 筛选条件变化时重置到第一页
}, { deep: true });
</script>

<style scoped>
/* 页面整体样式 */
.products-page {
  min-height: calc(100vh - 70px);
  background-color: #fff9f5;
  padding: 30px 40px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
}

.breadcrumb {
  margin-bottom: 10px;
  --el-breadcrumb-item-color: #795548;
  --el-breadcrumb-separator-color: #795548;
}

.page-header h1 {
  font-size: 32px;
  color: #5d4037;
  margin: 0 0 8px;
}

.page-header p {
  font-size: 16px;
  color: #795548;
  margin: 0;
}

/* 筛选区域样式 */
.filter-section {
  background-color: #fff;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.filter-content {
  display: flex;
  align-items: center;
  gap: 40px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #5d4037;
  font-weight: 500;
  white-space: nowrap;
}

.filter-select {
  width: 200px;
  --el-select-input-color: #5d4037;
  --el-select-placeholder-color: #9e9e9e;
}

.search-input {
  width: 250px;
}

/* 结果提示 */
.result-tip {
  font-size: 14px;
  color: #795548;
  margin-bottom: 20px;
}

.tip-count {
  color: #e65100;
  font-weight: 600;
}

/* 加载状态 */
.loading-container {
  padding: 20px 0;
}

.skeleton-card {
  border-radius: 15px;
  overflow: hidden;
  background-color: white;
}

/* 商品空状态 */
.products-empty {
  padding: 60px 0;
}

/* 通用区块样式 */
.section {
  margin-bottom: 60px;
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

/* 商品卡片 */
.product-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  background-color: white;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(255, 167, 71, 0.15);
}

/* 商品图片容器 */
.product-img-container {
  position: relative;
  height: 200px;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 库存标签 */
.stock-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  background-color: #fff3e0;
  color: #e65100;
}

.stock-tag.low {
  background-color: #fff3e0;
  color: #e65100;
}

.stock-tag.out {
  background-color: #ffebee;
  color: #e53935;
}

/* 加入购物车按钮 */
.add-to-cart {
  position: absolute;
  bottom: 15px;
  right: 15px;
  background-color: white;
  color: #e65100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.add-to-cart:hover {
  background-color: #e65100;
  color: white;
}

.add-to-cart:disabled {
  background-color: #e0e0e0;
  color: #9e9e9e;
  cursor: not-allowed;
  box-shadow: none;
}

/* 商品信息 */
.product-info {
  padding: 15px;
}

.product-name {
  margin: 0 0 10px;
  font-size: 17px;
  color: #5d4037;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  font-size: 13px;
  color: #795548;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;
  color: #9e9e9e;
}

.product-category {
  background-color: #fff3e0;
  color: #e65100;
  padding: 2px 8px;
  border-radius: 10px;
}

.product-sales {
  color: #795548;
}

/* 价格区域 */
.product-price-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #e65100;
}

.product-stock {
  font-size: 12px;
  color: #795548;
}

.product-stock.out {
  color: #e53935;
}

/* 分页容器样式 */
.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.custom-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
}

.custom-pagination :deep(.el-pagination__total) {
  color: #795548;
  font-weight: 500;
}

.custom-pagination :deep(.el-pagination__sizes) {
  margin: 0 15px;
}

.custom-pagination :deep(.el-select .el-input__inner) {
  border-color: #e65100;
  color: #5d4037;
}

.custom-pagination :deep(.el-pagination__jump) {
  margin-left: 15px;
  color: #795548;
}

.custom-pagination :deep(.el-pagination__jump .el-input__inner) {
  border-color: #e65100;
  width: 50px;
  text-align: center;
}

.custom-pagination :deep(.btn-prev),
.custom-pagination :deep(.btn-next),
.custom-pagination :deep(.el-pager li) {
  background-color: #fff;
  border-color: #e65100;
  color: #5d4037;
  min-width: 35px;
  height: 35px;
  line-height: 35px;
}

.custom-pagination :deep(.btn-prev:hover),
.custom-pagination :deep(.btn-next:hover),
.custom-pagination :deep(.el-pager li:hover) {
  color: #e65100;
  border-color: #e65100;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background-color: #e65100;
  border-color: #e65100;
  color: white;
}


/* 响应式分页调整 */
@media (max-width: 768px) {
  .pagination-container {
    margin-top: 30px;
    padding: 15px 0;
  }

  .custom-pagination :deep(.el-pagination__sizes) {
    display: none;
  }

  .custom-pagination :deep(.el-pagination__jump) {
    display: none;
  }

  .custom-pagination :deep(.btn-prev),
  .custom-pagination :deep(.btn-next),
  .custom-pagination :deep(.el-pager li) {
    min-width: 30px;
    height: 30px;
    line-height: 30px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .custom-pagination :deep(.el-pagination__total) {
    display: none;
  }

  .custom-pagination :deep(.btn-prev),
  .custom-pagination :deep(.btn-next) {
    min-width: 25px;
    height: 25px;
    line-height: 25px;
    font-size: 12px;
  }
}
</style>