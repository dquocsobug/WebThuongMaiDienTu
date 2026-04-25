package com.example.webbanhang.controller;

import com.example.webbanhang.config.SecurityUtils;
import com.example.webbanhang.dto.request.CommentRequest;
import com.example.webbanhang.dto.response.CommentResponse;
import com.example.webbanhang.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * GET /api/comments/product/{productId} - Public: bình luận theo sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getByProduct(
            @PathVariable Integer productId) {

        return ResponseEntity.ok(
                ApiResponse.success(commentService.getByProduct(productId))
        );
    }

    /**
     * POST /api/comments       - User: tạo bình luận
     * Body: { productId, content }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @Valid @RequestBody CommentRequest request) {

        Integer userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(
                ApiResponse.success("Bình luận thành công",
                        commentService.create(userId, request))
        );
    }

    /**
     * PUT /api/comments/{id}   - User: sửa bình luận của mình
     * Body: { content }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable Integer id,
            @RequestParam String content) {

        Integer userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật bình luận thành công",
                        commentService.update(userId, id, content))
        );
    }

    /**
     * DELETE /api/comments/{id} - User: xóa bình luận của mình
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {

        Integer userId = SecurityUtils.getCurrentUserId();

        commentService.delete(userId, id);

        return ResponseEntity.ok(
                ApiResponse.success("Xóa bình luận thành công", null)
        );
    }
}