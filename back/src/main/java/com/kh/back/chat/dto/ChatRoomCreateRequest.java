package com.kh.back.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomCreateRequest {

    @NotBlank(message = "방 이름을 입력하세요.")
    @Size(max = 100, message = "방 이름은 100자 이하입니다.")
    private String title;
}
