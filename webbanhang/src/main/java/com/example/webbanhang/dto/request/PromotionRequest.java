package com.example.webbanhang.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PromotionRequest {

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Size(max = 255, message = "Tên khuyến mãi tối đa 255 ký tự")
    private String promotionName;

    @NotNull(message = "Phần trăm giảm không được để trống")
    @Min(value = 0,   message = "Phần trăm giảm không được âm")
    @Max(value = 100, message = "Phần trăm giảm tối đa 100%")
    private Integer discountPercent;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}