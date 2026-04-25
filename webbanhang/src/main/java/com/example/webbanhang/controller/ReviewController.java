package com.example.webbanhang.controller;

import com.example.webbanhang.config.SecurityUtils;
import com.example.webbanhang.dto.request.ReviewRequest;
import com.example.webbanhang.dto.response.ReviewResponse;
import com.example.webbanhang.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * GET /api/reviews/product/{productId} - Public: đánh giá theo sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByProduct(
            @PathVariable Integer productId) {

        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getByProduct(productId)));
    }

    /**
     * GET /api/reviews/my     - User: xem các đánh giá của mình
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(reviewService.getByUser(userId)));
    }

    /**
     * POST /api/reviews        - User: tạo đánh giá
     * Body: { productId, rating (1-5), comment }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody ReviewRequest request) {

        Integer userId = SecurityUtils.getCurrentUserId(); // OK vì endpoint này cần login

        return ResponseEntity.ok(
                ApiResponse.success("Đánh giá thành công",
                        reviewService.create(userId, request)));
    }

    /**
     * PUT /api/reviews/{id}   - User: sửa đánh giá của mình
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ReviewRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đánh giá thành công",
                reviewService.update(userId, id, request)));
    }

    /**
     * DELETE /api/reviews/{id} - User: xóa đánh giá của mình
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        Integer userId = null;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        reviewService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công", null));
    }
}