package com.kh.back.comment.service;

import com.kh.back.comment.dto.CommentDto;
import com.kh.back.comment.mapper.CommentMapper;
import com.kh.back.common.exception.ApiException;
import com.kh.back.post.mapper.PostMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;

    @Override
    public List<CommentDto> getComments(Long postId) {
        checkPostExists(postId);
        return commentMapper.findByPostId(postId);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long postId, Long memberId, String content) {
        checkPostExists(postId);

        CommentDto comment = new CommentDto();
        comment.setPostId(postId);
        comment.setMemberId(memberId);
        comment.setContent(content);
        commentMapper.insert(comment);

        return commentMapper.findById(comment.getCommentId()); // 닉네임 · 작성 시각까지 채워서 돌려준다
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long memberId) {
        CommentDto comment = commentMapper.findById(commentId);
        if (comment == null || !comment.getPostId().equals(postId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.");
        }
        if (!comment.getMemberId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "COMMENT_FORBIDDEN", "작성자만 삭제할 수 있습니다.");
        }
        commentMapper.delete(commentId);
    }

    private void checkPostExists(Long postId) {
        if (postMapper.findById(postId) == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다.");
        }
    }
}
