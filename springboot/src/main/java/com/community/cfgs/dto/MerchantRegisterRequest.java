package com.community.cfgs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * 商家注册请求DTO
 */
@Data
public class MerchantRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$",
            message = "密码必须为6-20位，包含字母和数字")
    private String password;

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "店铺地址不能为空")
    private String address;

    private String description;

    private String logo;

    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z\\d]{18}$", message = "统一社会信用代码格式不正确")
    private String businessLicense;

    private String categories;

    private BigDecimal deliveryFee = new BigDecimal("5.00");

    private BigDecimal minOrder = new BigDecimal("20.00");

    private String deliveryTime = "30分钟";

    private BigDecimal lat;

    private BigDecimal lng;
}
