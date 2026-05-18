package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public Result<Map<String, Object>> getCart(@PathVariable String userId) {
        return Result.success(cartService.getCart(userId));
    }

    @PostMapping("/add")
    public Result<String> addToCart(@RequestBody Map<String, Object> params) {
        Object userIdObj = params.get("userId");
        Object merchantIdObj = params.get("merchantId");
        Object dishIdObj = params.get("dishId");
        
        // 确保ID转换为字符串
        String userId = userIdObj != null ? String.valueOf(userIdObj) : null;
        String merchantId = merchantIdObj != null ? String.valueOf(merchantIdObj) : null;
        String dishId = dishIdObj != null ? String.valueOf(dishIdObj) : null;
        
        if (userId == null || userId.isEmpty() || "null".equals(userId)) {
            return Result.error("用户ID不能为空");
        }
        if (merchantId == null || merchantId.isEmpty() || "null".equals(merchantId)) {
            return Result.error("商家ID不能为空");
        }
        if (dishId == null || dishId.isEmpty() || "null".equals(dishId)) {
            return Result.error("菜品ID不能为空");
        }
        
        Integer quantity = (Integer) params.get("quantity");
        if (quantity == null) quantity = 1;
        
        boolean success = cartService.addToCart(userId, merchantId, dishId, quantity);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result<String> updateQuantity(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer quantity = (Integer) params.get("quantity");
        
        boolean success = cartService.updateQuantity(id, quantity);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/remove/{id}")
    public Result<String> removeItem(@PathVariable Integer id) {
        boolean success = cartService.removeItem(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @DeleteMapping("/clear/{userId}")
    public Result<String> clearCart(@PathVariable String userId) {
        boolean success = cartService.clearCart(userId);
        return success ? Result.success("清空成功") : Result.error("清空失败");
    }
}
