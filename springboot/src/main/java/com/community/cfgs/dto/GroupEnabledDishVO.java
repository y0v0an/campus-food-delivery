package com.community.cfgs.dto;

import com.community.cfgs.entity.Dish;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GroupEnabledDishVO {
    private String id;
    private String name;
    private String image;
    private String description;
    private BigDecimal groupPrice;
    private BigDecimal originalPrice;
    private BigDecimal discountRate;
    private Integer groupTargetCount;
    private Integer groupDurationMinutes;
    private Integer sales;

    // 商家信息
    private MerchantInfo merchant;

    @Data
    public static class MerchantInfo {
        private String id;
        private String name;
        private String logo;
        private BigDecimal rating;
        private String deliveryTime;
        private BigDecimal deliveryFee;
        private Integer distance;
        private String area;
        private Boolean isOpen;
    }

    /**
     * 从 Dish 实体转换为 VO
     */
    public static GroupEnabledDishVO from(Dish dish, MerchantInfo merchantInfo) {
        GroupEnabledDishVO vo = new GroupEnabledDishVO();
        vo.setId(dish.getId());
        vo.setName(dish.getName());
        vo.setImage(dish.getImage());
        vo.setDescription(dish.getCategory());
        vo.setGroupPrice(dish.getGroupPrice());
        vo.setOriginalPrice(dish.getOriginalPrice() != null ? dish.getOriginalPrice() : dish.getPrice());
        vo.setDiscountRate(calculateDiscountRate(vo.getOriginalPrice(), dish.getGroupPrice()));
        vo.setGroupTargetCount(dish.getGroupTargetCount());
        vo.setGroupDurationMinutes(dish.getGroupDurationMinutes());
        vo.setSales(dish.getSales());
        vo.setMerchant(merchantInfo);
        return vo;
    }

    private static BigDecimal calculateDiscountRate(BigDecimal original, BigDecimal groupPrice) {
        if (original == null || groupPrice == null || original.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return groupPrice.divide(original, 2, BigDecimal.ROUND_HALF_UP);
    }
}
