// ============================================
// 路由配置 - 校园外卖配送系统
// ============================================

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 登录页
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login/LoginPage.vue'),
      meta: { requiresAuth: false }
    },
    // 注册页
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Login/RegisterPage.vue'),
      meta: { requiresAuth: false }
    },
    // 忘记密码页
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('@/views/Login/ForgotPasswordPage.vue'),
      meta: { requiresAuth: false }
    },

    // ============================================
    // 学生端路由
    // ============================================
    {
      path: '/student',
      name: 'Student',
      component: () => import('@/views/Student/StudentLayout.vue'),
      meta: { requiresAuth: true, role: 'student' },
      redirect: '/student/home',
      children: [
        {
          path: 'home',
          name: 'StudentHome',
          component: () => import('@/views/Student/HomePage.vue')
        },
        {
          path: 'merchant/:id',
          name: 'MerchantDetail',
          component: () => import('@/views/Student/MerchantDetail.vue')
        },
        {
          path: 'cart',
          name: 'Cart',
          component: () => import('@/views/Student/Cart.vue')
        },
        {
          path: 'checkout',
          name: 'Checkout',
          component: () => import('@/views/Student/Checkout.vue')
        },
        {
          path: 'group-orders',
          name: 'MyGroupOrders',
          component: () => import('@/views/Student/MyGroupOrders.vue')
        },
        {
          path: 'group-list',
          name: 'GroupOrderList',
          component: () => import('@/views/Student/GroupOrderList.vue')
        },
        {
          path: 'orders',
          name: 'MyOrders',
          component: () => import('@/views/Student/MyOrders.vue')
        },
        {
          path: 'order/:id',
          name: 'OrderTrack',
          component: () => import('@/views/Student/OrderTrack.vue')
        },
        {
          path: 'review/:id',
          name: 'OrderReview',
          component: () => import('@/views/Student/OrderReview.vue')
        },
        {
          path: 'my-reviews',
          name: 'MyReviews',
          component: () => import('@/views/Student/MyReviews.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Student/Profile.vue')
        },
        {
          path: 'my-coupons',
          name: 'MyCoupons',
          component: () => import('@/views/Student/MyCoupons.vue')
        },
        {
          path: 'addresses',
          name: 'AddressManage',
          component: () => import('@/views/Student/AddressManage.vue')
        },
        {
          path: 'rider-apply',
          name: 'RiderApply',
          component: () => import('@/views/Student/RiderApply.vue')
        },
        {
          path: 'category/:type',
          name: 'CategoryList',
          component: () => import('@/views/Student/CategoryList.vue')
        }
      ]
    },

    // ============================================
    // 商家端路由
    // ============================================
    {
      path: '/merchant',
      name: 'Merchant',
      component: () => import('@/views/Merchant/MerchantLayout.vue'),
      meta: { requiresAuth: true, role: 'merchant' },
      redirect: '/merchant/orders',
      children: [
        {
          path: 'orders',
          name: 'MerchantOrders',
          component: () => import('@/views/Merchant/OrderManage.vue')
        },
        {
          path: 'dishes',
          name: 'DishManage',
          component: () => import('@/views/Merchant/DishManage.vue')
        },
        {
          path: 'statistics',
          name: 'MerchantStatistics',
          component: () => import('@/views/Merchant/Statistics.vue')
        },
        {
          path: 'coupons',
          name: 'MerchantCoupons',
          component: () => import('@/views/Merchant/CouponManage.vue')
        },
        {
          path: 'group-coupons',
          name: 'MerchantGroupCoupons',
          component: () => import('@/views/Merchant/GroupCouponManage.vue'),
          meta: { title: '拼单优惠券', icon: 'lucide:ticket' }
        },
        {
          path: 'profile',
          name: 'MerchantProfile',
          component: () => import('@/views/Merchant/MerchantProfile.vue')
        }
      ]
    },

    // ============================================
    // 骑手端路由
    // ============================================
    {
      path: '/rider',
      name: 'Rider',
      component: () => import('@/views/Rider/RiderLayout.vue'),
      meta: { requiresAuth: true, role: 'rider' },
      redirect: '/rider/pool',
      children: [
        {
          path: 'pool',
          name: 'OrderPool',
          component: () => import('@/views/Rider/OrderPool.vue')
        },
        {
          path: 'delivery',
          name: 'MyDelivery',
          component: () => import('@/views/Rider/MyDelivery.vue')
        },
        {
          path: 'earnings',
          name: 'Earnings',
          component: () => import('@/views/Rider/Earnings.vue')
        }
      ]
    },

    // ============================================
    // 管理员端路由
    // ============================================
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/views/Admin/AdminLayout.vue'),
      meta: { requiresAuth: true, role: 'admin' },
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Admin/Dashboard.vue')
        },
        {
          path: 'users',
          name: 'UserManage',
          component: () => import('@/views/Admin/UserManage.vue')
        },
        {
          path: 'merchants',
          name: 'MerchantList',
          component: () => import('@/views/Admin/MerchantList.vue')
        },
        {
          path: 'orders',
          name: 'AdminOrderList',
          component: () => import('@/views/Admin/OrderList.vue')
        },
        {
          path: 'rider-audit',
          name: 'RiderAudit',
          component: () => import('@/views/Admin/RiderAudit.vue')
        },
        {
          path: 'profile',
          name: 'AdminProfile',
          component: () => import('@/views/Admin/AdminProfile.vue')
        }
      ]
    },

    // 404 页面
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})


// ============================================
// 路由守卫（角色以 Pinia 为准，避免仅读 localStorage 与切换后不同步导致被踢回学生端）
// ============================================

function getRouteMeta(to) {
  const matched = to.matched || []
  const needsAuth = matched.some((r) => r.meta && r.meta.requiresAuth === true)
  const roleRecord = matched.find((r) => r.meta && r.meta.role)
  const requiredRole = roleRecord?.meta?.role
  return { needsAuth, requiredRole }
}

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = !!(userStore.isLogin && userStore.token)
  const userRole = userStore.currentRole

  const { needsAuth, requiredRole } = getRouteMeta(to)

  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    if (isLoggedIn && to.path === '/login') {
      const rolePathMap = {
        student: '/student',
        merchant: '/merchant',
        rider: '/rider',
        admin: '/admin'
      }
      next(rolePathMap[userRole] || '/login')
      return
    }
    next()
    return
  }

  if (needsAuth) {
    if (!isLoggedIn) {
      next('/login')
      return
    }

    if (requiredRole && requiredRole !== userRole) {
      const rolePathMap = {
        student: '/student',
        merchant: '/merchant',
        rider: '/rider',
        admin: '/admin'
      }
      next(rolePathMap[userRole] || '/login')
      return
    }

    next()
    return
  }

  next()
})

export default router
