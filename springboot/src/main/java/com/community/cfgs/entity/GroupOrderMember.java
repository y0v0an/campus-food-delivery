package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("group_order_members")
public class GroupOrderMember {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String groupOrderId;
    private String userId;
    private String userName;
    private Integer quantity;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String addressBuilding;
    private String addressRoom;
    private String addressContact;
    private String addressPhone;
}
