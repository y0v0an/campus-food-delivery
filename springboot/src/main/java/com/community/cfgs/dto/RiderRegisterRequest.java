package com.community.cfgs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 骑手注册请求DTO
 */
@Data
public class RiderRegisterRequest {

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$",
            message = "密码必须为6-20位，包含字母和数字")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
            message = "身份证号格式不正确")
    private String idCard;

    @NotBlank(message = "车辆类型不能为空")
    private String vehicleType;

    @NotBlank(message = "车牌号不能为空")
    @Pattern(regexp = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5}$",
            message = "车牌号格式不正确")
    private String plateNumber;

    private String availableTime;
}
