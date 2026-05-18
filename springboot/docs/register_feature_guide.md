# 校园外卖配送系统 - 注册功能实现说明

## 功能概述

本系统实现了三种角色的用户注册功能：
- 学生注册
- 商家注册
- 骑手注册

## 技术实现

### 后端实现

#### 1. 新增文件

| 文件路径 | 说明 |
|----------|------|
| `dto/StudentRegisterRequest.java` | 学生注册请求DTO |
| `dto/MerchantRegisterRequest.java` | 商家注册请求DTO |
| `dto/RiderRegisterRequest.java` | 骑手注册请求DTO |
| `dto/RegisterResponse.java` | 注册响应DTO |
| `service/RegisterService.java` | 注册服务类 |
| `controller/RegisterController.java` | 注册控制器 |

#### 2. 修改文件

| 文件路径 | 修改内容 |
|----------|----------|
| `pom.xml` | 添加spring-security-crypto依赖 |
| `entity/RiderInfo.java` | 添加身份证、车辆类型、车牌号、状态字段 |
| `service/UserService.java` | 支持BCrypt密码验证 |

#### 3. API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/register/student` | POST | 学生注册 |
| `/api/register/merchant` | POST | 商家注册 |
| `/api/register/rider` | POST | 骑手注册 |
| `/api/register/check-username` | GET | 检查用户名是否可用 |
| `/api/register/check-phone` | GET | 检查手机号是否已注册 |

### 前端实现

#### 1. 新增文件

| 文件路径 | 说明 |
|----------|------|
| `views/Login/RegisterPage.vue` | 注册页面 |

#### 2. 修改文件

| 文件路径 | 修改内容 |
|----------|----------|
| `router/index.js` | 添加注册路由 `/register` |
| `views/Login/LoginPage.vue` | 添加"立即注册"链接 |

## 数据库变更

### 执行SQL脚本

```bash
# 执行骑手信息表扩展
mysql -u root -p your_database < docs/db/add_rider_info_fields.sql
```

### SQL脚本内容

```sql
-- 骑手信息表扩展字段
ALTER TABLE rider_info
ADD COLUMN id_card VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
ADD COLUMN vehicle_type VARCHAR(50) DEFAULT NULL COMMENT '车辆类型',
ADD COLUMN plate_number VARCHAR(20) DEFAULT NULL COMMENT '车牌号',
ADD COLUMN status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态';
```

## 部署步骤

### 1. 后端部署

```bash
# 1. 执行数据库脚本
mysql -u root -p cfgs < docs/db/add_rider_info_fields.sql

# 2. 重新编译项目
mvn clean package -DskipTests

# 3. 重启Spring Boot应用
java -jar target/cfgs-1.0.0.jar
```

### 2. 前端部署

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器
npm run dev
```

## 功能特性

### 安全性

- 密码使用BCrypt加密存储
- 密码强度验证（6-20位，包含字母和数字）
- 手机号和用户名唯一性校验
- 敏感信息不在日志中输出

### 表单验证

| 字段 | 验证规则 |
|------|----------|
| 用户名 | 3-20位，唯一 |
| 密码 | 6-20位，包含字母和数字 |
| 手机号 | 11位，1开头 |
| 身份证号 | 18位，格式正确 |
| 车牌号 | 格式正确 |

### 业务逻辑

- 学生注册：创建用户（角色为student）
- 商家注册：创建用户（角色为merchant）+ 商家信息
- 骑手注册：如果手机号已存在学生账号，则升级为骑手；否则创建新账号

## 测试账号

注册完成后，可以使用注册的账号直接登录。

## 注意事项

1. 商家和骑手注册后默认为待审核状态，需要管理员审核后才能正常使用
2. 骑手注册时如果手机号已存在学生账号，会将该账号升级为骑手
3. 密码验证兼容BCrypt加密和明文密码（旧账号）
