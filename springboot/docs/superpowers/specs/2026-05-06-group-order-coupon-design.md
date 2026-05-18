# 拼单优惠券功能设计文档

**创建日期**: 2026-05-06
**项目**: CFGs 校园外卖配送系统

---

## 1. 功能概述

为拼单功能添加优惠券激励系统，既提高学生参与拼单的积极性，又给商家提供灵活的促销工具。

### 核心目标
- 提高拼单参与度：通过优惠券吸引更多学生加入拼单
- 商家促销工具：给商家提供拼单折扣促销能力
- 保证骑手收入：拼单配送费适当提高后全员平分

---

## 2. 需求总结

| 维度 | 设计决策 |
|------|----------|
| 目的 | 激励参与 + 商家促销 |
| 优惠类型 | 折扣优惠（如 8 折） |
| 创建权限 | 商家自行创建 |
| 使用规则 | 折扣率 + 人数门槛 + 有效期 |
| 库存限制 | 可选是否限量 |
| 应用方式 | 系统默认最优 + 用户手动切换 |
| 价格计算 | 替代拼单价，原价打折 |
| 频次限制 | 每人限用次数 |
| 展示位置 | 菜单详情页 + 商家首页 + 拼单列表 |
| 券类型 | 商家菜品券 + 平台通用券 |

---

## 3. 数据库设计

### 3.1 拼单优惠券表 (group_coupons)

```sql
CREATE TABLE group_coupons (
    id VARCHAR(32) PRIMARY KEY COMMENT '优惠券ID',
    merchant_id VARCHAR(32) NOT NULL COMMENT '商家ID，平台券为platform',
    coupon_type VARCHAR(20) NOT NULL COMMENT '券类型: dish-菜品券/shop-店铺券/platform-平台券',

    dish_id VARCHAR(32) COMMENT '菜品ID（仅dish类型需要）',

    discount_rate DECIMAL(4,2) NOT NULL COMMENT '折扣率（如0.80表示8折）',
    min_people INT NOT NULL DEFAULT 2 COMMENT '最低拼单人数',

    total_limit INT COMMENT '总限量（NULL=不限）',
    used_count INT DEFAULT 0 COMMENT '已使用次数',
    per_user_limit INT DEFAULT 1 COMMENT '每人限用次数',

    valid_from DATETIME NOT NULL COMMENT '有效期开始',
    valid_until DATETIME NOT NULL COMMENT '有效期结束',

    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-活跃/paused-暂停/expired-过期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_merchant_type (merchant_id, coupon_type),
    INDEX idx_dish (dish_id),
    INDEX idx_valid_period (valid_from, valid_until)
) COMMENT '拼单优惠券表';
```

### 3.2 优惠券使用记录表 (group_coupon_usage)

```sql
CREATE TABLE group_coupon_usage (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id VARCHAR(32) NOT NULL COMMENT '优惠券ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    group_order_id VARCHAR(32) NOT NULL COMMENT '拼单ID',
    used_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
    discount_amount DECIMAL(10,2) NOT NULL COMMENT '优惠金额',

    UNIQUE KEY uk_coupon_user (coupon_id, user_id, group_order_id),
    INDEX idx_user (user_id),
    INDEX idx_coupon (coupon_id)
) COMMENT '拼单优惠券使用记录表';
```

### 3.3 表结构修改

**group_orders 表新增字段**：

```sql
ALTER TABLE group_orders ADD COLUMN coupon_id VARCHAR(32) COMMENT '使用的优惠券ID';
ALTER TABLE group_orders ADD COLUMN discount_rate DECIMAL(4,2) COMMENT '实际折扣率';
ALTER TABLE group_orders ADD COLUMN original_price DECIMAL(10,2) COMMENT '菜品原价';
```

---

## 4. 核心业务逻辑

### 4.1 拼单配送费计算

```
1. 拼单满员后，收集所有成员的配送地址
2. 调用配送费计算接口，获取每个成员的单独配送费
3. 计算平均配送费 = sum(各成员配送费) / 成员数
4. 拼单配送费 = 平均配送费 + 固定加收(可配置，默认2元)
5. 每个成员的订单配送费 = 拼单配送费
```

**计算示例**：

| 成员 | 原配送费 | |
|------|----------|---|
| A | 5元 | 平均 = (5+4+6)/3 = 5元 |
| B | 4元 | 拼单配送费 = 5 + 2 = 7元 |
| C | 6元 | 每人支付 = 7元 |
| **骑手收入** | | **7 × 3 = 21元** |

### 4.2 优惠券匹配逻辑

**自动匹配最优券**：
```java
// 伪代码
Coupon bestCoupon = null;
for (coupon in availableCoupons) {
    // 检查是否有效
    if (!isValid(coupon)) continue;
    // 检查拼单人数是否满足
    if (currentCount < coupon.minPeople) continue;
    // 检查用户使用次数
    if (getUserUsedCount(userId, coupon.id) >= coupon.perUserLimit) continue;
    // 检查库存
    if (coupon.totalLimit != null && coupon.usedCount >= coupon.totalLimit) continue;

    // 选择折扣率最高的
    if (bestCoupon == null || coupon.discountRate < bestCoupon.discountRate) {
        bestCoupon = coupon;
    }
}
return bestCoupon;
```

### 4.3 价格计算

```
优惠后单价 = 菜品原价 × 折扣率
订单总额 = 优惠后单价 × 数量 + 拼单配送费
```

---

## 5. 后端 API 接口设计

### 5.1 商家端 - 优惠券管理

| 接口 | 方法 | 描述 | 请求参数 |
|------|------|------|----------|
| `/api/merchant/coupons` | POST | 创建拼单优惠券 | couponDto |
| `/api/merchant/coupons` | GET | 查询我的优惠券列表 | page, size, status |
| `/api/merchant/coupons/{id}` | GET | 获取优惠券详情 | - |
| `/api/merchant/coupons/{id}` | PUT | 更新优惠券 | couponDto |
| `/api/merchant/coupons/{id}` | DELETE | 删除优惠券 | - |
| `/api/merchant/coupons/{id}/pause` | POST | 暂停/恢复优惠券 | - |

**CouponDto 结构**：
```java
{
    "id": "gc_12345678",
    "couponType": "dish",           // dish/shop/platform
    "dishId": "d_123",              // 菜品券必填
    "discountRate": 0.80,           // 8折
    "minPeople": 3,                 // 至少3人
    "totalLimit": 100,              // 总限量100
    "perUserLimit": 1,              // 每人限用1次
    "validFrom": "2026-05-06T00:00:00",
    "validUntil": "2026-05-13T23:59:59",
    "status": "active"
}
```

### 5.2 学生端 - 优惠券使用

| 接口 | 方法 | 描述 | 请求参数 |
|------|------|------|----------|
| `/api/student/coupons/available` | GET | 获取可用拼单优惠券 | merchantId, dishId |
| `/api/group-order/create` | POST | 发起拼单（增加参数） | dishId, couponId |
| `/api/group-order/{id}/join` | POST | 加入拼单（返回价格） | - |
| `/api/group-order/{id}` | GET | 拼单详情（含优惠信息） | - |

### 5.3 返回数据结构

**可用优惠券列表响应**：
```java
{
    "code": 200,
    "message": "success",
    "data": {
        "bestCoupon": {
            "id": "gc_12345678",
            "discountRate": 0.80,
            "minPeople": 3,
            "validUntil": "2026-05-13T23:59:59"
        },
        "availableCoupons": [
            // 所有可用优惠券列表
        ],
        "priceCalculation": {
            "originalPrice": 15.00,
            "discountedPrice": 12.00,
            "savedAmount": 3.00
        }
    }
}
```

---

## 6. 前端改动

### 6.1 新增页面

| 页面 | 路径 | 描述 |
|------|------|------|
| 优惠券管理 | `Merchant/CouponManage.vue` | 商家管理优惠券（列表、创建、编辑、删除） |

### 6.2 改动页面

| 页面 | 改动内容 |
|------|----------|
| `Student/MerchantDetail.vue` | 菜品详情页增加优惠券展示和选择 |
| `Student/HomePage.vue` | 拼单列表增加优惠标识 |
| 发起/加入拼单弹窗 | 显示优惠后价格 |

### 6.3 UI 设计要点

**优惠券展示位置**：
1. 商家首页显眼位置 - 显示当前有效的拼单优惠活动
2. 菜品详情页 - 发起拼单时展示可用优惠券
3. 拼单列表页 - 显示优惠标识

**优惠券选择器 UI**：
```
┌─────────────────────────────────┐
│  🎫 可用优惠券 (2)               │
│  ┌─────────────────────────┐   │
│  │ ✓ 拼单8折 (推荐)         │   │
│  │   最少3人 | 有效期至5/13  │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │   拼单9折                │   │
│  │   最少2人 | 有效期至5/20  │   │
│  └─────────────────────────┘   │
│                                 │
│  原价: ¥15  优惠后: ¥12         │
│  [发起拼单]                     │
└─────────────────────────────────┘
```

---

## 7. 实体类设计

### 7.1 GroupCoupon 实体

```java
@Data
@TableName("group_coupons")
public class GroupCoupon {
    @TableId
    private String id;

    private String merchantId;
    private String couponType;      // dish/shop/platform

    private String dishId;          // 仅 dish 类型需要

    private BigDecimal discountRate;
    private Integer minPeople;

    private Integer totalLimit;
    private Integer usedCount;
    private Integer perUserLimit;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private String status;          // active/paused/expired
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String dishName;        // 关联查询

    @TableField(exist = false)
    private Integer remainingCount; // 剩余数量
}
```

### 7.2 GroupCouponUsage 实体

```java
@Data
@TableName("group_coupon_usage")
public class GroupCouponUsage {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String couponId;
    private String userId;
    private String groupOrderId;
    private LocalDateTime usedAt;
    private BigDecimal discountAmount;
}
```

---

## 8. 配置项

### 8.1 系统配置

| 配置项 | 默认值 | 描述 |
|--------|--------|------|
| `group.delivery.extra-fee` | 2.0 | 拼单配送费固定加收金额（元） |

---

## 9. 后续扩展

该设计预留了扩展空间，未来可支持：
- 满减型优惠券（满X元减Y元）
- 固定金额减免（立减X元）
- 新用户专享券
- 节日主题券
- 优惠券分享/转赠功能
