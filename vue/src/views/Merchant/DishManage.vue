<template>
  <div class="dish-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">菜品管理</h1>
      <div class="header-actions">
        <el-button type="warning" @click="openTemplateDialog">
          一键套用拼单模板
        </el-button>
        <el-button type="primary" :icon="Plus" @click="openAddDialog">
          添加菜品
        </el-button>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeCategory" @change="filterByCategory">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button
          v-for="category in categoriesInDishes"
          :key="category"
          :label="category"
        >
          {{ category }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 菜品列表 -->
    <div class="dish-list">
      <el-table :data="filteredDishes" stripe>
        <el-table-column label="菜品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.image)"
              :preview-src-list="[getImageUrl(row.image)]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 8px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
            <span class="original-price" v-if="row.originalPrice">
              ¥{{ row.originalPrice }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80">
          <template #default="{ row }">
            <el-tag :type="row.stock > 0 ? 'success' : 'danger'" size="small">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isAvailable ? 'success' : 'info'" size="small">
              {{ row.isAvailable ? '在售' : '已售罄' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="拼单" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isGroupEnabled ? 'warning' : 'info'" size="small">
              {{ row.isGroupEnabled ? '可拼单' : '不参与' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="拼单规则" min-width="160">
          <template #default="{ row }">
            <span v-if="row.isGroupEnabled">
              {{ row.groupTargetCount || 2 }}人成团 / 拼单价¥{{ Number(row.groupPrice || row.price || 0).toFixed(2) }} / {{ Number(row.groupDurationMinutes || 30) }}分钟
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="adjustStock(row)">
              调整库存
            </el-button>
            <el-button type="danger" link @click="deleteDish(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜品' : '添加菜品'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="dishForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="dishForm.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select
            v-model="dishForm.category"
            placeholder="选择或输入新分类"
            allow-create
            filterable
            default-first-option
          >
            <el-option
              v-for="cat in categoriesForForm"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
          <div class="field-tip">可选预设分类，或输入新名称后回车添加；新分类会出现在筛选栏（该分类下有菜品时）</div>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="dishForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="dishForm.originalPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="dishForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="拼单商品">
          <el-switch v-model="dishForm.isGroupEnabled" />
        </el-form-item>
        <el-form-item label="成团人数" v-if="dishForm.isGroupEnabled">
          <el-input-number v-model="dishForm.groupTargetCount" :min="2" :max="10" />
        </el-form-item>
        <el-form-item label="拼单价" v-if="dishForm.isGroupEnabled">
          <el-input-number v-model="dishForm.groupPrice" :min="0.01" :precision="2" />
          <div class="field-tip">建议小于原价，表示拼单优惠价</div>
        </el-form-item>
        <el-form-item label="拼单时长" v-if="dishForm.isGroupEnabled">
          <el-input-number v-model="dishForm.groupDurationMinutes" :min="1" :max="1440" :precision="0" />
          <div class="field-tip">单位：分钟，超时后该拼单自动取消</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="dishForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入菜品描述"
          />
        </el-form-item>
        <el-form-item label="图片">
          <div class="image-upload-container">
            <el-upload
              class="image-uploader"
              action="http://localhost:8080/api/file/upload"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <img v-if="dishForm.image" :src="getImageUrl(dishForm.image)" class="uploaded-image" />
              <el-icon v-else class="upload-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 jpg、png、gif、webp 格式，大小不超过5MB</div>
            <el-input v-model="dishForm.image" placeholder="或直接输入图片URL" style="margin-top: 8px;" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDish">保存</el-button>
      </template>
    </el-dialog>

    <!-- 库存调整弹窗 -->
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前库存">
          <span>{{ currentDish?.stock }}</span>
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number v-model="stockAdjust" :min="-currentDish?.stock" />
        </el-form-item>
        <el-form-item label="调整后">
          <span>{{ (currentDish?.stock || 0) + stockAdjust }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStockAdjust">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateDialogVisible" title="一键套用拼单模板" width="460px">
      <el-form label-width="110px">
        <el-form-item label="选择模板">
          <el-select v-model="selectedTemplateKey" style="width: 100%">
            <el-option
              v-for="tpl in groupTemplates"
              :key="tpl.key"
              :label="tpl.label"
              :value="tpl.key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="应用范围">
          <el-radio-group v-model="templateScope">
            <el-radio label="all">全部菜品</el-radio>
            <el-radio label="filtered">当前筛选结果</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <div class="field-tip">
            套用后将自动开启“拼单商品”，并按模板折扣计算拼单价。
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyingTemplate" @click="applyGroupTemplate">
          立即套用
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'
import { parseMerchantCategories } from '@/utils/merchantCategories'

const userStore = useUserStore()

// 菜品列表
const dishes = ref([])

// 商家信息（每次加载菜品时重新请求，保证与后台「经营品类」一致）
const merchantInfo = ref(null)

/** 无菜品且无店铺分类时的常用预设，便于新店首张菜单 */
const DEFAULT_FORM_CATEGORIES = ['主食', '热菜', '小菜', '汤羹', '饮品', '甜品', '小吃', '其他']

// 筛选栏：仅已有菜品中出现过的分类（避免空店刷屏）
const categoriesInDishes = computed(() => {
  const cats = new Set(dishes.value.map((d) => d.category).filter(Boolean))
  return Array.from(cats).sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

// 添加/编辑下拉：店铺预设 + 已有菜品分类 + 默认预设（前两者都空时）
const categoriesForForm = computed(() => {
  const fromMerchant = parseMerchantCategories(merchantInfo.value?.categories)
  const fromDishes = dishes.value.map((d) => d.category).filter(Boolean)
  const set = new Set([...fromMerchant, ...fromDishes])
  if (set.size === 0) {
    DEFAULT_FORM_CATEGORIES.forEach((c) => set.add(c))
  }
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

// 当前筛选分类
const activeCategory = ref('')

// 过滤后的菜品
const filteredDishes = computed(() => {
  if (!activeCategory.value) return dishes.value
  return dishes.value.filter(d => d.category === activeCategory.value)
})

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 菜品表单
const dishForm = ref({
  name: '',
  category: '',
  price: 0,
  originalPrice: null,
  stock: 0,
  description: '',
  image: '',
  isGroupEnabled: false,
  groupTargetCount: 2,
  groupPrice: 0,
  groupDurationMinutes: 30
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 库存调整
const stockDialogVisible = ref(false)
const currentDish = ref(null)
const stockAdjust = ref(0)
const templateDialogVisible = ref(false)
const applyingTemplate = ref(false)
const templateScope = ref('all')
const selectedTemplateKey = ref('2_95')
const groupTemplates = [
  { key: '2_95', label: '2人成团 95折', targetCount: 2, discountRate: 0.95 },
  { key: '3_90', label: '3人成团 9折', targetCount: 3, discountRate: 0.9 },
  { key: '4_85', label: '4人成团 85折', targetCount: 4, discountRate: 0.85 }
]

// 每次拉取当前店铺资料（含经营品类），避免沿用缓存导致与管理员/个人中心不同步
const getMerchantId = async () => {
  const userId = userStore.userInfo?.id || ''
  if (!userId) return ''

  try {
    const merchant = await request.get(`/merchant/user/${userId}`)
    if (merchant) {
      merchantInfo.value = merchant
      return merchant.id
    }
  } catch (error) {
    console.error('获取商家信息失败:', error)
  }
  return ''
}

// 加载菜品
const loadDishes = async () => {
  const merchantId = await getMerchantId()
  if (!merchantId) return
  
  try {
    // 商家端使用 /dish/all 获取所有菜品（包括已售罄的）
    const result = await request.get(`/dish/all/${merchantId}`)
    dishes.value = result || []
  } catch (error) {
    console.error('加载菜品失败:', error)
  }
}

// 按分类筛选
const filterByCategory = () => {
  // 已通过 computed 实现
}

// 打开添加弹窗
const openAddDialog = () => {
  isEdit.value = false
  dishForm.value = {
    name: '',
    category: '',
    price: 0,
    originalPrice: null,
    stock: 0,
    description: '',
    image: '',
    isGroupEnabled: false,
    groupTargetCount: 2,
    groupPrice: 0,
    groupDurationMinutes: 30
  }
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (dish) => {
  isEdit.value = true
  dishForm.value = {
    ...dish,
    groupTargetCount: Number(dish.groupTargetCount || 2),
    groupPrice: Number(dish.groupPrice || dish.price || 0),
    groupDurationMinutes: Number(dish.groupDurationMinutes || 30)
  }
  dialogVisible.value = true
}

// 保存菜品
const saveDish = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    const merchantId = await getMerchantId()
    if (!merchantId) {
      ElMessage.error('获取商家信息失败')
      return
    }

    try {
      if (dishForm.value.isGroupEnabled && Number(dishForm.value.groupPrice || 0) <= 0) {
        ElMessage.warning('请设置有效拼单价')
        return
      }
      if (isEdit.value) {
        await request.put('/dish/update', {
          ...dishForm.value,
          groupTargetCount: undefined,
          groupPrice: undefined
        })
        await request.put('/dish/group-rule', {
          dishId: dishForm.value.id,
          enabled: dishForm.value.isGroupEnabled,
          targetCount: dishForm.value.groupTargetCount,
          groupPrice: dishForm.value.groupPrice,
          durationMinutes: dishForm.value.groupDurationMinutes
        })
        ElMessage.success('菜品已更新')
      } else {
        await request.post('/dish/add', {
          ...dishForm.value,
          merchantId
        })
        ElMessage.success('菜品已添加')
      }

      dialogVisible.value = false
      loadDishes()
    } catch (error) {
      ElMessage.error('保存失败')
    }
  })
}

// 删除菜品
const deleteDish = async (dish) => {
  try {
    await ElMessageBox.confirm(`确定要删除「${dish.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.delete(`/dish/${dish.id}`)
    ElMessage.success('菜品已删除')
    loadDishes()
  } catch (e) {
    // 用户取消或删除失败
  }
}

// 调整库存
const adjustStock = (dish) => {
  currentDish.value = dish
  stockAdjust.value = 0
  stockDialogVisible.value = true
}

// 确认库存调整
const confirmStockAdjust = async () => {
  const newStock = (currentDish.value?.stock || 0) + stockAdjust.value
  if (newStock < 0) {
    ElMessage.warning('库存不能为负数')
    return
  }

  try {
    await request.put('/dish/update', {
      id: currentDish.value.id,
      stock: newStock
    })
    ElMessage.success('库存已调整')
    stockDialogVisible.value = false
    loadDishes()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}

const openTemplateDialog = () => {
  templateScope.value = activeCategory.value ? 'filtered' : 'all'
  selectedTemplateKey.value = '2_95'
  templateDialogVisible.value = true
}

const applyGroupTemplate = async () => {
  const template = groupTemplates.find(t => t.key === selectedTemplateKey.value)
  if (!template) return

  const targetDishes = templateScope.value === 'filtered' ? filteredDishes.value : dishes.value
  if (!targetDishes.length) {
    ElMessage.warning('当前没有可应用的菜品')
    return
  }
  applyingTemplate.value = true
  try {
    const merchantId = await getMerchantId()
    if (!merchantId) {
      ElMessage.error('获取商家信息失败')
      return
    }
    await request.post('/dish/group-template/apply', {
      merchantId,
      targetCount: template.targetCount,
      discountRate: template.discountRate,
      dishIds: targetDishes.map(d => d.id)
    })
    ElMessage.success(`已为${targetDishes.length}个菜品套用模板`)
    templateDialogVisible.value = false
    loadDishes()
  } catch (error) {
    ElMessage.error('模板套用失败，请重试')
  } finally {
    applyingTemplate.value = false
  }
}

// 获取图片完整URL
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/img/')) return `http://localhost:8080${url}`
  return url
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
    dishForm.value.image = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败处理
const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

onMounted(() => {
  loadDishes()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.dish-manage {
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
      gap: $spacing-sm;
    }
  }

  .filter-bar {
    margin-bottom: $spacing-lg;
  }

  .dish-list {
    background: $color-bg-white;
    border-radius: $border-radius-md;
    padding: $spacing-base;

    .price {
      color: $color-warning;
      font-weight: $font-weight-medium;
    }

    .original-price {
      color: $color-text-placeholder;
      text-decoration: line-through;
      font-size: $font-size-xs;
      margin-left: $spacing-xs;
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
    margin-top: 8px;
  }
}

.field-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 6px;
}
</style>
