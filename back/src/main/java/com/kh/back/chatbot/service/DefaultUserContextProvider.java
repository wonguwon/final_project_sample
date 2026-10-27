package com.kh.back.chatbot.service;

import org.springframework.stereotype.Component;

// 처음에는 이것으로 시작한다 — 넘길 데이터 없음.
// 우리 서비스 데이터로 바꿀 때는 이 클래스를 지우고 새로 만든다 (같은 인터페이스의 Bean 이 두 개면 서버가 뜨지 않는다)
@Component
public class DefaultUserContextProvider implements UserContextProvider {

    @Override
    public String build(Long memberId) {
        return "없음";
    }
}
