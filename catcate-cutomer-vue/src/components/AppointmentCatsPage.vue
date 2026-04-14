<!-- 预约猫咪 -->
<template>
  <div class="appointment-cats-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="$router.push('/HomePage')">首页</el-breadcrumb-item>
        <el-breadcrumb-item @click="$router.push('/CatGuidePage')">猫咪图鉴</el-breadcrumb-item>
        <el-breadcrumb-item>预约撸猫</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>预约撸猫</h1>
      <p>为心爱的猫咪预约专属互动时光</p>
    </div>

    <!-- 猫咪信息展示 -->
    <div class="cat-info-section" v-if="selectedCat">
      <el-card class="cat-card">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-image :src="getProcessedCatImageUrl(selectedCat.photoUrl)" :alt="selectedCat.name" class="cat-avatar"
              fit="cover" v-if="selectedCat && selectedCat.photoUrl"></el-image>
            <!-- 当没有图片时显示占位符 -->
            <div v-else class="cat-avatar-placeholder">
              <el-icon size="40" color="#ccc">
                <Picture />
              </el-icon>
              <p>暂无图片</p>
            </div>

          </el-col>
          <el-col :span="16">
            <div class="cat-details">
              <h2>{{ selectedCat.name }}</h2>
              <div class="cat-desc">
                <span class="breed">品种: {{ selectedCat.breed }}</span>
                <span>年龄: {{ formatAge(selectedCat.age) }}</span>
                <span>性别: {{ selectedCat.gender === 'MALE' ? '公' : '母' }}</span>
              </div>
              <p class="personality">性格特点: {{ selectedCat.personality || '暂无描述' }}</p>
              <p class="health-status">健康状况: {{ getHealthStatusText(selectedCat.healthStatus) }}</p>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 预约表单 -->
    <el-card class="reservation-card">
      <template #header>
        <div class="card-header">
          <span>预约信息填写</span>
        </div>
      </template>

      <el-form ref="reservationFormRef" :model="reservationForm" :rules="reservationRules" label-width="100px"
        class="reservation-form">
        <!-- 预约日期 -->
        <el-form-item label="预约日期" prop="reservationDate">
          <el-date-picker v-model="reservationForm.reservationDate" type="date" placeholder="请选择预约日期"
            :disabled-date="disabledDate" style="width: 100%" @change="onDateChange"
            value-format="YYYY-MM-DD"></el-date-picker>
        </el-form-item>

        <!-- 预约开始时间 -->
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="reservationForm.startTime" placeholder="请选择开始时间" format="HH:mm" value-format="HH:mm"
            style="width: 100%" @change="onStartTimeChange"></el-time-picker>
        </el-form-item>

        <!-- 预约时长 -->
        <el-form-item label="预约时长" prop="duration">
          <el-input-number v-model="reservationForm.duration" :min="1" :max="10" placeholder="请输入预约时长(分钟)"
            style="width: 100%" @change="calculateEndTime">
            <template #append>分钟</template>
          </el-input-number>
          <div class="form-tip">
            <el-icon>
              <InfoFilled />
            </el-icon>
            每次预约时长1-10分钟
          </div>
        </el-form-item>

        <!-- 预约结束时间 -->
        <el-form-item label="结束时间">
          <el-input v-model="reservationForm.endTime" placeholder="自动计算结束时间" readonly disabled
            style="width: 100%; background-color: #f5f7fa;">
            <template #prefix>
              <el-icon>
                <Clock />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 用户备注 -->
        <el-form-item label="备注信息" prop="userNotes">
          <el-input v-model="reservationForm.userNotes" type="textarea" placeholder="如有特殊需求请在此说明（选填）"
            :rows="2"></el-input>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="form-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="submitReservation" :loading="submitting"
            style="background-color: rgb(255, 152, 0); border-color: rgb(255, 152, 0);">
            提交预约
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CircleCheck, InfoFilled, Clock } from '@element-plus/icons-vue';
import { api } from '@/utils/api';
import { getUserId, isLoggedIn } from '@/utils/auth';

// 路由和路由参数
const router = useRouter();
const route = useRoute();

// 状态管理
const submitting = ref(false);
const showSuccessDialog = ref(false);
const reservationFormRef = ref(null);
const selectedCat = ref(null);
const reservationNumber = ref('');
const defaultCatImage = 'https://picsum.photos/seed/catdefault/400/400';

// 登录状态检查
const isLogin = computed(() => isLoggedIn());
const currentUserId = computed(() => getUserId());

// 预约表单数据
const reservationForm = reactive({
  reservationDate: '',
  startTime: '',
  endTime: '',
  duration: null,
  userNotes: ''
});

// 表单验证规则
const reservationRules = reactive({
  reservationDate: [
    { required: true, message: '请选择预约日期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入预约时长', trigger: 'change' },
    { type: 'number', min: 1, max: 10, message: '预约时长必须在1-10分钟之间', trigger: 'change' }
  ]
});

// 禁用日期（今天之前的日期）
const disabledDate = (date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date.getTime() < today.getTime();
};

// 开始时间改变处理
const onStartTimeChange = () => {
  // 如果有时长，重新计算结束时间
  if (reservationForm.duration && reservationForm.startTime) {
    calculateEndTime();
  }
};

// 计算结束时间
const calculateEndTime = () => {
  if (reservationForm.startTime && reservationForm.duration) {
    const [hours, minutes] = reservationForm.startTime.split(':').map(Number);
    const startDate = new Date();
    startDate.setHours(hours, minutes, 0, 0);

    // 添加预约时长（分钟）
    const endDate = new Date(startDate.getTime() + reservationForm.duration * 60000);

    // 格式化为 HH:mm
    const endHours = endDate.getHours().toString().padStart(2, '0');
    const endMinutes = endDate.getMinutes().toString().padStart(2, '0');
    reservationForm.endTime = `${endHours}:${endMinutes}`;
  } else {
    reservationForm.endTime = '';
  }
};

// 页面加载时初始化
onMounted(() => {
  // 检查登录状态
  if (!isLogin.value) {
    ElMessageBox.confirm(
      '预约撸猫需要先登录账号，是否立即前往登录？',
      '需要登录',
      {
        confirmButtonText: '登录',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      router.push('/login');
    }).catch(() => {
      router.push('/CatGuidePage');
    });
    return;
  }

  // 获取猫咪ID
  const catId = route.query.catId;
  if (!catId) {
    ElMessage.error('未指定猫咪信息');
    router.push('/CatGuidePage');
    return;
  }

  // 获取猫咪详情
  getCatDetail(catId);
});

// 图片URL处理函数
const getProcessedCatImageUrl = (url) => {
  if (!url) {
    return defaultCatImage;
  }
  
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http')) {
    return url;
  }
  
  // 处理不同类型的路径
  let processedUrl;
  
  if (url.startsWith('/uploads/cats/')) {
    // 已经是正确的上传路径格式 - 指向管理员端
    processedUrl = `http://localhost:8081${url}`;
  } else if (url.startsWith('uploads/cats/')) {
    // 缺少开头斜杠
    processedUrl = `http://localhost:8081/${url}`;
  } else if (url.startsWith('/cats/')) {
    // cats目录下的图片
    processedUrl = `http://localhost:8081${url}`;
  } else if (url.startsWith('cats/')) {
    // cats目录下的图片（缺少斜杠）
    processedUrl = `http://localhost:8081/${url}`;
  } else {
    // 其他情况，尝试作为相对路径处理
    processedUrl = `http://localhost:8081/${url}`;
  }
  
  return processedUrl;
};

// 获取猫咪详情
const getCatDetail = async (catId) => {
  try {
    const response = await api.get(`/cats/detail/${catId}`);
    if (response.code === 200) {
      selectedCat.value = response.data;
    } else {
      ElMessage.error('获取猫咪信息失败');
      router.push('/CatGuidePage');
    }
  } catch (error) {
    ElMessage.error('获取猫咪信息失败：' + error.message);
    router.push('/CatGuidePage');
  }
};

// 格式化年龄
const formatAge = (months) => {
  if (!months) return '未知';
  const years = Math.floor(months / 12);
  const remainMonths = months % 12;
  let result = '';
  if (years > 0) result += `${years}年`;
  if (remainMonths > 0) result += `${remainMonths}月`;
  return result || '1个月以下';
};

// 获取健康状态文本
const getHealthStatusText = (status) => {
  const statusMap = {
    HEALTHY: '健康',
    SICK: '生病',
    RECOVERING: '恢复中'
  };
  return statusMap[status] || '未知状态';
};

// 日期变化处理
const onDateChange = (date) => {
  console.log('选择日期:', date);
};

// 提交预约
const submitReservation = async () => {
  // 表单验证
  const valid = await reservationFormRef.value.validate().catch(() => false);
  if (!valid) {
    ElMessage.warning('请完善预约信息');
    return;
  }

  // 检查是否已存在当天预约
  if (await checkExistingReservation()) {
    ElMessage.error('您今天已经预约过了，每天只能预约一次');
    return;
  }

  submitting.value = true;

  try {
    // 构造符合后端要求的 timeSlot 字段
    const timeSlot = `${reservationForm.startTime}-${reservationForm.endTime}`;

    const reservationData = {
      userId: currentUserId.value,
      catId: selectedCat.value.id,
      reservationDate: reservationForm.reservationDate,
      timeSlot: timeSlot,
      visitorCount: 1,
      duration: reservationForm.duration,
      userNotes: reservationForm.userNotes,
      status: 'PENDING'
    };

    console.log('提交的预约数据:', reservationData);

    const response = await api.post('/reservations/create', reservationData);

    if (response.code === 200) {
      reservationNumber.value = response.data.id;

      // 提醒用户并跳转到猫咪图鉴页面
      ElMessage.success('预约提交成功！感谢您的预约，即将跳转到猫咪图鉴页面...');

      // 延迟跳转，给用户时间看到成功消息
      setTimeout(() => {
        router.push('/CatGuidePage');
      }, 2000);

      // 重置表单
      reservationFormRef.value.resetFields();
      reservationForm.duration = null;
      reservationForm.endTime = '';
    } else {
      ElMessage.error(response.message || '预约提交失败');
    }
  } catch (error) {
    console.error('预约提交失败:', error);
    ElMessage.error('预约提交失败：' + error.message);
  } finally {
    submitting.value = false;
  }
};

// 检查当天是否已有预约
const checkExistingReservation = async () => {
  try {
    const response = await api.get(`/reservations/user/${currentUserId.value}`);
    if (response.code === 200) {
      const today = new Date().toISOString().split('T')[0];
      return response.data.some(reservation =>
        reservation.reservationDate === today
      );
    }
    return false;
  } catch (error) {
    console.error('检查预约记录失败：', error);
    return false;
  }
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};
</script>

<style scoped>
.cat-avatar-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 10px;
  border: 1px dashed #ddd;
}

.cat-avatar-placeholder p {
  margin-top: 10px;
  color: #999;
  font-size: 14px;
}

.appointment-cats-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #5d4037;
  margin: 15px 0;
}

.page-header p {
  font-size: 16px;
  color: #795548;
  margin: 0;
}

/* 猫咪信息区域 */
.cat-info-section {
  margin-bottom: 30px;
}

.cat-card {
  border-radius: 15px;
  overflow: hidden;
}

.cat-avatar {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 10px;
}

.cat-details h2 {
  margin: 0 0 15px;
  color: #5d4037;
  font-size: 24px;
}

.cat-desc {
  margin: 15px 0;
}

.cat-desc span {
  margin-right: 20px;
  font-size: 14px;
  color: #795548;
}

.breed {
  background-color: #fff3e0;
  padding: 3px 10px;
  border-radius: 12px;
  color: #e65100;
}

.personality,
.health-status {
  margin: 10px 0;
  color: #795548;
  font-size: 14px;
}

/* 预约表单 */
.reservation-card {
  border-radius: 15px;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
  color: #5d4037;
}

.reservation-form {
  padding: 20px 0;
}

.form-tip {
  margin: 5px 0 0;
  font-size: 12px;
  color: #e65100;
  display: flex;
  align-items: center;
}

.form-tip .el-icon {
  margin-right: 5px;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
}

.form-actions button {
  margin: 0 10px;
  padding: 12px 30px;
}

/* 结束时间样式 - 明确显示为只读 */
.el-input__inner[readonly] {
  background-color: #f5f7fa !important;
  cursor: not-allowed;
  color: #666;
}

.el-input__inner[disabled] {
  background-color: #f5f7fa !important;
  cursor: not-allowed;
  color: #666;
}

/* 成功弹窗 */
.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-content h3 {
  margin: 20px 0 10px;
  color: #5d4037;
}

.success-content p {
  color: #795548;
  margin: 5px 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .appointment-cats-page {
    padding: 15px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .cat-avatar {
    height: 150px;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 24px;
  }

  .cat-details h2 {
    font-size: 20px;
  }

  .form-actions button {
    padding: 10px 20px;
    margin: 5px;
  }
}
</style>