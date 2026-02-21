<!-- 最近动态 -->
<template>
  <div class="recent-activities-page">
    <div class="page-header">
      <h1>最近动态</h1>
      <el-button type="primary" icon="Refresh" @click="refreshActivities">刷新</el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="filterForm.activityType" placeholder="动态类型" clearable @change="handleFilter">
            <el-option label="全部" value=""></el-option>
            <el-option label="预约请求" value="reservation"></el-option>
            <el-option label="订单创建" value="order"></el-option>
            <el-option label="新用户注册" value="user"></el-option>
            <el-option label="商品相关" value="product"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleFilter"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterForm.status" placeholder="状态" clearable @change="handleFilter">
            <el-option label="全部" value=""></el-option>
            <el-option label="待处理" value="pending"></el-option>
            <el-option label="已处理" value="processed"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <div class="filter-actions">
            <el-button type="primary" @click="handleFilter">搜索</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 动态列表 -->
    <el-card class="activities-card">
      <el-table 
        :data="activitiesList" 
        border 
        stripe 
        :loading="loading" 
        style="width: 100%"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="time" label="时间" min-width="180" align="center" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.time) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="动态标题" min-width="200">
          <template #default="scope">
            <div class="activity-title">
              <el-icon :size="16" :color="getIconColor(scope.row.type)">
                <component :is="getIconComponent(scope.row.icon)" />
              </el-icon>
              <span>{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容详情" min-width="300">
          <template #default="scope">
            <div class="activity-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getActivityTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getActivityStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-for="op in scope.row.operations" 
              :key="op.id" 
              :type="op.type"
              size="small" 
              @click="handleOperation(op.action, scope.row)"
              style="margin-right: 5px;"
            >
              {{ op.text }}
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="viewDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentActivity.title"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="activity-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="时间">
            {{ formatDateTime(currentActivity.time) }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(currentActivity.type)">
              {{ getActivityTypeName(currentActivity.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentActivity.status)">
              {{ getActivityStatusName(currentActivity.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="详细内容">
            {{ currentActivity.content }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentActivity.data" label="相关数据">
            <pre>{{ JSON.stringify(currentActivity.data, null, 2) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  InfoFilled, Warning, User, ShoppingCart, CheckCircle,
  Refresh
} from '@element-plus/icons-vue';

// 路由
const router = useRouter();

// 状态管理
const loading = ref(false);
const activitiesList = ref([]);
const detailDialogVisible = ref(false);
const currentActivity = reactive({});

// 筛选表单
const filterForm = reactive({
  activityType: '',
  dateRange: [],
  status: ''
});

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
});

// 生命周期
onMounted(() => {
  fetchActivitiesList();
});

// 获取动态列表
const fetchActivitiesList = async () => {
  loading.value = true;
  try {
    console.log('开始获取动态数据...');
    console.log('当前页码:', pagination.currentPage);
    console.log('页面大小:', pagination.pageSize);
    
    const url = `http://localhost:8081/catcate/users/recentActivities?pageNum=${pagination.currentPage}&pageSize=${pagination.pageSize}`;
    console.log('请求URL:', url);
    
    const response = await fetch(url);
    console.log('响应状态:', response.status);
    console.log('响应状态文本:', response.statusText);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    console.log('接收到的数据:', result);
    
    // 直接使用后端返回的分页数据
    activitiesList.value = result.list || [];
    pagination.total = result.total || 0;
    
    console.log('处理后的活动列表长度:', activitiesList.value.length);
    console.log('总记录数:', pagination.total);
    
    // 如果没有数据，显示提示信息
    if (activitiesList.value.length === 0) {
      console.log('没有找到任何动态数据');
      ElMessage.info('暂无动态数据');
    }
    
    // 应用前端筛选条件（如果需要额外筛选）
    let filteredData = [...activitiesList.value];
    
    if (filterForm.activityType) {
      filteredData = filteredData.filter(activity => 
        getActivityType(activity.title) === filterForm.activityType
      );
    }
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      const [startDate, endDate] = filterForm.dateRange;
      filteredData = filteredData.filter(activity => {
        const activityDate = new Date(activity.time).toISOString().split('T')[0];
        return activityDate >= startDate && activityDate <= endDate;
      });
    }
    
    // 如果有前端筛选，重新设置数据
    if (filterForm.activityType || (filterForm.dateRange && filterForm.dateRange.length === 2)) {
      activitiesList.value = filteredData;
      pagination.total = filteredData.length;
    }
    
  } catch (error) {
    console.error('获取动态列表失败:', error);
    ElMessage.error('获取动态列表失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 刷新动态
const refreshActivities = () => {
  pagination.currentPage = 1;
  fetchActivitiesList();
};

// 处理筛选
const handleFilter = () => {
  pagination.currentPage = 1;
  fetchActivitiesList();
};

// 重置筛选
const resetFilter = () => {
  filterForm.activityType = '';
  filterForm.dateRange = [];
  filterForm.status = '';
  pagination.currentPage = 1;
  fetchActivitiesList();
};

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.currentPage = 1; // 页码重置为第一页
  fetchActivitiesList();
};

const handleCurrentChange = (val) => {
  pagination.currentPage = val;
  fetchActivitiesList();
};

// 操作处理
const handleOperation = (action, activity) => {
  switch (action) {
    case 'confirm':
      ElMessageBox.confirm('确定要确认这个预约吗？', '确认预约', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 这里调用确认预约的API
        ElMessage.success('预约已确认');
        refreshActivities();
      });
      break;
    case 'reject':
      ElMessageBox.prompt('请输入拒绝原因', '拒绝预约', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S/,
        inputErrorMessage: '拒绝原因不能为空'
      }).then(({ value }) => {
        // 这里调用拒绝预约的API
        ElMessage.success('预约已拒绝');
        refreshActivities();
      });
      break;
    case 'restock':
      // 跳转到商品补货页面
      router.push('/admin/products/list');
      break;
    default:
      ElMessage.info(`执行操作: ${action}`);
  }
};

// 查看详情
const viewDetail = (activity) => {
  Object.assign(currentActivity, activity);
  detailDialogVisible.value = true;
};

// 工具函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

const getActivityType = (title) => {
  if (title.includes('预约')) return 'reservation';
  if (title.includes('订单')) return 'order';
  if (title.includes('用户')) return 'user';
  if (title.includes('商品')) return 'product';
  return 'other';
};

const getActivityTypeName = (type) => {
  const typeMap = {
    'reservation': '预约请求',
    'order': '订单创建',
    'user': '用户相关',
    'product': '商品相关',
    'other': '其他'
  };
  return typeMap[type] || '未知';
};

const getActivityStatusName = (status) => {
  const statusMap = {
    'pending': '待处理',
    'processed': '已处理',
    'confirmed': '已确认',
    'rejected': '已拒绝'
  };
  return statusMap[status] || '未知';
};

const getTypeTagType = (type) => {
  const typeColorMap = {
    'reservation': 'primary',
    'order': 'success',
    'user': 'info',
    'product': 'warning',
    'other': ''
  };
  return typeColorMap[type] || '';
};

const getStatusTagType = (status) => {
  const statusColorMap = {
    'pending': 'warning',
    'processed': 'success',
    'confirmed': 'success',
    'rejected': 'danger'
  };
  return statusColorMap[status] || '';
};

const getIconComponent = (iconName) => {
  const iconMap = {
    'InfoFilled': InfoFilled,
    'Warning': Warning,
    'User': User,
    'ShoppingCart': ShoppingCart,
    'CheckCircle': CheckCircle
  };
  return iconMap[iconName] || InfoFilled;
};

const getIconColor = (type) => {
  const colorMap = {
    'primary': '#409eff',
    'success': '#67c23a',
    'warning': '#e6a23c',
    'danger': '#f56c6c',
    'info': '#909399'
  };
  return colorMap[getTypeTagType(type)] || '#409eff';
};

const tableRowClassName = ({ row }) => {
  if (row.status === 'pending') {
    return 'pending-row';
  }
  return '';
};
</script>

<style scoped>
.recent-activities-page {
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
  font-size: 24px;
  color: #1f2329;
}

.filter-card {
  margin-bottom: 20px;
  padding: 15px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.activities-card {
  overflow: hidden;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.activity-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-content {
  line-height: 1.5;
  color: #606266;
}

.activity-detail pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

/* 表格行样式 */
:deep(.pending-row) {
  background-color: rgba(255, 248, 225, 0.3);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .filter-actions {
    justify-content: flex-start;
  }
}
</style>