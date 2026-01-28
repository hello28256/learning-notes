-- 学习笔记数据库表结构
CREATE DATABASE IF NOT EXISTS learning_notes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE learning_notes;

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
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_title (title)
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

-- 插入常用标签
INSERT IGNORE INTO tags (name) VALUES
('Java'), ('Python'), ('JavaScript'), ('Vue'), ('Spring'), ('MySQL'),
('Redis'), ('Docker'), ('Linux'), ('Git'), ('算法'), ('数据结构'),
('设计模式'), ('微服务'), ('RESTful'), ('HTTP'), ('网络'), ('操作系统');