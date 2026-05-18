package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupons")
public class Coupon {
    @TableId
    private String id;
    private String merchantId;
    private String name;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private LocalDateTime expireAt;
    private Integer totalCount;
    private Integer remainCount;
    private Integer claimLimitPerUser;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
}
