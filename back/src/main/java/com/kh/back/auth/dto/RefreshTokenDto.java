package com.kh.back.auth.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * refresh_token 테이블 1행.
 */
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenDto {

    private Long tokenId;
    private Long memberId;
    private String token;
    private LocalDateTime expiresAt;
}
