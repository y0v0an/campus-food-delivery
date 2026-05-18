<template>
  <div class="order-pool-page">
    <div class="page-header">
      <h1 class="page-title">订单池</h1>
      <el-button :icon="Refresh" circle @click="loadOrders" />
    </div>

    <div class="online-status">
      <span class="status-label">接单状态</span>
      <el-switch
        v-model="isOnline"
        active-text="在线"
        inactive-text="离线"
        @change="toggleOnline"
      />
    </div>

    <div class="order-list" v-if="isOnline">
      <div v-if="orders.length === 0" class="empty-state">
        <div class="empty-ring">
          <span class="empty-emoji">📋</span>
        </div>
        <p class="empty-text">暂无待接订单</p>
        <p class="empty-hint">开启在线并保持刷新，新单会出现在这里</p>
      </div>

      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-card-inner">
          <div v-if="orderCoverUrl(order)" class="order-thumb">
            <img :src="orderCoverUrl(order)" :alt="order.merchantName" loading="lazy" />
          </div>
          <div
            v-else
            class="order-thumb order-thumb--ph"
            :style="{ background: placeholderGradient(order.merchantName) }"
          >
            {{ merchantInitial(order.merchantName) }}
          </div>
          <div class="order-main">
            <div class="order-header">
              <span class="merchant-name">{{ order.merchantName }}</span>
              <span class="order-time">{{ formatTime(order.createdAt) }}</span>
            </div>
            <div class="order-route">
              <div class="route-point">
                <span class="point-icon">🏪</span>
                <span class="point-text">取餐 · 商家</span>
              </div>
              <div class="route-line"></div>
              <div class="route-point">
                <span class="point-icon">📍</span>
                <span class="point-text">{{ order.addressBuilding }} {{ order.addressRoom }}</span>
              </div>
            </div>
            <div class="order-info">
              <span class="info-item">
                <span class="label">商品</span>
                {{ (order.items || []).length }} 件
              </span>
              <span class="info-item fee">
                <span class="label">配送费</span>
                ¥{{ formatMoney(order.deliveryFee) }}
              </span>
              <!-- 拼单订单标记 -->
              <span v-if="order.groupOrderId" class="info-item group-badge">
                <span class="badge-icon">👥</span>
                <span>{{ order.groupOrderTotal || 2 }}人拼单</span>
              </span>
            </div>
          </div>
        </div>
        <div class="order-actions">
          <el-button @click="showOrderDetail(order)">详情</el-button>
          <el-button type="primary" @click="acceptOrder(order)">接单</el-button>
        </div>
      </div>
    </div>

    <div class="offline-state" v-else>
      <div class="offline-visual">😴</div>
      <p class="offline-text">当前为离线</p>
      <p class="offline-hint">上线后即可查看并接取待配送订单</p>
    </div>

    <el-drawer
      v-model="detailVisible"
      title="订单详情"
      direction="btt"
      size="88%"
      class="pool-order-drawer"
      append-to-body
      :destroy-on-close="false"
      @closed="onDetailClosed"
    >
      <div class="pool-order-drawer-body">
        <OrderDetail v-if="currentOrder" :key="currentOrder.id" :order="currentOrder" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import OrderDetail from '@/components/order/OrderDetail.vue'
import { useUserStore } from '@/stores/user'
import { useOrderStore } from '@/stores/order'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'

const userStore = useUserStore()
const orderStore = useOrderStore()

const isOnline = ref(true)
const orders = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)

let refreshTimer = null

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000 / 60)

  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  return `${Math.floor(diff / 60)}小时前`
}

const formatMoney = (v) => {
  if (v == null || v === '') return '0.00'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

const orderCoverUrl = (order) => {
  const items = order.items || []
  if (!items.length) return ''
  return getImageUrl(items[0].dishImage) || ''
}

const merchantInitial = (name) => (name && String(name).charAt(0)) || '店'

const placeholderGradient = (name) => {
  let h = 0
  const s = name || '店'
  for (let i = 0; i < s.length; i++) h = (h + s.charCodeAt(i) * 17) % 360
  return `linear-gradient(135deg, hsl(${h}, 58%, 90%) 0%, hsl(${(h + 35) % 360}, 48%, 82%) 100%)`
}

const loadOrders = async () => {
  if (!isOnline.value) return

  await orderStore.loadPendingOrders()
  orders.value = orderStore.pendingOrders
}

const toggleOnline = (value) => {
  if (value) {
    ElMessage.success('已上线')
    loadOrders()
  } else {
    ElMessage.info('已离线')
    orders.value = []
  }
}

const showOrderDetail = (order) => {
  const oid = order.id
  currentOrder.value = { ...order }
  detailVisible.value = true
  request
    .get(`/order/${oid}`)
    .then((full) => {
      if (!detailVisible.value || currentOrder.value?.id !== oid) return
      currentOrder.value = full || currentOrder.value
    })
    .catch(() => {})
}

const onDetailClosed = () => {
  currentOrder.value = null
}

const acceptOrder = async (order) => {
  const result = await orderStore.riderAcceptOrder(order.id, userStore.userId, userStore.userName)

  if (result.success) {
    ElMessage.success('接单成功')
    loadOrders()
  } else {
    ElMessage.error(result.message || '接单失败')
  }
}

watch(() => orderStore.pendingOrders, (newOrders) => {
  orders.value = newOrders
})

onMounted(() => {
  if (isOnline.value) {
    loadOrders()
    refreshTimer = setInterval(loadOrders, 20000)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.order-pool-page {
  padding: $spacing-base;
  min-height: 100%;
  background: linear-gradient(180deg, #eef6ff 0%, #f5f7fa 30%, #f5f7fa 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;

  .page-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
  }
}

.online-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-base;
  background: $color-bg-white;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
  border: 1px solid $color-border-extra-light;
  box-shadow: $shadow-sm;

  .status-label {
    font-weight: $font-weight-medium;
  }
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.empty-state {
  text-align: center;
  padding: 48px $spacing-lg;
  background: linear-gradient(180deg, #fff 0%, #f0f7ff 100%);
  border-radius: $border-radius-lg;
  border: 1px solid rgba(74, 144, 217, 0.1);

  .empty-ring {
    display: inline-flex;
    width: 88px;
    height: 88px;
    border-radius: 50%;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(126, 200, 255, 0.25) 0%, rgba(74, 144, 217, 0.1) 100%);
    margin-bottom: $spacing-md;
  }

  .empty-emoji {
    font-size: 40px;
  }

  .empty-text {
    font-size: $font-size-md;
    color: $color-text-primary;
    margin-bottom: $spacing-sm;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.order-card {
  background: $color-bg-white;
  border-radius: $border-radius-md;
  padding: $spacing-base;
  border: 1px solid $color-border-extra-light;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);

  .order-card-inner {
    display: flex;
    gap: $spacing-md;
    margin-bottom: $spacing-md;
  }

  .order-thumb {
    flex-shrink: 0;
    width: 76px;
    height: 76px;
    border-radius: $border-radius-md;
    overflow: hidden;
    border: 1px solid $color-border-lighter;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    &--ph {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      font-weight: $font-weight-bold;
      color: #fff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
    }
  }

  .order-main {
    flex: 1;
    min-width: 0;
  }

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;

    .merchant-name {
      font-weight: $font-weight-semibold;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .order-time {
      font-size: $font-size-xs;
      color: $color-text-secondary;
      flex-shrink: 0;
    }
  }

  .order-route {
    padding: $spacing-sm $spacing-md;
    background: $color-bg-light;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-sm;

    .route-point {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      .point-text {
        font-size: $font-size-sm;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .route-line {
      width: 2px;
      height: 16px;
      background: $color-border-base;
      margin: $spacing-xs 0 $spacing-xs 8px;
    }
  }

  .order-info {
    display: flex;
    gap: $spacing-lg;
    font-size: $font-size-sm;
    flex-wrap: wrap;

    .label {
      color: $color-text-secondary;
      margin-right: 4px;
    }

    .fee {
      color: $color-success;
      font-weight: $font-weight-semibold;
    }

    .group-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 2px 8px;
      background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
      border: 1px solid #fed7aa;
      border-radius: 12px;
      color: #ea580c;
      font-weight: 600;
      font-size: $font-size-xs;

      .badge-icon {
        font-size: 12px;
      }
    }
  }

  .order-actions {
    display: flex;
    gap: $spacing-sm;

    .el-button {
      flex: 1;
    }
  }
}

.offline-state {
  text-align: center;
  padding: 72px $spacing-lg;
  background: $color-bg-white;
  border-radius: $border-radius-md;

  .offline-visual {
    font-size: 56px;
    margin-bottom: $spacing-md;
  }

  .offline-text {
    font-size: $font-size-lg;
    color: $color-text-primary;
    margin-bottom: $spacing-sm;
  }

  .offline-hint {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.pool-order-drawer-body {
  min-height: 30vh;
}
</style>

<style lang="scss">
.pool-order-drawer.el-drawer {
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.12);
}

.pool-order-drawer .el-drawer__header {
  margin-bottom: 0;
  padding-bottom: 12px;
}

.pool-order-drawer .el-drawer__body {
  padding: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
}
</style>
