<template>
  <div class="statistics-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">经营统计</h1>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="loadStats"
      />
    </div>

    <!-- 核心指标 -->
    <div class="stat-cards">
      <StatCard
        :value="stats.todayOrders"
        label="订单数"
        suffix="单"
        :icon="Document"
        type="primary"
      />
      <StatCard
        :value="stats.todayRevenue"
        label="营业额"
        prefix="¥"
        :icon="Money"
        type="warning"
      />
      <StatCard
        :value="stats.avgRating"
        label="平均评分"
        :icon="Star"
        type="success"
        :format="false"
      />
      <StatCard
        :value="stats.completionRate"
        label="完成率"
        suffix="%"
        :icon="CircleCheck"
        type="primary"
        :format="false"
      />
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card">
        <h3 class="chart-title">订单趋势</h3>
        <LineChart
          :data="trendData"
          xField="date"
          :yField="['count', 'amount']"
          :legendNames="['订单数', '营业额']"
          :height="300"
        />
      </div>

      <div class="chart-card">
        <h3 class="chart-title">订单状态分布</h3>
        <PieChart
          :data="statusData"
          :height="300"
          :centerValue="stats.todayOrders"
          centerText="总订单"
        />
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="reviews-section">
      <h3 class="section-title">最新评价</h3>
      <div class="review-list">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <el-rate v-model="review.merchantRating" disabled size="small" />
            <span class="review-time">{{ formatTime(review.createdAt) }}</span>
          </div>
          <p class="review-content">{{ review.content || '用户未填写评价内容' }}</p>
        </div>
        <el-empty v-if="reviews.length === 0" description="暂无评价" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { Document, Money, Star, CircleCheck } from '@element-plus/icons-vue'
import StatCard from '@/components/charts/StatCard.vue'
import LineChart from '@/components/charts/LineChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import { useUserStore } from '@/stores/user'
import { useOrderStore } from '@/stores/order'
import request from '@/axios/request'

const userStore = useUserStore()
const orderStore = useOrderStore()

// 格式化日期为 yyyy-MM-dd
const formatDate = (date) => {
  const d = date instanceof Date ? date : new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 日期范围（默认为今天，使用 Date 对象）
const today = new Date()
const dateRange = ref([today, today])

// 统计数据
const stats = ref({
  todayOrders: 0,
  todayRevenue: 0,
  avgRating: 0,
  completionRate: 0
})

// 趋势数据
const trendData = ref([])

// 状态分布数据
const statusData = ref([])

// 评价列表
const reviews = ref([])

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 商家信息
const merchantInfo = ref(null)

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

// 加载趋势数据（最近7天，每日统计）
const loadTrendData = async (merchantId) => {
  try {
    const today = new Date()
    const trends = []

    // 获取最近7天的数据
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today)
      date.setDate(date.getDate() - i)
      const dateStr = formatDate(date)

      // 获取该天的统计数据
      const dayStats = await orderStore.getStats(merchantId, dateStr, dateStr)

      trends.push({
        date: `${date.getMonth() + 1}/${date.getDate()}`,
        count: dayStats.totalOrders || 0,
        amount: dayStats.totalRevenue || 0
      })
    }

    trendData.value = trends
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    trendData.value = []
  }
}

// 加载统计数据
const loadStats = async () => {
  const merchantId = await getMerchantId()
  if (!merchantId) return

  try {
    // 总是获取日期范围（默认为今天）
    let startDate = formatDate(new Date())
    let endDate = formatDate(new Date())

    // 如果用户选择了日期范围，使用用户选择的日期
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = formatDate(dateRange.value[0])
      endDate = formatDate(dateRange.value[1])
    }

    console.log('加载统计数据，日期范围:', startDate, '至', endDate)

    // 获取订单统计
    const orderStats = await orderStore.getStats(merchantId, startDate, endDate)

    // 获取商户信息以获取真实评分
    const merchant = await request.get(`/merchant/${merchantId}`)
    const realRating = merchant?.rating || 5.0

    stats.value = {
      todayOrders: orderStats.totalOrders || 0,
      todayRevenue: orderStats.totalRevenue || 0,
      avgRating: realRating,
      completionRate: orderStats.totalOrders > 0
        ? Math.round((orderStats.completedOrders / orderStats.totalOrders) * 100)
        : 100
    }

    // 状态分布
    statusData.value = [
      { name: '待接单', value: orderStats.pendingOrders || 0 },
      { name: '制作中', value: orderStats.preparingOrders || 0 },
      { name: '配送中', value: orderStats.deliveringOrders || 0 },
      { name: '已完成', value: orderStats.completedOrders || 0 }
    ].filter(item => item.value > 0)

    // 加载趋势数据（最近7天）
    await loadTrendData(merchantId)

    // 获取评价
    const reviewList = await request.get(`/review/merchant/${merchantId}`)
    reviews.value = (reviewList || []).slice(0, 5)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.statistics-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;

    .page-title {
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }
  }

  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-md;
    margin-bottom: $spacing-lg;
  }

  .charts-section {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: $spacing-md;
    margin-bottom: $spacing-lg;

    .chart-card {
      background: $color-bg-white;
      border-radius: $border-radius-md;
      padding: $spacing-lg;

      .chart-title {
        font-size: $font-size-md;
        font-weight: $font-weight-medium;
        margin-bottom: $spacing-md;
      }
    }
  }

  .reviews-section {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-lg;

    .section-title {
      font-size: $font-size-md;
      font-weight: $font-weight-medium;
      margin-bottom: $spacing-md;
    }

    .review-list {
      .review-item {
        padding: $spacing-md 0;
        border-bottom: 1px solid $color-border-lighter;

        &:last-child {
          border-bottom: none;
        }

        .review-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: $spacing-sm;

          .review-time {
            font-size: $font-size-xs;
            color: $color-text-secondary;
          }
        }

        .review-content {
          font-size: $font-size-sm;
          color: $color-text-regular;
          line-height: $line-height-base;
        }
      }
    }
  }
}
</style>
