<template>
  <div class="checkout-page">
    <!-- 头部 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <iconify-icon icon="lucide:arrow-left" class="text-xl"></iconify-icon>
      </button>
      <h1 class="page-title">确认订单</h1>
      <div></div>
    </div>

    <!-- 取餐方式 -->
    <div class="section serve-mode-section">
      <div class="section-label">取餐方式</div>
      <div class="serve-mode-group">
        <label class="serve-mode-option" :class="{ active: serveMode === 'delivery' }">
          <input type="radio" v-model="serveMode" value="delivery" />
          <span>外卖配送</span>
        </label>
        <label class="serve-mode-option" :class="{ active: serveMode === 'dine_in' }">
          <input type="radio" v-model="serveMode" value="dine_in" />
          <span>店内就餐（到店取餐，无配送费）</span>
        </label>
      </div>
    </div>

    <!-- 配送地址（仅外卖） -->
    <div
      v-show="serveMode === 'delivery'"
      class="section address-section"
      @click="showAddressDialog = true"
    >
      <div class="section-icon">📍</div>
      <div class="section-content" v-if="selectedAddress">
        <div class="address-main">
          {{ selectedAddress.building }} {{ selectedAddress.room }}
        </div>
        <div class="address-contact">
          {{ selectedAddress.contact }} {{ selectedAddress.phone }}
        </div>
      </div>
      <div class="section-content" v-else>
        <div class="address-placeholder">请选择配送地址</div>
      </div>
      <iconify-icon icon="lucide:chevron-right"></iconify-icon>
    </div>

    <!-- 店内就餐说明 -->
    <div v-show="serveMode === 'dine_in'" class="section dine-in-hint">
      <div class="section-icon">🍽️</div>
      <div class="section-content">
        <div class="hint-title">到店取餐</div>
        <p class="hint-text">无需骑手配送，商家出餐完成后请到门店取餐，并在订单页点击「确认取餐」。</p>
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="section goods-section">
      <div class="merchant-name">{{ cartStore.merchantInfo?.name }}</div>
      <div class="goods-list">
        <div v-for="item in cartStore.items" :key="item.dishId" class="goods-item">
          <div class="goods-thumb">
            <img
              v-if="item.image"
              :src="getImageUrl(item.image)"
              :alt="item.dishName"
            />
            <div v-else class="goods-thumb-placeholder">{{ item.dishName?.charAt(0) || '餐' }}</div>
          </div>
          <div class="goods-main">
            <span class="goods-name">{{ item.dishName }}</span>
            <div class="goods-meta">
              <span class="goods-quantity">x{{ item.quantity }}</span>
              <span class="goods-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 备注 -->
    <div class="section remark-section">
      <div class="section-label">备注</div>
      <textarea
        v-model="remark"
        class="remark-input"
        placeholder="口味、忌口等要求（选填）"
        rows="2"
        maxlength="100"
      ></textarea>
    </div>

    <!-- 优惠券 -->
    <div class="section coupon-section" @click="showCouponDialog = true">
      <div class="section-label">优惠券</div>
      <div class="coupon-value" v-if="selectedCoupon">
        <span class="coupon-name">{{ selectedCoupon.coupon?.name }}</span>
        <span class="coupon-discount">-¥{{ Number(selectedCoupon.coupon?.discountAmount || 0).toFixed(2) }}</span>
      </div>
      <div class="coupon-value placeholder" v-else>
        {{ availableCoupons.length > 0 ? '请选择优惠券' : '暂无可用优惠券' }}
      </div>
      <iconify-icon icon="lucide:chevron-right"></iconify-icon>
    </div>

    <!-- 价格明细 -->
    <div class="section price-section">
      <div class="price-row">
        <span>商品小计</span>
        <span>¥{{ cartStore.totalPrice.toFixed(2) }}</span>
      </div>
      <div class="price-row">
        <span>配送费</span>
        <span>¥{{ effectiveDeliveryFee.toFixed(2) }}</span>
      </div>
      <div class="price-row">
        <span>包装费</span>
        <span>¥{{ effectivePackingFee.toFixed(2) }}</span>
      </div>
      <div class="price-row discount" v-if="couponDiscount > 0">
        <span>优惠券</span>
        <span>-¥{{ couponDiscount.toFixed(2) }}</span>
      </div>
      <div class="price-row total">
        <span>合计</span>
        <span class="total-price">¥{{ effectiveActualAmount.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 支付方式 -->
    <div class="section payment-section">
      <div class="section-label">支付方式</div>
      <div class="payment-options">
        <label class="payment-option" :class="{ active: paymentMethod === 'wechat' }">
          <input type="radio" v-model="paymentMethod" value="wechat" />
          <span class="payment-icon">💳</span>
          <span class="payment-text">微信支付</span>
        </label>
        <label class="payment-option" :class="{ active: paymentMethod === 'alipay' }">
          <input type="radio" v-model="paymentMethod" value="alipay" />
          <span class="payment-icon">💰</span>
          <span class="payment-text">支付宝</span>
        </label>
      </div>
    </div>

    <!-- 底部提交栏 -->
    <div class="submit-bar">
      <div class="total-info">
        <span class="total-label">实付</span>
        <span class="total-price">¥{{ effectiveActualAmount.toFixed(2) }}</span>
      </div>
      <button
        class="submit-btn"
        :class="{ disabled: serveMode === 'delivery' && !selectedAddress }"
        :disabled="serveMode === 'delivery' && !selectedAddress || submitting"
        @click="showPaymentConfirm"
      >
        {{ submitting ? '提交中...' : '提交订单' }}
      </button>
    </div>

    <!-- 支付确认弹窗 -->
    <div v-if="showPaymentDialog" class="modal-overlay" @click="showPaymentDialog = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>确认支付</h3>
        </div>
        <div class="modal-body">
          <div class="payment-amount">
            <span class="label">支付金额</span>
            <span class="amount">¥{{ effectiveActualAmount.toFixed(2) }}</span>
          </div>
          <div class="payment-method-info">
            <span class="label">支付方式</span>
            <span class="method">{{ paymentMethod === 'wechat' ? '微信支付' : '支付宝' }}</span>
          </div>
          <div class="payment-tips">
            <iconify-icon icon="lucide:info"></iconify-icon>
            <span>请确认支付信息，点击确认支付后将创建订单</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="showPaymentDialog = false">取消</button>
          <button class="btn btn-confirm" :disabled="submitting" @click="confirmPayment">
            {{ submitting ? '支付中...' : '确认支付' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 地址选择弹窗 -->
    <div v-if="showAddressDialog" class="modal-overlay" @click="showAddressDialog = false">
      <div class="modal-content modal-address" @click.stop>
        <div class="modal-header">
          <h3>选择配送地址</h3>
        </div>
        <div class="modal-body">
          <div class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="address-item"
              :class="{ active: selectedAddress?.id === addr.id }"
              @click="selectAddress(addr)"
            >
              <div class="address-info">
                <div class="address-main">{{ addr.building }} {{ addr.room }}</div>
                <div class="address-contact">{{ addr.contact }} {{ addr.phone }}</div>
              </div>
              <span v-if="addr.isDefault" class="tag-default">默认</span>
            </div>
          </div>
          <div class="add-address" @click="showNewAddressForm = true">
            <iconify-icon icon="lucide:plus"></iconify-icon>
            新增地址
          </div>
          <div v-if="showNewAddressForm" class="new-address-form">
            <input v-model="newAddress.building" placeholder="楼栋（如：学生公寓1号楼）" class="input-field" />
            <input v-model="newAddress.room" placeholder="房间号（如：301）" class="input-field" />
            <input v-model="newAddress.contact" placeholder="联系人姓名" class="input-field" />
            <input v-model="newAddress.phone" placeholder="联系电话" class="input-field" />
            <button class="btn btn-primary" @click="saveNewAddress">保存地址</button>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="showAddressDialog = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 优惠券选择弹窗 -->
    <div v-if="showCouponDialog" class="modal-overlay" @click="showCouponDialog = false">
      <div class="modal-content modal-coupon" @click.stop>
        <div class="modal-header">
          <h3>选择优惠券</h3>
        </div>
        <div class="modal-body">
          <div class="coupon-list">
            <div
              class="coupon-item"
              :class="{ active: !selectedCoupon }"
              @click="selectCoupon(null)"
            >
              <div class="coupon-main">
                <div class="coupon-title">不使用优惠券</div>
              </div>
            </div>
            <div
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              class="coupon-item"
              :class="{ active: selectedCoupon?.id === coupon.id }"
              @click="selectCoupon(coupon)"
            >
              <div class="coupon-main">
                <div class="coupon-title">{{ coupon.coupon?.name }}</div>
                <div class="coupon-meta">
                  满{{ Number(coupon.coupon?.thresholdAmount || 0).toFixed(2) }}减{{ Number(coupon.coupon?.discountAmount || 0).toFixed(2) }}
                </div>
              </div>
              <div class="coupon-expire">到期：{{ formatExpireTime(coupon.expireAt) }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="showCouponDialog = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useOrderStore } from '@/stores/order'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const router = useRouter()
const cartStore = useCartStore()
const orderStore = useOrderStore()
const userStore = useUserStore()

// 配送地址
const addresses = ref([])
const selectedAddress = ref(null)
const showAddressDialog = ref(false)
const showNewAddressForm = ref(false)
const newAddress = ref({
  building: '',
  room: '',
  contact: '',
  phone: ''
})

// 备注
const remark = ref('')
const availableCoupons = ref([])
const selectedCoupon = ref(null)
const showCouponDialog = ref(false)

// 取餐方式：delivery 外卖 | dine_in 店内就餐
const serveMode = ref('delivery')

const effectiveDeliveryFee = computed(() =>
  serveMode.value === 'dine_in' ? 0 : Number(cartStore.deliveryFee) || 0
)
const effectivePackingFee = computed(() =>
  serveMode.value === 'dine_in' ? 0 : Number(cartStore.packingFee) || 0
)
const effectiveActualAmount = computed(
  () => Math.max(0, cartStore.totalPrice + effectiveDeliveryFee.value + effectivePackingFee.value - couponDiscount.value)
)
const couponDiscount = computed(() => Number(selectedCoupon.value?.coupon?.discountAmount || 0))

// 支付方式
const paymentMethod = ref('wechat')

// 提交状态
const submitting = ref(false)

// 支付确认弹窗
const showPaymentDialog = ref(false)

// 返回
const goBack = () => {
  router.back()
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/img/')) return `http://localhost:8080${url}`
  return url
}

// 加载地址列表
const loadAddresses = async () => {
  if (!userStore.userId) return
  try {
    const result = await request.get(`/address/${userStore.userId}`)
    addresses.value = result || []
    const defaultAddr = addresses.value.find(a => a.isDefault)
    if (defaultAddr && !selectedAddress.value) {
      selectedAddress.value = defaultAddr
      cartStore.setDeliveryAddress(defaultAddr)
    }
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

// 选择地址
const selectAddress = (addr) => {
  selectedAddress.value = addr
  cartStore.setDeliveryAddress(addr)
  showAddressDialog.value = false
}

const formatExpireTime = (time) => {
  if (!time) return '长期有效'
  return String(time).replace('T', ' ').slice(0, 16)
}

const loadAvailableCoupons = async () => {
  const merchantId = cartStore.merchantId
  if (!merchantId || !cartStore.totalPrice) {
    availableCoupons.value = []
    selectedCoupon.value = null
    return
  }
  try {
    const result = await request.get(`/coupon/user/${userStore.userId}`, {
      params: {
        merchantId,
        orderAmount: cartStore.totalPrice
      }
    })
    availableCoupons.value = result || []
    if (selectedCoupon.value) {
      const latest = availableCoupons.value.find(c => c.id === selectedCoupon.value.id)
      selectedCoupon.value = latest || null
    }
  } catch (error) {
    availableCoupons.value = []
    selectedCoupon.value = null
  }
}

const selectCoupon = (coupon) => {
  selectedCoupon.value = coupon
  showCouponDialog.value = false
}

// 保存新地址
const saveNewAddress = async () => {
  if (!newAddress.value.building || !newAddress.value.room ||
      !newAddress.value.contact || !newAddress.value.phone) {
    ElMessage.warning('请填写完整地址信息')
    return
  }

  try {
    const addr = await request.post('/address/add', {
      userId: userStore.userId,
      ...newAddress.value,
      isDefault: addresses.value.length === 0
    })

    addresses.value.push(addr)
    selectAddress(addr)
    showNewAddressForm.value = false
    newAddress.value = { building: '', room: '', contact: '', phone: '' }
    ElMessage.success('地址添加成功')
  } catch (error) {
    ElMessage.error('添加地址失败')
  }
}

// 显示支付确认弹窗
const showPaymentConfirm = () => {
  if (serveMode.value === 'delivery' && !selectedAddress.value) {
    ElMessage.warning('请选择配送地址')
    return
  }
  showPaymentDialog.value = true
}

// 确认支付并创建订单
const confirmPayment = async () => {
  submitting.value = true

  try {
    // 调用后端创建订单接口（订单状态为已支付待接单）
    const result = await orderStore.createOrder(
      userStore.userId,
      selectedAddress.value?.id,
      remark.value,
      serveMode.value === 'dine_in' ? 'dine_in' : 'delivery',
      selectedCoupon.value?.id || null
    )

    if (result.success) {
      showPaymentDialog.value = false
      ElMessage.success('支付成功，订单已创建')
      // 清空本地购物车状态
      cartStore.items = []
      cartStore.merchantId = null
      cartStore.merchantInfo = null
      router.replace(`/student/order/${result.order.id}`)
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    ElMessage.error('支付失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 加载用户地址
  loadAddresses()

  // 恢复之前选择的地址
  if (cartStore.deliveryAddress) {
    selectedAddress.value = cartStore.deliveryAddress
  }
  loadAvailableCoupons()
})
</script>

<style scoped>
.checkout-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #fff7ed 0%, #f5f7fa 20%, #f5f7fa 100%);
  padding-bottom: 140px;
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

/* 通用 section */
.section {
  background: white;
  margin: 16px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #F2F6FC;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.section-label {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
}

/* 取餐方式 */
.serve-mode-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.serve-mode-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #F5F7FA;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
  border: 2px solid transparent;
}

.serve-mode-option input {
  display: none;
}

.serve-mode-option.active {
  background: #FFF7ED;
  border-color: #FF7A45;
  color: #FF7A45;
}

.serve-mode-option span {
  font-size: 14px;
}

/* 地址区域 */
.address-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.section-icon {
  font-size: 24px;
}

.section-content {
  flex: 1;
}

.address-main {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
  color: #303133;
}

.address-contact {
  font-size: 13px;
  color: #909399;
}

.address-placeholder {
  color: #C0C4CC;
}

/* 店内就餐说明 */
.dine-in-hint {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.hint-title {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
  color: #303133;
}

.hint-text {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

/* 商品区域 */
.goods-section .merchant-name {
  font-weight: 500;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #EBEEF5;
  font-size: 14px;
  color: #303133;
}

.goods-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
  font-size: 13px;
}

.goods-thumb {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  background: #F5F7FA;
  border: 1px solid #EBEEF5;
}

.goods-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.goods-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 500;
  color: #909399;
  background: #F2F6FC;
}

.goods-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.goods-name {
  color: #303133;
  line-height: 1.25;
}

.goods-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.goods-quantity {
  color: #909399;
}

.goods-price {
  color: #606266;
  font-weight: 500;
}

/* 备注区域 */
.remark-input {
  width: 100%;
  padding: 12px;
  background: #F5F7FA;
  border: 1px solid #EBEEF5;
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  outline: none;
}

.remark-input:focus {
  background: white;
  border-color: #FF7A45;
}

/* 优惠券区域 */
.coupon-section {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.coupon-value {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #F5222D;
  font-weight: 500;
}

.coupon-value.placeholder {
  color: #909399;
  font-weight: 400;
}

/* 价格区域 */
.price-section .price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  color: #909399;
}

.price-section .price-row.total {
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid #EBEEF5;
  font-size: 14px;
  color: #303133;
}

.price-section .price-row.total .total-price {
  font-size: 18px;
  font-weight: 700;
  color: #FF7A45;
}

.price-section .price-row.discount {
  color: #F5222D;
}

/* 支付方式 */
.payment-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid #DCDFE6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.payment-option input {
  display: none;
}

.payment-option.active {
  border-color: #FF7A45;
  background: rgba(255, 122, 69, 0.06);
}

.payment-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  flex-shrink: 0;
  font-size: 20px;
  line-height: 1;
}

.payment-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

/* 底部提交栏 */
.submit-bar {
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
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  border-top: 1px solid #F2F6FC;
}

.submit-bar .total-info .total-label {
  font-size: 13px;
  color: #909399;
  margin-right: 4px;
}

.submit-bar .total-info .total-price {
  font-size: 20px;
  font-weight: 700;
  color: #FF7A45;
}

.submit-btn {
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

.submit-btn:hover:not(.disabled) {
  opacity: 0.9;
}

.submit-btn.disabled {
  background: #DCDFE6;
  cursor: not-allowed;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 400px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.modal-body {
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  padding: 12px 16px;
  border-top: 1px solid #EBEEF5;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 支付确认 */
.payment-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #EBEEF5;
}

.payment-amount .label {
  font-size: 14px;
  color: #909399;
}

.payment-amount .amount {
  font-size: 24px;
  font-weight: 700;
  color: #FF7A45;
}

.payment-method-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.payment-method-info .label {
  color: #909399;
  font-size: 14px;
}

.payment-method-info .method {
  color: #303133;
}

.payment-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #FFF7E6;
  border-radius: 8px;
  margin-top: 12px;
  font-size: 13px;
  color: #FA8C16;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.address-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #DCDFE6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.address-item:hover,
.address-item.active {
  border-color: #FF7A45;
  background: rgba(255, 122, 69, 0.05);
}

.address-item .address-main {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
  color: #303133;
}

.address-item .address-contact {
  font-size: 13px;
  color: #909399;
}

.tag-default {
  padding: 2px 8px;
  background: #F5F7FA;
  color: #606266;
  font-size: 11px;
  border-radius: 4px;
}

.add-address {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: 1px dashed #DCDFE6;
  border-radius: 12px;
  color: #303133;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.add-address:hover {
  border-color: #52C41A;
  background: rgba(82, 196, 26, 0.06);
}

.new-address-form {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-field {
  padding: 12px;
  background: #F5F7FA;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.input-field:focus {
  background: white;
  border-color: #FF7A45;
}

/* 优惠券列表 */
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-item {
  border: 1px solid #DCDFE6;
  border-radius: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.15s;
}

.coupon-item:hover,
.coupon-item.active {
  border-color: #FF7A45;
  background: rgba(255, 122, 69, 0.05);
}

.coupon-title {
  font-weight: 500;
  color: #303133;
}

.coupon-meta {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.coupon-expire {
  margin-top: 8px;
  font-size: 11px;
  color: #C0C4CC;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}

.btn:hover {
  opacity: 0.9;
}

.btn-cancel {
  background: #F5F7FA;
  color: #606266;
}

.btn-confirm,
.btn-primary {
  background: #FF7A45;
  color: white;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
