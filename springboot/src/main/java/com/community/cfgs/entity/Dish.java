package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dishes")
public class Dish {
    @TableId
    private String id;
    private String merchantId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private String category;
    private Integer stock;
    private Integer sales;
    @TableField("is_available")
    private Boolean isAvailable;
    @TableField("is_group_enabled")
    private Boolean isGroupEnabled;
    private Integer groupTargetCount;
    private BigDecimal groupPrice;
    private Integer groupDurationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
