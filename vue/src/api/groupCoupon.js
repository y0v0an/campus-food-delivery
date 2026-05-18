import request from '../axios/request'

// 商家端 - 创建拼单优惠券
export const createGroupCoupon = (data) => {
  return request({
    url: '/merchant/coupons',
    method: 'post',
    data
  })
}

// 商家端 - 获取我的优惠券列表
export const getMyGroupCoupons = () => {
  return request({
    url: '/merchant/coupons',
    method: 'get'
  })
}

// 商家端 - 获取优惠券详情
export const getGroupCouponDetail = (id) => {
  return request({
    url: `/merchant/coupons/${id}`,
    method: 'get'
  })
}

// 商家端 - 更新优惠券
export const updateGroupCoupon = (id, data) => {
  return request({
    url: `/merchant/coupons/${id}`,
    method: 'put',
    data
  })
}

// 商家端 - 删除优惠券
export const deleteGroupCoupon = (id) => {
  return request({
    url: `/merchant/coupons/${id}`,
    method: 'delete'
  })
}

// 商家端 - 暂停/恢复优惠券
export const toggleGroupCouponStatus = (id) => {
  return request({
    url: `/merchant/coupons/${id}/toggle`,
    method: 'post'
  })
}

// 学生端 - 获取可用拼单优惠券
export const getAvailableGroupCoupons = (params) => {
  return request({
    url: '/student/coupons/available',
    method: 'get',
    params
  })
}
