package com.example.webbanhang.service.impl;

import com.example.webbanhang.dto.request.ChangePasswordRequest;
import com.example.webbanhang.dto.request.LoginRequest;
import com.example.webbanhang.dto.request.RegisterRequest;
import com.example.webbanhang.dto.response.AuthResponse;
import com.example.webbanhang.entity.Cart;
import com.example.webbanhang.entity.User;
import com.example.webbanhang.exception.BadRequestException;
import com.example.webbanhang.exception.ConflictException;
import com.example.webbanhang.exception.ResourceNotFoundException;
import com.example.webbanhang.repository.CartRepository;
import com.example.webbanhang.repository.UserRepository;
import com.example.webbanhang.security.JwtTokenProvider;
import com.example.webbanhang.security.UserPrincipal;
import com.example.webbanhang.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final CartRepository        cartRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;

    // ── Register ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Kiểm tra email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        // Kiểm tra confirmPassword
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        // Tạo user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .build(); // role mặc định = USER
        userRepository.save(user);

        // Tự động tạo Cart cho user mới
        Cart cart = Cart.builder().user(user).build();
        cartRepository.save(cart);

        log.info("[Auth] User đăng ký thành công: {}", user.getEmail());

        // Sinh JWT
        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtTokenProvider.generateToken(principal);

        return AuthResponse.of(token, user.getUserId(),
                user.getFullName(), user.getEmail(), user.getRole());
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // AuthenticationManager gọi UserDetailsServiceImpl → ném exception nếu sai
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);

        log.info("[Auth] User đăng nhập: {}", principal.getEmail());

        // Lấy thêm role đầy đủ từ DB (principal chỉ có authorities string)
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getEmail()));

        return AuthResponse.of(token, principal.getUserId(),
                principal.getFullName(), principal.getEmail(), user.getRole());
    }

    // ── Change Password ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Mật khẩu mới xác nhận không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("[Auth] User {} đổi mật khẩu thành công", userId);
    }
}