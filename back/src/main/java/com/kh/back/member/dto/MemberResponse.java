package com.kh.back.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 프론트에 보내는 회원 정보. 비밀번호는 절대 넣지 않는다.
 */
@Getter
@AllArgsConstructor
public class MemberResponse {

    private final Long memberId;
    private final String loginId;
    private final String nickname;

    public static MemberResponse from(MemberDto member) {
        return new MemberResponse(member.getMemberId(), member.getLoginId(), member.getNickname());
    }
}
