package com.kh.back.prediction.client;

import com.kh.back.common.exception.ApiException;
import com.kh.back.prediction.dto.SurgePrediction;
import com.kh.back.prediction.dto.SurgeRequest;
import java.net.http.HttpClient;
import java.time.Duration;
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
 * FastAPI(예측 서버)를 부르는 클라이언트.
 * React 는 FastAPI 를 직접 부르지 않는다. 항상 Spring 을 거친다 (서비스의 입구는 하나).
 */
@Slf4j
@Component
public class MlClient {

    private final RestClient restClient;

    public MlClient(
            @Value("${ml.base-url}") String baseUrl,
            @Value("${ml.connect-timeout}") Duration connectTimeout,
            @Value("${ml.read-timeout}") Duration readTimeout) {

        // 시간 제한을 꼭 둔다. 없으면 예측 서버가 멈췄을 때 Spring 요청도 끝없이 기다린다
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)    // 연결이 안 되면 이 시간 뒤 포기
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout); // 연결은 됐는데 응답이 늦으면 이 시간 뒤 포기

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    // POST {ml.base-url}/predict/surge
    public SurgePrediction predictSurge(SurgeRequest request) {
        try {
            return restClient.post()
                    .uri("/predict/surge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)                     // 객체 → JSON
                    .retrieve()
                    .body(SurgePrediction.class);      // JSON → 객체
        } catch (ResourceAccessException e) {
            // 연결 실패 · 시간 초과 — 예측 서버가 꺼져 있거나 응답이 없다
            log.warn("예측 서버 연결 실패: {}", e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ML_UNAVAILABLE", "예측 서버에 연결할 수 없습니다.");
        } catch (RestClientResponseException e) {
            // 예측 서버가 4xx · 5xx 로 응답 — 요청 형식이 다르거나 예측 서버 안에서 오류
            log.warn("예측 서버 오류 응답: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "ML_ERROR", "예측 서버가 오류를 반환했습니다.");
        }
    }
}
