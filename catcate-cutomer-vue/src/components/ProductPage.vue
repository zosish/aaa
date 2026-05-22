<!-- 宠物用品页面 -->
<template>
  <Layout>
    <!-- 页面头部：标题+面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item @click="$router.push('/home')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>宠物用品</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>宠物用品</h1>
      <p>精选猫咪用品，给猫咪更好的生活体验</p>
    </div>

    <!-- 筛选区域：分类+排序 -->
    <section class="filter-section">
      <div class="filter-content">
        <!-- 商品分类筛选 -->
        <div class="filter-item">
          <label class="filter-label">商品分类</label>
          <el-cascader
            v-model="categoryPath"
            :options="categoryTree"
            :props="cascaderProps"
            placeholder="全部分类"
            class="filter-select"
            @change="handleCategoryChange"
          ></el-cascader>
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
          </el-input>
        </div>
      </div>
    </section>

    <!-- 商品展示区域 -->
    <section class="section products-section">
      <!-- 筛选结果提示 -->
      <div class="result-tip">
        共找到 <span class="tip-count">{{ totalProducts }}</span> 件宠物用品
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
      <el-empty v-else-if="paginatedProducts.length === 0" description="暂无相关宠物用品，换个分类试试吧~" class="products-empty">
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
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import Layout from './AppLayout.vue';

const router = useRouter();

const PET_SUPPLIES_CODE = 'CHONGWUYONGPIN';

const loading = ref(false);

const currentPage = ref(1);
const pageSize = ref(16);

const selectedCate = ref('all');
const sortType = ref('default');
const searchKeyword = ref('');

const categoryTree = ref([]);
const categoryPath = ref([]);
const cascaderProps = {
    value: 'code',
    label: 'name',
    children: 'children',
    checkStrictly: false,
    emitPath: true,
    expandTrigger: 'click'
};

const productCategories = ref([]);
const products = ref([]);
const defaultImage = 'https://picsum.photos/seed/default/300/300';

const totalProducts = computed(() => {
  return filteredProducts.value.length;
});

const paginatedProducts = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return filteredProducts.value.slice(startIndex, endIndex);
});

const goToProductDetail = (product) => {
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

const getCurrentCategoryName = () => {
  if (selectedCate.value === 'all') return '全部';
  const category = productCategories.value.find(cate => cate.code === selectedCate.value);
  return category ? category.name : '';
};

const getCategoryName = (categoryCode) => {
  const category = productCategories.value.find(cate => cate.code === categoryCode);
  return category ? category.name : categoryCode;
};

const formatPrice = computed(() => {
  return (price) => {
    if (typeof price === 'string') {
      return parseFloat(price).toFixed(2);
    }
    return price?.toFixed(2) || '0.00';
  };
});

const filteredProducts = computed(() => {
  let result = [...products.value];

  if (selectedCate.value !== 'all') {
    result = result.filter(product => product.category === selectedCate.value);
  }

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim();
    result = result.filter(product =>
      (product.name && product.name.toLowerCase().includes(keyword)) ||
      (product.description && product.description.toLowerCase().includes(keyword)) ||
      (product.brand && product.brand.toLowerCase().includes(keyword))
    );
  }

  if (sortType.value === 'price_asc') {
    result.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
  } else if (sortType.value === 'price_desc') {
    result.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
  }

  return result;
});

const onFilterChange = () => {
  currentPage.value = 1;
};

const onSearch = () => {
  currentPage.value = 1;
};

const onSearchClear = () => {
  searchKeyword.value = '';
  currentPage.value = 1;
};

const handleCategoryChange = (value) => {
  if (value && value.length > 0) {
    selectedCate.value = value[value.length - 1];
  } else {
    selectedCate.value = 'all';
  }
  onFilterChange();
};

const buildCategoryTree = (categories) => {
  const map = {};
  const tree = [];

  categories.forEach(category => {
    map[category.code] = {
      ...category,
      children: []
    };
  });

  categories.forEach(category => {
    if (!category.parentId) {
      tree.push(map[category.code]);
    } else {
      for (const key in map) {
        if (map[key].id === category.parentId) {
          map[key].children.push(map[category.code]);
          break;
        }
      }
    }
  });

  return tree;
};

const resetFilter = () => {
  selectedCate.value = 'all';
  categoryPath.value = [];
  sortType.value = 'default';
  searchKeyword.value = '';
  currentPage.value = 1;
  loadProducts();
  ElMessage.success('筛选条件已重置');
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const handleImageError = (event) => {
  event.target.src = defaultImage;
};

const loadCategories = async () => {
  try {
    const response = await fetch('http://localhost:8083/catcatecutomer/product-categories/list');

    if (response.ok) {
      const result = await response.json();
      if (result.code === 200 && Array.isArray(result.data)) {
        const petSuppliesCategory = result.data.find(cat => cat.code === PET_SUPPLIES_CODE);
        
        if (petSuppliesCategory) {
          // 获取宠物用品分类及其所有层级的子分类
          const getChildCategories = (parentId) => {
            const children = result.data.filter(cat => cat.parentId === parentId && cat.isActive === 1);
            const allChildren = [...children];
            children.forEach(child => {
              const grandChildren = getChildCategories(child.id);
              allChildren.push(...grandChildren);
            });
            return allChildren;
          };
          
          // 获取宠物用品主分类
          const mainCategory = result.data.filter(cat => cat.id === petSuppliesCategory.id && cat.isActive === 1);
          // 获取所有层级的子分类
          const allSubCategories = getChildCategories(petSuppliesCategory.id);
          
          // 合并主分类和所有子分类
          const petCategories = [...mainCategory, ...allSubCategories];
          
          productCategories.value = petCategories;
          categoryTree.value = buildCategoryTree(petCategories);
        } else {
          productCategories.value = getDefaultCategories();
          categoryTree.value = buildCategoryTree(productCategories.value);
        }
      } else {
        productCategories.value = getDefaultCategories();
        categoryTree.value = buildCategoryTree(productCategories.value);
      }
    } else {
      throw new Error('HTTP Error: ' + response.status);
    }
  } catch (error) {
    console.error('获取商品分类失败:', error);
    productCategories.value = getDefaultCategories();
    categoryTree.value = buildCategoryTree(productCategories.value);
  }
};

const getDefaultCategories = () => {
  return [
    { id: 1, name: '猫咪食品', code: 'FOOD', parentId: null, sortOrder: 1, isActive: 1 },
    { id: 2, name: '猫咪用品', code: 'SUPPLIES', parentId: null, sortOrder: 2, isActive: 1 },
    { id: 3, name: '猫咪零食', code: 'MAOTIAO', parentId: null, sortOrder: 3, isActive: 1 },
    { id: 4, name: '健康护理', code: 'HEALTH', parentId: null, sortOrder: 4, isActive: 1 }
  ];
};

const loadProducts = async () => {
  try {
    loading.value = true;

    const response = await fetch('http://localhost:8083/catcatecutomer/products/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        pageNum: 1,
        pageSize: 9999,
        status: 'ON_SALE'
      })
    });

    if (response.ok) {
      const result = await response.json();

      if (result.code === 200 && result.data) {
        const data = result.data;
        if (data.records) {
          // 获取所有宠物用品相关分类的代码（包括所有层级的子分类）
          const petCategoryCodes = productCategories.value.map(cat => cat.code);
          const petProducts = data.records.filter(product => petCategoryCodes.includes(product.category));
          
          products.value = petProducts.map(product => ({
            ...product,
            price: parseFloat(product.price),
            stockQuantity: parseInt(product.stockQuantity) || 0,
            salesCount: parseInt(product.salesCount) || 0
          }));
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

const getMockProducts = () => {
  return [
    {
      id: 1,
      name: '进口无谷猫粮',
      category: 'FOOD',
      description: '天然无谷配方，含鸡肉三文鱼，适合全年龄段猫咪',
      price: 129.00,
      stockQuantity: 50,
      imageUrl: 'https://picsum.photos/seed/catfood1/300/300',
      brand: '皇家宠物',
      salesCount: 328
    },
    {
      id: 2,
      name: '猫咪豪华猫窝',
      category: 'SUPPLIES',
      description: '柔软舒适，四季通用，保暖透气',
      price: 89.00,
      stockQuantity: 30,
      imageUrl: 'https://picsum.photos/seed/catbed1/300/300',
      brand: '宠物之家',
      salesCount: 89
    },
    {
      id: 3,
      name: '猫咪零食肉条',
      category: 'MAOTIAO',
      description: '美味猫条，营养丰富，猫咪最爱',
      price: 29.90,
      stockQuantity: 100,
      imageUrl: 'https://picsum.photos/seed/cattreat1/300/300',
      brand: '喵趣生活',
      salesCount: 156
    },
    {
      id: 4,
      name: '猫咪营养膏',
      category: 'HEALTH',
      description: '补充营养，增强体质，促进健康',
      price: 45.00,
      stockQuantity: 60,
      imageUrl: 'https://picsum.photos/seed/cathealth1/300/300',
      brand: '美毛专家',
      salesCount: 72
    },
    {
      id: 5,
      name: '猫咪美容梳子',
      category: 'SUPPLIES',
      description: '专业脱毛梳，去除浮毛，促进血液循环',
      price: 35.00,
      stockQuantity: 80,
      imageUrl: 'https://picsum.photos/seed/catgroom1/300/300',
      brand: '宠物用品',
      salesCount: 234
    },
    {
      id: 6,
      name: '猫咪抓板',
      category: 'SUPPLIES',
      description: '瓦楞纸材质，耐磨耐用，保护家具',
      price: 25.00,
      stockQuantity: 80,
      imageUrl: 'https://picsum.photos/seed/catscratch1/300/300',
      brand: '宠物用品',
      salesCount: 234
    }
  ];
};

const loadData = async () => {
  await Promise.all([
    loadCategories(),
    loadProducts()
  ]);
};

onMounted(() => {
  loadData();
});

watch([selectedCate, sortType, searchKeyword], () => {
  currentPage.value = 1;
}, { deep: true });
</script>

<style scoped>
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

.result-tip {
  font-size: 14px;
  color: #795548;
  margin-bottom: 20px;
}

.tip-count {
  color: #e65100;
  font-weight: 600;
}

.loading-container {
  padding: 20px 0;
}

.skeleton-card {
  border-radius: 15px;
  overflow: hidden;
  background-color: white;
}

.products-empty {
  padding: 60px 0;
}

.section {
  margin-bottom: 60px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

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

.product-img-container {
  position: relative;
  height: 200px;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

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