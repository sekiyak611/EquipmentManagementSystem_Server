-- 最初に入れるデータ（docs/データ設計.md 5.3・7章。詳細設計 テーブル.md 5章）。
-- データを入れるだけなので、途中で失敗したら Flyway が全部を元に戻す。
-- 作った日時は日本時間の今（データベースの時間帯の設定に左右されないように、世界標準時から9時間ずらす）。
-- 人がいない作業なので、created_by・updated_by は空（データ設計 2章）。

SET @now = CONVERT_TZ(UTC_TIMESTAMP(), '+00:00', '+09:00');

-- 5.3 権限項目（18行）
INSERT INTO permissions (code, name, group_name, sort_order) VALUES
    ('EQUIPMENT_VIEW',    '備品一覧を見る',                 '備品',         1),
    ('EQUIPMENT_CREATE',  '備品を登録する',                 '備品',         2),
    ('EQUIPMENT_UPDATE',  '備品を編集する',                 '備品',         3),
    ('EQUIPMENT_DELETE',  '備品を削除する',                 '備品',         4),
    ('CATEGORY_MANAGE',   '種別を管理する',                 '備品',         5),
    ('LOAN_SELF',         '備品を借りる・返す（自分）',     '貸出',         6),
    ('LOAN_MANAGE',       '貸出・返却を管理する',           '貸出',         7),
    ('LOAN_HISTORY_VIEW', '貸出履歴を見る',                 '貸出',         8),
    ('CONFIRM_SELF',      '最終確認日を更新する（自分）',   '確認',         9),
    ('CONFIRM_ALL',       '最終確認日を更新する（すべて）', '確認',         10),
    ('CONFIRM_CSV',       '最終確認日をCSVで一括更新する',  '確認',         11),
    ('REMINDER_LOG_VIEW', '催促履歴を見る',                 '確認',         12),
    ('USER_VIEW',         'ユーザを見る',                   'ユーザ・権限', 13),
    ('USER_CREATE',       'ユーザを登録する',               'ユーザ・権限', 14),
    ('USER_UPDATE',       'ユーザを編集する',               'ユーザ・権限', 15),
    ('USER_DELETE',       'ユーザを削除する',               'ユーザ・権限', 16),
    ('PERMISSION_VIEW',   '権限を見る',                     'ユーザ・権限', 17),
    ('PERMISSION_MANAGE', '権限を管理する',                 'ユーザ・権限', 18);

-- 7章 権限（id を決めて入れる。users.role_id をサブクエリなしで書くため）
INSERT INTO roles (id, name, is_default, created_at, updated_at) VALUES
    (1, '総務',     FALSE, @now, @now),
    (2, '一般社員', TRUE,  @now, @now);

-- 5.3 の表の○のとおり。総務はすべて、一般社員は「借りる・返す（自分）」と「最終確認日を更新する（自分）」
INSERT INTO role_permissions (role_id, permission_code) VALUES
    (1, 'EQUIPMENT_VIEW'),
    (1, 'EQUIPMENT_CREATE'),
    (1, 'EQUIPMENT_UPDATE'),
    (1, 'EQUIPMENT_DELETE'),
    (1, 'CATEGORY_MANAGE'),
    (1, 'LOAN_SELF'),
    (1, 'LOAN_MANAGE'),
    (1, 'LOAN_HISTORY_VIEW'),
    (1, 'CONFIRM_SELF'),
    (1, 'CONFIRM_ALL'),
    (1, 'CONFIRM_CSV'),
    (1, 'REMINDER_LOG_VIEW'),
    (1, 'USER_VIEW'),
    (1, 'USER_CREATE'),
    (1, 'USER_UPDATE'),
    (1, 'USER_DELETE'),
    (1, 'PERMISSION_VIEW'),
    (1, 'PERMISSION_MANAGE'),
    (2, 'LOAN_SELF'),
    (2, 'CONFIRM_SELF');

-- 7章 種別（頭文字：名前）
INSERT INTO categories (id, name, initial, created_at, updated_at) VALUES
    (1, 'PC',           'A', @now, @now),
    (2, 'ヘッドセット', 'B', @now, @now),
    (3, 'マウス',       'C', @now, @now),
    (4, 'カメラ',       'D', @now, @now),
    (5, 'モニター',     'E', @now, @now),
    (6, 'アダプタ',     'F', @now, @now);

-- 7章 最初の総務の担当者（パスワードは空。本人が「パスワードを忘れた方はこちら」から決める）。
-- メールアドレスはプログラムに書かず、設定（開発は secret.yml の app.initial-admin-email、AWS は環境変数 APP_INITIAL_ADMIN_EMAIL）から
-- Flyway が ${initial_admin_email} に入れる（application.yml の spring.flyway.placeholders）。
-- 前後の空白を除いて小文字にそろえる。設定が空なら NULL になり、email の NOT NULL で失敗する（総務の担当者をメールアドレスなしで作らない）。
INSERT INTO users (name, email, password_hash, role_id, created_at, updated_at) VALUES
    ('関谷 海仁', NULLIF(LOWER(TRIM('${initial_admin_email}')), ''), NULL, 1, @now, @now);
