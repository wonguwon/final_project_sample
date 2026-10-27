package com.kh.back.common.security;

import com.kh.back.common.jwt.JwtProvider;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정.
 * Security 는 의존성을 넣는 순간 "모든 주소를 잠그고, 로그인 폼 · 세션을 쓰는" 기본 동작을 한다.
 * 여기서 그 기본값을 끄고 "JWT 로 검사, 정해 둔 주소만 로그인 필요"로 바꾼다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ① 쓰지 않는 기본 기능 끄기
                .csrf(csrf -> csrf.disable())             // 세션 · 쿠키 로그인을 노리는 공격 방어. 토큰을 헤더로 보내므로 끈다
                .formLogin(form -> form.disable())        // Security 가 만들어 주는 로그인 페이지
                .httpBasic(basic -> basic.disable())      // 브라우저 팝업 로그인
                .logout(logout -> logout.disable())       // Security 기본 /logout. 우리는 /api/v1/auth/logout 을 쓴다

                // ② 세션을 만들지 않는다 → 요청마다 토큰으로만 판단 (STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ③ 주소별 접근 규칙 — 위에서부터 차례로 검사한다
                .authorizeHttpRequests(auth -> auth
                        // SSE 연결이 끝날 때 Spring 은 같은 요청을 한 번 더(ASYNC 디스패치) 처리한다.
                        // 처음 요청에서 이미 로그인을 확인했으므로 이 뒤처리 단계는 열어 둔다
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()      // 회원가입 · 로그인 · 재발급 · 로그아웃
                        .requestMatchers("/api/v1/companies/**").permitAll()  // 종목 조회는 누구나
                        // 게시글 · 댓글 · 첨부 파일 "조회"는 누구나 (작성 · 수정 · 삭제는 아래 규칙으로 로그인 필요)
                        // <img src> 나 다운로드 링크는 Authorization 헤더를 실을 수 없으므로 파일 조회는 반드시 공개해야 한다
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                        // 뉴스 검색은 로그인하지 않아도 쓸 수 있다 (조회만). 개인정보가 담긴 검색은 절대 열지 않는다
                        .requestMatchers(HttpMethod.GET, "/api/v1/search/news").permitAll()
                        // 채팅 WebSocket 연결 주소. 브라우저 WebSocket 은 Authorization 헤더를 실을 수 없으므로
                        // 로그인 확인은 STOMP CONNECT 때 StompAuthInterceptor 가 한다
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated())                        // 나머지는 로그인 필요

                // ④ 인증이 필요한데 없을 때의 응답 → 401 실패 봉투
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))

                // ⑤ 우리 토큰 필터를 Security 필터 체인에 끼워 넣는다
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호를 BCrypt 로 암호화하는 객체 → AuthServiceImpl 에서 주입받아 쓴다
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
