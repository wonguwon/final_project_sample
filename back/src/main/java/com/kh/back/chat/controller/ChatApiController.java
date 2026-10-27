package com.kh.back.chat.controller;

import com.kh.back.chat.dto.ChatMessageDto;
import com.kh.back.chat.dto.ChatRoomCreateRequest;
import com.kh.back.chat.dto.ChatRoomDto;
import com.kh.back.chat.service.ChatService;
import com.kh.back.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅방 · 지난 메시지 (REST). 실시간 메시지는 ChatMessageController (STOMP).
 * 모든 API 가 로그인 필요 — SecurityConfig 의 anyRequest().authenticated() 에 걸린다.
 */
@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatService chatService;

    // GET /api/v1/chat/rooms — 방 목록 + 내가 참여했는지
    @GetMapping
    public ApiResponse<List<ChatRoomDto>> rooms(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(chatService.getRooms(memberId));
    }

    // POST /api/v1/chat/rooms  { "title": "가온전자 토론방" }
    @PostMapping
    public ApiResponse<ChatRoomDto> create(@AuthenticationPrincipal Long memberId,
                                           @Valid @RequestBody ChatRoomCreateRequest request) {
        return ApiResponse.ok(chatService.createRoom(memberId, request.getTitle()));
    }

    // POST /api/v1/chat/rooms/1/members — 참여 (나를 이 방의 참여자로 추가)
    @PostMapping("/{roomId}/members")
    public ApiResponse<Void> join(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId) {
        chatService.joinRoom(roomId, memberId);
        return ApiResponse.ok(null);
    }

    // GET /api/v1/chat/rooms/1/messages?beforeId=120&size=30 — beforeId 보다 오래된 메시지 30개
    @GetMapping("/{roomId}/messages")
    public ApiResponse<List<ChatMessageDto>> messages(@AuthenticationPrincipal Long memberId,
                                                      @PathVariable Long roomId,
                                                      @RequestParam(required = false) Long beforeId,
                                                      @RequestParam(defaultValue = "30") int size) {
        size = Math.min(Math.max(size, 1), 100);   // 한 번에 최대 100개
        return ApiResponse.ok(chatService.getMessages(roomId, memberId, beforeId, size));
    }
}
