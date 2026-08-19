# 在线交易配送平台

基于 Spring Boot 的外卖交易后端，包含管理端运营与用户端下单全流程。

## 技术栈

Spring Boot、MyBatis-Plus、MySQL、Redis、JWT、WebSocket、Spring Task、RabbitMQ、阿里云 OSS、Apache POI、Knife4j

## 功能概览

**管理端**
- 员工登录 / JWT 鉴权 / 员工管理
- 分类、菜品、套餐、口味
- 订单接单、拒单、派送、完成、取消
- 店铺营业 / 打烊
- 工作台数据、报表统计、Excel 导出
- WebSocket 来单 / 催单提醒

**用户端**
- 登录、地址簿、购物车
- 浏览菜品 / 套餐、下单、模拟支付、催单
- 历史订单、取消、再来一单

## 环境要求

- JDK 17
- MySQL 8
- Redis
- Maven
- （可选）RabbitMQ：超时关单延迟队列

## 快速启动

1. 启动 MySQL、Redis
2. 执行 `sql/init.sql` 建库建表（已有库执行 `sql/upgrade.sql`）
3. 修改 `src/main/resources/application.yml` 中的数据库账号密码
4. 启动 `OlineTradingPlatformApplication`
5. 接口测试见 `login.http`

默认管理员：`admin` / `123456`

## 接口说明

| 端 | 前缀 | Token 请求头 |
|----|------|----------------|
| 管理端 | `/admin/**` | `token` |
| 用户端 | `/user/**` | `authentication` |

- 文档：http://localhost:8080/doc.html
- 来单 WebSocket：`ws://localhost:8080/ws/order`

## 配置说明

- OSS 未配置时，图片上传会保存到本地 `uploads/`
- 未支付订单 15 分钟超时取消：默认靠定时任务；启用 RabbitMQ 时把 `oline.rabbitmq.enabled` 改为 `true`

## 项目结构
