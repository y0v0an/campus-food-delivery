package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("group_orders")
public class GroupOrder {
    @TableId
    private String id;
    private String merchantId;
    private String merchantName;
    private String dishId;
    private String dishName;
    private String dishImage;
    private String initiatorId;
    private String initiatorName;
    private Integer targetCount;
    private Integer currentCount;
    private BigDecimal unitPrice;
    /** open/full/accepted/cancelled */
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime cancelledAt;
    /** 取消原因：timeout/expired/manual/initiator_cancel/merchant_cancel */
    private String cancelReason;
    /** 取消人ID */
    private String cancelledBy;
    /** 使用的优惠券ID */
    private String couponId;
    /** 实际折扣率 */
    private BigDecimal discountRate;
    /** 菜品原价 */
    private BigDecimal originalPrice;

    /** 商家配送费（非数据库字段，用于前端显示） */
    @TableField(exist = false)
    private BigDecimal merchantDeliveryFee;

    @TableField(exist = false)
    private List<GroupOrderMember> members;
}
