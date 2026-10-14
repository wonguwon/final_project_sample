package com.kh.back.post.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 목록 화면의 한 줄. 내용(content) 대신 댓글 수 · 첨부 수를 담는다.
 * 종목명 · 닉네임은 JOIN 으로, 개수는 서브쿼리로 한 번에 가져온다 (PostMapper.xml)
 */
@Getter
@Setter
@NoArgsConstructor
public class PostListItem {

    private Long postId;
    private String companyCode;
    private String companyName;
    private String title;
    private String nickname;
    private int commentCount;
    private int fileCount;
    private LocalDateTime createdAt;
}
