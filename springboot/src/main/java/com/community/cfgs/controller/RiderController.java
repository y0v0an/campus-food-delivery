package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.RiderApplication;
import com.community.cfgs.service.RiderApplicationService;
import com.community.cfgs.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rider")
public class RiderController {

    @Autowired
    private RiderApplicationService applicationService;

    @Autowired
    private RiderService riderService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody RiderApplication application) {
        boolean success = applicationService.apply(application);
        return success ? Result.success("申请提交成功") : Result.error("您已有待审核的申请");
    }

    @GetMapping("/applications")
    public Result<List<RiderApplication>> listApplications(
            @RequestParam(required = false, defaultValue = "pending") String status) {
        return Result.success(applicationService.listByStatus(status));
    }

    @GetMapping("/application/{studentId}")
    public Result<RiderApplication> getApplicationByStudentId(@PathVariable String studentId) {
        return Result.success(applicationService.getByStudentId(studentId));
    }

    @PutMapping("/approve/{id}")
    public Result<String> approve(@PathVariable String id) {
        boolean success = applicationService.approve(id);
        return success ? Result.success("审核通过") : Result.error("审核失败");
    }

    @PutMapping("/reject/{id}")
    public Result<String> reject(@PathVariable String id) {
        boolean success = applicationService.reject(id);
        return success ? Result.success("已拒绝") : Result.error("操作失败");
    }

    @GetMapping("/earnings/{riderId}")
    public Result<Map<String, Object>> getEarnings(@PathVariable String riderId) {
        return Result.success(riderService.getEarnings(riderId));
    }
}
