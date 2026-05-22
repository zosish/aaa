<!-- 预约撸猫套餐页面 -->
<template>
  <Layout>
    <div class="reservation-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>预约撸猫</h1>
        <p>选择心仪的猫咪和时段，享受温馨的撸猫时光</p>
      </div>

    <!-- 套餐活动展示 -->
    <section class="section activities-section">
      <div class="section-header">
        <h2>精选套餐</h2>
        <p class="section-desc">为您提供不同需求的专属撸猫套餐</p>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loadingPackages" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div class="reservation-activity-grid">
              <div v-for="i in 3" :key="i" class="skeleton-card">
                <el-skeleton-item variant="text" style="width: 60%; height: 24px; margin-bottom: 15px" />
                <el-skeleton-item variant="text" style="width: 100%; height: 60px; margin-bottom: 15px" />
                <el-skeleton-item variant="text" style="width: 80%; height: 20px; margin-bottom: 10px" />
                <el-skeleton-item variant="text" style="width: 70%; height: 20px; margin-bottom: 20px" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- 套餐列表 -->
      <div v-else class="reservation-activity-grid">
        <div 
          v-for="activity in packageActivities" 
          :key="activity.id"
          class="reservation-activity-card"
        >
          <div class="activity-card-header">
            <h3>{{ activity.title }}</h3>
            <div class="activity-price">¥{{ activity.setmealPrice || 0 }}</div>
          </div>
          <div class="activity-card-content">
            <p class="slot-desc">{{ activity.content }}</p>
            <!-- <div class="package-info">
              <span class="duration"><el-icon><Clock /></el-icon> {{ calculateDuration(activity.startTime, activity.endTime) }}</span>
            </div> -->
            <ul class="activity-benefits">
              <li><el-icon><Check /></el-icon> 专业人员陪同</li>
              <li><el-icon><Check /></el-icon> 免费饮品一杯</li>
              <li><el-icon><Check /></el-icon> 拍照指导服务</li>
            </ul>
          </div>
          <div class="activity-card-footer">
            <el-button type="primary" size="small" @click="selectPackage(activity)" :loading="selectingPackage">
              选择套餐
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loadingPackages && packageActivities.length === 0" class="empty-state">
        <el-empty description="暂无套餐活动">
          <el-button type="primary" @click="loadPackageActivities">刷新</el-button>
        </el-empty>
      </div>
    </section>

    <!-- 选择预约时间弹窗 -->
    <el-dialog v-model="showTimeSelectionDialog" title="选择预约时间" width="500px" :close-on-click-modal="false">
      <div v-if="selectedPackage" class="time-selection">
        <div class="package-summary">
          <h3>{{ selectedPackage.title }}</h3>
          <p class="price">价格：¥{{ selectedPackage.setmealPrice }}</p>
        </div>
        
        <el-form ref="timeFormRef" :model="timeForm" :rules="timeRules" label-width="80px">
          <el-form-item label="预约日期" prop="reservationDate">
            <el-date-picker
              v-model="timeForm.reservationDate"
              type="date"
              placeholder="请选择预约日期"
              :disabled-date="disabledDate"
              style="width: 100%"
              value-format="YYYY-MM-DD"
              @change="onDateChange"
            />
          </el-form-item>
          
          <el-form-item label="预约开始时间" prop="startTime">
            <el-time-picker
              v-model="timeForm.startTime"
              placeholder="请选择开始时间"
              format="HH:mm"
              value-format="HH:mm"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="预约时长" prop="duration">
            <el-select v-model="timeForm.duration" placeholder="请选择时长" style="width: 100%">
              <el-option label="30分钟" :value="30"></el-option>
              <el-option label="60分钟" :value="60"></el-option>
              <el-option label="90分钟" :value="90"></el-option>
              <el-option label="120分钟" :value="120"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="访客人数" prop="visitorCount">
            <el-input-number v-model.number="timeForm.visitorCount" :min="1" :max="10" label="人数"
              placeholder="请输入人数" style="width: 100%">
            </el-input-number>
          </el-form-item>
          
          <el-form-item label="备注信息">
            <el-input
              v-model="timeForm.notes"
              type="textarea"
              placeholder="如有特殊需求请在此说明（选填）"
              :rows="3"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTimeSelectionDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmTimeSelection" 
            :loading="timeSelecting"
          >
            确认预约时间
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 桌号选择弹窗 -->
    <el-dialog v-model="showTableSelectionDialog" title="选择桌号" width="500px" :close-on-click-modal="false">
      <div v-if="selectedPackage && availableTables.length > 0" class="table-selection">
        <div class="package-summary">
          <h3>{{ selectedPackage.title }}</h3>
          <p class="info">日期：{{ confirmedReservation.reservationDate }}</p>
          <p class="info">时间：{{ confirmedReservation.startTime }} ({{ confirmedReservation.duration }}分钟)</p>
          <p class="info">人数：{{ confirmedReservation.visitorCount }}人</p>
        </div>
        
        <div class="table-grid">
          <div 
            v-for="table in availableTables" 
            :key="table.id"
            class="table-item"
            :class="{ selected: selectedTable?.id === table.id }"
            @click="selectTable(table)"
          >
            <div class="table-number">{{ table.tableNumber }}</div>
            <div class="table-capacity">{{ table.capacity }}人桌</div>
          </div>
        </div>
        
        <div v-if="!selectedTable" class="warning-text">
          <el-icon><AlertCircle /></el-icon>
          请选择一个桌号
        </div>
      </div>
      
      <div v-else class="empty-state">
        <el-empty description="当前时段暂无可用桌号">
          <el-button type="primary" @click="goBackToTimeSelection">重新选择时间</el-button>
        </el-empty>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="goBackToTimeSelection">上一步</el-button>
          <el-button 
            type="primary" 
            @click="confirmTableSelection" 
            :loading="tableSelecting"
            :disabled="!selectedTable"
          >
            确认桌号
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 套餐支付弹窗 -->
    <el-dialog v-model="showPaymentDialog" title="套餐支付" width="500px">
      <div v-if="selectedPackage" class="package-payment">
        <div class="package-info">
          <h3>{{ selectedPackage.title }}</h3>
          <p>{{ selectedPackage.content }}</p>
          <div class="reservation-summary">
            <p><strong>预约日期：</strong>{{ confirmedReservation.reservationDate }}</p>
            <p><strong>预约时间：</strong>{{ confirmedReservation.startTime }} ({{ confirmedReservation.duration }}分钟)</p>
            <p><strong>访客人数：</strong>{{ confirmedReservation.visitorCount }}人</p>
            <p><strong>桌号：</strong>{{ confirmedReservation.tableNumber }}</p>
            <p v-if="confirmedReservation.notes"><strong>备注：</strong>{{ confirmedReservation.notes }}</p>
          </div>
          <div class="package-meta">
            <span class="price">¥{{ selectedPackage.setmealPrice }}</span>
          </div>
        </div>
        
        <div class="payment-methods">
          <el-radio-group v-model="selectedPaymentMethod">
            <el-radio label="ALIPAY" border>
              <div class="payment-option">
                <span>支付宝支付</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelPayment">取消</el-button>
          <el-button 
            type="primary" 
            @click="proceedToPayment" 
            :loading="paymentLoading"
          >
            立即支付
          </el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </Layout>
</template>
<!-- eslint-disable no-unused-vars -->
<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Check, Clock, AlertCircle } from '@element-plus/icons-vue';
import { api } from '@/utils/api';
import { getUserId, getUserInfo, isLoggedIn } from '@/utils/auth';
import Layout from './AppLayout.vue';

// 路由实例
const router = useRouter();

// 状态管理
const selectingPackage = ref(false);
const timeSelecting = ref(false);
const tableSelecting = ref(false);
const tableLoading = ref(false);
const paymentLoading = ref(false);
const showTimeSelectionDialog = ref(false);
const showTableSelectionDialog = ref(false);
const showPaymentDialog = ref(false);
const showSuccessDialog = ref(false);
const selectedPackage = ref(null);
const selectedTable = ref(null);
const loadingPackages = ref(false);
const selectedPaymentMethod = ref('ALIPAY');
const orderNumber = ref('');

// 登录状态检查
const isLogin = ref(isLoggedIn());
const currentUserId = ref(getUserId());

const getUserUsername = () => {
  const userInfo = getUserInfo();
  return userInfo ? userInfo.username || '' : '';
};

const getUserPhone = () => {
  const userInfo = getUserInfo();
  return userInfo ? userInfo.phone || userInfo.phoneNumber || '' : '';
};

// 数据列表
const packageActivities = ref([]);
const availableTables = ref([]);

// 时间选择表单
const timeFormRef = ref(null);
const timeForm = reactive({
  reservationDate: '',
  startTime: '',
  duration: 60,
  visitorCount: 1,
  notes: ''
});

// 确认的预约信息
const confirmedReservation = reactive({
  reservationDate: '',
  startTime: '',
  duration: 60,
  visitorCount: 1,
  tableId: null,
  tableNumber: '',
  notes: ''
});

// 表单验证规则
const timeRules = reactive({
  reservationDate: [
    { required: true, message: '请选择预约日期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请选择预约时长', trigger: 'change' }
  ],
  visitorCount: [
    { required: true, message: '请输入访客人数', trigger: 'change' },
    { type: 'number', min: 1, max: 10, message: '人数应在1-10人之间', trigger: 'change' }
  ]
});

// 生命周期
onMounted(() => {
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
      router.push('/home');
    });
    return;
  }
  
  // 初始化数据
  initializeData();
});

// 初始化所有数据
const initializeData = async () => {
  try {
    await Promise.all([
      loadPackageActivities()
    ]);
  } catch (error) {
    console.error('初始化数据失败:', error);
    ElMessage.error('数据加载失败，请刷新页面重试');
  }
};

// 加载套餐活动数据
const loadPackageActivities = async () => {
  loadingPackages.value = true;
  try {
    // 查询活动类型为SETMEAL的数据
    const response = await api.post('/activities/selectList', {
      activityType: 'SETMEAL',
      status: 'ACTIVE',
      current: 1,
      size: 100
    });
    
    // 从响应中提取数据，兼容不同的字段名
    const dataList = response.list || response.records || response.data || [];
    packageActivities.value = Array.isArray(dataList) ? dataList : [];
    
    console.log('套餐活动加载成功，数量:', packageActivities.value.length);
  } catch (error) {
    console.error('加载套餐活动失败:', error);
    ElMessage.error('套餐活动加载失败: ' + error.message);
    packageActivities.value = [];
  } finally {
    loadingPackages.value = false;
  }
};

// // 计算活动时长
// const calculateDuration = (startTime, endTime) => {
//   if (!startTime || !endTime) return '未知时长';
  
//   try {
//     const start = new Date(startTime);
//     const end = new Date(endTime);
//     const diffHours = (end - start) / (1000 * 60 * 60);
    
//     if (diffHours >= 1) {
//       return `${Math.floor(diffHours)}小时${(diffHours % 1) * 60 > 0 ? Math.round((diffHours % 1) * 60) + '分钟' : ''}`;
//     } else {
//       return `${Math.round(diffHours * 60)}分钟`;
//     }
//   } catch (error) {
//     return '未知时长';
//   }
// };

// 禁用过去日期
const disabledDate = (date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date.getTime() < today.getTime();
};

// 日期变化处理
const onDateChange = () => {
  // 日期变化时重置时间选择
  timeForm.startTime = '';
};

// 选择套餐
const selectPackage = async (packageActivity) => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  selectingPackage.value = true;
  try {
    selectedPackage.value = packageActivity;
    // 重置表单
    Object.assign(timeForm, {
      reservationDate: '',
      startTime: '',
      duration: 60,
      visitorCount: 1,
      notes: ''
    });
    selectedTable.value = null;
    availableTables.value = [];
    showTimeSelectionDialog.value = true;
  } catch (error) {
    ElMessage.error('选择套餐失败');
    console.error(error);
  } finally {
    selectingPackage.value = false;
  }
};

// 计算时间段 (开始时间-结束时间)
const calculateTimeSlot = (startTime, duration) => {
  if (!startTime || !duration) return '';
  
  const [hours, minutes] = startTime.split(':').map(Number);
  const startDate = new Date();
  startDate.setHours(hours, minutes, 0, 0);
  
  const endDate = new Date(startDate.getTime() + duration * 60000);
  const endHours = endDate.getHours().toString().padStart(2, '0');
  const endMinutes = endDate.getMinutes().toString().padStart(2, '0');
  
  return `${startTime}-${endHours}:${endMinutes}`;
};

// 加载可用桌号
const loadAvailableTables = async () => {
  if (!timeForm.reservationDate || !timeForm.startTime || !timeForm.duration) {
    return;
  }
  
  const timeSlot = calculateTimeSlot(timeForm.startTime, timeForm.duration);
  
  tableLoading.value = true;
  try {
    const response = await api.get('/tables/available/by-time', {
      params: {
        reservationDate: timeForm.reservationDate,
        timeSlot: timeSlot
      }
    });
    
    if (response.code === 200 && response.data && response.data.length > 0) {
      availableTables.value = response.data;
      console.log('可用桌号:', availableTables.value);
    } else {
      // 如果API返回空，使用默认数据
      availableTables.value = [
        { id: 1, tableNumber: '1号桌', capacity: 2, status: 'AVAILABLE' },
        { id: 2, tableNumber: '2号桌', capacity: 2, status: 'AVAILABLE' },
        { id: 3, tableNumber: '3号桌', capacity: 4, status: 'AVAILABLE' },
        { id: 4, tableNumber: '4号桌', capacity: 4, status: 'AVAILABLE' },
        { id: 5, tableNumber: '5号桌', capacity: 6, status: 'AVAILABLE' },
        { id: 6, tableNumber: 'A1包厢', capacity: 8, status: 'AVAILABLE' }
      ];
    }
  } catch (error) {
    console.error('加载可用桌号失败:', error);
    // API调用失败时使用默认数据
    availableTables.value = [
      { id: 1, tableNumber: '1号桌', capacity: 2, status: 'AVAILABLE' },
      { id: 2, tableNumber: '2号桌', capacity: 2, status: 'AVAILABLE' },
      { id: 3, tableNumber: '3号桌', capacity: 4, status: 'AVAILABLE' },
      { id: 4, tableNumber: '4号桌', capacity: 4, status: 'AVAILABLE' },
      { id: 5, tableNumber: '5号桌', capacity: 6, status: 'AVAILABLE' },
      { id: 6, tableNumber: 'A1包厢', capacity: 8, status: 'AVAILABLE' }
    ];
    ElMessage.warning('使用示例桌号数据，请确保后端服务已启动');
  } finally {
    tableLoading.value = false;
  }
};

// 确认预约时间
const confirmTimeSelection = async () => {
  if (!timeFormRef.value) return;
  
  try {
    await timeFormRef.value.validate();
    
    // 保存预约信息
    Object.assign(confirmedReservation, {
      reservationDate: timeForm.reservationDate,
      startTime: timeForm.startTime,
      duration: timeForm.duration,
      visitorCount: timeForm.visitorCount,
      notes: timeForm.notes
    });
    
    // 加载可用桌号
    await loadAvailableTables();
    
    if (availableTables.value.length === 0) {
      ElMessage.warning('当前时间暂无可用桌号，请选择其他时间');
      return;
    }
    
    showTimeSelectionDialog.value = false;
    showTableSelectionDialog.value = true;
  } catch (error) {
    ElMessage.error('请完善预约信息');
  }
};

// 选择桌号
const selectTable = (table) => {
  selectedTable.value = table;
};

// 确认桌号选择
const confirmTableSelection = () => {
  if (!selectedTable.value) {
    ElMessage.warning('请选择一个桌号');
    return;
  }
  
  confirmedReservation.tableId = selectedTable.value.id;
  confirmedReservation.tableNumber = selectedTable.value.tableNumber;
  
  showTableSelectionDialog.value = false;
  showPaymentDialog.value = true;
};

// 返回时间选择
const goBackToTimeSelection = () => {
  showTableSelectionDialog.value = false;
  showTimeSelectionDialog.value = true;
};

// 取消支付
const cancelPayment = () => {
  ElMessageBox.confirm('确定要取消支付吗？预约信息将被保留', '取消支付', {
    confirmButtonText: '确定取消',
    cancelButtonText: '继续支付',
    type: 'warning'
  }).then(() => {
    showPaymentDialog.value = false;
    showTableSelectionDialog.value = true;
  }).catch(() => {
    // 用户选择继续支付
  });
};

// 进行支付
const proceedToPayment = async () => {
  if (!selectedPackage.value) {
    ElMessage.error('请选择套餐');
    return;
  }

  paymentLoading.value = true;
  try {
    // 创建订单
    const timeSlot = calculateTimeSlot(confirmedReservation.startTime, confirmedReservation.duration);
    const orderData = {
      userId: currentUserId.value,
      totalAmount: selectedPackage.value.setmealPrice,
      customerNotes: `套餐预约：${selectedPackage.value.title} | 日期：${confirmedReservation.reservationDate} | 时间：${confirmedReservation.startTime} (${confirmedReservation.duration}分钟) | 桌号：${confirmedReservation.tableNumber}`,
      items: [{
        productId: selectedPackage.value.id,
        productName: selectedPackage.value.title,
        productPrice: selectedPackage.value.setmealPrice,
        quantity: 1,
        subtotal: selectedPackage.value.setmealPrice,
        itemType: 'ACTIVITY'
      }]
    };

    console.log('创建订单请求数据:', orderData);
    const orderResult = await api.post('/orders/create', orderData);
    console.log('订单创建响应:', orderResult);

    // 检查订单创建是否成功
    if (!orderResult || orderResult.code !== 200 || !orderResult.data || !orderResult.data.orderNumber) {
      throw new Error('订单创建失败：' + (orderResult?.message || '未知错误'));
    }

    orderNumber.value = orderResult.data.orderNumber;

    // 创建预约记录（使用正确的日期格式 yyyy-MM-dd HH:mm:ss）
    const reservationData = {
      userId: currentUserId.value,
      username: getUserUsername(),
      phone: getUserPhone(),
      activityId: selectedPackage.value.id,
      reservationDate: confirmedReservation.reservationDate,
      timeSlot: timeSlot,
      reservationTime: `${confirmedReservation.reservationDate} ${confirmedReservation.startTime}:00`,
      duration: confirmedReservation.duration,
      visitorCount: confirmedReservation.visitorCount,
      tableId: confirmedReservation.tableId,
      tableNumber: confirmedReservation.tableNumber,
      status: 'PENDING',
      userNotes: confirmedReservation.notes || ''
    };

    try {
      const reservationResult = await api.post('/reservations/create', reservationData);
      console.log('预约记录创建成功:', reservationResult);
    } catch (reservationError) {
      console.warn('预约记录创建失败，但订单已创建:', reservationError);
    }

    // 发起支付
    const paymentData = {
      orderNumber: orderResult.data.orderNumber,
      amount: selectedPackage.value.setmealPrice,
      subject: `套餐预约：${selectedPackage.value.title}`
    };

    console.log('发起支付请求数据:', paymentData);
    const paymentResponse = await api.post('/payment/alipay/create', paymentData);
    console.log('支付响应类型:', typeof paymentResponse);

    // 处理支付宝返回的HTML表单
    if (typeof paymentResponse === 'string') {
      if (paymentResponse.includes('<form')) {
        // 创建临时表单并提交
        const formContainer = document.createElement('div');
        formContainer.innerHTML = paymentResponse;
        const form = formContainer.querySelector('form');
        
        if (form) {
          // 在新窗口中打开支付页面
          const paymentWindow = window.open('', '_blank', 'width=800,height=600,scrollbars=yes,resizable=yes');
          if (paymentWindow) {
            paymentWindow.document.write(paymentResponse);
            paymentWindow.document.close();
            ElMessage.success('正在跳转到支付页面...');
            showPaymentDialog.value = false;
            setTimeout(() => {
              showSuccessDialog.value = true;
            }, 2000);
          } else {
            ElMessage.warning('支付页面已在新窗口打开，请检查浏览器弹窗设置');
            const link = document.createElement('a');
            link.href = 'data:text/html;charset=utf-8,' + encodeURIComponent(paymentResponse);
            link.target = '_blank';
            link.click();
            showPaymentDialog.value = false;
            showSuccessDialog.value = true;
          }
        } else {
          throw new Error('支付表单格式错误');
        }
      } else {
        ElMessage.success('支付成功！');
        showPaymentDialog.value = false;
        showSuccessDialog.value = true;
      }
    } else {
      ElMessage.success('支付成功！');
      showPaymentDialog.value = false;
      showSuccessDialog.value = true;
    }
    
  } catch (error) {
    ElMessage.error('支付发起失败：' + error.message);
    console.error('支付失败详细信息:', error);
  } finally {
    paymentLoading.value = false;
  }
};
</script>

<style scoped>
/* 页面整体样式，继承首页背景色 */
.reservation-page {
  min-height: calc(100vh - 70px);
  background-color: #fff9f5;
  padding: 30px 40px;
}

/* 页面标题，与首页区块标题风格一致 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  color: #5d4037;
  margin: 0 0 10px;
}

.page-header p {
  font-size: 16px;
  color: #795548;
  margin: 0;
}

/* 通用区块样式，复用首页 */
.section {
  margin-bottom: 60px;
}

.section-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 25px;
}

.section-header h2 {
  font-size: 24px;
  color: #5d4037;
  margin: 0 0 8px;
}

.section-desc {
  font-size: 14px;
  color: #795548;
  margin: 0;
}

/* 预约活动卡片网格，复用首页猫咪/商品卡片布局 */
.reservation-activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

/* 预约活动卡片，复用首页预约区域卡片视觉风格 */
.reservation-activity-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  background-color: #fff3e0; /* 与首页预约区域背景色一致 */
  cursor: pointer;
}

.reservation-activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(255, 167, 71, 0.15);
}

/* 卡片头部：时段+剩余名额 */
.activity-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px 10px;
  border-bottom: 1px solid rgba(229, 115, 115, 0.2);
}

.activity-card-header h3 {
  font-size: 18px;
  color: #e65100;
  margin: 0;
}

.activity-price {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

/* 卡片内容：描述+福利 */
.activity-card-content {
  padding: 15px 20px;
}

.slot-desc {
  font-size: 14px;
  color: #795548;
  margin: 0 0 10px;
  line-height: 1.5;
}

.package-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 10px 0;
  font-size: 13px;
  color: #5d4037;
}

.duration {
  display: flex;
  align-items: center;
  gap: 5px;
}

.activity-benefits {
  list-style: none;
  padding: 0;
  margin: 0;
}

.activity-benefits li {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #5d4037;
  margin-bottom: 5px;
}

.activity-benefits li .el-icon {
  color: #4caf50;
  margin-right: 8px;
  font-size: 14px;
}

/* 卡片底部：按钮 */
.activity-card-footer {
  padding: 10px 20px 15px;
  text-align: center;
}

/* 加载状态 */
.loading-container {
  padding: 20px 0;
}

.skeleton-card {
  border-radius: 15px;
  overflow: hidden;
  background-color: white;
  padding: 20px;
}

/* 空状态 */
.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 时间选择弹窗样式 */
.time-selection {
  padding: 20px 0;
}

/* 桌号选择弹窗样式 */
.table-selection {
  padding: 20px 0;
}

.table-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-top: 20px;
}

.table-item {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fff;
}

.table-item:hover {
  border-color: #ff9800;
  background-color: #fff8f0;
}

.table-item.selected {
  border-color: #ff9800;
  background-color: #fff3e0;
  box-shadow: 0 0 10px rgba(255, 152, 0, 0.2);
}

.table-number {
  font-size: 24px;
  font-weight: bold;
  color: #5d4037;
  margin-bottom: 5px;
}

.table-capacity {
  font-size: 14px;
  color: #795548;
}

.warning-text {
  text-align: center;
  color: #e65100;
  margin-top: 20px;
  font-size: 14px;
}

.warning-text .el-icon {
  margin-right: 5px;
}

.package-summary {
  background-color: #fff8f0;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.package-summary h3 {
  color: #5d4037;
  margin: 0 0 10px;
}

.price {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

/* 支付弹窗样式 */
.package-payment {
  padding: 20px 0;
}

.package-info h3 {
  color: #5d4037;
  margin: 0 0 10px;
  font-size: 20px;
}

.package-info p {
  color: #795548;
  margin: 0 0 15px;
  line-height: 1.5;
}

.reservation-summary {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin: 15px 0;
}

.reservation-summary p {
  margin: 5px 0;
  font-size: 14px;
}

.package-meta {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 10px 15px;
  background-color: #fff8f0;
  border-radius: 8px;
  margin-top: 15px;
}

.payment-methods {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 成功弹窗样式 */
.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 20px;
}

.success-content h3 {
  margin: 0 0 15px;
  color: #5d4037;
}

.success-content p {
  color: #795548;
  margin: 5px 0;
}

@media (max-width: 768px) {
  .reservation-page {
    padding: 20px 15px;
  }
  
  .reservation-activity-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .package-meta {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
}
</style>