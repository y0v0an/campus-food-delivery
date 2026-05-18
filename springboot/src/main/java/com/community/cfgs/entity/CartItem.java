package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cart_items")
public class CartItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String userId;
    private String merchantId;
    private String dishId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Dish dish;
}
