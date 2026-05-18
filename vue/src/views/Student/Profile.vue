<template>
  <div class="profile-page">
    <section class="profile-hero">
      <div class="hero-main">
        <el-upload
          class="avatar-uploader"
          action="http://localhost:8080/api/file/upload"
          :show-file-list="false"
          :on-success="onAvatarUploadSuccess"
          :on-error="onAvatarUploadError"
          :before-upload="beforeAvatarUpload"
          accept=".jpg,.jpeg,.png,.gif,.webp"
        >
          <div
            class="avatar-wrap"
            :class="{ loading: avatarSaving }"
            title="点击更换头像"
          >
            <img
              v-if="avatarDisplaySrc"
              :src="avatarDisplaySrc"
              class="avatar-img"
              alt=""
            />
            <div v-else class="avatar-placeholder">{{ userStore.userName?.charAt(0) || '用' }}</div>
          </div>
        </el-upload>
        <div class="hero-text">
          <h2 class="hero-name">{{ userStore.userName }}</h2>
        </div>
      </div>
      <el-button
        v-if="userStore.canSwitchToRider"
        type="primary"
        class="btn-switch-rider"
        round
        @click="switchToRider"
      >
        <span class="btn-switch-icon">🛵</span>
        切换骑手端
      </el-button>
    </section>

    <p class="section-label">常用功能</p>
    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-item" @click="goToOrders">
        <span class="menu-icon">📋</span>
        <span class="menu-label">我的订单</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="goToMyReviews">
        <span class="menu-icon">⭐</span>
        <span class="menu-label">我的评价</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="goToMyCoupons">
        <span class="menu-icon">🎟️</span>
        <span class="menu-label">我的优惠券</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="showAddressDialog = true">
        <span class="menu-icon">📍</span>
        <span class="menu-label">收货地址</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="goToRiderApply" v-if="!userStore.isRider">
        <span class="menu-icon">🛵</span>
        <span class="menu-label">申请成为骑手</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" v-else>
        <span class="menu-icon">✅</span>
        <span class="menu-label">已认证骑手</span>
        <el-tag type="success" size="small">已通过</el-tag>
      </div>
    </div>

    <p class="section-label">其他</p>
    <!-- 其他设置 -->
    <div class="menu-section">
      <div class="menu-item" @click="showAbout">
        <span class="menu-icon">ℹ️</span>
        <span class="menu-label">关于我们</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="showHelp">
        <span class="menu-icon">❓</span>
        <span class="menu-label">帮助中心</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <el-button type="danger" plain size="large" @click="handleLogout">
        退出登录
      </el-button>
    </div>

    <!-- 地址管理弹窗 -->
    <el-dialog
      v-model="showAddressDialog"
      title="收货地址管理"
      width="90%"
      class="address-manage-dialog"
    >
      <div class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-item"
        >
          <div class="address-info">
            <div class="address-main">
              {{ addr.building }} {{ addr.room }}
              <el-tag v-if="addr.isDefault" type="info" effect="light" size="small" class="tag-default-addr">默认</el-tag>
            </div>
            <div class="address-contact">{{ addr.contact }} {{ addr.phone }}</div>
          </div>
          <div class="address-actions">
            <el-button size="small" text @click="setDefaultAddress(addr.id)" v-if="!addr.isDefault">
              设为默认
            </el-button>
            <el-button size="small" text class="btn-edit-addr" @click="editAddress(addr)">
              编辑
            </el-button>
            <el-button size="small" text type="danger" @click="deleteAddress(addr.id)">
              删除
            </el-button>
          </div>
        </div>
        <div v-if="addresses.length === 0" class="empty-address">
          暂无收货地址
        </div>
      </div>
      <div class="add-address" @click="showNewAddressForm = true">
        <el-icon><Plus /></el-icon>
        新增地址
      </div>

      <!-- 新增/编辑地址表单 -->
      <el-form v-if="showNewAddressForm" :model="addressForm" label-position="top" class="new-address-form">
        <el-form-item label="楼栋">
          <el-input v-model="addressForm.building" placeholder="如：学生公寓1号楼" />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="addressForm.room" placeholder="如：301" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="addressForm.contact" placeholder="收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addressForm.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
        <div class="form-actions">
          <el-button @click="cancelAddressForm">取消</el-button>
          <el-button type="primary" @click="saveAddress">保存</el-button>
        </div>
      </el-form>
    </el-dialog>

    <!-- 关于弹窗 -->
    <el-dialog v-model="aboutVisible" title="关于我们" width="90%">
      <div class="about-content">
        <div class="about-logo">🍔</div>
        <h3>校园专属外卖配送系统</h3>
        <p>版本 1.0.0</p>
        <p class="about-desc">
          专为校园用户打造的外卖配送平台，提供便捷、高效的校园外卖服务。
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const userStore = useUserStore()

const avatarDisplaySrc = computed(() => getImageUrl(userStore.userAvatar))
const avatarSaving = ref(false)

const beforeAvatarUpload = (file) => {
  const ok = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!ok) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP')
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片不能超过 5MB')
    return false
  }
  avatarSaving.value = true
  return true
}

const onAvatarUploadSuccess = async (response) => {
  try {
    if (response?.code === 200 && response?.data?.url) {
      const ok = await userStore.updateUserInfo({ avatar: response.data.url })
      if (ok) {
        await userStore.refreshUserInfo()
        ElMessage.success('头像已更新')
      } else {
        ElMessage.error('保存头像失败')
      }
    } else {
      ElMessage.error(response?.message || '上传失败')
    }
  } finally {
    avatarSaving.value = false
  }
}

const onAvatarUploadError = () => {
  avatarSaving.value = false
  ElMessage.error('上传失败，请重试')
}

// 关于弹窗
const aboutVisible = ref(false)

// 地址管理
const showAddressDialog = ref(false)
const showNewAddressForm = ref(false)
const addresses = ref([])
const editingAddressId = ref(null)
const addressForm = ref({
  building: '',
  room: '',
  contact: '',
  phone: '',
  isDefault: false
})

// 加载地址列表
const loadAddresses = async () => {
  if (!userStore.userId) return
  try {
    const result = await request.get(`/address/${userStore.userId}`)
    addresses.value = result || []
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

// 设为默认地址
const setDefaultAddress = async (id) => {
  try {
    await request.put(`/address/default/${id}`)
    ElMessage.success('设置成功')
    loadAddresses()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

// 编辑地址
const editAddress = (addr) => {
  editingAddressId.value = addr.id
  addressForm.value = {
    building: addr.building,
    room: addr.room,
    contact: addr.contact,
    phone: addr.phone,
    isDefault: addr.isDefault
  }
  showNewAddressForm.value = true
}

// 删除地址
const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/address/${id}`)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (e) {
    // 用户取消或删除失败
  }
}

// 保存地址
const saveAddress = async () => {
  if (!addressForm.value.building || !addressForm.value.room || 
      !addressForm.value.contact || !addressForm.value.phone) {
    ElMessage.warning('请填写完整地址信息')
    return
  }

  try {
    if (editingAddressId.value) {
      // 更新地址
      await request.put('/address/update', {
        id: editingAddressId.value,
        userId: userStore.userId,
        ...addressForm.value
      })
      ElMessage.success('更新成功')
    } else {
      // 新增地址
      await request.post('/address/add', {
        userId: userStore.userId,
        ...addressForm.value
      })
      ElMessage.success('添加成功')
    }
    cancelAddressForm()
    loadAddresses()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 取消地址表单
const cancelAddressForm = () => {
  showNewAddressForm.value = false
  editingAddressId.value = null
  addressForm.value = {
    building: '',
    room: '',
    contact: '',
    phone: '',
    isDefault: false
  }
}

// 切换到骑手模式
const switchToRider = () => {
  const result = userStore.switchRole()
  if (result.success) {
    ElMessage.success('已切换为骑手模式')
    router.push(result.redirectPath)
  }
}

// 跳转到订单页
const goToOrders = () => {
  router.push('/student/orders')
}

const goToMyReviews = () => {
  router.push('/student/my-reviews')
}

const goToMyCoupons = () => {
  router.push('/student/my-coupons')
}

// 跳转到骑手申请
const goToRiderApply = () => {
  router.push('/student/rider-apply')
}

// 显示关于
const showAbout = () => {
  aboutVisible.value = true
}

// 显示帮助
const showHelp = () => {
  ElMessage.info('帮助中心功能开发中')
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

onMounted(async () => {
  await userStore.refreshUserInfo()
  loadAddresses()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.profile-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #eef6ff 0%, #f5f7fa 16%, #f5f7fa 100%);
  padding-bottom: $spacing-xl;
}

.section-label {
  margin: $spacing-base $spacing-lg $spacing-sm;
  font-size: 13px;
  font-weight: $font-weight-semibold;
  color: $color-text-secondary;
  letter-spacing: 0.06em;
}

.profile-hero {
  margin: $spacing-base $spacing-base $spacing-lg;
  padding: $spacing-xl $spacing-lg;
  background: $color-bg-white;
  border-radius: $border-radius-lg;
  box-shadow: 0 4px 20px rgba(74, 144, 217, 0.08);
  border: 1px solid $color-border-extra-light;
}

.hero-main {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.avatar-uploader {
  flex-shrink: 0;
  :deep(.el-upload) {
    border: none;
    background: transparent;
    padding: 0;
  }
}

.avatar-wrap {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid rgba($color-primary, 0.25);
  box-shadow: 0 4px 14px rgba(74, 144, 217, 0.2);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 6px 18px rgba(74, 144, 217, 0.28);
  }

  &.loading {
    opacity: 0.75;
    pointer-events: none;
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(145deg, $color-primary-light 0%, $color-primary 100%);
}

.hero-text {
  flex: 1;
  min-width: 0;
}

.hero-name {
  margin: 0;
  font-size: 20px;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  line-height: 1.3;
}

.btn-switch-rider {
  width: 100%;
  margin-top: $spacing-lg;
  height: 44px;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  box-shadow: 0 4px 12px rgba(74, 144, 217, 0.35);

  .btn-switch-icon {
    margin-right: 6px;
  }
}

// 菜单区域
.menu-section {
  background: $color-bg-white;
  margin: 0 $spacing-base $spacing-lg;
  border-radius: $border-radius-lg;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid $color-border-extra-light;

  .menu-item {
    display: flex;
    align-items: center;
    padding: $spacing-md $spacing-lg;
    min-height: 54px;
    border-bottom: 1px solid $color-border-lighter;
    cursor: pointer;
    transition: background $transition-fast;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: rgba($color-primary, 0.06);
    }

    &:active {
      background: $color-bg-light;
    }

    .menu-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      margin-right: $spacing-md;
      font-size: 20px;
      border-radius: 12px;
      background: linear-gradient(145deg, rgba(74, 144, 217, 0.12) 0%, rgba(74, 144, 217, 0.06) 100%);
    }

    .menu-label {
      flex: 1;
      font-size: 15px;
      color: $color-text-primary;
      font-weight: $font-weight-medium;
    }

    .el-icon {
      color: $color-text-placeholder;
      font-size: 16px;
    }
  }
}

// 退出登录
.logout-section {
  padding: $spacing-lg $spacing-base $spacing-xl;
  text-align: center;

  .el-button {
    width: 100%;
    max-width: 400px;
    border-radius: $border-radius-base;
  }
}

// 关于内容
.about-content {
  text-align: center;
  padding: $spacing-lg 0;

  .about-logo {
    font-size: 60px;
    margin-bottom: $spacing-md;
  }

  h3 {
    font-size: $font-size-lg;
    margin-bottom: $spacing-sm;
    color: $color-text-primary;
  }

  p {
    color: $color-text-regular;
    font-size: $font-size-sm;
  }

  .about-desc {
    margin-top: $spacing-lg;
    line-height: $line-height-loose;
  }
}

// 地址列表
.address-list {
  max-height: 300px;
  overflow-y: auto;

  .address-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-base;
    border: 1px solid $color-border-light;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-md;

    .address-info {
      flex: 1;

      .address-main {
        font-weight: $font-weight-medium;
        margin-bottom: $spacing-xs;
        display: flex;
        align-items: center;
        gap: $spacing-sm;
      }

      .address-contact {
        font-size: $font-size-sm;
        color: $color-text-regular;
      }
    }

    .address-actions {
      display: flex;
      gap: $spacing-xs;
    }
  }

  .empty-address {
    text-align: center;
    padding: $spacing-xl;
    color: $color-text-regular;
    font-size: $font-size-base;
  }
}

.add-address {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-base;
  border: 1px dashed $color-border-base;
  border-radius: $border-radius-base;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all $transition-fast;
  margin-top: $spacing-md;

  .el-icon {
    color: $color-success;
  }

  &:hover {
    border-color: $color-success;
    background: rgba($color-success, 0.06);
    color: $color-text-primary;
  }
}

.new-address-form {
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 1px solid $color-border-lighter;

  :deep(.el-form-item__label) {
    color: $color-text-primary;
    font-weight: $font-weight-semibold;
  }

  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    color: $color-text-primary;
  }

  :deep(.el-input__count) {
    color: $color-text-regular;
    background: transparent;
  }

  :deep(.el-checkbox__label) {
    color: $color-text-primary;
    font-weight: $font-weight-medium;
  }

  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: $color-success;
    border-color: $color-success;
  }

  :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: $color-text-primary;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-md;
  }
}

.address-manage-dialog {
  :deep(.el-dialog__title) {
    color: $color-text-primary;
    font-weight: $font-weight-semibold;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: $color-text-regular;
  }
}

.tag-default-addr {
  --el-tag-bg-color: #{$color-bg-light};
  --el-tag-border-color: #{$color-border-base};
  --el-tag-text-color: #{$color-text-regular};
}

.btn-edit-addr {
  color: $color-text-regular;

  &:hover {
    color: $color-text-primary;
  }
}
</style>
