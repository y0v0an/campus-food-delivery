# 地址管理功能设计文档

**日期**: 2026-05-13
**状态**: 已批准
**优先级**: 中

## 问题背景

用户反馈个人中心"收货地址"功能点击无响应，无法正常使用。经分析发现：
- 后端 API 和数据库功能完整正常
- 前端代码逻辑存在，但地址管理功能内嵌在弹窗中，用户体验不佳
- 缺少独立的地址管理页面和路由

## 设计目标

创建独立的地址管理页面，提供完整的地址 CRUD 功能，改善用户体验。

## 技术方案

### 1. 新建文件

| 文件路径 | 说明 |
|----------|------|
| `vue/src/views/Student/AddressManage.vue` | 独立地址管理页面 |

### 2. 修改文件

| 文件路径 | 修改内容 |
|----------|----------|
| `vue/src/router/index.js` | 添加 `/student/addresses` 路由 |
| `vue/src/views/Student/Profile.vue` | 移除地址管理弹窗，改为跳转链接 |

### 3. 页面结构

```
AddressManage.vue
├── 页面头部
│   ├── 返回按钮
│   └── 标题"收货地址"
├── 地址列表
│   ├── 空状态提示（无地址时显示）
│   └── 地址卡片列表
│       ├── 地址信息（楼栋、房间、联系人、电话）
│       ├── 默认标签（isDefault=true时显示）
│       └── 操作按钮（设为默认、编辑、删除）
├── 底部新增按钮（固定）
└── 新增/编辑弹窗
    └── 表单（楼栋、房间、联系人、电话、设为默认）
```

### 4. 路由配置

在 `router/index.js` 的 `student` children 中添加：

```javascript
{
  path: 'addresses',
  name: 'AddressManage',
  component: () => import('@/views/Student/AddressManage.vue'),
  meta: { requiresAuth: true, role: 'student' }
}
```

### 5. API 接口

复用现有后端接口：

| 接口 | 方法 | 功能 |
|------|------|------|
| `/api/address/{userId}` | GET | 获取地址列表 |
| `/api/address/add` | POST | 新增地址 |
| `/api/address/update` | PUT | 更新地址 |
| `/api/address/{id}` | DELETE | 删除地址 |
| `/api/address/default/{id}` | PUT | 设为默认 |

### 6. 数据模型

```typescript
interface Address {
  id: string
  userId: string
  building: string    // 楼栋
  room: string        // 房间号
  contact: string     // 联系人
  phone: string       // 电话
  isDefault: boolean  // 是否默认
  lat?: number        // 纬度（可选）
  lng?: number        // 经度（可选）
  createdAt: string   // 创建时间
}
```

### 7. 功能规格

#### 7.1 地址列表

- 按 `isDefault` 降序、`createdAt` 降序排序
- 默认地址显示"默认"标签
- 空列表时显示引导提示

#### 7.2 新增地址

- 点击底部固定按钮打开弹窗
- 表单字段：楼栋、房间、联系人、电话、设为默认
- 验证规则：所有字段必填，手机号格式验证
- 首个地址自动设为默认

#### 7.3 编辑地址

- 点击"编辑"按钮打开弹窗，预填充数据
- 保存时调用更新接口

#### 7.4 删除地址

- 二次确认弹窗
- 删除默认地址时，自动将第一个地址设为默认

#### 7.5 设为默认

- 点击"设为默认"按钮
- 后端自动取消其他地址的默认状态

### 8. UI 设计要点

- 遵循现有移动端设计风格
- 圆角卡片布局，渐变背景
- 底部固定新增按钮（56px 高度）
- 空状态插图和引导文案
- 操作按钮样式：编辑（灰色）、删除（红色）、设为默认（蓝色）

### 9. 入口更新

**Profile.vue 修改：**

```javascript
// 原代码
<div class="menu-item" @click="showAddressDialog = true">
  ...
</div>

// 修改为
<div class="menu-item" @click="goToAddresses">
  ...
</div>

// 添加方法
const goToAddresses = () => {
  router.push('/student/addresses')
}
```

同时移除 Profile.vue 中的：
- `showAddressDialog` 相关变量和方法
- 地址管理弹窗模板代码
- 地址相关的 API 调用方法

### 10. 不变的部分

- `Checkout.vue` 中的地址选择功能保持不变
- 后端代码无需修改
- 数据库结构无需修改

## 实现检查清单

- [ ] 创建 `AddressManage.vue` 文件
- [ ] 实现地址列表展示
- [ ] 实现新增地址功能
- [ ] 实现编辑地址功能
- [ ] 实现删除地址功能
- [ ] 实现设为默认功能
- [ ] 添加表单验证
- [ ] 添加空状态处理
- [ ] 添加错误处理和提示
- [ ] 更新路由配置
- [ ] 修改 Profile.vue 入口
- [ ] 测试完整流程

## 测试计划

1. 无地址时显示空状态
2. 新增第一个地址自动设为默认
3. 新增地址时设为默认取消其他默认
4. 编辑地址保存成功
5. 删除地址二次确认
6. 删除默认地址后其他地址自动设为默认
7. 设为默认功能正常
8. 表单验证（空值、手机号格式）
9. API 错误处理
10. 返回导航正常
