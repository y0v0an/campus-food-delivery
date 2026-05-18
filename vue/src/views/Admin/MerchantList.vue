<template>
  <div class="merchant-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">商家管理</h1>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商家"
          :prefix-icon="Search"
          clearable
          style="width: 250px; margin-right: 12px"
        />
        <el-button type="primary" :icon="Plus" @click="openAddDialog">新增商家</el-button>
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="merchant-list">
      <el-table :data="filteredMerchants" stripe>
        <el-table-column label="商家Logo" width="100">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.logo)"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 8px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商家名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <span class="rating">
              <el-icon color="#FF7A45"><Star /></el-icon>
              {{ row.rating }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="monthSales" label="月销量" width="100" />
        <el-table-column label="营业状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isOpen ? 'success' : 'info'" size="small">
              {{ row.isOpen ? '营业中' : '休息中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              详情
            </el-button>
            <el-button type="primary" link @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button
              :type="row.isOpen ? 'warning' : 'success'"
              link
              @click="toggleStatus(row)"
            >
              {{ row.isOpen ? '关闭' : '开启' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增 / 编辑商家 -->
    <el-dialog
      v-model="merchantDialogVisible"
      :title="isEditMode ? '编辑商家' : '新增商家'"
      width="620px"
      destroy-on-close
      @closed="onMerchantDialogClosed"
    >
      <el-form ref="formRef" :model="merchantForm" :rules="formRules" label-width="100px">
        <el-form-item label="商家名称" prop="name">
          <el-input v-model="merchantForm.name" placeholder="请输入商家名称" />
        </el-form-item>
        <el-form-item label="登录账号" prop="username">
          <el-input
            v-model="merchantForm.username"
            placeholder="商家登录账号"
            :disabled="isEditMode"
          />
        </el-form-item>
        <el-form-item :label="isEditMode ? '新密码' : '登录密码'" prop="password">
          <el-input
            v-model="merchantForm.password"
            type="password"
            show-password
            :placeholder="isEditMode ? '留空则不修改密码' : '商家登录密码'"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="merchantForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="商家地址" prop="address">
          <el-input v-model="merchantForm.address" placeholder="请输入商家地址" />
        </el-form-item>
        <el-form-item label="预计送达">
          <el-input v-model="merchantForm.deliveryTime" placeholder="如 30分钟" maxlength="32" />
        </el-form-item>
        <el-form-item label="经营品类">
          <el-select
            v-model="merchantForm.categoriesTags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择经营品类"
            style="width: 100%"
          >
            <el-option-group
              v-for="group in GROUPED_CATEGORY_OPTIONS"
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
        </el-form-item>
        <el-form-item label="商家简介">
          <el-input v-model="merchantForm.description" type="textarea" :rows="3" placeholder="请输入商家简介" />
        </el-form-item>
        <el-form-item label="配送费">
          <el-input-number v-model="merchantForm.deliveryFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="起送价">
          <el-input-number v-model="merchantForm.minOrder" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="商家Logo">
          <div class="image-upload-container">
            <el-upload
              class="image-uploader"
              action="http://localhost:8080/api/file/upload"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <img v-if="merchantForm.logo" :src="getImageUrl(merchantForm.logo)" class="uploaded-image" />
              <el-icon v-else class="upload-icon"><Plus /></el-icon>
            </el-upload>
            <el-input v-model="merchantForm.logo" placeholder="或直接输入图片URL" style="margin-top: 8px;" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="merchantDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveMerchant">保存</el-button>
      </template>
    </el-dialog>

    <!-- 商家详情弹窗 -->
    <el-dialog v-model="detailVisible" title="商家详情" width="600px">
      <el-descriptions :column="2" border v-if="currentMerchant">
        <el-descriptions-item label="商家名称" :span="2">
          {{ currentMerchant.name }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentMerchant.phone }}
        </el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentMerchant.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">
          {{ currentMerchant.address }}
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">
          {{ currentMerchant.description }}
        </el-descriptions-item>
        <el-descriptions-item label="月销量">
          {{ currentMerchant.monthSales }}
        </el-descriptions-item>
        <el-descriptions-item label="配送费">
          ¥{{ currentMerchant.deliveryFee }}
        </el-descriptions-item>
        <el-descriptions-item label="起送价">
          ¥{{ currentMerchant.minOrder }}
        </el-descriptions-item>
        <el-descriptions-item label="配送时间">
          {{ currentMerchant.deliveryTime }}
        </el-descriptions-item>
        <el-descriptions-item label="分类" :span="2">
          <el-tag
            v-for="cat in currentMerchant.categories"
            :key="cat"
            size="small"
            style="margin-right: 8px"
          >
            {{ cat }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Search, Star, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/axios/request'
import { parseMerchantCategories, categoriesToApiString } from '@/utils/merchantCategories'
import { MERCHANT_CATEGORY_OPTIONS, GROUPED_CATEGORY_OPTIONS } from '@/utils/categoryConfig'

// 商家列表
const merchants = ref([])

// 搜索关键词
const searchKeyword = ref('')

// 详情弹窗
const detailVisible = ref(false)
const currentMerchant = ref(null)

const merchantDialogVisible = ref(false)
const isEditMode = ref(false)
const editingSnapshot = ref(null)
const formRef = ref(null)
const submitting = ref(false)
const merchantForm = ref({
  id: '',
  userId: '',
  name: '',
  username: '',
  password: '',
  phone: '',
  address: '',
  description: '',
  logo: '',
  deliveryFee: 3,
  minOrder: 15,
  deliveryTime: '30分钟',
  categoriesTags: []
})

const formRules = computed(() => ({
  name: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
  username: [
    { required: !isEditMode.value, message: '请输入登录账号', trigger: 'blur' }
  ],
  password: [
    {
      validator: (_rule, val, cb) => {
        if (!isEditMode.value && (!val || !String(val).trim())) {
          cb(new Error('请输入登录密码'))
        } else {
          cb()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入商家地址', trigger: 'blur' }]
}))

// 过滤后的商家
const filteredMerchants = computed(() => {
  if (!searchKeyword.value) return merchants.value

  const keyword = searchKeyword.value.toLowerCase()
  return merchants.value.filter(m =>
    m.name?.toLowerCase().includes(keyword) ||
    m.address?.toLowerCase().includes(keyword)
  )
})

// 获取图片完整URL
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/img/')) return `http://localhost:8080${url}`
  return url
}

// 加载商家
const loadMerchants = async () => {
  try {
    const result = await request.get('/merchant/all')
    merchants.value = (result || []).map(m => ({
      ...m,
      categories: parseMerchantCategories(m.categories)
    }))
  } catch (error) {
    console.error('加载商家列表失败:', error)
  }
}

const resetMerchantForm = () => {
  merchantForm.value = {
    id: '',
    userId: '',
    name: '',
    username: '',
    password: '123456',
    phone: '',
    address: '',
    description: '',
    logo: '',
    deliveryFee: 3,
    minOrder: 15,
    deliveryTime: '30分钟',
    categoriesTags: []
  }
}

const openAddDialog = () => {
  isEditMode.value = false
  editingSnapshot.value = null
  resetMerchantForm()
  merchantDialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const openEditDialog = async (row) => {
  isEditMode.value = true
  editingSnapshot.value = { ...row }
  let username = ''
  try {
    const u = await request.get(`/user/info/${row.userId}`)
    if (u) username = u.username || ''
  } catch {
    /* ignore */
  }
  const tags = parseMerchantCategories(row.categories)
  merchantForm.value = {
    id: row.id,
    userId: row.userId,
    name: row.name || '',
    username,
    password: '',
    phone: row.phone || '',
    address: row.address || '',
    description: row.description || '',
    logo: row.logo || '',
    deliveryFee: row.deliveryFee != null ? Number(row.deliveryFee) : 0,
    minOrder: row.minOrder != null ? Number(row.minOrder) : 0,
    deliveryTime: row.deliveryTime || '30分钟',
    categoriesTags: tags
  }
  merchantDialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const onMerchantDialogClosed = () => {
  editingSnapshot.value = null
}

// 上传前验证
const beforeUpload = (file) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  const isAllowedType = allowedTypes.includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isAllowedType) {
    ElMessage.error('只能上传 JPG/PNG/GIF/WEBP 格式的图片!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 上传成功处理
const handleUploadSuccess = (response) => {
  if (response.code === 200 && response.data) {
    merchantForm.value.logo = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const saveMerchant = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const tags = merchantForm.value.categoriesTags || []
      const categoriesJson = categoriesToApiString(tags)

      if (isEditMode.value) {
        const snap = editingSnapshot.value
        if (!snap?.id) {
          ElMessage.error('数据异常，请重试')
          return
        }
        const payload = {
          id: snap.id,
          userId: snap.userId,
          name: merchantForm.value.name,
          phone: merchantForm.value.phone,
          address: merchantForm.value.address,
          description: merchantForm.value.description,
          logo: merchantForm.value.logo,
          deliveryFee: merchantForm.value.deliveryFee,
          minOrder: merchantForm.value.minOrder,
          deliveryTime: merchantForm.value.deliveryTime || '30分钟',
          categories: categoriesJson,
          rating: snap.rating,
          monthSales: snap.monthSales,
          isOpen: snap.isOpen,
          lat: snap.lat,
          lng: snap.lng
        }
        const pwd = merchantForm.value.password?.trim()
        await request.put('/merchant/admin/update', payload, {
          params: pwd ? { newPassword: pwd } : {}
        })
        ElMessage.success('商家已更新')
      } else {
        await request.post('/merchant/add', {
          username: merchantForm.value.username,
          password: merchantForm.value.password,
          name: merchantForm.value.name,
          phone: merchantForm.value.phone,
          address: merchantForm.value.address,
          description: merchantForm.value.description,
          logo: merchantForm.value.logo,
          deliveryFee: merchantForm.value.deliveryFee,
          minOrder: merchantForm.value.minOrder,
          deliveryTime: merchantForm.value.deliveryTime,
          categories: categoriesJson
        })
        ElMessage.success('商家添加成功')
      }
      merchantDialogVisible.value = false
      loadMerchants()
    } catch (error) {
      ElMessage.error(error.message || (isEditMode.value ? '更新失败' : '添加失败'))
    } finally {
      submitting.value = false
    }
  })
}

// 查看详情
const viewDetail = (merchant) => {
  currentMerchant.value = { ...merchant }
  detailVisible.value = true
}

// 切换营业状态
const toggleStatus = async (merchant) => {
  try {
    await request.put('/merchant/status', {
      id: merchant.id,
      isOpen: !merchant.isOpen
    })
    ElMessage.success(merchant.isOpen ? '已关闭营业' : '已开启营业')
    loadMerchants()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadMerchants()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.merchant-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;

    .page-title {
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }

    .header-actions {
      display: flex;
      align-items: center;
    }
  }

  .merchant-list {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-base;

    .rating {
      display: flex;
      align-items: center;
      gap: 4px;
      color: $color-warning;
      font-weight: $font-weight-medium;
    }
  }
}

// 图片上传样式
.image-upload-container {
  .image-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      cursor: pointer;
      width: 100px;
      height: 100px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        border-color: #409eff;
      }
    }
  }

  .uploaded-image {
    width: 100px;
    height: 100px;
    object-fit: cover;
  }

  .upload-icon {
    font-size: 28px;
    color: #8c939d;
  }
}
</style>
