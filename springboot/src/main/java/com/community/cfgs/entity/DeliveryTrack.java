package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("delivery_tracks")
public class DeliveryTrack {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderId;
    private String riderId;
    private BigDecimal lat;
    private BigDecimal lng;
    private String address;
    private String status;  // to_merchant, at_merchant, to_student, delivered
    private LocalDateTime createdAt;
}
