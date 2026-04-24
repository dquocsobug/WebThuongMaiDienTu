package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.CommentRequest;
import com.example.webbanhang.dto.response.CommentResponse;
import com.example.webbanhang.entity.Comment;
import com.example.webbanhang.entity.Product;
import com.example.webbanhang.entity.User;
import com.example.webbanhang.repository.CommentRepository;
import com.example.webbanhang.repository.ProductRepository;
import com.example.webbanhang.repository.UserRepository;
import com.example.webbanhang.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public CommentResponse create(Integer userId, CommentRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        Comment comment = Comment.builder()
                .user(user)
                .product(product)
                .content(request.getContent())
                .build();

        return toResponse(commentRepository.save(comment));
    }

    @Override
    public CommentResponse update(Integer userId, Integer commentId, String content) {

        Comment comment = findOrThrow(commentId);
        checkOwner(comment, userId);

        comment.setContent(content);

        return toResponse(commentRepository.save(comment));
    }

    @Override
    public void delete(Integer userId, Integer commentId) {

        Comment comment = findOrThrow(commentId);
        checkOwner(comment, userId);

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentResponse> getByProduct(Integer productId) {
        return commentRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Comment findOrThrow(Integer id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy bình luận id: " + id));
    }

    private void checkOwner(Comment comment, Integer userId) {
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền thao tác bình luận này");
        }
    }

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .commentId(c.getCommentId())
                .userId(c.getUser().getUserId())
                .userFullName(c.getUser().getFullName())
                .productId(c.getProduct().getProductId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}