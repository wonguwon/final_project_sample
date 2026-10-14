package com.kh.back.post.service;

import com.kh.back.common.exception.ApiException;
import com.kh.back.common.file.FileStorage;
import com.kh.back.company.mapper.CompanyMapper;
import com.kh.back.post.dto.PostCreateRequest;
import com.kh.back.post.dto.PostDetail;
import com.kh.back.post.dto.PostDto;
import com.kh.back.post.dto.PostFileDto;
import com.kh.back.post.dto.PostFileResponse;
import com.kh.back.post.dto.PostListItem;
import com.kh.back.post.dto.PostUpdateRequest;
import com.kh.back.post.mapper.PostFileMapper;
import com.kh.back.post.mapper.PostMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final int MAX_FILES = 5; // 글 하나에 붙일 수 있는 파일 수

    private final PostMapper postMapper;
    private final PostFileMapper postFileMapper;
    private final CompanyMapper companyMapper;
    private final FileStorage fileStorage;

    @Override
    public List<PostListItem> getPosts(String code, int page, int size) {
        return postMapper.findAll(code, (page - 1) * size, size);
    }

    @Override
    public int countPosts(String code) {
        return postMapper.countAll(code);
    }

    @Override
    public PostDetail getPost(Long postId) {
        PostDetail post = postMapper.findDetail(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다.");
        }
        post.setFiles(postFileMapper.findByPostId(postId).stream().map(this::toResponse).toList());
        return post;
    }

    @Override
    @Transactional
    public PostDetail create(Long memberId, PostCreateRequest request) {
        if (companyMapper.findByCode(request.getCompanyCode()) == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "COMPANY_NOT_FOUND", "없는 종목입니다.");
        }
        List<MultipartFile> files = nonEmpty(request.getFiles());
        checkFileCount(files.size());

        PostDto post = new PostDto();
        post.setCompanyCode(request.getCompanyCode());
        post.setMemberId(memberId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        postMapper.insert(post); // 이후 post.getPostId() 에 새 번호

        saveFiles(post.getPostId(), files);
        return getPost(post.getPostId());
    }

    @Override
    @Transactional
    public PostDetail update(Long postId, Long memberId, PostUpdateRequest request) {
        PostDto post = getOwnedPost(postId, memberId);

        // 지금 붙어 있는 파일 중 "지우라고 한 것"만 고른다 (다른 글의 파일 번호를 보내도 무시된다)
        List<PostFileDto> current = postFileMapper.findByPostId(postId);
        List<PostFileDto> toDelete = current.stream()
                .filter(f -> request.getDeleteFileIds().contains(f.getFileId()))
                .toList();
        List<MultipartFile> newFiles = nonEmpty(request.getFiles());
        checkFileCount(current.size() - toDelete.size() + newFiles.size());

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        postMapper.update(post);

        for (PostFileDto file : toDelete) {
            postFileMapper.delete(file.getFileId());
            fileStorage.delete(file.getStoredName()); // 실제 삭제는 커밋에 성공한 뒤
        }
        saveFiles(postId, newFiles);
        return getPost(postId);
    }

    @Override
    @Transactional
    public void delete(Long postId, Long memberId) {
        getOwnedPost(postId, memberId);
        List<PostFileDto> files = postFileMapper.findByPostId(postId);

        postMapper.delete(postId); // post_file · comment 행은 ON DELETE CASCADE 로 함께 삭제
        files.forEach(f -> fileStorage.delete(f.getStoredName())); // 디스크 파일은 직접 지워야 한다
    }

    @Override
    public PostFileDto getFile(Long postId, Long fileId) {
        PostFileDto file = postFileMapper.findById(fileId);
        // 주소의 글 번호와 파일이 속한 글이 다르면 없는 것으로 본다
        if (file == null || !file.getPostId().equals(postId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다.");
        }
        return file;
    }

    // 파일을 디스크에 저장하고 정보를 post_file 에 넣는다
    private void saveFiles(Long postId, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            String storedName = fileStorage.store(file); // 형식이 허용되지 않으면 여기서 400

            PostFileDto dto = new PostFileDto();
            dto.setPostId(postId);
            dto.setOriginalName(StringUtils.getFilename(file.getOriginalFilename())); // 경로가 붙어 와도 이름만
            dto.setStoredName(storedName);
            // 브라우저가 보낸 Content-Type 은 믿지 않고, 확장자로 다시 정한다 (png 라면서 html 을 보내는 경우 차단)
            dto.setContentType(MediaTypeFactory.getMediaType(storedName)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM).toString());
            dto.setFileSize(file.getSize());
            postFileMapper.insert(dto);
        }
    }

    // 작성자 본인의 글인지 확인한다. 로그인(누구인가)과 권한(해도 되는가)은 다른 문제다
    private PostDto getOwnedPost(Long postId, Long memberId) {
        PostDto post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다.");
        }
        if (!post.getMemberId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "POST_FORBIDDEN", "작성자만 수정 · 삭제할 수 있습니다.");
        }
        return post;
    }

    private void checkFileCount(int count) {
        if (count > MAX_FILES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_LIMIT", "첨부 파일은 " + MAX_FILES + "개까지 올릴 수 있습니다.");
        }
    }

    // 파일을 고르지 않아도 빈 항목이 올 수 있으므로 걸러 낸다
    private List<MultipartFile> nonEmpty(List<MultipartFile> files) {
        return files == null ? List.of() : files.stream().filter(f -> !f.isEmpty()).toList();
    }

    private PostFileResponse toResponse(PostFileDto f) {
        String url = "/api/v1/posts/" + f.getPostId() + "/files/" + f.getFileId();
        return new PostFileResponse(f.getFileId(), f.getOriginalName(), f.getContentType(), f.getFileSize(),
                fileStorage.isImage(f.getStoredName()), url);
    }
}
