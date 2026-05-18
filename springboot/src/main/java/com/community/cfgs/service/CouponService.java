package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.Coupon;
import com.community.cfgs.entity.Merchant;
import com.community.cfgs.entity.UserCoupon;
import com.community.cfgs.mapper.CouponMapper;
import com.community.cfgs.mapper.MerchantMapper;
import com.community.cfgs.mapper.UserCouponMapper;
import com.community.cfgs.vo.UserCouponMerchantGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private MerchantMapper merchantMapper;

    public List<Coupon> listByMerchantId(String merchantId) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getMerchantId, merchantId).orderByDesc(Coupon::getCreatedAt);
        return couponMapper.selectList(wrapper);
    }

    public List<Coupon> listAvailableForOrder(String merchantId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getMerchantId, merchantId)
                .eq(Coupon::getIsEnabled, true)
                .gt(Coupon::getRemainCount, 0)
                .ge(Coupon::getExpireAt, now)
                .le(Coupon::getThresholdAmount, orderAmount)
                .orderByDesc(Coupon::getDiscountAmount);
        return couponMapper.selectList(wrapper);
    }

    public List<Coupon> listHotAvailable(int limit) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getIsEnabled, true)
                .gt(Coupon::getRemainCount, 0)
                .ge(Coupon::getExpireAt, LocalDateTime.now())
                .orderByDesc(Coupon::getDiscountAmount)
                .orderByDesc(Coupon::getCreatedAt)
                .last("LIMIT " + Math.max(1, limit));
        return couponMapper.selectList(wrapper);
    }

    public Coupon getById(String couponId) {
        return couponMapper.selectById(couponId);
    }

    public boolean add(Coupon coupon) {
        if (coupon.getMerchantId() == null || coupon.getMerchantId().trim().isEmpty()) {
            return false;
        }
        coupon.setId("cp_" + UUID.randomUUID().toString().substring(0, 8));
        coupon.setCreatedAt(LocalDateTime.now());
        if (coupon.getIsEnabled() == null) {
            coupon.setIsEnabled(true);
        }
        if (coupon.getTotalCount() == null || coupon.getTotalCount() < 1) {
            coupon.setTotalCount(1);
        }
        if (coupon.getRemainCount() == null || coupon.getRemainCount() < 0) {
            coupon.setRemainCount(coupon.getTotalCount());
        }
        if (coupon.getClaimLimitPerUser() == null || coupon.getClaimLimitPerUser() < 1) {
            coupon.setClaimLimitPerUser(1);
        }
        return couponMapper.insert(coupon) > 0;
    }

    public boolean deleteById(String id, String merchantId) {
        Coupon existing = couponMapper.selectById(id);
        if (existing == null || !merchantId.equals(existing.getMerchantId())) {
            return false;
        }
        return couponMapper.deleteById(id) > 0;
    }

    public boolean update(Coupon coupon, String merchantId) {
        if (coupon == null || coupon.getId() == null || coupon.getId().trim().isEmpty()) {
            return false;
        }
        Coupon existing = couponMapper.selectById(coupon.getId());
        if (existing == null || !merchantId.equals(existing.getMerchantId())) {
            return false;
        }
        // 保存原始数据的副本
        Coupon original = new Coupon();
        original.setExpireAt(existing.getExpireAt());
        original.setIsEnabled(existing.getIsEnabled());

        existing.setName(coupon.getName());
        existing.setThresholdAmount(coupon.getThresholdAmount());
        existing.setDiscountAmount(coupon.getDiscountAmount());
        existing.setExpireAt(coupon.getExpireAt());
        existing.setIsEnabled(coupon.getIsEnabled());
        existing.setClaimLimitPerUser(coupon.getClaimLimitPerUser() == null || coupon.getClaimLimitPerUser() < 1 ? 1 : coupon.getClaimLimitPerUser());

        int usedCount = Math.max(0, (existing.getTotalCount() == null ? 0 : existing.getTotalCount())
                - (existing.getRemainCount() == null ? 0 : existing.getRemainCount()));
        int targetTotal = coupon.getTotalCount() == null ? existing.getTotalCount() : coupon.getTotalCount();
        if (targetTotal < usedCount) {
            return false;
        }
        existing.setTotalCount(targetTotal);
        existing.setRemainCount(targetTotal - usedCount);

        // 批量同步用户优惠券
        if (shouldSyncUserCoupons(original, existing)) {
            syncUserCoupons(existing.getId(), existing.getExpireAt(), existing.getIsEnabled());
        }

        return couponMapper.updateById(existing) > 0;
    }

    public BigDecimal calculateDiscountForOrder(Coupon coupon, BigDecimal orderAmount) {
        if (coupon == null || orderAmount == null) {
            return BigDecimal.ZERO;
        }
        if (!Boolean.TRUE.equals(coupon.getIsEnabled())) {
            return BigDecimal.ZERO;
        }
        if (coupon.getExpireAt() == null || coupon.getExpireAt().isBefore(LocalDateTime.now())) {
            return BigDecimal.ZERO;
        }
        if (coupon.getThresholdAmount() != null && orderAmount.compareTo(coupon.getThresholdAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        if (discount.compareTo(orderAmount) > 0) {
            return orderAmount;
        }
        return discount.max(BigDecimal.ZERO);
    }

    public boolean consumeCoupon(String couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getRemainCount() == null || coupon.getRemainCount() <= 0) {
            return false;
        }
        coupon.setRemainCount(coupon.getRemainCount() - 1);
        return couponMapper.updateById(coupon) > 0;
    }

    /**
     * 领取优惠券（使用事务保证原子性，使用乐观锁防止并发问题）
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 是否领取成功
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public boolean claimCoupon(String userId, String couponId) {
        // 1. 检查用户是否已领取过（防止重复领取）
        int limit = couponMapper.selectById(couponId) != null ?
                (couponMapper.selectById(couponId).getClaimLimitPerUser() == null ||
                 couponMapper.selectById(couponId).getClaimLimitPerUser() < 1 ? 1 :
                 couponMapper.selectById(couponId).getClaimLimitPerUser()) : 1;

        LambdaQueryWrapper<UserCoupon> claimCountWrapper = new LambdaQueryWrapper<>();
        claimCountWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);
        Long claimedCount = userCouponMapper.selectCount(claimCountWrapper);
        if (claimedCount != null && claimedCount >= limit) {
            throw new RuntimeException("已达到该券限领数量");
        }

        // 2. 使用乐观锁更新优惠券库存（CAS方式）
        // 先获取当前优惠券信息
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !Boolean.TRUE.equals(coupon.getIsEnabled())) {
            return false;
        }
        if (coupon.getExpireAt() == null || coupon.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("优惠券已过期");
        }
        if (coupon.getRemainCount() == null || coupon.getRemainCount() <= 0) {
            throw new RuntimeException("优惠券已被抢完");
        }

        // 使用乐观锁更新：只有当remainCount等于当前值时才更新成功
        LambdaUpdateWrapper<Coupon> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Coupon::getId, couponId)
                .eq(Coupon::getRemainCount, coupon.getRemainCount())  // 乐观锁条件
                .set(Coupon::getRemainCount, coupon.getRemainCount() - 1);

        if (couponMapper.update(null, updateWrapper) <= 0) {
            // 更新失败说明库存已被其他线程修改，需要重试或提示用户
            throw new RuntimeException("优惠券领取失败，请重试");
        }

        // 3. 插入用户领取记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setId("uc_" + UUID.randomUUID().toString().substring(0, 8));
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(coupon.getId());
        userCoupon.setMerchantId(coupon.getMerchantId());
        userCoupon.setStatus("claimed");
        userCoupon.setClaimedAt(LocalDateTime.now());
        userCoupon.setExpireAt(coupon.getExpireAt());

        boolean insertResult = userCouponMapper.insert(userCoupon) > 0;
        if (!insertResult) {
            // 如果插入失败，回滚库存
            throw new RuntimeException("优惠券领取记录创建失败");
        }

        return true;
    }

    public List<UserCouponMerchantGroupVo> listUserCouponsGrouped(String userId, String status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId).orderByDesc(UserCoupon::getClaimedAt);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
        if (userCoupons.isEmpty()) {
            return java.util.Collections.emptyList();
        }
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
        if (userCoupons.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<String> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<Coupon> cw = new LambdaQueryWrapper<>();
        cw.in(Coupon::getId, couponIds);
        List<Coupon> coupons = couponMapper.selectList(cw);
        Map<String, Coupon> couponMap = coupons.stream().collect(Collectors.toMap(Coupon::getId, c -> c, (a, b) -> a));

        List<String> merchantIds = userCoupons.stream().map(UserCoupon::getMerchantId).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<Merchant> mw = new LambdaQueryWrapper<>();
        mw.in(Merchant::getId, merchantIds);
        List<Merchant> merchants = merchantMapper.selectList(mw);
        Map<String, String> merchantNameMap = merchants.stream().collect(Collectors.toMap(Merchant::getId, Merchant::getName, (a, b) -> a));

        Map<String, List<UserCoupon>> byMerchant = new LinkedHashMap<>();
        for (UserCoupon uc : userCoupons) {
            uc.setCoupon(couponMap.get(uc.getCouponId()));
            byMerchant.computeIfAbsent(uc.getMerchantId(), k -> new java.util.ArrayList<>()).add(uc);
        }
        List<UserCouponMerchantGroupVo> result = new java.util.ArrayList<>();
        byMerchant.forEach((merchantId, list) -> {
            UserCouponMerchantGroupVo vo = new UserCouponMerchantGroupVo();
            vo.setMerchantId(merchantId);
            vo.setMerchantName(merchantNameMap.getOrDefault(merchantId, merchantId));
            vo.setCoupons(list);
            result.add(vo);
        });
        return result;
    }

    public List<UserCoupon> listClaimedCouponsForOrder(String userId, String merchantId, BigDecimal orderAmount) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "claimed")
                .eq(merchantId != null && !merchantId.trim().isEmpty(), UserCoupon::getMerchantId, merchantId)
                .ge(UserCoupon::getExpireAt, LocalDateTime.now())
                .orderByDesc(UserCoupon::getClaimedAt);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
        if (userCoupons.isEmpty()) {
            return userCoupons;
        }
        List<String> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        LambdaQueryWrapper<Coupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.in(Coupon::getId, couponIds);
        List<Coupon> coupons = couponMapper.selectList(couponWrapper);
        Map<String, Coupon> couponMap = new HashMap<>();
        for (Coupon coupon : coupons) {
            couponMap.put(coupon.getId(), coupon);
        }
        return userCoupons.stream().filter(uc -> {
            Coupon coupon = couponMap.get(uc.getCouponId());
            if (coupon == null || !Boolean.TRUE.equals(coupon.getIsEnabled())) {
                return false;
            }
            if (coupon.getExpireAt() == null || coupon.getExpireAt().isBefore(LocalDateTime.now())) {
                return false;
            }
            if (orderAmount == null) {
                return true;
            }
            return coupon.getThresholdAmount() == null || orderAmount.compareTo(coupon.getThresholdAmount()) >= 0;
        }).peek(uc -> uc.setCoupon(couponMap.get(uc.getCouponId()))).collect(Collectors.toList());
    }

    /**
     * 核销用户优惠券（订单创建时调用，需在事务内执行）
     * @param userCouponId 用户优惠券ID
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @param orderAmount 订单金额
     * @return 优惠券使用结果
     */
    public CouponUseResult consumeUserCouponForOrder(String userCouponId, String userId, String merchantId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("优惠券领取记录不存在");
        }
        if (!Objects.equals(userId, userCoupon.getUserId())) {
            throw new RuntimeException("无权使用该优惠券");
        }
        if (!Objects.equals(merchantId, userCoupon.getMerchantId())) {
            throw new RuntimeException("优惠券不属于当前商家");
        }
        if (!"claimed".equals(userCoupon.getStatus())) {
            throw new RuntimeException("优惠券已使用或失效");
        }
        if (userCoupon.getExpireAt() == null || userCoupon.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("优惠券已过期");
        }
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null || !Boolean.TRUE.equals(coupon.getIsEnabled())) {
            throw new RuntimeException("优惠券不可用");
        }
        BigDecimal discount = calculateDiscountForOrder(coupon, orderAmount);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("优惠券不满足使用条件");
        }

        // 使用乐观锁更新状态，防止并发使用
        LambdaUpdateWrapper<UserCoupon> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserCoupon::getId, userCouponId)
                .eq(UserCoupon::getStatus, "claimed")  // 乐观锁条件
                .set(UserCoupon::getStatus, "used")
                .set(UserCoupon::getUsedAt, LocalDateTime.now());

        if (userCouponMapper.update(null, updateWrapper) <= 0) {
            throw new RuntimeException("优惠券核销失败，可能已被使用");
        }

        CouponUseResult result = new CouponUseResult();
        result.setCoupon(coupon);
        result.setDiscount(discount);
        return result;
    }

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

    @lombok.Data
    public static class CouponUseResult {
        private Coupon coupon;
        private BigDecimal discount;
    }
}
