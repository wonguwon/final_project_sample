package com.kh.back.post.mapper;

import com.kh.back.post.dto.PostDetail;
import com.kh.back.post.dto.PostDto;
import com.kh.back.post.dto.PostListItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {

    // code 가 null 이면 전체 종목
    List<PostListItem> findAll(@Param("code") String code, @Param("offset") int offset, @Param("size") int size);

    int countAll(@Param("code") String code);

    PostDetail findDetail(Long postId);

    PostDto findById(Long postId);

    void insert(PostDto post);

    void update(PostDto post);

    void delete(Long postId);
}
