# 忘记密码功能设计文档

**日期**: 2026-05-06
**项目**: CFGs 校园外卖配送系统
**功能**: 忘记密码 / 密码重置

## 一、功能概述

用户可通过手机号接收短信验证码来重置密码。现阶段使用模拟验证码 `000000`，代码设计支持未来接入真实短信服务。

## 二、前端设计

### 2.1 新增页面

**文件**: `vue/src/views/Login/ForgotPasswordPage.vue`

### 2.2 用户流程

```
┌─────────────────────────────────────────────────────────┐
│  步骤 1：验证身份                                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │  输入手机号                                       │   │
│  │  [获取验证码] 按钮 → 60秒倒计时                   │   │
│  │  输入验证码                                       │   │
│  │  [下一步] 按钮                                    │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                         ↓ 验证码验证成功
┌─────────────────────────────────────────────────────────┐
│  步骤 2：设置新密码                                      │
│  ┌─────────────────────────────────────────────────┐   │
│  │  输入新密码                                       │   │
│  │  确认新密码                                       │   │
│  │  [重置密码] 按钮                                  │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                         ↓ 重置成功
                    返回登录页
```

### 2.3 路由配置

在 `vue/src/router/index.js` 中添加：

```javascript
{
  path: '/forgot-password',
  name: 'ForgotPassword',
  component: () => import('@/views/Login/ForgotPasswordPage.vue'),
  meta: { requiresAuth: false }
}
```

### 2.4 登录页修改

修改 `LoginPage.vue` 第553行：

```html
<!-- 修改前 -->
<a class="text-orange-500 hover:text-orange-600 font-medium" href="#">忘记密码？</a>

<!-- 修改后 -->
<a @click="goToForgotPassword" class="text-orange-500 hover:text-orange-600 font-medium cursor-pointer">忘记密码？</a>
```

添加跳转方法：

```javascript
const goToForgotPassword = () => {
  router.push('/forgot-password')
}
```

## 三、后端设计

### 3.1 新增 API 接口

| 接口 | 方法 | 描述 | 请求参数 | 返回值 |
|-----|------|------|----------|--------|
| `/api/user/send-code` | POST | 发送验证码 | `{ phone: string }` | `{ message: string }` |
| `/api/user/verify-code` | POST | 验证验证码 | `{ phone: string, code: string }` | `{ valid: boolean, message: string }` |
| `/api/user/reset-password` | POST | 重置密码 | `{ phone: string, code: string, newPassword: string }` | `{ message: string }` |

### 3.2 核心组件

#### 3.2.1 VerificationCodeService

**文件**: `springboot/src/main/java/com/community/cfgs/service/VerificationCodeService.java`

**职责**：
- 生成并发送验证码
- 验证验证码正确性
- 清理过期验证码
- 检查发送频率限制

**存储结构**：
```java
ConcurrentHashMap<String, CodeRecord> codeStore;
```

#### 3.2.2 SmsService 接口

**文件**: `springboot/src/main/java/com/community/cfgs/service/SmsService.java`

```java
public interface SmsService {
    /**
     * 发送验证码短信
     * @param phone 手机号
     * @param code 验证码
     */
    void sendVerificationCode(String phone, String code);
}
```

#### 3.2.3 MockSmsService 实现

**文件**: `springboot/src/main/java/com/community/cfgs/service/impl/MockSmsServiceImpl.java`

- 验证码固定为 `000000`
- 仅在控制台输出日志，不发送真实短信

#### 3.2.4 CodeRecord 实体

**文件**: `springboot/src/main/java/com/community/cfgs/entity/CodeRecord.java`

```java
@Data
public class CodeRecord {
    private String phone;           // 手机号
    private String code;            // 验证码
    private LocalDateTime createTime;    // 创建时间
    private LocalDateTime expireTime;    // 过期时间
    private Integer verifyAttempts; // 验证尝试次数
    private LocalDateTime lastSendTime;  // 上次发送时间
}
```

### 3.3 UserController 新增方法

在 `UserController.java` 中添加三个新接口方法。

## 四、业务规则

| 规则 | 值 |
|-----|-----|
| 验证码有效期 | 5分钟 |
| 同一手机号发送间隔 | 60秒 |
| 最大验证尝试次数 | 5次 |
| 模拟验证码 | 000000 |
| 新密码最小长度 | 6位 |
| 密码加密方式 | BCrypt |

## 五、错误处理

| 场景 | 提示信息 |
|-----|----------|
| 手机号不存在 | "该手机号未注册" |
| 手机号格式错误 | "请输入正确的手机号格式" |
| 发送过于频繁 | "请60秒后再试" |
| 验证码过期 | "验证码已过期，请重新获取" |
| 验证码错误 | "验证码错误，还剩X次机会" |
| 验证码错误次数过多 | "验证码错误次数过多，请重新获取" |
| 两次密码不一致 | "两次输入的密码不一致" |
| 密码过短 | "密码长度不能少于6位" |

## 六、未来扩展

### 6.1 接入真实短信服务

1. 实现 `AliyunSmsServiceImpl` 或 `TencentSmsServiceImpl`
2. 修改 Spring 配置，切换 SmsService 实现
3. 无需修改 VerificationCodeService 代码

### 6.2 迁移到 Redis

1. 将 `ConcurrentHashMap` 替换为 `RedisTemplate`
2. 使用 Redis TTL 自动过期功能
3. 便于分布式部署

## 七、测试要点

1. 发送验证码60秒限制
2. 验证码5分钟过期
3. 验证码错误5次限制
4. 模拟验证码 000000 可用
5. 密码重置成功后可用新密码登录
6. 前端两步式流程交互
