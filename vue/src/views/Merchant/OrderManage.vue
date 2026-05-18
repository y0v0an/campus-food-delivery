<template>
  <div class="order-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">订单管理</h1>
      <div class="header-actions">
        <el-button type="success" @click="openFreeDialog">免单制单</el-button>
        <el-button :icon="Refresh" @click="loadOrders">刷新</el-button>
      </div>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-tabs">
      <el-radio-group v-model="activeStatus" @change="filterByStatus">
        <el-radio-button label="">
          全部 ({{ orders.length }})
        </el-radio-button>
        <el-radio-button label="pending_accept">
          待接单 ({{ getStatusCount('pending_accept') }})
        </el-radio-button>
        <el-radio-button label="preparing">
          制作中 ({{ getStatusCount('preparing') }})
        </el-radio-button>
        <el-radio-button label="ready">
          待取餐 ({{ getStatusCount('ready') }})
        </el-radio-button>
        <el-radio-button label="completed">
          已完成 ({{ getStatusCount('completed') }})
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <div v-if="filteredOrders.length === 0" class="empty-state">
        <el-empty description="暂无订单" />
      </div>

      <div
        v-for="order in filteredOrders"
        :key="order.id"
        class="order-card"
      >
        <div class="order-header">
          <div class="order-info">
            <div class="order-title-row">
              <span class="order-no">订单号: {{ order.orderNo }}</span>
              <el-tag v-if="order.groupOrderId" size="small" type="warning" effect="plain">拼单</el-tag>
            </div>
            <span class="order-time">{{ formatTime(order.createdAt) }}</span>
          </div>
          <StatusTag :status="order.status" />
        </div>

        <div class="order-items">
          <div v-for="item in order.items" :key="item.dishId" class="item">
            <span class="item-name">{{ item.dishName }}</span>
            <span class="item-quantity">x{{ item.quantity }}</span>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-amount">
            <span class="label">实付金额:</span>
            <span class="amount">¥{{ order.actualAmount }}</span>
          </div>
          <div class="order-actions">
            <el-button size="small" @click="showOrderDetail(order)">
              查看详情
            </el-button>
            <el-button size="small" @click="showReview(order)">
              查看评价
            </el-button>
            <el-button
              v-if="order.status === 'pending_accept'"
              type="primary"
              size="small"
              @click="acceptOrder(order)"
            >
              接单
            </el-button>
            <el-button
              v-if="order.status === 'preparing'"
              type="success"
              size="small"
              @click="readyOrder(order)"
            >
              已出餐
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <OrderDetail v-if="currentOrder" :order="currentOrder" />
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="订单评价" width="640px">
      <div v-if="currentReview" class="review-panel">
        <div class="review-row">
          <span class="label">商家评分</span>
          <el-rate
            :model-value="currentReview.merchantRating || 0"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value} 分"
          />
        </div>
        <div class="review-row">
          <span class="label">骑手评分</span>
          <el-rate
            :model-value="currentReview.riderRating || 0"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value} 分"
          />
        </div>
        <div class="review-row">
          <span class="label">评价时间</span>
          <span class="value">{{ formatTime(currentReview.createdAt) || '-' }}</span>
        </div>
        <div class="review-content">
          {{ currentReview.content || '用户未填写文字评价' }}
        </div>
        <div v-if="reviewImages.length" class="review-images">
          <el-image
            v-for="(img, index) in reviewImages"
            :key="`${img}-${index}`"
            :src="getImageUrl(img)"
            :preview-src-list="reviewImages.map(getImageUrl)"
            fit="cover"
            class="review-image"
          />
        </div>
      </div>
      <el-empty v-else description="该订单暂无评价" />
    </el-dialog>

    <el-dialog v-model="freeDialogVisible" title="免单制单" width="480px">
      <el-form :model="freeForm" label-width="90px">
        <el-form-item label="菜品">
          <el-select v-model="freeForm.dishId" style="width: 100%" placeholder="请选择菜品">
            <el-option v-for="d in merchantDishes" :key="d.id" :label="`${d.name} ¥${d.price}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="freeForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="取餐人">
          <el-input v-model="freeForm.contactName" placeholder="可不填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="freeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="freeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createFreeOrder">确认制单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import OrderDetail from '@/components/order/OrderDetail.vue'
import { useUserStore } from '@/stores/user'
import { useOrderStore } from '@/stores/order'
import request from '@/axios/request'

const userStore = useUserStore()
const orderStore = useOrderStore()

// 订单列表
const orders = ref([])

// 当前筛选状态
const activeStatus = ref('')

// 过滤后的订单
const filteredOrders = computed(() => {
  if (!activeStatus.value) return orders.value
  return orders.value.filter(o => o.status === activeStatus.value)
})

// 详情弹窗
const detailVisible = ref(false)
const currentOrder = ref(null)
const reviewVisible = ref(false)
const currentReview = ref(null)
const reviewImages = ref([])
const groupOrders = ref([])
const merchantDishes = ref([])
const freeDialogVisible = ref(false)
const freeForm = ref({
  dishId: '',
  quantity: 1,
  contactName: '',
  remark: ''
})

// 刷新定时器
let refreshTimer = null

// 获取状态数量
const getStatusCount = (status) => {
  return orders.value.filter(o => o.status === status).length
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/img/')) return `http://localhost:8080${url}`
  return url
}

const parseReviewImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  const raw = String(images).trim()
  if (!raw) return []
  if (raw.startsWith('[')) {
    try {
      const list = JSON.parse(raw)
      return Array.isArray(list) ? list.filter(Boolean) : []
    } catch {
      return []
    }
  }
  if (raw.includes(',')) {
    return raw.split(',').map(v => v.trim()).filter(Boolean)
  }
  return [raw]
}

// 获取商家ID（通过用户ID获取商家信息）
const getMerchantId = async () => {
  const userId = userStore.userInfo?.id || ''
  if (!userId) return ''
  
  // 如果已经获取过商家信息，直接返回
  if (merchantInfo.value) {
    return merchantInfo.value.id
  }
  
  try {
    // 通过用户ID获取商家信息
    const merchant = await request.get(`/merchant/user/${userId}`)
    if (merchant) {
      merchantInfo.value = merchant
      return merchant.id
    }
  } catch (error) {
    console.error('获取商家信息失败:', error)
  }
  return ''
}

// 商家信息
const merchantInfo = ref(null)

// 加载订单
const loadOrders = async () => {
  const merchantId = await getMerchantId()
  if (!merchantId) return

  // 始终加载全部订单，筛选由前端 computed filteredOrders 处理
  await orderStore.loadMerchantOrders(merchantId, null)
  orders.value = orderStore.orderList
  await loadGroupOrders()
  await loadMerchantDishes()
}

const loadGroupOrders = async () => {
  const merchantId = await getMerchantId()
  if (!merchantId) return
  groupOrders.value = await request.get(`/group-order/merchant/${merchantId}`)
}

const loadMerchantDishes = async () => {
  const merchantId = await getMerchantId()
  if (!merchantId) return
  merchantDishes.value = await request.get(`/dish/all/${merchantId}`)
}

// 按状态筛选
const filterByStatus = () => {
  // 筛选由前端 computed filteredOrders 自动处理，无需额外操作
}

// 显示订单详情
const showOrderDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

const showReview = (order) => {
  currentReview.value = order.review || null
  reviewImages.value = parseReviewImages(order.review?.images)
  reviewVisible.value = true
}

// 接单
const acceptOrder = async (order) => {
  const result = await orderStore.updateOrderStatus(order.id, 'preparing')
  if (result.success) {
    ElMessage.success('已接单')
    loadOrders()
  } else {
    ElMessage.error(result.message || '接单失败')
  }
}

// 出餐
const readyOrder = async (order) => {
  const result = await orderStore.updateOrderStatus(order.id, 'ready')
  if (result.success) {
    ElMessage.success('已出餐，等待骑手取餐')
    loadOrders()
  } else {
    ElMessage.error(result.message || '操作失败')
  }
}

const acceptGroupOrder = async (groupOrder) => {
  const merchantId = await getMerchantId()
  if (!merchantId) return
  try {
    await request.put(`/group-order/accept/${groupOrder.id}`, null, { params: { merchantId } })
    ElMessage.success('拼单已接单，并生成参与用户订单')
    loadOrders()
  } catch (error) {
    // interceptor handles
  }
}

const openFreeDialog = async () => {
  await loadMerchantDishes()
  freeForm.value = { dishId: '', quantity: 1, contactName: '', remark: '' }
  freeDialogVisible.value = true
}

const createFreeOrder = async () => {
  const merchantId = await getMerchantId()
  if (!merchantId) return
  if (!freeForm.value.dishId) {
    ElMessage.warning('请选择菜品')
    return
  }
  try {
    await request.post('/order/merchant/free-create', {
      merchantId,
      ...freeForm.value
    })
    ElMessage.success('免单订单已创建，已进入制作中')
    freeDialogVisible.value = false
    loadOrders()
  } catch (error) {
    // interceptor handles
  }
}

const getGroupStatusText = (status) => {
  const map = {
    open: '拼单中',
    full: '待商家接单',
    accepted: '已成团',
    cancelled: '已取消'
  }
  return map[status] || '未知'
}

// 判断是否可以取消拼单
const canCancelGroup = (groupOrder) => {
  return groupOrder && ['open', 'full'].includes(groupOrder.status)
}

// 商家取消拼单确认
const confirmCancelGroupOrder = async (groupOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消「${groupOrder.dishName}」的拼单吗？取消后已参与的成员将被通知，且无法恢复。`,
      '取消拼单',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )
    await cancelGroupOrderByMerchant(groupOrder.id)
  } catch (error) {
    // 用户取消操作
  }
}

// 商家取消拼单
const cancelGroupOrderByMerchant = async (groupOrderId) => {
  const merchantId = await getMerchantId()
  if (!merchantId) return
  try {
    await request.post(`/group-order/cancel-by-merchant/${groupOrderId}`, {
      merchantId,
      cancelReason: 'merchant_cancel'
    })
    ElMessage.success('拼单已取消')
    loadGroupOrders()
  } catch (error) {
    // interceptor handles
  }
}

// 监听订单列表变化
watch(() => orderStore.orderList, (newList) => {
  orders.value = newList
})

onMounted(() => {
  loadOrders()
  // 定时刷新
  refreshTimer = setInterval(loadOrders, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.order-manage {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;

    .page-title {
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }

    .header-actions {
      display: flex;
      gap: $spacing-sm;
    }
  }

  .filter-tabs {
    margin-bottom: $spacing-lg;
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }

  .empty-state {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-xxl;
  }

  .order-card {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-base;
    box-shadow: $shadow-sm;

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: $spacing-md;
      padding-bottom: $spacing-md;
      border-bottom: 1px solid $color-border-lighter;

      .order-info {
        display: flex;
        flex-direction: column;
        gap: $spacing-xs;

        .order-title-row {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
        }

        .order-no {
          font-weight: $font-weight-medium;
        }

        .order-time {
          font-size: $font-size-sm;
          color: $color-text-secondary;
        }
      }
    }

    .order-items {
      margin-bottom: $spacing-md;

      .item {
        display: flex;
        justify-content: space-between;
        padding: $spacing-xs 0;
        font-size: $font-size-sm;

        .item-quantity {
          color: $color-text-secondary;
        }
      }
    }

    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: $spacing-md;
      border-top: 1px solid $color-border-lighter;

      .order-amount {
        .label {
          color: $color-text-secondary;
          margin-right: $spacing-sm;
        }

        .amount {
          font-size: $font-size-lg;
          font-weight: $font-weight-bold;
          color: $color-warning;
        }
      }

      .order-actions {
        display: flex;
        gap: $spacing-sm;
      }
    }
  }
}

.review-panel {
  background: $color-bg-white;
  border: 1px solid $color-border-lighter;
  border-radius: $border-radius-md;
  padding: $spacing-base;

  .review-row {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    margin-bottom: $spacing-sm;

    .label {
      width: 72px;
      color: $color-text-secondary;
      font-size: $font-size-sm;
    }

    .value {
      color: $color-text-primary;
      font-size: $font-size-sm;
    }
  }

  .review-content {
    margin-top: $spacing-md;
    padding: $spacing-sm $spacing-base;
    border-radius: $border-radius-base;
    background: $color-bg-light;
    color: $color-text-primary;
    line-height: 1.7;
    white-space: pre-wrap;
  }

  .review-images {
    margin-top: $spacing-md;
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .review-image {
    width: 96px;
    height: 96px;
    border-radius: $border-radius-base;
    border: 1px solid $color-border-lighter;
    overflow: hidden;
  }
}
</style>
