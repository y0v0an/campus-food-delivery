package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.Dish;
import com.community.cfgs.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping("/list/{merchantId}")
    public Result<List<Dish>> listByMerchantId(@PathVariable String merchantId) {
        return Result.success(dishService.listAvailableByMerchantId(merchantId));
    }

    @GetMapping("/all/{merchantId}")
    public Result<List<Dish>> listAllByMerchantId(@PathVariable String merchantId) {
        return Result.success(dishService.listByMerchantId(merchantId));
    }

    @GetMapping("/{id}")
    public Result<Dish> getById(@PathVariable String id) {
        return Result.success(dishService.getById(id));
    }

    @PostMapping("/add")
    public Result<Dish> add(@RequestBody Dish dish) {
        boolean success = dishService.add(dish);
        return success ? Result.success(dish) : Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Dish dish) {
        boolean success = dishService.update(dish);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        boolean success = dishService.delete(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/stock")
    public Result<String> updateStock(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        Integer stock = (Integer) params.get("stock");
        boolean success = dishService.updateStock(id, stock);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @GetMapping("/recommend")
    public Result<List<Dish>> getRecommend() {
        return Result.success(dishService.getRecommend());
    }

    @PutMapping("/group-rule")
    public Result<String> updateGroupRule(@RequestBody Map<String, Object> params) {
        String dishId = (String) params.get("dishId");
        Boolean enabled = params.get("enabled") == null ? null : Boolean.valueOf(params.get("enabled").toString());
        Integer targetCount = params.get("targetCount") == null ? null : Integer.valueOf(params.get("targetCount").toString());
        BigDecimal groupPrice = params.get("groupPrice") == null ? null : new BigDecimal(params.get("groupPrice").toString());
        Integer durationMinutes = params.get("durationMinutes") == null ? null : Integer.valueOf(params.get("durationMinutes").toString());
        boolean success = dishService.updateGroupRule(dishId, enabled, targetCount, groupPrice, durationMinutes);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PostMapping("/group-template/apply")
    public Result<String> applyGroupTemplate(@RequestBody Map<String, Object> params) {
        String merchantId = (String) params.get("merchantId");
        Integer targetCount = params.get("targetCount") == null ? null : Integer.valueOf(params.get("targetCount").toString());
        BigDecimal discountRate = params.get("discountRate") == null ? null : new BigDecimal(params.get("discountRate").toString());
        @SuppressWarnings("unchecked")
        List<String> dishIds = (List<String>) params.get("dishIds");
        int affected = dishService.applyGroupTemplate(merchantId, dishIds, targetCount, discountRate);
        return Result.success("已应用到" + affected + "个菜品");
    }
}
