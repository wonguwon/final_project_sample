package com.kh.back.common.websocket;

import com.kh.back.chat.mapper.ChatMapper;
import com.kh.back.common.exception.ApiException;
import com.kh.back.common.jwt.JwtProvider;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * REST 의 JwtAuthenticationFilter 가 하던 일을 STOMP 에서 한다.
 *   CONNECT   : 토큰을 확인하고, 이 연결이 누구의 것인지 기록한다
 *   SUBSCRIBE : 이 방의 참여자인지 확인한다 (남의 방 대화를 엿듣지 못하게)
 *   SEND      : 이 방의 참여자인지 확인한다 (남의 방에 글을 쓰지 못하게)
 */
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    // 구독은 /topic, 전송은 /app 으로만. 주소에서 방 번호를 꺼낸다
    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/chat/rooms/(\\d+)$");   // SUBSCRIBE
    private static final Pattern ROOM_APP = Pattern.compile("^/app/chat/rooms/(\\d+)$");       // SEND
    private static final String MY_ERRORS = "/user/queue/errors";

    private final JwtProvider jwtProvider;
    private final ChatMapper chatMapper;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        // ① CONNECT — 토큰 확인. 연결 전체에서 한 번만 한다
        if (StompCommand.CONNECT.equals(command)) {
            String header = accessor.getFirstNativeHeader("Authorization");   // STOMP 헤더에서 꺼낸다
            if (header == null || !header.startsWith("Bearer ")) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "로그인이 필요합니다.");
            }
            // 만료 · 위조면 JwtProvider 가 TOKEN_EXPIRED · TOKEN_INVALID 를 던진다
            Long memberId = jwtProvider.getMemberId(header.substring("Bearer ".length()));

            // 이 연결의 주인을 기록한다. 이후 이 연결로 오는 모든 메시지에 자동으로 붙는다
            accessor.setUser(new UsernamePasswordAuthenticationToken(memberId, null, List.of()));
        }

        // ② ③ SUBSCRIBE · SEND — 정해 둔 주소만, 참여한 방만
        if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            String destination = accessor.getDestination();

            if (StompCommand.SUBSCRIBE.equals(command) && MY_ERRORS.equals(destination)) {
                return message;   // 내 오류 메시지 구독은 누구나
            }

            Pattern allowed = StompCommand.SUBSCRIBE.equals(command) ? ROOM_TOPIC : ROOM_APP;
            Matcher matcher = allowed.matcher(destination == null ? "" : destination);
            if (!matcher.matches()) {
                throw new ApiException(HttpStatus.FORBIDDEN, "CHAT_FORBIDDEN", "허용되지 않은 주소입니다.");
            }

            Long roomId = Long.valueOf(matcher.group(1));
            Long memberId = (Long) ((Authentication) accessor.getUser()).getPrincipal();
            if (chatMapper.countMember(roomId, memberId) == 0) {
                throw new ApiException(HttpStatus.FORBIDDEN, "CHAT_FORBIDDEN", "참여하지 않은 채팅방입니다.");
            }
        }

        return message;
    }
}
