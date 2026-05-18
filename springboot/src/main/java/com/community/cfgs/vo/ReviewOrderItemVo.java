package com.community.cfgs.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 我的评价列表中展示的订单菜品（来自 order_items） */
@Data
public class ReviewOrderItemVo {
    private String dishId;
    private String dishName;
    private String dishImage;
    private Integer quantity;
    private BigDecimal price;
}
