<template>
  <div class="my-reservations-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我的预约</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>我的预约</h1>
      <p>查看和管理您的猫咪预约记录</p>
    </div>

    <!-- 筛选和操作区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="预约状态" clearable @change="filterReservations">
            <el-option label="全部" value=""></el-option>
            <el-option label="待确认" value="pending"></el-option>
            <el-option label="已确认" value="confirmed"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
            end-placeholder="结束日期" @change="filterReservations" />
        </el-col>
        <el-col :span="12">
          <div class="filter-actions">
            <el-button @click="resetFilters">重置筛选</el-button>
            <!-- 删除了新建预约按钮 -->
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 预约列表 -->
    <div class="reservations-container">
      <el-table :data="paginatedReservations" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="预约编号" width="120"></el-table-column>
        <el-table-column prop="catName" label="预约猫咪" width="180">
          <template #default="scope">
            <div class="cat-info">
              <el-avatar :size="32" :src="getCatImageUrl(scope.row.catPhoto)" shape="square"
                :alt="scope.row.catName"></el-avatar>
              <span class="cat-name">{{ scope.row.catName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reservationTime" label="预约时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.reservationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="scope">
            {{ scope.row.duration }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewReservation(scope.row)">
              查看详情
            </el-button>
            <el-button v-if="canCancel(scope.row)" size="small" type="danger" @click="cancelReservation(scope.row)">
              取消预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="!loading && reservations.length === 0" class="empty-state">
        <el-empty description="暂无预约记录">
          <!-- 删除了立即预约按钮 -->
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="totalReservations > 0">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
        :total="totalReservations" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <!-- 预约详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="预约详情" width="600px" :before-close="handleDetailClose">
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
            <div class="cat-info">
              <el-avatar :size="40" :src="getCatImageUrl(selectedReservation.catPhoto)" shape="square"></el-avatar>
              <span class="cat-name">{{ selectedReservation.catName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatDateTime(selectedReservation.reservationTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="预约时长">
            {{ selectedReservation.duration }}小时
          </el-descriptions-item>
          <el-descriptions-item label="参与人数">
            {{ selectedReservation.peopleCount }}人
          </el-descriptions-item>
          <el-descriptions-item label="总金额">
            ¥{{ selectedReservation.totalAmount.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(selectedReservation.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注信息" :span="2">
            {{ selectedReservation.remarks || '无备注' }}
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
</template>
<!-- eslint-disable no-unused-vars -->
<!--  eslint-disable no-undef  -->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/utils/api'
import { getUserId, isLoggedIn } from '@/utils/auth'

const router = useRouter()

// 状态管理
const loading = ref(false)
const showDetailDialog = ref(false)
const selectedReservation = ref(null)

// 筛选条件
const statusFilter = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

// 默认图片
const defaultCatImage = 'https://picsum.photos/seed/cat/100/100'

// 从数据库获取的真实预约数据
const reservations = ref([])

// 计算属性
const filteredReservations = computed(() => {
  let result = [...reservations.value]

  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(reservation => reservation.status === statusFilter.value)
  }

  // 日期筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    result = result.filter(reservation => {
      const reservationDate = new Date(reservation.reservationTime)
      return reservationDate >= startDate && reservationDate <= endDate
    })
  }

  return result
})

// 格式化时长显示
const formatDuration = (minutes) => {
  if (!minutes || minutes <= 0) return '未知';

  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;

  if (hours > 0) {
    if (mins > 0) {
      return `${hours}小时${mins}分钟`;
    } else {
      return `${hours}小时`;
    }
  } else {
    return `${mins}分钟`;
  }
};

const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReservations.value.slice(start, end)
})

const totalReservations = computed(() => filteredReservations.value.length)

// 方法
const filterReservations = () => {
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

      // 调用取消预约API
      const response = await api.put(`/reservations/${reservation.id}/cancel`)

      if (response.code === 200) {
        // 更新本地数据
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
    new Date(reservation.reservationTime) > new Date()
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

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取猫咪图片URL（处理可能的null值）
const getCatImageUrl = (imageUrl) => {
  if (!imageUrl) {
    return defaultCatImage
  }

  // 如果已经是完整的HTTP URL，直接返回
  if (imageUrl.startsWith('http')) {
    return imageUrl
  }
  return `http://localhost:8083${imageUrl}`
}
// 从数据库获取预约数据
const loadReservations = async () => {
  try {
    loading.value = true

    // 检查用户登录状态
    if (!isLoggedIn()) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const userId = getUserId()
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }

    // 调用获取用户预约列表API
    const response = await api.get(`/reservations/user/${userId}`)

    if (response.code === 200) {
      // 数据已经是包含猫咪信息的VO对象，直接使用
      const data = response.data || response.list || response.records || []

      reservations.value = data.map(item => ({
        id: item.id,
        catName: item.catName,
        catPhoto: item.catPhoto || '',
        reservationTime: item.reservationDate, 
        duration: calculateDuration(item.timeSlot),
        status: (item.status || '').toLowerCase(),
        createTime: item.createTime,
        remarks: item.userNotes || item.purpose || ''
      }))
    } else {
      throw new Error(response.message || '获取预约数据失败')
    }
  } catch (error) {
    console.error('获取预约数据失败:', error)
    ElMessage.error('获取预约数据失败：' + error.message)
    reservations.value = []
  } finally {
    loading.value = false
  }
}

const calculateDuration = (timeSlot) => {
  if (!timeSlot || !timeSlot.includes('-')) return '未知时长';

  try {
    const [startTimeStr, endTimeStr] = timeSlot.split('-');

    // 解析时间字符串
    const [startHour, startMinute] = startTimeStr.split(':').map(Number);
    const [endHour, endMinute] = endTimeStr.split(':').map(Number);

    // 将时间转换为分钟
    const startTimeMinutes = startHour * 60 + startMinute;
    const endTimeMinutes = endHour * 60 + endMinute;

    // 计算总分钟数
    const totalMinutes = endTimeMinutes - startTimeMinutes;

    // 转换为小时和分钟
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    // 格式化输出
    if (hours > 0) {
      return `${hours}小时${minutes}分钟`;
    } else {
      return `${minutes}分钟`;
    }
  } catch (error) {
    console.error('计算时长失败:', error);
    return '未知时长';
  }
};


// 生命周期
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

.breadcrumb {
  margin-bottom: 10px;
  --el-breadcrumb-item-color: #795548;
  --el-breadcrumb-separator-color: #795548;
}

.filter-section {
  background-color: #fff;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  align-items: center;
  height: 100%;
}

.reservations-container {
  background-color: #fff;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

.cat-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cat-name {
  font-weight: 500;
  color: #5d4037;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.reservation-detail {
  padding: 20px 0;
}

.detail-actions {
  margin-top: 20px;
  text-align: right;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .my-reservations-page {
    padding: 20px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .filter-section {
    padding: 15px 20px;
  }

  .filter-actions {
    justify-content: center;
    margin-top: 15px;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table th) {
    padding: 8px 0;
  }

  :deep(.el-table td) {
    padding: 8px 0;
  }
}
</style>