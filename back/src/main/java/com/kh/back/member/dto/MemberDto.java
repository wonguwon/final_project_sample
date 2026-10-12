package com.kh.back.member.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * member 테이블 1행. 암호화된 비밀번호까지 담고 있으므로 이 객체를 그대로 응답하지 않는다.
 * 응답에는 MemberResponse 를 쓴다.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "password") // 로그에 비밀번호가 찍히지 않게
public class MemberDto {

    private Long memberId;
    private String loginId;
    private String password;
    private String nickname;
    private LocalDateTime createdAt;
}
