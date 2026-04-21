<template>
  <Layout>
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="$router.push('/home')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>个人中心</el-breadcrumb-item>
        <el-breadcrumb-item>我的评价</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>我的评价</h1>
      <p>查看和管理您的评价记录</p>
    </div>

    <!-- 筛选和搜索区域 -->
    <div class="filter-section">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input v-model="searchKeyword" placeholder="搜索评价内容或商品名称..." clearable @keyup.enter="searchReviews">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="ratingFilter" placeholder="评分" clearable @change="filterReviews">
            <el-option label="全部评分" value=""></el-option>
            <el-option label="5星" value="5"></el-option>
            <el-option label="4星" value="4"></el-option>
            <el-option label="3星" value="3"></el-option>
            <el-option label="2星" value="2"></el-option>
            <el-option label="1星" value="1"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="评价状态" clearable @change="filterReviews">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="已发布" value="PUBLISHED"></el-option>
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <div class="filter-actions">
            <el-button type="primary" @click="searchReviews">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 评价列表 -->
    <div class="reviews-container">
      <el-table :data="paginatedReviews" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="评价编号" width="120"></el-table-column>
        <el-table-column prop="productName" label="商品名称" width="180">
          <template #default="scope">
            <div class="product-info">
              <el-avatar :size="32" :src="scope.row.productImage" shape="square" v-if="scope.row.productImage"></el-avatar>
              <span class="product-name">{{ scope.row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="scope">
            <el-rate :value="scope.row.rating" disabled :max="5" :show-text="false"></el-rate>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容">
          <template #default="scope">
            <div class="review-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button size="small" @click="viewReview(scope.row)">查看详情</el-button>
            <el-button size="small" type="primary" @click="editReview(scope.row)" v-if="scope.row.status === 'PUBLISHED'">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteReview(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="!loading && reviews.length === 0" class="empty-state">
        <el-empty description="暂无评价记录">
          <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="totalReviews > 0">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
        :total="totalReviews" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <!-- 评价详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="评价详情" width="600px" :before-close="handleDetailClose">
      <div v-if="selectedReview" class="review-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="评价编号">
            {{ selectedReview.id }}
          </el-descriptions-item>
          <el-descriptions-item label="商品信息">
            <div class="product-info">
              <el-avatar :size="40" :src="selectedReview.productImage" shape="square" v-if="selectedReview.productImage"></el-avatar>
              <span class="product-name">{{ selectedReview.productName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate :value="selectedReview.rating" disabled :max="5" :show-text="true"></el-rate>
          </el-descriptions-item>
          <el-descriptions-item label="评价内容">
            {{ selectedReview.content }}
          </el-descriptions-item>
          <el-descriptions-item label="评价时间">
            {{ formatDate(selectedReview.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="评价状态">
            <el-tag :type="getStatusTagType(selectedReview.status)">
              {{ getStatusText(selectedReview.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="回复内容" v-if="selectedReview.replyContent">
            {{ selectedReview.replyContent }}
          </el-descriptions-item>
          <el-descriptions-item label="回复时间" v-if="selectedReview.replyTime">
            {{ formatDate(selectedReview.replyTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 编辑评价对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑评价" width="600px" :before-close="handleEditClose">
      <div class="edit-review-form">
        <el-form :model="editForm" :rules="editRules" ref="editFormRef">
          <el-form-item label="评分" prop="rating">
            <el-rate v-model="editForm.rating" :max="5" :show-text="true"></el-rate>
          </el-form-item>
          <el-form-item label="评价内容" prop="content">
            <el-input type="textarea" v-model="editForm.content" rows="4" placeholder="请输入评价内容"></el-input>
          </el-form-item>
          <el-form-item label="上传图片">
            <el-upload
              class="upload-demo"
              action="#"
              :auto-upload="false"
              :on-change="handleImageChange"
              :file-list="editForm.images"
              list-type="picture-card"
            >
              <el-icon><Plus /></el-icon>
              <template #file="{ file }">
                <div>
                  <img :src="file.url" class="el-upload-list__item-thumbnail" />
                  <el-icon class="el-upload-list__item-delete" @click="handleImageRemove(file)"><Delete /></el-icon>
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { api } from '@/utils/api'
import { getUserId, isLoggedIn } from '@/utils/auth'
import Layout from './Layout.vue'

const router = useRouter()

// 状态管理
const loading = ref(false)
const reviews = ref([])
const searchKeyword = ref('')
const ratingFilter = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const selectedReview = ref(null)
const editForm = ref({})
const editFormRef = ref(null)

// 表单验证规则
const editRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'blur' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }, { min: 10, message: '评价内容至少10个字符', trigger: 'blur' }]
}

// 计算属性
const filteredReviews = computed(() => {
  let result = [...reviews.value]

  // 搜索关键词
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(review => 
      review.content.toLowerCase().includes(keyword) || 
      review.productName.toLowerCase().includes(keyword)
    )
  }

  // 评分筛选
  if (ratingFilter.value) {
    result = result.filter(review => review.rating === parseInt(ratingFilter.value))
  }

  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(review => review.status === statusFilter.value)
  }

  return result
})

const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReviews.value.slice(start, end)
})

const totalReviews = computed(() => filteredReviews.value.length)

// 生命周期
onMounted(() => {
  loadReviews()
})

// 加载评价列表
const loadReviews = async () => {
  try {
    loading.value = true

    // 检查登录状态
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

    // 调用API获取评价列表
    const response = await api.get(`/reviews/user/${userId}`)

    if (response.code === 200) {
      reviews.value = response.data || []
    } else {
      throw new Error(response.message || '获取评价列表失败')
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败：' + error.message)
    reviews.value = []
  } finally {
    loading.value = false
  }
}

// 搜索评价
const searchReviews = () => {
  currentPage.value = 1
}

// 筛选评价
const filterReviews = () => {
  currentPage.value = 1
}

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  ratingFilter.value = ''
  statusFilter.value = ''
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
}

// 查看评价详情
const viewReview = (review) => {
  selectedReview.value = review
  showDetailDialog.value = true
}

// 关闭详情对话框
const handleDetailClose = () => {
  showDetailDialog.value = false
  selectedReview.value = null
}

// 编辑评价
const editReview = (review) => {
  editForm.value = {
    id: review.id,
    rating: review.rating,
    content: review.content,
    images: review.images || []
  }
  showEditDialog.value = true
}

// 关闭编辑对话框
const handleEditClose = () => {
  showEditDialog.value = false
  editForm.value = {}
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// 提交编辑
const submitEdit = async () => {
  if (editFormRef.value) {
    await editFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          loading.value = true

          // 调用API更新评价
          const response = await api.put(`/reviews/${editForm.value.id}`, {
            rating: editForm.value.rating,
            content: editForm.value.content,
            images: editForm.value.images
          })

          if (response.code === 200) {
            ElMessage.success('评价更新成功')
            showEditDialog.value = false
            loadReviews()
          } else {
            throw new Error(response.message || '更新评价失败')
          }
        } catch (error) {
          ElMessage.error('更新评价失败：' + error.message)
        } finally {
          loading.value = false
        }
      }
    })
  }
}

// 删除评价
const deleteReview = (review) => {
  ElMessageBox.confirm(
    `确定要删除评价[${review.id}]吗？`,
    '删除评价',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      loading.value = true

      // 调用API删除评价
      const response = await api.delete(`/reviews/${review.id}`)

      if (response.code === 200) {
        ElMessage.success('评价删除成功')
        loadReviews()
      } else {
        throw new Error(response.message || '删除评价失败')
      }
    } catch (error) {
      ElMessage.error('删除评价失败：' + error.message)
    } finally {
      loading.value = false
    }
  })
}

// 处理图片上传
const handleImageChange = (file, fileList) => {
  editForm.value.images = fileList
}

// 处理图片删除
const handleImageRemove = (file) => {
  editForm.value.images = editForm.value.images.filter(item => item.uid !== file.uid)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    PUBLISHED: '已发布',
    PENDING: '待审核',
    REJECTED: '已驳回'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    PUBLISHED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped>
.my-reviews-page {
  min-height: calc(100vh - 70px);
  background-color: #fff9f5;
  padding: 30px 40px;
}

/* 页面头部 */
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

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 评价容器 */
.reviews-container {
  background-color: #fff;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

/* 商品信息 */
.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-name {
  font-weight: 500;
  color: #5d4037;
}

/* 评价内容 */
.review-content {
  line-height: 1.5;
  color: #555;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 空状态 */
.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 评价详情 */
.review-detail {
  padding: 10px 0;
}

/* 编辑评价表单 */
.edit-review-form {
  padding: 10px 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .my-reviews-page {
    padding: 20px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .filter-section {
    padding: 15px 20px;
  }

  .filter-actions {
    flex-direction: column;
    margin-top: 15px;
  }

  .review-content {
    max-width: 200px;
  }
}
</style>