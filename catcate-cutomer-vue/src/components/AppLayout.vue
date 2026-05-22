<template>
  <div class="app-layout">
    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 侧边目录 -->
      <el-aside width="200px" class="side-menu">
        <div class="logo-container">
          <div class="cat-icon">🐱</div>
          <h1>喵时光猫咖</h1>
        </div>
        <el-menu
          :default-active="currentRoute"
          class="el-menu-vertical-demo"
          @select="handleSideMenuSelect"
          background-color="#fff9f5"
          text-color="#5d4037"
          active-text-color="#e65100"
        >
          <el-menu-item index="home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="CatGuidePage">
            <el-icon><StarFilled /></el-icon>
            <span>猫咪图鉴</span>
          </el-menu-item>
          <el-menu-item index="ReservationActivityPage">
            <el-icon><Calendar /></el-icon>
            <span>预约撸猫</span>
          </el-menu-item>
          <el-menu-item index="CateringMenuPage">
            <el-icon><ShoppingBag /></el-icon>
            <span>店内餐饮</span>
          </el-menu-item>
          <el-menu-item index="ProductPage">
            <el-icon><ShoppingBag /></el-icon>
            <span>宠物用品</span>
          </el-menu-item>
          <el-menu-item index="activities">
            <el-icon><Promotion /></el-icon>
            <span>店内活动</span>
          </el-menu-item>
          <el-sub-menu index="user">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </template>
            <el-menu-item index="profile">个人信息</el-menu-item>
            <el-menu-item index="my-orders">我的订单</el-menu-item>
            <!-- <el-menu-item index="my-reservations">我的预约</el-menu-item> -->
            <el-menu-item index="reviews">我的评价</el-menu-item>
            <el-menu-item index="cart">购物车</el-menu-item>
            <el-menu-item @click="handleLogout">退出登录</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 内容区域 -->
      <el-main class="content-area">
        <slot></slot>
      </el-main>
    </el-container>

    <!-- 页脚 -->
    <el-footer class="main-footer">
      <div class="footer-content">
        <div class="footer-column">
          <h3>喵时光猫咖</h3>
          <p>与猫咪共度美好时光</p>
          <div class="social-icons">
            <el-button icon="Wechat" circle size="small" class="social-btn">微信</el-button>
            <el-button icon="Weibo" circle size="small" class="social-btn">微博</el-button>
            <el-button icon="Instagram" circle size="small" class="social-btn">Instagram</el-button>
          </div>
        </div>
        <div class="footer-column">
          <h4>快速链接</h4>
          <ul class="footer-links">
            <li><a href="#" @click.prevent="$router.push('/home')">首页</a></li>
            <li><a href="#" @click.prevent="$router.push('/cats')">猫咪图鉴</a></li>
            <li><a href="#" @click.prevent="$router.push('/reservation')">预约撸猫</a></li>
            <li><a href="#" @click.prevent="$router.push('/products')">周边商品</a></li>
          </ul>
        </div>
        <div class="footer-column">
          <h4>联系我们</h4>
          <ul class="contact-info">
            <li><el-icon><MapLocation /></el-icon> 北京市朝阳区建国路88号</li>
            <li><el-icon><Phone /></el-icon> 010-12345678</li>
            <li><el-icon><Message /></el-icon> contact@miaoshiguang.com</li>
            <li><el-icon><Clock /></el-icon> 10:00 - 21:00 (周一休息)</li>
          </ul>
        </div>
      </div>
      <div class="copyright">
        © 2023 喵时光猫咖 版权所有
      </div>
    </el-footer>
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  HomeFilled, StarFilled, Calendar, ShoppingBag, Promotion, UserFilled,
  MapLocation, Phone, Message, Clock, Wechat, Weibo, Instagram
} from '@element-plus/icons-vue';
import { isLoggedIn, clearAuth, getUserInfo } from '../utils/auth';

// 路由
const router = useRouter();
const route = useRoute();

// 状态管理
const activeMenu = ref('home');
const userAvatar = ref('');
const defaultAvatar = 'https://picsum.photos/seed/user/100/100';
const isLogin = ref(isLoggedIn());

// 计算当前路由
const currentRoute = computed(() => {
  const path = route.path;
  return path.replace('/', '') || 'home';
});

// 生命周期
onMounted(() => {
  // 检查登录状态并获取用户信息
  checkLoginStatus();
  
  // 设置当前活动菜单
  updateActiveMenu();
});

// 监听路由变化
watch(() => route.path, () => {
  updateActiveMenu();
});

// 更新活动菜单
const updateActiveMenu = () => {
  const path = route.path;
  if (path === '/') {
    activeMenu.value = 'home';
  } else {
    const parts = path.split('/');
    activeMenu.value = parts[1] || 'home';
  }
};

// 检查登录状态并获取用户信息
const checkLoginStatus = () => {
  isLogin.value = isLoggedIn();
  if (isLogin.value) {
    // 获取用户信息
    loadUserInfo();
  }
};

// 获取用户信息
const loadUserInfo = async () => {
  try {
    const userInfo = getUserInfo();
    if (userInfo && userInfo.avatar) {
      userAvatar.value = userInfo.avatar;
    } else {
      // 如果本地没有头像信息，则使用默认头像
      userAvatar.value = defaultAvatar;
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    // 使用默认头像
    userAvatar.value = defaultAvatar;
  }
};

// 处理菜单选择
const handleMenuSelect = (key) => {
  activeMenu.value = key;
  router.push(`/${key}`);
};

// 处理侧边菜单选择
const handleSideMenuSelect = (key) => {
  router.push(`/${key}`);
};

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '确认退出',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    try {
      // 清除认证信息
      clearAuth();

      // 重置状态
      isLogin.value = false;
      userAvatar.value = '';

      ElMessage.success('已退出登录');
      router.replace('/login');
    } catch (error) {
      ElMessage.error('退出登录失败，请重试');
    }
  }).catch(() => {
    ElMessage.info('已取消退出');
  });
};
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff9f5;
}

/* 主容器 */
.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 侧边菜单 */
.side-menu {
  background-color: #fff9f5;
  border-right: 1px solid #ffe0b2;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  z-index: 100;
  overflow-y: auto;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
}

/* 侧边栏logo */
.logo-container {
  display: flex;
  align-items: center;
  padding: 0 20px 20px;
  border-bottom: 1px solid #ffe0b2;
  margin-bottom: 10px;
}

.cat-icon {
  font-size: 24px;
  margin-right: 8px;
}

.logo-container h1 {
  margin: 0;
  font-size: 18px;
  color: #e65100;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.el-menu-vertical-demo {
  border-right: none;
  flex: 1;
}

.el-menu-item {
  margin: 0 10px;
  border-radius: 8px;
}

.el-menu-item:hover {
  background-color: #fff3e0 !important;
}

.el-menu-item.is-active {
  background-color: #ffe0b2 !important;
}

/* 内容区域 */
.content-area {
  flex: 1;
  padding: 30px;
  background-color: rgb(254, 248, 244);
  margin: 20px 20px 20px 220px;
  border-radius: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  min-height: calc(100vh - 40px);
}

/* 页脚样式 */
.main-footer {
  background-color: #5d4037;
  color: white;
  padding: 50px 40px 20px 220px;
  margin-top: auto;
  height: 100%;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
}

.footer-column h3 {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 20px;
  color: #ffcc80;
}

.footer-column h4 {
  font-size: 16px;
  margin-top: 0;
  margin-bottom: 15px;
  color: #ffcc80;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 10px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #ffcc80;
}

.contact-info {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contact-info li {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.contact-info li .el-icon {
  margin-right: 10px;
  width: 18px;
  height: 18px;
}

.social-icons {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.social-btn {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.social-btn:hover {
  background-color: #ffcc80;
  color: #5d4037;
}

.copyright {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .side-menu {
    display: none;
  }
  
  .content-area {
    margin: 10px;
    padding: 20px;
  }
  
  .main-footer {
    padding: 50px 40px 20px;
  }
}

@media (max-width: 768px) {
  .content-area {
    margin: 5px;
    padding: 15px;
  }
  
  .main-footer {
    padding: 40px 20px 20px;
  }
  
  .logo-container h1 {
    font-size: 16px;
  }
}
</style>