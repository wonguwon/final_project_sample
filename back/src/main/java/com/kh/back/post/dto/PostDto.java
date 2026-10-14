package com.kh.back.post.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * post 테이블 1행. (저장 · 수정 · 작성자 확인에 쓴다)
 */
@Getter
@Setter
@NoArgsConstructor
public class PostDto {

    private Long postId;
    private String companyCode;
    private Long memberId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
