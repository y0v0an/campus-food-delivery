<template>
  <div class="order-track-page" v-if="order">
    <!-- 头部 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack">返回</el-button>
      <h1 class="page-title">订单详情</h1>
      <div></div>
    </div>

    <!-- 订单状态 -->
    <div class="status-section">
      <StatusTag :status="order.status" size="large" :showDot="isActiveOrder" />
      <p class="status-desc">{{ statusDescription }}</p>
    </div>

    <!-- 配送地图（仅外卖配送） -->
    <div class="map-section" v-if="showMap && order?.orderType !== 'dine_in'">
      <DeliveryMap
        :merchantLocation="merchantLocation"
        :studentLocation="studentLocation"
        :riderLocation="riderLocation"
        :estimatedTime="estimatedTime"
      />
    </div>

    <!-- 配送时间线 -->
    <div class="timeline-section">
      <TrackTimeline :order="order" />
    </div>

    <!-- 订单详情 -->
    <OrderDetail :order="order" hide-status />

    <!-- 底部操作 -->
    <div class="action-bar" v-if="showActionBar">
      <el-button v-if="canCancel" @click="cancelOrder">取消订单</el-button>
      <el-button
        v-if="canConfirmDineIn"
        type="primary"
        :loading="confirming"
        @click="confirmDineInPickup"
      >
        确认取餐
      </el-button>
      <el-button v-if="canReview" type="success" @click="goReview">评价订单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import DeliveryMap from '@/components/delivery/DeliveryMap.vue'
import TrackTimeline from '@/components/delivery/TrackTimeline.vue'
import OrderDetail from '@/components/order/OrderDetail.vue'
import { useOrderStore } from '@/stores/order'
import { useDeliveryStore } from '@/stores/delivery'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const deliveryStore = useDeliveryStore()
const userStore = useUserStore()

// 订单数据
const order = ref(null)
const confirming = ref(false)

// 商家位置
const merchantLocation = ref(null)

// 学生位置
const studentLocation = ref(null)

// 骑手位置
const riderLocation = ref(null)

// 预计送达时间
const estimatedTime = ref(0)

// 刷新定时器
let refreshTimer = null

// 是否是活跃订单
const isActiveOrder = computed(() => {
  return ['pending_accept', 'preparing', 'ready', 'delivering'].includes(order.value?.status)
})

// 状态描述
const statusDescription = computed(() => {
  const isDine = order.value?.orderType === 'dine_in'
  const descMap = {
    pending_payment: '请尽快完成支付',
    pending_accept: '商家正在确认订单',
    preparing: '商家正在准备您的餐品',
    ready: isDine ? '餐品已备好，请到店取餐' : '餐品已备好，等待骑手取餐',
    delivering: '骑手正在配送中',
    completed: '订单已完成，感谢您的支持',
    cancelled: '订单已取消'
  }
  return descMap[order.value?.status] || ''
})

// 是否显示地图
const showMap = computed(() => {
  return ['delivering', 'completed'].includes(order.value?.status)
})

// 是否可以取消
const canCancel = computed(() => {
  return ['pending_payment', 'pending_accept'].includes(order.value?.status)
})

// 是否可以评价
const canReview = computed(() => {
  return order.value?.status === 'completed' && !order.value?.review
})

// 店内就餐：商家出餐后学生确认取餐
const canConfirmDineIn = computed(() => {
  return (
    order.value?.orderType === 'dine_in' &&
    order.value?.status === 'ready'
  )
})

// 底部操作栏：有任一可操作按钮时才显示（避免空条）
const showActionBar = computed(() => {
  if (!order.value || order.value.status === 'cancelled') return false
  return canCancel.value || canConfirmDineIn.value || canReview.value
})

// 返回
const goBack = () => {
  router.back()
}

const confirmDineInPickup = async () => {
  if (!order.value?.id || !userStore.userId) return
  confirming.value = true
  try {
    const result = await orderStore.studentConfirmPickup(order.value.id, userStore.userId)
    if (result.success) {
      ElMessage.success('已确认取餐')
      await loadOrder()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } finally {
    confirming.value = false
  }
}

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await orderStore.cancelOrder(order.value.id)
    if (result.success) {
      ElMessage.success('订单已取消')
      await loadOrder()
    } else {
      ElMessage.error(result.message)
    }
  } catch {
    // 用户取消
  }
}

// 去评价
const goReview = () => {
  router.push(`/student/review/${order.value.id}`)
}

// 加载订单数据
const loadOrder = async () => {
  const orderId = route.params.id
  try {
    await orderStore.fetchOrderDetail(orderId)
    order.value = orderStore.currentOrder

    if (order.value) {
      // 加载商家位置
      try {
        const merchant = await request.get(`/merchant/${order.value.merchantId}`)
        if (merchant && merchant.lat && merchant.lng) {
          merchantLocation.value = { lat: merchant.lat, lng: merchant.lng }
        }
      } catch (e) {
        console.error('加载商家位置失败:', e)
      }

      // 学生位置（从订单地址中获取）
      studentLocation.value = { 
        lat: 30.5728, 
        lng: 104.0668,
        address: `${order.value.addressBuilding} ${order.value.addressRoom}`
      }

      // 配送中时显示骑手位置（模拟）
      if (order.value.status === 'delivering' && order.value.riderId) {
        riderLocation.value = { lat: 30.5730, lng: 104.0670 }
        estimatedTime.value = 15
      }
    }
  } catch (error) {
    console.error('加载订单失败:', error)
  }
}

// 刷新数据
const refreshData = () => {
  if (isActiveOrder.value) {
    loadOrder()
  }
}

// 监听订单状态变化
watch(() => orderStore.currentOrder, (newOrder) => {
  if (newOrder) {
    order.value = newOrder
  }
})

onMounted(() => {
  loadOrder()
  
  // 活跃订单定时刷新
  refreshTimer = setInterval(refreshData, 10000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.order-track-page {
  min-height: 100vh;
  background: $color-bg-base;
  padding-bottom: 80px;
}

// 头部
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-base;
  background: $color-bg-white;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;

  .page-title {
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
  }
}

// 状态区域
.status-section {
  text-align: center;
  padding: $spacing-xl $spacing-base;
  background: linear-gradient(135deg, rgba($color-primary, 0.05) 0%, rgba($color-primary, 0.02) 100%);

  .status-desc {
    margin-top: $spacing-md;
    color: $color-text-secondary;
    font-size: $font-size-sm;
  }
}

// 地图区域
.map-section {
  margin: $spacing-base;
}

// 时间线区域
.timeline-section {
  margin: $spacing-base;
}

// 底部操作栏
.action-bar {
  position: fixed;
  bottom: $bottom-nav-height;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-base;
  background: $color-bg-white;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>
