<template>
  <div class="admin-layout">
    <!-- 顶部导航 -->
    <el-header class="main-header">
      <div class="logo">
        <el-icon :size="28">
          <div>🐱</div>
        </el-icon>
        <span class="logo-text">喵时光猫咖管理系统</span>
      </div>
      <div class="header-actions">
        <el-dropdown>
          <el-button type="text" class="user-info">
            <el-avatar :size="32" :src="currentUser.avatar || defaultAvatar"></el-avatar>
            <span>{{ currentUser.nickname || currentUser.username }}</span>
            <el-icon :size="16" class="el-icon--right"></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleSettings">系统设置</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧导航栏 -->
      <el-aside width="220px" class="sidebar">
        <el-menu :default-openeds="['dashboard']" :default-active="activeMenu" class="sidebar-menu"
          @select="handleMenuSelect">
          <el-menu-item index="HomePage">
            <el-icon>
              <Layout />
            </el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-sub-menu index="users">
            <template #title>
              <el-icon>
                <User />
              </el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="users">用户列表</el-menu-item>
            <el-menu-item index="roles/list">角色权限</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="cats">
            <template #title>
              <el-icon>
                <User />
              </el-icon>
              <span>猫咪管理</span>
            </template>
            <el-menu-item index="cats/list">猫咪列表</el-menu-item>
            <el-menu-item index="cats/health">健康记录</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="reservations">
            <template #title>
              <el-icon>
                <Calendar />
              </el-icon>
              <span>预约管理</span>
            </template>
            <el-menu-item index="appointments">预约列表</el-menu-item>
            <el-menu-item index="reservations/slots">时段设置</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="products">
            <template #title>
              <el-icon>
                <ShoppingCart />
              </el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="products/list">商品列表</el-menu-item>
            <el-menu-item index="products/categories">分类管理</el-menu-item>
            <el-menu-item index="products/orders">订单管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="activities">
            <template #title>
              <el-icon>
                <ShoppingCart />
              </el-icon>
              <span>活动管理</span>
            </template>
            <el-menu-item index="activities/list">活动列表</el-menu-item>
            <el-menu-item index="activities/create">创建活动</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="reviews">
            <template #title>
              <el-icon>
                <ShoppingCart />
              </el-icon>
              <span>评价管理</span>
            </template>
            <el-menu-item index="reviews/list">评价列表</el-menu-item>
            <el-menu-item index="reviews/settings">审核设置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="content-area">
        <slot></slot>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Layout, User, Calendar, ShoppingCart
} from '@element-plus/icons-vue';

// 路由
const router = useRouter();

// 状态管理
const activeMenu = ref('dashboard');
const defaultAvatar = 'https://picsum.photos/seed/admin/100/100';

// 当前用户信息
const currentUser = reactive({
  id: 1,
  username: 'admin',
  nickname: '管理员',
  avatar: '',
  role: 'ADMIN'
});

// 方法
const handleMenuSelect = (key) => {
  activeMenu.value = key;
  if (key === 'dashboard') {
    router.push('/admin/dashboard');
  } else {
    router.push(`/admin/${key}`);
  }
};

const handleProfile = () => {
  router.push('/admin/profile');
};

const handleSettings = () => {
  router.push('/admin/settings');
};

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
    router.push('/');
    ElMessage.success('已退出登录');
  });
};
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.main-header {
  height: 60px;
  background-color: #fff7ee;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}

.user-info:hover {
  color: #333 !important;
  border: none !important;
  background-color: transparent !important;
  outline: none !important;
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  background-color: #fff7ee;
  color: #333;
  box-shadow: 1px 0 2px rgba(0, 0, 0, 0.1);
}

.sidebar-menu {
  width: 100%;
  height: 100%;
  background-color: #fff7ee;
  border-right: none;
}

.sidebar-menu .el-sub-menu__title,
.sidebar-menu .el-menu-item {
  color: #666;
  height: 50px;
  line-height: 50px;
}

.sidebar-menu .el-sub-menu__title:hover,
.sidebar-menu .el-menu-item:hover {
  background-color: #fff7ee;
  color: #333;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #fff7ee;
  color: #333;
}

.content-area {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}
</style>