package com.kh.back.chatbot.dto;

// 응답 — 답변과 오늘 남은 질문 수
public record ChatbotReply(String reply, int remaining) {}
