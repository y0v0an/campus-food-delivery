package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.Address;
import com.community.cfgs.entity.Order;
import com.community.cfgs.entity.User;
import com.community.cfgs.service.AddressService;
import com.community.cfgs.service.OrderService;
import com.community.cfgs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Map<String, Object> params) {
        String studentId = (String) params.get("studentId");
        String addressId = (String) params.get("addressId");
        String remark = (String) params.get("remark");
        String userCouponId = (String) params.get("userCouponId");
        String orderType = params.get("orderType") != null ? (String) params.get("orderType") : "delivery";

        try {
            if ("dine_in".equals(orderType)) {
                Order order = orderService.createOrder(studentId, null, remark, "dine_in", userCouponId);
                return Result.success(order);
            }
            Address address = addressService.getById(addressId);
            if (address == null) {
                return Result.error("地址不存在");
            }
            Order order = orderService.createOrder(studentId, address, remark, "delivery", userCouponId);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/merchant/free-create")
    public Result<Order> freeCreate(@RequestBody Map<String, Object> params) {
        try {
            String merchantId = (String) params.get("merchantId");
            String dishId = (String) params.get("dishId");
            Integer quantity = params.get("quantity") == null ? 1 : Integer.parseInt(params.get("quantity").toString());
            String contactName = (String) params.get("contactName");
            String remark = (String) params.get("remark");
            Order order = orderService.createFreeOrderByMerchant(merchantId, dishId, quantity, contactName, remark);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/student/confirm")
    public Result<String> studentConfirmPickup(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String studentId = params.get("studentId");
        if (orderId == null || studentId == null) {
            return Result.error("参数不完整");
        }
        boolean ok = orderService.studentConfirmPickup(orderId, studentId);
        return ok ? Result.success("确认成功") : Result.error("确认失败，请确认订单为店内就餐且已出餐");
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable String id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<Order>> listByStudentId(@PathVariable String studentId) {
        return Result.success(orderService.listByStudentId(studentId));
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<List<Order>> listByMerchantId(
            @PathVariable String merchantId,
            @RequestParam(required = false) String status) {
        return Result.success(orderService.listByMerchantId(merchantId, status));
    }

    @GetMapping("/rider/{riderId}")
    public Result<List<Order>> listByRiderId(@PathVariable String riderId) {
        return Result.success(orderService.listByRiderId(riderId));
    }

    @GetMapping("/pool")
    public Result<List<Order>> getOrderPool() {
        return Result.success(orderService.getOrderPool());
    }

    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String status = params.get("status");
        boolean success = orderService.updateStatus(orderId, status);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PutMapping("/accept")
    public Result<String> acceptOrder(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String riderId = params.get("riderId");
        String riderName = params.get("riderName");
        boolean success = orderService.acceptOrder(orderId, riderId, riderName);
        return success ? Result.success("接单成功") : Result.error("接单失败，订单可能已被接走");
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable String id) {
        boolean success = orderService.cancelOrder(id);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(orderService.getStats(merchantId, startDate, endDate));
    }

    @GetMapping("/admin/list")
    public Result<List<Order>> listForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (!isAdmin(userId)) {
            return Result.error("无权访问");
        }
        return Result.success(orderService.listForAdmin(keyword, merchantId, status));
    }

    @DeleteMapping("/admin/{id}")
    public Result<String> adminDeleteOrder(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (!isAdmin(userId)) {
            return Result.error("无权操作");
        }
        boolean success = orderService.adminDeleteOrder(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    private boolean isAdmin(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        User user = userService.getById(userId);
        return user != null && "admin".equals(user.getRole());
    }
}
