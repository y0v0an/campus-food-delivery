package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rider_applications")
public class RiderApplication {
    @TableId
    private String id;
    private String studentId;
    private String studentName;
    private String phone;
    private String availableTime;  // JSON数组
    private String status;  // pending, approved, rejected
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
