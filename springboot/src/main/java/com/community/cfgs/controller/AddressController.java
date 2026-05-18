package com.community.cfgs.controller;

import com.community.cfgs.common.Result;
import com.community.cfgs.entity.Address;
import com.community.cfgs.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/{userId}")
    public Result<List<Address>> listByUserId(@PathVariable String userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    @GetMapping("/detail/{id}")
    public Result<Address> getById(@PathVariable String id) {
        return Result.success(addressService.getById(id));
    }

    @GetMapping("/default/{userId}")
    public Result<Address> getDefault(@PathVariable String userId) {
        return Result.success(addressService.getDefaultByUserId(userId));
    }

    @PostMapping("/add")
    public Result<Address> add(@RequestBody Address address) {
        return Result.success(addressService.add(address));
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Address address) {
        boolean success = addressService.update(address);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        boolean success = addressService.delete(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/default/{id}")
    public Result<String> setDefault(@PathVariable String id) {
        boolean success = addressService.setDefault(id);
        return success ? Result.success("设置成功") : Result.error("设置失败");
    }
}
