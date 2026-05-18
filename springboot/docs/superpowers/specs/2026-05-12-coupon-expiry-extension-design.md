# 优惠券延期与过期处理设计文档

**日期**: 2026-05-12
**作者**: Claude
**状态**: 待审查

## 1. 问题背景

当前系统存在以下问题：

1. **商户延期优惠券不生效**：商户将优惠券过期时间从 5月12日 延期到 5月13日后，已领取的用户端仍显示过期
2. **用户无法重新领取**：延期后用户不能重新领取更新后的券
3. **过期券一直显示**：已过期很久的券仍显示在用户界面上，影响体验

## 2. 需求定义

| 场景 | 期望行为 |
|------|---------|
| 商户延期优惠券 | 已领取用户自动获得延期 |
| 券过期 > 24小时 | 界面隐藏，数据保留用于分析 |
| 用户重新领取 | 不允许（已自动延期，避免重复） |

## 3. 技术方案

### 3.1 商户更新时批量同步 UserCoupon

**修改位置**: `CouponService.update()` 方法

**同步规则**:

| Coupon 字段变更 | UserCoupon 同步动作 |
|----------------|-------------------|
| `expireAt` 延期 | 更新所有 `status='claimed'` 的 `expireAt` |
| `isEnabled=false` | 将 `status='claimed'` 改为 `status='expired'` |
| `isEnabled=true` | 不恢复已过期券（保持原状态） |

**实现逻辑**:
```java
public boolean update(Coupon coupon, String merchantId) {
    // ... 原有验证和更新逻辑 ...

    // 新增：批量同步 UserCoupon
    if (shouldSyncUserCoupons(existing, coupon)) {
        syncUserCoupons(existing.getId(), existing.getExpireAt(), existing.getIsEnabled());
    }

    return couponMapper.updateById(existing) > 0;
}

private boolean shouldSyncUserCoupons(Coupon existing, Coupon updated) {
    // 过期时间发生变化，或启用状态发生变化
    return !Objects.equals(existing.getExpireAt(), updated.getExpireAt()) ||
           !Objects.equals(existing.getIsEnabled(), updated.getIsEnabled());
}

private void syncUserCoupons(String couponId, LocalDateTime newExpireAt, Boolean isEnabled) {
    LambdaUpdateWrapper<UserCoupon> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(UserCoupon::getCouponId, couponId)
           .eq(UserCoupon::getStatus, "claimed");

    if (newExpireAt != null) {
        wrapper.set(UserCoupon::getExpireAt, newExpireAt);
    }

    if (Boolean.FALSE.equals(isEnabled)) {
        wrapper.set(UserCoupon::getStatus, "expired");
    }

    userCouponMapper.update(null, wrapper);
}
```

### 3.2 过期券隐藏（查询时过滤）

**修改位置**: `CouponService.listUserCouponsGrouped()` 方法

**过滤规则**:
- 过期未满 24 小时 → 显示，状态 = `expired`
- 过期超过 24 小时 → 不返回（隐藏）
- 已使用券 → 正常显示

**实现逻辑**:
```java
public List<UserCouponMerchantGroupVo> listUserCouponsGrouped(String userId, String status) {
    // ... 原有查询逻辑 ...

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime hideThreshold = now.minusHours(24);
    String targetStatus = status == null ? "all" : status.trim();

    userCoupons = userCoupons.stream()
        .filter(uc -> {
            String displayStatus = uc.getStatus();

            // 判断是否过期
            if ("claimed".equals(displayStatus) && uc.getExpireAt() != null && uc.getExpireAt().isBefore(now)) {
                displayStatus = "expired";
            }

            // 过期超过24小时的券隐藏
            if ("expired".equals(displayStatus) && uc.getExpireAt().isBefore(hideThreshold)) {
                return false;
            }

            uc.setStatus(displayStatus);
            return "all".equals(targetStatus) || targetStatus.equals(displayStatus);
        })
        .collect(Collectors.toList());

    // ... 后续分组逻辑 ...
}
```

同样修改 `listClaimedCouponsForOrder()` 方法，确保结算时可用券查询也隐藏过期券。

### 3.3 不允许重复领取

**现有逻辑**: `CouponService.claimCoupon()` 已有限制逻辑，检查用户已领取数量是否超过 `claimLimitPerUser`。

**结论**: 无需修改，现有逻辑已满足需求。

## 4. 数据流图

```
┌──────────────────────────────────────────────────────────────────┐
│                      商户端更新优惠券                              │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  CouponService.update()                                          │
│  1. 验证权限                                                      │
│  2. 更新 Coupon 表                                               │
│  3. 检测是否需要同步 (expireAt/isEnabled 变更)                   │
│  4. 批量更新 UserCoupon 表 (仅 status='claimed')                 │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                      用户端查看优惠券                              │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  CouponService.listUserCouponsGrouped()                          │
│  1. 查询 user_coupons 表                                         │
│  2. 过期判断 (claimed + expireAt < now → expired)               │
│  3. 隐藏过滤 (expired + expireAt < now - 24h → 过滤掉)           │
│  4. 按商户分组返回                                               │
└──────────────────────────────────────────────────────────────────┘
```

## 5. 边界情况处理

| 场景 | 处理方式 |
|------|---------|
| 商户缩短过期时间 | 同步更新，用户券提前过期 |
| 商户禁用优惠券 | 已领取的 claimed 券标记为 expired |
| 商户重新启用优惠券 | 不恢复已过期的券（保持安全） |
| 已使用/已过期券 | 不同步过期时间（只同步 claimed 状态） |
| 大量用户领取（100人） | 同步批量更新，性能影响可忽略 |

## 6. 改动文件清单

| 文件 | 改动类型 | 说明 |
|------|---------|------|
| `CouponService.java` | 修改 | 添加 `shouldSyncUserCoupons()` 和 `syncUserCoupons()` 方法，修改 `update()` 方法 |
| `CouponService.java` | 修改 | 修改 `listUserCouponsGrouped()` 添加24小时隐藏逻辑 |
| `CouponService.java` | 修改 | 修改 `listClaimedCouponsForOrder()` 同步隐藏逻辑 |

## 7. 测试要点

| 测试场景 | 预期结果 |
|---------|---------|
| 商户延期优惠券后，用户刷新我的优惠券 | 已领取券的过期时间同步更新 |
| 商户禁用优惠券后，用户刷新 | 已领取券状态变为 expired |
| 过期 2 小时的券 | 在"已过期"标签下可见 |
| 过期 25 小时的券 | 在任何标签下都不可见 |
| 已使用的券 | 无论过期多久都可见（状态为 used） |
