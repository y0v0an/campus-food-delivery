// ============================================
// 配送状态管理
// ============================================

import { defineStore } from 'pinia'
import request from '@/axios/request'

// 轨迹点状态
export const TrackStatus = {
  TO_MERCHANT: 'to_merchant',   // 前往商家
  AT_MERCHANT: 'at_merchant',   // 到达商家
  TO_STUDENT: 'to_student',     // 前往学生
  DELIVERED: 'delivered'        // 已送达
}

export const useDeliveryStore = defineStore('delivery', {
  state: () => ({
    // 当前配送轨迹
    currentTrack: null,
    // 骑手当前位置
    riderLocation: null,
    // 是否正在配送
    isDelivering: false,
    // 位置更新定时器
    locationTimer: null
  }),

  getters: {
    // 获取轨迹状态文本
    getTrackStatusText: () => (status) => {
      const statusMap = {
        [TrackStatus.TO_MERCHANT]: '前往商家',
        [TrackStatus.AT_MERCHANT]: '到达商家',
        [TrackStatus.TO_STUDENT]: '配送中',
        [TrackStatus.DELIVERED]: '已送达'
      }
      return statusMap[status] || '未知状态'
    },
    
    // 轨迹点数量
    trackPointCount: (state) => {
      return state.currentTrack?.points?.length || 0
    },
    
    // 最新轨迹状态
    latestStatus: (state) => {
      if (!state.currentTrack?.points?.length) return null
      return state.currentTrack.points[state.currentTrack.points.length - 1].status
    },
    
    // 预计送达时间
    estimatedTime: (state) => {
      return state.currentTrack?.estimatedTime || 0
    },
    
    // 轨迹是否完整
    isComplete: (state) => {
      if (!state.currentTrack?.points?.length) return false
      return state.currentTrack.points.some(p => p.status === TrackStatus.DELIVERED)
    }
  },

  actions: {
    /**
     * 加载订单配送轨迹
     * @param {string} orderId - 订单ID
     */
    async loadTrack(orderId) {
      try {
        const res = await request.get(`/delivery/track/${orderId}`)
        if (res.code === 200) {
          this.currentTrack = res.data
        }
      } catch (error) {
        console.error('加载配送轨迹失败:', error)
      }
    },

    /**
     * 开始配送（创建轨迹）
     * @param {string} orderId - 订单ID
     * @param {string} riderId - 骑手ID
     * @param {number} estimatedTime - 预计时间
     * @returns {object} - 创建的轨迹
     */
    async startDelivery(orderId, riderId, estimatedTime = 30) {
      try {
        const res = await request.post('/delivery/start', {
          orderId,
          riderId,
          estimatedTime
        })
        if (res.code === 200) {
          this.currentTrack = res.data
          this.isDelivering = true
        }
        return this.currentTrack
      } catch (error) {
        console.error('开始配送失败:', error)
        return null
      }
    },

    /**
     * 更新配送状态 - 到达商家
     * @param {string} orderId - 订单ID
     * @param {object} location - 位置信息
     */
    async arriveAtMerchant(orderId, location) {
      try {
        const res = await request.post('/delivery/track/add', {
          orderId,
          location,
          status: TrackStatus.AT_MERCHANT
        })
        if (res.code === 200) {
          this.currentTrack = res.data
        }
        return this.currentTrack
      } catch (error) {
        console.error('更新配送状态失败:', error)
        return null
      }
    },

    /**
     * 更新配送状态 - 开始送餐
     * @param {string} orderId - 订单ID
     * @param {object} location - 位置信息
     */
    async startToStudent(orderId, location) {
      try {
        const res = await request.post('/delivery/track/add', {
          orderId,
          location,
          status: TrackStatus.TO_STUDENT
        })
        if (res.code === 200) {
          this.currentTrack = res.data
        }
        return this.currentTrack
      } catch (error) {
        console.error('更新配送状态失败:', error)
        return null
      }
    },

    /**
     * 更新配送状态 - 已送达
     * @param {string} orderId - 订单ID
     * @param {object} location - 位置信息
     */
    async completeDelivery(orderId, location) {
      try {
        const res = await request.post('/delivery/track/add', {
          orderId,
          location,
          status: TrackStatus.DELIVERED
        })
        if (res.code === 200) {
          this.currentTrack = res.data
          this.isDelivering = false
        }
        return this.currentTrack
      } catch (error) {
        console.error('完成配送失败:', error)
        return null
      }
    },

    /**
     * 更新骑手位置
     * @param {string} riderId - 骑手ID
     * @param {object} location - 位置信息
     */
    async updateLocation(riderId, location) {
      try {
        await request.post('/delivery/location', {
          riderId,
          ...location
        })
        this.riderLocation = location
      } catch (error) {
        console.error('更新骑手位置失败:', error)
      }
    },

    /**
     * 获取骑手位置
     * @param {string} riderId - 骑手ID
     * @returns {object|null} - 位置信息
     */
    async fetchRiderLocation(riderId) {
      try {
        const res = await request.get(`/delivery/location/${riderId}`)
        if (res.code === 200) {
          this.riderLocation = res.data
        }
        return this.riderLocation
      } catch (error) {
        console.error('获取骑手位置失败:', error)
        return null
      }
    },

    /**
     * 开始位置更新（模拟GPS）
     * @param {string} riderId - 骑手ID
     * @param {number} interval - 更新间隔（毫秒）
     */
    startLocationUpdates(riderId, interval = 5000) {
      this.stopLocationUpdates()
      
      this.locationTimer = setInterval(() => {
        // 模拟位置变化
        if (this.riderLocation) {
          const newLocation = {
            lat: this.riderLocation.lat + (Math.random() - 0.5) * 0.001,
            lng: this.riderLocation.lng + (Math.random() - 0.5) * 0.001,
            address: '配送途中'
          }
          this.updateLocation(riderId, newLocation)
        }
      }, interval)
    },

    /**
     * 停止位置更新
     */
    stopLocationUpdates() {
      if (this.locationTimer) {
        clearInterval(this.locationTimer)
        this.locationTimer = null
      }
    },

    /**
     * 获取骑手配送统计
     * @param {string} riderId - 骑手ID
     * @returns {object} - 统计数据
     */
    async getRiderStats(riderId) {
      try {
        const res = await request.get(`/rider/stats/${riderId}`)
        if (res.code === 200) {
          return res.data
        }
        return { totalDeliveries: 0, averageTime: 0 }
      } catch (error) {
        console.error('获取骑手统计失败:', error)
        return { totalDeliveries: 0, averageTime: 0 }
      }
    },

    /**
     * 检查轨迹是否完整
     * @param {string} orderId - 订单ID
     * @returns {boolean} - 是否完整
     */
    checkTrackComplete(orderId) {
      if (!this.currentTrack || this.currentTrack.orderId !== orderId) {
        return false
      }
      return this.currentTrack.points?.some(p => p.status === TrackStatus.DELIVERED) || false
    },

    /**
     * 获取最新轨迹状态
     * @param {string} orderId - 订单ID
     * @returns {string|null} - 状态
     */
    getLatestStatus(orderId) {
      if (!this.currentTrack || this.currentTrack.orderId !== orderId) {
        return null
      }
      const points = this.currentTrack.points
      if (!points || points.length === 0) return null
      return points[points.length - 1].status
    },

    /**
     * 获取在线骑手数量
     * @returns {number} - 在线骑手数
     */
    async getOnlineRiders() {
      try {
        const res = await request.get('/rider/online/count')
        if (res.code === 200) {
          return res.data
        }
        return 0
      } catch (error) {
        console.error('获取在线骑手数量失败:', error)
        return 0
      }
    },

    /**
     * 清除当前轨迹
     */
    clearTrack() {
      this.currentTrack = null
      this.riderLocation = null
      this.isDelivering = false
      this.stopLocationUpdates()
    }
  }
})

export default useDeliveryStore
