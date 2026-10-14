package com.kh.back.prediction.dto;

import com.kh.back.price.dto.PriceDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Spring → FastAPI 요청 본문. FastAPI 의 SurgeRequest(Pydantic)와 모양이 같아야 한다.
 * { "code": "G0001", "prices": [ { "date": "2026-06-26", "open": ..., "volume": ... }, ... ] }
 */
@Getter
@AllArgsConstructor
public class SurgeRequest {

    private final String code;
    private final List<PriceDto> prices; // 날짜 오름차순
}
