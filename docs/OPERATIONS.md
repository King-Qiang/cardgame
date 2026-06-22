# 棋牌游戏平台 — 本地开发与运行操作文档

> 适用阶段：Phase 2/3 ✅ 缺口已补齐；Phase 4 波次 1–3 ✅ 已实现；**波次 4 详细设计见两份 `DESIGN.md` §11**  
> 最后更新：2026-06-20

---

## 1. 概述

本仓库包含三个子项目：

| 项目 | 目录 | 说明 |
|------|------|------|
| 后端 | `CardGameBackend/` | Spring Boot 4.1 + JPA + Flyway + MySQL |
| 运营后台 | `cardgame-frontend/` | Vue 3 + TypeScript + Element Plus + Vite |
| 微信小程序 | `cardgame-miniprogram/` | 微信原生小程序 + TypeScript |

**数据库无需手动建表。** 只需创建空库，Flyway 会在后端首次启动时自动执行 **`V1__init_schema.sql`**（唯一迁移脚本），创建 **21 张表**并写入种子数据（含段位、Bot、系统账号 900000）。

---

## 2. 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 项目 `pom.xml` 指定 Java 17；本地用 21 亦可 |
| Maven | 内置 `./mvnw` | 无需单独安装 |
| MySQL | 8.0+ | 本地测试使用 9.x 亦可 |
| Redis | 7+（可选） | 默认关闭；启用后用于匹配队列与多实例 WS 广播 |
| Node.js | 18+ | 前端开发与构建 |
| npm | 9+ | 随 Node 安装 |

---

## 3. 目录结构

```
background/
├── docs/
│   └── OPERATIONS.md          ← 本文档
├── CardGameBackend/
│   ├── docs/
│   │   ├── DESIGN.md          ← 后端设计文档
│   │   ├── schema.sql         ← 完整 DDL 参考（含 CREATE DATABASE）
│   │   └── reset-db.sql       ← 本地删库重建（见 §8.3）
│   ├── src/main/resources/
│   │   ├── application.yaml   ← 应用配置
│   │   └── db/migration/
│   │       └── V1__init_schema.sql  ← Flyway 唯一迁移（21 表 + 种子）
│   └── mvnw                   ← Maven Wrapper
├── cardgame-frontend/
│   ├── docs/DESIGN.md         ← 运营后台设计文档
│   ├── vite.config.ts         ← 开发代理配置
│   └── package.json
└── cardgame-miniprogram/
    ├── docs/DESIGN.md         ← 小程序设计文档
    ├── miniprogram/           ← 小程序源码（微信开发者工具打开项目根）
    │   └── config/env.ts      ← API/WS 地址；真机改 DEV_LAN_HOST
    └── package.json           ← npm run typecheck
```

---

## 4. 首次部署

### 4.1 启动 MySQL

确保 MySQL 服务已运行，并能以 `root` 用户连接（默认配置无密码）。

### 4.2 创建数据库（仅需一次）

登录 MySQL 后执行：

```sql
CREATE DATABASE IF NOT EXISTS cardgame_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

> **不需要**手动执行建表 SQL。表结构由 Flyway 自动迁移。

### 4.3 配置数据库连接（可选）

默认配置见 `CardGameBackend/src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cardgame_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password:          # 本地默认为空
```

若 MySQL 有密码，修改 `password` 字段即可。也可通过环境变量覆盖：

```bash
export SPRING_DATASOURCE_PASSWORD=your_password
```

### 4.4 启动后端

```bash
cd CardGameBackend
./mvnw spring-boot:run
```

**启动成功标志：**

```
Started CardGameBackendApplication in X.XXX seconds
Tomcat started on port 8080 (http)
```

Flyway 日志应显示迁移成功，例如：

```
Successfully validated 1 migrations
Current version of schema `cardgame_db`: 1
```

JPA 校验通过后，应用即处于可用状态。

### 4.5 启动前端

新开一个终端：

```bash
cd cardgame-frontend
npm install        # 首次需要
npm run dev
```

浏览器访问：**http://localhost:5173**

Vite 会将 `/api` 请求代理到 `http://localhost:8080`（见 `vite.config.ts`）。

### 4.6 启动微信小程序（可选）

1. 确保后端已在 `8080` 运行（§4.4）
2. 用**微信开发者工具**打开目录 `cardgame-miniprogram/`（项目根，含 `project.config.json`）
3. 开发者工具 → 详情 → 本地设置 → 勾选 **不校验合法域名、web-view、TLS 版本**
4. 模拟器：自动使用 `127.0.0.1:8080`（见 `miniprogram/config/env.ts`）
5. 真机预览：将 `miniprogram/config/env.ts` 中 `DEV_LAN_HOST` 改为电脑局域网 IP（`ipconfig getifaddr en0`），手机与电脑同一 WiFi

```bash
cd cardgame-miniprogram
npm install        # 首次需要
npm run typecheck  # 可选：TS 类型检查
```

---

## 5. 默认账号

| 用途 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 运营后台 | `admin` | `admin123` | 超级管理员，首次登录后请修改 |

---

## 6. 接口与文档

### 6.1 运营后台接口概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/admin/v1/admin/auth/login` | 管理员登录 |
| 认证 | POST | `/api/admin/v1/admin/auth/logout` | 管理员登出 |
| 认证 | POST | `/api/admin/v1/admin/auth/refresh` | 刷新 Token |
| 仪表盘 | GET | `/api/admin/v1/admin/dashboard/overview` | 核心指标概览 |
| 仪表盘 | GET | `/api/admin/v1/admin/dashboard/trends?days=7` | 近 N 日趋势 |
| 用户 | GET | `/api/admin/v1/admin/users` | 用户列表 |
| 用户 | POST | `/api/admin/v1/admin/users/{id}/ban` | 封禁用户 |
| 用户 | POST | `/api/admin/v1/admin/users/{id}/unban` | 解封用户 |
| 用户 | POST | `/api/admin/v1/admin/users/{id}/adjust-wallet` | 小额直接调账 |
| 房间 | GET | `/api/admin/v1/admin/rooms` | 房间列表 |
| 房间 | POST | `/api/admin/v1/admin/rooms/{roomId}/disband` | 强制解散 |
| 房间 | POST | `/api/admin/v1/admin/rooms/{roomId}/kick` | 踢出玩家 |
| 对局 | GET | `/api/admin/v1/admin/records` | 对局记录 |
| 对局 | GET | `/api/admin/v1/admin/records/{recordId}` | 对局详情 |
| 对局 | GET | `/api/admin/v1/admin/records/{recordId}/replay` | 回放 action 列表 |
| 经济 | GET | `/api/admin/v1/admin/orders` | 充值订单列表 |
| 经济 | GET | `/api/admin/v1/admin/wallet/transactions` | 金币流水分页 |
| 经济 | POST | `/api/admin/v1/admin/wallet/adjust-requests` | 提交大额调账申请 |
| 经济 | GET | `/api/admin/v1/admin/wallet/adjust-requests` | 调账申请列表 |
| 经济 | POST | `/api/admin/v1/admin/wallet/adjust-requests/{id}/approve` | 审批通过并执行 |
| 经济 | POST | `/api/admin/v1/admin/wallet/adjust-requests/{id}/reject` | 驳回调账申请 |
| 系统 | GET | `/api/admin/v1/admin/system/configs` | 系统配置列表 |
| 系统 | GET | `/api/admin/v1/admin/system/configs/{key}` | 读取单个配置 |
| 系统 | PUT | `/api/admin/v1/admin/system/configs/{key}` | 更新单个配置 |
| 商城 | GET/POST/PUT/DELETE | `/api/admin/v1/admin/shop/items[/{id}]` | 商品 CRUD |
| 活动 | GET/POST/PUT/DELETE | `/api/admin/v1/admin/activities[/{id}]` | 活动 CRUD |
| 系统 | GET/POST/PUT/DELETE | `/api/admin/v1/admin/roles[/{id}]` | 角色 CRUD |
| 系统 | GET/POST/PUT | `/api/admin/v1/admin/admins[/{id}]` | 管理员账号 |
| 系统 | POST | `/api/admin/v1/admin/admins/{id}/reset-password` | 重置管理员密码 |
| 审计 | GET | `/api/admin/v1/admin/operation-logs` | 操作日志分页 |
| 玩家 | POST | `/api/v1/match/quick` | 快速匹配（`mode`: MATCH / RANKED） |
| 玩家 | POST | `/api/v1/rooms/pve` | 人机练习（PVE，直进对局） |
| 玩家 | POST | `/api/v1/rooms/{roomId}/bots` | 亲友房添加电脑 |
| 玩家 | GET | `/api/v1/rank/me` | 我的段位 |
| 玩家 | GET | `/api/v1/rank/leaderboard` | 排行榜 |
| 运营 | GET | `/api/admin/v1/admin/rank/leaderboard` | 段位排行榜 |
| 运营 | GET | `/api/admin/v1/admin/rank/users/{id}` | 用户段位 |
| 对局 | GET | `/api/admin/v1/admin/records/{recordId}/replay/meta` | 回放元数据 |
| 玩家 | DELETE | `/api/v1/match/quick?gameType=DOUDIZHU` | 取消匹配 |
| 玩家 | GET | `/api/v1/wallet` | 钱包余额 |
| 玩家 | GET | `/api/v1/wallet/transactions` | 金币流水 |
| 玩家 | GET | `/api/v1/shop/items` | 商城商品 |
| 玩家 | POST | `/api/v1/shop/buy` | 购买商品 |
| 玩家 | POST | `/api/v1/orders` | 创建充值订单 |
| 玩家 | POST | `/api/v1/orders/{orderNo}/pay-callback` | 支付回调（免鉴权，本地 mock） |
| 玩家 | GET | `/api/v1/activities/daily-sign` | 签到状态 |
| 玩家 | POST | `/api/v1/activities/daily-sign` | 执行签到 |

### 6.2 后台菜单路径

| 菜单 | 路径 | 所需权限 |
|------|------|----------|
| 仪表盘 | `/dashboard` | `dashboard:view` |
| 用户管理 | `/users` | `user:list` |
| 房间监控 | `/rooms` | `room:monitor` |
| 对局记录 | `/records` | `record:list` |
| 对局详情 | `/records/:recordId` | `record:list` |
| 金币流水 | `/economy/transactions` | `economy:transaction` |
| 充值订单 | `/economy/orders` | `economy:transaction` |
| 调账审批 | `/economy/adjust-requests` | `economy:adjust` |
| 商城管理 | `/shop/items` | `shop:manage` |
| 活动管理 | `/activities` | `shop:manage` |
| 系统配置 | `/system/configs` | `system:config` |
| 角色管理 | `/system/roles` | `system:role` |
| 管理员账号 | `/system/admins` | `system:admin` |
| 操作日志 | `/system/operation-logs` | `system:log` |

超级管理员 `admin` 账号拥有上述全部权限。

### 6.3 仪表盘趋势图

前端仪表盘使用 **ECharts** 展示近 7 日：

- 对局数折线图（`games`）
- 新增用户折线图（`newUsers`）
- 充值柱状图（`revenue`，单位：分）

数据来源：`GET /api/admin/v1/admin/dashboard/trends?days=7`

### 6.4 调账流程说明

调账阈值由系统配置 `wallet.adjust_threshold` 控制，默认：

```json
{ "amount": 100000, "require_approval": true }
```

| 场景 | 金额 | 流程 |
|------|------|------|
| 小额调账 | `< 100000` 金币 | 用户列表 →「调账」→ 直接执行，写入 `wallet_transaction`（`ADMIN_ADJUST`） |
| 大额调账 | `≥ 100000` 金币 | 用户列表 →「调账」→ 自动提示提交审批 → 调账审批页通过/驳回 |
| 手动提交审批 | `≥ 100000` | 也可直接调用 `POST /wallet/adjust-requests` |

审批通过后状态变为 `EXECUTED`，并自动执行钱包变动；驳回后状态为 `REJECTED`。所有直接调账与审批通过均写入 `operation_log`（`WALLET_ADJUST`）。

### 6.5 商城与活动说明

- **商城商品**：种子数据含 3 个金币礼包；`payload` 为 JSON，如 `{"gold": 1000}` 表示购买后发放金币
- **活动配置**：种子数据含 `DAILY_SIGN` 每日签到；`config_json` 为活动规则 JSON
- 商品/活动的增删改均写入操作日志（`SHOP_ITEM_*` / `ACTIVITY_*`）

### 6.6 系统管理说明

| 功能 | 说明 |
|------|------|
| 角色管理 | 不可删除 ID=1 的超级管理员角色；有关联管理员的角色不可删 |
| 管理员账号 | 支持新增、编辑角色/状态、重置密码；不能禁用当前登录账号 |
| 操作日志 | 只读查询；记录封禁、调账、配置变更、商城/活动/角色/管理员等敏感操作 |

### 6.7 快速匹配与 Redis

**快速匹配（玩家 API）**

```bash
# 需玩家 JWT（微信登录 mock 后获取）
curl -s -X POST http://localhost:8080/api/v1/match/quick \
  -H "Authorization: Bearer $PLAYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gameType":"DOUDIZHU"}' | jq
```

凑齐 3 人自动创建 `MATCH` 模式房间并返回 `roomId`；否则返回 `WAITING` 及队列人数。

**Redis（可选，默认关闭）**

| 配置 | 说明 | 默认值 |
|------|------|--------|
| `REDIS_ENABLED` / `cardgame.redis.enabled` | 是否启用 Redis | `false` |
| `REDIS_HOST` | Redis 地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |

启用后：
- 匹配队列使用 Redis ZSet（`match:queue:{gameType}`）
- WebSocket 房间消息经 Redis Pub/Sub 广播（多实例部署）

未启用时使用内存匹配队列，WebSocket 仅本机广播（本地开发足够）。

**定时任务**

- `RoomCleanupJob` 每 5 分钟清理超过 2 小时仍处于 `WAITING` 的空闲房间
- 可通过 `cardgame.job.stale-room-hours` 调整超时小时数

### 6.8 Swagger 文档

后端启动后访问：

- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs

### 6.9 命令行验证

#### 登录获取 Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/admin/v1/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.accessToken')
echo $TOKEN
```

#### 查询金币流水

```bash
curl -s "http://localhost:8080/api/admin/v1/admin/wallet/transactions?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 小额直接调账（用户 ID 需存在）

```bash
curl -s -X POST "http://localhost:8080/api/admin/v1/admin/users/1/adjust-wallet" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"adjustType":"INCREASE","amount":1000,"reason":"测试补偿"}' | jq
```

#### 提交大额调账申请

```bash
curl -s -X POST http://localhost:8080/api/admin/v1/admin/wallet/adjust-requests \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"adjustType":"DECREASE","amount":200000,"reason":"异常回收"}' | jq
```

#### 审批通过（将 `{id}` 替换为申请 ID）

```bash
curl -s -X POST "http://localhost:8080/api/admin/v1/admin/wallet/adjust-requests/1/approve" \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 读取调账阈值配置

```bash
curl -s http://localhost:8080/api/admin/v1/admin/system/configs/wallet.adjust_threshold \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 查询商城商品

```bash
curl -s "http://localhost:8080/api/admin/v1/admin/shop/items?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 查询操作日志

```bash
curl -s "http://localhost:8080/api/admin/v1/admin/operation-logs?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 查询仪表盘趋势

```bash
curl -s "http://localhost:8080/api/admin/v1/admin/dashboard/trends?days=7" \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 查询对局回放

```bash
curl -s "http://localhost:8080/api/admin/v1/admin/records/GR20260101120000001/replay" \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 踢出房间玩家

```bash
curl -s -X POST "http://localhost:8080/api/admin/v1/admin/rooms/R20260101120000001/kick" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"reason":"长时间挂机"}' | jq
```

#### 查询充值订单

```bash
curl -s "http://localhost:8080/api/admin/v1/admin/orders?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

#### 玩家登录与签到（mock 微信）

```bash
PLAYER_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/wechat/login \
  -H "Content-Type: application/json" \
  -d '{"code":"mock-openid-001","nickname":"测试玩家"}' | jq -r '.data.accessToken')

curl -s http://localhost:8080/api/v1/wallet -H "Authorization: Bearer $PLAYER_TOKEN" | jq
curl -s -X POST http://localhost:8080/api/v1/activities/daily-sign \
  -H "Authorization: Bearer $PLAYER_TOKEN" | jq
```

#### 创建充值订单并 mock 支付回调

```bash
ORDER_NO=$(curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $PLAYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":100,"goldAmount":1000,"payChannel":"WECHAT"}' | jq -r '.data.orderNo')

curl -s -X POST "http://localhost:8080/api/v1/orders/${ORDER_NO}/pay-callback" | jq
```

#### RANKED 段位匹配

```bash
# mode=RANKED 按段位桶撮合；等待 30s 后自动扩大相邻段位（MatchExpandJob 每 10s）
curl -s -X POST http://localhost:8080/api/v1/match/quick \
  -H "Authorization: Bearer $PLAYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gameType":"DOUDIZHU","mode":"RANKED"}' | jq

curl -s "http://localhost:8080/api/v1/rank/me?gameType=DOUDIZHU" \
  -H "Authorization: Bearer $PLAYER_TOKEN" | jq
```

> 段位表已包含在 `V1__init_schema.sql` 中；新环境只需空库 + 启动后端即可。

#### 人机练习（PVE）

```bash
# 需 V1 种子含系统账号 900000 与 Bot；脏库请先 reset-db.sql
curl -s -X POST http://localhost:8080/api/v1/rooms/pve \
  -H "Authorization: Bearer $PLAYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gameType":"DOUDIZHU","config":{"botDifficulty":"EASY"}}' | jq
```

响应 `status=PLAYING` 表示已自动开局；小程序/WebSocket 连接 `ws://host:8080/ws/game` 后发送 `BIND_ROOM` 收 `STATE_SYNC`。

#### CSV 数据导出

```bash
# 金币流水（筛选参数与列表 API 相同）
curl -s "http://localhost:8080/api/admin/v1/admin/export/wallet/transactions?format=csv&page=1&pageSize=100" \
  -H "Authorization: Bearer $TOKEN" -o wallet_transactions.csv

# 充值订单 / 对局记录 / 操作日志
curl -s "http://localhost:8080/api/admin/v1/admin/export/orders?format=csv" \
  -H "Authorization: Bearer $TOKEN" -o recharge_orders.csv
curl -s "http://localhost:8080/api/admin/v1/admin/export/records?format=csv" \
  -H "Authorization: Bearer $TOKEN" -o game_records.csv
curl -s "http://localhost:8080/api/admin/v1/admin/export/operation-logs?format=csv" \
  -H "Authorization: Bearer $TOKEN" -o operation_logs.csv
```

运营后台对应列表页已提供「导出 CSV」按钮；超过行数上限返回 `90001 EXPORT_TOO_LARGE`。

#### 仪表盘告警

```bash
curl -s "http://localhost:8080/api/admin/v1/admin/dashboard/alerts" \
  -H "Authorization: Bearer $TOKEN" | jq
```

前端仪表盘每 60 秒轮询告警区；`level` 为 `WARNING` / `INFO`，`link` 可跳转对应页面。

#### 异步结算（可选）

默认**同步结算**（对局结束立即写库）。启用 Redis Stream 异步消费需同时设置：

| 配置 | 说明 | 默认值 |
|------|------|--------|
| `REDIS_ENABLED` | 必须开启 Redis | `false` |
| `SETTLEMENT_ASYNC_ENABLED` / `cardgame.settlement.async-enabled` | 发布结算事件到 Stream | `false` |
| `cardgame.settlement.stream-name` | Stream 键名 | `cardgame:settlement` |
| `cardgame.job.settlement-consume-interval-ms` | 消费者轮询间隔 | `2000` |

```bash
export REDIS_ENABLED=true
export SETTLEMENT_ASYNC_ENABLED=true
./mvnw spring-boot:run
```

Redis 不可用或消费失败时自动**降级为同步结算**，不影响对局完成。

#### 登录响应示例

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 7200,
    "permissions": ["dashboard:view", "..."],
    "user": { "id": 1, "username": "admin", "realName": "系统管理员" }
  }
}
```

### 6.10 Phase 4 路线图

Phase 4 按波次交付，**详细设计**见：

- 后端：[CardGameBackend/docs/DESIGN.md §11](../CardGameBackend/docs/DESIGN.md#11-phase-4-详细设计)
- 前端：[cardgame-frontend/docs/DESIGN.md §11](../cardgame-frontend/docs/DESIGN.md#11-phase-4-详细设计)

| 波次 | 主题 | 状态 |
|------|------|------|
| 1 | 快速匹配、Redis WS、房间清理、ECharts 趋势 | ✅ 已实现 |
| 2 | 段位 + RANKED 匹配、回放协议、ReplayPlayer | ✅ 已实现 |
| 3 | 异步结算、CSV 导出、仪表盘告警 | ✅ 已实现 |
| 4 | 麻将 MVP、活动可视化表单 | 麻将待开发；活动表单 ✅ |

---

## 7. 日常开发操作

### 7.1 后端

| 操作 | 命令 |
|------|------|
| 启动（热重载） | `./mvnw spring-boot:run` |
| 编译 | `./mvnw compile` |
| 打包 | `./mvnw package -DskipTests` |
| 运行测试 | `./mvnw test` |

修改 Java 代码后，Spring DevTools 会自动重启（无需手动停止）。

### 7.2 前端

| 操作 | 命令 |
|------|------|
| 开发模式 | `npm run dev` |
| 生产构建 | `npm run build` |
| 预览构建产物 | `npm run preview` |

### 7.3 微信小程序

| 操作 | 说明 |
|------|------|
| 打开项目 | 微信开发者工具 → 导入 `cardgame-miniprogram/` |
| 类型检查 | `cd cardgame-miniprogram && npm run typecheck` |
| 真机调试 | 修改 `miniprogram/config/env.ts` 的 `DEV_LAN_HOST` |
| 域名校验 | 开发阶段勾选「不校验合法域名」 |

### 7.4 数据库变更

1. 在 `CardGameBackend/src/main/resources/db/migration/` 下新增 `V2__xxx.sql`
2. 重启后端，Flyway 自动执行新迁移
3. 同步更新 JPA 实体与 `docs/DESIGN.md`

> 设计阶段若尚未发布，可直接修改 `V1__init_schema.sql`，然后**删库重建**（见 §8.3）。

---

## 8. 常见问题

### 8.1 `Schema validation: missing table [xxx]`

**原因：** 数据库为空，Flyway 未成功执行迁移。

**处理：**

1. 确认 `cardgame_db` 已创建
2. 确认 `application.yaml` 中 `spring.flyway.enabled: true`
3. 确认 `pom.xml` 包含 `spring-boot-starter-flyway` 和 `flyway-mysql`
4. 重启后端，查看 Flyway 日志

### 8.2 `Schema validation: wrong column type`（TINYINT vs INTEGER）

**原因：** DDL 中字段为 `TINYINT`，JPA 实体未声明对应类型。

**处理：** 在实体字段上添加 `@Column(columnDefinition = "TINYINT")`。

### 8.3 需要完全重置数据库

**方式一（推荐）**

```bash
mysql -u root -p < CardGameBackend/docs/reset-db.sql
```

**方式二（手动 SQL）**

```sql
DROP DATABASE IF EXISTS cardgame_db;
CREATE DATABASE cardgame_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

然后重新启动后端，Flyway 会执行 **`V1__init_schema.sql`**（21 表 + 种子数据）。

> 若曾跑过旧版 V2–V4 迁移，必须删库重建，不能只删业务表。  
> 重置后：小程序/运营台需重新登录；若启用 Redis 建议 `FLUSHDB` 清缓存。

### 8.4 端口 8080 被占用

```bash
# macOS 查看占用进程
lsof -iTCP:8080 -sTCP:LISTEN

# 结束进程（替换 PID）
kill <PID>
```

或修改 `application.yaml` 中的 `server.port`。

### 8.5 登录返回「用户名或密码错误」

1. 确认使用的是 `admin` / `admin123`
2. 若数据库是很早之前创建的，可能含有旧版错误密码哈希 — 执行 §8.3 删库重建即可

### 8.6 zsh `compinit: insecure directories`

与项目无关，是 zsh 补全目录权限问题。任选其一：

```bash
# 查看问题目录
compaudit

# 修复权限（将 OUTPUT 替换为 compaudit 输出的目录）
compaudit | xargs chmod g-w

# 或在 ~/.zshrc 中跳过检查
ZSH_DISABLE_COMPFIX=true
```

### 8.7 `Using generated security password` 警告

Spring Security 默认生成了内存用户，可忽略。本项目使用 JWT + 数据库管理员账号认证，该警告不影响功能。

---

## 9. 环境变量（生产部署）

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_DATASOURCE_URL` | 数据库连接 URL | 见 application.yaml |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | 空 |
| `ADMIN_JWT_SECRET` | 管理员 JWT 密钥（≥32 字节） | 开发默认值 |
| `PLAYER_JWT_SECRET` | 玩家 JWT 密钥（≥32 字节） | 开发默认值 |
| `REDIS_ENABLED` | 是否启用 Redis | `false` |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `SETTLEMENT_ASYNC_ENABLED` | 对局结算走 Redis Stream 异步消费（需 `REDIS_ENABLED=true`） | `false` |

生产环境**必须**设置 JWT 密钥，示例：

```bash
export ADMIN_JWT_SECRET="your-production-admin-secret-at-least-32-chars"
export PLAYER_JWT_SECRET="your-production-player-secret-at-least-32-chars"
```

---

## 10. 服务地址汇总

| 服务 | 地址 |
|------|------|
| 运营后台开发服务器 | http://localhost:5173 |
| 后端 API | http://localhost:8080 |
| 游戏 WebSocket | ws://localhost:8080/ws/game |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| 运营后台登录页 | http://localhost:5173/login |

---

## 11. 相关文档

- 后端设计：`CardGameBackend/docs/DESIGN.md`
- 运营后台设计：`cardgame-frontend/docs/DESIGN.md`
- 小程序设计：`cardgame-miniprogram/docs/DESIGN.md`
- DDL 参考：`CardGameBackend/docs/schema.sql`
- 删库重建：`CardGameBackend/docs/reset-db.sql`
