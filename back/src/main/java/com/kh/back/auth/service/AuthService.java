package com.kh.back.auth.service;

import com.kh.back.auth.dto.AuthTokens;
import com.kh.back.auth.dto.LoginRequest;
import com.kh.back.auth.dto.SignupRequest;
import com.kh.back.member.dto.MemberResponse;

public interface AuthService {

    MemberResponse signup(SignupRequest request);

    AuthTokens login(LoginRequest request);

    AuthTokens refresh(String refreshToken);

    void logout(String refreshToken);
}
