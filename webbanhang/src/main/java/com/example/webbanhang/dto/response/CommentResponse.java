package com.example.webbanhang.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CommentResponse {
    private Integer commentId;
    private Integer userId;
    private String userFullName;
    private Integer productId;
    private String content;
    private LocalDateTime createdAt;
}