package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.dto.GroupEnabledDishVO;
import com.community.cfgs.entity.Dish;
import com.community.cfgs.entity.Merchant;
import com.community.cfgs.mapper.DishMapper;
import com.community.cfgs.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private MerchantMapper merchantMapper;

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

    /**
     * 获取所有可拼单的菜品列表
     * @param sortBy 排序方式：default(默认)/discount(优惠最大)/sales(销量最高)/distance(距离最近)
     * @param userId 用户ID（可选，用于计算距离）
     * @return 可拼单菜品列表
     */
    public List<GroupEnabledDishVO> listGroupEnabledDishes(String sortBy, String userId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getIsGroupEnabled, true)
               .eq(Dish::getIsAvailable, true)
               .gt(Dish::getStock, 0);

        List<Dish> dishes = dishMapper.selectList(wrapper);

        if (dishes.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取商家信息
        Set<String> merchantIds = dishes.stream()
                .map(Dish::getMerchantId)
                .collect(Collectors.toSet());
        List<Merchant> merchants = merchantMapper.selectBatchIds(merchantIds);
        Map<String, Merchant> merchantMap = merchants.stream()
                .collect(Collectors.toMap(Merchant::getId, m -> m));

        // 构建 VO 列表
        List<GroupEnabledDishVO> result = dishes.stream()
                .map(dish -> {
                    Merchant merchant = merchantMap.get(dish.getMerchantId());
                    GroupEnabledDishVO.MerchantInfo merchantInfo = buildMerchantInfo(merchant);
                    return GroupEnabledDishVO.from(dish, merchantInfo);
                })
                .collect(Collectors.toList());

        // 排序
        sortDishes(result, sortBy);

        return result;
    }

    /**
     * 构建商家信息
     */
    private GroupEnabledDishVO.MerchantInfo buildMerchantInfo(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        GroupEnabledDishVO.MerchantInfo info = new GroupEnabledDishVO.MerchantInfo();
        info.setId(merchant.getId());
        info.setName(merchant.getName());
        info.setLogo(merchant.getLogo());
        info.setRating(merchant.getRating());
        info.setDeliveryTime(merchant.getDeliveryTime());
        info.setDeliveryFee(merchant.getDeliveryFee());
        info.setIsOpen(merchant.getIsOpen());
        // 暂时使用商家地址作为区域，后续可扩展为距离计算
        info.setArea(merchant.getAddress());
        info.setDistance(null); // 暂无定位功能
        return info;
    }

    /**
     * 根据排序方式对菜品列表排序
     */
    private void sortDishes(List<GroupEnabledDishVO> dishes, String sortBy) {
        if (dishes == null || dishes.isEmpty()) {
            return;
        }

        Comparator<GroupEnabledDishVO> comparator;
        switch (sortBy == null ? "default" : sortBy) {
            case "discount":
                // 按折扣率升序（折扣越大，rate 越小）
                comparator = Comparator.comparing(
                    GroupEnabledDishVO::getDiscountRate,
                    Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
            case "sales":
                // 按销量降序
                comparator = Comparator.comparing(
                    GroupEnabledDishVO::getSales,
                    Comparator.nullsLast(Comparator.reverseOrder())
                );
                break;
            case "distance":
                // 按距离升序（暂时按区域热度，后续可扩展为真实距离）
                comparator = Comparator.comparing(
                    vo -> vo.getMerchant() == null ? Integer.MAX_VALUE : 999,
                    Comparator.naturalOrder()
                );
                break;
            case "default":
            default:
                // 默认按优惠金额降序（原价 - 拼单价）
                comparator = Comparator.comparing(
                    (GroupEnabledDishVO vo) -> {
                        BigDecimal original = vo.getOriginalPrice();
                        BigDecimal groupPrice = vo.getGroupPrice();
                        if (original == null || groupPrice == null) {
                            return BigDecimal.ZERO;
                        }
                        return original.subtract(groupPrice);
                    },
                    Comparator.nullsLast(Comparator.reverseOrder())
                );
                break;
        }

        dishes.sort(comparator);
    }
}
