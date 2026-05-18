<template>
  <div class="min-h-screen flex bg-slate-50">
    <!-- 左侧装饰区域（大屏显示） -->
    <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-orange-500 to-orange-600 relative overflow-hidden">
      <!-- 背景图片 -->
      <div class="absolute inset-0 opacity-20">
        <img
            alt="Campus illustration"
            class="w-full h-full object-cover"
            src="https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=1200&h=800&fit=crop"
        />
      </div>

      <!-- 装饰圆形 -->
      <div class="absolute top-0 right-0 w-96 h-96 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2"></div>
      <div class="absolute bottom-0 left-0 w-64 h-64 bg-white/10 rounded-full translate-y-1/2 -translate-x-1/2"></div>

      <!-- 内容区域 -->
      <div class="relative z-10 flex flex-col items-center justify-center w-full text-white px-12">
        <!-- Logo -->
        <div class="w-32 h-32 bg-white rounded-3xl flex items-center justify-center shadow-2xl mb-8">
          <span class="text-7xl">🍔</span>
        </div>

        <!-- 标题 -->
        <h1 class="text-5xl font-black tracking-wider mb-4">校食达</h1>
        <p class="text-xl text-orange-100 font-medium mb-2">校园外卖配送平台</p>
        <p class="text-orange-200">UniEat · 专属于校园的美食配送</p>
      </div>
    </div>

    <!-- 右侧表单 -->
    <div class="w-full lg:w-1/2 flex items-center justify-center p-8">
      <div class="w-full max-w-md">
        <!-- 移动端Logo -->
        <div class="lg:hidden flex items-center justify-center mb-8">
          <div class="w-16 h-16 bg-orange-500 rounded-2xl flex items-center justify-center shadow-lg">
            <span class="text-3xl">🍔</span>
          </div>
          <div class="ml-4">
            <h1 class="text-2xl font-black text-gray-800">校食达</h1>
            <p class="text-sm text-gray-500">校园外卖配送平台</p>
          </div>
        </div>

        <!-- 卡片 -->
        <div class="bg-white rounded-3xl shadow-xl p-8">
          <!-- 标题 -->
          <div class="text-center mb-6">
            <h2 class="text-2xl font-black text-gray-800">
              {{ currentStep === 1 ? '验证身份' : '设置新密码' }}
            </h2>
            <p class="text-gray-500 mt-2">
              {{ currentStep === 1 ? '请输入注册手机号获取验证码' : '请输入您的新密码' }}
            </p>
          </div>

          <!-- 步骤 1：验证身份 -->
          <div v-if="currentStep === 1" class="space-y-5">
            <!-- 手机号输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">📱</span>
              <input
                  v-model="form.phone"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  placeholder="请输入手机号"
                  type="text"
                  maxlength="11"
              />
            </div>

            <!-- 验证码输入 -->
            <div class="flex gap-3">
              <div class="relative flex-1">
                <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">🔢</span>
                <input
                    v-model="form.code"
                    class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                    placeholder="请输入验证码"
                    type="text"
                    maxlength="6"
                />
              </div>
              <button
                  class="px-6 py-4 bg-orange-500 hover:bg-orange-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-medium rounded-2xl transition-all whitespace-nowrap min-w-[120px]"
                  :disabled="countdown > 0 || sending"
                  @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
              </button>
            </div>

            <!-- 开发提示 -->
            <div class="bg-blue-50 border border-blue-200 rounded-xl p-3 text-sm text-blue-600">
              <span class="font-medium">开发模式：</span>验证码固定为 000000
            </div>

            <!-- 下一步按钮 -->
            <button
                class="w-full bg-orange-500 hover:bg-orange-600 text-white font-black py-4 rounded-2xl shadow-xl shadow-orange-200 transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="verifying || !form.phone || !form.code"
                @click="verifyCodeAndNext"
            >
              {{ verifying ? '验证中...' : '下一步' }}
            </button>
          </div>

          <!-- 步骤 2：设置新密码 -->
          <div v-else class="space-y-5">
            <!-- 新密码输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">🔒</span>
              <input
                  v-model="form.newPassword"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  placeholder="请输入新密码（至少6位）"
                  type="password"
              />
            </div>

            <!-- 确认密码输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">🔒</span>
              <input
                  v-model="form.confirmPassword"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  placeholder="请再次输入新密码"
                  type="password"
                  @keyup.enter="resetPassword"
              />
            </div>

            <!-- 重置按钮 -->
            <button
                class="w-full bg-orange-500 hover:bg-orange-600 text-white font-black py-4 rounded-2xl shadow-xl shadow-orange-200 transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="resetting || !form.newPassword || !form.confirmPassword"
                @click="resetPassword"
            >
              {{ resetting ? '重置中...' : '重置密码' }}
            </button>
          </div>

          <!-- 返回登录 -->
          <div class="mt-6 text-center text-sm text-gray-500">
            <span>想起密码了？</span>
            <a @click="goToLogin" class="text-orange-500 hover:text-orange-600 font-medium cursor-pointer">
              返回登录
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/axios/request'

const router = useRouter()

// 当前步骤：1-验证身份，2-设置新密码
const currentStep = ref(1)

// 表单数据
const form = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 状态
const sending = ref(false)
const verifying = ref(false)
const resetting = ref(false)
const countdown = ref(0)

// 验证手机号格式
const isValidPhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 发送验证码
const sendCode = async () => {
  if (!form.phone) {
    ElMessage.warning('请输入手机号')
    return
  }

  if (!isValidPhone(form.phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }

  sending.value = true

  try {
    await request.post('/user/send-code', { phone: form.phone })
    ElMessage.success('验证码已发送')

    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    sending.value = false
  }
}

// 验证验证码并进入下一步
const verifyCodeAndNext = async () => {
  if (!form.phone || !form.code) {
    ElMessage.warning('请输入手机号和验证码')
    return
  }

  verifying.value = true

  try {
    const result = await request.post('/user/verify-code', {
      phone: form.phone,
      code: form.code
    })

    if (result.valid) {
      currentStep.value = 2
    } else {
      ElMessage.error(result.message || '验证码错误')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error(error.message || '验证失败')
  } finally {
    verifying.value = false
  }
}

// 重置密码
const resetPassword = async () => {
  if (!form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请输入新密码和确认密码')
    return
  }

  if (form.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }

  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  resetting.value = true

  try {
    await request.post('/user/reset-password', {
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword
    })

    ElMessage.success('密码重置成功，请使用新密码登录')

    // 延迟跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error(error.message || '重置密码失败')
  } finally {
    resetting.value = false
  }
}

// 返回登录
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* Tailwind CSS 已处理所有样式 */
</style>
