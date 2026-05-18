package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("addresses")
public class Address {
    @TableId
    private String id;
    private String userId;
    private String building;
    private String room;
    private String contact;
    private String phone;
    @TableField("is_default")
    private Boolean isDefault;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime createdAt;
}
