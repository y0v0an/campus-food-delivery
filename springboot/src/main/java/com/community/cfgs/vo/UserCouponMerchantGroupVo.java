package com.community.cfgs.vo;

import com.community.cfgs.entity.UserCoupon;
import lombok.Data;

import java.util.List;

@Data
public class UserCouponMerchantGroupVo {
    private String merchantId;
    private String merchantName;
    private List<UserCoupon> coupons;
}
