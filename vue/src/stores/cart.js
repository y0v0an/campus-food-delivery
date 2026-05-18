// ============================================
// 购物车状态管理
// ============================================

import { defineStore } from 'pinia'
import request from '@/axios/request'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    merchantId: null,
    merchantInfo: null,
    deliveryAddress: null,
    remark: ''
  }),

  getters: {
    itemCount: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
    
    totalPrice: (state) => state.items.reduce((sum, item) => sum + item.price * item.quantity, 0),
    
    deliveryFee: (state) => state.merchantInfo?.deliveryFee || 0,
    
    packingFee: (state) => state.items.length > 0 ? 2 : 0,
    
    actualAmount: (state) => {
      const total = state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
      const delivery = state.merchantInfo?.deliveryFee || 0
      const packing = state.items.length > 0 ? 2 : 0
      return total + delivery + packing
    },
    
    reachMinOrder: (state) => {
      const total = state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
      const minOrder = state.merchantInfo?.minOrder || 0
      return total >= minOrder
    },
    
    amountToMinOrder: (state) => {
      const total = state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
      const minOrder = state.merchantInfo?.minOrder || 0
      return Math.max(0, minOrder - total)
    },
    
    isEmpty: (state) => state.items.length === 0,
    
    getItemQuantity: (state) => (dishId) => {
      const item = state.items.find(i => i.dishId === dishId)
      return item ? item.quantity : 0
    }
  },

  actions: {
    async loadCart(userId) {
      try {
        const result = await request.get(`/cart/${userId}`)
        this.items = (result.items || []).map(item => ({
          id: item.id,
          dishId: item.dishId,
          dishName: item.dish?.name || '',
          price: item.dish?.price || 0,
          originalPrice: item.dish?.originalPrice,
          image: item.dish?.image || '',
          quantity: item.quantity,
          merchantId: item.merchantId
        }))
        this.merchantId = result.merchantId
        if (this.merchantId) {
          const merchant = await request.get(`/merchant/${this.merchantId}`)
          this.merchantInfo = merchant
        }
      } catch (error) {
        console.error('加载购物车失败:', error)
      }
    },

    async addItem(dish, quantity = 1, userId) {
      if (!userId) {
        return { success: false, message: '请先登录' }
      }
      
      if (this.merchantId && this.merchantId !== dish.merchantId) {
        return { 
          success: false, 
          message: '购物车中已有其他商家的商品，是否清空购物车？',
          needClear: true
        }
      }
      
      if (dish.isAvailable === false) {
        return { success: false, message: '该商品已售罄' }
      }
      
      try {
        await request.post('/cart/add', {
          userId: String(userId),
          merchantId: String(dish.merchantId),
          dishId: String(dish.id),
          quantity
        })
        
        await this.loadCart(userId)
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message || '添加失败' }
      }
    },

    async decreaseItem(itemId, userId) {
      const item = this.items.find(i => i.id === itemId)
      if (!item) return
      
      if (item.quantity <= 1) {
        await this.removeItem(itemId, userId)
      } else {
        try {
          await request.put('/cart/update', { id: itemId, quantity: item.quantity - 1 })
          await this.loadCart(userId)
        } catch (error) {
          console.error('更新购物车失败:', error)
        }
      }
    },

    async setItemQuantity(itemId, quantity, userId) {
      if (quantity <= 0) {
        await this.removeItem(itemId, userId)
        return
      }
      
      try {
        await request.put('/cart/update', { id: itemId, quantity })
        await this.loadCart(userId)
      } catch (error) {
        console.error('更新购物车失败:', error)
      }
    },

    async removeItem(itemId, userId) {
      try {
        await request.delete(`/cart/remove/${itemId}`)
        await this.loadCart(userId)
      } catch (error) {
        console.error('删除购物车项失败:', error)
      }
    },

    async clearCart(userId) {
      try {
        await request.delete(`/cart/clear/${userId}`)
        this.items = []
        this.merchantId = null
        this.merchantInfo = null
        this.deliveryAddress = null
        this.remark = ''
      } catch (error) {
        console.error('清空购物车失败:', error)
      }
    },

    setDeliveryAddress(address) {
      this.deliveryAddress = address
    },

    setRemark(remark) {
      this.remark = remark
    },

    getOrderData() {
      return {
        merchantId: this.merchantId,
        merchantName: this.merchantInfo?.name || '',
        items: this.items.map(item => ({
          dishId: item.dishId,
          dishName: item.dishName,
          price: item.price,
          quantity: item.quantity
        })),
        totalAmount: this.totalPrice,
        deliveryFee: this.deliveryFee,
        packingFee: this.packingFee,
        actualAmount: this.actualAmount,
        address: this.deliveryAddress,
        remark: this.remark
      }
    },

    initCart(userId) {
      if (userId) {
        this.loadCart(userId)
      }
    }
  }
})

export default useCartStore
