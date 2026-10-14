package com.kh.back.comment.mapper;

import com.kh.back.comment.dto.CommentDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {

    List<CommentDto> findByPostId(Long postId);

    CommentDto findById(Long commentId);

    void insert(CommentDto comment);

    void delete(Long commentId);
}
