package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId
    private String id;
    private String username;
    private String password;
    private String role;  // student, merchant, rider, admin
    private String name;
    private String phone;
    private String avatar;
    @TableField("is_rider")
    private Boolean isRider;
    @TableField("is_disabled")
    private Boolean isDisabled;
    @TableField("is_online")
    private Boolean isOnline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
