package com.kh.back.post.service;

import com.kh.back.post.dto.PostCreateRequest;
import com.kh.back.post.dto.PostDetail;
import com.kh.back.post.dto.PostFileDto;
import com.kh.back.post.dto.PostListItem;
import com.kh.back.post.dto.PostUpdateRequest;
import java.util.List;

public interface PostService {

    List<PostListItem> getPosts(String code, int page, int size);

    int countPosts(String code);

    PostDetail getPost(Long postId);

    PostDetail create(Long memberId, PostCreateRequest request);

    PostDetail update(Long postId, Long memberId, PostUpdateRequest request);

    void delete(Long postId, Long memberId);

    PostFileDto getFile(Long postId, Long fileId);
}
