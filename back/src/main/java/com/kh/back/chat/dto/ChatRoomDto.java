package com.kh.back.chat.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채팅방 목록 한 줄. 참여 인원과 "내가 참여했는지" 를 함께 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatRoomDto {

    private Long roomId;
    private String title;
    private int memberCount;
    private boolean joined;      // 내가 참여한 방인가
    private LocalDateTime createdAt;
}
