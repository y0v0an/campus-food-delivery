import request from '../axios/request'

/**
 * 获取所有可拼单的菜品列表
 * @param {Object} params - 查询参数
 * @param {string} params.sortBy - 排序方式：default/discount/sales/distance
 * @param {string} params.userId - 用户ID（可选）
 * @returns {Promise}
 */
export const getGroupEnabledDishes = (params) => {
  return request({
    url: '/dish/group-enabled',
    method: 'get',
    params
  })
}

/**
 * 根据商家ID获取菜品列表
 * @param {string} merchantId - 商家ID
 * @returns {Promise}
 */
export const getDishesByMerchantId = (merchantId) => {
  return request({
    url: `/dish/list/${merchantId}`,
    method: 'get'
  })
}

/**
 * 获取推荐菜品
 * @returns {Promise}
 */
export const getRecommendDishes = () => {
  return request({
    url: '/dish/recommend',
    method: 'get'
  })
}
