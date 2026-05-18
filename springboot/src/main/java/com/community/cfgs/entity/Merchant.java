package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchants")
public class Merchant {
    @TableId
    private String id;
    private String userId;
    private String name;
    private String logo;
    private String description;
    private String address;
    private String phone;
    private BigDecimal rating;
    private Integer monthSales;
    private String categories;  // JSON数组
    @TableField("is_open")
    private Boolean isOpen;
    private BigDecimal deliveryFee;
    private BigDecimal minOrder;
    private String deliveryTime;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
