package com.kh.back.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// news 인덱스 문서 중 화면에 쓰는 필드만 (GET /news/_mapping 의 필드 이름과 같아야 한다)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsDocument(
        Integer newsId,
        String title,
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)   // 값이 하나뿐이어도 목록으로
        List<String> companyNames,
        String sentiment,
        String publishedAt) {}
