package com.kh.back.chatbot.dto;

// 대화 기록 한 줄 — role 은 USER 또는 ASSISTANT
public record ChatbotMessageDto(String role, String text) {}
