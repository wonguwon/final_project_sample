USE final_sample;

--종목
CREATE TABLE IF NOT EXISTS company (
    code        VARCHAR(10)  NOT NULL PRIMARY KEY,  -- 종목코드 (예: G0001)
    name        VARCHAR(100) NOT NULL,              -- 종목명
    sectorCode  VARCHAR(10),                        -- 업종 코드
    sectorName  VARCHAR(50),                        -- 업종명
    market      VARCHAR(20),                        -- 시장 구분
    marketCap   BIGINT                              -- 시가총액 (원)
) DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO company (code, name, sectorCode, sectorName, market, marketCap) VALUES
('G0001', '가온전자',     'S02', '전기전자', 'GX-MAIN',   770259456000),
('G0002', '단비자산운용', 'S01', '금융',     'GX-MAIN',   1285743666000),
('G0003', '보람금융지주', 'S01', '금융',     'GX-GROWTH', 114030611000),
('G0004', '푸른소프트',   'S08', 'IT서비스', 'GX-MAIN',   3450287724000),
('G0005', '샛별소프트',   'S08', 'IT서비스', 'GX-MAIN',   757112240000);

-- 회원
CREATE TABLE IF NOT EXISTS member (
    member_id   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login_id    VARCHAR(30)  NOT NULL UNIQUE,                     -- 로그인 아이디
    password    VARCHAR(100) NOT NULL,                            -- BCrypt 로 암호화한 값 (60자)
    nickname    VARCHAR(30)  NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) DEFAULT CHARSET = utf8mb4;

-- refresh token (로그인할 때마다 1행, 로그아웃하면 삭제)
CREATE TABLE IF NOT EXISTS refresh_token (
    token_id    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    token       VARCHAR(100) NOT NULL UNIQUE,                     -- 쿠키로 보낸 값과 같은 문자열
    expires_at  DATETIME     NOT NULL,                            -- 이 시각이 지나면 사용 불가
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
) DEFAULT CHARSET = utf8mb4;
