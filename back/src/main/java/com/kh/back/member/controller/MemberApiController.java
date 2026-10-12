package com.kh.back.member.controller;

import com.kh.back.common.dto.ApiResponse;
import com.kh.back.member.dto.MemberResponse;
import com.kh.back.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인한 회원만 부를 수 있는 API. (SecurityConfig 에서 /api/v1/auth/**, /api/v1/companies/** 외에는 로그인 필요)
 */
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // GET /api/v1/members/me — 내 정보
    // memberId 는 요청 값이 아니라 JwtAuthenticationFilter 가 토큰에서 꺼내 SecurityContext 에 넣어 둔 값이다
    @GetMapping("/me")
    public ApiResponse<MemberResponse> me(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(memberService.getMember(memberId));
    }
}
