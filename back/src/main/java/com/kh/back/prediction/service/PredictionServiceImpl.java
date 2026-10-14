package com.kh.back.prediction.service;

import com.kh.back.common.exception.ApiException;
import com.kh.back.company.dto.CompanyDto;
import com.kh.back.company.mapper.CompanyMapper;
import com.kh.back.prediction.client.MlClient;
import com.kh.back.prediction.dto.SurgePrediction;
import com.kh.back.prediction.dto.SurgeRequest;
import com.kh.back.price.dto.PriceDto;
import com.kh.back.price.mapper.PriceMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private static final int RECENT_DAYS = 30; // FastAPI 로 보낼 최근 시세 일수
    private static final int MIN_DAYS = 21;    // 피처 계산에 필요한 최소 일수 (FastAPI 와 같은 값)

    private final CompanyMapper companyMapper;
    private final PriceMapper priceMapper;
    private final MlClient mlClient;

    @Override
    public SurgePrediction predictSurge(String code) {
        CompanyDto company = companyMapper.findByCode(code);
        if (company == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "COMPANY_NOT_FOUND", "종목을 찾을 수 없습니다: " + code);
        }

        // 1) 데이터는 Spring 이 DB 에서 읽는다
        List<PriceDto> prices = priceMapper.findRecent(code, RECENT_DAYS);
        if (prices.size() < MIN_DAYS) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PRICE_NOT_FOUND", "예측에 필요한 시세가 부족합니다.");
        }

        // 2) 계산은 FastAPI 가 한다
        SurgePrediction prediction = mlClient.predictSurge(new SurgeRequest(code, prices));

        // 3) 화면에 필요한 정보를 더해 돌려준다
        prediction.setCompanyName(company.getName());
        return prediction;
    }
}
