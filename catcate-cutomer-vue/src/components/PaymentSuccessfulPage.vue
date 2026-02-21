<!-- 支付成功页面 -->
<template>
  <div class="pay-success-page">
    <div class="success-container">
      <el-result
        icon="success"
        title="支付成功"
        sub-title="您的订单已支付成功，我们会尽快为您发货"
      >
        <template #extra>
          <div class="order-info" v-if="orderInfo">
            <p><strong>订单号：</strong>{{ orderInfo.orderNumber }}</p>
            <p><strong>支付金额：</strong><span class="amount">¥{{ orderInfo.amount }}</span></p>
            <p><strong>支付时间：</strong>{{ orderInfo.payTime }}</p>
            <p><strong>支付方式：</strong><span class="payment-method">{{ orderInfo.paymentMethod }}</span></p>
          </div>
          
          <div class="order-details" v-if="orderItems.length > 0">
            <h3>订单商品</h3>
            <div class="item-list">
              <div v-for="item in orderItems" :key="item.id" class="order-item">
                <img :src="item.image" :alt="item.name" class="item-image">
                <div class="item-info">
                  <p class="item-name">{{ item.name }}</p>
                  <p class="item-spec">数量：{{ item.quantity }}  单价：¥{{ item.price }}</p>
                </div>
                <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              </div>
            </div>
          </div>

          <div class="action-buttons">
            <el-button type="primary" @click="viewOrder">查看订单详情</el-button>
            <el-button @click="continueShopping">继续购物</el-button>
            <el-button type="success" @click="goToHomepage">返回首页</el-button>
          </div>
        </template>
      </el-result>
    </div>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { api } from '../utils/api';
import { getUserId } from '../utils/auth';

const route = useRoute();
const router = useRouter();

const orderInfo = ref(null);
const orderItems = ref([]);

onMounted(async () => {
  const orderNumber = route.query.orderNumber || route.query.out_trade_no;

  if (orderNumber) {
    try {
      // 直接显示从支付宝回调传递过来的订单信息
      // 不再主动查询数据库，因为支付宝同步回调已经更新了订单状态
      orderInfo.value = {
        orderNumber: orderNumber,
        amount: route.query.total_amount || '0.00',
        payTime: new Date().toLocaleString(),
        paymentMethod: '支付宝'
      };
      
      ElMessage.success('支付成功！');
    } catch (error) {
      console.error('处理支付成功信息失败:', error);
      ElMessage.error('页面加载异常，请稍后重试');
    }
  } else {
    ElMessage.warning('缺少订单信息');
  }
});

const viewOrder = () => {
  if (orderInfo.value && orderInfo.value.orderNumber) {
    router.push(`/order-detail?orderNumber=${orderInfo.value.orderNumber}`);
  } else {
    ElMessage.info('订单详情功能待开发');
  }
};

const continueShopping = () => {
  router.push('/ProductPage');
};

const goToHomepage = () => {
  router.push('/HomePage');
};
</script>

<style scoped>
.pay-success-page {
  min-height: calc(100vh - 70px);
  background: linear-gradient(135deg, #fff9f5 0%, #fff 100%);
  padding: 30px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-container {
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.order-info {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 12px;
  margin: 25px 0;
}

.order-info p {
  margin: 12px 0;
  color: #5d4037;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount {
  color: #e74c3c;
  font-size: 20px;
  font-weight: bold;
}

.payment-method {
  color: #1677ff;
  font-weight: 500;
}

.order-details {
  margin: 30px 0;
}

.order-details h3 {
  color: #5d4037;
  margin-bottom: 20px;
  text-align: center;
}

.item-list {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background: white;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.order-item:last-child {
  margin-bottom: 0;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 15px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 500;
  color: #333;
  margin: 0 0 8px 0;
}

.item-spec {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.item-subtotal {
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .success-container {
    padding: 20px;
    margin: 10px;
  }
  
  .order-item {
    flex-direction: column;
    text-align: center;
  }
  
  .item-image {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>