<template>
  <aside class="app-sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-header">
      <div class="logo" v-if="!isCollapsed">
        <span class="logo-icon">🍔</span>
        <span class="logo-text">{{ title }}</span>
      </div>
      <span class="logo-icon" v-else>🍔</span>
    </div>

    <div class="sidebar-menu">
      <template v-for="item in menuItems" :key="item.path">
        <!-- 有子菜单 -->
        <div v-if="item.children?.length" class="menu-group">
          <div
            class="menu-item has-children"
            :class="{ active: isMenuActive(item.path) || hasActiveChild(item) }"
            @click="toggleSubmenu(item.path)"
          >
            <iconify-icon :icon="item.icon" class="menu-icon"></iconify-icon>
            <span v-if="!isCollapsed">{{ item.title }}</span>
            <iconify-icon
              v-if="!isCollapsed"
              icon="lucide:chevron-down"
              class="chevron"
              :class="{ expanded: expandedMenus[item.path] }"
            ></iconify-icon>
          </div>
          <div class="submenu" v-if="!isCollapsed && expandedMenus[item.path]">
            <div
              v-for="child in item.children"
              :key="child.path"
              class="submenu-item"
              :class="{ active: isActive(child.path) }"
              @click="navigateTo(child.path)"
            >
              <iconify-icon v-if="child.icon" :icon="child.icon" class="menu-icon submenu-icon"></iconify-icon>
              <span>{{ child.title }}</span>
            </div>
          </div>
        </div>

        <!-- 无子菜单 -->
        <div
          v-else
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          @click="navigateTo(item.path)"
        >
          <iconify-icon :icon="item.icon" class="menu-icon"></iconify-icon>
          <span v-if="!isCollapsed">{{ item.title }}</span>
        </div>
      </template>
    </div>

    <div class="sidebar-footer">
      <button
        class="footer-btn logout-btn"
        @click="handleLogout"
      >
        <iconify-icon icon="lucide:log-out"></iconify-icon>
        <span v-if="!isCollapsed">退出系统</span>
      </button>
      <button
        class="footer-btn collapse-btn"
        @click="toggleCollapse"
      >
        <iconify-icon :icon="isCollapsed ? 'lucide:chevron-right' : 'lucide:chevron-left'"></iconify-icon>
        <span v-if="!isCollapsed">收起菜单</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  title: {
    type: String,
    default: '校园专属'
  },
  menuItems: {
    type: Array,
    default: () => []
  },
  defaultCollapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(props.defaultCollapsed)
const expandedMenus = ref({})

const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const toggleSubmenu = (path) => {
  expandedMenus.value[path] = !expandedMenus.value[path]
}

const hasActiveChild = (item) => {
  if (!item.children) return false
  return item.children.some(child => isActive(child.path))
}

const isMenuActive = (path) => {
  return activeMenu.value?.startsWith(path)
}

const isActive = (path) => {
  if (path === '/rider') {
    return (
      route.path === '/rider' ||
      route.path === '/rider/' ||
      route.path.startsWith('/rider/pool')
    )
  }
  return route.path?.startsWith(path)
}

const navigateTo = (path) => {
  router.push(path)
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.app-sidebar {
  width: 220px;
  height: 100vh;
  background: white;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  transition: width 0.25s;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}

.app-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid #EBEEF5;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #FF7A45;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 12px 8px;
}

.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #DCDFE6;
  border-radius: 2px;
}

.menu-group {
  margin-bottom: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  color: #606266;
  font-size: 14px;
  position: relative;
}

.menu-item:hover {
  background: #F5F7FA;
}

.menu-item.active {
  background: linear-gradient(135deg, rgba(255, 122, 69, 0.1) 0%, rgba(255, 122, 69, 0.05) 100%);
  color: #FF7A45;
  font-weight: 500;
}

.menu-item .chevron {
  margin-left: auto;
  transition: transform 0.2s;
}

.menu-item .chevron.expanded {
  transform: rotate(180deg);
}

.menu-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.submenu-icon {
  width: 16px;
  height: 16px;
}

.submenu {
  margin-left: 38px;
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.submenu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
  color: #606266;
  font-size: 13px;
}

.submenu-item:hover {
  background: #F5F7FA;
}

.submenu-item.active {
  background: rgba(255, 122, 69, 0.08);
  color: #FF7A45;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #EBEEF5;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
  background: transparent;
}

.footer-btn:hover {
  background: #F5F7FA;
}

.logout-btn {
  color: #F5222D;
}

.logout-btn:hover {
  background: #FEF0F0;
}

.collapse-btn {
  color: #909399;
}
</style>
