package com.kh.back.common.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 설정 (채팅).
 * 브라우저 → 서버는 /app, 서버 → 구독자는 /topic, 한 사람에게는 /user/queue 로 보낸다.
 */
@Configuration
@EnableWebSocketMessageBroker   // STOMP 메시지 처리를 켠다
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor stompAuthInterceptor;
    private final StompErrorHandler stompErrorHandler;

    // ① 연결 주소: ws://localhost:8080/ws
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173");   // React 개발 서버에서 오는 연결만 허용
        registry.setErrorHandler(stompErrorHandler);
    }

    // ② 주소 규칙
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");   // 브라우저 → 서버 (@MessageMapping)
        registry.enableSimpleBroker("/topic", "/queue");      // 서버 → 구독자 (Spring 안의 작은 우체국)
        registry.setUserDestinationPrefix("/user");           // 한 사람에게 (/user/queue/...)
    }

    // ③ 브라우저에서 들어오는 모든 STOMP 메시지가 이 인터셉터를 먼저 거친다
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
}
