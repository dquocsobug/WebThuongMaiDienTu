package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.LoginRequest;
import com.example.webbanhang.dto.request.RegisterRequest;
import com.example.webbanhang.dto.response.AuthResponse;
import com.example.webbanhang.entity.User;
import com.example.webbanhang.repository.UserRepository;
import com.example.webbanhang.security.JwtUtil;
import com.example.webbanhang.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email hoặc mật khẩu không đúng");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getUserId());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .userId(user.getUserId())
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role("USER")
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getUserId());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .userId(user.getUserId())
                .build();
    }
}