package com.kh.back.prediction.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FastAPI → Spring 응답 (FastAPI 의 SurgeResponse 와 같은 모양).
 * Spring 은 여기에 종목명만 더해 React 에 그대로 내려보낸다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SurgePrediction {

    private String code;
    private String companyName;        // FastAPI 응답에는 없다. Spring 이 채운다
    private LocalDate baseDate;        // 마지막 시세 날짜. 예측 대상은 이 날의 다음 거래일
    private double probability;        // 다음 거래일 거래량이 20일 평균보다 많을 확률
    private double baseRate;           // 평소 급증 비율
    private String level;              // HIGH · NORMAL · LOW
    private long lastVolume;
    private double volumeAvg20;
    private Map<String, Double> features;
    private ModelInfo modelInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ModelInfo {
        private List<String> trainPeriod;
        private double testAuc;
        private double baseRate;
        private String createdAt;
    }
}
