package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.UpdateUserRequest;
import com.example.webbanhang.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse getById(Integer id);
    UserResponse getByEmail(String email);
    List<UserResponse> getAll();
    UserResponse update(Integer id, UpdateUserRequest request);
    void delete(Integer id);
}