package com.kh.back.search.client;

import com.kh.back.common.exception.ApiException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Elasticsearch 를 부르는 클라이언트. ES 도 HTTP 로 JSON 을 주고받는 서버이므로 MlClient 와 같은 방법으로 부른다.
 * 9200 은 Spring 서버만 안다. React 는 절대 직접 부르지 않는다.
 */
@Slf4j
@Component
public class EsClient {

    private final RestClient restClient;

    public EsClient(
            @Value("${search.es.base-url}") String baseUrl,
            @Value("${search.es.connect-timeout}") Duration connectTimeout,
            @Value("${search.es.read-timeout}") Duration readTimeout) {

        // MlClient 와 같다. 시간 제한이 없으면 ES 가 멈췄을 때 Spring 요청도 끝없이 기다린다
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    // POST /{index}/_search — Kibana 에서 치던 그 요청
    public EsSearchResponse search(String index, Map<String, Object> body) {
        try {
            return restClient.post()
                    .uri("/{index}/_search", index)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)                          // Map → JSON (Jackson)
                    .retrieve()
                    .body(EsSearchResponse.class);       // JSON → 필요한 부분만

        } catch (ResourceAccessException e) {
            log.warn("검색 서버 연결 실패: {}", e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SEARCH_UNAVAILABLE", "검색 서버에 연결할 수 없습니다.");

        } catch (RestClientResponseException e) {
            // ES 가 알려 준 이유(쿼리 오류 등)는 로그에만 남긴다
            log.warn("검색 서버 오류 응답: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SEARCH_INDEX_MISSING", "검색 데이터가 준비되지 않았습니다.");
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "SEARCH_ERROR", "검색 중 오류가 발생했습니다.");
        }
    }
}
