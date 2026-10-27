package com.kh.back.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 질문 { "message": "..." } — 대화 번호는 받지 않는다 (서버가 로그인한 회원으로 정한다)
@Getter
@Setter
@NoArgsConstructor
public class ChatbotRequest {

    private String message;
}
