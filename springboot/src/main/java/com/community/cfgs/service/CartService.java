package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.CartItem;
import com.community.cfgs.entity.Dish;
import com.community.cfgs.mapper.CartMapper;
import com.community.cfgs.mapper.DishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private DishMapper dishMapper;

    public Map<String, Object> getCart(String userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        List<CartItem> items = cartMapper.selectList(wrapper);
        
        BigDecimal total = BigDecimal.ZERO;
        String merchantId = null;
        
        for (CartItem item : items) {
            Dish dish = dishMapper.selectById(item.getDishId());
            item.setDish(dish);
            if (dish != null) {
                total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                merchantId = item.getMerchantId();
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("merchantId", merchantId);
        return result;
    }

    /**
     * 添加商品到购物车（使用乐观锁防止并发问题）
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @param dishId 菜品ID
     * @param quantity 数量
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public boolean addToCart(String userId, String merchantId, String dishId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        // 检查是否已有该商品
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId).eq(CartItem::getDishId, dishId);
        CartItem existing = cartMapper.selectOne(wrapper);

        if (existing != null) {
            // 使用乐观锁更新数量（防止并发覆盖）
            LambdaUpdateWrapper<CartItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CartItem::getId, existing.getId())
                    .eq(CartItem::getQuantity, existing.getQuantity())  // 乐观锁条件
                    .set(CartItem::getQuantity, existing.getQuantity() + quantity);

            int rows = cartMapper.update(null, updateWrapper);
            if (rows <= 0) {
                // 更新失败，说明数量已被其他请求修改，重试一次
                CartItem fresh = cartMapper.selectById(existing.getId());
                fresh.setQuantity(fresh.getQuantity() + quantity);
                return cartMapper.updateById(fresh) > 0;
            }
            return true;
        } else {
            // 检查是否有其他商家的商品，如果有则清空
            LambdaQueryWrapper<CartItem> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(CartItem::getUserId, userId);
            List<CartItem> existingItems = cartMapper.selectList(checkWrapper);
            if (!existingItems.isEmpty() && !existingItems.get(0).getMerchantId().equals(merchantId)) {
                clearCart(userId);
            }

            // 新增购物车项
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setMerchantId(merchantId);
            item.setDishId(dishId);
            item.setQuantity(quantity);
            return cartMapper.insert(item) > 0;
        }
    }

    public boolean updateQuantity(Integer id, int quantity) {
        if (quantity <= 0) {
            return cartMapper.deleteById(id) > 0;
        }
        LambdaUpdateWrapper<CartItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CartItem::getId, id).set(CartItem::getQuantity, quantity);
        return cartMapper.update(null, wrapper) > 0;
    }

    public boolean removeItem(Integer id) {
        return cartMapper.deleteById(id) > 0;
    }

    public boolean clearCart(String userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        return cartMapper.delete(wrapper) >= 0;
    }

    public List<CartItem> getCartItems(String userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);
        List<CartItem> items = cartMapper.selectList(wrapper);
        for (CartItem item : items) {
            Dish dish = dishMapper.selectById(item.getDishId());
            item.setDish(dish);
        }
        return items;
    }
}
