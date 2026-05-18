<template>
  <div class="group-coupon-manage">
    <div class="header">
      <h2>拼单优惠券管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <iconify-icon icon="lucide:plus" style="vertical-align: middle; margin-right: 4px;" />
        创建优惠券
      </el-button>
    </div>

    <!-- 优惠券列表 -->
    <el-table :data="coupons" stripe>
      <el-table-column prop="discountRate" label="折扣" width="100">
        <template #default="{ row }">
          <el-tag type="success">{{ (row.discountRate * 10).toFixed(1) }}折</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="couponType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.couponType === 'dish'" type="primary">菜品券</el-tag>
          <el-tag v-else type="info">店铺券</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dishName" label="适用菜品" width="150">
        <template #default="{ row }">
          {{ row.dishName || '全店适用' }}
        </template>
      </el-table-column>
      <el-table-column prop="minPeople" label="最低人数" width="100" />
      <el-table-column label="限量/已用" width="120">
        <template #default="{ row }">
          {{ row.totalLimit || '不限' }} / {{ row.usedCount }}
        </template>
      </el-table-column>
      <el-table-column prop="validUntil" label="有效期至" width="180">
        <template #default="{ row }">
          {{ formatDate(row.validUntil) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">
            {{ row.status === 'active' ? '活跃' : '已暂停' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editCoupon(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 'active' ? 'warning' : 'success'"
                     @click="toggleStatus(row)">
            {{ row.status === 'active' ? '暂停' : '恢复' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteCoupon(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="showCreateDialog" :title="editingCoupon ? '编辑优惠券' : '创建优惠券'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="券类型" prop="couponType">
          <el-select v-model="form.couponType" @change="onTypeChange">
            <el-option label="菜品券" value="dish" />
            <el-option label="店铺券" value="shop" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.couponType === 'dish'" label="选择菜品" prop="dishId">
          <el-select v-model="form.dishId" placeholder="请选择菜品">
            <el-option v-for="dish in dishes" :key="dish.id" :label="dish.name" :value="dish.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate">
          <el-input-number v-model="form.discountRate" :min="0.1" :max="1" :step="0.05"
                           :precision="2" />
          <span style="margin-left: 8px;">(0.8 = 8折)</span>
        </el-form-item>
        <el-form-item label="最低人数" prop="minPeople">
          <el-input-number v-model="form.minPeople" :min="2" :max="20" />
        </el-form-item>
        <el-form-item label="总限量">
          <el-input-number v-model="form.totalLimit" :min="1" />
          <span style="margin-left: 8px;">留空表示不限</span>
        </el-form-item>
        <el-form-item label="每人限用">
          <el-input-number v-model="form.perUserLimit" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="有效期" prop="validPeriod">
          <el-date-picker v-model="validPeriod" type="daterange" range-separator="至"
                          start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/axios/request'
import {
  createGroupCoupon,
  getMyGroupCoupons,
  updateGroupCoupon,
  deleteGroupCoupon,
  toggleGroupCouponStatus
} from '@/api/groupCoupon'

// 获取商家ID
const getMerchantId = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return null
  try {
    const user = JSON.parse(userStr)
    return user.merchantId
  } catch {
    return null
  }
}

const coupons = ref([])
const dishes = ref([])
const showCreateDialog = ref(false)
const editingCoupon = ref(null)
const validPeriod = ref([])
const formRef = ref(null)

const form = ref({
  couponType: 'dish',
  dishId: null,
  discountRate: 0.8,
  minPeople: 3,
  totalLimit: null,
  perUserLimit: 1
})

const rules = {
  couponType: [{ required: true, message: '请选择券类型', trigger: 'change' }],
  dishId: [{ required: true, message: '请选择菜品', trigger: 'change' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  minPeople: [{ required: true, message: '请输入最低人数', trigger: 'blur' }],
  validPeriod: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadCoupons = async () => {
  try {
    const res = await getMyGroupCoupons()
    coupons.value = res.data || []
  } catch (err) {
    ElMessage.error('加载优惠券失败')
  }
}

const loadDishes = async () => {
  try {
    const merchantId = await getMerchantId()
    if (!merchantId) {
      console.warn('无法获取商家ID')
      return
    }
    const result = await request.get(`/dish/all/${merchantId}`)
    dishes.value = result || []
  } catch (err) {
    console.error('加载菜品失败', err)
  }
}

const onTypeChange = () => {
  if (form.value.couponType === 'shop') {
    form.value.dishId = null
  }
}

const editCoupon = (coupon) => {
  editingCoupon.value = coupon
  form.value = {
    couponType: coupon.couponType,
    dishId: coupon.dishId,
    discountRate: coupon.discountRate,
    minPeople: coupon.minPeople,
    totalLimit: coupon.totalLimit,
    perUserLimit: coupon.perUserLimit
  }
  validPeriod.value = [new Date(coupon.validFrom), new Date(coupon.validUntil)]
  showCreateDialog.value = true
}

const toggleStatus = async (coupon) => {
  try {
    await toggleGroupCouponStatus(coupon.id)
    ElMessage.success('操作成功')
    loadCoupons()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const deleteCoupon = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此优惠券吗？', '确认', { type: 'warning' })
    await deleteGroupCoupon(id)
    ElMessage.success('删除成功')
    loadCoupons()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
    const data = {
      ...form.value,
      validFrom: validPeriod.value[0].toISOString(),
      validUntil: validPeriod.value[1].toISOString()
    }

    if (editingCoupon.value) {
      await updateGroupCoupon(editingCoupon.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await createGroupCoupon(data)
      ElMessage.success('创建成功')
    }

    showCreateDialog.value = false
    loadCoupons()
  } catch (err) {
    if (err !== false) {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadCoupons()
  loadDishes()
})
</script>

<style scoped>
.group-coupon-manage {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}
</style>
