package com.example.webbanhang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull
    private Integer productId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
}