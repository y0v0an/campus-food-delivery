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

        <!-- 悬浮评分 -->
        <div class="mag-rating">
          <span class="mag-rating-value">{{ merchant.rating }}</span>
          <iconify-icon icon="lucide:star" class="mag-rating-star" />
        </div>
      </div>

      <!-- 右侧信息区 -->
      <div class="mag-info">
        <!-- 商家名称 -->
        <h3 class="mag-name">{{ merchant.name }}</h3>

        <!-- 月售信息 -->
        <p class="mag-sales">月售 {{ merchant.monthSales }} 单</p>

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
          <div class="mag-dishes-scroll-wrapper">
            <div class="mag-dishes-scroll">
              <div
                v-for="dish in merchant.dishes"
                :key="dish.id"
                class="mag-dish-item"
              >
                <img :src="getImageUrl(dish.image)" :alt="dish.name" class="mag-dish-img" />
                <span class="mag-dish-name">{{ dish.name }}</span>
                <span class="mag-dish-price">¥{{ dish.price }}</span>
              </div>
            </div>
            <!-- 右侧渐变提示 -->
            <div class="mag-scroll-hint"></div>
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

/* 评分徽章 */
.mag-rating {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #fef08a, #fde047);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4px 12px rgba(253, 224, 71, 0.4),
    0 0 0 3px #fff;
}

.mag-rating-value {
  font-size: 16px;
  font-weight: 800;
  color: #a16207;
  line-height: 1;
}

.mag-rating-star {
  position: absolute;
  width: 12px;
  height: 12px;
  color: #a16207;
  fill: currentColor;
  bottom: 8px;
  right: 10px;
}

/* 右侧信息区 */
.mag-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-left: 8px;
}

.mag-name {
  font-family: system-ui, -apple-system, sans-serif;
  font-size: 22px;
  font-weight: 900;
  color: #0a0a0a;
  line-height: 1.1;
  letter-spacing: -0.03em;
  margin-bottom: 4px;
}

.mag-sales {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  margin-bottom: 16px;
}

/* 标签 */
.mag-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.mag-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 12px;
  font-size: 12px;
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
  padding-top: 12px;
}

.mag-dishes-title {
  font-size: 12px;
  font-weight: 700;
  color: #6b7280;
  margin-bottom: 10px;
}

.mag-dishes-scroll-wrapper {
  position: relative;
}

.mag-dishes-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 4px 4px 12px 4px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.mag-dishes-scroll::-webkit-scrollbar {
  display: none;
}

.mag-scroll-hint {
  position: absolute;
  top: 4px;
  right: 0;
  bottom: 12px;
  width: 40px;
  background: linear-gradient(to right, transparent, #fff);
  pointer-events: none;
}

.mag-dish-item {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 80px;
  scroll-snap-align: start;
}

.mag-dish-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 12px;
  background: #f3f4f6;
}

.mag-dish-name {
  font-size: 12px;
  font-weight: 500;
  color: #374151;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.mag-dish-price {
  font-size: 14px;
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

  .mag-rating {
    width: 36px;
    height: 36px;
    top: -4px;
    right: -4px;
  }

  .mag-rating-value {
    font-size: 14px;
  }

  .mag-rating-star {
    width: 10px;
    bottom: 6px;
    right: 8px;
  }

  .mag-name {
    font-size: 16px;
  }

  .mag-sales {
    font-size: 12px;
    margin-bottom: 10px;
  }

  .mag-tags {
    gap: 6px;
    margin-bottom: 10px;
  }

  .mag-tag {
    padding: 6px 10px;
    font-size: 11px;
  }

  .mag-dishes {
    display: block;
    margin-top: 10px;
  }

  .mag-dishes-title {
    font-size: 11px;
    margin-bottom: 8px;
  }

  .mag-dishes-scroll {
    gap: 10px;
    padding: 4px 4px 10px 4px;
  }

  .mag-scroll-hint {
    width: 30px;
    background: linear-gradient(to right, transparent, rgba(255,255,255,0.9));
  }

  .mag-dish-item {
    width: 70px;
  }

  .mag-dish-img {
    width: 70px;
    height: 70px;
    border-radius: 10px;
  }

  .mag-dish-name {
    font-size: 11px;
  }

  .mag-dish-price {
    font-size: 13px;
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
