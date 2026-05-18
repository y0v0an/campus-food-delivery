package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.cfgs.entity.Order;
import com.community.cfgs.entity.Review;
import com.community.cfgs.entity.RiderInfo;
import com.community.cfgs.mapper.OrderMapper;
import com.community.cfgs.mapper.RiderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RiderService {

    @Autowired
    private RiderInfoMapper riderInfoMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ReviewService reviewService;

    public RiderInfo getByUserId(String userId) {
        LambdaQueryWrapper<RiderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiderInfo::getUserId, userId);
        return riderInfoMapper.selectOne(wrapper);
    }

    public Map<String, Object> getEarnings(String riderId) {
        Map<String, Object> earnings = new HashMap<>();
        
        // 获取骑手完成的订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getRiderId, riderId).eq(Order::getStatus, "completed");
        List<Order> completedOrders = orderMapper.selectList(wrapper);
        
        // 计算总收益（配送费）
        BigDecimal totalEarnings = completedOrders.stream()
                .map(Order::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 今日收益
        BigDecimal todayEarnings = completedOrders.stream()
                .filter(o -> o.getDeliveredAt() != null && 
                        o.getDeliveredAt().toLocalDate().equals(java.time.LocalDate.now()))
                .map(Order::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 今日订单数
        long todayOrders = completedOrders.stream()
                .filter(o -> o.getDeliveredAt() != null && 
                        o.getDeliveredAt().toLocalDate().equals(java.time.LocalDate.now()))
                .count();
        
        // 本月收益
        java.time.LocalDate monthStart = java.time.LocalDate.now().withDayOfMonth(1);
        BigDecimal monthEarnings = completedOrders.stream()
                .filter(o -> o.getDeliveredAt() != null && 
                        !o.getDeliveredAt().toLocalDate().isBefore(monthStart))
                .map(Order::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 获取骑手信息
        RiderInfo riderInfo = getByUserId(riderId);

        List<Review> riderReviewList = reviewService.listByRiderId(riderId);
        List<Review> withRiderStar = riderReviewList.stream()
                .filter(r -> r.getRiderRating() != null && r.getRiderRating() > 0)
                .collect(Collectors.toList());
        int riderRatingCount = withRiderStar.size();
        BigDecimal riderAvg;
        if (riderRatingCount == 0) {
            riderAvg = riderInfo != null && riderInfo.getRating() != null
                    ? riderInfo.getRating().setScale(1, RoundingMode.HALF_UP)
                    : new BigDecimal("5.0");
        } else {
            double avg = withRiderStar.stream().mapToInt(Review::getRiderRating).average().orElse(5.0);
            riderAvg = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        }

        earnings.put("totalEarnings", totalEarnings);
        earnings.put("todayEarnings", todayEarnings);
        earnings.put("todayOrders", todayOrders);
        earnings.put("monthEarnings", monthEarnings);
        earnings.put("totalDeliveries", completedOrders.size());
        earnings.put("avgTime", 20);
        earnings.put("rating", riderAvg);
        earnings.put("ratingCount", riderRatingCount);

        return earnings;
    }

    public boolean updateRiderInfo(RiderInfo riderInfo) {
        return riderInfoMapper.updateById(riderInfo) > 0;
    }
}
