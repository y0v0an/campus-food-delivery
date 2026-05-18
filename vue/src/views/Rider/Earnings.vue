<template>
  <div class="earnings-page">
    <div class="earnings-overview">
      <div class="overview-header">
        <h1 class="page-title">我的收入</h1>
        <el-button text type="primary" @click="loadData">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      <div class="total-earnings">
        <span class="currency">¥</span>
        <span class="amount">{{ monthAmount }}</span>
      </div>
      <p class="earnings-label">本月配送费（累计）</p>
      <p class="earnings-sub">历史累计 ¥{{ formatMoney(earningsPayload.totalEarnings) }}</p>
    </div>

    <div class="stat-cards">
      <div class="stat-card stat-card--a">
        <span class="stat-value">{{ todayOrders }}</span>
        <span class="stat-label">今日单量</span>
      </div>
      <div class="stat-card stat-card--b">
        <span class="stat-value">¥{{ formatMoney(todayEarnings) }}</span>
        <span class="stat-label">今日收入</span>
      </div>
      <div class="stat-card stat-card--c">
        <span class="stat-value">{{ totalDeliveries }}</span>
        <span class="stat-label">累计配送</span>
      </div>
      <div class="stat-card stat-card--d">
        <span class="stat-value">{{ avgTime }}<span class="unit">分钟</span></span>
        <span class="stat-label">参考时效</span>
      </div>
    </div>

    <div class="rating-section">
      <h2 class="section-title">骑手评分</h2>
      <div class="rating-card">
        <div class="rating-main">
          <span class="rating-value">{{ ratingScoreText }}</span>
          <el-rate
            v-if="ratingCount > 0"
            :model-value="ratingScore"
            disabled
            size="large"
          />
          <div v-else class="rating-empty-hint">完成订单后，用户可对配送打分</div>
        </div>
        <p class="rating-desc">基于 {{ ratingCount }} 条对骑手的评价</p>
      </div>
    </div>

    <div class="trend-section">
      <h2 class="section-title">近7日配送费</h2>
      <div class="trend-chart">
        <LineChart
          v-if="trendData.length"
          :data="trendData"
          xField="date"
          yField="amount"
          :height="200"
          :showArea="true"
        />
        <el-empty v-else description="暂无数据" />
      </div>
    </div>

    <div class="reviews-section">
      <h2 class="section-title">用户评价</h2>
      <div class="review-list">
        <div v-for="item in reviewRows" :key="item.id" class="review-item">
          <div class="review-top">
            <span class="review-merchant">{{ item.merchantName }}</span>
            <span class="review-time">{{ formatReviewTime(item.createdAt) }}</span>
          </div>
          <div v-if="item.riderRating" class="review-stars">
            <span class="star-label">配送</span>
            <el-rate :model-value="item.riderRating" disabled size="small" />
          </div>
          <div v-else class="review-stars muted">本次未单独评价配送</div>
          <p class="review-content">
            {{ item.content?.trim() ? item.content : '用户未填写文字评价' }}
          </p>
        </div>
        <el-empty v-if="reviewRows.length === 0" description="暂无评价，完成配送并等待用户评价后将显示在这里" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import LineChart from '@/components/charts/LineChart.vue'
import { useUserStore } from '@/stores/user'
import { useOrderStore } from '@/stores/order'
import request from '@/axios/request'

const userStore = useUserStore()
const orderStore = useOrderStore()

const earningsPayload = ref({
  totalEarnings: 0,
  monthEarnings: 0,
  todayEarnings: 0
})
const todayOrders = ref(0)
const todayEarnings = ref(0)
const totalDeliveries = ref(0)
const avgTime = ref(20)
const rating = ref(5)
const ratingCount = ref(0)

const trendData = ref([])
const reviewRows = ref([])

const monthAmount = computed(() => formatMoney(earningsPayload.value.monthEarnings))

const ratingScore = computed(() => {
  const n = Number(rating.value)
  return Number.isFinite(n) ? Math.min(5, Math.max(0, n)) : 5
})

const ratingScoreText = computed(() => {
  if (ratingCount.value <= 0) return '—'
  return ratingScore.value.toFixed(1)
})

const formatMoney = (v) => {
  if (v == null || v === '') return '0.00'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

const formatReviewTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function buildLast7DaysTrend(orders) {
  const result = []
  const now = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setHours(0, 0, 0, 0)
    d.setDate(d.getDate() - i)
    const start = d.getTime()
    const end = start + 24 * 60 * 60 * 1000
    const label = `${d.getMonth() + 1}/${d.getDate()}`
    const amount = orders
      .filter((o) => o.status === 'completed' && o.deliveredAt)
      .filter((o) => {
        const t = new Date(o.deliveredAt).getTime()
        return t >= start && t < end
      })
      .reduce((s, o) => s + Number(o.deliveryFee || 0), 0)
    result.push({ date: label, amount: Math.round(amount * 100) / 100 })
  }
  return result
}

const loadData = async () => {
  const riderId = userStore.userId
  if (!riderId) return

  try {
    const earnings = await request.get(`/rider/earnings/${riderId}`)
    earningsPayload.value = {
      totalEarnings: earnings.totalEarnings ?? 0,
      monthEarnings: earnings.monthEarnings ?? 0,
      todayEarnings: earnings.todayEarnings ?? 0
    }
    todayEarnings.value = Number(earnings.todayEarnings) || 0
    todayOrders.value = Number(earnings.todayOrders) || 0
    totalDeliveries.value = Number(earnings.totalDeliveries) || 0
    avgTime.value = Number(earnings.avgTime) || 20
    rating.value = Number(earnings.rating) || 5
    ratingCount.value = Number(earnings.ratingCount) || 0

    await orderStore.loadRiderOrders(riderId)
    const orders = orderStore.orderList || []
    trendData.value = buildLast7DaysTrend(orders)

    const orderMap = Object.fromEntries(orders.map((o) => [o.id, o]))
    const reviews = await request.get(`/review/rider/${riderId}`)
    const list = Array.isArray(reviews) ? reviews : []
    reviewRows.value = list.slice(0, 20).map((r) => ({
      ...r,
      merchantName: orderMap[r.orderId]?.merchantName || '订单'
    }))
  } catch (error) {
    console.error('加载收益数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.earnings-page {
  padding: $spacing-base;
  min-height: 100%;
  background: linear-gradient(180deg, #eef6ff 0%, #f5f7fa 35%, #f5f7fa 100%);
}

.earnings-overview {
  background: linear-gradient(135deg, #3d8fd9 0%, #1e6bb8 55%, #155a9e 100%);
  border-radius: $border-radius-lg;
  padding: $spacing-lg $spacing-base $spacing-xl;
  color: white;
  text-align: center;
  margin-bottom: $spacing-md;
  box-shadow: 0 10px 28px rgba(30, 107, 184, 0.28);

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;

    .page-title {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
    }

    .el-button {
      color: white;
    }
  }

  .total-earnings {
    .currency {
      font-size: $font-size-xl;
      vertical-align: top;
      opacity: 0.95;
    }

    .amount {
      font-size: 44px;
      font-weight: $font-weight-bold;
      letter-spacing: -1px;
    }
  }

  .earnings-label {
    margin-top: $spacing-sm;
    opacity: 0.92;
    font-size: $font-size-sm;
  }

  .earnings-sub {
    margin-top: $spacing-xs;
    font-size: $font-size-xs;
    opacity: 0.75;
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;

  .stat-card {
    border-radius: $border-radius-md;
    padding: $spacing-md $spacing-base;
    text-align: center;
    border: 1px solid rgba(255, 255, 255, 0.85);
    box-shadow: $shadow-sm;

    &--a {
      background: linear-gradient(145deg, #fff 0%, #e8f4ff 100%);
    }
    &--b {
      background: linear-gradient(145deg, #fff 0%, #e8f8ef 100%);
    }
    &--c {
      background: linear-gradient(145deg, #fff 0%, #f3e8ff 100%);
    }
    &--d {
      background: linear-gradient(145deg, #fff 0%, #fff8e8 100%);
    }

    .stat-value {
      display: block;
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $color-primary;
      margin-bottom: $spacing-xs;

      .unit {
        font-size: $font-size-sm;
        font-weight: $font-weight-medium;
        margin-left: 2px;
      }
    }

    .stat-label {
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }
  }
}

.rating-section {
  margin-bottom: $spacing-lg;

  .section-title {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    margin-bottom: $spacing-md;
    color: $color-text-primary;
  }

  .rating-card {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-lg;
    text-align: center;
    border: 1px solid $color-border-extra-light;
    box-shadow: $shadow-sm;

    .rating-main {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: $spacing-sm;

      .rating-value {
        font-size: 40px;
        font-weight: $font-weight-bold;
        color: $color-warning;
        line-height: 1;
      }

      .rating-empty-hint {
        font-size: $font-size-sm;
        color: $color-text-secondary;
      }
    }

    .rating-desc {
      margin-top: $spacing-md;
      font-size: $font-size-sm;
      color: $color-text-secondary;
    }
  }
}

.trend-section {
  margin-bottom: $spacing-lg;

  .section-title {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    margin-bottom: $spacing-md;
  }

  .trend-chart {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-base;
    border: 1px solid $color-border-extra-light;
    box-shadow: $shadow-sm;
  }
}

.reviews-section {
  padding-bottom: $spacing-xl;

  .section-title {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    margin-bottom: $spacing-md;
  }

  .review-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  .review-item {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-base;
    border: 1px solid $color-border-extra-light;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    text-align: left;

    .review-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: $spacing-sm;
      margin-bottom: $spacing-sm;
    }

    .review-merchant {
      font-weight: $font-weight-medium;
      color: $color-text-primary;
      font-size: $font-size-base;
    }

    .review-time {
      font-size: $font-size-xs;
      color: $color-text-secondary;
      flex-shrink: 0;
    }

    .review-stars {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      margin-bottom: $spacing-sm;

      &.muted {
        font-size: $font-size-xs;
        color: $color-text-placeholder;
      }

      .star-label {
        font-size: $font-size-xs;
        color: $color-text-secondary;
        width: 32px;
      }
    }

    .review-content {
      font-size: $font-size-sm;
      color: $color-text-regular;
      line-height: $line-height-loose;
      margin: 0;
    }
  }
}
</style>
