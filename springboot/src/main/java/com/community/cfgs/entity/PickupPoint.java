package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pickup_points")
public class PickupPoint {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String address;
    private Boolean isActive;
}
