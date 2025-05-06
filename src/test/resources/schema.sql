-- -- データベース作成
-- CREATE DATABASE helthlog;
--
-- -- データベースを使用
-- USE helthlog;

-- ユーザーごとのライフログ記録テーブル
CREATE TABLE daily_log
(
    user_id     BIGINT           NOT NULL,
    log_date    DATE             NOT NULL,
    sleep_hours DOUBLE PRECISION NOT NULL,
    mood_level  INT              NOT NULL,
    PRIMARY KEY (user_id, log_date)
);

-- 体組成（体重など）
CREATE TABLE body_metrics
(
    user_id             BIGINT           NOT NULL,
    log_date            DATE             NOT NULL,
    weight              DOUBLE PRECISION NOT NULL,
    body_fat_percentage DOUBLE PRECISION NOT NULL,
    muscle_mass         DOUBLE PRECISION NOT NULL,
    measured_at         TIMESTAMP        NOT NULL,
    PRIMARY KEY (user_id, log_date, measured_at)
);

-- 活動記録（運動）
CREATE TABLE activity
(
    user_id             BIGINT      NOT NULL,
    log_date            DATE        NOT NULL,
    type                VARCHAR(50) NOT NULL,
    started_at          TIMESTAMP   NOT NULL,
    duration_in_minutes INT         NOT NULL,
    distance_km         DOUBLE PRECISION,
    steps               INT,
    calories_burned     INT,
    PRIMARY KEY (user_id, log_date, started_at)
);

-- 食事記録
CREATE TABLE meal
(
    user_id   BIGINT NOT NULL,
    log_date  DATE   NOT NULL,
    time      TIME   NOT NULL,
    content   TEXT   NOT NULL,
    calories  INT,
    photo_url VARCHAR(255),
    PRIMARY KEY (user_id, log_date, time)
);

-- メンタルメモ
CREATE TABLE mental_note
(
    user_id          BIGINT    NOT NULL,
    log_date         DATE      NOT NULL,
    recorded_at      TIMESTAMP NOT NULL,
    stress_level     INT,
    motivation_level INT,
    note             TEXT,
    PRIMARY KEY (user_id, log_date, recorded_at)
);

