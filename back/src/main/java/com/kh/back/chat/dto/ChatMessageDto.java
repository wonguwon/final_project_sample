package com.kh.back.chat.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * chat_message 1행 + 보낸 사람 닉네임 (ChatMapper.xml 에서 member 와 JOIN).
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ChatMessageDto {

    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
}
