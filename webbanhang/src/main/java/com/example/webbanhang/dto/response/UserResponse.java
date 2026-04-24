package com.example.webbanhang.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UserResponse {
    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private LocalDateTime createdAt;
}