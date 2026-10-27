package com.kh.back.notification.service;

import com.kh.back.common.exception.ApiException;
import com.kh.back.common.sse.SseEmitters;
import com.kh.back.notification.dto.NotificationDto;
import com.kh.back.notification.mapper.NotificationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SseEmitters sseEmitters;

    @Override
    public SseEmitter subscribe(Long memberId) {
        return sseEmitters.add(memberId);
    }

    @Override
    public void send(Long memberId, String type, String message, String link) {
        NotificationDto notification = new NotificationDto();
        notification.setMemberId(memberId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setLink(link);
        notificationMapper.insert(notification);   // ① 먼저 저장 (접속해 있지 않아도 남는다)

        NotificationDto saved = notificationMapper.findById(notification.getNotificationId());
        sseEmitters.send(memberId, "notification", saved.getNotificationId(), saved);   // ② 접속해 있으면 바로
    }

    @Override
    public List<NotificationDto> getRecent(Long memberId, int size) {
        return notificationMapper.findRecent(memberId, size);
    }

    @Override
    public int countUnread(Long memberId) {
        return notificationMapper.countUnread(memberId);
    }

    @Override
    public void read(Long memberId, Long notificationId) {
        // WHERE 에 member_id 를 함께 → 남의 알림 번호를 넣어도 바뀌지 않는다
        if (notificationMapper.markRead(notificationId, memberId) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다.");
        }
    }

    @Override
    public void readAll(Long memberId) {
        notificationMapper.markAllRead(memberId);
    }
}
