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
 * 글 수정 요청 (multipart/form-data).
 * 종목은 바꿀 수 없다. 첨부는 "지울 파일 번호(deleteFileIds)"와 "새로 추가할 파일(files)"로 나눠 받는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    @Size(max = 5000, message = "내용은 5000자 이하로 입력해 주세요.")
    private String content;

    private List<Long> deleteFileIds = new ArrayList<>();

    private List<MultipartFile> files = new ArrayList<>();
}
