package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.Review;
import com.community.cfgs.service.ReviewService;
import com.community.cfgs.vo.StudentReviewItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public Result<String> addReview(@RequestBody Review review) {
        boolean success = reviewService.addReview(review);
        return success ? Result.success("评价成功") : Result.error("该订单已评价");
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<List<Review>> listByMerchantId(@PathVariable String merchantId) {
        return Result.success(reviewService.listByMerchantId(merchantId));
    }

    @GetMapping("/order/{orderId}")
    public Result<Review> getByOrderId(@PathVariable String orderId) {
        return Result.success(reviewService.getByOrderId(orderId));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<StudentReviewItemVo>> listByStudent(@PathVariable String studentId) {
        return Result.success(reviewService.listStudentReviewSummary(studentId));
    }

    @GetMapping("/rider/{riderId}")
    public Result<List<Review>> listByRiderId(@PathVariable String riderId) {
        return Result.success(reviewService.listByRiderId(riderId));
    }
}
