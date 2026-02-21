<!-- 预约撸猫套餐页面 -->
<template>
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
            <div class="package-info">
              <!-- <span class="duration"><el-icon><Clock /></el-icon> {{ calculateDuration(activity.startTime, activity.endTime) }}</span> -->
            </div>
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

    <!-- 套餐支付弹窗 -->
    <el-dialog v-model="showPaymentDialog" title="套餐支付" width="500px">
      <div v-if="selectedPackage" class="package-payment">
        <div class="package-info">
          <h3>{{ selectedPackage.title }}</h3>
          <p>{{ selectedPackage.content }}</p>
          <div class="package-meta">
            <!-- <span class="duration"><el-icon><Clock /></el-icon> {{ calculateDuration(selectedPackage.startTime, selectedPackage.endTime) }}</span> -->
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
          <el-button @click="showPaymentDialog = false">取消</el-button>
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
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Check, Clock } from '@element-plus/icons-vue';
import { api } from '@/utils/api';
import { getUserId, isLoggedIn } from '@/utils/auth';

// 路由实例
const router = useRouter();

// 状态管理
const selectingPackage = ref(false);
const paymentLoading = ref(false);
const showPaymentDialog = ref(false);
const selectedPackage = ref(null);
const loadingPackages = ref(false);
const selectedPaymentMethod = ref('ALIPAY');

// 登录状态检查
const isLogin = ref(isLoggedIn());
const currentUserId = ref(getUserId());

// 数据列表
const packageActivities = ref([]);

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
      router.push('/HomePage');
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

// 计算活动时长
const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return '未知时长';
  
  try {
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffHours = (end - start) / (1000 * 60 * 60);
    
    if (diffHours >= 1) {
      return `${Math.floor(diffHours)}小时${(diffHours % 1) * 60 > 0 ? Math.round((diffHours % 1) * 60) + '分钟' : ''}`;
    } else {
      return `${Math.round(diffHours * 60)}分钟`;
    }
  } catch (error) {
    return '未知时长';
  }
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
    showPaymentDialog.value = true;
  } catch (error) {
    ElMessage.error('选择套餐失败');
    console.error(error);
  } finally {
    selectingPackage.value = false;
  }
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
    const orderData = {
      userId: currentUserId.value,
      totalAmount: selectedPackage.value.setmealPrice,
      customerNotes: `套餐预约：${selectedPackage.value.title}`,
      items: [{
        productId: selectedPackage.value.id,
        productName: selectedPackage.value.title,
        productPrice: selectedPackage.value.setmealPrice,
        quantity: 1,
        subtotal: selectedPackage.value.setmealPrice,
        itemType: 'ACTIVITY' // 指定为活动类型，避免库存检查
      }]
    };

    console.log('创建订单请求数据:', orderData);
    const orderResult = await api.post('/orders/create', orderData);
    console.log('订单创建响应:', orderResult);

    // 检查订单创建是否成功
    if (!orderResult || orderResult.code !== 200 || !orderResult.data || !orderResult.data.orderNumber) {
      throw new Error('订单创建失败：' + (orderResult?.message || '未知错误'));
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
    console.log('支付响应内容预览:', typeof paymentResponse === 'string' ? paymentResponse.substring(0, 200) : paymentResponse);

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
          } else {
            // 如果弹窗被阻止，提示用户手动点击
            ElMessage.warning('支付页面已在新窗口打开，请检查浏览器弹窗设置');
            // 创建一个链接让用户手动点击
            const link = document.createElement('a');
            link.href = 'data:text/html;charset=utf-8,' + encodeURIComponent(paymentResponse);
            link.target = '_blank';
            link.click();
            showPaymentDialog.value = false;
          }
        } else {
          throw new Error('支付表单格式错误');
        }
      } else if (paymentResponse.includes('支付请求失败') || paymentResponse.includes('订单不存在') || paymentResponse.includes('订单状态不正确')) {
        // 处理支付失败的情况
        throw new Error(paymentResponse.replace(/<[^>]*>/g, '').trim());
      } else {
        // 其他HTML响应，直接在新窗口打开
        const paymentWindow = window.open('', '_blank', 'width=800,height=600');
        if (paymentWindow) {
          paymentWindow.document.write(paymentResponse);
          paymentWindow.document.close();
          ElMessage.success('正在跳转到支付页面...');
          showPaymentDialog.value = false;
        } else {
          ElMessage.warning('支付页面已在新窗口打开，请检查浏览器弹窗设置');
          const blob = new Blob([paymentResponse], { type: 'text/html' });
          const url = URL.createObjectURL(blob);
          window.open(url, '_blank');
          showPaymentDialog.value = false;
        }
      }
    } else {
      // 如果返回的是JSON数据或其他格式
      ElMessage.success('正在跳转到支付页面...');
      showPaymentDialog.value = false;
      console.log('支付响应不是HTML格式:', paymentResponse);
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

.package-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #fff8f0;
  border-radius: 8px;
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