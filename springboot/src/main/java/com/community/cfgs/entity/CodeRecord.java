package com.community.cfgs.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 验证码记录（不映射到数据库，仅内存存储）
 */
@Data
public class CodeRecord {
    /** 手机号 */
    private String phone;

    /** 验证码 */
    private String code;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 验证尝试次数 */
    private Integer verifyAttempts;

    /** 上次发送时间 */
    private LocalDateTime lastSendTime;

    /** 判断是否过期 */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    /** 判断是否在发送冷却期内（60秒） */
    public boolean isInCooldown() {
        if (lastSendTime == null) {
            return false;
        }
        return LocalDateTime.now().minusSeconds(60).isBefore(lastSendTime);
    }

    /** 判断是否超过最大尝试次数（5次） */
    public boolean isAttemptsExceeded() {
        return verifyAttempts != null && verifyAttempts >= 5;
    }

    /** 增加验证尝试次数 */
    public void incrementAttempts() {
        if (verifyAttempts == null) {
            verifyAttempts = 1;
        } else {
            verifyAttempts++;
        }
    }
}
