package com.kh.back.price.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * daily_price 한 행 중 예측에 필요한 값. FastAPI 로 그대로 보낸다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PriceDto {

    private LocalDate date;
    private Long open;
    private Long high;
    private Long low;
    private Long close;
    private Long volume;
}
