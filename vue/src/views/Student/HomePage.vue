<template>
  <div class="min-h-full pb-6">
    <!-- 顶部搜索栏 -->
    <div class="sticky top-0 z-20 bg-white/95 backdrop-blur-sm px-6 pt-4 pb-4 border-b border-gray-100">
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-1">
          <iconify-icon class="text-orange-500 text-xl" icon="lucide:map-pin"></iconify-icon>
          <span class="font-bold text-gray-800">上海电机学院</span>
          <iconify-icon class="text-gray-400" icon="lucide:chevron-down"></iconify-icon>
        </div>
        <div class="flex gap-3">
          <iconify-icon class="text-2xl text-gray-600" icon="lucide:bell"></iconify-icon>
          <iconify-icon class="text-2xl text-gray-600" icon="lucide:search"></iconify-icon>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="relative">
        <iconify-icon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-xl" icon="lucide:search"></iconify-icon>
        <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索商家或菜品"
            class="w-full pl-12 pr-4 py-3 bg-gray-50 border-2 border-transparent focus:border-orange-500 focus:bg-white rounded-2xl outline-none transition-all text-gray-800"
            @input="handleSearch"
        />
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="px-6 py-4">
      <div class="category-scroll-wrapper">
        <div class="flex gap-4 overflow-x-auto overflow-y-hidden custom-scrollbar py-2 scroll-smooth" ref="categoryScrollRef">
          <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
               @click="goToCategory('hot')">
            <div class="w-14 h-14 bg-orange-100 rounded-2xl flex items-center justify-center">
              <iconify-icon class="text-2xl text-orange-500" icon="lucide:flame"></iconify-icon>
            </div>
            <span class="text-xs font-bold text-gray-600">热门推荐</span>
          </div>
          <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
               @click="goToCategory('group')">
            <div class="w-14 h-14 bg-yellow-100 rounded-2xl flex items-center justify-center">
              <iconify-icon class="text-2xl text-yellow-600" icon="lucide:users"></iconify-icon>
            </div>
            <span class="text-xs font-bold text-gray-600">拼单专区</span>
          </div>
          <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
               @click="goToCategory('dining')">
            <div class="w-14 h-14 bg-red-100 rounded-2xl flex items-center justify-center">
              <iconify-icon class="text-2xl text-red-600" icon="lucide:chef-hat"></iconify-icon>
            </div>
            <span class="text-xs font-bold text-gray-600">正餐</span>
          </div>
          <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
               @click="goToCategory('snacks')">
            <div class="w-14 h-14 bg-gray-100 rounded-2xl flex items-center justify-center">
              <iconify-icon class="text-2xl text-gray-600" icon="lucide:utensils"></iconify-icon>
            </div>
            <span class="text-xs font-bold text-gray-600">小吃简餐</span>
          </div>
          <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
               @click="goToCategory('dessert-drinks')">
            <div class="w-14 h-14 bg-green-100 rounded-2xl flex items-center justify-center">
              <iconify-icon class="text-2xl text-green-600" icon="lucide:coffee"></iconify-icon>
            </div>
            <span class="text-xs font-bold text-gray-600">下午茶</span>
          </div>
          <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
               @click="goToCategory('fruits')">
            <div class="w-14 h-14 bg-blue-100 rounded-2xl flex items-center justify-center">
              <iconify-icon class="text-2xl text-blue-600" icon="lucide:apple"></iconify-icon>
            </div>
            <span class="text-xs font-bold text-gray-600">新鲜水果</span>
          </div>
          <div class="flex flex-col items-center flex-shrink-0 gap-1 cursor-pointer"
               @click="goToCategory('desserts')">
            <div class="w-14 h-14 bg-purple-100 rounded-2xl flex items-center justify-center">
              <iconify-icon class="text-2xl text-purple-600" icon="lucide:cake"></iconify-icon>
            </div>
            <span class="text-xs font-bold text-gray-600">甜点</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Banner -->
    <div class="px-6 py-2">
      <div class="h-32 bg-gradient-to-r from-orange-500 to-orange-600 rounded-3xl relative overflow-hidden flex items-center p-6 text-white shadow-lg shadow-orange-200">
        <div class="relative z-10">
          <h3 class="text-xl font-black italic">拼单立减 15</h3>
          <p class="text-xs opacity-90 mt-1">集结寝室友，美味不用等</p>
          <button class="mt-3 bg-white text-orange-600 text-[10px] font-black px-4 py-1.5 rounded-full uppercase tracking-tighter hover:bg-orange-50 transition-all" @click="goToGroupList">立即参与</button>
        </div>
        <img alt="Group eating illustration" class="absolute right-[-20px] bottom-[-10px] w-40 h-40 object-contain opacity-40" src="https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=400&fit=crop"/>
      </div>
    </div>

    <!-- 推荐菜品 -->
    <div class="px-6 py-4" v-if="recommendedDishes.length">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-black text-gray-800">🔥 热门推荐</h2>
        <span class="text-xs text-orange-500 font-bold">查看更多</span>
      </div>
      <div class="flex gap-4 overflow-x-auto custom-scrollbar pb-2">
        <div
            v-for="dish in recommendedDishes"
            :key="dish.id"
            class="flex-shrink-0 w-28 cursor-pointer active:scale-95 transition-transform"
            @click="goToMerchant(dish.merchantId)"
        >
          <img :src="getImageUrl(dish.image)" :alt="dish.name" class="w-28 h-28 rounded-2xl object-cover shadow-md" />
          <div class="mt-2">
            <span class="text-sm font-bold text-gray-800 block truncate">{{ dish.name }}</span>
            <span class="text-sm font-black text-orange-500">¥{{ dish.price }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门拼单 -->
    <div class="px-6 py-4" v-if="hotGroupOrders.length">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-black text-gray-800">👥 热门拼单</h2>
        <span class="text-xs text-orange-500 font-bold">查看全部</span>
      </div>
      <div class="flex gap-4 overflow-x-auto custom-scrollbar pb-2">
        <div
            v-for="go in hotGroupOrders"
            :key="go.id"
            class="flex-shrink-0 w-28 cursor-pointer active:scale-95 transition-transform"
            @click="goToMerchant(go.merchantId)"
        >
          <img :src="getImageUrl(go.dishImage)" :alt="go.dishName" class="w-28 h-28 rounded-2xl object-cover shadow-md" />
          <div class="mt-2">
            <span class="text-sm font-bold text-gray-800 block truncate">{{ go.dishName }}</span>
            <div class="flex items-center gap-1 mt-1">
              <span class="text-xs font-bold text-gray-500">{{ go.currentCount }}/{{ go.targetCount }}人</span>
            </div>
            <div class="price-section">
              <span class="current-price">¥{{ go.unitPrice }}</span>
              <span v-if="go.originalPrice && go.originalPrice > go.unitPrice"
                    class="original-price">
                ¥{{ go.originalPrice }}
              </span>
              <span v-if="go.discountRate" class="discount-tag">
                {{ (go.discountRate * 10).toFixed(1) }}折
              </span>
            </div>
            <div class="delivery-hint mt-1">
              <span class="text-[10px] text-blue-500">配送约¥{{ calculateGroupDeliveryFee(go) }}/人</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="showSearchResults" class="px-6 py-4">
      <h3 class="text-base font-bold text-gray-800 mb-3">
        {{ groupedSearchResults.length > 0 ? `搜索结果` : '未找到匹配的商家或菜品' }}
      </h3>

      <!-- 按商家分组显示 -->
      <div class="space-y-4">
        <div
            v-for="group in groupedSearchResults"
            :key="group.merchant.id"
            class="bg-white border border-gray-100 rounded-2xl overflow-hidden shadow-sm"
        >
          <!-- 商家头部 -->
          <div
              class="p-3 border-b border-gray-100 flex gap-3 cursor-pointer bg-orange-50/50"
              @click="goToMerchant(group.merchant.id)"
          >
            <div class="w-14 h-14 bg-gray-100 rounded-xl overflow-hidden flex-shrink-0">
              <img :src="getImageUrl(group.merchant.logo)" :alt="group.merchant.name" class="w-full h-full object-cover" />
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <h4 class="font-bold text-gray-800">{{ group.merchant.name }}</h4>
                <span v-if="group.isMerchantMatch" class="text-[10px] bg-orange-100 text-orange-600 px-2 py-0.5 rounded-full font-bold">匹配</span>
                <span
                    class="text-[10px] px-2 py-0.5 rounded-full font-bold"
                    :class="group.merchant.isOpen ? 'bg-green-100 text-green-600' : 'bg-gray-200 text-gray-500'"
                >
                  {{ group.merchant.isOpen ? '营业中' : '休息中' }}
                </span>
              </div>
              <div class="flex items-center gap-2 mt-1 text-xs">
                <div class="flex items-center text-yellow-500">
                  <iconify-icon class="fill-current" icon="lucide:star"></iconify-icon>
                  <span class="font-bold ml-1">{{ group.merchant.rating }}</span>
                </div>
                <span class="text-gray-300">|</span>
                <span class="text-gray-500">{{ group.merchant.deliveryTime }}</span>
                <span class="text-gray-300">|</span>
                <span class="text-gray-500">配送¥{{ group.merchant.deliveryFee }}</span>
              </div>
            </div>
            <div class="flex items-center">
              <iconify-icon class="text-gray-400" icon="lucide:chevron-right"></iconify-icon>
            </div>
          </div>

          <!-- 该商家的匹配菜品 -->
          <div v-if="group.matchedDishes.length > 0" class="p-3 bg-gray-50/50">
            <div class="text-xs text-gray-500 mb-2">匹配的菜品 ({{ group.matchedDishes.length }})</div>
            <div class="grid grid-cols-2 gap-2">
              <div
                  v-for="dish in group.matchedDishes"
                  :key="dish.id"
                  class="bg-white border border-gray-100 rounded-xl p-2 flex gap-2 cursor-pointer active:scale-[0.98] transition-transform"
                  @click.stop="goToMerchant(group.merchant.id)"
              >
                <div class="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                  <img :src="getImageUrl(dish.image)" :alt="dish.name" class="w-full h-full object-cover" />
                </div>
                <div class="flex-1 min-w-0 flex flex-col justify-center">
                  <h5 class="text-sm font-bold text-gray-800 truncate">{{ dish.name }}</h5>
                  <span class="text-sm font-black text-orange-500">¥{{ dish.price }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 无搜索结果 -->
      <div v-if="groupedSearchResults.length === 0 && !isSearching" class="text-center py-12">
        <p class="text-gray-400 text-sm">未找到匹配的商家或菜品</p>
      </div>
    </div>

    <!-- 商家列表（无搜索时显示） -->
    <div v-else class="merchant-list-section">
      <div class="section-header">
        <h2 class="section-title">🏪 附近校园食堂 & 商家</h2>
        <span class="section-sort">按距离排序</span>
      </div>

      <MerchantCardMagazine
          v-for="merchant in merchants"
          :key="merchant.id"
          :merchant="merchant"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/axios/request'
import { getImageUrl } from '@/utils/imageUrl'
import { parseMerchantCategories } from '@/utils/merchantCategories'
import MerchantCardMagazine from '@/components/student/MerchantCardMagazine.vue'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 商家列表
const merchants = ref([])

// 所有菜品（用于搜索）
const allDishes = ref([])

// 搜索结果
const searchResults = ref({ merchants: [], dishes: [] })
const isSearching = ref(false)

// 推荐菜品
const recommendedDishes = ref([])
const hotGroupOrders = ref([])

// 过滤后的商家列表（无搜索时使用）
const filteredMerchants = computed(() => {
  // 有搜索关键词时，使用搜索结果
  if (searchKeyword.value) return searchResults.value.merchants
  return merchants.value
})

// 是否显示搜索结果
const showSearchResults = computed(() => {
  return searchKeyword.value.trim().length > 0
})

// 将搜索结果按商家分组
const groupedSearchResults = computed(() => {
  const merchantMap = new Map()

  // 添加匹配的商家
  searchResults.value.merchants.forEach(merchant => {
    merchantMap.set(merchant.id, {
      merchant,
      matchedDishes: [],
      isMerchantMatch: true
    })
  })

  // 将匹配的菜品按商家分组
  searchResults.value.dishes.forEach(dish => {
    if (!merchantMap.has(dish.merchantId)) {
      // 需要获取商家信息
      const merchant = merchants.value.find(m => m.id === dish.merchantId)
      if (merchant) {
        merchantMap.set(dish.merchantId, {
          merchant,
          matchedDishes: [],
          isMerchantMatch: false
        })
      }
    }
    const group = merchantMap.get(dish.merchantId)
    if (group) {
      group.matchedDishes.push(dish)
    }
  })

  // 转换为数组，商家匹配的排在前面
  return Array.from(merchantMap.values())
    .filter(g => g.matchedDishes.length > 0 || g.isMerchantMatch)
    .sort((a, b) => {
      if (a.isMerchantMatch && !b.isMerchantMatch) return -1
      if (!a.isMerchantMatch && b.isMerchantMatch) return 1
      return 0
    })
})

// 搜索处理 - 同时搜索商家和菜品
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()

  if (!keyword) {
    isSearching.value = false
    searchResults.value = { merchants: [], dishes: [] }
    return
  }

  isSearching.value = true

  try {
    // 搜索商家
    const merchantResult = await request.get('/merchant/search', { params: { keyword } })
    const matchedMerchants = merchantResult || []

    // 搜索菜品 - 从所有菜品中匹配
    const matchedDishes = allDishes.value.filter(dish =>
      dish.name.toLowerCase().includes(keyword.toLowerCase())
    )

    searchResults.value = {
      merchants: matchedMerchants,
      dishes: matchedDishes
    }
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = { merchants: [], dishes: [] }
  } finally {
    isSearching.value = false
  }
}

// 加载所有菜品（用于搜索）
const loadAllDishes = async () => {
  try {
    // 从每个商家获取菜品列表
    const merchantList = await request.get('/merchant/list')
    const dishPromises = merchantList.map(m =>
      request.get(`/dish/all/${m.id}`).catch(() => [])
    )
    const dishArrays = await Promise.all(dishPromises)

    // 合并所有菜品，并添加商家信息
    allDishes.value = dishArrays.flat().map(dish => ({
      ...dish,
      merchantName: merchantList.find(m => m.id === dish.merchantId)?.name || ''
    }))
  } catch (error) {
    console.error('加载菜品失败:', error)
    allDishes.value = []
  }
}

// 跳转到商家详情
const goToMerchant = (merchantId) => {
  router.push(`/student/merchant/${merchantId}`)
}

// 加载商家列表及菜品
const loadMerchants = async () => {
  try {
    const result = await request.get('/merchant/list')
    const merchantsWithData = result || []

    // 为每个商家加载前3个菜品
    const dishPromises = merchantsWithData.map(async (merchant) => {
      try {
        const dishes = await request.get(`/dish/list/${merchant.id}`)
        return {
          ...merchant,
          dishes: (dishes || []).slice(0, 3) // 只取前3个
        }
      } catch {
        return {
          ...merchant,
          dishes: []
        }
      }
    })

    merchants.value = await Promise.all(dishPromises)
  } catch (error) {
    console.error('加载商家列表失败:', error)
  }
}

// 加载推荐菜品
const loadRecommendDishes = async () => {
  try {
    const result = await request.get('/dish/recommend')
    recommendedDishes.value = result || []
  } catch (error) {
    console.error('加载推荐菜品失败:', error)
  }
}

const loadHotGroupOrders = async () => {
  try {
    const result = await request.get('/group-order/hot', { params: { limit: 8 } })
    const hot = (result || []).filter(go => ['open', 'full'].includes(go.status))
    if (hot.length > 0) {
      hotGroupOrders.value = hot
      return
    }
    await loadHotGroupOrdersFallback()
  } catch (error) {
    await loadHotGroupOrdersFallback()
  }
}

const loadHotGroupOrdersFallback = async () => {
  try {
    const merchantList = merchants.value?.length ? merchants.value : (await request.get('/merchant/list') || [])
    const requests = merchantList.slice(0, 10).map(m => request.get(`/group-order/open/${m.id}`))
    const results = await Promise.allSettled(requests)
    const merged = []
    results.forEach(r => {
      if (r.status === 'fulfilled' && Array.isArray(r.value)) {
        merged.push(...r.value)
      }
    })
    hotGroupOrders.value = merged
        .filter(go => ['open', 'full'].includes(go.status))
        .sort((a, b) => Number(b.currentCount || 0) - Number(a.currentCount || 0))
        .slice(0, 8)
  } catch (e) {
    hotGroupOrders.value = []
  }
}

// 计算拼单配送费（每人）
const calculateGroupDeliveryFee = (groupOrder) => {
  const peopleCount = groupOrder.targetCount || 2
  // 从后端返回的商家配送费获取
  const merchantDeliveryFee = Number(groupOrder.merchantDeliveryFee || 0)
  const totalFee = merchantDeliveryFee + peopleCount
  return (totalFee / peopleCount).toFixed(2)
}

// 跳转到拼单列表页面
const goToGroupList = () => {
  router.push('/student/group-list')
}

// 跳转到分类页面
const goToCategory = (type) => {
  if (type === 'group') {
    router.push('/student/group-list')
  } else {
    router.push(`/student/category/${type}`)
  }
}

onMounted(() => {
  loadMerchants()
  loadRecommendDishes()
  loadHotGroupOrders()
  loadAllDishes()
})
</script>

<style scoped>
/* 自定义滚动条隐藏 */
.custom-scrollbar::-webkit-scrollbar {
  display: none;
}
.custom-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 分类滚动容器 */
.category-scroll-wrapper {
  position: relative;
}

.category-scroll-wrapper::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 30px;
  background: linear-gradient(to right, rgba(255,255,255,0), rgba(255,255,255,1));
  pointer-events: none;
  z-index: 10;
}

/* 平滑滚动 */
.scroll-smooth {
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
}

/* 价格区域样式 */
.price-section {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.current-price {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 12px;
  color: #909399;
  text-decoration: line-through;
}

.discount-tag {
  font-size: 10px;
  font-weight: bold;
  color: #67c23a;
  background: #f0f9ff;
  padding: 1px 4px;
  border-radius: 4px;
}

.delivery-hint {
  font-size: 10px;
  color: #409eff;
}

/* 商家列表区域 - 手机端优化 */
.merchant-list-section {
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.section-title {
  font-size: 18px;
  font-weight: 900;
  color: #1a1a2e;
  letter-spacing: -0.02em;
}

.section-sort {
  font-size: 12px;
  font-weight: 700;
  color: #f97316;
}

/* 手机端适配 */
@media (max-width: 640px) {
  .merchant-list-section {
    padding: 12px;
  }

  .section-header {
    margin-bottom: 12px;
  }

  .section-title {
    font-size: 16px;
  }
}
</style>
