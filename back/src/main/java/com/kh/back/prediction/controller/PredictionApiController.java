package com.kh.back.prediction.controller;

import com.kh.back.common.dto.ApiResponse;
import com.kh.back.prediction.dto.SurgePrediction;
import com.kh.back.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예측은 종목에 속하므로 종목 주소 아래에 둔다.
 * /api/v1/companies/** 는 SecurityConfig 에서 이미 공개되어 있다.
 */
@RestController
@RequiredArgsConstructor
public class PredictionApiController {

    private final PredictionService predictionService;

    // GET /api/v1/companies/{code}/prediction — 다음 거래일 거래량 급증 확률
    @GetMapping("/api/v1/companies/{code}/prediction")
    public ApiResponse<SurgePrediction> prediction(@PathVariable String code) {
        return ApiResponse.ok(predictionService.predictSurge(code));
    }
}
