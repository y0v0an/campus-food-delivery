// ============================================
// 订单状态管理
// ============================================

import { defineStore } from 'pinia'
import request from '@/axios/request'

export const OrderStatus = {
  PENDING_PAYMENT: 'pending_payment',
  PENDING_ACCEPT: 'pending_accept',
  PREPARING: 'preparing',
  READY: 'ready',
  DELIVERING: 'delivering',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled'
}

export const useOrderStore = defineStore('order', {
  state: () => ({
    currentOrder: null,
    orderList: [],
    pendingOrders: [],
    loading: false,
    error: null
  }),

  getters: {
    getStatusText: () => (status) => {
      const statusMap = {
        [OrderStatus.PENDING_PAYMENT]: '待支付',
        [OrderStatus.PENDING_ACCEPT]: '待接单',
        [OrderStatus.PREPARING]: '制作中',
        [OrderStatus.READY]: '待取餐',
        [OrderStatus.DELIVERING]: '配送中',
        [OrderStatus.COMPLETED]: '已完成',
        [OrderStatus.CANCELLED]: '已取消'
      }
      return statusMap[status] || '未知状态'
    },
    
    getStatusColor: () => (status) => {
      const colorMap = {
        [OrderStatus.PENDING_PAYMENT]: '#FF7A45',
        [OrderStatus.PENDING_ACCEPT]: '#1890FF',
        [OrderStatus.PREPARING]: '#722ED1',
        [OrderStatus.READY]: '#13C2C2',
        [OrderStatus.DELIVERING]: '#4A90D9',
        [OrderStatus.COMPLETED]: '#52C41A',
        [OrderStatus.CANCELLED]: '#909399'
      }
      return colorMap[status] || '#909399'
    },
    
    activeOrders: (state) => {
      const activeStatuses = [
        OrderStatus.PENDING_PAYMENT,
        OrderStatus.PENDING_ACCEPT,
        OrderStatus.PREPARING,
        OrderStatus.READY,
        OrderStatus.DELIVERING
      ]
      return state.orderList.filter(o => activeStatuses.includes(o.status))
    },
    
    completedOrders: (state) => {
      return state.orderList.filter(o => o.status === OrderStatus.COMPLETED)
    }
  },

  actions: {
    async createOrder(studentId, addressId, remark, orderType = 'delivery', userCouponId = null) {
      try {
        this.loading = true
        this.error = null
        
        const payload = { studentId, remark, orderType }
        if (orderType === 'delivery') {
          payload.addressId = addressId
        }
        if (userCouponId) {
          payload.userCouponId = userCouponId
        }
        const order = await request.post('/order/create', payload)
        
        this.currentOrder = order
        return { success: true, order }
      } catch (e) {
        this.error = e.message
        return { success: false, message: e.message }
      } finally {
        this.loading = false
      }
    },

    async updateOrderStatus(orderId, status) {
      try {
        await request.put('/order/status', { orderId, status })
        await this.fetchOrderDetail(orderId)
        return { success: true }
      } catch (e) {
        return { success: false, message: e.message }
      }
    },

    async riderAcceptOrder(orderId, riderId, riderName) {
      try {
        await request.put('/order/accept', { orderId, riderId, riderName })
        await this.loadPendingOrders()
        return { success: true }
      } catch (e) {
        return { success: false, message: e.message || '该订单已被其他骑手接单' }
      }
    },

    async cancelOrder(orderId) {
      try {
        await request.put(`/order/cancel/${orderId}`)
        return { success: true }
      } catch (e) {
        return { success: false, message: e.message }
      }
    },

    async studentConfirmPickup(orderId, studentId) {
      try {
        await request.put('/order/student/confirm', { orderId, studentId })
        await this.fetchOrderDetail(orderId)
        return { success: true }
      } catch (e) {
        return { success: false, message: e.message || '确认失败' }
      }
    },

    async submitReview(reviewData) {
      try {
        await request.post('/review/add', reviewData)
        return { success: true }
      } catch (e) {
        return { success: false, message: e.message }
      }
    },

    async fetchOrderDetail(orderId) {
      try {
        this.currentOrder = await request.get(`/order/${orderId}`)
      } catch (e) {
        console.error('获取订单详情失败:', e)
      }
    },

    async loadStudentOrders(studentId) {
      try {
        this.orderList = await request.get(`/order/student/${studentId}`)
      } catch (e) {
        console.error('加载订单列表失败:', e)
        this.orderList = []
      }
    },

    async loadMerchantOrders(merchantId, status = null) {
      try {
        const params = status ? { status } : {}
        this.orderList = await request.get(`/order/merchant/${merchantId}`, { params })
      } catch (e) {
        console.error('加载订单列表失败:', e)
        this.orderList = []
      }
    },

    async loadRiderOrders(riderId) {
      try {
        this.orderList = await request.get(`/order/rider/${riderId}`)
      } catch (e) {
        console.error('加载订单列表失败:', e)
        this.orderList = []
      }
    },

    async loadPendingOrders() {
      try {
        this.pendingOrders = await request.get('/order/pool')
      } catch (e) {
        console.error('加载订单池失败:', e)
        this.pendingOrders = []
      }
    },

    async getStats(merchantId = null, startDate = null, endDate = null) {
      try {
        const params = {}
        if (merchantId) params.merchantId = merchantId
        if (startDate) params.startDate = startDate
        if (endDate) params.endDate = endDate
        return await request.get('/order/stats', { params })
      } catch (e) {
        console.error('获取统计数据失败:', e)
        return {}
      }
    },

    clearCurrentOrder() {
      this.currentOrder = null
    }
  }
})

export default useOrderStore
