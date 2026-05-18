<template>
  <div class="my-coupons-page">
    <div class="page-head">
      <h2>我的优惠券</h2>
      <el-radio-group v-model="status" size="small" @change="loadGroupedCoupons">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="claimed">未使用</el-radio-button>
        <el-radio-button label="used">已使用</el-radio-button>
        <el-radio-button label="expired">已过期</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="groupedCoupons.length" class="merchant-groups">
      <section
        v-for="group in groupedCoupons"
        :key="group.merchantId"
        class="merchant-group"
      >
        <div class="merchant-name">{{ group.merchantName }}</div>
        <div class="coupon-list">
          <div
            v-for="uc in group.coupons"
            :key="uc.id"
            class="coupon-item"
            :class="statusClass(uc.status)"
          >
            <div class="top">
              <span class="amount">减¥{{ Number(uc.coupon?.discountAmount || 0).toFixed(0) }}</span>
              <el-tag :type="tagType(uc.status)" size="small">{{ tagText(uc.status) }}</el-tag>
            </div>
            <div class="line">券名：{{ uc.coupon?.name || '优惠券' }}</div>
            <div class="line">门槛：满¥{{ Number(uc.coupon?.thresholdAmount || 0).toFixed(2) }}</div>
            <div class="line">有效期：{{ formatExpireTime(uc.expireAt) }}</div>
          </div>
        </div>
      </section>
    </div>
    <el-empty v-else description="暂无优惠券" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const userStore = useUserStore()
const groupedCoupons = ref([])
const status = ref('all')

const formatExpireTime = (time) => {
  if (!time) return '长期有效'
  return String(time).replace('T', ' ').slice(0, 16)
}

const tagText = (s) => {
  if (s === 'claimed') return '未使用'
  if (s === 'used') return '已使用'
  if (s === 'expired') return '已过期'
  return '未知'
}

const tagType = (s) => {
  if (s === 'claimed') return 'success'
  if (s === 'used') return 'info'
  if (s === 'expired') return 'warning'
  return ''
}

const statusClass = (s) => {
  if (s === 'used') return 'is-used'
  if (s === 'expired') return 'is-expired'
  return 'is-claimed'
}

const loadGroupedCoupons = async () => {
  if (!userStore.userId) return
  try {
    groupedCoupons.value = await request.get(`/coupon/user/grouped/${userStore.userId}`, {
      params: { status: status.value }
    })
  } catch (error) {
    groupedCoupons.value = []
    ElMessage.error('加载优惠券失败')
  }
}

onMounted(() => {
  loadGroupedCoupons()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.my-coupons-page {
  padding: $spacing-base;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
  margin-bottom: $spacing-base;

  h2 {
    margin: 0;
    font-size: $font-size-lg;
    color: $color-text-primary;
  }
}

.merchant-groups {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;
}

.merchant-group {
  background: $color-bg-white;
  border: 1px solid $color-border-extra-light;
  border-radius: $border-radius-base;
  padding: $spacing-base;
}

.merchant-name {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  margin-bottom: $spacing-sm;
}

.coupon-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: $spacing-sm;
}

.coupon-item {
  border-radius: $border-radius-sm;
  padding: $spacing-sm;
  border: 1px solid #dce6f5;
  background: #f8fbff;

  &.is-used {
    opacity: 0.7;
  }

  &.is-expired {
    opacity: 0.6;
    filter: grayscale(0.2);
  }
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.amount {
  color: #2f7dd3;
  font-weight: $font-weight-bold;
}

.line {
  font-size: $font-size-xs;
  color: $color-text-regular;
  line-height: 1.5;
}
</style>
