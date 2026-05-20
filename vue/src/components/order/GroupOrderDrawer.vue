<template>
  <el-drawer
    v-model="visible"
    direction="btt"
    :size="'70%'"
    :with-header="false"
    :show-close="false"
    class="group-drawer"
  >
    <div class="drawer-content" @click.stop>
      <!-- 抽屉头部 -->
      <div class="drawer-header">
        <div class="header-left">
          <iconify-icon icon="lucide:users" class="icon"></iconify-icon>
          <h3 class="header-title">正在拼单</h3>
          <span class="count-badge">({{ orders.length }})</span>
        </div>
        <button class="close-btn" @click="handleClose">
          <iconify-icon icon="lucide:x"></iconify-icon>
        </button>
      </div>

      <!-- 拼单列表 -->
      <div class="drawer-body" v-if="orders.length > 0">
        <div
          class="order-item"
          v-for="order in orders"
          :key="order.id"
          @click="handleOrderClick(order)"
        >
          <img :src="getImageUrl(order.dishImage)" :alt="order.dishName" class="order-image" />
          <div class="order-info">
            <h4 class="order-name">{{ order.dishName }}</h4>
            <p class="order-merchant">{{ order.merchantName }}</p>
            <div class="order-progress">
              <span class="progress-text">{{ order.currentCount }}/{{ order.targetCount }}人</span>
              <span class="progress-status" :class="`status-${order.status}`">
                {{ getStatusText(order.status) }}
              </span>
            </div>
          </div>
          <button
            class="join-mini-btn"
            v-if="canJoin(order)"
            @click.stop="handleJoin(order)"
          >
            参与
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="drawer-empty" v-else>
        <iconify-icon icon="lucide:users" class="empty-icon"></iconify-icon>
        <p>暂无进行中的拼单</p>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getImageUrl } from '@/utils/imageUrl'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  orders: {
    type: Array,
    default: () => []
  },
  userId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'join'])

const router = useRouter()
const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const handleOrderClick = (order) => {
  visible.value = false
  router.push(`/student/merchant/${order.merchantId}`)
}

const canJoin = (order) => {
  return order.status === 'open' &&
         order.initiatorId !== props.userId &&
         !order.members?.some(m => m.userId === props.userId)
}

const handleJoin = (order) => {
  emit('join', order)
}

const getStatusText = (status) => {
  const map = {
    open: '拼单中',
    full: '待确认'
  }
  return map[status] || '未知'
}
</script>

<style scoped>
.group-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left .icon {
  font-size: 20px;
  color: #FF7A45;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.count-badge {
  font-size: 14px;
  color: #909399;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #f5f7fa;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  color: #606266;
}

.close-btn:hover {
  background: #e4e7ed;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.order-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.order-item:active {
  transform: scale(0.98);
}

.order-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-merchant {
  font-size: 12px;
  color: #909399;
  margin: 0 0 6px 0;
}

.order-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  font-size: 13px;
  font-weight: 500;
  color: #FF7A45;
}

.progress-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 8px;
}

.progress-status.status-open {
  background: #e6f7ff;
  color: #1890ff;
}

.progress-status.status-full {
  background: #fff7e6;
  color: #fa8c16;
}

.join-mini-btn {
  padding: 6px 14px;
  background: linear-gradient(135deg, #FF7A45 0%, #FFB84D 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  align-self: center;
}

.drawer-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.drawer-empty p {
  font-size: 14px;
  margin: 0;
}
</style>
