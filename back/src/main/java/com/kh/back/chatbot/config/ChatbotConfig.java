package com.kh.back.chatbot.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatbotConfig {

    // 자동 설정(기본 20개) 대신 이 Bean 을 쓴다. 저장 위치는 서버 메모리 (재시작하면 사라진다)
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)   // 질문 · 답변 합쳐 최근 10개만 다음 질문에 함께 보낸다 (토큰 절약)
                .build();
    }
}
