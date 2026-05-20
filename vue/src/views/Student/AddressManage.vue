<template>
  <div class="address-manage-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <iconify-icon icon="lucide:arrow-left" class="text-xl"></iconify-icon>
      </button>
      <h1 class="page-title">收货地址</h1>
      <div class="header-spacer"></div>
    </div>

    <!-- 地址列表容器 -->
    <div class="content-container">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <iconify-icon icon="lucide:loader-2" class="loading-icon"></iconify-icon>
        <p>加载中...</p>
      </div>

      <!-- 空状态（没有表单时显示） -->
      <div v-else-if="addresses.length === 0 && !showForm" class="empty-state">
        <div class="empty-icon">📍</div>
        <p class="empty-text">暂无收货地址</p>
        <button class="create-btn" @click="showAddForm">添加地址</button>
      </div>

      <!-- 地址列表 -->
      <div v-else class="address-section">
        <div class="address-list">
          <div
            v-for="addr in addresses"
            :key="addr.id"
            class="address-item"
          >
            <div class="address-info">
              <div class="address-main">
                {{ addr.building }} {{ addr.room }}
                <span v-if="addr.isDefault" class="tag-default">默认</span>
              </div>
              <div class="address-contact">{{ addr.contact }} {{ maskPhone(addr.phone) }}</div>
            </div>
            <div class="address-actions">
              <button
                v-if="!addr.isDefault"
                class="action-btn btn-default"
                @click="setDefaultAddress(addr.id)"
              >
                设为默认
              </button>
              <button class="action-btn btn-edit" @click="editAddress(addr)">
                编辑
              </button>
              <button class="action-btn btn-delete" @click="deleteAddress(addr.id)">
                删除
              </button>
            </div>
          </div>
        </div>

        <!-- 新增地址按钮（表单隐藏时显示） -->
        <div v-if="!showForm" class="add-address-btn" @click="showAddForm">
          <iconify-icon icon="lucide:plus" class="icon"></iconify-icon>
          <span>新增地址</span>
        </div>

        <!-- 新增/编辑地址表单 -->
        <div v-if="showForm" class="address-form-card">
          <h3 class="form-title">{{ isEditing ? '编辑地址' : '新增地址' }}</h3>
          <el-form :model="addressForm" label-position="top" class="address-form">
            <el-form-item label="楼栋" required>
              <el-input
                v-model="addressForm.building"
                placeholder="如：学生公寓1号楼"
                clearable
              />
            </el-form-item>
            <el-form-item label="房间号" required>
              <el-input
                v-model="addressForm.room"
                placeholder="如：301"
                clearable
              />
            </el-form-item>
            <el-form-item label="联系人" required>
              <el-input
                v-model="addressForm.contact"
                placeholder="收货人姓名"
                clearable
              />
            </el-form-item>
            <el-form-item label="手机号" required>
              <el-input
                v-model="addressForm.phone"
                placeholder="联系电话"
                clearable
                maxlength="11"
              />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
            </el-form-item>
          </el-form>
          <div class="form-actions">
            <el-button @click="cancelForm">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveAddress">
              保存
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const router = useRouter()
const userStore = useUserStore()

// 地址列表
const addresses = ref([])
const loading = ref(false)

// 表单状态
const showForm = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const saving = ref(false)

// 地址表单
const addressForm = ref({
  building: '',
  room: '',
  contact: '',
  phone: '',
  isDefault: false
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 加载地址列表
const loadAddresses = async () => {
  if (!userStore.userId) return
  loading.value = true
  try {
    const result = await request.get(`/address/${userStore.userId}`)
    addresses.value = result || []
  } catch (error) {
    console.error('加载地址失败:', error)
    ElMessage.error('加载地址失败')
  } finally {
    loading.value = false
  }
}

// 显示新增表单
const showAddForm = () => {
  isEditing.value = false
  editingId.value = null
  addressForm.value = {
    building: '',
    room: '',
    contact: '',
    phone: '',
    isDefault: addresses.value.length === 0 // 第一个地址默认设为默认
  }
  showForm.value = true
}

// 编辑地址
const editAddress = (addr) => {
  isEditing.value = true
  editingId.value = addr.id
  addressForm.value = {
    building: addr.building,
    room: addr.room,
    contact: addr.contact,
    phone: addr.phone,
    isDefault: addr.isDefault
  }
  showForm.value = true
  // 滚动到表单
  setTimeout(() => {
    const formElement = document.querySelector('.address-form-card')
    if (formElement) {
      formElement.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, 100)
}

// 取消表单
const cancelForm = () => {
  showForm.value = false
  addressForm.value = {
    building: '',
    room: '',
    contact: '',
    phone: '',
    isDefault: false
  }
}

// 保存地址
const saveAddress = async () => {
  // 验证表单
  if (!addressForm.value.building?.trim()) {
    ElMessage.warning('请输入楼栋')
    return
  }
  if (!addressForm.value.room?.trim()) {
    ElMessage.warning('请输入房间号')
    return
  }
  if (!addressForm.value.contact?.trim()) {
    ElMessage.warning('请输入联系人')
    return
  }
  if (!addressForm.value.phone?.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1\d{10}$/.test(addressForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  saving.value = true
  try {
    if (isEditing.value) {
      // 更新地址
      await request.put('/address/update', {
        id: editingId.value,
        userId: userStore.userId,
        building: addressForm.value.building.trim(),
        room: addressForm.value.room.trim(),
        contact: addressForm.value.contact.trim(),
        phone: addressForm.value.phone.trim(),
        isDefault: addressForm.value.isDefault
      })
      ElMessage.success('更新成功')
    } else {
      // 新增地址
      await request.post('/address/add', {
        userId: userStore.userId,
        building: addressForm.value.building.trim(),
        room: addressForm.value.room.trim(),
        contact: addressForm.value.contact.trim(),
        phone: addressForm.value.phone.trim(),
        isDefault: addressForm.value.isDefault
      })
      ElMessage.success('添加成功')
    }
    cancelForm()
    await loadAddresses()
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 设为默认地址
const setDefaultAddress = async (id) => {
  try {
    await request.put(`/address/default/${id}`)
    ElMessage.success('设置成功')
    await loadAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置失败')
  }
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
    await loadAddresses()
  } catch (error) {
    // 用户取消
  }
}

// 手机号脱敏
const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.address-manage-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: #f5f7fa;
}

/* 页面头部 */
.page-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #EBEEF5;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:active {
  background: #e4e7ed;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-spacer {
  width: 36px;
}

/* 内容容器 */
.content-container {
  padding: 16px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-icon {
  font-size: 32px;
  color: #FF7A45;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  font-size: 16px;
  color: #303133;
  margin-bottom: 24px;
}

.create-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #FF7A45 0%, #FFB84D 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.create-btn:active {
  opacity: 0.8;
}

/* 地址区域 */
.address-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.address-info {
  flex: 1;
  min-width: 0;
}

.address-main {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-default {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: #FF7A45;
  color: white;
  font-size: 11px;
  border-radius: 4px;
}

.address-contact {
  font-size: 13px;
  color: #606266;
}

.address-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-default {
  background: #ecf5ff;
  color: #409eff;
}

.btn-default:active {
  background: #d9ecff;
}

.btn-edit {
  background: #f5f7fa;
  color: #606266;
}

.btn-edit:active {
  background: #e4e7ed;
}

.btn-delete {
  background: #fef0f0;
  color: #f56c6c;
}

.btn-delete:active {
  background: #fde2e2;
}

/* 新增地址按钮 */
.add-address-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: white;
  border: 2px dashed #FFB84D;
  border-radius: 12px;
  color: #FF7A45;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.add-address-btn:active {
  background: #fff7ed;
}

.add-address-btn .icon {
  font-size: 20px;
}

/* 地址表单卡片 */
.address-form-card {
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.address-form {
  margin-bottom: 16px;
}

.address-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.address-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.address-form :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #606266;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.form-actions .el-button {
  flex: 1;
  height: 44px;
  border-radius: 8px;
  font-size: 15px;
}
</style>
