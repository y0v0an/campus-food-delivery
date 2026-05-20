<template>
  <div class="group-dish-card" @click="handleCardClick">
    <!-- 菜品图片 -->
    <img :src="getImageUrl(dish.image)" :alt="dish.name" class="dish-image" />

    <!-- 菜品信息 -->
    <div class="dish-info">
      <h4 class="dish-name">{{ dish.name }}</h4>
      <p class="merchant-name">{{ dish.merchant?.name }}</p>
      <div class="dish-meta">
        <span class="rating" v-if="dish.merchant?.rating">
          <iconify-icon icon="lucide:star" class="fill-current"></iconify-icon>
          {{ dish.merchant.rating }}
        </span>
        <span class="category">{{ dish.description }}</span>
      </div>

      <!-- 价格信息 -->
      <div class="price-info">
        <span class="group-price">¥{{ Number(dish.groupPrice || 0).toFixed(2) }}</span>
        <span class="original-price" v-if="dish.originalPrice && dish.originalPrice > dish.groupPrice">
          ¥{{ Number(dish.originalPrice).toFixed(2) }}
        </span>
        <span class="discount-tag" v-if="dish.discountRate && dish.discountRate < 1">
          {{ (dish.discountRate * 10).toFixed(1) }}折
        </span>
        <span class="save-tag" v-if="dish.originalPrice && dish.originalPrice > dish.groupPrice">
          省¥{{ (dish.originalPrice - dish.groupPrice).toFixed(2) }}
        </span>
      </div>

      <!-- 商家信息 -->
      <div class="merchant-info" v-if="dish.merchant">
        <span class="info-item" v-if="dish.merchant.distance">
          <iconify-icon icon="lucide:map-pin"></iconify-icon>
          {{ dish.merchant.distance }}m
        </span>
        <span class="info-item" v-else-if="dish.merchant.area">
          <iconify-icon icon="lucide:map-pin"></iconify-icon>
          {{ dish.merchant.area }}
        </span>
        <span class="info-item" v-if="dish.merchant.deliveryTime">
          <iconify-icon icon="lucide:clock"></iconify-icon>
          {{ dish.merchant.deliveryTime }}
        </span>
        <span class="info-item" v-if="dish.merchant.deliveryFee !== null">
          <iconify-icon icon="lucide:shopping-bag"></iconify-icon>
          配送¥{{ Number(dish.merchant.deliveryFee).toFixed(0) }}
        </span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <button class="start-group-btn" @click.stop="handleStartGroup">
      发起拼单
    </button>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { useRouter } from 'vue-router'
import { getImageUrl } from '@/utils/imageUrl'

const props = defineProps({
  dish: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['start-group'])

const router = useRouter()

const handleCardClick = () => {
  if (props.dish.merchant?.id) {
    router.push(`/student/merchant/${props.dish.merchant.id}`)
  }
}

const handleStartGroup = () => {
  emit('start-group', props.dish)
}
</script>

<style scoped>
.group-dish-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.group-dish-card:active {
  transform: scale(0.98);
}

.dish-image {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.dish-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 2px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-name {
  font-size: 12px;
  color: #909399;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: #FFB84D;
}

.category {
  font-size: 11px;
  color: #C0C4CC;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
}

.group-price {
  font-size: 18px;
  font-weight: 700;
  color: #FF7A45;
}

.original-price {
  font-size: 12px;
  color: #C0C4CC;
  text-decoration: line-through;
}

.discount-tag {
  padding: 2px 6px;
  background: #fff0f6;
  color: #ff4d4f;
  font-size: 10px;
  font-weight: 500;
  border-radius: 4px;
}

.save-tag {
  padding: 2px 6px;
  background: #f6ffed;
  color: #52c41a;
  font-size: 10px;
  font-weight: 500;
  border-radius: 4px;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: #909399;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 2px;
}

.start-group-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #FF7A45 0%, #FFB84D 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  align-self: center;
  flex-shrink: 0;
  transition: opacity 0.2s;
}

.start-group-btn:hover {
  opacity: 0.9;
}
</style>
