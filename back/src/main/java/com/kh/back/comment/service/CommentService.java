package com.kh.back.comment.service;

import com.kh.back.comment.dto.CommentDto;
import java.util.List;

public interface CommentService {

    List<CommentDto> getComments(Long postId);

    CommentDto addComment(Long postId, Long memberId, String content);

    void deleteComment(Long postId, Long commentId, Long memberId);
}
