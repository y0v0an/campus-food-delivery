<template>
  <div class="order-card" @click="handleClick" :class="{ 'group-order': isGroupOrder }">
    <!-- 拼单标识 -->
    <div class="group-badge" v-if="isGroupOrder">
      <iconify-icon icon="lucide:users" class="icon"></iconify-icon>
      <span>拼单订单</span>
    </div>

    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="merchant-info">
        <span class="merchant-name">{{ order.merchantName }}</span>
        <span class="order-no">订单号: {{ order.orderNo }}</span>
        <el-tag v-if="order.orderType === 'dine_in'" type="warning" size="small" class="order-type-tag">
          店内就餐
        </el-tag>
      </div>
      <StatusTag :status="order.status" />
    </div>

    <!-- 商品列表 -->
    <div class="card-body">
      <div class="items-list">
        <div class="item" v-for="(item, idx) in displayItems" :key="item.id ?? `${item.dishId}-${idx}`">
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
              <span class="item-price">¥{{ item.price }}</span>
            </div>
          </div>
        </div>
        <div class="more-items" v-if="order.items.length > 3">
          还有 {{ order.items.length - 3 }} 件商品...
        </div>
      </div>

      <!-- 配送信息（骑手/学生视图） -->
      <div class="delivery-info" v-if="showDeliveryInfo">
        <div class="info-row" v-if="order.address">
          <el-icon><Location /></el-icon>
          <span>{{ order.address.building }} {{ order.address.room }}</span>
        </div>
        <div class="info-row" v-if="order.riderName && role !== 'rider'">
          <el-icon><User /></el-icon>
          <span>骑手: {{ order.riderName }}</span>
        </div>
      </div>
    </div>

    <!-- 卡片底部 -->
    <div class="card-footer">
      <div class="order-info">
        <span class="order-time">{{ formatTime(order.createdAt) }}</span>
        <span class="order-amount">实付 ¥{{ order.actualAmount }}</span>
      </div>

      <div class="actions" v-if="showActions" @click.stop>
        <!-- 商家操作 -->
        <template v-if="role === 'merchant'">
          <el-button
            v-if="order.status === 'pending_accept'"
            type="primary"
            size="small"
            @click="$emit('accept')"
          >
            接单
          </el-button>
          <el-button
            v-if="order.status === 'preparing'"
            type="success"
            size="small"
            @click="$emit('ready')"
          >
            已出餐
          </el-button>
        </template>

        <!-- 骑手操作 -->
        <template v-if="role === 'rider'">
          <el-button
            v-if="order.status === 'ready' && !order.riderId"
            type="primary"
            size="small"
            @click="$emit('accept')"
          >
            接单
          </el-button>
          <el-button
            v-if="order.status === 'ready' && order.riderId"
            type="primary"
            size="small"
            @click="$emit('pickup')"
          >
            已取餐
          </el-button>
          <el-button
            v-if="order.status === 'delivering'"
            type="success"
            size="small"
            @click="$emit('deliver')"
          >
            已送达
          </el-button>
        </template>

        <!-- 学生操作 -->
        <template v-if="role === 'student'">
          <el-button
            v-if="order.status === 'pending_payment'"
            type="primary"
            size="small"
            @click="$emit('pay')"
          >
            去支付
          </el-button>
          <el-button
            v-if="order.orderType === 'dine_in' && order.status === 'ready'"
            type="primary"
            size="small"
            @click="$emit('confirmPickup')"
          >
            确认取餐
          </el-button>
          <el-button
            v-if="order.status === 'completed' && !order.review"
            type="success"
            size="small"
            @click="$emit('review')"
          >
            评价
          </el-button>
          <el-button
            v-if="order.status === 'completed' && order.review"
            type="info"
            plain
            size="small"
            @click="$emit('viewReview')"
          >
            已评价
          </el-button>
        </template>

        <!-- 查看详情按钮 -->
        <el-button size="small" text @click="$emit('detail')">
          详情
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Location, User, ArrowRight } from '@element-plus/icons-vue'
import StatusTag from '@/components/common/StatusTag.vue'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  role: {
    type: String,
    default: 'student',
    validator: (value) => ['merchant', 'rider', 'student', 'admin'].includes(value)
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

defineEmits(['accept', 'ready', 'pickup', 'deliver', 'pay', 'review', 'viewReview', 'detail', 'click', 'confirmPickup'])

// 判断是否为拼单订单
const isGroupOrder = computed(() => {
  // 支持两种判断方式：1. 通过 groupOrderId 字段 2. 通过 remark 字段
  return props.order.groupOrderId || props.order.remark?.includes('拼单订单#')
})

// 只显示前3个商品
const displayItems = computed(() => props.order.items.slice(0, 3))

// 是否显示配送信息
const showDeliveryInfo = computed(() => {
  return ['rider', 'student'].includes(props.role) && 
         ['delivering', 'completed'].includes(props.order.status)
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  // 今天的订单显示时间
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 昨天的订单
  const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000)
  if (date.getDate() === yesterday.getDate()) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 其他日期
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const handleClick = () => {
  // 点击卡片触发详情
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

.order-card {
  background: $color-bg-white;
  border-radius: $border-radius-md;
  box-shadow: $shadow-sm;
  padding: $spacing-base;
  cursor: pointer;
  transition: all $transition-fast;
  position: relative;
  overflow: hidden;

  &:hover {
    box-shadow: $shadow-base;
    transform: translateY(-2px);
  }

  // 拼单订单样式
  &.group-order {
    border: 2px solid #FFB84D;
    background: linear-gradient(to right, #fffbf5, #fff 30%);
  }

  .group-badge {
    position: absolute;
    top: 0;
    right: 0;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background: linear-gradient(135deg, #FFB84D 0%, #FF7A45 100%);
    color: white;
    font-size: 12px;
    font-weight: 600;
    border-radius: 0 $border-radius-md 0 12px;

    .icon {
      font-size: 14px;
    }
  }
}

.card-header {
  padding-right: 80px; // 为拼单标识留空间
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $color-border-lighter;

  .merchant-info {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;

    .merchant-name {
      font-size: $font-size-md;
      font-weight: $font-weight-medium;
      color: $color-text-primary;
    }

    .order-no {
      font-size: $font-size-xs;
      color: $color-text-regular;
    }

    .order-type-tag {
      margin-top: $spacing-xs;
    }
  }
}

.card-body {
  .items-list {
    .item {
      display: flex;
      align-items: flex-start;
      gap: $spacing-sm;
      padding: $spacing-xs 0;
      font-size: $font-size-sm;
    }

    .item-thumb {
      flex-shrink: 0;
      width: 44px;
      height: 44px;
      border-radius: $border-radius-sm;
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
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $color-text-secondary;
      background: $color-border-extra-light;
    }

    .item-main {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .item-name {
      color: $color-text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: $spacing-sm;
    }

    .item-quantity {
      color: $color-text-regular;
    }

    .item-price {
      color: $color-text-regular;
    }

    .more-items {
      font-size: $font-size-xs;
      color: $color-text-regular;
      padding: $spacing-xs 0;
    }
  }

  .delivery-info {
    margin-top: $spacing-md;
    padding-top: $spacing-md;
    border-top: 1px dashed $color-border-lighter;

    .info-row {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-size: $font-size-sm;
      color: $color-text-regular;
      margin-bottom: $spacing-xs;

      .el-icon {
        color: $color-primary;
      }
    }
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1px solid $color-border-lighter;

  .order-info {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;

    .order-time {
      font-size: $font-size-xs;
      color: $color-text-regular;
    }

    .order-amount {
      font-size: $font-size-md;
      font-weight: $font-weight-semibold;
      color: $color-warning;
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }
}
</style>
