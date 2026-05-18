<template>
  <div class="merchant-layout">
    <!-- 侧边栏 -->
    <AppSidebar :title="merchantName" :menuItems="menuItems" />

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <AppHeader />

      <!-- 页面内容 -->
      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppSidebar from '@/components/common/AppSidebar.vue'
import AppHeader from '@/components/common/AppHeader.vue'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const userStore = useUserStore()

onMounted(async () => {
  const uid = userStore.userInfo?.id
  if (!uid) return
  try {
    const m = await request.get(`/merchant/user/${uid}`)
    if (m?.logo) userStore.setMerchantProfileLogo(m.logo)
  } catch (e) {
    console.error('加载店铺头像失败:', e)
  }
})

// 商家名称
const merchantName = computed(() => userStore.userName || '商家端')

// 菜单项 - 使用 iconify-icon 图标名称
const menuItems = [
  {
    path: '/merchant/orders',
    title: '订单管理',
    icon: 'lucide:clipboard-list'
  },
  {
    path: '/merchant/dishes',
    title: '菜品管理',
    icon: 'lucide:utensils'
  },
  {
    path: '/merchant/statistics',
    title: '经营统计',
    icon: 'lucide:bar-chart-3'
  },
  {
    path: '/merchant/coupons',
    title: '优惠券管理',
    icon: 'lucide:ticket'
  },
  {
    path: '/merchant/profile',
    title: '店铺设置',
    icon: 'lucide:settings'
  }
]
</script>

<style scoped>
.merchant-layout {
  display: flex;
  min-height: 100vh;
}

.main-container {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  background: #F5F7FA;
}

.page-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
