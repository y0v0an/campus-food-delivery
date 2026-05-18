package com.community.cfgs.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentReviewItemVo {
    private String reviewId;
    private String orderId;
    private String orderNo;
    private String merchantName;
    private Integer merchantRating;
    private Integer riderRating;
    private String content;
    /** JSON 数组字符串，与 Review.images 一致 */
    private String images;
    /** 解析后的图片路径，前端可直接展示 */
    private List<String> imageUrls;
    /** 本单菜品明细（名称、菜品图、数量等），用于「我的评价」展示 */
    private List<ReviewOrderItemVo> orderItems;
    private LocalDateTime createdAt;
}
