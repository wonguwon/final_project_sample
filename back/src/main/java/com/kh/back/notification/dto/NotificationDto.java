package com.kh.back.notification.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * notification 테이블 1행. readAt 이 null 이면 안 읽은 알림.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class NotificationDto {

    private Long notificationId;
    private Long memberId;
    private String type;
    private String message;
    private String link;
    private LocalDateTime readAt;      // null 이면 안 읽음
    private LocalDateTime createdAt;
}
