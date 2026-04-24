package com.example.webbanhang.controller;

import com.example.webbanhang.config.SecurityUtils;
import com.example.webbanhang.dto.request.UpdateUserRequest;
import com.example.webbanhang.dto.response.ApiResponse;
import com.example.webbanhang.dto.response.UserResponse;
import com.example.webbanhang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users          - ADMIN: lấy danh sách tất cả user
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll()));
    }

    /**
     * GET /api/users/{id}     - ADMIN: xem thông tin user bất kỳ
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    /**
     * GET /api/users/me       - USER: xem thông tin chính mình
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.getById(userId)));
    }

    /**
     * PUT /api/users/me       - USER: cập nhật thông tin cá nhân
     * Body: { fullName, phone, address, password }
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @RequestBody UpdateUserRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công",
                userService.update(userId, request)));
    }

    /**
     * DELETE /api/users/{id}  - ADMIN: xóa user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa user thành công", null));
    }
}