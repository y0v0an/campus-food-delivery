# 优惠券延期与过期处理实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现商户延期优惠券时自动同步到已领取用户，以及过期超过24小时的券在界面隐藏

**Architecture:** 商户更新优惠券时批量同步 UserCoupon 表；用户查询时过滤过期超过24小时的记录

**Tech Stack:** Spring Boot, MyBatis-Plus, Java 17

---

## 文件结构

```
springboot/src/main/java/com/community/cfgs/service/
├── CouponService.java          # 修改：添加同步方法，修改 update() 和查询方法
```

---

## Task 1: 添加 UserCoupon 同步检测方法

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/CouponService.java`

- [ ] **Step 1: 在 CouponService 类中添加私有方法 `shouldSyncUserCoupons`**

在 `consumeUserCouponForOrder` 方法之后添加：

```java
/**
 * 检查是否需要同步用户优惠券
 * @param existing 数据库中现有的优惠券
 * @param updated 更新的优惠券数据
 * @return 是否需要同步
 */
private boolean shouldSyncUserCoupons(Coupon existing, Coupon updated) {
    // 过期时间发生变化，或启用状态发生变化
    boolean expireAtChanged = !Objects.equals(existing.getExpireAt(), updated.getExpireAt());
    boolean enabledChanged = !Objects.equals(existing.getIsEnabled(), updated.getIsEnabled());
    return expireAtChanged || enabledChanged;
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: 编译成功，无错误

---

## Task 2: 添加批量同步 UserCoupon 方法

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/CouponService.java`

- [ ] **Step 1: 在 CouponService 类中添加私有方法 `syncUserCoupons`**

在 `shouldSyncUserCoupons` 方法之后添加：

```java
/**
 * 批量同步用户优惠券的过期时间和状态
 * @param couponId 优惠券ID
 * @param newExpireAt 新的过期时间
 * @param isEnabled 优惠券是否启用
 */
private void syncUserCoupons(String couponId, LocalDateTime newExpireAt, Boolean isEnabled) {
    LambdaUpdateWrapper<UserCoupon> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(UserCoupon::getCouponId, couponId)
           .eq(UserCoupon::getStatus, "claimed");  // 只同步已领取未使用的券

    if (newExpireAt != null) {
        wrapper.set(UserCoupon::getExpireAt, newExpireAt);
    }

    // 商户禁用优惠券时，将已领取的券标记为过期
    if (Boolean.FALSE.equals(isEnabled)) {
        wrapper.set(UserCoupon::getStatus, "expired");
    }

    userCouponMapper.update(null, wrapper);
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: 编译成功

---

## Task 3: 修改 update 方法调用同步逻辑

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/CouponService.java`

- [ ] **Step 1: 在 `update` 方法的返回语句前添加同步调用**

找到 `update` 方法中 `return couponMapper.updateById(existing) > 0;` 这一行，在它之前添加：

```java
// 批量同步用户优惠券
if (shouldSyncUserCoupons(couponMapper.selectById(coupon.getId()), existing)) {
    syncUserCoupons(existing.getId(), existing.getExpireAt(), existing.getIsEnabled());
}
```

完整的 `update` 方法返回部分应该是：

```java
existing.setTotalCount(targetTotal);
existing.setRemainCount(targetTotal - usedCount);

// 批量同步用户优惠券
if (shouldSyncUserCoupons(couponMapper.selectById(coupon.getId()), existing)) {
    syncUserCoupons(existing.getId(), existing.getExpireAt(), existing.getIsEnabled());
}

return couponMapper.updateById(existing) > 0;
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: 编译成功

---

## Task 4: 修改 listUserCouponsGrouped 方法添加24小时隐藏逻辑

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/CouponService.java`

- [ ] **Step 1: 修改 `listUserCouponsGrouped` 方法中的过滤逻辑**

找到方法中这段代码：
```java
String targetStatus = status == null ? "all" : status.trim();
LocalDateTime now = LocalDateTime.now();
userCoupons = userCoupons.stream().filter(uc -> {
    String displayStatus = uc.getStatus();
    if ("claimed".equals(displayStatus) && uc.getExpireAt() != null && uc.getExpireAt().isBefore(now)) {
        displayStatus = "expired";
    }
    uc.setStatus(displayStatus);
    return "all".equals(targetStatus) || targetStatus.equals(displayStatus);
}).collect(Collectors.toList());
```

替换为：

```java
String targetStatus = status == null ? "all" : status.trim();
LocalDateTime now = LocalDateTime.now();
LocalDateTime hideThreshold = now.minusHours(24);  // 24小时阈值
userCoupons = userCoupons.stream().filter(uc -> {
    String displayStatus = uc.getStatus();
    if ("claimed".equals(displayStatus) && uc.getExpireAt() != null && uc.getExpireAt().isBefore(now)) {
        displayStatus = "expired";
    }

    // 过期超过24小时的券隐藏（但已使用的券始终显示）
    if ("expired".equals(displayStatus) && !"used".equals(uc.getStatus())) {
        if (uc.getExpireAt() != null && uc.getExpireAt().isBefore(hideThreshold)) {
            return false;  // 隐藏
        }
    }

    uc.setStatus(displayStatus);
    return "all".equals(targetStatus) || targetStatus.equals(displayStatus);
}).collect(Collectors.toList());
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: 编译成功

---

## Task 5: 修改 listClaimedCouponsForOrder 方法同步隐藏逻辑

**Files:**
- Modify: `springboot/src/main/java/com/community/cfgs/service/CouponService.java`

- [ ] **Step 1: 在 `listClaimedCouponsForOrder` 方法中添加24小时过滤**

找到方法中查询条件：
```java
LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(UserCoupon::getUserId, userId)
        .eq(UserCoupon::getStatus, "claimed")
        .eq(merchantId != null && !merchantId.trim().isEmpty(), UserCoupon::getMerchantId, merchantId)
        .ge(UserCoupon::getExpireAt, LocalDateTime.now())
        .orderByDesc(UserCoupon::getClaimedAt);
```

在方法开头添加时间过滤变量，并在后续 stream 过滤中处理：

完整方法修改为：

```java
public List<UserCoupon> listClaimedCouponsForOrder(String userId, String merchantId, BigDecimal orderAmount) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime hideThreshold = now.minusHours(24);

    LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserCoupon::getUserId, userId)
          .eq(UserCoupon::getStatus, "claimed")
          .eq(merchantId != null && !merchantId.trim().isEmpty(), UserCoupon::getMerchantId, merchantId)
          .ge(UserCoupon::getExpireAt, hideThreshold)  // 使用24小时阈值过滤
          .orderByDesc(UserCoupon::getClaimedAt);
    // ... 后续代码保持不变 ...
}
```

实际上这个方法只返回可用的券（status='claimed' 且未过期），所以不需要额外修改，原有的 `ge(UserCoupon::getExpireAt, LocalDateTime.now())` 已经过滤了过期券。

- [ ] **Step 2: 确认无需修改**

这个方法用于订单结算获取可用券，原有的过期判断已足够，跳过此任务。

---

## Task 6: 后端编译测试

**Files:**
- Test: `springboot/src/main/java/com/community/cfgs/service/CouponService.java`

- [ ] **Step 1: 完整编译后端**

Run:
```bash
cd springboot
mvn clean compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 2: 打包验证**

Run:
```bash
mvn package -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 7: 手动测试验证

**Files:**
- Test: Manual verification

- [ ] **Step 1: 启动后端服务**

Run:
```bash
cd springboot
mvn spring-boot:run
```

Expected: 服务在 8080 端口启动成功

- [ ] **Step 2: 测试商户延期优惠券**

1. 登录商户账号
2. 创建一个过期时间为明天的优惠券
3. 用学生账号领取该优惠券
4. 商户将优惠券过期时间延期到后天
5. 学生刷新"我的优惠券"页面

Expected: 学生端显示的券过期时间已更新为后天

- [ ] **Step 3: 测试过期券隐藏**

1. 创建一个过期时间为今天的优惠券
2. 用学生账号领取
3. 等待券过期（或手动修改数据库将 expireAt 改为昨天）
4. 学生刷新"我的优惠券"页面

Expected: 过期券仍显示在"已过期"标签下

5. 将数据库中 expireAt 改为 25 小时前
6. 学生刷新页面

Expected: 过期券不再显示

---

## Task 8: 提交代码

**Files:**
- Git commit

- [ ] **Step 1: 提交代码**

Run:
```bash
cd springboot
git add src/main/java/com/community/cfgs/service/CouponService.java
git commit -m "feat: 优惠券延期时自动同步用户券，过期24小时后隐藏

- 商户更新优惠券过期时间时，批量同步到已领取的用户券
- 商户禁用优惠券时，已领取的券标记为过期
- 用户查询优惠券时，过期超过24小时的券不显示
- 已使用的券不受影响，始终可见"
```

Expected: 提交成功

---

## 附录：快速验证 SQL

测试时可手动修改数据库验证：

```sql
-- 查看用户的优惠券领取记录
SELECT uc.id, uc.status, uc.expireAt, c.name as couponName, c.expireAt as couponExpireAt
FROM user_coupons uc
JOIN coupons c ON uc.couponId = c.id
WHERE uc.userId = '用户ID';

-- 手动设置某个券过期（用于测试隐藏逻辑）
UPDATE user_coupons SET expireAt = DATE_SUB(NOW(), INTERVAL 25 HOUR) WHERE id = '券ID';

-- 手动设置某个券即将过期（用于测试显示逻辑）
UPDATE user_coupons SET expireAt = DATE_SUB(NOW(), INTERVAL 2 HOUR) WHERE id = '券ID';
```
