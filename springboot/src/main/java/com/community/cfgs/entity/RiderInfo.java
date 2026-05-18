package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rider_info")
public class RiderInfo {
    @TableId
    private String id;
    private String userId;
    private String availableTime;  // JSON数组
    private String idCard;         // 身份证号
    private String vehicleType;    // 车辆类型
    private String plateNumber;    // 车牌号
    private String status;         // 审核状态: pending/approved/rejected
    private Integer totalDeliveries;
    private BigDecimal rating;
    private LocalDateTime createdAt;
}
