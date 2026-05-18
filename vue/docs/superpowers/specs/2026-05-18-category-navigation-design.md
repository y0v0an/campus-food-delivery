# 分类跳转功能设计文档

**日期**: 2026-05-18
**作者**: Claude & 用户
**状态**: 已批准

## 概述

为学生端首页的分类标签添加跳转功能，用户点击分类标签后可以浏览该分类下的商家列表。

## 分类标签配置

| 标签名称 | 图标 | 路由参数 | 关键词映射 |
|---------|------|----------|-----------|
| 热门推荐 | `lucide:flame` | `hot` | 特殊逻辑：按 monthSales 降序 |
| 拼单专区 | `lucide:users` | - | 跳转至 `/student/group-list` |
| 下午茶 | `lucide:coffee` | `dessert-drinks` | 奶茶、咖啡、饮品、下午茶 |
| 新鲜水果 | `lucide:apple` | `fruits` | 水果、鲜果 |
| 甜点 | `lucide:cake` | `desserts` | 面包店、蛋糕、甜品、烘焙、甜点 |
| 小吃简餐 | `lucide:utensils` | `snacks` | 小吃、快餐、简餐、炸鸡、汉堡 |
| 正餐 | `lucide:bowl` | `dining` | 中餐、西餐、料理、餐厅、食堂 |

## 新建页面：CategoryList.vue

### 路由配置

```javascript
{
  path: 'category/:type',
  name: 'CategoryList',
  component: () => import('@/views/Student/CategoryList.vue')
}
```

### 页面功能

1. **根据路由参数过滤商家**
   - 从 `route.params.type` 获取分类类型
   - 使用关键词映射表匹配商家的 `categories` 字段
   - 调用 `parseMerchantCategories()` 解析商家分类

2. **热门推荐特殊处理**
   - `type === 'hot'` 时，按 `monthSales` 降序排列
   - 不进行分类过滤，显示所有商家

3. **UI 复用**
   - 复用 HomePage.vue 中的商家列表样式
   - 保持一致的用户体验

4. **支持搜索**
   - 复用现有的搜索功能

### 关键词映射定义

```javascript
const CATEGORY_KEYWORDS = {
  'dessert-drinks': ['奶茶', '咖啡', '饮品', '下午茶'],
  'fruits': ['水果', '鲜果'],
  'desserts': ['面包店', '蛋糕', '甜品', '烘焙', '甜点'],
  'snacks': ['小吃', '快餐', '简餐', '炸鸡', '汉堡'],
  'dining': ['中餐', '西餐', '料理', '餐厅', '食堂']
}
```

## HomePage.vue 修改

### 添加新标签

在分类标签区域添加：
- 小吃简餐（`lucide:utensils`，灰色系）
- 正餐（`lucide:bowl`，红色系）

### 添加跳转方法

```javascript
const goToCategory = (type) => {
  if (type === 'group') {
    router.push('/student/group-list')
  } else {
    router.push(`/student/category/${type}`)
  }
}
```

### 模板修改

为每个分类标签添加点击事件：
```vue
<div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
     @click="goToCategory('desserts')">
  <!-- 图标和文字 -->
</div>
```

## 数据流

```
用户点击分类标签
  ↓
goToCategory(type) 被调用
  ↓
路由跳转到 /student/category/:type
  ↓
CategoryList.vue 组件加载
  ↓
根据 type 获取对应关键词
  ↓
调用 /merchant/list 获取所有商家
  ↓
前端过滤：使用 parseMerchantCategories + 关键词匹配
  ↓
渲染商家列表
```

## API 依赖

- 复用现有 `GET /merchant/list` 接口
- 无需新增后端接口

## 实现文件清单

1. **新建**: `vue/src/views/Student/CategoryList.vue`
2. **修改**: `vue/src/router/index.js` - 添加路由
3. **修改**: `vue/src/views/Student/HomePage.vue` - 添加标签和跳转逻辑
