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
