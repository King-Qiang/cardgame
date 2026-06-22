# cardgame-frontend 后台管理系统设计文档


| 项目   | cardgame-frontend                                   |
| ---- | --------------------------------------------------- |
| 版本   | v1.6                                                |
| 技术栈  | Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router |
| 关联项目 | CardGameBackend（后端服务）                               |


**变更记录**


| 版本   | 日期  | 说明                                          |
| ---- | --- | ------------------------------------------- |
| v1.0 | —   | 初版页面、路由、API 对接规范                            |
| v1.1 | —   | 补充 system:config、调账审批、活动 API；对齐后端权限与 DDL 引用 |
| v1.2 | —   | 补关键 API 请求体 TypeScript 类型；与后端 §5.5 对齐       |
| v1.3 | 2026-06-18 | Phase 6 人机模式运营配置：§10、§11.12（PVE 开关、Bot 参数、房间/对局展示） |
| v1.4 | 2026-06-18 | Phase 6C 工程：`SystemConfigView` 结构化编辑、房间详情弹窗、对局 mode 筛选 |
| v1.6 | 2026-06-20 | Flyway 整合对齐：§12.3 改为 21 表 + 单一 V1；系统账号说明对齐 V1 种子（非 V4 增量） |
| v1.5 | 2026-06-20 | 回放展示说明：系统事件 `userId=900000`（对齐 CardGameBackend §11.4.2 / §11.13.8.1） |

---

## 1. 项目概述

### 1.1 定位

cardgame-frontend 是棋牌游戏平台的**运营后台管理系统**，面向内部运营、客服、财务人员，不直接面向游戏玩家。

核心职责：

- 数据监控：DAU、在线人数、收入、对局量
- 用户运营：查询、封禁、解封、人工调账
- 对局管理：历史记录查询、回放、争议处理
- 房间监控：实时在线房间、踢人、强制解散
- 经济管理：流水查询、充值订单、调账审批
- 商城与活动：商品配置、活动规则
- 系统管理：角色权限、管理员账号、操作审计

### 1.2 用户角色


| 角色    | 典型使用者 | 权限范围          |
| ----- | ----- | ------------- |
| 超级管理员 | 技术负责人 | 全部功能          |
| 运营    | 运营人员  | 用户、活动、商城；对局只读 |
| 客服    | 客服人员  | 用户查询、封禁/解封    |
| 财务    | 财务人员  | 流水、订单、调账（需审批） |
| 只读    | 管理层   | 仪表盘、报表只读      |


### 1.3 系统边界

```
┌─────────────────────────────────────────┐
│           cardgame-frontend              │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐ │
│  │ 登录鉴权 │  │ 业务页面 │  │ 权限控制 │ │
│  └────┬────┘  └────┬────┘  └────┬────┘ │
│       └────────────┼────────────┘       │
│                    │ axios              │
└────────────────────┼────────────────────┘
                     │ HTTPS
                     ▼
         CardGameBackend /api/admin/v1/**
```

---

## 2. 技术架构

### 2.1 技术选型


| 类别    | 选型                                         | 版本（当前） |
| ----- | ------------------------------------------ | ------ |
| 框架    | Vue 3 (Composition API + `<script setup>`) | ^3.5   |
| 语言    | TypeScript                                 | ~6.0   |
| 构建    | Vite                                       | ^8.0   |
| UI 组件 | Element Plus                               | ^2.14  |
| 状态管理  | Pinia                                      | ^3.0   |
| 路由    | Vue Router                                 | ^4.6   |
| HTTP  | Axios                                      | ^1.18  |


### 2.2 目录结构

```
cardgame-frontend/
├── docs/
│   └── DESIGN.md              # 本文档
├── public/
├── src/
│   ├── api/                   # API 请求层
│   │   ├── client.ts          # axios 实例、拦截器
│   │   ├── auth.ts            # 登录、登出、改密
│   │   ├── user.ts            # 用户管理
│   │   ├── room.ts            # 房间监控
│   │   ├── record.ts          # 对局记录
│   │   ├── economy.ts         # 经济、流水、订单
│   │   ├── shop.ts            # 商城
│   │   ├── activity.ts        # 活动
│   │   ├── dashboard.ts       # 仪表盘
│   │   └── system.ts          # 角色、管理员、日志
│   ├── assets/                # 静态资源
│   ├── components/            # 通用组件
│   │   ├── SearchForm.vue     # 通用搜索表单
│   │   ├── DataTable.vue      # 封装 el-table + 分页
│   │   ├── PermissionButton.vue  # 权限按钮
│   │   ├── ConfirmDialog.vue  # 二次确认弹窗
│   │   └── ReplayPlayer.vue   # 对局回放播放器
│   ├── composables/           # 组合式函数
│   │   ├── usePermission.ts   # 权限判断
│   │   ├── useTable.ts        # 表格分页逻辑
│   │   └── usePolling.ts      # 轮询（房间监控）
│   ├── layouts/
│   │   └── AdminLayout.vue    # 主布局（侧边栏 + 顶栏 + 内容区）
│   ├── router/
│   │   ├── index.ts           # 路由入口、守卫
│   │   └── routes.ts          # 路由表 + 动态路由
│   ├── stores/
│   │   ├── user.ts            # 登录态、用户信息
│   │   ├── permission.ts      # 权限、菜单
│   │   └── app.ts             # 侧边栏折叠、主题等
│   ├── types/                 # TypeScript 类型定义
│   │   ├── api.ts             # 通用响应类型
│   │   ├── user.ts
│   │   ├── room.ts
│   │   └── ...
│   ├── utils/
│   │   ├── auth.ts            # token 存取
│   │   ├── format.ts          # 日期、金额格式化
│   │   └── permission.ts      # 权限工具
│   ├── views/
│   │   ├── login/
│   │   │   └── LoginView.vue
│   │   ├── dashboard/
│   │   │   └── DashboardView.vue
│   │   ├── user/
│   │   │   ├── UserListView.vue
│   │   │   └── UserDetailView.vue
│   │   ├── room/
│   │   │   └── RoomMonitorView.vue
│   │   ├── record/
│   │   │   ├── RecordListView.vue
│   │   │   └── RecordDetailView.vue
│   │   ├── economy/
│   │   │   ├── TransactionListView.vue
│   │   │   ├── OrderListView.vue
│   │   │   ├── AdjustWalletView.vue
│   │   │   └── AdjustApprovalView.vue
│   │   ├── shop/
│   │   │   └── ShopItemView.vue
│   │   ├── activity/
│   │   │   └── ActivityView.vue
│   │   ├── system/
│   │   │   ├── AdminUserView.vue
│   │   │   ├── RoleView.vue
│   │   │   ├── SystemConfigView.vue
│   │   │   └── OperationLogView.vue
│   │   └── error/
│   │       ├── 403View.vue
│   │       └── 404View.vue
│   ├── App.vue
│   ├── main.ts
│   └── style.css
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

### 2.3 开发与部署

**开发环境代理**（已在 `vite.config.ts` 配置）：

```ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  }
}
```

**生产部署**：

- `npm run build` 产出静态文件
- Nginx 托管静态资源，同域反向代理 `/api` 到后端
- 避免跨域，token 存 `localStorage` 或 `sessionStorage`

---

## 3. 核心模块设计

### 3.1 认证与权限

#### 3.1.1 登录流程

```
用户输入账号密码
  → POST /api/admin/v1/admin/auth/login
  → 后端返回 { accessToken, refreshToken, expiresIn, user, permissions }
  → 前端存储 token + 用户信息 + 权限列表
  → 根据 permissions 生成动态路由
  → 跳转仪表盘
```

#### 3.1.2 Token 管理

```ts
// utils/auth.ts
const TOKEN_KEY = 'admin_access_token'

export function getToken(): string | null
export function setToken(token: string): void
export function removeToken(): void
```

#### 3.1.3 Axios 拦截器

```ts
// api/client.ts
// 请求拦截：自动附加 Authorization: Bearer {token}
// 响应拦截：
//   - code !== 0 → ElMessage.error + 拒绝
//   - 401 → 清除 token，跳转登录页
//   - 403 → 跳转 403 页面
```

#### 3.1.4 路由守卫

```ts
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    return token ? next('/') : next()
  }
  if (!token) return next('/login')
  if (to.meta.permission && !hasPermission(to.meta.permission)) {
    return next('/403')
  }
  next()
})
```

#### 3.1.5 权限模型

权限以字符串标识，与后端 `admin_role.permissions` 对齐：

```ts
// 权限常量示例
export const PERMISSIONS = {
  DASHBOARD_VIEW: 'dashboard:view',
  USER_LIST: 'user:list',
  USER_BAN: 'user:ban',
  USER_ADJUST: 'user:adjust',
  ROOM_MONITOR: 'room:monitor',
  ROOM_KICK: 'room:kick',
  ROOM_DISBAND: 'room:disband',
  RECORD_LIST: 'record:list',
  ECONOMY_TRANSACTION: 'economy:transaction',
  ECONOMY_ADJUST: 'economy:adjust',
  SHOP_MANAGE: 'shop:manage',
  SYSTEM_ADMIN: 'system:admin',
  SYSTEM_ROLE: 'system:role',
  SYSTEM_LOG: 'system:log',
  SYSTEM_CONFIG: 'system:config',
} as const
```

**PermissionButton 组件**：无权限时隐藏或禁用按钮。

```vue
<PermissionButton permission="user:ban" @click="handleBan">
  封禁用户
</PermissionButton>
```

#### 3.1.6 动态菜单

根据用户 permissions 过滤路由表，生成侧边栏菜单：

```ts
// router/routes.ts
{
  path: '/user',
  meta: { title: '用户管理', icon: 'User', permission: 'user:list' },
  children: [
    { path: 'list', component: UserListView, meta: { title: '用户列表' } },
    { path: ':id', component: UserDetailView, meta: { title: '用户详情', hidden: true } },
  ]
}
```

### 3.2 布局设计

#### AdminLayout 结构

```
┌──────────────────────────────────────────────────────────┐
│  Logo   棋牌运营后台                    管理员 ▼  [退出]   │
├────────────┬─────────────────────────────────────────────┤
│            │  面包屑：用户管理 / 用户列表                  │
│  侧边栏     │  ┌───────────────────────────────────────┐  │
│            │  │                                       │  │
│  · 仪表盘   │  │           <router-view />             │  │
│  · 用户管理 │  │                                       │  │
│  · 房间监控 │  └───────────────────────────────────────┘  │
│  · 对局记录 │                                             │
│  · 经济管理 │                                             │
│  · 商城活动 │                                             │
│  · 系统设置 │                                             │
│            │                                             │
└────────────┴─────────────────────────────────────────────┘
```

- 侧边栏可折叠（状态存 `app` store）
- 顶栏：当前管理员名称、修改密码、退出登录
- 内容区：面包屑 + 页面主体

---

## 4. 功能模块详细设计

### 4.1 登录页（LoginView）


| 元素    | 说明             |
| ----- | -------------- |
| 账号输入框 | 必填             |
| 密码输入框 | 必填，可切换显示       |
| 登录按钮  | 提交后 loading 状态 |
| 错误提示  | 账号或密码错误        |


无需注册功能，账号由超级管理员在系统管理中创建。

### 4.2 仪表盘（DashboardView）

**概览卡片**


| 指标     | 数据来源                        | 刷新     |
| ------ | --------------------------- | ------ |
| 今日 DAU | `/admin/dashboard/overview` | 页面加载   |
| 当前在线   | 同上                          | 30s 轮询 |
| 今日对局数  | 同上                          | 页面加载   |
| 今日充值金额 | 同上                          | 页面加载   |


**趋势图表**


| 图表            | 说明                                   |
| ------------- | ------------------------------------ |
| 近 7 日 DAU 折线图 | `/admin/dashboard/trends?metric=dau` |
| 近 7 日充值柱状图    | `metric=revenue`                     |
| 近 7 日对局数折线图   | `metric=games`                       |


**告警区域**

- 今日封禁用户数异常增高
- 大额调账待审批
- 异常房间解散数

技术建议：图表可用 ECharts（需新增依赖 `echarts`）。

### 4.3 用户管理

#### 4.3.1 用户列表（UserListView）

**搜索条件**


| 字段     | 类型   | 说明           |
| ------ | ---- | ------------ |
| 用户 ID  | 输入框  | 精确匹配         |
| 昵称     | 输入框  | 模糊匹配         |
| openid | 输入框  | 模糊匹配（脱敏展示）   |
| 状态     | 下拉   | 全部 / 正常 / 封禁 |
| 注册时间   | 日期范围 |              |


**表格列**


| 列    | 说明                |
| ---- | ----------------- |
| ID   | 用户 ID             |
| 昵称   |                   |
| 头像   | 缩略图               |
| 金币   | 当前余额              |
| 状态   | Tag：正常(绿) / 封禁(红) |
| 注册时间 |                   |
| 操作   | 查看详情、封禁/解封        |


**操作**

- 封禁：弹窗填写原因、时长 → `POST /admin/users/{id}/ban`

```json
{ "reason": "恶意刷分", "banUntil": "2026-07-01T00:00:00+08:00" }
```

- 解封：二次确认 → `POST /admin/users/{id}/unban`

```json
{ "reason": "申诉通过，解除封禁" }
```

#### 4.3.2 用户详情（UserDetailView）

**信息区块**

1. 基本信息：ID、昵称、头像、openid（脱敏）、注册时间、状态
2. 钱包信息：金币、钻石、冻结金币
3. 最近对局：表格（最近 10 局），可跳转对局详情
4. 封禁历史：时间、原因、操作人、解封时间
5. 最近流水：表格（最近 20 条）

**操作按钮**

- 封禁 / 解封
- 人工调账（跳转调账或弹窗）

### 4.4 房间监控（RoomMonitorView）

实时性要求高，建议 **10s 自动刷新**（`usePolling`）。

**表格列**


| 列    | 说明                  |
| ---- | ------------------- |
| 房间号  | 可点击看详情              |
| 玩法   | 斗地主 / 麻将            |
| 模式   | 亲友房 / 匹配            |
| 状态   | WAITING / PLAYING 等 |
| 玩家数  | 2/3                 |
| 房主   | 昵称                  |
| 创建时间 |                     |
| 操作   | 查看、踢人、解散            |


**房间详情抽屉**

- 玩家列表：座位、昵称、准备状态、是否在线
- 当前对局 ID（若在局中）

**运营操作（需权限 + 二次确认 + 填写原因）**


| 操作   | API                                  | 请求体                  |
| ---- | ------------------------------------ | -------------------- |
| 踢出玩家 | `POST /admin/rooms/{roomId}/kick`    | `{ userId, reason }` |
| 强制解散 | `POST /admin/rooms/{roomId}/disband` | `{ reason }`         |


### 4.5 对局记录

#### 4.5.1 对局列表（RecordListView）

**搜索条件**


| 字段    | 类型       |
| ----- | -------- |
| 对局 ID | 输入框      |
| 房间号   | 输入框      |
| 用户 ID | 输入框（参与者） |
| 玩法    | 下拉       |
| 时间范围  | 日期范围     |


**表格列**


| 列     | 说明               |
| ----- | ---------------- |
| 对局 ID |                  |
| 房间号   |                  |
| 玩法    |                  |
| 状态    | 进行中 / 已结束 / 异常终止 |
| 开始时间  |                  |
| 结束时间  |                  |
| 操作    | 详情、回放            |


#### 4.5.2 对局详情（RecordDetailView）

- 基本信息：对局 ID、房间、玩法、时间
- 参与者与结算：表格（用户、金币变化、得分）
- 操作时间线：按 seq 展示 action 列表

#### 4.5.3 回放播放器（ReplayPlayer）

解析 `game_action_log`，按步骤播放：

- 播放 / 暂停 / 上一步 / 下一步
- 进度条
- 当前步骤 action 说明

斗地主回放需展示：叫地主、出牌、过牌等关键节点。

**系统事件**：`userId=900000`（昵称「系统」）表示 `GAME_START` / `DEAL` / `SETTLEMENT` 等服务端流水，**不是 Bot**（Bot 为 900001–900003）。见 [CardGameBackend §11.4.2 / §11.13.8.1](../CardGameBackend/docs/DESIGN.md#111381-已知问题与修复v19)。

> **Phase 4 完整交互与组件设计见 [§11.5 回放播放器](#115-回放播放器-replayplayer)**。

### 4.6 经济管理

#### 4.6.1 流水查询（TransactionListView）

**搜索条件**：用户 ID、流水类型、时间范围、金额范围

**表格列**：流水 ID、用户、类型、变动金额、变动后余额、关联 ID、时间、备注

**功能**：导出 CSV（前端或后端导出接口）

#### 4.6.2 充值订单（OrderListView）

**搜索条件**：订单号、用户 ID、状态、时间范围

**表格列**：订单号、用户、金额、发放金币、支付渠道、状态、支付时间

#### 4.6.3 人工调账（AdjustWalletView）

**表单**


| 字段    | 说明       |
| ----- | -------- |
| 用户 ID | 必填，可搜索用户 |
| 调账类型  | 增加 / 减少  |
| 金额    | 正整数      |
| 原因    | 必填       |


提交 → `POST /admin/users/{id}/adjust-wallet`

```json
{ "adjustType": "INCREASE", "amount": 1000, "reason": "活动补偿" }
```

金额低于阈值时直接执行；超过阈值时调用 `POST /admin/wallet/adjust-requests`：

```json
{ "userId": 10001, "adjustType": "DECREASE", "amount": 200000, "reason": "异常金币回收" }
```

### 4.7 商城管理（ShopItemView）

标准 CRUD 页面：


| 字段   | 说明            |
| ---- | ------------- |
| 商品名  |               |
| 价格   |               |
| 货币类型 | 金币 / 钻石       |
| 发放内容 | JSON 配置，表单化编辑 |
| 排序   |               |
| 状态   | 上架 / 下架       |


### 4.8 活动管理（ActivityView）

初期以配置化为主：


| 活动类型 | 配置项           |
| ---- | ------------- |
| 每日签到 | 每日奖励金币数组（7 天） |
| 新手礼包 | 一次性奖励         |
| 限时活动 | 起止时间、规则 JSON  |


后期可增加可视化表单编辑器。

对应 API：`/admin/activities` CRUD（api/activity.ts）。

### 4.9 系统配置（SystemConfigView）

管理后端 `system_config` 表，需权限 `system:config`。


| 配置键                     | 页面表单       | 说明      |
| ----------------------- | ---------- | ------- |
| game.enabled_types      | 多选玩法       | 启用/禁用玩法 |
| maintenance             | 开关 + 公告文本  | 维护模式    |
| wallet.adjust_threshold | 金额 + 是否需审批 | 调账阈值    |


API：`GET/PUT /admin/system/configs`、`GET/PUT /admin/system/configs/{key}`

### 4.10 调账审批（AdjustApprovalView）

**表格列**：申请 ID、目标用户、类型、金额、申请人、状态、申请时间

**状态 Tag**：PENDING=warning、APPROVED=success、REJECTED=danger、EXECUTED=info

**操作**：

- 审批通过 → `POST /admin/wallet/adjust-requests/{id}/approve`（无 body）
- 驳回 → `POST /admin/wallet/adjust-requests/{id}/reject`

```json
{ "rejectReason": "材料不足，驳回" }
```

流程：`提交申请 → 财务审批 → 自动执行调账 → 写流水`

### 4.11 系统管理

#### 4.11.1 管理员账号（AdminUserView）


| 功能   | 说明               |
| ---- | ---------------- |
| 列表   | 账号、姓名、角色、状态、最后登录 |
| 新增   | 账号、初始密码、角色       |
| 编辑   | 角色、状态            |
| 重置密码 | 超级管理员操作          |


#### 4.11.2 角色管理（RoleView）


| 功能    | 说明           |
| ----- | ------------ |
| 列表    | 角色名、权限数、创建时间 |
| 新增/编辑 | 角色名 + 权限树勾选  |


权限树结构示例：

```
├── 仪表盘
├── 用户管理
│   ├── 查看列表
│   ├── 封禁/解封
│   └── 人工调账
├── 房间监控
│   ├── 查看
│   ├── 踢人
│   └── 解散
├── 对局记录
├── 经济管理
├── 商城管理
├── 活动管理
├── 系统配置
└── 系统管理
```

#### 4.11.3 操作日志（OperationLogView）

**搜索**：操作人、操作类型、时间范围、目标 ID

**表格**：时间、操作人、操作类型、目标、详情摘要、IP

只读，不可删除。

---

## 5. 通用组件设计

### 5.1 SearchForm

封装常用搜索模式：

```vue
<SearchForm :fields="searchFields" @search="handleSearch" @reset="handleReset" />
```

- 支持 input、select、date-range
- 搜索/重置按钮
- 与 `useTable` 配合自动触发列表刷新

### 5.2 DataTable

```vue
<DataTable
  :columns="columns"
  :data="tableData"
  :total="total"
  :loading="loading"
  @page-change="fetchData"
/>
```

- 封装 `el-table` + `el-pagination`
- 统一 loading、空状态
- 操作列插槽

### 5.3 useTable Composable

```ts
function useTable<T>(fetchFn: (params: PageParams) => Promise<PageResult<T>>) {
  const loading = ref(false)
  const tableData = ref<T[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(20)
  const searchParams = ref({})

  async function fetchData() { ... }
  function handleSearch(params: object) { ... }
  function handleReset() { ... }

  return { loading, tableData, total, page, pageSize, fetchData, handleSearch, handleReset }
}
```

### 5.4 金额与日期格式化

```ts
// utils/format.ts
formatGold(amount: number): string    // 12345 → "12,345 金币"
formatMoney(cents: number): string    // 10050 → "¥100.50"
formatDateTime(iso: string): string  // → "2025-06-18 14:30:00"
maskOpenid(openid: string): string    // → "oXXXX***XXXX"
```

---

## 6. API 对接规范

### 6.1 基础配置


| 项            | 值                               |
| ------------ | ------------------------------- |
| Base URL     | `/api/admin/v1`                 |
| 认证           | `Authorization: Bearer {token}` |
| Content-Type | `application/json`              |


### 6.2 响应类型

```ts
// types/api.ts
interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  traceId?: string
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

interface PageParams {
  page?: number
  pageSize?: number
  [key: string]: unknown
}
```

### 6.3 主要 API 清单


| 模块       | 方法       | 路径                                           | 前端文件             |
| -------- | -------- | -------------------------------------------- | ---------------- |
| 登录       | POST     | `/admin/auth/login`                          | api/auth.ts      |
| 登出       | POST     | `/admin/auth/logout`                         | api/auth.ts      |
| 改密       | PUT      | `/admin/auth/password`                       | api/auth.ts      |
| 概览       | GET      | `/admin/dashboard/overview`                  | api/dashboard.ts |
| 趋势       | GET      | `/admin/dashboard/trends`                    | api/dashboard.ts |
| 用户列表     | GET      | `/admin/users`                               | api/user.ts      |
| 用户详情     | GET      | `/admin/users/{id}`                          | api/user.ts      |
| 封禁       | POST     | `/admin/users/{id}/ban`                      | api/user.ts      |
| 解封       | POST     | `/admin/users/{id}/unban`                    | api/user.ts      |
| 调账       | POST     | `/admin/users/{id}/adjust-wallet`            | api/user.ts      |
| 调账申请     | GET/POST | `/admin/wallet/adjust-requests`              | api/economy.ts   |
| 审批通过     | POST     | `/admin/wallet/adjust-requests/{id}/approve` | api/economy.ts   |
| 审批驳回     | POST     | `/admin/wallet/adjust-requests/{id}/reject`  | api/economy.ts   |
| 房间列表     | GET      | `/admin/rooms`                               | api/room.ts      |
| 房间详情     | GET      | `/admin/rooms/{roomId}`                      | api/room.ts      |
| 踢人       | POST     | `/admin/rooms/{roomId}/kick`                 | api/room.ts      |
| 解散       | POST     | `/admin/rooms/{roomId}/disband`              | api/room.ts      |
| 对局列表     | GET      | `/admin/records`                             | api/record.ts    |
| 对局详情     | GET      | `/admin/records/{recordId}`                  | api/record.ts    |
| 回放       | GET      | `/admin/records/{recordId}/replay`           | api/record.ts    |
| 流水       | GET      | `/admin/wallet/transactions`                 | api/economy.ts   |
| 订单       | GET      | `/admin/orders`                              | api/economy.ts   |
| 商品 CRUD  | *        | `/admin/shop/items`                          | api/shop.ts      |
| 活动 CRUD  | *        | `/admin/activities`                          | api/activity.ts  |
| 系统配置     | GET/PUT  | `/admin/system/configs`                      | api/system.ts    |
| 系统配置项    | GET/PUT  | `/admin/system/configs/{key}`                | api/system.ts    |
| 角色 CRUD  | *        | `/admin/roles`                               | api/system.ts    |
| 管理员 CRUD | *        | `/admin/admins`                              | api/system.ts    |
| 操作日志     | GET      | `/admin/operation-logs`                      | api/system.ts    |


### 6.4 关键请求体（TypeScript 类型）

与后端 [§5.5](../../CardGameBackend/docs/DESIGN.md) 对齐，前端 `types/` 目录建议定义：

```ts
// types/admin.ts

/** 登录 */
interface LoginRequest {
  username: string
  password: string
}

/** 封禁 */
interface BanUserRequest {
  reason: string
  banUntil?: string   // ISO 8601，省略表示永久
}

/** 解封 */
interface UnbanUserRequest {
  reason: string
}

/** 直接调账 */
interface AdjustWalletRequest {
  adjustType: 'INCREASE' | 'DECREASE'
  amount: number
  reason: string
}

/** 调账申请 */
interface AdjustRequestCreate {
  userId: number
  adjustType: 'INCREASE' | 'DECREASE'
  amount: number
  reason: string
}

/** 驳回调账 */
interface AdjustRequestReject {
  rejectReason: string
}

/** 踢人 */
interface KickPlayerRequest {
  userId: number
  reason: string
}

/** 解散房间 */
interface DisbandRoomRequest {
  reason: string
}

/** 更新系统配置项 */
interface SystemConfigUpdate {
  configValue: Record<string, unknown>
}
```

**登录响应 `data` 结构**（存入 `user` store）：

```ts
interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: { id: number; username: string; realName: string }
  permissions: string[]
}
```

---

## 7. 路由表


| 路径                         | 页面                  | 权限                  | 菜单显示 |
| -------------------------- | ------------------- | ------------------- | ---- |
| `/login`                   | LoginView           | 无                   | 否    |
| `/`                        | DashboardView       | dashboard:view      | 是    |
| `/user/list`               | UserListView        | user:list           | 是    |
| `/user/:id`                | UserDetailView      | user:list           | 否    |
| `/room/monitor`            | RoomMonitorView     | room:monitor        | 是    |
| `/record/list`             | RecordListView      | record:list         | 是    |
| `/record/:id`              | RecordDetailView    | record:list         | 否    |
| `/economy/transactions`    | TransactionListView | economy:transaction | 是    |
| `/economy/orders`          | OrderListView       | economy:transaction | 是    |
| `/economy/adjust`          | AdjustWalletView    | economy:adjust      | 是    |
| `/economy/adjust-approval` | AdjustApprovalView  | economy:adjust      | 是    |
| `/shop/items`              | ShopItemView        | shop:manage         | 是    |
| `/activity`                | ActivityView        | shop:manage         | 是    |
| `/system/config`           | SystemConfigView    | system:config       | 是    |
| `/system/admins`           | AdminUserView       | system:admin        | 是    |
| `/system/roles`            | RoleView            | system:role         | 是    |
| `/system/logs`             | OperationLogView    | system:log          | 是    |
| `/403`                     | 403View             | 无                   | 否    |
| `/404`                     | 404View             | 无                   | 否    |


---

## 8. UI/UX 规范

### 8.1 交互规范


| 场景       | 规范                             |
| -------- | ------------------------------ |
| 删除/封禁/解散 | 必须二次确认弹窗                       |
| 表单提交     | 按钮 loading，防止重复提交              |
| 列表加载     | 表格 loading 遮罩                  |
| 操作成功     | `ElMessage.success`            |
| 操作失败     | `ElMessage.error`，展示后端 message |
| 空数据      | 表格内置 empty 状态                  |


### 8.2 样式约定

- 主色：Element Plus 默认蓝 `#409EFF`
- 侧边栏：深色背景 `#304156`
- 页面内边距：20px
- 搜索区与表格区间距：16px
- 状态 Tag：正常=success、封禁=danger、进行中=warning

### 8.3 响应式

后台主要面向 PC 端，最低宽度 1280px。移动端不做适配。

---

## 9. 安全注意事项


| 项        | 措施                   |
| -------- | -------------------- |
| Token 存储 | localStorage；退出时清除   |
| 敏感操作     | 封禁、调账、踢人、解散需二次确认     |
| 权限控制     | 路由级 + 按钮级双重校验        |
| XSS      | 避免 `v-html` 渲染用户输入   |
| 日志       | 前端不记录密码；错误日志不含 token |


---

## 10. 实施计划

### Phase 1 — 框架搭建（1–2 周）✅ 已完成

- [x] 目录结构、axios 封装、路由、Pinia
- [x] AdminLayout 布局
- [x] 登录页 + 路由守卫（含基础 `meta.permission` 校验）

> Phase 1 验收标准：可登录进入后台骨架。权限框架完整版、通用表格组件延后至 Phase 2 一并完成（与业务页开发同步落地）。

### Phase 2 — 核心业务页 + Phase 1 收尾（2–3 周）✅ 已完成

**Phase 1 收尾（前端）**

- [x] 权限框架（菜单过滤、`PermissionButton`、`usePermission`）
- [x] 通用 `SearchForm`、`DataTable`、`useTable`

**核心业务页**

- [x] 仪表盘（对接 `/admin/dashboard/overview`）
- [x] 用户列表 + 封禁/解封
- [x] 房间监控（15s 轮询刷新）
- [x] 对局列表 + 详情/回放页

### Phase 3 — 经济与系统（2 周）✅ 已完成

- [x] 流水查询、充值订单列表
- [x] 人工调账（用户列表弹窗）
- [x] 调账审批页
- [x] 商城 CRUD
- [x] 活动 CRUD
- [x] 系统配置页
- [x] 管理员、角色、操作日志

### Phase 4 — 增强（持续）— 设计 ✅ / 波次 1–3 已实现

> **详细设计见 [§11 Phase 4 详细设计](#11-phase-4-详细设计)**。以下为实施进度勾选。

**波次 1（已实现）**

- [x] ECharts 趋势图（仪表盘 7 日趋势）
- [x] 对局回放（运营 action 列表页，非动画播放器）
- [x] 调账审批流
- [x] 房间踢人

**波次 2（已实现）**

- [x] 回放动画播放器 `ReplayPlayer.vue`（§11.5）

**波次 3（已实现）**

- [x] 列表页 CSV 导出（§11.6）
- [x] 仪表盘告警区（§11.7）

**波次 4（部分完成）**

- [x] 活动配置可视化表单编辑器（§11.9）
- [x] 段位展示（用户列表/详情弹窗/排行榜，§11.8）

### Phase 6 — 人机模式运营支撑 — **Phase 6C 已完成 ✅**

> 后端 Bot / PVE 见 [CardGameBackend §11.13](../../CardGameBackend/docs/DESIGN.md#1113-phase-6-人机模式)；玩家端见 [cardgame-miniprogram §10 Phase 6](../../cardgame-miniprogram/docs/DESIGN.md#phase-6--人机模式1-2-周phase-66b-已完成-)。

**Phase 6A — 系统配置** ✅

- [x] `SystemConfigView` 结构化编辑 `game.pve`、`game.bot`
- [x] PVE 总开关 `enabled`：关闭后玩家 `POST /rooms/pve` 返回 `40007`

**Phase 6B — 监控与对局** ✅

- [x] `RoomMonitorView`：模式筛选 + 详情弹窗 **电脑/玩家** Tag（`isRobot`）
- [x] `RecordListView`：模式列 + **PVE（练习）** 筛选
- [x] `RecordDetailView`：参与者表格 + action 列表 Bot 标注

**Phase 6C — 可选（未做）**

- [ ] 仪表盘「今日人机练习局数」
- [ ] 小程序牌桌「电脑」角标、难度选择 UI

**验收**：运营可关闭 PVE；房间监控可区分 Bot；对局列表可筛练习局。

---

## 11. Phase 4 详细设计

与后端 [CardGameBackend/docs/DESIGN.md §11](../../CardGameBackend/docs/DESIGN.md#11-phase-4-详细设计) 对齐，本章定义运营后台 Phase 4 的页面、组件、交互与 API 对接方式。

### 11.1 目标与波次划分

| 波次 | 页面/组件 | 依赖后端 | 优先级 |
|------|-----------|----------|--------|
| 1 | ECharts 趋势、对局 action 列表、踢人 | 已实现 | P0 ✅ |
| 2 | ReplayPlayer、对局回放 | 段位/回放 API 已实现 | P0 ✅ |
| 3 | 导出按钮、仪表盘告警卡片 | §11.7–11.8 API | P1 ✅ |
| 4 | 活动可视化编辑器、段位展示 | 活动 CRUD + 段位 API | P2 ✅ |

### 11.2 目录与路由规划

```
src/
├── components/
│   └── replay/
│       ├── ReplayPlayer.vue      # 回放主组件
│       ├── ReplayControls.vue    # 播放/暂停/步进/进度条
│       └── DoudizhuBoard.vue     # 斗地主牌桌视图（简化）
├── views/
│   ├── record/
│   │   └── RecordDetailView.vue  # 嵌入 ReplayPlayer
│   ├── rank/
│   │   └── RankLeaderboardView.vue  # 可选：运营排行榜
│   └── dashboard/
│       └── DashboardView.vue     # 增加告警区
└── api/
    ├── record.ts                 # + fetchRecordReplayMeta
    ├── export.ts                 # 导出下载
    └── dashboard.ts              # + fetchAlerts
```

| 路由 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/records/:recordId` | RecordDetailView | `record:list` | 详情 + 回放播放器 |
| `/rank/leaderboard` | RankLeaderboardView | `user:list` | 可选，运营查排行榜 |
| `/dashboard` | DashboardView | `dashboard:view` | 增加告警卡片 |

### 11.3 技术依赖

```bash
npm install echarts          # 已安装
npm install dayjs            # 日期格式化（导出文件名、告警）
# 回放播放器无需新依赖，牌面用 CSS + 文本编码渲染
```

### 11.4 回放数据对接

#### 11.4.1 API

| 方法 | 路径 | 封装 |
|------|------|------|
| GET | `/admin/records/{recordId}/replay` | `fetchRecordReplay()` |
| GET | `/admin/records/{recordId}/replay/meta` | `fetchRecordReplayMeta()`（可选） |
| GET | `/admin/records/{recordId}` | `fetchRecordDetail()` |

#### 11.4.2 前端状态模型

```typescript
interface ReplayState {
  recordId: string
  gameType: 'DOUDIZHU' | 'MAHJONG'
  actions: GameActionLogItem[]
  currentStep: number          // 0 .. actions.length-1
  playing: boolean
  speed: 1 | 2 | 4             // 自动播放倍速
  board: DoudizhuBoardState    // 由 actions[0..currentStep] 归约得到
}

interface DoudizhuBoardState {
  seats: { userId: number; handCount: number; isLandlord?: boolean }[]
  lastPlay?: { seat: number; cards: string[] }
  phase: 'BID' | 'PLAY' | 'SETTLED'
  bottomCards?: string[]
}
```

#### 11.4.3 归约规则（斗地主）

按 `seq` 顺序应用 action，更新 `board`：

| action | 更新逻辑 |
|--------|----------|
| `GAME_START` / `DEAL` | 初始化 seats、handCount |
| `BID` | 记录叫分；`passed=true` 跳过 |
| `LANDLORD` | 标记 `landlordSeat`，展示底牌 |
| `PLAY_CARDS` | 更新 `lastPlay`，对应 seat `handCount -= len(cards)` |
| `PASS` | 清空或保留 `lastPlay`（按 payload） |
| `SETTLEMENT` | `phase=SETTLED`，展示结算面板 |

无法识别的 action：**跳过并 console.warn**，不中断播放。

### 11.5 回放播放器（ReplayPlayer）

#### 11.5.1 布局

```
┌─────────────────────────────────────────────────────────┐
│  对局 GRxxx · 斗地主 · 已结束          [1x] [2x] [4x]   │
├──────────────────────────────┬──────────────────────────┤
│                              │  步骤 12 / 48             │
│      DoudizhuBoard           │  ─────────────────────    │
│   （三方座位 + 出牌区）        │  Action: PLAY_CARDS       │
│                              │  玩家 10001 · 座位 1      │
│                              │  出牌: 3S 3H 3D           │
├──────────────────────────────┴──────────────────────────┤
│  [◀ 上一步]  [▶ 播放/暂停]  [下一步 ▶]   ═══●════ 25%    │
└─────────────────────────────────────────────────────────┘
```

#### 11.5.2 交互

| 操作 | 行为 |
|------|------|
| 播放 | 每 800ms / speed 前进一步，到末尾自动暂停 |
| 暂停 | 停止定时器 |
| 上一步 / 下一步 | 调整 `currentStep` 并重算 board |
| 进度条拖拽 | 跳转到对应 step |
| 键盘 ← / → | 步进（PC 端） |

#### 11.5.3 嵌入方式

`RecordDetailView` 结构：

1. 顶部：`el-descriptions` 对局基本信息（已有）
2. 中部：`ReplayPlayer`（`gameType=DOUDIZHU` 时渲染牌桌；其他玩法 fallback 为 action 列表）
3. 底部（可折叠）：原始 action JSON 表格（调试/仲裁）

#### 11.5.4 牌面展示

- 编码 `3S` → 文本「3♠」，用 CSS 区分红黑花色；
- 不加载图片资源，降低 MVP 复杂度；
- 地主标识：座位旁 `el-tag`「地主」。

### 11.6 数据导出

#### 11.6.1 支持页面

| 页面 | 按钮位置 | API |
|------|----------|-----|
| TransactionListView | 搜索栏右侧「导出 CSV」 | `GET /admin/export/wallet/transactions?...&format=csv` |
| OrderListView | 同上 | `GET /admin/export/orders?...` |
| RecordListView | 同上 | `GET /admin/export/records?...` |
| OperationLogView | 同上 | `GET /admin/export/operation-logs?...` |

#### 11.6.2 前端实现

新建 `api/export.ts`：

```typescript
export async function downloadExport(path: string, params: Record<string, unknown>, filename: string) {
  const res = await client.get(path, { params: { ...params, format: 'csv' }, responseType: 'blob' })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
```

- 导出时使用**当前搜索条件**，不含分页参数（或传 `pageSize=50000` 由后端截断）；
- 请求期间按钮 `loading`；
- 失败时 ElMessage 提示「数据量过大，请缩小筛选范围」（对应后端 `90001`）。

#### 11.6.3 权限

与列表页相同，如 `economy:transaction`、`record:list`、`system:log`。

### 11.7 仪表盘告警区

#### 11.7.1 API

`GET /admin/dashboard/alerts` → `fetchAlerts()`（`api/dashboard.ts`）

#### 11.7.2 UI

在 `DashboardView` 趋势图**上方**增加告警条：

```
┌──────────────────────────────────────────────────────────┐
│ ⚠ 3 笔大额调账待审批          [去处理 →]                  │
│ ℹ 今日封禁 8 人，高于均值      [查看用户 →]               │
└──────────────────────────────────────────────────────────┘
```

- 使用 `el-alert`，`:type` 映射 `level`：`WARNING→warning`，`INFO→info`；
- 点击「去处理」：`router.push(alert.link)`；
- 页面加载时请求；与 overview 相同，**60s 轮询**刷新；
- 无告警时不占高度（`v-if="alerts.length"`）。

### 11.8 段位展示（运营侧）

#### 11.8.1 用户列表扩展（可选列）

| 列 | 字段 | 说明 |
|----|------|------|
| 段位 | `rankTier` | Tag 颜色按段位 |
| 积分 | `rankPoints` | 数字 |

需后端 `GET /admin/users` 列表项增加 `rankSummary` 或在用户详情单独请求。

#### 11.8.2 用户详情（UserDetailView，Phase 4 新建或弹窗）

区块「排位信息」：

- 当前赛季、段位、积分、胜/负场；
- 最近 5 条 `player_rank_log`（时间、对局 ID、积分变动）。

#### 11.8.3 排行榜页（可选）

`RankLeaderboardView`：表格列 — 排名、用户 ID、昵称、段位、积分、胜/负。  
搜索：玩法下拉、赛季、段位筛选。

### 11.9 活动配置可视化编辑器

在现有 `ActivityView` 编辑弹窗中，按 `type` 渲染不同表单：

| type | 表单组件 |
|------|----------|
| `DAILY_SIGN` | 7 个 `el-input-number`（每日金币奖励），预览总周期 |
| `NEWBIE_GIFT` | 单次金币 + 可选道具 JSON |
| `LIMITED` | 日期范围 + Monaco/json-editor 编辑 `config_json` |

**DAILY_SIGN 示例 UI**

```
第1天 [100]  第2天 [100]  第3天 [200]  ...  第7天 [500]
说明：连续签到 7 天循环奖励
```

- 保存仍调用 `PUT /admin/activities/{id}`，`config_json` 由表单序列化；
- 校验：奖励 ≥ 0，数组长度 = 7；
- 复杂活动（LIMITED）保留 JSON 文本框，Phase 4 不做拖拽式编辑器。

### 11.10 权限补充

| 权限 | 说明 |
|------|------|
| `record:replay` | 可选细粒度；默认复用 `record:list` |
| `economy:export` | 可选；默认与各列表读权限相同 |
| `dashboard:alert` | 可选；默认复用 `dashboard:view` |

### 11.11 验收标准

| 波次 | 验收项 |
|------|--------|
| 2 | 对局详情页可播放/暂停/步进；牌桌区随 step 更新；RANKED 用户可见段位 Tag |
| 3 | 流水/订单/对局/日志页可下载 CSV；仪表盘展示可点击告警；导出超限时友好提示 |
| 4 | DAILY_SIGN 活动可用数字表单编辑；保存后玩家签到奖励生效 |

### 11.12 Phase 6 人机模式（运营配置）

与后端 `system_config` 键 [§11.13.2](../../CardGameBackend/docs/DESIGN.md#111132-数据模型) 对齐。

#### 11.12.1 系统配置页扩展

在 `SystemConfigView.vue` 对以下 key 提供**结构化编辑**（或 JSON 模板 + 校验）：

**`game.pve`**

| 字段 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `enabled` | boolean | `true` | 是否允许玩家一键人机练习 |
| `defaultDifficulty` | string | `EASY` | 默认 Bot 难度 |

**`game.bot`**

| 字段 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `minDelayMs` | number | 500 | Bot 行动最小延迟 |
| `maxDelayMs` | number | 1500 | Bot 行动最大延迟 |
| `difficulties` | string[] | `["EASY"]` | 已启用难度档位 |

API：`GET/PUT /admin/system/configs/{key}`（现有 `api/system.ts`）。

保存后无需重启；后端 `BotTurnScheduler` / `PveRoomService` 读取最新配置（可 `@CacheEvict`）。

#### 11.12.2 房间监控

`RoomMonitorView` 玩家子表 / 展开行：

| 列 | 字段 | 渲染 |
|----|------|------|
| 昵称 | `nickname` | 文本 |
| 类型 | `isRobot` | `true` → `el-tag`「电脑」；否则「玩家」 |
| 座位 | `seat` | 数字 |

依赖后端 `GET /admin/rooms/{roomId}` 响应扩展 `players[].isRobot`（与玩家端 DTO 一致）。

#### 11.12.3 对局记录

**列表 `RecordListView`**

- 模式筛选：`FRIEND` / `MATCH` / `RANKED` / **`PVE`**
- 列表可增加列「含电脑」：由 `result_json.participants` 或后端列表字段 `hasRobot`（Phase 6B 可选）

**详情 `RecordDetailView`**

- 参与者表格：`isRobot` 或 `userId >= 900001` 时显示 Tag「电脑」
- `userId === 900000` 显示 Tag「系统」（`GAME_START` / `DEAL` / `SETTLEMENT` 等系统流水，非 Bot）
- `ReplayPlayer` **无需改动**：Bot 操作已在 `game_action_log`；系统事件见后端 §11.4.2

#### 11.12.4 API 类型补充（`src/api/`）

```typescript
// room.ts — AdminRoomPlayer
interface AdminRoomPlayer {
  userId: number
  nickname: string
  seat: number
  ready: boolean
  isRobot?: boolean
}

// record.ts — 列表筛 Query
mode?: 'FRIEND' | 'MATCH' | 'RANKED' | 'PVE'
```

#### 11.12.5 权限

| 权限 | 说明 |
|------|------|
| `system:config` | 编辑 `game.pve` / `game.bot`（已有） |
| `room:monitor` | 查看 Bot 标识（已有） |
| `record:list` | 筛选 PVE（已有） |

#### 11.12.6 验收标准（Phase 6C 已实现 ✅）

| 项 | 标准 | 状态 |
|----|------|------|
| 关闭 PVE | `game.pve.enabled=false` 后小程序人机练习 toast 明确错误 | ✅ |
| 房间监控 | 人机对局中 Bot 座位显示「电脑」Tag | ✅ |
| 对局筛选 | 可仅看 `mode=PVE` 练习局 | ✅ |
| 配置热更新 | 修改 `maxDelayMs` 后新开局 Bot 延迟生效 | ✅ |
| 仪表盘 | 今日人机练习局数 | ⏳ 可选 |

---

## 12. 附录

### 12.1 建议新增依赖

```bash
npm install echarts vue-echarts   # 仪表盘图表
npm install dayjs                 # 日期处理
```

### 12.2 环境变量

```env
# .env.development
VITE_API_BASE_URL=/api/admin/v1

# .env.production
VITE_API_BASE_URL=/api/admin/v1
```

### 12.3 数据库与迁移

后端数据库完整初始化（**21 张表** + 初始数据）见：[CardGameBackend/docs/DESIGN.md §3.8、§12.2](../../CardGameBackend/docs/DESIGN.md)

Flyway 脚本：`CardGameBackend/src/main/resources/db/migration/V1__init_schema.sql`（**唯一迁移**，含段位、PVE/Bot、系统账号 900000）。  
本地脏数据重建：`CardGameBackend/docs/reset-db.sql`（见 [OPERATIONS.md §8.3](../docs/OPERATIONS.md#83-需要完全重置数据库)）。  
后续 schema 变更通过 **V2、V3…** 增量脚本追加，勿修改已发布的 V1。

### 12.4 关联文档

- 后端服务设计（含 API 请求体 §5.5）：[CardGameBackend/docs/DESIGN.md](../../CardGameBackend/docs/DESIGN.md)

