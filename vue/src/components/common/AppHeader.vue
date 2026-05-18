<template>
  <header class="app-header">
    <div class="header-left">
      <div class="logo" @click="goHome">
        <span class="logo-icon">🍔</span>
        <span class="logo-text">校园专属</span>
      </div>
    </div>

    <div class="header-center">
      <slot name="center"></slot>
    </div>

    <div class="header-right">
      <slot name="right">
        <!-- 默认用户信息 -->
        <div class="user-info" v-if="userStore.isLogin">
          <div class="user-dropdown" @click="showUserMenu = !showUserMenu">
            <el-avatar :size="32" :src="headerAvatarSrc">
              {{ userStore.userName?.charAt(0) }}
            </el-avatar>
            <span class="user-name">{{ userStore.userName }}</span>
            <iconify-icon icon="lucide:chevron-down"></iconify-icon>
          </div>
          <!-- 下拉菜单 -->
          <div class="user-menu" v-if="showUserMenu" @click="showUserMenu = false">
            <div class="menu-item" v-if="userStore.canSwitchToRider" @click.stop="handleCommand('switch')">
              <iconify-icon icon="lucide:switch"></iconify-icon>
              切换为{{ userStore.isRiderRole ? '学生' : '骑手' }}
            </div>
            <div class="menu-item" @click.stop="handleCommand('profile')">
              <iconify-icon icon="lucide:user"></iconify-icon>
              个人中心
            </div>
            <div class="menu-divider"></div>
            <div class="menu-item text-danger" @click.stop="handleCommand('logout')">
              <iconify-icon icon="lucide:log-out"></iconify-icon>
              退出登录
            </div>
          </div>
        </div>
      </slot>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getImageUrl } from '@/utils/imageUrl'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const showUserMenu = ref(false)

const headerAvatarSrc = computed(() => {
  const raw =
    userStore.currentRole === 'merchant' && userStore.merchantProfileLogo
      ? userStore.merchantProfileLogo
      : userStore.userInfo?.avatar || ''
  return getImageUrl(raw)
})

const goHome = () => {
  const path = userStore.redirectPath || '/'
  router.push(path)
}

const handleCommand = (command) => {
  showUserMenu.value = false
  switch (command) {
    case 'switch':
      const result = userStore.switchRole()
      if (result.success) {
        ElMessage.success(`已切换为${result.newRole === 'rider' ? '骑手' : '学生'}模式`)
        router.push(result.redirectPath)
      }
      break
    case 'profile': {
      const profileByRole = {
        student: '/student/profile',
        merchant: '/merchant/profile',
        rider: '/rider/pool',
        admin: '/admin/profile'
      }
      const path = profileByRole[userStore.currentRole]
      if (path) router.push(path)
      break
    }
    case 'logout':
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
      break
  }
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: opacity 0.15s;
}

.logo:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #FF7A45;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  position: relative;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.15s;
}

.user-dropdown:hover {
  background: #F5F7FA;
}

.user-name {
  font-size: 14px;
  color: #303133;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
</style>
