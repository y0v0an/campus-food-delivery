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
            class="bg-white border border-gray-100 rounded-3xl p-4 shadow-sm cursor-pointer active:scale-[0.98] transition-transform"
            @click="goToMerchant(merchant.id)"
        >
          <!-- 商家头部 -->
          <div class="flex gap-4">
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
          <!-- 热销菜品 -->
          <div v-if="merchant.dishes && merchant.dishes.length > 0" class="mt-3 pt-3 border-t border-gray-100">
            <div class="flex gap-3 overflow-x-auto">
              <div
                v-for="dish in merchant.dishes"
                :key="dish.id"
                class="flex-shrink-0 flex gap-2 items-center"
                @click.stop="goToMerchant(merchant.id)"
              >
                <img :src="getImageUrl(dish.image)" :alt="dish.name" class="w-12 h-12 rounded-xl object-cover" />
                <div class="flex flex-col">
                  <span class="text-xs font-medium text-gray-700 truncate max-w-[80px]">{{ dish.name }}</span>
                  <span class="text-xs font-bold text-orange-500">¥{{ dish.price }}</span>
                </div>
              </div>
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
import { CATEGORY_KEYWORDS as categoryKeywordsMap } from '@/utils/categoryConfig'

const router = useRouter()
const route = useRoute()

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

// 获取当前分类的关键词（从统一配置获取）
const categoryKeywords = computed(() => {
  return categoryKeywordsMap[categoryType.value] || []
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
    merchants.value = []
  }
}

onMounted(() => {
  loadMerchants()
})
</script>

<style scoped>
/* 菜品滚动区域隐藏滚动条 */
.merchant-dishes::-webkit-scrollbar {
  display: none;
}
.merchant-dishes {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
