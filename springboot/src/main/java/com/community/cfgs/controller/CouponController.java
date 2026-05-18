package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.Coupon;
import com.community.cfgs.entity.UserCoupon;
import com.community.cfgs.service.CouponService;
import com.community.cfgs.vo.UserCouponMerchantGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/merchant/{merchantId}")
    public Result<List<Coupon>> listByMerchant(@PathVariable String merchantId) {
        return Result.success(couponService.listByMerchantId(merchantId));
    }

    @GetMapping("/available")
    public Result<List<Coupon>> listAvailable(
            @RequestParam String merchantId,
            @RequestParam BigDecimal orderAmount) {
        return Result.success(couponService.listAvailableForOrder(merchantId, orderAmount));
    }

    @GetMapping("/hot")
    public Result<List<Coupon>> listHot(@RequestParam(required = false, defaultValue = "8") Integer limit) {
        return Result.success(couponService.listHotAvailable(limit == null ? 8 : limit));
    }

    @PostMapping("/claim")
    public Result<String> claim(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String couponId = params.get("couponId");
        try {
            boolean ok = couponService.claimCoupon(userId, couponId);
            return ok ? Result.success("领取成功") : Result.error("领取失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<UserCoupon>> listUserCoupons(
            @PathVariable String userId,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) BigDecimal orderAmount) {
        return Result.success(couponService.listClaimedCouponsForOrder(userId, merchantId, orderAmount));
    }

    @GetMapping("/user/grouped/{userId}")
    public Result<List<UserCouponMerchantGroupVo>> listUserCouponsGrouped(
            @PathVariable String userId,
            @RequestParam(required = false, defaultValue = "all") String status) {
        return Result.success(couponService.listUserCouponsGrouped(userId, status));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Coupon coupon) {
        boolean ok = couponService.add(coupon);
        return ok ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result<String> update(
            @RequestBody Coupon coupon,
            @RequestParam String merchantId) {
        boolean ok = couponService.update(coupon, merchantId);
        return ok ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable String id,
            @RequestParam String merchantId) {
        boolean ok = couponService.deleteById(id, merchantId);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }
}
