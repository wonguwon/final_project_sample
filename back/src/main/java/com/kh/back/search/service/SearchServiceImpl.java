package com.kh.back.search.service;

import com.kh.back.common.exception.ApiException;
import com.kh.back.search.client.EsClient;
import com.kh.back.search.client.EsSearchResponse;
import com.kh.back.search.dto.NewsSearchItem;
import com.kh.back.search.dto.SearchResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    // 강조 시작 · 끝 표시. HTML 태그(<em>) 대신 본문에 나올 일이 없는 문자로 위치만 표시한다 (React 가 <mark> 로 그린다)
    public static final String MARK_START = "";
    public static final String MARK_END = "";
    private static final Set<String> SENTIMENTS = Set.of("positive", "neutral", "negative");
    private static final int MAX_WINDOW = 10_000;       // ES 기본값: from + size 는 10,000 을 넘을 수 없다

    private final EsClient esClient;

    @Value("${search.es.news-index}")
    private String newsIndex;

    @Override
    public SearchResult searchNews(String q, String sentiment, String sort, int page, int size) {
        // ① 입력 확인
        String keyword = (q == null) ? "" : q.strip();
        if (keyword.isEmpty() || keyword.length() > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SEARCH_INVALID", "검색어는 1~100자로 입력합니다.");
        }
        if (sentiment != null && !SENTIMENTS.contains(sentiment)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SEARCH_INVALID", "sentiment 값이 올바르지 않습니다.");
        }
        int from = (page - 1) * size;
        if (from + size > MAX_WINDOW) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SEARCH_TOO_DEEP", "더 이상 결과를 볼 수 없습니다. 검색어를 좁혀 주세요.");
        }

        // ② Kibana 에서 완성한 쿼리를 Map 으로 (문자열로 이어 붙이지 않는다 — 검색어의 " } 를 Jackson 이 처리)
        List<Object> filters = new ArrayList<>();
        if (sentiment != null) {
            filters.add(Map.of("term", Map.of("sentiment", sentiment)));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", Map.of("bool", Map.of(
                "must", List.of(Map.of("multi_match", Map.of(
                        "query", keyword,
                        "fields", List.of("title^2", "content", "companyNames")))),
                "filter", filters)));
        body.put("highlight", Map.of(
                "pre_tags", List.of(MARK_START),
                "post_tags", List.of(MARK_END),
                "fields", Map.of(
                        "title", Map.of("number_of_fragments", 0),
                        "content", Map.of("fragment_size", 80, "number_of_fragments", 1))));
        body.put("_source", List.of("newsId", "title", "companyNames", "sentiment", "publishedAt"));
        body.put("from", from);
        body.put("size", size);
        if ("latest".equals(sort)) {
            body.put("sort", List.of(Map.of("publishedAt", "desc")));   // 없으면 관련도(_score) 순
        }

        // ③ 보내고, 화면에 필요한 모양으로
        EsSearchResponse response = esClient.search(newsIndex, body);
        List<NewsSearchItem> items = response.hits().hits().stream()
                .map(NewsSearchItem::from)
                .toList();
        return new SearchResult(items, (int) response.hits().total().value());
    }
}
