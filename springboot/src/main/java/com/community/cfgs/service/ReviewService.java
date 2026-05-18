package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.Merchant;
import com.community.cfgs.entity.Order;
import com.community.cfgs.entity.OrderItem;
import com.community.cfgs.entity.Review;
import com.community.cfgs.mapper.MerchantMapper;
import com.community.cfgs.mapper.OrderItemMapper;
import com.community.cfgs.mapper.OrderMapper;
import com.community.cfgs.mapper.ReviewMapper;
import com.community.cfgs.vo.ReviewOrderItemVo;
import com.community.cfgs.vo.StudentReviewItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

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

        review.setId("r_" + UUID.randomUUID().toString().substring(0, 8));
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        
        // 更新商家评分
        updateMerchantRating(review.getMerchantId());
        
        return true;
    }

    public List<Review> listByMerchantId(String merchantId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getMerchantId, merchantId)
               .orderByDesc(Review::getCreatedAt);
        return reviewMapper.selectList(wrapper);
    }

    public Review getByOrderId(String orderId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId);
        return reviewMapper.selectOne(wrapper);
    }

    public List<Review> listByStudentId(String studentId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getStudentId, studentId).orderByDesc(Review::getCreatedAt);
        return reviewMapper.selectList(wrapper);
    }

    /** 学生「我的评价」列表（附订单号、商家名、订单菜品明细） */
    public List<StudentReviewItemVo> listStudentReviewSummary(String studentId) {
        List<Review> reviews = listByStudentId(studentId);
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> orderIds = reviews.stream().map(Review::getOrderId).collect(Collectors.toList());
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds);
        List<OrderItem> allItems = orderItemMapper.selectList(itemWrapper);
        Map<String, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<StudentReviewItemVo> result = new ArrayList<>(reviews.size());
        for (Review r : reviews) {
            StudentReviewItemVo vo = new StudentReviewItemVo();
            vo.setReviewId(r.getId());
            vo.setOrderId(r.getOrderId());
            vo.setMerchantRating(r.getMerchantRating());
            vo.setRiderRating(r.getRiderRating());
            vo.setContent(r.getContent());
            vo.setImages(r.getImages());
            vo.setImageUrls(parseReviewImagePaths(r.getImages()));
            vo.setCreatedAt(r.getCreatedAt());
            Order o = orderMapper.selectById(r.getOrderId());
            if (o != null) {
                vo.setOrderNo(o.getOrderNo());
                vo.setMerchantName(o.getMerchantName());
            }
            List<OrderItem> lineItems = itemsByOrderId.getOrDefault(r.getOrderId(), Collections.emptyList());
            vo.setOrderItems(lineItems.stream().map(this::toReviewOrderItemVo).collect(Collectors.toList()));
            result.add(vo);
        }
        return result;
    }

    private ReviewOrderItemVo toReviewOrderItemVo(OrderItem oi) {
        ReviewOrderItemVo v = new ReviewOrderItemVo();
        v.setDishId(oi.getDishId());
        v.setDishName(oi.getDishName());
        v.setDishImage(oi.getDishImage());
        v.setQuantity(oi.getQuantity());
        v.setPrice(oi.getPrice());
        return v;
    }

    /** 将 reviews.images 字段解析为路径列表（供列表接口直接使用） */
    public static List<String> parseReviewImagePaths(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String t = raw.trim();
        try {
            if (t.startsWith("[")) {
                List<String> list = JSON.readValue(t, new TypeReference<List<String>>() {});
                if (list == null) {
                    return Collections.emptyList();
                }
                List<String> out = new ArrayList<>();
                for (String s : list) {
                    if (s != null && !s.trim().isEmpty()) {
                        out.add(s.trim());
                    }
                }
                return out;
            }
        } catch (JsonProcessingException ignored) {
            // fall through
        }
        if (t.contains(",")) {
            List<String> out = new ArrayList<>();
            for (String s : t.split(",")) {
                String x = s.trim();
                if (!x.isEmpty()) {
                    out.add(x);
                }
            }
            return out;
        }
        if (t.startsWith("/") || t.startsWith("http") || t.startsWith("img/")) {
            return Collections.singletonList(t);
        }
        return Collections.emptyList();
    }

    /** 骑手收到的评价（含对骑手的星级） */
    public List<Review> listByRiderId(String riderId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRiderId, riderId)
                .orderByDesc(Review::getCreatedAt);
        return reviewMapper.selectList(wrapper);
    }

    public BigDecimal calculateAverageRating(String merchantId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getMerchantId, merchantId);
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        if (reviews.isEmpty()) {
            return new BigDecimal("5.0");
        }
        
        double avg = reviews.stream()
                .mapToInt(Review::getMerchantRating)
                .average()
                .orElse(5.0);
        
        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
    }

    private void updateMerchantRating(String merchantId) {
        BigDecimal avgRating = calculateAverageRating(merchantId);
        LambdaUpdateWrapper<Merchant> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Merchant::getId, merchantId)
               .set(Merchant::getRating, avgRating);
        merchantMapper.update(null, wrapper);
    }
}
