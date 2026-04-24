package com.example.webbanhang.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ReviewResponse {
    private Integer reviewId;
    private Integer userId;
    private String userFullName;
    private Integer productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}