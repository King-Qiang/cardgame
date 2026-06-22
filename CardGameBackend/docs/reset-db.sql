-- 本地开发：清空 cardgame_db，便于 Flyway 重新执行 V1__init_schema.sql
-- 用法: mysql -u root -p < docs/reset-db.sql
-- 然后重启 CardGameBackend（Flyway 会自动建表 + 种子数据）

DROP DATABASE IF EXISTS `cardgame_db`;

CREATE DATABASE `cardgame_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
