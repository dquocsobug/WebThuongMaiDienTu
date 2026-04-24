package com.example.webbanhang.dto.response;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
    private Integer userId;
}