<template>
  <div class="rider-layout">
    <!-- 顶部导航栏 -->
    <header class="top-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-icon">🚴</span>
          <span class="logo-text">骑手端</span>
        </div>
      </div>
      <div class="header-right">
        <div class="user-info" @click="showUserMenu = !showUserMenu">
          <div class="avatar">
            <img v-if="avatarDisplaySrc" :src="avatarDisplaySrc" alt="avatar" />
            <span v-else>{{ userStore.userName?.charAt(0) }}</span>
          </div>
          <span class="user-name">{{ userStore.userName }}</span>
          <iconify-icon icon="lucide:chevron-down"></iconify-icon>

          <!-- 下拉菜单 -->
          <div class="user-menu" v-if="showUserMenu" @click="showUserMenu = false">
            <div class="menu-item" v-if="userStore.canSwitchToStudent" @click.stop="handleCommand('switch')">
              <iconify-icon icon="lucide:switch"></iconify-icon>
              切换到学生端
            </div>
            <div class="menu-divider"></div>
            <div class="menu-item text-danger" @click.stop="handleCommand('logout')">
              <iconify-icon icon="lucide:log-out"></iconify-icon>
              退出登录
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部导航栏 -->
    <nav class="bottom-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <iconify-icon :icon="item.icon" class="nav-icon"></iconify-icon>
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getImageUrl } from '@/utils/imageUrl'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showUserMenu = ref(false)

const avatarDisplaySrc = computed(() => getImageUrl(userStore.userAvatar))

const navItems = [
  { path: '/rider', icon: 'lucide:clipboard-list', label: '订单池' },
  { path: '/rider/delivery', icon: 'lucide:truck', label: '我的配送' },
  { path: '/rider/earnings', icon: 'lucide:wallet', label: '收入' }
]

const isActive = (path) => {
  if (path === '/rider') {
    return (
      route.path === '/rider' ||
      route.path === '/rider/' ||
      route.path.startsWith('/rider/pool')
    )
  }
  return route.path.startsWith(path)
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  showUserMenu.value = false
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'switch') {
    const result = userStore.switchRole()
    if (result.success) {
      ElMessage.success('已切换到学生端')
      router.push(result.redirectPath)
    }
  }
}
</script>

<style scoped>
.rider-layout {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff7ed 0%, #f5f7fa 22%, #f5f7fa 100%);
  padding-top: 56px;
  padding-bottom: 56px;
}

/* 顶部导航栏 */
.top-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.header-left .logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left .logo-icon {
  font-size: 24px;
}

.header-left .logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #FF7A45;
}

.header-right {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.15s;
}

.user-info:hover {
  background: #F5F7FA;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #FF7A45;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 500;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.user-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  min-width: 160px;
  padding: 8px 0;
  z-index: 1000;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  transition: background 0.15s;
}

.menu-item:hover {
  background: #F5F7FA;
}

.menu-item.text-danger {
  color: #F5222D;
}

.menu-divider {
  height: 1px;
  background: #EBEEF5;
  margin: 4px 0;
}

.main-content {
  min-height: calc(100vh - 56px - 56px);
}

/* 底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: white;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.06);
  z-index: 100;
  border-top: 1px solid #F2F6FC;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 16px;
  text-decoration: none;
  transition: all 0.15s;
  color: #909399;
}

.nav-icon {
  font-size: 20px;
  transition: transform 0.15s;
}

.nav-label {
  font-size: 11px;
  transition: all 0.15s;
}

.nav-item.active {
  color: #FF7A45;
}

.nav-item.active .nav-icon {
  transform: scale(1.08);
}

.nav-item.active .nav-label {
  font-weight: 500;
}
</style>
