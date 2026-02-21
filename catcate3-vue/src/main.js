import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router' // 确保你有一个 router 文件


const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
const resizeObserverErr = window.console.error;
window.console.error = (...args) => {
  if (args[0] && args[0].includes && args[0].includes('ResizeObserver')) {
    return;
  }
  resizeObserverErr(...args);
};