package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.dto.MerchantRegisterRequest;
import com.community.cfgs.dto.RegisterResponse;
import com.community.cfgs.dto.RiderRegisterRequest;
import com.community.cfgs.dto.StudentRegisterRequest;
import com.community.cfgs.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 注册控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/register")
@Validated
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    /**
     * 学生注册
     * POST /api/register/student
     */
    @PostMapping("/student")
    public Result<RegisterResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        try {
            log.info("学生注册请求: username={}, phone={}", request.getUsername(), maskPhone(request.getPhone()));

            RegisterResponse response = registerService.registerStudent(request);

            if (Boolean.TRUE.equals(response.getSuccess())) {
                return Result.success(response);
            } else {
                return Result.error(response.getMessage());
            }
        } catch (Exception e) {
            log.error("学生注册失败: {}", e.getMessage(), e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 商家注册
     * POST /api/register/merchant
     */
    @PostMapping("/merchant")
    public Result<RegisterResponse> registerMerchant(@Valid @RequestBody MerchantRegisterRequest request) {
        try {
            log.info("商家注册请求: username={}, shopName={}, phone={}",
                    request.getUsername(), request.getShopName(), maskPhone(request.getPhone()));

            RegisterResponse response = registerService.registerMerchant(request);

            if (Boolean.TRUE.equals(response.getSuccess())) {
                return Result.success(response);
            } else {
                return Result.error(response.getMessage());
            }
        } catch (Exception e) {
            log.error("商家注册失败: {}", e.getMessage(), e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 骑手注册
     * POST /api/register/rider
     */
    @PostMapping("/rider")
    public Result<RegisterResponse> registerRider(@Valid @RequestBody RiderRegisterRequest request) {
        try {
            log.info("骑手注册请求: username={}, phone={}, vehicle={}",
                    request.getUsername(), maskPhone(request.getPhone()), request.getVehicleType());

            RegisterResponse response = registerService.registerRider(request);

            if (Boolean.TRUE.equals(response.getSuccess())) {
                return Result.success(response);
            } else {
                return Result.error(response.getMessage());
            }
        } catch (Exception e) {
            log.error("骑手注册失败: {}", e.getMessage(), e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户名是否可用
     * GET /api/register/check-username?username=xxx
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = registerService.checkUsernameExists(username);
        return Result.success(!exists);
    }

    /**
     * 检查手机号是否已注册
     * GET /api/register/check-phone?phone=xxx
     */
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = registerService.checkPhoneExists(phone);
        return Result.success(!exists);
    }

    /**
     * 手机号脱敏（用于日志）
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 删除测试数据（仅用于测试环境清理）
     * DELETE /api/register/test-data
     */
    @DeleteMapping("/test-data")
    public Result<String> deleteTestData() {
        try {
            int deletedCount = registerService.deleteTestData();
            return Result.success("成功删除 " + deletedCount + " 条测试数据");
        } catch (Exception e) {
            log.error("删除测试数据失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
