import { getToken } from './auth';

// API基础配置
const BASE_URL = 'http://localhost:8083/catcatecutomer';

/**
 * 通用API请求函数
 * @param {string} url 请求地址
 * @param {Object} options 请求选项
 * @returns {Promise} 请求Promise
 */
export async function apiRequest(url, options = {}) {
  const token = getToken();
  
  // 处理查询参数
  let requestUrl = url;
  if (options.params) {
    const params = new URLSearchParams();
    Object.entries(options.params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value);
      }
    });
    const paramsString = params.toString();
    if (paramsString) {
      requestUrl = `${url}${url.includes('?') ? '&' : '?'}${paramsString}`;
    }
    // 删除params属性，避免传递给fetch
    delete options.params;
  }
  
  const config = {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers
    },
    ...options
  };

  try {
    const response = await fetch(`${BASE_URL}${requestUrl}`, config);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    // 检查响应内容类型
    const contentType = response.headers.get('content-type');
    
    if (contentType && contentType.includes('application/json')) {
      // JSON响应
      const result = await response.json();
      
      // 处理统一响应格式 {code: 200, message: ..., data/list/records: ..., total: ...}
      if (result.code === 200) {
        // 返回包含所有数据的对象，让调用者决定使用哪个字段
        return result;
      } else if (result.success !== undefined) {
        // 处理包含success字段的响应格式
        if (result.success) {
          return result;
        } else {
          throw new Error(result.message || '请求失败');
        }
      } else {
        throw new Error(result.message || '请求失败');
      }
    } else {
      // 非JSON响应（如HTML表单或text/plain），尝试解析为JSON
      const text = await response.text();
      try {
        // 尝试解析为JSON
        const result = JSON.parse(text);
        if (result.success !== undefined) {
          if (result.success) {
            return result;
          } else {
            throw new Error(result.message || '请求失败');
          }
        }
        return text;
      } catch (e) {
        // 解析失败，直接返回文本
        return text;
      }
    }
  } catch (error) {
    console.error('API请求失败:', error);
    throw error;
  }
}

// 导出常用的HTTP方法
export const api = {
  get: (url, options) => apiRequest(url, { ...options, method: 'GET' }),
  post: (url, data, options) => apiRequest(url, { 
    ...options, 
    method: 'POST',
    body: JSON.stringify(data) 
  }),
  put: (url, data, options) => apiRequest(url, { 
    ...options, 
    method: 'PUT',
    body: JSON.stringify(data)
  }),
  delete: (url, options) => apiRequest(url, { ...options, method: 'DELETE' })
};
