# 拼单收货地址选择功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复拼单时无法选择收货地址的问题，使用户在发起/参与拼单时能选择配送地址，商家接单后生成的订单包含正确的收货信息。

**Architecture:** 后端在 `GroupOrderMember` 实体添加地址字段，前端新增 `AddressSelector` 组件并在拼单流程中集成。商家接单时从成员记录读取地址生成订单。

**Tech Stack:** Spring Boot 2.7.18, MyBatis-Plus, Vue 3, Element Plus, MySQL 8

---

## File Structure

```
springboot/
  docs/db/
    add_group_order_address_fields.sql          # NEW - 数据库迁移脚本
  src/main/java/com/community/cfgs/
    entity/
      GroupOrderMember.java                     # MODIFY - 添加地址字段
    controller/
      GroupOrderController.java                 # MODIFY - 修改 create/join 接口
    service/
      GroupOrderService.java                    # MODIFY - 修改 create/join/accept 方法
    dto/
      AddressDTO.java                           # NEW - 地址数据传输对象

vue/src/
  components/common/
    AddressSelector.vue                         # NEW - 地址选择器组件
  views/
    Student/
      MerchantDetail.vue                        # MODIFY - 发起拼单对话框添加地址选择
      GroupOrderList.vue                        # MODIFY - 参与拼单对话框添加地址选择
```

---

## Task 1: 数据库迁移脚本

**Files:**
- Create: `springboot/docs/db/add_group_order_address_fields.sql`

- [ ] **Step 1: 创建迁移脚本文件**

```sql
-- 为 group_order_members 表添加收货地址字段
-- 执行前请备份数据库

ALTER TABLE `group_order_members`
ADD COLUMN `address_building` VARCHAR(100) NULL COMMENT '楼栋/宿舍号' AFTER `created_at`,
ADD COLUMN `address_room` VARCHAR(50) NULL COMMENT '房间号' AFTER `address_building`,
ADD COLUMN `address_contact` VARCHAR(50) NULL COMMENT '联系人姓名' AFTER `address_room`,
ADD COLUMN `address_phone` VARCHAR(20) NULL COMMENT '联系电话' AFTER `address_contact`;
```

保存到: `springboot/docs/db/add_group_order_address_fields.sql`

- [ ] **Step 2: 执行迁移脚本**

通过 Navicat 或 MySQL 客户端执行 `add_group_order_address_fields.sql`

验证: 查询 `information_schema.columns` 确认字段已添加
```sql
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_NAME = 'group_order_members'
AND COLUMN_NAME LIKE 'address_%';
```

- [ ] **Step 3: 提交迁移脚本**

```bash
git add springboot/docs/db/add_group_order_address_fields.sql
git commit -m "db: add address fields to group_order_members table"
```

---

## Task 2: 后端 - 创建 AddressDTO

**Files:**
- Create: `springboot/src/main/java/com/community/cfgs/dto/AddressDTO.java`

- [ ] **Step 1: 创建 AddressDTO 类**

```java
package com.community.cfgs.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String building;
    private String room;
    private String contact;
    private String phone;
}
```

保存到: `springboot/src/main/java/com/community/cfgs/dto/AddressDTO.java`

- [ ] **Step 2: 编译验证**

```bash
cd springboot
mvn compile -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/community/cfgs/dto/AddressDTO.java
git commit -m "feat: add AddressDTO for group order address"
```

---

## Task 3: 后端 - 修改 GroupOrderMember 实体

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/entity/GroupOrderMember.java`

- [ ] **Step 1: 添加地址字段**

在 `GroupOrderMember.java` 中，在 `createdAt` 字段后添加以下字段：

```java
private String addressBuilding;
private String addressRoom;
private String addressContact;
private String addressPhone;
```

完整修改后的类应包含：
```java
package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("group_order_members")
public class GroupOrderMember {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String groupOrderId;
    private String userId;
    private String userName;
    private Integer quantity;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String addressBuilding;
    private String addressRoom;
    private String addressContact;
    private String addressPhone;
}
```

- [ ] **Step 2: 编译验证**

```bash
cd springboot
mvn compile -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/community/cfgs/entity/GroupOrderMember.java
git commit -m "feat: add address fields to GroupOrderMember entity"
```

---

## Task 4: 后端 - 修改 createGroupOrder 方法

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/GroupOrderService.java:220-289`
- Modify: `springboot/src/main/java/com/community/cfgs/controller/GroupOrderController.java:19-30`

- [ ] **Step 1: 修改 Controller 接收 address 参数**

修改 `GroupOrderController.create()` 方法：

```java
@PostMapping("/create")
public Result<GroupOrder> create(@RequestBody Map<String, Object> params) {
    try {
        String userId = (String) params.get("userId");
        String dishId = (String) params.get("dishId");
        String remark = (String) params.get("remark");
        String couponId = (String) params.get("couponId");
        // 新增：获取地址信息
        Map<String, String> address = (Map<String, String>) params.get("address");
        return Result.success(groupOrderService.createGroupOrder(userId, dishId, null, remark, couponId, address));
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

- [ ] **Step 2: 修改 Service 方法签名和实现**

修改 `GroupOrderService.createGroupOrder()` 方法签名（约第 221 行）：

```java
@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public GroupOrder createGroupOrder(String userId, String dishId, Integer targetCountIgnored, String remark, String couponId, Map<String, String> address) {
```

在同一方法中，创建 `GroupOrderMember` 的部分（约第 280-287 行），添加地址字段赋值：

```java
GroupOrderMember member = new GroupOrderMember();
member.setGroupOrderId(groupOrder.getId());
member.setUserId(userId);
member.setUserName(user != null ? user.getName() : userId);
member.setQuantity(1);
member.setAmount(unitPrice);
member.setCreatedAt(LocalDateTime.now());
// 新增：设置地址信息
if (address != null) {
    member.setAddressBuilding(address.get("building"));
    member.setAddressRoom(address.get("room"));
    member.setAddressContact(address.get("contact"));
    member.setAddressPhone(address.get("phone"));
}
groupOrderMemberMapper.insert(member);
```

同时添加地址验证（在方法开始处，约第 221 行后）：

```java
// 验证地址信息
if (address == null || address.get("building") == null || address.get("phone") == null) {
    throw new RuntimeException("请选择收货地址");
}
```

- [ ] **Step 3: 编译验证**

```bash
cd springboot
mvn compile -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/community/cfgs/service/GroupOrderService.java
git add src/main/java/com/community/cfgs/controller/GroupOrderController.java
git commit -m "feat: add address support to create group order"
```

---

## Task 5: 后端 - 修改 joinGroupOrder 方法

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/GroupOrderService.java:291-325`
- Modify: `springboot/src/main/java/com/community/cfgs/controller/GroupOrderController.java:32-40`

- [ ] **Step 1: 修改 Controller 接收 address 参数**

修改 `GroupOrderController.join()` 方法：

```java
@PostMapping("/join")
public Result<String> join(@RequestBody Map<String, Object> params) {
    try {
        String groupOrderId = (String) params.get("groupOrderId");
        String userId = (String) params.get("userId");
        Map<String, String> address = (Map<String, String>) params.get("address");
        boolean ok = groupOrderService.joinGroupOrder(groupOrderId, userId, address);
        return ok ? Result.success("参与成功") : Result.error("参与失败");
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

- [ ] **Step 2: 修改 Service 方法签名和实现**

修改 `GroupOrderService.joinGroupOrder()` 方法签名（约第 292 行）：

```java
@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public boolean joinGroupOrder(String groupOrderId, String userId, Map<String, String> address) {
```

在同一方法中，创建 `GroupOrderMember` 的部分（约第 310-317 行），添加地址字段赋值：

```java
User user = userMapper.selectById(userId);
GroupOrderMember member = new GroupOrderMember();
member.setGroupOrderId(groupOrderId);
member.setUserId(userId);
member.setUserName(user != null ? user.getName() : userId);
member.setQuantity(1);
member.setAmount(groupOrder.getUnitPrice());
member.setCreatedAt(LocalDateTime.now());
// 新增：设置地址信息
if (address != null) {
    member.setAddressBuilding(address.get("building"));
    member.setAddressRoom(address.get("room"));
    member.setAddressContact(address.get("contact"));
    member.setAddressPhone(address.get("phone"));
}
groupOrderMemberMapper.insert(member);
```

同时添加地址验证（在方法开始处，约第 293 行后）：

```java
// 验证地址信息
if (address == null || address.get("building") == null || address.get("phone") == null) {
    throw new RuntimeException("请选择收货地址");
}
```

- [ ] **Step 3: 编译验证**

```bash
cd springboot
mvn compile -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/community/cfgs/service/GroupOrderService.java
git add src/main/java/com/community/cfgs/controller/GroupOrderController.java
git commit -m "feat: add address support to join group order"
```

---

## Task 6: 后端 - 修改 acceptGroupOrder 方法

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/GroupOrderService.java:368-472`

- [ ] **Step 1: 修改订单地址赋值逻辑**

在 `acceptGroupOrder` 方法中，找到生成订单的部分（约第 427-444 行），修改地址赋值：

原代码：
```java
order.setAddressBuilding("拼单外送");
order.setAddressRoom("");
order.setAddressContact(m.getUserName());
order.setAddressPhone("");
```

修改为：
```java
order.setAddressBuilding(m.getAddressBuilding() != null ? m.getAddressBuilding() : "拼单外送");
order.setAddressRoom(m.getAddressRoom() != null ? m.getAddressRoom() : "");
order.setAddressContact(m.getAddressContact() != null ? m.getAddressContact() : m.getUserName());
order.setAddressPhone(m.getAddressPhone() != null ? m.getAddressPhone() : "");
```

- [ ] **Step 2: 编译验证**

```bash
cd springboot
mvn compile -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/community/cfgs/service/GroupOrderService.java
git commit -m "feat: use member address when generating delivery orders"
```

---

## Task 7: 前端 - 创建 AddressSelector 组件

**Files:**
- Create: `vue/src/components/common/AddressSelector.vue`

- [ ] **Step 1: 创建 AddressSelector 组件**

```vue
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
```

保存到: `vue/src/components/common/AddressSelector.vue`

- [ ] **Step 2: 提交**

```bash
git add vue/src/components/common/AddressSelector.vue
git commit -m "feat: add AddressSelector component"
```

---

## Task 8: 前端 - 修改发起拼单对话框

**Files:**
- Modify: `vue/src/views/Student/MerchantDetail.vue:168-228`

- [ ] **Step 1: 导入 AddressSelector 组件**

在 `<script setup>` 区域添加导入（约第 232 行后）：

```js
import AddressSelector from '@/components/common/AddressSelector.vue'
```

- [ ] **Step 2: 添加状态变量**

在 `const currentDish = ref(null)` 后添加（约第 265 行）：

```js
const selectedAddress = ref(null)
```

- [ ] **Step 3: 在模板中添加地址选择器**

在优惠券选择区域后、对话框 footer 前添加（约第 221 行后）：

```vue
<!-- 地址选择 -->
<AddressSelector v-model="selectedAddress" />
```

- [ ] **Step 4: 修改发起拼单方法添加地址验证**

修改 `handleCreateGroupOrder` 方法（约第 471-488 行）：

```js
const handleCreateGroupOrder = async () => {
  // 验证地址
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    const dishId = currentDish.value.id
    const remark = ''
    await request.post('/group-order/create', {
      userId: userStore.userId,
      dishId,
      targetCount: targetCount.value,
      remark,
      couponId: selectedCoupon.value?.id,
      address: {
        building: selectedAddress.value.building,
        room: selectedAddress.value.room,
        contact: selectedAddress.value.contact,
        phone: selectedAddress.value.phone
      }
    })
    ElMessage.success('拼单已发起')
    groupOrderDialogVisible.value = false
    selectedAddress.value = null // 重置地址选择
    loadGroupOrders(merchant.value.id)
  } catch (error) {
    // handled by interceptor
  }
}
```

- [ ] **Step 5: 提交**

```bash
git add vue/src/views/Student/MerchantDetail.vue
git commit -m "feat: add address selector to group order dialog"
```

---

## Task 9: 前端 - 修改参与拼单对话框

**Files:**
- Modify: `vue/src/views/Student/GroupOrderList.vue:198-229`

- [ ] **Step 1: 导入 AddressSelector 组件**

在 `<script setup>` 区域添加导入（约第 90 行后）：

```js
import AddressSelector from '@/components/common/AddressSelector.vue'
```

- [ ] **Step 2: 添加状态变量**

在 `const drawerVisible = ref(false)` 后添加（约第 112 行）：

```js
const selectedAddress = ref(null)
const joinDialogVisible = ref(false)
const joiningOrder = ref(null)
```

- [ ] **Step 3: 在模板中添加参与拼单对话框**

在文件末尾 `</template>` 前添加（约第 86 行后）：

```vue
<!-- 参与拼单对话框 -->
<el-dialog
  v-model="joinDialogVisible"
  title="参与拼单"
  width="90%"
  :style="{ maxWidth: '400px' }"
>
  <p v-if="joiningOrder">确定要参与「{{ joiningOrder.dishName }}」的拼单吗？</p>
  <AddressSelector v-model="selectedAddress" />
  <template #footer>
    <el-button @click="joinDialogVisible = false">再想想</el-button>
    <el-button type="primary" @click="confirmJoin">确定参与</el-button>
  </template>
</el-dialog>
```

- [ ] **Step 4: 修改参与拼单方法**

修改 `handleJoinFromDrawer` 方法（约第 214-229 行）：

```js
const handleJoinFromDrawer = async (order) => {
  joiningOrder.value = order
  selectedAddress.value = null // 重置地址
  joinDialogVisible.value = true
}

const confirmJoin = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    await request.post('/group-order/join', {
      groupOrderId: joiningOrder.value.id,
      userId: userStore.userId,
      address: {
        building: selectedAddress.value.building,
        room: selectedAddress.value.room,
        contact: selectedAddress.value.contact,
        phone: selectedAddress.value.phone
      }
    })
    ElMessage.success('参与成功！')
    joinDialogVisible.value = false
    await loadAllData()
  } catch (error) {
    // interceptor handles message
  }
}
```

同样修改 `joinGroupOrder` 方法（约第 199-211 行）：

```js
const joinGroupOrder = async (order) => {
  joiningOrder.value = order
  selectedAddress.value = null // 重置地址
  joinDialogVisible.value = true
}
```

- [ ] **Step 5: 提交**

```bash
git add vue/src/views/Student/GroupOrderList.vue
git commit -m "feat: add address selector to join group order dialog"
```

---

## Task 10: 测试验证

**Files:**
- No files created/modified

- [ ] **Step 1: 启动后端服务**

```bash
cd springboot
mvn spring-boot:run
```

验证: 控制台显示 "Started CfgsApplication"

- [ ] **Step 2: 启动前端服务**

```bash
cd vue
npm run dev
```

验证: 浏览器可以访问 http://localhost:5173

- [ ] **Step 3: 测试发起拼单**

1. 登录学生账号
2. 进入商家详情页，点击"发起拼单"
3. 验证: 对话框显示地址选择器
4. 选择一个地址，点击"确认发起"
5. 验证: 拼单创建成功

- [ ] **Step 4: 测试参与拼单**

1. 在拼单列表页点击"参与"
2. 验证: 显示地址选择对话框
3. 选择地址，点击"确定参与"
4. 验证: 参与成功

- [ ] **Step 5: 测试商家接单**

1. 登录商家账号
2. 找到已满员的拼单，点击"接单"
3. 验证: 订单生成成功
4. 检查生成的订单，验证地址字段正确

- [ ] **Step 6: 测试边界情况**

1. 无地址用户发起拼单 → 验证显示提示
2. 无地址用户参与拼单 → 验证显示提示
3. 手机号格式验证 → 验证无效手机号被拒绝

- [ ] **Step 7: 最终提交**

```bash
git add -A
git commit -m "test: complete group order address feature testing"
```

---

## Self-Review Checklist

- [x] **Spec coverage**: 所有设计文档要求都已实现（数据库、后端、前端）
- [x] **Placeholder scan**: 无 TBD、TODO 或占位符
- [x] **Type consistency**: 方法签名在各任务中保持一致
- [x] **File paths**: 所有文件路径使用完整路径
