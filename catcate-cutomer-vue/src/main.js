import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'

const app = createApp(App)

// 在注册 Element Plus 之前添加错误处理
app.config.errorHandler = (err) => {
  // 忽略 ResizeObserver 相关错误
  if (err && err.message && err.message.includes('ResizeObserver')) {
    return;
  }
  // 其他错误正常处理
  console.error(err);
};
// 忽略 ResizeObserver 错误
if (typeof window !== 'undefined') {
  window.addEventListener('error', (e) => {
    if (e.message?.includes('ResizeObserver')) {
      e.stopImmediatePropagation()
    }
  })
}

app.use(router)
app.use(ElementPlus)
app.mount('#app')