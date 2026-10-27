package com.kh.back.chat.service;

import com.kh.back.chat.dto.ChatMessageDto;
import com.kh.back.chat.dto.ChatRoomDto;
import java.util.List;

public interface ChatService {

    List<ChatRoomDto> getRooms(Long memberId);

    ChatRoomDto createRoom(Long memberId, String title);

    void joinRoom(Long roomId, Long memberId);

    List<ChatMessageDto> getMessages(Long roomId, Long memberId, Long beforeId, int size);

    ChatMessageDto saveMessage(Long roomId, Long memberId, String content);
}
