package com.example.webbanhang.controller;

import com.example.webbanhang.config.SecurityUtils;
import com.example.webbanhang.dto.request.PostRequest;
import com.example.webbanhang.dto.response.ApiResponse;
import com.example.webbanhang.dto.response.PostResponse;
import com.example.webbanhang.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * GET /api/posts              - Public: tất cả bài viết (mới nhất trước)
     * GET /api/posts?keyword=áo   - Public: tìm kiếm bài viết
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAll(
            @RequestParam(required = false) String keyword) {
        List<PostResponse> data = (keyword != null && !keyword.isBlank())
                ? postService.search(keyword)
                : postService.getAll();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * GET /api/posts/{id}         - Public: chi tiết bài viết
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getById(id)));
    }

    /**
     * POST /api/posts             - ADMIN: tạo bài viết
     * Body: { title, content, imageUrl }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @Valid @RequestBody PostRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Đăng bài viết thành công",
                postService.create(userId, request)));
    }

    /**
     * PUT /api/posts/{id}         - ADMIN: cập nhật bài viết
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody PostRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công",
                postService.update(userId, id, request)));
    }

    /**
     * DELETE /api/posts/{id}      - ADMIN: xóa bài viết
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        postService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", null));
    }
}