<template>
  <div class="admin-profile">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <el-button type="primary" @click="goBack">返回工作台</el-button>
    </div>

    <el-card class="info-card" shadow="never">
      <template #header>
        <span class="card-title">账号资料</span>
      </template>
      <el-form :model="form" label-width="100px" class="profile-form">
        <el-form-item label="头像">
          <div class="avatar-row">
            <div class="avatar-preview">
              <img v-if="avatarSrc" :src="avatarSrc" alt="" />
              <span v-else class="avatar-fallback">{{ form.name?.charAt(0) || '管' }}</span>
            </div>
            <div class="avatar-actions">
              <el-upload
                class="avatar-uploader"
                action="http://localhost:8080/api/file/upload"
                :show-file-list="false"
                :on-success="onUploadSuccess"
                :on-error="onUploadError"
                :before-upload="beforeUpload"
                accept=".jpg,.jpeg,.png,.gif,.webp"
              >
                <el-button class="btn-upload" size="small">上传图片</el-button>
              </el-upload>
              <el-input
                v-model="form.avatar"
                placeholder="或填写 /img/xxx.png 等地址"
                clearable
                class="avatar-input"
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="登录账号">
          <span class="readonly-text">{{ userStore.userInfo?.username || '—' }}</span>
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" maxlength="20" placeholder="联系电话" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  name: '',
  phone: '',
  avatar: ''
})
const saving = ref(false)

const avatarSrc = computed(() => getImageUrl(form.value.avatar || ''))

const syncForm = () => {
  form.value = {
    name: userStore.userName || '',
    phone: userStore.userPhone || '',
    avatar: userStore.userInfo?.avatar || ''
  }
}

const goBack = () => {
  router.push('/admin/dashboard')
}

const beforeUpload = (file) => {
  const ok = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!ok) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP')
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片不能超过 5MB')
    return false
  }
  return true
}

const onUploadSuccess = (res) => {
  if (res?.code === 200 && res?.data?.url) {
    form.value.avatar = res.data.url
    ElMessage.success('已上传，请点击保存生效')
  } else {
    ElMessage.error(res?.message || '上传失败')
  }
}

const onUploadError = () => {
  ElMessage.error('上传失败')
}

const save = async () => {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请填写姓名')
    return
  }
  if (!form.value.phone?.trim()) {
    ElMessage.warning('请填写手机号')
    return
  }
  saving.value = true
  try {
    const ok = await userStore.updateUserInfo({
      name: form.value.name.trim(),
      phone: form.value.phone.trim(),
      avatar: form.value.avatar?.trim() || ''
    })
    if (ok) {
      await userStore.refreshUserInfo()
      syncForm()
      ElMessage.success('已保存')
    } else {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await userStore.refreshUserInfo()
  syncForm()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.admin-profile {
  max-width: 640px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;

  .page-title {
    margin: 0;
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }
}

/* 白底深字，避免 primary plain 在浅蓝底上对比度不足 */
.btn-header-action {
  color: $color-text-primary !important;
  background: $color-bg-white !important;
  border: 1px solid $color-border-base !important;
  font-weight: $font-weight-medium;

  &:hover {
    color: $color-primary !important;
    border-color: $color-primary-light !important;
    background: rgba($color-primary, 0.06) !important;
  }
}

.btn-upload {
  color: $color-text-primary !important;
  background: $color-bg-white !important;
  border: 1px solid $color-border-base !important;
  font-weight: $font-weight-medium;

  &:hover {
    color: $color-primary !important;
    border-color: $color-primary-light !important;
    background: rgba($color-primary, 0.08) !important;
  }
}

.info-card {
  border-radius: $border-radius-md;

  .card-title {
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }
}

.profile-form {
  max-width: 480px;
}

.readonly-text {
  color: $color-text-secondary;
}

.avatar-row {
  display: flex;
  gap: $spacing-lg;
  align-items: flex-start;
  flex-wrap: wrap;
}

.avatar-preview {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  background: $color-bg-light;
  border: 1px solid $color-border-lighter;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
  color: $color-text-secondary;
}

.avatar-actions {
  flex: 1;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.avatar-input {
  max-width: 360px;
}
</style>
