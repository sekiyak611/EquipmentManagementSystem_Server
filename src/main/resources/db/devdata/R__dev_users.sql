-- 開発用のユーザ（開発（dev）と自動テスト（test）のときだけ当てる。本番（prod）には入らない）。
-- 最初の総務の担当者はパスワードが空で、パスワード再設定ができるまで画面からログインできないため（詳細設計 common.md 4章、auth/auth.md 4章の No.1）。
-- パスワードはどちらも devpass-1234。ここには bcrypt で変換した値だけを書く。
-- R__ で始まるファイルは、Flyway が版のファイル（V1・V2…）のあとに当て、中身が変わったら当て直す。
-- 同じメールアドレスの行があれば、氏名・パスワード・権限を上書きするので、何回当てても同じになる。
-- 念のための守り：本番の設定（prod）では当てないが、起動の設定を付け忘れたときのために、
-- データベースの名前が開発用（equipment_dev）・テスト用（equipment_test）のときだけ入れる。本番のデータベースにはこの名前を使わない。

SET @now = CONVERT_TZ(UTC_TIMESTAMP(), '+00:00', '+09:00');

INSERT INTO users (name, email, password_hash, role_id, created_at, updated_at)
SELECT dev.name, dev.email, dev.password_hash, dev.role_id, @now, @now
FROM (
    SELECT '開発 総務' AS name, 'soumu@example.com' AS email,
           '$2a$10$arsu14GOfECh6JpnIq5fLuhrwquqPTQQuwFKTekCUBLkdLVkjRtu.' AS password_hash, 1 AS role_id
    UNION ALL
    SELECT '開発 一般', 'ippan@example.com',
           '$2a$10$arsu14GOfECh6JpnIq5fLuhrwquqPTQQuwFKTekCUBLkdLVkjRtu.', 2
) AS dev
WHERE DATABASE() IN ('equipment_dev', 'equipment_test')
ON DUPLICATE KEY UPDATE
    name = dev.name,
    password_hash = dev.password_hash,
    role_id = dev.role_id,
    failed_login_count = 0,
    locked_at = NULL,
    updated_at = @now;
