<template>
  <div class="my-delivery-page">
    <!-- 头部 -->
    <div class="page-header">
      <h1 class="page-title">我的配送</h1>
      <el-button :icon="Refresh" circle @click="loadData" />
    </div>

    <!-- 今日概览 -->
    <div class="stats-row">
      <div class="stat-card stat-card--orders">
        <span class="stat-value">{{ todayCompletedCount }}</span>
        <span class="stat-label">今日完成</span>
      </div>
      <div class="stat-card stat-card--fee">
        <span class="stat-value accent">¥{{ todayFeeSum }}</span>
        <span class="stat-label">今日配送费</span>
      </div>
      <div class="stat-card stat-card--week">
        <span class="stat-value">{{ recentCompletedOrders.length }}</span>
        <span class="stat-label">近7天单量</span>
      </div>
    </div>

    <!-- 当前配送 -->
    <div class="current-delivery" v-if="currentOrder">
      <div class="delivery-status">
        <StatusTag :status="currentOrder.status" size="large" :showDot="true" />
        <span class="status-text">{{ statusText }}</span>
      </div>

      <div class="order-meta-card">
        <div class="meta-row">
          <span class="meta-label">订单号</span>
          <span class="meta-value mono">{{ currentOrder.orderNo }}</span>
        </div>
        <div class="meta-row" v-if="currentOrder.orderType === 'dine_in'">
          <el-tag type="info" size="small">店内订单</el-tag>
        </div>
        <div class="meta-row">
          <span class="meta-label">实付</span>
          <span class="meta-value price">¥{{ formatMoney(currentOrder.actualAmount) }}</span>
        </div>
        <div class="meta-row" v-if="currentOrder.remark">
          <span class="meta-label">备注</span>
          <span class="meta-value remark">{{ currentOrder.remark }}</span>
        </div>
      </div>

      <!-- 配送地图 -->
      <DeliveryMap
        :merchantLocation="merchantLocation"
        :studentLocation="studentLocation"
        :riderLocation="riderLocation"
        :estimatedTime="20"
      />

      <!-- 商品摘要 -->
      <div class="items-preview" v-if="currentOrderItems.length">
        <h3 class="preview-title">商品清单</h3>
        <ul class="preview-list">
          <li v-for="(it, idx) in currentOrderItems.slice(0, 5)" :key="it.id ?? idx">
            <div class="preview-thumb">
              <img
                v-if="getImageUrl(it.dishImage)"
                :src="getImageUrl(it.dishImage)"
                :alt="it.dishName"
              />
              <span v-else class="thumb-fallback">{{ dishInitial(it.dishName) }}</span>
            </div>
            <div class="preview-line">
              <span class="name">{{ it.dishName }}</span>
              <span class="qty">×{{ it.quantity }}</span>
            </div>
          </li>
        </ul>
        <p v-if="currentOrderItems.length > 5" class="preview-more">
          等 {{ currentOrderItems.length }} 件商品
        </p>
      </div>

      <!-- 配送信息 -->
      <div class="delivery-info">
        <div class="info-section">
          <h3 class="section-title">取餐地点</h3>
          <div class="info-content">
            <div class="merchant-avatar" v-if="currentMerchantLogo">
              <img :src="currentMerchantLogo" alt="" />
            </div>
            <span v-else class="info-icon">🏪</span>
            <div class="info-detail">
              <p class="info-main">{{ currentOrder.merchantName }}</p>
              <p class="info-sub">{{ merchantAddressText }}</p>
            </div>
            <el-button class="nav-action-btn" size="small" :icon="Position" circle />
          </div>
        </div>

        <div class="info-section">
          <h3 class="section-title">送餐地点</h3>
          <div class="info-content">
            <span class="info-icon">📍</span>
            <div class="info-detail">
              <p class="info-main">{{ currentOrder.addressBuilding }} {{ currentOrder.addressRoom }}</p>
              <p class="info-sub">{{ currentOrder.addressContact }} {{ currentOrder.addressPhone }}</p>
            </div>
            <el-button class="nav-action-btn" size="small" :icon="Phone" circle />
          </div>
        </div>
      </div>

      <div class="detail-link-row">
        <el-button text class="detail-text-btn" @click="openOrderDetail(currentOrder)">
          查看完整订单详情
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- 操作按钮 -->
      <div class="delivery-actions">
        <el-button
          v-if="currentOrder.status === 'delivering'"
          type="success"
          size="large"
          @click="deliverOrder"
        >
          已送达
        </el-button>
      </div>
    </div>

    <!-- 无配送任务 -->
    <div class="no-delivery" v-else>
      <div class="empty-visual" aria-hidden="true">
        <div class="empty-photo-ring">
          <div class="empty-inner">
            <svg class="empty-svg" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="emptyGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#7ec8ff;stop-opacity:1" />
                  <stop offset="100%" style="stop-color:#4a90d9;stop-opacity:1" />
                </linearGradient>
              </defs>
              <circle cx="60" cy="60" r="52" fill="url(#emptyGrad)" opacity="0.25" />
              <path
                fill="#4a90d9"
                d="M38 78c0-8 6-14 14-14h4l6-22c1-4 5-7 10-7h18v8H74l-4 14h8c10 0 18 8 18 18v6H38v-3zm12 0c3 0 5-2 5-5s-2-5-5-5-5 2-5 5 2 5 5 5zm34 0c3 0 5-2 5-5s-2-5-5-5-5 2-5 5 2 5 5 5z"
              />
              <circle cx="88" cy="42" r="10" fill="#ffb347" opacity="0.9" />
              <path fill="#fff" d="M84 40h8v4h-8z" opacity="0.9" />
            </svg>
          </div>
        </div>
      </div>
      <p class="empty-text">暂无进行中的配送</p>
      <p class="empty-hint">接单后任务会显示在这里</p>
      <el-button type="primary" round @click="goToPool">去接单</el-button>
    </div>

    <!-- 最近配送 -->
    <div class="history-section">
      <div class="section-head">
        <h2 class="section-title">最近配送</h2>
        <span class="section-sub">近7天已完成，点击查看详情</span>
      </div>
      <div class="history-list">
        <div
          v-for="order in recentCompletedOrders"
          :key="order.id"
          class="history-item"
          role="button"
          tabindex="0"
          @click="openOrderDetail(order)"
          @keydown.enter="openOrderDetail(order)"
        >
          <div class="item-thumb-wrap">
            <div v-if="orderCoverUrl(order)" class="item-thumb">
              <img :src="orderCoverUrl(order)" :alt="order.merchantName" loading="lazy" />
            </div>
            <div
              v-else
              class="item-thumb item-thumb--placeholder"
              :style="{ background: placeholderGradient(order.merchantName) }"
            >
              <span>{{ merchantInitial(order.merchantName) }}</span>
            </div>
            <div v-if="extraDishThumb(order)" class="item-thumb-mini">
              <img :src="extraDishThumb(order)" alt="" loading="lazy" />
            </div>
          </div>
          <div class="item-main-block">
            <div class="item-top">
              <span class="merchant-name">{{ order.merchantName }}</span>
              <StatusTag :status="order.status" size="small" />
            </div>
            <span class="order-no">{{ order.orderNo }}</span>
            <div class="item-address">
              {{ order.addressBuilding }} {{ order.addressRoom }}
            </div>
            <div class="item-goods">
              {{ itemSummary(order) }}
            </div>
            <div class="item-bottom">
              <span class="delivery-time">{{ formatDateTime(order.deliveredAt) }}</span>
              <span class="amount">实付 ¥{{ formatMoney(order.actualAmount) }}</span>
            </div>
          </div>
          <div class="item-side">
            <span class="delivery-fee">+¥{{ formatMoney(order.deliveryFee) }}</span>
            <el-icon class="chev"><ArrowRight /></el-icon>
          </div>
        </div>
        <el-empty v-if="recentCompletedOrders.length === 0" description="近7天暂无已完成配送" />
      </div>
    </div>

    <el-drawer
      v-model="detailVisible"
      title="订单详情"
      direction="btt"
      size="88%"
      class="order-detail-drawer"
      append-to-body
      :destroy-on-close="false"
      @closed="onDetailDrawerClosed"
    >
      <div class="order-detail-drawer-body">
        <OrderDetail v-if="detailOrder" :key="detailOrder.id" :order="detailOrder" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Position, Phone, Refresh, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import DeliveryMap from '@/components/delivery/DeliveryMap.vue'
import OrderDetail from '@/components/order/OrderDetail.vue'
import { useUserStore } from '@/stores/user'
import { useOrderStore } from '@/stores/order'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const userStore = useUserStore()
const orderStore = useOrderStore()

const currentOrder = ref(null)
const currentMerchant = ref(null)
const merchantAddressText = ref('加载中…')
const allRiderOrders = ref([])

const currentMerchantLogo = computed(() => {
  const logo = currentMerchant.value?.logo
  return logo ? getImageUrl(logo) : ''
})

const merchantLocation = ref(null)
const studentLocation = ref(null)
const riderLocation = ref(null)

const detailVisible = ref(false)
const detailOrder = ref(null)

const currentOrderItems = computed(() => currentOrder.value?.items || [])

const todayStr = () => new Date().toISOString().slice(0, 10)

const recentCompletedOrders = computed(() => {
  const cutoff = Date.now() - 7 * 24 * 60 * 60 * 1000
  return allRiderOrders.value
    .filter((o) => o.status === 'completed' && o.deliveredAt)
    .filter((o) => new Date(o.deliveredAt).getTime() >= cutoff)
    .sort((a, b) => new Date(b.deliveredAt) - new Date(a.deliveredAt))
})

const todayCompletedCount = computed(() => {
  const t = todayStr()
  return allRiderOrders.value.filter(
    (o) => o.status === 'completed' && o.deliveredAt?.slice(0, 10) === t
  ).length
})

const todayFeeSum = computed(() => {
  const t = todayStr()
  const sum = allRiderOrders.value
    .filter((o) => o.status === 'completed' && o.deliveredAt?.slice(0, 10) === t)
    .reduce((acc, o) => acc + Number(o.deliveryFee || 0), 0)
  return sum.toFixed(2)
})

const statusText = computed(() => {
  const textMap = {
    ready: '请前往商家取餐',
    delivering: '正在配送中'
  }
  return textMap[currentOrder.value?.status] || ''
})

const formatDateTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatMoney = (v) => {
  if (v == null || v === '') return '0.00'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

const itemSummary = (order) => {
  const items = order.items || []
  if (!items.length) return '商品信息暂无'
  const n = items.reduce((s, it) => s + (it.quantity || 0), 0)
  const names = items.slice(0, 2).map((it) => it.dishName).join('、')
  return `${names}${items.length > 2 ? '…' : ''} 共${n}件`
}

const dishInitial = (name) => (name && String(name).charAt(0)) || '餐'

const merchantInitial = (name) => (name && String(name).charAt(0)) || '店'

const orderCoverUrl = (order) => {
  const items = order.items || []
  if (!items.length) return ''
  const url = getImageUrl(items[0].dishImage)
  return url || ''
}

const extraDishThumb = (order) => {
  const items = order.items || []
  if (items.length < 2) return ''
  return getImageUrl(items[1].dishImage) || ''
}

const placeholderGradient = (name) => {
  let h = 0
  const s = name || '店'
  for (let i = 0; i < s.length; i++) h = (h + s.charCodeAt(i) * 17) % 360
  return `linear-gradient(135deg, hsl(${h}, 58%, 90%) 0%, hsl(${(h + 35) % 360}, 48%, 82%) 100%)`
}

const openOrderDetail = (order) => {
  const oid = order.id
  detailOrder.value = { ...order }
  detailVisible.value = true
  request
    .get(`/order/${oid}`)
    .then((full) => {
      if (!detailVisible.value || detailOrder.value?.id !== oid) return
      detailOrder.value = full || detailOrder.value
    })
    .catch(() => {})
}

const onDetailDrawerClosed = () => {
  detailOrder.value = null
}

const loadData = async () => {
  const riderId = userStore.userId
  if (!riderId) return

  await orderStore.loadRiderOrders(riderId)

  const orders = orderStore.orderList
  allRiderOrders.value = orders

  currentOrder.value = orders.find(
    (o) => o.status === 'delivering' && o.riderId === riderId
  )

  merchantAddressText.value = '—'
  merchantLocation.value = null
  currentMerchant.value = null

  if (currentOrder.value) {
    try {
      const merchant = await request.get(`/merchant/${currentOrder.value.merchantId}`)
      currentMerchant.value = merchant
      if (merchant?.address) {
        merchantAddressText.value = merchant.address
      }
      if (merchant?.lat && merchant?.lng) {
        merchantLocation.value = { lat: merchant.lat, lng: merchant.lng }
      }
    } catch (e) {
      merchantAddressText.value = '商家地址'
    }
    studentLocation.value = { lat: 30.5728, lng: 104.0668 }
    riderLocation.value = { lat: 30.5730, lng: 104.0670 }
  }
}

const goToPool = () => {
  router.push('/rider/pool')
}

const deliverOrder = async () => {
  const result = await orderStore.updateOrderStatus(currentOrder.value.id, 'completed')
  if (result.success) {
    ElMessage.success('配送完成')
    loadData()
  } else {
    ElMessage.error(result.message || '操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.my-delivery-page {
  padding: $spacing-base;
  min-height: 100%;
  background: linear-gradient(180deg, #eef6ff 0%, #f5f7fa 28%, #f5f7fa 100%);
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
  margin-bottom: $spacing-md;

  .stat-card {
    border-radius: $border-radius-md;
    padding: $spacing-md $spacing-sm;
    text-align: center;
    box-shadow: $shadow-sm;
    border: 1px solid rgba(255, 255, 255, 0.8);

    &--orders {
      background: linear-gradient(145deg, #ffffff 0%, #e8f4ff 100%);
    }

    &--fee {
      background: linear-gradient(145deg, #ffffff 0%, #e8f8ef 100%);
    }

    &--week {
      background: linear-gradient(145deg, #ffffff 0%, #f3e8ff 100%);
    }

    .stat-value {
      display: block;
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: $color-text-primary;

      &.accent {
        color: $color-success;
      }
    }

    .stat-label {
      display: block;
      margin-top: $spacing-xs;
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }
  }
}

.order-meta-card {
  background: $color-bg-white;
  border-radius: $border-radius-md;
  padding: $spacing-base;
  margin-bottom: $spacing-md;
  font-size: $font-size-sm;

  .meta-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: $spacing-md;
    padding: $spacing-xs 0;

    &:not(:last-child) {
      border-bottom: 1px solid $color-border-extra-light;
    }
  }

  .meta-label {
    color: $color-text-secondary;
    flex-shrink: 0;
  }

  .meta-value {
    text-align: right;
    word-break: break-all;

    &.mono {
      font-family: ui-monospace, monospace;
      font-size: $font-size-xs;
    }

    &.price {
      font-weight: $font-weight-semibold;
      color: $color-warning;
    }

    &.remark {
      color: $color-text-regular;
    }
  }
}

.items-preview {
  background: $color-bg-white;
  border-radius: $border-radius-md;
  padding: $spacing-base;
  margin-bottom: $spacing-md;

  .preview-title {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-bottom: $spacing-sm;
  }

  .preview-list {
    list-style: none;
    margin: 0;
    padding: 0;

    li {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      padding: $spacing-sm 0;
      font-size: $font-size-sm;
      border-bottom: 1px dashed $color-border-lighter;

      &:last-child {
        border-bottom: none;
      }

      .preview-thumb {
        flex-shrink: 0;
        width: 44px;
        height: 44px;
        border-radius: $border-radius-base;
        overflow: hidden;
        background: $color-bg-light;
        border: 1px solid $color-border-extra-light;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }

        .thumb-fallback {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;
          font-size: $font-size-md;
          font-weight: $font-weight-semibold;
          color: $color-text-secondary;
          background: linear-gradient(135deg, #e8f4fd 0%, #dceefc 100%);
        }
      }

      .preview-line {
        flex: 1;
        min-width: 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: $spacing-sm;
      }

      .name {
        flex: 1;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .qty {
        color: $color-text-secondary;
        flex-shrink: 0;
      }
    }
  }

  .preview-more {
    margin-top: $spacing-sm;
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }
}

.detail-link-row {
  text-align: center;
  margin-bottom: $spacing-md;
}

.detail-text-btn {
  color: #606266 !important;

  &:hover {
    color: #303133 !important;
  }
}

:deep(.nav-action-btn) {
  color: #303133 !important;
  border-color: #dcdfe6 !important;
  background: #ffffff !important;

  &:hover {
    border-color: #c0c4cc !important;
    background: #f5f7fa !important;
  }
}

// 当前配送
.current-delivery {
  .delivery-status {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-base;
    background: $color-bg-white;
    border-radius: $border-radius-md;
    margin-bottom: $spacing-md;

    .status-text {
      color: $color-text-secondary;
    }
  }

  .delivery-info {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    margin-top: $spacing-md;

    .info-section {
      padding: $spacing-base;
      border-bottom: 1px solid $color-border-lighter;

      &:last-child {
        border-bottom: none;
      }

      .section-title {
        font-size: $font-size-sm;
        color: $color-text-secondary;
        margin-bottom: $spacing-sm;
      }

      .info-content {
        display: flex;
        align-items: center;
        gap: $spacing-md;

        .info-icon {
          font-size: 24px;
        }

        .merchant-avatar {
          flex-shrink: 0;
          width: 48px;
          height: 48px;
          border-radius: $border-radius-md;
          overflow: hidden;
          border: 1px solid $color-border-lighter;
          background: $color-bg-white;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
          }
        }

        .info-detail {
          flex: 1;

          .info-main {
            font-weight: $font-weight-medium;
            margin-bottom: $spacing-xs;
          }

          .info-sub {
            font-size: $font-size-sm;
            color: $color-text-secondary;
          }
        }
      }
    }
  }

  .delivery-actions {
    margin-top: $spacing-lg;

    .el-button {
      width: 100%;
    }
  }
}

// 无配送任务
.no-delivery {
  text-align: center;
  padding: 48px $spacing-lg 56px;
  background: linear-gradient(180deg, #ffffff 0%, #f0f7ff 100%);
  border-radius: $border-radius-lg;
  border: 1px solid rgba(74, 144, 217, 0.12);
  box-shadow: 0 8px 24px rgba(74, 144, 217, 0.08);

  .empty-visual {
    margin-bottom: $spacing-lg;
  }

  .empty-photo-ring {
    display: inline-flex;
    padding: 10px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(126, 200, 255, 0.35) 0%, rgba(74, 144, 217, 0.15) 100%);
  }

  .empty-inner {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-sm;
  }

  .empty-svg {
    width: 100px;
    height: 100px;
    display: block;
  }

  .empty-text {
    color: $color-text-secondary;
    margin-bottom: $spacing-xs;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    margin-bottom: $spacing-lg;
  }
}

.history-section {
  margin-top: $spacing-lg;

  .section-head {
    margin-bottom: $spacing-md;

    .section-title {
      font-size: $font-size-md;
      font-weight: $font-weight-semibold;
      margin-bottom: $spacing-xs;
    }

    .section-sub {
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }
  }

  .history-list {
    background: transparent;
    border-radius: $border-radius-md;
    overflow: visible;

    .history-item {
      display: flex;
      justify-content: space-between;
      align-items: stretch;
      gap: $spacing-md;
      padding: $spacing-base;
      margin-bottom: $spacing-sm;
      background: $color-bg-white;
      border-radius: $border-radius-md;
      border: 1px solid $color-border-extra-light;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      cursor: pointer;
      transition: box-shadow 0.2s ease, transform 0.2s ease;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover,
      &:focus-visible {
        box-shadow: 0 6px 16px rgba(74, 144, 217, 0.12);
        transform: translateY(-1px);
        outline: none;
      }

      .item-thumb-wrap {
        position: relative;
        flex-shrink: 0;
        width: 76px;
      }

      .item-thumb {
        width: 76px;
        height: 76px;
        border-radius: $border-radius-md;
        overflow: hidden;
        background: $color-bg-light;
        border: 1px solid $color-border-lighter;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }

        &--placeholder {
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 28px;
          font-weight: $font-weight-bold;
          color: rgba(255, 255, 255, 0.95);
          text-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
        }
      }

      .item-thumb-mini {
        position: absolute;
        right: -4px;
        bottom: -4px;
        width: 32px;
        height: 32px;
        border-radius: 8px;
        overflow: hidden;
        border: 2px solid #fff;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
        background: $color-bg-white;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }
      }

      .item-main-block {
        flex: 1;
        min-width: 0;
      }

      .item-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: $spacing-sm;
        margin-bottom: $spacing-xs;
      }

      .merchant-name {
        font-weight: $font-weight-medium;
        font-size: $font-size-base;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .order-no {
        display: block;
        font-size: $font-size-xs;
        font-family: ui-monospace, monospace;
        color: $color-text-secondary;
        margin-bottom: $spacing-xs;
      }

      .item-address,
      .item-goods {
        font-size: $font-size-sm;
        color: $color-text-regular;
        margin-bottom: $spacing-xs;
        line-height: $line-height-tight;
      }

      .item-goods {
        color: $color-text-secondary;
      }

      .item-bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: $font-size-xs;
        color: $color-text-secondary;
        margin-top: $spacing-xs;

        .amount {
          color: $color-text-primary;
          font-weight: $font-weight-medium;
        }
      }

      .item-side {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: center;
        flex-shrink: 0;

        .delivery-fee {
          color: $color-success;
          font-weight: $font-weight-semibold;
          font-size: $font-size-sm;
        }

        .chev {
          margin-top: $spacing-sm;
          color: $color-text-placeholder;
        }
      }
    }
  }
}

.order-detail-drawer-body {
  min-height: 30vh;
}
</style>

<style lang="scss">
/* 抽屉挂到 body，非 scoped 才能作用到动画面板 */
.order-detail-drawer.el-drawer {
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.12);
}

.order-detail-drawer .el-drawer__header {
  margin-bottom: 0;
  padding-bottom: 12px;
}

.order-detail-drawer .el-drawer__body {
  padding: 0;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
}
</style>
