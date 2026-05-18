<template>
  <div class="my-group-orders-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <iconify-icon icon="lucide:arrow-left" class="text-xl"></iconify-icon>
      </button>
      <h1 class="page-title">我的拼单</h1>
      <div class="header-right">
        <iconify-icon icon="lucide:refresh-cw" class="refresh-icon" @click="loadData"></iconify-icon>
      </div>
    </div>

    <!-- 切换标签 -->
    <div class="tabs">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'initiated' }"
        @click="activeTab = 'initiated'"
      >
        <span class="tab-label">我发起的</span>
        <span class="tab-count" v-if="initiatedCount > 0">{{ initiatedCount }}</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'joined' }"
        @click="activeTab = 'joined'"
      >
        <span class="tab-label">我参与的</span>
        <span class="tab-count" v-if="joinedCount > 0">{{ joinedCount }}</span>
      </div>
    </div>

    <!-- 拼单列表 -->
    <div class="group-list" v-if="filteredOrders.length > 0">
      <div class="group-card" v-for="order in filteredOrders" :key="order.id">
        <!-- 菜品信息 -->
        <div class="dish-section">
          <img :src="getImageUrl(order.dishImage)" :alt="order.dishName" class="dish-image" />
          <div class="dish-info">
            <h3 class="dish-name">{{ order.dishName }}</h3>
            <p class="merchant-name">{{ order.merchantName }}</p>
          </div>
        </div>

        <!-- 拼单进度 -->
        <div class="progress-section">
          <div class="progress-header">
            <span class="progress-label">拼单进度</span>
            <span class="progress-count">{{ order.currentCount }}/{{ order.targetCount }}人</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :class="{ full: order.currentCount >= order.targetCount }"
              :style="{ width: `${Math.min(100, (order.currentCount / order.targetCount) * 100)}%` }"
            ></div>
          </div>
          <p class="progress-hint" v-if="order.currentCount < order.targetCount">
            还差 {{ order.targetCount - order.currentCount }} 人成团
          </p>
          <p class="progress-hint success" v-else>
            已满员，等待商家接单
          </p>
        </div>

        <!-- 价格信息 -->
        <div class="price-section">
          <span class="price-label">拼单价</span>
          <span class="price-value">¥{{ order.unitPrice }}</span>
          <span class="original-price" v-if="getDishOriginalPrice(order) > Number(order.unitPrice || 0)">
            原价¥{{ getDishOriginalPrice(order) }}
          </span>
        </div>

        <!-- 配送费信息 -->
        <div class="delivery-fee-section" v-if="order.targetCount">
          <iconify-icon icon="lucide:truck" class="icon"></iconify-icon>
          <span class="delivery-label">预计配送费</span>
          <span class="delivery-value">¥{{ calculateDeliveryFee(order) }}/人</span>
          <span class="delivery-total">总价 ¥{{ calculateTotalPrice(order) }}</span>
        </div>

        <!-- 截止时间 -->
        <div class="expire-section" v-if="order.expireAt">
          <iconify-icon icon="lucide:clock" class="icon"></iconify-icon>
          <span>截止：{{ formatExpireTime(order.expireAt) }}</span>
          <span class="remain-time" :class="{ urgent: isUrgent(order.expireAt) }">
            {{ getRemainTime(order.expireAt) }}
          </span>
        </div>

        <!-- 参与成员 -->
        <div class="members-section" v-if="order.members && order.members.length > 0">
          <div class="members-label">参与成员：</div>
          <div class="members-list">
            <span class="member-item" v-for="member in order.members" :key="member.id">
              {{ member.userName }}
              <span v-if="member.userId === order.initiatorId" class="initiator-badge">发起者</span>
            </span>
          </div>
        </div>

        <!-- 状态标签 -->
        <div class="status-section">
          <span class="status-tag" :class="`status-${order.status}`">
            {{ getStatusText(order.status) }}
          </span>
          <span class="cancel-reason" v-if="order.status === 'cancelled' && order.cancelReason">
            {{ getCancelReasonText(order.cancelReason) }}
          </span>
        </div>

        <!-- 操作按钮 -->
        <div class="actions-section" v-if="canCancel(order)">
          <button class="action-btn cancel-btn" @click="confirmCancel(order)">
            取消拼单
          </button>
        </div>

        <!-- 商家接单后的提示 -->
        <div class="accepted-hint" v-if="order.status === 'accepted'">
          <iconify-icon icon="lucide:check-circle" class="icon"></iconify-icon>
          <span>拼单已成功，订单已生成，可在「订单」中查看</span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <div class="empty-icon">👥</div>
      <p class="empty-text">
        {{ activeTab === 'initiated' ? '你还没有发起过拼单' : '你还没有参与过拼单' }}
      </p>
      <el-button type="primary" @click="goHome">去发起拼单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/axios/request'
import { useUserStore } from '@/stores/user'
import { getImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const userStore = useUserStore()

// 当前标签
const activeTab = ref('initiated')

// 拼单列表
const groupOrders = ref([])

// 刷新定时器
let refreshTimer = null

// 发起的拼单
const initiatedOrders = computed(() => {
  return groupOrders.value.filter(go => go.initiatorId === userStore.userId)
})

// 参与的拼单
const joinedOrders = computed(() => {
  return groupOrders.value.filter(go => {
    if (go.initiatorId === userStore.userId) return false
    return go.members?.some(m => m.userId === userStore.userId)
  })
})

// 当前显示的拼单
const filteredOrders = computed(() => {
  return activeTab.value === 'initiated' ? initiatedOrders.value : joinedOrders.value
})

// 数量统计
const initiatedCount = computed(() => initiatedOrders.value.length)
const joinedCount = computed(() => joinedOrders.value.length)

// 获取菜品原价
const getDishOriginalPrice = (order) => {
  // 这里简化处理，实际可以从缓存或接口获取
  return Number(order.unitPrice || 0) * 1.15 // 假设原价比拼单价高15%
}

// 计算拼单配送费（每人）
// 规则：总配送费 = 商家配送费 + 拼单人数，每人平摊
const calculateDeliveryFee = (order) => {
  const peopleCount = order.targetCount || 2
  // 从后端返回的商家配送费获取
  const merchantDeliveryFee = Number(order.merchantDeliveryFee || 0)
  const totalFee = merchantDeliveryFee + peopleCount
  return (totalFee / peopleCount).toFixed(2)
}

// 计算预计总价（拼单价 + 配送费）
const calculateTotalPrice = (order) => {
  const unitPrice = Number(order.unitPrice || 0)
  const deliveryFee = Number(calculateDeliveryFee(order))
  return (unitPrice + deliveryFee).toFixed(2)
}

// 格式化过期时间
const formatExpireTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 判断是否紧急
const isUrgent = (expireTime) => {
  if (!expireTime) return false
  const remain = new Date(expireTime).getTime() - Date.now()
  return remain > 0 && remain < 10 * 60 * 1000 // 少于10分钟
}

// 获取剩余时间
const getRemainTime = (expireTime) => {
  if (!expireTime) return ''
  const remain = new Date(expireTime).getTime() - Date.now()
  if (remain <= 0) return '已过期'
  const minutes = Math.floor(remain / 60000)
  if (minutes < 60) return `剩余${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  return `剩余${hours}小时${minutes % 60}分钟`
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    open: '拼单中',
    full: '待商家接单',
    accepted: '已成团',
    cancelled: '已取消'
  }
  return map[status] || '未知'
}

// 获取取消原因文本
const getCancelReasonText = (reason) => {
  const map = {
    timeout: '超时自动取消',
    expired: '已过期',
    manual: '手动取消',
    initiator_cancel: '发起者取消',
    merchant_cancel: '商家取消',
    admin_cancel: '管理员取消'
  }
  return map[reason] || '已取消'
}

// 判断是否可以取消
const canCancel = (order) => {
  if (order.status !== 'open' && order.status !== 'full') return false
  if (activeTab.value === 'joined') return false // 参与者不能取消
  return order.initiatorId === userStore.userId
}

// 确认取消拼单
const confirmCancel = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消「${order.dishName}」的拼单吗？取消后已参与的成员将被通知，且无法恢复。`,
      '取消拼单',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    await cancelGroupOrder(order.id)
  } catch (error) {
    // 用户取消操作
  }
}

// 取消拼单
const cancelGroupOrder = async (groupOrderId) => {
  try {
    await request.post(`/group-order/cancel/${groupOrderId}`, {
      userId: userStore.userId,
      cancelReason: 'initiator_cancel'
    })
    ElMessage.success('拼单已取消')
    await loadData()
  } catch (error) {
    // interceptor handles message
  }
}

// 加载数据
const loadData = async () => {
  if (!userStore.userId) return
  try {
    const result = await request.get(`/group-order/my-active/${userStore.userId}`)
    groupOrders.value = result || []
  } catch (error) {
    console.error('加载拼单失败:', error)
    groupOrders.value = []
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 去首页
const goHome = () => {
  router.push('/student')
}

onMounted(() => {
  loadData()
  // 定时刷新（30秒）
  refreshTimer = setInterval(loadData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.my-group-orders-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #fff7ed 0%, #f5f7fa 30%, #f5f7fa 100%);
  padding-bottom: 20px;
}

/* 页面头部 */
.page-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #e8eef6;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.refresh-icon {
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  transition: transform 0.3s, color 0.2s;
}

.refresh-icon:hover {
  color: #FF7A45;
  transform: rotate(180deg);
}

/* 切换标签 */
.tabs {
  display: flex;
  padding: 12px 16px;
  background: white;
  gap: 16px;
}

.tab-item {
  position: relative;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-item.active {
  color: #FF7A45;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #FF7A45;
  border-radius: 2px;
}

.tab-count {
  margin-left: 4px;
  padding: 2px 6px;
  background: #FF7A45;
  color: white;
  font-size: 11px;
  border-radius: 10px;
}

/* 拼单列表 */
.group-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 菜品信息 */
.dish-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.dish-image {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
}

.dish-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.merchant-name {
  font-size: 13px;
  color: #909399;
}

/* 拼单进度 */
.progress-section {
  margin-bottom: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 13px;
  color: #606266;
}

.progress-count {
  font-size: 15px;
  font-weight: 700;
  color: #FF7A45;
}

.progress-bar {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FFB84D 0%, #FF7A45 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-fill.full {
  background: linear-gradient(90deg, #52c41a 0%, #389e0d 100%);
}

.progress-hint {
  font-size: 12px;
  color: #FF7A45;
  margin-top: 6px;
}

.progress-hint.success {
  color: #52c41a;
}

/* 价格信息 */
.price-section {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.price-label {
  font-size: 13px;
  color: #909399;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: #FF7A45;
}

.original-price {
  font-size: 12px;
  color: #C0C4CC;
  text-decoration: line-through;
}

/* 配送费信息 */
.delivery-fee-section {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 8px;
  margin-bottom: 12px;
}

.delivery-fee-section .icon {
  color: #409eff;
  font-size: 14px;
}

.delivery-label {
  font-size: 12px;
  color: #606266;
}

.delivery-value {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  margin-left: auto;
}

.delivery-total {
  font-size: 13px;
  font-weight: 700;
  color: #FF7A45;
  margin-left: 8px;
}

/* 截止时间 */
.expire-section {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fff7ed;
  border-radius: 8px;
  margin-bottom: 12px;
}

.expire-section .icon {
  color: #FF7A45;
}

.remain-time {
  margin-left: auto;
  font-size: 12px;
  color: #52c41a;
}

.remain-time.urgent {
  color: #ff4d4f;
}

/* 参与成员 */
.members-section {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.members-label {
  font-size: 13px;
  color: #606266;
  width: 100%;
  margin-bottom: 6px;
}

.members-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.member-item {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  background: white;
  border-radius: 12px;
  font-size: 12px;
  color: #606266;
}

.initiator-badge {
  margin-left: 4px;
  padding: 2px 6px;
  background: #FF7A45;
  color: white;
  font-size: 10px;
  border-radius: 8px;
}

/* 状态 */
.status-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.status-open {
  background: #e6f7ff;
  color: #1890ff;
}

.status-tag.status-full {
  background: #fff7e6;
  color: #fa8c16;
}

.status-tag.status-accepted {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.status-cancelled {
  background: #f5f5f5;
  color: #8c8c8c;
}

.cancel-reason {
  font-size: 12px;
  color: #8c8c8c;
}

/* 操作按钮 */
.actions-section {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.action-btn:hover {
  opacity: 0.9;
}

.cancel-btn {
  background: #fff1f0;
  color: #ff4d4f;
}

/* 成团提示 */
.accepted-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  color: #52c41a;
  font-size: 13px;
}

.accepted-hint .icon {
  font-size: 16px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  color: #909399;
  font-size: 14px;
  margin-bottom: 24px;
}
</style>
