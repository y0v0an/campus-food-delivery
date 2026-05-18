# 评分系统测试指南

## 📋 测试目标

验证商家端和学生端显示的评分一致，且评分计算正确。

## 🔧 测试准备

### 1. 执行测试SQL脚本

在Navicat中打开 `docs/db/test_rating_system.sql` 并执行，这会创建：
- 2个测试商户（黄焖鸡米饭、麻辣烫）
- 5个测试学生
- 5个测试订单
- 5个初始评价（评分: 5,5,5,4,4 → 平均 4.6分）

### 2. 启动后端服务

```bash
cd springboot
mvn spring-boot:run
```

### 3. 启动前端服务

```bash
cd vue
npm run dev
```

---

## 🧪 测试场景

### 场景1: 验证初始评分显示

**步骤:**

1. 打开Navicat，查询商户评分：
   ```sql
   SELECT id, name, rating FROM merchants WHERE id = 'test_m_001';
   ```
   预期: 初始 `rating = 5.0`

2. 使用Postman/curl触发评分更新：
   ```bash
   # 添加一个评价会自动触发评分更新
   POST http://localhost:8080/api/review/add
   {
     "orderId": "test_o_001",
     "merchantId": "test_m_001",
     "studentId": "test_u_001",
     "merchantRating": 5,
     "riderRating": 5,
     "content": "测试评价"
   }
   ```

3. 再次查询商户评分，应该更新为计算值

### 场景2: 手动计算验证平均分

**在MySQL中执行:**

```sql
-- 查看所有评价和计算平均分
SELECT
    merchant_id,
    COUNT(*) as review_count,
    ROUND(AVG(merchant_rating), 1) as calculated_avg,
    (SELECT rating FROM merchants WHERE id = 'test_m_001') as stored_rating
FROM reviews
WHERE merchant_id = 'test_m_001';
```

**验证点:** `calculated_avg` 应该等于 `stored_rating`

### 场景3: 前端显示一致性测试

**步骤:**

1. **学生端验证**
   - 登录学生账号
   - 在首页找到"黄焖鸡米饭(测试店)"
   - 记录显示的评分: ______

2. **学生端商户详情**
   - 点击进入商户详情
   - 记录显示的评分: ______

3. **商家端验证**
   - 登录商家账号 (test_merchant_1)
   - 进入"经营统计"页面
   - 记录显示的评分: ______

**预期结果:** 三个评分应该完全一致！

### 场景4: 评分动态更新测试

**步骤:**

1. 添加一个新评价（通过前端或API）：
   ```bash
   POST http://localhost:8080/api/review/add
   {
     "orderId": "test_o_002",
     "merchantId": "test_m_001",
     "studentId": "test_u_002",
     "merchantRating": 1,
     "content": "差评测试"
   }
   ```

2. 立即刷新学生端和商家端，评分应该同步更新

3. 用SQL验证数据库中的评分已更新

### 场景5: 无评价默认5分测试

**步骤:**

1. 查询 `test_m_002`（麻辣烫）商户，该商户无评价
2. 前端应该显示 5.0 分
3. 数据库查询：
   ```sql
   SELECT rating FROM merchants WHERE id = 'test_m_002';
   ```
   预期: `5.0`

---

## 📊 测试记录表

| 测试项 | 数据库评分 | 学生端首页 | 学生端详情 | 商家端统计 | 结果 |
|-------|----------|-----------|-----------|-----------|------|
| 初始状态 | ___ | ___ | ___ | ___ | |
| 添加好评后 | ___ | ___ | ___ | ___ | |
| 添加差评后 | ___ | ___ | ___ | ___ | |
| 无评价商户 | 5.0 | 5.0 | 5.0 | 5.0 | |

---

## 🐛 常见问题排查

### 问题1: 商家端评分不更新

**检查:**
```javascript
// vue/src/views/Merchant/Statistics.vue 第168-169行
const merchant = await request.get(`/merchant/${merchantId}`)
const realRating = merchant?.rating || 5.0
```

确保代码已更新，不再使用硬编码的 `4.8`

### 问题2: 评分计算不正确

**检查后端代码:**
```java
// ReviewService.java calculateAverageRating() 方法
BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
```

确保使用的是 `HALF_UP` 四舍五入模式

### 问题3: 数据库评分未自动更新

**检查:**
- 添加评价后是否调用了 `updateMerchantRating()`
- 检查是否有事务回滚

---

## ✅ 测试完成标准

- [ ] 数据库评分计算正确（与评价平均值一致）
- [ ] 学生端首页显示评分正确
- [ ] 学生端商户详情显示评分正确
- [ ] 商家端统计页显示评分正确
- [ ] 三个端显示的评分完全一致
- [ ] 无评价商户默认显示5.0分
- [ ] 添加新评价后评分实时更新
