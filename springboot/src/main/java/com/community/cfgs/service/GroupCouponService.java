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

    /**
     * 创建拼单优惠券
     */
    @Transactional(rollbackFor = Exception.class)
    public GroupCoupon createCoupon(String merchantId, GroupCouponCreateRequest request) {
        // 验证
        if ("dish".equals(request.getCouponType()) && request.getDishId() == null) {
            throw new RuntimeException("菜品券必须指定菜品");
        }
        if (request.getDiscountRate().compareTo(BigDecimal.ZERO) <= 0 || request.getDiscountRate().compareTo(BigDecimal.ONE) > 0) {
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
        coupon.setDiscountRate(request.getDiscountRate());
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
            coupon.setDiscountRate(request.getDiscountRate());
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
     * 计算拼单配送费（每人平摊）
     * 规则：总配送费 = 商家配送费 + 拼单人数，每人平摊 = 总配送费 / 人数
     * 例如：商家配送费3元，4人拼单 → 总配送费3+4=7元 → 每人1.75元
     * @param individualDeliveryFees 每个成员的配送费列表（通常都相同，为商家配送费）
     * @return 每人应支付的配送费（已平摊）
     */
    public BigDecimal calculateGroupDeliveryFee(List<BigDecimal> individualDeliveryFees) {
        if (individualDeliveryFees == null || individualDeliveryFees.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 获取商家配送费（取第一个即可，通常都相同）
        BigDecimal merchantDeliveryFee = individualDeliveryFees.get(0);
        int peopleCount = individualDeliveryFees.size();

        // 总配送费 = 商家配送费 + 拼单人数
        BigDecimal totalDeliveryFee = merchantDeliveryFee.add(BigDecimal.valueOf(peopleCount));

        // 每人平摊配送费 = 总配送费 / 人数
        return totalDeliveryFee.divide(
                BigDecimal.valueOf(peopleCount),
                2,
                BigDecimal.ROUND_HALF_UP
        );
    }

    /**
     * 计算拼单骑手实际获得的配送费
     * 规则：骑手获得 = 商家配送费 + 拼单人数
     * @param merchantDeliveryFee 商家配送费
     * @param peopleCount 拼单人数
     * @return 骑手获得的配送费
     */
    public BigDecimal calculateRiderDeliveryFee(BigDecimal merchantDeliveryFee, int peopleCount) {
        return merchantDeliveryFee.add(BigDecimal.valueOf(peopleCount));
    }
}
