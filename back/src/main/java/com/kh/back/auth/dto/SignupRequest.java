package com.kh.back.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 요청 본문. Controller 의 @Valid 가 아래 조건을 검사하고,
 * 어긋나면 GlobalExceptionHandler 가 400 과 message 를 돌려준다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Size(min = 4, max = 30, message = "아이디는 4~30자로 입력해 주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해 주세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 30, message = "닉네임은 30자 이하로 입력해 주세요.")
    private String nickname;
}
