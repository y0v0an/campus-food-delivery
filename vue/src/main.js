import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import 'iconify-icon'
import * as echarts from 'echarts'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'

// 全局样式
import './assets/styles/global.scss'
import './assets/styles/animations.scss'
import './assets/styles/tailwind.css'  // ← 添加这一行
const app = createApp(App)
const pinia = createPinia()

// 全局挂载 ECharts
app.config.globalProperties.$echarts = echarts

app.use(pinia)


// 必须在 use(router) 之前恢复登录态，否则首次路由守卫可能读到空角色
const userStore = useUserStore()
userStore.initUserStatus()

app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')
