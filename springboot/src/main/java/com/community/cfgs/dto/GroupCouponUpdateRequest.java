package com.community.cfgs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupCouponUpdateRequest {
    private String id;
    private String couponType;
    private String dishId;
    private BigDecimal discountRate;
    private Integer minPeople;
    private Integer totalLimit;
    private Integer perUserLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
}
