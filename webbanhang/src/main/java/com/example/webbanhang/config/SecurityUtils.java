package com.example.webbanhang.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Lấy userId của user đang đăng nhập.
     * JwtAuthFilter lưu userId vào credentials của Authentication.
     */
    public static Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object credentials = token.getCredentials();
            if (credentials instanceof Integer) {
                return (Integer) credentials;
            }
        }
        throw new SecurityException("Không thể xác định người dùng hiện tại");
    }

    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}