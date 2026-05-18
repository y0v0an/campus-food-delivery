<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    :class="['status-tag', `status-${status}`]"
  >
    <span class="status-dot" v-if="showDot"></span>
    {{ statusText }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (value) => [
      'pending_payment',
      'pending_accept',
      'preparing',
      'ready',
      'delivering',
      'completed',
      'cancelled'
    ].includes(value)
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  effect: {
    type: String,
    default: 'light',
    validator: (value) => ['dark', 'light', 'plain'].includes(value)
  },
  showDot: {
    type: Boolean,
    default: false
  }
})

// 状态文本映射
const statusTextMap = {
  pending_payment: '待支付',
  pending_accept: '待接单',
  preparing: '制作中',
  ready: '待取餐',
  delivering: '配送中',
  completed: '已完成',
  cancelled: '已取消'
}

// 状态类型映射（Element Plus tag type）
const statusTypeMap = {
  pending_payment: 'warning',
  pending_accept: 'info',
  preparing: 'info',
  ready: 'info',
  delivering: 'primary',
  completed: 'success',
  cancelled: 'info'
}

const statusText = computed(() => statusTextMap[props.status] || '未知状态')
const tagType = computed(() => statusTypeMap[props.status] || '')
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-sm;

  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: currentColor;
    animation: pulse 2s infinite;
  }
}

// 自定义状态颜色
.status-pending_payment {
  --el-tag-bg-color: rgba(255, 122, 69, 0.1);
  --el-tag-border-color: rgba(255, 122, 69, 0.2);
  --el-tag-text-color: #FF7A45;
}

.status-pending_accept {
  --el-tag-bg-color: rgba(24, 144, 255, 0.1);
  --el-tag-border-color: rgba(24, 144, 255, 0.2);
  --el-tag-text-color: #1890FF;
}

.status-preparing {
  --el-tag-bg-color: rgba(114, 46, 209, 0.1);
  --el-tag-border-color: rgba(114, 46, 209, 0.2);
  --el-tag-text-color: #722ED1;
}

.status-ready {
  --el-tag-bg-color: rgba(19, 194, 194, 0.1);
  --el-tag-border-color: rgba(19, 194, 194, 0.2);
  --el-tag-text-color: #13C2C2;
}

.status-delivering {
  --el-tag-bg-color: rgba(74, 144, 217, 0.1);
  --el-tag-border-color: rgba(74, 144, 217, 0.2);
  --el-tag-text-color: #4A90D9;
}

.status-completed {
  --el-tag-bg-color: rgba(82, 196, 26, 0.1);
  --el-tag-border-color: rgba(82, 196, 26, 0.2);
  --el-tag-text-color: #52C41A;
}

.status-cancelled {
  --el-tag-bg-color: rgba(144, 147, 153, 0.1);
  --el-tag-border-color: rgba(144, 147, 153, 0.2);
  --el-tag-text-color: #909399;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
