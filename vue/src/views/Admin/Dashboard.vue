<template>
  <div class="dashboard-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">数据看板</h1>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="loadData"
      />
    </div>

    <!-- 核心指标 -->
    <div class="stat-cards">
      <StatCard
        :value="stats.todayOrders"
        label="今日订单"
        suffix="单"
        :icon="Document"
        type="primary"
        :trend="stats.orderTrend"
      />
      <StatCard
        :value="stats.todayRevenue"
        label="今日营业额"
        prefix="¥"
        :icon="Money"
        type="warning"
        :trend="stats.revenueTrend"
      />
      <StatCard
        :value="stats.activeUsers"
        label="活跃用户"
        suffix="人"
        :icon="User"
        type="success"
        :trend="stats.userTrend"
      />
      <StatCard
        :value="stats.onlineRiders"
        label="在线骑手"
        suffix="人"
        :icon="Bicycle"
        type="primary"
      />
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card large">
        <h3 class="chart-title">订单趋势</h3>
        <LineChart
          :data="trendData"
          xField="date"
          :yField="['count', 'amount']"
          :legendNames="['订单数', '营业额']"
          :height="300"
        />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">商家营业额排名</h3>
        <BarChart
          :data="merchantRanking"
          xField="merchantName"
          yField="revenue"
          :horizontal="true"
          :height="300"
        />
      </div>
      <div class="chart-card">
        <h3 class="chart-title">订单状态分布</h3>
        <PieChart
          :data="statusDistribution"
          :height="300"
          :centerValue="stats.todayOrders"
          centerText="总订单"
        />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">下单高峰时段</h3>
        <BarChart
          :data="peakHours"
          xField="hour"
          yField="count"
          :height="250"
          color="#52C41A"
        />
      </div>
      <div class="chart-card">
        <h3 class="chart-title">骑手绩效排名</h3>
        <div class="rider-ranking">
          <div
            v-for="(rider, index) in riderRanking"
            :key="rider.id"
            class="ranking-item"
          >
            <span class="rank" :class="`rank-${index + 1}`">{{ index + 1 }}</span>
            <span class="name">{{ rider.name }}</span>
            <span class="value">{{ rider.deliveries }}单</span>
            <span class="time">平均{{ rider.avgTime }}分钟</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { Document, Money, User, Bicycle } from '@element-plus/icons-vue'
import StatCard from '@/components/charts/StatCard.vue'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import { useOrderStore } from '@/stores/order'
import request from '@/axios/request'

const orderStore = useOrderStore()

// 日期范围
const dateRange = ref([])

// 统计数据
const stats = ref({
  todayOrders: 0,
  todayRevenue: 0,
  activeUsers: 0,
  onlineRiders: 0,
  orderTrend: 12,
  revenueTrend: 8,
  userTrend: 5
})

// 趋势数据
const trendData = ref([])

// 商家排名
const merchantRanking = ref([])

// 状态分布
const statusDistribution = ref([])

// 高峰时段
const peakHours = ref([])

// 骑手排名
const riderRanking = ref([])

// 加载数据
const loadData = async () => {
  try {
    // 获取订单统计
    const orderStats = await orderStore.getStats()
    
    // 获取用户列表统计
    const users = await request.get('/user/list')
    const studentCount = users.filter(u => u.role === 'student').length
    const riderCount = users.filter(u => u.isRider).length
    
    stats.value = {
      todayOrders: orderStats.totalOrders || 0,
      todayRevenue: orderStats.totalRevenue || 0,
      activeUsers: studentCount,
      onlineRiders: riderCount,
      orderTrend: 12,
      revenueTrend: 8,
      userTrend: 5
    }

    // 趋势数据（模拟最近7天）
    const today = new Date()
    trendData.value = Array.from({ length: 7 }, (_, i) => {
      const date = new Date(today)
      date.setDate(date.getDate() - (6 - i))
      return {
        date: `${date.getMonth() + 1}/${date.getDate()}`,
        count: Math.floor(Math.random() * 50) + 20,
        amount: Math.floor(Math.random() * 2000) + 500
      }
    })

    // 商家排名
    const merchants = await request.get('/merchant/list')
    merchantRanking.value = (merchants || [])
      .sort((a, b) => (b.monthSales || 0) - (a.monthSales || 0))
      .slice(0, 5)
      .map(m => ({
        merchantName: m.name,
        revenue: (m.monthSales || 0) * 25
      }))

    // 状态分布
    statusDistribution.value = [
      { name: '待接单', value: orderStats.pendingOrders || 0 },
      { name: '制作中', value: orderStats.preparingOrders || 0 },
      { name: '配送中', value: orderStats.deliveringOrders || 0 },
      { name: '已完成', value: orderStats.completedOrders || 0 }
    ].filter(item => item.value > 0)

    // 高峰时段
    peakHours.value = [
      { hour: '10:00', count: 15 },
      { hour: '11:00', count: 45 },
      { hour: '12:00', count: 78 },
      { hour: '13:00', count: 52 },
      { hour: '17:00', count: 38 },
      { hour: '18:00', count: 65 },
      { hour: '19:00', count: 72 },
      { hour: '20:00', count: 48 }
    ]

    // 骑手排名（模拟数据）
    riderRanking.value = users
      .filter(u => u.isRider)
      .slice(0, 5)
      .map((r, i) => ({
        id: r.id,
        name: r.name,
        deliveries: 50 - i * 8,
        avgTime: 18 + Math.floor(Math.random() * 10)
      }))
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.dashboard-page {
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

  .charts-row {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-md;
    margin-bottom: $spacing-md;

    .chart-card {
      background: $color-bg-white;
      border-radius: $border-radius-md;
      padding: $spacing-lg;

      &.large {
        grid-column: span 2;
      }

      .chart-title {
        font-size: $font-size-md;
        font-weight: $font-weight-medium;
        margin-bottom: $spacing-md;
      }
    }
  }

  .rider-ranking {
    .ranking-item {
      display: flex;
      align-items: center;
      padding: $spacing-md 0;
      border-bottom: 1px solid $color-border-lighter;

      &:last-child {
        border-bottom: none;
      }

      .rank {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: $color-bg-light;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: $font-size-sm;
        font-weight: $font-weight-bold;
        margin-right: $spacing-md;

        &.rank-1 {
          background: #FFD700;
          color: white;
        }

        &.rank-2 {
          background: #C0C0C0;
          color: white;
        }

        &.rank-3 {
          background: #CD7F32;
          color: white;
        }
      }

      .name {
        flex: 1;
        font-weight: $font-weight-medium;
      }

      .value {
        color: $color-primary;
        font-weight: $font-weight-medium;
        margin-right: $spacing-lg;
      }

      .time {
        font-size: $font-size-sm;
        color: $color-text-secondary;
      }
    }
  }
}
</style>
