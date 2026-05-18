<template>
  <div class="merchant-profile">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <el-button type="primary" @click="goBack">返回工作台</el-button>
    </div>

    <el-card class="info-card" shadow="never">
      <template #header>
        <span class="card-title">账号信息</span>
      </template>
      <el-form :model="userForm" label-width="100px" class="profile-form">
        <el-form-item label="登录账号">
          <span class="readonly-text">{{ userStore.userInfo?.username || '—' }}</span>
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="userForm.name" placeholder="用于平台展示的联系人姓名" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="userForm.phone" placeholder="登录账号关联手机号" maxlength="20" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingUser" @click="saveUserInfo">
            保存账号信息
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="merchant" class="info-card" shadow="never">
      <template #header>
        <span class="card-title">店铺资料</span>
      </template>
      <el-form :model="shopForm" label-width="100px" class="profile-form">
        <el-form-item label="店铺门头图">
          <div class="image-upload-container">
            <el-upload
              class="image-uploader"
              action="http://localhost:8080/api/file/upload"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <img v-if="shopForm.logo" :src="getImageUrl(shopForm.logo)" alt="店铺图" class="uploaded-image" />
              <el-icon v-else class="upload-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 jpg、png、gif、webp，最大 5MB；也可在下方填写图片地址</div>
            <el-input v-model="shopForm.logo" placeholder="或粘贴图片 URL（如 /img/xxx.png）" clearable />
          </div>
        </el-form-item>
        <el-form-item label="店铺名称" required>
          <el-input v-model="shopForm.name" placeholder="店铺名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="店铺电话" required>
          <el-input v-model="shopForm.phone" placeholder="顾客联系用的店铺电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="店铺地址" required>
          <el-input v-model="shopForm.address" type="textarea" :rows="2" placeholder="取餐/配送地址" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="店铺简介">
          <el-input v-model="shopForm.description" type="textarea" :rows="3" placeholder="简短介绍，会展示在店铺页" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="配送费">
          <el-input-number v-model="shopForm.deliveryFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="起送价">
          <el-input-number v-model="shopForm.minOrder" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="预计送达">
          <el-input v-model="shopForm.deliveryTime" placeholder="如 30分钟" maxlength="32" />
        </el-form-item>
        <el-form-item label="经营品类">
          <el-select
            v-model="shopForm.categoriesTags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入品类，与菜品分类一致"
            style="width: 100%"
          >
            <el-option-group
              v-for="group in categoryGroups"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="item in group.options"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-option-group>
          </el-select>
          <div class="field-tip">选择适合的分类，方便学生在分类页面找到你的店铺；也可手动输入新分类。</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingShop" @click="saveShopInfo">
            保存店铺资料
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-else-if="loadError" class="info-card" shadow="never">
      <el-empty description="暂未获取到店铺信息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'
import { parseMerchantCategories, categoriesToApiString } from '@/utils/merchantCategories'
import { GROUPED_CATEGORY_OPTIONS as categoryGroups } from '@/utils/categoryConfig'

const router = useRouter()
const userStore = useUserStore()

const merchant = ref(null)
const loadError = ref(false)

const userForm = ref({
  name: '',
  phone: ''
})

const shopForm = ref({
  name: '',
  phone: '',
  address: '',
  description: '',
  logo: '',
  deliveryFee: 0,
  minOrder: 0,
  deliveryTime: '',
  categoriesTags: []
})

const savingUser = ref(false)
const savingShop = ref(false)

const goBack = () => {
  router.push('/merchant/orders')
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/img/')) return `http://localhost:8080${url}`
  return url
}

const beforeUpload = (file) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  const isAllowedType = allowedTypes.includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isAllowedType) {
    ElMessage.error('只能上传 JPG/PNG/GIF/WEBP 格式的图片')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleLogoSuccess = (response) => {
  if (response.code === 200 && response.data) {
    shopForm.value.logo = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const syncUserForm = () => {
  userForm.value = {
    name: userStore.userName || '',
    phone: userStore.userPhone || ''
  }
}

const syncShopForm = (m) => {
  shopForm.value = {
    name: m.name || '',
    phone: m.phone || '',
    address: m.address || '',
    description: m.description || '',
    logo: m.logo || '',
    deliveryFee: m.deliveryFee != null ? Number(m.deliveryFee) : 0,
    minOrder: m.minOrder != null ? Number(m.minOrder) : 0,
    deliveryTime: m.deliveryTime || '30分钟',
    categoriesTags: parseMerchantCategories(m.categories)
  }
}

const loadMerchant = async () => {
  const userId = userStore.userInfo?.id
  if (!userId) {
    loadError.value = true
    return
  }
  try {
    merchant.value = await request.get(`/merchant/user/${userId}`)
    syncShopForm(merchant.value)
  } catch {
    loadError.value = true
  }
}

const saveUserInfo = async () => {
  if (!userForm.value.name?.trim()) {
    ElMessage.warning('请填写姓名')
    return
  }
  if (!userForm.value.phone?.trim()) {
    ElMessage.warning('请填写手机号')
    return
  }
  savingUser.value = true
  try {
    const ok = await userStore.updateUserInfo({
      name: userForm.value.name.trim(),
      phone: userForm.value.phone.trim()
    })
    if (ok) ElMessage.success('账号信息已保存')
    else ElMessage.error('保存失败')
  } finally {
    savingUser.value = false
  }
}

const saveShopInfo = async () => {
  if (!merchant.value?.id) {
    ElMessage.warning('店铺数据未加载')
    return
  }
  if (!shopForm.value.name?.trim()) {
    ElMessage.warning('请填写店铺名称')
    return
  }
  if (!shopForm.value.phone?.trim()) {
    ElMessage.warning('请填写店铺电话')
    return
  }
  if (!shopForm.value.address?.trim()) {
    ElMessage.warning('请填写店铺地址')
    return
  }
  savingShop.value = true
  try {
    const payload = {
      ...merchant.value,
      name: shopForm.value.name.trim(),
      phone: shopForm.value.phone.trim(),
      address: shopForm.value.address.trim(),
      description: shopForm.value.description?.trim() || null,
      logo: shopForm.value.logo,
      deliveryFee: shopForm.value.deliveryFee,
      minOrder: shopForm.value.minOrder,
      deliveryTime: shopForm.value.deliveryTime || '30分钟',
      categories: categoriesToApiString(shopForm.value.categoriesTags)
    }
    await request.put('/merchant/update', payload)
    merchant.value = { ...merchant.value, ...payload }
    syncShopForm(merchant.value)
    if (payload.logo) userStore.setMerchantProfileLogo(payload.logo)
    // 管理员「用户管理」展示的是 users.avatar，与店铺门头图同步
    const logo = shopForm.value.logo?.trim()
    if (logo) {
      const ok = await userStore.updateUserInfo({ avatar: logo })
      if (ok) await userStore.refreshUserInfo()
    }
    ElMessage.success('店铺资料已保存')
  } catch {
    // 拦截器已提示
  } finally {
    savingShop.value = false
  }
}

onMounted(async () => {
  syncUserForm()
  await loadMerchant()
  // 历史数据：已有店铺图但账号未设头像时，补写到 users.avatar，便于管理员用户管理展示
  const m = merchant.value
  if (m?.logo?.trim() && !userStore.userInfo?.avatar) {
    await userStore.updateUserInfo({ avatar: m.logo.trim() })
    await userStore.refreshUserInfo()
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.merchant-profile {
  max-width: 720px;
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

.info-card {
  margin-bottom: $spacing-lg;
  border-radius: $border-radius-md;

  .card-title {
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }
}

.profile-form {
  max-width: 560px;
}

.readonly-text {
  color: $color-text-secondary;
}

.field-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 6px;
}

.image-upload-container {
  width: 100%;

  .image-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      width: 120px;
      height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: border-color 0.3s;

      &:hover {
        border-color: #409eff;
      }
    }
  }

  .uploaded-image {
    width: 120px;
    height: 120px;
    object-fit: cover;
  }

  .upload-icon {
    font-size: 28px;
    color: #8c939d;
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin: 8px 0;
  }
}
</style>
