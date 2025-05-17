/* 用户信息表（普通用户与超管）*/
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    avatar_url VARCHAR(255),
    bio TEXT,
    role ENUM('user', 'admin') DEFAULT 'user',
    is_banned TINYINT(1) DEFAULT 0 COMMENT '是否被封禁，0-正常，1-封禁',
    is_logged_in TINYINT(1) DEFAULT 0 COMMENT '是否已登录，0-未登录，1-已登录',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否被删除，0-正常，1-删除',
    deleted_by INT COMMENT '删除该用户的操作者ID',
    last_login_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX (deleted_by)
);

/* 验证码表 */
CREATE TABLE verification_codes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type ENUM('register', 'login') NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

/* 博客内容表 */
CREATE TABLE blogs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image VARCHAR(255),
    category ENUM('科技','生活','职场','爱好','影视','美食','旅游','健身','养生'),
    tags VARCHAR(255),       -- 逗号分隔标签，如 "Python,AI"
    status ENUM('draft', 'published', 'removed','rejected') DEFAULT 'draft',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否被删除，0-正常，1-删除',
    deleted_by INT DEFAULT NULL COMMENT '删除者ID，若为NULL表示未删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (deleted_by) REFERENCES users(id)
);

/* 超管操作日志 */
CREATE TABLE admin_actions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id INT NOT NULL,
    action_type ENUM(
        'CREATE_USER', 'BAN_USER', 'UNBAN_USER', 'KICK_USER',
        'DELETE_BLOG', 'WITHDRAW_BLOG',
        'ADD_SENSITIVE_WORD', 'REMOVE_SENSITIVE_WORD'
    ) NOT NULL,
    target_id INT,
    target_type ENUM('USER','BLOG','WORD') NOT NULL,
    reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

/* 敏感词库表（管理员维护） */
CREATE TABLE sensitive_words (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) UNIQUE NOT NULL,
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否被删除，0-正常，1-删除',
    deleted_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (deleted_by) REFERENCES users(id)
);

/* 登录日志表 */
CREATE TABLE login_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

/* 封禁记录表（可记录解封时间）*/
CREATE TABLE ban_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action ENUM('ban', 'unban', 'kick') NOT NULL,
    operator_id INT NOT NULL,
    reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (operator_id) REFERENCES users(id)
);

/* 登录失败记录表（用于滑块验证）*/
CREATE TABLE failed_login_attempts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    failure_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

/* 异常记录表 */
CREATE TABLE anomalies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    blog_id INT,
    type ENUM('login_failure', 'negative_content', 'violation_image') NOT NULL,
    detail TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (blog_id) REFERENCES blogs(id)
);


