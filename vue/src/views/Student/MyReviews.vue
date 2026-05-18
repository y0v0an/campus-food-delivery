<template>
  <div class="my-reviews-page">
    <div class="toolbar-sticky">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索商家、订单号、评价内容"
        class="search-input"
        @clear="keyword = ''"
      >
        <template #prefix>
          <el-icon class="search-ico"><Search /></el-icon>
        </template>
      </el-input>
      <p v-if="!loading && list.length > 0" class="toolbar-hint">
        <template v-if="keyword.trim()">
          找到 <strong>{{ filteredList.length }}</strong> 条
        </template>
        <template v-else>共 {{ list.length }} 条评价</template>
      </p>
    </div>

    <div class="review-list" v-if="filteredList.length > 0">
      <article
        v-for="item in filteredList"
        :key="item.reviewId"
        class="review-card"
        @click="goDetail(item.orderId)"
      >
        <!-- 顶部：订单内菜品（名称、菜品图、数量），非评价上传图 -->
        <div class="card-dishes" @click.stop>
          <div class="dishes-head">
            <span class="dishes-head-icon" aria-hidden="true">🍽️</span>
            <span class="dishes-head-title">订单菜品</span>
          </div>
          <template v-if="orderItemList(item).length > 0">
            <div class="dishes-scroll">
              <div
                v-for="(line, idx) in orderItemList(item)"
                :key="line.dishId + '-' + idx"
                class="dish-card"
              >
                <div class="dish-img-box">
                  <el-image
                    v-if="dishImageSrc(line)"
                    :src="dishImageSrc(line)"
                    fit="cover"
                    class="dish-img"
                    :preview-src-list="dishPreviewList(item)"
                    :initial-index="previewIndex(item, idx)"
                    preview-teleported
                    hide-on-click-modal
                  >
                    <template #error>
                      <div class="dish-img-fail">无图</div>
                    </template>
                  </el-image>
                  <div v-else class="dish-placeholder">
                    {{ (line.dishName || '餐').charAt(0) }}
                  </div>
                </div>
                <div class="dish-text">
                  <span class="dish-name">{{ line.dishName || '商品' }}</span>
                  <span class="dish-qty">×{{ line.quantity ?? 1 }}</span>
                  <span v-if="line.price != null" class="dish-price">
                    ¥{{ line.price }}
                  </span>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="dishes-empty">暂无菜品明细</div>
        </div>

        <div class="card-body">
          <div class="card-head">
            <div class="head-row">
              <span class="merchant-name">{{ item.merchantName || '商家' }}</span>
              <span class="order-pill">{{ item.orderNo || item.orderId }}</span>
            </div>
          </div>

          <div class="rating-bar">
            <div class="rating-chip">
              <span class="chip-label">商家</span>
              <el-rate :model-value="item.merchantRating" disabled size="small" />
            </div>
            <div class="rating-chip" v-if="item.riderRating != null">
              <span class="chip-label">骑手</span>
              <el-rate :model-value="item.riderRating" disabled size="small" />
            </div>
          </div>

          <p class="content-preview">{{ item.content || '（无文字评价）' }}</p>

          <div class="card-foot">
            <time class="time">{{ formatTime(item.createdAt) }}</time>
            <span class="link-more">
              评价详情
              <el-icon><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </article>
    </div>

    <div class="empty-state empty-search" v-else-if="!loading && list.length > 0 && keyword.trim()">
      <p class="empty-title">未找到相关评价</p>
      <p class="empty-desc">换个关键词试试</p>
      <el-button text type="primary" @click="keyword = ''">清空搜索</el-button>
    </div>

    <div class="empty-state" v-else-if="!loading && list.length === 0">
      <div class="empty-illu" aria-hidden="true">
        <span class="star">⭐</span>
      </div>
      <p class="empty-title">暂无评价记录</p>
      <p class="empty-desc">完成订单并评价后会显示在这里</p>
      <el-button type="primary" round class="empty-btn" @click="goOrders">去下单</el-button>
    </div>

    <div class="loading-mask" v-if="loading">
      <span class="loading-text">加载中…</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const userStore = useUserStore()

const list = ref([])
const loading = ref(true)
const keyword = ref('')

const orderItemList = (item) =>
  Array.isArray(item?.orderItems) ? item.orderItems : []

const dishImageSrc = (line) => {
  const u = getImageUrl(line?.dishImage)
  return u || ''
}

const dishPreviewList = (item) => {
  const urls = []
  for (const line of orderItemList(item)) {
    const u = dishImageSrc(line)
    if (u) urls.push(u)
  }
  return urls
}

const previewIndex = (item, idx) => {
  const lines = orderItemList(item)
  let n = 0
  for (let i = 0; i < idx && i < lines.length; i++) {
    if (dishImageSrc(lines[i])) n++
  }
  return dishImageSrc(lines[idx]) ? n : 0
}

const filteredList = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return list.value
  return list.value.filter((item) => {
    const dishHay = orderItemList(item)
      .map((d) => d.dishName)
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    const hay = [
      item.merchantName,
      item.orderNo,
      item.orderId,
      item.content,
      dishHay
    ]
      .filter((x) => x != null && x !== '')
      .map((x) => String(x).toLowerCase())
    return hay.some((s) => s.includes(k))
  })
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  if (Number.isNaN(d.getTime())) return String(timeStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

const loadList = async () => {
  if (!userStore.userId) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const data = await request.get(`/review/student/${userStore.userId}`)
    list.value = Array.isArray(data) ? data : []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

const goDetail = (orderId) => {
  if (orderId) {
    router.push(`/student/review/${orderId}`)
  }
}

const goOrders = () => {
  router.push('/student/orders')
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.my-reviews-page {
  min-height: 100vh;
  min-height: 100dvh;
  padding-bottom: calc($spacing-xl + env(safe-area-inset-bottom, 0px));
  background: #edf1f7;
}

.toolbar-sticky {
  position: sticky;
  top: 0;
  z-index: 20;
  padding: 10px $spacing-base 12px;
  margin: 0 0 6px;
  background: rgba(237, 241, 247, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.search-input {
  :deep(.el-input__wrapper) {
    border-radius: 14px;
    box-shadow: 0 2px 12px rgba(15, 23, 42, 0.06);
  }

  :deep(.el-input__inner) {
    color: #0f172a;
    font-size: 15px;
  }
}

.search-ico {
  color: #64748b;
  font-size: 18px;
}

.toolbar-hint {
  margin: 10px 4px 0;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;

  strong {
    color: #2563eb;
    font-weight: 700;
  }
}

.review-list {
  padding: 4px $spacing-base 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  box-shadow:
    0 1px 3px rgba(15, 23, 42, 0.06),
    0 12px 32px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  &:active {
    transform: scale(0.99);
  }
}

/* ========== 订单菜品区（与订单详情一致的数据来源） ========== */
.card-dishes {
  padding: 14px 14px 16px;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 48%);
  border-bottom: 1px solid #f1f5f9;
}

.dishes-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.dishes-head-icon {
  font-size: 16px;
  line-height: 1;
}

.dishes-head-title {
  font-size: 14px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.03em;
}

.dishes-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
  scroll-snap-type: x mandatory;

  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

.dish-card {
  flex: 0 0 auto;
  width: 108px;
  scroll-snap-align: start;
}

.dish-img-box {
  width: 108px;
  height: 108px;
  border-radius: 14px;
  overflow: hidden;
  background: #f1f5f9;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  margin-bottom: 8px;
}

.dish-img {
  width: 108px;
  height: 108px;
  display: block;

  :deep(.el-image__inner) {
    width: 108px;
    height: 108px;
    object-fit: cover;
  }
}

.dish-img-fail {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #94a3b8;
}

.dish-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
  color: #94a3b8;
  background: linear-gradient(145deg, #e2e8f0, #f1f5f9);
}

.dish-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 2px;
}

.dish-name {
  font-size: 12px;
  font-weight: 700;
  color: #334155;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.dish-qty {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
}

.dish-price {
  font-size: 11px;
  font-weight: 600;
  color: #ea580c;
}

.dishes-empty {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  background: #fafafa;
  border-radius: 12px;
  border: 1px dashed #e2e8f0;
}

/* ========== 正文 ========== */
.card-body {
  padding: 16px 16px 14px;
}

.card-head {
  margin-bottom: 12px;
}

.head-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.merchant-name {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.02em;
  line-height: 1.3;
}

.order-pill {
  display: inline-block;
  padding: 5px 11px;
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  background: #f1f5f9;
  border-radius: 999px;
}

.rating-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.rating-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #f1f5f9;

  .chip-label {
    font-size: 12px;
    font-weight: 700;
    color: #94a3b8;
    min-width: 28px;
  }

  :deep(.el-rate) {
    --el-rate-icon-size: 15px;
    --el-rate-fill-color: #f59e0b;
    --el-rate-void-color: #e2e8f0;
  }
}

.content-preview {
  margin: 0 0 14px;
  font-size: 15px;
  line-height: 1.7;
  color: #334155;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.card-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.time {
  font-size: 12px;
  color: #94a3b8;
  font-variant-numeric: tabular-nums;
}

.link-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 700;
  color: #2563eb;

  .el-icon {
    font-size: 14px;
  }
}

.empty-state {
  margin: 28px $spacing-lg 0;
  padding: 40px 24px;
  text-align: center;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.06);
}

.empty-search {
  padding: 32px 24px;
}

.empty-illu .star {
  font-size: 56px;
  filter: drop-shadow(0 4px 12px rgba(245, 158, 11, 0.35));
}

.empty-title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 700;
  color: #334155;
}

.empty-desc {
  margin: 0 0 24px;
  font-size: 13px;
  line-height: 1.6;
  color: #94a3b8;
}

.empty-search .empty-desc {
  margin-bottom: 12px;
}

.empty-btn {
  min-width: 140px;
  font-weight: 600;
}

.loading-mask {
  padding: 48px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.loading-text {
  font-weight: 500;
}
</style>
