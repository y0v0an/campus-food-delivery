package com.community.cfgs.service.impl;

import com.community.cfgs.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 模拟短信服务实现
 * 当前开发阶段使用，验证码固定为 000000
 * 未来可替换为 AliyunSmsServiceImpl 或 TencentSmsServiceImpl
 */
@Slf4j
@Service
public class MockSmsServiceImpl implements SmsService {

    /** 模拟验证码（固定值） */
    private static final String MOCK_CODE = "000000";

    @Override
    public void sendVerificationCode(String phone, String code) {
        // 模拟发送短信，仅输出日志
        log.info("==========================================");
        log.info("【模拟短信】发送验证码到手机: {}", phone);
        log.info("【模拟短信】验证码: {}", MOCK_CODE);
        log.info("【模拟短信】有效期: 5分钟");
        log.info("【提示】正式环境中，此短信会发送到用户手机");
        log.info("==========================================");
    }

    /**
     * 获取模拟验证码（用于测试）
     * @return 模拟验证码
     */
    public static String getMockCode() {
        return MOCK_CODE;
    }
}
