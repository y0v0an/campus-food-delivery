// ============================================
// 用户状态管理
// ============================================

import { defineStore } from 'pinia'
import request from '@/axios/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    userInfo: null,
    // 登录状态
    isLogin: false,
    // 当前角色 (student | merchant | rider | admin)
    currentRole: null,
    // 是否同时拥有骑手身份
    isRider: false,
    // Token（模拟）
    token: null,
    // 商家端顶栏：店铺 logo 原始路径（与 userInfo.avatar 二选一展示）
    merchantProfileLogo: null
  }),

  getters: {
    // 获取用户ID
    userId: (state) => state.userInfo?.id || null,
    
    // 获取用户名
    userName: (state) => state.userInfo?.name || '',
    
    // 获取用户头像
    userAvatar: (state) => state.userInfo?.avatar || '',
    
    // 获取用户手机号
    userPhone: (state) => state.userInfo?.phone || '',
    
    // 是否是学生
    isStudent: (state) => state.currentRole === 'student',
    
    // 是否是商家
    isMerchant: (state) => state.currentRole === 'merchant',
    
    // 是否是骑手角色
    isRiderRole: (state) => state.currentRole === 'rider',
    
    // 是否是管理员
    isAdmin: (state) => state.currentRole === 'admin',
    
    // 是否可以切换到骑手角色（当前在学生端）
    canSwitchToRider: (state) => state.isStudent && state.isRider,

    // 是否可以切回学生端（当前在骑手端，且已认证骑手）
    canSwitchToStudent: (state) => state.currentRole === 'rider' && state.isRider,
    
    // 获取跳转路径
    redirectPath: (state) => {
      const paths = {
        student: '/student',
        merchant: '/merchant',
        rider: '/rider',
        admin: '/admin'
      }
      return paths[state.currentRole] || '/login'
    }
  },

  actions: {
    /**
     * 设置用户信息（登录成功后调用）
     * @param {Object} userInfo - 用户信息对象
     * @param {string} selectedLoginRole - 登录页选中的 Tab（student|merchant|rider|admin）。选「骑手」且账号 isRider 时，currentRole 应为 rider（库中多为 role=student）
     */
    setUserInfo(userInfo, selectedLoginRole = null) {
      this.userInfo = userInfo
      this.isLogin = true
      this.isRider = userInfo.isRider || false
      this.token = `token_${Date.now()}_${userInfo.id}`
      if (selectedLoginRole === 'rider' && userInfo.isRider) {
        this.currentRole = 'rider'
      } else {
        this.currentRole = userInfo.role
      }
      if (userInfo.role !== 'merchant') {
        this.merchantProfileLogo = null
      }
      this.saveToStorage()
    },

    setMerchantProfileLogo(url) {
      this.merchantProfileLogo = url || null
      this.saveToStorage()
    },

    /**
     * 用户登录（调用API）
     * @param {string} phone - 手机号
     * @param {string} password - 密码
     * @param {string} role - 角色
     */
    async login(phone, password, role) {
      try {
        // 手机号格式验证
        if (!/^1[3-9]\d{9}$/.test(phone)) {
          return { success: false, message: '手机号格式不正确' }
        }

        const userInfo = await request.post('/user/login', {
          phone: phone,
          password: password
        })

        // 验证角色
        if (userInfo.role !== role &&
            !(role === 'rider' && userInfo.role === 'student' && userInfo.isRider)) {
          return { success: false, message: '账号角色不匹配' }
        }

        this.setUserInfo(userInfo, role)

        return {
          success: true,
          redirectPath: this.redirectPath,
          user: userInfo
        }
      } catch (error) {
        return { success: false, message: error.message || '登录失败' }
      }
    },

    /**
     * 用户登出
     */
    logout() {
      // 先清除本地状态，不等待后端响应
      this.userInfo = null
      this.isLogin = false
      this.currentRole = null
      this.isRider = false
      this.token = null
      this.merchantProfileLogo = null
      this.clearStorage()
      
      // 异步通知后端（不阻塞）
      request.post('/user/logout').catch(() => {})
    },

    /**
     * 切换角色（学生 <-> 骑手）
     */
    switchRole() {
      if (!this.canSwitchToRider && this.currentRole !== 'rider') {
        return { success: false, message: '无法切换角色' }
      }
      
      if (this.currentRole === 'student') {
        this.currentRole = 'rider'
      } else if (this.currentRole === 'rider') {
        this.currentRole = 'student'
      } else {
        return { success: false, message: '当前角色不支持切换' }
      }
      
      this.saveToStorage()
      
      return { 
        success: true, 
        newRole: this.currentRole,
        redirectPath: this.redirectPath
      }
    },

    /**
     * 更新用户信息
     */
    async updateUserInfo(updates) {
      try {
        const merged = { ...this.userInfo, ...updates }
        delete merged.password
        await request.put('/user/update', merged)
        this.userInfo = { ...this.userInfo, ...updates }
        this.saveToStorage()
        return true
      } catch (error) {
        return false
      }
    },

    /**
     * 设置骑手身份
     */
    setRiderStatus(isRider) {
      this.isRider = isRider
      if (this.userInfo) {
        this.userInfo.isRider = isRider
      }
      this.saveToStorage()
    },

    /**
     * 保存到本地存储
     */
    saveToStorage() {
      const data = {
        userInfo: this.userInfo,
        isLoggedIn: this.isLogin,
        isLogin: this.isLogin,
        role: this.currentRole,
        currentRole: this.currentRole,
        isRider: this.isRider,
        token: this.token,
        merchantProfileLogo: this.merchantProfileLogo
      }
      localStorage.setItem('campus_delivery_user', JSON.stringify(data))
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    /**
     * 从本地存储恢复
     */
    restoreFromStorage() {
      try {
        const data = localStorage.getItem('campus_delivery_user')
        if (data) {
          const parsed = JSON.parse(data)
          this.userInfo = parsed.userInfo
          this.isLogin = parsed.isLogin
          this.currentRole = parsed.currentRole
          this.isRider = parsed.isRider
          this.token = parsed.token
          this.merchantProfileLogo = parsed.merchantProfileLogo || null
          return true
        }
      } catch (e) {
        console.error('Failed to restore user data:', e)
      }
      return false
    },

    /**
     * 清除本地存储
     */
    clearStorage() {
      localStorage.removeItem('campus_delivery_user')
      localStorage.removeItem('userInfo')
    },

    /**
     * 初始化用户状态
     */
    initUserStatus() {
      this.restoreFromStorage()
      this.setupStorageListener()
    },

    /**
     * 设置 storage 事件监听（同步多标签页状态）
     */
    setupStorageListener() {
      // 只在浏览器环境中执行
      if (typeof window === 'undefined') return

      const handleStorageChange = (e) => {
        // 只监听我们自己的用户存储变化
        if (e.key === 'campus_delivery_user' || e.key === 'userInfo') {
          // 当其他标签页修改了存储时，重新加载用户信息
          this.restoreFromStorage()

          // 如果新的存储显示已登出，清除当前状态
          if (e.newValue === null || e.newValue === '""') {
            this.userInfo = null
            this.isLogin = false
            this.currentRole = null
            this.isRider = false
            this.token = null
          }
        }
      }

      window.addEventListener('storage', handleStorageChange)

      // 保存清理函数（供组件卸载时使用）
      this._cleanupStorageListener = () => {
        window.removeEventListener('storage', handleStorageChange)
      }
    },

    /**
     * 清理 storage 监听器
     */
    cleanupStorageListener() {
      if (this._cleanupStorageListener) {
        this._cleanupStorageListener()
        this._cleanupStorageListener = null
      }
    },

    /**
     * 检查登录状态
     */
    checkLoginStatus() {
      if (!this.isLogin || !this.token) {
        this.restoreFromStorage()
      }
      return this.isLogin
    },

    /**
     * 刷新用户信息
     */
    async refreshUserInfo() {
      if (this.userInfo?.id) {
        try {
          const freshUser = await request.get(`/user/info/${this.userInfo.id}`)
          if (freshUser) {
            this.userInfo = freshUser
            this.isRider = freshUser.isRider || false
            this.saveToStorage()
          }
        } catch (error) {
          console.error('刷新用户信息失败:', error)
        }
      }
    }
  }
})

export default useUserStore
