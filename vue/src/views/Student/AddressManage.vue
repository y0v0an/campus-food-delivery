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

    <!-- 地址列表 -->
    <div class="address-list-container">
      <!-- 空状态 -->
      <div v-if="addresses.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">📍</div>
        <p class="empty-text">暂无收货地址</p>
        <p class="empty-hint">点击下方按钮添加地址</p>
      </div>

      <!-- 地址卡片列表 -->
      <div v-else class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-card"
          :class="{ 'is-default': addr.isDefault }"
        >
          <div class="address-info">
            <div class="address-main">
              {{ addr.building }} {{ addr.room }}
              <span v-if="addr.isDefault" class="tag-default">默认</span>
            </div>
            <div class="address-contact">{{ addr.contact }} {{ addr.phone }}</div>
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
    </div>

    <!-- 底部新增按钮 -->
    <div class="bottom-bar">
      <button class="add-address-btn" @click="showAddDialog">
        <iconify-icon icon="lucide:plus" class="text-xl"></iconify-icon>
        新增地址
      </button>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="isEditing ? '编辑地址' : '新增地址'"
      width="90%"
      class="address-dialog"
    >
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
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveAddress">
          保存
        </el-button>
      </template>
    </el-dialog>
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

// 弹窗状态
const showDialog = ref(false)
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
</script>
