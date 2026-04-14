<!-- 用户个人信息页面 -->
 <!-- 个人中心页面 -->
<template>
  <div class="user-profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="$router.push('/HomePage')">首页</el-breadcrumb-item>
        <el-breadcrumb-item>个人中心</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>个人中心</h1>
    </div>

    <div class="profile-container">
      <!-- 侧边栏菜单 -->
      <div class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="profile-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="basic">
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="reservations">
            <el-icon><Calendar /></el-icon>
            <span>我的预约</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="reviews">
            <el-icon><ChatLineSquare /></el-icon>
            <span>我的评价</span>
          </el-menu-item>
          <el-menu-item index="addresses">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </el-menu-item>
          <el-menu-item index="security">
            <el-icon><Lock /></el-icon>
            <span>账户安全</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 基本信息面板 -->
        <div v-if="activeMenu === 'basic'" class="content-panel">
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <span>个人信息</span>
                <el-button 
                  v-if="!isEditing" 
                  type="primary" 
                  size="small" 
                  @click="startEdit"
                >
                  编辑信息
                </el-button>
                <div v-else>
                  <el-button size="small" @click="cancelEdit">取消</el-button>
                  <el-button type="primary" size="small" @click="saveProfile" :loading="saving">
                    保存
                  </el-button>
                </div>
              </div>
            </template>
            
            <el-form 
              :model="profileForm" 
              :rules="profileRules" 
              ref="profileFormRef" 
              label-width="100px"
              :disabled="!isEditing"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="头像">
                    <el-upload
                      class="avatar-uploader"
                      action="#"
                      :auto-upload="false"
                      :show-file-list="false"
                      :on-change="handleAvatarChange"
                      accept="image/*"
                    >
                      <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar">
                      <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                    </el-upload>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="profileForm.username" />
                  </el-form-item>
                </el-col>
              </el-row>
              
                            <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="profileForm.phone" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="profileForm.email" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-input 
                      v-model="profileForm.status" 
                      disabled 
                      placeholder="用户状态"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="创建时间">
                <el-input 
                  v-model="profileForm.createTime" 
                  disabled 
                  placeholder="账户创建时间"
                />
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- 我的订单面板 -->
        <div v-if="activeMenu === 'orders'" class="content-panel">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的订单</span>
                <el-button type="primary" size="small" @click="$router.push('/my-orders')">
                  查看全部订单
                </el-button>
              </div>
            </template>
            
            <div v-if="recentOrders.length > 0">
              <div 
                v-for="order in recentOrders" 
                :key="order.id" 
                class="order-item"
                @click="viewOrderDetail(order)"
              >
                <div class="order-header">
                  <span class="order-number">订单号：{{ order.orderNumber }}</span>
                  <el-tag :type="getOrderStatusTagType(order.orderStatus)">
                    {{ getOrderStatusText(order.orderStatus) }}
                  </el-tag>
                </div>
                <div class="order-content">
                  <div class="order-products">
                    <el-image
                      v-for="(item, index) in order.items.slice(0, 3)"
                      :key="index"
                      :src="item.productImage"
                      class="product-thumb"
                      fit="cover"
                    />
                    <div v-if="order.items.length > 3" class="more-products">
                      +{{ order.items.length - 3 }}
                    </div>
                  </div>
                  <div class="order-info">
                    <div class="order-amount">¥{{ order.totalAmount }}</div>
                    <div class="order-date">{{ formatDate(order.createTime) }}</div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无订单记录" />
          </el-card>
        </div>

        <!-- 我的预约面板 -->
        <div v-if="activeMenu === 'reservations'" class="content-panel">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的预约</span>
              </div>
            </template>
            
            <div v-if="reservations.length > 0">
              <div 
                v-for="reservation in reservations" 
                :key="reservation.id" 
                class="reservation-item"
              >
                <div class="reservation-header">
                  <span class="reservation-title">{{ reservation.activityTitle }}</span>
                  <el-tag :type="getReservationStatusType(reservation.status)">
                    {{ getReservationStatusText(reservation.status) }}
                  </el-tag>
                </div>
                <div class="reservation-content">
                  <div class="reservation-info">
                    <div class="info-item">
                      <el-icon><Calendar /></el-icon>
                      <span>{{ formatDate(reservation.reservationTime) }}</span>
                    </div>
                    <div class="info-item">
                      <el-icon><User /></el-icon>
                      <span>{{ reservation.participantCount }}人</span>
                    </div>
                  </div>
                  <div class="reservation-actions">
                    <el-button 
                      size="small" 
                      type="primary"
                      @click="viewReservationDetail(reservation)"
                    >
                      查看详情
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无预约记录" />
          </el-card>
        </div>

        <!-- 我的收藏面板 -->
        <div v-if="activeMenu === 'favorites'" class="content-panel">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的收藏</span>
              </div>
            </template>
            
            <div v-if="favorites.length > 0" class="favorites-grid">
              <div 
                v-for="favorite in favorites" 
                :key="favorite.id" 
                class="favorite-item"
                @click="viewProduct(favorite.productId)"
              >
                <el-image :src="favorite.productImage" class="favorite-image" fit="cover" />
                <div class="favorite-info">
                  <h4 class="product-name">{{ favorite.productName }}</h4>
                  <div class="product-price">¥{{ favorite.price }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无收藏商品" />
          </el-card>
        </div>

        <!-- 我的评价面板 -->
        <div v-if="activeMenu === 'reviews'" class="content-panel">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的评价</span>
              </div>
            </template>
            
            <div v-if="myReviews.length > 0">
              <div 
                v-for="review in myReviews" 
                :key="review.id" 
                class="review-item"
              >
                <div class="review-header">
                  <div class="review-product">
                    <el-image :src="review.productImage" class="product-thumb" fit="cover" />
                    <span>{{ review.productName }}</span>
                  </div>
                  <el-rate 
                    :model-value="review.rating" 
                    disabled 
                    :max="5"
                    size="small"
                  />
                </div>
                <div class="review-content">
                  <p>{{ review.content }}</p>
                  <div class="review-date">{{ formatDate(review.createTime) }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无评价记录" />
          </el-card>
        </div>

        <!-- 收货地址面板 -->
        <div v-if="activeMenu === 'addresses'" class="content-panel">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>收货地址</span>
                <el-button type="primary" size="small" @click="showAddressDialog = true">
                  新增收货地址
                </el-button>
              </div>
            </template>
            
            <div v-if="addresses.length > 0" class="addresses-list">
              <div 
                v-for="address in addresses" 
                :key="address.id" 
                class="address-item"
                :class="{ 'default-address': address.isDefault }"
              >
                <div class="address-header">
                  <div class="address-info">
                    <span class="contact-name">{{ address.receiverName }}</span>
                    <span class="contact-phone">{{ address.phone }}</span>
                    <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
                  </div>
                  <div class="address-actions">
                    <el-button size="small" @click="editAddress(address)">编辑</el-button>
                    <el-button 
                      v-if="!address.isDefault" 
                      size="small" 
                      type="success"
                      @click="setDefaultAddress(address.id)"
                    >
                      设为默认
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger"
                      @click="deleteAddress(address.id)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div class="address-detail">
                  {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无收货地址" />
          </el-card>
        </div>

        <!-- 账户安全面板 -->
        <div v-if="activeMenu === 'security'" class="content-panel">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>账户安全</span>
              </div>
            </template>
            
            <div class="security-settings">
              <div class="security-item">
                <div class="security-info">
                  <el-icon><Lock /></el-icon>
                  <div>
                    <h4>登录密码</h4>
                    <p>定期更换密码可以提高账户安全性</p>
                  </div>
                </div>
                <el-button @click="showPasswordDialog = true">修改密码</el-button>
              </div>
              
              <div class="security-item">
                <div class="security-info">
                  <el-icon><Iphone /></el-icon>
                  <div>
                    <h4>手机绑定</h4>
                    <p>已绑定手机号：{{ userProfile.phone || '未绑定' }}</p>
                  </div>
                </div>
                <el-button>更换手机</el-button>
              </div>
              
              <div class="security-item">
                <div class="security-info">
                  <el-icon><Message /></el-icon>
                  <div>
                    <h4>邮箱绑定</h4>
                    <p>已绑定邮箱：{{ userProfile.email || '未绑定' }}</p>
                  </div>
                </div>
                <el-button>更换邮箱</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 地址编辑对话框 -->
    <el-dialog 
      v-model="showAddressDialog" 
      :title="editingAddress ? '编辑收货地址' : '新增收货地址'"
      width="500px"
    >
      <el-form 
        :model="addressForm" 
        :rules="addressRules" 
        ref="addressFormRef" 
        label-width="80px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省市区"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="addressForm.detailAddress" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入详细地址"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="addressSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog 
      v-model="showPasswordDialog" 
      title="修改登录密码"
      width="400px"
    >
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules" 
        ref="passwordFormRef" 
        label-width="80px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="passwordSaving">确定</el-button>
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
  User, Document, Calendar, Star, ChatLineSquare, 
  Location, Lock, Plus, Iphone, Message 
} from '@element-plus/icons-vue';
import { api } from '../utils/api';
import { getUserInfo, getUserId } from '../utils/auth';

const router = useRouter();

// 状态管理
const activeMenu = ref('basic');
const isEditing = ref(false);
const saving = ref(false);
const addressSaving = ref(false);
const passwordSaving = ref(false);
const showAddressDialog = ref(false);
const showPasswordDialog = ref(false);
const editingAddress = ref(null);

// 用户信息
const userProfile = ref({});
const profileForm = reactive({
  avatar: '',
  username: '',
  nickname: '',
  phone: '',
  email: '',
  status: '',
  createTime: ''
});

// 表单引用
const profileFormRef = ref(null);
const addressFormRef = ref(null);
const passwordFormRef = ref(null);

// 数据列表
const recentOrders = ref([]);
const reservations = ref([]);
const favorites = ref([]);
const myReviews = ref([]);
const addresses = ref([]);

// 地址表单
const addressForm = reactive({
  receiverName: '',
  phone: '',
  region: [],
  detailAddress: '',
  isDefault: false
});

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 表单验证规则
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
};

const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ]
};

// 省市区数据（简化版）
const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' }
        ]
      }
    ]
  }
];

// 生命周期
onMounted(() => {
  // 加载用户信息
  loadUserProfile();
  // 加载用户地址
  loadRecentOrders();
  // 加载用户订单
  loadReservations();
  // 加载用户收藏
  loadFavorites();
  // 加载用户评论
  loadMyReviews();
  // 加载用户地址
  loadAddresses();
});

// 菜单切换
const handleMenuSelect = (key) => {
  activeMenu.value = key;
};

// 加载用户信息
const loadUserProfile = async () => {
  try {
    // 从后端获取完整的用户信息
    const response = await api.get('/users/current');
    console.log('后端返回的用户信息:', response);
    
    if (response.code === 200 && response.data) {
      const userData = response.data;
      userProfile.value = userData;
      
      // 初始化表单数据
      Object.keys(profileForm).forEach(key => {
        profileForm[key] = userData[key] || '';
      });
      
      // 同时更新本地存储的用户信息
      localStorage.setItem('userInfo', JSON.stringify(userData));
    } else {
      // 如果后端获取失败，尝试从本地存储获取
      const localUserInfo = getUserInfo();
      if (localUserInfo) {
        userProfile.value = localUserInfo;
        Object.keys(profileForm).forEach(key => {
          profileForm[key] = localUserInfo[key] || '';
        });
      }
      ElMessage.warning(response.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
    // 出错时也尝试从本地存储获取
    const localUserInfo = getUserInfo();
    if (localUserInfo) {
      userProfile.value = localUserInfo;
      Object.keys(profileForm).forEach(key => {
        profileForm[key] = localUserInfo[key] || '';
      });
    }
    ElMessage.error('获取用户信息失败：' + error.message);
  }
};
// 开始编辑
const startEdit = () => {
  isEditing.value = true;
};

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false;
  // 恢复原始数据
  Object.keys(profileForm).forEach(key => {
    profileForm[key] = userProfile.value[key] || '';
  });
};

// 保存个人信息
const saveProfile = async () => {
  try {
    await profileFormRef.value.validate();
    saving.value = true;
    
    const updateData = {
      ...profileForm,
      id: getUserId()
    };
    
    const response = await api.put('/users/profile', updateData);
    if (response && response.code === 200) {
      ElMessage.success('个人信息保存成功');
      isEditing.value = false;
      
      // 保存成功后重新从后端获取最新用户信息
      await loadUserProfile();
    } else {
      ElMessage.error(response.message || '保存失败');
    }
  } catch (error) {
    ElMessage.error('保存失败：' + error.message);
  } finally {
    saving.value = false;
  }
};
// 处理头像上传 - 综合优化方案
const handleAvatarChange = async (file) => {
  // 设置更宽松的大小限制 - 8MB
  const isLt8M = file.size / 1024 / 1024 < 8;
  if (!isLt8M) {
    ElMessage.error('上传头像图片大小不能超过 8MB!');
    return;
  }
  
  try {
    // 对于较大的图片，先进行预压缩
    if (file.size > 3 * 1024 * 1024) { // 大于3MB时进行压缩
      const compressedFile = await compressImage(file.raw, 600, 0.8); // 600px, 80%质量
      profileForm.avatar = compressedFile;
      ElMessage.info('大图片已自动压缩优化');
    } else {
      // 小图片直接转换
      const reader = new FileReader();
      reader.onload = (e) => {
        profileForm.avatar = e.target.result;
      };
      reader.readAsDataURL(file.raw);
    }
  } catch (error) {
    ElMessage.error('图片处理失败：' + error.message);
  }
};

// 图片压缩辅助函数
const compressImage = (file, maxWidth, quality) => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    const image = new Image();
    
    reader.onload = (e) => {
      image.src = e.target.result;
    };
    
    image.onload = () => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      
      let { width, height } = image;
      
      // 按比例缩放
      if (width > maxWidth) {
        height = (height * maxWidth) / width;
        width = maxWidth;
      }
      
      canvas.width = width;
      canvas.height = height;
      
      ctx.drawImage(image, 0, 0, width, height);
      const compressedData = canvas.toDataURL('image/jpeg', quality);
      resolve(compressedData);
    };
    
    reader.readAsDataURL(file);
  });
};

// 加载最近订单
const loadRecentOrders = async () => {
  try {
    const response = await api.get(`/orders/user/${getUserId()}/recent-with-items`, {
      params: { limit: 5 }
    });
    if (response && response.code === 200) {
      recentOrders.value = response.data || [];
    }
  } catch (error) {
    console.error('加载订单失败:', error);
  }
};

// 加载预约信息
const loadReservations = async () => {
  try {
    const response = await api.get(`/reservations/user/${getUserId()}`);
    if (response && response.code === 200) {
      reservations.value = response.data || [];
    }
  } catch (error) {
    console.error('加载预约失败:', error);
  }
};

// 加载收藏商品
const loadFavorites = async () => {
  try {
    const response = await api.get(`/favorites/user/${getUserId()}`);
    if (response && response.code === 200) {
      favorites.value = response.data || [];
    }
  } catch (error) {
    console.error('加载收藏失败:', error);
  }
};

// 加载我的评价
const loadMyReviews = async () => {
  try {
    const response = await api.get(`/reviews/user/${getUserId()}`);
    if (response && response.code === 200) {
      myReviews.value = response.data || [];
    }
  } catch (error) {
    console.error('加载评价失败:', error);
  }
};

// 加载收货地址
const loadAddresses = async () => {
  try {
    const response = await api.get(`/addresses/user/${getUserId()}`);
    if (response && response.code === 200) {
      addresses.value = response.data || [];
    }
  } catch (error) {
    console.error('加载地址失败:', error);
  }
};

// 查看订单详情
const viewOrderDetail = (order) => {
  router.push(`/OrderDetailPage?orderNumber=${order.orderNumber}`);
};

// 查看预约详情
const viewReservationDetail = (reservation) => {
  router.push(`/reservation/${reservation.id}`);
};

// 查看商品详情
const viewProduct = (productId) => {
  router.push(`/product/${productId}`);
};

// 编辑地址
const editAddress = (address) => {
  editingAddress.value = address;
  addressForm.receiverName = address.receiverName;
  addressForm.phone = address.phone;
  addressForm.region = [address.province, address.city, address.district];
  addressForm.detailAddress = address.detailAddress;
  addressForm.isDefault = address.isDefault;
  showAddressDialog.value = true;
};

// 设置默认地址
const setDefaultAddress = async (addressId) => {
  try {
    const userId = getUserId(); // 获取当前用户ID
    const response = await api.put(`/addresses/${addressId}/default?userId=${userId}`);
    if (response && response.code === 200) {
      ElMessage.success('设置默认地址成功');
      loadAddresses();
    }
  } catch (error) {
    ElMessage.error('设置失败：' + error.message);
  }
};
// 删除地址
const deleteAddress = async (addressId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个收货地址吗？', '确认删除', {
      type: 'warning'
    });
    
    const response = await api.delete(`/addresses/${addressId}`);
    if (response && response.code === 200) {
      ElMessage.success('删除成功');
      loadAddresses();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message);
    }
  }
};

// 保存地址
const saveAddress = async () => {
  try {
    await addressFormRef.value.validate();
    addressSaving.value = true;
    
    const addressData = {
      userId: getUserId(),
      receiverName: addressForm.receiverName,
      phone: addressForm.phone,
      province: addressForm.region[0],
      city: addressForm.region[1],
      district: addressForm.region[2],
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault ? 1 : 0
    };
    
    console.log('发送的地址数据:', addressData);
    console.log('编辑中的地址:', editingAddress.value);
    
    let response;
    if (editingAddress.value) {
      // 编辑模式 - 使用 PUT 请求更新现有地址
      const addressId = editingAddress.value.id;
      response = await api.put(`/addresses/${addressId}`, addressData);
      console.log('更新地址响应:', response);
      
      // 如果设置为默认地址，单独调用设置默认地址接口
      if (addressForm.isDefault) {
        try {
          await api.put(`/addresses/${addressId}/default`);
          console.log('设置默认地址成功');
        } catch (defaultError) {
          console.error('设置默认地址失败:', defaultError);
          // 这里可以选择是否回滚地址更新
        }
      }
    } else {
      // 新增模式 - 使用 POST 请求创建新地址
      response = await api.post('/addresses', addressData);
      console.log('创建地址响应:', response);
    }
    
    if (response && response.code === 200) {
      ElMessage.success(editingAddress.value ? '地址更新成功' : '地址添加成功');
      showAddressDialog.value = false;
      resetAddressForm();
      loadAddresses();
    } else {
      const errorMessage = response?.message || '保存失败';
      ElMessage.error(errorMessage);
    }
  } catch (error) {
    console.error('保存地址失败:', error);
    const errorMessage = error?.message || error?.toString() || '保存失败：网络错误';
    ElMessage.error(errorMessage);
  } finally {
    addressSaving.value = false;
  }
};
// 重置地址表单
const resetAddressForm = () => {
  addressForm.receiverName = '';
  addressForm.phone = '';
  addressForm.region = [];
  addressForm.detailAddress = '';
  addressForm.isDefault = false;
  editingAddress.value = null;
};

// 修改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate();
    passwordSaving.value = true;
    
    const passwordData = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      userId: getUserId()
    };
    
    const response = await api.put('/users/password', passwordData);
    if (response && response.code === 200) {
      ElMessage.success('密码修改成功');
      showPasswordDialog.value = false;
      resetPasswordForm();
    }
  } catch (error) {
    ElMessage.error('修改失败：' + error.message);
  } finally {
    passwordSaving.value = false;
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
};

// 格式化用户状态显示
const formatUserStatus = (status) => {
  const statusMap = {
    'ACTIVE': '活跃',
    'INACTIVE': '禁用',
    'PENDING': '待激活'
  };
  return statusMap[status] || '未知状态';
};

// 格式化日期时间显示
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

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('zh-CN');
};

// 订单状态标签类型
const getOrderStatusTagType = (status) => {
  const statusMap = {
    'PENDING': '',
    'PROCESSING': 'primary',
    'SHIPPED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  };
  return statusMap[status] || '';
};

// 订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  };
  return statusMap[status] || status;
};

// 预约状态类型
const getReservationStatusType = (status) => {
  const statusMap = {
    'PENDING': '',
    'CONFIRMED': 'success',
    'CANCELLED': 'danger',
    'COMPLETED': 'success'
  };
  return statusMap[status] || '';
};

// 预约状态文本
const getReservationStatusText = (status) => {
  const statusMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成'
  };
  return statusMap[status] || status;
};
</script>

<style scoped>
.user-profile-page {
  min-height: calc(100vh - 70px);
  background-color: #f5f5f5;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin: 10px 0 0;
}

.profile-container {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
}

.profile-menu {
  border-radius: 8px;
  overflow: hidden;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.content-panel {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-card :deep(.el-card__body) {
  padding: 30px;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader .avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  line-height: 100px;
}

.order-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-products {
  display: flex;
  gap: 10px;
}

.product-thumb {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
}

.more-products {
  width: 50px;
  height: 50px;
  background-color: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.order-info {
  text-align: right;
}

.order-amount {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 5px;
}

.reservation-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 15px;
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reservation-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reservation-info {
  display: flex;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.favorite-item {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.favorite-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.favorite-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.favorite-info {
  padding: 15px;
}

.product-name {
  font-size: 14px;
  margin: 0 0 10px;
  color: #333;
}

.product-price {
  font-size: 16px;
  font-weight: bold;
  color: #e74c3c;
}

.review-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 15px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.review-product {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-content p {
  margin: 0 0 10px;
  color: #666;
  line-height: 1.6;
}

.review-date {
  font-size: 12px;
  color: #999;
}

.addresses-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 15px;
}

.default-address {
  border-color: #409eff;
  background-color: #f0f8ff;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.contact-name {
  font-weight: bold;
  font-size: 16px;
}

.contact-phone {
  color: #666;
}

.address-detail {
  color: #666;
  line-height: 1.6;
}

.security-settings {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 6px;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.security-info h4 {
  margin: 0 0 5px;
  color: #333;
}

.security-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
  
  .order-content,
  .reservation-content,
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .order-info,
  .reservation-actions {
    text-align: left;
  }
}
</style>