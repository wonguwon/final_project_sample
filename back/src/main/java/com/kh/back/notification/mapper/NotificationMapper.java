package com.kh.back.notification.mapper;

import com.kh.back.notification.dto.NotificationDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper {

    void insert(NotificationDto notification);

    NotificationDto findById(Long notificationId);

    List<NotificationDto> findRecent(@Param("memberId") Long memberId, @Param("size") int size);

    int countUnread(Long memberId);

    int markRead(@Param("notificationId") Long notificationId, @Param("memberId") Long memberId);

    int markAllRead(Long memberId);
}
