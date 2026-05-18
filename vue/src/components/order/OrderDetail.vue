<template>
  <div class="order-detail">
    <!-- 订单状态（在订单跟踪页由外层展示时可隐藏，避免重复） -->
    <div v-if="!hideStatus" class="status-section">
      <StatusTag :status="order.status" size="large" :showDot="isActiveStatus" />
      <p class="status-desc">{{ statusDescription }}</p>
    </div>

    <!-- 配送 / 取餐信息 -->
    <div class="section delivery-section" v-if="order.addressBuilding || order.address">
      <h3 class="section-title">
        <el-icon><Location /></el-icon>
        {{ order.orderType === 'dine_in' ? '取餐信息' : '配送信息' }}
      </h3>
      <div class="delivery-content">
        <div class="address-info">
          <p class="address">
            {{ order.address?.building ?? order.addressBuilding }}
            {{ order.address?.room ?? order.addressRoom }}
          </p>
          <p class="contact">
            {{ order.address?.contact ?? order.addressContact }}
            {{ order.address?.phone ?? order.addressPhone }}
          </p>
        </div>
        <div class="rider-info" v-if="order.riderName && order.orderType !== 'dine_in'">
          <el-avatar :size="40">{{ order.riderName?.charAt(0) }}</el-avatar>
          <div class="rider-detail">
            <span class="rider-name">{{ order.riderName }}</span>
            <span class="rider-label">配送骑手</span>
          </div>
          <el-button type="primary" :icon="Phone" circle size="small" />
        </div>
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="section items-section">
      <h3 class="section-title">
        <el-icon><Goods /></el-icon>
        商品信息
      </h3>
      <div class="merchant-name">{{ order.merchantName }}</div>
      <div class="items-list">
        <div
          class="item"
          v-for="(item, idx) in order.items"
          :key="item.id ?? `${item.dishId}-${idx}`"
        >
          <div class="item-thumb">
            <img
              v-if="item.dishImage"
              :src="getImageUrl(item.dishImage)"
              :alt="item.dishName"
            />
            <div v-else class="item-thumb-placeholder">{{ item.dishName?.charAt(0) || '餐' }}</div>
          </div>
          <div class="item-main">
            <span class="item-name">{{ item.dishName }}</span>
            <div class="item-meta">
              <span class="item-quantity">x{{ item.quantity }}</span>
              <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="price-detail">
        <div class="price-row">
          <span>商品小计</span>
          <span>¥{{ order.totalAmount?.toFixed(2) }}</span>
        </div>
        <div class="price-row">
          <span>配送费</span>
          <span>¥{{ order.deliveryFee?.toFixed(2) }}</span>
        </div>
        <div class="price-row" v-if="order.packingFee">
          <span>包装费</span>
          <span>¥{{ order.packingFee?.toFixed(2) }}</span>
        </div>
        <div class="price-row total">
          <span>实付金额</span>
          <span class="total-amount">¥{{ order.actualAmount?.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 订单信息 -->
    <div class="section order-info-section">
      <h3 class="section-title">
        <el-icon><Document /></el-icon>
        订单信息
      </h3>
      <div class="info-list">
        <div class="info-row">
          <span class="label">订单编号</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">下单时间</span>
          <span class="value">{{ formatDateTime(order.createdAt) }}</span>
        </div>
        <div class="info-row" v-if="order.paidAt">
          <span class="label">支付时间</span>
          <span class="value">{{ formatDateTime(order.paidAt) }}</span>
        </div>
        <div class="info-row" v-if="order.deliveredAt">
          <span class="label">送达时间</span>
          <span class="value">{{ formatDateTime(order.deliveredAt) }}</span>
        </div>
        <div class="info-row" v-if="order.remark">
          <span class="label">备注</span>
          <span class="value">{{ order.remark }}</span>
        </div>
      </div>
    </div>

    <!-- 评价信息 -->
    <div class="section review-section" v-if="order.review">
      <h3 class="section-title">
        <el-icon><Star /></el-icon>
        我的评价
      </h3>
      <div class="review-content">
        <div class="rating-row">
          <span class="rating-label">商家评分</span>
          <el-rate v-model="order.review.merchantRating" disabled />
        </div>
        <div class="rating-row">
          <span class="rating-label">骑手评分</span>
          <el-rate v-model="order.review.riderRating" disabled />
        </div>
        <p class="review-text" v-if="order.review.content">
          {{ order.review.content }}
        </p>
        <p class="review-time">
          {{ formatDateTime(order.review.createdAt) }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Location, Phone, Goods, Document, Star } from '@element-plus/icons-vue'
import StatusTag from '@/components/common/StatusTag.vue'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  /** 为 true 时不渲染顶部状态区（用于 OrderTrack 等已单独展示状态的页面） */
  hideStatus: {
    type: Boolean,
    default: false
  }
})

// 是否是活跃状态（显示动画点）
const isActiveStatus = computed(() => {
  return ['pending_accept', 'preparing', 'ready', 'delivering'].includes(props.order.status)
})

// 状态描述
const statusDescription = computed(() => {
  const isDine = props.order.orderType === 'dine_in'
  const descMap = {
    pending_payment: '请尽快完成支付',
    pending_accept: '商家正在确认订单',
    preparing: '商家正在准备您的餐品',
    ready: isDine ? '餐品已备好，请到店取餐' : '餐品已备好，等待骑手取餐',
    delivering: '骑手正在配送中',
    completed: '订单已完成，感谢您的支持',
    cancelled: '订单已取消'
  }
  return descMap[props.order.status] || ''
})

// 格式化日期时间
const formatDateTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/img/')) return `http://localhost:8080${url}`
  return url
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.order-detail {
  padding: $spacing-base;
}

.status-section {
  text-align: center;
  padding: $spacing-xl 0;
  background: linear-gradient(135deg, rgba($color-primary, 0.05) 0%, rgba($color-primary, 0.02) 100%);
  border-radius: $border-radius-md;
  margin-bottom: $spacing-lg;

  .status-desc {
    margin-top: $spacing-md;
    color: $color-text-secondary;
    font-size: $font-size-sm;
  }
}

.section {
  background: $color-bg-white;
  border-radius: $border-radius-md;
  padding: $spacing-base;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-sm;

  .section-title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-md;
    border-bottom: 1px solid $color-border-lighter;

    .el-icon {
      color: $color-primary;
    }
  }
}

.delivery-section {
  .delivery-content {
    .address-info {
      margin-bottom: $spacing-md;

      .address {
        font-size: $font-size-md;
        font-weight: $font-weight-medium;
        color: $color-text-primary;
        margin-bottom: $spacing-xs;
      }

      .contact {
        font-size: $font-size-sm;
        color: $color-text-secondary;
      }
    }

    .rider-info {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      padding: $spacing-md;
      background: $color-bg-light;
      border-radius: $border-radius-base;

      .rider-detail {
        flex: 1;
        display: flex;
        flex-direction: column;

        .rider-name {
          font-weight: $font-weight-medium;
          color: $color-text-primary;
        }

        .rider-label {
          font-size: $font-size-xs;
          color: $color-text-secondary;
        }
      }
    }
  }
}

.items-section {
  .merchant-name {
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $spacing-md;
  }

  .items-list {
    .item {
      display: flex;
      align-items: flex-start;
      gap: $spacing-md;
      padding: $spacing-md 0;
      border-bottom: 1px dashed $color-border-lighter;

      &:last-child {
        border-bottom: none;
      }
    }

    .item-thumb {
      flex-shrink: 0;
      width: 64px;
      height: 64px;
      border-radius: $border-radius-base;
      overflow: hidden;
      background: $color-bg-light;
      border: 1px solid $color-border-lighter;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
    }

    .item-thumb-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: $font-size-lg;
      font-weight: $font-weight-medium;
      color: $color-text-secondary;
      background: $color-border-extra-light;
    }

    .item-main {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: $spacing-xs;
    }

    .item-name {
      color: $color-text-primary;
      font-size: $font-size-base;
      line-height: $line-height-tight;
    }

    .item-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: $spacing-md;
    }

    .item-quantity {
      color: $color-text-secondary;
      font-size: $font-size-sm;
    }

    .item-price {
      color: $color-text-regular;
      font-weight: $font-weight-medium;
      font-size: $font-size-sm;
    }
  }

  .price-detail {
    margin-top: $spacing-md;
    padding-top: $spacing-md;
    border-top: 1px solid $color-border-lighter;

    .price-row {
      display: flex;
      justify-content: space-between;
      padding: $spacing-xs 0;
      font-size: $font-size-sm;
      color: $color-text-secondary;

      &.total {
        padding-top: $spacing-md;
        margin-top: $spacing-sm;
        border-top: 1px solid $color-border-lighter;
        font-size: $font-size-md;
        color: $color-text-primary;

        .total-amount {
          font-size: $font-size-lg;
          font-weight: $font-weight-bold;
          color: $color-warning;
        }
      }
    }
  }
}

.order-info-section {
  .info-list {
    .info-row {
      display: flex;
      justify-content: space-between;
      padding: $spacing-sm 0;
      font-size: $font-size-sm;

      .label {
        color: $color-text-secondary;
      }

      .value {
        color: $color-text-primary;
      }
    }
  }
}

.review-section {
  .review-content {
    .rating-row {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      margin-bottom: $spacing-sm;

      .rating-label {
        font-size: $font-size-sm;
        color: $color-text-secondary;
        width: 60px;
      }
    }

    .review-text {
      margin-top: $spacing-md;
      padding: $spacing-md;
      background: $color-bg-light;
      border-radius: $border-radius-base;
      font-size: $font-size-sm;
      color: $color-text-primary;
      line-height: $line-height-loose;
    }

    .review-time {
      margin-top: $spacing-sm;
      font-size: $font-size-xs;
      color: $color-text-secondary;
      text-align: right;
    }
  }
}
</style>
