package com.kh.back.chat.mapper;

import com.kh.back.chat.dto.ChatMessageDto;
import com.kh.back.chat.dto.ChatRoomDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 파라미터가 2개 이상이면 @Param 으로 XML 에서 쓸 이름을 붙인다
@Mapper
public interface ChatMapper {

    int insertRoom(@Param("room") ChatRoomDto room, @Param("memberId") Long memberId);

    int insertMember(@Param("roomId") Long roomId, @Param("memberId") Long memberId);

    List<ChatRoomDto> findRooms(Long memberId);

    ChatRoomDto findRoom(@Param("roomId") Long roomId, @Param("memberId") Long memberId);

    int countMember(@Param("roomId") Long roomId, @Param("memberId") Long memberId);

    int insertMessage(ChatMessageDto message);

    ChatMessageDto findMessageById(Long messageId);

    List<ChatMessageDto> findMessages(@Param("roomId") Long roomId, @Param("beforeId") Long beforeId, @Param("size") int size);
}
