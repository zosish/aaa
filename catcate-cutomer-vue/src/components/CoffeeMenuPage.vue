<!-- 不用 -->

<template>
  <Layout>
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item @click="$router.push('/home')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>咖啡菜单</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>精选咖啡</h1>
      <p>现磨咖啡，香醇可口，与猫咪共度美好时光</p>
    </div>

    <!-- 咖啡分类筛选 -->
    <section class="filter-section">
      <div class="filter-content">
        <div class="filter-tabs">
          <el-button 
            v-for="tab in coffeeCategories" 
            :key="tab.code"
            :type="selectedCategory === tab.code ? 'primary' : 'default'"
            @click="selectedCategory = tab.code"
          >
            {{ tab.name }}
          </el-button>
        </div>
      </div>
    </section>

    <!-- 咖啡展示区域 -->
    <section class="coffee-section">
      <div v-if="loading" class="loading-container">
        <el-spin size="large" />
      </div>
      <div v-else-if="filteredCoffees.length === 0" class="empty-state">
        <el-empty description="暂无咖啡商品">
          <el-button type="primary" @click="loadCoffees">重新加载</el-button>
        </el-empty>
      </div>
      <div v-else class="coffee-grid">
        <el-card 
          v-for="coffee in filteredCoffees" 
          :key="coffee.id" 
          class="coffee-card" 
          shadow="hover"
          @click="goToProductDetail(coffee.id)"
        >
          <div class="coffee-img-container">
            <img :src="coffee.imageUrl" :alt="coffee.name" class="coffee-img">
            <div class="coffee-badge" v-if="coffee.isHot">
              <el-tag type="danger">热卖</el-tag>
            </div>
          </div>
          <div class="coffee-info">
            <h3>{{ coffee.name }}</h3>
            <p class="coffee-desc">{{ coffee.description }}</p>
            <div class="coffee-specs">
              <span class="spec-item">{{ coffee.specifications }}</span>
            </div>
            <div class="coffee-price-row">
              <span class="coffee-price">¥{{ coffee.price.toFixed(2) }}</span>
              <el-button 
                type="primary" 
                size="small" 
                icon="Plus"
                @click.stop="addToCart(coffee)"
              >
                加入购物车
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </section>

    <!-- 推荐搭配区域 -->
    <section class="recommend-section">
      <div class="section-header">
        <h2>推荐搭配</h2>
        <p>咖啡与甜点的完美组合</p>
      </div>
      <div class="combo-list">
        <el-card v-for="combo in recommendCombos" :key="combo.id" class="combo-card">
          <div class="combo-content">
            <div class="combo-items">
              <div v-for="(item, index) in combo.items" :key="index" class="combo-item">
                <img :src="item.image" :alt="item.name" class="combo-item-img">
                <span class="combo-item-name">{{ item.name }}</span>
              </div>
              <span class="combo-plus">+</span>
            </div>
            <div class="combo-price">
              <span class="original-price">¥{{ combo.originalPrice.toFixed(2) }}</span>
              <span class="combo-price-tag">¥{{ combo.price.toFixed(2) }}</span>
            </div>
            <el-button type="success" @click="addComboToCart(combo)">
              立即购买
            </el-button>
          </div>
        </el-card>
      </div>
    </section>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from './AppLayout.vue'
import { api } from '@/utils/api'

const router = useRouter()

// 加载状态
const loading = ref(true)

// 咖啡分类
const coffeeCategories = ref([
  { code: 'all', name: '全部' }
])

const selectedCategory = ref('all')

// 咖啡列表
const coffees = ref([])

// 推荐搭配（暂时使用模拟数据）
const recommendCombos = ref([
  {
    id: 1,
    items: [
      { name: '经典拿铁', image: 'https://picsum.photos/seed/coffee1/100/100' },
      { name: '芝士蛋糕', image: 'https://picsum.photos/seed/cake1/100/100' }
    ],
    originalPrice: 58.00,
    price: 48.00
  },
  {
    id: 2,
    items: [
      { name: '焦糖玛奇朵', image: 'https://picsum.photos/seed/coffee2/100/100' },
      { name: '提拉米苏', image: 'https://picsum.photos/seed/cake2/100/100' }
    ],
    originalPrice: 62.00,
    price: 52.00
  }
])

// 筛选后的咖啡列表
const filteredCoffees = computed(() => {
  if (selectedCategory.value === 'all') {
    return coffees.value
  }
  return coffees.value.filter(coffee => coffee.category === selectedCategory.value)
})

// 加载咖啡数据
const loadCoffees = async () => {
  try {
    loading.value = true
    
    // 获取所有上架商品
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
      console.log('商品API返回:', result)
      
      if (result.code === 200 && result.data && result.data.records) {
        // 筛选出店内餐饮分类下的商品（饮品、小吃等）
        const diningCategories = ['YINPIN', 'XIAOCHI', 'DIANNEICANYIN']
        const filteredRecords = result.data.records.filter(product => 
          diningCategories.includes(product.category)
        )
        console.log('筛选后的店内餐饮商品:', filteredRecords)
        
        if (filteredRecords.length > 0) {
          coffees.value = filteredRecords.map(product => ({
            ...product,
            price: parseFloat(product.price),
            stockQuantity: parseInt(product.stockQuantity) || 0,
            isHot: (parseInt(product.salesCount) || 0) > 50 // 销量超过50视为热卖
          }))
        } else {
          // 如果没有店内餐饮分类数据，使用默认数据
          loadDefaultCoffees()
        }
      } else {
        loadDefaultCoffees()
      }
    } else {
      // API调用失败，使用默认数据
      loadDefaultCoffees()
    }
  } catch (error) {
    console.error('获取咖啡数据失败:', error)
    loadDefaultCoffees()
  } finally {
    loading.value = false
  }
}

// 加载默认咖啡数据（备用）
const loadDefaultCoffees = () => {
  coffees.value = [
    {
      id: 101,
      name: '经典拿铁',
      description: '浓缩咖啡与绵密奶泡的完美融合',
      price: 28.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/coffee1/300/300',
      category: 'LATTE',
      isHot: true,
      stockQuantity: 100,
      salesCount: 120
    },
    {
      id: 102,
      name: '焦糖玛奇朵',
      description: '香甜焦糖搭配丝滑拿铁',
      price: 32.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/coffee2/300/300',
      category: 'LATTE',
      isHot: true,
      stockQuantity: 80,
      salesCount: 85
    },
    {
      id: 103,
      name: '卡布奇诺',
      description: '浓郁咖啡与丰富奶泡的经典组合',
      price: 26.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/coffee3/300/300',
      category: 'CAPPUCCINO',
      isHot: false,
      stockQuantity: 120,
      salesCount: 45
    },
    {
      id: 104,
      name: '美式咖啡',
      description: '纯正浓缩加水，口感清爽',
      price: 22.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/coffee4/300/300',
      category: 'AMERICANO',
      isHot: false,
      stockQuantity: 150,
      salesCount: 60
    },
    {
      id: 105,
      name: '摩卡咖啡',
      description: '咖啡、巧克力与牛奶的甜蜜邂逅',
      price: 30.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/coffee5/300/300',
      category: 'MOCHA',
      isHot: true,
      stockQuantity: 90,
      salesCount: 95
    },
    {
      id: 106,
      name: '榛果拿铁',
      description: '榛果香气与拿铁的完美结合',
      price: 30.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/coffee6/300/300',
      category: 'LATTE',
      isHot: false,
      stockQuantity: 70,
      salesCount: 35
    },
    {
      id: 107,
      name: '抹茶拿铁',
      description: '日式抹茶与牛奶的清新组合',
      price: 32.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/coffee7/300/300',
      category: 'SPECIALTY',
      isHot: false,
      stockQuantity: 60,
      salesCount: 40
    },
    {
      id: 108,
      name: '蜂蜜柚子茶',
      description: '清新柚子与蜂蜜的甜蜜滋味',
      price: 22.00,
      specifications: '中杯/大杯',
      imageUrl: 'https://picsum.photos/seed/tea1/300/300',
      category: 'SPECIALTY',
      isHot: false,
      stockQuantity: 100,
      salesCount: 55
    }
  ]
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const response = await fetch('http://localhost:8083/catcatecutomer/product-categories/list')
    if (response.ok) {
      const result = await response.json()
      console.log('分类API返回:', result)
      
      if (result.code === 200 && Array.isArray(result.data)) {
        // 找到店内餐饮分类及其子分类
        const diningCategory = result.data.find(cat => cat.code === 'DIANNEICANYIN')
        
        if (diningCategory) {
          const diningSubCategories = result.data.filter(cat => cat.parentId === diningCategory.id)
          coffeeCategories.value = [
            { code: 'all', name: '全部' },
            ...diningSubCategories.map(cat => ({
              code: cat.code,
              name: cat.name
            }))
          ]
        } else {
          // 使用默认分类
          coffeeCategories.value = [
            { code: 'all', name: '全部' },
            { code: 'YINPIN', name: '饮品' },
            { code: 'XIAOCHI', name: '小吃' }
          ]
        }
      }
    }
  } catch (error) {
    console.error('获取分类数据失败:', error)
    // 使用默认分类
    coffeeCategories.value = [
      { code: 'all', name: '全部' },
      { code: 'YINPIN', name: '饮品' },
      { code: 'XIAOCHI', name: '小吃' }
    ]
  }
}

// 跳转到商品详情
const goToProductDetail = (productId) => {
  router.push(`/product/${productId}`)
}

// 加入购物车
const addToCart = async (coffee) => {
  try {
    const response = await api.post('/cart/add', {
      productId: coffee.id,
      quantity: 1
    })
    if (response.code === 200) {
      ElMessage.success('已加入购物车')
    } else {
      ElMessage.error(response.message || '加入购物车失败')
    }
  } catch (error) {
    ElMessage.error('加入购物车失败：' + error.message)
  }
}

// 添加套餐到购物车
const addComboToCart = (combo) => {
  ElMessage.success('套餐已加入购物车')
}

// 生命周期
onMounted(async () => {
  await loadCategories()
  await loadCoffees()
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

/* 筛选区域 */
.filter-section {
  background-color: #fff;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.filter-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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

/* 咖啡展示区域 */
.coffee-section {
  margin-bottom: 40px;
}

.coffee-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

.coffee-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  background-color: white;
}

.coffee-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(255, 167, 71, 0.15);
}

.coffee-img-container {
  position: relative;
  height: 200px;
}

.coffee-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.coffee-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.coffee-info {
  padding: 20px;
}

.coffee-info h3 {
  font-size: 18px;
  color: #5d4037;
  margin: 0 0 10px;
}

.coffee-desc {
  font-size: 14px;
  color: #795548;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.coffee-specs {
  margin-bottom: 15px;
}

.spec-item {
  display: inline-block;
  background-color: #f5f5f5;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.coffee-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coffee-price {
  font-size: 24px;
  font-weight: bold;
  color: #e65c00;
}

/* 推荐搭配区域 */
.recommend-section {
  margin-bottom: 40px;
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 24px;
  color: #5d4037;
  margin: 0 0 10px;
}

.section-header p {
  font-size: 14px;
  color: #795548;
  margin: 0;
}

.combo-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.combo-card {
  border-radius: 15px;
  overflow: hidden;
}

.combo-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.combo-items {
  display: flex;
  align-items: center;
  gap: 10px;
}

.combo-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.combo-item-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 50%;
}

.combo-item-name {
  font-size: 12px;
  margin-top: 5px;
}

.combo-plus {
  font-size: 24px;
  color: #ccc;
}

.combo-price {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.combo-price-tag {
  font-size: 24px;
  font-weight: bold;
  color: #e65c00;
}

@media (max-width: 768px) {
  .coffee-grid {
    grid-template-columns: 1fr;
  }
  
  .combo-list {
    grid-template-columns: 1fr;
  }
  
  .combo-content {
    flex-direction: column;
    text-align: center;
  }
}
</style>