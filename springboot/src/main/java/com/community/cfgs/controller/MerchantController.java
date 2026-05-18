package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.Merchant;
import com.community.cfgs.entity.User;
import com.community.cfgs.service.MerchantService;
import com.community.cfgs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Merchant>> list() {
        return Result.success(merchantService.listOpen());
    }

    @GetMapping("/all")
    public Result<List<Merchant>> listAll() {
        return Result.success(merchantService.list());
    }

    @GetMapping("/{id}")
    public Result<Merchant> getById(@PathVariable String id) {
        return Result.success(merchantService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public Result<Merchant> getByUserId(@PathVariable String userId) {
        return Result.success(merchantService.getByUserId(userId));
    }

    @GetMapping("/search")
    public Result<List<Merchant>> search(@RequestParam String keyword) {
        return Result.success(merchantService.search(keyword));
    }

    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        Boolean isOpen = (Boolean) params.get("isOpen");
        boolean success = merchantService.updateStatus(id, isOpen);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PutMapping("/update")
    public Result<String> update(
            @RequestBody Merchant merchant,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isEmpty()) {
            return Result.error("未登录");
        }
        Merchant existing = merchantService.getById(merchant.getId());
        if (existing == null || !userId.equals(existing.getUserId())) {
            return Result.error("无权修改该绑定店铺");
        }
        merchant.setUserId(existing.getUserId());
        boolean success = merchantService.update(merchant);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PutMapping("/admin/update")
    public Result<String> adminUpdate(
            @RequestBody Merchant merchant,
            @RequestParam(required = false) String newPassword,
            @RequestHeader(value = "X-User-Id", required = false) String adminUserId) {
        if (adminUserId == null || adminUserId.isEmpty()) {
            return Result.error("未登录");
        }
        User admin = userService.getById(adminUserId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return Result.error("无权操作");
        }
        if (merchant.getId() == null || merchant.getId().isEmpty()) {
            return Result.error("商家ID无效");
        }
        boolean success = merchantService.adminUpdateMerchant(merchant, newPassword);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Object> params) {
        Merchant merchant = new Merchant();
        merchant.setName((String) params.get("name"));
        merchant.setPhone((String) params.get("phone"));
        merchant.setAddress((String) params.get("address"));
        merchant.setDescription((String) params.get("description"));
        merchant.setLogo((String) params.get("logo"));
        Object categoriesObj = params.get("categories");
        if (categoriesObj != null) {
            merchant.setCategories(categoriesObj.toString());
        }

        Object deliveryFeeObj = params.get("deliveryFee");
        if (deliveryFeeObj != null) {
            merchant.setDeliveryFee(new java.math.BigDecimal(deliveryFeeObj.toString()));
        }
        
        Object minOrderObj = params.get("minOrder");
        if (minOrderObj != null) {
            merchant.setMinOrder(new java.math.BigDecimal(minOrderObj.toString()));
        }

        Object deliveryTimeObj = params.get("deliveryTime");
        if (deliveryTimeObj != null && !deliveryTimeObj.toString().isEmpty()) {
            merchant.setDeliveryTime(deliveryTimeObj.toString());
        }

        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String name = (String) params.get("name");
        
        boolean success = merchantService.add(merchant, username, password, name);
        return success ? Result.success("添加成功") : Result.error("添加失败，账号可能已存在");
    }

    @GetMapping("/stats/{id}")
    public Result<Map<String, Object>> getStats(@PathVariable String id) {
        return Result.success(merchantService.getStats(id));
    }

    @GetMapping("/ranking")
    public Result<List<Merchant>> getRanking() {
        return Result.success(merchantService.getRanking());
    }
}
