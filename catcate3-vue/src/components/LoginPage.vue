<template>
  <div class="auth-page">
    <!-- 背景装饰元素 -->
    <div class="bg-pattern"></div>
    <div class="cat-decoration"></div>

    <!-- 主卡片容器 -->
    <div class="auth-card">
      <!-- 猫咖Logo和标题 -->
      <div class="auth-header">
        <div class="logo-container">
          <div class="cat-icon">🐱</div>
        </div>
        <h1>猫咖管理系统</h1>
        <p>欢迎回来，请登录您的账户</p>
      </div>

      <!-- 切换标签 -->
      <div class="auth-tabs">
        <button class="tab-button" :class="{ active: isLogin }" @click="switchMode(true)">
          登录
        </button>
        <button class="tab-button" :class="{ active: !isLogin }" @click="switchMode(false)">
          注册
        </button>
      </div>

      <!-- 登录表单 -->
      <el-form v-if="isLogin" ref="loginFormRef" :model="loginForm" :rules="loginRules" class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" autocomplete="off"
            :class="{ 'input-focus': usernameFocus }" @focus="usernameFocus = true"
            @blur="usernameFocus = false"></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password
            :class="{ 'input-focus': passwordFocus }" @focus="passwordFocus = true"
            @blur="passwordFocus = false"></el-input>
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="rememberMe" size="small">
            记住我
          </el-checkbox>
          <a href="#" class="forgot-link" @click.prevent="showForgotDialog = true">
            忘记密码?
          </a>
        </div>

        <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loading">
          登录系统
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else ref="registerFormRef" :model="registerForm" :rules="registerRules" class="auth-form">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="请设置用户名" prefix-icon="User"
            autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="Message"
            autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请设置密码" prefix-icon="Lock"
            show-password></el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="Check"
            show-password></el-input>
        </el-form-item>

        <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="loading">
          创建账户
        </el-button>
      </el-form>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog v-model="showForgotDialog" title="找回密码" width="300px" :close-on-click-modal="false">
      <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules">
        <el-form-item prop="email">
          <el-input v-model="forgotForm.email" placeholder="请输入注册邮箱" prefix-icon="Message"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotDialog = false">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword">发送验证码</el-button>
      </template>
    </el-dialog>

    <!-- 页脚信息 -->
    <footer class="page-footer">
      <p>© 2023 猫咖管理系统 | 版权所有</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElDialog } from 'element-plus';
import { useRouter } from 'vue-router';

// 状态管理
const isLogin = ref(true);
const loading = ref(false);
const rememberMe = ref(false);
const showForgotDialog = ref(false);
const usernameFocus = ref(false);
const passwordFocus = ref(false);
const router = useRouter();

// 表单引用
const loginFormRef = ref(null);
const registerFormRef = ref(null);
const forgotFormRef = ref(null);

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
});

// 注册表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

// 找回密码表单数据
const forgotForm = reactive({
  email: ''
});

// 登录表单验证规则
const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
});

// 注册表单验证规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20位之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// 找回密码验证规则
const forgotRules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
});

// 切换登录/注册模式
const switchMode = (loginMode) => {
  isLogin.value = loginMode;
  // 滚动到顶部
  document.querySelector('.auth-card').scrollTop = 0;
  // 重置表单焦点状态
  usernameFocus.value = false;
  passwordFocus.value = false;
};

// 处理登录（完成）
const handleLogin = async () => {
  try {
    loading.value = true;
    // 表单验证
    await loginFormRef.value.validate();

    // 实际项目中替换为API调用
    const response = await fetch('http://localhost:8081/catcate/users/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: loginForm.username,
        password: loginForm.password
      })
    });
    // 输出response返回值
    // 解析响应数据
    const result = await response.json();
    console.log('登录响应:', result);

    if (result === true) {
      ElMessage.success('登录成功，正在进入系统...');

      // 处理记住密码功能：如果用户选择了记住密码，则将用户名和密码存储到localStorage中；
      // 否则从localStorage中移除已存储的用户名和密码
      if (rememberMe.value) {
        localStorage.setItem('catCafeUsername', loginForm.username);
        localStorage.setItem('catCafePassword', loginForm.password);
      } else {
        localStorage.removeItem('catCafeUsername');
        localStorage.removeItem('catCafePassword');
      }

      // 跳转到home页
      setTimeout(() => {
        router.push('/HomePage');
      }, 100);
    } else {
      ElMessage.error('登录失败，用户名或密码错误');
    }

    loading.value = false;
  } catch (error) {
    console.error('登录验证失败', error);
    ElMessage.error('网络错误，请检查连接');
    loading.value = false;
  }
};

// 处理注册（完成）
const handleRegister = async () => {
  try {
    loading.value = true;
    // 表单验证
    await registerFormRef.value.validate();

    // 实际项目中替换为API调用
    const response = await fetch('http://localhost:8081/catcate/users/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(registerForm)
    });

    if (response.ok) {
      ElMessage.success('注册成功，请登录');
      switchMode(true);
      // 自动填充用户名
      loginForm.username = registerForm.username;
    } else {
      ElMessage.error('注册失败，请重试');
    }

    loading.value = false;
  } catch (error) {
    console.error('注册验证失败', error);
    ElMessage.error('网络错误，请检查连接');
    loading.value = false;
  }
};

// 处理找回密码(未完成)
const handleForgotPassword = async () => {
  try {
    await forgotFormRef.value.validate();
    // 模拟发送验证码
    ElMessage.success('验证码已发送到您的邮箱');
    showForgotDialog.value = false;
  } catch (error) {
    console.error('验证失败', error);
  }
};

// 初始化 - 读取记住的密码
onMounted(() => {
  const savedUsername = localStorage.getItem('catCafeUsername');
  const savedPassword = localStorage.getItem('catCafePassword');

  if (savedUsername && savedPassword) {
    loginForm.username = savedUsername;
    loginForm.password = savedPassword;
    rememberMe.value = true;
  }

  // 添加页面载入动画
  document.body.classList.add('page-loaded');
});
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff9f0 0%, #fff0e0 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image:
    radial-gradient(#ffc288 0.5px, transparent 0.5px),
    radial-gradient(#ffc288 0.5px, #fff9f0 0.5px);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
  opacity: 0.2;
  z-index: 0;
}

.cat-decoration {
  position: absolute;
  width: 200px;
  height: 200px;
  right: 10%;
  top: 20%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ffb74d' opacity='0.2'%3E%3Cpath d='M12,8L10.67,8.09C9.81,7.07 7.4,4.5 5,4.5C5,4.5 3.03,7.46 4.96,11.41C4.41,12.24 4.07,13.2 4,14.21C4,17.5 6.66,19.68 11,21.93L12,22L13,21.93C17.34,19.68 20,17.5 20,14.21C19.93,13.2 19.59,12.24 19.04,11.41C20.97,7.46 19,4.5 19,4.5C16.6,4.5 14.19,7.07 13.33,8.09L12,8M7,13A5,5 0 0,0 12,18A5,5 0 0,0 17,13C17,11.07 13.41,9.96 12,9.96C10.59,9.96 7,11.07 7,13Z' /%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-size: contain;
  z-index: 0;
  transform: rotate(15deg);
}

/* 主卡片样式 */
.auth-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0 15px 40px rgba(255, 167, 71, 0.15);
  padding: 40px 30px;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  overflow: hidden;
}

.auth-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(90deg, #ff9800, #ffb74d);
}

.auth-card:hover {
  box-shadow: 0 20px 45px rgba(255, 167, 71, 0.2);
  transform: translateY(-5px);
}

/* 头部样式 */
.auth-header {
  text-align: center;
  margin-bottom: 35px;
}

.logo-container {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background-color: #fff3e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5px 15px rgba(255, 167, 71, 0.1);
  transition: all 0.3s ease;
}

.auth-card:hover .logo-container {
  transform: scale(1.05) rotate(5deg);
}

.cat-icon {
  font-size: 40px;
  transition: all 0.3s ease;
}

.auth-header h1 {
  margin: 0 0 10px;
  font-size: 26px;
  font-weight: 600;
  color: #333;
}

.auth-header p {
  margin: 0;
  color: #757575;
  font-size: 14px;
}

/* 标签切换样式 */
.auth-tabs {
  display: flex;
  margin-bottom: 30px;
  position: relative;
}

.tab-button {
  flex: 1;
  padding: 12px 0;
  background: none;
  border: none;
  font-size: 16px;
  font-weight: 500;
  color: #9e9e9e;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.tab-button.active {
  color: #ff9800;
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #ff9800;
  border-radius: 3px 3px 0 0;
  transition: all 0.3s ease;
}

.tab-button:hover:not(.active) {
  color: #757575;
}

/* 表单样式 */
.auth-form {
  width: 100%;
  animation: fadeIn 0.5s ease forwards;
}

.el-form-item {
  margin-bottom: 22px;
}

.el-input {
  --el-input-height: 50px;
  --el-input-bg-color: #fafafa;
  --el-input-border-color: #e0e0e0;
  --el-input-focus-border-color: #ffb74d;
  --el-input-text-color: #333;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.el-input.input-focus .el-input__wrapper {
  box-shadow: 0 0 0 3px rgba(255, 183, 77, 0.2);
}

.el-input__prefix {
  color: #bdbdbd;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  font-size: 14px;
}

.el-checkbox__label {
  color: #616161;
}

.forgot-link {
  color: #ff9800;
  text-decoration: none;
  transition: all 0.2s ease;
}

.forgot-link:hover {
  color: #e65100;
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  background-color: #ff9800;
  border-color: #ff9800;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background-color: #fb8c00;
  border-color: #fb8c00;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 页脚样式 */
.page-footer {
  margin-top: 30px;
  color: #9e9e9e;
  font-size: 12px;
  position: relative;
  z-index: 1;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 页面载入动画 */
.page-loaded .auth-card {
  animation: slideUp 0.6s ease-out forwards;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 480px) {
  .auth-card {
    padding: 30px 20px;
  }

  .auth-header h1 {
    font-size: 22px;
  }

  .tab-button {
    font-size: 15px;
  }

  .el-input {
    --el-input-height: 45px;
  }

  .submit-btn {
    height: 45px;
    font-size: 15px;
  }

  .cat-decoration {
    width: 150px;
    height: 150px;
    right: 5%;
    top: 15%;
  }
}
</style>