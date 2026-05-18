/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : 192.168.1.1:3306
 Source Schema         : pt

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 12/05/2026 13:48:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for addresses
-- ----------------------------
DROP TABLE IF EXISTS `addresses`;
CREATE TABLE `addresses`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地址ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `building` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楼栋',
  `room` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间号',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `is_default` tinyint(1) NOT NULL DEFAULT 0,
  `lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `lng` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '地址表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of addresses
-- ----------------------------
INSERT INTO `addresses` VALUES ('addr_d40442f9', 'student_001', 'A栋', '1001', '王', '15617098712', 1, NULL, NULL, '2026-04-15 23:28:37');

-- ----------------------------
-- Table structure for cart_items
-- ----------------------------
DROP TABLE IF EXISTS `cart_items`;
CREATE TABLE `cart_items`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `merchant_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `dish_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dish`(`user_id` ASC, `dish_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_merchant`(`user_id` ASC, `merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '购物车表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart_items
-- ----------------------------

-- ----------------------------
-- Table structure for coupons
-- ----------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券ID',
  `merchant_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券名称',
  `threshold_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额',
  `discount_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `expire_at` datetime NOT NULL COMMENT '过期时间',
  `total_count` int NOT NULL DEFAULT 1 COMMENT '总发放量',
  `remain_count` int NOT NULL DEFAULT 1 COMMENT '剩余数量',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `claim_limit_per_user` int NOT NULL DEFAULT 1 COMMENT '每个用户限领数量',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coupon_merchant`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_merchant_enabled`(`merchant_id` ASC, `is_enabled` ASC) USING BTREE,
  INDEX `idx_enabled_expire`(`is_enabled` ASC, `expire_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家优惠券表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupons
-- ----------------------------
INSERT INTO `coupons` VALUES ('cp_3a278900', 'm_001', '满10减1', 10.00, 1.00, '2026-05-10 00:00:00', 50, 47, 1, '2026-04-15 23:27:38', 1);

-- ----------------------------
-- Table structure for delivery_tracks
-- ----------------------------
DROP TABLE IF EXISTS `delivery_tracks`;
CREATE TABLE `delivery_tracks`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单ID',
  `rider_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '骑手ID',
  `lat` decimal(10, 6) NOT NULL COMMENT '纬度',
  `lng` decimal(10, 6) NOT NULL COMMENT '经度',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址描述',
  `status` enum('to_merchant','at_merchant','to_student','delivered') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '配送轨迹表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of delivery_tracks
-- ----------------------------

-- ----------------------------
-- Table structure for dishes
-- ----------------------------
DROP TABLE IF EXISTS `dishes`;
CREATE TABLE `dishes`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `merchant_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '菜品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '图片URL',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类',
  `stock` int NOT NULL DEFAULT 0,
  `sales` int NOT NULL DEFAULT 0,
  `is_available` tinyint(1) NOT NULL DEFAULT 1,
  `is_group_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否支持拼单',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `group_target_count` int NULL DEFAULT NULL COMMENT '拼单成团人数',
  `group_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '拼单价',
  `group_duration_minutes` int NULL DEFAULT 30 COMMENT '拼单时长（分钟）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_merchant_available`(`merchant_id` ASC, `is_available` ASC) USING BTREE,
  INDEX `idx_group_enabled`(`is_group_enabled` ASC, `is_available` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dishes
-- ----------------------------
INSERT INTO `dishes` VALUES ('d_186d8df9', 'm_002', '拉面', '拉面', 10.00, NULL, '/img/0f45159da38a4c5f88b9de07a6800747.png', '面食', 1000, 0, 1, 0, '2026-04-15 23:14:24', '2026-04-15 23:14:24', NULL, NULL, 30);
INSERT INTO `dishes` VALUES ('d_5b565930', 'm_001', '农夫山泉', '农夫山泉', 2.00, NULL, '/img/1598059017fb42209ad1d3c2bdc8b9ce.png', '饮料', 1999, 1, 1, 0, '2026-04-15 23:21:05', '2026-05-06 11:11:15', NULL, NULL, 30);
INSERT INTO `dishes` VALUES ('d_76a61ca4', 'm_001', '招牌黄焖鸡', '香辣招牌黄焖鸡', 18.00, 18.00, '/img/ebec0a98c69549d582d8cf7054e96e84.png', '招牌', 87, 12, 1, 1, '2026-04-15 23:07:11', '2026-05-12 12:39:34', 2, 16.00, 30);
INSERT INTO `dishes` VALUES ('d_963e16ba', 'm_001', '怡宝', '怡宝', 2.00, NULL, '/img/cb444e4926e04a37bbeafca7286a5e62.png', '饮料', 1000, 0, 1, 0, '2026-04-15 23:20:12', '2026-04-15 23:20:12', NULL, NULL, 30);
INSERT INTO `dishes` VALUES ('d_b4e6146a', 'm_001', '小份黄焖鸡', '小份黄焖鸡', 15.00, 15.00, '/img/64612e85fef7404b905225e6744b06d9.png', '招牌', 1000, 0, 1, 1, '2026-04-15 23:08:28', '2026-04-17 21:24:27', 3, 10.00, 30);
INSERT INTO `dishes` VALUES ('d_bfdc0a40', 'm_001', '百事可乐', '好喝的可乐', 2.50, NULL, '/img/fa05c466ccf24b779d3ce61dcff333a1.webp', '饮料', 2, 0, 1, 0, '2026-04-28 18:15:38', '2026-04-28 18:15:38', NULL, NULL, 30);
INSERT INTO `dishes` VALUES ('d_f96773a4', 'm_002', '馄饨', '美味馄饨', 8.00, 8.00, '/img/9c208b1d56784a2f960d54caaaf9836f.png', '面食', 1000, 0, 1, 0, '2026-04-15 23:13:37', '2026-05-12 13:00:59', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for group_coupon_usage
-- ----------------------------
DROP TABLE IF EXISTS `group_coupon_usage`;
CREATE TABLE `group_coupon_usage`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `coupon_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `group_order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '拼单ID',
  `used_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  `discount_amount` decimal(10, 2) NOT NULL COMMENT '优惠金额',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_coupon_user`(`coupon_id` ASC, `user_id` ASC, `group_order_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_coupon`(`coupon_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼单优惠券使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of group_coupon_usage
-- ----------------------------

-- ----------------------------
-- Table structure for group_coupons
-- ----------------------------
DROP TABLE IF EXISTS `group_coupons`;
CREATE TABLE `group_coupons`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券ID',
  `merchant_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商家ID，平台券为platform',
  `coupon_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '券类型: dish-菜品券/shop-店铺券/platform-平台券',
  `dish_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜品ID（仅dish类型需要）',
  `discount_rate` decimal(4, 2) NOT NULL COMMENT '折扣率（如0.80表示8折）',
  `min_people` int NOT NULL DEFAULT 2 COMMENT '最低拼单人数',
  `total_limit` int NULL DEFAULT NULL COMMENT '总限量（NULL=不限）',
  `used_count` int NULL DEFAULT 0 COMMENT '已使用次数',
  `per_user_limit` int NULL DEFAULT 1 COMMENT '每人限用次数',
  `valid_from` datetime NOT NULL COMMENT '有效期开始',
  `valid_until` datetime NOT NULL COMMENT '有效期结束',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态: active-活跃/paused-暂停/expired-过期',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_type`(`merchant_id` ASC, `coupon_type` ASC) USING BTREE,
  INDEX `idx_dish`(`dish_id` ASC) USING BTREE,
  INDEX `idx_valid_period`(`valid_from` ASC, `valid_until` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼单优惠券表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of group_coupons
-- ----------------------------

-- ----------------------------
-- Table structure for group_order_members
-- ----------------------------
DROP TABLE IF EXISTS `group_order_members`;
CREATE TABLE `group_order_members`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '拼单ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名称',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_go_user`(`group_order_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_gom_go`(`group_order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '拼单成员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_order_members
-- ----------------------------
INSERT INTO `group_order_members` VALUES (1, 'go_08decdeb', 'student_001', '张三', 1, 16.00, '2026-04-15 23:23:36');
INSERT INTO `group_order_members` VALUES (2, 'go_945ee2de', 'student_001', '张三', 1, 10.00, '2026-04-15 23:23:39');
INSERT INTO `group_order_members` VALUES (3, 'go_945ee2de', 'student_002', '李四', 1, 10.00, '2026-04-15 23:30:00');
INSERT INTO `group_order_members` VALUES (4, 'go_945ee2de', 'student_003', '王五', 1, 10.00, '2026-04-15 23:30:35');
INSERT INTO `group_order_members` VALUES (5, 'go_1af6cfe9', 'student_001', '张三', 1, 10.00, '2026-04-17 21:24:51');
INSERT INTO `group_order_members` VALUES (6, 'go_56e0f967', 'student_001', '张三', 1, 16.00, '2026-04-28 13:54:32');
INSERT INTO `group_order_members` VALUES (7, 'go_f8021041', 'student_b6a1985b', '王雯艳', 1, 16.00, '2026-05-06 08:28:41');
INSERT INTO `group_order_members` VALUES (8, 'go_f8021041', 'student_001', '张三', 1, 16.00, '2026-05-06 08:29:25');
INSERT INTO `group_order_members` VALUES (9, 'go_11ea6a76', 'student_b6a1985b', '王雯艳', 1, 16.00, '2026-05-06 08:55:58');
INSERT INTO `group_order_members` VALUES (10, 'go_c9f3ffdf', 'student_b6a1985b', '王雯艳', 1, 10.00, '2026-05-06 09:05:40');
INSERT INTO `group_order_members` VALUES (11, 'go_794254bd', 'student_001', '张三', 1, 16.00, '2026-05-06 13:15:11');
INSERT INTO `group_order_members` VALUES (12, 'go_794254bd', 'student_b6a1985b', '王雯艳', 1, 16.00, '2026-05-06 13:15:46');
INSERT INTO `group_order_members` VALUES (13, 'go_25411f7e', 'student_001', '张三', 1, 16.00, '2026-05-06 13:26:44');
INSERT INTO `group_order_members` VALUES (14, 'go_25411f7e', 'student_002', '李四', 1, 16.00, '2026-05-06 13:27:04');
INSERT INTO `group_order_members` VALUES (15, 'go_1959cac3', 'student_001', '张三', 1, 16.00, '2026-05-06 15:03:46');
INSERT INTO `group_order_members` VALUES (16, 'go_af249edb', 'student_001', '张三', 1, 16.00, '2026-05-06 15:03:46');
INSERT INTO `group_order_members` VALUES (17, 'go_1959cac3', 'student_002', '李四', 1, 16.00, '2026-05-06 15:04:40');
INSERT INTO `group_order_members` VALUES (18, 'go_e21c48af', 'student_001', '张三', 1, 16.00, '2026-05-06 15:09:19');
INSERT INTO `group_order_members` VALUES (19, 'go_e21c48af', 'student_002', '李四', 1, 16.00, '2026-05-06 15:09:33');
INSERT INTO `group_order_members` VALUES (20, 'go_834c9918', 'student_001', '张三', 1, 16.00, '2026-05-08 20:10:15');
INSERT INTO `group_order_members` VALUES (21, 'go_8ff83942', 'student_001', '张三', 1, 16.00, '2026-05-08 20:44:48');
INSERT INTO `group_order_members` VALUES (22, 'go_8ff83942', 'student_002', '李四', 1, 16.00, '2026-05-08 20:45:05');

-- ----------------------------
-- Table structure for group_orders
-- ----------------------------
DROP TABLE IF EXISTS `group_orders`;
CREATE TABLE `group_orders`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '拼单ID',
  `merchant_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `merchant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家名称',
  `dish_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `dish_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜品图',
  `initiator_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发起人ID',
  `initiator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发起人名称',
  `target_count` int NOT NULL DEFAULT 2 COMMENT '目标人数',
  `current_count` int NOT NULL DEFAULT 1 COMMENT '当前人数',
  `unit_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'open' COMMENT 'open/full/accepted/cancelled',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `accepted_at` datetime NULL DEFAULT NULL,
  `expire_at` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `cancelled_at` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因：timeout/expired/manual/initiator_cancel/merchant_cancel/admin_cancel',
  `cancelled_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消人ID',
  `coupon_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '使用的优惠券ID',
  `discount_rate` decimal(4, 2) NULL DEFAULT NULL COMMENT '实际折扣率',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '菜品原价',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_go_merchant`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_go_status`(`status` ASC) USING BTREE,
  INDEX `idx_status_initiator`(`status` ASC, `initiator_id` ASC) USING BTREE,
  INDEX `idx_status_merchant`(`status` ASC, `merchant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '拼单主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_orders
-- ----------------------------
INSERT INTO `group_orders` VALUES ('go_08decdeb', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 1, 16.00, 'cancelled', '', '2026-04-15 23:23:36', NULL, NULL, '2026-04-28 14:05:06', 'initiator_cancel', 'student_001', NULL, NULL, NULL);
INSERT INTO `group_orders` VALUES ('go_11ea6a76', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_b6a1985b', '王雯艳', 2, 1, 16.00, 'cancelled', '', '2026-05-06 08:55:57', NULL, '2026-05-06 09:25:57', '2026-05-06 09:05:51', 'initiator_cancel', 'student_b6a1985b', NULL, NULL, NULL);
INSERT INTO `group_orders` VALUES ('go_1959cac3', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 2, 16.00, 'accepted', '', '2026-05-06 15:03:46', '2026-05-06 15:06:01', '2026-05-06 15:33:46', NULL, NULL, NULL, NULL, NULL, 18.00);
INSERT INTO `group_orders` VALUES ('go_1af6cfe9', 'm_001', '黄焖鸡米饭', 'd_b4e6146a', '小份黄焖鸡', '/img/64612e85fef7404b905225e6744b06d9.png', 'student_001', '张三', 3, 1, 10.00, 'cancelled', '', '2026-04-17 21:24:51', NULL, '2026-04-17 21:54:51', '2026-04-20 22:56:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `group_orders` VALUES ('go_25411f7e', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 2, 16.00, 'accepted', '', '2026-05-06 13:26:44', '2026-05-06 13:27:21', '2026-05-06 13:56:44', NULL, NULL, NULL, NULL, NULL, 18.00);
INSERT INTO `group_orders` VALUES ('go_56e0f967', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 1, 16.00, 'cancelled', '', '2026-04-28 13:54:32', NULL, '2026-04-28 14:24:32', '2026-04-28 14:05:20', 'merchant_cancel', 'm_001', NULL, NULL, NULL);
INSERT INTO `group_orders` VALUES ('go_794254bd', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 2, 16.00, 'accepted', '', '2026-05-06 13:15:11', '2026-05-06 13:16:03', '2026-05-06 13:45:11', NULL, NULL, NULL, NULL, NULL, 18.00);
INSERT INTO `group_orders` VALUES ('go_834c9918', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 1, 16.00, 'cancelled', '', '2026-05-08 20:10:15', NULL, '2026-05-08 20:40:15', '2026-05-08 20:41:10', NULL, NULL, NULL, NULL, 18.00);
INSERT INTO `group_orders` VALUES ('go_8ff83942', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 2, 16.00, 'accepted', '', '2026-05-08 20:44:48', '2026-05-08 20:45:19', '2026-05-08 21:14:48', NULL, NULL, NULL, NULL, NULL, 18.00);
INSERT INTO `group_orders` VALUES ('go_945ee2de', 'm_001', '黄焖鸡米饭', 'd_b4e6146a', '小份黄焖鸡', '/img/64612e85fef7404b905225e6744b06d9.png', 'student_001', '张三', 3, 3, 10.00, 'accepted', '', '2026-04-15 23:23:39', '2026-04-15 23:30:48', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `group_orders` VALUES ('go_af249edb', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 1, 16.00, 'cancelled', '', '2026-05-06 15:03:46', NULL, '2026-05-06 15:33:46', '2026-05-06 15:03:54', 'initiator_cancel', 'student_001', NULL, NULL, 18.00);
INSERT INTO `group_orders` VALUES ('go_c9f3ffdf', 'm_001', '黄焖鸡米饭', 'd_b4e6146a', '小份黄焖鸡', '/img/64612e85fef7404b905225e6744b06d9.png', 'student_b6a1985b', '王雯艳', 3, 1, 10.00, 'cancelled', '', '2026-05-06 09:05:40', NULL, '2026-05-06 09:35:40', '2026-05-06 09:05:48', 'initiator_cancel', 'student_b6a1985b', NULL, NULL, NULL);
INSERT INTO `group_orders` VALUES ('go_e21c48af', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_001', '张三', 2, 2, 16.00, 'accepted', '', '2026-05-06 15:09:19', '2026-05-06 15:10:09', '2026-05-06 15:39:19', NULL, NULL, NULL, NULL, NULL, 18.00);
INSERT INTO `group_orders` VALUES ('go_f8021041', 'm_001', '黄焖鸡米饭', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 'student_b6a1985b', '王雯艳', 2, 2, 16.00, 'accepted', '', '2026-05-06 08:28:40', '2026-05-06 08:47:17', '2026-05-06 08:58:40', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for merchants
-- ----------------------------
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联用户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '商家Logo',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `rating` decimal(2, 1) NOT NULL DEFAULT 5.0,
  `month_sales` int NOT NULL DEFAULT 0,
  `categories` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '经营分类(JSON数组)',
  `is_open` tinyint(1) NOT NULL DEFAULT 1,
  `delivery_fee` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `min_order` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `delivery_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '30分钟' COMMENT '配送时间',
  `lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `lng` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_open`(`is_open` ASC) USING BTREE,
  INDEX `idx_open_sales`(`is_open` ASC, `month_sales` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchants
-- ----------------------------
INSERT INTO `merchants` VALUES ('m_001', 'merchant_001', '黄焖鸡米饭', '/img/5f09bc82a9ce4621aedb2a22af13420d.png', '正宗黄焖鸡米饭，鲜嫩多汁', '学生食堂一楼A01', '13800000010', 4.5, 19, '[\"快餐\",\"米饭\"]', 1, 3.00, 15.00, '30分钟', 30.123456, 120.123456, '2025-12-20 11:38:55', '2026-05-12 13:46:15');
INSERT INTO `merchants` VALUES ('m_002', 'merchant_002', '沙县小吃', '/img/aee90bd0ecf64aa1a33ebae2ce5cb3bc.png', '正宗沙县小吃，物美价廉', '学生食堂一楼A02', '13800000011', 4.5, 0, '[\"快餐\",\"小吃\",\"面食\"]', 1, 2.00, 10.00, '25分钟', 30.123457, 120.123457, '2025-12-20 11:38:55', '2026-05-12 13:46:15');
INSERT INTO `merchants` VALUES ('m_003', 'merchant_003', '兰州拉面', '/img/f87be999d8624de9952dcb0dee4abfa7.png', '手工拉面，汤鲜面劲', '学生食堂二楼B01', '13800000012', 4.6, 0, '[\"面食\",\"清真\"]', 1, 3.00, 12.00, '20分钟', 30.123458, 120.123458, '2025-12-20 11:38:55', '2026-05-12 13:46:15');
INSERT INTO `merchants` VALUES ('m_6977a431', 'merchant_02da4205', 'Test Shop', '', NULL, 'Test Address 123', '13800138002', 5.0, 0, '[]', 0, 5.00, 20.00, '30分钟', NULL, NULL, '2026-05-06 15:20:12', '2026-05-06 15:20:12');

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单ID',
  `dish_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称(快照)',
  `dish_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜品图片(快照)',
  `price` decimal(10, 2) NOT NULL COMMENT '单价(快照)',
  `quantity` int NOT NULL COMMENT '数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (1, 'o_6082d084', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 18.00, 1, 18.00);
INSERT INTO `order_items` VALUES (2, 'o_a71cb101', 'd_b4e6146a', '小份黄焖鸡', '/img/64612e85fef7404b905225e6744b06d9.png', 10.00, 1, 10.00);
INSERT INTO `order_items` VALUES (3, 'o_c0e073ab', 'd_b4e6146a', '小份黄焖鸡', '/img/64612e85fef7404b905225e6744b06d9.png', 10.00, 1, 10.00);
INSERT INTO `order_items` VALUES (4, 'o_bfb33f64', 'd_b4e6146a', '小份黄焖鸡', '/img/64612e85fef7404b905225e6744b06d9.png', 10.00, 1, 10.00);
INSERT INTO `order_items` VALUES (5, 'o_e969397c', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 18.00, 1, 18.00);
INSERT INTO `order_items` VALUES (6, 'o_e969397c', 'd_bfdc0a40', '百事可乐', '/img/fa05c466ccf24b779d3ce61dcff333a1.webp', 2.50, 1, 2.50);
INSERT INTO `order_items` VALUES (7, 'o_42ba0851', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (8, 'o_e98dcfdb', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (9, 'o_f78b2721', 'd_5b565930', '农夫山泉', '/img/1598059017fb42209ad1d3c2bdc8b9ce.png', 2.00, 1, 2.00);
INSERT INTO `order_items` VALUES (10, 'o_f78b2721', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 18.00, 1, 18.00);
INSERT INTO `order_items` VALUES (11, 'o_63b5be91', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (12, 'o_0791957d', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (13, 'o_514c1d9f', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (14, 'o_9b217c8f', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (15, 'o_526ae0ae', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (16, 'o_97c78279', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (17, 'o_cf2a7a04', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (18, 'o_c5638cc4', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (19, 'o_268cff87', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);
INSERT INTO `order_items` VALUES (20, 'o_b8b94890', 'd_76a61ca4', '招牌黄焖鸡', '/img/ebec0a98c69549d582d8cf7054e96e84.png', 16.00, 1, 16.00);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单ID',
  `order_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生ID',
  `merchant_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `merchant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家名称',
  `rider_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '骑手ID',
  `rider_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '骑手姓名',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '商品总价',
  `delivery_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '配送费',
  `packing_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '包装费',
  `coupon_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '优惠券ID',
  `coupon_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '优惠券名称',
  `coupon_discount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '优惠抵扣金额',
  `actual_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `status` enum('pending_payment','pending_accept','preparing','ready','delivering','completed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending_payment' COMMENT '订单状态',
  `address_building` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '楼栋',
  `address_room` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '房间号',
  `address_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `address_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `order_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'delivery' COMMENT 'delivery=外卖配送 dine_in=店内就餐',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `paid_at` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `accepted_at` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `ready_at` datetime NULL DEFAULT NULL COMMENT '备餐完成时间',
  `picked_at` datetime NULL DEFAULT NULL COMMENT '取餐时间',
  `delivered_at` datetime NULL DEFAULT NULL COMMENT '送达时间',
  `group_order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `group_order_index` int NULL DEFAULT NULL,
  `group_order_total` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_rider_id`(`rider_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_merchant_status`(`merchant_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_rider_status`(`rider_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_status_created`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_group_order_id`(`group_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES ('o_0791957d', 'GO1778044562385336', 'student_b6a1985b', 'm_001', '黄焖鸡米饭', NULL, NULL, 23.00, 7.00, 0.00, NULL, NULL, 0.00, 23.00, 'ready', '拼单自取', '', '王雯艳', '', '拼单订单#go_794254bd', 'dine_in', '2026-05-06 13:16:02', '2026-05-06 13:16:02', '2026-05-06 13:16:02', '2026-05-06 13:16:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_268cff87', 'GO1778244318399799', 'student_001', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 18.50, 2.50, 0.00, NULL, NULL, 0.00, 18.50, 'completed', '拼单外送', '', '张三', '', '拼单订单#go_8ff83942', 'delivery', '2026-05-08 20:45:18', '2026-05-08 20:45:18', '2026-05-08 20:45:18', '2026-05-12 12:25:03', '2026-05-12 12:39:29', '2026-05-12 12:39:35', 'go_8ff83942', 1, 2);
INSERT INTO `orders` VALUES ('o_42ba0851', 'GO1778028436489630', 'student_001', 'm_001', '黄焖鸡米饭', NULL, NULL, 16.00, 0.00, 0.00, NULL, NULL, 0.00, 16.00, 'completed', '拼单自取', '', '张三', '', '拼单订单#go_f8021041', 'dine_in', '2026-05-06 08:47:16', '2026-05-06 08:47:16', '2026-05-06 08:47:16', '2026-05-06 08:51:46', NULL, '2026-05-06 08:52:06', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_514c1d9f', 'GO1778045240195324', 'student_001', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 21.00, 5.00, 0.00, NULL, NULL, 0.00, 21.00, 'completed', '拼单外送', '', '张三', '', '拼单订单#go_25411f7e', 'delivery', '2026-05-06 13:27:20', '2026-05-06 13:27:20', '2026-05-06 13:27:20', '2026-05-06 13:27:46', '2026-05-06 13:50:03', '2026-05-06 13:50:10', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_526ae0ae', 'GO1778051160857577', 'student_001', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 18.50, 2.50, 0.00, NULL, NULL, 0.00, 18.50, 'completed', '拼单外送', '', '张三', '', '拼单订单#go_1959cac3', 'delivery', '2026-05-06 15:06:01', '2026-05-06 15:06:01', '2026-05-06 15:06:01', '2026-05-06 15:06:52', '2026-05-06 15:07:04', '2026-05-06 15:07:18', 'go_1959cac3', 1, 2);
INSERT INTO `orders` VALUES ('o_6082d084', 'ORD20260415982', 'student_001', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 18.00, 3.00, 2.00, 'cp_3a278900', '满10减1', 1.00, 22.00, 'completed', 'A栋', '1001', '王', '15617098712', '', 'delivery', '2026-04-15 23:28:43', '2026-04-15 23:28:43', '2026-04-15 23:29:04', '2026-04-15 23:29:07', '2026-04-15 23:29:22', '2026-04-15 23:29:26', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_63b5be91', 'GO1778044562251814', 'student_001', 'm_001', '黄焖鸡米饭', NULL, NULL, 23.00, 7.00, 0.00, NULL, NULL, 0.00, 23.00, 'completed', '拼单自取', '', '张三', '', '拼单订单#go_794254bd', 'dine_in', '2026-05-06 13:16:02', '2026-05-06 13:16:02', '2026-05-06 13:16:02', '2026-05-06 13:16:19', NULL, '2026-05-06 13:17:17', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_97c78279', 'GO1778051161000795', 'student_002', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 18.50, 2.50, 0.00, NULL, NULL, 0.00, 18.50, 'completed', '拼单外送', '', '李四', '', '拼单订单#go_1959cac3', 'delivery', '2026-05-06 15:06:01', '2026-05-06 15:06:01', '2026-05-06 15:06:01', '2026-05-06 15:06:54', '2026-05-06 15:07:04', '2026-05-06 15:11:12', 'go_1959cac3', 2, 2);
INSERT INTO `orders` VALUES ('o_9b217c8f', 'GO1778045240547774', 'student_002', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 21.00, 5.00, 0.00, NULL, NULL, 0.00, 21.00, 'completed', '拼单外送', '', '李四', '', '拼单订单#go_25411f7e', 'delivery', '2026-05-06 13:27:21', '2026-05-06 13:27:21', '2026-05-06 13:27:21', '2026-05-06 13:27:47', '2026-05-06 13:50:04', '2026-05-06 13:50:08', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_a71cb101', 'GO1776267047912692', 'student_001', 'm_001', '黄焖鸡米饭', NULL, NULL, 10.00, 0.00, 0.00, NULL, NULL, 0.00, 10.00, 'completed', '拼单自取', '', '张三', '', '拼单订单#go_945ee2de', 'dine_in', '2026-04-15 23:30:48', '2026-04-15 23:30:48', '2026-04-15 23:30:48', '2026-04-20 22:57:18', NULL, '2026-04-20 23:16:33', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_b8b94890', 'GO1778244318528711', 'student_002', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 18.50, 2.50, 0.00, NULL, NULL, 0.00, 18.50, 'completed', '拼单外送', '', '李四', '', '拼单订单#go_8ff83942', 'delivery', '2026-05-08 20:45:19', '2026-05-08 20:45:19', '2026-05-08 20:45:19', '2026-05-12 12:25:02', '2026-05-12 12:39:29', '2026-05-12 12:39:33', 'go_8ff83942', 2, 2);
INSERT INTO `orders` VALUES ('o_bfb33f64', 'GO1776267047918224', 'student_003', 'm_001', '黄焖鸡米饭', NULL, NULL, 10.00, 0.00, 0.00, NULL, NULL, 0.00, 10.00, 'completed', '拼单自取', '', '王五', '', '拼单订单#go_945ee2de', 'dine_in', '2026-04-15 23:30:48', '2026-04-15 23:30:48', '2026-04-15 23:30:48', '2026-04-20 22:57:27', NULL, '2026-04-20 23:06:18', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_c0e073ab', 'GO1776267047915640', 'student_002', 'm_001', '黄焖鸡米饭', NULL, NULL, 10.00, 0.00, 0.00, NULL, NULL, 0.00, 10.00, 'completed', '拼单自取', '', '李四', '', '拼单订单#go_945ee2de', 'dine_in', '2026-04-15 23:30:48', '2026-04-15 23:30:48', '2026-04-15 23:30:48', '2026-04-20 22:57:28', NULL, '2026-04-20 23:05:23', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_c5638cc4', 'GO1778051408540823', 'student_002', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 18.50, 2.50, 0.00, NULL, NULL, 0.00, 18.50, 'completed', '拼单外送', '', '李四', '', '拼单订单#go_e21c48af', 'delivery', '2026-05-06 15:10:09', '2026-05-06 15:10:09', '2026-05-06 15:10:09', '2026-05-06 15:10:32', '2026-05-06 15:10:59', '2026-05-06 15:11:08', 'go_e21c48af', 2, 2);
INSERT INTO `orders` VALUES ('o_cf2a7a04', 'GO1778051408401661', 'student_001', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 18.50, 2.50, 0.00, NULL, NULL, 0.00, 18.50, 'completed', '拼单外送', '', '张三', '', '拼单订单#go_e21c48af', 'delivery', '2026-05-06 15:10:08', '2026-05-06 15:10:08', '2026-05-06 15:10:08', '2026-05-06 15:10:33', '2026-05-06 15:10:59', '2026-05-06 15:11:10', 'go_e21c48af', 1, 2);
INSERT INTO `orders` VALUES ('o_e969397c', 'ORD20260428225', 'student_001', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 20.50, 3.00, 2.00, NULL, NULL, 0.00, 25.50, 'completed', 'A栋', '1001', '王', '15617098712', '', 'delivery', '2026-04-28 18:18:20', '2026-04-28 18:18:20', '2026-04-28 18:19:31', '2026-04-28 18:19:36', '2026-04-28 18:20:00', '2026-04-28 18:20:24', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_e98dcfdb', 'GO1778028437028236', 'student_b6a1985b', 'm_001', '黄焖鸡米饭', NULL, NULL, 16.00, 0.00, 0.00, NULL, NULL, 0.00, 16.00, 'completed', '拼单自取', '', '王雯艳', '', '拼单订单#go_f8021041', 'dine_in', '2026-05-06 08:47:17', '2026-05-06 08:47:17', '2026-05-06 08:47:17', '2026-05-06 08:48:16', NULL, '2026-05-06 08:52:57', NULL, NULL, NULL);
INSERT INTO `orders` VALUES ('o_f78b2721', 'ORD20260506586', 'student_001', 'm_001', '黄焖鸡米饭', 'rider_001', '赵六', 20.00, 3.00, 2.00, NULL, NULL, 0.00, 25.00, 'completed', 'A栋', '1001', '王', '15617098712', '', 'delivery', '2026-05-06 11:06:45', '2026-05-06 11:06:45', '2026-05-06 11:06:59', '2026-05-06 11:07:03', '2026-05-06 11:11:10', '2026-05-06 11:11:16', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for reviews
-- ----------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价ID',
  `order_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单ID',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生ID',
  `merchant_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `rider_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '骑手ID',
  `merchant_rating` int NOT NULL COMMENT '商家评分(1-5)',
  `rider_rating` int NULL DEFAULT NULL COMMENT '骑手评分(1-5)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价图片(JSON数组)',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_rider_id`(`rider_id` ASC) USING BTREE,
  CONSTRAINT `chk_merchant_rating` CHECK (`merchant_rating` between 1 and 5),
  CONSTRAINT `chk_rider_rating` CHECK ((`rider_rating` is null) or (`rider_rating` between 1 and 5))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of reviews
-- ----------------------------
INSERT INTO `reviews` VALUES ('r_ccde4bce', 'o_e969397c', 'student_001', 'm_001', 'rider_001', 5, 5, '味道好，分量足，包装精美。', '[\"/img/b274c6e6994247f9a104159d45aa1192.webp\"]', '2026-04-28 18:21:19');
INSERT INTO `reviews` VALUES ('r_db8b053f', 'o_268cff87', 'student_001', 'm_001', 'rider_001', 4, 4, '服务好，配送快，味道好。', NULL, '2026-05-12 12:40:21');

-- ----------------------------
-- Table structure for rider_applications
-- ----------------------------
DROP TABLE IF EXISTS `rider_applications`;
CREATE TABLE `rider_applications`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请ID',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `available_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '可用时间段(JSON)',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '骑手申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rider_applications
-- ----------------------------
INSERT INTO `rider_applications` VALUES ('ra_199ed185', 'student_b6a1985b', '王雯艳', '15821382564', '[\"17:00-19:00\",\"12:00-14:00\"]', 'rejected', '2026-05-06 15:42:54', '2026-05-06 15:43:10');

-- ----------------------------
-- Table structure for rider_info
-- ----------------------------
DROP TABLE IF EXISTS `rider_info`;
CREATE TABLE `rider_info`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `available_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '可用时间段(JSON)',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `vehicle_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆类型: electric_bike/motorcycle/bicycle',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '审核状态: pending/approved/rejected',
  `total_deliveries` int NOT NULL DEFAULT 0,
  `rating` decimal(2, 1) NOT NULL DEFAULT 5.0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_rider_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_rider_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '骑手信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rider_info
-- ----------------------------

-- ----------------------------
-- Table structure for user_coupons
-- ----------------------------
DROP TABLE IF EXISTS `user_coupons`;
CREATE TABLE `user_coupons`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户优惠券ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `coupon_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券ID',
  `merchant_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `status` enum('claimed','used','expired') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'claimed' COMMENT 'claimed=已领取 used=已使用 expired=已过期',
  `claimed_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `used_at` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `expire_at` datetime NULL DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uc_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_uc_coupon`(`coupon_id` ASC) USING BTREE,
  INDEX `idx_uc_status`(`status` ASC) USING BTREE,
  INDEX `idx_user_status_expire`(`user_id` ASC, `status` ASC, `expire_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户领取优惠券表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_coupons
-- ----------------------------
INSERT INTO `user_coupons` VALUES ('uc_7bc3863c', 'student_002', 'cp_3a278900', 'm_001', 'claimed', '2026-05-12 13:27:06', NULL, '2026-05-10 00:00:00');
INSERT INTO `user_coupons` VALUES ('uc_97a41fab', 'student_b6a1985b', 'cp_3a278900', 'm_001', 'claimed', '2026-05-12 13:35:33', NULL, '2026-05-10 00:00:00');
INSERT INTO `user_coupons` VALUES ('uc_98091555', 'student_001', 'cp_1a5cdcdb', 'm_001', 'claimed', '2026-05-06 08:29:35', NULL, '2026-05-06 08:30:31');
INSERT INTO `user_coupons` VALUES ('uc_b114881e', 'student_001', 'cp_3a278900', 'm_001', 'used', '2026-04-15 23:27:52', '2026-04-15 23:28:43', '2031-04-17 00:00:00');
INSERT INTO `user_coupons` VALUES ('uc_dc1120fa', 'student_b6a1985b', 'cp_1a5cdcdb', 'm_001', 'claimed', '2026-05-06 08:28:24', NULL, '2026-05-06 08:30:31');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名/学号/商家ID',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role` enum('student','merchant','rider','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '头像URL',
  `is_rider` tinyint(1) NOT NULL DEFAULT 0,
  `is_disabled` tinyint(1) NOT NULL DEFAULT 0,
  `is_online` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE COMMENT '手机号唯一索引',
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role_rider`(`role` ASC, `is_rider` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('admin_001', 'admin', '123456', 'admin', '系统管理员', '13800000000', '/img/c22c200458894eacadb4f76b96cba00a.png', 0, 0, 0, '2026-04-15 11:38:34', '2026-04-15 11:38:34');
INSERT INTO `users` VALUES ('merchant_001', 'merchant1', '123456', 'merchant', '黄焖鸡老板', '13800000010', '/img/5f09bc82a9ce4621aedb2a22af13420d.png', 0, 0, 0, '2026-04-15 11:38:34', '2026-05-12 12:46:42');
INSERT INTO `users` VALUES ('merchant_002', 'merchant2', '123456', 'merchant', '沙县老板', '13800000011', '/img/aee90bd0ecf64aa1a33ebae2ce5cb3bc.png', 0, 0, 0, '2026-04-15 11:38:34', '2026-04-15 23:11:51');
INSERT INTO `users` VALUES ('merchant_003', 'merchant3', '123456', 'merchant', '兰州拉面老板', '13800000012', '/img/f87be999d8624de9952dcb0dee4abfa7.png', 0, 0, 0, '2026-04-15 11:38:34', '2026-04-15 23:14:43');
INSERT INTO `users` VALUES ('rider_001', 'rider1', '123456', 'student', '赵六', '13800000020', '', 1, 0, 1, '2026-04-15 11:38:34', '2026-05-06 08:49:30');
INSERT INTO `users` VALUES ('rider_002', 'rider2', '123456', 'student', '钱七', '13800000021', '', 1, 0, 0, '2026-04-15 11:38:34', '2026-04-15 11:38:34');
INSERT INTO `users` VALUES ('student_001', 'student1', '123456', 'student', '张三', '13800000001', '/img/af79e7b4485a456292a20ab0947e802b.jpg', 1, 0, 0, '2026-04-15 11:38:34', '2026-04-15 13:34:57');
INSERT INTO `users` VALUES ('student_002', 'student2', '123456', 'student', '李四', '13800000002', '', 1, 0, 0, '2026-04-15 11:38:34', '2026-04-15 13:44:58');
INSERT INTO `users` VALUES ('student_003', 'student3', '123456', 'student', '王五', '13800000003', '', 1, 0, 1, '2026-04-15 11:38:34', '2026-04-15 11:38:34');
INSERT INTO `users` VALUES ('student_b6a1985b', 'yyan', '$2a$10$G9bBNEagwIRmmi5/JINBmOqROSRJ8jWt/BAh990GdBHn3CFQHgzhC', 'student', '王雯艳', '15821382564', '', 0, 0, 0, '2026-04-28 16:34:43', '2026-04-28 16:34:43');

SET FOREIGN_KEY_CHECKS = 1;
