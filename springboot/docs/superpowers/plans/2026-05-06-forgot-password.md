# 忘记密码功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现基于手机号验证码的密码重置功能，使用模拟验证码，代码支持未来扩展真实短信服务。

**架构:** 后端提供三个API（发送验证码、验证验证码、重置密码），前端提供两步式忘记密码页面。短信服务设计为接口，当前实现为Mock版本，便于未来切换。

**Tech Stack:**
- 后端: Spring Boot, MyBatis-Plus, BCrypt, ConcurrentHashMap
- 前端: Vue 3, Element Plus, Tailwind CSS, Axios

---

## 文件结构概览

### 后端新增文件
- `src/main/java/com/community/cfgs/entity/CodeRecord.java` - 验证码记录实体
- `src/main/java/com/community/cfgs/service/SmsService.java` - 短信服务接口
- `src/main/java/com/community/cfgs/service/impl/MockSmsServiceImpl.java` - 模拟短信实现
- `src/main/java/com/community/cfgs/service/VerificationCodeService.java` - 验证码服务
- `src/main/java/com/community/cfgs/config/SmsConfig.java` - 短信服务配置

### 后端修改文件
- `src/main/java/com/community/cfgs/controller/UserController.java` - 添加三个新接口

### 前端新增文件
- `src/views/Login/ForgotPasswordPage.vue` - 忘记密码页面

### 前端修改文件
- `src/router/index.js` - 添加忘记密码路由
- `src/views/Login/LoginPage.vue` - 修改忘记密码链接

---

## 后端实现

### Task 1: 创建验证码记录实体类

**Files:**
- Create: `src/main/java/com/community/cfgs/entity/CodeRecord.java`

- [ ] **Step 1: 创建 CodeRecord 实体类**

```java
package com.community.cfgs.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 验证码记录（不映射到数据库，仅内存存储）
 */
@Data
public class CodeRecord {
    /** 手机号 */
    private String phone;

    /** 验证码 */
    private String code;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 验证尝试次数 */
    private Integer verifyAttempts;

    /** 上次发送时间 */
    private LocalDateTime lastSendTime;

    /** 判断是否过期 */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    /** 判断是否在发送冷却期内（60秒） */
    public boolean isInCooldown() {
        if (lastSendTime == null) {
            return false;
        }
        return LocalDateTime.now().minusSeconds(60).isBefore(lastSendTime);
    }

    /** 判断是否超过最大尝试次数（5次） */
    public boolean isAttemptsExceeded() {
        return verifyAttempts != null && verifyAttempts >= 5;
    }

    /** 增加验证尝试次数 */
    public void incrementAttempts() {
        if (verifyAttempts == null) {
            verifyAttempts = 1;
        } else {
            verifyAttempts++;
        }
    }
}
```

- [ ] **Step 2: 验证编译成功**

Run: `mvn compile`
Expected: BUILD SUCCESS

---

### Task 2: 创建短信服务接口

**Files:**
- Create: `src/main/java/com/community/cfgs/service/SmsService.java`

- [ ] **Step 1: 创建 SmsService 接口**

```java
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
```

- [ ] **Step 2: 验证编译成功**

Run: `mvn compile`
Expected: BUILD SUCCESS

---

### Task 3: 创建模拟短信服务实现

**Files:**
- Create: `src/main/java/com/community/cfgs/service/impl/MockSmsServiceImpl.java`

- [ ] **Step 1: 创建 MockSmsServiceImpl 类**

```java
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
```

- [ ] **Step 2: 验证编译成功**

Run: `mvn compile`
Expected: BUILD SUCCESS

---

### Task 4: 创建验证码服务

**Files:**
- Create: `src/main/java/com/community/cfgs/service/VerificationCodeService.java`

- [ ] **Step 1: 创建 VerificationCodeService 类**

```java
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
    public boolean[] verifyCode(String phone, String code) {
        CodeRecord record = codeStore.get(phone);

        // 1. 检查验证码记录是否存在
        if (record == null) {
            return new boolean[]{false, "验证码不存在或已过期"};
        }

        // 2. 检查是否过期
        if (record.isExpired()) {
            codeStore.remove(phone);
            return new boolean[]{false, "验证码已过期，请重新获取"};
        }

        // 3. 检查尝试次数
        if (record.isAttemptsExceeded()) {
            codeStore.remove(phone);
            return new boolean[]{false, "验证码错误次数过多，请重新获取"};
        }

        // 4. 验证验证码
        if (record.getCode().equals(code)) {
            // 验证成功，移除记录
            codeStore.remove(phone);
            return new boolean[]{true, "验证成功"};
        } else {
            // 验证失败，增加尝试次数
            record.incrementAttempts();
            int remainingAttempts = MAX_ATTEMPTS - record.getVerifyAttempts();
            return new boolean[]{false, "验证码错误，还剩" + remainingAttempts + "次机会"};
        }
    }

    /**
     * 验证并消耗验证码（用于重置密码场景）
     * @param phone 手机号
     * @param code 验证码
     * @return 验证是否成功
     */
    public boolean verifyAndConsumeCode(String phone, String code) {
        boolean[] result = verifyCode(phone, code);
        return result[0];
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
```

- [ ] **Step 2: 验证编译成功**

Run: `mvn compile`
Expected: BUILD SUCCESS

---

### Task 5: 在 UserController 中添加忘记密码接口

**Files:**
- Modify: `src/main/java/com/community/cfgs/controller/UserController.java`

- [ ] **Step 1: 添加 VerificationCodeService 依赖**

在类开头添加 `@Autowired` 注入：

```java
@Autowired
private VerificationCodeService verificationCodeService;
```

- [ ] **Step 2: 添加发送验证码接口**

在 `UserController` 类中添加方法：

```java
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
```

- [ ] **Step 3: 添加验证验证码接口**

在 `UserController` 类中添加方法：

```java
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

    boolean[] result = verificationCodeService.verifyCode(phone.trim(), code.trim());
    Map<String, Object> data = new HashMap<>();
    data.put("valid", result[0]);
    data.put("message", result[1]);

    return Result.success(data);
}
```

- [ ] **Step 4: 添加重置密码接口**

在 `UserController` 类中添加方法：

```java
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
```

- [ ] **Step 5: 在 UserService 中添加 getByPhone 方法**

修改 `UserService.java`，添加方法：

```java
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
```

- [ ] **Step 6: 验证编译成功**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 7: 启动后端服务测试**

Run: `mvn spring-boot:run`
Expected: 服务启动成功，无报错

---

## 前端实现

### Task 6: 添加忘记密码路由

**Files:**
- Modify: `src/router/index.js`

- [ ] **Step 1: 在路由配置中添加忘记密码路由**

在注册页路由之后添加：

```javascript
// 忘记密码页
{
  path: '/forgot-password',
  name: 'ForgotPassword',
  component: () => import('@/views/Login/ForgotPasswordPage.vue'),
  meta: { requiresAuth: false }
}
```

添加位置：第28行之后（注册页路由之后）

---

### Task 7: 修改登录页忘记密码链接

**Files:**
- Modify: `src/views/Login/LoginPage.vue`

- [ ] **Step 1: 修改忘记密码链接**

将第553行的：
```html
<a class="text-orange-500 hover:text-orange-600 font-medium" href="#">忘记密码？</a>
```

修改为：
```html
<a @click="goToForgotPassword" class="text-orange-500 hover:text-orange-600 font-medium cursor-pointer">忘记密码？</a>
```

- [ ] **Step 2: 添加跳转方法**

在 `goToRegister` 方法之后添加：

```javascript
// 跳转到忘记密码页面
const goToForgotPassword = () => {
  router.push('/forgot-password')
}
```

添加位置：第730行之后

---

### Task 8: 创建忘记密码页面

**Files:**
- Create: `src/views/Login/ForgotPasswordPage.vue`

- [ ] **Step 1: 创建忘记密码页面组件**

```vue
<template>
  <div class="min-h-screen flex bg-slate-50">
    <!-- 左侧装饰区域（大屏显示） -->
    <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-orange-500 to-orange-600 relative overflow-hidden">
      <!-- 背景图片 -->
      <div class="absolute inset-0 opacity-20">
        <img
            alt="Campus illustration"
            class="w-full h-full object-cover"
            src="https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=1200&h=800&fit=crop"
        />
      </div>

      <!-- 装饰圆形 -->
      <div class="absolute top-0 right-0 w-96 h-96 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2"></div>
      <div class="absolute bottom-0 left-0 w-64 h-64 bg-white/10 rounded-full translate-y-1/2 -translate-x-1/2"></div>

      <!-- 内容区域 -->
      <div class="relative z-10 flex flex-col items-center justify-center w-full text-white px-12">
        <!-- Logo -->
        <div class="w-32 h-32 bg-white rounded-3xl flex items-center justify-center shadow-2xl mb-8">
          <span class="text-7xl">🍔</span>
        </div>

        <!-- 标题 -->
        <h1 class="text-5xl font-black tracking-wider mb-4">校食达</h1>
        <p class="text-xl text-orange-100 font-medium mb-2">校园外卖配送平台</p>
        <p class="text-orange-200">UniEat · 专属于校园的美食配送</p>
      </div>
    </div>

    <!-- 右侧表单 -->
    <div class="w-full lg:w-1/2 flex items-center justify-center p-8">
      <div class="w-full max-w-md">
        <!-- 移动端Logo -->
        <div class="lg:hidden flex items-center justify-center mb-8">
          <div class="w-16 h-16 bg-orange-500 rounded-2xl flex items-center justify-center shadow-lg">
            <span class="text-3xl">🍔</span>
          </div>
          <div class="ml-4">
            <h1 class="text-2xl font-black text-gray-800">校食达</h1>
            <p class="text-sm text-gray-500">校园外卖配送平台</p>
          </div>
        </div>

        <!-- 卡片 -->
        <div class="bg-white rounded-3xl shadow-xl p-8">
          <!-- 标题 -->
          <div class="text-center mb-6">
            <h2 class="text-2xl font-black text-gray-800">
              {{ currentStep === 1 ? '验证身份' : '设置新密码' }}
            </h2>
            <p class="text-gray-500 mt-2">
              {{ currentStep === 1 ? '请输入注册手机号获取验证码' : '请输入您的新密码' }}
            </p>
          </div>

          <!-- 步骤 1：验证身份 -->
          <div v-if="currentStep === 1" class="space-y-5">
            <!-- 手机号输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">📱</span>
              <input
                  v-model="form.phone"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  placeholder="请输入手机号"
                  type="text"
                  maxlength="11"
              />
            </div>

            <!-- 验证码输入 -->
            <div class="flex gap-3">
              <div class="relative flex-1">
                <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">🔢</span>
                <input
                    v-model="form.code"
                    class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                    placeholder="请输入验证码"
                    type="text"
                    maxlength="6"
                />
              </div>
              <button
                  class="px-6 py-4 bg-orange-500 hover:bg-orange-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-medium rounded-2xl transition-all whitespace-nowrap min-w-[120px]"
                  :disabled="countdown > 0 || sending"
                  @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
              </button>
            </div>

            <!-- 开发提示 -->
            <div class="bg-blue-50 border border-blue-200 rounded-xl p-3 text-sm text-blue-600">
              <span class="font-medium">开发模式：</span>验证码固定为 000000
            </div>

            <!-- 下一步按钮 -->
            <button
                class="w-full bg-orange-500 hover:bg-orange-600 text-white font-black py-4 rounded-2xl shadow-xl shadow-orange-200 transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="verifying || !form.phone || !form.code"
                @click="verifyCodeAndNext"
            >
              {{ verifying ? '验证中...' : '下一步' }}
            </button>
          </div>

          <!-- 步骤 2：设置新密码 -->
          <div v-else class="space-y-5">
            <!-- 新密码输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">🔒</span>
              <input
                  v-model="form.newPassword"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  placeholder="请输入新密码（至少6位）"
                  type="password"
              />
            </div>

            <!-- 确认密码输入 -->
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl">🔒</span>
              <input
                  v-model="form.confirmPassword"
                  class="w-full pl-12 pr-4 py-4 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
                  placeholder="请再次输入新密码"
                  type="password"
                  @keyup.enter="resetPassword"
              />
            </div>

            <!-- 重置按钮 -->
            <button
                class="w-full bg-orange-500 hover:bg-orange-600 text-white font-black py-4 rounded-2xl shadow-xl shadow-orange-200 transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="resetting || !form.newPassword || !form.confirmPassword"
                @click="resetPassword"
            >
              {{ resetting ? '重置中...' : '重置密码' }}
            </button>
          </div>

          <!-- 返回登录 -->
          <div class="mt-6 text-center text-sm text-gray-500">
            <span>想起密码了？</span>
            <a @click="goToLogin" class="text-orange-500 hover:text-orange-600 font-medium cursor-pointer">
              返回登录
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/axios/request'

const router = useRouter()

// 当前步骤：1-验证身份，2-设置新密码
const currentStep = ref(1)

// 表单数据
const form = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 状态
const sending = ref(false)
const verifying = ref(false)
const resetting = ref(false)
const countdown = ref(0)

// 验证手机号格式
const isValidPhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 发送验证码
const sendCode = async () => {
  if (!form.phone) {
    ElMessage.warning('请输入手机号')
    return
  }

  if (!isValidPhone(form.phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }

  sending.value = true

  try {
    await request.post('/user/send-code', { phone: form.phone })
    ElMessage.success('验证码已发送')

    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    sending.value = false
  }
}

// 验证验证码并进入下一步
const verifyCodeAndNext = async () => {
  if (!form.phone || !form.code) {
    ElMessage.warning('请输入手机号和验证码')
    return
  }

  verifying.value = true

  try {
    const result = await request.post('/user/verify-code', {
      phone: form.phone,
      code: form.code
    })

    if (result.valid) {
      currentStep.value = 2
    } else {
      ElMessage.error(result.message || '验证码错误')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error(error.message || '验证失败')
  } finally {
    verifying.value = false
  }
}

// 重置密码
const resetPassword = async () => {
  if (!form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请输入新密码和确认密码')
    return
  }

  if (form.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }

  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  resetting.value = true

  try {
    await request.post('/user/reset-password', {
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword
    })

    ElMessage.success('密码重置成功，请使用新密码登录')

    // 延迟跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error(error.message || '重置密码失败')
  } finally {
    resetting.value = false
  }
}

// 返回登录
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* Tailwind CSS 已处理所有样式 */
</style>
```

- [ ] **Step 2: 验证前端编译成功**

Run: `cd vue && npm run dev`
Expected: 开发服务器启动成功，无报错

---

## 测试验证

### Task 9: 端到端测试

**Files:**
- Test: 手动测试

- [ ] **Step 1: 启动后端服务**

Run: `mvn spring-boot:run`
Expected: 服务启动在 http://localhost:8080

- [ ] **Step 2: 启动前端服务**

Run: `cd vue && npm run dev`
Expected: 前端启动在 http://localhost:5173

- [ ] **Step 3: 测试忘记密码流程**

1. 访问 http://localhost:5173
2. 点击"忘记密码？"链接
3. 输入已注册的手机号（如 13800000001）
4. 点击"获取验证码"
5. 查看后端日志，确认输出模拟验证码 000000
6. 输入验证码 000000
7. 点击"下一步"，验证是否进入步骤2
8. 输入新密码（如 123456）
9. 点击"重置密码"
10. 验证是否成功跳转到登录页
11. 使用新密码登录，验证是否成功

- [ ] **Step 4: 测试错误场景**

1. 输入未注册的手机号 → 应提示"该手机号未注册"
2. 连续点击"获取验证码" → 应提示"请60秒后再试"
3. 输入错误的验证码 → 应提示"验证码错误，还剩X次机会"
4. 5次输入错误验证码 → 应提示"验证码错误次数过多，请重新获取"
5. 两次密码不一致 → 应提示"两次输入的密码不一致"
6. 密码少于6位 → 应提示"密码长度不能少于6位"

---

## 完成检查清单

- [ ] 后端验证码服务正常工作
- [ ] 后端三个API接口响应正确
- [ ] 前端忘记密码页面正常显示
- [ ] 前端两步式流程交互正常
- [ ] 验证码60秒冷却限制生效
- [ ] 验证码5分钟过期生效
- [ ] 验证码5次错误限制生效
- [ ] 密码重置后可用新密码登录
- [ ] 登录页忘记密码链接正常跳转
- [ ] 所有错误提示显示正确
