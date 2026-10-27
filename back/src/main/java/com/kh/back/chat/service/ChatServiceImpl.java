package com.kh.back.chat.service;

import com.kh.back.chat.dto.ChatMessageDto;
import com.kh.back.chat.dto.ChatRoomDto;
import com.kh.back.chat.mapper.ChatMapper;
import com.kh.back.common.exception.ApiException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int MAX_LENGTH = 1000;   // chat_message.content VARCHAR(1000)

    private final ChatMapper chatMapper;

    @Override
    public List<ChatRoomDto> getRooms(Long memberId) {
        return chatMapper.findRooms(memberId);
    }

    @Override
    @Transactional
    public ChatRoomDto createRoom(Long memberId, String title) {
        ChatRoomDto room = new ChatRoomDto();
        room.setTitle(title.strip());
        chatMapper.insertRoom(room, memberId);                 // useGeneratedKeys → roomId
        chatMapper.insertMember(room.getRoomId(), memberId);   // 만든 사람은 자동 참여
        return chatMapper.findRoom(room.getRoomId(), memberId);
    }

    @Override
    public void joinRoom(Long roomId, Long memberId) {
        if (chatMapper.findRoom(roomId, memberId) == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.");
        }
        chatMapper.insertMember(roomId, memberId);   // INSERT IGNORE — 이미 참여했으면 아무 일도 없다
    }

    @Override
    public List<ChatMessageDto> getMessages(Long roomId, Long memberId, Long beforeId, int size) {
        // REST 로 읽을 때도 참여자인지 확인한다 (WebSocket 과 같은 규칙)
        if (chatMapper.countMember(roomId, memberId) == 0) {
            throw new ApiException(HttpStatus.FORBIDDEN, "CHAT_FORBIDDEN", "참여하지 않은 채팅방입니다.");
        }
        return chatMapper.findMessages(roomId, beforeId, size);
    }

    // 참여자인지는 StompAuthInterceptor 가 SEND 때 이미 확인했다
    @Override
    @Transactional
    public ChatMessageDto saveMessage(Long roomId, Long memberId, String content) {
        String text = (content == null) ? "" : content.strip();   // 앞뒤 공백 제거
        if (text.isEmpty() || text.length() > MAX_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CHAT_INVALID", "메시지는 1~1000자로 입력합니다.");
        }

        ChatMessageDto message = new ChatMessageDto();
        message.setRoomId(roomId);
        message.setSenderId(memberId);
        message.setContent(text);
        chatMapper.insertMessage(message);   // useGeneratedKeys → messageId 가 채워진다

        // 보낸 사람 이름 · 저장 시각까지 채운 모양으로 다시 읽어서 돌려준다
        return chatMapper.findMessageById(message.getMessageId());
    }
}
