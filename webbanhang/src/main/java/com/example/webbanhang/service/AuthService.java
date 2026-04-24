package com.example.webbanhang.service;

import com.example.webbanhang.dto.request.LoginRequest;
import com.example.webbanhang.dto.request.RegisterRequest;
import com.example.webbanhang.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}