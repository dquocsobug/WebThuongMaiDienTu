package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.AdminUpdateUserRequest;
import com.example.webbanhang.dto.request.UpdateProfileRequest;
import com.example.webbanhang.dto.response.PageResponse;
import com.example.webbanhang.dto.response.UserResponse;
import com.example.webbanhang.enums.Role;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /** Lấy thông tin cá nhân của user đang đăng nhập. */
    UserResponse getMyProfile(Integer userId);

    /** Cập nhật thông tin cá nhân. */
    UserResponse updateMyProfile(Integer userId, UpdateProfileRequest request);

    /** [ADMIN] Lấy danh sách user, hỗ trợ filter theo keyword + role. */
    PageResponse<UserResponse> getAllUsers(String keyword, Role role, Pageable pageable);

    /** [ADMIN] Lấy chi tiết một user. */
    UserResponse getUserById(Integer userId);

    /** [ADMIN] Cập nhật thông tin + role của user. */
    UserResponse adminUpdateUser(Integer userId, AdminUpdateUserRequest request);

    /** [ADMIN] Xóa user (soft delete hoặc hard delete tùy yêu cầu). */
    void deleteUser(Integer userId);

    /** [ADMIN] Nâng cấp LOYAL_CUSTOMER hàng loạt (chạy scheduled hoặc thủ công). */
    int upgradeLoyalCustomers();
}