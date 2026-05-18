<!--<template>-->
<!--  <div class="login-page">-->
<!--    &lt;!&ndash; 背景装饰 &ndash;&gt;-->
<!--    <div class="bg-decoration">-->
<!--      <div class="circle circle-1"></div>-->
<!--      <div class="circle circle-2"></div>-->
<!--      <div class="circle circle-3"></div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 登录卡片 &ndash;&gt;-->
<!--    <div class="login-card">-->
<!--      &lt;!&ndash; Logo &ndash;&gt;-->
<!--      <div class="login-header">-->
<!--        <div class="logo">-->
<!--          <span class="logo-icon">🍔</span>-->
<!--          <span class="logo-text">校园专属</span>-->
<!--        </div>-->
<!--        <p class="login-subtitle">校园外卖配送平台</p>-->
<!--      </div>-->

<!--      &lt;!&ndash; 角色选择 &ndash;&gt;-->
<!--      <div class="role-tabs">-->
<!--        <div-->
<!--          v-for="role in roles"-->
<!--          :key="role.value"-->
<!--          class="role-tab"-->
<!--          :class="{ active: currentRole === role.value }"-->
<!--          @click="currentRole = role.value"-->
<!--        >-->
<!--          <span class="role-icon">{{ role.icon }}</span>-->
<!--          <span class="role-name">{{ role.label }}</span>-->
<!--        </div>-->
<!--      </div>-->

<!--      &lt;!&ndash; 登录表单 &ndash;&gt;-->
<!--      <el-form-->
<!--        ref="formRef"-->
<!--        :model="loginForm"-->
<!--        :rules="loginRules"-->
<!--        class="login-form"-->
<!--        @submit.prevent="handleLogin"-->
<!--      >-->
<!--        <el-form-item prop="account">-->
<!--          <el-input-->
<!--            v-model="loginForm.account"-->
<!--            :placeholder="accountPlaceholder"-->
<!--            :prefix-icon="User"-->
<!--            size="large"-->
<!--            clearable-->
<!--          />-->
<!--        </el-form-item>-->

<!--        <el-form-item prop="password">-->
<!--          <el-input-->
<!--            v-model="loginForm.password"-->
<!--            type="password"-->
<!--            placeholder="请输入密码"-->
<!--            :prefix-icon="Lock"-->
<!--            size="large"-->
<!--            show-password-->
<!--            @keyup.enter="handleLogin"-->
<!--          />-->
<!--        </el-form-item>-->

<!--        <el-form-item>-->
<!--          <el-button-->
<!--            type="primary"-->
<!--            size="large"-->
<!--            :loading="loading"-->
<!--            class="login-btn"-->
<!--            @click="handleLogin"-->
<!--          >-->
<!--            登录-->
<!--          </el-button>-->
<!--        </el-form-item>-->
<!--      </el-form>-->

<!--      &lt;!&ndash; 测试账号提示 &ndash;&gt;-->
<!--      <div class="test-accounts">-->
<!--        <el-collapse>-->
<!--          <el-collapse-item title="测试账号" name="1">-->
<!--            <div class="account-list">-->
<!--              <div class="account-item" @click="fillTestAccount('student')">-->
<!--                <span class="account-role">学生</span>-->
<!--                <span class="account-info">student1 / 123456</span>-->
<!--              </div>-->
<!--              <div class="account-item" @click="fillTestAccount('merchant')">-->
<!--                <span class="account-role">商家</span>-->
<!--                <span class="account-info">merchant1 / 123456</span>-->
<!--              </div>-->
<!--              <div class="account-item" @click="fillTestAccount('rider')">-->
<!--                <span class="account-role">骑手</span>-->
<!--                <span class="account-info">rider1 / 123456</span>-->
<!--              </div>-->
<!--              <div class="account-item" @click="fillTestAccount('admin')">-->
<!--                <span class="account-role">管理员</span>-->
<!--                <span class="account-info">admin / 123456</span>-->
<!--              </div>-->
<!--            </div>-->
<!--          </el-collapse-item>-->
<!--        </el-collapse>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 底部信息 &ndash;&gt;-->
<!--    <div class="login-footer">-->
<!--      <p>校园专属外卖配送系统</p>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, computed } from 'vue'-->
<!--import { useRouter } from 'vue-router'-->
<!--import { User, Lock } from '@element-plus/icons-vue'-->
<!--import { ElMessage } from 'element-plus'-->
<!--import { useUserStore } from '@/stores/user'-->
<!--import request from '@/axios/request'-->

<!--const router = useRouter()-->
<!--const userStore = useUserStore()-->

<!--// 角色配置-->
<!--const roles = [-->
<!--  { value: 'student', label: '学生', icon: '🎓' },-->
<!--  { value: 'merchant', label: '商家', icon: '🏪' },-->
<!--  { value: 'rider', label: '骑手', icon: '🚴' },-->
<!--  { value: 'admin', label: '管理员', icon: '👔' }-->
<!--]-->

<!--// 当前选择的角色-->
<!--const currentRole = ref('student')-->

<!--// 表单引用-->
<!--const formRef = ref(null)-->

<!--// 加载状态-->
<!--const loading = ref(false)-->

<!--// 登录表单-->
<!--const loginForm = ref({-->
<!--  account: '',-->
<!--  password: ''-->
<!--})-->

<!--// 账号输入框占位符-->
<!--const accountPlaceholder = computed(() => {-->
<!--  const placeholders = {-->
<!--    student: '请输入学号',-->
<!--    merchant: '请输入商家账号',-->
<!--    rider: '请输入骑手账号',-->
<!--    admin: '请输入管理员账号'-->
<!--  }-->
<!--  return placeholders[currentRole.value]-->
<!--})-->

<!--// 表单验证规则-->
<!--const loginRules = {-->
<!--  account: [-->
<!--    { required: true, message: '请输入账号', trigger: 'blur' }-->
<!--  ],-->
<!--  password: [-->
<!--    { required: true, message: '请输入密码', trigger: 'blur' },-->
<!--    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }-->
<!--  ]-->
<!--}-->

<!--// 处理登录-->
<!--const handleLogin = async () => {-->
<!--  if (!formRef.value) return-->
<!--  -->
<!--  await formRef.value.validate(async (valid) => {-->
<!--    if (!valid) return-->
<!--    -->
<!--    loading.value = true-->
<!--    -->
<!--    try {-->
<!--      // 调用后端登录接口-->
<!--      const userInfo = await request.post('/user/login', {-->
<!--        username: loginForm.value.account,-->
<!--        password: loginForm.value.password-->
<!--      })-->
<!--      -->
<!--      // 验证角色是否匹配-->
<!--      if (userInfo.role !== currentRole.value && -->
<!--          !(currentRole.value === 'rider' && userInfo.role === 'student' && userInfo.isRider)) {-->
<!--        ElMessage.error('账号角色不匹配')-->
<!--        return-->
<!--      }-->
<!--      -->
<!--      // 保存用户信息（传入登录 Tab，骑手账号在库中为 student，需把 currentRole 设为 rider）-->
<!--      userStore.setUserInfo(userInfo, currentRole.value)-->
<!--      localStorage.setItem('userInfo', JSON.stringify(userInfo))-->

<!--      ElMessage.success('登录成功')-->

<!--      router.push(userStore.redirectPath || '/student')-->
<!--      -->
<!--    } catch (error) {-->
<!--      console.error('登录失败:', error)-->
<!--    } finally {-->
<!--      loading.value = false-->
<!--    }-->
<!--  })-->
<!--}-->

<!--// 填充测试账号-->
<!--const fillTestAccount = (role) => {-->
<!--  currentRole.value = role-->
<!--  -->
<!--  const accounts = {-->
<!--    student: { account: 'student1', password: '123456' },-->
<!--    merchant: { account: 'merchant1', password: '123456' },-->
<!--    rider: { account: 'rider1', password: '123456' },-->
<!--    admin: { account: 'admin', password: '123456' }-->
<!--  }-->
<!--  -->
<!--  loginForm.value = { ...accounts[role] }-->
<!--}-->
<!--</script>-->

<!--<style lang="scss" scoped>-->
<!--@use '@/assets/styles/variables.scss' as *;-->

<!--.login-page {-->
<!--  min-height: 100vh;-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  background: linear-gradient(135deg, #E8F4FD 0%, #F5F7FA 50%, #E8F8E8 100%);-->
<!--  position: relative;-->
<!--  overflow: hidden;-->
<!--  padding: $spacing-lg;-->
<!--}-->

<!--.bg-decoration {-->
<!--  position: absolute;-->
<!--  inset: 0;-->
<!--  pointer-events: none;-->
<!--  overflow: hidden;-->

<!--  .circle {-->
<!--    position: absolute;-->
<!--    border-radius: 50%;-->
<!--    opacity: 0.5;-->
<!--  }-->

<!--  .circle-1 {-->
<!--    width: 400px;-->
<!--    height: 400px;-->
<!--    background: linear-gradient(135deg, rgba($color-primary, 0.2) 0%, transparent 70%);-->
<!--    top: -100px;-->
<!--    right: -100px;-->
<!--  }-->

<!--  .circle-2 {-->
<!--    width: 300px;-->
<!--    height: 300px;-->
<!--    background: linear-gradient(135deg, rgba($color-success, 0.2) 0%, transparent 70%);-->
<!--    bottom: -50px;-->
<!--    left: -50px;-->
<!--  }-->

<!--  .circle-3 {-->
<!--    width: 200px;-->
<!--    height: 200px;-->
<!--    background: linear-gradient(135deg, rgba($color-warning, 0.2) 0%, transparent 70%);-->
<!--    top: 50%;-->
<!--    left: 10%;-->
<!--  }-->
<!--}-->

<!--.login-card {-->
<!--  width: 100%;-->
<!--  max-width: 420px;-->
<!--  background: $glass-bg;-->
<!--  backdrop-filter: $glass-blur;-->
<!--  border-radius: $border-radius-lg;-->
<!--  box-shadow: $shadow-lg;-->
<!--  padding: $spacing-xl;-->
<!--  position: relative;-->
<!--  z-index: 1;-->
<!--}-->

<!--.login-header {-->
<!--  text-align: center;-->
<!--  margin-bottom: $spacing-xl;-->

<!--  .logo {-->
<!--    display: flex;-->
<!--    align-items: center;-->
<!--    justify-content: center;-->
<!--    gap: $spacing-sm;-->
<!--    margin-bottom: $spacing-sm;-->

<!--    .logo-icon {-->
<!--      font-size: 40px;-->
<!--    }-->

<!--    .logo-text {-->
<!--      font-size: $font-size-title;-->
<!--      font-weight: $font-weight-bold;-->
<!--      color: $color-primary;-->
<!--    }-->
<!--  }-->

<!--  .login-subtitle {-->
<!--    color: $color-text-secondary;-->
<!--    font-size: $font-size-sm;-->
<!--  }-->
<!--}-->

<!--.role-tabs {-->
<!--  display: grid;-->
<!--  grid-template-columns: repeat(4, 1fr);-->
<!--  gap: $spacing-sm;-->
<!--  margin-bottom: $spacing-xl;-->

<!--  .role-tab {-->
<!--    display: flex;-->
<!--    flex-direction: column;-->
<!--    align-items: center;-->
<!--    gap: $spacing-xs;-->
<!--    padding: $spacing-md $spacing-sm;-->
<!--    border-radius: $border-radius-base;-->
<!--    cursor: pointer;-->
<!--    transition: all $transition-fast;-->
<!--    background: $color-bg-white;-->
<!--    border: 2px solid transparent;-->

<!--    &:hover {-->
<!--      background: $color-bg-light;-->
<!--    }-->

<!--    &.active {-->
<!--      border-color: $color-primary;-->
<!--      background: rgba($color-primary, 0.05);-->

<!--      .role-name {-->
<!--        color: $color-primary;-->
<!--      }-->
<!--    }-->

<!--    .role-icon {-->
<!--      font-size: 24px;-->
<!--    }-->

<!--    .role-name {-->
<!--      font-size: $font-size-xs;-->
<!--      color: $color-text-secondary;-->
<!--      transition: color $transition-fast;-->
<!--    }-->
<!--  }-->
<!--}-->

<!--.login-form {-->
<!--  .el-form-item {-->
<!--    margin-bottom: $spacing-lg;-->
<!--  }-->

<!--  .login-btn {-->
<!--    width: 100%;-->
<!--    height: 44px;-->
<!--    font-size: $font-size-md;-->
<!--    border-radius: $border-radius-base;-->
<!--  }-->
<!--}-->

<!--.test-accounts {-->
<!--  margin-top: $spacing-lg;-->

<!--  :deep(.el-collapse) {-->
<!--    border: none;-->
<!--    -->
<!--    .el-collapse-item__header {-->
<!--      background: transparent;-->
<!--      border: none;-->
<!--      font-size: $font-size-sm;-->
<!--      color: $color-text-secondary;-->
<!--      height: 36px;-->
<!--      line-height: 36px;-->
<!--    }-->

<!--    .el-collapse-item__wrap {-->
<!--      border: none;-->
<!--      background: transparent;-->
<!--    }-->

<!--    .el-collapse-item__content {-->
<!--      padding-bottom: 0;-->
<!--    }-->
<!--  }-->

<!--  .account-list {-->
<!--    display: flex;-->
<!--    flex-direction: column;-->
<!--    gap: $spacing-sm;-->
<!--  }-->

<!--  .account-item {-->
<!--    display: flex;-->
<!--    justify-content: space-between;-->
<!--    align-items: center;-->
<!--    padding: $spacing-sm $spacing-md;-->
<!--    background: $color-bg-light;-->
<!--    border-radius: $border-radius-sm;-->
<!--    cursor: pointer;-->
<!--    transition: all $transition-fast;-->

<!--    &:hover {-->
<!--      background: rgba($color-primary, 0.1);-->
<!--    }-->

<!--    .account-role {-->
<!--      font-size: $font-size-sm;-->
<!--      color: $color-text-primary;-->
<!--      font-weight: $font-weight-medium;-->
<!--    }-->

<!--    .account-info {-->
<!--      font-size: $font-size-xs;-->
<!--      color: $color-text-secondary;-->
<!--      font-family: monospace;-->
<!--    }-->
<!--  }-->
<!--}-->

<!--.login-footer {-->
<!--  margin-top: $spacing-xl;-->
<!--  text-align: center;-->
<!--  color: $color-text-placeholder;-->
<!--  font-size: $font-size-xs;-->
<!--}-->
<!--</style>-->
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
        <div class="w-32 h-32 bg-white rounded-3xl flex items-center justify-center shadow-2xl mb-8 animate-float">
          <span class="text-7xl">🍔</span>
        </div>

        <!-- 标题 -->
        <h1 class="text-5xl font-black tracking-wider mb-4">校食达</h1>
        <p class="text-xl text-orange-100 font-medium mb-2">校园外卖配送平台</p>
        <p class="text-orange-200">UniEat · 专属于校园的美食配送</p>

        <!-- 功能卡片 -->
        <div class="mt-12 grid grid-cols-3 gap-8 text-center">
          <div class="bg-white/10 backdrop-blur-sm rounded-2xl p-6">
            <div class="text-3xl mb-2">🎓</div>
            <p class="text-sm text-orange-100">学生端</p>
          </div>
          <div class="bg-white/10 backdrop-blur-sm rounded-2xl p-6">
            <div class="text-3xl mb-2">🏪</div>
            <p class="text-sm text-orange-100">商家端</p>
          </div>
          <div class="bg-white/10 backdrop-blur-sm rounded-2xl p-6">
            <div class="text-3xl mb-2">🚴</div>
            <p class="text-sm text-orange-100">骑手端</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
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

        <!-- 登录卡片 -->
        <div class="bg-white rounded-3xl shadow-xl p-8">
          <!-- 标题 -->
          <div class="text-center mb-8">
            <h2 class="text-2xl font-black text-gray-800">欢迎登录</h2>
            <p class="text-gray-500 mt-2">请选择角色并输入账号密码</p>
          </div>

          <!-- 角色选择 -->
          <div class="grid grid-cols-4 gap-3 mb-6">
            <div
                v-for="role in roles"
                :key="role.value"
                class="flex flex-col items-center gap-2 p-3 rounded-2xl cursor-pointer transition-all border-2"
                :class="currentRole === role.value ? 'border-orange-500 bg-orange-50' : 'border-transparent bg-gray-50 hover:bg-gray-100'"
                @click="currentRole = role.value"
            >
              <span class="text-2xl">{{ role.icon }}</span>
              <span class="text-xs font-medium" :class="currentRole === role.value ? 'text-orange-600' : 'text-gray-600'">
                {{ role.label }}
              </span>
            </div>
          </div>

          <!-- 登录表单 -->
          <div class="space-y-5">
            <!-- 账号输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">👤</span>
              <input
                  v-model="loginForm.account"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  :placeholder="accountPlaceholder"
                  type="text"
              />
            </div>

            <!-- 密码输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">🔒</span>
              <input
                  v-model="loginForm.password"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  placeholder="请输入密码"
                  type="password"
                  @keyup.enter="handleLogin"
              />
            </div>
          </div>

          <!-- 记住我和忘记密码 -->
          <div class="flex items-center justify-between mt-6 text-sm">
            <label class="flex items-center gap-2 cursor-pointer text-gray-500">
              <input v-model="rememberMe" class="accent-orange-500 w-4 h-4" type="checkbox"/>
              <span>记住我</span>
            </label>
            <a @click="goToForgotPassword" class="text-orange-500 hover:text-orange-600 font-medium cursor-pointer">忘记密码？</a>
          </div>

          <!-- 登录按钮 -->
          <div class="mt-8">
            <button
                class="w-full block text-center bg-orange-500 hover:bg-orange-600 text-white font-black py-4 rounded-2xl shadow-xl shadow-orange-200 transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="loading"
                @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </div>

          <!-- 测试账号 -->
          <div class="mt-6">
            <el-collapse>
              <el-collapse-item title="📋 测试账号（点击快速填充）" name="1">
                <div class="space-y-2">
                  <div
                      v-for="(account, role) in testAccounts"
                      :key="role"
                      class="flex justify-between items-center p-3 bg-gray-50 rounded-xl cursor-pointer hover:bg-orange-50 transition-all"
                      @click="fillTestAccount(role)"
                  >
                    <span class="text-sm font-medium text-gray-700">{{ account.role }}</span>
                    <span class="text-xs text-gray-500 font-mono">{{ account.username }} / {{ account.password }}</span>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>

        <!-- 底部链接 -->
        <div class="mt-6 text-center">
          <div class="text-xs text-gray-400 mb-3">
            <label class="flex items-center justify-center gap-2 cursor-pointer">
              <input checked class="accent-orange-500" type="checkbox"/>
              <span>登录即表示同意《用户服务协议》和《隐私政策》</span>
            </label>
          </div>
          <div class="text-sm text-gray-500">
            没有账号？
            <a @click="goToRegister" class="text-orange-500 hover:text-orange-600 font-medium cursor-pointer">
              立即注册
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElCollapse, ElCollapseItem } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const router = useRouter()
const userStore = useUserStore()

// 角色配置
const roles = [
  { value: 'student', label: '学生', icon: '🎓' },
  { value: 'merchant', label: '商家', icon: '🏪' },
  { value: 'rider', label: '骑手', icon: '🚴' },
  { value: 'admin', label: '管理员', icon: '👔' }
]

// 当前选择的角色
const currentRole = ref('student')

// 加载状态
const loading = ref(false)

// 记住我
const rememberMe = ref(false)

// 登录表单
const loginForm = ref({
  account: '',
  password: ''
})

// 测试账号（使用手机号格式）
const testAccounts = {
  student: { role: '学生', username: '13800138001', password: '123456' },
  merchant: { role: '商家', username: '13800138002', password: '123456' },
  rider: { role: '骑手', username: '13800138003', password: '123456' },
  admin: { role: '管理员', username: '13800138000', password: '123456' }
}

// 账号输入框占位符（手机号登录）
const accountPlaceholder = computed(() => {
  return '请输入手机号'
})

// 手机号验证
const isValidPhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 处理登录
const handleLogin = async () => {
  // 简单验证
  if (!loginForm.value.account || !loginForm.value.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }

  // 手机号格式验证
  if (!isValidPhone(loginForm.value.account)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }

  loading.value = true

  try {
    // 调用后端登录接口（手机号登录）
    const userInfo = await request.post('/user/login', {
      phone: loginForm.value.account,
      password: loginForm.value.password
    })

    // 检查返回数据
    if (!userInfo || !userInfo.id) {
      ElMessage.error('登录失败：服务器返回数据异常')
      return
    }

    // 验证角色是否匹配
    if (userInfo.role !== currentRole.value &&
        !(currentRole.value === 'rider' && userInfo.role === 'student' && userInfo.isRider)) {
      ElMessage.error('账号角色不匹配')
      return
    }

    // 保存用户信息（传入登录 Tab，骑手账号在库中为 student，需把 currentRole 设为 rider）
    userStore.setUserInfo(userInfo, currentRole.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo))

    ElMessage.success('登录成功')

    router.push(userStore.redirectPath || '/student')

  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查手机号密码')
  } finally {
    loading.value = false
  }
}




// 填充测试账号（使用手机号格式）
const fillTestAccount = (role) => {
  currentRole.value = role

  const accounts = {
    student: { account: '13800000001', password: '123456' },
    merchant: { account: '13800000010', password: '123456' },
    rider: { account: '13800000020', password: '123456' },
    admin: { account: '13800000000', password: '123456' }
  }

  loginForm.value = { ...accounts[role] }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 跳转到忘记密码页面
const goToForgotPassword = () => {
  router.push('/forgot-password')
}

</script>

<style scoped>
/* Tailwind CSS 已经处理了所有样式，这里不需要额外样式 */
</style>
