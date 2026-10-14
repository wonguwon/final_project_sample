package com.kh.back.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 프론트에 보내는 첨부 파일 정보.
 * url 을 <img src> 나 다운로드 링크에 그대로 쓴다.
 */
@Getter
@AllArgsConstructor
public class PostFileResponse {

    private final Long fileId;
    private final String originalName;
    private final String contentType;
    private final long fileSize;
    private final boolean image;  // true 면 화면에 미리보기, false 면 다운로드 링크
    private final String url;     // 예: /api/v1/posts/3/files/7
}
