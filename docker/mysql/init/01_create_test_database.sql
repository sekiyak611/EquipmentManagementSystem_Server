-- 最初の1回だけ流す SQL（compose.yaml。データの場所が空のときだけ、MySQL のコンテナが root で流す）。
-- 開発用のデータベース（equipment_dev）とアプリ用のユーザ（equipment）は、compose.yaml の設定で作られる。
-- ここでは、自動テスト専用のデータベース（equipment_test）を作り、同じユーザに使えるようにする（CLAUDE.md 6章）。
-- パスワードはここに書かない（.env に書く）。

CREATE DATABASE IF NOT EXISTS equipment_test CHARACTER SET utf8mb4 COLLATE utf8mb4_ja_0900_as_cs_ks;
GRANT ALL PRIVILEGES ON equipment_test.* TO 'equipment'@'%';
