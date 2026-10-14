package com.kh.back.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * post_file 테이블 1행. storedName(디스크 이름)은 서버 안에서만 쓰고 응답에는 내보내지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PostFileDto {

    private Long fileId;
    private Long postId;
    private String originalName;
    private String storedName;
    private String contentType;
    private long fileSize;
}
