/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.0.108_mysql
 Source Server Type    : MySQL
 Source Server Version : 80023 (8.0.23)
 Source Host           : 192.168.0.108:3312
 Source Schema         : wzy_test

 Target Server Type    : MySQL
 Target Server Version : 80023 (8.0.23)
 File Encoding         : 65001

 Date: 23/04/2026 15:52:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏂囦欢鍚嶇О',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏂囦欢璺?緞锛圡inIO瀵硅薄鍚嶇О锛',
  `file_size` bigint NULL DEFAULT 0 COMMENT '鏂囦欢澶у皬锛堝瓧鑺傦級',
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏂囦欢绫诲瀷/MIME绫诲瀷',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瀛樺偍妗跺悕绉',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '鐖舵枃浠跺すID锛?琛ㄧず鏍圭洰褰',
  `is_folder` tinyint NULL DEFAULT 0 COMMENT '鏄?惁鏂囦欢澶?1鏄?0鍚',
  `user_id` bigint NULL DEFAULT NULL COMMENT '涓婁紶鐢ㄦ埛ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_file_path`(`bucket_name` ASC, `file_path` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鏂囦欢琛' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file
-- ----------------------------
INSERT INTO `sys_file` VALUES (17, 'test', '', 0, NULL, 'first', 0, 1, 1, '2026-03-31 18:55:14', '2026-03-31 18:55:25');
INSERT INTO `sys_file` VALUES (48, 'Colin.mp4', 'Colin_1775530101883.mp4', 23356808, 'video/mp4', 'first', 0, 0, 1, '2026-04-07 10:48:24', '2026-04-07 10:48:24');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙掕壊鍚嶇О',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙掕壊鏍囪瘑',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎻忚堪',
  `status` tinyint NULL DEFAULT 1 COMMENT '鐘舵? 1鍚?敤 0绂佺敤',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瑙掕壊琛' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'ADMIN', '系统管理员，拥有所有权限', 1, '2026-03-31 14:45:16', '2026-03-31 14:45:16');
INSERT INTO `sys_role` VALUES (2, '普通用户', 'USER', '普通用户，基本操作权限', 1, '2026-03-31 14:45:16', '2026-03-31 14:45:16');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐢ㄦ埛鍚',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '濮撳悕',
  `pwd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瀵嗙爜',
  `sex` tinyint NULL DEFAULT 0 COMMENT '鎬у埆 1鐢?0濂',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_name`(`user_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛琛' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '管理员', '$2a$10$wHSZW9TT0rPvQQ00.QpYfO90KFIOn.K0/hyC5440QBuYNYr7xjgL.', 0, '2026-03-31 14:45:16', '2026-04-07 10:21:59');
INSERT INTO `sys_user` VALUES (3, 'wangzhongyu', '王钟毓', '$2a$10$dRlScIbVpUkfg09Q3ilSneSohyfaX0ZOxVubZ0MgIHM6F2/mJ7DG6', 1, '2026-03-31 15:03:32', '2026-04-07 15:49:39');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛瑙掕壊鍏宠仈琛' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-03-31 14:45:17');
INSERT INTO `sys_user_role` VALUES (5, 3, 1, '2026-03-31 15:36:51');

SET FOREIGN_KEY_CHECKS = 1;
