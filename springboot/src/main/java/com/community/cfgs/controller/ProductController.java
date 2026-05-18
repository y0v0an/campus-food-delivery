package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.Product;
import com.community.cfgs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<List<Product>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return Result.success(productService.list(status, category));
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable String id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Product> add(@RequestBody Product product) {
        return Result.success(productService.add(product));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @RequestBody Product product) {
        if (productService.update(id, product)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> params) {
        String status = params.get("status");
        if (productService.updateStatus(id, status)) {
            return Result.success();
        }
        return Result.error("更新状态失败");
    }

    @PutMapping("/{id}/restock")
    public Result<Map<String, Integer>> restock(@PathVariable String id, @RequestBody Map<String, Integer> params) {
        Integer quantity = params.get("quantity");
        Integer newStock = productService.restock(id, quantity);
        if (newStock != null) {
            Map<String, Integer> result = new HashMap<>();
            result.put("newStock", newStock);
            return Result.success(result);
        }
        return Result.error("补货失败");
    }

    @PutMapping("/{id}/reduce-stock")
    public Result<Void> reduceStock(@PathVariable String id, @RequestBody Map<String, Integer> params) {
        Integer quantity = params.get("quantity");
        if (productService.reduceStock(id, quantity)) {
            return Result.success();
        }
        return Result.error("扣减库存失败");
    }

    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(productService.getCategories());
    }
}
