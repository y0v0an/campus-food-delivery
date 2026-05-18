package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.Dish;
import com.community.cfgs.mapper.DishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class DishService {

    @Autowired
    private DishMapper dishMapper;

    public List<Dish> listByMerchantId(String merchantId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getMerchantId, merchantId);
        return dishMapper.selectList(wrapper);
    }

    public List<Dish> listAvailableByMerchantId(String merchantId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getMerchantId, merchantId)
               .eq(Dish::getIsAvailable, true);
        return dishMapper.selectList(wrapper);
    }

    public Dish getById(String id) {
        return dishMapper.selectById(id);
    }

    public boolean add(Dish dish) {
        if (dish.getId() == null || dish.getId().isEmpty()) {
            dish.setId("d_" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (dish.getIsAvailable() == null) {
            dish.setIsAvailable(true);
        }
        if (dish.getIsGroupEnabled() == null) {
            dish.setIsGroupEnabled(false);
        }
        if (Boolean.TRUE.equals(dish.getIsGroupEnabled())) {
            if (dish.getGroupTargetCount() == null || dish.getGroupTargetCount() < 2) {
                dish.setGroupTargetCount(2);
            }
            if (dish.getGroupPrice() == null || dish.getGroupPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                dish.setGroupPrice(dish.getPrice());
            }
            if (dish.getGroupDurationMinutes() == null || dish.getGroupDurationMinutes() < 1) {
                dish.setGroupDurationMinutes(30);
            }
        } else {
            dish.setGroupTargetCount(null);
            dish.setGroupPrice(null);
            dish.setGroupDurationMinutes(null);
        }
        if (dish.getSales() == null) {
            dish.setSales(0);
        }
        return dishMapper.insert(dish) > 0;
    }

    public boolean update(Dish dish) {
        if (Boolean.TRUE.equals(dish.getIsGroupEnabled())) {
            if (dish.getGroupTargetCount() == null || dish.getGroupTargetCount() < 2) {
                dish.setGroupTargetCount(2);
            }
            if (dish.getGroupPrice() == null || dish.getGroupPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                Dish old = dish.getId() == null ? null : dishMapper.selectById(dish.getId());
                if (old != null && old.getPrice() != null) {
                    dish.setGroupPrice(old.getPrice());
                }
            }
            if (dish.getGroupDurationMinutes() == null || dish.getGroupDurationMinutes() < 1) {
                dish.setGroupDurationMinutes(30);
            }
        } else if (Boolean.FALSE.equals(dish.getIsGroupEnabled())) {
            dish.setGroupTargetCount(null);
            dish.setGroupPrice(null);
            dish.setGroupDurationMinutes(null);
        }
        // 如果库存为0，自动设置为不可用
        if (dish.getStock() != null && dish.getStock() == 0) {
            dish.setIsAvailable(false);
        }
        return dishMapper.updateById(dish) > 0;
    }

    public boolean delete(String id) {
        return dishMapper.deleteById(id) > 0;
    }

    public boolean updateStock(String id, int stock) {
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Dish::getId, id)
               .set(Dish::getStock, stock)
               .set(Dish::getIsAvailable, stock > 0);
        return dishMapper.update(null, wrapper) > 0;
    }

    public List<Dish> getRecommend() {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getIsAvailable, true)
               .orderByDesc(Dish::getSales)
               .last("LIMIT 10");
        return dishMapper.selectList(wrapper);
    }

    /**
     * 扣减菜品库存（使用数据库原子操作防止超卖）
     * @param id 菜品ID
     * @param quantity 扣减数量
     * @return 是否扣减成功（false表示库存不足）
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public boolean decreaseStock(String id, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        // 使用数据库层面的原子操作，一条SQL完成检查和更新
        // 只有当库存 >= quantity 时才会更新成功
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Dish::getId, id)
                .ge(Dish::getStock, quantity)  // 条件：库存 >= 扣减数量
                .setSql(String.format("stock = stock - %d", quantity))
                .setSql(String.format("is_available = (stock - %d) > 0", quantity));

        int rows = dishMapper.update(null, wrapper);
        return rows > 0;  // 返回true表示扣减成功，false表示库存不足
    }

    /**
     * 增加菜品销量（使用原子操作）
     * @param id 菜品ID
     * @param quantity 增加数量
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseSales(String id, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        // 使用SQL表达式原子性地增加销量
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Dish::getId, id)
                .setSql(String.format("sales = sales + %d", quantity));
        return dishMapper.update(null, wrapper) > 0;
    }

    /**
     * 恢复菜品库存（用于订单取消时回滚库存）
     * @param id 菜品ID
     * @param quantity 恢复数量
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(String id, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        // 使用SQL表达式原子性地增加库存，并自动设置为可用
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Dish::getId, id)
                .setSql(String.format("stock = stock + %d", quantity))
                .set(Dish::getIsAvailable, true);
        return dishMapper.update(null, wrapper) > 0;
    }

    public boolean updateGroupRule(String dishId, Boolean enabled, Integer targetCount, BigDecimal groupPrice, Integer durationMinutes) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            return false;
        }
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Dish::getId, dishId);
        boolean on = Boolean.TRUE.equals(enabled);
        wrapper.set(Dish::getIsGroupEnabled, on);
        if (on) {
            int target = (targetCount == null || targetCount < 2) ? 2 : targetCount;
            BigDecimal price = (groupPrice == null || groupPrice.compareTo(BigDecimal.ZERO) <= 0)
                    ? dish.getPrice()
                    : groupPrice;
            int duration = (durationMinutes == null || durationMinutes < 1) ? 30 : durationMinutes;
            wrapper.set(Dish::getGroupTargetCount, target)
                    .set(Dish::getGroupPrice, price)
                    .set(Dish::getGroupDurationMinutes, duration);
        } else {
            wrapper.set(Dish::getGroupTargetCount, null)
                    .set(Dish::getGroupPrice, null)
                    .set(Dish::getGroupDurationMinutes, null);
        }
        return dishMapper.update(null, wrapper) > 0;
    }

    public int applyGroupTemplate(String merchantId, List<String> dishIds, Integer targetCount, BigDecimal discountRate) {
        if (merchantId == null || merchantId.trim().isEmpty()) {
            return 0;
        }
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getMerchantId, merchantId);
        if (dishIds != null && !dishIds.isEmpty()) {
            query.in(Dish::getId, dishIds);
        }
        List<Dish> targets = dishMapper.selectList(query);
        if (targets.isEmpty()) {
            return 0;
        }
        int count = 0;
        int target = (targetCount == null || targetCount < 2) ? 2 : targetCount;
        BigDecimal rate = (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) <= 0)
                ? BigDecimal.ONE
                : discountRate;
        for (Dish dish : targets) {
            if (dish == null || dish.getPrice() == null) {
                continue;
            }
            BigDecimal groupPrice = dish.getPrice().multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
            if (groupPrice.compareTo(new BigDecimal("0.01")) < 0) {
                groupPrice = new BigDecimal("0.01");
            }
            if (updateGroupRule(dish.getId(), true, target, groupPrice, dish.getGroupDurationMinutes())) {
                count++;
            }
        }
        return count;
    }
}
