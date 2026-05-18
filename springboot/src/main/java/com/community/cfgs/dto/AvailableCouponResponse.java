package com.community.cfgs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvailableCouponResponse {
    private CouponInfo bestCoupon;
    private List<CouponInfo> availableCoupons;
    private PriceCalculation priceCalculation;

    @Data
    public static class CouponInfo {
        private String id;
        private String couponType;
        private BigDecimal discountRate;
        private Integer minPeople;
        private LocalDateTime validUntil;
        private Integer remainingCount;
    }

    @Data
    public static class PriceCalculation {
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
        private BigDecimal savedAmount;
    }
}
