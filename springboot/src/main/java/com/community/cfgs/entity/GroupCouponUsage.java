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
