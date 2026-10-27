package com.kh.back.search.dto;

import com.kh.back.search.client.EsSearchResponse;
import java.util.List;
import java.util.Map;

// 화면에 줄 검색 결과 한 줄
public record NewsSearchItem(
        Integer newsId,
        String title,          // 강조 표시가 들어간 제목 (없으면 원래 제목)
        String snippet,        // 본문 중 검색어가 들어간 조각 (없으면 빈 문자열)
        List<String> companyNames,
        String sentiment,
        String publishedAt) {

    public static NewsSearchItem from(EsSearchResponse.Hit hit) {
        NewsDocument doc = hit.source();
        Map<String, List<String>> hl = (hit.highlight() == null) ? Map.of() : hit.highlight();

        String title = hl.containsKey("title") ? hl.get("title").get(0) : doc.title();
        String snippet = hl.containsKey("content") ? hl.get("content").get(0) : "";
        return new NewsSearchItem(doc.newsId(), title, snippet, doc.companyNames(), doc.sentiment(), doc.publishedAt());
    }
}
