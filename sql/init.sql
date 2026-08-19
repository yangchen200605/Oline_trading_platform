CREATE DATABASE IF NOT EXISTS trading_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE trading_platform;

-- 员工（管理端）
CREATE TABLE employee (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(32)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(64)  NOT NULL COMMENT '密码(MD5/BCrypt)',
    name        VARCHAR(32)  NOT NULL COMMENT '姓名',
    phone       VARCHAR(11)  DEFAULT NULL,
    sex         VARCHAR(2)   DEFAULT NULL,
    id_number   VARCHAR(18)  DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    create_user BIGINT       DEFAULT NULL,
    update_user BIGINT       DEFAULT NULL
) COMMENT '员工表';

-- C端用户
CREATE TABLE user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid      VARCHAR(64)  DEFAULT NULL COMMENT '微信openid等',
    name        VARCHAR(32)  DEFAULT NULL,
    phone       VARCHAR(11)  DEFAULT NULL,
    sex         VARCHAR(2)   DEFAULT NULL,
    avatar      VARCHAR(500) DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL
) COMMENT '用户表';

-- 分类
CREATE TABLE category (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    type        TINYINT      NOT NULL COMMENT '1菜品分类 2套餐分类',
    name        VARCHAR(32)  NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    create_user BIGINT       DEFAULT NULL,
    update_user BIGINT       DEFAULT NULL
) COMMENT '分类表';

-- 商品/菜品
CREATE TABLE dish (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(64)  NOT NULL,
    category_id BIGINT       NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    image       VARCHAR(500) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1起售 0停售',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    create_user BIGINT       DEFAULT NULL,
    update_user BIGINT       DEFAULT NULL,
    INDEX idx_category_id (category_id)
) COMMENT '菜品表';

-- 菜品口味
CREATE TABLE dish_flavor (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT       NOT NULL,
    name    VARCHAR(32)  DEFAULT NULL COMMENT '口味名称，如辣度',
    value   VARCHAR(255) DEFAULT NULL COMMENT '口味选项 JSON，如 ["微辣","中辣"]',
    INDEX idx_dish_id (dish_id)
) COMMENT '菜品口味';

-- 套餐
CREATE TABLE setmeal (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT        NOT NULL,
    name        VARCHAR(32)   NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1起售 0停售',
    description VARCHAR(255)  DEFAULT NULL,
    image       VARCHAR(500)  DEFAULT NULL,
    create_time DATETIME      DEFAULT NULL,
    update_time DATETIME      DEFAULT NULL,
    create_user BIGINT        DEFAULT NULL,
    update_user BIGINT        DEFAULT NULL,
    INDEX idx_category_id (category_id)
) COMMENT '套餐';

-- 套餐菜品关系
CREATE TABLE setmeal_dish (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    setmeal_id BIGINT        DEFAULT NULL,
    dish_id    BIGINT        DEFAULT NULL,
    name      VARCHAR(32)   DEFAULT NULL,
    price     DECIMAL(10,2) DEFAULT NULL,
    copies    INT           DEFAULT NULL COMMENT '份数',
    INDEX idx_setmeal_id (setmeal_id)
) COMMENT '套餐菜品关系';

-- 购物车
CREATE TABLE shopping_cart (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(64)  DEFAULT NULL,
    image       VARCHAR(500) DEFAULT NULL,
    user_id     BIGINT       NOT NULL,
    dish_id     BIGINT       DEFAULT NULL,
    setmeal_id  BIGINT       DEFAULT NULL,
    dish_flavor VARCHAR(50)  DEFAULT NULL,
    number      INT          NOT NULL DEFAULT 1,
    amount      DECIMAL(10,2) NOT NULL,
    create_time DATETIME     DEFAULT NULL,
    INDEX idx_user_id (user_id)
) COMMENT '购物车';

-- 订单
CREATE TABLE orders (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
    number                  VARCHAR(50)  DEFAULT NULL COMMENT '订单号',
    status                  INT          NOT NULL DEFAULT 1 COMMENT '1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
    user_id                 BIGINT       NOT NULL,
    address_book_id         BIGINT       DEFAULT NULL,
    order_time              DATETIME     DEFAULT NULL,
    checkout_time           DATETIME     DEFAULT NULL,
    pay_method              INT          DEFAULT 1 COMMENT '1微信 2支付宝',
    pay_status              TINYINT      NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2退款',
    amount                  DECIMAL(10,2) NOT NULL,
    remark                  VARCHAR(100) DEFAULT NULL,
    phone                   VARCHAR(11)  DEFAULT NULL,
    address                 VARCHAR(255) DEFAULT NULL,
    user_name               VARCHAR(32)  DEFAULT NULL,
    consignee               VARCHAR(32)  DEFAULT NULL,
    cancel_reason           VARCHAR(255) DEFAULT NULL,
    rejection_reason        VARCHAR(255) DEFAULT NULL,
    cancel_time             DATETIME     DEFAULT NULL,
    estimated_delivery_time DATETIME     DEFAULT NULL,
    delivery_status         TINYINT      DEFAULT 1,
    delivery_time           DATETIME     DEFAULT NULL,
    pack_amount             INT          DEFAULT 0,
    tableware_number        INT          DEFAULT 0,
    tableware_status        TINYINT      DEFAULT 1,
    INDEX idx_user_id (user_id),
    INDEX idx_status_order_time (status, order_time)
) COMMENT '订单表';

-- 订单明细
CREATE TABLE order_detail (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(64)  DEFAULT NULL,
    image       VARCHAR(500) DEFAULT NULL,
    order_id    BIGINT       NOT NULL,
    dish_id     BIGINT       DEFAULT NULL,
    setmeal_id  BIGINT       DEFAULT NULL,
    dish_flavor VARCHAR(50)  DEFAULT NULL,
    number      INT          NOT NULL DEFAULT 1,
    amount      DECIMAL(10,2) NOT NULL,
    INDEX idx_order_id (order_id)
) COMMENT '订单明细';

-- 地址簿（下单配送用）
CREATE TABLE address_book (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    consignee     VARCHAR(50)  DEFAULT NULL,
    sex           VARCHAR(2)   DEFAULT NULL,
    phone         VARCHAR(11)  NOT NULL,
    province_code VARCHAR(12)  DEFAULT NULL,
    province_name VARCHAR(32)  DEFAULT NULL,
    city_code     VARCHAR(12)  DEFAULT NULL,
    city_name     VARCHAR(32)  DEFAULT NULL,
    district_code VARCHAR(12)  DEFAULT NULL,
    district_name VARCHAR(32)  DEFAULT NULL,
    detail        VARCHAR(200) DEFAULT NULL,
    label         VARCHAR(100) DEFAULT NULL,
    is_default    TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) COMMENT '地址簿';

-- 初始管理员（密码为 123456 的 MD5，后续若改用 BCrypt 需同步修改）
INSERT INTO employee (username, password, name, status, create_time, update_time)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 1, NOW(), NOW());
