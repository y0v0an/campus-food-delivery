import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: null,
        isLogin: false
    }),
    actions: {
        // 保存用户登录信息
        setUserInfo(info) {
            this.userInfo = info
            this.isLogin = true
            // 同时存储到localStorage防止刷新丢失
            localStorage.setItem('userInfo', JSON.stringify(info))
            localStorage.setItem('isLogin', 'true')
        },
        // 清除登录状态
        clearUserInfo() {
            this.userInfo = null
            this.isLogin = false
            localStorage.removeItem('userInfo')
            localStorage.removeItem('isLogin')
        },
        // 初始化登录状态（页面加载时调用）
        initUserStatus() {
            const info = localStorage.getItem('userInfo')
            const loginStatus = localStorage.getItem('isLogin')
            if (info && loginStatus === 'true') {
                this.userInfo = JSON.parse(info)
                this.isLogin = true
            }
        }
    }
})