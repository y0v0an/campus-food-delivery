# 拼单收货地址选择功能设计

**日期**: 2026-05-20
**问题**: 用户在拼单时无法选择收货地址，导致生成的配送订单无法实际配送

## 问题分析

当前拼单流程中：
1. 发起拼单时只选择菜品和优惠券，没有收货地址选择
2. 参与拼单时直接加入，没有收货地址选择
3. 商家接单生成配送订单时，使用固定值 `"拼单外送"` 作为地址，且 `addressPhone` 为空

这导致生成的配送订单无法实际配送——骑手不知道送到哪里，也无法联系用户。

## 设计决策

**拼单成团后，每个参与者选择自己的收货地址**（方案一：拼单时立即选择地址）

### 选择理由
1. 拼单是即时性较强的行为，一次性完成更符合用户预期
2. 避免成团后发现地址未填写的尴尬情况
3. 实现相对简单，不需要额外的状态管理

---

## 实现方案

### 一、数据库变更

在 `group_order_members` 表中添加地址相关字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| address_building | VARCHAR(100) | 楼栋/宿舍号 |
| address_room | VARCHAR(50) | 房间号 |
| address_contact | VARCHAR(50) | 联系人姓名 |
| address_phone | VARCHAR(20) | 联系电话 |

**迁移脚本**:
```sql
ALTER TABLE group_order_members
ADD COLUMN address_building VARCHAR(100) COMMENT '楼栋/宿舍号',
ADD COLUMN address_room VARCHAR(50) COMMENT '房间号',
ADD COLUMN address_contact VARCHAR(50) COMMENT '联系人姓名',
ADD COLUMN address_phone VARCHAR(20) COMMENT '联系电话';
```

---

### 二、后端接口变更

#### 1. 修改 `/group-order/create` 接口

**新增参数**:
```json
{
  "userId": "xxx",
  "dishId": "xxx",
  "remark": "xxx",
  "couponId": "xxx",
  "address": {
    "building": "3号楼",
    "room": "201",
    "contact": "张三",
    "phone": "13800138000"
  }
}
```

**变更点**:
- `GroupOrderController.create()` 方法接收 `address` 参数
- `GroupOrderService.createGroupOrder()` 方法保存发起者地址到 `GroupOrderMember`
- `GroupOrderMember` 实体添加地址字段

#### 2. 修改 `/group-order/join` 接口

**新增参数**:
```json
{
  "groupOrderId": "xxx",
  "userId": "xxx",
  "address": {
    "building": "3号楼",
    "room": "201",
    "contact": "张三",
    "phone": "13800138000"
  }
}
```

**变更点**:
- `GroupOrderController.join()` 方法接收 `address` 参数
- `GroupOrderService.joinGroupOrder()` 方法保存参与者地址到 `GroupOrderMember`

#### 3. 修改 `acceptGroupOrder` 方法

**变更点**: 从 `GroupOrderMember` 读取地址并设置到生成的订单中

```java
// 之前
order.setAddressBuilding("拼单外送");
order.setAddressRoom("");
order.setAddressContact(m.getUserName());
order.setAddressPhone("");

// 修改后
order.setAddressBuilding(m.getAddressBuilding());
order.setAddressRoom(m.getAddressRoom());
order.setAddressContact(m.getAddressContact());
order.setAddressPhone(m.getAddressPhone());
```

---

### 三、前端组件变更

#### 1. 发起拼单对话框 (`MerchantDetail.vue`)

在优惠券选择下方添加地址选择区域：

```
┌─────────────────────────────────────┐
│ 🎫 可用优惠券 (2)                    │
│ ┌─────────┐ ┌─────────┐             │
│ │ 8.5折   │ │ 9折     │             │
│ └─────────┘ └─────────┘             │
│                                     │
│ 📍 配送地址 (必选)  [更改地址 >]     │
│ ┌─────────────────────────────────┐ │
│ │ 3号楼 201室                      │ │
│ │ 张三 138****8000                 │ │
│ └─────────────────────────────────┘ │
│                                     │
│      [取消]          [确认发起]      │
└─────────────────────────────────────┘
```

**实现要点**:
- 创建 `AddressSelector.vue` 组件，复用现有地址管理功能
- 默认使用用户的默认地址
- 提供"更改地址"功能，打开地址选择器弹窗
- 地址为必选项，无地址时显示提示

#### 2. 参与拼单确认对话框 (`GroupOrderList.vue`)

在确认对话框中添加地址选择：

```
┌─────────────────────────────────────┐
│         参与拼单                     │
│                                     │
│ 确定「麻辣香锅」的拼单吗？            │
│                                     │
│ 📍 配送地址 (必选)  [更改地址 >]     │
│ ┌─────────────────────────────────┐ │
│ │ 3号楼 201室                      │ │
│ │ 张三 138****8000                 │ │
│ └─────────────────────────────────┘ │
│                                     │
│   [再想想]        [确定参与]         │
└─────────────────────────────────────┘
```

**实现要点**:
- 修改现有的 `ElMessageBox.confirm()` 为自定义对话框
- 复用 `AddressSelector.vue` 组件
- 将选中的地址与拼单加入请求一起发送

#### 3. 新增 `AddressSelector.vue` 组件

**Props**:
- `modelValue`: 当前选中的地址对象
- `userId`: 用户ID

**Events**:
- `update:modelValue`: 地址变更时触发

**功能**:
- 显示当前选中地址
- 点击"更改地址"打开地址选择弹窗
- 复用 `AddressManage.vue` 的地址列表

---

### 四、数据流

```
用户发起拼单:
用户 → 选择菜品 → 选择优惠券 → 选择地址 → 确认发起
                                    ↓
                            保存到 GroupOrderMember.address_*

用户参与拼单:
用户 → 点击参与 → 选择地址 → 确认参与
                      ↓
              保存到 GroupOrderMember.address_*

商家接单:
商家 → 确认接单 → 读取每个成员的地址 → 生成配送订单
                            ↓
                    Order.setAddress*(member.getAddress*)
```

---

## 边界情况处理

1. **用户没有地址**: 显示提示，引导用户先添加地址
2. **地址验证**: 手机号格式验证，必填字段验证
3. **旧数据兼容**: 对于已存在但无地址的 `GroupOrderMember`，在接单时需要提示商家

---

## 测试要点

1. 发起拼单时能正常选择地址
2. 参与拼单时能正常选择地址
3. 商家接单后生成的订单地址正确
4. 无地址用户有明确提示
5. 地址验证正常工作
