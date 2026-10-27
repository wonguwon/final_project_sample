package com.kh.back.chatbot.service;

/**
 * 챗봇에 넘길 "이 회원의 데이터" — 시스템 프롬프트의 {userContext} 에 들어간다.
 * 반드시 memberId 로 조회하고, 답에 필요 없는 개인정보(이름 · 연락처 등)는 넣지 않는다.
 */
public interface UserContextProvider {

    String build(Long memberId);
}
