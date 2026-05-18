# 防止自接自刷功能设计文档

**日期**: 2026-05-12
**作者**: Claude
**状态**: 设计中

## 一、背景

当前系统允许学生申请成为骑手，骑手信息通过 `RiderInfo.userId` 关联到用户表（`role=student` + `isRider=true`）。这导致以下业务漏洞：

1. **自己接自己的单** - 同一用户可以下单后，切换到骑手角色接自己的单，获取配送费
2. **自己给自己好评** - 同一用户可以给自己配送的订单打高分，刷高骑手评分

## 二、目标

防止用户通过骑手身份接自己下的单并获利，以及给自己配送的订单刷评。

## 三、方案

### 3.1 接单防刷

在 `OrderService.acceptOrder()` 方法中添加验证逻辑：

```java
public boolean acceptOrder(String orderId, String riderId, String riderName) {
    Order targetOrder = orderMapper.selectById(orderId);
    if (targetOrder == null) {
        return false;
    }

    // 防止接自己的订单
    if (riderId.equals(targetOrder.getStudentId())) {
        throw new RuntimeException("不能接自己下的订单");
    }

    // 后续逻辑...
}
```

### 3.2 评价防刷

在 `ReviewService.addReview()` 方法中添加验证逻辑：

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

    // 后续逻辑...
}
```

### 3.3 前端优化（可选）

- 骑手订单池：过滤掉自己下的订单，不显示在可接单列表中
- 评价页面：如果是自己配送的订单，禁用骑手评分

## 四、修改文件清单

| 文件 | 修改内容 |
|-----|---------|
| `OrderService.java` | 在 `acceptOrder()` 中添加自接单检测 |
| `ReviewService.java` | 在 `addReview()` 中添加自评价检测 |
| `OrderController.java` | 可能需要调整异常处理 |
| `ReviewController.java` | 可能需要调整异常处理 |

## 五、测试场景

| 场景 | 输入 | 预期结果 |
|-----|------|---------|
| 自己接自己的单 | 学生A下单，用A的骑手账号接单 | 抛出异常："不能接自己下的订单" |
| 正常接单 | 学生A下单，学生B的骑手账号接单 | 接单成功 |
| 评价自己配送的订单 | 自己配送的订单，自己给骑手评分 | 抛出异常："不能评价自己配送的订单" |
| 正常评价 | 他人配送的订单，正常评价 | 评价成功 |

## 六、兼容性

- 现有订单不受影响
- 不影响骑手接其他学生的订单
- 不影响学生评价他人配送的订单
