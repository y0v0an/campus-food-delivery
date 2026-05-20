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
import java.util.*;
import java.util.stream.Collectors;

import com.community.cfgs.service.GroupCouponService;

@Service
public class GroupOrderService {

    @Autowired
    private GroupOrderMapper groupOrderMapper;
    @Autowired
    private GroupOrderMemberMapper groupOrderMemberMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishService dishService;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private GroupCouponService groupCouponService;

    /**
     * 取消拼单
     * @param groupOrderId 拼单ID
     * @param userId 操作用户ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean cancelGroupOrder(String groupOrderId, String userId, String cancelReason) {
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null) {
            throw new RuntimeException("拼单不存在");
        }

        // 状态校验：只能取消 open 或 full 状态的拼单
        if (!"open".equals(groupOrder.getStatus()) && !"full".equals(groupOrder.getStatus())) {
            throw new RuntimeException("拼单已" + getStatusText(groupOrder.getStatus()) + "，无法取消");
        }

        // 权限校验：只有发起者可以取消
        if (!userId.equals(groupOrder.getInitiatorId())) {
            throw new RuntimeException("只有发起者可以取消拼单");
        }

        // 获取参与成员（用于通知）
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getGroupOrderId, groupOrderId);
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(memberWrapper);

        // 更新拼单状态
        LambdaUpdateWrapper<GroupOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupOrder::getId, groupOrderId)
                .set(GroupOrder::getStatus, "cancelled")
                .set(GroupOrder::getCancelledAt, LocalDateTime.now())
                .set(GroupOrder::getCancelReason, cancelReason != null ? cancelReason : "initiator_cancel")
                .set(GroupOrder::getCancelledBy, userId);
        boolean success = groupOrderMapper.update(null, updateWrapper) > 0;

        if (success) {
            // TODO: 这里可以添加通知逻辑，如发送消息通知参与成员
            // notifyGroupOrderMembers(members, "拼单已取消", groupOrder);
        }

        return success;
    }

    /**
     * 商家取消拼单
     * @param groupOrderId 拼单ID
     * @param merchantId 商家ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean cancelGroupOrderByMerchant(String groupOrderId, String merchantId, String cancelReason) {
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null) {
            throw new RuntimeException("拼单不存在");
        }

        // 校验是否为该商家的拼单
        if (!merchantId.equals(groupOrder.getMerchantId())) {
            throw new RuntimeException("无权操作此拼单");
        }

        // 状态校验
        if (!"open".equals(groupOrder.getStatus()) && !"full".equals(groupOrder.getStatus())) {
            throw new RuntimeException("拼单已" + getStatusText(groupOrder.getStatus()) + "，无法取消");
        }

        // 更新拼单状态
        LambdaUpdateWrapper<GroupOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupOrder::getId, groupOrderId)
                .set(GroupOrder::getStatus, "cancelled")
                .set(GroupOrder::getCancelledAt, LocalDateTime.now())
                .set(GroupOrder::getCancelReason, cancelReason != null ? cancelReason : "merchant_cancel")
                .set(GroupOrder::getCancelledBy, merchantId);
        return groupOrderMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 获取用户相关的进行中拼单列表（包括发起的和参与的）
     * @param userId 用户ID
     * @return 拼单列表
     */
    public List<GroupOrder> listMyActiveGroupOrders(String userId) {
        cancelExpiredOpenOrders();

        // 查询用户发起的拼单
        LambdaQueryWrapper<GroupOrder> initiatorWrapper = new LambdaQueryWrapper<>();
        initiatorWrapper.eq(GroupOrder::getInitiatorId, userId)
                .in(GroupOrder::getStatus, "open", "full");
        List<GroupOrder> orders = groupOrderMapper.selectList(initiatorWrapper);

        // 查询用户参与的拼单（作为成员）
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getUserId, userId);
        List<GroupOrderMember> memberRecords = groupOrderMemberMapper.selectList(memberWrapper);

        if (!memberRecords.isEmpty()) {
            List<String> groupOrderIds = memberRecords.stream()
                    .map(GroupOrderMember::getGroupOrderId)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询这些拼单的详情（排除已发起的，避免重复）
            LambdaQueryWrapper<GroupOrder> joinedWrapper = new LambdaQueryWrapper<>();
            joinedWrapper.in(GroupOrder::getId, groupOrderIds)
                    .ne(GroupOrder::getInitiatorId, userId)  // 排除自己发起的
                    .in(GroupOrder::getStatus, "open", "full");
            List<GroupOrder> joinedOrders = groupOrderMapper.selectList(joinedWrapper);

            // 合并结果
            orders.addAll(joinedOrders);
        }

        // 按创建时间排序
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        fillMembers(orders);
        fillMerchantDeliveryFee(orders);
        return orders;
    }

    /**
     * 获取用户相关的历史拼单列表（包括发起的和参与的）
     * @param userId 用户ID
     * @return 历史拼单列表（accepted, cancelled状态）
     */
    public List<GroupOrder> listMyHistoryGroupOrders(String userId) {
        // 查询用户发起的已完成或已取消的拼单
        LambdaQueryWrapper<GroupOrder> initiatorWrapper = new LambdaQueryWrapper<>();
        initiatorWrapper.eq(GroupOrder::getInitiatorId, userId)
                .in(GroupOrder::getStatus, "accepted", "cancelled");
        List<GroupOrder> orders = groupOrderMapper.selectList(initiatorWrapper);

        // 查询用户参与的拼单（作为成员）
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getUserId, userId);
        List<GroupOrderMember> memberRecords = groupOrderMemberMapper.selectList(memberWrapper);

        if (!memberRecords.isEmpty()) {
            List<String> groupOrderIds = memberRecords.stream()
                    .map(GroupOrderMember::getGroupOrderId)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询这些拼单的详情（排除已发起的，避免重复）
            LambdaQueryWrapper<GroupOrder> joinedWrapper = new LambdaQueryWrapper<>();
            joinedWrapper.in(GroupOrder::getId, groupOrderIds)
                    .ne(GroupOrder::getInitiatorId, userId)  // 排除自己发起的
                    .in(GroupOrder::getStatus, "accepted", "cancelled");
            List<GroupOrder> joinedOrders = groupOrderMapper.selectList(joinedWrapper);

            // 合并结果
            orders.addAll(joinedOrders);
        }

        // 按创建时间倒序排序
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        fillMembers(orders);
        fillMerchantDeliveryFee(orders);
        return orders;
    }

    private String getStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "open": return "进行中";
            case "full": return "已满员";
            case "accepted": return "已接单";
            case "cancelled": return "已取消";
            default: return status;
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public GroupOrder createGroupOrder(String userId, String dishId, Integer targetCountIgnored, String remark, String couponId, Map<String, String> address) {
        // 验证地址信息
        if (address == null || address.get("building") == null || address.get("phone") == null) {
            throw new RuntimeException("请选择收货地址");
        }
        cancelExpiredOpenOrders();
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !Boolean.TRUE.equals(dish.getIsAvailable())) {
            throw new RuntimeException("菜品不可用");
        }
        if (!Boolean.TRUE.equals(dish.getIsGroupEnabled())) {
            throw new RuntimeException("该菜品暂不支持拼单");
        }
        Integer targetCount = dish.getGroupTargetCount();
        if (targetCount == null || targetCount < 2) {
            throw new RuntimeException("商家未配置拼单人数");
        }
        BigDecimal unitPrice = dish.getGroupPrice() != null && dish.getGroupPrice().compareTo(BigDecimal.ZERO) > 0
                ? dish.getGroupPrice()
                : dish.getPrice();

        // 处理优惠券
        BigDecimal originalPrice = dish.getPrice();
        BigDecimal finalUnitPrice = unitPrice;
        BigDecimal discountRateValue = null;

        if (couponId != null) {
            finalUnitPrice = groupCouponService.calculateDiscountedPrice(couponId, originalPrice);
            // 获取折扣率用于记录
            List<GroupCoupon> merchantCoupons = groupCouponService.listByMerchant(dish.getMerchantId());
            discountRateValue = merchantCoupons.stream()
                    .filter(c -> c.getId().equals(couponId))
                    .findFirst()
                    .map(GroupCoupon::getDiscountRate)
                    .orElse(null);
        }
        Integer durationMinutes = dish.getGroupDurationMinutes();
        if (durationMinutes == null || durationMinutes < 1) {
            durationMinutes = 30;
        }
        User user = userMapper.selectById(userId);
        Merchant merchant = merchantMapper.selectById(dish.getMerchantId());
        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setId("go_" + UUID.randomUUID().toString().substring(0, 8));
        groupOrder.setMerchantId(dish.getMerchantId());
        groupOrder.setMerchantName(merchant != null ? merchant.getName() : "");
        groupOrder.setDishId(dish.getId());
        groupOrder.setDishName(dish.getName());
        groupOrder.setDishImage(dish.getImage());
        groupOrder.setInitiatorId(userId);
        groupOrder.setInitiatorName(user != null ? user.getName() : userId);
        groupOrder.setTargetCount(targetCount);
        groupOrder.setCurrentCount(1);
        groupOrder.setUnitPrice(finalUnitPrice);
        groupOrder.setCouponId(couponId);
        groupOrder.setDiscountRate(discountRateValue);
        groupOrder.setOriginalPrice(originalPrice);
        groupOrder.setStatus("open");
        groupOrder.setRemark(remark);
        groupOrder.setCreatedAt(LocalDateTime.now());
        groupOrder.setExpireAt(LocalDateTime.now().plusMinutes(durationMinutes));
        groupOrderMapper.insert(groupOrder);

        GroupOrderMember member = new GroupOrderMember();
        member.setGroupOrderId(groupOrder.getId());
        member.setUserId(userId);
        member.setUserName(user != null ? user.getName() : userId);
        member.setQuantity(1);
        member.setAmount(unitPrice);
        member.setCreatedAt(LocalDateTime.now());
        // 新增：设置地址信息
        if (address != null) {
            member.setAddressBuilding(address.get("building"));
            member.setAddressRoom(address.get("room"));
            member.setAddressContact(address.get("contact"));
            member.setAddressPhone(address.get("phone"));
        }
        groupOrderMemberMapper.insert(member);
        return groupOrder;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean joinGroupOrder(String groupOrderId, String userId) {
        cancelExpiredOpenOrders();
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null) return false;
        if (groupOrder.getExpireAt() != null && groupOrder.getExpireAt().isBefore(LocalDateTime.now())) {
            cancelGroupOrder(groupOrderId);
            throw new RuntimeException("拼单已超时取消");
        }
        if (!"open".equals(groupOrder.getStatus()) && !"full".equals(groupOrder.getStatus())) return false;
        if (groupOrder.getCurrentCount() >= groupOrder.getTargetCount()) return false;

        LambdaQueryWrapper<GroupOrderMember> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(GroupOrderMember::getGroupOrderId, groupOrderId)
                .eq(GroupOrderMember::getUserId, userId);
        if (groupOrderMemberMapper.selectCount(existsWrapper) > 0) {
            throw new RuntimeException("你已参与该拼单");
        }
        User user = userMapper.selectById(userId);
        GroupOrderMember member = new GroupOrderMember();
        member.setGroupOrderId(groupOrderId);
        member.setUserId(userId);
        member.setUserName(user != null ? user.getName() : userId);
        member.setQuantity(1);
        member.setAmount(groupOrder.getUnitPrice());
        member.setCreatedAt(LocalDateTime.now());
        groupOrderMemberMapper.insert(member);

        int newCount = groupOrder.getCurrentCount() + 1;
        LambdaUpdateWrapper<GroupOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupOrder::getId, groupOrderId)
                .set(GroupOrder::getCurrentCount, newCount)
                .set(GroupOrder::getStatus, newCount >= groupOrder.getTargetCount() ? "full" : "open");
        return groupOrderMapper.update(null, updateWrapper) > 0;
    }

    public List<GroupOrder> listOpenByMerchantId(String merchantId) {
        cancelExpiredOpenOrders();
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupOrder::getMerchantId, merchantId)
                .in(GroupOrder::getStatus, "open", "full")
                .orderByDesc(GroupOrder::getCreatedAt);
        List<GroupOrder> orders = groupOrderMapper.selectList(wrapper);
        fillMembers(orders);
        fillMerchantDeliveryFee(orders);
        return orders;
    }

    public List<GroupOrder> listByMerchantId(String merchantId) {
        cancelExpiredOpenOrders();
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupOrder::getMerchantId, merchantId).orderByDesc(GroupOrder::getCreatedAt);
        List<GroupOrder> orders = groupOrderMapper.selectList(wrapper);
        fillMembers(orders);
        fillMerchantDeliveryFee(orders);
        return orders;
    }

    public List<GroupOrder> listHotOpen(int limit) {
        cancelExpiredOpenOrders();
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GroupOrder::getStatus, "open", "full")
                .orderByDesc(GroupOrder::getCurrentCount)
                .orderByDesc(GroupOrder::getCreatedAt)
                .last("LIMIT " + Math.max(1, limit));
        List<GroupOrder> orders = groupOrderMapper.selectList(wrapper);
        fillMembers(orders);
        fillMerchantDeliveryFee(orders);
        return orders;
    }

    /**
     * 商家接受拼单并生成个人订单（扣减库存）
     * @param groupOrderId 拼单ID
     * @param merchantId 商家ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ, timeout = 30)
    public boolean acceptGroupOrder(String groupOrderId, String merchantId) {
        cancelExpiredOpenOrders();
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder == null || !Objects.equals(merchantId, groupOrder.getMerchantId())) {
            return false;
        }
        if (!"full".equals(groupOrder.getStatus())) {
            throw new RuntimeException("拼单未满，不能接单");
        }
        if (groupOrder.getExpireAt() != null && groupOrder.getExpireAt().isBefore(LocalDateTime.now())) {
            cancelGroupOrder(groupOrderId);
            throw new RuntimeException("拼单已超时取消");
        }

        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getGroupOrderId, groupOrderId);
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(memberWrapper);
        if (members.isEmpty()) return false;

        Dish dish = dishMapper.selectById(groupOrder.getDishId());
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }

        // 扣减库存（所有成员的总数量）
        int totalQuantity = members.size();  // 每个成员1份
        if (!dishService.decreaseStock(groupOrder.getDishId(), totalQuantity)) {
            throw new RuntimeException("菜品库存不足，无法接单");
        }

        // 获取商家配送费
        Merchant merchant = merchantMapper.selectById(groupOrder.getMerchantId());
        BigDecimal merchantDeliveryFee = merchant != null && merchant.getDeliveryFee() != null
                ? merchant.getDeliveryFee()
                : BigDecimal.ZERO;

        // 计算拼单配送费（使用商家实际配送费）
        List<BigDecimal> individualFees = members.stream()
                .map(m -> merchantDeliveryFee)
                .collect(Collectors.toList());
        BigDecimal groupDeliveryFee = groupCouponService.calculateGroupDeliveryFee(individualFees);

        // 记录优惠券使用
        if (groupOrder.getCouponId() != null) {
            BigDecimal discountAmount = groupOrder.getOriginalPrice()
                    .subtract(groupOrder.getUnitPrice())
                    .multiply(BigDecimal.valueOf(members.size()));
            groupCouponService.recordUsage(
                groupOrder.getCouponId(),
                groupOrder.getInitiatorId(),
                groupOrderId,
                discountAmount
            );
        }

        int totalMembers = members.size();
        for (int i = 0; i < members.size(); i++) {
            GroupOrderMember m = members.get(i);
            Order order = new Order();
            order.setId("o_" + UUID.randomUUID().toString().substring(0, 8));
            order.setOrderNo("GO" + System.currentTimeMillis() + new Random().nextInt(1000));
            order.setStudentId(m.getUserId());
            order.setMerchantId(groupOrder.getMerchantId());
            order.setMerchantName(groupOrder.getMerchantName());
            order.setTotalAmount(groupOrder.getUnitPrice().add(groupDeliveryFee));
            order.setDeliveryFee(groupDeliveryFee);
            order.setPackingFee(BigDecimal.ZERO);
            order.setCouponDiscount(BigDecimal.ZERO);
            order.setActualAmount(groupOrder.getUnitPrice().add(groupDeliveryFee));
            order.setStatus("preparing");
            order.setOrderType("delivery");
            order.setAddressBuilding("拼单外送");
            order.setAddressRoom("");
            order.setAddressContact(m.getUserName());
            order.setAddressPhone("");
            order.setRemark("拼单订单#" + groupOrder.getId());

            // 设置拼单关联标记
            order.setGroupOrderId(groupOrder.getId());
            order.setGroupOrderIndex(i + 1);
            order.setGroupOrderTotal(totalMembers);

            order.setCreatedAt(LocalDateTime.now());
            order.setPaidAt(LocalDateTime.now());
            order.setAcceptedAt(LocalDateTime.now());
            orderMapper.insert(order);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishImage(dish.getImage());
            item.setPrice(groupOrder.getUnitPrice());
            item.setQuantity(1);
            item.setSubtotal(groupOrder.getUnitPrice());
            orderItemMapper.insert(item);
        }

        LambdaUpdateWrapper<GroupOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupOrder::getId, groupOrderId)
                .set(GroupOrder::getStatus, "accepted")
                .set(GroupOrder::getAcceptedAt, LocalDateTime.now());
        return groupOrderMapper.update(null, updateWrapper) > 0;
    }

    private void cancelExpiredOpenOrders() {
        LambdaUpdateWrapper<GroupOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(GroupOrder::getStatus, "open", "full")
                .isNotNull(GroupOrder::getExpireAt)
                .lt(GroupOrder::getExpireAt, LocalDateTime.now())
                .set(GroupOrder::getStatus, "cancelled")
                .set(GroupOrder::getCancelledAt, LocalDateTime.now());
        groupOrderMapper.update(null, wrapper);
    }

    private void cancelGroupOrder(String groupOrderId) {
        LambdaUpdateWrapper<GroupOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GroupOrder::getId, groupOrderId)
                .in(GroupOrder::getStatus, "open", "full")
                .set(GroupOrder::getStatus, "cancelled")
                .set(GroupOrder::getCancelledAt, LocalDateTime.now());
        groupOrderMapper.update(null, wrapper);
    }

    private void fillMembers(List<GroupOrder> orders) {
        if (orders == null || orders.isEmpty()) return;
        List<String> ids = orders.stream().map(GroupOrder::getId).collect(Collectors.toList());
        LambdaQueryWrapper<GroupOrderMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GroupOrderMember::getGroupOrderId, ids).orderByAsc(GroupOrderMember::getCreatedAt);
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(wrapper);
        Map<String, List<GroupOrderMember>> byOrder = new HashMap<>();
        for (GroupOrderMember m : members) {
            byOrder.computeIfAbsent(m.getGroupOrderId(), k -> new ArrayList<>()).add(m);
        }
        for (GroupOrder order : orders) {
            order.setMembers(byOrder.getOrDefault(order.getId(), Collections.emptyList()));
        }
    }

    /**
     * 填充商家配送费
     */
    private void fillMerchantDeliveryFee(List<GroupOrder> orders) {
        if (orders == null || orders.isEmpty()) return;
        // 收集所有商家ID
        List<String> merchantIds = orders.stream()
                .map(GroupOrder::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        // 批量查询商家
        LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.in(Merchant::getId, merchantIds);
        List<Merchant> merchants = merchantMapper.selectList(merchantWrapper);
        Map<String, BigDecimal> merchantDeliveryFees = merchants.stream()
                .collect(Collectors.toMap(
                        Merchant::getId,
                        m -> m.getDeliveryFee() != null ? m.getDeliveryFee() : BigDecimal.ZERO
                ));
        // 填充配送费
        for (GroupOrder order : orders) {
            order.setMerchantDeliveryFee(merchantDeliveryFees.getOrDefault(order.getMerchantId(), BigDecimal.ZERO));
        }
    }

    public GroupOrder getDetail(String groupOrderId) {
        GroupOrder groupOrder = groupOrderMapper.selectById(groupOrderId);
        if (groupOrder != null) {
            List<GroupOrder> orders = Collections.singletonList(groupOrder);
            fillMembers(orders);
            fillMerchantDeliveryFee(orders);
        }
        return groupOrder;
    }
}
