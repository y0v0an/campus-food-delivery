# 分类跳转功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现首页分类标签的跳转功能，用户点击分类标签后浏览该分类下的商家列表

**Architecture:** 前端基于商家 `categories` 字段进行关键词匹配过滤，复用现有 `/merchant/list` API，新建统一分类页面 `CategoryList.vue`

**Tech Stack:** Vue 3, Vue Router, Pinia, Tailwind CSS, iconify-icon

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `vue/src/views/Student/CategoryList.vue` | 新建 | 分类商家列表页面，根据分类类型过滤商家 |
| `vue/src/router/index.js` | 修改 | 添加 `/student/category/:type` 路由 |
| `vue/src/views/Student/HomePage.vue` | 修改 | 添加两个新分类标签，为所有标签添加点击跳转 |

---

### Task 1: 添加分类列表路由

**Files:**
- Modify: `vue/src/router/index.js`

- [ ] **Step 1: 在学生端路由 children 中添加 CategoryList 路由**

在 `Student` 路由的 `children` 数组中，在 `rider-apply` 路由之后添加：

```javascript
{
  path: 'category/:type',
  name: 'CategoryList',
  component: () => import('@/views/Student/CategoryList.vue')
}
```

完整插入位置参考：在第 115 行 `}` 和第 116 行 `]` 之间

- [ ] **Step 2: 保存文件**

```bash
# 保存文件，无需 commit（等待后续任务一起提交）
```

---

### Task 2: 创建 CategoryList.vue 页面

**Files:**
- Create: `vue/src/views/Student/CategoryList.vue`

- [ ] **Step 1: 创建 CategoryList.vue 基础结构**

```vue
<template>
  <div class="min-h-full pb-6">
    <!-- 顶部标题栏 -->
    <div class="sticky top-0 z-20 bg-white/95 backdrop-blur-sm px-6 pt-4 pb-4 border-b border-gray-100">
      <div class="flex items-center gap-3">
        <iconify-icon
          class="text-xl text-gray-600 cursor-pointer"
          icon="lucide:arrow-left"
          @click="goBack"
        ></iconify-icon>
        <h1 class="text-lg font-black text-gray-800">{{ categoryTitle }}</h1>
      </div>

      <!-- 搜索框 -->
      <div class="relative mt-4">
        <iconify-icon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl" icon="lucide:search"></iconify-icon>
        <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索商家"
            class="w-full pl-12 pr-4 py-3 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
            @input="handleSearch"
        />
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="px-6 py-4">
      <div v-if="filteredMerchants.length > 0" class="space-y-4">
        <div
            v-for="merchant in filteredMerchants"
            :key="merchant.id"
            class="bg-white border border-gray-100 rounded-3xl p-4 shadow-sm flex gap-4 cursor-pointer active:scale-[0.98] transition-transform"
            @click="goToMerchant(merchant.id)"
        >
          <div class="w-20 h-20 bg-gray-100 rounded-2xl overflow-hidden flex-shrink-0">
            <img :src="getImageUrl(merchant.logo)" :alt="merchant.name" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1">
            <div class="flex justify-between items-start">
              <h3 class="font-black text-gray-800">{{ merchant.name }}</h3>
              <span
                  class="text-[10px] px-2 py-0.5 rounded-full font-bold"
                  :class="merchant.isOpen ? 'bg-green-100 text-green-600' : 'bg-gray-200 text-gray-500'"
              >
                {{ merchant.isOpen ? '营业中' : '休息中' }}
              </span>
            </div>
            <div class="flex items-center gap-2 mt-1">
              <div class="flex items-center text-yellow-500 text-xs">
                <iconify-icon class="fill-current" icon="lucide:star"></iconify-icon>
                <span class="font-bold ml-1">{{ merchant.rating }}</span>
              </div>
              <span class="text-gray-300 text-xs">|</span>
              <span class="text-gray-500 text-xs">月售{{ merchant.monthSales }}</span>
            </div>
            <div class="mt-2 flex gap-2 flex-wrap">
              <span class="text-[10px] bg-orange-50 text-orange-600 border border-orange-100 px-2 py-0.5 rounded-lg font-bold">{{ merchant.deliveryTime }}</span>
              <span class="text-[10px] bg-blue-50 text-blue-600 border border-blue-100 px-2 py-0.5 rounded-lg font-bold">配送¥{{ merchant.deliveryFee }}</span>
              <span class="text-[10px] bg-gray-50 text-gray-600 border border-gray-100 px-2 py-0.5 rounded-lg font-bold">¥{{ merchant.minOrder }}起送</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-12">
        <iconify-icon class="text-6xl text-gray-300 mx-auto" icon="lucide:store"></iconify-icon>
        <p class="text-gray-400 text-sm mt-4">暂无相关商家</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'
import { parseMerchantCategories } from '@/utils/merchantCategories'

const router = useRouter()
const route = useRoute()

// 分类关键词映射
const CATEGORY_KEYWORDS = {
  'hot': [], // 特殊处理：不过滤，按销量排序
  'dessert-drinks': ['奶茶', '咖啡', '饮品', '下午茶'],
  'fruits': ['水果', '鲜果'],
  'desserts': ['面包店', '蛋糕', '甜品', '烘焙', '甜点'],
  'snacks': ['小吃', '快餐', '简餐', '炸鸡', '汉堡'],
  'dining': ['中餐', '西餐', '料理', '餐厅', '食堂']
}

// 分类标题映射
const CATEGORY_TITLES = {
  'hot': '热门推荐',
  'dessert-drinks': '下午茶',
  'fruits': '新鲜水果',
  'desserts': '甜点',
  'snacks': '小吃简餐',
  'dining': '正餐'
}

// 所有商家
const merchants = ref([])

// 搜索关键词
const searchKeyword = ref('')

// 当前分类类型
const categoryType = computed(() => route.params.type || '')

// 分类标题
const categoryTitle = computed(() => {
  return CATEGORY_TITLES[categoryType.value] || '分类'
})

// 获取当前分类的关键词
const categoryKeywords = computed(() => {
  return CATEGORY_KEYWORDS[categoryType.value] || []
})

// 过滤后的商家列表
const filteredMerchants = computed(() => {
  let result = [...merchants.value]

  // 热门推荐：按销量排序
  if (categoryType.value === 'hot') {
    result.sort((a, b) => Number(b.monthSales || 0) - Number(a.monthSales || 0))
  } else {
    // 其他分类：根据关键词过滤
    const keywords = categoryKeywords.value
    result = result.filter(merchant => {
      const categories = parseMerchantCategories(merchant.categories)
      return categories.some(cat =>
        keywords.some(keyword => cat.includes(keyword))
      )
    })
  }

  // 搜索过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    result = result.filter(m =>
      m.name.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到商家详情
const goToMerchant = (merchantId) => {
  router.push(`/student/merchant/${merchantId}`)
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑在 computed 中处理
}

// 加载商家列表
const loadMerchants = async () => {
  try {
    const result = await request.get('/merchant/list')
    merchants.value = result || []
  } catch (error) {
    console.error('加载商家列表失败:', error)
    merchants.value = []
  }
}

onMounted(() => {
  loadMerchants()
})
</script>

<style scoped>
/* 无需额外样式，使用 Tailwind */
</style>
```

- [ ] **Step 2: 保存文件**

```bash
# 保存文件，等待后续任务一起提交
```

---

### Task 3: 修改 HomePage.vue 添加分类跳转功能

**Files:**
- Modify: `vue/src/views/Student/HomePage.vue`

- [ ] **Step 1: 在 script setup 中添加 goToCategory 方法**

在 `const goToGroupList = () => { ... }` 函数之后，`onMounted` 之前添加：

```javascript
// 跳转到分类页面
const goToCategory = (type) => {
  if (type === 'group') {
    router.push('/student/group-list')
  } else {
    router.push(`/student/category/${type}`)
  }
}
```

- [ ] **Step 2: 为现有分类标签添加点击事件和 cursor-pointer 类**

找到"热门推荐"标签的 `div`（约第 33 行），修改：

```vue
<!-- 修改前 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1">

<!-- 修改后 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
     @click="goToCategory('hot')">
```

找到"拼单专区"标签的 `div`（约第 39 行），修改：

```vue
<!-- 修改前 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1">

<!-- 修改后 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
     @click="goToCategory('group')">
```

找到"下午茶"标签的 `div`（约第 45 行），修改：

```vue
<!-- 修改前 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1">

<!-- 修改后 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
     @click="goToCategory('dessert-drinks')">
```

找到"新鲜水果"标签的 `div`（约第 51 行），修改：

```vue
<!-- 修改前 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1">

<!-- 修改后 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
     @click="goToCategory('fruits')">
```

找到"甜点"标签的 `div`（约第 57 行），修改：

```vue
<!-- 修改前 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1">

<!-- 修改后 -->
<div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
     @click="goToCategory('desserts')">
```

- [ ] **Step 3: 在"甜点"标签后添加两个新分类标签**

在"甜点"标签的 `div` 结束后（约第 62 行），添加：

```vue
        <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
             @click="goToCategory('snacks')">
          <div class="w-14 h-14 bg-gray-100 rounded-2xl flex items-center justify-center">
            <iconify-icon class="text-2xl text-gray-600" icon="lucide:utensils"></iconify-icon>
          </div>
          <span class="text-xs font-bold text-gray-600">小吃简餐</span>
        </div>
        <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
             @click="goToCategory('dining')">
          <div class="w-14 h-14 bg-red-100 rounded-2xl flex items-center justify-center">
            <iconify-icon class="text-2xl text-red-600" icon="lucide:bowl"></iconify-icon>
          </div>
          <span class="text-xs font-bold text-gray-600">正餐</span>
        </div>
```

- [ ] **Step 4: 保存文件**

```bash
# 保存文件
```

---

### Task 4: 提交代码

- [ ] **Step 1: 提交所有修改**

```bash
cd E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue
git add src/router/index.js src/views/Student/CategoryList.vue src/views/Student/HomePage.vue
git commit -m "feat: 添加分类跳转功能

- 新增 CategoryList.vue 分类商家列表页面
- 支持按关键词过滤商家（甜点、下午茶、水果、小吃、正餐）
- 热门推荐按销量排序
- 首页分类标签支持点击跳转
- 新增小吃简餐和正餐两个分类标签
"
```

---

### Task 5: 测试验证

- [ ] **Step 1: 启动开发服务器**

```bash
cd E:\Desktop\校园外卖配送系统-增加拼单优惠券\校园外卖配送系统\vue
npm run dev
```

- [ ] **Step 2: 手动测试清单**

1. 打开首页，点击"热门推荐"标签 → 应跳转到分类页面，显示按销量排序的商家
2. 点击"拼单专区"标签 → 应跳转到 `/student/group-list`
3. 点击"甜点"标签 → 应跳转到分类页面，只显示包含面包店、蛋糕等关键词的商家
4. 点击"下午茶"标签 → 应跳转到分类页面，只显示包含奶茶、咖啡等关键词的商家
5. 点击"新鲜水果"标签 → 应跳转到分类页面，只显示包含水果关键词的商家
6. 点击"小吃简餐"标签 → 应跳转到分类页面，只显示包含小吃、快餐等关键词的商家
7. 点击"正餐"标签 → 应跳转到分类页面，只显示包含中餐、西餐等关键词的商家
8. 在分类页面测试搜索功能 → 输入关键词应过滤商家名称
9. 点击商家卡片 → 应跳转到商家详情页
10. 点击返回按钮 → 应返回上一页

- [ ] **Step 3: 完成测试**

如果所有测试通过，功能实现完成。如有问题，记录并修复。
