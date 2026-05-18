<template>
  <div class="cart-page">
    <!-- 头部 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <iconify-icon icon="lucide:arrow-left" class="text-xl"></iconify-icon>
      </button>
      <h1 class="page-title">购物车</h1>
      <button
        v-if="!cartStore.isEmpty"
        class="clear-btn"
        @click="clearCart"
      >
        清空
      </button>
    </div>

    <!-- 空购物车 -->
    <div class="empty-cart" v-if="cartStore.isEmpty">
      <div class="empty-icon">🛒</div>
      <p class="empty-text">购物车是空的</p>
      <button class="go-shop-btn" @click="goHome">去逛逛</button>
    </div>

    <!-- 购物车内容 -->
    <template v-else>
      <!-- 商家信息 -->
      <div class="merchant-info" v-if="cartStore.merchantInfo">
        <img :src="getImageUrl(cartStore.merchantInfo.logo)" class="merchant-logo" />
        <span class="merchant-name">{{ cartStore.merchantInfo.name }}</span>
        <iconify-icon icon="lucide:chevron-right"></iconify-icon>
      </div>

      <!-- 商品列表 -->
      <div class="cart-items">
        <div v-for="item in cartStore.items" :key="item.dishId" class="cart-item">
          <img :src="getImageUrl(item.image)" :alt="item.dishName" class="item-image" />
          <div class="item-info">
            <h4 class="item-name">{{ item.dishName }}</h4>
            <div class="item-price">
              <span class="current-price">¥{{ item.price }}</span>
              <span class="original-price" v-if="item.originalPrice">
                ¥{{ item.originalPrice }}
              </span>
            </div>
          </div>
          <div class="item-actions">
            <button
              class="qty-btn qty-minus"
              :disabled="cartBusy"
              @click="decreaseItem(item)"
            >
              <iconify-icon icon="lucide:minus"></iconify-icon>
            </button>
            <span class="quantity">{{ item.quantity }}</span>
            <button
              class="qty-btn qty-plus"
              :disabled="cartBusy"
              @click="increaseItem(item)"
            >
              <iconify-icon icon="lucide:plus"></iconify-icon>
            </button>
          </div>
        </div>
      </div>

      <!-- 价格明细 -->
      <div class="price-detail">
        <div class="price-row">
          <span>商品小计</span>
          <span>¥{{ cartStore.totalPrice.toFixed(2) }}</span>
        </div>
        <div class="price-row">
          <span>配送费</span>
          <span>¥{{ cartStore.deliveryFee.toFixed(2) }}</span>
        </div>
        <div class="price-row">
          <span>包装费</span>
          <span>¥{{ cartStore.packingFee.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="checkout-bar">
        <div class="total-info">
          <span class="total-label">合计</span>
          <span class="total-price">¥{{ cartStore.actualAmount.toFixed(2) }}</span>
        </div>
        <button
          class="checkout-btn"
          :class="{ disabled: !cartStore.reachMinOrder }"
          :disabled="!cartStore.reachMinOrder"
          @click="goCheckout"
        >
          {{ cartStore.reachMinOrder ? '去结算' : `还差¥${cartStore.amountToMinOrder.toFixed(2)}起送` }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const cartBusy = ref(false)

// 返回
const goBack = () => {
  router.back()
}

// 去首页
const goHome = () => {
  router.push('/student')
}

// 清空购物车
const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const uid = userStore.userId
    if (!uid) {
      ElMessage.warning('请先登录')
      return
    }
    await cartStore.clearCart(String(uid))
  } catch {
    // 用户取消
  }
}

const increaseItem = async (item) => {
  const uid = userStore.userId
  if (!uid) {
    ElMessage.warning('请先登录')
    return
  }
  const dish = {
    id: item.dishId,
    name: item.dishName,
    price: item.price,
    originalPrice: item.originalPrice,
    image: item.image,
    merchantId: cartStore.merchantId,
    isAvailable: true
  }
  cartBusy.value = true
  try {
    const result = await cartStore.addItem(dish, 1, String(uid))
    if (!result.success && result.message) {
      ElMessage.warning(result.message)
    }
  } finally {
    cartBusy.value = false
  }
}

const decreaseItem = async (item) => {
  const uid = userStore.userId
  if (!uid) {
    ElMessage.warning('请先登录')
    return
  }
  if (!item.id) {
    ElMessage.error('购物车数据异常，请刷新页面')
    return
  }
  cartBusy.value = true
  try {
    await cartStore.decreaseItem(item.id, String(uid))
  } finally {
    cartBusy.value = false
  }
}

// 去结算
const goCheckout = () => {
  router.push('/student/checkout')
}

onMounted(() => {
  const uid = userStore.userId
  if (uid) {
    cartStore.loadCart(String(uid))
  }
})

</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #fff7ed 0%, #f5f7fa 24%, #f5f7fa 100%);
  padding-bottom: 88px;
}

/* 头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #F2F6FC;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: #303133;
  cursor: pointer;
  border-radius: 50%;
}

.back-btn:hover {
  background: #F5F7FA;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.clear-btn {
  padding: 6px 12px;
  background: transparent;
  color: #F5222D;
  border: none;
  font-size: 14px;
  cursor: pointer;
  border-radius: 8px;
}

.clear-btn:hover {
  background: #FEF0F0;
}

/* 空购物车 */
.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  color: #909399;
  margin-bottom: 20px;
  font-size: 14px;
}

.go-shop-btn {
  padding: 10px 24px;
  background: #FF7A45;
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.go-shop-btn:hover {
  opacity: 0.9;
}

/* 商家信息 */
.merchant-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  margin: 16px;
  border-radius: 16px;
  border: 1px solid #F2F6FC;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.merchant-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
}

.merchant-name {
  flex: 1;
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

/* 商品列表 */
.cart-items {
  background: white;
  margin: 0 16px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #F2F6FC;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #303133;
}

.item-price .current-price {
  color: #FF7A45;
  font-weight: 500;
  font-size: 14px;
}

.item-price .original-price {
  font-size: 11px;
  color: #C0C4CC;
  text-decoration: line-through;
  margin-left: 4px;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.qty-minus {
  background: #F5F7FA;
  color: #606266;
}

.qty-plus {
  background: #FF7A45;
  color: white;
}

.qty-btn:hover:not(:disabled) {
  opacity: 0.8;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity {
  min-width: 24px;
  text-align: center;
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

/* 价格明细 */
.price-detail {
  background: white;
  margin: 16px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #F2F6FC;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  color: #909399;
}

/* 底部结算栏 */
.checkout-bar {
  position: fixed;
  bottom: 56px;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.08);
  border-top: 1px solid #F2F6FC;
}

.total-info .total-label {
  font-size: 13px;
  color: #909399;
  margin-right: 4px;
}

.total-info .total-price {
  font-size: 20px;
  font-weight: 700;
  color: #FF7A45;
}

.checkout-btn {
  padding: 10px 24px;
  background: #FF7A45;
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.checkout-btn:hover:not(.disabled) {
  opacity: 0.9;
}

.checkout-btn.disabled {
  background: #DCDFE6;
  cursor: not-allowed;
}
</style>
