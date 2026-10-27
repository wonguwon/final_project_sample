package com.kh.back.chatbot.service;

import com.kh.back.chatbot.dto.ChatbotMessageDto;
import com.kh.back.chatbot.dto.ChatbotReply;
import java.util.List;

public interface ChatbotService {

    ChatbotReply ask(Long memberId, String message);

    List<ChatbotMessageDto> getHistory(Long memberId);

    void clearHistory(Long memberId);
}
