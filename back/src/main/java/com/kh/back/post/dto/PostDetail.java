package com.kh.back.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상세 화면 응답. memberId 는 프론트가 "내 글인가"(수정 · 삭제 버튼 표시)를 판단할 때 쓴다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PostDetail {

    private Long postId;
    private String companyCode;
    private String companyName;
    private Long memberId;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PostFileResponse> files; // SQL 이 아니라 Service 에서 따로 채운다
}
