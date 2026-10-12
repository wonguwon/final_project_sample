package com.kh.back.auth.controller;

import com.kh.back.auth.dto.AuthTokens;
import com.kh.back.auth.dto.LoginRequest;
import com.kh.back.auth.dto.SignupRequest;
import com.kh.back.auth.dto.TokenResponse;
import com.kh.back.auth.service.AuthService;
import com.kh.back.common.dto.ApiResponse;
import com.kh.back.common.jwt.JwtProvider;
import com.kh.back.member.dto.MemberResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입 · 로그인 · 토큰 재발급 · 로그아웃.
 * 이 주소들은 로그인 전에도 부를 수 있어야 하므로 AuthInterceptor 를 걸지 않는다.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    // https 에서만 쿠키를 보내라는 Secure 속성. 로컬 http 개발에서는 false (application.yaml)
    @Value("${jwt.cookie-secure}")
    private boolean cookieSecure;

    // POST /api/v1/auth/signup — 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.signup(request)));
    }

    // POST /api/v1/auth/login — 로그인. access token 은 본문으로, refresh token 은 쿠키로 보낸다
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return withTokens(authService.login(request));
    }

    // POST /api/v1/auth/refresh — access token 재발급. 브라우저가 refresh token 쿠키를 자동으로 실어 보낸다
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        return withTokens(authService.refresh(refreshToken));
    }

    // POST /api/v1/auth/logout — DB 의 refresh token 을 지우고 쿠키도 지운다
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString()) // 유효 시간 0 = 삭제
                .body(ApiResponse.ok(null));
    }

    // 로그인 · 재발급 공통 응답: Set-Cookie 헤더(refresh token) + 본문(access token, 회원 정보)
    private ResponseEntity<ApiResponse<TokenResponse>> withTokens(AuthTokens tokens) {
        ResponseCookie cookie = refreshCookie(tokens.getRefreshToken(), jwtProvider.getRefreshTokenValidity());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.ok(new TokenResponse(tokens.getAccessToken(), tokens.getMember())));
    }

    private ResponseCookie refreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)            // JavaScript 에서 읽을 수 없다 → XSS 로 훔쳐 갈 수 없다
                .secure(cookieSecure)      // true 면 https 에서만 전송
                .sameSite("Strict")        // 다른 사이트에서 시작된 요청에는 실리지 않는다
                .path("/api/v1/auth")      // 재발급 · 로그아웃 요청에만 실린다
                .maxAge(maxAge)
                .build();
    }
}
