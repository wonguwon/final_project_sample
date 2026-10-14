package com.kh.back.comment.controller;

import com.kh.back.comment.dto.CommentDto;
import com.kh.back.comment.dto.CommentRequest;
import com.kh.back.comment.service.CommentService;
import com.kh.back.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 댓글은 게시글에 속하므로 주소도 게시글 아래에 둔다: /api/v1/posts/{postId}/comments
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    // GET /api/v1/posts/{postId}/comments
    @GetMapping
    public ApiResponse<List<CommentDto>> list(@PathVariable Long postId) {
        return ApiResponse.ok(commentService.getComments(postId));
    }

    // POST /api/v1/posts/{postId}/comments — { "content": "..." }
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> add(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal Long memberId) {
        CommentDto comment = commentService.addComment(postId, memberId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(comment));
    }

    // DELETE /api/v1/posts/{postId}/comments/{commentId}
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long memberId) {
        commentService.deleteComment(postId, commentId, memberId);
        return ApiResponse.ok(null);
    }
}
