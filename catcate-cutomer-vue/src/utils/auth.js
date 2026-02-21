// 认证工具类

/**
 * 获取存储的用户信息
 * @returns {Object|null} 用户信息对象
 */
export function getUserInfo() {
  const userInfoStr = localStorage.getItem('catCafeUserInfo');
  return userInfoStr ? JSON.parse(userInfoStr) : null;
}

/**
 * 获取用户token
 * @returns {string|null} 用户token
 */
export function getToken() {
  const userInfo = getUserInfo();
  return userInfo ? userInfo.token : null;
}

/**
 * 获取用户ID
 * @returns {number|null} 用户ID
 */
export function getUserId() {
  const userInfo = getUserInfo();
  return userInfo ? userInfo.id : null;
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
  return !!getToken();
}

/**
 * 清除用户认证信息
 */
export function clearAuth() {
  localStorage.removeItem('catCafeUserInfo');
}

/**
 * 创建带认证头的请求配置
 * @param {Object} options 其他请求选项
 * @returns {Object} 包含认证头的请求配置
 */
export function createAuthRequest(options = {}) {
  const token = getToken();
  return {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...options.headers
    }
  };
}