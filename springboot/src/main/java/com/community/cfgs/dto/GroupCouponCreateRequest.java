package com.community.cfgs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupCouponCreateRequest {
    private String couponType;      // dish/shop/platform
    private String dishId;          // 菜品券必填
    private BigDecimal discountRate;    // 折扣率（如0.80）
    private Integer minPeople;      // 最低拼单人数
    private Integer totalLimit;     // 总限量
    private Integer perUserLimit;   // 每人限用次数
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}
