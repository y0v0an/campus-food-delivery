package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId
    private String id;
    private String name;
    private BigDecimal price;
    private Integer minPurchase;
    private Integer stock;
    private String deliveryTime;
    private String status;
    private String image;
    private String category;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
