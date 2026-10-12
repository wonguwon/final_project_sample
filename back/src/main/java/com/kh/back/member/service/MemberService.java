package com.kh.back.member.service;

import com.kh.back.member.dto.MemberResponse;

public interface MemberService {

    MemberResponse getMember(Long memberId);
}
