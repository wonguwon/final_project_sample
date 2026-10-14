package com.kh.back.post.mapper;

import com.kh.back.post.dto.PostFileDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostFileMapper {

    List<PostFileDto> findByPostId(Long postId);

    PostFileDto findById(Long fileId);

    void insert(PostFileDto file);

    void delete(Long fileId);
}
