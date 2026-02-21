<!-- 评价列表页面 -->
<template>
  <div class="review-management">
    <!-- 页面标题和操作区 -->
    <div class="page-header">
      <h1>评价管理</h1>

    </div>

    <!-- 搜索和筛选区 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.keyword" placeholder="搜索评价内容/用户昵称" clearable prefix-icon="Search"></el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.targetType" placeholder="选择评价对象类型" clearable>
            <el-option label="猫咪" value="CAT"></el-option>
            <el-option label="商品" value="PRODUCT"></el-option>
            <el-option label="服务" value="SERVICE"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.status" placeholder="选择评价状态" clearable>
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已拒绝" value="REJECTED"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.rating" placeholder="选择评分" clearable>
            <el-option label="1分" value="1"></el-option>
            <el-option label="2分" value="2"></el-option>
            <el-option label="3分" value="3"></el-option>
            <el-option label="4分" value="4"></el-option>
            <el-option label="5分" value="5"></el-option>
          </el-select>
        </el-col>
        <!-- <el-col :span="7">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-col> -->
      </el-row>
      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">
          搜索
        </el-button>
        <el-button @click="resetSearch">
          重置
        </el-button>
        <el-button type="warning" icon="Check" @click="handleBatchApproved" :disabled="selectedReviews.length === 0"
          style="padding-left: 0px;">
          批量通过
        </el-button>
        <el-button type="danger" icon="Close" @click="handleBatchRejected" :disabled="selectedReviews.length === 0"
          style="padding-left: 0px;">
          批量拒绝
        </el-button>
        <el-button type="danger" icon="Delete" @click="handleBatchDelete" :disabled="selectedReviews.length === 0"
          style="padding-left: 0px;">
          批量删除
        </el-button>
      </div>
    </el-card>

    <!-- 评价列表 -->
    <el-card class="table-card">
      <el-table :data="reviewList" border stripe :loading="loading" @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column label="评价用户" min-width="120" align="center">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :src="scope.row.userAvatar || defaultAvatar" size="36"></el-avatar>
              <div class="user-name">
                <!-- 管理员端显示真实用户信息，即使是匿名评价 -->
                <span v-if="scope.row.userNickname && !scope.row.userNickname.startsWith('用户')">
                  {{ scope.row.userNickname }}
                </span>
                <span v-else-if="scope.row.userUsername">
                  {{ scope.row.userUsername }}
                </span>
                <span v-else>
                  用户{{ scope.row.userId }}
                </span>
                <!-- 显示匿名标识，让管理员知道这是匿名评价 -->
                <el-tag size="mini" v-if="scope.row.isAnonymous" type="info" style="margin-left: 5px;">
                  匿名评价
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价对象" min-width="150">
          <template #default="scope">
            <div class="target-info">
              <div class="target-type">
                <el-tag :type="getTargetTypeTagType(scope.row.targetType)">
                  {{ formatTargetType(scope.row.targetType) }}
                </el-tag>
              </div>
              <div class="target-name">
                {{ scope.row.targetName || `ID:${scope.row.targetId}` }}
              </div>
              <div class="order-info" v-if="scope.row.orderId">
                订单号: <span class="order-number">{{ scope.row.orderNumber || `#${scope.row.orderId}` }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" min-width="130" align="center">
          <template #default="scope">
            <el-rate :value="scope.row.rating" :max="5" disabled allow-half style="color: #ff9f43;"
              disabled-void-color="#ff9f43" />
            <span class="rating-text">{{ scope.row.rating }}分</span>
          </template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="200">
          <template #default="scope">
            <div class="review-content" :title="scope.row.content">
              {{ scope.row.content || '无评价内容' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价图片" min-width="120" align="center">
          <template #default="scope">
            <div class="review-images" v-if="scope.row.images && scope.row.images.length > 0">
              <el-image v-for="(img, index) in scope.row.images" :key="index" :src="img"
                :preview-src-list="scope.row.images" width="40" height="40" fit="cover" class="review-image"></el-image>
              <span class="image-count" v-if="scope.row.images.length > 3">
                +{{ scope.row.images.length - 3 }}
              </span>
            </div>
            <span class="no-image" v-else>无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="评价状态" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" style="color: white;">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="点赞数" min-width="100" align="center">
          <template #default="scope">
            <el-tag type="info" size="mini">
              <el-icon :size="14">
                <Like />
              </el-icon>
              {{ scope.row.likeCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" icon="Eye" @click="handleViewReview(scope.row)"
              style="padding-left: 0px;">查看</el-button>
            <!-- 拒绝后不显示回复 -->
            <el-button size="small" type="primary" icon="Message" @click="handleReplyReview(scope.row)"
              style="padding-left: 0px;" v-if="scope.row.status === 'APPROVED'">回复</el-button>
            <template v-if="scope.row.status === 'PENDING'">
              <el-button size="small" type="success" icon="Check" @click="handleApproveReview(scope.row)"
                style="padding-left: 0px;">通过</el-button>
              <el-button size="small" type="danger" icon="Close" @click="handleRejectReview(scope.row)"
                style="padding-left: 0px;">拒绝</el-button>
            </template>
            <el-button size="small" type="danger" icon="Delete" @click="handleDeleteReview(scope.row)"
              style="padding-left: 0px;">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"></el-pagination>
      </div>
    </el-card>

    <!-- 查看评价详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="评价详情" width="800px" :close-on-click-modal="false" append-to-body>
            <div class="review-detail">
        <div class="detail-header">
          <div class="user-section">
            <el-avatar :src="currentReview.userAvatar || defaultAvatar" size="48"></el-avatar>
            <div class="user-info">
              <div class="user-name">
                <!-- 管理员端显示真实用户信息 -->
                <span v-if="currentReview.userNickname && !currentReview.userNickname.startsWith('用户')">
                  {{ currentReview.userNickname }}
                </span>
                <span v-else-if="currentReview.userUsername">
                  {{ currentReview.userUsername }}
                </span>
                <span v-else>
                  用户{{ currentReview.userId }}
                </span>
                <el-tag size="mini" v-if="currentReview.isAnonymous" type="info" style="margin-left: 5px;">
                  匿名评价
                </el-tag>
              </div>
              <div class="create-time">
                评价时间: {{ formatDateTime(currentReview.createTime) }}
              </div>
            </div>
          </div>
          <div class="rating-section">
            <el-rate :value="currentReview.rating" :max="5" disabled allow-half
              style="color: #ff9f43; font-size: 20px;"></el-rate>
            <span class="rating-text">{{ currentReview.rating }}分</span>
          </div>
        </div>

        <div class="target-section">
          <el-tag :type="getTargetTypeTagType(currentReview.targetType)">
            {{ formatTargetType(currentReview.targetType) }}
          </el-tag>
          <span class="target-name">
            {{ currentReview.targetName || `ID:${currentReview.targetId}` }}
          </span>
          <span class="order-info" v-if="currentReview.orderId">
            关联订单: {{ currentReview.orderNumber || `#${currentReview.orderId}` }}
          </span>
        </div>

        <div class="content-section">
          <h4>评价内容:</h4>
          <div class="content">
            {{ currentReview.content || '无评价内容' }}
          </div>
        </div>

        <div class="images-section" v-if="currentReview.images && currentReview.images.length > 0">
          <h4>评价图片:</h4>
          <div class="images-container">
            <el-image v-for="(img, index) in currentReview.images" :key="index" :src="img"
              :preview-src-list="currentReview.images" width="120" height="120" fit="cover"
              class="detail-image"></el-image>
          </div>
        </div>

        <div class="reply-section" v-if="currentReview.adminReply">
          <div class="reply-header">
            <el-avatar src="https://picsum.photos/seed/admin/100/100" size="36"></el-avatar>
            <div class="reply-info">
              <div class="reply-name">管理员回复</div>
              <div class="reply-time">
                {{ formatDateTime(currentReview.updateTime) }}
              </div>
            </div>
          </div>
          <div class="reply-content">
            {{ currentReview.adminReply }}
          </div>
        </div>
      </div>
    </el-dialog>

        <!-- 回复评价弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="600px" :close-on-click-modal="false" append-to-body>
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="80px" class="reply-form">
        <el-form-item label="评价用户">
          <el-input 
            :value="getRealUserName(currentReview)" 
            disabled>
          </el-input>
        </el-form-item>
        <el-form-item label="评价对象" disabled>
          <el-input
            :value="`${formatTargetType(currentReview.targetType)} - ${currentReview.targetName || `ID:${currentReview.targetId}`}`"
            disabled></el-input>
        </el-form-item>
        <el-form-item label="回复内容" prop="adminReply">
          <el-input v-model="replyForm.adminReply" type="textarea" :rows="6" placeholder="请输入回复内容（最多500字）"></el-input>
          <div class="form-hint">
            回复内容将公开显示，请注意措辞
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveReply">保存回复</el-button>
      </template>
    </el-dialog>

    <!-- 批量操作确认弹窗 -->
    <el-dialog v-model="batchConfirmVisible" :title="batchConfirmTitle" width="400px" :close-on-click-modal="false">
      <p>{{ batchConfirmContent }}</p>
      <template v-if="batchActionType === 'reject'">
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input v-model="batchForm.rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因（选填）"></el-input>
        </el-form-item>
      </template>
      <template #footer>
        <el-button @click="batchConfirmVisible = false">取消</el-button>
        <el-button
          :type="batchActionType === 'delete' ? 'danger' : (batchActionType === 'approve' ? 'success' : 'warning')"
          @click="confirmBatchAction">
          确认{{ batchActionText }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Search, Eye, Edit, Delete, Check, Close, Like,
  Star, StarOff, Time, Calendar, User, ShoppingCart,
  Cat, Coffee
} from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const reviewList = ref([]);
const selectedReviews = ref([]);
const batchActionType = ref('approve'); // approve: 通过, reject: 拒绝, delete: 删除
const replyFormRef = ref(null);
const defaultAvatar = 'https://picsum.photos/seed/user/100/100';

// 搜索表单
const searchForm = reactive({
  keyword: '',
  targetType: '',
  status: '',
  rating: '',
  dateRange: []
});

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 弹窗相关
const viewDialogVisible = ref(false);
const replyDialogVisible = ref(false);
const batchConfirmVisible = ref(false);
const batchConfirmTitle = ref('');
const batchConfirmContent = ref('');

// 当前操作的评价
const currentReview = reactive({
  id: '',
  userId: '',
  userNickname: '',
  userAvatar: '',
  targetType: '',
  targetId: '',
  targetName: '',
  orderId: '',
  orderNumber: '',
  rating: 5,
  content: '',
  images: [],
  isAnonymous: 0,
  status: 'PENDING',
  adminReply: '',
  likeCount: 0,
  createTime: '',
  updateTime: ''
});

// 回复表单
const replyForm = reactive({
  adminReply: ''
});

// 批量操作表单
const batchForm = reactive({
  rejectReason: ''
});

// 表单验证规则
const replyRules = reactive({
  adminReply: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 2, max: 500, message: '回复内容长度必须在2-500个字符之间', trigger: 'blur' }
  ]
});

// 计算属性：批量操作文本
const batchActionText = computed(() => {
  if (batchActionType.value === 'approve') {
    return '通过';
  } else if (batchActionType.value === 'reject') {
    return '拒绝';
  } else {
    return '删除';
  }
});

// 生命周期
onMounted(() => {
  fetchReviewList();
});

// 获取真实用户名（供管理员查看）
const getRealUserName = (review) => {
  if (review.userNickname && !review.userNickname.startsWith('用户')) {
    return review.userNickname;
  } else if (review.userUsername) {
    return review.userUsername;
  } else {
    return `用户${review.userId}`;
  }
};

// 方法：获取评价列表
const fetchReviewList = async () => {
  loading.value = true;
  try {
    // 构造请求参数
    const params = {
      keyword: searchForm.keyword,
      targetType: searchForm.targetType,
      status: searchForm.status,
      rating: searchForm.rating,
      // startTime: searchForm.dateRange[0] || '',
      // endTime: searchForm.dateRange[1] || '',
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    };

    const res = await fetch("http://localhost:8081/catcate/reviews/selectList", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(params)
    });

    if (res.ok) {
      const data = await res.json();
      // 假设后端返回格式：{ total: 总数, list: 评价列表 }
      if (data && Array.isArray(data.list)) {
        reviewList.value = data.list.map(item => ({
          ...item,
          // 处理图片数组
          images: item.images ? item.images.split(',').filter(img => img) : [],
          // 格式化时间
          createTime: item.createTime ? new Date(item.createTime) : '',
          updateTime: item.updateTime ? new Date(item.updateTime) : ''
        }));
        pagination.total = data.total || 0;
      } else {
        ElMessage.error('数据格式不正确');
      }
    } else {
      ElMessage.error('获取评价列表失败: ' + res.status);
    }
  } catch (error) {
    console.error('获取评价列表失败:', error);
    ElMessage.error('获取评价列表失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 搜索和重置
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchReviewList();
};

const resetSearch = () => {
  searchForm.keyword = '';
  searchForm.targetType = '';
  searchForm.status = '';
  searchForm.rating = '';
  searchForm.dateRange = [];
  pagination.currentPage = 1;
  fetchReviewList();
};

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size;
  pagination.currentPage = 1;
  fetchReviewList();
};

const handleCurrentChange = (page) => {
  pagination.currentPage = page;
  fetchReviewList();
};

// 选择变化
const handleSelectionChange = (selection) => {
  selectedReviews.value = selection;
};

// 表格行样式（根据状态区分）
const tableRowClassName = ({ row }) => {
  if (row.status === 'PENDING') {
    return 'row-pending'; // 待审核-黄色背景
  } else if (row.status === 'REJECTED') {
    return 'row-rejected'; // 已拒绝-红色背景
  }
  return '';
};

// 格式化评价对象类型
const formatTargetType = (type) => {
  const typeMap = {
    'CAT': '猫咪',
    'PRODUCT': '商品',
    'SERVICE': '服务'
  };
  return typeMap[type] || type;
};

// 格式化评价状态
const formatStatus = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  };
  return statusMap[status] || status;
};

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString('zh-CN');
};

// 获取评价对象类型标签样式
const getTargetTypeTagType = (type) => {
  const typeMap = {
    'CAT': 'success',
    'PRODUCT': 'primary',
    'SERVICE': 'warning'
  };
  return typeMap[type] || 'info';
};

// 获取评价状态标签样式
const getStatusTagType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  };
  return statusMap[status] || 'default';
};

// 查看评价详情
const handleViewReview = (row) => {
  Object.assign(currentReview, {
    ...row,
    // 确保图片是数组格式
    images: Array.isArray(row.images) ? row.images : row.images ? row.images.split(',').filter(img => img) : []
  });
  viewDialogVisible.value = true;
};

// 回复评价
const handleReplyReview = (row) => {
  Object.assign(currentReview, {
    ...row,
    images: Array.isArray(row.images) ? row.images : row.images ? row.images.split(',').filter(img => img) : []
  });
  // 填充已有回复
  replyForm.adminReply = row.adminReply || '';
  replyDialogVisible.value = true;
};

// 保存回复
const handleSaveReply = async () => {
  await nextTick();
  try {
    await replyFormRef.value.validate();
    loading.value = true;

    const res = await fetch("http://localhost:8081/catcate/reviews/reply", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        id: currentReview.id,
        adminReply: replyForm.adminReply
      })
    });

    if (res.ok) {
      ElMessage.success('回复成功');
      replyDialogVisible.value = false;
      // 更新当前评价的回复内容
      currentReview.adminReply = replyForm.adminReply;
      currentReview.updateTime = new Date();
      // 刷新列表
      fetchReviewList();
    } else {
      ElMessage.error('回复失败');
    }
  } catch (error) {
    console.error('回复失败:', error);
    ElMessage.error('回复失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 审核通过
const handleApproveReview = (row) => {
  ElMessageBox.confirm(
    `确定要通过评价【ID:${row.id}】吗？`,
    '确认通过',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(async () => {
    await updateReviewStatus(row.id, 'APPROVED', '通过');
  });
};

// 审核拒绝
const handleRejectReview = (row) => {
  ElMessageBox.prompt(
    `请输入拒绝评价【ID:${row.id}】的原因`,
    '确认拒绝',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      inputType: 'textarea',
      inputPlaceholder: '请输入拒绝原因（选填）',
      inputValidator: (value) => {
        if (value && value.length > 200) {
          return '拒绝原因不能超过200个字符';
        }
      }
    }
  ).then(async ({ value }) => {
    await updateReviewStatus(row.id, 'REJECTED', '拒绝', value);
  });
};

// 删除评价
const handleDeleteReview = (row) => {
  ElMessageBox.confirm(
    `确定要删除评价【ID:${row.id}】吗？此操作不可撤销。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    await deleteReview(row.id);
  });
};

// 更新评价状态
const updateReviewStatus = async (id, status, actionText, rejectReason = '') => {
  loading.value = true;
  try {
    // 如果是拒绝操作，将拒绝原因作为管理员回复
    const requestData = {
      id,
      status,
      rejectReason: rejectReason || ''
    };

    // 如果是拒绝操作且有拒绝原因，则将其作为管理员回复
    if (status === 'REJECTED' && rejectReason) {
      requestData.adminReply = `拒绝原因：${rejectReason}`;
    }

    const res = await fetch("http://localhost:8081/catcate/reviews/updateStatus", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(requestData)
    });

    if (res.ok) {
      ElMessage.success(`评价已${actionText}成功`);
      fetchReviewList();
    } else {
      ElMessage.error(`评价${actionText}失败`);
    }
  } catch (error) {
    console.error(`评价${actionText}失败:`, error);
    ElMessage.error(`评价${actionText}失败: ` + error.message);
  } finally {
    loading.value = false;
  }
};

// 删除评价
const deleteReview = async (id) => {
  loading.value = true;
  try {
    const res = await fetch("http://localhost:8081/catcate/reviews/deleteById", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ id })
    });

    if (res.ok) {
      ElMessage.success('评价删除成功');
      fetchReviewList();
    } else {
      ElMessage.error('评价删除失败');
    }
  } catch (error) {
    console.error('评价删除失败:', error);
    ElMessage.error('评价删除失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 批量通过
const handleBatchApproved = () => {
  if (selectedReviews.value.length === 0) {
    ElMessage.warning('请选择要操作的评价');
    return;
  }

  batchActionType.value = 'approve';
  batchConfirmTitle.value = '批量通过评价';
  batchConfirmContent.value = `您确定要通过选中的 ${selectedReviews.value.length} 个评价吗？`;
  batchConfirmVisible.value = true;
};

// 批量拒绝
const handleBatchRejected = () => {
  if (selectedReviews.value.length === 0) {
    ElMessage.warning('请选择要操作的评价');
    return;
  }

  batchActionType.value = 'reject';
  batchConfirmTitle.value = '批量拒绝评价';
  batchConfirmContent.value = `您确定要拒绝选中的 ${selectedReviews.value.length} 个评价吗？`;
  batchForm.rejectReason = '';
  batchConfirmVisible.value = true;
};

// 批量删除
const handleBatchDelete = () => {
  if (selectedReviews.value.length === 0) {
    ElMessage.warning('请选择要操作的评价');
    return;
  }

  batchActionType.value = 'delete';
  batchConfirmTitle.value = '批量删除评价';
  batchConfirmContent.value = `您确定要删除选中的 ${selectedReviews.value.length} 个评价吗？此操作不可撤销。`;
  batchConfirmVisible.value = true;
};

// 确认批量操作
const confirmBatchAction = async () => {
  if (selectedReviews.value.length === 0) {
    ElMessage.warning('请选择要操作的评价');
    batchConfirmVisible.value = false;
    return;
  }

  loading.value = true;

  try {
    if (batchActionType.value === 'approve') {
      // 批量通过 - 构造符合后端要求的数据格式
      const requestData = selectedReviews.value.map(item => ({
        id: item.id,
        status: 'APPROVED'
      }));

      const res = await fetch("http://localhost:8081/catcate/reviews/batchUpdateStatus", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
      });

      if (res.ok) {
        ElMessage.success('批量通过成功');
      } else {
        ElMessage.error('批量通过失败');
      }
    } else if (batchActionType.value === 'reject') {
      // 批量拒绝 - 构造符合后端要求的数据格式
      const requestData = selectedReviews.value.map(item => {
        const reviewData = {
          id: item.id,
          status: 'REJECTED',
          rejectReason: batchForm.rejectReason || ''
        };

        // 如果有拒绝原因，也作为管理员回复
        if (batchForm.rejectReason) {
          reviewData.adminReply = `拒绝原因：${batchForm.rejectReason}`;
        }

        return reviewData;
      });

      const res = await fetch("http://localhost:8081/catcate/reviews/batchUpdateStatus", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
      });

      if (res.ok) {
        ElMessage.success('批量拒绝成功');
      } else {
        ElMessage.error('批量拒绝失败');
      }
    } else {
      // 批量删除
      const ids = selectedReviews.value.map(item => item.id);
      const res = await fetch("http://localhost:8081/catcate/reviews/deleteByIds", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(ids)
      });

      if (res.ok) {
        ElMessage.success('批量删除成功');
      } else {
        // 添加更详细的错误信息
        const errorMsg = await res.text();
        console.error('删除失败响应:', errorMsg);
        ElMessage.error('批量删除失败: ' + errorMsg);
      }
    }

    // 刷新列表
    fetchReviewList();
    batchConfirmVisible.value = false;
    selectedReviews.value = [];
  } catch (error) {
    console.error(`批量${batchActionText.value}失败:`, error);
    ElMessage.error(`批量${batchActionText.value}失败: ` + error.message);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.review-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 20px;
  color: #1f2329;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff7ee;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  gap: 10px;
}

.table-card {
  overflow: hidden;
  background-color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.pagination {
  margin-top: 15px;
  text-align: right;
}

/* 评价内容样式 */
.review-content {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #666;
}

/* 评价图片样式 */
.review-images {
  display: flex;
  align-items: center;
  gap: 5px;
  position: relative;
  width: 135px;
}

.review-image {
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #eee;
}

.image-count {
  position: absolute;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 12px;
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-image {
  color: #999;
  font-size: 14px;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

/* 评价对象样式 */
.target-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.target-type {
  display: inline-block;
}

.target-name {
  font-size: 14px;
  color: #333;
}

.order-info {
  font-size: 12px;
  color: #666;
}

.order-number {
  color: #ff9f43;
  font-weight: 500;
}

/* 评分样式 */
.rating-text {
  margin-left: 8px;
  font-size: 14px;
  color: #666;
}

/* 表格行样式 */
::v-deep .el-table .row-pending {
  background-color: rgba(255, 229, 153, 0.1);
}

::v-deep .el-table .row-rejected {
  background-color: rgba(245, 108, 108, 0.05);
}

/* 详情弹窗样式 */
.review-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.create-time {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.target-section {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-section {
  margin-bottom: 20px;
}

.content-section h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #333;
}

.content {
  line-height: 1.6;
  color: #333;
  white-space: pre-line;
}

.images-section {
  margin-bottom: 20px;
}

.images-section h4 {
  margin: 0 0 10px 0;
  font-size: 15px;
  color: #333;
}

.images-container {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-image {
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #eee;
}

.reply-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #eee;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.reply-name {
  font-weight: 500;
  font-size: 14px;
}

.reply-time {
  font-size: 12px;
  color: #666;
}

.reply-content {
  padding: 12px;
  background-color: #f5fafe;
  border-radius: 4px;
  line-height: 1.6;
  color: #333;
}

/* 回复表单样式 */
.reply-form {
  margin-top: 10px;
}

.form-hint {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

/* 保持与首页一致的样式 */
::v-deep .el-card {
  border-radius: 8px;
}

::v-deep .el-button--primary {
  background-color: #ff9f43;
  border-color: #ff9f43;
}

::v-deep .el-button--primary:hover {
  background-color: #ff8c21;
  border-color: #ff8c21;
}

::v-deep .el-button--success {
  background-color: #00d2d3;
  border-color: #00d2d3;
}

::v-deep .el-button--success:hover {
  background-color: #00b8b9;
  border-color: #00b8b9;
}

::v-deep .el-button--danger {
  background-color: #ff6b6b;
  border-color: #ff6b6b;
}

::v-deep .el-button--danger:hover {
  background-color: #ff5252;
  border-color: #ff5252;
}

::v-deep .el-button--warning {
  background-color: #ffa801;
  border-color: #ffa801;
}

::v-deep .el-button--warning:hover {
  background-color: #ff9100;
  border-color: #ff9100;
}

::v-deep .el-tag--primary {
  background-color: #ff9f43;
}

::v-deep .el-tag--success {
  background-color: #00d2d3;
}

::v-deep .el-tag--info {
  background-color: #409eff;
}

::v-deep .el-tag--danger {
  background-color: #ff6b6b;
}

::v-deep .el-tag--warning {
  background-color: #ffa801;
}

::v-deep .el-input__inner,
::v-deep .el-select-dropdown,
::v-deep .el-date-editor .el-input__inner,
::v-deep .el-textarea__inner {
  border-radius: 4px;
}

::v-deep .el-table th {
  background-color: #fff7ee;
}

/* 图片预览样式 */
::v-deep .el-image-viewer__wrapper {
  z-index: 3000 !important;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .el-table-column {
    min-width: 100px !important;
  }
}

@media (max-width: 992px) {
  .el-row {
    flex-direction: column;
  }

  .el-col {
    width: 100% !important;
    margin-bottom: 10px;
  }

  .filter-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .filter-actions {
    flex-direction: column;
  }

  .filter-actions .el-button {
    width: 100%;
  }

  /* 隐藏部分列 */
  .el-table-column:not(.el-table-column--selection):not([label="评价ID"]):not([label="评价用户"]):not([label="评价对象"]):not([label="评分"]):not([label="评价状态"]):not([label="操作"]) {
    display: none !important;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>