/**
 * 分类配置 - 用于商家后台经营品类选择器和学生端分类标签
 * 商家选择这些分类后，学生端可以正确匹配和展示
 */

// 学生端分类与商家经营品类的一一对应映射
export const CATEGORY_KEYWORDS = {
  'hot': [], // 特殊处理：不过滤，按销量排序
  'dessert-drinks': ['下午茶'],
  'fruits': ['新鲜水果'],
  'desserts': ['甜点'],
  'snacks': ['小吃简餐'],
  'dining': ['正餐']
}

// 商家可选的经营品类（简化版，只保留大类）
export const MERCHANT_CATEGORY_OPTIONS = [
  '下午茶',
  '新鲜水果',
  '甜点',
  '小吃简餐',
  '正餐'
]

// 分组选项（用于商家后台选择器）
export const GROUPED_CATEGORY_OPTIONS = [
  {
    label: '📦 分类标签',
    options: MERCHANT_CATEGORY_OPTIONS
  }
]
