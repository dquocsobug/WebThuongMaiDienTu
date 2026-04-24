package com.example.webbanhang.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class PostResponse {
    private Integer postId;
    private String title;
    private String content;
    private String imageUrl;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}