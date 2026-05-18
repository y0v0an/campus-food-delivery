package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.*;
import com.community.cfgs.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private CouponService couponService;

    @Autowired
    private DishService dishService;

    @Autowired
    private com.community.cfgs.mapper.UserCouponMapper userCouponMapper;

    /**
     * 创建订单（完整事务保护：库存扣减、优惠券核销、订单创建、购物车清空）
     * @param studentId 学生ID
     * @param addressId 地址ID
     * @param address 地址对象
     * @param remark 备注
     * @param orderType 订单类型（dine_in店内就餐 / delivery配送）
     * @param userCouponId 用户优惠券ID
     * @return 创建的订单
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ, timeout = 30)
    public Order createOrder(String studentId, String addressId, Address address, String remark, String orderType, String userCouponId) {
        boolean dineIn = "dine_in".equals(orderType);
        // 获取购物车
        LambdaQueryWrapper<CartItem> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(CartItem::getUserId, studentId);
        List<CartItem> cartItems = cartMapper.selectList(cartWrapper);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        String merchantId = cartItems.get(0).getMerchantId();

        // ============ 1. 先扣减所有菜品库存（防止超卖）============
        for (CartItem cartItem : cartItems) {
            Dish dish = dishMapper.selectById(cartItem.getDishId());
            if (dish == null) {
                throw new RuntimeException("菜品不存在: " + cartItem.getDishId());
            }
            // 扣减库存，如果库存不足会抛出异常并回滚整个事务
            if (!dishService.decreaseStock(cartItem.getDishId(), cartItem.getQuantity())) {
                throw new RuntimeException("菜品[" + dish.getName() + "]库存不足");
            }
        }
        Merchant merchant = merchantMapper.selectById(merchantId);

        // 计算总价
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            Dish dish = dishMapper.selectById(cartItem.getDishId());
            if (dish == null) continue;
            
            BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setDishId(dish.getId());
            orderItem.setDishName(dish.getName());
            orderItem.setDishImage(dish.getImage());
            orderItem.setPrice(dish.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
        }

        // 创建订单
        Order order = new Order();
        order.setId("o_" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderNo(generateOrderNo());
        order.setStudentId(studentId);
        order.setMerchantId(merchantId);
        order.setMerchantName(merchant != null ? merchant.getName() : "");
        order.setTotalAmount(totalAmount);
        order.setOrderType(dineIn ? "dine_in" : "delivery");
        if (dineIn) {
            order.setDeliveryFee(BigDecimal.ZERO);
            order.setPackingFee(BigDecimal.ZERO);
            User student = userService.getById(studentId);
            order.setAddressBuilding("店内就餐");
            order.setAddressRoom("");
            order.setAddressContact(student != null && student.getName() != null ? student.getName() : "");
            order.setAddressPhone(student != null && student.getPhone() != null ? student.getPhone() : "");
        } else {
            if (address == null) {
                throw new RuntimeException("请选择配送地址");
            }
            order.setDeliveryFee(merchant != null ? merchant.getDeliveryFee() : BigDecimal.ZERO);
            order.setPackingFee(new BigDecimal("2.00"));
            order.setAddressBuilding(address.getBuilding());
            order.setAddressRoom(address.getRoom());
            order.setAddressContact(address.getContact());
            order.setAddressPhone(address.getPhone());
        }
        BigDecimal beforeDiscountAmount = totalAmount.add(order.getDeliveryFee()).add(order.getPackingFee());
        BigDecimal couponDiscount = BigDecimal.ZERO;

        // ============ 2. 核销优惠券（在同一事务内执行）============
        if (userCouponId != null && !userCouponId.trim().isEmpty()) {
            String ucId = userCouponId.trim();

            // 查询用户优惠券
            LambdaQueryWrapper<com.community.cfgs.entity.UserCoupon> ucWrapper = new LambdaQueryWrapper<>();
            ucWrapper.eq(com.community.cfgs.entity.UserCoupon::getId, ucId)
                    .eq(com.community.cfgs.entity.UserCoupon::getUserId, studentId)
                    .eq(com.community.cfgs.entity.UserCoupon::getStatus, "claimed");
            com.community.cfgs.entity.UserCoupon userCoupon = userCouponMapper.selectOne(ucWrapper);

            if (userCoupon == null) {
                throw new RuntimeException("优惠券不存在或已使用");
            }
            if (!merchantId.equals(userCoupon.getMerchantId())) {
                throw new RuntimeException("优惠券不属于当前商家");
            }

            // 查询优惠券详情
            Coupon coupon = couponService.getById(userCoupon.getCouponId());
            if (coupon == null || !Boolean.TRUE.equals(coupon.getIsEnabled())) {
                throw new RuntimeException("优惠券不可用");
            }

            // 计算折扣
            couponDiscount = couponService.calculateDiscountForOrder(coupon, totalAmount);
            if (couponDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("优惠券不满足使用条件");
            }

            // 使用乐观锁更新优惠券状态（防止并发使用）
            LambdaUpdateWrapper<com.community.cfgs.entity.UserCoupon> ucUpdateWrapper = new LambdaUpdateWrapper<>();
            ucUpdateWrapper.eq(com.community.cfgs.entity.UserCoupon::getId, ucId)
                    .eq(com.community.cfgs.entity.UserCoupon::getStatus, "claimed")
                    .set(com.community.cfgs.entity.UserCoupon::getStatus, "used")
                    .set(com.community.cfgs.entity.UserCoupon::getUsedAt, LocalDateTime.now());

            if (userCouponMapper.update(null, ucUpdateWrapper) <= 0) {
                throw new RuntimeException("优惠券核销失败，请重试");
            }

            order.setCouponId(coupon.getId());
            order.setCouponName(coupon.getName());
        }
        order.setCouponDiscount(couponDiscount);
        order.setActualAmount(beforeDiscountAmount.subtract(couponDiscount).max(BigDecimal.ZERO));
        order.setStatus("pending_accept");
        order.setRemark(remark);
        order.setCreatedAt(LocalDateTime.now());
        order.setPaidAt(LocalDateTime.now());

        orderMapper.insert(order);

        // 保存订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 清空购物车
        cartMapper.delete(cartWrapper);

        order.setItems(orderItems);
        return order;
    }

    public Order getById(String id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            fillOrderItems(order);
            fillReviewForOrder(order);
        }
        return order;
    }

    public List<Order> listByStudentId(String studentId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStudentId, studentId).orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(wrapper);
        orders.forEach(this::fillOrderItems);
        fillReviewsForOrders(orders);
        return orders;
    }

    public List<Order> listByMerchantId(String merchantId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId);
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(wrapper);
        orders.forEach(this::fillOrderItems);
        fillReviewsForOrders(orders);
        return orders;
    }

    public List<Order> listForAdmin(String keyword, String merchantId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (merchantId != null && !merchantId.trim().isEmpty()) {
            wrapper.eq(Order::getMerchantId, merchantId.trim());
        }
        if (status != null && !status.trim().isEmpty() && !"all".equals(status.trim())) {
            wrapper.eq(Order::getStatus, status.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(Order::getOrderNo, k)
                    .or().like(Order::getStudentId, k)
                    .or().like(Order::getMerchantName, k)
                    .or().like(Order::getAddressContact, k)
                    .or().like(Order::getAddressPhone, k));
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(wrapper);
        orders.forEach(this::fillOrderItems);
        fillReviewsForOrders(orders);
        fillStudentNamesForOrders(orders);
        return orders;
    }

    public List<Order> listByRiderId(String riderId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getRiderId, riderId).orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(wrapper);
        orders.forEach(this::fillOrderItems);
        return orders;
    }

    public List<Order> getOrderPool() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "ready").isNull(Order::getRiderId)
                .and(w -> w.isNull(Order::getOrderType).or().eq(Order::getOrderType, "delivery"));
        List<Order> orders = orderMapper.selectList(wrapper);
        orders.forEach(this::fillOrderItems);
        return orders;
    }

    /**
     * 更新订单状态（订单完成时增加菜品销量和商家月售）
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String orderId, String status) {
        // 先查询订单获取商家ID
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId).set(Order::getStatus, status);

        // 根据状态更新对应时间戳
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case "preparing":
                wrapper.set(Order::getAcceptedAt, now);
                break;
            case "ready":
                wrapper.set(Order::getReadyAt, now);
                break;
            case "delivering":
                wrapper.set(Order::getPickedAt, now);
                break;
            case "completed":
                wrapper.set(Order::getDeliveredAt, now);
                break;
        }

        int result = orderMapper.update(null, wrapper);

        // 订单完成时，增加菜品销量和商家月售
        if (result > 0 && "completed".equals(status)) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

            // 计算订单总数量
            int totalQuantity = 0;

            for (OrderItem item : items) {
                dishService.increaseSales(item.getDishId(), item.getQuantity());
                totalQuantity += item.getQuantity();
            }

            // 更新商家月售（使用原子操作）
            if (totalQuantity > 0 && order.getMerchantId() != null) {
                LambdaUpdateWrapper<Merchant> merchantWrapper = new LambdaUpdateWrapper<>();
                merchantWrapper.eq(Merchant::getId, order.getMerchantId())
                        .setSql(String.format("month_sales = month_sales + %d", totalQuantity));
                merchantMapper.update(null, merchantWrapper);
            }
        }

        return result > 0;
    }

    /**
     * 骑手接单
     * 如果是拼单订单，会自动接下同一拼单的所有订单
     * @param orderId 订单ID
     * @param riderId 骑手ID
     * @param riderName 骑手姓名
     * @return 是否接单成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptOrder(String orderId, String riderId, String riderName) {
        // 先查询订单，检查是否是拼单订单
        Order targetOrder = orderMapper.selectById(orderId);
        if (targetOrder == null) {
            return false;
        }

        // 防止接自己的订单
        if (riderId.equals(targetOrder.getStudentId())) {
            throw new RuntimeException("不能接自己下的订单");
        }

        // 检查是否是拼单订单
        if (targetOrder.getGroupOrderId() != null && !targetOrder.getGroupOrderId().trim().isEmpty()) {
            // 拼单订单：接下同一拼单的所有订单
            return acceptGroupOrders(targetOrder.getGroupOrderId(), riderId, riderName);
        }

        // 普通订单：只接当前订单
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
               .eq(Order::getStatus, "ready")
               .isNull(Order::getRiderId)
               .set(Order::getRiderId, riderId)
               .set(Order::getRiderName, riderName)
               .set(Order::getStatus, "delivering")
               .set(Order::getPickedAt, LocalDateTime.now());
        return orderMapper.update(null, wrapper) > 0;
    }

    /**
     * 接下拼单的所有关联订单
     * @param groupOrderId 拼单ID
     * @param riderId 骑手ID
     * @param riderName 骑手姓名
     * @return 是否接单成功
     */
    private boolean acceptGroupOrders(String groupOrderId, String riderId, String riderName) {
        // 查询同一拼单的所有订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getGroupOrderId, groupOrderId)
                   .eq(Order::getStatus, "ready")
                   .isNull(Order::getRiderId);
        List<Order> groupOrders = orderMapper.selectList(queryWrapper);

        if (groupOrders.isEmpty()) {
            return false;
        }

        // 防止接包含自己订单的拼单
        for (Order order : groupOrders) {
            if (riderId.equals(order.getStudentId())) {
                throw new RuntimeException("不能接包含自己订单的拼单");
            }
        }

        // 批量接单
        LocalDateTime now = LocalDateTime.now();
        for (Order order : groupOrders) {
            LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Order::getId, order.getId())
                   .eq(Order::getStatus, "ready")
                   .isNull(Order::getRiderId)
                   .set(Order::getRiderId, riderId)
                   .set(Order::getRiderName, riderName)
                   .set(Order::getStatus, "delivering")
                   .set(Order::getPickedAt, now);
            orderMapper.update(null, wrapper);
        }

        return true;
    }

    /**
     * 店内就餐：商家出餐后由学生确认取餐，不经过骑手。
     */
    public boolean studentConfirmPickup(String orderId, String studentId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !studentId.equals(order.getStudentId())) {
            return false;
        }
        if (!"dine_in".equals(order.getOrderType())) {
            return false;
        }
        if (!"ready".equals(order.getStatus())) {
            return false;
        }
        return updateStatus(orderId, "completed");
    }

    /**
     * 取消订单（恢复库存和优惠券）
     * @param orderId 订单ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public boolean cancelOrder(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        // 只允许取消特定状态的订单
        if (!"pending_payment".equals(order.getStatus()) && !"pending_accept".equals(order.getStatus())) {
            return false;
        }

        // 1. 恢复库存
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        for (OrderItem item : items) {
            dishService.increaseStock(item.getDishId(), item.getQuantity());
        }

        // 2. 回退优惠券状态（如果使用了优惠券）
        if (order.getCouponId() != null && !order.getCouponId().trim().isEmpty()) {
            LambdaQueryWrapper<com.community.cfgs.entity.UserCoupon> ucWrapper = new LambdaQueryWrapper<>();
            ucWrapper.eq(com.community.cfgs.entity.UserCoupon::getUserId, order.getStudentId())
                    .eq(com.community.cfgs.entity.UserCoupon::getCouponId, order.getCouponId())
                    .eq(com.community.cfgs.entity.UserCoupon::getStatus, "used");
            com.community.cfgs.entity.UserCoupon userCoupon = userCouponMapper.selectOne(ucWrapper);
            if (userCoupon != null) {
                LambdaUpdateWrapper<com.community.cfgs.entity.UserCoupon> ucUpdateWrapper = new LambdaUpdateWrapper<>();
                ucUpdateWrapper.eq(com.community.cfgs.entity.UserCoupon::getId, userCoupon.getId())
                        .set(com.community.cfgs.entity.UserCoupon::getStatus, "claimed")
                        .set(com.community.cfgs.entity.UserCoupon::getUsedAt, null);
                userCouponMapper.update(null, ucUpdateWrapper);
            }
        }

        // 3. 更新订单状态
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
               .in(Order::getStatus, "pending_payment", "pending_accept")
               .set(Order::getStatus, "cancelled");
        return orderMapper.update(null, wrapper) > 0;
    }

    @Transactional
    public boolean adminDeleteOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }
        String id = orderId.trim();
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, id);
        orderItemMapper.delete(itemWrapper);
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getOrderId, id);
        reviewMapper.delete(reviewWrapper);
        return orderMapper.deleteById(id) > 0;
    }

    /**
     * 获取订单统计数据
     * @param merchantId 商家ID
     * @param startDate 开始日期（格式：yyyy-MM-dd），为null时默认为今天
     * @param endDate 结束日期（格式：yyyy-MM-dd），为null时默认为今天
     * @return 统计数据
     */
    public Map<String, Object> getStats(String merchantId, String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 处理日期：默认为今天
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.toLocalDate().atTime(23, 59, 59);

        LocalDateTime start = todayStart;
        LocalDateTime end = todayEnd;

        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                start = java.time.LocalDate.parse(startDate.trim()).atStartOfDay();
                System.out.println("解析 startDate: " + startDate + " -> " + start);
            } catch (Exception e) {
                System.out.println("解析 startDate 失败: " + startDate + ", 使用今天: " + e.getMessage());
                start = todayStart;
            }
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                end = java.time.LocalDate.parse(endDate.trim()).atTime(23, 59, 59);
                System.out.println("解析 endDate: " + endDate + " -> " + end);
            } catch (Exception e) {
                System.out.println("解析 endDate 失败: " + endDate + ", 使用今天: " + e.getMessage());
                end = todayEnd;
            }
        }

        System.out.println("商家 " + merchantId + " 统计日期范围: " + start + " 至 " + end);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (merchantId != null) {
            wrapper.eq(Order::getMerchantId, merchantId);
        }
        // 添加日期范围过滤
        wrapper.ge(Order::getCreatedAt, start)
               .le(Order::getCreatedAt, end);

        List<Order> orders = orderMapper.selectList(wrapper);
        System.out.println("查询到订单数: " + orders.size());

        long totalOrders = orders.size();
        long completedOrders = orders.stream().filter(o -> "completed".equals(o.getStatus())).count();
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .map(Order::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("pendingOrders", orders.stream().filter(o -> "pending_accept".equals(o.getStatus())).count());
        stats.put("preparingOrders", orders.stream().filter(o -> "preparing".equals(o.getStatus())).count());
        stats.put("deliveringOrders", orders.stream().filter(o -> "delivering".equals(o.getStatus())).count());

        return stats;
    }

    /**
     * 兼容旧方法（获取全部历史数据）
     */
    public Map<String, Object> getStats(String merchantId) {
        return getStats(merchantId, null, null);
    }

    private void fillOrderItems(Order order) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        order.setItems(orderItemMapper.selectList(wrapper));
    }

    private void fillReviewForOrder(Order order) {
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.eq(Review::getOrderId, order.getId());
        order.setReview(reviewMapper.selectOne(w));
    }

    private void fillReviewsForOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<String> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.in(Review::getOrderId, orderIds);
        List<Review> reviews = reviewMapper.selectList(w);
        Map<String, Review> byOrderId = reviews.stream()
                .collect(Collectors.toMap(Review::getOrderId, r -> r, (a, b) -> a));
        for (Order o : orders) {
            o.setReview(byOrderId.get(o.getId()));
        }
    }

    private void fillStudentNamesForOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        for (Order o : orders) {
            if (o == null || o.getStudentId() == null || o.getStudentId().trim().isEmpty()) {
                continue;
            }
            User u = userService.getById(o.getStudentId());
            o.setStudentName(u != null && u.getName() != null && !u.getName().trim().isEmpty()
                    ? u.getName()
                    : o.getStudentId());
        }
    }

    private String generateOrderNo() {
        String prefix = "ORD";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%03d", new Random().nextInt(1000));
        return prefix + date + random;
    }

    /**
     * 商家创建免单订单（也需要扣减库存）
     * @param merchantId 商家ID
     * @param dishId 菜品ID
     * @param quantity 数量
     * @param contactName 联系人
     * @param remark 备注
     * @return 创建的订单
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public Order createFreeOrderByMerchant(String merchantId, String dishId, Integer quantity, String contactName, String remark) {
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !merchantId.equals(dish.getMerchantId())) {
            throw new RuntimeException("菜品不存在或不属于当前商家");
        }

        // 扣减库存
        if (!dishService.decreaseStock(dishId, quantity)) {
            throw new RuntimeException("菜品库存不足");
        }

        Merchant merchant = merchantMapper.selectById(merchantId);
        BigDecimal total = dish.getPrice().multiply(BigDecimal.valueOf(quantity));

        Order order = new Order();
        order.setId("o_" + UUID.randomUUID().toString().substring(0, 8));
        order.setOrderNo(generateOrderNo());
        order.setStudentId("free_order");
        order.setMerchantId(merchantId);
        order.setMerchantName(merchant != null ? merchant.getName() : "");
        order.setTotalAmount(total);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPackingFee(BigDecimal.ZERO);
        order.setCouponDiscount(total);
        order.setCouponName("商家免单");
        order.setActualAmount(BigDecimal.ZERO);
        order.setStatus("preparing");
        order.setRemark(remark);
        order.setOrderType("dine_in");
        order.setAddressBuilding("商家免单制单");
        order.setAddressRoom("");
        order.setAddressContact(contactName == null ? "到店自取" : contactName);
        order.setAddressPhone("");
        order.setCreatedAt(LocalDateTime.now());
        order.setPaidAt(LocalDateTime.now());
        order.setAcceptedAt(LocalDateTime.now());
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setDishId(dish.getId());
        item.setDishName(dish.getName());
        item.setDishImage(dish.getImage());
        item.setPrice(dish.getPrice());
        item.setQuantity(quantity);
        item.setSubtotal(total);
        orderItemMapper.insert(item);
        return order;
    }
}
