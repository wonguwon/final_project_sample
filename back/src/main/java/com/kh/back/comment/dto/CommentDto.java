package com.kh.back.comment.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * comment 테이블 1행. 응답에는 닉네임까지 담아 보낸다 (CommentMapper.xml 에서 member 와 JOIN).
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentDto {

    private Long commentId;
    private Long postId;
    private Long memberId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
}
