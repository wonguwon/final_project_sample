-- 종목 토론 게시판 테이블 (init.sql 을 실행한 뒤 실행)
USE final_sample;

-- 게시글 — 하나의 종목에 속한다
CREATE TABLE IF NOT EXISTS post (
    post_id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    company_code  VARCHAR(10)  NOT NULL,                          -- 어느 종목의 글인가
    member_id     BIGINT       NOT NULL,                          -- 작성자
    title         VARCHAR(100) NOT NULL,
    content       TEXT         NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 수정할 때 SQL 에서 NOW() 로 바꾼다
    FOREIGN KEY (company_code) REFERENCES company (code),
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
) DEFAULT CHARSET = utf8mb4;

-- 첨부 파일 — 글 하나에 여러 개. 파일 자체는 서버 폴더에, 여기에는 정보만 저장한다
CREATE TABLE IF NOT EXISTS post_file (
    file_id        BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id        BIGINT       NOT NULL,
    original_name  VARCHAR(255) NOT NULL,                         -- 사용자가 올린 파일 이름 (다운로드할 때 이 이름으로)
    stored_name    VARCHAR(100) NOT NULL UNIQUE,                  -- 서버에 저장한 이름 (UUID + 확장자)
    content_type   VARCHAR(100) NOT NULL,                         -- image/png, application/pdf ...
    file_size      BIGINT       NOT NULL,                         -- 바이트
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE
) DEFAULT CHARSET = utf8mb4;

-- 댓글
CREATE TABLE IF NOT EXISTS comment (
    comment_id  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT        NOT NULL,
    member_id   BIGINT        NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
) DEFAULT CHARSET = utf8mb4;
