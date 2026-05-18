<template>
  <iconify-icon
    :icon="iconName"
    :class="classes"
    :style="iconStyle"
    :width="size"
    :height="size"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 图标名称 (支持 iconify-icon 格式，如 'lucide:home')
  name: {
    type: String,
    required: true
  },
  // 图标大小
  size: {
    type: [String, Number],
    default: 20
  },
  // 颜色
  color: {
    type: String,
    default: ''
  },
  // 旋转角度
  rotate: {
    type: [String, Number],
    default: 0
  },
  // 是否动画旋转
  spin: {
    type: Boolean,
    default: false
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

const iconName = computed(() => {
  // 如果已经包含了前缀，直接使用
  if (props.name.includes(':')) {
    return props.name
  }
  // 默认使用 lucide 图标集
  return `lucide:${props.name}`
})

const classes = computed(() => {
  const classes = ['app-icon']
  if (props.spin) classes.push('animate-spin')
  if (props.customClass) classes.push(props.customClass)
  return classes.join(' ')
})

const iconStyle = computed(() => {
  const style = {}
  if (props.color) style.color = props.color
  if (props.rotate) style.transform = `rotate(${props.rotate}deg)`
  return style
})
</script>

<style scoped>
.app-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
  transition: transform 0.2s ease, color 0.2s ease;
}

.app-icon.clickable {
  cursor: pointer;
}

.app-icon.clickable:hover {
  transform: scale(1.1);
}

.app-icon.clickable:active {
  transform: scale(0.95);
}
</style>
