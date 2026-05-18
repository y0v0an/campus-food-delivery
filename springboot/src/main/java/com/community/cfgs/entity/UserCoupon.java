package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupons")
public class UserCoupon {
    @TableId
    private String id;
    private String userId;
    private String couponId;
    private String merchantId;
    /** claimed=已领取 used=已使用 expired=已过期 */
    private String status;
    private LocalDateTime claimedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expireAt;

    @TableField(exist = false)
    private Coupon coupon;
}
