package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.PostRequest;
import com.example.webbanhang.dto.response.PostResponse;
import com.example.webbanhang.entity.Post;
import com.example.webbanhang.entity.User;
import com.example.webbanhang.repository.PostRepository;
import com.example.webbanhang.repository.UserRepository;
import com.example.webbanhang.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public PostResponse create(Integer userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .createdBy(user)
                .build();

        return toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse update(Integer userId, Integer postId, PostRequest request) {
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết"));

        if (!post.getCreatedBy().getUserId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền sửa bài viết này");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());

        return toResponse(postRepository.save(post));
    }

    @Override
    public void delete(Integer userId, Integer postId) {
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết"));

        if (!post.getCreatedBy().getUserId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền xóa bài viết này");
        }

        postRepository.delete(post);
    }

    @Override
    public PostResponse getById(Integer id) {
        return toResponse(postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết id: " + id)));
    }

    @Override
    public List<PostResponse> getAll() {
        return postRepository.findAllWithUser().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PostResponse> search(String keyword) {
        return postRepository.searchByKeywordWithUser(keyword).stream()
                .map(this::toResponse)
                .toList();
    }

    private PostResponse toResponse(Post p) {
        return PostResponse.builder()
                .postId(p.getPostId())
                .title(p.getTitle())
                .content(p.getContent())
                .imageUrl(p.getImageUrl())
                .createdById(p.getCreatedBy() != null ? p.getCreatedBy().getUserId() : null)
                .createdByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : "Admin")
                .createdAt(p.getCreatedAt())
                .build();
    }
}