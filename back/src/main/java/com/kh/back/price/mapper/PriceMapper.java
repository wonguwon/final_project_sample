package com.kh.back.price.mapper;

import com.kh.back.price.dto.PriceDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PriceMapper {

    // 가장 최근 limit 일의 시세를 날짜 오름차순으로
    List<PriceDto> findRecent(@Param("code") String code, @Param("limit") int limit);
}
