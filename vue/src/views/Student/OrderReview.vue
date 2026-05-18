<template>
  <div class="review-page" v-if="order">
    <!-- 头部 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack">返回</el-button>
      <h1 class="page-title">{{ pageTitle }}</h1>
      <div></div>
    </div>

    <!-- 订单信息 -->
    <div class="order-info">
      <span class="merchant-name">{{ order.merchantName }}</span>
      <span class="order-no">订单号: {{ order.orderNo }}</span>
    </div>

    <!-- 评价表单 / 只读详情 -->
    <div class="review-form">
      <!-- 商家评分 -->
      <div class="rating-section">
        <div class="rating-label">
          <span class="label-icon">🏪</span>
          <span>商家评分</span>
        </div>
        <el-rate
          v-if="!isReadonly"
          v-model="reviewForm.merchantRating"
          :texts="ratingTexts"
          show-text
          size="large"
        />
        <el-rate
          v-else
          :model-value="existingReview.merchantRating"
          disabled
          show-text
          :texts="ratingTexts"
          size="large"
        />
      </div>

      <!-- 骑手评分 -->
      <div class="rating-section" v-if="order.riderId || order.riderName">
        <div class="rating-label">
          <span class="label-icon">🛵</span>
          <span>骑手评分<span v-if="order.riderName"> ({{ order.riderName }})</span></span>
        </div>
        <el-rate
          v-if="!isReadonly"
          v-model="reviewForm.riderRating"
          :texts="ratingTexts"
          show-text
          size="large"
        />
        <el-rate
          v-else
          :model-value="existingReview.riderRating ?? 5"
          disabled
          show-text
          :texts="ratingTexts"
          size="large"
        />
      </div>

      <!-- 评价内容 -->
      <div class="content-section">
        <div class="section-label">评价内容</div>
        <el-input
          v-if="!isReadonly"
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          placeholder="分享您的用餐体验..."
          maxlength="200"
          show-word-limit
        />
        <div v-else class="readonly-content">
          {{ existingReview.content || '（无文字评价）' }}
        </div>
      </div>

      <!-- 评价配图（编辑） -->
      <div class="upload-section" v-if="!isReadonly">
        <div class="section-label">上传实拍图<span class="label-hint">选填，最多 6 张</span></div>
        <el-upload
          class="review-uploader"
          action="http://localhost:8080/api/file/upload"
          list-type="picture-card"
          :limit="6"
          :on-success="handleUploadSuccess"
          :on-remove="handleUploadRemove"
          :before-upload="beforeReviewImage"
          accept="image/jpeg,image/png,image/gif,image/webp"
        >
          <el-icon class="upload-plus"><Plus /></el-icon>
        </el-upload>
      </div>

      <!-- 评价配图（详情） -->
      <div class="review-images-readonly" v-if="isReadonly && readonlyImageUrls.length">
        <div class="section-label">图片</div>
        <div class="img-grid">
          <el-image
            v-for="(u, i) in readonlyImageUrls"
            :key="i"
            class="grid-img"
            :src="u"
            :preview-src-list="readonlyImageUrls"
            :initial-index="i"
            fit="cover"
            preview-teleported
            hide-on-click-modal
          />
        </div>
      </div>

      <!-- 快捷标签 -->
      <div class="quick-tags" v-if="!isReadonly">
        <el-tag
          v-for="tag in quickTags"
          :key="tag"
          :type="selectedTags.includes(tag) ? 'success' : 'info'"
          effect="plain"
          @click="toggleTag(tag)"
        >
          {{ tag }}
        </el-tag>
      </div>

      <div class="review-meta" v-if="isReadonly && existingReview.createdAt">
        <span class="meta-text">评价时间：{{ formatDateTime(existingReview.createdAt) }}</span>
      </div>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-section" v-if="!isReadonly">
      <el-button
        type="success"
        size="large"
        class="submit-review-btn"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="submitReview"
      >
        提交评价
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '@/stores/order'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'
import { getImageUrl, parseReviewImages } from '@/utils/imageUrl'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

const order = ref(null)
const existingReview = ref(null)

const reviewForm = ref({
  merchantRating: 5,
  riderRating: 5,
  content: ''
})

const ratingTexts = ['很差', '较差', '一般', '满意', '非常满意']

const quickTags = [
  '味道好',
  '分量足',
  '包装精美',
  '配送快',
  '服务好',
  '性价比高'
]

const selectedTags = ref([])
const submitting = ref(false)
/** 已上传图片相对路径，提交时 JSON 存入后端 */
const reviewImageUrls = ref([])

const isReadonly = computed(() => !!existingReview.value)

const readonlyImageUrls = computed(() => {
  if (!existingReview.value?.images) return []
  return parseReviewImages(existingReview.value.images).map((u) => getImageUrl(u))
})

const pageTitle = computed(() => (isReadonly.value ? '评价详情' : '订单评价'))

const canSubmit = computed(() => {
  return reviewForm.value.merchantRating > 0
})

const goBack = () => {
  router.back()
}

const formatDateTime = (timeStr) => {
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

const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
  updateContent()
}

const updateContent = () => {
  if (selectedTags.value.length > 0) {
    reviewForm.value.content = selectedTags.value.join('，') + '。'
  }
}

const handleUploadSuccess = (response) => {
  if (response?.code === 200 && response?.data?.url) {
    reviewImageUrls.value.push(response.data.url)
  } else {
    ElMessage.error(response?.message || '上传失败')
  }
}

const handleUploadRemove = (file) => {
  const url = file.response?.data?.url
  if (url) {
    const i = reviewImageUrls.value.indexOf(url)
    if (i > -1) reviewImageUrls.value.splice(i, 1)
  }
}

const beforeReviewImage = (file) => {
  const ok = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!ok) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP')
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('单张图片不能超过 5MB')
    return false
  }
  if (reviewImageUrls.value.length >= 6) {
    ElMessage.warning('最多上传 6 张图片')
    return false
  }
  return true
}

const submitReview = async () => {
  submitting.value = true

  try {
    const result = await orderStore.submitReview({
      orderId: order.value.id,
      studentId: userStore.userId,
      merchantId: order.value.merchantId,
      riderId: order.value.riderId,
      merchantRating: reviewForm.value.merchantRating,
      riderRating: order.value.riderId ? reviewForm.value.riderRating : null,
      content: reviewForm.value.content,
      images:
        reviewImageUrls.value.length > 0
          ? JSON.stringify(reviewImageUrls.value)
          : null
    })

    if (result.success) {
      ElMessage.success('评价成功')
      if (userStore.userId) {
        await orderStore.loadStudentOrders(userStore.userId)
      }
      router.back()
    } else {
      ElMessage.error(result.message || '评价失败')
    }
  } catch (error) {
    ElMessage.error('评价失败，请重试')
  } finally {
    submitting.value = false
  }
}

const loadPage = async () => {
  const orderId = route.params.id
  await orderStore.fetchOrderDetail(orderId)
  order.value = orderStore.currentOrder

  if (!order.value) {
    return
  }

  try {
    const rev = await request.get(`/review/order/${orderId}`)
    if (rev) {
      existingReview.value = rev
      reviewForm.value = {
        merchantRating: rev.merchantRating ?? 5,
        riderRating: rev.riderRating ?? 5,
        content: rev.content || ''
      }
    }
  } catch (e) {
    // 未评价，可填表
  }
}

onMounted(() => {
  loadPage()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.review-page {
  min-height: 100vh;
  background: $color-bg-base;
  /* 本页弱化 Element Plus 默认主色蓝（输入框聚焦、星级焦点环等） */
  --el-input-focus-border-color: #909399;
  --el-input-hover-border-color: #c0c4cc;
}

// 头部
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-base;
  background: $color-bg-white;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
  box-shadow: 0 1px 0 $color-border-lighter;

  :deep(.el-button.is-text) {
    color: $color-text-primary;
    font-weight: $font-weight-medium;
  }

  :deep(.el-button.is-text:hover) {
    color: $color-text-primary;
    background: $color-bg-light;
  }

  .page-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }
}

// 订单信息
.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-base;
  background: $color-bg-white;
  margin: $spacing-base;
  border-radius: $border-radius-base;
  border: 1px solid $color-border-lighter;

  .merchant-name {
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    font-size: $font-size-md;
  }

  .order-no {
    font-size: $font-size-sm;
    color: $color-text-regular;
    font-weight: $font-weight-medium;
  }
}

// 评价表单
.review-form {
  background: $color-bg-white;
  margin: 0 $spacing-base;
  padding: $spacing-base;
  border-radius: $border-radius-base;
}

// 评分区域
.rating-section {
  padding: $spacing-base 0;
  border-bottom: 1px solid $color-border-lighter;

  &:last-of-type {
    border-bottom: none;
  }

  .rating-label {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    font-size: $font-size-base;

    .label-icon {
      font-size: 20px;
    }
  }

  :deep(.el-rate) {
    --el-rate-icon-size: 28px;
    --el-rate-fill-color: #f0a020;
    --el-rate-void-color: #c0c4cc;
    /* 右侧文案、半星小数部分，不用主色 */
    --el-rate-text-color: #{$color-text-regular};
    /* 键盘聚焦时不要用主色蓝描边 */
    --el-rate-outline-color: rgba(144, 147, 153, 0.45);
  }

  :deep(.el-rate__text) {
    color: $color-text-regular !important;
    font-weight: $font-weight-medium;
    font-size: $font-size-sm;
  }

  :deep(.el-rate__icon.is-active),
  :deep(.el-rate__decimal) {
    color: #f0a020 !important;
  }
}

// 内容区域
.content-section {
  padding: $spacing-base 0;

  .section-label {
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $spacing-md;

    .label-hint {
      margin-left: $spacing-sm;
      font-weight: $font-weight-normal;
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }
  }

  .readonly-content {
    padding: $spacing-md;
    min-height: 88px;
    line-height: $line-height-loose;
    color: $color-text-primary;
    font-size: $font-size-base;
    background: $color-bg-light;
    border-radius: $border-radius-base;
    border: 1px solid $color-border-lighter;
    white-space: pre-wrap;
    word-break: break-word;
  }

  :deep(.el-textarea__inner) {
    color: $color-text-primary;
    border-color: $color-border-base;

    &:hover {
      border-color: #c0c4cc;
    }

    &:focus {
      outline: none;
      border-color: #909399;
      box-shadow: 0 0 0 1px #909399 inset;
    }
  }

  :deep(.el-input__count) {
    color: $color-text-regular;
    background: transparent;
  }
}

.upload-section {
  padding: $spacing-base 0;
  border-top: 1px solid $color-border-lighter;

  .upload-plus {
    font-size: 22px;
    color: $color-text-secondary;
  }

  :deep(.review-uploader) {
    .el-upload--picture-card {
      --el-upload-picture-card-size: 88px;
      border-radius: $border-radius-base;
      border-color: $color-border-base;
      background: $color-bg-light;
    }

    .el-upload-list--picture-card .el-upload-list__item {
      width: 88px;
      height: 88px;
      border-radius: $border-radius-base;
    }
  }
}

.review-images-readonly {
  padding: $spacing-base 0 0;
  border-top: 1px solid $color-border-lighter;

  .img-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: $spacing-sm;
  }

  .grid-img {
    width: 100%;
    aspect-ratio: 1;
    border-radius: $border-radius-base;
    overflow: hidden;
    border: 1px solid $color-border-lighter;
    background: $color-bg-light;

    :deep(.el-image__inner) {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.review-meta {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1px dashed $color-border-lighter;

  .meta-text {
    font-size: $font-size-sm;
    color: $color-text-regular;
  }
}

// 快捷标签（未选：灰边深字；已选：绿色系，避免蓝底蓝字）
.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  padding: $spacing-base 0;

  :deep(.el-tag) {
    cursor: pointer;
    transition: all $transition-fast;
    font-weight: $font-weight-medium;

    &:hover {
      transform: scale(1.02);
    }
  }

  :deep(.el-tag--info) {
    --el-tag-text-color: #{$color-text-regular};
    --el-tag-border-color: #{$color-border-base};
    --el-tag-bg-color: #{$color-bg-white};
  }

  :deep(.el-tag--info:hover) {
    --el-tag-text-color: #{$color-text-primary};
    --el-tag-border-color: #{$color-text-secondary};
  }

  :deep(.el-tag--success) {
    --el-tag-text-color: #{$color-success-dark};
    --el-tag-border-color: #{$color-success};
    --el-tag-bg-color: rgba(82, 196, 26, 0.08);
  }
}

// 提交区域（success 主按钮，避免主色蓝）
.submit-section {
  padding: $spacing-lg $spacing-base;

  :deep(.submit-review-btn) {
    width: 100%;
    font-weight: $font-weight-semibold;
  }
}
</style>
