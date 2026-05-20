<template>
  <div class="merchant-detail" v-if="merchant">
    <!-- 商家头部信息 -->
    <div class="merchant-header">
      <button class="back-btn" @click="goBack">
        <iconify-icon icon="lucide:arrow-left" class="text-xl"></iconify-icon>
      </button>
      <img :src="getImageUrl(merchant.logo)" :alt="merchant.name" class="merchant-logo" />
      <div class="merchant-info">
        <h1 class="merchant-name">{{ merchant.name }}</h1>
        <div class="merchant-meta">
          <span class="rating">
            <iconify-icon icon="lucide:star" class="fill-current"></iconify-icon>
            {{ merchant.rating }}
          </span>
          <span class="sales">月售{{ merchant.monthSales }}</span>
          <span
            v-if="!merchant.isOpen"
            class="status-tag status-closed"
          >
            休息中
          </span>
        </div>
        <p class="merchant-desc">{{ merchant.description }}</p>
        <div class="merchant-tags">
          <span class="tag">{{ merchant.deliveryTime }}</span>
          <span class="tag">配送¥{{ merchant.deliveryFee }}</span>
          <span class="tag">¥{{ merchant.minOrder }}起送</span>
        </div>
      </div>
    </div>

    <!-- 优惠券区域 -->
    <div class="coupon-section" v-if="coupons.length">
      <div class="section-title">🎟️ 店铺优惠券</div>
      <div class="coupon-list">
        <div class="coupon-item" v-for="coupon in coupons" :key="coupon.id">
          <div class="coupon-main">
            <div class="coupon-name">{{ coupon.name || '优惠券' }}</div>
            <div class="discount">减¥{{ Number(coupon.discountAmount || 0).toFixed(0) }}</div>
            <div class="rule">满{{ Number(coupon.thresholdAmount || 0).toFixed(0) }}可用</div>
            <div class="expire">至{{ formatExpireTime(coupon.expireAt) }}</div>
          </div>
          <button
            class="claim-btn"
            :class="{ disabled: !canClaimCoupon(coupon) }"
            :disabled="!canClaimCoupon(coupon)"
            @click="claimCoupon(coupon)"
          >
            {{ claimedCouponIds.has(coupon.id) ? '已领' : (coupon.remainCount > 0 ? '领' : '空') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 拼单区域 -->
    <div class="group-section" v-if="groupOrders.length">
      <div class="section-title">👥 正在拼单</div>
      <div class="group-list">
        <div class="group-item" v-for="go in groupOrders" :key="go.id">
          <div class="group-main">
            <div class="name">{{ go.dishName }}</div>
            <div class="desc">
              {{ go.currentCount }}/{{ go.targetCount }}人 · 拼单价¥{{ go.unitPrice }}
              <span v-if="getDishOriginPrice(go.dishId) > Number(go.unitPrice || 0)" class="cut">
                立减¥{{ (getDishOriginPrice(go.dishId) - Number(go.unitPrice || 0)).toFixed(2) }}
              </span>
            </div>
            <div class="expire">发起人：{{ go.initiatorName }}</div>
            <div class="expire" v-if="go.expireAt">截止：{{ formatExpireTime(go.expireAt) }}</div>
          </div>
          <div class="group-actions">
            <button
              v-if="isGroupInitiator(go)"
              class="cancel-btn"
              @click="confirmCancelGroup(go)"
            >
              取消拼单
            </button>
            <button
              v-else
              class="join-btn"
              :class="{ disabled: !canJoinGroup(go) }"
              :disabled="!canJoinGroup(go)"
              @click="joinGroupOrder(go)"
            >
              参与拼单
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分类和菜品 -->
    <div class="menu-container">
      <!-- 左侧分类 -->
      <div class="category-nav">
        <div
          v-for="(category, index) in categories"
          :key="category"
          class="category-item"
          :class="{ active: activeCategory === index }"
          @click="scrollToCategory(index)"
        >
          {{ category }}
        </div>
      </div>

      <!-- 右侧菜品列表 -->
      <div class="dish-list" ref="dishListRef">
        <div
          v-for="(category, index) in categories"
          :key="category"
          class="category-section"
          :ref="el => categoryRefs[index] = el"
        >
          <h3 class="category-title">{{ category }}</h3>
          <div class="dishes">
            <div
              v-for="dish in dishesByCategory[category]"
              :key="dish.id"
              class="dish-item"
            >
              <img :src="getImageUrl(dish.image)" :alt="dish.name" class="dish-image" />
              <div class="dish-info">
                <h4 class="dish-name">{{ dish.name }}</h4>
                <p class="dish-desc">{{ dish.description }}</p>
                <div class="dish-footer">
                  <div class="dish-price">
                    <span class="current-price">¥{{ dish.price }}</span>
                    <span class="original-price" v-if="dish.originalPrice">
                      ¥{{ dish.originalPrice }}
                    </span>
                  </div>
                  <div class="dish-actions">
                    <template v-if="dish.isAvailable">
                      <button
                        v-if="getCartQuantity(dish.id) > 0"
                        class="qty-btn qty-minus"
                        @click="decreaseFromCart(dish)"
                      >
                        <iconify-icon icon="lucide:minus"></iconify-icon>
                      </button>
                      <span class="quantity" v-if="getCartQuantity(dish.id) > 0">
                        {{ getCartQuantity(dish.id) }}
                      </span>
                      <button class="qty-btn qty-plus" @click="addToCart(dish)">
                        <iconify-icon icon="lucide:plus"></iconify-icon>
                      </button>
                      <button
                        v-if="dish.isGroupEnabled"
                        class="group-btn"
                        @click="startGroupOrder(dish)"
                      >
                        发起拼单
                      </button>
                    </template>
                    <span v-else class="sold-out">已售罄</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 拼单对话框 -->
    <el-dialog
      v-model="groupOrderDialogVisible"
      title="发起拼单"
      width="90%"
      :style="{ maxWidth: '400px' }"
    >
      <div v-if="currentDish">
        <div class="dish-info-preview">
          <img :src="getImageUrl(currentDish.image)" :alt="currentDish.name" class="preview-image" />
          <div class="preview-details">
            <h4>{{ currentDish.name }}</h4>
            <p class="preview-price">
              拼单价：¥{{ Number(currentDish.groupPrice || currentDish.price).toFixed(2) }}
            </p>
            <p class="preview-delivery-fee">
              配送费：¥{{ groupDeliveryFeePerPerson }}/人
            </p>
            <p class="preview-total">
              预计总价：¥{{ (Number(currentDish.groupPrice || currentDish.price) + Number(groupDeliveryFeePerPerson)).toFixed(2) }}
            </p>
            <p class="preview-rule">
              {{ Number(currentDish.groupTargetCount || 2) }}人成团 · {{ Number(currentDish.groupDurationMinutes || 30) }}分钟内成团
            </p>
            <p class="preview-hint">
              <iconify-icon icon="lucide:info" class="icon"></iconify-icon>
              拼单仅支持外送，商家接单后将生成配送订单
            </p>
          </div>
        </div>

        <!-- 优惠券选择 -->
        <div v-if="availableCoupons.length > 0" class="coupon-section">
          <div class="section-title">🎫 可用优惠券 ({{ availableCoupons.length }})</div>
          <div class="coupon-list">
            <div
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              class="coupon-item"
              :class="{ selected: selectedCoupon?.id === coupon.id }"
              @click="selectedCoupon = coupon"
            >
              <div class="coupon-header">
                <span class="coupon-discount">{{ (coupon.discountRate * 10).toFixed(1) }}折</span>
                <span v-if="selectedCoupon?.id === coupon.id" class="selected-tag">✓ 已选</span>
              </div>
              <div class="coupon-info">
                <span>最少{{ coupon.minPeople }}人</span>
                <span>有效期至 {{ formatDate(coupon.validUntil) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 地址选择 -->
        <AddressSelector v-model="selectedAddress" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="groupOrderDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateGroupOrder">确认发起</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 参与拼单对话框 -->
    <el-dialog
      v-model="joinDialogVisible"
      title="参与拼单"
      width="90%"
      :style="{ maxWidth: '400px' }"
    >
      <div v-if="joiningOrder">
        <p class="join-hint">确定要参与「{{ joiningOrder.dishName }}」的拼单吗？</p>
        <div class="join-info">
          <span>当前进度：{{ joiningOrder.currentCount }}/{{ joiningOrder.targetCount }}人</span>
        </div>
        <AddressSelector v-model="selectedAddress" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="joinDialogVisible = false; selectedAddress = null">再想想</el-button>
          <el-button type="primary" @click="confirmJoinGroup">确定参与</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'
import { getAvailableGroupCoupons } from '@/api/groupCoupon'
import AddressSelector from '@/components/common/AddressSelector.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 商家信息
const merchant = ref(null)

// 菜品列表
const dishes = ref([])
const coupons = ref([])
const groupOrders = ref([])

// 用户已领取的优惠券ID列表
const claimedCouponIds = ref(new Set())

// 拼单优惠券
const availableCoupons = ref([])
const selectedCoupon = ref(null)

// 拼单对话框
const groupOrderDialogVisible = ref(false)
const currentDish = ref(null)
const selectedAddress = ref(null)
const targetCount = ref(2)

// 参与拼单对话框
const joinDialogVisible = ref(false)
const joiningOrder = ref(null)

// 菜品按分类
const dishesByCategory = computed(() => {
  const result = {}
  dishes.value.forEach(dish => {
    const category = dish.category || '其他'
    if (!result[category]) {
      result[category] = []
    }
    result[category].push(dish)
  })
  return result
})

// 分类列表
const categories = computed(() => Object.keys(dishesByCategory.value))

// 计算拼单平摊后的配送费
const groupDeliveryFeePerPerson = computed(() => {
  if (!currentDish.value || !merchant.value) return 0
  const peopleCount = Number(currentDish.value.groupTargetCount || 2)
  const merchantDeliveryFee = Number(merchant.value.deliveryFee || 0)
  const totalFee = merchantDeliveryFee + peopleCount
  return (totalFee / peopleCount).toFixed(2)
})

// 当前激活的分类
const activeCategory = ref(0)

// 分类元素引用
const categoryRefs = ref([])
const dishListRef = ref(null)

// 返回
const goBack = () => {
  router.back()
}

// 滚动到分类
const scrollToCategory = (index) => {
  activeCategory.value = index
  const el = categoryRefs.value[index]
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 获取购物车中的数量
const getCartQuantity = (dishId) => {
  return cartStore.getItemQuantity(dishId)
}

// 添加到购物车
const addToCart = async (dish) => {
  const userId = userStore.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  const dishWithMerchant = { ...dish, merchantId: merchant.value.id }
  const result = await cartStore.addItem(dishWithMerchant, 1, String(userId))

  if (result.needClear) {
    try {
      await ElMessageBox.confirm(
        '购物车中已有其他商家的商品，是否清空购物车？',
        '提示',
        { confirmButtonText: '清空并添加', cancelButtonText: '取消', type: 'warning' }
      )
      await cartStore.clearCart(String(userId))
      await cartStore.addItem(dishWithMerchant, 1, String(userId))
    } catch {
      // 用户取消
    }
  } else if (!result.success) {
    ElMessage.warning(result.message)
  }
}

// 从购物车减少
const decreaseFromCart = (dish) => {
  const item = cartStore.items.find(i => i.dishId === dish.id)
  if (item) {
    cartStore.decreaseItem(item.id, userStore.userId)
  }
}

// 加载商家信息
const loadMerchant = async (merchantId) => {
  try {
    merchant.value = await request.get(`/merchant/${merchantId}`)
  } catch (error) {
    console.error('加载商家信息失败:', error)
    ElMessage.error('加载商家信息失败')
  }
}

// 加载菜品列表
const loadDishes = async (merchantId) => {
  try {
    dishes.value = (await request.get(`/dish/list/${merchantId}`)) || []
  } catch (error) {
    console.error('加载菜品列表失败:', error)
  }
}

const formatExpireTime = (time) => {
  if (!time) return '长期有效'
  return String(time).replace('T', ' ').slice(0, 10)
}

const canClaimCoupon = (coupon) => {
  if (!userStore.userId) return false
  if (!coupon || !coupon.isEnabled) return false
  if (Number(coupon.remainCount || 0) <= 0) return false
  // 检查是否已领取
  if (claimedCouponIds.value.has(coupon.id)) return false
  if (!coupon.expireAt) return true
  return new Date(coupon.expireAt).getTime() >= Date.now()
}

const loadCoupons = async (merchantId) => {
  try {
    const all = await request.get(`/coupon/merchant/${merchantId}`)
    // 过滤掉过期、禁用、库存为0的券
    coupons.value = all.filter(c => {
      if (!c.isEnabled) return false
      if (Number(c.remainCount || 0) <= 0) return false
      if (!c.expireAt) return true
      return new Date(c.expireAt).getTime() >= Date.now()
    })
  } catch (error) {
    coupons.value = []
  }
}

// 加载用户已领取的优惠券ID列表
const loadUserClaimedCoupons = async () => {
  if (!userStore.userId) return
  try {
    const result = await request.get(`/coupon/user/grouped/${userStore.userId}`)
    // 提取所有已领取的优惠券ID
    const ids = new Set()
    result.forEach(group => {
      group.coupons.forEach(uc => {
        if (uc.status === 'claimed') {
          ids.add(uc.couponId)
        }
      })
    })
    claimedCouponIds.value = ids
  } catch (error) {
    claimedCouponIds.value = new Set()
  }
}

const loadGroupOrders = async (merchantId) => {
  try {
    groupOrders.value = await request.get(`/group-order/open/${merchantId}`)
  } catch (error) {
    groupOrders.value = []
  }
}

const canJoinGroup = (groupOrder) => {
  if (!userStore.userId) return false
  if (!groupOrder) return false
  if (groupOrder.expireAt && new Date(groupOrder.expireAt).getTime() < Date.now()) return false
  return groupOrder.status !== 'accepted' && Number(groupOrder.currentCount || 0) < Number(groupOrder.targetCount || 0)
}

const loadAvailableCoupons = async (dishId) => {
  try {
    const merchantId = route.params.id
    const res = await getAvailableGroupCoupons({
      merchantId,
      dishId
    })
    availableCoupons.value = res.data?.availableCoupons || []
    selectedCoupon.value = res.data?.bestCoupon || null
  } catch (err) {
    console.error('加载优惠券失败', err)
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return String(date).replace('T', ' ').slice(0, 10)
}

const startGroupOrder = async (dish) => {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    return
  }
  currentDish.value = dish
  targetCount.value = Number(dish.groupTargetCount || 2)
  // 加载可用优惠券
  await loadAvailableCoupons(dish.id)
  groupOrderDialogVisible.value = true
}

const handleCreateGroupOrder = async () => {
  // 验证地址
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    const dishId = currentDish.value.id
    const remark = ''
    await request.post('/group-order/create', {
      userId: userStore.userId,
      dishId,
      targetCount: targetCount.value,
      remark,
      couponId: selectedCoupon.value?.id,
      address: {
        building: selectedAddress.value.building,
        room: selectedAddress.value.room,
        contact: selectedAddress.value.contact,
        phone: selectedAddress.value.phone
      }
    })
    ElMessage.success('拼单已发起')
    groupOrderDialogVisible.value = false
    selectedAddress.value = null // 重置地址选择
    loadGroupOrders(merchant.value.id)
  } catch (error) {
    // handled by interceptor
  }
}

const getDishOriginPrice = (dishId) => {
  const dish = dishes.value.find(d => d.id === dishId)
  return Number(dish?.price || 0)
}

const joinGroupOrder = async (groupOrder) => {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    return
  }

  // 显示参与拼单对话框
  joiningOrder.value = groupOrder
  joinDialogVisible.value = true
}

// 确认参与拼单
const confirmJoinGroup = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    await request.post('/group-order/join', {
      groupOrderId: joiningOrder.value.id,
      userId: userStore.userId,
      address: {
        building: selectedAddress.value.building,
        room: selectedAddress.value.room,
        contact: selectedAddress.value.contact,
        phone: selectedAddress.value.phone
      }
    })
    ElMessage.success('参与拼单成功')
    joinDialogVisible.value = false
    selectedAddress.value = null
    loadGroupOrders(merchant.value.id)
  } catch (error) {
    // interceptor handles message
  }
}

// 判断当前用户是否为拼单发起者
const isGroupInitiator = (groupOrder) => {
  return userStore.userId && groupOrder && groupOrder.initiatorId === userStore.userId
}

// 取消拼单确认弹窗
const confirmCancelGroup = async (groupOrder) => {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要取消「${groupOrder.dishName}」的拼单吗？取消后已参与的成员将被通知，且无法恢复。`,
      '取消拼单',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    await cancelGroupOrder(groupOrder.id)
  } catch (error) {
    // 用户取消操作
  }
}

// 取消拼单
const cancelGroupOrder = async (groupOrderId) => {
  try {
    await request.post(`/group-order/cancel/${groupOrderId}`, {
      userId: userStore.userId,
      cancelReason: 'initiator_cancel'
    })
    ElMessage.success('拼单已取消')
    loadGroupOrders(merchant.value.id)
  } catch (error) {
    // interceptor handles message
  }
}

const claimCoupon = async (coupon) => {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await request.post('/coupon/claim', {
      userId: userStore.userId,
      couponId: coupon.id
    })
    claimedCouponIds.value.add(coupon.id) // 标记为已领取
    ElMessage.success('领取成功，可在下单时使用')
    loadCoupons(merchant.value.id)
  } catch (error) {
    // handled by interceptor
  }
}

onMounted(async () => {
  const merchantId = route.params.id
  await loadMerchant(merchantId)
  await loadDishes(merchantId)
  await loadCoupons(merchantId)
  await loadGroupOrders(merchantId)
  await loadUserClaimedCoupons()
  // 初始化购物车
  if (userStore.userId) {
    cartStore.loadCart(userStore.userId)
  }

  // 处理从拼单列表跳转过来的发起拼单请求
  const startGroupDishId = route.query.startGroup
  if (startGroupDishId) {
    // 找到对应的菜品
    const dish = dishes.value.find(d => d.id === startGroupDishId)
    if (dish) {
      // 等待下一帧确保菜品数据已加载
      setTimeout(() => {
        startGroupOrder(dish)
      }, 100)
    }
  }
})
</script>

<style scoped>
.merchant-detail {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #fff7ed 0%, #f5f7fa 30%, #f5f7fa 100%);
}

/* 商家头部 */
.merchant-header {
  position: relative;
  background: linear-gradient(135deg, #FF7A45 0%, #FA541C 100%);
  padding: 60px 16px 20px;
  color: white;
}

.back-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.merchant-logo {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  object-fit: cover;
  margin-bottom: 12px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.merchant-name {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.merchant-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 13px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 2px;
}

.merchant-desc {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 8px;
  line-height: 1.5;
}

.merchant-tags {
  display: flex;
  gap: 8px;
  font-size: 12px;
}

.merchant-tags .tag {
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

/* 优惠券区域 */
.coupon-section {
  margin: 16px;
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.coupon-list {
  display: flex;
  align-items: stretch;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.coupon-list::-webkit-scrollbar {
  display: none;
}

.coupon-item {
  flex: 0 0 calc((100% - 36px) / 4);
  min-width: 80px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid #e8eef6;
  border-radius: 12px;
  padding: 8px;
  background: linear-gradient(180deg, #fbfdff 0%, #f4f8fd 100%);
}

.coupon-main {
  min-width: 0;
}

.coupon-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.discount {
  font-size: 18px;
  font-weight: 700;
  color: #FF7A45;
  line-height: 1.2;
}

.rule {
  font-size: 11px;
  color: #606266;
  margin-top: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.expire {
  font-size: 10px;
  color: #909399;
  margin-top: 3px;
  line-height: 1.2;
}

.claim-btn {
  margin-top: 8px;
  width: 100%;
  height: 24px;
  padding: 0;
  font-size: 12px;
  border-radius: 12px;
  background: #FF7A45;
  color: white;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
}

.claim-btn:hover {
  opacity: 0.9;
}

.claim-btn.disabled {
  background: #b8c4d6;
  cursor: not-allowed;
}

/* 拼单区域 */
.group-section {
  margin: 16px;
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #EBEEF5;
  border-radius: 12px;
  padding: 12px 16px;
}

.name {
  font-weight: 500;
  color: #303133;
}

.desc {
  font-size: 13px;
  color: #FF7A45;
  margin-top: 2px;
}

.cut {
  margin-left: 8px;
  color: #F5222D;
}

.expire {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.join-btn {
  padding: 6px 12px;
  background: #FF7A45;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.join-btn:hover {
  opacity: 0.9;
}

.join-btn.disabled {
  background: #d8d8d8;
  cursor: not-allowed;
}

.group-actions {
  display: flex;
  align-items: center;
}

.cancel-btn {
  padding: 6px 12px;
  background: #F5222D;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.cancel-btn:hover {
  opacity: 0.85;
}

/* 菜单容器 */
.menu-container {
  display: flex;
  height: calc(100vh - 220px);
}

/* 左侧分类导航 */
.category-nav {
  width: 80px;
  background: white;
  overflow-y: auto;
  flex-shrink: 0;
}

.category-item {
  padding: 12px 8px;
  font-size: 13px;
  color: #909399;
  text-align: center;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.15s;
}

.category-item.active {
  background: #F5F7FA;
  color: #FF7A45;
  border-left-color: #FF7A45;
  font-weight: 500;
}

/* 右侧菜品列表 */
.dish-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.category-section {
  margin-bottom: 20px;
}

.category-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #EBEEF5;
}

.dishes {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.dish-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.dish-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.dish-desc {
  font-size: 11px;
  color: #909399;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.dish-price .current-price {
  font-size: 16px;
  font-weight: 700;
  color: #FF7A45;
}

.dish-price .original-price {
  font-size: 11px;
  color: #C0C4CC;
  text-decoration: line-through;
  margin-left: 4px;
}

.dish-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-btn {
  width: 24px;
  height: 24px;
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

.qty-btn:hover {
  opacity: 0.8;
}

.quantity {
  min-width: 20px;
  text-align: center;
  font-weight: 500;
  font-size: 14px;
}

.group-btn {
  padding: 4px 10px;
  background: #FFB84D;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 11px;
  cursor: pointer;
}

.sold-out {
  font-size: 11px;
  color: #909399;
  padding: 4px 8px;
  background: #F5F7FA;
  border-radius: 4px;
}

/* 滚动条样式 */
.category-nav::-webkit-scrollbar,
.dish-list::-webkit-scrollbar {
  width: 4px;
}

.category-nav::-webkit-scrollbar-thumb,
.dish-list::-webkit-scrollbar-thumb {
  background: #DCDFE6;
  border-radius: 2px;
}

/* 拼单对话框 */
.dish-info-preview {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.preview-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.preview-details h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #303133;
}

.preview-price {
  margin: 4px 0;
  font-size: 14px;
  color: #FF7A45;
  font-weight: 600;
}

.preview-delivery-fee {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
}

.preview-total {
  margin: 6px 0;
  font-size: 15px;
  color: #FF7A45;
  font-weight: 700;
}

.preview-hint {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #ecf5ff;
  padding: 6px 10px;
  border-radius: 6px;
}

.preview-hint .icon {
  flex-shrink: 0;
}

.preview-rule {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #909399;
}

/* 优惠券选择器 */
.coupon-section {
  margin: 15px 0;
}

.section-title {
  font-weight: 600;
  margin-bottom: 10px;
  color: #606266;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coupon-item {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.coupon-item:hover {
  border-color: #409eff;
}

.coupon-item.selected {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.coupon-discount {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.selected-tag {
  color: #67c23a;
  font-weight: 600;
}

.coupon-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

/* 参与拼单对话框 */
.join-hint {
  font-size: 14px;
  color: #303133;
  margin: 0 0 12px 0;
}

.join-info {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 16px;
}
</style>
