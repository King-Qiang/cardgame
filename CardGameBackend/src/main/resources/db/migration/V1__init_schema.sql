-- ============================================================
-- CardGameBackend 数据库初始化脚本
-- 包含：表结构（21 张表）+ 初始数据（含段位、Bot、系统账号）
-- 数据库: cardgame_db
-- 引擎: MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================================

-- ------------------------------------------------------------
-- 1. 用户域
-- ------------------------------------------------------------

CREATE TABLE `player` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `openid`      VARCHAR(64)  NOT NULL COMMENT '微信 openid',
    `unionid`     VARCHAR(64)           DEFAULT NULL COMMENT '微信 unionid',
    `nickname`    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '头像 URL',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0 正常 / 1 封禁 / 2 注销',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '注册时间',
    `updated_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_player_openid` (`openid`),
    KEY `idx_player_status_created` (`status`, `created_at`),
    KEY `idx_player_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家主表';

CREATE TABLE `user_profile` (
    `user_id`    BIGINT NOT NULL COMMENT '用户 ID',
    `level`      INT    NOT NULL DEFAULT 1 COMMENT '等级',
    `exp`        BIGINT NOT NULL DEFAULT 0 COMMENT '经验值',
    `vip_level`  INT    NOT NULL DEFAULT 0 COMMENT 'VIP 等级',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_user_profile_player` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户扩展信息';

CREATE TABLE `user_wallet` (
    `user_id`      BIGINT NOT NULL COMMENT '用户 ID',
    `gold`         BIGINT NOT NULL DEFAULT 0 COMMENT '金币余额',
    `diamond`      BIGINT NOT NULL DEFAULT 0 COMMENT '钻石余额',
    `frozen_gold`  BIGINT NOT NULL DEFAULT 0 COMMENT '冻结金币',
    `version`      INT    NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at`   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_user_wallet_player` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包';

CREATE TABLE `user_ban` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '被封用户 ID',
    `reason`      VARCHAR(256) NOT NULL COMMENT '封禁原因',
    `ban_until`   DATETIME(3)           DEFAULT NULL COMMENT '解封时间，NULL 表示永久',
    `operator_id` BIGINT                DEFAULT NULL COMMENT '操作人（运营账号 ID）',
    `revoked_at`  DATETIME(3)           DEFAULT NULL COMMENT '提前解封时间',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '封禁时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_ban_user_id` (`user_id`),
    KEY `idx_user_ban_created_at` (`created_at`),
    CONSTRAINT `fk_user_ban_player` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户封禁记录';

-- ------------------------------------------------------------
-- 2. 运营域（先于 room 相关外键引用 admin_user）
-- ------------------------------------------------------------

CREATE TABLE `admin_role` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
    `name`         VARCHAR(64)  NOT NULL COMMENT '角色名称',
    `permissions`  JSON         NOT NULL COMMENT '权限列表 JSON 数组',
    `description`  VARCHAR(256) NOT NULL DEFAULT '' COMMENT '角色描述',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营角色';

CREATE TABLE `admin_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员 ID',
    `username`        VARCHAR(64)  NOT NULL COMMENT '登录名',
    `password_hash`   VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码哈希',
    `real_name`       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '真实姓名',
    `role_id`         BIGINT       NOT NULL COMMENT '角色 ID',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '0 禁用 / 1 启用',
    `last_login_at`   DATETIME(3)           DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(64)           DEFAULT NULL COMMENT '最后登录 IP',
    `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_user_username` (`username`),
    KEY `idx_admin_user_role_id` (`role_id`),
    CONSTRAINT `fk_admin_user_role` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营账号';

ALTER TABLE `user_ban`
    ADD CONSTRAINT `fk_user_ban_operator` FOREIGN KEY (`operator_id`) REFERENCES `admin_user` (`id`);

CREATE TABLE `operation_log` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_id`  BIGINT       NOT NULL COMMENT '操作人 ID',
    `action`       VARCHAR(64)  NOT NULL COMMENT '操作类型',
    `target_type`  VARCHAR(32)  NOT NULL COMMENT '目标类型：USER / ROOM / RECORD 等',
    `target_id`    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '目标 ID',
    `detail`       JSON                  DEFAULT NULL COMMENT '操作详情',
    `ip`           VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '操作 IP',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_operation_log_operator` (`operator_id`, `created_at`),
    KEY `idx_operation_log_target` (`target_type`, `target_id`),
    KEY `idx_operation_log_action_created` (`action`, `created_at`),
    CONSTRAINT `fk_operation_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `admin_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营操作审计日志';

-- ------------------------------------------------------------
-- 3. 房间域
-- ------------------------------------------------------------

CREATE TABLE `game_room` (
    `room_id`      VARCHAR(32)  NOT NULL COMMENT '房间号',
    `game_type`    VARCHAR(32)  NOT NULL COMMENT '玩法：DOUDIZHU / MAHJONG 等',
    `mode`         VARCHAR(32)  NOT NULL COMMENT '模式：FRIEND / MATCH / RANKED / PVE',
    `status`       VARCHAR(16)  NOT NULL DEFAULT 'WAITING' COMMENT '房间状态',
    `owner_id`     BIGINT       NOT NULL COMMENT '房主 ID',
    `max_players`  INT          NOT NULL DEFAULT 3 COMMENT '最大人数',
    `config_json`  JSON                  DEFAULT NULL COMMENT '房间规则配置',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`room_id`),
    KEY `idx_game_room_status_created` (`status`, `created_at`),
    KEY `idx_game_room_owner_id` (`owner_id`),
    KEY `idx_game_room_game_type_mode` (`game_type`, `mode`),
    CONSTRAINT `fk_game_room_owner` FOREIGN KEY (`owner_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏房间';

CREATE TABLE `room_player` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `room_id`    VARCHAR(32) NOT NULL COMMENT '房间号',
    `user_id`    BIGINT      NOT NULL COMMENT '玩家 ID',
    `seat`       INT         NOT NULL COMMENT '座位号，从 0 开始',
    `ready`      TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否准备',
    `is_robot`   TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否机器人',
    `joined_at`  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_player_room_user` (`room_id`, `user_id`),
    UNIQUE KEY `uk_room_player_room_seat` (`room_id`, `seat`),
    KEY `idx_room_player_user_id` (`user_id`),
    CONSTRAINT `fk_room_player_room` FOREIGN KEY (`room_id`) REFERENCES `game_room` (`room_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_room_player_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间玩家';

-- ------------------------------------------------------------
-- 4. 对局域
-- ------------------------------------------------------------

CREATE TABLE `game_record` (
    `record_id`    VARCHAR(32) NOT NULL COMMENT '对局 ID',
    `room_id`      VARCHAR(32) NOT NULL COMMENT '房间号',
    `game_type`    VARCHAR(32) NOT NULL COMMENT '玩法',
    `status`       VARCHAR(16) NOT NULL DEFAULT 'PLAYING' COMMENT 'PLAYING / FINISHED / ABORTED',
    `start_at`     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '开始时间',
    `end_at`       DATETIME(3)          DEFAULT NULL COMMENT '结束时间',
    `result_json`  JSON                 DEFAULT NULL COMMENT '结算摘要',
    PRIMARY KEY (`record_id`),
    KEY `idx_game_record_room_start` (`room_id`, `start_at`),
    KEY `idx_game_record_status_start` (`status`, `start_at`),
    KEY `idx_game_record_game_type_start` (`game_type`, `start_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对局记录';

CREATE TABLE `game_action_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `record_id`   VARCHAR(32)  NOT NULL COMMENT '对局 ID',
    `seq`         INT          NOT NULL COMMENT '操作序号，从 1 递增',
    `user_id`     BIGINT       NOT NULL COMMENT '操作玩家 ID',
    `action`      VARCHAR(32)  NOT NULL COMMENT '操作类型',
    `payload`     JSON                  DEFAULT NULL COMMENT '操作详情',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_game_action_record_seq` (`record_id`, `seq`),
    KEY `idx_game_action_user_id` (`user_id`),
    KEY `idx_game_action_created_at` (`created_at`),
    CONSTRAINT `fk_game_action_record` FOREIGN KEY (`record_id`) REFERENCES `game_record` (`record_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_game_action_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对局操作流水（回放/仲裁）';

CREATE TABLE `game_settlement` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `record_id`   VARCHAR(32) NOT NULL COMMENT '对局 ID',
    `user_id`     BIGINT      NOT NULL COMMENT '玩家 ID',
    `gold_delta`  BIGINT      NOT NULL DEFAULT 0 COMMENT '金币变化，正负',
    `score`       INT         NOT NULL DEFAULT 0 COMMENT '本局得分',
    `created_at`  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '结算时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_game_settlement_record_user` (`record_id`, `user_id`),
    KEY `idx_game_settlement_user_id` (`user_id`),
    CONSTRAINT `fk_game_settlement_record` FOREIGN KEY (`record_id`) REFERENCES `game_record` (`record_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_game_settlement_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对局结算明细';

-- ------------------------------------------------------------
-- 5. 经济域
-- ------------------------------------------------------------

CREATE TABLE `wallet_transaction` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流水 ID',
    `user_id`        BIGINT       NOT NULL COMMENT '用户 ID',
    `type`           VARCHAR(32)  NOT NULL COMMENT '流水类型',
    `amount`         BIGINT       NOT NULL COMMENT '变动金额，正负',
    `balance_after`  BIGINT       NOT NULL COMMENT '变动后余额',
    `ref_type`       VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '关联类型：GAME / ORDER / ADMIN 等',
    `ref_id`         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '关联 ID',
    `remark`         VARCHAR(256) NOT NULL DEFAULT '' COMMENT '备注',
    `created_at`     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_wallet_tx_user_created` (`user_id`, `created_at`),
    KEY `idx_wallet_tx_type_created` (`type`, `created_at`),
    KEY `idx_wallet_tx_ref` (`ref_type`, `ref_id`),
    CONSTRAINT `fk_wallet_tx_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包流水（只增不改）';

CREATE TABLE `shop_item` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '商品 ID',
    `name`        VARCHAR(128) NOT NULL COMMENT '商品名称',
    `price`       BIGINT       NOT NULL COMMENT '价格',
    `currency`    VARCHAR(16)  NOT NULL COMMENT 'GOLD / DIAMOND',
    `payload`     JSON                  DEFAULT NULL COMMENT '发放内容',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0 下架 / 1 上架',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `created_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_shop_item_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城商品';

CREATE TABLE `recharge_order` (
    `order_no`     VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id`      BIGINT      NOT NULL COMMENT '用户 ID',
    `amount`       BIGINT      NOT NULL COMMENT '支付金额（分）',
    `gold_amount`  BIGINT      NOT NULL DEFAULT 0 COMMENT '发放金币',
    `pay_channel`  VARCHAR(32) NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道',
    `status`       VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / PAID / FAILED / REFUNDED',
    `paid_at`      DATETIME(3)          DEFAULT NULL COMMENT '支付时间',
    `created_at`   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`order_no`),
    KEY `idx_recharge_order_user_created` (`user_id`, `created_at`),
    KEY `idx_recharge_order_status_created` (`status`, `created_at`),
    CONSTRAINT `fk_recharge_order_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值订单';

CREATE TABLE `wallet_adjust_request` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请 ID',
    `user_id`        BIGINT       NOT NULL COMMENT '目标用户 ID',
    `adjust_type`    VARCHAR(16)  NOT NULL COMMENT 'INCREASE / DECREASE',
    `amount`         BIGINT       NOT NULL COMMENT '调账金额（金币）',
    `reason`         VARCHAR(256) NOT NULL COMMENT '申请原因',
    `status`         VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / APPROVED / REJECTED / EXECUTED',
    `applicant_id`   BIGINT       NOT NULL COMMENT '申请人（运营账号 ID）',
    `approver_id`    BIGINT                DEFAULT NULL COMMENT '审批人 ID',
    `approved_at`    DATETIME(3)           DEFAULT NULL COMMENT '审批时间',
    `reject_reason`  VARCHAR(256)          DEFAULT NULL COMMENT '驳回原因',
    `created_at`     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '申请时间',
    `updated_at`     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_wallet_adjust_user_id` (`user_id`),
    KEY `idx_wallet_adjust_status_created` (`status`, `created_at`),
    KEY `idx_wallet_adjust_applicant` (`applicant_id`),
    CONSTRAINT `fk_wallet_adjust_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`),
    CONSTRAINT `fk_wallet_adjust_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `admin_user` (`id`),
    CONSTRAINT `fk_wallet_adjust_approver` FOREIGN KEY (`approver_id`) REFERENCES `admin_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调账审批';

-- ------------------------------------------------------------
-- 6. 活动与系统配置（扩展）
-- ------------------------------------------------------------

CREATE TABLE `activity_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动 ID',
    `code`         VARCHAR(64)  NOT NULL COMMENT '活动编码，如 DAILY_SIGN',
    `name`         VARCHAR(128) NOT NULL COMMENT '活动名称',
    `type`         VARCHAR(32)  NOT NULL COMMENT '活动类型',
    `config_json`  JSON         NOT NULL COMMENT '活动规则配置',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '0 禁用 / 1 启用',
    `start_at`     DATETIME(3)           DEFAULT NULL COMMENT '开始时间',
    `end_at`       DATETIME(3)           DEFAULT NULL COMMENT '结束时间',
    `created_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_config_code` (`code`),
    KEY `idx_activity_config_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动配置';

CREATE TABLE `system_config` (
    `config_key`   VARCHAR(64)  NOT NULL COMMENT '配置键',
    `config_value` JSON         NOT NULL COMMENT '配置值',
    `description`  VARCHAR(256) NOT NULL DEFAULT '' COMMENT '说明',
    `updated_at`   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';

CREATE TABLE `player_sign_record` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT      NOT NULL COMMENT '用户 ID',
    `sign_date`    DATE        NOT NULL COMMENT '签到日期',
    `streak_day`   INT         NOT NULL COMMENT '连续签到天数（1-7 循环）',
    `reward_gold`  BIGINT      NOT NULL DEFAULT 0 COMMENT '本次奖励金币',
    `created_at`   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '签到时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_player_sign_user_date` (`user_id`, `sign_date`),
    KEY `idx_player_sign_user_created` (`user_id`, `created_at`),
    CONSTRAINT `fk_player_sign_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家签到记录';

-- ------------------------------------------------------------
-- 7. 段位域
-- ------------------------------------------------------------

CREATE TABLE `player_rank` (
    `user_id`     BIGINT       NOT NULL COMMENT '玩家 ID',
    `game_type`   VARCHAR(32)  NOT NULL COMMENT '玩法',
    `season_id`   VARCHAR(16)  NOT NULL COMMENT '赛季 ID',
    `tier`        VARCHAR(16)  NOT NULL DEFAULT 'BRONZE' COMMENT '段位',
    `points`      INT          NOT NULL DEFAULT 0 COMMENT '积分',
    `wins`        INT          NOT NULL DEFAULT 0 COMMENT '胜场',
    `losses`      INT          NOT NULL DEFAULT 0 COMMENT '负场',
    `updated_at`  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`user_id`, `game_type`, `season_id`),
    KEY `idx_player_rank_tier_points` (`game_type`, `season_id`, `tier`, `points` DESC),
    CONSTRAINT `fk_player_rank_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家段位';

CREATE TABLE `player_rank_log` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT       NOT NULL,
    `record_id`      VARCHAR(32)  NOT NULL DEFAULT '',
    `game_type`      VARCHAR(32)  NOT NULL,
    `season_id`      VARCHAR(16)  NOT NULL,
    `delta_points`   INT          NOT NULL,
    `tier_before`    VARCHAR(16)  NOT NULL,
    `tier_after`     VARCHAR(16)  NOT NULL,
    `points_before`  INT          NOT NULL,
    `points_after`   INT          NOT NULL,
    `created_at`     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_player_rank_log_user_created` (`user_id`, `created_at`),
    CONSTRAINT `fk_player_rank_log_user` FOREIGN KEY (`user_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='段位变动日志';

-- ------------------------------------------------------------
-- 8. 初始数据
-- ------------------------------------------------------------

-- 超级管理员角色（拥有全部权限）
INSERT INTO `admin_role` (`id`, `name`, `description`, `permissions`)
VALUES (
    1,
    '超级管理员',
    '拥有全部后台权限',
    JSON_ARRAY(
        'dashboard:view',
        'user:list',
        'user:ban',
        'user:adjust',
        'room:monitor',
        'room:kick',
        'room:disband',
        'record:list',
        'economy:transaction',
        'economy:adjust',
        'shop:manage',
        'system:admin',
        'system:role',
        'system:log',
        'system:config'
    )
);

-- 只读角色
INSERT INTO `admin_role` (`id`, `name`, `description`, `permissions`)
VALUES (
    2,
    '只读',
    '仅可查看仪表盘与报表',
    JSON_ARRAY(
        'dashboard:view',
        'user:list',
        'record:list',
        'economy:transaction'
    )
);

-- 默认超级管理员账号
-- 用户名: admin  密码: admin123
-- 首次登录后请立即修改密码
INSERT INTO `admin_user` (`id`, `username`, `password_hash`, `real_name`, `role_id`, `status`)
VALUES (
    1,
    'admin',
    '$2a$10$0TLqH8e1ylGFsRdY6PZX2OPVLXq3DMsvOQXLRgxSYMyT7U8z6QvE2',
    '系统管理员',
    1,
    1
);

-- 默认商城商品示例
INSERT INTO `shop_item` (`name`, `price`, `currency`, `payload`, `status`, `sort_order`)
VALUES
    ('1000 金币礼包', 100, 'DIAMOND', JSON_OBJECT('gold', 1000), 1, 1),
    ('5000 金币礼包', 450, 'DIAMOND', JSON_OBJECT('gold', 5000), 1, 2),
    ('10000 金币礼包', 800, 'DIAMOND', JSON_OBJECT('gold', 10000), 1, 3);

-- 每日签到活动配置
INSERT INTO `activity_config` (`code`, `name`, `type`, `config_json`, `status`)
VALUES (
    'DAILY_SIGN',
    '每日签到',
    'DAILY_SIGN',
    JSON_OBJECT(
        'rewards', JSON_ARRAY(100, 100, 200, 200, 300, 300, 500),
        'description', '连续签到 7 天循环奖励'
    ),
    1
);

-- 系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES
    (
        'game.enabled_types',
        JSON_ARRAY('DOUDIZHU'),
        '已启用的游戏玩法'
    ),
    (
        'maintenance',
        JSON_OBJECT('enabled', false, 'message', ''),
        '维护公告配置'
    ),
    (
        'wallet.adjust_threshold',
        JSON_OBJECT('amount', 100000, 'require_approval', true),
        '大额调账阈值（金币）'
    ),
    (
        'rank.tier_thresholds',
        JSON_OBJECT(
            'winPoints', 25,
            'losePoints', 15,
            'tiers', JSON_ARRAY(
                JSON_OBJECT('tier', 'BRONZE', 'min', 0, 'max', 99),
                JSON_OBJECT('tier', 'SILVER', 'min', 100, 'max', 299),
                JSON_OBJECT('tier', 'GOLD', 'min', 300, 'max', 599),
                JSON_OBJECT('tier', 'PLATINUM', 'min', 600, 'max', 999),
                JSON_OBJECT('tier', 'DIAMOND', 'min', 1000, 'max', null)
            )
        ),
        '段位积分阈值与升降段规则'
    ),
    (
        'game.pve',
        JSON_OBJECT('enabled', true, 'defaultDifficulty', 'EASY'),
        '人机练习开关与默认难度'
    ),
    (
        'game.bot',
        JSON_OBJECT('minDelayMs', 500, 'maxDelayMs', 1500, 'difficulties', JSON_ARRAY('EASY')),
        'Bot 行动延迟与可用难度'
    );

-- 系统事件操作者 + Bot 玩家（不可微信登录；game_action_log FK 依赖 900000）
INSERT INTO `player` (`id`, `openid`, `nickname`, `avatar`, `status`)
VALUES
    (900000, '__system_actor__', '系统', '', 0),
    (900001, '__bot_sys_001__', '电脑一号', '', 0),
    (900002, '__bot_sys_002__', '电脑二号', '', 0),
    (900003, '__bot_sys_003__', '电脑三号', '', 0);

ALTER TABLE `player` AUTO_INCREMENT = 900004;
