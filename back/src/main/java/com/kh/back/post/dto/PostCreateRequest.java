package com.kh.back.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 글 작성 요청. JSON 이 아니라 multipart/form-data 로 받는다 (파일이 함께 오므로).
 * 같은 이름(files)으로 여러 번 보낸 파일이 List 로 모인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "종목을 선택해 주세요.")
    private String companyCode;

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    @Size(max = 5000, message = "내용은 5000자 이하로 입력해 주세요.")
    private String content;

    private List<MultipartFile> files = new ArrayList<>();
}
