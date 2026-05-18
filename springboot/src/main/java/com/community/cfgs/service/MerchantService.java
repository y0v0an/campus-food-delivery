package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.Merchant;
import com.community.cfgs.entity.User;
import com.community.cfgs.mapper.MerchantMapper;
import com.community.cfgs.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserMapper userMapper;

    public List<Merchant> list() {
        return merchantMapper.selectList(null);
    }

    public List<Merchant> listOpen() {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getIsOpen, true);
        return merchantMapper.selectList(wrapper);
    }

    public Merchant getById(String id) {
        return merchantMapper.selectById(id);
    }

    public Merchant getByUserId(String userId) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUserId, userId);
        return merchantMapper.selectOne(wrapper);
    }

    public List<Merchant> search(String keyword) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Merchant::getName, keyword)
               .or().like(Merchant::getDescription, keyword)
               .or().like(Merchant::getCategories, keyword);
        return merchantMapper.selectList(wrapper);
    }

    public boolean updateStatus(String id, boolean isOpen) {
        LambdaUpdateWrapper<Merchant> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Merchant::getId, id).set(Merchant::getIsOpen, isOpen);
        return merchantMapper.update(null, wrapper) > 0;
    }

    public boolean update(Merchant merchant) {
        return merchantMapper.updateById(merchant) > 0;
    }

    /**
     * 管理员更新商家资料；可选重置登录密码；同步更新关联用户的姓名与手机号（与新建商家时规则一致）
     */
    public boolean adminUpdateMerchant(Merchant merchant, String newPassword) {
        if (merchant.getId() == null) {
            return false;
        }
        Merchant existing = merchantMapper.selectById(merchant.getId());
        if (existing == null) {
            return false;
        }
        merchant.setUserId(existing.getUserId());
        if (merchant.getRating() == null) {
            merchant.setRating(existing.getRating());
        }
        if (merchant.getMonthSales() == null) {
            merchant.setMonthSales(existing.getMonthSales());
        }
        if (merchant.getIsOpen() == null) {
            merchant.setIsOpen(existing.getIsOpen());
        }
        if (merchant.getLat() == null) {
            merchant.setLat(existing.getLat());
        }
        if (merchant.getLng() == null) {
            merchant.setLng(existing.getLng());
        }
        boolean ok = merchantMapper.updateById(merchant) > 0;
        if (!ok) {
            return false;
        }
        String uid = existing.getUserId();
        if (uid == null) {
            return true;
        }
        LambdaUpdateWrapper<User> uw = new LambdaUpdateWrapper<>();
        uw.eq(User::getId, uid)
                .set(User::getName, merchant.getName())
                .set(User::getPhone, merchant.getPhone());
        if (StringUtils.hasText(newPassword)) {
            uw.set(User::getPassword, newPassword);
        }
        userMapper.update(null, uw);
        return true;
    }

    public boolean add(Merchant merchant, String username, String password, String name) {
        // 先创建商家用户
        User user = new User();
        user.setId("merchant_" + java.util.UUID.randomUUID().toString().substring(0, 8));
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("merchant");
        user.setName(name);
        user.setPhone(merchant.getPhone());
        user.setIsDisabled(false);
        user.setIsRider(false);
        user.setIsOnline(false);
        
        // 检查账号是否已存在
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> userWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        userWrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(userWrapper) > 0) {
            return false;
        }
        
        userMapper.insert(user);
        
        // 创建商家
        merchant.setId("m_" + java.util.UUID.randomUUID().toString().substring(0, 8));
        merchant.setUserId(user.getId());
        if (merchant.getRating() == null) {
            merchant.setRating(new java.math.BigDecimal("5.0"));
        }
        if (merchant.getMonthSales() == null) {
            merchant.setMonthSales(0);
        }
        if (merchant.getIsOpen() == null) {
            merchant.setIsOpen(true);
        }
        if (merchant.getDeliveryTime() == null) {
            merchant.setDeliveryTime("30分钟");
        }
        if (merchant.getCategories() == null) {
            merchant.setCategories("[]");
        }

        return merchantMapper.insert(merchant) > 0;
    }

    public Map<String, Object> getStats(String merchantId) {
        Map<String, Object> stats = new HashMap<>();
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant != null) {
            stats.put("rating", merchant.getRating());
            stats.put("monthSales", merchant.getMonthSales());
        }
        return stats;
    }

    public List<Merchant> getRanking() {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Merchant::getMonthSales).last("LIMIT 10");
        return merchantMapper.selectList(wrapper);
    }
}
