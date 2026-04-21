<template>
  <Layout>
    <!-- 轮播图 -->
    <el-carousel height="400px" class="hero-carousel" trigger="click">
      <el-carousel-item>
        <img src="https://picsum.photos/seed/catcafe1/1200/400" alt="猫咖环境" class="carousel-img">
        <div class="carousel-caption">
          <h2>欢迎来到喵时光</h2>
          <p>与可爱猫咪共度美好时光</p>
          <el-button type="primary" @click="$router.push('/ReservationActivityPage')">立即预约</el-button>
        </div>
      </el-carousel-item>
      <el-carousel-item>
        <img src="https://picsum.photos/seed/catcafe2/1200/400" alt="猫咪互动" class="carousel-img">
        <div class="carousel-caption">
          <h2>新成员加入</h2>
          <p>布偶猫"雪球"等你来撸</p>
          <el-button type="primary" @click="$router.push('/CatGuidePage')">认识猫咪</el-button>
        </div>
      </el-carousel-item>
      <el-carousel-item>
        <img src="https://picsum.photos/seed/catcafe3/1200/400" alt="特色饮品" class="carousel-img">
        <div class="carousel-caption">
          <h2>夏日特饮上市</h2>
          <p>购买任意饮品送猫咪小零食</p>
          <el-button type="primary" @click="$router.push('/ProductPage')">查看商品</el-button>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 猫咪展示区 -->
    <section class="section cats-section">
      <div class="section-header">
        <h2>人气猫咪</h2>
        <el-button type="text" @click="$router.push('/CatGuidePage')">查看全部 <el-icon>
            <ArrowRight />
          </el-icon></el-button>
      </div>

      <div class="cats-grid">
        <el-card v-for="cat in featuredCats" :key="cat.id" class="cat-card" shadow="hover">
          <div class="cat-img-container">
            <img :src="getProcessedCatImageUrl(cat.photoUrl)" :alt="cat.name" class="cat-img"
              v-if="cat.photoUrl && isValidImageUrl(cat.photoUrl)">
            <div v-else class="cat-no-image">
              <el-icon size="30" color="#ccc">
                <Picture />
              </el-icon>
              <p>暂无图片</p>
            </div>
            <div class="cat-tag">{{ cat.breed }}</div>
          </div>
          <div class="cat-info">
            <h3>{{ cat.name }}</h3>
            <div class="cat-meta">
              <span>年龄: {{ formatAge(cat.age) }}</span>
              <span>性格: {{ cat.personality }}</span>
            </div>
            <el-rate :value="cat.rating" disabled :max="5" :show-text="false" class="cat-rating"></el-rate>
          </div>
        </el-card>
      </div>
    </section>

    <!-- 预约区域 -->
    <section class="section reservation-section">
      <div class="section-content">
        <div class="reservation-text">
          <h2>预约撸猫时段</h2>
          <p>为了给您和猫咪更好的体验，建议提前预约到店时间。每个时段限制人数，确保猫咪不会过度疲劳。</p>
          <ul class="reservation-features">
            <li><el-icon>
                <Check />
              </el-icon> 专业人员陪同</li>
            <li><el-icon>
                <Check />
              </el-icon> 免费饮品一杯</li>
            <!-- <li><el-icon><Check /></el-icon> 猫咪零食一份</li> -->
            <li><el-icon>
                <Check />
              </el-icon> 拍照指导服务</li>
          </ul>
          <el-button type="primary" size="large" class="reservation-btn"
            @click="$router.push('/ReservationActivityPage')">
            立即预约
          </el-button>
        </div>
        <div class="reservation-image">
          <img src="https://picsum.photos/seed/catplay/600/400" alt="预约撸猫" class="section-img">
        </div>
      </div>
    </section>

    <!-- 商品区域 -->
    <section class="section products-section">
      <div class="section-header">
        <h2>精选商品</h2>
        <el-button type="text" @click="$router.push('/ProductPage')">查看全部 <el-icon>
            <ArrowRight />
          </el-icon></el-button>
      </div>

      <div class="products-grid">
        <el-card v-for="product in featuredProducts" :key="product.id" class="product-card" shadow="hover"
          @click="goToProductDetail(product.id)">
          <div class="product-img-container">
            <img :src="product.imageUrl" :alt="product.name" class="product-img">
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
          </div>
        </el-card>
      </div>
    </section>

    <!-- 活动区域 -->
    <section class="section activities-section">
      <div class="section-header">
        <h2>近期活动</h2>
        <el-button type="text" @click="$router.push('/activities')">查看全部 <el-icon>
            <ArrowRight />
          </el-icon></el-button>
      </div>

      <div class="activities-list">
        <el-card v-for="activity in currentActivities" :key="activity.id" class="activity-card" shadow="hover">
          <div class="activity-content">
            <div class="activity-img-container">
              <img :src="activity.coverImage" :alt="activity.title" class="activity-img">
            </div>
            <div class="activity-info">
              <div class="activity-date">{{ formatDateRange(activity.startTime, activity.endTime) }}</div>
              <h3>{{ activity.title }}</h3>
              <p class="activity-desc">{{ activity.content }}</p>
              <!-- <el-button type="text" class="activity-details"
                @click="$router.push(`/activities/${activity.id}`)">了解详情</el-button> -->
            </div>
          </div>
        </el-card>
      </div>
    </section>

    <!-- 顾客评价 -->
    <section class="section reviews-section">
      <div class="section-header">
        <h2>顾客评价</h2>
      </div>

      <div class="reviews-slider">
        <el-carousel type="card" :interval="5000" class="reviews-carousel">
          <el-carousel-item v-for="review in customerReviews" :key="review.id">
            <div class="review-card">
              <el-rate :model-value="review.rating" disabled :max="5" class="review-rating"></el-rate>
              <p class="review-content">{{ review.content }}</p>
              <div class="review-author">
                <el-avatar :size="40" :src="review.userAvatar || defaultAvatar"></el-avatar>
                <div class="author-info">
                  <div class="author-name">{{ review.isAnonymous ? '匿名用户' : review.username }}</div>
                  <div class="review-date">{{ formatDate(review.createTime) }}</div>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>
  </Layout>
</template>
<!-- eslint-disable no-unused-vars -->
<!--  eslint-disable no-undef  -->
<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowRight, Check
} from '@element-plus/icons-vue';
import { api } from '../utils/api';
import Layout from './Layout.vue';

// 路由
const router = useRouter();

// 状态管理
const defaultAvatar = 'https://picsum.photos/seed/user/100/100';

// 特色猫咪数据（关联cats表）- 修改为从API获取热门猫咪
const featuredCats = ref([]);

// 精选商品数据（关联products表）- 修改为从API获取热销商品
const featuredProducts = ref([]);

// 近期活动数据（关联activities表）- 修改为从API获取最新活动
const currentActivities = ref([]);

// 顾客评价数据（关联reviews表）
const customerReviews = ref([]);


// 生命周期
onMounted(async () => {
  // 获取热门猫咪数据
  loadPopularCats();
  // 获取热销商品数据
  loadBestSellingProducts();
  // 获取最新活动数据
  loadRecentActivities();
  // 获取最新顾客评价
  loadLatestReviews();
});

// 图片URL处理函数
const getProcessedCatImageUrl = (url) => {
  if (!url) {
    return null;
  }

  // 如果已经是完整URL，直接返回
  if (url.startsWith('http')) {
    return url;
  }

  // 处理相对路径
  if (url.startsWith('/')) {
    return `http://localhost:8081${url}`;
  } else if (url.startsWith('uploads/')) {
    return `http://localhost:8081/${url}`;
  } else {
    return `http://localhost:8081/${url}`;
  }
};

// 验证图片URL是否有效
const isValidImageUrl = (url) => {
  if (!url) return false;
  // 简单验证URL格式
  return url.startsWith('http') || url.startsWith('/') || url.startsWith('uploads/');
};

// 修改获取热门猫咪函数，移除默认数据
const loadPopularCats = async () => {
  try {
    console.log('开始获取热门猫咪数据...');
    const response = await fetch('http://localhost:8083/catcatecutomer/cats/popular?limit=3');
    console.log('热门猫咪API响应状态:', response.status);

    const result = await response.json();
    console.log('热门猫咪API响应数据:', result);

    if (result && result.code === 200 && result.data) {
      // 只处理真实的API数据，不添加默认数据
      featuredCats.value = result.data.map(cat => ({
        ...cat,
        photoUrl: getProcessedCatImageUrl(cat.photoUrl)
      }));
      console.log('处理后的热门猫咪数据:', featuredCats.value);
    } else {
      console.log('API调用失败，不使用默认数据');
      featuredCats.value = []; // 清空数组，不显示任何数据
    }
  } catch (error) {
    console.error('获取热门猫咪失败:', error);
    console.log('API调用异常，不使用默认数据');
    featuredCats.value = []; // 清空数组，不显示任何数据
  }
};

// 获取热销商品数据
const loadBestSellingProducts = async () => {
  try {
    const response = await fetch('http://localhost:8083/catcatecutomer/products/best-selling?limit=3');
    const result = await response.json();

    if (result && result.code === 200 && result.data) {
      featuredProducts.value = result.data;
    } else {
      // 如果API调用失败，使用默认数据
      featuredProducts.value = getDefaultProducts();
    }
  } catch (error) {
    console.error('获取热销商品失败:', error);
    // 使用默认数据作为备选
    featuredProducts.value = getDefaultProducts();
  }
};

// 获取最新活动数据
const loadRecentActivities = async () => {
  try {
    const response = await fetch('http://localhost:8083/catcatecutomer/activities/recent?limit=3');
    const result = await response.json();

    if (result && result.code === 200 && result.data) {
      currentActivities.value = result.data;
      console.log('获取最新活动成功:', result.data);
    } else {
      // 如果API调用失败，使用默认数据
      currentActivities.value = getDefaultActivities();
      console.log('使用默认活动数据');
    }
  } catch (error) {
    console.error('获取最新活动失败:', error);
    // 使用默认数据作为备选
    currentActivities.value = getDefaultActivities();
    console.log('使用默认活动数据（异常情况）');
  }
};

// 获取最新顾客评价
const loadLatestReviews = async () => {
  try {
    const response = await api.get('/reviews/latest?limit=6');
    if (response && response.code === 200) {
      customerReviews.value = response.data || [];
    } else {
      // 提示错误
      ElMessage.error('获取最新评价失败，请稍后重试');
    }
  } catch (error) {
    console.error('获取最新评价失败:', error);
    // 提示错误
    ElMessage.error('获取最新评价失败，请稍后重试');
  }
};

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push(`/product/${productId}`);
};

// 格式化年龄（月转年+月）
const formatAge = (months) => {
  if (!months) return '未知';
  const years = Math.floor(months / 12);
  const remainMonths = months % 12;
  let result = '';
  if (years > 0) result += `${years}年`;
  if (remainMonths > 0) result += `${remainMonths}月`;
  return result;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
};

// 格式化日期范围
const formatDateRange = (start, end) => {
  if (!start || !end) return '';
  const startDate = new Date(start);
  const endDate = new Date(end);

  // 同一天
  if (startDate.toDateString() === endDate.toDateString()) {
    return startDate.toLocaleDateString('zh-CN');
  }

  return `${startDate.toLocaleDateString('zh-CN')} - ${endDate.toLocaleDateString('zh-CN')}`;
};
</script>

<style scoped>
/* 轮播图样式 */
.hero-carousel {
  margin-bottom: 40px;
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  text-align: center;
}

.carousel-caption h2 {
  font-size: 36px;
  margin-bottom: 15px;
}

.carousel-caption p {
  font-size: 18px;
  margin-bottom: 25px;
  max-width: 600px;
}

/* 通用区块样式 */
.section {
  margin-bottom: 60px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-header h2 {
  font-size: 24px;
  color: #5d4037;
  margin: 0;
}

/* 猫咪展示区 */
.cats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

.cat-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  background-color: white;
}

.cat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(255, 167, 71, 0.15);
}

.cat-img-container {
  position: relative;
  height: 220px;
}

.cat-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cat-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 255, 255, 0.9);
  color: #e65100;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.cat-info {
  padding: 15px;
}

.cat-info h3 {
  margin: 0 0 10px;
  font-size: 18px;
  color: #5d4037;
}

.cat-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #795548;
  margin-bottom: 10px;
}

.cat-rating {
  --el-rate-icon-size: 16px;
  color: #ffb74d;
}

/* 预约区域 */
.reservation-section {
  background-color: #fff3e0;
  border-radius: 20px;
  padding: 40px;
}

.section-content {
  display: flex;
  align-items: center;
  gap: 40px;
}

.reservation-text {
  flex: 1;
}

.reservation-text h2 {
  font-size: 28px;
  color: #5d4037;
  margin-top: 0;
  margin-bottom: 20px;
}

.reservation-text p {
  font-size: 16px;
  color: #795548;
  margin-bottom: 25px;
  line-height: 1.6;
}

.reservation-features {
  list-style: none;
  padding: 0;
  margin-bottom: 30px;
}

.reservation-features li {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: #5d4037;
}

.reservation-features li .el-icon {
  color: #e65100;
  margin-right: 10px;
}

.reservation-btn {
  background-color: #e65100;
  border-color: #e65100;
  padding: 12px 30px;
  font-size: 16px;
}

.reservation-btn:hover {
  background-color: #d32f2f;
  border-color: #d32f2f;
}

.reservation-image {
  flex: 1;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.section-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品区域 */
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

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px;
  font-size: 17px;
  color: #5d4037;
}

.product-desc {
  font-size: 13px;
  color: #795548;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #e65100;
}

/* 活动区域 */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(255, 167, 71, 0.15);
}

.activity-content {
  display: flex;
  gap: 20px;
  padding: 20px;
}

.activity-img-container {
  width: 220px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
}

.activity-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.activity-card:hover .activity-img {
  transform: scale(1.05);
}

.activity-info {
  flex: 1;
  padding: 5px 0;
  display: flex;
  flex-direction: column;
}

.activity-date {
  display: inline-block;
  background-color: #fff3e0;
  color: #e65100;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 12px;
  align-self: flex-start;
}

.activity-info h3 {
  margin: 0 0 12px;
  font-size: 19px;
  color: #5d4037;
  font-weight: 600;
  line-height: 1.3;
}

.activity-desc {
  font-size: 14px;
  color: #795548;
  margin: 0 0 18px;
  line-height: 1.6;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-details {
  color: #e65100;
  padding: 0;
  font-weight: 500;
  align-self: flex-start;
  margin-top: auto;
}

.activity-details:hover {
  color: #d32f2f;
  text-decoration: underline;
}

/* 顾客评价 */
.reviews-slider {
  padding: 20px 0;
}

.reviews-carousel {
  width: 100%;
}

.review-card {
  background-color: white;
  border-radius: 15px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.review-rating {
  --el-rate-icon-size: 20px;
  color: #ffb74d;
  margin-bottom: 20px;
}

.review-content {
  font-size: 16px;
  color: #5d4037;
  line-height: 1.8;
  margin-bottom: 25px;
  min-height: 80px;
}

.review-author {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.author-info {
  text-align: left;
}

.author-name {
  font-weight: 500;
  color: #5d4037;
}

.review-date {
  font-size: 12px;
  color: #795548;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .section-content {
    flex-direction: column;
  }

  .reservation-image {
    width: 100%;
    max-width: 500px;
  }

  .activity-content {
    flex-direction: column;
  }

  .activity-img-container {
    width: 100%;
    height: 200px;
  }
}

@media (max-width: 768px) {
  .carousel-caption h2 {
    font-size: 28px;
  }

  .carousel-caption p {
    font-size: 16px;
  }

  .section {
    margin-bottom: 40px;
  }

  .reservation-section {
    padding: 25px 20px;
  }

  .activity-content {
    padding: 15px;
  }

  .activity-info h3 {
    font-size: 17px;
  }
}

@media (max-width: 480px) {
  .hero-carousel {
    height: 250px !important;
  }

  .carousel-caption h2 {
    font-size: 22px;
  }

  .carousel-caption p {
    font-size: 14px;
    margin-bottom: 15px;
  }

  .section-header h2 {
    font-size: 20px;
  }

  .reservation-text h2 {
    font-size: 24px;
  }

  .review-card {
    padding: 20px 15px;
  }

  .review-content {
    font-size: 14px;
    min-height: auto;
  }

  .activity-content {
    padding: 12px;
  }

  .activity-info h3 {
    font-size: 16px;
  }

  .activity-desc {
    font-size: 13px;
    -webkit-line-clamp: 2;
  }
}
</style>