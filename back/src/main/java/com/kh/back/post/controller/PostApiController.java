package com.kh.back.post.controller;

import com.kh.back.common.dto.ApiResponse;
import com.kh.back.common.dto.PageMeta;
import com.kh.back.common.exception.ApiException;
import com.kh.back.common.file.FileStorage;
import com.kh.back.post.dto.PostCreateRequest;
import com.kh.back.post.dto.PostDetail;
import com.kh.back.post.dto.PostFileDto;
import com.kh.back.post.dto.PostListItem;
import com.kh.back.post.dto.PostUpdateRequest;
import com.kh.back.post.service.PostService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 종목 토론 게시판.
 * 조회(GET)는 누구나, 작성 · 수정 · 삭제는 로그인 필요 (SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;
    private final FileStorage fileStorage;

    // GET /api/v1/posts?code=G0001&page=1&size=10 — code 가 없으면 전체 종목
    @GetMapping
    public ApiResponse<List<PostListItem>> list(
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);

        List<PostListItem> posts = postService.getPosts(code, page, size);
        return ApiResponse.ok(posts, PageMeta.of(page, size, postService.countPosts(code)));
    }

    // GET /api/v1/posts/{postId}
    @GetMapping("/{postId}")
    public ApiResponse<PostDetail> detail(@PathVariable Long postId) {
        return ApiResponse.ok(postService.getPost(postId));
    }

    // POST /api/v1/posts — multipart/form-data (companyCode, title, content, files...)
    // JSON 이 아니라 폼 데이터이므로 @RequestBody 가 아니라 @ModelAttribute 로 받는다
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDetail>> create(
            @Valid @ModelAttribute PostCreateRequest request,
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(postService.create(memberId, request)));
    }

    // PUT /api/v1/posts/{postId} — multipart/form-data (title, content, deleteFileIds..., files...)
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostDetail> update(
            @PathVariable Long postId,
            @Valid @ModelAttribute PostUpdateRequest request,
            @AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(postService.update(postId, memberId, request));
    }

    // DELETE /api/v1/posts/{postId}
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(@PathVariable Long postId, @AuthenticationPrincipal Long memberId) {
        postService.delete(postId, memberId);
        return ApiResponse.ok(null);
    }

    // GET /api/v1/posts/{postId}/files/{fileId} — 첨부 파일 내려받기 (이미지는 화면에 바로 표시)
    // 이 응답은 ApiResponse 가 아니라 파일 자체다
    @GetMapping("/{postId}/files/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long postId, @PathVariable Long fileId) {
        PostFileDto file = postService.getFile(postId, fileId);
        Resource resource = fileStorage.load(file.getStoredName());
        if (!resource.exists()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다.");
        }

        // 이미지는 inline(브라우저에 표시), 나머지는 attachment(다운로드).
        // filename 에 한글이 있어도 깨지지 않게 UTF-8 로 인코딩한다 (filename*=UTF-8''...)
        boolean image = fileStorage.isImage(file.getStoredName());
        ContentDisposition disposition = (image ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }
}
