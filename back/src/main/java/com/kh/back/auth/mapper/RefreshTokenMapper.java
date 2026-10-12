package com.kh.back.auth.mapper;

import com.kh.back.auth.dto.RefreshTokenDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper {

    void insert(RefreshTokenDto refreshToken);

    RefreshTokenDto findByToken(String token);

    void deleteByToken(String token);
}
