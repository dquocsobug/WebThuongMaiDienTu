package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.ReviewRequest;
import com.example.webbanhang.dto.response.ReviewResponse;
import com.example.webbanhang.entity.Product;
import com.example.webbanhang.entity.Review;
import com.example.webbanhang.entity.User;
import com.example.webbanhang.repository.ProductRepository;
import com.example.webbanhang.repository.ReviewRepository;
import com.example.webbanhang.repository.UserRepository;
import com.example.webbanhang.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public ReviewResponse create(Integer userId, ReviewRequest request) {

        if (reviewRepository.existsByUserUserIdAndProductProductId(userId, request.getProductId())) {
            throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này rồi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponse update(Integer userId, Integer reviewId, ReviewRequest request) {
        Review review = findOrThrow(reviewId);
        checkOwner(review, userId);

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toResponse(reviewRepository.save(review));
    }

    @Override
    public void delete(Integer userId, Integer reviewId) {
        Review review = findOrThrow(reviewId);
        checkOwner(review, userId);
        reviewRepository.delete(review);
    }

    // 🔥 FIX QUAN TRỌNG: dùng JOIN FETCH query mới
    @Transactional(readOnly = true)
    @Override
    public List<ReviewResponse> getByProduct(Integer productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ReviewResponse> getByUser(Integer userId) {
        return reviewRepository.findByUserUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── helpers ─────────────────────────────────────────────

    private Review findOrThrow(Integer id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá id: " + id));
    }

    private void checkOwner(Review review, Integer userId) {
        if (!review.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền thao tác đánh giá này");
        }
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .reviewId(r.getReviewId())
                .userId(r.getUser().getUserId())
                .userFullName(r.getUser().getFullName())
                .productId(r.getProduct().getProductId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}