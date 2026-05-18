package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("group_coupons")
public class GroupCoupon {
    @TableId
    private String id;

    private String merchantId;
    private String couponType;      // dish/shop/platform

    private String dishId;          // 仅 dish 类型需要

    private BigDecimal discountRate;
    private Integer minPeople;

    private Integer totalLimit;
    private Integer usedCount;
    private Integer perUserLimit;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private String status;          // active/paused/expired
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String dishName;        // 关联查询用

    @TableField(exist = false)
    private Integer remainingCount; // 剩余数量
}
