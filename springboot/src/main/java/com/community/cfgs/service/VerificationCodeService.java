package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.cfgs.entity.CodeRecord;
import com.community.cfgs.entity.User;
import com.community.cfgs.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * 负责：生成验证码、发送短信、验证验证码、清理过期数据
 */
@Slf4j
@Service
public class VerificationCodeService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserMapper userMapper;

    /** 验证码存储（内存存储，未来可迁移到Redis） */
    private final ConcurrentHashMap<String, CodeRecord> codeStore = new ConcurrentHashMap<>();

    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /** 发送冷却时间（秒） */
    private static final int SEND_COOLDOWN_SECONDS = 60;

    /** 最大验证尝试次数 */
    private static final int MAX_ATTEMPTS = 5;

    /** 随机数生成器 */
    private final Random random = new Random();

    /** 定时清理线程池 */
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

    /**
     * 构造函数，启动定时清理任务
     */
    public VerificationCodeService() {
        // 每10分钟清理一次过期验证码
        cleanupExecutor.scheduleAtFixedRate(this::cleanExpiredCodes, 10, 10, TimeUnit.MINUTES);
        log.info("验证码服务已启动，定时清理任务已注册");
    }

    /**
     * 发送验证码
     * @param phone 手机号
     * @return 发送结果消息
     */
    public String sendCode(String phone) {
        // 1. 验证手机号格式
        if (!isValidPhone(phone)) {
            return "手机号格式不正确";
        }

        // 2. 检查手机号是否已注册
        if (!isPhoneRegistered(phone)) {
            return "该手机号未注册";
        }

        // 3. 检查冷却时间
        CodeRecord existingRecord = codeStore.get(phone);
        if (existingRecord != null && existingRecord.isInCooldown()) {
            return "请60秒后再试";
        }

        // 4. 生成新验证码（模拟版本使用固定验证码）
        String code = "000000"; // 模拟版本固定验证码

        // 5. 创建验证码记录
        CodeRecord record = new CodeRecord();
        record.setPhone(phone);
        record.setCode(code);
        record.setCreateTime(LocalDateTime.now());
        record.setExpireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        record.setVerifyAttempts(0);
        record.setLastSendTime(LocalDateTime.now());

        codeStore.put(phone, record);

        // 6. 发送短信
        try {
            smsService.sendVerificationCode(phone, code);
            return "验证码已发送";
        } catch (Exception e) {
            log.error("发送短信失败: phone={}", phone, e);
            return "发送验证码失败，请稍后重试";
        }
    }

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果 [是否成功, 错误消息]
     */
    public Object[] verifyCode(String phone, String code) {
        CodeRecord record = codeStore.get(phone);

        // 1. 检查验证码记录是否存在
        if (record == null) {
            return new Object[]{false, "验证码不存在或已过期"};
        }

        // 2. 检查是否过期
        if (record.isExpired()) {
            codeStore.remove(phone);
            return new Object[]{false, "验证码已过期，请重新获取"};
        }

        // 3. 检查尝试次数
        if (record.isAttemptsExceeded()) {
            codeStore.remove(phone);
            return new Object[]{false, "验证码错误次数过多，请重新获取"};
        }

        // 4. 验证验证码
        if (record.getCode().equals(code)) {
            // 验证成功，移除记录
            codeStore.remove(phone);
            return new Object[]{true, "验证成功"};
        } else {
            // 验证失败，增加尝试次数
            record.incrementAttempts();
            int remainingAttempts = MAX_ATTEMPTS - record.getVerifyAttempts();
            return new Object[]{false, "验证码错误，还剩" + remainingAttempts + "次机会"};
        }
    }

    /**
     * 验证并消耗验证码（用于重置密码场景）
     * @param phone 手机号
     * @param code 验证码
     * @return 验证是否成功
     */
    public boolean verifyAndConsumeCode(String phone, String code) {
        Object[] result = verifyCode(phone, code);
        return (Boolean) result[0];
    }

    /**
     * 验证手机号格式（中国大陆11位手机号）
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 检查手机号是否已注册
     */
    private boolean isPhoneRegistered(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 清理过期的验证码记录
     */
    private void cleanExpiredCodes() {
        int beforeSize = codeStore.size();
        codeStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterSize = codeStore.size();
        int cleaned = beforeSize - afterSize;
        if (cleaned > 0) {
            log.info("清理过期验证码: {} 条", cleaned);
        }
    }
}
