<script setup>
import { RouterView, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 应用挂载时初始化用户状态（包括 storage 事件监听）
onMounted(() => {
  userStore.initUserStatus()
})

// 应用卸载时清理监听器
onUnmounted(() => {
  userStore.cleanupStorageListener()
})
</script>

<template>
  <RouterView />
</template>

<style scoped>
</style>
