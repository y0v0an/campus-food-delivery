<!--<template>-->
<!--  <div class="student-layout">-->
<!--    &lt;!&ndash; 顶部导航栏 &ndash;&gt;-->
<!--    <header class="top-header">-->
<!--      <div class="header-left">-->
<!--        <span class="header-title">校园外卖</span>-->
<!--      </div>-->
<!--      <div class="header-right">-->
<!--        <el-dropdown @command="handleCommand">-->
<!--          <div class="user-info">-->
<!--            <el-avatar :size="32" :src="avatarDisplaySrc">-->
<!--              {{ userStore.userName?.charAt(0) }}-->
<!--            </el-avatar>-->
<!--            <span class="user-name">{{ userStore.userName }}</span>-->
<!--            <el-icon><ArrowDown /></el-icon>-->
<!--          </div>-->
<!--          <template #dropdown>-->
<!--            <el-dropdown-menu>-->
<!--              <el-dropdown-item command="profile">-->
<!--                <span class="nav-dd-icon">👤</span>-->
<!--                个人中心-->
<!--              </el-dropdown-item>-->
<!--              <el-dropdown-item v-if="userStore.canSwitchToRider" command="switch">-->
<!--                <el-icon><Switch /></el-icon>-->
<!--                切换到骑手端-->
<!--              </el-dropdown-item>-->
<!--              <el-dropdown-item command="logout" divided>-->
<!--                <el-icon><SwitchButton /></el-icon>-->
<!--                退出登录-->
<!--              </el-dropdown-item>-->
<!--            </el-dropdown-menu>-->
<!--          </template>-->
<!--        </el-dropdown>-->
<!--      </div>-->
<!--    </header>-->

<!--    &lt;!&ndash; 主内容区 &ndash;&gt;-->
<!--    <main class="main-content">-->
<!--      <router-view />-->
<!--    </main>-->

<!--    &lt;!&ndash; 底部导航栏 &ndash;&gt;-->
<!--    <nav class="bottom-nav">-->
<!--      <router-link-->
<!--        v-for="item in navItems"-->
<!--        :key="item.path"-->
<!--        :to="item.path"-->
<!--        class="nav-item"-->
<!--        :class="{ active: isActive(item.path) }"-->
<!--      >-->
<!--        <span class="nav-icon-wrap">-->
<!--          <el-badge-->
<!--            v-if="item.path === '/student/cart'"-->
<!--            :value="cartStore.itemCount"-->
<!--            :hidden="cartStore.itemCount === 0"-->
<!--            :max="99"-->
<!--            class="cart-badge"-->
<!--          >-->
<!--            <el-icon class="nav-icon-ep" :size="22">-->
<!--              <component :is="item.icon" />-->
<!--            </el-icon>-->
<!--          </el-badge>-->
<!--          <el-icon v-else class="nav-icon-ep" :size="22">-->
<!--            <component :is="item.icon" />-->
<!--          </el-icon>-->
<!--        </span>-->
<!--        <span class="nav-label">{{ item.label }}</span>-->
<!--      </router-link>-->
<!--    </nav>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { computed } from 'vue'-->
<!--import { useRoute, useRouter } from 'vue-router'-->
<!--import { useCartStore } from '@/stores/cart'-->
<!--import { useUserStore } from '@/stores/user'-->
<!--import {-->
<!--  ArrowDown,-->
<!--  SwitchButton,-->
<!--  Switch,-->
<!--  House,-->
<!--  ShoppingCart,-->
<!--  List,-->
<!--  User-->
<!--} from '@element-plus/icons-vue'-->
<!--import { ElMessage, ElMessageBox } from 'element-plus'-->
<!--import { getImageUrl } from '@/utils/imageUrl'-->

<!--const route = useRoute()-->
<!--const router = useRouter()-->
<!--const cartStore = useCartStore()-->
<!--const userStore = useUserStore()-->

<!--const avatarDisplaySrc = computed(() => getImageUrl(userStore.userAvatar))-->

<!--// 导航项-->
<!--const navItems = [-->
<!--  { path: '/student', icon: House, label: '首页' },-->
<!--  { path: '/student/cart', icon: ShoppingCart, label: '购物车' },-->
<!--  { path: '/student/orders', icon: List, label: '订单' },-->
<!--  { path: '/student/profile', icon: User, label: '我的' }-->
<!--]-->

<!--const isActive = (path) => {-->
<!--  if (path === '/student') {-->
<!--    return route.path === '/student' || route.path === '/student/'-->
<!--  }-->
<!--  if (path === '/student/cart') {-->
<!--    return (-->
<!--      route.path.startsWith('/student/cart') || route.path.startsWith('/student/checkout')-->
<!--    )-->
<!--  }-->
<!--  return route.path.startsWith(path)-->
<!--}-->

<!--// 处理下拉菜单命令-->
<!--const handleCommand = (command) => {-->
<!--  if (command === 'profile') {-->
<!--    router.push('/student/profile')-->
<!--    return-->
<!--  }-->
<!--  if (command === 'logout') {-->
<!--    ElMessageBox.confirm('确定要退出登录吗？', '提示', {-->
<!--      confirmButtonText: '确定',-->
<!--      cancelButtonText: '取消',-->
<!--      type: 'warning'-->
<!--    }).then(() => {-->
<!--      userStore.logout()-->
<!--      ElMessage.success('已退出登录')-->
<!--      router.push('/login')-->
<!--    }).catch(() => {})-->
<!--  } else if (command === 'switch') {-->
<!--    const result = userStore.switchRole()-->
<!--    if (result.success) {-->
<!--      ElMessage.success('已切换到骑手端')-->
<!--      router.push(result.redirectPath)-->
<!--    }-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style lang="scss" scoped>-->
<!--@use '@/assets/styles/variables.scss' as *;-->

<!--// 整页 flex：仅中间主区域滚动，顶栏/底栏始终完整可见（避免 fixed 在移动端被裁切/叠盖）-->
<!--.student-layout {-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  height: 100vh;-->
<!--  height: 100dvh;-->
<!--  max-height: 100vh;-->
<!--  max-height: 100dvh;-->
<!--  overflow: hidden;-->
<!--  background: linear-gradient(180deg, #eef6ff 0%, #f5f7fa 18%, #f5f7fa 100%);-->
<!--}-->

<!--// 顶部导航栏-->
<!--.top-header {-->
<!--  flex-shrink: 0;-->
<!--  height: 56px;-->
<!--  background: $color-bg-white;-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  padding: 0 $spacing-base;-->
<!--  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);-->
<!--  z-index: 10;-->

<!--  .header-left {-->
<!--    .header-title {-->
<!--      font-size: $font-size-lg;-->
<!--      font-weight: $font-weight-semibold;-->
<!--      color: $color-primary;-->
<!--    }-->
<!--  }-->

<!--  .header-right {-->
<!--    .user-info {-->
<!--      display: flex;-->
<!--      align-items: center;-->
<!--      gap: $spacing-sm;-->
<!--      cursor: pointer;-->
<!--      padding: $spacing-xs $spacing-sm;-->
<!--      border-radius: $border-radius-base;-->
<!--      transition: background $transition-fast;-->

<!--      &:hover {-->
<!--        background: $color-bg-base;-->
<!--      }-->

<!--      .user-name {-->
<!--        font-size: $font-size-sm;-->
<!--        color: $color-text-primary;-->
<!--        font-weight: $font-weight-medium;-->
<!--      }-->

<!--      .el-icon {-->
<!--        color: $color-text-regular;-->
<!--      }-->
<!--    }-->
<!--  }-->
<!--}-->

<!--.main-content {-->
<!--  flex: 1;-->
<!--  min-height: 0;-->
<!--  overflow-x: hidden;-->
<!--  overflow-y: auto;-->
<!--  -webkit-overflow-scrolling: touch;-->
<!--}-->

<!--// 底部导航栏-->
<!--.bottom-nav {-->
<!--  flex-shrink: 0;-->
<!--  height: $bottom-nav-height;-->
<!--  background: $color-bg-white;-->
<!--  display: flex;-->
<!--  justify-content: space-around;-->
<!--  align-items: center;-->
<!--  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.06);-->
<!--  border-top: 1px solid $color-border-extra-light;-->
<!--  z-index: 10;-->

<!--  .nav-icon-wrap {-->
<!--    display: flex;-->
<!--    align-items: center;-->
<!--    justify-content: center;-->
<!--    min-height: 24px;-->
<!--  }-->

<!--  :deep(.cart-badge.el-badge) {-->
<!--    display: flex;-->
<!--    align-items: center;-->
<!--    justify-content: center;-->

<!--    .el-badge__content {-->
<!--      border: none;-->
<!--      font-size: 10px;-->
<!--      height: 16px;-->
<!--      line-height: 16px;-->
<!--      padding: 0 5px;-->
<!--      min-width: 16px;-->
<!--    }-->
<!--  }-->

<!--  .nav-item {-->
<!--    display: flex;-->
<!--    flex-direction: column;-->
<!--    align-items: center;-->
<!--    gap: 4px;-->
<!--    padding: $spacing-sm $spacing-sm;-->
<!--    flex: 1;-->
<!--    max-width: 25%;-->
<!--    text-decoration: none;-->
<!--    transition: color $transition-fast, transform $transition-fast;-->
<!--    color: $color-text-secondary;-->

<!--    .nav-icon-ep {-->
<!--      transition: transform $transition-fast, color $transition-fast;-->
<!--    }-->

<!--    .nav-label {-->
<!--      font-size: $font-size-xs;-->
<!--      color: inherit;-->
<!--      font-weight: $font-weight-medium;-->
<!--      transition: font-weight $transition-fast;-->
<!--    }-->

<!--    &.active {-->
<!--      color: $color-primary;-->

<!--      .nav-icon-ep {-->
<!--        transform: scale(1.08);-->
<!--        color: $color-primary;-->
<!--      }-->

<!--      .nav-label {-->
<!--        font-weight: $font-weight-semibold;-->
<!--      }-->
<!--    }-->

<!--    &:active .nav-icon-ep {-->
<!--      transform: scale(0.96);-->
<!--    }-->
<!--  }-->
<!--}-->
<!--</style>-->
<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 顶部导航栏 -->
    <header class="sticky top-0 z-30 bg-white/95 backdrop-blur-sm border-b border-gray-100 px-6 py-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="text-xl font-black text-orange-500">🍔</span>
          <span class="text-lg font-black text-gray-800">校食达</span>
        </div>

        <el-dropdown @command="handleCommand" trigger="click">
          <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-50 px-3 py-2 rounded-2xl transition-all">
            <el-avatar :size="32" :src="avatarDisplaySrc" class="border-2 border-orange-200">
              {{ userStore.userName?.charAt(0) }}
            </el-avatar>
            <span class="text-sm font-bold text-gray-700">{{ userStore.userName }}</span>
            <iconify-icon class="text-gray-400" icon="lucide:chevron-down"></iconify-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="rounded-2xl shadow-xl border border-gray-100">
              <el-dropdown-item command="profile" class="py-3">
                <span class="mr-2">👤</span>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.canSwitchToRider" command="switch" class="py-3">
                <span class="mr-2">🔄</span>
                切换到骑手端
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided class="py-3 text-red-500">
                <span class="mr-2">🚪</span>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="flex-1 overflow-y-auto custom-scrollbar">
      <router-view />
    </main>

    <!-- 底部导航栏 -->
    <nav class="sticky bottom-0 bg-white border-t border-gray-100 h-20 flex items-center justify-around px-4 z-30 shadow-[0_-4px_16px_rgba(0,0,0,0.06)]">
      <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex flex-col items-center gap-1 transition-all"
          :class="isActive(item.path) ? 'text-orange-500' : 'text-gray-400'"
      >
        <span class="relative">
          <el-badge
              v-if="item.path === '/student/cart'"
              :value="cartStore.itemCount"
              :hidden="cartStore.itemCount === 0"
              :max="99"
              class="cart-badge"
          >
            <iconify-icon
                class="text-2xl transition-transform"
                :class="isActive(item.path) ? 'scale-110' : ''"
                :icon="item.iconName"
            ></iconify-icon>
          </el-badge>
          <iconify-icon
              v-else
              class="text-2xl transition-transform"
              :class="isActive(item.path) ? 'scale-110' : ''"
              :icon="item.iconName"
          ></iconify-icon>
        </span>
        <span class="text-[10px] font-black uppercase" :class="isActive(item.path) ? 'text-orange-600' : ''">
          {{ item.label }}
        </span>
      </router-link>

      <!-- 中间突出的购物车按钮 -->
      <div class="relative -top-6">
        <router-link to="/student/cart" class="w-14 h-14 bg-gradient-to-br from-orange-500 to-orange-600 rounded-full flex items-center justify-center shadow-xl shadow-orange-200 text-white border-4 border-white hover:scale-105 transition-transform">
          <el-badge
              :value="cartStore.itemCount"
              :hidden="cartStore.itemCount === 0"
              :max="99"
              class="cart-badge-center"
          >
            <iconify-icon class="text-2xl" icon="lucide:shopping-cart"></iconify-icon>
          </el-badge>
        </router-link>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getImageUrl } from '@/utils/imageUrl'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const avatarDisplaySrc = computed(() => getImageUrl(userStore.userAvatar))

// 导航项 - 使用 Iconify 图标名称
const navItems = [
  { path: '/student', iconName: 'lucide:home', label: '首页' },
  { path: '/student/group-list', iconName: 'lucide:users', label: '拼单' },
  { path: '/student/orders', iconName: 'lucide:clipboard-list', label: '订单' },
  { path: '/student/profile', iconName: 'lucide:user', label: '我的' }
]

const isActive = (path) => {
  if (path === '/student') {
    return route.path === '/student' || route.path === '/student/'
  }
  if (path === '/student/cart') {
    return (
        route.path.startsWith('/student/cart') || route.path.startsWith('/student/checkout')
    )
  }
  return route.path.startsWith(path)
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/student/profile')
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'bg-orange-500 hover:bg-orange-600',
      cancelButtonClass: 'bg-gray-200 hover:bg-gray-300'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'switch') {
    const result = userStore.switchRole()
    if (result.success) {
      ElMessage.success('已切换到骑手端')
      router.push(result.redirectPath)
    }
  }
}
</script>

<style scoped>
/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  display: none;
}
.custom-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* Badge 样式优化 */
:deep(.cart-badge .el-badge__content),
:deep(.cart-badge-center .el-badge__content) {
  border: 2px solid white;
  background: linear-gradient(135deg, #f97316, #ea580c);
  font-size: 10px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
  min-width: 18px;
  font-weight: bold;
}

:deep(.cart-badge-center .el-badge__content) {
  right: -8px;
  top: -8px;
}

/* Dropdown 样式优化 */
:deep(.el-dropdown-menu__item) {
  padding: 10px 16px;
  font-size: 14px;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #fff7ed;
  color: #f97316;
}

:deep(.el-dropdown-menu__item--divided) {
  margin-top: 6px;
  border-top-color: #f3f4f6;
}
</style>
