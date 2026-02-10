-- 学习笔记数据库表结构
CREATE DATABASE IF NOT EXISTS learning_notes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE learning_notes;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 笔记表
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '笔记标题',
    content TEXT COMMENT 'Markdown内容',
    file_name VARCHAR(255) COMMENT '原始文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    category VARCHAR(100) COMMENT '分类标签',
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开：0-私有，1-公开',
    user_id BIGINT COMMENT '用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_title (title),
    INDEX idx_is_public (is_public),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习笔记表';

-- 笔记分类表
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(500) COMMENT '分类描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记分类表';

-- 笔记标签表
CREATE TABLE IF NOT EXISTS tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记标签表';

-- 笔记标签关联表（多对多关系）
CREATE TABLE IF NOT EXISTS note_tags (
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    tag_id INT NOT NULL COMMENT '标签ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (note_id, tag_id),
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记标签关联表';

-- 插入默认分类
INSERT IGNORE INTO categories (name, description) VALUES
('编程学习', '编程语言和技术学习笔记'),
('算法数据结构', '算法和数据结构相关笔记'),
('数据库', '数据库技术学习笔记'),
('前端开发', '前端技术学习笔记'),
('后端开发', '后端技术学习笔记'),
('系统设计', '系统架构和设计模式笔记'),
('工具使用', '开发工具和效率工具笔记'),
('其他', '其他学习笔记');

-- 笔记分享表
CREATE TABLE IF NOT EXISTS note_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    share_code VARCHAR(20) NOT NULL UNIQUE COMMENT '分享短码',
    share_title VARCHAR(100) COMMENT '分享标题（自定义）',
    created_by BIGINT COMMENT '创建分享的用户ID',
    view_count INT DEFAULT 0 COMMENT '分享浏览次数',
    expires_at DATETIME COMMENT '过期时间（NULL表示永不过期）',
    is_active TINYINT DEFAULT 1 COMMENT '是否有效：0-无效，1-有效',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_share_code (share_code),
    INDEX idx_note_id (note_id),
    INDEX idx_created_by (created_by),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记分享表';

-- 插入常用标签
INSERT IGNORE INTO tags (name) VALUES
('Java'), ('Python'), ('JavaScript'), ('Vue'), ('Spring'), ('MySQL'),
('Redis'), ('Docker'), ('Linux'), ('Git'), ('算法'), ('数据结构'),
('设计模式'), ('微服务'), ('RESTful'), ('HTTP'), ('网络'), ('操作系统');

