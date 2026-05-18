package com.community.cfgs.service;

/**
 * 短信服务接口
 * 设计为接口以便未来扩展不同短信服务提供商（阿里云、腾讯云等）
 */
public interface SmsService {

    /**
     * 发送验证码短信
     * @param phone 手机号
     * @param code 验证码
     */
    void sendVerificationCode(String phone, String code);
}
