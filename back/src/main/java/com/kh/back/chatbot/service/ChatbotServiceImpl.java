package com.kh.back.chatbot.service;

import com.kh.back.chatbot.dto.ChatbotMessageDto;
import com.kh.back.chatbot.dto.ChatbotReply;
import com.kh.back.chatbot.util.PiiMasker;
import com.kh.back.common.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final int DAILY_LIMIT = 30;   // 한 사람이 하루에 질문할 수 있는 수
    private static final int MAX_LENGTH = 500;   // 질문 최대 글자 수

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final UserContextProvider userContextProvider;
    private final boolean enabled;

    // "회원번호:날짜" → 오늘 질문 수 (서버 메모리. 재시작하면 0 부터)
    private final Map<String, AtomicInteger> usage = new ConcurrentHashMap<>();

    public ChatbotServiceImpl(ChatClient.Builder builder,
                              ChatMemory chatMemory,
                              UserContextProvider userContextProvider,
                              @Value("classpath:prompts/chatbot-system.st") Resource systemPrompt,
                              @Value("${spring.ai.openai.api-key}") String apiKey) {
        this.chatMemory = chatMemory;
        this.userContextProvider = userContextProvider;
        this.chatClient = builder
                // 모든 질문에 붙는 역할 · 규칙. 문자셋을 꼭 지정한다 — 빼면 Windows 에서 MS949 로 읽어 한글이 깨진 채 OpenAI 로 간다
                .defaultSystem(systemPrompt, StandardCharsets.UTF_8)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())  // 지난 대화를 자동으로 붙이고 저장
                .build();

        this.enabled = !"no-key".equals(apiKey);
        if (!enabled) {
            log.warn("OPENAI_API_KEY 환경변수가 없어 챗봇이 꺼진 상태로 시작합니다.");
        }
    }

    @Override
    public ChatbotReply ask(Long memberId, String message) {
        // ① 키가 없는 PC — OpenAI 를 부르지 않고 바로 알린다
        if (!enabled) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CHATBOT_DISABLED", "챗봇이 설정되지 않은 서버입니다.");
        }

        // ② 입력 확인
        String question = (message == null) ? "" : message.strip();
        if (question.isEmpty() || question.length() > MAX_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CHATBOT_INVALID", "질문은 1~" + MAX_LENGTH + "자로 입력합니다.");
        }

        // ③ 하루 사용량
        int used = usage.computeIfAbsent(memberId + ":" + LocalDate.now(), k -> new AtomicInteger()).incrementAndGet();
        if (used > DAILY_LIMIT) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "CHATBOT_LIMIT", "오늘 질문 횟수(" + DAILY_LIMIT + "회)를 모두 썼습니다.");
        }

        // ④ OpenAI 에 질문
        try {
            String reply = chatClient.prompt()
                    .system(s -> s.param("userContext", userContextProvider.build(memberId)))    // {userContext} 채우기
                    .user(PiiMasker.mask(question))                                                // 개인정보를 가린 질문
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId(memberId)))  // 이 회원의 대화
                    .call()
                    .content();
            return new ChatbotReply(reply, DAILY_LIMIT - used);

        } catch (RuntimeException e) {
            // 키 오류 · 토큰 소진 · 시간 초과 · 네트워크 — 원인은 로그에만, 화면에는 정해 둔 메시지만
            log.warn("챗봇 호출 실패 (회원 {}): {}", memberId, e.toString());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CHATBOT_UNAVAILABLE", "챗봇을 잠시 사용할 수 없습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @Override
    public List<ChatbotMessageDto> getHistory(Long memberId) {
        return chatMemory.get(conversationId(memberId)).stream()
                .map(m -> new ChatbotMessageDto(m.getMessageType().name(), m.getText()))
                .toList();
    }

    @Override
    public void clearHistory(Long memberId) {
        chatMemory.clear(conversationId(memberId));
    }

    // 대화 번호는 서버가 정한다. 요청 값으로 받으면 남의 대화를 이어 갈 수 있다
    private String conversationId(Long memberId) {
        return "member-" + memberId;
    }
}
