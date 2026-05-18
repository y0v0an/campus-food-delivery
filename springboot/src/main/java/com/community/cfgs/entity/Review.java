package com.community.cfgs.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review {
    @TableId
    private String id;
    private String orderId;
    private String studentId;
    private String merchantId;
    private String riderId;
    private Integer merchantRating;  // 1-5
    private Integer riderRating;     // 1-5
    private String content;
    /** JSON 数组字符串，如 ["/img/a.jpg","/img/b.png"] */
    private String images;
    private LocalDateTime createdAt;
}
