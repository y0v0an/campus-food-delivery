package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.User;
import com.community.cfgs.service.UserService;
import com.community.cfgs.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 用户登录（手机号登录）
     * @param params 包含phone（手机号）和password（密码）
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");

        // 参数校验
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        User user = userService.login(phone.trim(), password.trim());
        if (user == null) {
            return Result.error("账号或密码错误，或账号已被禁用");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("name", user.getName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("isRider", user.getIsRider());
        userInfo.put("isDisabled", user.getIsDisabled());
        userInfo.put("isOnline", user.getIsOnline());

        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("登出成功");
    }

    @GetMapping("/info/{id}")
    public Result<User> getById(@PathVariable String id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        boolean success = userService.updateUser(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @GetMapping("/list")
    public Result<List<User>> list(@RequestParam(required = false) String role) {
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userService.listByRole(role);
        } else {
            users = userService.list();
        }
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @PutMapping("/disable/{id}")
    public Result<String> disableUser(@PathVariable String id) {
        boolean success = userService.disableUser(id);
        return success ? Result.success("禁用成功") : Result.error("禁用失败");
    }

    @PutMapping("/enable/{id}")
    public Result<String> enableUser(@PathVariable String id) {
        boolean success = userService.enableUser(id);
        return success ? Result.success("启用成功") : Result.error("启用失败");
    }

    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        boolean success = userService.addUser(user);
        return success ? Result.success("添加成功") : Result.error("添加失败，账号可能已存在");
    }

    /**
     * 发送验证码（忘记密码）
     * @param params 包含phone（手机号）
     * @return 发送结果
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");

        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }

        String message = verificationCodeService.sendCode(phone.trim());
        if (message.equals("验证码已发送")) {
            return Result.success(message);
        } else {
            return Result.error(message);
        }
    }

    /**
     * 验证验证码
     * @param params 包含phone（手机号）和code（验证码）
     * @return 验证结果
     */
    @PostMapping("/verify-code")
    public Result<Map<String, Object>> verifyCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");

        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }

        Object[] result = verificationCodeService.verifyCode(phone.trim(), code.trim());
        Map<String, Object> data = new HashMap<>();
        data.put("valid", result[0]);
        data.put("message", result[1]);

        return Result.success(data);
    }

    /**
     * 重置密码
     * @param params 包含phone（手机号）、code（验证码）、newPassword（新密码）
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String newPassword = params.get("newPassword");

        // 参数校验
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error("密码长度不能少于6位");
        }

        phone = phone.trim();
        code = code.trim();
        newPassword = newPassword.trim();

        // 验证验证码
        if (!verificationCodeService.verifyAndConsumeCode(phone, code)) {
            return Result.error("验证码错误或已过期");
        }

        // 查找用户
        User user = userService.getByPhone(phone);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新密码
        boolean success = userService.updatePassword(user.getId(), newPassword);
        if (success) {
            return Result.success("密码重置成功，请使用新密码登录");
        } else {
            return Result.error("密码重置失败，请稍后重试");
        }
    }
}
