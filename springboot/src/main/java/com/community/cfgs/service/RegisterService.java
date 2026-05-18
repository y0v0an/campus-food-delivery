package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.cfgs.dto.MerchantRegisterRequest;
import com.community.cfgs.dto.RegisterResponse;
import com.community.cfgs.dto.RiderRegisterRequest;
import com.community.cfgs.dto.StudentRegisterRequest;
import com.community.cfgs.entity.Merchant;
import com.community.cfgs.entity.RiderInfo;
import com.community.cfgs.entity.User;
import com.community.cfgs.mapper.MerchantMapper;
import com.community.cfgs.mapper.RiderInfoMapper;
import com.community.cfgs.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 注册服务
 */
@Slf4j
@Service
public class RegisterService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @javax.annotation.Resource
    private UserMapper userMapper;

    @javax.annotation.Resource
    private MerchantMapper merchantMapper;

    @javax.annotation.Resource
    private RiderInfoMapper riderInfoMapper;

    /**
     * 学生注册
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse registerStudent(StudentRegisterRequest request) {
        // 验证用户名唯一性
        if (checkUsernameExists(request.getUsername())) {
            return RegisterResponse.builder()
                    .success(false)
                    .message("用户名已存在")
                    .build();
        }

        // 验证手机号唯一性
        if (checkPhoneExists(request.getPhone())) {
            return RegisterResponse.builder()
                    .success(false)
                    .message("手机号已被注册")
                    .build();
        }

        // 创建用户
        User user = new User();
        String userId = "student_" + generateId();
        user.setId(userId);
        user.setUsername(request.getUsername());
        user.setPassword(encodePassword(request.getPassword()));
        user.setRole("student");
        user.setName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setIsRider(false);
        user.setIsDisabled(false);
        user.setIsOnline(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        log.info("学生注册成功: userId={}, username={}", userId, request.getUsername());

        return RegisterResponse.builder()
                .userId(userId)
                .username(request.getUsername())
                .role("student")
                .message("注册成功")
                .success(true)
                .build();
    }

    /**
     * 商家注册
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse registerMerchant(MerchantRegisterRequest request) {
        // 验证用户名唯一性
        if (checkUsernameExists(request.getUsername())) {
            return RegisterResponse.builder()
                    .success(false)
                    .message("用户名已存在")
                    .build();
        }

        // 验证手机号唯一性
        if (checkPhoneExists(request.getPhone())) {
            return RegisterResponse.builder()
                    .success(false)
                    .message("手机号已被注册")
                    .build();
        }

        // 创建用户
        User user = new User();
        String userId = "merchant_" + generateId();
        user.setId(userId);
        user.setUsername(request.getUsername());
        user.setPassword(encodePassword(request.getPassword()));
        user.setRole("merchant");
        user.setName(request.getContactName());
        user.setPhone(request.getPhone());
        user.setIsRider(false);
        user.setIsDisabled(false);
        user.setIsOnline(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 创建商家信息
        Merchant merchant = new Merchant();
        String merchantId = "m_" + generateId();
        merchant.setId(merchantId);
        merchant.setUserId(userId);
        merchant.setName(request.getShopName());
        merchant.setPhone(request.getPhone());
        merchant.setAddress(request.getAddress());
        merchant.setDescription(request.getDescription());
        merchant.setLogo(request.getLogo());
        merchant.setCategories(StringUtils.hasText(request.getCategories()) ? request.getCategories() : "[]");
        merchant.setIsOpen(false); // 新注册商家默认关闭，需要审核后开启
        merchant.setDeliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : new BigDecimal("5.00"));
        merchant.setMinOrder(request.getMinOrder() != null ? request.getMinOrder() : new BigDecimal("20.00"));
        merchant.setDeliveryTime(StringUtils.hasText(request.getDeliveryTime()) ? request.getDeliveryTime() : "30分钟");
        merchant.setLat(request.getLat());
        merchant.setLng(request.getLng());
        merchant.setRating(new BigDecimal("5.0"));
        merchant.setMonthSales(0);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());

        merchantMapper.insert(merchant);

        log.info("商家注册成功: userId={}, merchantId={}, shopName={}", userId, merchantId, request.getShopName());

        return RegisterResponse.builder()
                .userId(userId)
                .merchantId(merchantId)
                .username(request.getUsername())
                .role("merchant")
                .message("注册成功，请等待管理员审核")
                .success(true)
                .build();
    }

    /**
     * 骑手注册
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse registerRider(RiderRegisterRequest request) {
        // 验证用户名唯一性
        if (checkUsernameExists(request.getUsername())) {
            return RegisterResponse.builder()
                    .success(false)
                    .message("用户名已存在")
                    .build();
        }

        // 验证手机号唯一性
        if (checkPhoneExists(request.getPhone())) {
            return RegisterResponse.builder()
                    .success(false)
                    .message("手机号已被注册")
                    .build();
        }

        // 检查是否已经是学生用户，如果是则升级为骑手
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getPhone, request.getPhone());
        User existingUser = userMapper.selectOne(userWrapper);

        String userId;
        boolean isNewUser = false;

        if (existingUser != null && "student".equals(existingUser.getRole())) {
            // 已有学生账号，升级为骑手
            userId = existingUser.getId();
            existingUser.setIsRider(true);
            userMapper.updateById(existingUser);

            log.info("学生用户升级为骑手: userId={}", userId);
        } else {
            // 创建新用户
            isNewUser = true;
            userId = "student_" + generateId();

            User user = new User();
            user.setId(userId);
            user.setUsername(request.getUsername());
            user.setPassword(encodePassword(request.getPassword()));
            user.setRole("student"); // 骑手的基础角色是学生
            user.setName(request.getRealName());
            user.setPhone(request.getPhone());
            user.setIsRider(true);
            user.setIsDisabled(false);
            user.setIsOnline(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userMapper.insert(user);

            log.info("新骑手注册成功: userId={}", userId);
        }

        // 创建骑手信息
        RiderInfo riderInfo = new RiderInfo();
        String riderInfoId = "rider_" + generateId();
        riderInfo.setId(riderInfoId);
        riderInfo.setUserId(userId);
        riderInfo.setAvailableTime(StringUtils.hasText(request.getAvailableTime()) ? request.getAvailableTime() : "[]");
        riderInfo.setIdCard(request.getIdCard());
        riderInfo.setVehicleType(request.getVehicleType());
        riderInfo.setPlateNumber(request.getPlateNumber());
        riderInfo.setStatus("pending"); // 默认为待审核状态
        riderInfo.setTotalDeliveries(0);
        riderInfo.setRating(new BigDecimal("5.0"));
        riderInfo.setCreatedAt(LocalDateTime.now());

        riderInfoMapper.insert(riderInfo);

        String message = isNewUser ? "注册成功，请等待管理员审核" : "已升级为骑手，请等待管理员审核";

        return RegisterResponse.builder()
                .userId(userId)
                .riderInfoId(riderInfoId)
                .username(request.getUsername())
                .role("rider")
                .message(message)
                .success(true)
                .build();
    }

    /**
     * 检查用户名是否存在
     */
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查手机号是否存在
     */
    public boolean checkPhoneExists(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 密码加密
     */
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 生成随机ID
     */
    private String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 删除测试数据（删除用户名包含 test_ 的用户）
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestData() {
        // 删除测试用户（用户名包含 test_）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.likeRight(User::getUsername, "test_");

        List<User> testUsers = userMapper.selectList(userWrapper);

        if (testUsers.isEmpty()) {
            return 0;
        }

        // 收集所有用户ID
        List<String> userIds = testUsers.stream().map(User::getId).collect(java.util.stream.Collectors.toList());

        // 删除骑手信息
        LambdaQueryWrapper<RiderInfo> riderWrapper = new LambdaQueryWrapper<>();
        riderWrapper.in(RiderInfo::getUserId, userIds);
        riderInfoMapper.delete(riderWrapper);

        // 删除商家信息
        LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.in(Merchant::getUserId, userIds);
        merchantMapper.delete(merchantWrapper);

        // 删除用户
        int deletedCount = userMapper.delete(userWrapper);

        log.info("删除测试数据: {} 条用户记录", deletedCount);

        return deletedCount;
    }
}
