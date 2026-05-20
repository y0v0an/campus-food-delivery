<template>
  <div class="group-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">拼单专区</h1>
      <div class="header-right">
        <button
          class="active-orders-btn"
          :class="{ hasActive: activeOrdersCount > 0 }"
          @click="drawerVisible = true"
        >
          <iconify-icon icon="lucide:users" class="icon"></iconify-icon>
          <span class="btn-text">正在拼单</span>
          <span class="count-badge" v-if="activeOrdersCount > 0">{{ activeOrdersCount }}</span>
        </button>
        <iconify-icon icon="lucide:refresh-cw" class="refresh-icon" @click="loadAllData"></iconify-icon>
      </div>
    </div>

    <!-- 拼单优惠 Banner -->
    <div class="px-6 py-4">
      <div class="h-28 bg-gradient-to-r from-orange-500 to-orange-600 rounded-3xl relative overflow-hidden flex items-center p-5 text-white shadow-lg shadow-orange-200">
        <div class="relative z-10">
          <h3 class="text-xl font-black italic">拼单立减 15%</h3>
          <p class="text-xs opacity-90 mt-1">多人下单，共享优惠</p>
        </div>
        <img alt="Group eating" class="absolute right-[-20px] bottom-[-10px] w-36 h-36 object-contain opacity-40" src="https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=400&fit=crop"/>
      </div>
    </div>

    <!-- 排序切换 -->
    <div class="sort-tabs px-6 py-3">
      <div
        class="sort-tab"
        :class="{ active: sortBy === 'default' }"
        @click="sortBy = 'default'; loadDishes()"
      >
        默认
      </div>
      <div
        class="sort-tab"
        :class="{ active: sortBy === 'discount' }"
        @click="sortBy = 'discount'; loadDishes()"
      >
        优惠最大
      </div>
      <div
        class="sort-tab"
        :class="{ active: sortBy === 'sales' }"
        @click="sortBy = 'sales'; loadDishes()"
      >
        销量最高
      </div>
    </div>

    <!-- 可拼单菜品列表 -->
    <div class="dish-list px-6 pb-6" v-if="dishes.length > 0">
      <GroupDishCard
        v-for="dish in dishes"
        :key="dish.id"
        :dish="dish"
        @start-group="handleStartGroup"
      />
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="!loading">
      <div class="empty-icon">🍜</div>
      <p class="empty-text">当前没有可拼单的菜品</p>
      <button class="create-btn" @click="goHome">去商家页面看看</button>
    </div>

    <!-- 加载中 -->
    <div class="loading-state" v-if="loading">
      <iconify-icon icon="lucide:loader-2" class="loading-icon"></iconify-icon>
      <p>加载中...</p>
    </div>

    <!-- 正在拼单抽屉 -->
    <GroupOrderDrawer
      v-model="drawerVisible"
      :orders="activeOrders"
      :userId="userStore.userId"
      @join="handleJoinFromDrawer"
    />

    <!-- 参与拼单对话框 -->
    <el-dialog
      v-model="joinDialogVisible"
      title="参与拼单"
      width="90%"
      :style="{ maxWidth: '400px' }"
    >
      <p v-if="joiningOrder">确定要参与「{{ joiningOrder.dishName }}」的拼单吗？</p>
      <AddressSelector v-model="selectedAddress" />
      <template #footer>
        <el-button @click="joinDialogVisible = false">再想想</el-button>
        <el-button type="primary" @click="confirmJoin">确定参与</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/axios/request'
import { useUserStore } from '@/stores/user'
import GroupDishCard from '@/components/order/GroupDishCard.vue'
import GroupOrderDrawer from '@/components/order/GroupOrderDrawer.vue'
import AddressSelector from '@/components/common/AddressSelector.vue'

const router = useRouter()
const userStore = useUserStore()

// 排序方式
const sortBy = ref('default')

// 可拼单菜品列表
const dishes = ref([])

// 正在拼单列表
const activeOrders = ref([])

// 抽屉显示状态
const drawerVisible = ref(false)
const selectedAddress = ref(null)
const joinDialogVisible = ref(false)
const joiningOrder = ref(null)

// 加载状态
const loading = ref(false)

// 刷新定时器
let refreshTimer = null

// 正在拼单数量
const activeOrdersCount = computed(() => activeOrders.value.length)

// 加载可拼单菜品
const loadDishes = async () => {
  try {
    const result = await request.get('/dish/group-enabled', {
      params: {
        sortBy: sortBy.value,
        userId: userStore.userId
      }
    })
    dishes.value = result || []
  } catch (error) {
    console.error('加载可拼单菜品失败:', error)
    dishes.value = []
  }
}

// 加载正在拼单列表
const loadActiveOrders = async () => {
  try {
    const result = await request.get('/group-order/hot', { params: { limit: 100 } })
    activeOrders.value = (result || []).filter(go => ['open', 'full'].includes(go.status))
  } catch (error) {
    console.error('加载正在拼单失败:', error)
    activeOrders.value = []
  }
}

// 加载所有数据
const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([loadDishes(), loadActiveOrders()])
  } finally {
    loading.value = false
  }
}

// 处理发起拼单
const handleStartGroup = async (dish) => {
  // 检查是否有同一菜品的进行中拼单
  const existingOrder = activeOrders.value.find(
    o => o.merchantId === dish.merchant.id && o.dishId === dish.id
  )

  if (existingOrder) {
    try {
      await ElMessageBox.confirm(
        `该菜品已有拼单正在进行，要加入吗？\n当前进度：${existingOrder.currentCount}/${existingOrder.targetCount}人`,
        '发现已有拼单',
        {
          confirmButtonText: '加入拼单',
          cancelButtonText: '发起新的',
          type: 'info'
        }
      )
      // 用户选择加入
      await joinGroupOrder(existingOrder)
      return
    } catch (error) {
      // 用户选择发起新的，继续
    }
  }

  // 检查商家是否打烊
  if (!dish.merchant.isOpen) {
    ElMessage.warning('该商家已打烊，无法发起拼单')
    return
  }

  // 跳转到商家详情页，带上发起拼单参数
  router.push({
    path: `/student/merchant/${dish.merchant.id}`,
    query: { startGroup: dish.id }
  })
}

// 参与拼单
const joinGroupOrder = async (order) => {
  joiningOrder.value = order
  selectedAddress.value = null // 重置地址
  joinDialogVisible.value = true
}

// 从抽屉参与拼单
const handleJoinFromDrawer = async (order) => {
  joiningOrder.value = order
  selectedAddress.value = null // 重置地址
  joinDialogVisible.value = true
}

// 确认参与拼单
const confirmJoin = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    await request.post('/group-order/join', {
      groupOrderId: joiningOrder.value.id,
      userId: userStore.userId,
      address: {
        building: selectedAddress.value.building,
        room: selectedAddress.value.room,
        contact: selectedAddress.value.contact,
        phone: selectedAddress.value.phone
      }
    })
    ElMessage.success('参与成功！')
    joinDialogVisible.value = false
    await loadAllData()
  } catch (error) {
    // interceptor handles message
  }
}

// 去首页
const goHome = () => {
  router.push('/student/home')
}

onMounted(() => {
  loadAllData()
  // 定时刷新（30秒）
  refreshTimer = setInterval(loadAllData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.group-list-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #fff7ed 0%, #f5f7fa 20%, #f5f7fa 100%);
  padding-bottom: 100px;
}

/* 页面头部 */
.page-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.header-right {
  position: absolute;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.active-orders-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #f5f7fa;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.active-orders-btn.hasActive {
  background: #fff7ed;
  color: #FF7A45;
}

.active-orders-btn .icon {
  font-size: 16px;
}

.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #FF7A45;
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
}

.refresh-icon {
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  transition: transform 0.3s, color 0.2s;
}

.refresh-icon:hover {
  color: #FF7A45;
  transform: rotate(180deg);
}

/* 排序标签 */
.sort-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
}

.sort-tab {
  flex-shrink: 0;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-tab.active {
  background: #FF7A45;
  color: white;
}

/* 菜品列表 */
.dish-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  color: #909399;
  font-size: 14px;
  margin-bottom: 24px;
}

.create-btn {
  padding: 10px 24px;
  background: #FF7A45;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

/* 加载中 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-icon {
  font-size: 32px;
  color: #FF7A45;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
