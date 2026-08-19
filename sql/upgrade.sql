-- 已有库升级脚本（新库请直接执行 init.sql）
USE trading_platform;

CREATE TABLE IF NOT EXISTS dish_flavor (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT       NOT NULL,
    name    VARCHAR(32)  DEFAULT NULL COMMENT '口味名称，如辣度',
    value   VARCHAR(255) DEFAULT NULL COMMENT '口味选项 JSON，如 ["微辣","中辣"]',
    INDEX idx_dish_id (dish_id)
) COMMENT '菜品口味';

CREATE TABLE IF NOT EXISTS setmeal (
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

CREATE TABLE IF NOT EXISTS setmeal_dish (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    setmeal_id BIGINT        DEFAULT NULL,
    dish_id    BIGINT        DEFAULT NULL,
    name       VARCHAR(32)   DEFAULT NULL,
    price      DECIMAL(10,2) DEFAULT NULL,
    copies     INT           DEFAULT NULL COMMENT '份数',
    INDEX idx_setmeal_id (setmeal_id)
) COMMENT '套餐菜品关系';

-- 已有 shopping_cart / order_detail 补套餐字段（若已存在会报错，可忽略）
ALTER TABLE shopping_cart ADD COLUMN setmeal_id BIGINT DEFAULT NULL AFTER dish_id;
ALTER TABLE order_detail ADD COLUMN setmeal_id BIGINT DEFAULT NULL AFTER dish_id;
