<template>
  <div class="my-orders-page">
    <!-- 头部 -->
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
    </div>

    <!-- 订单状态筛选 -->
    <div class="filter-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
        <span class="tab-count" v-if="getTabCount(tab.value) > 0">（{{ getTabCount(tab.value) }}）</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-if="filteredOrders.length > 0">
      <OrderCard
        v-for="order in filteredOrders"
        :key="order.id"
        :order="order"
        role="student"
        @pay="handlePay(order)"
        @confirmPickup="handleConfirmPickup(order)"
        @review="handleReview(order)"
        @view-review="handleViewReview(order)"
        @detail="handleDetail(order)"
      />
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <div class="empty-icon">📋</div>
      <p class="empty-text">暂无订单</p>
      <el-button type="primary" @click="goHome">去下单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import OrderCard from '@/components/order/OrderCard.vue'
import { useOrderStore, OrderStatus } from '@/stores/order'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

// 筛选标签
const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: OrderStatus.PENDING_PAYMENT },
  { label: '进行中', value: 'active' },
  { label: '已完成', value: OrderStatus.COMPLETED }
]

// 当前选中的标签
const activeTab = ref('all')

// 过滤后的订单
const filteredOrders = computed(() => {
  const orders = orderStore.orderList
  
  if (activeTab.value === 'all') {
    return orders
  }
  
  if (activeTab.value === 'active') {
    return orders.filter(o => 
      [OrderStatus.PENDING_ACCEPT, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.DELIVERING].includes(o.status)
    )
  }
  
  return orders.filter(o => o.status === activeTab.value)
})

// 获取标签数量
const getTabCount = (tabValue) => {
  const orders = orderStore.orderList
  
  if (tabValue === 'all') {
    return orders.length
  }
  
  if (tabValue === 'active') {
    return orders.filter(o => 
      [OrderStatus.PENDING_ACCEPT, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.DELIVERING].includes(o.status)
    ).length
  }
  
  return orders.filter(o => o.status === tabValue).length
}

// 去首页
const goHome = () => {
  router.push('/student')
}

const handleConfirmPickup = async (order) => {
  const result = await orderStore.studentConfirmPickup(order.id, userStore.userId)
  if (result.success) {
    ElMessage.success('已确认取餐')
    loadOrders()
  } else {
    ElMessage.error(result.message || '操作失败')
  }
}

// 去支付
const handlePay = (order) => {
  // 模拟支付
  const result = orderStore.payOrder(order.id)
  if (result.success) {
    ElMessage.success('支付成功')
    loadOrders()
  } else {
    ElMessage.error(result.message)
  }
}

// 去评价
const handleReview = (order) => {
  router.push(`/student/review/${order.id}`)
}

// 查看已提交的评价
const handleViewReview = (order) => {
  router.push(`/student/review/${order.id}`)
}

// 查看详情
const handleDetail = (order) => {
  router.push(`/student/order/${order.id}`)
}

// 加载订单
const loadOrders = () => {
  if (userStore.userId) {
    orderStore.loadStudentOrders(userStore.userId)
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.my-orders-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #eef6ff 0%, #f5f7fa 18%, #f5f7fa 100%);
}

// 头部
.page-header {
  padding: $spacing-lg $spacing-base $spacing-md;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid $color-border-extra-light;

  .page-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    letter-spacing: 0.02em;
  }
}

// 筛选标签
.filter-tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.96);
  padding: 0 $spacing-base $spacing-md;
  gap: $spacing-lg;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
  border-bottom: 1px solid $color-border-extra-light;

  .filter-tab {
    position: relative;
    padding: $spacing-sm 0;
    font-size: $font-size-base;
    color: $color-text-regular;
    font-weight: $font-weight-medium;
    cursor: pointer;
    transition: all $transition-fast;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: transparent;
      transition: background $transition-fast;
    }

    &.active {
      color: $color-primary;
      font-weight: $font-weight-semibold;

      &::after {
        background: $color-primary;
        height: 3px;
        border-radius: 2px 2px 0 0;
      }
    }

    .tab-count {
      margin-left: 2px;
      font-size: $font-size-sm;
      color: $color-text-secondary;
    }

    &.active .tab-count {
      color: $color-primary;
    }
  }
}

// 订单列表
.order-list {
  padding: $spacing-base;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px $spacing-lg;

  .empty-icon {
    font-size: 80px;
    margin-bottom: $spacing-lg;
    opacity: 0.5;
  }

  .empty-text {
    color: $color-text-regular;
    font-size: $font-size-base;
    margin-bottom: $spacing-lg;
  }
}
</style>
