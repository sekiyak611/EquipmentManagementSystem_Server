# CLAUDE.md（備品管理システム：Server）

このリポジトリ（EquipmentManagementSystem_Server）は、備品管理システムのサーバー（Spring Boot）。

## 1. 共通の決まり（必ず読む）

共通の決まりは、同じフォルダに並べた Document リポジトリ（EquipmentManagementSystem_Document）の `internaldesign/CLAUDE.md` にある。作業の前に必ず読み、そのとおりに作業する。次の1行で読み込んでいる。

@../EquipmentManagementSystem_Document/internaldesign/CLAUDE.md

- 共通の決まりが読み込めていない（中身が分からない）ときは、作業を始めずに人に知らせる。Document のフォルダが、このフォルダと同じ場所に並んでいるかを確かめてもらう。
- Claude Code は、Document も読み書きできるように起動する（`claude --add-dir ../EquipmentManagementSystem_Document`、または起動したあとに `/add-dir ../EquipmentManagementSystem_Document`）。詳細設計と証跡は Document に書くため。

## 2. このリポジトリだけの決まり

- Gradle のプロジェクト名は、フォルダ名と同じ `EquipmentManagementSystem_Server`（settings.gradle）。Eclipse は、ワークスペースの直下にあるプロジェクトの名前を、フォルダ名と同じにする必要があるため。変えない。
- 秘密の値（データベースのパスワードなど）は、このフォルダの `secret.yml` に書く（Gitで管理しない。見本は `secret.example.yml`）。
- 開発のときのログは、このフォルダの `logs/` に出る。
- skill の名前の「backend」（backend-impl・backend-test・backend-review）は、このリポジトリの作業に使うもの（共通の決まり 4.4）。

## 3. よく使うコマンド

**最初に1回だけ**

- MySQL 8.4.13 を PC に直接入れる（Document の internaldesign/環境構築手順書.md の手順4）。Docker は使わない（共通の決まり 3章。2026-11-14 にやめた）。
- 開発用（equipment_dev）とテスト用（equipment_test）のデータベースと、アプリ用のユーザ `equipment` を作る（手順書の手順5。SQL がある）。
- `secret.example.yml` をコピーして `secret.yml` を作り、データベースのユーザ名（`equipment`）と、手順5で決めたパスワードを書く。あわせて `app.initial-admin-email` に、メールアドレスの形の値を書く（空だと、最初の起動で止まる。手順6）。

| やること | コマンド（このフォルダで） |
|---|---|
| Claude Code を起動する | `claude --add-dir ../EquipmentManagementSystem_Document` |
| MySQL を起動する・止める | Windows のサービス `MySQL84`（PC を起動すると自動で動く）。管理者の PowerShell で `Start-Service MySQL84`・`Stop-Service MySQL84`（データは残る） |
| MySQL が動いているか確かめる | `Get-Service *mysql*`（Status が Running） |
| サーバーを起動する（開発用の設定） | `.\gradlew.bat bootRun`（Eclipse なら、EquipmentApplication を右クリック →「実行」→「Spring Boot アプリケーション」） |
| 動いているか確かめる | ブラウザで http://localhost:8080/api/health を開く |
| サーバーのテスト | `.\gradlew.bat test`（結果は build/reports/tests/test/index.html） |
| テーブルを作る・変える | サーバーを起動すると Flyway が自動で反映する |

AWS 用に組み立てるときは、先に Client のフォルダで `npm run build` を動かしてから、このフォルダで `.\gradlew.bat bootJar` を動かす。build.gradle が、隣の Client の dist（組み立てた画面）を jar に取り込む（詳細設計 common.md 3.9）。

テストの証跡は、build/reports/tests/test の中身（レポート）と build/test-results/test の中身（元の記録）を、Document の `evidence/日付_種類_内容/` に写す（docs/テスト方針.md 7章）。

## 4. Gitで管理しないもの

Claude Code が .gitignore に入れる（共通の CLAUDE.md 7章）。公開してはいけないものが増えたら、.gitignore とこの表を同じ作業で直す。

| もの | 理由 |
|---|---|
| secret.yml | 秘密の値（パスワード、トークン） |
| .env | 以前の Docker の手順で使っていた、MySQL のパスワードのファイル。もう使わないが、PC に残っていても Git に入れない |
| logs/ | ログ |
| build/、.gradle/ | Gradle が作るもの |
| bin/ | Eclipse が作るもの |
| .settings/、.classpath、.project | Eclipse の各自のPCの設定（PCの場所や個人のフォルダの名前が入る） |
| .vscode/、.idea/ | エディタの各自の設定 |
| output/ | Claude Code の作業記録（作業メモなど）を置く場所 |
| .claude/settings.local.json | Claude Code の各自の設定（.claude/settings.json は共有するので管理する） |
| Thumbs.db、desktop.ini、.DS_Store | OS が作るもの |
