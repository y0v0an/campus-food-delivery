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

    <!-- 右侧注册表单 -->
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

        <!-- 注册卡片 -->
        <div class="bg-white rounded-3xl shadow-xl p-8">
          <!-- 标题 -->
          <div class="text-center mb-6">
            <h2 class="text-2xl font-black text-gray-800">创建账号</h2>
            <p class="text-gray-500 mt-2">请选择角色并填写注册信息</p>
          </div>

          <!-- 角色选择 -->
          <div class="grid grid-cols-2 gap-3 mb-6">
            <div
                v-for="role in roles"
                :key="role.value"
                class="flex flex-col items-center gap-2 p-3 rounded-2xl cursor-pointer transition-all border-2"
                :class="currentRole === role.value ? 'border-orange-500 bg-orange-50' : 'border-transparent bg-gray-50 hover:bg-gray-100'"
                @click="switchRole(role.value)"
            >
              <span class="text-2xl">{{ role.icon }}</span>
              <span class="text-xs font-medium" :class="currentRole === role.value ? 'text-orange-600' : 'text-gray-600'">
                {{ role.label }}
              </span>
            </div>
          </div>

          <!-- 注册表单 -->
          <el-form
              ref="formRef"
              :model="registerForm"
              :rules="registerRules"
              label-position="top"
              size="large"
              class="register-form"
          >
            <!-- 学生注册表单 -->
            <template v-if="currentRole === 'student'">
              <el-form-item prop="studentId">
                <el-input
                    v-model="registerForm.studentId"
                    placeholder="请输入学号"
                    prefix-icon="User"
                />
              </el-form-item>
              <el-form-item prop="username">
                <el-input
                    v-model="registerForm.username"
                    placeholder="请输入用户名"
                    prefix-icon="User"
                />
              </el-form-item>
              <el-form-item prop="realName">
                <el-input
                    v-model="registerForm.realName"
                    placeholder="请输入真实姓名"
                    prefix-icon="UserFilled"
                />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input
                    v-model="registerForm.phone"
                    placeholder="请输入手机号"
                    prefix-icon="Phone"
                    maxlength="11"
                />
              </el-form-item>
              <el-form-item prop="email">
                <el-input
                    v-model="registerForm.email"
                    placeholder="请输入邮箱（选填）"
                    prefix-icon="Message"
                />
              </el-form-item>
            </template>

            <!-- 骑手注册表单 -->
            <template v-if="currentRole === 'rider'">
              <el-form-item prop="studentId">
                <el-input
                    v-model="registerForm.studentId"
                    placeholder="请输入学号"
                    prefix-icon="User"
                />
              </el-form-item>
              <el-form-item prop="username">
                <el-input
                    v-model="registerForm.username"
                    placeholder="请输入用户名"
                    prefix-icon="User"
                />
              </el-form-item>
              <el-form-item prop="realName">
                <el-input
                    v-model="registerForm.realName"
                    placeholder="请输入真实姓名"
                    prefix-icon="UserFilled"
                />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input
                    v-model="registerForm.phone"
                    placeholder="请输入手机号"
                    prefix-icon="Phone"
                    maxlength="11"
                />
              </el-form-item>
              <el-form-item prop="idCard">
                <el-input
                    v-model="registerForm.idCard"
                    placeholder="请输入身份证号"
                    prefix-icon="Postcard"
                    maxlength="18"
                />
              </el-form-item>
              <el-form-item prop="vehicleType">
                <el-select
                    v-model="registerForm.vehicleType"
                    placeholder="请选择车辆类型"
                    class="w-full"
                >
                  <el-option label="电动车" value="electric_bike" />
                  <el-option label="摩托车" value="motorcycle" />
                  <el-option label="自行车" value="bicycle" />
                </el-select>
              </el-form-item>
              <el-form-item prop="plateNumber">
                <el-input
                    v-model="registerForm.plateNumber"
                    placeholder="请输入车牌号"
                    prefix-icon="Van"
                />
              </el-form-item>
            </template>

            <!-- 密码字段（通用） -->
            <el-form-item prop="password">
              <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码（6-20位，包含字母和数字）"
                  prefix-icon="Lock"
                  show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleRegister"
              />
            </el-form-item>

            <!-- 注册按钮 -->
            <el-button
                type="primary"
                :loading="loading"
                class="register-btn"
                @click="handleRegister"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form>

          <!-- 已有账号 -->
          <div class="mt-6 text-center text-sm text-gray-500">
            已有账号？
            <a @click="router.push('/login')" class="text-orange-500 hover:text-orange-600 font-medium cursor-pointer">
              立即登录
            </a>
          </div>
        </div>

        <!-- 底部提示 -->
        <div class="mt-6 text-center text-xs text-gray-400">
          注册即表示同意《用户服务协议》和《隐私政策》
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/axios/request'

const router = useRouter()

// 角色配置
const roles = [
  { value: 'student', label: '学生', icon: '🎓' },
  { value: 'rider', label: '骑手', icon: '🚴' }
]

// 当前角色
const currentRole = ref('student')

// 加载状态
const loading = ref(false)

// 表单引用
const formRef = ref(null)

// 注册表单
const registerForm = reactive({
  studentId: '',
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  idCard: '',
  vehicleType: '',
  plateNumber: ''
})

// 验证规则
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度为6-20位'))
  } else if (!/[a-zA-Z]/.test(value) || !/\d/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    if (registerForm.confirmPassword) {
      formRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const validatePlateNumber = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入车牌号'))
  } else if (!/^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5}$/.test(value)) {
    callback(new Error('车牌号格式不正确，如：京A12345'))
  } else {
    callback()
  }
}

// 通用验证规则
const commonRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ]
}

// 学生验证规则
const studentRules = {
  ...commonRules,
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

// 骑手验证规则
const riderRules = {
  ...commonRules,
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, validator: validateIdCard, trigger: 'blur' }
  ],
  vehicleType: [
    { required: true, message: '请选择车辆类型', trigger: 'change' }
  ],
  plateNumber: [
    { required: true, validator: validatePlateNumber, trigger: 'blur' }
  ]
}

// 当前验证规则
const registerRules = computed(() => {
  switch (currentRole.value) {
    case 'student':
      return studentRules
    case 'rider':
      return riderRules
    default:
      return commonRules
  }
})

// 切换角色
const switchRole = (role) => {
  currentRole.value = role
  // 清空表单
  Object.keys(registerForm).forEach(key => {
    registerForm[key] = ''
  })
  // 重置验证
  formRef.value?.clearValidate()
}

// 处理注册
const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      let api = ''
      let data = {}

      switch (currentRole.value) {
        case 'student':
          api = '/register/student'
          data = {
            studentId: registerForm.studentId,
            username: registerForm.username,
            password: registerForm.password,
            realName: registerForm.realName,
            phone: registerForm.phone,
            email: registerForm.email || undefined
          }
          break

        case 'rider':
          api = '/register/rider'
          data = {
            studentId: registerForm.studentId,
            username: registerForm.username,
            password: registerForm.password,
            realName: registerForm.realName,
            phone: registerForm.phone,
            idCard: registerForm.idCard,
            vehicleType: registerForm.vehicleType,
            plateNumber: registerForm.plateNumber
          }
          break
      }

      const response = await request.post(api, data)

      ElMessage.success(response.message || '注册成功！')

      // 延迟跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 1500)

    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error(error.message || '注册失败，请重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-form .el-form-item {
  margin-bottom: 18px;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 16px;
  background: linear-gradient(135deg, #FF7A45 0%, #FA541C 100%);
  border: none;
  margin-top: 10px;
}

.register-btn:hover {
  opacity: 0.9;
}
</style>
