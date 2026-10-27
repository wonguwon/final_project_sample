package com.kh.back.search.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kh.back.search.dto.NewsDocument;
import java.util.List;
import java.util.Map;

// ES 응답 중 쓰는 부분만 받는다. 나머지(took, _shards, max_score …)는 무시
// { "hits": { "total": { "value": 87 }, "hits": [ { "_id", "_score", "_source", "highlight" } ] } }
@JsonIgnoreProperties(ignoreUnknown = true)
public record EsSearchResponse(Hits hits) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hits(Total total, List<Hit> hits) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Total(long value) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hit(
            @JsonProperty("_id") String id,
            @JsonProperty("_score") Double score,
            @JsonProperty("_source") NewsDocument source,
            Map<String, List<String>> highlight) {}   // 필드 이름 → 강조된 조각들
}
