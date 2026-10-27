package com.kh.back.chatbot.controller;

import com.kh.back.chatbot.dto.ChatbotMessageDto;
import com.kh.back.chatbot.dto.ChatbotReply;
import com.kh.back.chatbot.dto.ChatbotRequest;
import com.kh.back.chatbot.service.ChatbotService;
import com.kh.back.common.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 챗봇. /api/v1/chatbot/** 는 SecurityConfig 의 anyRequest().authenticated() 에 걸려 이미 로그인이 필요하다.
 */
@RestController
@RequestMapping("/api/v1/chatbot/messages")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final ChatbotService chatbotService;

    // POST /api/v1/chatbot/messages { "message": "..." } → { "reply": "...", "remaining": 27 }
    @PostMapping
    public ApiResponse<ChatbotReply> ask(@AuthenticationPrincipal Long memberId, @RequestBody ChatbotRequest request) {
        return ApiResponse.ok(chatbotService.ask(memberId, request.getMessage()));
    }

    // GET — 지금까지의 대화 (새로고침해도 이어서 보이게)
    @GetMapping
    public ApiResponse<List<ChatbotMessageDto>> history(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(chatbotService.getHistory(memberId));
    }

    // DELETE — 대화 지우고 새로 시작
    @DeleteMapping
    public ApiResponse<Void> clear(@AuthenticationPrincipal Long memberId) {
        chatbotService.clearHistory(memberId);
        return ApiResponse.ok(null);
    }
}
