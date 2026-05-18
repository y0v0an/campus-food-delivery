<template>
  <div class="admin-order-page">
    <div class="page-header">
      <h1 class="page-title">订单列表</h1>
      <div class="actions">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索订单号/商家/手机号"
          :prefix-icon="Search"
          style="width: 260px"
          @keyup.enter="loadOrders"
        />
        <el-select v-model="merchantId" clearable placeholder="全部商家" style="width: 220px">
          <el-option
            v-for="item in merchants"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="status" clearable placeholder="全部状态" style="width: 160px">
          <el-option label="待接单" value="pending_accept" />
          <el-option label="制作中" value="preparing" />
          <el-option label="待取餐" value="ready" />
          <el-option label="配送中" value="delivering" />
          <el-option label="已完成" value="completed" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-button type="primary" @click="loadOrders">查询</el-button>
      </div>
    </div>

    <el-table :data="orders" stripe>
      <el-table-column prop="merchantName" label="商家" min-width="120" />
      <el-table-column prop="studentName" label="学生姓名" width="120" />
      <el-table-column prop="actualAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.actualAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" min-width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="下单内容" min-width="260">
        <template #default="{ row }">
          <div class="order-items-cell">
            <template v-if="row.items?.length">
              <div
                v-for="(item, index) in row.items.slice(0, 3)"
                :key="`${item.dishId || item.dishName}-${index}`"
                class="item-line"
              >
                <span class="item-name">{{ item.dishName }}</span>
                <span class="item-qty">x{{ item.quantity }}</span>
              </div>
              <div v-if="row.items.length > 3" class="more-tip">
                等{{ row.items.length }}件商品
              </div>
            </template>
            <span v-else class="empty-text">无商品明细</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="评价" min-width="220">
        <template #default="{ row }">
          <span v-if="row.review">
            商家{{ row.review.merchantRating || '-' }}星
            <span v-if="row.review.riderRating"> / 骑手{{ row.review.riderRating }}星</span>
          </span>
          <span v-else>暂无评价</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="showReview(row)">
            查看评价
          </el-button>
          <el-popconfirm title="确认删除该订单？" @confirm="deleteOrder(row)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="reviewVisible" title="订单评价" width="640px">
      <div class="review-panel">
        <div class="order-items-block">
          <div class="block-title">下单商品</div>
          <template v-if="currentOrder?.items?.length">
            <div
              v-for="(item, index) in currentOrder.items"
              :key="`${item.dishId || item.dishName}-${index}`"
              class="order-item-row"
            >
              <div class="left">
                <el-image
                  v-if="item.dishImage"
                  :src="getImageUrl(item.dishImage)"
                  :preview-src-list="[getImageUrl(item.dishImage)]"
                  fit="cover"
                  class="dish-image"
                />
                <span class="name">{{ item.dishName }}</span>
              </div>
              <div class="right">
                <span class="price">¥{{ toPrice(item.price) }}</span>
                <span class="qty">x{{ item.quantity }}</span>
                <span class="subtotal">¥{{ toPrice(item.subtotal) }}</span>
              </div>
            </div>
          </template>
          <div v-else class="empty-text">无商品明细</div>
        </div>
        <template v-if="currentReview">
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
        </template>
        <el-empty v-else description="该订单暂无评价" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/axios/request'

const orders = ref([])
const merchants = ref([])
const keyword = ref('')
const merchantId = ref('')
const status = ref('')

const reviewVisible = ref(false)
const currentReview = ref(null)
const currentOrder = ref(null)
const reviewImages = ref([])

const statusMap = {
  pending_payment: { text: '待支付', type: 'warning' },
  pending_accept: { text: '待接单', type: 'warning' },
  preparing: { text: '制作中', type: 'primary' },
  ready: { text: '待取餐', type: 'primary' },
  delivering: { text: '配送中', type: '' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'info' }
}

const getStatusText = (statusValue) => statusMap[statusValue]?.text || statusValue || '-'
const getStatusType = (statusValue) => statusMap[statusValue]?.type || 'info'

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  if (Number.isNaN(date.getTime())) {
    return String(timeStr).replace('T', ' ')
  }
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
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

const toPrice = (num) => {
  const n = Number(num)
  if (Number.isNaN(n)) return num || '0.00'
  return n.toFixed(2)
}

const loadMerchants = async () => {
  merchants.value = await request.get('/merchant/all')
}

const loadOrders = async () => {
  orders.value = await request.get('/order/admin/list', {
    params: {
      keyword: keyword.value || undefined,
      merchantId: merchantId.value || undefined,
      status: status.value || undefined
    }
  })
}

const showReview = (order) => {
  currentOrder.value = order
  currentReview.value = order.review || null
  reviewImages.value = parseReviewImages(order.review?.images)
  reviewVisible.value = true
}

const deleteOrder = async (order) => {
  await request.delete(`/order/admin/${order.id}`)
  ElMessage.success('订单已删除')
  loadOrders()
}

watch([merchantId, status], () => loadOrders())

onMounted(async () => {
  await loadMerchants()
  await loadOrders()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.admin-order-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;
  }

  .page-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.order-items-cell {
  .item-line {
    display: flex;
    justify-content: space-between;
    line-height: 1.6;
  }

  .item-name {
    max-width: 170px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .item-qty {
    color: $color-text-secondary;
    margin-left: $spacing-sm;
    flex-shrink: 0;
  }

  .more-tip {
    color: $color-text-secondary;
    font-size: $font-size-sm;
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

  .order-items-block {
    margin-bottom: $spacing-md;
    padding: $spacing-sm $spacing-base;
    border-radius: $border-radius-base;
    border: 1px solid $color-border-lighter;

    .block-title {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      margin-bottom: $spacing-xs;
    }

    .order-item-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      line-height: 1.8;
      padding: 6px 0;
      border-bottom: 1px dashed $color-border-lighter;
    }

    .order-item-row:last-child {
      border-bottom: none;
    }

    .left {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      min-width: 0;
    }

    .dish-image {
      width: 42px;
      height: 42px;
      border-radius: $border-radius-base;
      border: 1px solid $color-border-lighter;
      overflow: hidden;
      flex-shrink: 0;
    }

    .name {
      color: $color-text-primary;
      max-width: 220px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .right {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      margin-left: $spacing-md;
      flex-shrink: 0;
      color: $color-text-secondary;
      font-size: $font-size-sm;
    }

    .subtotal {
      color: $color-text-primary;
      font-weight: $font-weight-medium;
    }
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

.empty-text {
  color: $color-text-secondary;
  font-size: $font-size-sm;
}
</style>
