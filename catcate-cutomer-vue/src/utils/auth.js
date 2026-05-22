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
 * 检查登录是否过期
 * @returns {boolean} 是否过期
 */
export function isLoginExpired() {
  const userInfo = getUserInfo();
  if (!userInfo) return true;
  
  // 检查token是否存在
  if (!userInfo.token) return true;
  
  // 检查登录时间是否过期（假设24小时过期）
  const loginTime = userInfo.loginTime;
  if (!loginTime) return false; // 如果没有登录时间，暂时认为没有过期
  
  const now = new Date().getTime();
  const loginTimestamp = new Date(loginTime).getTime();
  const expireTime = 24 * 60 * 60 * 1000; // 24小时
  
  return now - loginTimestamp > expireTime;
}

/**
 * 检查用户是否已登录且未过期
 * @returns {boolean} 是否已登录且未过期
 */
export function isLoginValid() {
  return isLoggedIn() && !isLoginExpired();
}

/**
 * 清除用户认证信息
 */
export function clearAuth() {
  localStorage.removeItem('catCafeUserInfo');
}

/**
 * 保存用户信息
 * @param {Object} userInfo 用户信息对象
 */
export function saveUserInfo(userInfo) {
  const infoWithLoginTime = {
    ...userInfo,
    loginTime: new Date().toISOString()
  };
  localStorage.setItem('catCafeUserInfo', JSON.stringify(infoWithLoginTime));
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