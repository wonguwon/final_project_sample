package com.kh.back.chat.controller;

import com.kh.back.chat.dto.ChatMessageDto;
import com.kh.back.chat.dto.ChatSendRequest;
import com.kh.back.chat.service.ChatService;
import com.kh.back.common.dto.ApiResponse;
import com.kh.back.common.exception.ApiException;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller   // @RestController 가 아니다. 응답은 return 이 아니라 messagingTemplate 으로 보낸다
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 브라우저가 SEND /app/chat/rooms/1 → 이 메서드 (/app 은 빼고 적는다)
    @MessageMapping("/chat/rooms/{roomId}")
    public void send(@DestinationVariable Long roomId, @Payload ChatSendRequest request, Principal principal) {
        Long memberId = Long.valueOf(principal.getName());   // CONNECT 때 기록한 회원 번호

        // 1) 먼저 DB 에 저장한다
        ChatMessageDto saved = chatService.saveMessage(roomId, memberId, request.getContent());

        // 2) 저장된 메시지를 이 방 구독자 전원에게 보낸다 (보낸 사람 포함)
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, saved);
    }

    // send() 에서 ApiException 이 나면 → 보낸 사람에게만 /user/queue/errors 로 알린다. 연결은 유지된다
    @MessageExceptionHandler(ApiException.class)
    @SendToUser("/queue/errors")
    public ApiResponse<Void> handleError(ApiException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }
}
