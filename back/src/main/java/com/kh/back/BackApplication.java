package com.kh.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// UserDetailsServiceAutoConfiguration 제외:
// Spring Security 가 기본으로 만드는 임시 계정(user / 실행할 때마다 바뀌는 비밀번호)을 만들지 않는다.
// 우리는 member 테이블과 JWT 로 로그인하므로 필요 없다.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling   // @Scheduled 가 동작하게 한다 (SSE 알림 30초 ping)
public class BackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}

}
