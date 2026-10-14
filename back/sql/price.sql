-- 일별 시세 테이블 (강의 09_database/02_schema.py 의 daily_price 와 같은 구조)
-- 테이블을 만든 뒤 ml/scripts/load_prices.py 로 prices.csv 9만 행을 적재한다.
USE final_sample;

CREATE TABLE IF NOT EXISTS daily_price (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20)   NOT NULL,
    date        DATE          NOT NULL,
    open        BIGINT,
    high        BIGINT,
    low         BIGINT,
    close       BIGINT,
    volume      BIGINT,
    `change`    BIGINT,                 -- change 는 예약어라 백틱으로 감싼다
    changeRate  DECIMAL(6, 2),
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code_date (code, date) -- 같은 종목 · 같은 날짜는 한 행만 (다시 적재해도 늘어나지 않게)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
