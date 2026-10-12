package com.kh.back.auth.dto;

import com.kh.back.member.dto.MemberResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Service 가 Controller 에 넘기는 로그인 결과.
 * Controller 는 refreshToken 을 쿠키로, 나머지를 TokenResponse 본문으로 나눠 보낸다.
 */
@Getter
@AllArgsConstructor
public class AuthTokens {

    private final String accessToken;
    private final String refreshToken;
    private final MemberResponse member;
}
