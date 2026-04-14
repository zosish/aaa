<!-- 客户端活动页面 -->
<template>
  <div class="client-activities-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>店内活动</h1>
      <p>精彩活动，等你参与</p>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索活动标题或内容..."
        clearable
        class="search-input"
        @keyup.enter="searchActivities"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="searchActivities">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <el-select
        v-model="selectedType"
        placeholder="活动类型"
        clearable
        class="type-select"
        @change="filterActivities"
      >
        <el-option label="全部类型" value=""></el-option>
        <el-option label="折扣活动" value="DISCOUNT"></el-option>
        <el-option label="主题活动" value="EVENT"></el-option>
        <el-option label="促销活动" value="PROMOTION"></el-option>
        <el-option label="套餐活动" value="SETMEAL"></el-option>
      </el-select>
    </div>

    <!-- 活动列表 -->
    <div class="activities-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div class="activity-skeleton" v-for="i in 6" :key="i">
              <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
              <div style="padding: 14px">
                <el-skeleton-item variant="h3" style="width: 80%" />
                <div style="margin-top: 10px">
                  <el-skeleton-item variant="text" style="width: 60%" />
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- 活动卡片列表 -->
      <div v-else class="activities-grid">
        <el-card 
          v-for="activity in activities" 
          :key="activity.id"
          class="activity-card"
          shadow="hover"
          @click="viewActivityDetail(activity)"
        >
          <div class="activity-image-container">
            <img 
              :src="activity.coverImage || defaultImage" 
              :alt="activity.title"
              class="activity-image"
              @error="handleImageError"
            >
            <!-- 推荐标签 -->
            <div v-if="activity.isFeatured === 1" class="featured-tag">
              <el-tag type="warning" effect="dark">推荐</el-tag>
            </div>
            <!-- 活动类型标签 -->
            <div class="activity-type-tag">
              <el-tag :type="getActivityTypeTagType(activity.activityType)">
                {{ formatActivityType(activity.activityType) }}
              </el-tag>
            </div>
            <!-- 套餐价格 -->
            <div v-if="activity.activityType === 'SETMEAL' && activity.setmealPrice" class="setmeal-price">
              ¥{{ activity.setmealPrice }}
            </div>
          </div>
          
          <div class="activity-content">
            <h3 class="activity-title">{{ activity.title || '无标题' }}</h3>
            <p class="activity-description">{{ activity.content || '无内容' }}</p>
            
            <div class="activity-meta">
              <div class="time-info">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatActivityTime(activity.startTime, activity.endTime) }}</span>
              </div>
              <div class="remaining-time" v-if="activity.status === 'ACTIVE'">
                <el-icon><Clock /></el-icon>
                <span>剩余 {{ calculateRemainingTime(activity.endTime) }}</span>
              </div>
            </div>
            
            <div class="activity-stats">
              <el-tag type="info" size="small">
                浏览 {{ activity.viewCount || 0 }} 次
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && activities.length === 0" class="empty-state">
        <el-empty description="暂无活动信息">
          <el-button type="primary" @click="refreshActivities">刷新试试</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 活动详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentActivity?.title"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentActivity" class="activity-detail">
        <div class="detail-image">
          <img :src="currentActivity.coverImage || defaultImage" :alt="currentActivity.title">
        </div>
        
        <div class="detail-content">
          <div class="detail-tags">
            <el-tag :type="getActivityTypeTagType(currentActivity.activityType)" size="small">
              {{ formatActivityType(currentActivity.activityType) }}
            </el-tag>
            <el-tag v-if="currentActivity.isFeatured === 1" type="warning" size="small">
              推荐活动
            </el-tag>
          </div>
          
          <p class="detail-description">{{ currentActivity.content }}</p>
          
          <div class="detail-info">
            <div class="info-item">
              <span class="info-label">活动时间：</span>
              <span class="info-value">{{ formatActivityTime(currentActivity.startTime, currentActivity.endTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">剩余时间：</span>
              <span class="info-value">{{ calculateRemainingTime(currentActivity.endTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">浏览次数：</span>
              <span class="info-value">{{ currentActivity.viewCount || 0 }} 次</span>
            </div>
            <div v-if="currentActivity.activityType === 'SETMEAL' && currentActivity.setmealPrice" class="info-item">
              <span class="info-label">套餐价格：</span>
              <span class="info-value price">¥{{ currentActivity.setmealPrice }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="participateActivity">立即参与</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Clock, Search } from '@element-plus/icons-vue'
import { api } from '@/utils/api'

// 数据响应式变量
const loading = ref(false)
const activities = ref([])
const searchKeyword = ref('')
const selectedType = ref('')
const dialogVisible = ref(false)
const currentActivity = ref(null)

// 默认图片
const defaultImage = 'https://picsum.photos/seed/activity/400/200'

// 生命周期钩子
onMounted(() => {
  loadActivities()
})

// 加载活动列表
const loadActivities = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (selectedType.value) params.activityType = selectedType.value
    
    const result = await api.get('/activities/client/list', { params })
    
    if (result && result.code === 200) {
      activities.value = result.data || []
    } else {
      ElMessage.error(result?.message || '加载活动失败')
    }
  } catch (error) {
    console.error('加载活动失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索活动
const searchActivities = () => {
  loadActivities()
}

// 筛选活动
const filterActivities = () => {
  loadActivities()
}

// 刷新活动
const refreshActivities = () => {
  loadActivities()
}

// 查看活动详情
const viewActivityDetail = async (activity) => {
  try {
    const result = await api.get(`/activities/client/detail/${activity.id}`)
    if (result && result.code === 200) {
      currentActivity.value = result.data
      dialogVisible.value = true
    } else {
      ElMessage.error(result?.message || '获取活动详情失败')
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 参与活动
const participateActivity = () => {
  if (currentActivity.value) {
    ElMessageBox.alert(`您已成功关注"${currentActivity.value.title}"活动！`, '参与成功', {
      confirmButtonText: '确定',
      type: 'success'
    })
    dialogVisible.value = false
  }
}

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = defaultImage
}

// 格式化活动类型
const formatActivityType = (type) => {
  const typeMap = {
    'DISCOUNT': '折扣活动',
    'EVENT': '主题活动',
    'PROMOTION': '促销活动',
    'SETMEAL': '套餐活动'
  }
  return typeMap[type] || '未知类型'
}

// 获取活动类型标签类型
const getActivityTypeTagType = (type) => {
  const typeMap = {
    'DISCOUNT': 'danger',
    'EVENT': 'primary',
    'PROMOTION': 'success',
    'SETMEAL': 'warning'
  }
  return typeMap[type] || 'info'
}

// 格式化活动时间
const formatActivityTime = (startTime, endTime) => {
  if (!startTime || !endTime) return '时间待定'
  
  const start = new Date(startTime)
  const end = new Date(endTime)
  
  const formatDate = (date) => {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
  
  const formatTime = (date) => {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  
  return `${formatDate(start)} ${formatTime(start)} - ${formatDate(end)} ${formatTime(end)}`
}

// 计算剩余时间
const calculateRemainingTime = (endTime) => {
  if (!endTime) return '未知'
  
  const now = new Date()
  const end = new Date(endTime)
  const diff = end.getTime() - now.getTime()
  
  if (diff <= 0) return '已结束'
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}
</script>

<style scoped>
.client-activities-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #666;
}

/* 搜索区域 */
.search-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  justify-content: center;
  flex-wrap: wrap;
}

.search-input {
  width: 400px;
}

.type-select {
  width: 150px;
}

/* 活动容器 */
.activities-container {
  min-height: 400px;
}

/* 加载骨架屏 */
.loading-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.activity-skeleton {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

/* 活动网格 */
.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

/* 活动卡片 */
.activity-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

/* 活动图片容器 */
.activity-image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.activity-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.activity-card:hover .activity-image {
  transform: scale(1.05);
}

/* 标签 */
.featured-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
}

.activity-type-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.setmeal-price {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(255, 159, 67, 0.9);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
}

/* 活动内容 */
.activity-content {
  padding: 16px;
}

.activity-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

/* 活动元信息 */
.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.time-info, .remaining-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #666;
}

.remaining-time {
  color: #ff6b6b;
  font-weight: 500;
}

/* 活动统计 */
.activity-stats {
  display: flex;
  justify-content: flex-end;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 0;
}

/* 弹窗详情 */
.activity-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image {
  text-align: center;
}

.detail-image img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-tags {
  display: flex;
  gap: 10px;
}

.detail-description {
  font-size: 15px;
  line-height: 1.6;
  color: #555;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: 500;
  color: #333;
  width: 80px;
}

.info-value {
  color: #666;
}

.info-value.price {
  color: #ff9f43;
  font-weight: bold;
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .client-activities-page {
    padding: 15px;
  }
  
  .search-section {
    flex-direction: column;
    align-items: center;
  }
  
  .search-input, .type-select {
    width: 100%;
    max-width: 400px;
  }
  
  .activities-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .activity-image-container {
    height: 180px;
  }
}
</style>