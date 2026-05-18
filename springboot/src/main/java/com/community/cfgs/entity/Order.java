package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("orders")
public class Order {
    @TableId
    private String id;
    private String orderNo;
    private String studentId;
    private String merchantId;
    private String merchantName;
    private String riderId;
    private String riderName;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal packingFee;
    private String couponId;
    private String couponName;
    private BigDecimal couponDiscount;
    private BigDecimal actualAmount;
    private String status;  // pending_payment, pending_accept, preparing, ready, delivering, completed, cancelled
    private String addressBuilding;
    private String addressRoom;
    private String addressContact;
    private String addressPhone;
    private String remark;
    /** delivery=外卖配送 dine_in=店内就餐（无骑手） */
    private String orderType;

    /** 拼单关联字段：所属拼单ID */
    private String groupOrderId;

    /** 拼单关联字段：在拼单中的序号（从1开始） */
    private Integer groupOrderIndex;

    /** 拼单关联字段：拼单总人数 */
    private Integer groupOrderTotal;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime readyAt;
    private LocalDateTime pickedAt;
    private LocalDateTime deliveredAt;

    @TableField(exist = false)
    private List<OrderItem> items;

    @TableField(exist = false)
    private Merchant merchant;

    /** 学生端列表/详情：是否已评价 */
    @TableField(exist = false)
    private Review review;

    /** 管理端使用：学生姓名 */
    @TableField(exist = false)
    private String studentName;
}
