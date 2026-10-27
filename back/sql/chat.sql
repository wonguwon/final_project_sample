-- 채팅 (2차 개발 미션 1. 채팅). member 테이블(init.sql)이 먼저 있어야 한다.
USE final_sample;

-- 채팅방
CREATE TABLE IF NOT EXISTS chat_room (
    room_id     BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100)  NOT NULL,
    created_by  BIGINT        NOT NULL,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 누가 어느 방에 참여했나 (회원 ↔ 채팅방 다대다)
CREATE TABLE IF NOT EXISTS chat_room_member (
    room_id     BIGINT    NOT NULL,
    member_id   BIGINT    NOT NULL,
    joined_at   DATETIME  DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, member_id),          -- 같은 방에 두 번 참여할 수 없다
    FOREIGN KEY (room_id) REFERENCES chat_room (room_id),
    FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 메시지
CREATE TABLE IF NOT EXISTS chat_message (
    message_id  BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_id     BIGINT         NOT NULL,
    sender_id   BIGINT         NOT NULL,
    content     VARCHAR(1000)  NOT NULL,
    created_at  DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_room_message (room_id, message_id),   -- "이 방의 최근 메시지" 를 빠르게
    FOREIGN KEY (room_id) REFERENCES chat_room (room_id),
    FOREIGN KEY (sender_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
