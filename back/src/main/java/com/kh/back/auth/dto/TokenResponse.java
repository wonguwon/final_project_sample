package com.kh.back.auth.dto;

import com.kh.back.member.dto.MemberResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 · 재발급 응답 본문.
 * refresh token 은 본문이 아니라 HttpOnly 쿠키로 보내므로 여기에 없다.
 */
@Getter
@AllArgsConstructor
public class TokenResponse {

    private final String accessToken;
    private final MemberResponse member;
}
