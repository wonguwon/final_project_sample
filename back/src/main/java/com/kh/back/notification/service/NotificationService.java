package com.kh.back.notification.service;

import com.kh.back.notification.dto.NotificationDto;
import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    SseEmitter subscribe(Long memberId);

    // 다른 기능(Service)에서 이 메서드 하나만 부르면 된다
    void send(Long memberId, String type, String message, String link);

    List<NotificationDto> getRecent(Long memberId, int size);

    int countUnread(Long memberId);

    void read(Long memberId, Long notificationId);

    void readAll(Long memberId);
}
