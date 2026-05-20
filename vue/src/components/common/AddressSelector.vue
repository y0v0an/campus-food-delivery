<template>
  <div class="address-selector">
    <div class="selector-header" @click="showPicker = true">
      <span class="label">📍 配送地址 (必选)</span>
      <span class="change-btn">更改地址 ></span>
    </div>
    <div v-if="selectedAddress" class="address-display" @click="showPicker = true">
      <div class="address-line">{{ selectedAddress.building }} {{ selectedAddress.room }}</div>
      <div class="contact-line">{{ selectedAddress.contact }} {{ maskPhone(selectedAddress.phone) }}</div>
    </div>
    <div v-else class="no-address" @click="showPicker = true">
      请选择收货地址
    </div>

    <!-- 地址选择弹窗 -->
    <el-dialog v-model="showPicker" title="选择收货地址" width="90%" :style="{ maxWidth: '400px' }">
      <div v-if="addresses.length === 0" class="empty-hint">
        <p>暂无地址，请先添加收货地址</p>
      </div>
      <div v-else class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-item"
          :class="{ selected: selectedAddress?.id === addr.id }"
          @click="selectAddress(addr)"
        >
          <div class="addr-main">{{ addr.building }} {{ addr.room }}</div>
          <div class="addr-contact">{{ addr.contact }} {{ addr.phone }}</div>
          <span v-if="addr.isDefault" class="tag-default">默认</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPicker = false">取消</el-button>
        <el-button type="primary" @click="goToAddAddress">添加新地址</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/axios/request'

const props = defineProps({
  modelValue: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const userStore = useUserStore()

const addresses = ref([])
const showPicker = ref(false)

const selectedAddress = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 加载地址列表
const loadAddresses = async () => {
  try {
    const result = await request.get(`/address/list/${userStore.userId}`)
    addresses.value = result || []
    // 如果没有选中地址且有默认地址，自动选中默认地址
    if (!selectedAddress.value && addresses.value.length > 0) {
      const defaultAddr = addresses.value.find(a => a.isDefault)
      if (defaultAddr) {
        selectedAddress.value = defaultAddr
      }
    }
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

const selectAddress = (addr) => {
  selectedAddress.value = addr
  showPicker.value = false
}

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const goToAddAddress = () => {
  showPicker.value = false
  router.push('/student/address')
}

onMounted(() => {
  loadAddresses()
})

// 暴露方法供父组件刷新地址列表
defineExpose({
  reload: loadAddresses
})
</script>

<style scoped>
.address-selector {
  margin: 15px 0;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.change-btn {
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
}

.address-display {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
}

.address-line {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.contact-line {
  font-size: 13px;
  color: #606266;
}

.no-address {
  padding: 12px;
  background: #fff7ed;
  border: 1px dashed #FFB84D;
  border-radius: 8px;
  text-align: center;
  color: #FF7A45;
  font-size: 13px;
  cursor: pointer;
}

.address-list {
  max-height: 300px;
  overflow-y: auto;
}

.address-item {
  position: relative;
  padding: 12px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
}

.address-item.selected {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.addr-main {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.addr-contact {
  font-size: 13px;
  color: #606266;
}

.tag-default {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 2px 8px;
  background: #67c23a;
  color: white;
  font-size: 11px;
  border-radius: 4px;
}

.empty-hint {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}
</style>
