# 拼单优惠券功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为拼单功能添加优惠券激励系统，支持商家创建拼单优惠券，学生发起/加入拼单时自动应用优惠，配送费适当提高后全员平分。

**Architecture:** 后端采用独立优惠券表设计，与现有拼单系统解耦；前端在商家端新增优惠券管理页面，学生端在拼单流程中增加优惠券展示和选择功能。

**Tech Stack:** Spring Boot 2.7.18, MyBatis-Plus 3.5.3.1, MySQL 8, Vue 3, Vite 6, Element Plus

---

## 文件结构概览

### 后端新增文件
```
src/main/java/com/community/cfgs/
├── entity/
│   ├── GroupCoupon.java                    # 拼单优惠券实体
│   └── GroupCouponUsage.java               # 优惠券使用记录实体
├── mapper/
│   ├── GroupCouponMapper.java              # 优惠券 Mapper
│   └── GroupCouponUsageMapper.java         # 使用记录 Mapper
├── dto/
│   ├── GroupCouponCreateRequest.java       # 创建优惠券请求
│   ├── GroupCouponUpdateRequest.java       # 更新优惠券请求
│   └── AvailableCouponResponse.java        # 可用优惠券响应
├── service/
│   └── GroupCouponService.java             # 优惠券服务
└── controller/
    └── GroupCouponController.java          # 优惠券控制器（商家端+学生端）
```

### 后端修改文件
```
src/main/java/com/community/cfgs/
├── entity/GroupOrder.java                  # 新增 couponId, discountRate, originalPrice 字段
├── service/GroupOrderService.java          # 修改接单逻辑（配送费计算+优惠券应用）
└── controller/GroupOrderController.java    # 修改发起/加入拼单接口
```

### 前端新增文件
```
src/views/Merchant/
└── GroupCouponManage.vue                   # 拼单优惠券管理页面

src/api/
└── groupCoupon.js                          # 拼单优惠券 API
```

### 前端修改文件
```
src/views/Student/
├── MerchantDetail.vue                      # 新增优惠券展示和选择
└── HomePage.vue                            # 拼单列表增加优惠标识
```

### 数据库迁移文件
```
docs/db/
└── add_group_coupon_tables.sql             # 优惠券表 + 使用记录表 + group_orders 表修改
```

---

## Task 1: 创建数据库迁移脚本

**Files:**
- Create: `docs/db/add_group_coupon_tables.sql`

- [ ] **Step 1: 创建 SQL 迁移脚本**

```sql
-- =====================================================
-- 拼单优惠券功能数据库迁移脚本
-- 创建日期: 2026-05-06
-- =====================================================

-- 1.1 创建拼单优惠券表
CREATE TABLE IF NOT EXISTS group_coupons (
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

-- 1.2 创建优惠券使用记录表
CREATE TABLE IF NOT EXISTS group_coupon_usage (
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

-- 1.3 修改 group_orders 表，新增优惠券相关字段
ALTER TABLE group_orders
ADD COLUMN IF NOT EXISTS coupon_id VARCHAR(32) COMMENT '使用的优惠券ID',
ADD COLUMN IF NOT EXISTS discount_rate DECIMAL(4,2) COMMENT '实际折扣率',
ADD COLUMN IF NOT EXISTS original_price DECIMAL(10,2) COMMENT '菜品原价';

-- 1.4 添加系统配置（如需要单独的配置表）
-- 注意：如果项目使用 application.yml 配置，跳过此步骤
-- 此处假设在代码中使用配置项 @Value("${group.delivery.extra-fee:2.0}")
```

- [ ] **Step 2: 执行数据库迁移**

```bash
# 使用 Navicat 或 MySQL 客户端执行迁移脚本
# 1. 打开 Navicat
# 2. 连接到数据库
# 3. 打开查询编辑器
# 4. 粘贴 add_group_coupon_tables.sql 内容
# 5. 执行 (F5)
```

预期结果：表创建成功，无错误信息

- [ ] **Step 3: 验证表结构**

```sql
-- 验证表是否创建成功
SHOW TABLES LIKE 'group_coupon%';
DESC group_coupons;
DESC group_coupon_usage;

-- 验证 group_orders 表字段
DESC group_orders;
```

预期结果：显示 3 个表，字段结构正确

- [ ] **Step 4: 提交**

```bash
git add docs/db/add_group_coupon_tables.sql
git commit -m "feat: 添加拼单优惠券数据库表"
```

---

## Task 2: 创建 GroupCoupon 实体类

**Files:**
- Create: `src/main/java/com/community/cfgs/entity/GroupCoupon.java`
- Reference: `src/main/java/com/community/cfgs/entity/GroupOrder.java` (参考现有实体风格)

- [ ] **Step 1: 创建 GroupCoupon 实体类**

```java
package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String dishName;        // 关联查询用

    @TableField(exist = false)
    private Integer remainingCount; // 剩余数量
}
```

- [ ] **Step 2: 创建 GroupCouponUsage 实体类**

```java
package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

- [ ] **Step 3: 验证编译**

```bash
cd E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\springboot
mvn compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/community/cfgs/entity/GroupCoupon.java
git add src/main/java/com/community/cfgs/entity/GroupCouponUsage.java
git commit -m "feat: 添加拼单优惠券实体类"
```

---

## Task 3: 创建 DTO 类

**Files:**
- Create: `src/main/java/com/community/cfgs/dto/GroupCouponCreateRequest.java`
- Create: `src/main/java/com/community/cfgs/dto/GroupCouponUpdateRequest.java`
- Create: `src/main/java/com/community/cfgs/dto/AvailableCouponResponse.java`
- Reference: `src/main/java/com/community/cfgs/dto/StudentRegisterRequest.java` (参考现有 DTO 风格)

- [ ] **Step 1: 创建 GroupCouponCreateRequest**

```java
package com.community.cfgs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupCouponCreateRequest {
    private String couponType;      // dish/shop/platform
    private String dishId;          // 菜品券必填
    private Double discountRate;    // 折扣率（如0.80）
    private Integer minPeople;      // 最低拼单人数
    private Integer totalLimit;     // 总限量
    private Integer perUserLimit;   // 每人限用次数
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}
```

- [ ] **Step 2: 创建 GroupCouponUpdateRequest**

```java
package com.community.cfgs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupCouponUpdateRequest {
    private String id;
    private String couponType;
    private String dishId;
    private Double discountRate;
    private Integer minPeople;
    private Integer totalLimit;
    private Integer perUserLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
}
```

- [ ] **Step 3: 创建 AvailableCouponResponse**

```java
package com.community.cfgs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvailableCouponResponse {
    private CouponInfo bestCoupon;
    private List<CouponInfo> availableCoupons;
    private PriceCalculation priceCalculation;

    @Data
    public static class CouponInfo {
        private String id;
        private String couponType;
        private BigDecimal discountRate;
        private Integer minPeople;
        private LocalDateTime validUntil;
        private Integer remainingCount;
    }

    @Data
    public static class PriceCalculation {
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
        private BigDecimal savedAmount;
    }
}
```

- [ ] **Step 4: 验证编译**

```bash
mvn compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/community/cfgs/dto/GroupCouponCreateRequest.java
git add src/main/java/com/community/cfgs/dto/GroupCouponUpdateRequest.java
git add src/main/java/com/community/cfgs/dto/AvailableCouponResponse.java
git commit -m "feat: 添加拼单优惠券 DTO 类"
```

---

## Task 4: 创建 Mapper 接口

**Files:**
- Create: `src/main/java/com/community/cfgs/mapper/GroupCouponMapper.java`
- Create: `src/main/java/com/community/cfgs/mapper/GroupCouponUsageMapper.java`
- Reference: `src/main/java/com/community/cfgs/mapper/GroupOrderMapper.java` (参考现有 Mapper 风格)

- [ ] **Step 1: 创建 GroupCouponMapper**

```java
package com.community.cfgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.cfgs.entity.GroupCoupon;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupCouponMapper extends BaseMapper<GroupCoupon> {
}
```

- [ ] **Step 2: 创建 GroupCouponUsageMapper**

```java
package com.community.cfgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.cfgs.entity.GroupCouponUsage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupCouponUsageMapper extends BaseMapper<GroupCouponUsage> {
}
```

- [ ] **Step 3: 验证编译**

```bash
mvn compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/community/cfgs/mapper/GroupCouponMapper.java
git add src/main/java/com/community/cfgs/mapper/GroupCouponUsageMapper.java
git commit -m "feat: 添加拼单优惠券 Mapper 接口"
```

---

## Task 5: 创建 GroupCouponService 服务类

**Files:**
- Create: `src/main/java/com/community/cfgs/service/GroupCouponService.java`
- Modify: `src/main/java/com/community/cfgs/entity/GroupOrder.java` (添加新字段)

- [ ] **Step 1: 先修改 GroupOrder 实体类，添加新字段**

打开 `src/main/java/com/community/cfgs/entity/GroupOrder.java`，在类中添加以下字段：

```java
// 在现有字段后添加
private String couponId;           // 使用的优惠券ID
private BigDecimal discountRate;   // 实际折扣率
private BigDecimal originalPrice;  // 菜品原价
```

- [ ] **Step 2: 创建 GroupCouponService**

```java
package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.cfgs.dto.AvailableCouponResponse;
import com.community.cfgs.dto.GroupCouponCreateRequest;
import com.community.cfgs.dto.GroupCouponUpdateRequest;
import com.community.cfgs.entity.Dish;
import com.community.cfgs.entity.GroupCoupon;
import com.community.cfgs.entity.GroupCouponUsage;
import com.community.cfgs.mapper.DishMapper;
import com.community.cfgs.mapper.GroupCouponMapper;
import com.community.cfgs.mapper.GroupCouponUsageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupCouponService {

    @Autowired
    private GroupCouponMapper groupCouponMapper;

    @Autowired
    private GroupCouponUsageMapper groupCouponUsageMapper;

    @Autowired
    private DishMapper dishMapper;

    private static final BigDecimal DELIVERY_EXTRA_FEE = new BigDecimal("2.00");

    /**
     * 创建拼单优惠券
     */
    @Transactional(rollbackFor = Exception.class)
    public GroupCoupon createCoupon(String merchantId, GroupCouponCreateRequest request) {
        // 验证
        if ("dish".equals(request.getCouponType()) && request.getDishId() == null) {
            throw new RuntimeException("菜品券必须指定菜品");
        }
        if (request.getDiscountRate() == null || request.getDiscountRate() <= 0 || request.getDiscountRate() > 1) {
            throw new RuntimeException("折扣率必须在 0-1 之间");
        }
        if (request.getValidFrom().isAfter(request.getValidUntil())) {
            throw new RuntimeException("有效期开始时间不能晚于结束时间");
        }

        GroupCoupon coupon = new GroupCoupon();
        coupon.setId("gc_" + UUID.randomUUID().toString().substring(0, 8));
        coupon.setMerchantId(merchantId);
        coupon.setCouponType(request.getCouponType());
        coupon.setDishId(request.getDishId());
        coupon.setDiscountRate(BigDecimal.valueOf(request.getDiscountRate()));
        coupon.setMinPeople(request.getMinPeople() != null ? request.getMinPeople() : 2);
        coupon.setTotalLimit(request.getTotalLimit());
        coupon.setUsedCount(0);
        coupon.setPerUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1);
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setStatus("active");
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        groupCouponMapper.insert(coupon);
        return coupon;
    }

    /**
     * 更新优惠券
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCoupon(String merchantId, GroupCouponUpdateRequest request) {
        GroupCoupon coupon = groupCouponMapper.selectById(request.getId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此优惠券");
        }

        coupon.setCouponType(request.getCouponType());
        coupon.setDishId(request.getDishId());
        if (request.getDiscountRate() != null) {
            coupon.setDiscountRate(BigDecimal.valueOf(request.getDiscountRate()));
        }
        coupon.setMinPeople(request.getMinPeople());
        coupon.setTotalLimit(request.getTotalLimit());
        coupon.setPerUserLimit(request.getPerUserLimit());
        if (request.getValidFrom() != null) {
            coupon.setValidFrom(request.getValidFrom());
        }
        if (request.getValidUntil() != null) {
            coupon.setValidUntil(request.getValidUntil());
        }
        if (request.getStatus() != null) {
            coupon.setStatus(request.getStatus());
        }
        coupon.setUpdatedAt(LocalDateTime.now());

        return groupCouponMapper.updateById(coupon) > 0;
    }

    /**
     * 删除优惠券
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCoupon(String merchantId, String couponId) {
        GroupCoupon coupon = groupCouponMapper.selectById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此优惠券");
        }
        return groupCouponMapper.deleteById(couponId) > 0;
    }

    /**
     * 暂停/恢复优惠券
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCouponStatus(String merchantId, String couponId) {
        GroupCoupon coupon = groupCouponMapper.selectById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此优惠券");
        }

        String newStatus = "active".equals(coupon.getStatus()) ? "paused" : "active";
        coupon.setStatus(newStatus);
        coupon.setUpdatedAt(LocalDateTime.now());
        return groupCouponMapper.updateById(coupon) > 0;
    }

    /**
     * 获取商家的优惠券列表
     */
    public List<GroupCoupon> listByMerchant(String merchantId) {
        LambdaQueryWrapper<GroupCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupCoupon::getMerchantId, merchantId)
                .orderByDesc(GroupCoupon::getCreatedAt);
        List<GroupCoupon> coupons = groupCouponMapper.selectList(wrapper);

        // 填充菜品名称和剩余数量
        coupons.forEach(c -> {
            if (c.getDishId() != null) {
                Dish dish = dishMapper.selectById(c.getDishId());
                if (dish != null) {
                    c.setDishName(dish.getName());
                }
            }
            if (c.getTotalLimit() != null) {
                c.setRemainingCount(c.getTotalLimit() - c.getUsedCount());
            }
        });
        return coupons;
    }

    /**
     * 获取用户可用的拼单优惠券
     */
    public AvailableCouponResponse getAvailableCoupons(String userId, String merchantId, String dishId, Integer currentPeople) {
        LambdaQueryWrapper<GroupCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupCoupon::getMerchantId, merchantId)
                .eq(GroupCoupon::getStatus, "active")
                .le(GroupCoupon::getValidFrom, LocalDateTime.now())
                .ge(GroupCoupon::getValidUntil, LocalDateTime.now());
        List<GroupCoupon> allCoupons = groupCouponMapper.selectList(wrapper);

        // 过滤可用优惠券
        List<GroupCoupon> availableCoupons = allCoupons.stream()
                .filter(c -> {
                    // 检查类型
                    if ("dish".equals(c.getCouponType()) && !c.getDishId().equals(dishId)) {
                        return false;
                    }
                    // 检查人数
                    if (currentPeople != null && currentPeople < c.getMinPeople()) {
                        return false;
                    }
                    // 检查库存
                    if (c.getTotalLimit() != null && c.getUsedCount() >= c.getTotalLimit()) {
                        return false;
                    }
                    // 检查用户使用次数
                    LambdaQueryWrapper<GroupCouponUsage> usageWrapper = new LambdaQueryWrapper<>();
                    usageWrapper.eq(GroupCouponUsage::getCouponId, c.getId())
                            .eq(GroupCouponUsage::getUserId, userId);
                    long userUsedCount = groupCouponUsageMapper.selectCount(usageWrapper);
                    if (userUsedCount >= c.getPerUserLimit()) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 找出最优券
        GroupCoupon bestCoupon = availableCoupons.stream()
                .min((c1, c2) -> c1.getDiscountRate().compareTo(c2.getDiscountRate()))
                .orElse(null);

        // 构建响应
        AvailableCouponResponse response = new AvailableCouponResponse();
        response.setAvailableCoupons(availableCoupons.stream()
                .map(this::toCouponInfo)
                .collect(Collectors.toList()));

        if (bestCoupon != null) {
            response.setBestCoupon(toCouponInfo(bestCoupon));
        }

        return response;
    }

    private AvailableCouponResponse.CouponInfo toCouponInfo(GroupCoupon coupon) {
        AvailableCouponResponse.CouponInfo info = new AvailableCouponResponse.CouponInfo();
        info.setId(coupon.getId());
        info.setCouponType(coupon.getCouponType());
        info.setDiscountRate(coupon.getDiscountRate());
        info.setMinPeople(coupon.getMinPeople());
        info.setValidUntil(coupon.getValidUntil());
        if (coupon.getTotalLimit() != null) {
            info.setRemainingCount(coupon.getTotalLimit() - coupon.getUsedCount());
        }
        return info;
    }

    /**
     * 计算优惠后价格
     */
    public BigDecimal calculateDiscountedPrice(String couponId, BigDecimal originalPrice) {
        if (couponId == null) {
            return originalPrice;
        }
        GroupCoupon coupon = groupCouponMapper.selectById(couponId);
        if (coupon == null || !"active".equals(coupon.getStatus())) {
            return originalPrice;
        }
        return originalPrice.multiply(coupon.getDiscountRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 记录优惠券使用
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordUsage(String couponId, String userId, String groupOrderId, BigDecimal discountAmount) {
        // 创建使用记录
        GroupCouponUsage usage = new GroupCouponUsage();
        usage.setCouponId(couponId);
        usage.setUserId(userId);
        usage.setGroupOrderId(groupOrderId);
        usage.setDiscountAmount(discountAmount);
        usage.setUsedAt(LocalDateTime.now());
        groupCouponUsageMapper.insert(usage);

        // 更新优惠券使用次数
        GroupCoupon coupon = groupCouponMapper.selectById(couponId);
        if (coupon != null) {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            coupon.setUpdatedAt(LocalDateTime.now());
            groupCouponMapper.updateById(coupon);
        }
    }

    /**
     * 计算拼单配送费
     * @param individualDeliveryFees 各成员的单独配送费列表
     * @return 每人应支付的配送费
     */
    public BigDecimal calculateGroupDeliveryFee(List<BigDecimal> individualDeliveryFees) {
        if (individualDeliveryFees == null || individualDeliveryFees.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算平均配送费
        BigDecimal sum = individualDeliveryFees.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgFee = sum.divide(
                BigDecimal.valueOf(individualDeliveryFees.size()),
                2,
                BigDecimal.ROUND_HALF_UP
        );

        // 加上固定加收金额
        return avgFee.add(DELIVERY_EXTRA_FEE);
    }
}
```

- [ ] **Step 3: 验证编译**

```bash
mvn compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/community/cfgs/service/GroupCouponService.java
git add src/main/java/com/community/cfgs/entity/GroupOrder.java
git commit -m "feat: 添加拼单优惠券服务类和 GroupOrder 字段"
```

---

## Task 6: 创建 GroupCouponController 控制器

**Files:**
- Create: `src/main/java/com/community/cfgs/controller/GroupCouponController.java`
- Reference: `src/main/java/com/community/cfgs/controller/GroupOrderController.java` (参考现有控制器风格)

- [ ] **Step 1: 创建 GroupCouponController**

```java
package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.dto.AvailableCouponResponse;
import com.community.cfgs.dto.GroupCouponCreateRequest;
import com.community.cfgs.dto.GroupCouponUpdateRequest;
import com.community.cfgs.entity.GroupCoupon;
import com.community.cfgs.service.GroupCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GroupCouponController {

    @Autowired
    private GroupCouponService groupCouponService;

    // ==================== 商家端接口 ====================

    /**
     * 创建拼单优惠券
     */
    @PostMapping("/merchant/coupons")
    public Result<GroupCoupon> createCoupon(@RequestHeader("X-User-Id") String merchantId,
                                            @RequestBody GroupCouponCreateRequest request) {
        try {
            GroupCoupon coupon = groupCouponService.createCoupon(merchantId, request);
            return Result.success(coupon);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的优惠券列表
     */
    @GetMapping("/merchant/coupons")
    public Result<List<GroupCoupon>> listMyCoupons(@RequestHeader("X-User-Id") String merchantId) {
        List<GroupCoupon> coupons = groupCouponService.listByMerchant(merchantId);
        return Result.success(coupons);
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/merchant/coupons/{id}")
    public Result<GroupCoupon> getCouponDetail(@RequestHeader("X-User-Id") String merchantId,
                                               @PathVariable String id) {
        GroupCoupon coupon = groupCouponService.listByMerchant(merchantId).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        return Result.success(coupon);
    }

    /**
     * 更新优惠券
     */
    @PutMapping("/merchant/coupons/{id}")
    public Result<String> updateCoupon(@RequestHeader("X-User-Id") String merchantId,
                                       @PathVariable String id,
                                       @RequestBody GroupCouponUpdateRequest request) {
        try {
            request.setId(id);
            boolean success = groupCouponService.updateCoupon(merchantId, request);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/merchant/coupons/{id}")
    public Result<String> deleteCoupon(@RequestHeader("X-User-Id") String merchantId,
                                       @PathVariable String id) {
        try {
            boolean success = groupCouponService.deleteCoupon(merchantId, id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 暂停/恢复优惠券
     */
    @PostMapping("/merchant/coupons/{id}/toggle")
    public Result<String> toggleCouponStatus(@RequestHeader("X-User-Id") String merchantId,
                                             @PathVariable String id) {
        try {
            boolean success = groupCouponService.toggleCouponStatus(merchantId, id);
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 学生端接口 ====================

    /**
     * 获取可用拼单优惠券
     */
    @GetMapping("/student/coupons/available")
    public Result<AvailableCouponResponse> getAvailableCoupons(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String merchantId,
            @RequestParam(required = false) String dishId,
            @RequestParam(required = false) Integer currentPeople) {
        AvailableCouponResponse response = groupCouponService.getAvailableCoupons(
                userId, merchantId, dishId, currentPeople);
        return Result.success(response);
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/community/cfgs/controller/GroupCouponController.java
git commit -m "feat: 添加拼单优惠券控制器"
```

---

## Task 7: 修改 GroupOrderService 集成优惠券

**Files:**
- Modify: `src/main/java/com/community/cfgs/service/GroupOrderService.java`

- [ ] **Step 1: 修改 GroupOrderService，注入 GroupCouponService**

在 `GroupOrderService` 类中添加 `@Autowired private GroupCouponService groupCouponService;`

找到 `GroupOrderService` 类中的：
```java
@Autowired
private OrderMapper orderMapper;
```

在其后添加：
```java
@Autowired
private GroupCouponService groupCouponService;
```

同时在类顶部添加导入：
```java
import com.community.cfgs.service.GroupCouponService;
```

- [ ] **Step 2: 修改 createGroupOrder 方法，支持优惠券参数**

找到 `createGroupOrder` 方法签名：
```java
public GroupOrder createGroupOrder(String userId, String dishId, Integer targetCountIgnored, String remark)
```

修改为：
```java
public GroupOrder createGroupOrder(String userId, String dishId, Integer targetCountIgnored, String remark, String couponId)
```

在方法中找到设置 `unitPrice` 的代码块：
```java
BigDecimal unitPrice = dish.getGroupPrice() != null && dish.getGroupPrice().compareTo(BigDecimal.ZERO) > 0
        ? dish.getGroupPrice()
        : dish.getPrice();
```

在其后添加优惠券处理逻辑：
```java
// 处理优惠券
BigDecimal originalPrice = dish.getPrice();
BigDecimal finalUnitPrice = unitPrice;
BigDecimal discountRate = null;

if (couponId != null) {
    finalUnitPrice = groupCouponService.calculateDiscountedPrice(couponId, originalPrice);
    discountRate = groupCouponService.listByMerchant(dish.getMerchantId()).stream()
            .filter(c -> c.getId().equals(couponId))
            .findFirst()
            .map(GroupCoupon::getDiscountRate)
            .orElse(null);
}
```

修改 `setUnitPrice` 调用为使用 `finalUnitPrice`，并添加新字段：
```java
groupOrder.setUnitPrice(finalUnitPrice);
groupOrder.setCouponId(couponId);
groupOrder.setDiscountRate(discountRate);
groupOrder.setOriginalPrice(originalPrice);
```

- [ ] **Step 3: 修改 acceptGroupOrder 方法，计算拼单配送费**

找到 `acceptGroupOrder` 方法中生成订单的循环部分：
```java
for (GroupOrderMember m : members) {
    Order order = new Order();
    // ... 现有代码 ...
    order.setDeliveryFee(BigDecimal.ZERO);
    // ...
}
```

修改配送费计算逻辑（注意：此处需要获取每个成员的地址，实际实现可能需要调整）：

在 `for (GroupOrderMember m : members)` 循环前添加配送费计算：
```java
// 计算拼单配送费
// 注意：此处假设每个成员单独配送费为 5 元，实际需要根据地址计算
List<BigDecimal> individualFees = members.stream()
        .map(m -> new BigDecimal("5.00"))  // TODO: 实际应根据地址计算
        .collect(java.util.stream.Collectors.toList());
BigDecimal groupDeliveryFee = groupCouponService.calculateGroupDeliveryFee(individualFees);
```

修改循环中的 `setDeliveryFee`：
```java
order.setDeliveryFee(groupDeliveryFee);
order.setActualAmount(groupOrder.getUnitPrice().add(groupDeliveryFee));
```

同时修改 `order.setTotalAmount`：
```java
order.setTotalAmount(groupOrder.getUnitPrice().add(groupDeliveryFee));
```

- [ ] **Step 4: 验证编译**

```bash
mvn compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/community/cfgs/service/GroupOrderService.java
git commit -m "feat: 集成优惠券到拼单服务"
```

---

## Task 8: 修改 GroupOrderController 支持优惠券参数

**Files:**
- Modify: `src/main/java/com/community/cfgs/controller/GroupOrderController.java`

- [ ] **Step 1: 修改发起拼单接口，添加 couponId 参数**

找到 `createGroupOrder` 接口：
```java
@PostMapping("/create")
public Result<GroupOrder> createGroupOrder(@RequestParam String dishId,
                                           @RequestParam(required = false) Integer targetCount,
                                           @RequestParam(required = false) String remark)
```

修改为：
```java
@PostMapping("/create")
public Result<GroupOrder> createGroupOrder(@RequestParam String dishId,
                                           @RequestParam(required = false) Integer targetCount,
                                           @RequestParam(required = false) String remark,
                                           @RequestParam(required = false) String couponId)
```

修改方法体中对 `groupOrderService.createGroupOrder` 的调用：
```java
GroupOrder groupOrder = groupOrderService.createGroupOrder(userId, dishId, targetCount, remark, couponId);
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/community/cfgs/controller/GroupOrderController.java
git commit -m "feat: 拼单接口支持优惠券参数"
```

---

## Task 9: 创建前端 API 文件

**Files:**
- Create: `vue/src/api/groupCoupon.js`

- [ ] **Step 1: 创建 groupCoupon API 文件**

```javascript
import request from './request'

// 商家端 - 创建拼单优惠券
export const createGroupCoupon = (data) => {
  return request({
    url: '/merchant/coupons',
    method: 'post',
    data
  })
}

// 商家端 - 获取我的优惠券列表
export const getMyGroupCoupons = () => {
  return request({
    url: '/merchant/coupons',
    method: 'get'
  })
}

// 商家端 - 获取优惠券详情
export const getGroupCouponDetail = (id) => {
  return request({
    url: `/merchant/coupons/${id}`,
    method: 'get'
  })
}

// 商家端 - 更新优惠券
export const updateGroupCoupon = (id, data) => {
  return request({
    url: `/merchant/coupons/${id}`,
    method: 'put',
    data
  })
}

// 商家端 - 删除优惠券
export const deleteGroupCoupon = (id) => {
  return request({
    url: `/merchant/coupons/${id}`,
    method: 'delete'
  })
}

// 商家端 - 暂停/恢复优惠券
export const toggleGroupCouponStatus = (id) => {
  return request({
    url: `/merchant/coupons/${id}/toggle`,
    method: 'post'
  })
}

// 学生端 - 获取可用拼单优惠券
export const getAvailableGroupCoupons = (params) => {
  return request({
    url: '/student/coupons/available',
    method: 'get',
    params
  })
}
```

- [ ] **Step 2: 提交**

```bash
cd E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue
git add src/api/groupCoupon.js
git commit -m "feat: 添加拼单优惠券前端 API"
```

---

## Task 10: 创建商家端优惠券管理页面

**Files:**
- Create: `vue/src/views/Merchant/GroupCouponManage.vue`
- Reference: `vue/src/views/Merchant/CouponManage.vue` (参考现有优惠券管理页面风格)

- [ ] **Step 1: 创建 GroupCouponManage.vue 组件**

```vue
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
import {
  createGroupCoupon,
  getMyGroupCoupons,
  updateGroupCoupon,
  deleteGroupCoupon,
  toggleGroupCouponStatus
} from '@/api/groupCoupon'
import { getDishList } from '@/api/dish'

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
    const res = await getDishList()
    dishes.value = res.data || []
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
```

- [ ] **Step 2: 添加路由**

打开 `vue/src/router/index.js`，在商家路由中添加：

找到 `merchantLayout.children` 数组，添加：
```javascript
{
  path: 'group-coupons',
  name: 'MerchantGroupCoupons',
  component: () => import('@/views/Merchant/GroupCouponManage.vue'),
  meta: { title: '拼单优惠券', icon: 'lucide:ticket' }
}
```

同时在商家侧边栏添加入口（如果有的话）。

- [ ] **Step 3: 提交**

```bash
git add src/views/Merchant/GroupCouponManage.vue
git add src/router/index.js
git commit -m "feat: 添加商家端拼单优惠券管理页面"
```

---

## Task 11: 修改学生端 MerchantDetail 页面支持优惠券

**Files:**
- Modify: `vue/src/views/Student/MerchantDetail.vue`

- [ ] **Step 1: 在 MerchantDetail.vue 中添加优惠券展示**

首先在 `<script setup>` 中添加 API 导入和状态：
```javascript
import { getAvailableGroupCoupons } from '@/api/groupCoupon'

// 在现有 ref 定义后添加
const availableCoupons = ref([])
const selectedCoupon = ref(null)
const showCouponSelector = ref(false)
```

添加加载优惠券的方法：
```javascript
const loadAvailableCoupons = async (dishId) => {
  try {
    const merchantId = route.params.id
    const res = await getAvailableGroupCoupons({
      merchantId,
      dishId
    })
    availableCoupons.value = res.data?.availableCoupons || []
    selectedCoupon.value = res.data?.bestCoupon || null
  } catch (err) {
    console.error('加载优惠券失败', err)
  }
}
```

在 `handleCreateGroupOrder` 方法中传递 `selectedCoupon.value?.id`：
```javascript
const handleCreateGroupOrder = async (dishId, remark) => {
  try {
    const res = await createGroupOrder({
      dishId,
      targetCount: targetCount.value,
      remark,
      couponId: selectedCoupon.value?.id  // 新增
    })
    ElMessage.success('拼单发起成功')
    // ... 现有逻辑
  } catch (err) {
    // ...
  }
}
```

- [ ] **Step 2: 在模板中添加优惠券选择器**

在发起拼单的对话框中，添加优惠券展示：

找到拼单对话框的内容区域，添加：
```vue
<!-- 优惠券选择 -->
<div v-if="availableCoupons.length > 0" class="coupon-section">
  <div class="section-title">🎫 可用优惠券 ({{ availableCoupons.length }})</div>
  <div class="coupon-list">
    <div
      v-for="coupon in availableCoupons"
      :key="coupon.id"
      class="coupon-item"
      :class="{ selected: selectedCoupon?.id === coupon.id }"
      @click="selectedCoupon = coupon"
    >
      <div class="coupon-header">
        <span class="coupon-discount">{{ (coupon.discountRate * 10).toFixed(1) }}折</span>
        <span v-if="selectedCoupon?.id === coupon.id" class="selected-tag">✓ 已选</span>
      </div>
      <div class="coupon-info">
        <span>最少{{ coupon.minPeople }}人</span>
        <span>有效期至 {{ formatDate(coupon.validUntil) }}</span>
      </div>
    </div>
  </div>
</div>
```

- [ ] **Step 3: 添加样式**

在 `<style>` 中添加：
```css
.coupon-section {
  margin: 15px 0;
}

.section-title {
  font-weight: 600;
  margin-bottom: 10px;
  color: #606266;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coupon-item {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.coupon-item:hover {
  border-color: #409eff;
}

.coupon-item.selected {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.coupon-discount {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.selected-tag {
  color: #67c23a;
  font-weight: 600;
}

.coupon-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
```

- [ ] **Step 4: 提交**

```bash
git add src/views/Student/MerchantDetail.vue
git commit -m "feat: 学生端拼单支持优惠券选择"
```

---

## Task 12: 修改学生端 HomePage 拼单列表显示优惠

**Files:**
- Modify: `vue/src/views/Student/HomePage.vue`

- [ ] **Step 1: 在拼单卡片中添加优惠标识**

找到拼单列表的卡片展示部分，在价格显示区域添加优惠标识：

找到显示价格的部分，修改为：
```vue
<div class="price-section">
  <span class="current-price">¥{{ groupOrder.unitPrice }}</span>
  <span v-if="groupOrder.originalPrice && groupOrder.originalPrice > groupOrder.unitPrice"
        class="original-price">
    ¥{{ groupOrder.originalPrice }}
  </span>
  <el-tag v-if="groupOrder.discountRate" type="success" size="small" style="margin-left: 8px;">
    {{ (groupOrder.discountRate * 10).toFixed(1) }}折
  </el-tag>
</div>
```

- [ ] **Step 2: 添加样式**

```css
.price-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}
```

- [ ] **Step 3: 提交**

```bash
git add src/views/Student/HomePage.vue
git commit -m "feat: 拼单列表显示优惠信息"
```

---

## Task 13: 后端编译测试

**Files:**
- Build: `springboot/target/`

- [ ] **Step 1: 清理并编译**

```bash
cd E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\springboot
mvn clean compile -DskipTests
```

预期结果：BUILD SUCCESS

- [ ] **Step 2: 打包**

```bash
mvn package -DskipTests
```

预期结果：BUILD SUCCESS，生成 JAR 文件

- [ ] **Step 3: 提交**

```bash
git add .
git commit -m "feat: 完成拼单优惠券功能后端实现"
```

---

## Task 14: 前端编译测试

**Files:**
- Build: `vue/dist/`

- [ ] **Step 1: 安装依赖**

```bash
cd E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue
npm install
```

预期结果：依赖安装成功，无错误

- [ ] **Step 2: 开发模式测试**

```bash
npm run dev
```

预期结果：开发服务器启动成功，访问 http://localhost:5173

- [ ] **Step 3: 生产构建**

```bash
npm run build
```

预期结果：构建成功，生成 dist 目录

- [ ] **Step 4: 提交**

```bash
git add .
git commit -m "feat: 完成拼单优惠券功能前端实现"
```

---

## 验收检查清单

完成所有任务后，请验证以下功能：

### 后端验证
- [ ] 数据库表 `group_coupons` 和 `group_coupon_usage` 创建成功
- [ ] `group_orders` 表包含 `coupon_id`, `discount_rate`, `original_price` 字段
- [ ] 商家可以创建、查看、编辑、删除优惠券
- [ ] 学生端接口返回可用优惠券列表

### 前端验证
- [ ] 商家端"拼单优惠券"菜单可访问
- [ ] 商家可以创建优惠券（菜品券/店铺券）
- [ ] 菜品详情页发起拼单时显示可用优惠券
- [ ] 拼单列表显示优惠折扣信息
- [ ] 选择优惠券后价格正确计算

### 业务逻辑验证
- [ ] 发起拼单时选择的优惠券被正确记录
- [ ] 优惠券使用次数正确累加
- [ ] 拼单配送费计算正确（平均 + 固定加收）
- [ ] 每人限用次数限制生效
- [ ] 优惠券过期后不可用

---

## 实现完成

完成所有任务后，拼单优惠券功能即可投入使用。商家可以通过创建优惠券来促销拼单，学生可以在发起拼单时选择优惠券享受折扣，同时拼单配送费会适当提高以保证骑手收入。
