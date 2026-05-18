<template>
  <div class="coupon-manage">
    <div class="page-header">
      <h1 class="page-title">优惠券管理</h1>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">新建优惠券</el-button>
    </div>

    <el-table :data="coupons" stripe>
      <el-table-column prop="name" label="优惠券名称" min-width="180" />
      <el-table-column label="门槛" width="120">
        <template #default="{ row }">满 ¥{{ Number(row.thresholdAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="优惠" width="120">
        <template #default="{ row }">减 ¥{{ Number(row.discountAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="库存" width="120">
        <template #default="{ row }">{{ row.remainCount }}/{{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column label="每人限领" width="100">
        <template #default="{ row }">{{ Number(row.claimLimitPerUser || 1) }}</template>
      </el-table-column>
      <el-table-column label="有效期" width="200">
        <template #default="{ row }">{{ formatExpireTime(row.expireAt) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="isCouponActive(row) ? 'success' : 'info'">
            {{ isCouponActive(row) ? '可用' : '不可用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" link @click="deleteCoupon(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '新建优惠券'" width="500px">
      <el-form ref="formRef" :model="couponForm" :rules="rules" label-width="100px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="couponForm.name" maxlength="30" />
        </el-form-item>
        <el-form-item label="满减门槛" prop="thresholdAmount">
          <el-input-number v-model="couponForm.thresholdAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="优惠金额" prop="discountAmount">
          <el-input-number v-model="couponForm.discountAmount" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="发放数量" prop="totalCount">
          <el-input-number v-model="couponForm.totalCount" :min="1" :precision="0" />
        </el-form-item>
        <el-form-item label="每人限领" prop="claimLimitPerUser">
          <el-input-number v-model="couponForm.claimLimitPerUser" :min="1" :precision="0" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireAt">
          <el-date-picker
            v-model="couponForm.expireAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择过期时间"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCoupon">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const userStore = useUserStore()
const coupons = ref([])
const merchantId = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const couponForm = ref({
  id: '',
  name: '',
  thresholdAmount: 0,
  discountAmount: 1,
  totalCount: 50,
  claimLimitPerUser: 1,
  expireAt: ''
})

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  expireAt: [{ required: true, message: '请选择过期时间', trigger: 'change' }]
}

const formatExpireTime = (time) => {
  if (!time) return '长期有效'
  return String(time).replace('T', ' ').slice(0, 16)
}

const isCouponActive = (coupon) => {
  const now = new Date().getTime()
  const expire = new Date(coupon.expireAt).getTime()
  return Boolean(coupon.isEnabled) && coupon.remainCount > 0 && expire >= now
}

const loadMerchantId = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) return ''
  const merchant = await request.get(`/merchant/user/${uid}`)
  merchantId.value = merchant?.id || ''
  return merchantId.value
}

const loadCoupons = async () => {
  const mid = merchantId.value || await loadMerchantId()
  if (!mid) return
  coupons.value = await request.get(`/coupon/merchant/${mid}`)
}

const openAddDialog = () => {
  isEdit.value = false
  couponForm.value = {
    id: '',
    name: '',
    thresholdAmount: 0,
    discountAmount: 1,
    totalCount: 50,
    claimLimitPerUser: 1,
    expireAt: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (coupon) => {
  isEdit.value = true
  couponForm.value = {
    id: coupon.id,
    name: coupon.name,
    thresholdAmount: Number(coupon.thresholdAmount || 0),
    discountAmount: Number(coupon.discountAmount || 0),
    totalCount: Number(coupon.totalCount || 1),
    claimLimitPerUser: Number(coupon.claimLimitPerUser || 1),
    expireAt: coupon.expireAt,
    isEnabled: Boolean(coupon.isEnabled)
  }
  dialogVisible.value = true
}

const saveCoupon = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (Number(couponForm.value.discountAmount) > Number(couponForm.value.thresholdAmount)) {
      ElMessage.warning('优惠金额不能大于门槛金额')
      return
    }
    if (isEdit.value) {
      await request.put('/coupon/update', {
        ...couponForm.value
      }, {
        params: { merchantId: merchantId.value }
      })
    } else {
      await request.post('/coupon/add', {
        ...couponForm.value,
        merchantId: merchantId.value,
        remainCount: couponForm.value.totalCount,
        isEnabled: true
      })
    }
    dialogVisible.value = false
    ElMessage.success(isEdit.value ? '优惠券更新成功' : '优惠券创建成功')
    loadCoupons()
  })
}

const deleteCoupon = async (coupon) => {
  try {
    await ElMessageBox.confirm(`确认删除优惠券「${coupon.name}」？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/coupon/${coupon.id}`, { params: { merchantId: merchantId.value } })
    ElMessage.success('删除成功')
    loadCoupons()
  } catch (error) {
    // ignore cancel
  }
}

onMounted(() => {
  loadCoupons()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.coupon-manage {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;
  }

  .page-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
  }
}
</style>
