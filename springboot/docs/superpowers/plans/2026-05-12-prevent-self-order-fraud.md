# 防止自接自刷功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 防止用户通过骑手身份接自己下的单并获利，以及给自己配送的订单刷评。

**Architecture:** 在接单和评价的业务逻辑中添加身份验证检查，确保骑手ID不等于订单的studentId。

**Tech Stack:** Spring Boot, MyBatis-Plus, Java

---

## 文件结构

| 文件 | 修改内容 |
|-----|---------|
| `OrderService.java` | 在 `acceptOrder()` 中添加自接单检测 |
| `ReviewService.java` | 在 `addReview()` 中添加自评价检测 |

---

### Task 1: 在 OrderService 中添加自接单检测

**Files:**
- Modify: `src/main/java/com/community/cfgs/service/OrderService.java:348-372`

- [ ] **Step 1: 修改 acceptOrder 方法，添加自接单检测**

在 `acceptOrder` 方法中，查询订单后立即添加检查逻辑：

```java
@Transactional(rollbackFor = Exception.class)
public boolean acceptOrder(String orderId, String riderId, String riderName) {
    // 先查询订单，检查是否是拼单订单
    Order targetOrder = orderMapper.selectById(orderId);
    if (targetOrder == null) {
        return false;
    }

    // 防止接自己的订单
    if (riderId.equals(targetOrder.getStudentId())) {
        throw new RuntimeException("不能接自己下的订单");
    }

    // 检查是否是拼单订单
    if (targetOrder.getGroupOrderId() != null && !targetOrder.getGroupOrderId().trim().isEmpty()) {
        // 拼单订单：接下同一拼单的所有订单
        return acceptGroupOrders(targetOrder.getGroupOrderId(), riderId, riderName);
    }

    // 普通订单：只接当前订单
    LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(Order::getId, orderId)
           .eq(Order::getStatus, "ready")
           .isNull(Order::getRiderId)
           .set(Order::getRiderId, riderId)
           .set(Order::getRiderName, riderName)
           .set(Order::getStatus, "delivering")
           .set(Order::getPickedAt, LocalDateTime.now());
    return orderMapper.update(null, wrapper) > 0;
}
```

- [ ] **Step 2: 验证修改**

确保代码编译通过，检查：
- 异常消息为中文："不能接自己下的订单"
- 检查位置在订单查询之后、拼单判断之前

- [ ] **Step 3: 提交修改**

```bash
git add src/main/java/com/community/cfgs/service/OrderService.java
git commit -m "feat(order): 防止骑手接自己的订单"
```

---

### Task 2: 在 ReviewService 中添加自评价检测

**Files:**
- Modify: `src/main/java/com/community/cfgs/service/ReviewService.java:51-68`

- [ ] **Step 1: 修改 addReview 方法，添加自评价检测**

在 `addReview` 方法中，检查是否已评价之后添加自评价检查：

```java
@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public boolean addReview(Review review) {
    // 检查是否已评价
    LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Review::getOrderId, review.getOrderId());
    if (reviewMapper.selectCount(wrapper) > 0) {
        return false;
    }

    // 防止评价自己配送的订单
    Order order = orderMapper.selectById(review.getOrderId());
    if (order != null && review.getStudentId().equals(order.getRiderId())) {
        throw new RuntimeException("不能评价自己配送的订单");
    }

    review.setId("r_" + UUID.randomUUID().toString().substring(0, 8));
    review.setCreatedAt(LocalDateTime.now());
    reviewMapper.insert(review);

    // 更新商家评分
    updateMerchantRating(review.getMerchantId());

    return true;
}
```

- [ ] **Step 2: 验证修改**

确保代码编译通过，检查：
- 异常消息为中文："不能评价自己配送的订单"
- 检查位置在重复评价检查之后、插入评价之前
- 需要注入 `OrderMapper`（已存在）

- [ ] **Step 3: 提交修改**

```bash
git add src/main/java/com/community/cfgs/service/ReviewService.java
git commit -m "feat(review): 防止用户评价自己配送的订单"
```

---

### Task 3: 手动测试验证

**Files:**
- Test: 手动测试场景

- [ ] **Step 1: 测试自己接自己的单**

1. 启动后端服务
2. 使用学生账号A下单
3. 切换到骑手角色（账号A的骑手身份）
4. 尝试接自己下的单
5. **预期结果**: 返回错误 "不能接自己下的订单"

- [ ] **Step 2: 测试正常接单**

1. 使用学生账号B下单
2. 使用账号A的骑手身份接单
3. **预期结果**: 接单成功

- [ ] **Step 3: 测试评价自己配送的订单**

1. 完成一个自己配送的订单（通过其他方式绕过或修改数据库）
2. 尝试给自己配送的订单添加评价
3. **预期结果**: 返回错误 "不能评价自己配送的订单"

- [ ] **Step 4: 测试正常评价**

1. 完成一个他人配送的订单
2. 添加评价
3. **预期结果**: 评价成功

---

## 自我审查清单

- [x] **Spec coverage**: 所有设计文档中的功能都有对应的实施任务
- [x] **Placeholder scan**: 无占位符，所有代码步骤都包含完整代码
- [x] **Type consistency**: 方法签名和类型名称与现有代码一致
