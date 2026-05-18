<template>
  <div class="rider-apply-page">
    <!-- 头部 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" text @click="goBack">返回</el-button>
      <h1 class="page-title">申请成为骑手</h1>
      <div></div>
    </div>

    <!-- 申请说明 -->
    <div class="intro-section">
      <div class="intro-icon">🛵</div>
      <h2 class="intro-title">加入校园骑手团队</h2>
      <p class="intro-desc">利用课余时间，赚取零花钱，锻炼自己</p>
      <div class="benefits">
        <div class="benefit-item">
          <span class="benefit-icon">💰</span>
          <span>灵活收入</span>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">⏰</span>
          <span>自由时间</span>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">🎓</span>
          <span>校园专属</span>
        </div>
      </div>
    </div>

    <!-- 申请表单 -->
    <div class="form-section" v-if="!existingApplication">
      <el-form
        ref="formRef"
        :model="applyForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="applyForm.name" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="applyForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="可配送时间段" prop="availableTime">
          <el-checkbox-group v-model="applyForm.availableTime">
            <el-checkbox label="08:00-12:00">上午 (8:00-12:00)</el-checkbox>
            <el-checkbox label="12:00-14:00">午间 (12:00-14:00)</el-checkbox>
            <el-checkbox label="17:00-19:00">傍晚 (17:00-19:00)</el-checkbox>
            <el-checkbox label="19:00-22:00">晚间 (19:00-22:00)</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="agreeTerms">
            我已阅读并同意《骑手服务协议》
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            :disabled="!agreeTerms"
            @click="submitApply"
          >
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 已有申请状态 -->
    <div class="application-status" v-if="existingApplication">
      <div class="status-card">
        <div class="status-icon" :class="existingApplication.status">
          {{ existingApplication.status === 'pending' ? '⏳' : existingApplication.status === 'approved' ? '✅' : '❌' }}
        </div>
        <div class="status-info">
          <h3>{{ existingApplication.status === 'pending' ? '审核中' : existingApplication.status === 'approved' ? '已通过' : '已拒绝' }}</h3>
          <p v-if="existingApplication.status === 'pending'">您的申请正在审核中，请耐心等待</p>
          <p v-else-if="existingApplication.status === 'approved'">恭喜！您已成为骑手，可以开始接单了</p>
          <p v-else>很抱歉，您的申请未通过审核</p>
        </div>
      </div>
    </div>

    <!-- 申请须知 -->
    <div class="notice-section">
      <h3 class="notice-title">申请须知</h3>
      <ul class="notice-list">
        <li>申请人必须是本校在读学生</li>
        <li>需要有稳定的课余时间进行配送</li>
        <li>审核通过后即可开始接单</li>
        <li>配送收入按单结算，每周发放</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const formRef = ref(null)

// 申请表单
const applyForm = ref({
  name: userStore.userName || '',
  phone: userStore.userPhone || '',
  availableTime: []
})

// 同意条款
const agreeTerms = ref(false)

// 提交状态
const submitting = ref(false)

// 已有申请
const existingApplication = ref(null)

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  availableTime: [
    { type: 'array', required: true, message: '请选择可配送时间段', trigger: 'change' }
  ]
}

// 返回
const goBack = () => {
  router.back()
}

// 检查是否已有申请
const checkExistingApplication = async () => {
  if (!userStore.userId) return
  try {
    const result = await request.get(`/rider/application/${userStore.userId}`)
    if (result) {
      existingApplication.value = result
    }
  } catch (e) {
    // 没有申请记录
  }
}

// 提交申请
const submitApply = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true

    try {
      await request.post('/rider/apply', {
        studentId: userStore.userId,
        studentName: applyForm.value.name,
        phone: applyForm.value.phone,
        availableTime: JSON.stringify(applyForm.value.availableTime)
      })

      ElMessage.success('申请已提交，请等待审核')
      router.back()
    } catch (error) {
      ElMessage.error(error.message || '申请提交失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  checkExistingApplication()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.rider-apply-page {
  min-height: 100vh;
  background: $color-bg-base;
}

// 头部
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-base;
  background: $color-bg-white;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;

  .page-title {
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
  }
}

// 介绍区域
.intro-section {
  text-align: center;
  padding: $spacing-xl $spacing-base;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
  color: white;

  .intro-icon {
    font-size: 60px;
    margin-bottom: $spacing-md;
  }

  .intro-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    margin-bottom: $spacing-sm;
  }

  .intro-desc {
    opacity: 0.9;
    margin-bottom: $spacing-lg;
  }

  .benefits {
    display: flex;
    justify-content: center;
    gap: $spacing-xl;

    .benefit-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: $spacing-xs;

      .benefit-icon {
        font-size: 24px;
      }

      span:last-child {
        font-size: $font-size-sm;
      }
    }
  }
}

// 表单区域
.form-section {
  background: $color-bg-white;
  margin: $spacing-base;
  padding: $spacing-lg;
  border-radius: $border-radius-md;

  .el-button {
    width: 100%;
  }

  .el-checkbox-group {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }
}

// 须知区域
.notice-section {
  background: $color-bg-white;
  margin: 0 $spacing-base $spacing-base;
  padding: $spacing-lg;
  border-radius: $border-radius-md;

  .notice-title {
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
    margin-bottom: $spacing-md;
  }

  .notice-list {
    padding-left: $spacing-lg;

    li {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      line-height: $line-height-loose;
      list-style: disc;
    }
  }
}

// 申请状态
.application-status {
  margin: $spacing-base;

  .status-card {
    background: $color-bg-white;
    padding: $spacing-xl;
    border-radius: $border-radius-md;
    text-align: center;

    .status-icon {
      font-size: 60px;
      margin-bottom: $spacing-md;

      &.pending {
        animation: pulse 2s infinite;
      }
    }

    .status-info {
      h3 {
        font-size: $font-size-lg;
        font-weight: $font-weight-bold;
        margin-bottom: $spacing-sm;
      }

      p {
        color: $color-text-secondary;
        font-size: $font-size-sm;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
