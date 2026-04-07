-- 用户管理相关表初始化SQL

-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户名',
    real_name VARCHAR(100) COMMENT '姓名',
    pwd VARCHAR(255) NOT NULL COMMENT '密码',
    sex TINYINT DEFAULT 0 COMMENT '性别 1男 0女',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员用户 (密码: admin123 经过BCrypt加密)
INSERT INTO `sys_user` (user_name, real_name, pwd, sex) VALUES
('admin', '系统管理员', '$2a$10$7zQMf0lpsMhVjbAYjpr2EeLW5KGshZNSpAZyGjlp0oKDO7KSYanU2', 1);
