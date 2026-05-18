<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <AppSidebar title="管理后台" :menuItems="menuItems" />

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
import AppSidebar from '@/components/common/AppSidebar.vue'
import AppHeader from '@/components/common/AppHeader.vue'

// 菜单项 - 使用 iconify-icon 图标名称
const menuItems = [
  {
    path: '/admin/dashboard',
    title: '数据看板',
    icon: 'lucide:bar-chart-3'
  },
  {
    path: '/admin/users',
    title: '用户管理',
    icon: 'lucide:users'
  },
  {
    path: '/admin/merchants',
    title: '商家管理',
    icon: 'lucide:store'
  },
  {
    path: '/admin/orders',
    title: '订单列表',
    icon: 'lucide:clipboard-list'
  },
  {
    path: '/admin/rider-audit',
    title: '骑手审核',
    icon: 'lucide:user-check'
  },
  {
    path: '/admin/profile',
    title: '个人中心',
    icon: 'lucide:user-circle'
  }
]
</script>

<style scoped>
.admin-layout {
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
