package com.kh.back.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 브라우저가 STOMP 로 보내는 메시지 { "content": "..." }.
 * 보낸 사람 · 방 번호는 받지 않는다 — 보낸 사람은 연결의 주인(Principal), 방 번호는 주소에서 정한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatSendRequest {

    private String content;
}
