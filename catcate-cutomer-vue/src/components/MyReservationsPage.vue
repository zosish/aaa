<template>
  <Layout>
    <div class="my-reservations-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="$router.push('/home')">首页</el-breadcrumb-item>
          <el-breadcrumb-item>我的预约</el-breadcrumb-item>
        </el-breadcrumb>
        <h1>我的预约</h1>
        <p>查看和管理您的猫咪预约记录</p>
      </div>

      <!-- 状态统计卡片 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card" :class="{ active: statusFilter === '' }" @click="filterReservations('')">
              <div class="stat-icon"><CalendarCheck /></div>
              <div class="stat-info">
                <div class="stat-value">{{ totalCount }}</div>
                <div class="stat-label">全部预约</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card pending" :class="{ active: statusFilter === 'pending' }" @click="filterReservations('pending')">
              <div class="stat-icon"><Clock /></div>
              <div class="stat-info">
                <div class="stat-value">{{ pendingCount }}</div>
                <div class="stat-label">待确认</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card confirmed" :class="{ active: statusFilter === 'confirmed' }" @click="filterReservations('confirmed')">
              <div class="stat-icon"><CheckCircle /></div>
              <div class="stat-info">
                <div class="stat-value">{{ confirmedCount }}</div>
                <div class="stat-label">已确认</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card completed" :class="{ active: statusFilter === 'completed' }" @click="filterReservations('completed')">
              <div class="stat-icon"><Check /></div>
              <div class="stat-info">
                <div class="stat-value">{{ completedCount }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-select v-model="statusFilter" placeholder="预约状态" clearable @change="handleFilterChange">
              <el-option label="全部" value=""></el-option>
              <el-option label="待确认" value="pending"></el-option>
              <el-option label="已确认" value="confirmed"></el-option>
              <el-option label="已完成" value="completed"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
            </el-select>
          </el-col>
          <el-col :span="10">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
              end-placeholder="结束日期" @change="handleFilterChange" />
          </el-col>
          <el-col :span="8">
            <div class="filter-actions">
              <el-button @click="resetFilters">重置筛选</el-button>
              <el-button type="primary" @click="$router.push('/appointment')">预约撸猫</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 预约列表 -->
      <div class="reservations-container">
        <div v-if="!loading && reservations.length === 0" class="empty-state">
          <el-empty description="暂无预约记录">
            <template #image>
              <div class="empty-icon">
                <CalendarCheck />
              </div>
            </template>
            <template #bottom>
              <el-button type="primary" @click="$router.push('/appointment')">立即预约</el-button>
            </template>
          </el-empty>
        </div>

        <div v-else>
          <div v-for="reservation in paginatedReservations" :key="reservation.id" class="reservation-card">
            <div class="reservation-header">
              <div class="reservation-info">
                <span class="reservation-id">预约编号：{{ reservation.id }}</span>
                <span class="reservation-date">{{ reservation.reservationDate }}</span>
              </div>
              <el-tag :type="getStatusTagType(reservation.status)" size="small">
                {{ getStatusText(reservation.status) }}
              </el-tag>
            </div>

            <div class="reservation-content">
              <div class="cat-section">
                <img :src="getCatImageUrl(reservation.catPhoto)" class="cat-avatar" :alt="reservation.catName" />
                <div class="cat-info">
                  <div class="cat-name">{{ reservation.catName }}</div>
                  <div class="cat-details">
                    <span v-if="reservation.catBreed" class="detail-item">{{ reservation.catBreed }}</span>
                    <span v-if="reservation.catAge" class="detail-item">{{ reservation.catAge }}岁</span>
                    <span v-if="reservation.catGender" class="detail-item">{{ reservation.catGender === 'MALE' ? '公' : '母' }}</span>
                  </div>
                </div>
              </div>

              <div class="time-section">
                <div class="time-item">
                  <Clock class="time-icon" />
                  <span>{{ reservation.timeSlot }}</span>
                </div>
                <div class="time-item">
                  <Timer class="time-icon" />
                  <span>{{ reservation.duration }}</span>
                </div>
                <div class="time-item">
                  <Users class="time-icon" />
                  <span>{{ reservation.visitorCount }}人</span>
                </div>
              </div>
            </div>

            <div class="reservation-footer">
              <div class="notes-section" v-if="reservation.userNotes">
                <span class="notes-label">备注：</span>
                <span class="notes-content">{{ reservation.userNotes }}</span>
              </div>
              <div class="reservation-actions">
                <el-button size="small" @click="viewReservation(reservation)">查看详情</el-button>
                <el-button v-if="canCancel(reservation)" size="small" type="danger" @click="cancelReservation(reservation)">
                  取消预约
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="totalReservations > 0">
        <el-pagination 
          v-model:current-page="currentPage" 
          v-model:page-size="pageSize" 
          :page-sizes="[10, 20, 50]"
          :total="totalReservations" 
          layout="total, sizes, prev, pager, next, jumper" 
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" 
        />
      </div>

      <!-- 预约详情对话框 -->
      <el-dialog v-model="showDetailDialog" title="预约详情" width="650px" :before-close="handleDetailClose">
        <div v-if="selectedReservation" class="reservation-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预约编号">
              {{ selectedReservation.id }}
            </el-descriptions-item>
            <el-descriptions-item label="预约状态">
              <el-tag :type="getStatusTagType(selectedReservation.status)">
                {{ getStatusText(selectedReservation.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="预约猫咪">
              <div class="cat-info-row">
                <el-avatar :size="48" :src="getCatImageUrl(selectedReservation.catPhoto)" shape="square"></el-avatar>
                <div class="cat-text">
                  <div class="cat-name">{{ selectedReservation.catName }}</div>
                  <div class="cat-breed">{{ selectedReservation.catBreed || '未知品种' }}</div>
                </div>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="预约日期">
              {{ selectedReservation.reservationDate }}
            </el-descriptions-item>
            <el-descriptions-item label="时间段">
              {{ selectedReservation.timeSlot }}
            </el-descriptions-item>
            <el-descriptions-item label="预约时长">
              {{ selectedReservation.duration }}
            </el-descriptions-item>
            <el-descriptions-item label="参与人数">
              {{ selectedReservation.visitorCount }}人
            </el-descriptions-item>
            <el-descriptions-item label="预约目的">
              {{ getPurposeText(selectedReservation.purpose) }}
            </el-descriptions-item>
            <el-descriptions-item label="管理员备注" :span="2">
              {{ selectedReservation.adminNotes || '无备注' }}
            </el-descriptions-item>
            <el-descriptions-item label="用户备注" :span="2">
              {{ selectedReservation.userNotes || '无备注' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(selectedReservation.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDateTime(selectedReservation.updateTime) }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-actions" v-if="canCancel(selectedReservation)">
            <el-button type="danger" @click="cancelReservation(selectedReservation)">
              取消预约
            </el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CalendarCheck, Clock, CheckCircle, Check, Timer, Users } from '@element-plus/icons-vue'
import { api } from '@/utils/api'
import { getUserId, isLoginValid } from '@/utils/auth'
import Layout from './AppLayout.vue'

const router = useRouter()

const loading = ref(false)
const showDetailDialog = ref(false)
const selectedReservation = ref(null)

const statusFilter = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

const defaultCatImage = 'https://picsum.photos/seed/cat/100/100'

const reservations = ref([])

const filteredReservations = computed(() => {
  let result = [...reservations.value]

  if (statusFilter.value) {
    result = result.filter(reservation => reservation.status === statusFilter.value)
  }

  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    result = result.filter(reservation => {
      const reservationDate = new Date(reservation.reservationDate)
      return reservationDate >= startDate && reservationDate <= endDate
    })
  }

  result.sort((a, b) => {
    const dateA = new Date(a.reservationDate + ' ' + (a.timeSlot || '00:00'))
    const dateB = new Date(b.reservationDate + ' ' + (b.timeSlot || '00:00'))
    return dateB - dateA
  })

  return result
})

const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReservations.value.slice(start, end)
})

const totalReservations = computed(() => filteredReservations.value.length)

const totalCount = computed(() => reservations.value.length)

const pendingCount = computed(() => reservations.value.filter(r => r.status === 'pending').length)

const confirmedCount = computed(() => reservations.value.filter(r => r.status === 'confirmed').length)

const completedCount = computed(() => reservations.value.filter(r => r.status === 'completed').length)

const handleFilterChange = () => {
  currentPage.value = 1
}

const filterReservations = (status) => {
  statusFilter.value = status
  currentPage.value = 1
}

const resetFilters = () => {
  statusFilter.value = ''
  dateRange.value = []
  currentPage.value = 1
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const viewReservation = (reservation) => {
  selectedReservation.value = reservation
  showDetailDialog.value = true
}

const handleDetailClose = () => {
  showDetailDialog.value = false
  selectedReservation.value = null
}

const cancelReservation = (reservation) => {
  ElMessageBox.confirm(
    `确定要取消预约[${reservation.id}]吗？`,
    '取消预约',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      loading.value = true
      const response = await api.post('/reservations/cancel', { id: reservation.id })

      if (response.code === 200) {
        reservation.status = 'cancelled'
        ElMessage.success('预约已取消')
        showDetailDialog.value = false
      } else {
        throw new Error(response.message || '取消预约失败')
      }
    } catch (error) {
      ElMessage.error('取消预约失败：' + error.message)
    } finally {
      loading.value = false
    }
  })
}

const canCancel = (reservation) => {
  return ['pending', 'confirmed'].includes(reservation.status) &&
    new Date(reservation.reservationDate) > new Date()
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    pending: 'warning',
    confirmed: 'success',
    completed: 'info',
    cancelled: 'danger'
  }
  return typeMap[status] || 'info'
}

const getPurposeText = (purpose) => {
  const purposeMap = {
    PLAY: '玩耍互动',
    PHOTOSHOOT: '拍照留念',
    THERAPY: '疗愈陪伴',
    ADOPTION: '领养咨询',
    OTHER: '其他'
  }
  return purposeMap[purpose] || purpose || '未指定'
}

const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return ''
  const date = new Date(dateTimeString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getCatImageUrl = (imageUrl) => {
  if (!imageUrl) {
    return defaultCatImage
  }
  if (imageUrl.startsWith('http')) {
    return imageUrl
  }
  return `http://localhost:8083${imageUrl}`
}

const loadReservations = async () => {
  try {
    loading.value = true

    if (!isLoginValid()) {
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return
    }

    const userId = getUserId()
    if (!userId) {
      ElMessage.error('获取用户信息失败，请重新登录')
      router.push('/login')
      return
    }

    const response = await api.get(`/reservations/user/${userId}`)

    if (response.code === 200) {
      const data = response.data || []

      reservations.value = data.map(item => ({
        id: item.id,
        catId: item.catId,
        userId: item.userId,
        catName: item.catName || '未知猫咪',
        catPhoto: item.catPhoto || '',
        catBreed: item.catBreed || '',
        catAge: item.catAge,
        catGender: item.catGender,
        reservationDate: item.reservationDate,
        timeSlot: item.timeSlot || '',
        duration: calculateDuration(item.timeSlot),
        visitorCount: item.visitorCount || 1,
        purpose: item.purpose || '',
        status: (item.status || '').toLowerCase(),
        adminNotes: item.adminNotes || '',
        userNotes: item.userNotes || '',
        cancelReason: item.cancelReason || '',
        createTime: item.createTime,
        updateTime: item.updateTime
      }))
    } else {
      throw new Error(response.message || '获取预约数据失败')
    }
  } catch (error) {
    console.error('获取预约数据失败:', error)
    const errorMsg = error.message || '获取预约数据失败'
    
    if (errorMsg.includes('用户ID不能为空')) {
      ElMessage.error('用户信息无效，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error('获取预约数据失败：' + errorMsg)
    }
    reservations.value = []
  } finally {
    loading.value = false
  }
}

const calculateDuration = (timeSlot) => {
  if (!timeSlot || !timeSlot.includes('-')) return '未知时长';

  try {
    const [startTimeStr, endTimeStr] = timeSlot.split('-');
    const [startHour, startMinute] = startTimeStr.split(':').map(Number);
    const [endHour, endMinute] = endTimeStr.split(':').map(Number);

    const startTimeMinutes = startHour * 60 + startMinute;
    const endTimeMinutes = endHour * 60 + endMinute;
    const totalMinutes = endTimeMinutes - startTimeMinutes;

    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    if (hours > 0) {
      return `${hours}小时${minutes}分钟`;
    } else {
      return `${minutes}分钟`;
    }
  } catch (error) {
    return '未知时长';
  }
};

onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.my-reservations-page {
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
  margin: 15px 0 8px;
}

.page-header p {
  font-size: 16px;
  color: #795548;
  margin: 0;
}

.stats-section {
  margin-bottom: 25px;
}

.stat-card {
  background: #fff;
  border-radius: 15px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border: 2px solid #ff9800;
}

.stat-card.pending.active {
  border-color: #ff9800;
}

.stat-card.confirmed.active {
  border-color: #67c23a;
}

.stat-card.completed.active {
  border-color: #1989fa;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #67c23a;
}

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #ff9800;
}

.stat-card.confirmed .stat-icon {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #67c23a;
}

.stat-card.completed .stat-icon {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1989fa;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.filter-section {
  background-color: #fff;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  align-items: center;
  height: 100%;
}

.reservations-container {
  margin-bottom: 25px;
}

.reservation-card {
  background: #fff;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.reservation-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.reservation-id {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.reservation-date {
  font-size: 12px;
  color: #999;
}

.reservation-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.cat-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.cat-avatar {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
}

.cat-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cat-name {
  font-size: 18px;
  font-weight: 600;
  color: #5d4037;
}

.cat-details {
  display: flex;
  gap: 15px;
}

.detail-item {
  font-size: 13px;
  color: #666;
  padding: 4px 10px;
  background: #f5f5f5;
  border-radius: 20px;
}

.time-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #555;
}

.time-icon {
  width: 20px;
  height: 20px;
  color: #ff9800;
}

.reservation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.notes-section {
  display: flex;
  align-items: center;
  gap: 5px;
  flex: 1;
}

.notes-label {
  font-size: 13px;
  color: #999;
}

.notes-content {
  font-size: 13px;
  color: #666;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reservation-actions {
  display: flex;
  gap: 10px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.empty-icon {
  font-size: 60px;
  color: #ccc;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.reservation-detail {
  padding: 20px 0;
}

.cat-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cat-text {
  display: flex;
  flex-direction: column;
}

.cat-text .cat-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.cat-text .cat-breed {
  font-size: 13px;
  color: #999;
}

.detail-actions {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .my-reservations-page {
    padding: 20px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .stat-card {
    padding: 15px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .stat-value {
    font-size: 20px;
  }

  .filter-section {
    padding: 15px 20px;
  }

  .filter-actions {
    justify-content: center;
    margin-top: 15px;
  }

  .reservation-content {
    flex-direction: column;
    gap: 20px;
  }

  .time-section {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .reservation-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .reservation-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>