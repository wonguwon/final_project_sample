package com.kh.back.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 댓글 작성 요청 (JSON). 파일이 없으므로 게시글과 달리 @RequestBody 로 받는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentRequest {

    @NotBlank(message = "댓글 내용을 입력해 주세요.")
    @Size(max = 1000, message = "댓글은 1000자 이하로 입력해 주세요.")
    private String content;
}
