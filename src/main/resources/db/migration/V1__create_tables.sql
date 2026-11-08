-- テーブル13個を作る（docs/データ設計.md 3・5・6章。詳細設計 テーブル.md）。
-- 一度当てたあとは、このファイルを変えない。テーブルを足す・変えるときは、新しい版のファイルを足す（CLAUDE.md 9章）。
-- 生成列 xxx_active：削除されていない行だけ値が入る。UNIQUE は空（NULL）の重複を許すので、削除済みの行は重複に数えない（データ設計 2章）。

-- 5.2 権限
CREATE TABLE roles (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(30)  NOT NULL,
    name_active VARCHAR(30)  GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN name END) STORED,
    is_default  BOOLEAN      NOT NULL,
    deleted_at  DATETIME     NULL,
    version     INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL,
    created_by  BIGINT       NULL,
    updated_at  DATETIME     NOT NULL,
    updated_by  BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name_active UNIQUE (name_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.3 権限項目（行は V2 で入れ、画面からは変えない）
CREATE TABLE permissions (
    code       VARCHAR(50) NOT NULL,
    name       VARCHAR(50) NOT NULL,
    group_name VARCHAR(20) NOT NULL,
    sort_order INT         NOT NULL,
    PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.4 権限と権限項目の対応
CREATE TABLE role_permissions (
    role_id         BIGINT      NOT NULL,
    permission_code VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, permission_code),
    CONSTRAINT fk_role_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission_code FOREIGN KEY (permission_code) REFERENCES permissions (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.1 ユーザ（email は保存するときに小文字にそろえる。データ設計 8章）
CREATE TABLE users (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(50)  NOT NULL,
    email               VARCHAR(254) NOT NULL,
    email_active        VARCHAR(254) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN email END) STORED,
    password_hash       VARCHAR(100) NULL,
    role_id             BIGINT       NOT NULL,
    failed_login_count  INT          NOT NULL DEFAULT 0,
    locked_at           DATETIME     NULL,
    session_valid_after DATETIME     NULL,
    deleted_at          DATETIME     NULL,
    version             INT          NOT NULL DEFAULT 0,
    created_at          DATETIME     NOT NULL,
    created_by          BIGINT       NULL,
    updated_at          DATETIME     NOT NULL,
    updated_by          BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email_active UNIQUE (email_active),
    CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    INDEX idx_users_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.5 種別
CREATE TABLE categories (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    name           VARCHAR(20) NOT NULL,
    name_active    VARCHAR(20) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN name END) STORED,
    initial        CHAR(1)     NULL,
    initial_active CHAR(1)     GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN initial END) STORED,
    deleted_at     DATETIME    NULL,
    version        INT         NOT NULL DEFAULT 0,
    created_at     DATETIME    NOT NULL,
    created_by     BIGINT      NULL,
    updated_at     DATETIME    NOT NULL,
    updated_by     BIGINT      NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name_active UNIQUE (name_active),
    CONSTRAINT uk_categories_initial_active UNIQUE (initial_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.6 備品
CREATE TABLE equipment (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    management_no        VARCHAR(30)  NOT NULL,
    management_no_active VARCHAR(30)  GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN management_no END) STORED,
    category_id          BIGINT       NOT NULL,
    maker                VARCHAR(50)  NULL,
    model                VARCHAR(100) NULL,
    serial_no            VARCHAR(100) NULL,
    size                 VARCHAR(20)  NULL,
    resolution           VARCHAR(20)  NULL,
    height_adjustable    BOOLEAN      NULL,
    pivot                BOOLEAN      NULL,
    purchased_on         DATE         NULL,
    warranty_until       DATE         NULL,
    status               VARCHAR(10)  NOT NULL,
    disposed_on          DATE         NULL,
    last_confirmed_on    DATE         NULL,
    note                 VARCHAR(500) NULL,
    deleted_at           DATETIME     NULL,
    version              INT          NOT NULL DEFAULT 0,
    created_at           DATETIME     NOT NULL,
    created_by           BIGINT       NULL,
    updated_at           DATETIME     NOT NULL,
    updated_by           BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_equipment_management_no_active UNIQUE (management_no_active),
    CONSTRAINT fk_equipment_category_id FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_equipment_status CHECK (status IN ('MANAGED', 'DISPOSED', 'MISSING')),
    CONSTRAINT ck_equipment_disposed_on CHECK (disposed_on IS NULL OR status = 'DISPOSED'),
    INDEX idx_equipment_status_last_confirmed_on (status, last_confirmed_on),
    INDEX idx_equipment_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.7 貸出履歴
-- active_equipment_id：返却日が空で、取り消されていない行だけ equipment_id が入る。UNIQUE なので、1つの備品に貸出中の行は1つだけ（二重貸出の防止）
CREATE TABLE loans (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    equipment_id        BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    lent_on             DATE         NOT NULL,
    due_on              DATE         NULL,
    returned_on         DATE         NULL,
    memo                VARCHAR(200) NULL,
    active_equipment_id BIGINT       GENERATED ALWAYS AS (CASE WHEN returned_on IS NULL AND deleted_at IS NULL THEN equipment_id END) STORED,
    deleted_at          DATETIME     NULL,
    version             INT          NOT NULL DEFAULT 0,
    created_at          DATETIME     NOT NULL,
    created_by          BIGINT       NULL,
    updated_at          DATETIME     NOT NULL,
    updated_by          BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_loans_active_equipment_id UNIQUE (active_equipment_id),
    CONSTRAINT fk_loans_equipment_id FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_loans_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_loans_due_on CHECK (due_on IS NULL OR due_on >= lent_on),
    CONSTRAINT ck_loans_returned_on CHECK (returned_on IS NULL OR returned_on >= lent_on),
    INDEX idx_loans_equipment_id_lent_on (equipment_id, lent_on),
    INDEX idx_loans_user_id_returned_on (user_id, returned_on)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.8 パスワード設定用のリンク（合言葉そのものは保存しない。SHA-256 の値だけ）
CREATE TABLE password_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    token_hash CHAR(64)    NOT NULL,
    purpose    VARCHAR(10) NOT NULL,
    expires_at DATETIME    NOT NULL,
    used_at    DATETIME    NULL,
    revoked_at DATETIME    NULL,
    created_at DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_password_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_password_tokens_user_id_created_at (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.9 ログインしたままにする（Spring Security が決めた形。username だけ 254 文字に広げる）
CREATE TABLE persistent_logins (
    username  VARCHAR(254) NOT NULL,
    series    VARCHAR(64)  NOT NULL,
    token     VARCHAR(64)  NOT NULL,
    last_used TIMESTAMP    NOT NULL,
    PRIMARY KEY (series)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.10 催促の実行（同じ月に2回送らないため、target_month は UNIQUE）
CREATE TABLE reminder_runs (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    target_month  CHAR(7)  NOT NULL,
    started_at    DATETIME NOT NULL,
    finished_at   DATETIME NULL,
    success_count INT      NOT NULL,
    failure_count INT      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reminder_runs_target_month UNIQUE (target_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.11 催促の宛先
CREATE TABLE reminder_logs (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    run_id         BIGINT       NOT NULL,
    user_id        BIGINT       NOT NULL,
    sent_at        DATETIME     NOT NULL,
    result         VARCHAR(10)  NOT NULL,
    failure_reason VARCHAR(200) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reminder_logs_run_id FOREIGN KEY (run_id) REFERENCES reminder_runs (id),
    CONSTRAINT fk_reminder_logs_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_reminder_logs_run_id (run_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.12 催促した備品
CREATE TABLE reminder_log_items (
    log_id       BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    PRIMARY KEY (log_id, equipment_id),
    CONSTRAINT fk_reminder_log_items_log_id FOREIGN KEY (log_id) REFERENCES reminder_logs (id),
    CONSTRAINT fk_reminder_log_items_equipment_id FOREIGN KEY (equipment_id) REFERENCES equipment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;

-- 5.13 祝日
CREATE TABLE holidays (
    holiday_on DATE        NOT NULL,
    name       VARCHAR(50) NOT NULL,
    PRIMARY KEY (holiday_on)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_ja_0900_as_cs_ks;
