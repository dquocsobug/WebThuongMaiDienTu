package com.example.webbanhang.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private Integer productId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer rating;

    private String comment;
}