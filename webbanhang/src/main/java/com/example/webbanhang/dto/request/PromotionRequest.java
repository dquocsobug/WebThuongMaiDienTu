package com.example.webbanhang.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {
    @NotBlank(message = "Tên khuyến mãi không được để trống")
    private String promotionName;

    @NotNull
    @Min(value = 1, message = "Phần trăm giảm giá tối thiểu là 1")
    @Max(value = 100, message = "Phần trăm giảm giá tối đa là 100")
    private Integer discountPercent;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;
}