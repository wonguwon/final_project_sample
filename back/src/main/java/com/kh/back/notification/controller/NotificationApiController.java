package com.kh.back.notification.controller;

import com.kh.back.common.dto.ApiResponse;
import com.kh.back.notification.dto.NotificationDto;
import com.kh.back.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 알림. 모든 API 가 로그인 필요 — 받는 사람은 항상 로그인한 회원(@AuthenticationPrincipal)이다.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;

    // SSE 연결. ApiResponse 로 감싸지 않는다 — 응답이 JSON 이 아니라 이벤트 흐름이다
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long memberId) {
        return notificationService.subscribe(memberId);
    }

    // GET /api/v1/notifications?size=20 — 최근 알림
    @GetMapping
    public ApiResponse<List<NotificationDto>> list(@AuthenticationPrincipal Long memberId,
                                                   @RequestParam(defaultValue = "20") int size) {
        size = Math.min(Math.max(size, 1), 50);
        return ApiResponse.ok(notificationService.getRecent(memberId, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(notificationService.countUnread(memberId));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> read(@AuthenticationPrincipal Long memberId, @PathVariable Long notificationId) {
        notificationService.read(memberId, notificationId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> readAll(@AuthenticationPrincipal Long memberId) {
        notificationService.readAll(memberId);
        return ApiResponse.ok(null);
    }

    // 확인용 — 나에게 알림 하나 보내기. 발표 전에 지우거나 관리자만 쓰게 막는다
    @PostMapping("/test")
    public ApiResponse<Void> test(@AuthenticationPrincipal Long memberId) {
        notificationService.send(memberId, "TEST", "테스트 알림입니다.", "/mypage");
        return ApiResponse.ok(null);
    }
}
