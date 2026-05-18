package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderId;
    private String dishId;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
