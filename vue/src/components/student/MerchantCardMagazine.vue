<template>
  <div class="mag-card" @click="goToMerchant(merchant.id)">
    <!-- 背景装饰 -->
    <div class="mag-bg">
      <div class="mag-circle mag-circle-1"></div>
      <div class="mag-circle mag-circle-2"></div>
      <div class="mag-grid"></div>
    </div>

    <!-- 主要内容 -->
    <div class="mag-main">
      <!-- 左侧大图区 -->
      <div class="mag-visual">
        <div class="mag-image-container">
          <img :src="getImageUrl(merchant.logo)" :alt="merchant.name" class="mag-image" />
          <div class="mag-overlay"></div>
        </div>
      </div>

      <!-- 右侧信息区 -->
      <div class="mag-info">
        <!-- 商家名称 -->
        <h3 class="mag-name">{{ merchant.name }}</h3>

        <!-- 评分和月售 -->
        <div class="mag-meta-row">
          <div class="mag-rating-inline">
            <iconify-icon icon="lucide:star" class="mag-star-inline" />
            <span>{{ merchant.rating }}</span>
          </div>
          <span class="mag-divider">·</span>
          <span class="mag-sales">月售 {{ merchant.monthSales }}</span>
        </div>

        <!-- 信息标签 -->
        <div class="mag-tags">
          <div class="mag-tag mag-tag-time">
            <iconify-icon icon="lucide:timer" />
            <span>{{ merchant.deliveryTime }}</span>
          </div>
          <div class="mag-tag mag-tag-fee">
            <iconify-icon icon="lucide:bike" />
            <span>¥{{ merchant.deliveryFee }}</span>
          </div>
          <div class="mag-tag mag-tag-min">
            <span>¥{{ merchant.minOrder }}起</span>
          </div>
        </div>

        <!-- 热销菜品预览 -->
        <div v-if="merchant.dishes && merchant.dishes.length > 0" class="mag-dishes">
          <div class="mag-dishes-title">🔥 热销菜品</div>
          <div class="mag-dishes-scroll">
            <div
              v-for="dish in merchant.dishes"
              :key="dish.id"
              class="mag-dish-item"
              @click.stop="goToMerchant(merchant.id)"
            >
              <img :src="getImageUrl(dish.image)" :alt="dish.name" class="mag-dish-img" />
              <span class="mag-dish-name">{{ dish.name }}</span>
              <span class="mag-dish-price">¥{{ dish.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部装饰线 -->
    <div class="mag-divider"></div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { getImageUrl } from '@/utils/imageUrl'

const props = defineProps({
  merchant: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const goToMerchant = (merchantId) => {
  router.push(`/student/merchant/${merchantId}`)
}
</script>

<style scoped>
.mag-card {
  position: relative;
  background: #fff;
  border-radius: 32px;
  overflow: hidden;
  cursor: pointer;
  margin-bottom: 24px;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
}

.mag-card:hover {
  transform: translateY(-8px);
  box-shadow:
    0 32px 64px -12px rgba(249, 115, 22, 0.25),
    0 0 0 1px rgba(249, 115, 22, 0.1);
}

.mag-card:active {
  transform: translateY(-4px);
}

/* 背景装饰 */
.mag-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.mag-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.15;
}

.mag-circle-1 {
  width: 200px;
  height: 200px;
  background: #f97316;
  top: -50px;
  right: -50px;
  animation: float 8s ease-in-out infinite;
}

.mag-circle-2 {
  width: 150px;
  height: 150px;
  background: #fb923c;
  bottom: -30px;
  left: -30px;
  animation: float 6s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(10px, 20px); }
}

.mag-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 0, 0, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.02) 1px, transparent 1px);
  background-size: 20px 20px;
}

/* 主内容区 */
.mag-main {
  position: relative;
  display: flex;
  gap: 0;
  padding: 20px;
  z-index: 1;
}

/* 左侧视觉区 */
.mag-visual {
  position: relative;
  width: 120px;
  flex-shrink: 0;
}

.mag-image-container {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 24px;
  overflow: hidden;
  background: #f3f4f6;
}

.mag-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.mag-card:hover .mag-image {
  transform: scale(1.1);
}

.mag-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    transparent 0%,
    rgba(0, 0, 0, 0.3) 100%
  );
}

/* 右侧信息区 */
.mag-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-left: 8px;
  min-width: 0;
}

.mag-name {
  font-family: system-ui, -apple-system, sans-serif;
  font-size: 16px;
  font-weight: 900;
  color: #0a0a0a;
  line-height: 1.1;
  letter-spacing: -0.03em;
  margin-bottom: 3px;
}

/* 评分和月售行 */
.mag-meta-row {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 6px;
}

.mag-rating-inline {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  background: #fffbeb;
  color: #b45309;
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
}

.mag-star-inline {
  width: 9px;
  height: 9px;
  fill: currentColor;
  color: #f59e0b;
}

.mag-divider {
  color: #e5e7eb;
  font-size: 10px;
}

.mag-sales {
  font-size: 10px;
  color: #6b7280;
  font-weight: 500;
}

/* 标签 */
.mag-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.mag-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 9px;
  font-weight: 700;
}

.mag-tag-time {
  background: #fff7ed;
  color: #ea580c;
}

.mag-tag-fee {
  background: #eff6ff;
  color: #2563eb;
}

.mag-tag-min {
  background: #f3f4f6;
  color: #4b5563;
}

/* 热销菜品 */
.mag-dishes {
  margin-top: auto;
  padding-top: 6px;
  width: 100%;
}

.mag-dishes-title {
  font-size: 9px;
  font-weight: 700;
  color: #f97316;
  margin-bottom: 6px;
}

.mag-dishes-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.mag-dishes-scroll::-webkit-scrollbar {
  display: none;
}

.mag-dish-item {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 65px;
  cursor: pointer;
}

.mag-dish-img {
  width: 65px;
  height: 65px;
  object-fit: cover;
  border-radius: 10px;
  background: #f3f4f6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.mag-dish-name {
  font-size: 11px;
  font-weight: 500;
  color: #374151;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.mag-dish-price {
  font-size: 13px;
  font-weight: 700;
  color: #f97316;
}

/* 分割线 */
.mag-divider {
  height: 3px;
  background: linear-gradient(
    90deg,
    #f97316 0%,
    #eab308 25%,
    #22c55e 50%,
    #3b82f6 75%,
    #8b5cf6 100%
  );
  background-size: 200% 100%;
  animation: gradientMove 3s linear infinite;
}

@keyframes gradientMove {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 响应式 */
@media (max-width: 640px) {
  .mag-card {
    border-radius: 20px;
    margin-bottom: 16px;
  }

  .mag-main {
    flex-direction: row;
    padding: 16px;
    gap: 12px;
  }

  .mag-visual {
    width: 90px;
  }

  .mag-image-container {
    width: 90px;
    border-radius: 18px;
  }

  .mag-name {
    font-size: 14px;
    margin-bottom: 2px;
  }

  .mag-meta-row {
    margin-bottom: 4px;
    gap: 4px;
  }

  .mag-rating-inline {
    font-size: 9px;
    padding: 1px 4px;
  }

  .mag-star-inline {
    width: 8px;
    height: 8px;
  }

  .mag-sales {
    font-size: 9px;
    margin-bottom: 0;
  }

  .mag-tags {
    gap: 3px;
    margin-bottom: 6px;
  }

  .mag-tag {
    padding: 3px 6px;
    font-size: 8px;
  }

  .mag-dishes {
    display: block;
    margin-top: 6px;
  }

  .mag-dishes-title {
    font-size: 8px;
    margin-bottom: 4px;
  }

  .mag-dishes-scroll {
    gap: 6px;
  }

  .mag-dish-item {
    width: 55px;
    gap: 3px;
  }

  .mag-dish-img {
    width: 55px;
    height: 55px;
    border-radius: 8px;
  }

  .mag-dish-name {
    font-size: 10px;
  }

  .mag-dish-price {
    font-size: 12px;
  }
}

/* 超小屏幕适配 */
@media (max-width: 375px) {
  .mag-main {
    padding: 12px;
  }

  .mag-visual {
    width: 80px;
  }

  .mag-image-container {
    width: 80px;
  }

  .mag-name {
    font-size: 15px;
  }

  .mag-tag {
    padding: 5px 8px;
    font-size: 10px;
  }
}
</style>
