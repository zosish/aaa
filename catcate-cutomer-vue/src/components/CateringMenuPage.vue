<template>
  <Layout>
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item @click="$router.push('/home')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>店内餐饮</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>店内餐饮</h1>
      <p>现磨咖啡，可口蛋糕，与猫咪共度美好时光</p>
    </div>

    <!-- 筛选区域 -->
    <section class="filter-section">
      <div class="filter-content">
        <!-- 分类筛选 -->
        <div class="filter-item">
          <label class="filter-label">分类</label>
          <el-select v-model="selectedCategory" placeholder="全部分类" class="filter-select" @change="onFilterChange">
            <el-option label="全部" value="all" />
            <el-option label="饮品" value="YINPIN" />
            <el-option label="小吃" value="XIAOCHI" />
          </el-select>
        </div>

        <!-- 排序 -->
        <div class="filter-item">
          <label class="filter-label">排序</label>
          <el-select v-model="sortType" placeholder="默认排序" class="filter-select" @change="onFilterChange">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="销量优先" value="sales_desc" />
          </el-select>
        </div>

        <!-- 搜索框 -->
        <div class="filter-item search-item">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索餐饮商品..." 
            class="search-input" 
            clearable
            @keyup.enter="onSearch"
          >
            <template #prefix>
              <el-icon class="el-input__icon"><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </section>

    <!-- 统计信息 -->
    <div class="result-tip">
      共找到 <span class="tip-count">{{ totalProducts }}</span> 件餐饮商品
    </div>

    <!-- 餐饮展示区域 -->
    <section class="catering-section">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-spin size="large" />
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredProducts.length === 0" class="empty-state">
        <el-empty description="暂无店内餐饮商品" />
        <el-button type="primary" @click="loadProducts" class="reload-btn">重新加载</el-button>
      </div>

      <!-- 商品网格 -->
      <div v-else class="catering-grid">
        <el-card 
          v-for="product in paginatedProducts" 
          :key="product.id" 
          class="catering-card" 
          shadow="hover"
          @click="goToProductDetail(product.id)"
        >
          <div class="catering-img-container">
            <img 
              :src="product.imageUrl || 'https://picsum.photos/seed/food/300/300'" 
              :alt="product.name" 
              class="catering-img"
              @error="handleImageError"
            />
            <!-- 标签 -->
            <div class="catering-badges">
              <el-tag type="danger" v-if="product.isHot">热卖</el-tag>
              <el-tag type="success" v-if="product.isNew">新品</el-tag>
            </div>
            <!-- 库存标签 -->
            <div class="stock-badge" :class="{ low: product.stockQuantity <= 5, out: product.stockQuantity <= 0 }" v-if="product.stockQuantity <= 10">
              {{ product.stockQuantity <= 0 ? '已售罄' : `仅剩${product.stockQuantity}份` }}
            </div>
          </div>

          <div class="catering-info">
            <h3 class="catering-name">{{ product.name }}</h3>
            <p class="catering-desc">{{ product.description || '暂无描述' }}</p>
            <div class="catering-meta">
              <span class="catering-category">{{ getCategoryName(product.category) }}</span>
              <span class="catering-sales">已售{{ product.salesCount }}份</span>
            </div>
            <div class="catering-price-row">
              <span class="catering-price">¥{{ product.price.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-container" v-if="totalProducts > 0">
        <el-pagination 
          v-model:current-page="currentPage" 
          v-model:page-size="pageSize" 
          :page-sizes="[6, 12, 18, 24]"
          :total="totalProducts" 
          layout="total, sizes, prev, pager, next, jumper" 
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" 
          background 
          class="custom-pagination" 
        />
      </div>
    </section>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElIcon } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import Layout from './AppLayout.vue'

const router = useRouter()

// 状态管理
const loading = ref(true)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const selectedCategory = ref('all')
const sortType = ref('default')
const searchKeyword = ref('')

// 分类映射
const categoryMap = {
  YINPIN: '饮品',
  XIAOCHI: '小吃',
  DIANNEICANYIN: '店内餐饮'
}

// 获取分类名称
const getCategoryName = (categoryCode) => {
  return categoryMap[categoryCode] || categoryCode
}

// 计算属性：筛选后的商品
const filteredProducts = computed(() => {
  let result = [...products.value]

  // 分类筛选
  if (selectedCategory.value !== 'all') {
    result = result.filter(product => product.category === selectedCategory.value)
  }

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    result = result.filter(product =>
      (product.name && product.name.toLowerCase().includes(keyword)) ||
      (product.description && product.description.toLowerCase().includes(keyword))
    )
  }

  // 排序
  if (sortType.value === 'price_asc') {
    result.sort((a, b) => a.price - b.price)
  } else if (sortType.value === 'price_desc') {
    result.sort((a, b) => b.price - a.price)
  } else if (sortType.value === 'sales_desc') {
    result.sort((a, b) => b.salesCount - a.salesCount)
  }

  return result
})

// 计算属性：总商品数
const totalProducts = computed(() => filteredProducts.value.length)

// 计算属性：当前页商品
const paginatedProducts = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return filteredProducts.value.slice(startIndex, endIndex)
})

// 加载商品数据
const loadProducts = async () => {
  loading.value = true
  try {
    const response = await fetch('http://localhost:8083/catcatecutomer/products/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        pageNum: 1,
        pageSize: 999,
        status: 'ON_SALE'
      })
    })

    if (response.ok) {
      const result = await response.json()
      
      if (result.code === 200 && result.data && result.data.records) {
        const diningCategories = ['YINPIN', 'XIAOCHI', 'DIANNEICANYIN']
        const filteredRecords = result.data.records.filter(product => 
          diningCategories.includes(product.category)
        )
        
        const now = new Date()
        products.value = filteredRecords.map(product => ({
          ...product,
          price: parseFloat(product.price),
          stockQuantity: parseInt(product.stockQuantity) || 0,
          salesCount: parseInt(product.salesCount) || 0,
          isHot: (parseInt(product.salesCount) || 0) > 50,
          isNew: product.createdAt && new Date(product.createdAt) > new Date(now - 30 * 24 * 60 * 60 * 1000)
        }))
      }
    } else {
      console.error('API请求失败')
      ElMessage.error('获取餐饮数据失败')
    }
  } catch (error) {
    console.error('获取商品数据失败:', error)
    ElMessage.error('获取餐饮数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 跳转到商品详情
const goToProductDetail = (productId) => {
  router.push('/product/' + productId)
}


// 图片错误处理
const handleImageError = (event) => {
  event.target.src = 'https://picsum.photos/seed/food/300/300'
}

// 筛选条件变化
const onFilterChange = () => {
  currentPage.value = 1
}

const onSearch = () => {
  currentPage.value = 1
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听筛选条件变化
watch([selectedCategory, sortType, searchKeyword], () => {
  currentPage.value = 1
})

// 生命周期
onMounted(async () => {
  await loadProducts()
})
</script>

<style scoped>
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

.breadcrumb {
  --el-breadcrumb-item-color: #795548;
  --el-breadcrumb-separator-color: #795548;
}

/* 筛选区域 */
.filter-section {
  background-color: #fff;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.filter-content {
  display: flex;
  align-items: center;
  gap: 30px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item.search-item {
  margin-left: auto;
}

.filter-label {
  font-size: 14px;
  color: #5d4037;
  font-weight: 500;
}

.filter-select {
  width: 150px;
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
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

/* 空状态 */
.empty-state {
  padding: 60px 0;
  text-align: center;
}

.reload-btn {
  margin-top: 20px;
}

/* 餐饮展示区域 */
.catering-section {
  margin-bottom: 40px;
}

/* 商品网格 */
.catering-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

/* 商品卡片 */
.catering-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  background-color: white;
}

.catering-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(255, 167, 71, 0.15);
}

/* 图片容器 */
.catering-img-container {
  position: relative;
  height: 200px;
}

.catering-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 标签 */
.catering-badges {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 5px;
}

.catering-badges :deep(.el-tag) {
  font-size: 12px;
  padding: 2px 8px;
}

/* 库存标签 */
.stock-badge {
  position: absolute;
  bottom: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  background-color: #fff3e0;
  color: #e65100;
}

.stock-badge.out {
  background-color: #ffebee;
  color: #e53935;
}

/* 商品信息 */
.catering-info {
  padding: 20px;
}

.catering-name {
  font-size: 18px;
  color: #5d4037;
  margin: 0 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.catering-desc {
  font-size: 14px;
  color: #795548;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.catering-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 12px;
}

.catering-category {
  background-color: #fff3e0;
  color: #e65100;
  padding: 2px 8px;
  border-radius: 10px;
}

.catering-sales {
  color: #795548;
}

/* 价格区域 */
.catering-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.catering-price {
  font-size: 24px;
  font-weight: bold;
  color: #e65c00;
}

/* 分页 */
.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.custom-pagination :deep(.el-pagination__total) {
  color: #795548;
}

.custom-pagination :deep(.el-pagination__sizes) {
  margin: 0 15px;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background-color: #e65100;
  border-color: #e65100;
}

/* 响应式 */
@media (max-width: 768px) {
  .catering-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-item.search-item {
    margin-left: 0;
  }
  
  .search-input {
    width: 100%;
  }
}
</style>