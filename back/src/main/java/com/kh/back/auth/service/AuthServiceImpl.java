package com.kh.back.auth.service;

import com.kh.back.auth.dto.AuthTokens;
import com.kh.back.auth.dto.LoginRequest;
import com.kh.back.auth.dto.RefreshTokenDto;
import com.kh.back.auth.dto.SignupRequest;
import com.kh.back.auth.mapper.RefreshTokenMapper;
import com.kh.back.common.exception.ApiException;
import com.kh.back.common.jwt.JwtProvider;
import com.kh.back.member.dto.MemberDto;
import com.kh.back.member.dto.MemberResponse;
import com.kh.back.member.mapper.MemberMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberMapper memberMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberMapper.findByLoginId(request.getLoginId()) != null) {
            throw new ApiException(HttpStatus.CONFLICT, "MEMBER_DUPLICATED", "이미 사용 중인 아이디입니다.");
        }

        MemberDto member = new MemberDto();
        member.setLoginId(request.getLoginId());
        member.setPassword(passwordEncoder.encode(request.getPassword())); // 원래 비밀번호는 저장하지 않는다
        member.setNickname(request.getNickname());
        memberMapper.insert(member); // 이후 member.getMemberId() 에 새 번호가 들어 있다

        return MemberResponse.from(member);
    }

    @Override
    @Transactional
    public AuthTokens login(LoginRequest request) {
        MemberDto member = memberMapper.findByLoginId(request.getLoginId());

        // 아이디가 없을 때와 비밀번호가 틀렸을 때 같은 메시지를 쓴다 → 어떤 아이디가 있는지 알려 주지 않기 위해
        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return issueTokens(member);
    }

    @Override
    @Transactional
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null) {
            throw refreshInvalid();
        }

        RefreshTokenDto saved = refreshTokenMapper.findByToken(refreshToken);
        // DB 에 없으면 로그아웃했거나 이미 한 번 쓴(교체된) 토큰, 기간이 지났으면 만료된 토큰
        if (saved == null || saved.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw refreshInvalid();
        }

        // 한 번 쓴 refresh token 은 지우고 새로 발급한다 (rotation)
        refreshTokenMapper.deleteByToken(refreshToken);

        MemberDto member = memberMapper.findById(saved.getMemberId());
        if (member == null) {
            throw refreshInvalid();
        }
        return issueTokens(member);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenMapper.deleteByToken(refreshToken); // DB 에서 지우면 그 refresh token 은 더 이상 쓸 수 없다
        }
    }

    // access token(JWT) 과 refresh token(무작위 문자열)을 함께 발급한다
    private AuthTokens issueTokens(MemberDto member) {
        String accessToken = jwtProvider.createAccessToken(member.getMemberId());

        RefreshTokenDto refresh = new RefreshTokenDto();
        refresh.setMemberId(member.getMemberId());
        refresh.setToken(UUID.randomUUID().toString()); // 추측할 수 없는 무작위 값
        refresh.setExpiresAt(LocalDateTime.now().plus(jwtProvider.getRefreshTokenValidity()));
        refreshTokenMapper.insert(refresh);

        return new AuthTokens(accessToken, refresh.getToken(), MemberResponse.from(member));
    }

    private ApiException refreshInvalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_INVALID", "다시 로그인해 주세요.");
    }
}
