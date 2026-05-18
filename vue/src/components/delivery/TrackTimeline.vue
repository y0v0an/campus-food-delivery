<template>
  <div class="track-timeline">
    <el-timeline>
      <el-timeline-item
        v-for="(item, index) in timelineItems"
        :key="index"
        :type="item.type"
        :hollow="item.hollow"
        :timestamp="item.timestamp"
        placement="top"
      >
        <div class="timeline-content">
          <div class="timeline-title">
            <span class="status-icon">{{ item.icon }}</span>
            <span class="status-text">{{ item.title }}</span>
          </div>
          <div class="timeline-desc" v-if="item.description">
            {{ item.description }}
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 订单数据
  order: {
    type: Object,
    default: null
  },
  // 配送轨迹数据
  track: {
    type: Object,
    default: null
  }
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 生成时间线数据
const timelineItems = computed(() => {
  const items = []
  const order = props.order
  
  if (!order) return items

  // 下单
  items.push({
    title: '订单已提交',
    description: '等待支付',
    timestamp: formatTime(order.createdAt),
    icon: '📝',
    type: order.paidAt ? 'success' : 'primary',
    hollow: !order.paidAt
  })

  // 支付
  if (order.paidAt) {
    items.push({
      title: '支付成功',
      description: '等待商家接单',
      timestamp: formatTime(order.paidAt),
      icon: '💳',
      type: order.acceptedAt ? 'success' : 'primary',
      hollow: !order.acceptedAt
    })
  }

  // 商家接单
  if (order.acceptedAt) {
    items.push({
      title: '商家已接单',
      description: '正在准备您的餐品',
      timestamp: formatTime(order.acceptedAt),
      icon: '👨‍🍳',
      type: order.readyAt ? 'success' : 'primary',
      hollow: !order.readyAt
    })
  }

  // 出餐
  if (order.readyAt) {
    const dineIn = order.orderType === 'dine_in'
    items.push({
      title: '餐品已备好',
      description: dineIn
        ? '请到店取餐，取餐后请点击确认取餐'
        : order.riderName
          ? `骑手 ${order.riderName} 正在取餐`
          : '等待骑手取餐',
      timestamp: formatTime(order.readyAt),
      icon: '🍱',
      type: order.pickedAt || (dineIn && order.deliveredAt) ? 'success' : 'primary',
      hollow: dineIn ? !order.deliveredAt : !order.pickedAt
    })
  }

  // 骑手取餐（外卖）
  if (order.pickedAt && order.orderType !== 'dine_in') {
    items.push({
      title: '骑手已取餐',
      description: '正在配送中',
      timestamp: formatTime(order.pickedAt),
      icon: '🛵',
      type: order.deliveredAt ? 'success' : 'primary',
      hollow: !order.deliveredAt
    })
  }

  // 送达 / 取餐完成
  if (order.deliveredAt) {
    items.push({
      title: order.orderType === 'dine_in' ? '已确认取餐' : '订单已送达',
      description: '感谢您的支持，期待再次光临',
      timestamp: formatTime(order.deliveredAt),
      icon: '✅',
      type: 'success',
      hollow: false
    })
  }

  // 如果订单被取消
  if (order.status === 'cancelled') {
    items.push({
      title: '订单已取消',
      description: '',
      timestamp: '',
      icon: '❌',
      type: 'info',
      hollow: false
    })
  }

  return items.reverse() // 最新的在上面
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.track-timeline {
  padding: $spacing-base;
  background: $color-bg-white;
  border-radius: $border-radius-md;

  :deep(.el-timeline) {
    padding-left: $spacing-sm;
  }

  :deep(.el-timeline-item__wrapper) {
    padding-left: $spacing-lg;
  }

  :deep(.el-timeline-item__timestamp) {
    color: $color-text-secondary;
    font-size: $font-size-xs;
  }

  :deep(.el-timeline-item__node--success) {
    background-color: $color-success;
  }

  :deep(.el-timeline-item__node--primary) {
    background-color: $color-primary;
  }
}

.timeline-content {
  .timeline-title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-xs;

    .status-icon {
      font-size: $font-size-md;
    }

    .status-text {
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $color-text-primary;
    }
  }

  .timeline-desc {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    padding-left: 24px;
  }
}
</style>
