<template>
  <div class="stat-card" :class="[`stat-card--${type}`]">
    <div class="stat-icon">
      <el-icon :size="24">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="stat-content">
      <div class="stat-value">
        <span class="value-prefix" v-if="prefix">{{ prefix }}</span>
        <span class="value-number">{{ formattedValue }}</span>
        <span class="value-suffix" v-if="suffix">{{ suffix }}</span>
      </div>
      <div class="stat-label">{{ label }}</div>
      <div class="stat-trend" v-if="trend !== null">
        <el-icon :class="trendClass">
          <component :is="trendIcon" />
        </el-icon>
        <span :class="trendClass">{{ Math.abs(trend) }}%</span>
        <span class="trend-text">较昨日</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  ShoppingCart, 
  Money, 
  User, 
  Bicycle,
  TrendCharts,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'

const props = defineProps({
  // 数值
  value: {
    type: [Number, String],
    default: 0
  },
  // 标签
  label: {
    type: String,
    default: ''
  },
  // 前缀（如 ¥）
  prefix: {
    type: String,
    default: ''
  },
  // 后缀（如 单）
  suffix: {
    type: String,
    default: ''
  },
  // 图标
  icon: {
    type: [String, Object],
    default: () => TrendCharts
  },
  // 类型（影响颜色）
  type: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'success', 'warning', 'danger'].includes(value)
  },
  // 趋势（正数上涨，负数下跌，null不显示）
  trend: {
    type: Number,
    default: null
  },
  // 是否格式化数字
  format: {
    type: Boolean,
    default: true
  }
})

// 格式化数值
const formattedValue = computed(() => {
  if (!props.format) return props.value
  
  const num = Number(props.value)
  if (isNaN(num)) return props.value
  
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return num.toLocaleString()
  }
  return num
})

// 趋势图标
const trendIcon = computed(() => {
  return props.trend >= 0 ? ArrowUp : ArrowDown
})

// 趋势样式类
const trendClass = computed(() => {
  return props.trend >= 0 ? 'trend-up' : 'trend-down'
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.stat-card {
  display: flex;
  align-items: flex-start;
  gap: $spacing-base;
  padding: $spacing-lg;
  background: $color-bg-white;
  border-radius: $border-radius-md;
  box-shadow: $shadow-sm;
  transition: all $transition-fast;

  &:hover {
    box-shadow: $shadow-base;
    transform: translateY(-2px);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.stat-card--primary .stat-icon {
  background: rgba($color-primary, 0.1);
  color: $color-primary;
}

.stat-card--success .stat-icon {
  background: rgba($color-success, 0.1);
  color: $color-success;
}

.stat-card--warning .stat-icon {
  background: rgba($color-warning, 0.1);
  color: $color-warning;
}

.stat-card--danger .stat-icon {
  background: rgba($color-danger, 0.1);
  color: $color-danger;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 2px;
  margin-bottom: $spacing-xs;

  .value-prefix {
    font-size: $font-size-md;
    color: $color-text-primary;
  }

  .value-number {
    font-size: $font-size-title;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    line-height: 1;
  }

  .value-suffix {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-left: 2px;
  }
}

.stat-label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-bottom: $spacing-xs;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-size-xs;

  .trend-up {
    color: $color-success;
  }

  .trend-down {
    color: $color-danger;
  }

  .trend-text {
    color: $color-text-placeholder;
  }
}
</style>
