# 地址管理页面实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 创建独立的地址管理页面，提供完整的地址 CRUD 功能

**架构:** 新建 Vue 单文件组件，复用现有后端 API，通过路由集成到学生端导航

**技术栈:** Vue 3 Composition API, Element Plus, Axios, Pinia

---

## 文件结构

| 文件 | 操作 | 说明 |
|------|------|------|
| `vue/src/views/Student/AddressManage.vue` | 创建 | 独立地址管理页面 |
| `vue/src/router/index.js` | 修改 | 添加地址管理路由 |
| `vue/src/views/Student/Profile.vue` | 修改 | 移除弹窗代码，改为路由跳转 |

---

## Task 1: 添加路由配置

**文件:**
- 修改: `vue/src/router/index.js`

- [ ] **步骤 1: 在 student children 中添加地址管理路由**

在 `vue/src/router/index.js` 的学生端路由 children 数组中，`profile` 路由之前添加：

```javascript
{
  path: 'addresses',
  name: 'AddressManage',
  component: () => import('@/views/Student/AddressManage.vue')
},
```

完整插入位置（第 101 行之前）：

```javascript
// ... existing routes ...
        {
          path: 'my-coupons',
          name: 'MyCoupons',
          component: () => import('@/views/Student/MyCoupons.vue')
        },
        {
          path: 'addresses',
          name: 'AddressManage',
          component: () => import('@/views/Student/AddressManage.vue')
        },
        {
          path: 'rider-apply',
          name: 'RiderApply',
          component: () => import('@/views/Student/RiderApply.vue')
        }
// ... rest of routes ...
```

- [ ] **步骤 2: 提交路由配置更改**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/router/index.js
git commit -m "feat: add address management route"
```

---

## Task 2: 创建地址管理页面 - 模板结构

**文件:**
- 创建: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 创建文件基础结构和模板**

创建 `vue/src/views/Student/AddressManage.vue`，写入以下内容：

```vue
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
```

- [ ] **步骤 2: 提交模板结构**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "feat: add address management page template"
```

---

## Task 3: 添加脚本逻辑 - 变量和导入

**文件:**
- 修改: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 添加 script setup 部分**

在 `</template>` 标签后添加 `<script setup>` 部分：

```vue
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
```

- [ ] **步骤 2: 提交脚本添加**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "feat: add address management script setup"
```

---

## Task 4: 实现地址列表加载功能

**文件:**
- 修改: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 添加 loadAddresses 函数**

在 script setup 部分添加加载地址函数：

```javascript
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

// 组件挂载时加载地址
onMounted(() => {
  loadAddresses()
})
```

- [ ] **步骤 2: 提交加载功能**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "feat: implement load addresses function"
```

---

## Task 5: 实现新增地址功能

**文件:**
- 修改: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 添加新增地址相关函数**

在 script setup 部分添加：

```javascript
// 显示新增地址弹窗
const showAddDialog = () => {
  isEditing.value = false
  editingId.value = null
  addressForm.value = {
    building: '',
    room: '',
    contact: '',
    phone: '',
    isDefault: addresses.value.length === 0
  }
  showDialog.value = true
}

// 手机号验证
const validatePhone = (phone) => {
  const phoneReg = /^1[3-9]\d{9}$/
  return phoneReg.test(phone)
}

// 保存地址
const saveAddress = async () => {
  // 表单验证
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
  if (!validatePhone(addressForm.value.phone)) {
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
    showDialog.value = false
    await loadAddresses()
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}
```

- [ ] **步骤 2: 提交新增功能**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "feat: implement add address function"
```

---

## Task 6: 实现编辑地址功能

**文件:**
- 修改: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 添加编辑地址函数**

在 script setup 部分添加：

```javascript
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
  showDialog.value = true
}
```

- [ ] **步骤 2: 提交编辑功能**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "feat: implement edit address function"
```

---

## Task 7: 实现删除地址功能

**文件:**
- 修改: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 添加删除地址函数**

在 script setup 部分添加：

```javascript
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
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('删除失败')
    }
  }
}
```

- [ ] **步骤 2: 提交删除功能**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "feat: implement delete address function"
```

---

## Task 8: 实现设为默认功能

**文件:**
- 修改: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 添加设为默认函数**

在 script setup 部分添加：

```javascript
// 设为默认地址
const setDefaultAddress = async (id) => {
  try {
    await request.put(`/address/default/${id}`)
    ElMessage.success('设置成功')
    await loadAddresses()
  } catch (error) {
    console.error('设为默认失败:', error)
    ElMessage.error('设置失败')
  }
}
```

- [ ] **步骤 2: 提交设为默认功能**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "feat: implement set default address function"
```

---

## Task 9: 添加页面样式

**文件:**
- 修改: `vue/src/views/Student/AddressManage.vue`

- [ ] **步骤 1: 添加 style 部分**

在 `</script>` 标签后添加 `<style scoped>` 部分：

```vue
<style scoped>
.address-manage-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, #eef6ff 0%, #f5f7fa 16%, #f5f7fa 100%);
  padding-bottom: 80px;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #F2F6FC;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: #303133;
  cursor: pointer;
  border-radius: 50%;
}

.back-btn:hover {
  background: #F5F7FA;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.header-spacer {
  width: 36px;
}

/* 地址列表容器 */
.address-list-container {
  padding: 16px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: #909399;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 地址卡片 */
.address-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid #F2F6FC;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.15s;
}

.address-card.is-default {
  border-color: #67C23A;
  background: linear-gradient(135deg, #f0fff4 0%, #ffffff 100%);
}

.address-info {
  flex: 1;
  min-width: 0;
}

.address-main {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-default {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: #67C23A;
  color: white;
  font-size: 11px;
  border-radius: 4px;
}

.address-contact {
  font-size: 13px;
  color: #909399;
  word-break: break-all;
}

/* 地址操作按钮 */
.address-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.action-btn.btn-default {
  background: #E1F3D8;
  color: #67C23A;
}

.action-btn.btn-default:hover {
  background: #d0e8c4;
}

.action-btn.btn-edit {
  background: #F4F4F5;
  color: #606266;
}

.action-btn.btn-edit:hover {
  background: #E9E9EB;
}

.action-btn.btn-delete {
  background: #FEF0F0;
  color: #F56C6C;
}

.action-btn.btn-delete:hover {
  background: #FDE2E2;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #F2F6FC;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.add-address-btn {
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 22px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.add-address-btn:hover {
  opacity: 0.9;
}

/* 弹窗样式 */
.address-dialog :deep(.el-dialog__header) {
  padding: 16px 16px 0;
}

.address-dialog :deep(.el-dialog__body) {
  padding: 16px;
}

.address-dialog :deep(.el-dialog__footer) {
  padding: 0 16px 16px;
}

.address-form :deep(.el-form-item__label) {
  color: #303133;
  font-weight: 500;
}

.address-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.address-form :deep(.el-checkbox__label) {
  color: #606266;
}
</style>
```

- [ ] **步骤 2: 提交样式添加**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/AddressManage.vue
git commit -m "style: add address management page styles"
```

---

## Task 10: 修改个人中心入口

**文件:**
- 修改: `vue/src/views/Student/Profile.vue`

- [ ] **步骤 1: 修改收货地址菜单项为路由跳转**

找到 Profile.vue 中的"收货地址"菜单项（约第 62-66 行），将弹窗触发改为路由跳转：

原代码：
```html
<div class="menu-item" @click="showAddressDialog = true">
  <span class="menu-icon">📍</span>
  <span class="menu-label">收货地址</span>
  <el-icon><ArrowRight /></el-icon>
</div>
```

修改为：
```html
<div class="menu-item" @click="goToAddresses">
  <span class="menu-icon">📍</span>
  <span class="menu-label">收货地址</span>
  <el-icon><ArrowRight /></el-icon>
</div>
```

- [ ] **步骤 2: 在 script setup 中添加跳转方法**

在 Profile.vue 的 script setup 部分添加 `goToAddresses` 方法：

```javascript
// 跳转到地址管理
const goToAddresses = () => {
  router.push('/student/addresses')
}
```

- [ ] **步骤 3: 移除地址管理弹窗相关代码**

从 Profile.vue 中移除以下代码：

1. 模板中的地址管理弹窗（第 102-164 行）：
```html
<!-- 地址管理弹窗 -->
<el-dialog
  v-model="showAddressDialog"
  ...
</el-dialog>
```

2. script 中的地址相关变量和方法（约第 236-342 行）：
```javascript
const showAddressDialog = ref(false)
const showNewAddressForm = ref(false)
const addresses = ref([])
const editingAddressId = ref(null)
const addressForm = ref({...})

const loadAddresses = async () => {...}
const setDefaultAddress = async (id) => {...}
const editAddress = (addr) => {...}
const deleteAddress = async (id) => {...}
const saveAddress = async () => {...}
const cancelAddressForm = () => {...}
```

3. 移除 `onMounted` 中的 `loadAddresses()` 调用

4. 移除相关的 CSS 样式（第 604-739 行）

- [ ] **步骤 4: 移除不再使用的 Plus 图标导入**

如果 `Plus` 图标不再被其他地方使用，从 import 中移除：

原导入：
```javascript
import { ArrowRight, Plus } from '@element-plus/icons-vue'
```

修改为：
```javascript
import { ArrowRight } from '@element-plus/icons-vue'
```

- [ ] **步骤 5: 提交个人中心修改**

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git add src/views/Student/Profile.vue
git commit -m "refactor: remove address dialog, use route navigation"
```

---

## 测试验证

### 测试检查清单

- [ ] **导航测试**
  - [ ] 从个人中心点击"收货地址"能正确跳转到新页面
  - [ ] 点击返回按钮能回到个人中心

- [ ] **空状态测试**
  - [ ] 无地址时显示空状态提示

- [ ] **新增地址测试**
  - [ ] 点击新增按钮打开弹窗
  - [ ] 表单验证（空值、手机号格式）正常
  - [ ] 第一个地址自动设为默认
  - [ ] 新增成功后列表刷新

- [ ] **编辑地址测试**
  - [ ] 点击编辑打开弹窗，数据预填充正确
  - [ ] 修改后保存成功

- [ ] **删除地址测试**
  - [ ] 点击删除显示二次确认
  - [ ] 确认后删除成功
  - [ ] 删除默认地址后其他地址自动设为默认

- [ ] **设为默认测试**
  - [ ] 点击设为默认成功
  - [ ] 只有一个默认地址

---

## 完成检查

所有任务完成后，运行以下命令确认变更：

```bash
cd "E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue"
git log --oneline -10
```

预期输出应显示以下提交：
```
feat: add address management route
feat: add address management page template
feat: add address management script setup
feat: implement load addresses function
feat: implement add address function
feat: implement edit address function
feat: implement delete address function
feat: implement set default address function
style: add address management page styles
refactor: remove address dialog, use route navigation
```
