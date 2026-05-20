package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.GroupOrder;
import com.community.cfgs.service.GroupOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-order")
public class GroupOrderController {

    @Autowired
    private GroupOrderService groupOrderService;

    @PostMapping("/create")
    public Result<GroupOrder> create(@RequestBody Map<String, Object> params) {
        try {
            String userId = (String) params.get("userId");
            String dishId = (String) params.get("dishId");
            String remark = (String) params.get("remark");
            String couponId = (String) params.get("couponId");
            // 新增：获取地址信息
            Map<String, String> address = (Map<String, String>) params.get("address");
            return Result.success(groupOrderService.createGroupOrder(userId, dishId, null, remark, couponId, address));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/join")
    public Result<String> join(@RequestBody Map<String, String> params) {
        try {
            boolean ok = groupOrderService.joinGroupOrder(params.get("groupOrderId"), params.get("userId"));
            return ok ? Result.success("参与成功") : Result.error("参与失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/open/{merchantId}")
    public Result<List<GroupOrder>> listOpenByMerchant(@PathVariable String merchantId) {
        return Result.success(groupOrderService.listOpenByMerchantId(merchantId));
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<List<GroupOrder>> listByMerchant(@PathVariable String merchantId) {
        return Result.success(groupOrderService.listByMerchantId(merchantId));
    }

    @GetMapping("/hot")
    public Result<List<GroupOrder>> listHot(@RequestParam(required = false, defaultValue = "8") Integer limit) {
        return Result.success(groupOrderService.listHotOpen(limit == null ? 8 : limit));
    }

    @PutMapping("/accept/{id}")
    public Result<String> accept(@PathVariable String id, @RequestParam String merchantId) {
        try {
            boolean ok = groupOrderService.acceptGroupOrder(id, merchantId);
            return ok ? Result.success("拼单接单成功") : Result.error("拼单接单失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消拼单（发起者操作）
     */
    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable String id, @RequestBody Map<String, String> params) {
        try {
            String userId = params.get("userId");
            String cancelReason = params.get("cancelReason");
            boolean ok = groupOrderService.cancelGroupOrder(id, userId, cancelReason);
            return ok ? Result.success("拼单已取消") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 商家取消拼单
     */
    @PostMapping("/cancel-by-merchant/{id}")
    public Result<String> cancelByMerchant(@PathVariable String id, @RequestBody Map<String, String> params) {
        try {
            String merchantId = params.get("merchantId");
            String cancelReason = params.get("cancelReason");
            boolean ok = groupOrderService.cancelGroupOrderByMerchant(id, merchantId, cancelReason);
            return ok ? Result.success("拼单已取消") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户发起的进行中拼单
     */
    @GetMapping("/my-active/{userId}")
    public Result<List<GroupOrder>> listMyActive(@PathVariable String userId) {
        return Result.success(groupOrderService.listMyActiveGroupOrders(userId));
    }

    /**
     * 获取用户的历史拼单（已完成、已取消）
     */
    @GetMapping("/my-history/{userId}")
    public Result<List<GroupOrder>> listMyHistory(@PathVariable String userId) {
        return Result.success(groupOrderService.listMyHistoryGroupOrders(userId));
    }

    /**
     * 获取拼单详情
     */
    @GetMapping("/detail/{id}")
    public Result<GroupOrder> detail(@PathVariable String id) {
        try {
            GroupOrder groupOrder = groupOrderService.getDetail(id);
            if (groupOrder == null) {
                return Result.error("拼单不存在");
            }
            return Result.success(groupOrder);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
