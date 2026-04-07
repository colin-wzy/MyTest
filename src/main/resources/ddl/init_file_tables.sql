-- 文件管理相关表初始化SQL

-- 创建文件表（包含文件夹）
CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径（MinIO对象名称）',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    content_type VARCHAR(100) COMMENT '文件类型/MIME类型',
    bucket_name VARCHAR(100) NOT NULL COMMENT '存储桶名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父文件夹ID，0表示根目录',
    is_folder TINYINT DEFAULT 0 COMMENT '是否文件夹 1是 0否',
    user_id BIGINT COMMENT '上传用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_file_path (bucket_name, file_path),
    INDEX idx_parent_id (parent_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 插入根目录记录（如果不存在）
INSERT IGNORE INTO sys_file (id, file_name, file_path, bucket_name, parent_id, is_folder, user_id)
VALUES (0, '根目录', '/', 'root', 0, 1, 0);