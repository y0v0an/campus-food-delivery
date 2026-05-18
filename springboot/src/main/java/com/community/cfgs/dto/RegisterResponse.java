package com.community.cfgs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String userId;
    private String merchantId;
    private String riderInfoId;
    private String username;
    private String role;
    private String message;
    private Boolean success;
}
