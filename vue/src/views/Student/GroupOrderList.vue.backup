<template>
  <div class="group-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">拼单专区</h1>
      <div class="header-right">
        <iconify-icon icon="lucide:refresh-cw" class="refresh-icon" @click="loadAllData"></iconify-icon>
      </div>
    </div>

    <!-- 拼单优惠 Banner -->
    <div class="px-6 py-4">
      <div class="h-28 bg-gradient-to-r from-orange-500 to-orange-600 rounded-3xl relative overflow-hidden flex items-center p-5 text-white shadow-lg shadow-orange-200">
        <div class="relative z-10">
          <h3 class="text-xl font-black italic">拼单立减 15%</h3>
          <p class="text-xs opacity-90 mt-1">多人下单，共享优惠</p>
        </div>
        <img alt="Group eating" class="absolute right-[-20px] bottom-[-10px] w-36 h-36 object-contain opacity-40" src="https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=400&fit=crop"/>
      </div>
    </div>

    <!-- 标签页切换 -->
    <div class="tabs-container px-6 py-3">
      <div class="tabs">
        <div
          class="tab-item"
          :class="{ active: activeTab === 'all' }"
          @click="activeTab = 'all'"
        >
          <span class="tab-label">全部拼单</span>
          <span class="tab-count" v-if="allOrdersCount > 0">（{{ allOrdersCount }}）</span>
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'my' }"
          @click="activeTab = 'my'"
        >
          <span class="tab-label">我的拼单</span>
          <span class="tab-count" v-if="myOrdersCount > 0">（{{ myOrdersCount }}）</span>
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'history' }"
          @click="activeTab = 'history'"
        >
          <span class="tab-label">历史记录</span>
          <span class="tab-count" v-if="historyOrdersCount > 0">（{{ historyOrdersCount }}）</span>
        </div>
      </div>
    </div>

    <!-- 筛选标签（仅在全部拼单显示） -->
    <div class="filter-tabs px-6 py-3" v-if="activeTab === 'all'">
      <div
        class="filter-tab"
        :class="{ active: activeFilter === 'all' }"
        @click="activeFilter = 'all'"
      >
        全部
      </div>
      <div
        class="filter-tab"
        :class="{ active: activeFilter === 'urgent' }"
        @click="activeFilter = 'urgent'"
      >
        即将满员
      </div>
      <div
        class="filter-tab"
        :class="{ active: activeFilter === 'newest' }"
        @click="activeFilter = 'newest'"
      >
        最新
      </div>
    </div>

    <!-- 拼单列表 -->
    <div class="group-list px-6 pb-6" v-if="displayOrders.length > 0">
      <div
        class="group-card"
        v-for="order in displayOrders"
        :key="order.id"
        @click="goToMerchant(order.merchantId)"
      >
        <!-- 商家信息 -->
        <div class="merchant-section">
          <div class="merchant-info">
            <h3 class="merchant-name">{{ order.merchantName }}</h3>
            <div class="merchant-meta">
              <span class="rating">
                <iconify-icon icon="lucide:star" class="fill-current"></iconify-icon>
                {{ order.merchantRating || 4.5 }}
              </span>
              <span class="delivery-time">{{ order.merchantDeliveryTime || '30分钟' }}</span>
            </div>
          </div>
          <span class="status-badge" :class="`status-${order.status}`">
            {{ getStatusText(order.status) }}
          </span>
        </div>

        <!-- 菜品信息 -->
        <div class="dish-section">
          <img :src="getImageUrl(order.dishImage)" :alt="order.dishName" class="dish-image" />
          <div class="dish-info">
            <h4 class="dish-name">{{ order.dishName }}</h4>
            <p class="dish-desc" v-if="order.dishDesc">{{ order.dishDesc }}</p>
            <div class="price-info">
              <span class="current-price">¥{{ order.unitPrice }}</span>
              <span class="original-price" v-if="order.originalPrice && order.originalPrice > order.unitPrice">
                ¥{{ order.originalPrice }}
              </span>
              <span class="discount-tag" v-if="order.discountRate">
                {{ (order.discountRate * 10).toFixed(1) }}折
              </span>
            </div>
          </div>
        </div>

        <!-- 拼单进度 -->
        <div class="progress-section" v-if="['open', 'full'].includes(order.status)">
          <div class="progress-info">
            <span class="progress-text">{{ order.currentCount }}/{{ order.targetCount }}人</span>
            <span class="progress-remain" v-if="order.status === 'open'">
              还差 {{ order.targetCount - order.currentCount }} 人成团
            </span>
            <span class="progress-remain full" v-else>
              已满员，等待商家确认
            </span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :class="{ full: order.status === 'full' }"
              :style="{ width: `${Math.min(100, (order.currentCount / order.targetCount) * 100)}%` }"
            ></div>
          </div>
        </div>

        <!-- 发起者信息 -->
        <div class="initiator-section" v-if="activeTab === 'all' || activeTab === 'history'">
          <iconify-icon icon="lucide:user" class="icon"></iconify-icon>
          <span class="initiator-name">{{ order.initiatorName || '匿名同学' }}</span>
          <span class="initiate-time">{{ formatTime(order.createTime) }}</span>
        </div>

        <!-- 自己的拼单标识 -->
        <div class="my-order-tag" v-if="isMyOrder(order)">
          <iconify-icon icon="lucide:badge-check" class="icon"></iconify-icon>
          <span v-if="order.initiatorId === userStore.userId">我发起的</span>
          <span v-else>我参与的</span>
        </div>

        <!-- 截止时间倒计时 -->
        <div class="countdown-section" v-if="order.expireAt && ['open', 'full'].includes(order.status)">
          <iconify-icon icon="lucide:clock" class="icon"></iconify-icon>
          <span>剩余时间：</span>
          <span class="countdown-time" :class="{ urgent: isUrgent(order.expireAt) }">
            {{ getRemainTime(order.expireAt) }}
          </span>
        </div>

        <!-- 操作按钮 -->
        <div class="action-section" @click.stop v-if="['open', 'full'].includes(order.status)">
          <!-- 自己发起的拼单 -->
          <button
            v-if="order.initiatorId === userStore.userId"
            class="action-btn cancel-btn"
            @click="confirmCancel(order)"
          >
            取消拼单
          </button>
          <!-- 可以参与的拼单 -->
          <button
            v-else-if="!hasJoined(order)"
            class="join-btn"
            @click="joinGroupOrder(order)"
          >
            立即参与
          </button>
          <!-- 已参与的拼单 -->
          <button
            v-else
            class="action-btn joined-btn"
            disabled
          >
            已参与
          </button>
        </div>

        <!-- 历史记录的完成信息 -->
        <div class="completed-section" v-if="order.status === 'accepted'">
          <iconify-icon icon="lucide:check-circle" class="icon success"></iconify-icon>
          <span>拼单成功，订单已生成</span>
        </div>
        <div class="completed-section" v-else-if="order.status === 'cancelled'">
          <iconify-icon icon="lucide:x-circle" class="icon cancelled"></iconify-icon>
          <span>{{ getCancelReasonText(order.cancelReason) }}</span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="!loading">
      <div class="empty-icon">{{ getEmptyIcon() }}</div>
      <p class="empty-text">{{ getEmptyText() }}</p>
      <button class="create-btn" @click="goHome" v-if="activeTab === 'all'">去商家页面发起拼单</button>
    </div>

    <!-- 加载中 -->
    <div class="loading-state" v-if="loading">
      <iconify-icon icon="lucide:loader-2" class="loading-icon"></iconify-icon>
      <p>加载中...</p>
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
const activeTab = ref('all') // all, my, history

// 筛选条件
const activeFilter = ref('all')

// 拼单列表
const allOrders = ref([])
const myOrders = ref([])
const historyOrders = ref([])

// 加载状态
const loading = ref(false)

// 刷新定时器
let refreshTimer = null

// 计算数量
const allOrdersCount = computed(() => allOrders.value.length)
const myOrdersCount = computed(() => myOrders.value.filter(o => ['open', 'full'].includes(o.status)).length)
const historyOrdersCount = computed(() => historyOrders.value.length)

// 当前显示的拼单
const displayOrders = computed(() => {
  let orders = []

  switch (activeTab.value) {
    case 'my':
      orders = myOrders.value.filter(o => ['open', 'full'].includes(o.status))
      break
    case 'history':
      orders = historyOrders.value
      break
    default:
      orders = [...allOrders.value]
      // 根据筛选条件排序
      switch (activeFilter.value) {
        case 'urgent':
          orders.sort((a, b) => {
            const aRemain = a.targetCount - a.currentCount
            const bRemain = b.targetCount - b.currentCount
            return aRemain - bRemain
          })
          break
        case 'newest':
          orders.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
          break
        default:
          orders.sort((a, b) => b.currentCount - a.currentCount)
      }
  }

  return orders
})

// 判断是否是自己的拼单
const isMyOrder = (order) => {
  return order.initiatorId === userStore.userId || order.members?.some(m => m.userId === userStore.userId)
}

// 判断是否已参与
const hasJoined = (order) => {
  return order.members?.some(m => m.userId === userStore.userId)
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    open: '拼单中',
    full: '待确认',
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

// 判断是否紧急
const isUrgent = (expireTime) => {
  if (!expireTime) return false
  const remain = new Date(expireTime).getTime() - Date.now()
  return remain > 0 && remain < 15 * 60 * 1000
}

// 获取剩余时间
const getRemainTime = (expireTime) => {
  if (!expireTime) return ''
  const remain = new Date(expireTime).getTime() - Date.now()
  if (remain <= 0) return '已过期'
  const minutes = Math.floor(remain / 60000)
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  return `${hours}小时${minutes % 60}分钟`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 加载全部拼单
const loadAllOrders = async () => {
  try {
    const result = await request.get('/group-order/hot', { params: { limit: 100 } })
    allOrders.value = (result || []).filter(go => ['open', 'full'].includes(go.status))
  } catch (error) {
    console.error('加载全部拼单失败:', error)
    allOrders.value = []
  }
}

// 加载我的拼单
const loadMyOrders = async () => {
  try {
    const result = await request.get(`/group-order/my-active/${userStore.userId}`)
    myOrders.value = result || []
  } catch (error) {
    console.error('加载我的拼单失败:', error)
    myOrders.value = []
  }
}

// 加载历史记录
const loadHistoryOrders = async () => {
  try {
    const result = await request.get(`/group-order/my-history/${userStore.userId}`)
    historyOrders.value = result || []
  } catch (error) {
    console.error('加载历史记录失败:', error)
    historyOrders.value = []
  }
}

// 加载所有数据
const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadAllOrders(),
      loadMyOrders(),
      loadHistoryOrders()
    ])
  } finally {
    loading.value = false
  }
}

// 参与拼单
const joinGroupOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要参与「${order.dishName}」的拼单吗？`,
      '参与拼单',
      {
        confirmButtonText: '确定参与',
        cancelButtonText: '再想想',
        type: 'info'
      }
    )

    try {
      await request.post('/group-order/join', {
        groupOrderId: order.id,
        userId: userStore.userId
      })
      ElMessage.success('参与成功！')
      await loadAllData()
    } catch (error) {
      // interceptor handles message
    }
  } catch (error) {
    // 用户取消
  }
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
        type: 'warning'
      }
    )

    try {
      await request.post(`/group-order/cancel/${order.id}`, {
        userId: userStore.userId,
        cancelReason: 'initiator_cancel'
      })
      ElMessage.success('拼单已取消')
      await loadAllData()
    } catch (error) {
      // interceptor handles message
    }
  } catch (error) {
    // 用户取消
  }
}

// 跳转到商家详情
const goToMerchant = (merchantId) => {
  router.push(`/student/merchant/${merchantId}`)
}

// 去首页
const goHome = () => {
  router.push('/student/home')
}

// 获取空状态图标
const getEmptyIcon = () => {
  switch (activeTab.value) {
    case 'my': return '📋'
    case 'history': return '📜'
    default: return '🍜'
  }
}

// 获取空状态文本
const getEmptyText = () => {
  switch (activeTab.value) {
    case 'my': return '暂无进行中的拼单'
    case 'history': return '暂无历史拼单记录'
    default: return '暂无可参与的拼单'
  }
}

onMounted(() => {
  loadAllData()
  // 定时刷新（30秒）
  refreshTimer = setInterval(loadAllData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.group-list-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #fff7ed 0%, #f5f7fa 20%, #f5f7fa 100%);
  padding-bottom: 100px;
}

/* 页面头部 */
.page-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.refresh-icon {
  position: absolute;
  right: 16px;
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  transition: transform 0.3s, color 0.2s;
}

.refresh-icon:hover {
  color: #FF7A45;
  transform: rotate(180deg);
}

/* 标签页切换 */
.tabs-container {
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.tabs {
  display: flex;
  gap: 16px;
}

.tab-item {
  position: relative;
  padding: 12px 0;
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-item.active {
  color: #FF7A45;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #FF7A45;
  border-radius: 2px;
}

.tab-count {
  font-size: 13px;
  color: #909399;
}

.tab-item.active .tab-count {
  color: #FF7A45;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
}

.filter-tab {
  flex-shrink: 0;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tab.active {
  background: #FF7A45;
  color: white;
}

/* 拼单列表 */
.group-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.group-card:active {
  transform: scale(0.98);
}

/* 商家信息 */
.merchant-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.merchant-info {
  flex: 1;
}

.merchant-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.merchant-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.rating {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #FFB84D;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.status-open {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.status-full {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.status-accepted {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.status-cancelled {
  background: #f5f5f5;
  color: #8c8c8c;
}

/* 菜品信息 */
.dish-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.dish-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.dish-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.current-price {
  font-size: 20px;
  font-weight: 700;
  color: #FF7A45;
}

.original-price {
  font-size: 13px;
  color: #C0C4CC;
  text-decoration: line-through;
}

.discount-tag {
  padding: 2px 8px;
  background: #fff0f6;
  color: #ff4d4f;
  font-size: 11px;
  font-weight: 500;
  border-radius: 8px;
}

/* 拼单进度 */
.progress-section {
  margin-bottom: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #FF7A45;
}

.progress-remain {
  font-size: 12px;
  color: #909399;
}

.progress-remain.full {
  color: #fa8c16;
}

.progress-bar {
  height: 6px;
  background: #f0f2f5;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FFB84D 0%, #FF7A45 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-fill.full {
  background: linear-gradient(90deg, #52c41a 0%, #389e0d 100%);
}

/* 发起者信息 */
.initiator-section {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.initiator-section .icon {
  color: #909399;
  font-size: 14px;
}

.initiator-name {
  font-size: 13px;
  color: #606266;
}

.initiate-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

/* 自己的拼单标识 */
.my-order-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fff7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  color: #FF7A45;
  font-size: 13px;
  font-weight: 500;
}

.my-order-tag .icon {
  color: #FF7A45;
  font-size: 14px;
}

/* 倒计时 */
.countdown-section {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fff7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.countdown-section .icon {
  color: #FF7A45;
  font-size: 14px;
}

.countdown-time {
  margin-left: auto;
  color: #52c41a;
  font-weight: 500;
}

.countdown-time.urgent {
  color: #ff4d4f;
}

/* 操作按钮 */
.action-section {
  display: flex;
  gap: 8px;
}

.action-btn,
.join-btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.action-btn:hover,
.join-btn:hover {
  opacity: 0.9;
}

.join-btn {
  background: linear-gradient(135deg, #FF7A45 0%, #FFB84D 100%);
  color: white;
}

.cancel-btn {
  background: #fff1f0;
  color: #ff4d4f;
}

.joined-btn {
  background: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}

/* 完成状态 */
.completed-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f6ffed;
  border-radius: 8px;
  font-size: 13px;
  color: #52c41a;
}

.completed-section .icon {
  font-size: 16px;
}

.completed-section .icon.cancelled {
  color: #8c8c8c;
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

.create-btn {
  padding: 10px 24px;
  background: #FF7A45;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

/* 加载中 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-icon {
  font-size: 32px;
  color: #FF7A45;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
