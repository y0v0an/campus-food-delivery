package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.User;
import com.community.cfgs.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录（支持BCrypt密码验证）
     * @param phone 手机号
     * @param password 密码
     * @return 用户信息，登录失败返回null
     */
    public User login(String phone, String password) {
        // 手机号格式验证
        if (!isValidPhone(phone)) {
            log.warn("手机号格式不正确: phone={}", phone);
            return null;
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return null;
        }

        // 检查账号是否被禁用
        if (Boolean.TRUE.equals(user.getIsDisabled())) {
            log.warn("账号已禁用: phone={}", phone);
            return null;
        }

        // 验证密码（兼容BCrypt加密和明文密码）
        if (!verifyPassword(password, user.getPassword())) {
            log.warn("密码错误: phone={}", phone);
            return null;
        }

        return user;
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
     * 密码验证（兼容BCrypt加密和明文密码）
     * 新注册用户使用BCrypt加密，旧用户可能是明文密码
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        // BCrypt密码以$2a$或$2b$开头
        if (encodedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
        // 兼容旧明文密码
        return rawPassword.equals(encodedPassword);
    }

    public User getById(String id) {
        return userMapper.selectById(id);
    }

    public List<User> list() {
        return userMapper.selectList(null);
    }

    public List<User> listByRole(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, role);
        return userMapper.selectList(wrapper);
    }

    public boolean disableUser(String id) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id).set(User::getIsDisabled, true);
        return userMapper.update(null, wrapper) > 0;
    }

    public boolean enableUser(String id) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id).set(User::getIsDisabled, false);
        return userMapper.update(null, wrapper) > 0;
    }

    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }

    public boolean setRiderStatus(String userId, boolean isRider) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId).set(User::getIsRider, isRider);
        return userMapper.update(null, wrapper) > 0;
    }

    public boolean addUser(User user) {
        // 骑手在库中为 student + isRider，与登录、学生切骑手端一致
        if ("rider".equals(user.getRole())) {
            user.setRole("student");
            user.setIsRider(true);
        }

        // 检查账号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return false;
        }

        // 生成用户ID
        String prefix = "student";
        if ("merchant".equals(user.getRole())) {
            prefix = "merchant";
        } else if ("admin".equals(user.getRole())) {
            prefix = "admin";
        }
        user.setId(prefix + "_" + java.util.UUID.randomUUID().toString().substring(0, 8));

        if (user.getIsDisabled() == null) {
            user.setIsDisabled(false);
        }
        if (user.getIsRider() == null) {
            user.setIsRider(false);
        }
        if (user.getIsOnline() == null) {
            user.setIsOnline(false);
        }

        return userMapper.insert(user) > 0;
    }

    /**
     * 更新用户密码（BCrypt加密）
     */
    public boolean updatePassword(String userId, String newPassword) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getPassword, passwordEncoder.encode(newPassword));
        return userMapper.update(null, wrapper) > 0;
    }

    /**
     * 根据手机号获取用户
     * @param phone 手机号
     * @return 用户信息，不存在返回null
     */
    public User getByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }
}
